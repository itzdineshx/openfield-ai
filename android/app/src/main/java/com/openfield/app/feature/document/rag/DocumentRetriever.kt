package com.openfield.app.feature.document.rag

import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.entity.DocumentChunkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RetrievedChunk(
    val pageNumber: Int,
    val chunkIndex: Int,
    val text: String,
    val relevanceScore: Float = 1.0f
)

class DocumentRetriever(private val documentDao: DocumentDao) {

    suspend fun retrieveRelevantChunks(
        documentId: String,
        query: String,
        maxChunks: Int = 4
    ): List<RetrievedChunk> = withContext(Dispatchers.IO) {
        val cleanTerms = sanitizeQuery(query)

        if (cleanTerms.isNotEmpty()) {
            val ftsQuery = cleanTerms.joinToString(" OR ") { "$it*" }
            try {
                val ftsResults = documentDao.searchChunksInDocument(documentId, ftsQuery, limit = maxChunks)
                if (ftsResults.isNotEmpty()) {
                    return@withContext ftsResults.map { chunk ->
                        RetrievedChunk(
                            pageNumber = chunk.pageNumber,
                            chunkIndex = chunk.chunkIndex,
                            text = chunk.text
                        )
                    }
                }
            } catch (e: Exception) {
                // FTS syntax error or no match; fallback to lexical scoring
            }
        }

        // Fallback: Lexical scoring against all chunks in the document
        val allChunks = documentDao.getChunksForDocument(documentId)
        if (allChunks.isEmpty()) return@withContext emptyList()

        val queryWords = query.lowercase().split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length > 2 }
            .toSet()

        if (queryWords.isEmpty()) {
            return@withContext allChunks.take(maxChunks).map {
                RetrievedChunk(pageNumber = it.pageNumber, chunkIndex = it.chunkIndex, text = it.text)
            }
        }

        val scoredChunks = allChunks.map { chunk ->
            val chunkLower = chunk.text.lowercase()
            var matches = 0
            for (word in queryWords) {
                if (chunkLower.contains(word)) {
                    matches++
                }
            }
            val score = matches.toFloat() / queryWords.size.toFloat()
            chunk to score
        }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(maxChunks)

        if (scoredChunks.isNotEmpty()) {
            scoredChunks.map { (chunk, score) ->
                RetrievedChunk(
                    pageNumber = chunk.pageNumber,
                    chunkIndex = chunk.chunkIndex,
                    text = chunk.text,
                    relevanceScore = score
                )
            }
        } else {
            // If no match found, take the first 2 chunks (usually introductory/overview)
            allChunks.take(2).map {
                RetrievedChunk(pageNumber = it.pageNumber, chunkIndex = it.chunkIndex, text = it.text)
            }
        }
    }

    private fun sanitizeQuery(rawQuery: String): List<String> {
        return rawQuery.lowercase()
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

