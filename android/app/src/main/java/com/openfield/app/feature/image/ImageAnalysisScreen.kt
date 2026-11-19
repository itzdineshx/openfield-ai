package com.openfield.app.feature.image

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openfield.app.core.components.MarkdownText
import com.openfield.app.core.components.OpenFieldLogo
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

@Composable
fun ImageAnalysisScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val analysis by repository.currentImageAnalysis.collectAsState()
    val isAnalyzing by repository.isAnalyzingImage.collectAsState()
    var isSaved by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isSaved = false
            repository.analyzeSelectedImage(uri, context)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Analyze Image",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
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
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Image Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = CardShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (analysis?.imageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(analysis?.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Dark scrim when analyzing so spinner is readable
                        if (isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = OpenFieldPrimaryBlue,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tap to pick an image",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Any photo — plant, animal, object, scene",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                        }
                    }

                    // Analyzing overlay spinner
                    if (isAnalyzing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Analyzing…",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Camera badge button (bottom-right)
                    if (!isAnalyzing) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Change photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Analysis Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val a = analysis

                    when {
                        // Still running and no result yet
                        isAnalyzing && a?.rawText.isNullOrBlank() -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = OpenFieldPrimaryBlue,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "AI is analyzing your image…",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OpenFieldSecondaryText
                                    )
                                )
                            }
                        }

                        // Error result
                        a != null && a.isError -> {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = OpenFieldError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = a.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OpenFieldError
                                        )
                                    )
                                    if (a.safetyNotice.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = a.safetyNotice,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = OpenFieldSecondaryText,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Analysis complete
                        a != null && !a.isError -> {
                            // Header row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OpenFieldLogo(size = 28.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = a.title.ifBlank { "Analysis Result" },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = OpenFieldNavyText
                                    ),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // If we have structured fields, show them
                            val hasStructuredFields = a.commonName.isNotBlank() ||
                                a.scientificName.isNotBlank() || a.family.isNotBlank()

                            if (hasStructuredFields) {
                                if (a.commonName.isNotBlank()) {
                                    FieldRow("Common name", a.commonName)
                                }
                                if (a.scientificName.isNotBlank()) {
                                    FieldRow("Scientific name", a.scientificName)
                                }
                                if (a.family.isNotBlank()) {
                                    FieldRow("Family / Category", a.family)
                                }
                                if (a.keyFeatures.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Key features",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = OpenFieldNavyText
                                        )
                                    )
                                    a.keyFeatures.forEach { feature ->
                                        Text(
                                            text = "• $feature",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                color = OpenFieldSecondaryText
                                            ),
                                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Show full raw text below structured fields if available
                                if (a.rawText.isNotBlank() && a.rawText.length > 50) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp)
                                            .height(1.dp)
                                            .background(OpenFieldCardBorder)
                                    )
                                    Text(
                                        text = "Full analysis",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = OpenFieldNavyText
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    MarkdownText(
                                        text = a.rawText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            color = OpenFieldSecondaryText
                                        )
                                    )
                                }
                            } else if (a.rawText.isNotBlank()) {
                                // No structured fields — show the full AI response
                                MarkdownText(
                                    text = a.rawText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 21.sp,
                                        color = OpenFieldNavyText
                                    )
                                )
                            }

                            // Safety notice chip
                            if (a.safetyNotice.isNotBlank() && !a.isError) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = OpenFieldPrimaryBlueLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = OpenFieldPrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = a.safetyNotice,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                color = OpenFieldNavyText
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Initial empty state
                        else -> {
                            Text(
                                text = "Pick an image above to get a real, on-device analysis.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Share and Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Share action */ },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
                    enabled = analysis != null && !analysis!!.isError
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = OpenFieldNavyText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Share", color = OpenFieldNavyText, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = {
                        isSaved = !isSaved
                        if (isSaved) repository.saveCurrentAnalysis()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
                    enabled = analysis != null && !analysis!!.isError
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isSaved) OpenFieldPrimaryBlue else OpenFieldNavyText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSaved) "Saved" else "Save",
                        color = if (isSaved) OpenFieldPrimaryBlue else OpenFieldNavyText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = OpenFieldNavyText
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = OpenFieldSecondaryText
            )
        )
    }
}
