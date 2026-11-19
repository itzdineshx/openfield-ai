package com.openfield.app.feature.document

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.DocumentCard
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun DocumentQAScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val controller = repository.documentQaController
    val documents by controller.documents.collectAsState()
    val activeDocId by controller.activeDocumentId.collectAsState()
    val selectedDocIds by controller.selectedDocumentIds.collectAsState()
    val activeDoc = documents.find { it.id == activeDocId } ?: documents.firstOrNull()
    val messages by controller.messages.collectAsState()
    val indexingState by controller.indexingState.collectAsState()

    var inputQuestion by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // SAF Document Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            controller.importPdf(context, uri)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Ask Document",
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
                            text = { Text("Select All Documents (${documents.size})") },
                            onClick = {
                                showMenu = false
                                controller.selectAllDocuments()
                            },
                            enabled = documents.isNotEmpty()
                        )
                        if (activeDoc != null) {
                            DropdownMenuItem(
                                text = { Text("Re-index Current Document") },
                                onClick = {
                                    showMenu = false
                                    controller.reindexDocument(context, activeDoc.id)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Clear Messages") },
                            onClick = {
                                showMenu = false
                                controller.clearMessages()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            val scopeHint = when {
                selectedDocIds.size > 1 -> "Ask across ${selectedDocIds.size} documents..."
                activeDoc != null -> "Ask about ${activeDoc.name}..."
                else -> "Import a document first..."
            }

            ChatBottomComposer(
                value = inputQuestion,
                onValueChange = { inputQuestion = it },
                onSend = { question ->
                    if (question.isNotBlank()) {
                        controller.askQuestion(question)
                        inputQuestion = ""
                    }
                },
                onAttach = {
                    documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                },
                onVoiceClick = { },
                placeholder = scopeHint,
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

                // Indexing Progress Card
                if (indexingState.isIndexing) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = indexingState.documentName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                TextButton(onClick = { controller.cancelIndexing() }) {
                                    Text("Cancel", color = OpenFieldError, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { indexingState.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = OpenFieldPrimaryBlue,
                                trackColor = OpenFieldCardBorder
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = indexingState.currentStep,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Multi-Document Scope Selector (if 2 or more documents)
                if (documents.size > 1) {
                    Text(
                        text = "Search Scope:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isAllSelected = selectedDocIds.size == documents.size
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAllSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, if (isAllSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
                                modifier = Modifier.clickable {
                                    if (isAllSelected) controller.clearDocumentSelection()
                                    else controller.selectAllDocuments()
                                }
                            ) {
                                Text(
                                    text = "All (${documents.size})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isAllSelected) Color.White else OpenFieldNavyText,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        items(documents) { doc ->
                            val isSelected = selectedDocIds.contains(doc.id) || (selectedDocIds.isEmpty() && doc.id == activeDoc?.id)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) OpenFieldPrimaryBlueLight else OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
                                modifier = Modifier.clickable {
                                    controller.toggleDocumentSelection(doc.id)
                                    controller.selectDocument(doc.id)
                                }
                            ) {
                                Text(
                                    text = doc.name.take(18),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldNavyText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Active Document Info Card
                if (activeDoc != null) {
                    DocumentCard(
                        fileName = activeDoc.name,
                        fileSize = activeDoc.size,
                        pageCount = activeDoc.pageCount,
                        onClick = { onNavigateToDocumentDetail(activeDoc.id) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Document Utilities Row (Summarize & Compare)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OpenFieldSecondaryBackground,
                            border = BorderStroke(1.dp, OpenFieldCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { controller.summarizeDocument(activeDoc.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Summarize,
                                    contentDescription = null,
                                    tint = OpenFieldPrimaryBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Summarize doc",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OpenFieldNavyText
                                )
                            }
                        }

                        if (documents.size >= 2) {
                            val otherDoc = documents.find { it.id != activeDoc.id }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (otherDoc != null) {
                                            controller.compareDocuments(activeDoc.id, otherDoc.id)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                        contentDescription = null,
                                        tint = OpenFieldPrimaryBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Compare docs",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = OpenFieldNavyText
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    isUser = msg.isUser,
                    message = msg.text,
                    timestamp = msg.timestamp,
                    steps = msg.steps,
                    sourcePage = msg.sourcePage,
                    isComplete = msg.isComplete,
                    onSourceClick = {
                        activeDoc?.let { onNavigateToDocumentDetail(it.id) }
                    }
                )
            }
        }
    }
}
