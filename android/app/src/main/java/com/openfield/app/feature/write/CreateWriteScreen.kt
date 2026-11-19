package com.openfield.app.feature.write

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.PrimaryButton
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
import com.openfield.app.feature.ai.controller.DraftExportFormat
import com.openfield.app.feature.ai.mode.WritingIntent
import com.openfield.app.feature.ai.mode.WritingTone

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

    val wordCount = remember(docContent) {
        docContent.split(Regex("\\s+")).count { it.isNotBlank() }
    }
    val charCount = remember(docContent) { docContent.length }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Create & Write",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (isGenerating) {
                        IconButton(onClick = { controller.stopGeneration() }) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = "Stop",
                                tint = OpenFieldError
                            )
                        }
                    }
                    IconButton(onClick = { controller.clearAll() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear all"
                        )
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
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Workspace Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(OpenFieldPrimaryBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Document Writing Studio",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            )
                        )
                        Text(
                            text = "Draft, refine, and edit ready-to-use documents completely offline.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = OpenFieldSecondaryText
                            )
                        )
                    }
                }
            }

            val savedDrafts by controller.savedDrafts.collectAsState()
            if (savedDrafts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Drafts:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldSecondaryText,
                            fontSize = 11.sp
                        )
                    )
                    savedDrafts.take(6).forEach { draft ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OpenFieldBackground,
                            border = BorderStroke(1.dp, OpenFieldCardBorder),
                            modifier = Modifier.clickable { controller.loadDraft(draft) }
                        ) {
                            Text(
                                text = draft.title.take(24),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = OpenFieldNavyText
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Intent / Format Selector Carousel
            Text(
                text = "Format:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = OpenFieldNavyText
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WritingIntent.entries.forEach { item ->
                    val isSelected = intent == item
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder
                        ),
                        modifier = Modifier.clickable { controller.setIntent(item) }
                    ) {
                        Text(
                            text = item.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else OpenFieldNavyText
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tone Selector Carousel
            Text(
                text = "Tone:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = OpenFieldNavyText
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WritingTone.entries.forEach { t ->
                    val isSelected = tone == t
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) OpenFieldNavyText else OpenFieldBackground,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OpenFieldNavyText else OpenFieldCardBorder
                        ),
                        modifier = Modifier.clickable { controller.setTone(t) }
                    ) {
                        Text(
                            text = t.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else OpenFieldNavyText
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prompt instructions input
            Text(
                text = "What would you like to write?",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = promptInput,
                onValueChange = { controller.setPrompt(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    val hint = when (intent) {
                        WritingIntent.EMAIL -> "Subject, recipient, and main points to convey..."
                        WritingIntent.REPORT -> "Topic, findings, and desired sections..."
                        WritingIntent.ASSIGNMENT -> "Topic, argument points, and requirements..."
                        WritingIntent.REWRITE -> "Key tone or structural adjustments desired..."
                        WritingIntent.GRAMMAR -> "Paste text to proofread and correct..."
                        else -> "Describe requirements, target audience, and key points..."
                    }
                    Text(text = hint, fontSize = 13.sp, color = OpenFieldSecondaryText.copy(alpha = 0.6f))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OpenFieldPrimaryBlue,
                    unfocusedBorderColor = OpenFieldCardBorder,
                    focusedContainerColor = OpenFieldBackground,
                    unfocusedContainerColor = OpenFieldBackground
                )
            )

            // Optional source text box for Rewrite/Summarize/Polish
            if (intent in listOf(WritingIntent.REWRITE, WritingIntent.GRAMMAR, WritingIntent.SUMMARY, WritingIntent.TONE_CHANGE)) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Original / Source text:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = OpenFieldNavyText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = sourceText,
                    onValueChange = { controller.setSourceText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(text = "Paste text to transform...", fontSize = 12.sp, color = OpenFieldSecondaryText.copy(alpha = 0.5f))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenFieldPrimaryBlue,
                        unfocusedBorderColor = OpenFieldCardBorder,
                        focusedContainerColor = OpenFieldSecondaryBackground,
                        unfocusedContainerColor = OpenFieldSecondaryBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            PrimaryButton(
                text = if (isGenerating) "Writing draft offline…" else "Generate document",
                onClick = { controller.generateDraft() },
                leadingIcon = Icons.Default.PlayArrow,
                enabled = !isGenerating && promptInput.isNotBlank()
            )

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = CardShape,
                    color = OpenFieldError.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = OpenFieldError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMsg ?: "", color = OpenFieldError, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Document-like Editable Surface
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.5.dp, OpenFieldCardBorder),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Document Header: Title input + Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = docTitle,
                            onValueChange = { controller.updateDocumentTitle(it) },
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Text(
                            text = "$wordCount words • $charCount chars",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = OpenFieldSecondaryText
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(OpenFieldCardBorder)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isGenerating && docContent.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = OpenFieldPrimaryBlue,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Drafting with Gemma 4 E2B…",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // The Editable Document Body
                    OutlinedTextField(
                        value = docContent,
                        onValueChange = { controller.updateDocumentContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        placeholder = {
                            Text(
                                text = "Generated document text will appear here. You can directly edit, type, or reformat anything.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = OpenFieldSecondaryText.copy(alpha = 0.5f)
                                )
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = OpenFieldNavyText
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Document Action Bar: Copy + Share + Export + Refinements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(docContent))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                enabled = docContent.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = OpenFieldNavyText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Copy", fontSize = 11.sp, color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { controller.shareDraft(context) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                enabled = docContent.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = OpenFieldNavyText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Share", fontSize = 11.sp, color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val file = controller.exportDraftToFile(context, DraftExportFormat.MARKDOWN)
                                    if (file != null) {
                                        Toast.makeText(context, "Exported: ${file.name}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                enabled = docContent.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = OpenFieldNavyText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = ".md", fontSize = 11.sp, color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Refine Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier.clickable(enabled = docContent.isNotBlank() && !isGenerating) {
                                    controller.refineContent("shorter")
                                }
                            ) {
                                Text(
                                    text = "Shorter",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier.clickable(enabled = docContent.isNotBlank() && !isGenerating) {
                                    controller.refineContent("professional")
                                }
                            ) {
                                Text(
                                    text = "Pro",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier.clickable(enabled = docContent.isNotBlank() && !isGenerating) {
                                    controller.refineContent("grammar")
                                }
                            ) {
                                Text(
                                    text = "Grammar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
