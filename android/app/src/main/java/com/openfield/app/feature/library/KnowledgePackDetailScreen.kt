package com.openfield.app.feature.library

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.components.SecondaryButton
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.data.repository.OpenFieldRepository

@Composable
fun KnowledgePackDetailScreen(
    packId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val packs by repository.knowledgePacks.collectAsState()
    val pack = packs.find { it.id == packId }

    if (pack == null) {
        Scaffold(
            topBar = {
                ScreenTopBar(
                    title = "Knowledge Pack",
                    canNavigateBack = true,
                    onNavigateBack = onNavigateBack
                )
            },
            modifier = modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(OpenFieldBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pack not found",
                    style = MaterialTheme.typography.titleMedium.copy(color = OpenFieldSecondaryText)
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Knowledge Pack",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (pack.isInstalled) {
                    SecondaryButton(
                        text = "Uninstall Pack (${pack.size})",
                        onClick = { repository.toggleKnowledgePack(pack.id) },
                        leadingIcon = Icons.Default.DeleteOutline,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PrimaryButton(
                        text = "Install Knowledge Pack (${pack.size})",
                        onClick = { repository.toggleKnowledgePack(pack.id) },
                        leadingIcon = Icons.Default.Download,
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

            // Pack Header Card
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
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(pack.iconColorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPackIcon(pack.id),
                            contentDescription = null,
                            tint = Color(pack.iconColorHex),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = pack.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${pack.category} • Version ${pack.version} • ${pack.size}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (pack.isInstalled) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = OpenFieldSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Installed & active offline",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldSuccess
                                    )
                                )
                            } else {
                                Text(
                                    text = "Available for offline download",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = OpenFieldSecondaryText
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Description
            Text(
                text = "Overview",
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
                    text = pack.description.ifBlank {
                        "${pack.title} knowledge pack — contents listed below."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = OpenFieldNavyText
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Topics Included
            Text(
                text = "Topics & Modules Included",
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
                    if (pack.topics.isEmpty()) {
                        Text(
                            text = "No topics listed.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = OpenFieldSecondaryText
                            )
                        )
                    } else {
                        pack.topics.forEach { topic ->
                            Text(
                                text = "• $topic",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = OpenFieldNavyText
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

