package com.openfield.app.feature.ai.controller

import android.content.Context
import android.net.Uri
import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.model.DocumentItem
import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.entity.DocumentEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.document.rag.DocumentChunker
import com.openfield.app.feature.document.rag.DocumentRetriever
import com.openfield.app.feature.document.rag.PdfTextExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class DocumentIndexingState(
    val isIndexing: Boolean = false,
    val documentName: String = "",
    val currentStep: String = "",
    val processedPages: Int = 0,
    val totalPages: Int = 0,
    val progressFraction: Float = 0f,
    val error: String? = null
)

class DocumentQaController(
    private val documentDao: DocumentDao,
    private val retriever: DocumentRetriever,
    private val pdfExtractor: PdfTextExtractor,
    private val chunker: DocumentChunker,
    private val orchestrator: AiOrchestrator,
    private val scope: CoroutineScope
) {
    val documents: StateFlow<List<DocumentItem>> = documentDao.getDocumentsFlow()
        .map { entities ->
            entities.map { entity ->
                DocumentItem(
                    id = entity.id,
                    name = entity.name,
                    size = entity.formattedSize,
                    pageCount = entity.pageCount,
                    summary = entity.summary,
                    keyPoints = listOf(
                        "Pages: ${entity.pageCount}",
                        if (entity.isScanned) "Scanned document (OCR required)" else "Indexed locally via FTS4"
                    ),
                    sampleQuestions = emptyList(),
                    isCustomImport = true
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _activeDocumentId = MutableStateFlow<String?>(null)
    val activeDocumentId: StateFlow<String?> = _activeDocumentId.asStateFlow()

    private val _selectedDocumentIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedDocumentIds: StateFlow<Set<String>> = _selectedDocumentIds.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isQuerying = MutableStateFlow(false)
    val isQuerying: StateFlow<Boolean> = _isQuerying.asStateFlow()

    private val _indexingState = MutableStateFlow(DocumentIndexingState())
    val indexingState: StateFlow<DocumentIndexingState> = _indexingState.asStateFlow()

    private var activeIndexingJob: Job? = null

    fun selectDocument(documentId: String) {
        _activeDocumentId.value = documentId
    }

    fun toggleDocumentSelection(documentId: String) {
        val current = _selectedDocumentIds.value.toMutableSet()
        if (current.contains(documentId)) {
            current.remove(documentId)
        } else {
            current.add(documentId)
        }
        _selectedDocumentIds.value = current
        if (current.size == 1) {
            _activeDocumentId.value = current.first()
        }
    }

    fun selectAllDocuments() {
        val allIds = documents.value.map { it.id }.toSet()
        _selectedDocumentIds.value = allIds
    }

    fun clearDocumentSelection() {
        _selectedDocumentIds.value = emptySet()
    }

    fun cancelIndexing() {
        activeIndexingJob?.cancel()
        activeIndexingJob = null
        _indexingState.value = DocumentIndexingState()
    }

    fun importPdf(context: Context, uri: Uri) {
        cancelIndexing()
        activeIndexingJob = scope.launch(Dispatchers.IO) {
            _indexingState.value = DocumentIndexingState(
                isIndexing = true,
                documentName = "Opening document...",
                currentStep = "Reading document pages..."
            )
            try {
                val result = pdfExtractor.extractDocument(uri) { current, total ->
                    _indexingState.update { currentSt ->
                        currentSt.copy(
                            processedPages = current,
                            totalPages = total,
                            progressFraction = if (total > 0) current.toFloat() / total.toFloat() else 0f,
                            currentStep = "Extracting page $current of $total…"
                        )
                    }
                }
                result.fold(
                    onSuccess = { extracted ->
                        _indexingState.update { it.copy(
                            documentName = extracted.fileName,
                            currentStep = "Analyzing headings & building chunks…"
                        ) }
                        val docId = UUID.randomUUID().toString()
                        val entity = DocumentEntity(
                            id = docId,
                            name = extracted.fileName,
                            uriString = uri.toString(),
                            sizeBytes = extracted.sizeBytes,
                            formattedSize = extracted.formattedSize,
                            pageCount = extracted.pageCount,
                            summary = "Document: ${extracted.fileName} (${extracted.pageCount} pages, ${extracted.totalWordCount} words)",
                            isScanned = extracted.isScanned,
                            createdAt = System.currentTimeMillis()
                        )
                        documentDao.insertDocument(entity)

                        val chunks = chunker.chunkDocument(docId, extracted.pages)
                        if (chunks.isNotEmpty()) {
                            _indexingState.update { it.copy(currentStep = "Writing SQLite FTS4 index (${chunks.size} chunks)…") }
                            documentDao.insertChunks(chunks)
                        }

                        _activeDocumentId.value = docId
                        _selectedDocumentIds.value = setOf(docId)
                        _indexingState.value = DocumentIndexingState()
                    },
                    onFailure = { error ->
                        _indexingState.value = DocumentIndexingState(
                            isIndexing = false,
                            error = error.message ?: "Failed to import document"
                        )
                    }
                )
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    _indexingState.value = DocumentIndexingState()
                } else {
                    _indexingState.value = DocumentIndexingState(
                        isIndexing = false,
                        error = e.message ?: "Extraction failed"
                    )
                }
            }
        }
    }

    fun reindexDocument(context: Context, documentId: String) {
        cancelIndexing()
        activeIndexingJob = scope.launch(Dispatchers.IO) {
            val doc = documentDao.getDocument(documentId) ?: return@launch
            val uri = Uri.parse(doc.uriString)
            _indexingState.value = DocumentIndexingState(
                isIndexing = true,
                documentName = doc.name,
                currentStep = "Re-indexing: Reading document pages…"
            )
            try {
                // Delete previous chunks to prevent stale / duplicated FTS4 tokens
                documentDao.deleteChunksForDocument(documentId)

                val result = pdfExtractor.extractDocument(uri) { current, total ->
                    _indexingState.update { currentSt ->
                        currentSt.copy(
                            processedPages = current,
                            totalPages = total,
                            progressFraction = if (total > 0) current.toFloat() / total.toFloat() else 0f,
                            currentStep = "Re-extracting page $current of $total…"
                        )
                    }
                }
                result.onSuccess { extracted ->
                    _indexingState.update { it.copy(currentStep = "Rebuilding heading-aware chunks…") }
                    val chunks = chunker.chunkDocument(documentId, extracted.pages)
                    if (chunks.isNotEmpty()) {
                        documentDao.insertChunks(chunks)
                    }
                    _indexingState.value = DocumentIndexingState()
                }.onFailure { error ->
                    _indexingState.value = DocumentIndexingState(
                        isIndexing = false,
                        error = "Re-indexing failed: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                _indexingState.value = DocumentIndexingState()
            }
        }
    }

    fun askQuestion(question: String) {
        if (question.isBlank()) return

        val selectedScope = _selectedDocumentIds.value
        val targetDocIds = if (selectedScope.isNotEmpty()) {
            selectedScope.toList()
        } else {
            listOfNotNull(_activeDocumentId.value ?: documents.value.firstOrNull()?.id)
        }

        if (targetDocIds.isEmpty()) {
            addAssistantMessage("No document is selected. Please import or select a document first.")
            return
        }

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = question.trim(),
            timestamp = "Just now"
        )
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Searching document excerpts…",
            timestamp = "Just now",
            isComplete = false
        )

        _messages.value = _messages.value + listOf(userMsg, assistantMsg)

        scope.launch(Dispatchers.IO) {
            _isQuerying.value = true
            try {
                if (targetDocIds.size == 1) {
                    val docId = targetDocIds.first()
                    val doc = documentDao.getDocument(docId)
                    if (doc == null) {
                        updateAssistantMessage(assistantMsgId, "Error: Document not found in local library.", isComplete = true)
                        return@launch
                    }

                    if (doc.isScanned) {
                        updateAssistantMessage(
                            assistantMsgId,
                            "Notice: '${doc.name}' appears to be a scanned image without selectable text. Text extraction requires an OCR pass.",
                            isComplete = true
                        )
                        return@launch
                    }

                    val chunks = retriever.retrieveRelevantChunks(docId, question, maxChunks = 4)
                    if (chunks.isEmpty()) {
                        updateAssistantMessage(
                            assistantMsgId,
                            "The document '${doc.name}' does not contain enough relevant information to answer this question.",
                            isComplete = true
                        )
                        return@launch
                    }

                    val citedPages = chunks.map { it.pageNumber }.distinct().sorted()
                    val pageLabel = "Page ${citedPages.joinToString(", ")}"
                    val excerptsWithPages = chunks.map { it.pageNumber to it.text }

                    var accumulated = ""
                    orchestrator.streamDocumentQa(doc.name, excerptsWithPages, question).collect { token ->
                        accumulated += token
                        updateAssistantMessage(assistantMsgId, accumulated, isComplete = false, sourcePage = pageLabel)
                    }
                    updateAssistantMessage(assistantMsgId, accumulated, isComplete = true, sourcePage = pageLabel)
                } else {
                    // Multi-document search scope
                    val chunks = retriever.retrieveRelevantChunksAcrossDocuments(targetDocIds, question, maxChunks = 6)
                    if (chunks.isEmpty()) {
                        updateAssistantMessage(
                            assistantMsgId,
                            "The selected ${targetDocIds.size} documents do not contain enough relevant information to answer this question.",
                            isComplete = true
                        )
                        return@launch
                    }

                    val citedPages = chunks.map { it.pageNumber }.distinct().sorted()
                    val pageLabel = "Multi-doc (Pages ${citedPages.joinToString(", ")})"
                    val excerptsWithPages = chunks.map { it.pageNumber to it.text }

                    var accumulated = ""
                    orchestrator.streamDocumentQa("${targetDocIds.size} selected documents", excerptsWithPages, question).collect { token ->
                        accumulated += token
                        updateAssistantMessage(assistantMsgId, accumulated, isComplete = false, sourcePage = pageLabel)
                    }
                    updateAssistantMessage(assistantMsgId, accumulated, isComplete = true, sourcePage = pageLabel)
                }
            } catch (e: Exception) {
                updateAssistantMessage(assistantMsgId, "Error answering question: ${e.message}", isComplete = true)
            } finally {
                _isQuerying.value = false
            }
        }
    }

    fun summarizeDocument(documentId: String) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = "Summarize document",
            timestamp = "Just now"
        )
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Reading document overview excerpts…",
            timestamp = "Just now",
            isComplete = false
        )

        _messages.value = _messages.value + listOf(userMsg, assistantMsg)

        scope.launch(Dispatchers.IO) {
            _isQuerying.value = true
            try {
                val doc = documentDao.getDocument(documentId) ?: return@launch
                val chunks = documentDao.getChunksForDocument(documentId).take(5)
                if (chunks.isEmpty()) {
                    updateAssistantMessage(assistantMsgId, "No indexed text found to summarize.", isComplete = true)
                    return@launch
                }

                val excerptsWithPages = chunks.map { it.pageNumber to it.text }
                var accumulated = ""
                orchestrator.streamDocumentSummary(doc.name, excerptsWithPages).collect { token ->
                    accumulated += token
                    updateAssistantMessage(assistantMsgId, accumulated, isComplete = false, sourcePage = "Pages 1-${chunks.last().pageNumber}")
                }
                updateAssistantMessage(assistantMsgId, accumulated, isComplete = true, sourcePage = "Pages 1-${chunks.last().pageNumber}")
            } catch (e: Exception) {
                updateAssistantMessage(assistantMsgId, "Summary error: ${e.message}", isComplete = true)
            } finally {
                _isQuerying.value = false
            }
        }
    }

    fun compareDocuments(docIdA: String, docIdB: String, focusQuery: String? = null) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = "Compare selected documents${if (!focusQuery.isNullOrBlank()) " on \"$focusQuery\"" else ""}",
            timestamp = "Just now"
        )
        val assistantMsgId = UUID.randomUUID().toString()
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "Analyzing comparative passages…",
            timestamp = "Just now",
            isComplete = false
        )

        _messages.value = _messages.value + listOf(userMsg, assistantMsg)

        scope.launch(Dispatchers.IO) {
            _isQuerying.value = true
            try {
                val docA = documentDao.getDocument(docIdA)
                val docB = documentDao.getDocument(docIdB)
                if (docA == null || docB == null) {
                    updateAssistantMessage(assistantMsgId, "Could not load both documents for comparison.", isComplete = true)
                    return@launch
                }

                val query = focusQuery ?: "core concepts principles differences"
                val chunksA = retriever.retrieveRelevantChunks(docIdA, query, maxChunks = 3)
                val chunksB = retriever.retrieveRelevantChunks(docIdB, query, maxChunks = 3)

                val excerptsA = chunksA.map { it.pageNumber to it.text }
                val excerptsB = chunksB.map { it.pageNumber to it.text }

                var accumulated = ""
                orchestrator.streamDocumentComparison(docA.name, docB.name, excerptsA, excerptsB, focusQuery).collect { token ->
                    accumulated += token
                    updateAssistantMessage(assistantMsgId, accumulated, isComplete = false, sourcePage = "${docA.name} & ${docB.name}")
                }
                updateAssistantMessage(assistantMsgId, accumulated, isComplete = true, sourcePage = "${docA.name} & ${docB.name}")
            } catch (e: Exception) {
                updateAssistantMessage(assistantMsgId, "Comparison error: ${e.message}", isComplete = true)
            } finally {
                _isQuerying.value = false
            }
        }
    }

    private fun addAssistantMessage(text: String) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = false,
            text = text,
            timestamp = "Just now",
            isComplete = true
        )
        _messages.value = _messages.value + msg
    }

    private fun updateAssistantMessage(id: String, text: String, isComplete: Boolean, sourcePage: String? = null) {
        val current = _messages.value.toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            current[idx] = current[idx].copy(
                text = text,
                isComplete = isComplete,
                sourcePage = sourcePage ?: current[idx].sourcePage
            )
            _messages.value = current
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}
