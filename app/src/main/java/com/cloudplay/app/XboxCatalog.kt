
package com.cloudplay.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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

    private const val CATALOG_URL =
        "https://catalog.gamepass.com/v3/products"

    private const val LIST_URL =
        "https://catalog.gamepass.com/sigls/v2"

    fun loadCloudGames(): List<XboxGame> {
        return loadGamesFromList(ALL_CLOUD_ID)
    }

    fun loadRecentlyAdded(): List<XboxGame> {
        return loadGamesFromList(RECENT_CLOUD_ID)
    }

    private fun loadGamesFromList(listId: String): List<XboxGame> {
        val ids = getProductIds(listId)

        if (ids.isEmpty()) {
            return emptyList()
        }

        return getProductDetails(ids)
    }

    private fun getProductIds(listId: String): List<String> {
        val connection = URL(
            "$LIST_URL?id=$listId&language=$LANGUAGE&market=$MARKET"
        ).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 20000

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
            )

            connection.setRequestProperty(
                "MS-CV",
                "0"
            )

            val responseCode = connection.responseCode

            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream?.bufferedReader()?.use {
                it.readText()
            } ?: ""

            if (responseCode !in 200..299) {
                throw Exception(
                    "Erro HTTP $responseCode ao consultar a lista: $response"
                )
            }

            val json = JSONArray(response)
            val ids = mutableListOf<String>()

            for (i in 0 until json.length()) {
                val item = json.opt(i)

                val id = when (item) {
                    is String -> item
                    is JSONObject -> item.optString("id")
                    else -> ""
                }

                if (id.isNotBlank() && id != "null") {
                    ids.add(id)
                }
            }

            return ids.distinct()

        } finally {
            connection.disconnect()
        }
    }

    private fun getProductDetails(
        productIds: List<String>
    ): List<XboxGame> {

        val connection = URL(
            "$CATALOG_URL?market=$MARKET&language=$LANGUAGE&hydration=RemoteHighSapphire0"
        ).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 25000
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json; charset=UTF-8"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
            )

            connection.setRequestProperty(
                "MS-CV",
                "0"
            )

            connection.setRequestProperty(
                "Calling-App-Name",
                "Xbox Cloud Gaming Web"
            )

            connection.setRequestProperty(
                "Calling-App-Version",
                "21.0.0"
            )

            val requestBody = JSONObject().apply {
                put("Products", JSONArray(productIds))
            }

            connection.outputStream.use { output ->
                output.write(
                    requestBody.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val responseCode = connection.responseCode

            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream?.bufferedReader()?.use {
                it.readText()
            } ?: ""

            if (responseCode !in 200..299) {
                throw Exception(
                    "Erro HTTP $responseCode ao consultar o catálogo: $response"
                )
            }

            return parseProducts(response)

        } finally {
            connection.disconnect()
        }
    }

    private fun parseProducts(response: String): List<XboxGame> {
        val result = mutableListOf<XboxGame>()
        val json = JSONObject(response)

        val products = json.optJSONObject("Products")
            ?: json

        val keys = products.keys()

        while (keys.hasNext()) {
            val productId = keys.next()
            val product = products.optJSONObject(productId) ?: continue

            val localizedProperties =
                product.optJSONArray("LocalizedProperties")

            if (localizedProperties == null ||
                localizedProperties.length() == 0
            ) {
                continue
            }

            val localized = localizedProperties.optJSONObject(0)
                ?: continue

            val title = localized.optString("ProductTitle")
                .ifBlank {
                    localized.optString("ShortTitle")
                }
                .ifBlank {
                    localized.optString("Title")
                }

            if (title.isBlank()) {
                continue
            }

            val description = localized.optString("ProductDescription")
                .ifBlank {
                    localized.optString("ShortDescription")
                }

            val imageUrl = findImage(localized)

            result.add(
                XboxGame(
                    productId = productId,
                    title = title,
                    imageUrl = imageUrl,
                    description = description
                )
            )
        }

        return result.distinctBy { it.productId }
    }

    private fun findImage(localized: JSONObject): String {
        val images = localized.optJSONArray("Images")
            ?: return ""

        var fallback = ""

        for (i in 0 until images.length()) {
            val image = images.optJSONObject(i) ?: continue

            val uri = image.optString("Uri")
                .ifBlank {
                    image.optString("URL")
                }

            if (uri.isBlank()) {
                continue
            }

            val purpose = image.optString("ImagePurpose")

            if (
                purpose.equals("Poster", true) ||
                purpose.equals("BoxArt", true)
            ) {
                return uri
            }

            if (fallback.isBlank()) {
                fallback = uri
            }
        }

        return fallback
    }
}
