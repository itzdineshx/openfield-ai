package com.openfield.app.feature.tools

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.model.AiToolItem
import com.openfield.app.core.model.ToolFilterCategory
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun ToolsScreen(
    onNavigateToToolDetail: (String) -> Unit,
    onNavigateToImageAnalysis: () -> Unit,
    onNavigateToDocumentQA: () -> Unit,
    onNavigateToCreateWrite: () -> Unit = {},
    onNavigateToCalculator: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    var selectedCategory by remember { mutableStateOf(ToolFilterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    val tools by repository.tools.collectAsState()

    val filteredTools = remember(selectedCategory, tools, searchQuery) {
        val byCat = if (selectedCategory == ToolFilterCategory.ALL) tools else tools.filter { it.category == selectedCategory }
        if (searchQuery.isBlank()) byCat
        else byCat.filter { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenFieldBackground)
    ) {
        ScreenTopBar(
            title = "AI Tools",
            canNavigateBack = false,
            actions = {
                IconButton(onClick = { /* Menu */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options"
                    )
                }
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tools…", color = OpenFieldSecondaryText.copy(alpha = 0.7f), fontSize = 13.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OpenFieldSecondaryText, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = OpenFieldSecondaryText, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OpenFieldPrimaryBlue,
                unfocusedBorderColor = OpenFieldCardBorder,
                focusedContainerColor = OpenFieldSecondaryBackground,
                unfocusedContainerColor = OpenFieldSecondaryBackground
            )
        )

        // Filter Pills: All | Text | Image | Document
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ToolFilterCategory.values()) { category ->
                val isSelected = selectedCategory == category
                val label = when (category) {
                    ToolFilterCategory.ALL -> "All"
                    ToolFilterCategory.TEXT -> "Text"
                    ToolFilterCategory.IMAGE -> "Image"
                    ToolFilterCategory.DOCUMENT -> "Document"
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder
                    ),
                    modifier = Modifier.clickable { selectedCategory = category }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) OpenFieldBackground else OpenFieldNavyText
                        ),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tool list matching Screen 11 in reference
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTools, key = { it.id }) { tool ->
                ToolListItemCard(
                    tool = tool,
                    onClick = {
                        when (tool.id) {
                            "image_analysis" -> onNavigateToImageAnalysis()
                            "doc_qa" -> onNavigateToDocumentQA()
                            "write_create" -> onNavigateToCreateWrite()
                            "calculator" -> onNavigateToCalculator()
                            "tasks" -> onNavigateToTasks()
                            else -> onNavigateToToolDetail(tool.id)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ToolListItemCard(
    tool: AiToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "toolPressScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OpenFieldPrimaryBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToolIcon(tool.id),
                    contentDescription = null,
                    tint = OpenFieldPrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = OpenFieldNavyText
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (tool.id in listOf("calculator", "tasks")) OpenFieldPrimaryBlue.copy(alpha = 0.09f) else OpenFieldSecondaryBackground
                    ) {
                        Text(
                            text = if (tool.id in listOf("calculator", "tasks")) "OFFLINE ENGINE" else "ON-DEVICE AI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (tool.id in listOf("calculator", "tasks")) OpenFieldPrimaryBlue else OpenFieldSecondaryText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = OpenFieldSecondaryText
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

fun getToolIcon(toolId: String): ImageVector {
    return when (toolId) {
        "image_analysis" -> Icons.Default.Image
        "doc_qa" -> Icons.Default.Description
        "summarize" -> Icons.AutoMirrored.Filled.ShortText
        "explain" -> Icons.Default.Lightbulb
        "write_create" -> Icons.Default.Edit
        "translate" -> Icons.Default.Translate
        "reframe" -> Icons.Default.AutoAwesome
        "calculator" -> Icons.Default.Calculate
        "tasks" -> Icons.AutoMirrored.Filled.PlaylistAddCheck
        else -> Icons.Default.Refresh
    }
}
