package com.openfield.app.core.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.ComposerShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldMutedText
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText

/**
 * Large Home Dashboard composer card matching the 5th screen in the reference.
 */
@Composable
fun HomePromptComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ask anything..."
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Action shortcut buttons: Image, Camera, Mic, Document
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComposerActionButton(
                    icon = Icons.Default.Image,
                    contentDescription = "Attach image",
                    onClick = onPickImage
                )
                ComposerActionButton(
                    icon = Icons.Default.CameraAlt,
                    contentDescription = "Take photo",
                    onClick = onCameraClick
                )
                ComposerActionButton(
                    icon = Icons.Default.Mic,
                    contentDescription = "Voice input",
                    onClick = onVoiceClick
                )
                ComposerActionButton(
                    icon = Icons.Default.Description,
                    contentDescription = "Attach document",
                    onClick = onPickDocument
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Input field and send button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OpenFieldSecondaryBackground)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = OpenFieldNavyText,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(OpenFieldPrimaryBlue),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = OpenFieldMutedText,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (value.isNotBlank()) {
                            onSend(value)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (value.isNotBlank()) OpenFieldPrimaryBlue else OpenFieldPrimaryBlue.copy(alpha = 0.4f)),
                    enabled = value.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send prompt",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    background: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun ComposerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = OpenFieldSecondaryBackground,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = OpenFieldSecondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Fixed bottom message composer used across Chat, Image Analysis, Document Q&A, Learn & Explain.
 */
@Composable
fun ChatBottomComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAttach: (() -> Unit)? = null,
    onVoiceClick: (() -> Unit)? = null,
    placeholder: String = "Ask a follow-up...",
    enabled: Boolean = true,
    isListening: Boolean = false,
    onStopListening: () -> Unit = {},
    onCancelListening: () -> Unit = {},
    isGenerating: Boolean = false,
    onStopGenerating: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = OpenFieldBackground,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth()
                .clip(ComposerShape)
                .background(OpenFieldSecondaryBackground)
                .border(1.dp, OpenFieldCardBorder, ComposerShape)
                .padding(start = if (onAttach == null) 14.dp else 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isListening) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (value.isNotBlank()) value else "Listening... (speak clearly)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (value.isNotBlank()) OpenFieldNavyText else OpenFieldSecondaryText,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onStopListening,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Finish recording",
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onCancelListening,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel voice input",
                            tint = OpenFieldSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                if (onAttach != null) {
                    IconButton(
                        onClick = onAttach,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach",
                            tint = OpenFieldSecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = OpenFieldNavyText,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(OpenFieldPrimaryBlue),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OpenFieldMutedText,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (onVoiceClick != null && !isGenerating) {
                    IconButton(
                        onClick = onVoiceClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = OpenFieldSecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                if (isGenerating && onStopGenerating != null) {
                    CircleAction(
                        icon = Icons.Default.Stop,
                        description = "Stop generating",
                        background = OpenFieldNavyText,
                        onClick = onStopGenerating
                    )
                } else {
                    val canSend = value.isNotBlank() && enabled && !isGenerating
                    CircleAction(
                        icon = Icons.AutoMirrored.Filled.Send,
                        description = "Send",
                        background = if (canSend) OpenFieldPrimaryBlue else OpenFieldPrimaryBlue.copy(alpha = 0.35f),
                        enabled = canSend,
                        onClick = { onSend(value) }
                    )
                }
            }
        }
    }
}

/**
 * Modern Centralized Omnibar with Integrated Capability Hub (+)
 * Consolidates Chat, Vision, Document/Pack Grounding, and AI Tools into one friction-free composer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedOmnibar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (prompt: String, imageUri: String?, scopeId: String?, scopeTitle: String?, mode: String?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ask anything, summarize, translate, solve…",
    isGenerating: Boolean = false,
    onStopGenerating: (() -> Unit)? = null,
    onVoiceClick: (() -> Unit)? = null,
    isListening: Boolean = false,
    onStopListening: () -> Unit = {},
    onCancelListening: () -> Unit = {},
    initialImageUri: String? = null,
    initialScopeId: String? = null,
    initialScopeTitle: String? = null,
    initialMode: String? = null,
    onNavigateToStudio: (() -> Unit)? = null
) {
    var attachedImageUri by remember(initialImageUri) { mutableStateOf(initialImageUri) }
    var activeScopeId by remember(initialScopeId) { mutableStateOf(initialScopeId) }
    var activeScopeTitle by remember(initialScopeTitle) { mutableStateOf(initialScopeTitle) }
    var activeMode by remember(initialMode) { mutableStateOf(initialMode) }
    var showCapabilitySheet by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri.toString()
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            activeScopeId = uri.toString()
            activeScopeTitle = "Attached Document"
        }
    }

    if (showCapabilitySheet) {
        OmnibarCapabilitySheet(
            onDismiss = { showCapabilitySheet = false },
            onPickCamera = {
                showCapabilitySheet = false
                imagePickerLauncher.launch("image/*")
            },
            onPickGallery = {
                showCapabilitySheet = false
                imagePickerLauncher.launch("image/*")
            },
            onPickDocument = {
                showCapabilitySheet = false
                documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
            },
            onSelectScope = { scopeId, scopeTitle ->
                showCapabilitySheet = false
                activeScopeId = scopeId
                activeScopeTitle = scopeTitle
            },
            onSelectMode = { mode ->
                showCapabilitySheet = false
                activeMode = mode
            },
            onOpenStudio = {
                showCapabilitySheet = false
                onNavigateToStudio?.invoke()
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = OpenFieldBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            // Dynamic Scope & Attachment Tray
            val hasActivePills = attachedImageUri != null || activeScopeId != null || activeMode != null
            if (hasActivePills) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (attachedImageUri != null) {
                        OmnibarPill(
                            icon = Icons.Default.Image,
                            label = "Photo Attached",
                            onRemove = { attachedImageUri = null }
                        )
                    }
                    if (activeScopeId != null) {
                        OmnibarPill(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            label = activeScopeTitle ?: "Knowledge Pack",
                            onRemove = {
                                activeScopeId = null
                                activeScopeTitle = null
                            }
                        )
                    }
                    if (activeMode != null) {
                        OmnibarPill(
                            icon = Icons.Default.AutoAwesome,
                            label = "$activeMode Mode",
                            onRemove = { activeMode = null }
                        )
                    }
                }
            }

            // Main Omnibar Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ComposerShape)
                    .background(OpenFieldSecondaryBackground)
                    .border(1.dp, OpenFieldCardBorder, ComposerShape)
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isListening) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (value.isNotBlank()) value else "Listening... (speak clearly)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (value.isNotBlank()) OpenFieldNavyText else OpenFieldSecondaryText,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        IconButton(onClick = onStopListening, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Stop, contentDescription = "Done", tint = OpenFieldPrimaryBlue, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onCancelListening, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = OpenFieldSecondaryText, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    // + Capability Hub Button
                    IconButton(
                        onClick = { showCapabilitySheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attachments & Modes",
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        maxLines = 5,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = OpenFieldNavyText,
                            fontSize = 14.5.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(OpenFieldPrimaryBlue),
                        decorationBox = { innerTextField ->
                            Box {
                                if (value.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = OpenFieldMutedText,
                                            fontSize = 14.5.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (onVoiceClick != null && !isGenerating) {
                        IconButton(
                            onClick = onVoiceClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice input",
                                tint = OpenFieldSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    if (isGenerating && onStopGenerating != null) {
                        CircleAction(
                            icon = Icons.Default.Stop,
                            description = "Stop generating",
                            background = OpenFieldNavyText,
                            onClick = onStopGenerating
                        )
                    } else {
                        val canSend = (value.isNotBlank() || attachedImageUri != null) && !isGenerating
                        CircleAction(
                            icon = Icons.AutoMirrored.Filled.Send,
                            description = "Send",
                            background = if (canSend) OpenFieldPrimaryBlue else OpenFieldPrimaryBlue.copy(alpha = 0.35f),
                            enabled = canSend,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSend(value, attachedImageUri, activeScopeId, activeScopeTitle, activeMode)
                                attachedImageUri = null
                                activeScopeId = null
                                activeScopeTitle = null
                                activeMode = null
                                onValueChange("")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OmnibarPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = OpenFieldPrimaryBlueLight,
        border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldNavyText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = OpenFieldSecondaryText,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmnibarCapabilitySheet(
    onDismiss: () -> Unit,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onPickDocument: () -> Unit,
    onSelectScope: (scopeId: String, scopeTitle: String) -> Unit,
    onSelectMode: (mode: String) -> Unit,
    onOpenStudio: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = OpenFieldBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Capabilities & Knowledge",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OpenFieldNavyText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Section 1: Capture & Attach
            Text(
                text = "ATTACH TO CONVERSATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldSecondaryText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OmnibarGridAction(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    subtitle = "Snap photo",
                    modifier = Modifier.weight(1f),
                    onClick = onPickCamera
                )
                OmnibarGridAction(
                    icon = Icons.Default.Image,
                    title = "Gallery",
                    subtitle = "Pick image",
                    modifier = Modifier.weight(1f),
                    onClick = onPickGallery
                )
                OmnibarGridAction(
                    icon = Icons.Default.Description,
                    title = "PDF / Doc",
                    subtitle = "Ground query",
                    modifier = Modifier.weight(1f),
                    onClick = onPickDocument
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Quick Knowledge Scopes
            Text(
                text = "GROUND IN KNOWLEDGE PACKS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldSecondaryText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OmnibarScopeChip("🩺 First Aid", "pack_first_aid", "First Aid Pack", onSelectScope)
                OmnibarScopeChip("⚡ Electronics", "pack_electronics", "Field Electronics", onSelectScope)
                OmnibarScopeChip("🌲 Survival", "pack_survival", "Wilderness Survival", onSelectScope)
                OmnibarScopeChip("💻 Code Cheats", "pack_developer", "Developer Cheats", onSelectScope)
                OmnibarScopeChip("🌐 All Sources", "all", "All Knowledge Sources", onSelectScope)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 3: AI Quick Tools
            Text(
                text = "SPECIALIZED AI MODES",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldSecondaryText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OmnibarGridAction(
                    icon = Icons.Default.Description,
                    title = "Summarize",
                    subtitle = "Key takeaways",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMode("SUMMARIZE") }
                )
                OmnibarGridAction(
                    icon = Icons.Default.Translate,
                    title = "Translate",
                    subtitle = "Multi-lingual",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMode("TRANSLATE") }
                )
                OmnibarGridAction(
                    icon = Icons.Default.Build,
                    title = "Troubleshoot",
                    subtitle = "Step-by-step",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMode("SOLVE") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Studio Handoff Button
            Surface(
                onClick = onOpenStudio,
                shape = RoundedCornerShape(12.dp),
                color = OpenFieldSecondaryBackground,
                border = BorderStroke(1.dp, OpenFieldCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Open Create & Write Studio",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OpenFieldNavyText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Long-form drafting, notes and exports",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OmnibarScopeChip(
    label: String,
    scopeId: String,
    scopeTitle: String,
    onSelectScope: (scopeId: String, scopeTitle: String) -> Unit
) {
    Surface(
        onClick = { onSelectScope(scopeId, scopeTitle) },
        shape = RoundedCornerShape(10.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                color = OpenFieldNavyText,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp
            )
        )
    }
}

@Composable
private fun OmnibarGridAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = OpenFieldPrimaryBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OpenFieldNavyText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OpenFieldSecondaryText,
                    fontSize = 11.sp
                )
            )
        }
    }
}

