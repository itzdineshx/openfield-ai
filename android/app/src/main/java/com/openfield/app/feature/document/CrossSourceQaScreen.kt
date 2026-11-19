package com.openfield.app.feature.document

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChatBottomComposer
import com.openfield.app.core.components.ChatMessageBubble
import com.openfield.app.core.components.EmptyState
import com.openfield.app.core.components.FeatureHeaderCard
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.components.SuggestionChips
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.voice.rememberVoiceInput

private val crossSourceStarterQuestions = listOf(
    "Synthesize the key points across these sources",
    "Compare and contrast the main recommendations",
    "What are the common themes or findings?",
    "Identify any conflicting guidance or differences"
)

@Composable
fun CrossSourceQaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val controller = repository.crossSourceQaController
    val selectedSources by controller.selectedSources.collectAsState()
    val activeCollection by controller.activeCollection.collectAsState()
    val messages by controller.messages.collectAsState()
    val isGenerating by controller.isGenerating.collectAsState()
    val currentEvidence by controller.currentEvidence.collectAsState()
    val showEvidenceExplorer by controller.showEvidenceExplorer.collectAsState()

    var inputQuestion by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val voice = rememberVoiceInput { recognized ->
        inputQuestion = if (inputQuestion.isBlank()) recognized else "$inputQuestion $recognized"
    }

    fun ask(query: String) {
        if (query.isBlank() || isGenerating) return
        controller.askQuestion(query)
        inputQuestion = ""
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = activeCollection?.name ?: "Cross-Source Q&A",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (currentEvidence.isNotEmpty()) {
                        IconButton(onClick = { controller.toggleEvidenceExplorer(true) }) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = OpenFieldPrimaryBlue,
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    ) {
                                        Text(text = currentEvidence.size.toString(), fontSize = 10.sp)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FindInPage,
                                    contentDescription = "Evidence Explorer",
                                    tint = OpenFieldNavyText
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (selectedSources.isNotEmpty()) {
                val scopeHint = "Ask across ${selectedSources.size} sources…"
                ChatBottomComposer(
                    value = inputQuestion,
                    onValueChange = { inputQuestion = it },
                    onSend = ::ask,
                    onAttach = {},
                    onVoiceClick = voice.toggle,
                    isListening = voice.isListening,
                    onStopListening = voice.stop,
                    onCancelListening = voice.cancel,
                    isGenerating = isGenerating,
                    onStopGenerating = { controller.stopGeneration() },
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header item with sources
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sources chips shelf
                    if (selectedSources.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sources (${selectedSources.size}):",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            selectedSources.forEach { source ->
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = source.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        val icon = when (source.type) {
                                            KnowledgeSourceType.DOCUMENT -> Icons.Default.Description
                                            KnowledgeSourceType.KNOWLEDGE_PACK -> Icons.AutoMirrored.Filled.MenuBook
                                            KnowledgeSourceType.NOTE -> Icons.Default.Note
                                            KnowledgeSourceType.CONVERSATION -> Icons.Default.SmartToy
                                            KnowledgeSourceType.LEARNING_TOPIC -> Icons.Default.AutoAwesome
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = OpenFieldPrimaryBlue
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = OpenFieldSecondaryBackground,
                                        labelColor = OpenFieldNavyText
                                    ),
                                    border = BorderStroke(1.dp, OpenFieldCardBorder)
                                )
                            }
                        }
                    } else {
                        EmptyState(
                            title = "No Sources Selected",
                            message = "Select multiple documents or knowledge packs from the Knowledge Center to ask questions across them."
                        )
                    }

                    // Evidence button prompt if evidence is active
                    if (currentEvidence.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OpenFieldPrimaryBlueLight.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = OpenFieldPrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${currentEvidence.size} grounding passages retrieved",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = OpenFieldNavyText
                                        )
                                    )
                                }
                                OutlinedButton(
                                    onClick = { controller.toggleEvidenceExplorer(true) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, OpenFieldPrimaryBlue)
                                ) {
                                    Text(
                                        text = "Inspect Evidence",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OpenFieldPrimaryBlue
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (messages.isEmpty() && selectedSources.isNotEmpty()) {
                        FeatureHeaderCard(
                            icon = Icons.Default.AutoAwesome,
                            title = "Cross-Source Synthesis",
                            subtitle = "Ask questions that compare, synthesize, or contrast information across your selected documents and knowledge packs. Grounded citations cite exact titles and pages."
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Suggested queries:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        SuggestionChips(
                            suggestions = crossSourceStarterQuestions,
                            onClick = ::ask,
                            enabled = !isGenerating
                        )
                    }
                }
            }

            // Message list
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    isUser = msg.isUser,
                    message = msg.text,
                    timestamp = msg.timestamp,
                    steps = msg.steps,
                    sourcePage = msg.sourcePage,
                    isComplete = !isGenerating || msg.isUser,
                    pendingLabel = "Synthesizing across sources…"
                )
            }
        }
    }

    // Evidence Explorer Bottom Sheet
    if (showEvidenceExplorer && currentEvidence.isNotEmpty()) {
        EvidenceExplorerSheet(
            evidenceList = currentEvidence,
            onDismiss = { controller.toggleEvidenceExplorer(false) }
        )
    }
}
