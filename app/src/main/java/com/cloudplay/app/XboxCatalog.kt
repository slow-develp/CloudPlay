package com.cloudplay.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Callable
import java.util.concurrent.Executors

data class XboxGame(
    val productId: String,
    val title: String,
    val imageUrl: String,
    val description: String
)

object XboxCatalog {

    private const val MARKET = "BR"
    private const val LANGUAGE = "pt-BR"

    private const val ALL_CLOUD_ID =
        "29a81209-df6f-41fd-a528-2ae6b91f719c"
    private const val RECENT_CLOUD_ID =
        "f13cf6b4-57e6-4459-89df-6aec18cf0538"

    private const val LIST_URL =
        "https://catalog.gamepass.com/sigls/v2"
    private const val PRODUCT_URL =
        "https://displaycatalog.mp.microsoft.com/v7.0/products"

    private const val CONNECT_TIMEOUT = 10000
    private const val READ_TIMEOUT = 15000

    fun loadCloudGames(): List<XboxGame> =
        loadGamesFromList(ALL_CLOUD_ID)

    fun loadRecentlyAdded(): List<XboxGame> =
        loadGamesFromList(RECENT_CLOUD_ID)

    private fun loadGamesFromList(listId: String): List<XboxGame> {
        val ids = getProductIds(listId)
        if (ids.isEmpty()) {
            throw Exception("A lista do Xbox não retornou nenhum ID de jogo.")
        }
        return getProductDetails(ids)
    }

    private fun getProductIds(listId: String): List<String> {
        val url = URL(
            "$LIST_URL?id=$listId&language=$LANGUAGE&market=$MARKET"
        )
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                throw Exception("Erro HTTP $code na lista do Xbox.")
            }

            val json = JSONArray(response)
            val ids = mutableListOf<String>()

            for (i in 0 until json.length()) {
                val item = json.opt(i)
                val id = when (item) {
                    is String -> item
                    is JSONObject -> item.optString("id")
                        .ifBlank { item.optString("productId") }
                        .ifBlank { item.optString("ProductId") }
                    else -> ""
                }
                if (id.isNotBlank() && id != "null") ids.add(id)
            }
            return ids.distinct()
        } finally {
            connection.disconnect()
        }
    }

    /*
     * Consulta até três lotes simultaneamente.
     * Isso evita esperar cada lote terminar para iniciar o próximo.
     */
    private fun getProductDetails(productIds: List<String>): List<XboxGame> {
        val batches = productIds.distinct().chunked(20)
        val executor = Executors.newFixedThreadPool(3)
        val results = mutableListOf<XboxGame>()
        val errors = mutableListOf<String>()

        try {
            val tasks = batches.map { batch ->
                executor.submit(Callable { requestProductBatch(batch) })
            }

            tasks.forEach { task ->
                try {
                    results.addAll(task.get())
                } catch (e: Exception) {
                    errors.add(
                        e.cause?.localizedMessage
                            ?: e.localizedMessage
                            ?: "Erro ao consultar um lote."
                    )
                }
            }
        } finally {
            executor.shutdownNow()
        }

        val games = results.distinctBy { it.productId }
        if (games.isEmpty()) {
            throw Exception(
                errors.firstOrNull()
                    ?: "A Microsoft não retornou os detalhes dos produtos."
            )
        }
        return games
    }

    private fun requestProductBatch(productIds: List<String>): List<XboxGame> {
        val encodedIds = URLEncoder.encode(productIds.joinToString(","), "UTF-8")
        val url = URL(
            "$PRODUCT_URL?bigIds=$encodedIds" +
                "&market=$MARKET" +
                "&languages=$LANGUAGE" +
                "&actionFilter=Browse" +
                "&fieldsTemplate=Details"
        )
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.setRequestProperty("MS-CV", "0")

            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                throw Exception("Erro HTTP $code ao consultar produtos.")
            }
            return parseProducts(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseProducts(response: String): List<XboxGame> {
        val result = mutableListOf<XboxGame>()
        val products = JSONObject(response).optJSONArray("Products")
            ?: return emptyList()

        for (i in 0 until products.length()) {
            val product = products.optJSONObject(i) ?: continue
            val productId = product.optString("ProductId")
                .ifBlank { product.optString("StoreId") }

            if (productId.isBlank() || productId == "null") continue

            val localizedProperties =
                product.optJSONArray("LocalizedProperties") ?: continue
            if (localizedProperties.length() == 0) continue

            val localized = localizedProperties.optJSONObject(0) ?: continue
            val title = localized.optString("ProductTitle")
                .ifBlank { localized.optString("ShortTitle") }
                .ifBlank { localized.optString("Title") }

            if (title.isBlank()) continue

            val description = localized.optString("ProductDescription")
                .ifBlank { localized.optString("ShortDescription") }
                .ifBlank { "Explore este jogo no catálogo Xbox." }

            result.add(
                XboxGame(
                    productId = productId,
                    title = title,
                    imageUrl = findImage(localized),
                    description = description
                )
            )
        }
        return result.distinctBy { it.productId }
    }

    private fun findImage(localized: JSONObject): String {
        val images = localized.optJSONArray("Images") ?: return ""
        var poster = ""
        var boxArt = ""
        var fallback = ""

        for (i in 0 until images.length()) {
            val image = images.optJSONObject(i) ?: continue
            val uri = image.optString("Uri")
                .ifBlank { image.optString("URL") }

            if (uri.isBlank() || uri == "null") continue

            when {
                image.optString("ImagePurpose").equals("Poster", true) ->
                    poster = uri
                image.optString("ImagePurpose").equals("BoxArt", true) ->
                    boxArt = uri
                fallback.isBlank() -> fallback = uri
            }
        }
        return poster.ifBlank { boxArt }.ifBlank { fallback }
    }
}
