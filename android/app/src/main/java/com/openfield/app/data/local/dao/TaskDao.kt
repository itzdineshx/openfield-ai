package com.openfield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openfield.app.data.local.entity.TaskItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItemEntity)

    @Update
    suspend fun updateTask(task: TaskItemEntity)

    @Query("DELETE FROM task_items WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("SELECT * FROM task_items ORDER BY updatedAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskItemEntity>>

    @Query("SELECT * FROM task_items WHERE status != 'COMPLETED' ORDER BY updatedAt DESC")
    fun getPendingTasksFlow(): Flow<List<TaskItemEntity>>

    @Query("SELECT * FROM task_items WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskItemEntity?

    @Query("DELETE FROM task_items")
    suspend fun clearAll()
}
