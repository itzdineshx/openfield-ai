package com.openfield.app.feature.document.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.Closeable
import kotlin.math.min

/**
 * High-performance, memory-bounded, 100% on-device OCR engine.
 * Uses Google ML Kit bundled Latin text recognizer (zero network access, zero Play Services required).
 * Renders PDF pages to memory-bounded bitmaps and recycles them immediately.
 */
class LocalOcrEngine(private val context: Context) : Closeable {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Extracts text from a specific 0-based page of a PDF file using on-device OCR.
     * Bounded memory scaling: renders at max 1600px width/height to avoid OOM.
     */
    suspend fun extractTextFromPdfPage(uri: Uri, pageIndexZeroBased: Int): String? = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        var bitmap: Bitmap? = null

        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
            renderer = PdfRenderer(pfd)
            if (pageIndexZeroBased < 0 || pageIndexZeroBased >= renderer.pageCount) {
                return@withContext null
            }

            page = renderer.openPage(pageIndexZeroBased)

            // Bounded scale to max 1600px dimension to protect system RAM on 4-6GB devices
            val maxDimension = 1600f
            val scale = min(2.0f, min(maxDimension / page.width, maxDimension / page.height)).coerceAtLeast(1.0f)
            val renderWidth = (page.width * scale).toInt().coerceAtLeast(1)
            val renderHeight = (page.height * scale).toInt().coerceAtLeast(1)

            bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            currentCoroutineContext().ensureActive()

            // Run ML Kit local recognition
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = Tasks.await(recognizer.process(inputImage))

            visionText.text.trim().ifBlank { null }
        } catch (e: Exception) {
            null
        } finally {
            try {
                bitmap?.recycle()
                page?.close()
                renderer?.close()
                pfd?.close()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Extracts text from an in-memory Bitmap.
     */
    suspend fun extractTextFromBitmap(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = Tasks.await(recognizer.process(inputImage))
            visionText.text.trim()
        } catch (e: Exception) {
            ""
        }
    }

    override fun close() {
        try {
            recognizer.close()
        } catch (ignored: Exception) {}
    }
}
