package com.openfield.app.feature.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.core.model.UnifiedSearchResult
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ToolQuickMatch(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onSelect: () -> Unit
)

@Composable
fun GlobalSearchDialog(
    onDismiss: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToDocumentQA: () -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    onNavigateToImageAnalysis: () -> Unit,
    onNavigateToProblemSolver: () -> Unit,
    onNavigateToLearnExplain: () -> Unit,
    onNavigateToCreateWrite: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UnifiedSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val focusRequester = remember { FocusRequester() }

    val appTools = remember {
        listOf(
            ToolQuickMatch("Ask AI Chat", "Conversational AI assistant with Gemma 4", Icons.Default.SmartToy) {
                onDismiss()
                onNavigateToChat("")
            },
            ToolQuickMatch("Analyze Image", "Identify and inspect photos on-device", Icons.Default.Image) {
                onDismiss()
                onNavigateToImageAnalysis()
            },
            ToolQuickMatch("Ask Document", "Ask questions grounded in imported PDFs", Icons.Default.Description) {
                onDismiss()
                onNavigateToDocumentQA()
            },
            ToolQuickMatch("Solve a Problem", "Step-by-step diagnostic troubleshooter", Icons.Default.Speed) {
                onDismiss()
                onNavigateToProblemSolver()
            },
            ToolQuickMatch("Learn & Explain", "Interactive lessons and 3D flashcards", Icons.Default.AutoAwesome) {
                onDismiss()
                onNavigateToLearnExplain()
            },
            ToolQuickMatch("Create & Write", "Draft documents, summarize, and edit", Icons.AutoMirrored.Filled.Note) {
                onDismiss()
                onNavigateToCreateWrite()
            },
            ToolQuickMatch("Workflows & Tasks", "Automated checklists and action planning", Icons.Default.Speed) {
                onDismiss()
                onNavigateToCalculator()
            }
        )
    }

    val matchedTools = remember(query) {
        if (query.isBlank()) emptyList()
        else appTools.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }
    }

    LaunchedEffect(query) {
        searchJob?.cancel()
        if (query.trim().length >= 2) {
            isSearching = true
            searchJob = scope.launch {
                delay(120) // Debounce rapid typing
                val res = repository.unifiedSearchEngine.search(query.trim(), limit = 20)
                results = res
                isSearching = false
            }
        } else {
            results = emptyList()
            isSearching = false
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = OpenFieldBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Search bar header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search documents, notes, chats, tools…", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = OpenFieldSecondaryText, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OpenFieldPrimaryBlue,
                            unfocusedBorderColor = OpenFieldCardBorder,
                            focusedContainerColor = OpenFieldSecondaryBackground,
                            unfocusedContainerColor = OpenFieldSecondaryBackground
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OpenFieldPrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search state or results list
                when {
                    isSearching -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = OpenFieldPrimaryBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Searching local indexes…", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText))
                        }
                    }

                    query.isNotBlank() && results.isEmpty() && matchedTools.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = OpenFieldSecondaryText.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No local matches found", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText))
                            Text("Try searching with different keywords", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText))
                        }
                    }

                    query.isBlank() -> {
                        // Default starter view: quick tool shortcuts
                        Text(
                            text = "QUICK TOOLS & CAPABILITIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldSecondaryText,
                                letterSpacing = 0.5.sp,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(appTools) { tool ->
                                ToolResultRow(tool = tool)
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (matchedTools.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "MATCHED TOOLS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OpenFieldSecondaryText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                items(matchedTools) { tool ->
                                    ToolResultRow(tool = tool)
                                }
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }

                            if (results.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "LOCAL KNOWLEDGE & DOCUMENTS (${results.size})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OpenFieldSecondaryText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                items(results) { item ->
                                    SearchResultCard(
                                        item = item,
                                        onClick = {
                                            onDismiss()
                                            when (item.sourceType) {
                                                KnowledgeSourceType.DOCUMENT -> onNavigateToDocumentDetail(item.sourceId)
                                                KnowledgeSourceType.CONVERSATION -> onNavigateToChat(item.sourceId)
                                                KnowledgeSourceType.NOTE -> onNavigateToCreateWrite()
                                                KnowledgeSourceType.KNOWLEDGE_PACK -> onNavigateToDocumentQA()
                                                KnowledgeSourceType.LEARNING_TOPIC -> onNavigateToLearnExplain()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolResultRow(tool: ToolQuickMatch) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = tool.onSelect)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tool.icon, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tool.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText, fontSize = 14.sp))
                Text(text = tool.description, style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 11.5.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = OpenFieldSecondaryText.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun SearchResultCard(
    item: UnifiedSearchResult,
    onClick: () -> Unit
) {
    val (icon, tintColor, badgeText) = when (item.sourceType) {
        KnowledgeSourceType.DOCUMENT -> Triple(Icons.Default.Description, Color(0xFFE11D48), "DOCUMENT")
        KnowledgeSourceType.KNOWLEDGE_PACK -> Triple(Icons.AutoMirrored.Filled.MenuBook, Color(0xFF0D9488), "PACK")
        KnowledgeSourceType.NOTE -> Triple(Icons.AutoMirrored.Filled.Note, Color(0xFFD97706), "NOTE")
        KnowledgeSourceType.CONVERSATION -> Triple(Icons.Default.SmartToy, Color(0xFF2563EB), "CHAT")
        KnowledgeSourceType.LEARNING_TOPIC -> Triple(Icons.Default.AutoAwesome, Color(0xFF9333EA), "LEARNING")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tintColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.sourceTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText, fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.pageNumber != null) "$badgeText • P.${item.pageNumber}" else badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(color = tintColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.snippet,
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
