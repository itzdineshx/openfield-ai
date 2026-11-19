package com.openfield.app.feature.ai.context

import com.openfield.app.data.local.dao.TaskSessionDao
import com.openfield.app.data.local.entity.TaskSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Payload transfer representation between OpenField workspaces.
 */
data class SharedContextHandoff(
    val id: String = UUID.randomUUID().toString(),
    val sourceWorkspace: String,
    val targetWorkspace: String,
    val title: String,
    val textSnippet: String? = null,
    val sourceReferences: List<String> = emptyList(),
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Manages cross-workspace task continuity, handoffs, and session context persistence (Phase 3 Gate 4).
 */
class SharedContextManager(
    private val taskSessionDao: TaskSessionDao,
    private val scope: CoroutineScope
) {

    private val _pendingHandoff = MutableStateFlow<SharedContextHandoff?>(null)
    val pendingHandoff: StateFlow<SharedContextHandoff?> = _pendingHandoff.asStateFlow()

    fun handoffToWriter(title: String, initialDraft: String, sourceDocIds: List<String> = emptyList()) {
        val handoff = SharedContextHandoff(
            sourceWorkspace = "DOCUMENT_QA",
            targetWorkspace = "CREATE_WRITE",
            title = title,
            textSnippet = initialDraft,
            sourceReferences = sourceDocIds
        )
        setPendingHandoff(handoff)
    }

    fun handoffToTroubleshooter(problemSummary: String, imagePath: String?) {
        val handoff = SharedContextHandoff(
            sourceWorkspace = "IMAGE_ANALYSIS",
            targetWorkspace = "PROBLEM_SOLVER",
            title = problemSummary.take(40),
            textSnippet = problemSummary,
            imagePath = imagePath
        )
        setPendingHandoff(handoff)
    }

    fun handoffToAskAi(question: String, contextPassages: List<String> = emptyList()) {
        val handoff = SharedContextHandoff(
            sourceWorkspace = "KNOWLEDGE_CENTER",
            targetWorkspace = "ASK_AI",
            title = question.take(40),
            textSnippet = contextPassages.joinToString("\n\n"),
            sourceReferences = emptyList()
        )
        setPendingHandoff(handoff)
    }

    fun setPendingHandoff(handoff: SharedContextHandoff) {
        _pendingHandoff.value = handoff

        // Persist to TaskSessionEntity in Room
        scope.launch {
            val entity = TaskSessionEntity(
                id = handoff.id,
                title = handoff.title,
                createdAt = handoff.timestamp,
                updatedAt = handoff.timestamp,
                status = "ACTIVE",
                sourceWorkspace = handoff.sourceWorkspace,
                targetWorkspace = handoff.targetWorkspace,
                contextPayload = handoff.textSnippet ?: "",
                tagsJson = "[]"
            )
            taskSessionDao.insertSession(entity)
        }
    }

    fun consumePendingHandoff(): SharedContextHandoff? {
        val handoff = _pendingHandoff.value
        _pendingHandoff.value = null
        return handoff
    }

    fun clearPendingHandoff() {
        _pendingHandoff.value = null
    }
}
