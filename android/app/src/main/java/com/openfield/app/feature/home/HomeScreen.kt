package com.openfield.app.feature.home

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.components.UnifiedOmnibar
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.engine.AiEngineState
import com.openfield.app.feature.ai.model.ModelInstallationState
import com.openfield.app.feature.ai.model.ModelManagementDialog
import com.openfield.app.feature.voice.SpeechSynthesisManager
import com.openfield.app.feature.voice.rememberVoiceInput
import kotlinx.coroutines.launch

/**
 * Modern, Minimalist ChatGPT-Style Home Screen.
 * Unifies the entire OpenField AI experience into one clean conversational canvas.
 */
@Composable
fun HomeScreen(
    initialSessionId: String? = null,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToImageAnalysis: () -> Unit = {},
    onNavigateToDocumentQA: () -> Unit = {},
    onNavigateToProblemSolver: () -> Unit = {},
    onNavigateToLearnExplain: () -> Unit = {},
    onNavigateToCreateWrite: () -> Unit = {},
    onOpenProfileMenu: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToModelSetup: () -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeSessionId by remember(initialSessionId) { mutableStateOf(initialSessionId) }

    val allMessages by repository.messagesBySession.collectAsState()
    val messages = remember(allMessages, activeSessionId) {
        activeSessionId?.let { allMessages[it] } ?: emptyList()
    }

    val isGenerating by repository.chatController.isGenerating.collectAsState()
    val savedAnswers by repository.savedAnswers.collectAsState()
    val modelState by repository.modelRepository.installationState.collectAsState()
    val engineState by repository.aiEngine.engineState.collectAsState()

    val listState = rememberLazyListState()
    var inputMessage by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }

    val voice = rememberVoiceInput { recognized ->
        inputMessage = if (inputMessage.isBlank()) recognized else "$inputMessage $recognized"
    }

    val speechManager = remember { SpeechSynthesisManager(context) }
    val isSpeaking by speechManager.isSpeaking.collectAsState()
    val currentUtteranceId by speechManager.currentUtteranceId.collectAsState()

    DisposableEffect(Unit) {
        onDispose { speechManager.release() }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            messages.isNotEmpty() && lastVisible < messages.lastIndex
        }
    }

    fun send(
        text: String,
        imageUri: String? = null,
        scopeId: String? = null,
        scopeTitle: String? = null,
        mode: String? = null
    ) {
        if ((text.isBlank() && imageUri == null) || isGenerating) return
        val currentSettings = repository.settings.value
        val structuredMemory = repository.localMemoryManager.buildContextForPrompt()
        val combinedMemory = listOf(currentSettings.userCustomMemory, structuredMemory)
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .ifBlank { null }

        scope.launch {
            val targetSessionId = activeSessionId ?: repository.createNewSessionNow(
                if (text.isNotBlank()) text.take(30) else (scopeTitle ?: "AI Analysis")
            ).also {
                activeSessionId = it
            }

            repository.chatController.sendMessage(
                sessionId = targetSessionId,
                userPrompt = text,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Navigation Drawer Hamburger Button
                IconButton(onClick = { onOpenDrawer?.invoke() }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open navigation drawer",
                        tint = OpenFieldNavyText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Center: Minimal Model & Offline Pill
                Surface(
                    onClick = {
                        if (modelState !is ModelInstallationState.Installed) onNavigateToModelSetup()
                        else showModelDialog = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = OpenFieldSecondaryBackground,
                    border = BorderStroke(1.dp, OpenFieldCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CRUX",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = OpenFieldNavyText
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OpenFieldPrimaryBlueLight
                        ) {
                            Text(
                                text = "Gemma 4 • Offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Right: New Chat [✏️] & Overflow [⋮]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        repository.chatController.stopGeneration()
                        activeSessionId = null
                        inputMessage = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "New chat",
                            tint = OpenFieldNavyText,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Chat options",
                                tint = OpenFieldNavyText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Regenerate response") },
                                onClick = {
                                    showMenu = false
                                    activeSessionId?.let { repository.chatController.regenerateLastResponse(it) }
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
                                    activeSessionId?.let { repository.deleteSession(it) }
                                    activeSessionId = null
                                },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                                enabled = activeSessionId != null
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            UnifiedOmnibar(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                onSend = { prompt, imageUri, scopeId, scopeTitle, mode ->
                    send(prompt, imageUri, scopeId, scopeTitle, mode)
                },
                onVoiceClick = voice.toggle,
                isListening = voice.isListening,
                onStopListening = voice.stop,
                onCancelListening = voice.cancel,
                isGenerating = isGenerating,
                onStopGenerating = { repository.chatController.stopGeneration() },
                placeholder = if (messages.isEmpty()) "Ask anything, attach image or pack…" else "Ask a follow-up…",
                onNavigateToStudio = onNavigateToCreateWrite,
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
                // ChatGPT Centered Empty Canvas
                ChatGptEmptyCanvas(onSuggestionClick = { prompt -> send(prompt) })
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
                            mode = msg.mode,
                            onFollowUpAction = { actionText -> send(actionText) },
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
                                        Toast.makeText(context, "Removed from Saved Answers", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val idx = messages.indexOf(msg)
                                        val prompt = if (idx > 0) messages[idx - 1].text else "Chat Question"
                                        repository.saveAnswer(
                                            question = prompt,
                                            answer = msg.text,
                                            sourceTitle = "Chat Conversation",
                                            sourceType = "CHAT"
                                        )
                                        Toast.makeText(context, "Saved to Library", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null,
                            onShareClick = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, msg.text)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Answer"))
                                }
                            } else null,
                            onOpenInStudio = if (!msg.isUser && msg.text.isNotBlank() && msg.isComplete) {
                                { text ->
                                    repository.sharedContextManager.handoffToWriter(
                                        title = "Draft from Chat",
                                        initialDraft = text
                                    )
                                    onNavigateToCreateWrite()
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
                                        Toast.makeText(context, "Checklist created in Workflows & Tasks!", Toast.LENGTH_SHORT).show()
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
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
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
        title = "Clear conversation?",
        message = "This will remove all messages from this conversation. The conversation will remain in your history as empty.",
        confirmText = "Clear",
        onConfirm = {
            activeSessionId?.let { repository.clearSessionMessages(it) }
            showClearDialog = false
        },
        onDismiss = { showClearDialog = false }
    )

    if (showModelDialog) {
        ModelManagementDialog(
            modelRepository = repository.modelRepository,
            onDismissRequest = { showModelDialog = false }
        )
    }
}

/**
 * Centered ChatGPT Empty State with prompt starter cards.
 */
@Composable
private fun ChatGptEmptyCanvas(
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        OpenFieldLogo(size = 56.dp, showText = false)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "How can I help you today?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                color = OpenFieldNavyText,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "100% On-Device AI • Private & Offline",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = OpenFieldSecondaryText,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 4 Sleek Prompt Starter Cards in a 2x2 grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChatGptStarterCard(
                    icon = "💡",
                    title = "Explain a concept",
                    subtitle = "Explain how batteries work simply",
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClick("Explain how a battery works in simple terms") }
                )
                ChatGptStarterCard(
                    icon = "🩺",
                    title = "First aid advice",
                    subtitle = "Immediate steps to treat burns",
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClick("What are the immediate first aid steps to treat severe burns?") }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChatGptStarterCard(
                    icon = "⚡",
                    title = "Troubleshoot",
                    subtitle = "Why is phone battery draining fast?",
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClick("My phone battery drains very quickly — how do I diagnose and fix it?") }
                )
                ChatGptStarterCard(
                    icon = "✍️",
                    title = "Draft an outline",
                    subtitle = "Structure a concise project note",
                    modifier = Modifier.weight(1f),
                    onClick = { onSuggestionClick("Help me structure a clear, concise outline for a project summary") }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChatGptStarterCard(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = OpenFieldNavyText,
                    fontSize = 13.5.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OpenFieldSecondaryText,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
