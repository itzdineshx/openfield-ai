package com.openfield.app.feature.setup

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.CruxLogo
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.model.ModelCatalog
import com.openfield.app.feature.ai.model.ModelInstallationState
import com.openfield.app.feature.ai.model.ModelRepository
import java.util.Locale

@Composable
fun ModelSetupScreen(
    onComplete: () -> Unit,
    onSkipToHome: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val modelRepository: ModelRepository = repository.modelRepository
    val state by modelRepository.installationState.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Verifying and importing Gemma 4 E2B artifact...", Toast.LENGTH_SHORT).show()
            modelRepository.importLocalModel(
                uri = uri,
                onProgress = { /* progress */ },
                onComplete = { result ->
                    result.fold(
                        onSuccess = {
                            Toast.makeText(context, "Gemma 4 E2B IT imported successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(context, error.message ?: "Failed to import model", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpenFieldBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // CRUX Brand Mark
            CruxLogo(
                size = 64.dp,
                showText = true,
                textFontSize = 24,
                showSubtitle = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Download On-Device AI Model",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = OpenFieldNavyText
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CRUX runs 100% offline on your phone's processor. Download Gemma 4 E2B IT once to unlock full conversational and visual intelligence with zero internet required.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = OpenFieldSecondaryText
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Storage & Hardware Readiness Card
            val freeBytes = (state as? ModelInstallationState.NotInstalled)?.availableDiskSpaceBytes ?: (8L * 1024 * 1024 * 1024)
            val freeGb = freeBytes / (1024.0 * 1024.0 * 1024.0)
            val hasEnoughSpace = freeGb >= 3.2

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (hasEnoughSpace) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (hasEnoughSpace) Icons.Default.Storage else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (hasEnoughSpace) OpenFieldSuccess else Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (hasEnoughSpace) "Storage Ready" else "Low Storage Warning",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            )
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "Available: %.1f GB · Required: 3.5 GB", freeGb),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Model Specification Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldCardBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OpenFieldPrimaryBlueLight,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Memory,
                                        contentDescription = null,
                                        tint = OpenFieldPrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Gemma 4 E2B IT",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "Google LiteRT-LM (.litertlm)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OpenFieldPrimaryBlueLight
                        ) {
                            Text(
                                text = "2.58 GB",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OpenFieldPrimaryBlue
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "• Multimodal (Text and Image Understanding)\n• 4,096 Token Context Window\n• Optimized for CPU execution on ARM64\n• Zero data leaves your device",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OpenFieldNavyText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Action Area depending on installation state
            when (val s = state) {
                is ModelInstallationState.NotInstalled -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryButton(
                            text = "Download Gemma 4 (~2.58 GB)",
                            onClick = {
                                modelRepository.startDownload(ModelCatalog.GEMMA_4_E2B)
                            },
                            leadingIcon = Icons.Default.CloudDownload,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { filePicker.launch(arrayOf("*/*")) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, OpenFieldCardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = OpenFieldNavyText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Import from Device / SD Card",
                                color = OpenFieldNavyText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                is ModelInstallationState.Downloading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                        border = BorderStroke(1.dp, OpenFieldCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Downloading Model...",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "${s.progressPercent}%",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OpenFieldPrimaryBlue
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { s.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = OpenFieldPrimaryBlue,
                                trackColor = OpenFieldCardBorder
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val downloadedMb = s.bytesDownloaded / (1024.0 * 1024.0)
                            val totalMb = s.totalBytes / (1024.0 * 1024.0)
                            val speedMb = s.speedBytesPerSec / (1024.0 * 1024.0)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f / %.1f MB", downloadedMb, totalMb),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                                if (speedMb > 0) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f MB/s", speedMb),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OpenFieldPrimaryBlue,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = { modelRepository.cancelDownload() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel Download", color = Color(0xFFD32F2F), fontSize = 13.sp)
                            }
                        }
                    }
                }

                is ModelInstallationState.Verifying -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = OpenFieldPrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Verifying cryptographic SHA-256 integrity...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = OpenFieldNavyText
                                )
                            )
                        }
                    }
                }

                is ModelInstallationState.Installed -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = OpenFieldSuccess,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Model Ready & Verified!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OpenFieldNavyText
                            )
                        )

                        Text(
                            text = "Gemma 4 E2B IT is ready to provide 100% offline intelligence.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        PrimaryButton(
                            text = "Start Using CRUX",
                            onClick = onComplete,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is ModelInstallationState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Download Error",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = s.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldNavyText,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { modelRepository.startDownload(s.model ?: ModelCatalog.GEMMA_4_E2B) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry Download")
                            }
                        }
                    }
                }

                else -> {
                    CircularProgressIndicator(color = OpenFieldPrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip / Explore App First Option
            if (state !is ModelInstallationState.Installed) {
                TextButton(
                    onClick = onSkipToHome,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Explore App First (Download Later on Wi-Fi)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OpenFieldSecondaryText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
