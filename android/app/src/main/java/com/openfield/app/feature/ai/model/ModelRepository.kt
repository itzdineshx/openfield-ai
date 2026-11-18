package com.openfield.app.feature.ai.model

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ModelRepository(
    private val context: Context,
    private val storageManager: ModelStorageManager,
    private val downloadManager: ModelDownloadManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _installationState = MutableStateFlow<ModelInstallationState>(ModelInstallationState.Checking)
    val installationState: StateFlow<ModelInstallationState> = _installationState.asStateFlow()

    private var activeModelInfo: ModelInfo? = null
    private var activeDownloadJob: Job? = null

    init {
        refreshState()
    }

    fun refreshState() {
        val installedFiles = storageManager.listInstalledModelFiles()
        val installedModels = mutableListOf<Pair<ModelInfo, File>>()

        for (file in installedFiles) {
            val matched = ModelCatalog.findByFileName(file.name)
            if (matched != null) {
                installedModels.add(matched to file)
            }
        }

        if (installedModels.isEmpty()) {
            _installationState.value = ModelInstallationState.NotInstalled(
                catalog = ModelCatalog.ALL_MODELS,
                availableDiskSpaceBytes = storageManager.getAvailableDiskSpaceBytes()
            )
            activeModelInfo = null
        } else {
            val (selected, file) = installedModels.first()
            activeModelInfo = selected
            _installationState.value = ModelInstallationState.Installed(
                activeModel = selected,
                activeFile = file,
                allInstalledModels = installedModels.map { it.first }
            )
        }
    }

    fun getActiveModelFile(): File? {
        val state = _installationState.value
        return if (state is ModelInstallationState.Installed) state.activeFile else null
    }

    fun getActiveModel(): ModelInfo? = activeModelInfo

    fun startDownload(model: ModelInfo = ModelCatalog.GEMMA_4_E2B) {
        if (activeDownloadJob?.isActive == true) return

        // Launch the foreground service so Android does not kill us mid-download.
        try {
            ModelDownloadService.start(context)
        } catch (ignored: Throwable) {
            // If the service cannot start (e.g. battery optimizations), fall through to background job.
        }

        activeDownloadJob = scope.launch {
            _installationState.value = ModelInstallationState.Downloading(
                model = model,
                progressPercent = 0,
                bytesDownloaded = 0L,
                totalBytes = model.sizeBytes
            )

            val result = downloadManager.downloadModel(model) { progress ->
                _installationState.value = ModelInstallationState.Downloading(
                    model = model,
                    progressPercent = progress.progressPercent,
                    bytesDownloaded = progress.bytesDownloaded,
                    totalBytes = progress.totalBytes,
                    speedBytesPerSec = progress.speedBytesPerSec
                )
            }

            result.fold(
                onSuccess = {
                    _installationState.value = ModelInstallationState.Verifying(model)
                    activeModelInfo = model
                    refreshState()
                },
                onFailure = { error ->
                    _installationState.value = ModelInstallationState.Error(
                        message = error.message ?: "Failed to download model",
                        canRetry = true,
                        model = model
                    )
                }
            )
        }
    }

    fun cancelDownload() {
        downloadManager.cancelActiveDownload()
        activeDownloadJob?.cancel()
        activeDownloadJob = null
        refreshState()
    }

    fun importLocalModel(uri: Uri, onProgress: (Float) -> Unit = {}, onComplete: (Result<File>) -> Unit) {
        scope.launch {
            _installationState.value = ModelInstallationState.Checking
            val result = storageManager.importModelFromUri(uri) { copied, total ->
                val progress = if (total > 0) (copied.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
                onProgress(progress)
            }

            result.fold(
                onSuccess = {
                    refreshState()
                    onComplete(Result.success(it))
                },
                onFailure = { error ->
                    refreshState()
                    onComplete(Result.failure(error))
                }
            )
        }
    }

    fun selectModel(model: ModelInfo) {
        val file = storageManager.getModelFile(model.fileName)
        if (file.exists()) {
            activeModelInfo = model
            refreshState()
        }
    }

    fun deleteModel(model: ModelInfo = ModelCatalog.GEMMA_4_E2B): Boolean {
        val success = storageManager.deleteModelFile(model.fileName)
        if (activeModelInfo?.id == model.id) {
            activeModelInfo = null
        }
        refreshState()
        return success
    }
}
