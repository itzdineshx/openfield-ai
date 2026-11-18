package com.openfield.app.feature.ai.model

import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess

@Composable
fun ModelManagementDialog(
    modelRepository: ModelRepository,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by modelRepository.installationState.collectAsState()
    val repository = com.openfield.app.data.repository.OpenFieldRepository.getInstance()
    val engineState by repository.aiEngine.engineState.collectAsState()
    var isLoadingEngine by remember { mutableStateOf(false) }
    var engineLoadError by remember { mutableStateOf<String?>(null) }

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

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close", color = OpenFieldPrimaryBlue, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
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
                        text = "Gemma 4 E2B Management",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                    Text(
                        text = "Exclusive On-Device AI Model",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OpenFieldSecondaryText,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (val currentState = state) {
                    is ModelInstallationState.Checking -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OpenFieldPrimaryBlue)
                        }
                    }

                    is ModelInstallationState.Downloading -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = OpenFieldSecondaryBackground,
                            border = BorderStroke(1.dp, OpenFieldCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Downloading ${currentState.model.displayName}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Artifact: ${currentState.model.fileName} (~${currentState.model.formattedSize})",
                                    fontSize = 11.sp,
                                    color = OpenFieldSecondaryText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${currentState.progressPercent}% (${currentState.bytesDownloaded / (1024 * 1024)} MB / ${currentState.totalBytes / (1024 * 1024)} MB)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OpenFieldPrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { currentState.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = OpenFieldPrimaryBlue,
                                    trackColor = OpenFieldPrimaryBlueLight
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { modelRepository.cancelDownload() },
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancel", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    is ModelInstallationState.Verifying -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = OpenFieldPrimaryBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Verifying Gemma 4 E2B artifact integrity...", fontSize = 12.sp, color = OpenFieldSecondaryText)
                            }
                        }
                    }

                    is ModelInstallationState.Installed -> {
                        Column {
                            // Active status card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = OpenFieldPrimaryBlueLight,
                                border = BorderStroke(1.dp, OpenFieldPrimaryBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = OpenFieldSuccess,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Gemma 4 E2B IT",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = OpenFieldNavyText
                                        )
                                        Text(
                                            text = "Status: Installed & Ready Offline",
                                            fontSize = 12.sp,
                                            color = OpenFieldSuccess,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Artifact: ${ModelCatalog.GEMMA_4_E2B.fileName} (${currentState.activeFile.length() / (1024 * 1024)} MB)",
                                            fontSize = 11.sp,
                                            color = OpenFieldSecondaryText
                                        )
                                        Text(
                                            text = "Repository: litert-community/gemma-4-E2B-it-litert-lm",
                                            fontSize = 10.sp,
                                            color = OpenFieldSecondaryText
                                        )
                                    }

                                    IconButton(
                                        onClick = { modelRepository.deleteModel() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove model",
                                            tint = Color(0xFFD94A4A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Engine load status — the model is on disk, but LiteRT-LM
                            // native initialization is a separate step. We do NOT call it
                            // implicitly from the chat path because the native layer can
                            // abort the whole process on container-version mismatch or
                            // GPU-delegate failures.
                            val engineReady = engineState is com.openfield.app.feature.ai.engine.AiEngineState.Ready
                            val engineLoading = engineState is com.openfield.app.feature.ai.engine.AiEngineState.Loading || isLoadingEngine
                            val engineErrorState = engineState as? com.openfield.app.feature.ai.engine.AiEngineState.Error

                            when {
                                engineReady -> {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = OpenFieldPrimaryBlueLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = OpenFieldSuccess,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Engine loaded. Inference ready.",
                                                fontSize = 12.sp,
                                                color = OpenFieldNavyText,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                engineLoading -> {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = OpenFieldPrimaryBlueLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                color = OpenFieldPrimaryBlue,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Loading engine into memory…",
                                                    fontSize = 12.sp,
                                                    color = OpenFieldNavyText,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "If the app closes during this step, the model is incompatible with the installed LiteRT-LM runtime. See logs for details.",
                                                    fontSize = 10.sp,
                                                    color = OpenFieldSecondaryText,
                                                    lineHeight = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    val lastError = engineLoadError ?: engineErrorState?.message
                                    if (lastError != null) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFEE2E2),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 6.dp)
                                        ) {
                                            Text(
                                                text = lastError,
                                                color = Color(0xFF991B1B),
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            engineLoadError = null
                                            isLoadingEngine = true
                                            scope.launch {
                                                val result = repository.loadActiveModel()
                                                isLoadingEngine = false
                                                engineLoadError = result.exceptionOrNull()?.message
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OpenFieldPrimaryBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Load model", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    is ModelInstallationState.NotInstalled, is ModelInstallationState.Error -> {
                        Column {
                            if (currentState is ModelInstallationState.Error) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFEE2E2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = currentState.message,
                                        color = Color(0xFF991B1B),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Official On-Device Model:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OpenFieldNavyText
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val catalogModel = ModelCatalog.GEMMA_4_E2B
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, OpenFieldCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = catalogModel.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = OpenFieldNavyText
                                            )
                                            Text(
                                                text = "${catalogModel.formattedSize} • Multimodal (Text + Vision)",
                                                fontSize = 11.sp,
                                                color = OpenFieldSecondaryText
                                            )
                                        }

                                        Button(
                                            onClick = { modelRepository.startDownload(catalogModel) },
                                            colors = ButtonDefaults.buttonColors(containerColor = OpenFieldPrimaryBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Download", fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = catalogModel.description,
                                        fontSize = 11.sp,
                                        color = OpenFieldSecondaryText
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Repository: ${catalogModel.repository}",
                                        fontSize = 10.sp,
                                        color = OpenFieldPrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Import local Gemma 4 E2B .litertlm artifact from device
                OutlinedButton(
                    onClick = { filePicker.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import gemma-4-E2B-it.litertlm",
                        color = OpenFieldPrimaryBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        containerColor = OpenFieldBackground,
        shape = RoundedCornerShape(20.dp)
    )
}
