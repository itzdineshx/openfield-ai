package com.openfield.app.feature.ai.controller

import android.content.Context
import android.content.Intent
import com.openfield.app.core.text.Markdown
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.entity.WritingDraftEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.WritingIntent
import com.openfield.app.feature.ai.mode.WritingTone
import kotlinx.coroutines.CancellationException
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

    private val _previousContent = MutableStateFlow<String?>(null)
    val previousContent: StateFlow<String?> = _previousContent.asStateFlow()

    val usesSourceText: Boolean
        get() = _selectedIntent.value in SOURCE_INTENTS

    fun canGenerate(): Boolean =
        _promptInput.value.isNotBlank() || (usesSourceText && _sourceText.value.isNotBlank())

    fun generateDraft() {
        val source = _sourceText.value.trim().takeIf { usesSourceText && it.isNotBlank() }
        val prompt = _promptInput.value.trim().ifBlank {
            if (source != null) defaultInstruction(_selectedIntent.value) else ""
        }
        if (prompt.isBlank()) {
            _errorMessage.value = "Describe what you'd like to write first."
            return
        }

        runGeneration(clearFirst = true) {
            orchestrator.streamWritingDraft(
                intent = _selectedIntent.value,
                tone = _selectedTone.value,
                instruction = prompt,
                sourceText = source
            )
        }
    }

    fun refineContent(refinementType: String) {
        val current = _documentContent.value.trim()
        if (current.isBlank() || _isGenerating.value) return
        runGeneration(clearFirst = false) { orchestrator.streamWritingRefine(refinementType, current) }
    }

    @Volatile
    private var generationRun = 0

    private fun runGeneration(clearFirst: Boolean, stream: () -> kotlinx.coroutines.flow.Flow<String>) {
        val original = _documentContent.value
        activeJob?.cancel()
        val runId = ++generationRun
        _isGenerating.value = true
        _errorMessage.value = null
        if (original.isNotBlank()) _previousContent.value = original
        if (clearFirst) _documentContent.value = ""

        activeJob = scope.launch(Dispatchers.IO) {
            if (!orchestrator.awaitReady()) {
                _errorMessage.value = "The on-device AI model isn't loaded. Open AI Model Management from the Home banner."
                _documentContent.value = original
                _isGenerating.value = false
                return@launch
            }

            val buffer = StringBuilder()
            var lastEmit = 0L
            try {
                stream().collect { token ->
                    buffer.append(token)
                    val now = System.currentTimeMillis()
                    if (now - lastEmit >= 60 && runId == generationRun) {
                        lastEmit = now
                        _documentContent.value = buffer.toString()
                    }
                }
            } catch (e: CancellationException) {
                // keep the partial text
            } catch (e: Exception) {
                _errorMessage.value = "Writing failed: ${e.message}"
            } finally {
                if (runId != generationRun) return@launch
                val cleaned = cleanGeneratedText(buffer.toString())
                _documentContent.value = cleaned.ifBlank { original }
                if (cleaned.isNotBlank()) persistDraft()
                _isGenerating.value = false
            }
        }
    }

    fun undoLastChange() {
        val previous = _previousContent.value ?: return
        _previousContent.value = _documentContent.value.takeIf { it.isNotBlank() }
        _documentContent.value = previous
    }

    fun saveDraft() {
        if (_documentContent.value.isNotBlank()) persistDraft()
    }

    fun stopGeneration() {
        if (_isGenerating.value) orchestrator.cancelGeneration()
        activeJob?.cancel()
    }

    private fun defaultInstruction(intent: WritingIntent): String = when (intent) {
        WritingIntent.GRAMMAR -> "Correct all grammar, spelling and punctuation in the source text without changing its meaning."
        WritingIntent.REWRITE -> "Rewrite and polish the source text so it reads clearly and naturally."
        WritingIntent.SUMMARY -> "Summarize the source text into its key points."
        WritingIntent.TONE_CHANGE -> "Rewrite the source text in the selected tone."
        else -> "Write this based on the source text."
    }

    companion object {
        val SOURCE_INTENTS = setOf(WritingIntent.REWRITE, WritingIntent.GRAMMAR, WritingIntent.SUMMARY, WritingIntent.TONE_CHANGE)

        private val preambleRegex = Regex(
            "^(sure|certainly|of course|okay|ok|absolutely|here is|here's|here are|below is|revised content|revised text|draft)\\b[^\\n]*:\\s*\\n",
            RegexOption.IGNORE_CASE
        )
        private val trailingRemarkRegex = Regex(
            "\\n\\s*(let me know|i hope this|feel free to|hope this helps)[^\\n]*$",
            RegexOption.IGNORE_CASE
        )

        fun cleanGeneratedText(raw: String): String {
            var text = raw.trim()
            val fence = Regex("^```[a-zA-Z]*\\s*\\n([\\s\\S]*?)\\n```$").find(text)
            if (fence != null) text = fence.groupValues[1].trim()
            text = text.replace(preambleRegex, "").trimStart()
            text = text.replace(Regex("^(REVISED CONTENT|REVISED TEXT)\\s*:?\\s*", RegexOption.IGNORE_CASE), "")
            text = text.replace(trailingRemarkRegex, "").trimEnd()
            return text
        }
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

    fun plainTextContent(): String = Markdown.toPlainText(_documentContent.value)

    fun shareDraft(context: Context, format: DraftExportFormat = DraftExportFormat.PLAIN_TEXT) {
        val content = if (format == DraftExportFormat.PLAIN_TEXT) plainTextContent() else _documentContent.value
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
        generationRun++
        _isGenerating.value = false
        _promptInput.value = ""
        _sourceText.value = ""
        _documentContent.value = ""
        _previousContent.value = null
        _errorMessage.value = null
        _documentTitle.value = _selectedIntent.value.defaultTitle
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
