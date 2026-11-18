package com.openfield.app.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val uriString: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val pageCount: Int,
    val summary: String,
    val isScanned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "document_chunks",
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["pageNumber"])
    ]
)
data class DocumentChunkEntity(
    @PrimaryKey(autoGenerate = true)
    val rowid: Long = 0,
    val documentId: String,
    val pageNumber: Int,
    val chunkIndex: Int,
    val text: String
)

@Entity(tableName = "document_chunks_fts")
@Fts4(contentEntity = DocumentChunkEntity::class)
data class DocumentChunkFtsEntity(
    val text: String
)

