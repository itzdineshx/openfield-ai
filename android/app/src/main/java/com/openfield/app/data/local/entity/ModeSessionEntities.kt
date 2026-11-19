package com.openfield.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "troubleshoot_sessions",
    indices = [Index(value = ["updatedAt"])]
)
data class TroubleshootSessionEntity(
    @PrimaryKey
    val id: String,
    val problem: String,
    val category: String,
    val currentStepNumber: Int,
    val isResolved: Boolean,
    val assessmentJson: String,
    val historyJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "learning_sessions",
    indices = [Index(value = ["updatedAt"])]
)
data class LearningSessionEntity(
    @PrimaryKey
    val id: String,
    val topic: String,
    val level: String,
    val lessonTitle: String,
    val lessonJson: String,
    val isQuizCompleted: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "writing_drafts",
    indices = [Index(value = ["updatedAt"])]
)
data class WritingDraftEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val intent: String,
    val tone: String,
    val content: String,
    val wordCount: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
