package com.openfield.app.core.model

import com.openfield.app.core.components.StepItem

// Chat Models
data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val steps: List<StepItem> = emptyList(),
    val imageUri: Any? = null,
    val sourcePage: String? = null,
    val isComplete: Boolean = true
)

data class ChatSession(
    val id: String,
    val title: String,
    val preview: String,
    val lastUpdated: String,
    val messageCount: Int,
    val isPinned: Boolean = false
)

// Image Analysis Models
data class ImageAnalysisResult(
    val id: String,
    val title: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val keyFeatures: List<String>,
    val safetyNotice: String,
    val imageUri: Any? = null,
    val rawText: String = "",
    val isError: Boolean = false
)

// Document Models
data class DocumentItem(
    val id: String,
    val name: String,
    val size: String,
    val pageCount: Int,
    val summary: String,
    val keyPoints: List<String>,
    val sampleQuestions: List<String>,
    val isCustomImport: Boolean = false
)

// Troubleshooting Models
data class TroubleshootOption(
    val id: String,
    val text: String,
    val outcomeMessage: String? = null
)

data class TroubleshootStep(
    val stepIndex: Int,
    val totalSteps: Int,
    val categoryTitle: String = "Guided Troubleshooting",
    val subtitle: String = "Let me help you solve this step by step.",
    val question: String,
    val instruction: String? = null,
    val options: List<TroubleshootOption>,
    val solutionRecommendation: String? = null
)

// Learning Models
enum class LearningTab {
    EXPLAIN, TEACH, PRACTICE
}

data class LearningTopic(
    val id: String,
    val prompt: String,
    val definition: String,
    val inShortPoints: List<String>,
    val diagramTitle: String,
    val suggestions: List<String>
)

// AI Tool Models
enum class ToolFilterCategory {
    ALL, TEXT, IMAGE, DOCUMENT
}

data class AiToolItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolFilterCategory,
    val iconName: String,
    val inputPlaceholder: String,
    val sampleInput: String,
    val sampleOutput: String
)

// Knowledge Pack Models
data class KnowledgePack(
    val id: String,
    val title: String,
    val description: String,
    val size: String,
    val isInstalled: Boolean,
    val category: String,
    val iconColorHex: Long,
    val topics: List<String> = emptyList(),
    val version: String = "1.0.0"
)

