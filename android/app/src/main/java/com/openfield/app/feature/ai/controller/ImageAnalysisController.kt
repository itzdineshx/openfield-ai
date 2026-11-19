package com.openfield.app.feature.ai.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.util.PerformanceTracker
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.ImageAnalysisSections
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageAnalysisController(
    private val orchestrator: AiOrchestrator,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private var localImagePath: String? = null

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisSections = MutableStateFlow<ImageAnalysisSections?>(null)
    val analysisSections: StateFlow<ImageAnalysisSections?> = _analysisSections.asStateFlow()

    private val _rawAnalysisText = MutableStateFlow("")
    val rawAnalysisText: StateFlow<String> = _rawAnalysisText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _followUpMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val followUpMessages: StateFlow<List<ChatMessage>> = _followUpMessages.asStateFlow()

    fun setImage(context: Context, uri: Uri) {
        _selectedImageUri.value = uri
        _analysisSections.value = null
        _rawAnalysisText.value = ""
        _errorMessage.value = null
        _followUpMessages.value = emptyList()

        scope.launch(Dispatchers.IO) {
            val prepStart = System.currentTimeMillis()
            try {
                // Validate format and dimensions
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, boundsOptions)
                }

                if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                    _errorMessage.value = "Selected file is not a supported or valid image format."
                    return@launch
                }

                // Compute downsample ratio for large images (> 2048px) to prevent OOM
                val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
                var sampleSize = 1
                while (maxDim / sampleSize > 2048) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, decodeOptions)
                }

                if (bitmap == null) {
                    _errorMessage.value = "Failed to decode image data."
                    return@launch
                }

                val tempFile = File(context.cacheDir, "vision_${System.currentTimeMillis()}.jpg")
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                bitmap.recycle()

                localImagePath = tempFile.absolutePath
                val prepElapsed = System.currentTimeMillis() - prepStart
                PerformanceTracker.recordImagePreprocessing(prepElapsed)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process image: ${e.message}"
            }
        }
    }

    fun analyzeImage(userQuestion: String? = null) {
        val path = localImagePath
        if (path == null) {
            _errorMessage.value = "No image selected. Please pick or capture a photo first."
            return
        }

        if (!orchestrator.isReady()) {
            _errorMessage.value = "AI model not ready. Please verify Gemma 4 E2B is loaded."
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.IO) {
            _isAnalyzing.value = true
            _errorMessage.value = null
            _rawAnalysisText.value = ""

            val buffer = StringBuilder()
            try {
                orchestrator.streamImageAnalysis(path, userQuestion).collect { token ->
                    buffer.append(token)
                    _rawAnalysisText.value = buffer.toString()
                }

                val fullText = buffer.toString()
                val parsed = StructuredOutputParser.parseImageSections(fullText)
                _analysisSections.value = parsed
            } catch (e: Exception) {
                _errorMessage.value = "Image analysis error: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun askFollowUp(question: String) {
        val path = localImagePath
        if (path == null || question.isBlank()) return

        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()

        val userMsg = ChatMessage(
            id = userMsgId,
            isUser = true,
            text = question.trim(),
            timestamp = "Just now"
        )
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Thinking…",
            timestamp = "Just now",
            isComplete = false
        )

        _followUpMessages.value = _followUpMessages.value + listOf(userMsg, assistantMsg)

        scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                val priorSummary = _analysisSections.value?.let {
                    "Subject: ${it.subject}. Summary: ${it.whatISee.take(120)}"
                }
                orchestrator.streamImageFollowUp(path, question, priorSummary).collect { token ->
                    buffer.append(token)
                    updateAssistantFollowUp(assistantMsgId, buffer.toString(), isComplete = false)
                }
                updateAssistantFollowUp(assistantMsgId, buffer.toString(), isComplete = true)
            } catch (e: Exception) {
                val errorText = if (buffer.isNotBlank()) buffer.toString() else "Follow-up error: ${e.message}"
                updateAssistantFollowUp(assistantMsgId, errorText, isComplete = true)
            }
        }
    }

    private fun updateAssistantFollowUp(id: String, text: String, isComplete: Boolean) {
        val current = _followUpMessages.value.toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            current[idx] = current[idx].copy(text = text, isComplete = isComplete)
            _followUpMessages.value = current
        }
    }

    fun stopAnalysis() {
        orchestrator.cancelGeneration()
        activeJob?.cancel()
        _isAnalyzing.value = false
    }

    fun reset() {
        stopAnalysis()
        _selectedImageUri.value = null
        localImagePath = null
        _analysisSections.value = null
        _rawAnalysisText.value = ""
        _errorMessage.value = null
        _followUpMessages.value = emptyList()
    }
}
