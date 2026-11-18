package com.openfield.app.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.EmptyState
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.model.ChatSession
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun HistoryScreen(
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val sessions by repository.sessions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sessionToDeleteId by remember { mutableStateOf<String?>(null) }

    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) sessions
        else sessions.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.preview.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Chats",
                canNavigateBack = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newId = repository.createNewSession("New Chat")
                    onNavigateToChat(newId)
                },
                containerColor = OpenFieldPrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Conversation")
            }
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenFieldBackground)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                placeholder = {
                    Text(
                        text = "Search conversations...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OpenFieldSecondaryText.copy(alpha = 0.6f)
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OpenFieldSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OpenFieldPrimaryBlue,
                    unfocusedBorderColor = OpenFieldCardBorder,
                    focusedContainerColor = OpenFieldBackground,
                    unfocusedContainerColor = OpenFieldBackground
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredSessions.isEmpty()) {
                EmptyState(
                    title = "No conversations found",
                    message = if (searchQuery.isBlank()) "Start a new conversation to ask questions offline." else "No matches for '$searchQuery'"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSessions, key = { it.id }) { session ->
                        ChatSessionCard(
                            session = session,
                            onClick = { onNavigateToChat(session.id) },
                            onDelete = { sessionToDeleteId = session.id }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = sessionToDeleteId != null,
        title = "Delete Conversation",
        message = "Are you sure you want to delete this conversation from history?",
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = {
            sessionToDeleteId?.let { repository.deleteSession(it) }
            sessionToDeleteId = null
        },
        onDismiss = { sessionToDeleteId = null }
    )
}

@Composable
fun ChatSessionCard(
    session: ChatSession,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OpenFieldPrimaryBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = OpenFieldPrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = OpenFieldNavyText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (session.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = session.preview,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OpenFieldSecondaryText,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${session.lastUpdated} • ${session.messageCount} messages",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OpenFieldSecondaryText.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = OpenFieldSecondaryText.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

