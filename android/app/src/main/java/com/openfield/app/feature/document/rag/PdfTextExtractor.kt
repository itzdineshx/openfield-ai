package com.openfield.app.feature.document.rag

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.openfield.app.core.util.PerformanceTracker
import com.openfield.app.feature.document.ocr.LocalOcrEngine
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Locale

data class ExtractedPage(
    val pageNumber: Int,
    val text: String
)

data class DocumentExtractionResult(
    val fileName: String,
    val formattedSize: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val pages: List<ExtractedPage>,
    val isScanned: Boolean,
    val totalWordCount: Int
)

class PdfTextExtractor(private val context: Context) {

    private val ocrEngine by lazy { LocalOcrEngine(context) }

    init {
        try {
            PDFBoxResourceLoader.init(context.applicationContext)
        } catch (ignored: Exception) {}
    }

    suspend fun extractDocument(
        uri: Uri,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): Result<DocumentExtractionResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            var fileName = "Document_${System.currentTimeMillis()}"
            var sizeBytes = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) cursor.getString(nameIndex)?.let { fileName = it }
                    if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                }
            }

            val formattedSize = if (sizeBytes > 1024 * 1024) {
                String.format(Locale.getDefault(), "%.1f MB", sizeBytes / (1024.0 * 1024.0))
            } else {
                String.format(Locale.getDefault(), "%.1f KB", (sizeBytes / 1024.0).coerceAtLeast(1.0))
            }

            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isPdf = fileName.endsWith(".pdf", ignoreCase = true) || mimeType.contains("pdf", ignoreCase = true)

            val extractionResult = if (isPdf) {
                extractPdf(uri, fileName, formattedSize, sizeBytes, onProgress)
            } else {
                extractPlainText(uri, fileName, formattedSize, sizeBytes)
            }
            val elapsed = System.currentTimeMillis() - startTime
            PerformanceTracker.recordDocumentExtraction(elapsed)
            extractionResult
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            PerformanceTracker.recordDocumentExtraction(elapsed)
            Result.failure(e)
        }
    }

    private suspend fun extractPdf(
        uri: Uri,
        fileName: String,
        formattedSize: String,
        sizeBytes: Long,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): Result<DocumentExtractionResult> {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: return Result.failure(IllegalStateException("Unable to open stream for $uri"))

        return inputStream.use { stream ->
            PDDocument.load(stream).use { pdDocument ->
                val pageCount = pdDocument.numberOfPages
                val pages = mutableListOf<ExtractedPage>()
                val stripper = PDFTextStripper()
                var totalChars = 0
                var totalWords = 0

                for (pageIndex in 1..pageCount) {
                    currentCoroutineContext().ensureActive()
                    onProgress?.invoke(pageIndex, pageCount)

                    stripper.startPage = pageIndex
                    stripper.endPage = pageIndex
                    var pageText = stripper.getText(pdDocument).trim()

                    // If page has insufficient digital text, run memory-bounded on-device OCR
                    if (pageText.length < 30) {
                        val ocrText = ocrEngine.extractTextFromPdfPage(uri, pageIndex - 1)
                        if (!ocrText.isNullOrBlank()) {
                            pageText = ocrText.trim()
                        }
                    }

                    totalChars += pageText.length
                    val words = pageText.split(Regex("\\s+")).count { it.isNotBlank() }
                    totalWords += words

                    pages.add(ExtractedPage(pageNumber = pageIndex, text = pageText))
                }

                // If total characters extracted across all pages is under 30, it is likely an image-only / scanned PDF
                val isScanned = (totalChars < 30 && pageCount > 0)

                Result.success(
                    DocumentExtractionResult(
                        fileName = fileName,
                        formattedSize = formattedSize,
                        sizeBytes = sizeBytes,
                        pageCount = pageCount,
                        pages = pages,
                        isScanned = isScanned,
                        totalWordCount = totalWords
                    )
                )
            }
        }
    }

    private fun extractPlainText(
        uri: Uri,
        fileName: String,
        formattedSize: String,
        sizeBytes: Long
    ): Result<DocumentExtractionResult> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return Result.failure(IllegalStateException("Unable to open stream for $uri"))

        return inputStream.use { stream ->
            val text = stream.bufferedReader().readText()
            val words = text.split(Regex("\\s+")).count { it.isNotBlank() }
            Result.success(
                DocumentExtractionResult(
                    fileName = fileName,
                    formattedSize = formattedSize,
                    sizeBytes = sizeBytes,
                    pageCount = 1,
                    pages = listOf(ExtractedPage(pageNumber = 1, text = text)),
                    isScanned = false,
                    totalWordCount = words
                )
            )
        }
    }
}
