package com.openfield.app.feature.ai.memory

import java.util.UUID

enum class MemoryCategory(val displayName: String, val description: String) {
    PREFERENCES("User Preferences", "Personal communication styles, tone, and formatting preferences"),
    LEARNING_GOALS("Learning Goals", "Current subjects, topics of interest, and study milestones"),
    DOMAIN_CONTEXT("Domain Context", "Specific technical, academic, or professional domain focus")
}

data class LocalMemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val category: MemoryCategory,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
