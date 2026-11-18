package com.openfield.app.feature.document

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.DocumentCard
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.model.ChatMessage
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.data.repository.OpenFieldRepository
import java.util.UUID

@Composable
fun DocumentQAScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val documents by repository.documents.collectAsState()
    val activeDoc = documents.firstOrNull()

    var inputQuestion by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val conversationMessages = remember { mutableStateListOf<ChatMessage>() }

    // SAF Document Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            repository.importRealDocument(context, uri)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Document Q&A",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Document options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import PDF Document") },
                            onClick = {
                                showMenu = false
                                documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                            },
                            leadingIcon = {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Extracted Summary") },
                            onClick = {
                                showMenu = false
                                activeDoc?.let { onNavigateToDocumentDetail(it.id) }
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomComposer(
                value = inputQuestion,
                onValueChange = { inputQuestion = it },
                onSend = { question ->
                    if (question.isNotBlank()) {
                        val currentDoc = activeDoc
                        conversationMessages.add(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                isUser = true,
                                text = question,
                                timestamp = "Just now"
                            )
                        )
                        val assistantMsgId = UUID.randomUUID().toString()
                        conversationMessages.add(
                            ChatMessage(
                                id = assistantMsgId,
                                isUser = false,
                                text = "Searching document...",
                                timestamp = "Just now"
                            )
                        )
                        inputQuestion = ""

                        if (currentDoc == null) {
                            val idx = conversationMessages.indexOfFirst { it.id == assistantMsgId }
                            if (idx != -1) {
                                conversationMessages[idx] = conversationMessages[idx].copy(
                                    text = "No document is currently selected. Please import a PDF or document using the attachment icon (+)."
                                )
                            }
                        } else {
                            scope.launch {
                                var accumulated = ""
                                try {
                                    repository.documentQaUseCase.askQuestionStream(currentDoc.id, question).collect { token ->
                                        accumulated += token
                                        val idx = conversationMessages.indexOfFirst { it.id == assistantMsgId }
                                        if (idx != -1) {
                                            conversationMessages[idx] = conversationMessages[idx].copy(text = accumulated)
                                        }
                                    }
                                } catch (e: Exception) {
                                    val idx = conversationMessages.indexOfFirst { it.id == assistantMsgId }
                                    if (idx != -1) {
                                        conversationMessages[idx] = conversationMessages[idx].copy(
                                            text = if (accumulated.isNotBlank()) accumulated else "Error querying document: ${e.message}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                onAttach = {
                    documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                },
                onVoiceClick = { /* Voice capture not implemented; no fake fill-in. */ },
                placeholder = "Ask another question...",
                modifier = Modifier.imePadding()
            )
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenFieldBackground)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Active Document Info Card
                if (activeDoc != null) {
                    DocumentCard(
                        fileName = activeDoc.name,
                        fileSize = activeDoc.size,
                        pageCount = activeDoc.pageCount,
                        onClick = { onNavigateToDocumentDetail(activeDoc.id) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            items(conversationMessages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    isUser = msg.isUser,
                    message = msg.text,
                    timestamp = msg.timestamp,
                    steps = msg.steps,
                    sourcePage = msg.sourcePage,
                    onSourceClick = {
                        activeDoc?.let { onNavigateToDocumentDetail(it.id) }
                    }
                )
            }
        }
    }
}

