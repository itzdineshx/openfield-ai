package com.openfield.app.feature.ai.model

data class ModelInfo(
    val id: String,
    val displayName: String,
    val description: String,
    val fileName: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val downloadUrl: String,
    val repository: String,
    val sha256: String? = null,
    val supportsVision: Boolean = true,
    val isRecommended: Boolean = true,
    val contextLength: Int = 4096
)

object ModelCatalog {
    val GEMMA_4_E2B = ModelInfo(
        id = "gemma-4-e2b-it",
        displayName = "Gemma 4 E2B IT",
        description = "Official on-device multimodal model for text reasoning, vision understanding, document Q&A, and troubleshooting.",
        fileName = "gemma-4-E2B-it.litertlm",
        sizeBytes = 2_770_216_960L, // ~2.58 GB
        formattedSize = "2.58 GB",
        downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
        repository = "litert-community/gemma-4-E2B-it-litert-lm",
        supportsVision = true,
        isRecommended = true,
        contextLength = 4096
    )

    val ALL_MODELS = listOf(GEMMA_4_E2B)

    fun findById(id: String): ModelInfo? = ALL_MODELS.find { it.id == id }
    fun findByFileName(fileName: String): ModelInfo? = if (fileName.contains("gemma-4", ignoreCase = true) || fileName == GEMMA_4_E2B.fileName) GEMMA_4_E2B else null
}
