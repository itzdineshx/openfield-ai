package com.openfield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openfield.app.data.local.entity.LearningSessionEntity
import com.openfield.app.data.local.entity.TroubleshootSessionEntity
import com.openfield.app.data.local.entity.WritingDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeSessionDao {

    // Troubleshooting Sessions
    @Query("SELECT * FROM troubleshoot_sessions ORDER BY updatedAt DESC")
    fun getTroubleshootSessionsFlow(): Flow<List<TroubleshootSessionEntity>>

    @Query("SELECT * FROM troubleshoot_sessions WHERE id = :id LIMIT 1")
    suspend fun getTroubleshootSession(id: String): TroubleshootSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTroubleshootSession(session: TroubleshootSessionEntity)

    @Update
    suspend fun updateTroubleshootSession(session: TroubleshootSessionEntity)

    @Query("DELETE FROM troubleshoot_sessions WHERE id = :id")
    suspend fun deleteTroubleshootSession(id: String)

    // Learning Sessions
    @Query("SELECT * FROM learning_sessions ORDER BY updatedAt DESC")
    fun getLearningSessionsFlow(): Flow<List<LearningSessionEntity>>

    @Query("SELECT * FROM learning_sessions WHERE id = :id LIMIT 1")
    suspend fun getLearningSession(id: String): LearningSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningSession(session: LearningSessionEntity)

    @Update
    suspend fun updateLearningSession(session: LearningSessionEntity)

    @Query("DELETE FROM learning_sessions WHERE id = :id")
    suspend fun deleteLearningSession(id: String)

    // Writing Drafts
    @Query("SELECT * FROM writing_drafts ORDER BY updatedAt DESC")
    fun getWritingDraftsFlow(): Flow<List<WritingDraftEntity>>

    @Query("SELECT * FROM writing_drafts ORDER BY updatedAt DESC")
    suspend fun getWritingDraftsList(): List<WritingDraftEntity>

    @Query("SELECT * FROM writing_drafts WHERE id = :id LIMIT 1")
    suspend fun getWritingDraft(id: String): WritingDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWritingDraft(draft: WritingDraftEntity)

    @Update
    suspend fun updateWritingDraft(draft: WritingDraftEntity)

    @Query("SELECT * FROM troubleshoot_sessions ORDER BY updatedAt DESC")
    suspend fun getAllTroubleshootSessions(): List<TroubleshootSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTroubleshootSessions(sessions: List<TroubleshootSessionEntity>)

    @Query("SELECT * FROM learning_sessions ORDER BY updatedAt DESC")
    suspend fun getAllLearningSessions(): List<LearningSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningSessions(sessions: List<LearningSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWritingDrafts(drafts: List<WritingDraftEntity>)

    @Query("DELETE FROM writing_drafts WHERE id = :id")
    suspend fun deleteWritingDraft(id: String)
}
