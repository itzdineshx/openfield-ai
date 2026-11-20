package com.openfield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openfield.app.data.local.entity.CollectionSourceCrossRefEntity
import com.openfield.app.data.local.entity.KnowledgeCollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM knowledge_collections ORDER BY updatedAt DESC")
    fun getCollectionsFlow(): Flow<List<KnowledgeCollectionEntity>>

    @Query("SELECT * FROM knowledge_collections ORDER BY updatedAt DESC")
    suspend fun getCollections(): List<KnowledgeCollectionEntity>

    @Query("SELECT * FROM knowledge_collections WHERE id = :id LIMIT 1")
    suspend fun getCollectionById(id: String): KnowledgeCollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: KnowledgeCollectionEntity)

    @Update
    suspend fun updateCollection(collection: KnowledgeCollectionEntity)

    @Query("DELETE FROM knowledge_collections WHERE id = :id")
    suspend fun deleteCollection(id: String)

    // Cross-reference queries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSourceToCollection(ref: CollectionSourceCrossRefEntity)

    @Query("DELETE FROM collection_source_cross_ref WHERE collectionId = :collectionId AND sourceId = :sourceId")
    suspend fun removeSourceFromCollection(collectionId: String, sourceId: String)

    @Query("SELECT sourceId FROM collection_source_cross_ref WHERE collectionId = :collectionId")
    suspend fun getSourceIdsForCollection(collectionId: String): List<String>

    @Query("SELECT sourceId FROM collection_source_cross_ref WHERE collectionId = :collectionId")
    fun getSourceIdsForCollectionFlow(collectionId: String): Flow<List<String>>

    @Query("SELECT collectionId FROM collection_source_cross_ref WHERE sourceId = :sourceId")
    suspend fun getCollectionIdsForSource(sourceId: String): List<String>

    @Query("SELECT COUNT(*) FROM collection_source_cross_ref WHERE collectionId = :collectionId")
    fun getSourceCountForCollectionFlow(collectionId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM collection_source_cross_ref WHERE collectionId = :collectionId")
    suspend fun getSourceCountForCollection(collectionId: String): Int

    @Query("DELETE FROM collection_source_cross_ref WHERE sourceId = :sourceId")
    suspend fun removeSourceFromAllCollections(sourceId: String)

    @Query("DELETE FROM knowledge_collections")
    suspend fun clearAllCollections()

    @Query("DELETE FROM collection_source_cross_ref")
    suspend fun clearAllCrossRefs()
}
