package com.openfield.app.feature.ai.model

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class DownloadProgress(
    val model: ModelInfo,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val progressPercent: Int,
    val speedBytesPerSec: Long = 0L
)

class ModelDownloadManager(private val storageManager: ModelStorageManager) {

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    @Volatile
    private var cancelRequested: Boolean = false

    suspend fun downloadModel(
        model: ModelInfo,
        onProgress: (DownloadProgress) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        cancelRequested = false
        val targetFile = storageManager.getModelFile(model.fileName)
        val tempFile = File(storageManager.modelsDir, "${model.fileName}.download")

        if (!storageManager.hasEnoughSpaceFor(model.sizeBytes)) {
            return@withContext Result.failure(
                IllegalStateException("Insufficient disk space. Needed: ${model.formattedSize}")
            )
        }

        // Resume support — if partial download exists, pick up from there
        val existingBytes = if (tempFile.exists() && tempFile.isFile) tempFile.length() else 0L

        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            // Follow redirects manually so we can honor cross-protocol redirects (http -> https) that
            // HttpURLConnection otherwise drops silently.
            val finalUrl = followRedirects(model.downloadUrl)

            connection = (URL(finalUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 120_000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "OpenField-Android/1.0")
                setRequestProperty("Accept", "application/octet-stream, */*")
                if (existingBytes > 0) {
                    setRequestProperty("Range", "bytes=$existingBytes-")
                }
            }

            val responseCode = connection.responseCode
            val isPartial = responseCode == HttpURLConnection.HTTP_PARTIAL
            val isOk = responseCode == HttpURLConnection.HTTP_OK

            if (!isOk && !isPartial) {
                return@withContext Result.failure(
                    IllegalStateException("Model download failed (HTTP $responseCode). Check network connection and model URL.")
                )
            }

            // If server ignored Range and returned full stream, restart from zero
            val startBytes = if (isPartial) existingBytes else 0L
            if (!isPartial && existingBytes > 0) {
                tempFile.delete()
            }

            val contentLengthHeader = connection.contentLengthLong
            val totalLength = when {
                isPartial && contentLengthHeader > 0 -> contentLengthHeader + startBytes
                contentLengthHeader > 0 -> contentLengthHeader
                else -> model.sizeBytes
            }

            inputStream = connection.inputStream
            outputStream = FileOutputStream(tempFile, isPartial)

            val buffer = ByteArray(128 * 1024)
            var bytesRead: Int
            var totalBytesRead = startBytes
            var lastEmittedMs = 0L
            var lastEmittedBytes = startBytes
            var lastPercent = -1

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (cancelRequested) {
                    throw CancellationException("Model download cancelled by user")
                }
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                val now = System.currentTimeMillis()
                val percent = if (totalLength > 0) {
                    ((totalBytesRead * 100) / totalLength).toInt().coerceIn(0, 100)
                } else 0

                // Throttle progress updates: every 500 ms OR 1% change — keeps UI thread calm
                if (now - lastEmittedMs >= 500 || percent != lastPercent) {
                    val elapsed = (now - lastEmittedMs).coerceAtLeast(1L)
                    val speed = if (lastEmittedMs == 0L) 0L
                        else ((totalBytesRead - lastEmittedBytes) * 1000L) / elapsed

                    val progress = DownloadProgress(
                        model = model,
                        bytesDownloaded = totalBytesRead,
                        totalBytes = totalLength,
                        progressPercent = percent,
                        speedBytesPerSec = speed
                    )
                    _downloadProgress.value = progress
                    try { onProgress(progress) } catch (ignored: Throwable) {}
                    lastEmittedMs = now
                    lastEmittedBytes = totalBytesRead
                    lastPercent = percent
                }
            }

            outputStream.flush()
            outputStream.fd.sync()

            // Minimum size guard — real LiteRT models are hundreds of MB; reject error HTML or LFS pointer files
            val minExpected = (model.sizeBytes / 2L).coerceAtLeast(10_000_000L)
            if (tempFile.length() < minExpected) {
                val got = tempFile.length()
                tempFile.delete()
                return@withContext Result.failure(
                    IllegalStateException(
                        "Downloaded file is too small (${got / (1024 * 1024)} MB, expected ~${model.sizeBytes / (1024 * 1024)} MB). The URL may require authentication, be rate limited, or the connection dropped."
                    )
                )
            }

            if (model.sha256 != null) {
                val isValid = storageManager.verifySha256(tempFile, model.sha256)
                if (!isValid) {
                    tempFile.delete()
                    return@withContext Result.failure(
                        IllegalStateException("SHA-256 integrity check failed. The downloaded file is corrupted.")
                    )
                }
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            _downloadProgress.value = null
            Result.success(targetFile)
        } catch (e: CancellationException) {
            _downloadProgress.value = null
            throw e
        } catch (e: OutOfMemoryError) {
            tempFile.delete()
            _downloadProgress.value = null
            Result.failure(IllegalStateException("Device ran out of memory while downloading the model."))
        } catch (e: Throwable) {
            _downloadProgress.value = null
            Result.failure(e)
        } finally {
            try { outputStream?.close() } catch (ignored: Throwable) {}
            try { inputStream?.close() } catch (ignored: Throwable) {}
            try { connection?.disconnect() } catch (ignored: Throwable) {}
        }
    }

    fun cancelActiveDownload() {
        cancelRequested = true
        _downloadProgress.value = null
    }

    /**
     * Manually chases redirects up to 5 hops so http→https hand-offs (which
     * HttpURLConnection silently drops) resolve. Returns the final URL as a string.
     */
    private fun followRedirects(initialUrl: String, maxHops: Int = 5): String {
        var current = initialUrl
        var hops = 0
        while (hops < maxHops) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 30_000
                readTimeout = 30_000
                requestMethod = "HEAD"
                setRequestProperty("User-Agent", "OpenField-Android/1.0")
            }
            val code = try { conn.responseCode } catch (t: Throwable) { -1 }
            val location = conn.getHeaderField("Location")
            conn.disconnect()
            if (code in 300..399 && !location.isNullOrBlank()) {
                current = if (location.startsWith("http", ignoreCase = true)) {
                    location
                } else {
                    URL(URL(current), location).toString()
                }
                hops++
            } else {
                return current
            }
        }
        return current
    }
}
