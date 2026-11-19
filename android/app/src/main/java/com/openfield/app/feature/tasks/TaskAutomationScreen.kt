package com.openfield.app.feature.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.EmptyState
import com.openfield.app.core.components.FeatureHeaderCard
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.feature.ai.mode.LenientJson
import com.openfield.app.data.local.entity.TaskItemEntity
import com.openfield.app.data.repository.OpenFieldRepository
import kotlinx.coroutines.launch

@Composable
fun TaskAutomationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val tasks by repository.tasks.collectAsState()
    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Task & Workflow Automation",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = OpenFieldPrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Task")
            }
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenFieldBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FeatureHeaderCard(
                    icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                    title = "Workflows & Checklists",
                    subtitle = "Turn goals into action-oriented checklists and guided workflows. Plan manually or generate structured action steps using Gemma 4."
                )
            }

            if (tasks.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Active Tasks",
                        message = "Create a task or generate an automated workflow template."
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleSubtask = { subtaskId ->
                            scope.launch { repository.toggleSubtask(task.id, subtaskId) }
                        },
                        onDelete = {
                            scope.launch { repository.deleteTask(task.id) }
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, priority, subtasks ->
                scope.launch {
                    repository.createTask(title, desc, priority, subtasks)
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: TaskItemEntity,
    onToggleSubtask: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rawSubtasks = remember(task.subtasksJson) {
        (LenientJson.parse(task.subtasksJson) as? List<*>) ?: emptyList<Any>()
    }
    val subtasks = rawSubtasks.mapNotNull {
        val map = it as? Map<*, *> ?: return@mapNotNull null
        SubtaskItem(
            id = map["id"]?.toString() ?: "",
            title = map["title"]?.toString() ?: "",
            isCompleted = map["isCompleted"] as? Boolean ?: false
        )
    }

    val completedCount = subtasks.count { it.isCompleted }
    val progress = if (subtasks.isNotEmpty()) completedCount.toFloat() / subtasks.size.toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
        border = BorderStroke(1.dp, OpenFieldCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorityColor = when (task.priority) {
                        "HIGH" -> OpenFieldError
                        "MEDIUM" -> OpenFieldPrimaryBlue
                        else -> OpenFieldSecondaryText
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = priorityColor,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (task.status == "COMPLETED") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OpenFieldSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "DONE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OpenFieldSuccess,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = OpenFieldSecondaryText.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedCount of ${subtasks.size} steps completed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OpenFieldSecondaryText,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldPrimaryBlue,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress >= 1f) OpenFieldSuccess else OpenFieldPrimaryBlue,
                    trackColor = OpenFieldPrimaryBlueLight
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isExpanded) "Hide checklist" else "View checklist (${subtasks.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldPrimaryBlue
                        )
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subtasks.forEach { st ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleSubtask(st.id) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = st.isCompleted,
                                    onCheckedChange = { onToggleSubtask(st.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = OpenFieldPrimaryBlue,
                                        uncheckedColor = OpenFieldCardBorder
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = st.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (st.isCompleted) OpenFieldSecondaryText else OpenFieldNavyText,
                                        textDecoration = if (st.isCompleted) TextDecoration.LineThrough else null
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, priority: String, subtasks: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var subtaskInput by remember { mutableStateOf("") }
    val subtasks = remember { mutableStateListOf<String>() }

    // Pre-built templates
    fun applyTemplate(templateName: String) {
        when (templateName) {
            "Exam Preparation" -> {
                title = "Exam Revision Sprint"
                description = "Systematic review of core syllabus topics and self-assessment"
                priority = "HIGH"
                subtasks.clear()
                subtasks.addAll(listOf("Review chapter summary notes", "Formulate practice questions in Learn mode", "Complete 2 timed quizzes", "Document weak areas in Create & Write"))
            }
            "Device Maintenance" -> {
                title = "Hardware Inspection & Cleaning"
                description = "Periodic physical check, sensor dusting, and connection verification"
                priority = "MEDIUM"
                subtasks.clear()
                subtasks.addAll(listOf("Unplug and power down unit", "Inspect cables for wear or burns", "Clean optical glass and roller sensors", "Power on and verify diagnostic test"))
            }
            "Document Report" -> {
                title = "Synthesize Research Report"
                description = "Draft structured multi-source summary document"
                priority = "MEDIUM"
                subtasks.clear()
                subtasks.addAll(listOf("Retrieve key passages in Document Q&A", "Cross-reference pack guidelines", "Draft outline in Create & Write", "Export final report"))
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Workflow / Task",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Templates
                Text(
                    text = "Quick Templates:",
                    style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Exam Preparation", "Device Maintenance", "Document Report").forEach { t ->
                        OutlinedButton(
                            onClick = { applyTemplate(t) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            border = BorderStroke(1.dp, OpenFieldCardBorder)
                        ) {
                            Text(text = t.take(12) + "…", fontSize = 11.sp, color = OpenFieldNavyText)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )

                // Subtask input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = subtaskInput,
                        onValueChange = { subtaskInput = it },
                        label = { Text("Add Action Step") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = {
                            if (subtaskInput.isNotBlank()) {
                                subtasks.add(subtaskInput.trim())
                                subtaskInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OpenFieldPrimaryBlue)
                    ) {
                        Text("Add")
                    }
                }

                if (subtasks.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subtasks.forEachIndexed { idx, st ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${idx + 1}. $st",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldNavyText),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { subtasks.removeAt(idx) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = OpenFieldError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, description, priority, subtasks.toList())
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OpenFieldPrimaryBlue)
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OpenFieldSecondaryText)
            }
        }
    )
}
