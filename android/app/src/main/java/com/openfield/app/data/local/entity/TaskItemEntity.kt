package com.openfield.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persists user-controlled automated task workflows and action checklists (Phase 3 Gate 6).
 */
@Entity(
    tableName = "task_items",
    indices = [
        Index("updatedAt"),
        Index("status"),
        Index("priority")
    ]
)
data class TaskItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val status: String, // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    val priority: String, // LOW, MEDIUM, HIGH
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val subtasksJson: String = "[]", // Serialized JSON list of subtasks
    val linkedSourceIdsJson: String = "[]" // Linked documents or packs
)
