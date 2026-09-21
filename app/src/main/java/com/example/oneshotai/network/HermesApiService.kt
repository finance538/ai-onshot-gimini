package com.example.oneshotai.network

import com.example.oneshotai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
private data class HermesProxyRequest(val message: String)

@Serializable
private data class HermesProxyResponse(
    val text: String? = null,
    val error: String? = null,
    val agent: String? = null,
    val contextId: String? = null,
    val taskId: String? = null,
    val state: String? = null
)

object HermesClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(130, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendTask(message: String): String = withContext(Dispatchers.IO) {
        val url = BuildConfig.HERMES_PROXY_URL.trim()
        require(url.isNotEmpty()) { "Hermes proxy URL is not configured." }

        val payload = json.encodeToString(
            HermesProxyRequest.serializer(),
            HermesProxyRequest(message = message)
        )

        val request = Request.Builder()
            .url(url)
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val parsed = runCatching {
                json.decodeFromString(HermesProxyResponse.serializer(), raw)
            }.getOrNull()

            if (!response.isSuccessful) {
                throw IllegalStateException(parsed?.error ?: "Hermes request failed (" + response.code + ").")
            }

            val text = parsed?.text?.trim().orEmpty()
            if (text.isEmpty()) {
                throw IllegalStateException(parsed?.error ?: "Hermes returned an empty response.")
            }
            text
        }
    }
}
