package com.openfield.app.feature.library

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.DocumentCard
import com.openfield.app.core.components.EmptyState
import com.openfield.app.core.components.KnowledgePackCard
import com.openfield.app.core.components.MarkdownContent
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.data.repository.OpenFieldRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OpenField Library Screen
 * Compliant with PRD Screen Specification S-14 (Page 28):
 * 1. Knowledge Packs: installed and available topic bundles.
 * 2. My Documents: user-imported files with local extraction/index status.
 * 3. Saved Answers: user-selected snippets and bookmarked outputs.
 * 4. Downloads: active, completed, interrupted and failed pack downloads.
 */
@Composable
fun LibraryScreen(
    onNavigateToPackDetail: (String) -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    onOpenDrawerOrMenu: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val packs by repository.knowledgePacks.collectAsState()
    val documents by repository.documents.collectAsState()
    val savedAnswers by repository.savedAnswers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var docToDeleteId by remember { mutableStateOf<String?>(null) }
    var answerToDeleteId by remember { mutableStateOf<String?>(null) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            repository.importRealDocument(context, uri)
        }
    }

    val tabs = listOf(
        TabItem("Knowledge Packs", Icons.AutoMirrored.Filled.MenuBook),
        TabItem("My Documents", Icons.Default.Description),
        TabItem("Saved Answers", Icons.Default.Bookmark),
        TabItem("Downloads", Icons.Default.Download)
    )

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
            // PRD S-14: 4-Section Scrollable / Segmented Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = OpenFieldBackground,
                contentColor = OpenFieldPrimaryBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = OpenFieldPrimaryBlue,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(OpenFieldCardBorder)
                    )
                }
            ) {
                tabs.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            searchQuery = ""
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tabItem.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == index) OpenFieldPrimaryBlue else OpenFieldSecondaryText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tabItem.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == index) OpenFieldPrimaryBlue else OpenFieldSecondaryText
                                    )
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab 0: Knowledge Packs
            when (selectedTab) {
                0 -> {
                    KnowledgePacksSection(
                        packs = packs,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onNavigateToPackDetail = onNavigateToPackDetail,
                        onTogglePack = { repository.toggleKnowledgePack(it) }
                    )
                }
                1 -> {
                    MyDocumentsSection(
                        documents = documents,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onNavigateToDocumentDetail = onNavigateToDocumentDetail,
                        onDeleteClick = { docToDeleteId = it },
                        onImportClick = { documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain")) }
                    )
                }
                2 -> {
                    SavedAnswersSection(
                        savedAnswers = savedAnswers,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onCopyAnswer = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Answer copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteAnswer = { answerToDeleteId = it }
                    )
                }
                3 -> {
                    DownloadsSection(
                        packs = packs,
                        onInstallPack = { repository.toggleKnowledgePack(it) }
                    )
                }
            }
        }
    }

    // Delete Document Dialog
    ConfirmationDialog(
        show = docToDeleteId != null,
        title = "Delete Document",
        message = "Are you sure you want to remove this document and its local search index from your library?",
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = {
            docToDeleteId?.let { repository.deleteDocument(it) }
            docToDeleteId = null
        },
        onDismiss = { docToDeleteId = null }
    )

    // Delete Saved Answer Dialog
    ConfirmationDialog(
        show = answerToDeleteId != null,
        title = "Remove Saved Answer",
        message = "Are you sure you want to delete this saved answer from your bookmarks?",
        confirmText = "Remove",
        isDestructive = true,
        onConfirm = {
            answerToDeleteId?.let { repository.deleteSavedAnswer(it) }
            answerToDeleteId = null
        },
        onDismiss = { answerToDeleteId = null }
    )
}

private data class TabItem(val title: String, val icon: ImageVector)

// =================================================================
// 1. KNOWLEDGE PACKS SECTION
// =================================================================
@Composable
private fun KnowledgePacksSection(
    packs: List<com.openfield.app.core.model.KnowledgePack>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNavigateToPackDetail: (String) -> Unit,
    onTogglePack: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Technology", "Engineering", "Education", "Software", "Language")

    val filteredPacks = remember(packs, searchQuery, selectedCategory) {
        val byCat = if (selectedCategory == "All") packs else packs.filter { it.category.equals(selectedCategory, true) }
        if (searchQuery.isBlank()) byCat
        else {
            val q = searchQuery.trim().lowercase()
            byCat.filter { pack ->
                pack.title.lowercase().contains(q) ||
                pack.description.lowercase().contains(q) ||
                pack.topics.any { it.lowercase().contains(q) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        LibrarySearchBar(
            query = searchQuery,
            placeholder = "Search knowledge packs by keyword, category, topic…",
            onQueryChange = onSearchChange
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                    border = BorderStroke(1.dp, if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
                    modifier = Modifier.clickable { selectedCategory = category }
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else OpenFieldNavyText
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (filteredPacks.isEmpty()) {
            EmptyState(
                title = if (searchQuery.isBlank()) "No packs available" else "No matching packs found",
                message = "Try searching with a different keyword or category filter."
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
                        onActionClick = { onTogglePack(pack.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// =================================================================
// 2. MY DOCUMENTS SECTION
// =================================================================
@Composable
private fun MyDocumentsSection(
    documents: List<com.openfield.app.core.model.DocumentItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onImportClick: () -> Unit
) {
    val filteredDocs = remember(documents, searchQuery) {
        if (searchQuery.isBlank()) documents
        else {
            val q = searchQuery.trim().lowercase()
            documents.filter { it.name.lowercase().contains(q) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LibrarySearchBar(
            query = searchQuery,
            placeholder = "Search imported documents…",
            onQueryChange = onSearchChange
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (documents.isEmpty()) {
            EmptyState(
                title = "No documents imported",
                message = "Import PDF user manuals, guidebooks, and text notes to query them completely offline."
            )
        } else if (filteredDocs.isEmpty()) {
            EmptyState(
                title = "No matching documents",
                message = "Try searching with a different document name."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDocs, key = { it.id }) { doc ->
                    DocumentCard(
                        fileName = doc.name,
                        fileSize = doc.size,
                        pageCount = doc.pageCount,
                        onClick = { onNavigateToDocumentDetail(doc.id) },
                        onDeleteClick = { onDeleteClick(doc.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// =================================================================
// 3. SAVED ANSWERS SECTION (PRD Screen S-14)
// =================================================================
@Composable
private fun SavedAnswersSection(
    savedAnswers: List<com.openfield.app.data.local.entity.SavedAnswerEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCopyAnswer: (String) -> Unit,
    onDeleteAnswer: (String) -> Unit
) {
    val filteredAnswers = remember(savedAnswers, searchQuery) {
        if (searchQuery.isBlank()) savedAnswers
        else {
            val q = searchQuery.trim().lowercase()
            savedAnswers.filter {
                it.question.lowercase().contains(q) ||
                it.answer.lowercase().contains(q) ||
                it.sourceTitle.lowercase().contains(q)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LibrarySearchBar(
            query = searchQuery,
            placeholder = "Search saved answers and bookmarks…",
            onQueryChange = onSearchChange
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (savedAnswers.isEmpty()) {
            EmptyState(
                title = "No saved answers yet",
                message = "Bookmark helpful answers and passages from Chat, Document Q&A, and AI Tools to quickly revisit them offline."
            )
        } else if (filteredAnswers.isEmpty()) {
            EmptyState(
                title = "No matching saved answers",
                message = "Try searching with different keywords."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAnswers, key = { it.id }) { saved ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = OpenFieldPrimaryBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = saved.sourceTitle.ifBlank { "Saved Answer" },
                                        color = OpenFieldPrimaryBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onCopyAnswer(saved.answer) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy answer",
                                            tint = OpenFieldSecondaryText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onDeleteAnswer(saved.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (saved.question.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = saved.question,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OpenFieldNavyText,
                                        fontSize = 13.5.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            MarkdownContent(
                                text = saved.answer,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    color = OpenFieldNavyText
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Saved on ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(saved.savedAt))}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// =================================================================
// 4. DOWNLOADS SECTION (PRD Screen S-14)
// =================================================================
@Composable
private fun DownloadsSection(
    packs: List<com.openfield.app.core.model.KnowledgePack>,
    onInstallPack: (String) -> Unit
) {
    val installedCount = packs.count { it.isInstalled }
    val availableCount = packs.size - installedCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Status Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
            border = BorderStroke(1.dp, OpenFieldCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = OpenFieldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verified Knowledge Pack Downloads",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$installedCount packs installed & active offline • $availableCount packs available in local catalog",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OpenFieldSecondaryText,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Pack Download & Verification Status",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = OpenFieldNavyText
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(packs, key = { it.id }) { pack ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                    border = BorderStroke(1.dp, OpenFieldCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(pack.iconColorHex).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getPackIcon(pack.id),
                                        contentDescription = null,
                                        tint = Color(pack.iconColorHex),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = pack.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = OpenFieldNavyText,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    Text(
                                        text = "Version ${pack.version} • ${pack.size}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OpenFieldSecondaryText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            if (pack.isInstalled) {
                                Surface(
                                    color = OpenFieldSuccess.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = OpenFieldSuccess,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Verified",
                                            color = OpenFieldSuccess,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onInstallPack(pack.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, OpenFieldPrimaryBlue),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = OpenFieldPrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Download",
                                        fontSize = 11.5.sp,
                                        color = OpenFieldPrimaryBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        if (pack.isInstalled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { 1.0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = OpenFieldSuccess,
                                trackColor = OpenFieldCardBorder
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LibrarySearchBar(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit
) {
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
                contentDescription = null,
                tint = OpenFieldSecondaryText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = OpenFieldNavyText,
                    fontSize = 13.sp
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OpenFieldSecondaryText,
                                fontSize = 13.sp
                            )
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
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
