package com.openfield.app.feature.tasks

data class SubtaskItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

data class TaskPlan(
    val title: String,
    val description: String,
    val priority: String, // LOW, MEDIUM, HIGH
    val subtasks: List<String>,
    val suggestedCategory: String = "General"
)
