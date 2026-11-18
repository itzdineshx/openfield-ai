package com.openfield.app.feature.ai.model

import java.io.File

sealed interface ModelInstallationState {
    data class NotInstalled(
        val catalog: List<ModelInfo> = ModelCatalog.ALL_MODELS,
        val availableDiskSpaceBytes: Long = 0L
    ) : ModelInstallationState

    data object Checking : ModelInstallationState

    data class Downloading(
        val model: ModelInfo,
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val speedBytesPerSec: Long = 0L
    ) : ModelInstallationState

    data class Verifying(val model: ModelInfo) : ModelInstallationState

    data class Installed(
        val activeModel: ModelInfo,
        val activeFile: File,
        val allInstalledModels: List<ModelInfo>
    ) : ModelInstallationState

    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val model: ModelInfo? = null
    ) : ModelInstallationState
}

