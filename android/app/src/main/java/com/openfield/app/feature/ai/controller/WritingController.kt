package com.openfield.app.feature.ai.controller

import android.content.Context
import android.content.Intent
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.entity.WritingDraftEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.WritingIntent
import com.openfield.app.feature.ai.mode.WritingTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class DraftExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    MARKDOWN("md", "text/markdown", "Markdown (.md)"),
    PLAIN_TEXT("txt", "text/plain", "Plain Text (.txt)")
}

class WritingController(
    private val orchestrator: AiOrchestrator,
    private val modeSessionDao: ModeSessionDao,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null
    private var currentDraftId: String = UUID.randomUUID().toString()

    private val _selectedIntent = MutableStateFlow(WritingIntent.EMAIL)
    val selectedIntent: StateFlow<WritingIntent> = _selectedIntent.asStateFlow()

    private val _selectedTone = MutableStateFlow(WritingTone.PROFESSIONAL)
    val selectedTone: StateFlow<WritingTone> = _selectedTone.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    private val _sourceText = MutableStateFlow("")
    val sourceText: StateFlow<String> = _sourceText.asStateFlow()

    private val _documentContent = MutableStateFlow("")
    val documentContent: StateFlow<String> = _documentContent.asStateFlow()

    private val _documentTitle = MutableStateFlow(WritingIntent.EMAIL.defaultTitle)
    val documentTitle: StateFlow<String> = _documentTitle.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val savedDrafts: StateFlow<List<WritingDraftEntity>> = modeSessionDao.getWritingDraftsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun loadDraft(draft: WritingDraftEntity) {
        currentDraftId = draft.id
        _documentTitle.value = draft.title
        _documentContent.value = draft.content
        _selectedIntent.value = try {
            WritingIntent.valueOf(draft.intent)
        } catch (e: Exception) {
            WritingIntent.EMAIL
        }
        _selectedTone.value = try {
            WritingTone.valueOf(draft.tone)
        } catch (e: Exception) {
            WritingTone.PROFESSIONAL
        }
        _promptInput.value = draft.title
        _errorMessage.value = null
    }

    fun deleteDraft(draftId: String) {
        scope.launch(Dispatchers.IO) {
            modeSessionDao.deleteWritingDraft(draftId)
            if (currentDraftId == draftId) {
                clearAll()
            }
        }
    }

    fun setIntent(intent: WritingIntent) {
        _selectedIntent.value = intent
        _documentTitle.value = intent.defaultTitle
    }

    fun setTone(tone: WritingTone) {
        _selectedTone.value = tone
    }

    fun setPrompt(text: String) {
        _promptInput.value = text
    }

    fun setSourceText(text: String) {
        _sourceText.value = text
    }

    fun updateDocumentContent(newContent: String) {
        _documentContent.value = newContent
    }

    fun updateDocumentTitle(newTitle: String) {
        _documentTitle.value = newTitle
    }

    fun generateDraft() {
        val prompt = _promptInput.value.trim()
        if (prompt.isBlank()) {
            _errorMessage.value = "Please provide instructions for what to write."
            return
        }

        if (!orchestrator.isReady()) {
            _errorMessage.value = "AI model is not ready. Please verify Gemma 4 E2B is loaded."
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            _errorMessage.value = null
            _documentContent.value = ""

            val buffer = StringBuilder()
            try {
                orchestrator.streamWritingDraft(
                    intent = _selectedIntent.value,
                    tone = _selectedTone.value,
                    instruction = prompt,
                    sourceText = _sourceText.value.takeIf { it.isNotBlank() }
                ).collect { token ->
                    buffer.append(token)
                    _documentContent.value = buffer.toString()
                }

                persistDraft()
            } catch (e: Exception) {
                _errorMessage.value = "Writing generation error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun refineContent(refinementType: String) {
        val current = _documentContent.value.trim()
        if (current.isBlank()) return

        if (!orchestrator.isReady()) {
            _errorMessage.value = "AI model is not ready."
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            _errorMessage.value = null

            val buffer = StringBuilder()
            try {
                orchestrator.streamWritingRefine(refinementType, current).collect { token ->
                    buffer.append(token)
                    _documentContent.value = buffer.toString()
                }

                persistDraft()
            } catch (e: Exception) {
                _errorMessage.value = "Refine error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun stopGeneration() {
        orchestrator.cancelGeneration()
        activeJob?.cancel()
        _isGenerating.value = false
    }

    fun exportDraftToFile(context: Context, format: DraftExportFormat): File? {
        val content = _documentContent.value
        if (content.isBlank()) return null
        return try {
            val safeTitle = _documentTitle.value.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "${safeTitle}_${System.currentTimeMillis()}.${format.extension}"
            val exportDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)
            file.writeText(content)
            file
        } catch (e: Exception) {
            _errorMessage.value = "Export failed: ${e.message}"
            null
        }
    }

    fun shareDraft(context: Context, format: DraftExportFormat = DraftExportFormat.PLAIN_TEXT) {
        val content = _documentContent.value
        if (content.isBlank()) return
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, _documentTitle.value)
                putExtra(Intent.EXTRA_SUBJECT, _documentTitle.value)
                putExtra(Intent.EXTRA_TEXT, content)
                type = format.mimeType
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Draft")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            _errorMessage.value = "Share failed: ${e.message}"
        }
    }

    fun clearAll() {
        stopGeneration()
        _promptInput.value = ""
        _sourceText.value = ""
        _documentContent.value = ""
        _errorMessage.value = null
        currentDraftId = UUID.randomUUID().toString()
    }

    private fun persistDraft() {
        scope.launch(Dispatchers.IO) {
            val content = _documentContent.value
            val words = content.split(Regex("\\s+")).count { it.isNotBlank() }
            val entity = WritingDraftEntity(
                id = currentDraftId,
                title = _documentTitle.value,
                intent = _selectedIntent.value.name,
                tone = _selectedTone.value.name,
                content = content,
                wordCount = words,
                updatedAt = System.currentTimeMillis()
            )
            try {
                modeSessionDao.insertWritingDraft(entity)
            } catch (ignored: Exception) {}
        }
    }
}
