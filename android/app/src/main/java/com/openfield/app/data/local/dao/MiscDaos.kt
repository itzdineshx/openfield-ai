package com.openfield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openfield.app.data.local.entity.KnowledgePackEntity
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

