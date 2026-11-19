package com.openfield.app.feature.ai.controller

import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.model.KnowledgeCollection
import com.openfield.app.core.model.KnowledgeEvidence
import com.openfield.app.core.model.KnowledgeSource
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.PromptBuilder
import com.openfield.app.feature.document.rag.CitationValidator
import com.openfield.app.feature.document.rag.CrossSourceRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Manages cross-source multi-document reasoning sessions, citation verification, and evidence inspection.
 */
class CrossSourceQaController(
    private val crossSourceRetriever: CrossSourceRetriever,
    private val orchestrator: AiOrchestrator,
    private val scope: CoroutineScope
) {

    private val _selectedSources = MutableStateFlow<List<KnowledgeSource>>(emptyList())
    val selectedSources: StateFlow<List<KnowledgeSource>> = _selectedSources.asStateFlow()

    private val _activeCollection = MutableStateFlow<KnowledgeCollection?>(null)
    val activeCollection: StateFlow<KnowledgeCollection?> = _activeCollection.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentEvidence = MutableStateFlow<List<KnowledgeEvidence>>(emptyList())
    val currentEvidence: StateFlow<List<KnowledgeEvidence>> = _currentEvidence.asStateFlow()

    private val _showEvidenceExplorer = MutableStateFlow(false)
    val showEvidenceExplorer: StateFlow<Boolean> = _showEvidenceExplorer.asStateFlow()

    private var activeJob: Job? = null

    fun setScope(sources: List<KnowledgeSource>, collection: KnowledgeCollection? = null) {
        _selectedSources.value = sources
        _activeCollection.value = collection
        _messages.value = emptyList()
        _currentEvidence.value = emptyList()
    }

    fun toggleEvidenceExplorer(show: Boolean) {
        _showEvidenceExplorer.value = show
    }

    fun askQuestion(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = trimmed,
            timestamp = "Just now"
        )
        val assistantMessageId = UUID.randomUUID().toString()
        val pendingAssistantMessage = ChatMessage(
            id = assistantMessageId,
            isUser = false,
            text = "Searching across ${selectedSources.value.size} sources…",
            timestamp = "Just now"
        )

        _messages.value = _messages.value + userMessage + pendingAssistantMessage
        _isGenerating.value = true

        activeJob = scope.launch {
            try {
                val sourceIds = _selectedSources.value.map { it.id }
                val evidence = crossSourceRetriever.retrieveEvidence(trimmed, sourceIds)
                _currentEvidence.value = evidence

                val scopeTitle = _activeCollection.value?.name ?: "Selected Sources (${_selectedSources.value.size})"
                val prompt = PromptBuilder.buildCrossSourceQaPrompt(
                    query = trimmed,
                    evidenceList = evidence,
                    scopeName = scopeTitle
                )

                var accumulatedText = ""
                orchestrator.streamCrossSourceQa(prompt).collect { token ->
                    accumulatedText += token
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == assistantMessageId) {
                            msg.copy(text = accumulatedText.ifBlank { "Analyzing evidence…" })
                        } else msg
                    }
                }

                // Post-generation citation verification
                val validation = CitationValidator.validate(accumulatedText, evidence)
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == assistantMessageId) {
                        msg.copy(text = accumulatedText)
                    } else msg
                }
            } catch (e: Exception) {
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == assistantMessageId) {
                        msg.copy(text = "Unable to complete cross-source query: ${e.message}")
                    } else msg
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        _isGenerating.value = false
    }
}
