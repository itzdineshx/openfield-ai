package com.openfield.app.feature.ai.controller

import android.content.Context
import android.net.Uri
import com.openfield.app.data.local.dao.ModeSessionDao
import com.openfield.app.data.local.entity.TroubleshootSessionEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import com.openfield.app.feature.ai.mode.DiagnosticAssessment
import com.openfield.app.feature.ai.mode.DiagnosticOption
import com.openfield.app.feature.ai.mode.DiagnosticStepHistory
import com.openfield.app.feature.ai.mode.ValidationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed interface TroubleshootingUiState {
    data object Idle : TroubleshootingUiState
    data class Generating(val stepMessage: String) : TroubleshootingUiState
    data class DiagnosticStep(
        val assessment: DiagnosticAssessment,
        val selectedOptionId: String? = null
    ) : TroubleshootingUiState
    data class Resolution(
        val assessment: DiagnosticAssessment,
        val didFixWork: Boolean? = null
    ) : TroubleshootingUiState
    data class Error(val message: String, val canRetry: Boolean = true) : TroubleshootingUiState
}

class TroubleshootingController(
    private val orchestrator: AiOrchestrator,
    private val modeSessionDao: ModeSessionDao,
    private val scope: CoroutineScope
) {
    private var activeJob: Job? = null
    private var currentSessionId: String = UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow<TroubleshootingUiState>(TroubleshootingUiState.Idle)
    val uiState: StateFlow<TroubleshootingUiState> = _uiState.asStateFlow()

    private val _problemDescription = MutableStateFlow("")
    val problemDescription: StateFlow<String> = _problemDescription.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()
    private var localImagePath: String? = null

    private val _history = MutableStateFlow<List<DiagnosticStepHistory>>(emptyList())
    val history: StateFlow<List<DiagnosticStepHistory>> = _history.asStateFlow()

    val savedSessions: StateFlow<List<TroubleshootSessionEntity>> = modeSessionDao.getTroubleshootSessionsFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun setImage(context: Context, uri: Uri) {
        _selectedImageUri.value = uri
        scope.launch(Dispatchers.IO) {
            try {
                val tempFile = File(context.cacheDir, "troubleshoot_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                localImagePath = tempFile.absolutePath
            } catch (e: Exception) {
                localImagePath = null
            }
        }
    }

    fun removeImage() {
        _selectedImageUri.value = null
        localImagePath = null
    }

    fun resumeSession(session: TroubleshootSessionEntity) {
        currentSessionId = session.id
        _problemDescription.value = session.problem
        removeImage()

        val parsedHistory = mutableListOf<DiagnosticStepHistory>()
        if (session.historyJson.isNotBlank()) {
            session.historyJson.split(";").forEach { item ->
                val parts = item.split(":")
                if (parts.size >= 2) {
                    val stepIdx = parts[0].toIntOrNull() ?: 1
                    val ans = parts.subList(1, parts.size).joinToString(":")
                    parsedHistory.add(DiagnosticStepHistory(stepIdx, "Step $stepIdx check", ans))
                }
            }
        }
        _history.value = parsedHistory

        val assessment = DiagnosticAssessment(
            category = session.category,
            currentStepNumber = session.currentStepNumber,
            question = session.assessmentJson,
            isResolutionReached = session.isResolved,
            likelyCause = if (session.isResolved) session.assessmentJson else null,
            recommendedSolution = if (session.isResolved) "Refer to saved diagnostic resolution." else null
        )

        _uiState.value = if (session.isResolved) {
            TroubleshootingUiState.Resolution(assessment, didFixWork = true)
        } else {
            TroubleshootingUiState.DiagnosticStep(assessment)
        }
    }

    fun deleteSession(sessionId: String) {
        scope.launch(Dispatchers.IO) {
            modeSessionDao.deleteTroubleshootSession(sessionId)
            if (currentSessionId == sessionId) {
                reset()
            }
        }
    }

    fun startTroubleshooting(problem: String) {
        if (problem.isBlank()) return
        _problemDescription.value = problem.trim()
        _history.value = emptyList()
        currentSessionId = UUID.randomUUID().toString()

        fetchDiagnosticAssessment()
    }

    private fun fetchDiagnosticAssessment() {
        if (!orchestrator.isReady()) {
            _uiState.value = TroubleshootingUiState.Error(
                "Local AI model is not ready. Please load Gemma 4 E2B IT in Model Management.",
                canRetry = true
            )
            return
        }

        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.IO) {
            val stepNum = _history.value.size + 1
            _uiState.value = TroubleshootingUiState.Generating("Analyzing diagnostic step $stepNum…")

            val result = orchestrator.executeDiagnosticAssessment(
                problemDescription = _problemDescription.value,
                history = _history.value,
                imagePath = if (_history.value.isEmpty()) localImagePath else null
            )

            when (result) {
                is ValidationResult.Success -> {
                    val assessment = result.data
                    if (assessment.isResolutionReached) {
                        _uiState.value = TroubleshootingUiState.Resolution(assessment)
                        persistSession(assessment, isResolved = true)
                    } else {
                        _uiState.value = TroubleshootingUiState.DiagnosticStep(assessment)
                        persistSession(assessment, isResolved = false)
                    }
                }
                is ValidationResult.Failure -> {
                    _uiState.value = TroubleshootingUiState.Error(
                        message = result.reason,
                        canRetry = true
                    )
                }
            }
        }
    }

    fun selectOption(optionId: String) {
        val current = _uiState.value
        if (current is TroubleshootingUiState.DiagnosticStep) {
            _uiState.value = current.copy(selectedOptionId = optionId)
        }
    }

    fun submitAnswerAndProceed() {
        val current = _uiState.value
        if (current is TroubleshootingUiState.DiagnosticStep) {
            val selectedId = current.selectedOptionId ?: return
            val selectedOption = current.assessment.options.find { it.id == selectedId }
            val chosenText = selectedOption?.text ?: selectedId

            val historyEntry = DiagnosticStepHistory(
                stepIndex = current.assessment.currentStepNumber,
                question = current.assessment.question,
                selectedOptionText = chosenText
            )
            _history.value = _history.value + historyEntry

            fetchDiagnosticAssessment()
        }
    }

    fun exploreAlternativeCauses() {
        val nextStep = _history.value.size + 1
        val altEntry = DiagnosticStepHistory(
            stepIndex = nextStep,
            question = "Did earlier recommended fix resolve the problem?",
            selectedOptionText = "No, earlier suggestion did not resolve the issue. Seeking alternative root cause."
        )
        _history.value = _history.value + altEntry
        fetchDiagnosticAssessment()
    }

    fun verifySolution(worked: Boolean) {
        val current = _uiState.value
        if (current is TroubleshootingUiState.Resolution) {
            _uiState.value = current.copy(didFixWork = worked)
            persistSession(current.assessment, isResolved = worked)
        }
    }

    fun retryLastStep() {
        fetchDiagnosticAssessment()
    }

    fun reset() {
        activeJob?.cancel()
        _uiState.value = TroubleshootingUiState.Idle
        _problemDescription.value = ""
        _history.value = emptyList()
        removeImage()
        currentSessionId = UUID.randomUUID().toString()
    }

    private fun persistSession(assessment: DiagnosticAssessment, isResolved: Boolean) {
        scope.launch(Dispatchers.IO) {
            try {
                val entity = TroubleshootSessionEntity(
                    id = currentSessionId,
                    problem = _problemDescription.value,
                    category = assessment.category,
                    currentStepNumber = assessment.currentStepNumber,
                    isResolved = isResolved,
                    assessmentJson = assessment.question,
                    historyJson = _history.value.joinToString(";") { "${it.stepIndex}:${it.selectedOptionText}" },
                    updatedAt = System.currentTimeMillis()
                )
                modeSessionDao.insertTroubleshootSession(entity)
            } catch (ignored: Exception) {}
        }
    }
}
