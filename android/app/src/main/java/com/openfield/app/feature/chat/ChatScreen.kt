package com.openfield.app.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.data.repository.OpenFieldRepository

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
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Scroll to bottom when new messages arrive or text streams
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "AI Chat",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
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
                    repository.sendMessage(sessionId, text)
                    inputMessage = ""
                },
                onAttach = { /* Attach picker invoked from Home / Library — no fake fill-in. */ },
                onVoiceClick = { /* Voice capture not implemented; no fake fill-in. */ },
                placeholder = "Ask a follow-up...",
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
                        sourcePage = msg.sourcePage
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
            repository.clearSessionMessages(sessionId)
            showClearDialog = false
        },
        onDismiss = { showClearDialog = false }
    )
}
