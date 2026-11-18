package com.openfield.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_sessions",
    indices = [Index(value = ["lastUpdated"])]
)
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val preview: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val isPinned: Boolean = false
)

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stepsJson: String? = null,
    val imageUri: String? = null,
    val sourcePage: String? = null,
    val isComplete: Boolean = true
)

