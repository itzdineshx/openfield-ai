package com.openfield.app.feature.document

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun DocumentDetailScreen(
    docId: String,
    onNavigateBack: () -> Unit,
    onNavigateToQA: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val documents by repository.documents.collectAsState()
    val doc = documents.find { it.id == docId }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Document Overview",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete document"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (doc != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    PrimaryButton(
                        text = "Ask About This Document",
                        onClick = onNavigateToQA,
                        leadingIcon = Icons.AutoMirrored.Filled.Chat,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // File Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEAEA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = doc?.name ?: "Document not found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = OpenFieldNavyText
                            )
                        )
                        if (doc != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${doc.size} • ${doc.pageCount} pages • Offline Index Ready",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Document Summary
            Text(
                text = "Extracted Summary",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Text(
                    text = doc?.summary ?: "No summary available.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = OpenFieldNavyText
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Key Points Section
            Text(
                text = "Key Sections & Topics",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    doc?.keyPoints?.forEach { point ->
                        Text(
                            text = "• $point",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = OpenFieldNavyText
                            ),
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    ConfirmationDialog(
        show = showDeleteDialog,
        title = "Delete Document",
        message = "Are you sure you want to delete this document and its offline index?",
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = {
            doc?.let { repository.deleteDocument(it.id) }
            showDeleteDialog = false
            onNavigateBack()
        },
        onDismiss = { showDeleteDialog = false }
    )
}
