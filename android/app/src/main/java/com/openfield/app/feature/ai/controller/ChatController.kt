package com.openfield.app.feature.ai.controller

import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.text.Markdown
import com.openfield.app.data.local.dao.ChatDao
import com.openfield.app.data.local.entity.ChatMessageEntity
import com.openfield.app.feature.ai.mode.AiOrchestrator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
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
        startGeneration(sessionId, userPrompt.trim(), insertUserMessage = true)
    }

    private fun startGeneration(sessionId: String, userPrompt: String, insertUserMessage: Boolean) {
        val assistantMsgId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        activeGenerationJob?.cancel()
        _isGenerating.value = true
        activeGenerationJob = scope.launch(Dispatchers.IO) {
            val thisJob = coroutineContext.job
            if (insertUserMessage) {
                chatDao.insertMessage(
                    ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        isUser = true,
                        text = userPrompt,
                        timestamp = now,
                        isComplete = true
                    )
                )
            }

            chatDao.insertMessage(
                ChatMessageEntity(
                    id = assistantMsgId,
                    sessionId = sessionId,
                    isUser = false,
                    text = "",
                    timestamp = now + 1,
                    isComplete = false
                )
            )

            chatDao.getSession(sessionId)?.let { session ->
                val isFirstMessage = session.messageCount == 0
                chatDao.updateSession(
                    session.copy(
                        title = if (isFirstMessage && session.title.equals("New chat", ignoreCase = true)) {
                            Markdown.preview(userPrompt, 40)
                        } else session.title,
                        preview = Markdown.preview(userPrompt, 60),
                        lastUpdated = now,
                        messageCount = session.messageCount + if (insertUserMessage) 2 else 1
                    )
                )
            }

            if (!orchestrator.awaitReady()) {
                chatDao.updateMessageContent(
                    assistantMsgId,
                    "The on-device AI model isn't loaded yet. Open **AI Model Management** from the Home banner to download or load Gemma 4 E2B, then try again.",
                    isComplete = true
                )
                _isGenerating.value = false
                return@launch
            }

            val accumulated = StringBuilder()
            var lastWrite = 0L
            try {
                orchestrator.streamAskAi(sessionId, userPrompt).collect { token ->
                    accumulated.append(token)
                    val nowMs = System.currentTimeMillis()
                    if (nowMs - lastWrite >= STREAM_WRITE_INTERVAL_MS) {
                        lastWrite = nowMs
                        chatDao.updateMessageContent(assistantMsgId, accumulated.toString(), isComplete = false)
                    }
                }
            } catch (e: CancellationException) {
                // Stopped by the user — keep whatever was generated.
            } catch (e: Exception) {
                if (accumulated.isBlank()) accumulated.append("Sorry, something went wrong while generating a reply: ${e.message}")
            } finally {
                withContext(NonCancellable) {
                    val finalText = accumulated.toString().trim().ifEmpty { "_Response stopped._" }
                    chatDao.updateMessageContent(assistantMsgId, finalText, isComplete = true)
                    chatDao.getSession(sessionId)?.let { session ->
                        chatDao.updateSession(session.copy(preview = Markdown.preview(finalText, 60)))
                    }
                    if (activeGenerationJob === thisJob) _isGenerating.value = false
                }
            }
        }
    }

    fun regenerateLastResponse(sessionId: String) {
        scope.launch(Dispatchers.IO) {
            val messages = chatDao.getMessages(sessionId)
            val lastUserMsg = messages.lastOrNull { it.isUser } ?: return@launch
            messages.filter { !it.isUser && it.timestamp > lastUserMsg.timestamp }
                .forEach { chatDao.deleteMessage(it.id) }
            startGeneration(sessionId, lastUserMsg.text, insertUserMessage = false)
        }
    }

    fun stopGeneration() {
        orchestrator.cancelGeneration()
        activeGenerationJob?.cancel()
    }

    private companion object {
        const val STREAM_WRITE_INTERVAL_MS = 80L
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
