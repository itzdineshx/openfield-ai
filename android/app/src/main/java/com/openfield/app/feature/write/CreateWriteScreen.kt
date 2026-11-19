package com.openfield.app.feature.write

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ChoicePill
import com.openfield.app.core.components.FeatureHeaderCard
import com.openfield.app.core.components.InlineErrorCard
import com.openfield.app.core.components.MarkdownContent
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.components.SectionTitle
import com.openfield.app.core.components.TypingIndicator
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldDivider
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.controller.DraftExportFormat
import com.openfield.app.feature.ai.controller.WritingController
import com.openfield.app.feature.ai.mode.WritingIntent
import com.openfield.app.feature.ai.mode.WritingTone

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CreateWriteScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val controller = repository.writingController
    val clipboardManager = LocalClipboardManager.current

    val intent by controller.selectedIntent.collectAsState()
    val tone by controller.selectedTone.collectAsState()
    val promptInput by controller.promptInput.collectAsState()
    val sourceText by controller.sourceText.collectAsState()
    val docContent by controller.documentContent.collectAsState()
    val docTitle by controller.documentTitle.collectAsState()
    val isGenerating by controller.isGenerating.collectAsState()
    val errorMsg by controller.errorMessage.collectAsState()
    val savedDrafts by controller.savedDrafts.collectAsState()
    val previousContent by controller.previousContent.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    val documentRequester = remember { BringIntoViewRequester() }
    val usesSource = intent in WritingController.SOURCE_INTENTS
    val canGenerate = !isGenerating && (promptInput.isNotBlank() || (usesSource && sourceText.isNotBlank()))
    val wordCount = remember(docContent) { docContent.split(Regex("\\s+")).count { it.isNotBlank() } }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            isEditing = false
            documentRequester.bringIntoView()
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Create & Write",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (isGenerating) {
                        IconButton(onClick = { controller.stopGeneration() }) {
                            Icon(Icons.Default.StopCircle, contentDescription = "Stop writing", tint = OpenFieldError)
                        }
                    }
                    IconButton(onClick = { controller.clearAll() }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Start over")
                    }
                }
            )
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FeatureHeaderCard(
                icon = Icons.Default.AutoAwesome,
                title = "Create & Write",
                subtitle = "Draft emails, reports and messages — or polish text you already have."
            )

            if (savedDrafts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Recent drafts", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontWeight = FontWeight.Medium))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        savedDrafts.take(8).forEach { draft ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isGenerating) {
                                        controller.loadDraft(draft)
                                        isEditing = false
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        draft.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = OpenFieldNavyText)
                                    )
                                    Text(
                                        "${draft.wordCount} words",
                                        style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SectionTitle("What are you writing?")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WritingIntent.entries.forEach { item ->
                    ChoicePill(label = item.displayName, selected = intent == item, onClick = { controller.setIntent(item) }, enabled = !isGenerating)
                }
            }

            SectionTitle("Tone")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WritingTone.entries.forEach { t ->
                    ChoicePill(label = t.displayName, selected = tone == t, onClick = { controller.setTone(t) }, enabled = !isGenerating)
                }
            }

            if (usesSource) {
                SectionTitle("Your text")
                OutlinedTextField(
                    value = sourceText,
                    onValueChange = controller::setSourceText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Hint("Paste the text you want to improve…") },
                    colors = fieldColors(OpenFieldSecondaryBackground)
                )
            }

            SectionTitle(if (usesSource) "Instructions (optional)" else "What should it say?")
            OutlinedTextField(
                value = promptInput,
                onValueChange = controller::setPrompt,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (usesSource) 70.dp else 110.dp),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Hint(instructionHint(intent)) },
                colors = fieldColors(OpenFieldBackground)
            )

            PrimaryButton(
                text = if (isGenerating) "Writing…" else if (docContent.isBlank()) "Write it" else "Write a new version",
                onClick = {
                    isEditing = false
                    controller.generateDraft()
                },
                leadingIcon = Icons.Default.AutoAwesome,
                enabled = canGenerate
            )

            errorMsg?.let { InlineErrorCard(title = "Writing problem", message = it) }

            // Document
            Surface(
                shape = CardShape,
                color = OpenFieldBackground,
                border = BorderStroke(1.dp, OpenFieldCardBorder),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(documentRequester)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = controller::updateDocumentTitle,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OpenFieldCardBorder,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "$wordCount words",
                            style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 12.sp)
                        )
                    }

                    ModeToggle(isEditing = isEditing, enabled = !isGenerating, onChange = { isEditing = it })

                    Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(OpenFieldDivider))

                    when {
                        isEditing -> OutlinedTextField(
                            value = docContent,
                            onValueChange = controller::updateDocumentContent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 240.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp, lineHeight = 22.sp, color = OpenFieldNavyText),
                            placeholder = { Hint("Type or paste your text here.") },
                            colors = fieldColors(OpenFieldBackground)
                        )
                        docContent.isBlank() && isGenerating -> TypingIndicator(label = "Writing on your device…")
                        docContent.isBlank() -> Text(
                            "Your draft will appear here. Choose a format, describe what you need, and tap Write it.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldSecondaryText, fontSize = 14.sp, lineHeight = 21.sp),
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                        else -> MarkdownContent(text = docContent, modifier = Modifier.fillMaxWidth())
                    }

                    if (docContent.isNotBlank()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DocAction("Copy", Icons.Default.ContentCopy, enabled = !isGenerating) {
                                clipboardManager.setText(AnnotatedString(controller.plainTextContent()))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }
                            DocAction("Share", Icons.Default.Share, enabled = !isGenerating) { controller.shareDraft(context) }
                            DocAction("Save", Icons.Default.Save, enabled = !isGenerating) {
                                controller.saveDraft()
                                Toast.makeText(context, "Draft saved", Toast.LENGTH_SHORT).show()
                            }
                            DocAction("Export .md", Icons.Default.Download, enabled = !isGenerating) {
                                controller.exportDraftToFile(context, DraftExportFormat.MARKDOWN)?.let {
                                    Toast.makeText(context, "Saved to ${it.name}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            if (previousContent != null) {
                                DocAction("Undo", Icons.AutoMirrored.Filled.Undo, enabled = !isGenerating) { controller.undoLastChange() }
                            }
                        }

                        Text("Improve it", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontWeight = FontWeight.Medium))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Shorter" to "shorter",
                                "More detail" to "expand",
                                "More professional" to "professional",
                                "Fix grammar" to "grammar",
                                "Clearer" to "clarity"
                            ).forEach { (label, type) ->
                                ChoicePill(
                                    label = label,
                                    selected = false,
                                    enabled = !isGenerating,
                                    onClick = {
                                        isEditing = false
                                        controller.refineContent(type)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModeToggle(isEditing: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(OpenFieldSecondaryBackground)
            .padding(3.dp)
    ) {
        listOf(false to "Preview", true to "Edit").forEach { (editing, label) ->
            val selected = isEditing == editing
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) OpenFieldPrimaryBlue else OpenFieldSecondaryText,
                    fontSize = 13.sp
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) OpenFieldBackground else Color.Transparent)
                    .clickable(enabled = enabled) { onChange(editing) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun DocAction(label: String, icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (enabled) OpenFieldNavyText else OpenFieldSecondaryText, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (enabled) OpenFieldNavyText else OpenFieldSecondaryText)
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = OpenFieldSecondaryText.copy(alpha = 0.7f)))
}

private fun instructionHint(intent: WritingIntent): String = when (intent) {
    WritingIntent.EMAIL -> "Who it's to and what you need, e.g. \"Ask my manager for Friday off for a family event\""
    WritingIntent.REPORT -> "Topic, key findings and the sections you need"
    WritingIntent.ASSIGNMENT -> "The topic, your main argument and any requirements"
    WritingIntent.SUMMARY -> "Anything to focus on, e.g. \"key decisions only\""
    WritingIntent.PROFESSIONAL_MESSAGE -> "What the message should say and to whom"
    WritingIntent.REWRITE -> "Anything specific, e.g. \"make it friendlier\""
    WritingIntent.GRAMMAR -> "Anything specific, e.g. \"keep British spelling\""
    WritingIntent.TONE_CHANGE -> "Describe the tone you want"
}

@Composable
private fun fieldColors(container: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OpenFieldPrimaryBlue,
    unfocusedBorderColor = OpenFieldCardBorder,
    focusedContainerColor = container,
    unfocusedContainerColor = container
)
