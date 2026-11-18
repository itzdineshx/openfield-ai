package com.openfield.app.data.local.entity

import androidx.room.Entity
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

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

