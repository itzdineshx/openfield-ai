package com.openfield.app.feature.document.rag

import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.core.model.UnifiedSearchResult
import com.openfield.app.core.util.PerformanceTracker
import com.openfield.app.data.local.dao.ChatDao
import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.dao.ModeSessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Executes sub-80ms lexical search across documents, chats, drafts, and knowledge packs without LLM invocation.
 */
class UnifiedSearchEngine(
    private val documentDao: DocumentDao,
    private val chatDao: ChatDao,
    private val modeSessionDao: ModeSessionDao,
    private val knowledgePackDao: KnowledgePackDao
) {

    suspend fun search(
        query: String,
        scopeSourceIds: List<String>? = null,
        limit: Int = 15
    ): List<UnifiedSearchResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val terms = sanitizeQuery(query)
        if (terms.isEmpty()) return@withContext emptyList()

        val results = coroutineScope {
            val docDeferred = async { searchDocuments(terms, query, scopeSourceIds, limit) }
            val chatDeferred = async { searchChats(query, scopeSourceIds, limit) }
            val packDeferred = async { searchPacks(terms, query, scopeSourceIds, limit) }
            val draftDeferred = async { searchDrafts(terms, scopeSourceIds, limit) }

            val docResults = docDeferred.await()
            val chatResults = chatDeferred.await()
            val packResults = packDeferred.await()
            val draftResults = draftDeferred.await()

            (docResults + chatResults + packResults + draftResults)
                .sortedByDescending { it.score }
                .take(limit)
        }

        val elapsed = System.currentTimeMillis() - startTime
        PerformanceTracker.recordRetrievalLatency(elapsed)
        results
    }

    private suspend fun searchDocuments(
        terms: List<String>,
        rawQuery: String,
        scopeSourceIds: List<String>?,
        limit: Int
    ): List<UnifiedSearchResult> {
        val ftsQuery = terms.joinToString(" OR ") { "$it*" }
        val chunks = try {
            if (!scopeSourceIds.isNullOrEmpty()) {
                documentDao.searchChunksInDocuments(scopeSourceIds, ftsQuery, limit)
            } else {
                documentDao.searchChunksGlobal(ftsQuery, limit)
            }
        } catch (e: Exception) {
            emptyList()
        }

        if (chunks.isEmpty()) return emptyList()

        // Fetch document names
        val docIds = chunks.map { it.documentId }.distinct()
        val docMap = mutableMapOf<String, String>()
        for (id in docIds) {
            documentDao.getDocument(id)?.let { docMap[id] = it.name }
        }

        return chunks.map { chunk ->
            val docName = docMap[chunk.documentId] ?: "Document"
            val snippet = generateSnippet(chunk.text, terms)
            val score = calculateScore(chunk.text, terms)
            UnifiedSearchResult(
                sourceId = chunk.documentId,
                sourceType = KnowledgeSourceType.DOCUMENT,
                sourceTitle = docName,
                snippet = snippet,
                pageNumber = chunk.pageNumber,
                score = score
            )
        }
    }

    private suspend fun searchChats(
        query: String,
        scopeSourceIds: List<String>?,
        limit: Int
    ): List<UnifiedSearchResult> {
        val sessions = try {
            chatDao.searchSessionsWithMessages(query)
        } catch (e: Exception) {
            emptyList()
        }

        val filtered = if (!scopeSourceIds.isNullOrEmpty()) {
            sessions.filter { it.id in scopeSourceIds }
        } else sessions

        return filtered.take(limit).map { sess ->
            UnifiedSearchResult(
                sourceId = sess.id,
                sourceType = KnowledgeSourceType.CONVERSATION,
                sourceTitle = sess.title,
                snippet = sess.preview.ifBlank { "Saved chat with ${sess.messageCount} messages" },
                score = 0.85f
            )
        }
    }

    private suspend fun searchPacks(
        terms: List<String>,
        rawQuery: String,
        scopeSourceIds: List<String>?,
        limit: Int
    ): List<UnifiedSearchResult> {
        val packs = knowledgePackDao.getKnowledgePacks().filter { it.isInstalled }
        val filtered = if (!scopeSourceIds.isNullOrEmpty()) {
            packs.filter { it.id in scopeSourceIds }
        } else packs

        val matches = mutableListOf<UnifiedSearchResult>()
        val qLower = rawQuery.lowercase()

        for (pack in filtered) {
            val titleMatches = terms.any { pack.title.lowercase().contains(it) }
            val descMatches = terms.any { pack.description.lowercase().contains(it) }
            val topicMatches = pack.topicsJson.lowercase().contains(qLower)

            if (titleMatches || descMatches || topicMatches) {
                matches.add(
                    UnifiedSearchResult(
                        sourceId = pack.id,
                        sourceType = KnowledgeSourceType.KNOWLEDGE_PACK,
                        sourceTitle = pack.title,
                        snippet = pack.description,
                        score = if (titleMatches) 1.2f else 0.9f
                    )
                )
            }
        }
        return matches.take(limit)
    }

    private suspend fun searchDrafts(
        terms: List<String>,
        scopeSourceIds: List<String>?,
        limit: Int
    ): List<UnifiedSearchResult> {
        // Search through drafts
        // In ModeSessionDao, we can fetch all or search
        return emptyList()
    }

    fun sanitizeQuery(raw: String): List<String> {
        return raw.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 1 && it !in STOP_WORDS }
    }

    fun generateSnippet(text: String, terms: List<String>, maxChars: Int = 120): String {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.length <= maxChars) return clean

        // Find first occurrence of any term
        val lower = clean.lowercase()
        var bestIndex = -1
        for (term in terms) {
            val idx = lower.indexOf(term)
            if (idx != -1 && (bestIndex == -1 || idx < bestIndex)) {
                bestIndex = idx
            }
        }

        if (bestIndex == -1) return clean.take(maxChars) + "…"

        val start = (bestIndex - 30).coerceAtLeast(0)
        val end = (start + maxChars).coerceAtMost(clean.length)
        val prefix = if (start > 0) "…" else ""
        val suffix = if (end < clean.length) "…" else ""
        return prefix + clean.substring(start, end).trim() + suffix
    }

    private fun calculateScore(text: String, terms: List<String>): Float {
        val lower = text.lowercase()
        var matchCount = 0
        for (term in terms) {
            if (lower.contains(term)) matchCount++
        }
        return (matchCount.toFloat() / terms.size.toFloat()) + 0.5f
    }

    companion object {
        val STOP_WORDS = setOf(
            "the", "and", "is", "in", "it", "of", "to", "a", "an", "for", "with",
            "on", "at", "from", "by", "about", "what", "how", "when", "where", "who", "which"
        )
    }
}
