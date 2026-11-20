package com.openfield.app.feature.library

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ConfirmationDialog
import com.openfield.app.core.components.DocumentCard
import com.openfield.app.core.components.EmptyState
import com.openfield.app.core.components.KnowledgePackCard
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.model.KnowledgeCollection
import com.openfield.app.core.model.KnowledgeSource
import com.openfield.app.core.model.KnowledgeSourceType
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import kotlinx.coroutines.launch

@Composable
fun KnowledgeCenterScreen(
    repository: OpenFieldRepository = OpenFieldRepository.getInstance(),
    onNavigateToPackDetail: (String) -> Unit,
    onNavigateToDocumentDetail: (String) -> Unit,
    onNavigateToCrossSourceQa: (List<String>, String?) -> Unit,
    onOpenDrawerOrMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "All Sources", "Documents", "Collections", "Packs")

    val allSources by repository.knowledgeSourceRegistry.getAllSourcesFlow().collectAsState(initial = emptyList())
    val collections by repository.collections.collectAsState()
    val documents by repository.documents.collectAsState()
    val packs by repository.knowledgePacks.collectAsState()

    val selectedSourceIds = remember { mutableStateListOf<String>() }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
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
                title = "Knowledge Center",
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
                    if (selectedTab == 3) {
                        IconButton(onClick = { showCreateCollectionDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "New Collection")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = {
                        documentPickerLauncher.launch(arrayOf("application/pdf", "text/plain"))
                    },
                    containerColor = OpenFieldPrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Import Document")
                }
            } else if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = { showCreateCollectionDialog = true },
                    containerColor = OpenFieldPrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Collection")
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedSourceIds.size >= 2,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = OpenFieldPrimaryBlue,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ask Across ${selectedSourceIds.size} Sources",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            modifier = Modifier.clickable {
                                onNavigateToCrossSourceQa(selectedSourceIds.toList(), null)
                            }
                        ) {
                            Text(
                                text = "Start Q&A",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OpenFieldPrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
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
            // Segmented Scrollable Tab Row with native momentum fling
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tabTitles) { index, title ->
                    val isSelected = selectedTab == index
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder
                        ),
                        modifier = Modifier.clickable { selectedTab = index }
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else OpenFieldNavyText
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (selectedTab) {
                // 0. Overview
                0 -> {
                    KnowledgeOverviewContent(
                        allSources = allSources,
                        collections = collections,
                        documents = documents,
                        packs = packs,
                        onNavigateToSources = { selectedTab = 1 },
                        onNavigateToCollection = { selectedTab = 3 },
                        onNavigateToDocuments = { selectedTab = 2 },
                        onNavigateToPacks = { selectedTab = 4 }
                    )
                }

                // 1. All Sources (with multi-select & search)
                1 -> {
                    val filtered = if (searchQuery.isBlank()) allSources else {
                        allSources.filter { it.title.contains(searchQuery, ignoreCase = true) || it.snippet.contains(searchQuery, ignoreCase = true) }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search across all sources…"
                        )

                        if (filtered.isEmpty()) {
                            EmptyState(
                                title = "No sources found",
                                message = "Add documents, save conversations, or install knowledge packs to populate your knowledge hub."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { source ->
                                    val isSelected = source.id in selectedSourceIds
                                    SourceSelectableCard(
                                        source = source,
                                        isSelected = isSelected,
                                        onToggleSelection = {
                                            if (isSelected) selectedSourceIds.remove(source.id)
                                            else selectedSourceIds.add(source.id)
                                        },
                                        onClick = {
                                            if (source.type == KnowledgeSourceType.DOCUMENT) {
                                                onNavigateToDocumentDetail(source.id)
                                            } else if (source.type == KnowledgeSourceType.KNOWLEDGE_PACK) {
                                                onNavigateToPackDetail(source.id)
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }

                // 2. Documents
                2 -> {
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
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }

                // 3. Collections
                3 -> {
                    if (collections.isEmpty()) {
                        EmptyState(
                            title = "No collections yet",
                            message = "Organize documents, notes, and packs into collections to run targeted cross-source queries."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(collections, key = { it.id }) { collection ->
                                CollectionCard(
                                    collection = collection,
                                    onClick = {
                                        // Start Cross-Source Q&A for this collection
                                        scope.launch {
                                            val ids = repository.collectionDao.getSourceIdsForCollection(collection.id)
                                            onNavigateToCrossSourceQa(ids, collection.name)
                                        }
                                    },
                                    onDeleteClick = {
                                        scope.launch { repository.deleteCollection(collection.id) }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }

                // 4. Knowledge Packs
                4 -> {
                    val installedCount = packs.count { it.isInstalled }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(packs, key = { it.id }) { pack ->
                            KnowledgePackCard(
                                title = pack.title,
                                description = pack.description,
                                size = pack.size,
                                isInstalled = pack.isInstalled,
                                icon = Icons.Default.Menu,
                                iconColor = Color(pack.iconColorHex),
                                onCardClick = { onNavigateToPackDetail(pack.id) },
                                onActionClick = { repository.toggleKnowledgePack(pack.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    CreateCollectionDialog(
        show = showCreateCollectionDialog,
        onDismiss = { showCreateCollectionDialog = false },
        onConfirm = { name, desc, colorHex ->
            scope.launch { repository.createCollection(name, desc, colorHex) }
        }
    )

    ConfirmationDialog(
        show = docToDeleteId != null,
        title = "Delete Document",
        message = "Are you sure you want to remove this document from your offline knowledge center?",
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = {
            docToDeleteId?.let { repository.deleteDocument(it) }
            docToDeleteId = null
        },
        onDismiss = { docToDeleteId = null }
    )
}

@Composable
private fun KnowledgeOverviewContent(
    allSources: List<KnowledgeSource>,
    collections: List<KnowledgeCollection>,
    documents: List<Any>,
    packs: List<Any>,
    onNavigateToSources: () -> Unit,
    onNavigateToCollection: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToPacks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // High-Density Metrics Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = OpenFieldPrimaryBlueLight,
            border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Personal Knowledge Base",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = OpenFieldNavyText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Everything indexed locally and ready for multi-source offline AI queries.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(label = "Sources", value = "${allSources.size}", onClick = onNavigateToSources)
                    MetricItem(label = "Documents", value = "${documents.size}", onClick = onNavigateToDocuments)
                    MetricItem(label = "Collections", value = "${collections.size}", onClick = onNavigateToCollection)
                    MetricItem(label = "Packs", value = "${packs.size}", onClick = onNavigateToPacks)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Collections Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Collections",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = OpenFieldPrimaryBlue,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable(onClick = onNavigateToCollection)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (collections.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = OpenFieldSecondaryBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "No collections created yet. Tap 'Collections' to create your first.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText),
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                collections.take(5).forEach { col ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(col.colorHex).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(col.colorHex).copy(alpha = 0.3f)),
                        modifier = Modifier.clickable(onClick = onNavigateToCollection)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(col.colorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = col.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OpenFieldNavyText
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = OpenFieldPrimaryBlue
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = OpenFieldSecondaryText
            )
        )
    }
}

@Composable
private fun SourceSelectableCard(
    source: KnowledgeSource,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(checkedColor = OpenFieldPrimaryBlue)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = OpenFieldNavyText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val (badgeBg, badgeFg) = when (source.type) {
                        KnowledgeSourceType.DOCUMENT -> Color(0xFFFFE4E6) to Color(0xFFE11D48)
                        KnowledgeSourceType.CONVERSATION -> Color(0xFFEFF6FF) to Color(0xFF2563EB)
                        KnowledgeSourceType.NOTE -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                        KnowledgeSourceType.KNOWLEDGE_PACK -> Color(0xFFCCFBF1) to Color(0xFF0D9488)
                        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = source.type.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = badgeFg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (source.formattedSize.isNotBlank()) {
                        Text(
                            text = source.formattedSize,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionCard(
    collection: KnowledgeCollection,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = CardShape,
        color = OpenFieldBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(collection.colorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color(collection.colorHex),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = OpenFieldNavyText
                    )
                )
                if (collection.description.isNotBlank()) {
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Collection",
                    tint = OpenFieldSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OpenFieldSecondaryBackground,
        border = BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldNavyText, fontSize = 13.sp),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = MaterialTheme.typography.bodyMedium.copy(color = OpenFieldSecondaryText, fontSize = 13.sp))
                    }
                    inner()
                }
            )
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(18.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = OpenFieldSecondaryText, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
