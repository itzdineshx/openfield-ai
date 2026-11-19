package com.openfield.app.core.model

/**
 * Universal classification of local knowledge sources supported by OpenField.
 */
enum class KnowledgeSourceType(val displayName: String) {
    DOCUMENT("Document"),
    NOTE("Note"),
    CONVERSATION("Conversation"),
    KNOWLEDGE_PACK("Knowledge Pack"),
    LEARNING_TOPIC("Learning Topic")
}

/**
 * Unified representation of any local source without duplicating large content blobs.
 */
data class KnowledgeSource(
    val id: String,
    val type: KnowledgeSourceType,
    val title: String,
    val snippet: String,
    val itemCount: Int,
    val formattedSize: String,
    val updatedAt: Long,
    val isIndexed: Boolean = true,
    val isInRetrievalScope: Boolean = true
)

/**
 * Evidence passage retrieved from a knowledge source with verifiable provenance.
 */
data class KnowledgeEvidence(
    val evidenceId: String,
    val sourceId: String,
    val sourceTitle: String,
    val sourceType: KnowledgeSourceType,
    val pageNumber: Int? = null,
    val section: String? = null,
    val text: String,
    val relevanceScore: Float = 1.0f
)

/**
 * High-speed local search result across documents, chats, drafts, and packs.
 */
data class UnifiedSearchResult(
    val sourceId: String,
    val sourceType: KnowledgeSourceType,
    val sourceTitle: String,
    val snippet: String,
    val pageNumber: Int? = null,
    val score: Float = 1.0f
)

/**
 * Presentation model for user-defined semantic collections.
 */
data class KnowledgeCollection(
    val id: String,
    val name: String,
    val description: String,
    val colorHex: Long,
    val sourceCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
