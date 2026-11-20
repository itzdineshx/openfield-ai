package com.openfield.app.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.components.UnifiedOmnibar
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.voice.SpeechSynthesisManager
import com.openfield.app.feature.voice.rememberVoiceInput
import kotlinx.coroutines.launch

private data class ChatStarterCategory(
    val icon: String,
    val title: String,
    val prompt: String
)

private val chatStarters = listOf(
    ChatStarterCategory("💡", "Explain Simply", "Explain how a battery works in simple terms"),
    ChatStarterCategory("⚡", "Troubleshoot", "My phone battery drains very quickly — why?"),
    ChatStarterCategory("📚", "Study & Learn", "What are the most effective techniques to study for exams?"),
    ChatStarterCategory("✍️", "Draft & Outline", "Help me structure an outline for an introductory essay")
)

@Composable
fun ChatScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToStudio: (() -> Unit)? = null,
    onNavigateToTasks: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val allMessages by repository.messagesBySession.collectAsState()
    val messages = remember(allMessages, sessionId) { allMessages[sessionId] ?: emptyList() }
    val isGenerating by repository.chatController.isGenerating.collectAsState()
    val savedAnswers by repository.savedAnswers.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showScrollToBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            messages.isNotEmpty() && lastVisible < messages.lastIndex
        }
    }

    var inputMessage by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val voice = rememberVoiceInput { recognized ->
        inputMessage = if (inputMessage.isBlank()) recognized else "$inputMessage $recognized"
    }

    val speechManager = remember { SpeechSynthesisManager(context) }
    val isSpeaking by speechManager.isSpeaking.collectAsState()
    val currentUtteranceId by speechManager.currentUtteranceId.collectAsState()

    DisposableEffect(Unit) {
        onDispose { speechManager.release() }
    }

    fun send(text: String) {
        if (text.isBlank() || isGenerating) return
        val currentSettings = repository.settings.value
        val structuredMemory = repository.localMemoryManager.buildContextForPrompt()
        val combinedMemory = listOf(currentSettings.userCustomMemory, structuredMemory)
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .ifBlank { null }

        repository.chatController.sendMessage(
            sessionId = sessionId,
            userPrompt = text,
            language = currentSettings.language,
            responseLength = currentSettings.preferredResponseLength,
            explanationLevel = currentSettings.preferredExplanationLevel,
            measurementUnits = currentSettings.preferredMeasurementUnits,
            customMemory = combinedMemory
        )
        inputMessage = ""
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex, Int.MAX_VALUE)
    }
    LaunchedEffect(messages.lastOrNull()?.text?.length, messages.lastOrNull()?.isComplete) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (messages.isNotEmpty() && lastVisible >= messages.lastIndex - 1) {
            listState.scrollToItem(messages.lastIndex, Int.MAX_VALUE)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "AI Chat",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        repository.chatController.stopGeneration()
                        onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = "New conversation")
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Chat options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Regenerate response") },
                            onClick = {
                                showMenu = false
                                repository.chatController.regenerateLastResponse(sessionId)
                            },
                            leadingIcon = { Icon(Icons.Default.Autorenew, contentDescription = null) },
                            enabled = messages.any { it.isUser } && !isGenerating
                        )
                        DropdownMenuItem(
                            text = { Text("Clear messages") },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            enabled = messages.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text("Delete conversation") },
                            onClick = {
                                showMenu = false
                                repository.chatController.stopGeneration()
                                repository.deleteSession(sessionId)
                                onNavigateBack()
                            },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            UnifiedOmnibar(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                onSend = { prompt, imageUri, scopeId, scopeTitle, mode ->
                    if ((prompt.isNotBlank() || imageUri != null) && !isGenerating) {
                        val currentSettings = repository.settings.value
                        val structuredMemory = repository.localMemoryManager.buildContextForPrompt()
                        val combinedMemory = listOf(currentSettings.userCustomMemory, structuredMemory)
                            .filter { it.isNotBlank() }
                            .joinToString("\n")
                            .ifBlank { null }

                        repository.chatController.sendMessage(
                            sessionId = sessionId,
                            userPrompt = prompt,
                            language = currentSettings.language,
                            responseLength = currentSettings.preferredResponseLength,
                            explanationLevel = currentSettings.preferredExplanationLevel,
                            measurementUnits = currentSettings.preferredMeasurementUnits,
                            customMemory = combinedMemory,
                            imageUri = imageUri,
                            knowledgeScopeId = scopeId,
                            knowledgeScopeTitle = scopeTitle,
                            mode = mode
                        )
                        inputMessage = ""
                    }
                },
                onVoiceClick = voice.toggle,
                isListening = voice.isListening,
                onStopListening = voice.stop,
                onCancelListening = voice.cancel,
                isGenerating = isGenerating,
                onStopGenerating = { repository.chatController.stopGeneration() },
                placeholder = "Ask a follow-up, attach photo or pack…",
                onNavigateToStudio = onNavigateToStudio,
                modifier = Modifier.imePadding()
            )
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenFieldBackground)
        ) {
            if (messages.isEmpty()) {
                ChatEmptyState(onSuggestion = ::send)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val existingSaved = savedAnswers.find { it.answer == msg.text }
                        val isSaved = existingSaved != null

                        ChatMessageBubble(
                            isUser = msg.isUser,
                            message = msg.text,
                            timestamp = msg.timestamp,
                            steps = msg.steps,
                            imageUri = msg.imageUri,
                            sourcePage = msg.sourcePage,
                            isComplete = msg.isComplete,
                            onSpeakClick = if (!msg.isUser && msg.text.isNotBlank()) {
                                {
                                    if (isSpeaking && currentUtteranceId == msg.id) speechManager.stop()
                                    else speechManager.speak(msg.text, msg.id)
                                }
                            } else null,
                            isSpeaking = isSpeaking && currentUtteranceId == msg.id,
                            isSaved = isSaved,
                            onSaveClick = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                {
                                    if (existingSaved != null) {
                                        repository.deleteSavedAnswer(existingSaved.id)
                                        android.widget.Toast.makeText(context, "Removed from Saved Answers", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        val idx = messages.indexOf(msg)
                                        val prompt = if (idx > 0) messages[idx - 1].text else "Chat Question"
                                        repository.saveAnswer(
                                            question = prompt,
                                            answer = msg.text,
                                            sourceTitle = "Chat Conversation",
                                            sourceType = "CHAT"
                                        )
                                        android.widget.Toast.makeText(context, "Saved to Library", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null,
                            onShareClick = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, msg.text)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Answer"))
                                }
                            } else null,
                            onOpenInStudio = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                { text ->
                                    repository.sharedContextManager.handoffToWriter(
                                        title = "Draft from Chat",
                                        initialDraft = text
                                    )
                                    onNavigateToStudio?.invoke()
                                }
                            } else null,
                            onCreateChecklist = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                { text ->
                                    scope.launch {
                                        val lines = text.lines()
                                            .map { it.trim() }
                                            .filter { it.matches(Regex("^(?:\\d+\\.|[-*•])\\s+.+")) }
                                            .map { it.replaceFirst(Regex("^(?:\\d+\\.|[-*•])\\s+"), "") }
                                        val subtasks = if (lines.isNotEmpty()) lines.take(10) else listOf("Review generated response", "Follow up on action items")
                                        val firstLine = text.lines().firstOrNull { it.isNotBlank() }?.take(40) ?: "Chat Action Plan"
                                        repository.createTask(
                                            title = firstLine.replace(Regex("^#+\\s*"), ""),
                                            description = text.take(150),
                                            priority = "MEDIUM",
                                            subtaskTitles = subtasks
                                        )
                                        android.widget.Toast.makeText(context, "Checklist created in Workflows & Tasks!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null,
                            onAskQuiz = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                { text ->
                                    val topic = text.lines().firstOrNull { it.isNotBlank() }?.take(50)?.replace(Regex("^#+\\s*"), "") ?: "the topic above"
                                    send("Give me a quick 3-question interactive quiz to test my understanding of: $topic")
                                }
                            } else null,
                            onTranslate = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                { text ->
                                    send("Translate your previous response clearly into Spanish (or natural everyday phrasing):\n\n${text.take(300)}")
                                }
                            } else null
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showScrollToBottom,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = OpenFieldPrimaryBlue,
                        shadowElevation = 4.dp,
                        modifier = Modifier.clickable {
                            scope.launch { listState.animateScrollToItem(messages.lastIndex) }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Scroll to latest",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isGenerating) "New response" else "Latest",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showClearDialog,
        title = "Clear conversation",
        message = "Remove all messages in this conversation?",
        confirmText = "Clear",
        isDestructive = true,
        onConfirm = {
            repository.chatController.stopGeneration()
            repository.chatController.clearSession(sessionId)
            showClearDialog = false
        },
        onDismiss = { showClearDialog = false }
    )
}

@Composable
private fun ChatEmptyState(onSuggestion: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        OpenFieldLogo(size = 56.dp)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ask me anything",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "On-Device Gemma 4 • Complete Privacy • 100% Offline",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OpenFieldPrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            chatStarters.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = OpenFieldSecondaryBackground,
                    border = BorderStroke(1.dp, OpenFieldCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestion(item.prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OpenFieldPrimaryBlueLight,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(item.icon, fontSize = 18.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OpenFieldNavyText,
                                    fontSize = 13.5.sp
                                )
                            )
                            Text(
                                text = item.prompt,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
