package com.openfield.app.feature.ai.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.util.PerformanceTracker
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.ImageAnalysisSections
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageAnalysisController(
    private val orchestrator: AiOrchestrator,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null
    private var prepJob: Job? = null

    @Volatile
    private var analysisRun = 0

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    @Volatile
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

    private val _isAnswering = MutableStateFlow(false)
    val isAnswering: StateFlow<Boolean> = _isAnswering.asStateFlow()

    fun setImage(context: Context, uri: Uri) {
        stopAnalysis()
        analysisRun++
        _isAnalyzing.value = false
        _selectedImageUri.value = uri
        localImagePath = null
        _analysisSections.value = null
        _rawAnalysisText.value = ""
        _errorMessage.value = null
        _followUpMessages.value = emptyList()

        val appContext = context.applicationContext
        prepJob?.cancel()
        prepJob = scope.launch(Dispatchers.IO) {
            val prepStart = System.currentTimeMillis()
            try {
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, boundsOptions)
                }

                if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                    _errorMessage.value = "This file isn't a supported image. Try a JPG or PNG photo."
                    return@launch
                }

                val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
                var sampleSize = 1
                while (maxDim / sampleSize > 2048) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                val bitmap = appContext.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, decodeOptions)
                }

                if (bitmap == null) {
                    _errorMessage.value = "Couldn't read this image. Try another photo."
                    return@launch
                }

                val tempFile = File(appContext.cacheDir, "vision_${System.currentTimeMillis()}.jpg")
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                bitmap.recycle()

                localImagePath = tempFile.absolutePath
                PerformanceTracker.recordImagePreprocessing(System.currentTimeMillis() - prepStart)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = "Couldn't process this image: ${e.message}"
            }
        }
    }

    fun analyzeImage(userQuestion: String? = null) {
        if (_selectedImageUri.value == null) {
            _errorMessage.value = "Pick or take a photo first."
            return
        }

        activeJob?.cancel()
        val runId = ++analysisRun
        _isAnalyzing.value = true
        _errorMessage.value = null
        _rawAnalysisText.value = ""
        _analysisSections.value = null
        _followUpMessages.value = emptyList()

        activeJob = scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            fun isCurrent() = runId == analysisRun
            try {
                prepJob?.join()
                val path = localImagePath
                if (path == null) {
                    if (_errorMessage.value == null) _errorMessage.value = "The image couldn't be prepared. Try another photo."
                    return@launch
                }
                if (!orchestrator.awaitReady()) {
                    _errorMessage.value = "The on-device AI model isn't loaded. Open AI Model Management from the Home banner."
                    return@launch
                }

                var lastEmit = 0L
                orchestrator.streamImageAnalysis(path, userQuestion?.takeIf { it.isNotBlank() }).collect { token ->
                    buffer.append(token)
                    val now = System.currentTimeMillis()
                    if (now - lastEmit >= 60 && isCurrent()) {
                        lastEmit = now
                        _rawAnalysisText.value = buffer.toString()
                    }
                }
            } catch (e: CancellationException) {
                // Stopped: keep the partial analysis below.
            } catch (e: Exception) {
                if (isCurrent()) _errorMessage.value = "Image analysis failed: ${e.message}"
            } finally {
                if (!isCurrent()) return@launch
                val fullText = buffer.toString().trim()
                _rawAnalysisText.value = fullText
                if (fullText.isNotEmpty()) {
                    _analysisSections.value = StructuredOutputParser.parseImageSections(fullText)
                }
                _isAnalyzing.value = false
            }
        }
    }

    fun askFollowUp(question: String) {
        val path = localImagePath
        if (path == null || question.isBlank() || _isAnswering.value || _isAnalyzing.value) return

        val assistantMsgId = UUID.randomUUID().toString()
        _followUpMessages.update {
            it + listOf(
                ChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = question.trim(), timestamp = "Just now"),
                ChatMessage(id = assistantMsgId, isUser = false, text = "", timestamp = "Just now", isComplete = false)
            )
        }
        _isAnswering.value = true

        scope.launch(Dispatchers.IO) {
            val buffer = StringBuilder()
            try {
                val priorSummary = _analysisSections.value?.let { s ->
                    buildString {
                        append("Subject: ").append(s.subject).append('\n')
                        append(Markdown.toPlainText(s.whatISee).take(400))
                        if (s.importantDetails.isNotEmpty()) {
                            append("\nDetails: ").append(s.importantDetails.take(5).joinToString("; ") { Markdown.stripInline(it) })
                        }
                    }
                }
                var lastEmit = 0L
                orchestrator.streamImageFollowUp(path, question, priorSummary).collect { token ->
                    buffer.append(token)
                    val now = System.currentTimeMillis()
                    if (now - lastEmit >= 60) {
                        lastEmit = now
                        updateAssistantFollowUp(assistantMsgId, buffer.toString(), isComplete = false)
                    }
                }
            } catch (e: CancellationException) {
                // keep partial answer
            } catch (e: Exception) {
                if (buffer.isBlank()) buffer.append("Sorry, I couldn't answer that: ${e.message}")
            } finally {
                updateAssistantFollowUp(assistantMsgId, buffer.toString().trim().ifEmpty { "_Stopped._" }, isComplete = true)
                _isAnswering.value = false
            }
        }
    }

    private fun updateAssistantFollowUp(id: String, text: String, isComplete: Boolean) {
        _followUpMessages.update { list ->
            list.map { if (it.id == id) it.copy(text = text, isComplete = isComplete) else it }
        }
    }

    fun shareAnalysis(context: Context) {
        val s = _analysisSections.value ?: return
        val text = buildString {
            append(s.subject).append("\n\n")
            append(Markdown.toPlainText(_rawAnalysisText.value))
            append("\n\n— Analyzed offline with CRUX")
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, s.subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "Share analysis").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun stopAnalysis() {
        if (_isAnalyzing.value || _isAnswering.value) orchestrator.cancelGeneration()
        activeJob?.cancel()
    }

    fun reset() {
        stopAnalysis()
        analysisRun++
        prepJob?.cancel()
        _selectedImageUri.value = null
        localImagePath = null
        _analysisSections.value = null
        _rawAnalysisText.value = ""
        _errorMessage.value = null
        _followUpMessages.value = emptyList()
        _isAnalyzing.value = false
    }
}
