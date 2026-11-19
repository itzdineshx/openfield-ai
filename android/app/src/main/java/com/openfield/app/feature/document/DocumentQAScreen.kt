package com.openfield.app.feature.document

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.DocumentCard
import com.openfield.app.core.components.FeatureHeaderCard
import com.openfield.app.core.components.InlineErrorCard
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.components.SuggestionChips
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
import com.openfield.app.feature.voice.rememberVoiceInput

private val documentStarterQuestions = listOf(
    "What is this document about?",
    "List the key points",
    "What are the main steps or instructions?",
    "Are there any warnings or safety notes?"
)

@Composable
fun DocumentQAScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    onNavigateToCreateWrite: () -> Unit = {},
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
    val isQuerying by controller.isQuerying.collectAsState()

    var inputQuestion by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val voice = rememberVoiceInput { recognized ->
        inputQuestion = if (inputQuestion.isBlank()) recognized else "$inputQuestion $recognized"
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) controller.importPdf(context, uri)
    }
    val openPicker = { documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain")) }

    fun ask(question: String) {
        if (question.isBlank() || isQuerying) return
        controller.askQuestion(question)
        inputQuestion = ""
    }

    val headerItems = 1
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(headerItems + messages.lastIndex, Int.MAX_VALUE)
    }
    LaunchedEffect(messages.lastOrNull()?.text?.length, messages.lastOrNull()?.isComplete) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (messages.isNotEmpty() && lastVisible >= headerItems + messages.lastIndex - 1) {
            listState.scrollToItem(headerItems + messages.lastIndex, Int.MAX_VALUE)
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
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Document options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Import document") },
                            onClick = {
                                showMenu = false
                                openPicker()
                            },
                            leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null) }
                        )
                        if (documents.size > 1) {
                            DropdownMenuItem(
                                text = { Text("Search all documents (${documents.size})") },
                                onClick = {
                                    showMenu = false
                                    controller.selectAllDocuments()
                                }
                            )
                        }
                        if (activeDoc != null) {
                            DropdownMenuItem(
                                text = { Text("Re-index current document") },
                                onClick = {
                                    showMenu = false
                                    controller.reindexDocument(context, activeDoc.id)
                                },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Clear conversation") },
                            onClick = {
                                showMenu = false
                                controller.stopAnswer()
                                controller.clearMessages()
                            },
                            enabled = messages.isNotEmpty()
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (documents.isNotEmpty()) {
                val scopeHint = when {
                    selectedDocIds.size > 1 -> "Ask across ${selectedDocIds.size} documents…"
                    else -> "Ask another question…"
                }
                ChatBottomComposer(
                    value = inputQuestion,
                    onValueChange = { inputQuestion = it },
                    onSend = ::ask,
                    onAttach = openPicker,
                    onVoiceClick = voice.toggle,
                    isListening = voice.isListening,
                    onStopListening = voice.stop,
                    onCancelListening = voice.cancel,
                    isGenerating = isQuerying,
                    onStopGenerating = { controller.stopAnswer() },
                    placeholder = scopeHint,
                    modifier = Modifier.imePadding()
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenFieldBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (indexingState.isIndexing) {
                        IndexingCard(
                            name = indexingState.documentName,
                            step = indexingState.currentStep,
                            progress = indexingState.progressFraction,
                            onCancel = { controller.cancelIndexing() }
                        )
                    }

                    indexingState.error?.let { error ->
                        InlineErrorCard(
                            title = "Couldn't import that document",
                            message = error,
                            retryLabel = "Choose another file",
                            onRetry = openPicker,
                            onDismiss = { controller.dismissIndexingError() }
                        )
                    }

                    if (documents.isEmpty() && !indexingState.isIndexing) {
                        NoDocumentsState(onImport = openPicker)
                    }

                    if (documents.size > 1) {
                        ScopeSelector(
                            documents = documents.map { it.id to it.name },
                            selectedIds = selectedDocIds,
                            activeId = activeDoc?.id,
                            onToggleAll = {
                                if (selectedDocIds.size == documents.size) controller.clearDocumentSelection()
                                else controller.selectAllDocuments()
                            },
                            onSelect = { id ->
                                controller.toggleDocumentSelection(id)
                                controller.selectDocument(id)
                            }
                        )
                    }

                    if (activeDoc != null) {
                        DocumentCard(
                            fileName = activeDoc.name,
                            fileSize = activeDoc.size,
                            pageCount = activeDoc.pageCount,
                            onClick = { onNavigateToDocumentDetail(activeDoc.id) }
                        )

                        if (messages.isEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickAction(
                                    label = "Summarize",
                                    icon = Icons.Default.Summarize,
                                    enabled = !isQuerying,
                                    onClick = { controller.summarizeDocument(activeDoc.id) },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickAction(
                                    label = "Draft Note",
                                    icon = Icons.Default.EditNote,
                                    enabled = !isQuerying,
                                    onClick = {
                                        repository.sharedContextManager.handoffToWriter(
                                            title = activeDoc.name,
                                            initialDraft = activeDoc.summary.ifBlank { "Notes on ${activeDoc.name}" },
                                            sourceDocIds = listOf(activeDoc.id)
                                        )
                                        onNavigateToCreateWrite()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (documents.size >= 2) {
                                    val otherDoc = documents.firstOrNull { it.id != activeDoc.id }
                                    QuickAction(
                                        label = "Compare",
                                        icon = Icons.AutoMirrored.Filled.CompareArrows,
                                        enabled = !isQuerying && otherDoc != null,
                                        onClick = { otherDoc?.let { controller.compareDocuments(activeDoc.id, it.id) } },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try asking",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            SuggestionChips(suggestions = documentStarterQuestions, onClick = ::ask, enabled = !isQuerying)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    isUser = msg.isUser,
                    message = msg.text,
                    timestamp = msg.timestamp,
                    steps = msg.steps,
                    sourcePage = msg.sourcePage,
                    isComplete = msg.isComplete,
                    pendingLabel = "Searching your document…",
                    onSourceClick = { activeDoc?.let { onNavigateToDocumentDetail(it.id) } }
                )
            }
        }
    }
}

@Composable
private fun NoDocumentsState(onImport: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FeatureHeaderCard(
            icon = Icons.Default.Description,
            title = "Ask your documents",
            subtitle = "Import a PDF or text file. It's indexed on your device and answers cite the pages they come from."
        )
        PrimaryButton(text = "Import a document", onClick = onImport, leadingIcon = Icons.Default.UploadFile)
    }
}

@Composable
private fun IndexingCard(name: String, step: String, progress: Float, onCancel: () -> Unit) {
    Surface(
        shape = CardShape,
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name.ifBlank { "Importing document" },
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
                )
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = OpenFieldError, fontSize = 13.sp)
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = OpenFieldPrimaryBlue,
                trackColor = OpenFieldCardBorder
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = step,
                style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.sp)
            )
        }
    }
}

@Composable
private fun ScopeSelector(
    documents: List<Pair<String, String>>,
    selectedIds: Set<String>,
    activeId: String?,
    onToggleAll: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Search in",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = OpenFieldSecondaryText)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                val allSelected = selectedIds.size == documents.size
                ScopeChip("All (${documents.size})", allSelected, onToggleAll)
            }
            items(documents, key = { it.first }) { (id, name) ->
                val selected = selectedIds.contains(id) || (selectedIds.isEmpty() && id == activeId)
                ScopeChip(name, selected) { onSelect(id) }
            }
        }
    }
}

@Composable
private fun ScopeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) OpenFieldPrimaryBlueLight else OpenFieldBackground,
        border = BorderStroke(1.dp, if (selected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (selected) OpenFieldPrimaryBlue else OpenFieldNavyText,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.5.sp
            ),
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 7.dp)
                .widthIn(max = 160.dp)
        )
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) OpenFieldPrimaryBlue else OpenFieldSecondaryText,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) OpenFieldNavyText else OpenFieldSecondaryText
            )
        }
    }
}
