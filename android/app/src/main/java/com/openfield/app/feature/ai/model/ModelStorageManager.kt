package com.openfield.app.feature.ai.model

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

class ModelStorageManager(private val context: Context) {

    val modelsDir: File by lazy {
        File(context.filesDir, "models").apply {
            if (!exists()) mkdirs()
        }
    }

    fun getModelFile(fileName: String = ModelCatalog.GEMMA_4_E2B.fileName): File {
        return File(modelsDir, fileName)
    }

    fun isModelFilePresent(
        fileName: String = ModelCatalog.GEMMA_4_E2B.fileName,
        minSizeBytes: Long = 1_500_000_000L // Min 1.5 GB for valid Gemma 4 E2B
    ): Boolean {
        val file = getModelFile(fileName)
        return file.exists() && file.isFile && file.length() >= minSizeBytes && file.extension == "litertlm"
    }

    fun listInstalledModelFiles(): List<File> {
        return modelsDir.listFiles { file ->
            file.isFile &&
                file.extension.equals("litertlm", ignoreCase = true) &&
                !file.name.endsWith(".download", ignoreCase = true) &&
                !file.name.endsWith(".importing", ignoreCase = true) &&
                file.length() >= 100_000_000L
        }?.toList() ?: emptyList()
    }

    fun getAvailableDiskSpaceBytes(): Long {
        return try {
            val stat = StatFs(context.filesDir.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            0L
        }
    }

    fun hasEnoughSpaceFor(sizeBytes: Long, safetyBufferBytes: Long = 500 * 1024 * 1024L): Boolean {
        return getAvailableDiskSpaceBytes() >= (sizeBytes + safetyBufferBytes)
    }

    suspend fun importModelFromUri(
        uri: Uri,
        onProgress: (bytesCopied: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            var originalName = ""
            var reportedSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) cursor.getString(nameIndex)?.let { originalName = it }
                    if (sizeIndex != -1) reportedSize = cursor.getLong(sizeIndex)
                }
            }

            // Strictly require .litertlm extension - do not accept .bin or rename
            if (!originalName.endsWith(".litertlm", ignoreCase = true)) {
                return@withContext Result.failure(
                    IllegalArgumentException("Invalid artifact: '$originalName'. Crux requires the official Gemma 4 E2B .litertlm artifact (gemma-4-E2B-it.litertlm). Arbitrary .bin or other model formats are not supported.")
                )
            }

            // Validate file size (Gemma 4 E2B is ~2.58 GB, must be at least 1.5 GB)
            val minExpectedBytes = 1_500_000_000L
            if (reportedSize in 1 until minExpectedBytes) {
                return@withContext Result.failure(
                    IllegalArgumentException("File is too small (${reportedSize / (1024 * 1024)} MB). A genuine Gemma 4 E2B IT artifact is approximately 2.58 GB.")
                )
            }

            if (reportedSize > 0 && !hasEnoughSpaceFor(reportedSize)) {
                return@withContext Result.failure(
                    IllegalStateException("Insufficient storage space. Available: ${getAvailableDiskSpaceBytes() / (1024 * 1024)} MB, Required: ${reportedSize / (1024 * 1024)} MB + 500 MB buffer.")
                )
            }

            val targetFile = File(modelsDir, ModelCatalog.GEMMA_4_E2B.fileName)
            val tempFile = File(modelsDir, "${ModelCatalog.GEMMA_4_E2B.fileName}.importing")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var totalCopied = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalCopied += bytesRead
                        onProgress(totalCopied, reportedSize)
                    }
                    outputStream.flush()
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open input stream from selected URI"))

            if (tempFile.length() < minExpectedBytes) {
                val sizeCopied = tempFile.length()
                tempFile.delete()
                return@withContext Result.failure(
                    IllegalStateException("Imported model file size (${sizeCopied / (1024 * 1024)} MB) is below the minimum threshold for Gemma 4 E2B IT (~2.58 GB). The file may be truncated.")
                )
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifySha256(file: File, expectedSha256: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val hashBytes = digest.digest()
            val hexString = hashBytes.joinToString("") { "%02x".format(it) }
            hexString.equals(expectedSha256.trim(), ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    fun deleteModelFile(fileName: String = ModelCatalog.GEMMA_4_E2B.fileName): Boolean {
        val file = getModelFile(fileName)
        return if (file.exists()) file.delete() else false
    }
}
