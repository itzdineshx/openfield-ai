package com.openfield.app.data.repository

import com.openfield.app.core.model.KnowledgeSource
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.data.local.dao.ChatDao
import com.openfield.app.data.local.dao.CollectionDao
import com.openfield.app.data.local.dao.DocumentDao
import com.openfield.app.data.local.dao.KnowledgePackDao
import com.openfield.app.data.local.dao.ModeSessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Aggregates and synchronizes knowledge sources across Room tables into a single reactive domain stream.
 */
class KnowledgeSourceRegistry(
    private val documentDao: DocumentDao,
    private val chatDao: ChatDao,
    private val modeSessionDao: ModeSessionDao,
    private val knowledgePackDao: KnowledgePackDao,
    private val collectionDao: CollectionDao
) {

    /**
     * Emits all local knowledge sources sorted by latest update timestamp.
     */
    fun getAllSourcesFlow(): Flow<List<KnowledgeSource>> {
        return combine(
            documentDao.getDocumentsFlow(),
            chatDao.getSessionsFlow(),
            modeSessionDao.getWritingDraftsFlow(),
            knowledgePackDao.getKnowledgePacksFlow()
        ) { docs, chats, drafts, packs ->
            val result = mutableListOf<KnowledgeSource>()

            // 1. Documents
            docs.forEach { doc ->
                result.add(
                    KnowledgeSource(
                        id = doc.id,
                        type = KnowledgeSourceType.DOCUMENT,
                        title = doc.name,
                        snippet = doc.summary.ifBlank { "${doc.pageCount} page offline document" },
                        itemCount = doc.pageCount,
                        formattedSize = doc.formattedSize,
                        updatedAt = doc.createdAt,
                        isIndexed = true
                    )
                )
            }

            // 2. Saved Conversations
            chats.forEach { chat ->
                result.add(
                    KnowledgeSource(
                        id = chat.id,
                        type = KnowledgeSourceType.CONVERSATION,
                        title = chat.title,
                        snippet = chat.preview.ifBlank { "Conversation with ${chat.messageCount} messages" },
                        itemCount = chat.messageCount,
                        formattedSize = "${chat.messageCount} msgs",
                        updatedAt = chat.lastUpdated,
                        isIndexed = true
                    )
                )
            }

            // 3. Writing Drafts / Notes
            drafts.forEach { draft ->
                result.add(
                    KnowledgeSource(
                        id = draft.id,
                        type = KnowledgeSourceType.NOTE,
                        title = draft.title.ifBlank { "Untitled Note" },
                        snippet = draft.content.take(100).ifBlank { "${draft.intent} draft in ${draft.tone} tone" },
                        itemCount = draft.wordCount,
                        formattedSize = "${draft.wordCount} words",
                        updatedAt = draft.updatedAt,
                        isIndexed = true
                    )
                )
            }

            // 4. Installed Knowledge Packs
            packs.filter { it.isInstalled }.forEach { pack ->
                result.add(
                    KnowledgeSource(
                        id = pack.id,
                        type = KnowledgeSourceType.KNOWLEDGE_PACK,
                        title = pack.title,
                        snippet = pack.description,
                        itemCount = 1,
                        formattedSize = pack.size,
                        updatedAt = System.currentTimeMillis(),
                        isIndexed = true
                    )
                )
            }

            result.sortedByDescending { it.updatedAt }
        }
    }

    /**
     * Filters sources by specific type.
     */
    fun getSourcesByTypeFlow(type: KnowledgeSourceType): Flow<List<KnowledgeSource>> {
        return getAllSourcesFlow().map { list -> list.filter { it.type == type } }
    }

    /**
     * Filters sources belonging to a specific collection.
     */
    fun getSourcesForCollectionFlow(collectionId: String): Flow<List<KnowledgeSource>> {
        return combine(
            getAllSourcesFlow(),
            collectionDao.getSourceIdsForCollectionFlow(collectionId)
        ) { allSources, sourceIds ->
            val idSet = sourceIds.toSet()
            allSources.filter { it.id in idSet }
        }
    }

    /**
     * Resolves a single knowledge source by ID.
     */
    suspend fun getSourceById(id: String): KnowledgeSource? {
        // Check documents
        val doc = documentDao.getDocument(id)
        if (doc != null) {
            return KnowledgeSource(
                id = doc.id,
                type = KnowledgeSourceType.DOCUMENT,
                title = doc.name,
                snippet = doc.summary,
                itemCount = doc.pageCount,
                formattedSize = doc.formattedSize,
                updatedAt = doc.createdAt
            )
        }

        // Check writing drafts
        val draft = modeSessionDao.getWritingDraft(id)
        if (draft != null) {
            return KnowledgeSource(
                id = draft.id,
                type = KnowledgeSourceType.NOTE,
                title = draft.title,
                snippet = draft.content.take(100),
                itemCount = draft.wordCount,
                formattedSize = "${draft.wordCount} words",
                updatedAt = draft.updatedAt
            )
        }

        // Check chat sessions
        val chat = chatDao.getSession(id)
        if (chat != null) {
            return KnowledgeSource(
                id = chat.id,
                type = KnowledgeSourceType.CONVERSATION,
                title = chat.title,
                snippet = chat.preview,
                itemCount = chat.messageCount,
                formattedSize = "${chat.messageCount} msgs",
                updatedAt = chat.lastUpdated
            )
        }

        // Check knowledge packs
        val pack = knowledgePackDao.getPackById(id)
        if (pack != null && pack.isInstalled) {
            return KnowledgeSource(
                id = pack.id,
                type = KnowledgeSourceType.KNOWLEDGE_PACK,
                title = pack.title,
                snippet = pack.description,
                itemCount = 1,
                formattedSize = pack.size,
                updatedAt = System.currentTimeMillis()
            )
        }

        return null
    }
}
