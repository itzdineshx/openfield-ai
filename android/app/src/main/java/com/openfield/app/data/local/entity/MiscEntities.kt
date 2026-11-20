package com.openfield.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_packs")
data class KnowledgePackEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val size: String,
    val isInstalled: Boolean,
    val category: String,
    val iconColorHex: Long,
    val topicsJson: String
)

@Entity(
    tableName = "knowledge_pack_articles",
    indices = [
        Index(value = ["packId"]),
        Index(value = ["section"])
    ]
)
data class KnowledgePackArticleEntity(
    @PrimaryKey
    val id: String,
    val packId: String,
    val title: String,
    val section: String,
    val content: String,
    val tagsJson: String = "[]",
    val orderIndex: Int = 0
)

@Entity(
    tableName = "saved_answers",
    indices = [Index(value = ["savedAt"])]
)
data class SavedAnswerEntity(
    @PrimaryKey
    val id: String,
    val question: String,
    val answer: String,
    val sourceTitle: String,
    val sourceType: String, // "CHAT", "DOCUMENT", "KNOWLEDGE_PACK", "TOOL"
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)


