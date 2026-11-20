package com.openfield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openfield.app.data.local.entity.KnowledgePackArticleEntity
import com.openfield.app.data.local.entity.KnowledgePackEntity
import com.openfield.app.data.local.entity.SavedAnswerEntity
import com.openfield.app.data.local.entity.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgePackDao {

    @Query("SELECT * FROM knowledge_packs")
    fun getKnowledgePacksFlow(): Flow<List<KnowledgePackEntity>>

    @Query("SELECT * FROM knowledge_packs")
    suspend fun getKnowledgePacks(): List<KnowledgePackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packs: List<KnowledgePackEntity>)

    @Query("SELECT * FROM knowledge_packs WHERE id = :id LIMIT 1")
    suspend fun getPackById(id: String): KnowledgePackEntity?

    @Query("UPDATE knowledge_packs SET isInstalled = :isInstalled WHERE id = :id")
    suspend fun updateInstallation(id: String, isInstalled: Boolean)

    @Query("DELETE FROM knowledge_packs WHERE id = :id")
    suspend fun deletePack(id: String)

    // Knowledge Pack Articles (Real Curated Domain Content)
    @Query("SELECT * FROM knowledge_pack_articles WHERE packId = :packId ORDER BY orderIndex ASC")
    fun getArticlesForPackFlow(packId: String): Flow<List<KnowledgePackArticleEntity>>

    @Query("SELECT * FROM knowledge_pack_articles WHERE packId = :packId ORDER BY orderIndex ASC")
    suspend fun getArticlesForPack(packId: String): List<KnowledgePackArticleEntity>

    @Query("SELECT * FROM knowledge_pack_articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): KnowledgePackArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<KnowledgePackArticleEntity>)

    @Query("DELETE FROM knowledge_pack_articles WHERE packId = :packId")
    suspend fun deleteArticlesForPack(packId: String)

    @Query("SELECT * FROM knowledge_pack_articles WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun searchArticles(query: String): List<KnowledgePackArticleEntity>

    @Query("""
        SELECT a.* FROM knowledge_pack_articles a
        INNER JOIN knowledge_packs p ON a.packId = p.id
        WHERE p.isInstalled = 1
    """)
    suspend fun getAllInstalledArticles(): List<KnowledgePackArticleEntity>
}

@Dao
interface SavedAnswerDao {

    @Query("SELECT * FROM saved_answers ORDER BY savedAt DESC")
    fun getSavedAnswersFlow(): Flow<List<SavedAnswerEntity>>

    @Query("SELECT * FROM saved_answers ORDER BY savedAt DESC")
    suspend fun getSavedAnswers(): List<SavedAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAnswer(answer: SavedAnswerEntity)

    @Query("DELETE FROM saved_answers WHERE id = :id")
    suspend fun deleteSavedAnswer(id: String)

    @Query("DELETE FROM saved_answers")
    suspend fun clearAllSavedAnswers()
}

@Dao
interface SettingsDao {

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(entity: SettingEntity)

    @Query("SELECT * FROM app_settings")
    fun getAllSettingsFlow(): Flow<List<SettingEntity>>
}


