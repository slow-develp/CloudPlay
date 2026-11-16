
package com.cloudplay.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

data class XboxGame(
    val productId: String,
    val title: String,
    val imageUrl: String,
    val description: String = ""
)

object XboxCatalog {

    private const val MARKET = "BR"
    private const val LANGUAGE = "pt-BR"

    private const val ALL_CLOUD_ID =
        "29a81209-df6f-41fd-a528-2ae6b91f719c"

    private const val RECENT_CLOUD_ID =
        "f13cf6b4-57e6-4459-89df-6aec18cf0538"

    private val executor = Executors.newFixedThreadPool(2)

    private fun request(
        url: String,
        method: String = "GET",
        body: String? = null
    ): String {
        val connection = URL(url).openConnection()
            as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            connection.setRequestProperty(
                "Accept",
                "application/json"
            )
            connection.setRequestProperty(
                "User-Agent",
                "CloudPlay Android"
            )

            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
                )

                OutputStreamWriter(
                    connection.outputStream,
                    Charsets.UTF_8
                ).use {
                    it.write(body)
                }
            }

            val status = connection.responseCode

            val stream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream.bufferedReader().use {
                it.readText()
            }

            if (status !in 200..299) {
                throw Exception(
                    "Erro HTTP $status: $response"
                )
            }

            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun getProductIds(
        listId: String
    ): List<String> {
        val url =
            "https://catalog.gamepass.com/sigls/v2" +
            "?id=$listId" +
            "&market=$MARKET" +
            "&language=$LANGUAGE"

        val response = JSONArray(request(url))
        val ids = mutableListOf<String>()

        for (i in 0 until response.length()) {
            val item = response.optJSONObject(i)
                ?: continue

            val id = item.optString("id")

            if (id.isNotBlank()) {
                ids.add(id)
            }
        }

        return ids.distinct()
    }

    private fun getProducts(
        ids: List<String>
    ): List<XboxGame> {
        val games = mutableListOf<XboxGame>()

        ids.chunked(20).forEach { chunk ->

            val productsJson = JSONArray()

            chunk.forEach { id ->
                productsJson.put(id)
            }

            val body = JSONObject().apply {
                put("Products", productsJson)
            }

            val url =
                "https://catalog.gamepass.com/v3/products" +
                "?market=$MARKET" +
                "&language=$LANGUAGE" +
                "&hydration=RemoteHighSapphire0"

            val response = JSONObject(
                request(
                    url = url,
                    method = "POST",
                    body = body.toString()
                )
            )

            chunk.forEach { id ->
                val product = response.optJSONObject(id)
                    ?: return@forEach

                val title = findTitle(product)

                if (title.isBlank()) {
                    return@forEach
                }

                games.add(
                    XboxGame(
                        productId = id,
                        title = title,
                        imageUrl = findImage(product),
                        description = findDescription(product)
                    )
                )
            }
        }

        return games
    }

    private fun findTitle(
        product: JSONObject
    ): String {
        product.optJSONArray("LocalizedProperties")
            ?.let { localized ->
                for (i in 0 until localized.length()) {
                    val item = localized.optJSONObject(i)
                        ?: continue

                    val title = item.optString("ProductTitle")

                    if (title.isNotBlank()) {
                        return title
                    }
                }
            }

        return product.optString("ProductTitle")
    }

    private fun findDescription(
        product: JSONObject
    ): String {
        product.optJSONArray("LocalizedProperties")
            ?.let { localized ->
                for (i in 0 until localized.length()) {
                    val item = localized.optJSONObject(i)
                        ?: continue

                    val description =
                        item.optString("ShortDescription")
                            .ifBlank {
                                item.optString("ProductDescription")
                            }

                    if (description.isNotBlank()) {
                        return description
                    }
                }
            }

        return ""
    }

    private fun findImage(
        product: JSONObject
    ): String {
        val localized =
            product.optJSONArray("LocalizedProperties")
                ?: return ""

        for (i in 0 until localized.length()) {
            val item = localized.optJSONObject(i)
                ?: continue

            val images = item.optJSONArray("Images")
                ?: continue

            for (j in 0 until images.length()) {
                val image = images.optJSONObject(j)
                    ?: continue

                val purpose = image.optString("ImagePurpose")
                val uri = image.optString("Uri")

                if (
                    uri.startsWith("https://") &&
                    (
                        purpose.contains("Poster", true) ||
                        purpose.contains("Tile", true) ||
                        purpose.contains("BoxArt", true)
                    )
                ) {
                    return uri
                }
            }
        }

        return ""
    }

    fun loadCloudGames(
        onSuccess: (List<XboxGame>) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            try {
                val ids = getProductIds(ALL_CLOUD_ID)
                val games = getProducts(ids)

                onSuccess(games)
            } catch (error: Exception) {
                onError(
                    error.message ?: "Não foi possível carregar o catálogo."
                )
            }
        }
    }

    fun loadRecentlyAdded(
        onSuccess: (List<XboxGame>) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            try {
                val ids = getProductIds(RECENT_CLOUD_ID)
                val games = getProducts(ids)

                onSuccess(games)
            } catch (error: Exception) {
                onError(
                    error.message ?: "Não foi possível carregar as novidades."
                )
            }
        }
    }
}
