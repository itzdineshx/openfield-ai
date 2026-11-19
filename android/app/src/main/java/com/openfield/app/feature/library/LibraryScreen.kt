package com.openfield.app.feature.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.DocumentCard
import com.openfield.app.core.components.EmptyState
import com.openfield.app.core.components.KnowledgePackCard
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun LibraryScreen(
    onNavigateToPackDetail: (String) -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    onOpenDrawerOrMenu: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val packs by repository.knowledgePacks.collectAsState()
    val documents by repository.documents.collectAsState()
    var packSearchQuery by remember { mutableStateOf("") }

    val filteredPacks = remember(packs, packSearchQuery) {
        if (packSearchQuery.isBlank()) packs
        else {
            val q = packSearchQuery.trim().lowercase()
            packs.filter { pack ->
                pack.title.lowercase().contains(q) ||
                pack.description.lowercase().contains(q) ||
                pack.category.lowercase().contains(q) ||
                pack.topics.any { it.lowercase().contains(q) }
            }
        }
    }

    var docToDeleteId by remember { mutableStateOf<String?>(null) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            repository.importRealDocument(context, uri)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "My Library",
                canNavigateBack = false,
                navigationIcon = {
                    IconButton(onClick = onOpenDrawerOrMenu) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = OpenFieldNavyText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                    },
                    containerColor = OpenFieldPrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Import Document")
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
        ) {
            // Segmented Pills: [ Knowledge Packs ] [ My Documents ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Tab 0: Knowledge Packs
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedTab == 0) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                    border = BorderStroke(
                        1.dp,
                        if (selectedTab == 0) OpenFieldPrimaryBlue else OpenFieldCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Knowledge Packs",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == 0) Color.White else OpenFieldNavyText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Tab 1: My Documents
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedTab == 1) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                    border = BorderStroke(
                        1.dp,
                        if (selectedTab == 1) OpenFieldPrimaryBlue else OpenFieldCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "My Documents",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) Color.White else OpenFieldNavyText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            if (selectedTab == 0) {
                // Search & Discovery Bar for Knowledge Packs
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OpenFieldSecondaryBackground,
                    border = BorderStroke(1.dp, OpenFieldCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search packs",
                            tint = OpenFieldSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = packSearchQuery,
                            onValueChange = { packSearchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = OpenFieldNavyText,
                                fontSize = 13.sp
                            ),
                            decorationBox = { innerTextField ->
                                if (packSearchQuery.isEmpty()) {
                                    Text(
                                        text = "Search packs by keyword, category, or topic…",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = OpenFieldSecondaryText,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (packSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { packSearchQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = OpenFieldSecondaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (filteredPacks.isEmpty()) {
                    EmptyState(
                        title = if (packSearchQuery.isBlank()) "No knowledge packs available" else "No matching packs found",
                        message = if (packSearchQuery.isBlank()) {
                            "Knowledge packs will appear here when a pack catalog is connected or when custom packs are imported."
                        } else {
                            "Try searching with a different keyword or browse all available packs."
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredPacks, key = { it.id }) { pack ->
                            KnowledgePackCard(
                                title = pack.title,
                                description = pack.description,
                                size = pack.size,
                                isInstalled = pack.isInstalled,
                                icon = getPackIcon(pack.id),
                                iconColor = Color(pack.iconColorHex),
                                onCardClick = { onNavigateToPackDetail(pack.id) },
                                onActionClick = { repository.toggleKnowledgePack(pack.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            } else {
                // My Documents List
                if (documents.isEmpty()) {
                    EmptyState(
                        title = "No documents imported",
                        message = "Import PDF user manuals, guidebooks, and text notes to query them completely offline."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(documents, key = { it.id }) { doc ->
                            DocumentCard(
                                fileName = doc.name,
                                fileSize = doc.size,
                                pageCount = doc.pageCount,
                                onClick = { onNavigateToDocumentDetail(doc.id) },
                                onDeleteClick = { docToDeleteId = doc.id }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Delete Document Dialog
    ConfirmationDialog(
        show = docToDeleteId != null,
        title = "Delete Document",
        message = "Are you sure you want to remove this document from your offline library?",
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = {
            docToDeleteId?.let { repository.deleteDocument(it) }
            docToDeleteId = null
        },
        onDismiss = { docToDeleteId = null }
    )
}

fun getPackIcon(packId: String): ImageVector {
    return when (packId) {
        "comp_tech" -> Icons.Default.Computer
        "devices_repairs" -> Icons.Default.Build
        "learning" -> Icons.AutoMirrored.Filled.MenuBook
        "programming" -> Icons.Default.Code
        "languages" -> Icons.Default.Translate
        else -> Icons.AutoMirrored.Filled.MenuBook
    }
}
