package com.openfield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openfield.app.data.local.entity.TaskSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TaskSessionEntity)

    @Update
    suspend fun updateSession(session: TaskSessionEntity)

    @Query("DELETE FROM task_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("SELECT * FROM task_sessions WHERE status = 'ACTIVE' ORDER BY updatedAt DESC")
    fun getActiveSessionsFlow(): Flow<List<TaskSessionEntity>>

    @Query("SELECT * FROM task_sessions ORDER BY updatedAt DESC")
    fun getAllSessionsFlow(): Flow<List<TaskSessionEntity>>

    @Query("SELECT * FROM task_sessions WHERE id = :id LIMIT 1")
    suspend fun getSession(id: String): TaskSessionEntity?
}
