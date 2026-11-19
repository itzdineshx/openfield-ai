package com.openfield.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "knowledge_collections",
    indices = [Index(value = ["updatedAt"])]
)
data class KnowledgeCollectionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val colorHex: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "collection_source_cross_ref",
    primaryKeys = ["collectionId", "sourceId"],
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["sourceId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeCollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CollectionSourceCrossRefEntity(
    val collectionId: String,
    val sourceId: String,
    val sourceType: String,  // DOCUMENT, NOTE, CONVERSATION, KNOWLEDGE_PACK
    val addedAt: Long = System.currentTimeMillis()
)
