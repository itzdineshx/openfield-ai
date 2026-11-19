package com.openfield.app.feature.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.voice.SpeechSynthesisManager
import com.openfield.app.feature.voice.VoiceInputManager
import com.openfield.app.feature.voice.VoiceInputState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val allMessages by repository.messagesBySession.collectAsState()
    val messages = remember(allMessages, sessionId) { allMessages[sessionId] ?: emptyList() }
    val isGenerating by repository.chatController.isGenerating.collectAsState()
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val voiceInputManager = remember { VoiceInputManager(context) }
    val voiceState by voiceInputManager.state.collectAsState()
    val isListening = voiceState is VoiceInputState.Listening

    val speechManager = remember { SpeechSynthesisManager(context) }
    val isSpeaking by speechManager.isSpeaking.collectAsState()
    val currentUtteranceId by speechManager.currentUtteranceId.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            voiceInputManager.release()
            speechManager.release()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceInputManager.startListening { recognizedText ->
                inputMessage = if (inputMessage.isBlank()) recognizedText else "$inputMessage $recognizedText"
            }
        }
    }

    // Scroll to bottom when new messages arrive or text streams
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Ask AI",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (isGenerating) {
                        IconButton(onClick = { repository.chatController.stopGeneration() }) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop generation",
                                tint = OpenFieldError
                            )
                        }
                    }

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Chat options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Regenerate Response") },
                            onClick = {
                                showMenu = false
                                repository.chatController.regenerateLastResponse(sessionId)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Autorenew, contentDescription = null)
                            },
                            enabled = messages.isNotEmpty() && !isGenerating
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Messages") },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Conversation") },
                            onClick = {
                                showMenu = false
                                repository.deleteSession(sessionId)
                                onNavigateBack()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomComposer(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                onSend = { text ->
                    repository.chatController.sendMessage(sessionId, text)
                    inputMessage = ""
                },
                onAttach = { /* Attach picker invoked from Home / Library */ },
                onVoiceClick = {
                    if (isListening) {
                        voiceInputManager.stopListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                isListening = isListening,
                onStopListening = { voiceInputManager.stopListening() },
                onCancelListening = { voiceInputManager.cancelListening() },
                placeholder = "Ask a question...",
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
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        isUser = msg.isUser,
                        message = msg.text,
                        timestamp = msg.timestamp,
                        steps = msg.steps,
                        imageUri = msg.imageUri,
                        sourcePage = msg.sourcePage,
                        isComplete = msg.isComplete,
                        onSpeakClick = if (!msg.isUser) {
                            {
                                if (isSpeaking && currentUtteranceId == msg.id) {
                                    speechManager.stop()
                                } else {
                                    speechManager.speak(msg.text, msg.id)
                                }
                            }
                        } else null,
                        isSpeaking = isSpeaking && currentUtteranceId == msg.id
                    )
                }
            }
        }
    }

    ConfirmationDialog(
        show = showClearDialog,
        title = "Clear Conversation",
        message = "Are you sure you want to clear all messages in this conversation?",
        confirmText = "Clear",
        isDestructive = true,
        onConfirm = {
            repository.chatController.clearSession(sessionId)
            showClearDialog = false
        },
        onDismiss = { showClearDialog = false }
    )
}
