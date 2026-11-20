package com.openfield.app.feature.document.rag

import com.openfield.app.core.model.KnowledgeEvidence
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.core.util.PerformanceTracker
import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.dao.KnowledgePackDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Retrieves, ranks, and budgets evidence across multiple local sources for cross-source AI reasoning.
 */
class CrossSourceRetriever(
    private val documentDao: DocumentDao,
    private val knowledgePackDao: KnowledgePackDao
) {

    /**
     * Retrieves up to [maxTotalChunks] evidence passages across [sourceIds], ensuring no single source
     * starves others and context remains bounded to ~1,200 tokens.
     */
    suspend fun retrieveEvidence(
        query: String,
        sourceIds: List<String>,
        maxTotalChunks: Int = 6
    ): List<KnowledgeEvidence> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        if (sourceIds.isEmpty() || query.isBlank()) return@withContext emptyList()

        val cleanTerms = sanitizeQuery(query)
        val expandedTerms = RetrievalOptimizer.expandQueryTerms(cleanTerms)
        val ftsQuery = if (expandedTerms.isNotEmpty()) {
            expandedTerms.joinToString(" OR ") { "$it*" }
        } else query.trim()

        val evidenceBySource = mutableMapOf<String, MutableList<KnowledgeEvidence>>()
        val maxPerSource = (maxTotalChunks / sourceIds.size).coerceAtLeast(2)

        // 1. Retrieve from Documents
        val documents = sourceIds.mapNotNull { documentDao.getDocument(it) }
        for (doc in documents) {
            val chunks = try {
                documentDao.searchChunksInDocument(doc.id, ftsQuery, limit = maxPerSource)
            } catch (e: Exception) {
                emptyList()
            }

            val candidateChunks = if (chunks.isNotEmpty()) {
                chunks
            } else {
                documentDao.getChunksForDocument(doc.id).take(1)
            }

            val docEvidence = candidateChunks.take(maxPerSource).map { chunk ->
                val score = RetrievalOptimizer.scoreChunk(chunk.text, cleanTerms).coerceAtLeast(0.5f)
                KnowledgeEvidence(
                    evidenceId = "doc_${chunk.documentId}_${chunk.pageNumber}_${chunk.chunkIndex}",
                    sourceId = chunk.documentId,
                    sourceTitle = doc.name,
                    sourceType = KnowledgeSourceType.DOCUMENT,
                    pageNumber = chunk.pageNumber,
                    text = chunk.text,
                    relevanceScore = score
                )
            }.sortedByDescending { it.relevanceScore }

            if (docEvidence.isNotEmpty()) {
                evidenceBySource[doc.id] = docEvidence.toMutableList()
            }
        }

        // 2. Retrieve from Knowledge Packs
        val packs = sourceIds.mapNotNull { knowledgePackDao.getPackById(it) }.filter { it.isInstalled }
        for (pack in packs) {
            val packText = "${pack.description}\nTopics: ${pack.topicsJson}"
            val score = RetrievalOptimizer.scoreChunk(packText, cleanTerms).coerceAtLeast(0.8f)
            val packEvidence = mutableListOf(
                KnowledgeEvidence(
                    evidenceId = "pack_${pack.id}",
                    sourceId = pack.id,
                    sourceTitle = pack.title,
                    sourceType = KnowledgeSourceType.KNOWLEDGE_PACK,
                    section = pack.category,
                    text = packText,
                    relevanceScore = score
                )
            )
            evidenceBySource[pack.id] = packEvidence
        }

        val elapsed = System.currentTimeMillis() - startTime
        PerformanceTracker.recordRetrievalLatency(elapsed)

        // Fair-share interleave across disparate sources to preserve diversity within context budget
        RetrievalOptimizer.fairShareInterleave(evidenceBySource, maxTotalChunks)
    }

    private fun sanitizeQuery(raw: String): List<String> {
        return raw.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in STOP_WORDS }
    }

    companion object {
        private val STOP_WORDS = setOf(
            "the", "and", "is", "in", "it", "of", "to", "a", "an", "for", "with",
            "on", "at", "from", "by", "about", "what", "how", "when", "where", "who", "which"
        )
    }
}
