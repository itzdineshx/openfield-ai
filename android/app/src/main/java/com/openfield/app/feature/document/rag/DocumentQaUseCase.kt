package com.openfield.app.feature.document.rag

import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.feature.ai.engine.AiGenerationConfig
import com.openfield.app.feature.ai.engine.LocalAiEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class DocumentQaResult(
    val stream: Flow<String>,
    val citedPages: List<Int>
)

class DocumentQaUseCase(
    private val documentDao: DocumentDao,
    private val retriever: DocumentRetriever,
    private val aiEngine: LocalAiEngine
) {

    fun askQuestionStream(
        documentId: String,
        question: String
    ): Flow<String> = flow {
        val document = documentDao.getDocument(documentId)
        if (document == null) {
            emit("Error: Document not found in local library.")
            return@flow
        }

        if (document.isScanned) {
            emit("Note: This document appears to be a scanned image or contains no selectable text. Optical Character Recognition (OCR) is required to index scanned documents.")
            return@flow
        }

        val chunks = retriever.retrieveRelevantChunks(documentId, question, maxChunks = 4)
        if (chunks.isEmpty()) {
            emit("No relevant sections found in '${document.name}' to answer this question.")
            return@flow
        }

        val citedPages = chunks.map { it.pageNumber }.distinct().sorted()

        val contextBuilder = StringBuilder()
        for (chunk in chunks) {
            contextBuilder.append("[Page ${chunk.pageNumber}]:\n")
            contextBuilder.append(chunk.text.trim())
            contextBuilder.append("\n\n")
        }

        val prompt = """
Answer the following question about the document '${document.name}' using ONLY the provided excerpts.

RULES:
1. Ground your answer strictly on the provided excerpts.
2. State the relevant page number (e.g. Page ${citedPages.firstOrNull() ?: 1}) where the answer is found.
3. If the answer cannot be found in these excerpts, say: "The provided pages do not contain information to answer this question."

DOCUMENT EXCERPTS:
$contextBuilder
QUESTION:
$question

ANSWER:
""".trimIndent()

        val config = AiGenerationConfig(
            temperature = 0.3, // Lower temperature for faithful grounded RAG
            topK = 30,
            topP = 0.85,
            systemPrompt = "You are OpenField's offline document assistant. Provide clear, precise, and factually grounded answers with page citations based strictly on the document text provided."
        )

        aiEngine.generateStream(prompt, config).collect { token ->
            emit(token)
        }
    }
}

