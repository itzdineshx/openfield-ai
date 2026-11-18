package com.openfield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openfield.app.data.local.entity.DocumentChunkEntity
import com.openfield.app.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getDocumentsFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocument(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<DocumentChunkEntity>)

    @Query("DELETE FROM document_chunks WHERE documentId = :documentId")
    suspend fun deleteChunksForDocument(documentId: String)

    @Query("SELECT * FROM document_chunks WHERE documentId = :documentId ORDER BY pageNumber ASC, chunkIndex ASC")
    suspend fun getChunksForDocument(documentId: String): List<DocumentChunkEntity>

    @Query("""
        SELECT c.* FROM document_chunks c 
        JOIN document_chunks_fts f ON c.rowid = f.rowid 
        WHERE document_chunks_fts MATCH :query AND c.documentId = :documentId 
        LIMIT :limit
    """)
    suspend fun searchChunksInDocument(documentId: String, query: String, limit: Int = 5): List<DocumentChunkEntity>

    @Query("""
        SELECT c.* FROM document_chunks c 
        JOIN document_chunks_fts f ON c.rowid = f.rowid 
        WHERE document_chunks_fts MATCH :query 
        LIMIT :limit
    """)
    suspend fun searchChunksGlobal(query: String, limit: Int = 5): List<DocumentChunkEntity>
}

