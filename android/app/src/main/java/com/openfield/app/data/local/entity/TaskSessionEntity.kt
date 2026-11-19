package com.openfield.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persists persistent multi-step task sessions spanning multiple workspaces (Phase 3 Gate 4).
 */
@Entity(
    tableName = "task_sessions",
    indices = [
        Index("updatedAt"),
        Index("status")
    ]
)
data class TaskSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val status: String, // ACTIVE, COMPLETED, ARCHIVED
    val sourceWorkspace: String,
    val targetWorkspace: String,
    val contextPayload: String, // Serialized JSON containing text snippets or document references
    val tagsJson: String = "[]"
)
