package com.openfield.app.data.remote

import com.openfield.app.feature.library.KnowledgePackManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

sealed interface CatalogResult {
    data class Success(val items: List<KnowledgePackManifest>, val total: Int) : CatalogResult
    data class Error(val message: String, val isOffline: Boolean = false) : CatalogResult
}

/**
 * Remote API client for OpenField Knowledge Pack Catalog.
 * Network activity is strictly user-initiated.
 * Never transmits personal prompts, documents, or private device data.
 */
class CatalogApiClient(
    private val candidateUrls: List<String> = listOf("http://127.0.0.1:8000", "http://10.0.2.2:8000")
) {
    constructor(baseUrl: String) : this(listOf(baseUrl))

    suspend fun fetchCatalog(
        query: String? = null,
        category: String? = null,
        cursor: Int = 0,
        limit: Int = 20
    ): CatalogResult = withContext(Dispatchers.IO) {
        var lastError: CatalogResult.Error? = null
        for (baseUrl in candidateUrls) {
            var connection: HttpURLConnection? = null
            try {
                val queryParams = mutableListOf<String>()
                if (!query.isNullOrBlank()) queryParams.add("query=${query.trim()}")
                if (!category.isNullOrBlank()) queryParams.add("category=${category.trim()}")
                queryParams.add("cursor=$cursor")
                queryParams.add("limit=$limit")

                val urlString = "$baseUrl/api/v1/packs?${queryParams.joinToString("&")}"
                val url = URL(urlString)

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3000
                    readTimeout = 5000
                    setRequestProperty("Accept", "application/json")
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                    val json = JSONObject(responseText)
                    val itemsArray = json.getJSONArray("items")
                    val total = json.optInt("total", itemsArray.length())
                    val manifests = mutableListOf<KnowledgePackManifest>()

                    for (i in 0 until itemsArray.length()) {
                        val obj = itemsArray.getJSONObject(i)
                        val topicsArray = obj.optJSONArray("topics")
                        val topics = mutableListOf<String>()
                        if (topicsArray != null) {
                            for (t in 0 until topicsArray.length()) {
                                topics.add(topicsArray.getString(t))
                            }
                        }

                        manifests.add(
                            KnowledgePackManifest(
                                packId = obj.getString("pack_id"),
                                title = obj.getString("title"),
                                description = obj.getString("description"),
                                category = obj.getString("category"),
                                version = obj.getString("version"),
                                size = obj.getString("size"),
                                iconColorHex = obj.optLong("icon_color_hex", 0xFF1E88E5),
                                checksumSha256 = obj.getString("checksum_sha256"),
                                provenance = obj.optString("provenance", "Remote Catalog"),
                                topics = topics
                            )
                        )
                    }

                    return@withContext CatalogResult.Success(items = manifests, total = total)
                } else {
                    lastError = CatalogResult.Error("Catalog service returned HTTP $responseCode")
                }
            } catch (e: java.net.ConnectException) {
                lastError = CatalogResult.Error("Catalog service is offline or unreachable.", isOffline = true)
            } catch (e: java.net.UnknownHostException) {
                lastError = CatalogResult.Error("Unable to resolve catalog service host.", isOffline = true)
            } catch (e: java.net.SocketTimeoutException) {
                lastError = CatalogResult.Error("Catalog request timed out. Please check your connection.", isOffline = true)
            } catch (e: Exception) {
                lastError = CatalogResult.Error(e.message ?: "Failed to refresh knowledge pack catalog.")
            } finally {
                connection?.disconnect()
            }
        }
        return@withContext lastError ?: CatalogResult.Error("Catalog service is unreachable.", isOffline = true)
    }
}
