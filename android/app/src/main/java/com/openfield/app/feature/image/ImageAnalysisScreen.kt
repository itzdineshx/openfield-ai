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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
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
    var isSaved by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
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
                    IconButton(onClick = { /* More info */ }) {
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

            // Image Preview Card (or Canvas drawing of Jasmine in green foliage if no camera image picked)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
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
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to pick an image to analyze",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                        }
                    }

                    // Replace image badge button
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

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val a = analysis
                    if (a == null) {
                        Text(
                            text = "Pick an image above to get a real, on-device analysis.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OpenFieldSecondaryText
                            )
                        )
                    } else {
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
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (a.commonName.isNotBlank()) {
                            Text(
                                text = "Common name",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = a.commonName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (a.scientificName.isNotBlank()) {
                            Text(
                                text = "Scientific name",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = a.scientificName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (a.family.isNotBlank()) {
                            Text(
                                text = "Family / Category",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = a.family,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OpenFieldSecondaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (a.keyFeatures.isNotEmpty()) {
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
                                        color = OpenFieldSecondaryText
                                    ),
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        if (a.safetyNotice.isNotBlank()) {
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = OpenFieldNavyText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        color = OpenFieldNavyText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        isSaved = !isSaved
                        if (isSaved) repository.saveCurrentAnalysis()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder)
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
