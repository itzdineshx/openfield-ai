package com.openfield.app.feature.ai.controller

import com.openfield.app.core.model.ChatMessage
import com.openfield.app.data.local.dao.ChatDao
import com.openfield.app.data.local.entity.ChatMessageEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatController(
    private val chatDao: ChatDao,
    private val orchestrator: AiOrchestrator,
    private val scope: CoroutineScope
) {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    private var activeGenerationJob: Job? = null

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun getMessagesFlow(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesFlow(sessionId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    isUser = entity.isUser,
                    text = entity.text,
                    timestamp = formatTimestamp(entity.timestamp),
                    steps = emptyList(),
                    imageUri = entity.imageUri,
                    sourcePage = entity.sourcePage,
                    isComplete = entity.isComplete
                )
            }
        }
    }

    fun sendMessage(sessionId: String, userPrompt: String) {
        if (userPrompt.isBlank()) return

        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        activeGenerationJob?.cancel()
        activeGenerationJob = scope.launch(Dispatchers.IO) {
            _isGenerating.value = true

            // Insert user message
            val userEntity = ChatMessageEntity(
                id = userMsgId,
                sessionId = sessionId,
                isUser = true,
                text = userPrompt.trim(),
                timestamp = now,
                isComplete = true
            )
            chatDao.insertMessage(userEntity)

            // Insert empty assistant message for streaming
            val assistantEntity = ChatMessageEntity(
                id = assistantMsgId,
                sessionId = sessionId,
                isUser = false,
                text = "",
                timestamp = now + 1,
                isComplete = false
            )
            chatDao.insertMessage(assistantEntity)

            // Update session preview
            chatDao.getSession(sessionId)?.let { session ->
                chatDao.updateSession(
                    session.copy(
                        preview = userPrompt.take(60),
                        lastUpdated = now,
                        messageCount = session.messageCount + 2
                    )
                )
            }

            if (!orchestrator.isReady()) {
                chatDao.updateMessageContent(
                    assistantMsgId,
                    "AI model not ready. Please verify Gemma 4 E2B is loaded in Model Management.",
                    isComplete = true
                )
                _isGenerating.value = false
                return@launch
            }

            var accumulated = ""
            try {
                orchestrator.streamAskAi(sessionId, userPrompt).collect { token ->
                    accumulated += token
                    chatDao.updateMessageContent(assistantMsgId, accumulated, isComplete = false)
                }
                chatDao.updateMessageContent(assistantMsgId, accumulated, isComplete = true)

                // Update session preview with answer
                chatDao.getSession(sessionId)?.let { session ->
                    chatDao.updateSession(
                        session.copy(
                            preview = accumulated.take(60) + if (accumulated.length > 60) "..." else ""
                        )
                    )
                }
            } catch (e: Exception) {
                val errorMsg = if (accumulated.isNotBlank()) accumulated else "Generation failed: ${e.message}"
                chatDao.updateMessageContent(assistantMsgId, errorMsg, isComplete = true)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun regenerateLastResponse(sessionId: String) {
        scope.launch(Dispatchers.IO) {
            val messages = chatDao.getMessages(sessionId)
            if (messages.isEmpty()) return@launch

            val lastAssistantMsg = messages.lastOrNull { !it.isUser }
            val lastUserMsg = messages.lastOrNull { it.isUser }

            if (lastUserMsg != null) {
                if (lastAssistantMsg != null) {
                    chatDao.deleteMessage(lastAssistantMsg.id)
                }
                sendMessage(sessionId, lastUserMsg.text)
            }
        }
    }

    fun stopGeneration() {
        orchestrator.cancelGeneration()
        activeGenerationJob?.cancel()
        _isGenerating.value = false
    }

    fun clearSession(sessionId: String) {
        scope.launch(Dispatchers.IO) {
            chatDao.deleteMessagesForSession(sessionId)
            chatDao.getSession(sessionId)?.let {
                chatDao.updateSession(it.copy(preview = "No messages", messageCount = 0))
            }
        }
    }

    private fun formatTimestamp(millis: Long): String {
        return try {
            val now = System.currentTimeMillis()
            val diff = now - millis
            if (diff < 60_000) "Just now"
            else if (diff < 24 * 3600 * 1000) timeFormat.format(Date(millis))
            else dateFormat.format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }
}
