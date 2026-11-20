package com.openfield.app.feature.settings

import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WifiOff
import com.openfield.app.core.util.PerformanceTracker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.components.ScreenTopBar
import com.openfield.app.core.theme.CardShape
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.model.ModelManagementDialog

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val context = LocalContext.current
    val settings by repository.settings.collectAsState()
    val packs by repository.knowledgePacks.collectAsState()
    val installedPacksCount = packs.count { it.isInstalled }
    val documents by repository.documents.collectAsState()
    val modelState by repository.modelRepository.installationState.collectAsState()
    val metrics by PerformanceTracker.metrics.collectAsState()

    var showModelDialog by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var editedMemory by remember(settings.userCustomMemory) { mutableStateOf(settings.userCustomMemory) }

    val scope = rememberCoroutineScope()
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = repository.backupManager.exportBackup(context, uri)
                result.onSuccess { count ->
                    Toast.makeText(context, "Backup exported successfully ($count records)", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Export failed: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = repository.backupManager.importBackup(context, uri)
                result.onSuccess { count ->
                    Toast.makeText(context, "Restored $count records from backup", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    Toast.makeText(context, "Restore failed: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
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

            // Section: AI Engine & Offline
            SettingsSectionHeader(title = "AI Engine & Offline Mode")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Offline Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.WifiOff)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Offline Mode",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "Operate 100% on-device without internet",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = settings.offlineMode,
                            onCheckedChange = { repository.updateOfflineMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OpenFieldPrimaryBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showModelDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.Memory)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Active Local Model",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                val modelLabel = when (val s = modelState) {
                                    is com.openfield.app.feature.ai.model.ModelInstallationState.Installed ->
                                        "${s.activeModel.displayName} · Installed"
                                    is com.openfield.app.feature.ai.model.ModelInstallationState.Downloading ->
                                        "Downloading ${s.progressPercent}%"
                                    is com.openfield.app.feature.ai.model.ModelInstallationState.Verifying ->
                                        "Verifying..."
                                    is com.openfield.app.feature.ai.model.ModelInstallationState.Error ->
                                        "Error — tap to retry"
                                    else -> "Tap to download or import model"
                                }
                                Text(
                                    text = modelLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldPrimaryBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Personalization & Local Memory
            SettingsSectionHeader(title = "Personalization & Local Memory")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Preferred Explanation Level",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                            val isSelected = settings.preferredExplanationLevel.equals(level, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { repository.updatePreferredExplanationLevel(level) }
                            ) {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else OpenFieldNavyText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Response Length Preference",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Concise", "Balanced", "Detailed").forEach { len ->
                            val isSelected = settings.preferredResponseLength.equals(len, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { repository.updatePreferredResponseLength(len) }
                            ) {
                                Text(
                                    text = len,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else OpenFieldNavyText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Preferred Measurement Units",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OpenFieldNavyText
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Metric", "Imperial").forEach { unit ->
                            val isSelected = settings.preferredMeasurementUnits.equals(unit, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { repository.updatePreferredMeasurementUnits(unit) }
                            ) {
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else OpenFieldNavyText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Local Memory Inspector & Reset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editedMemory = settings.userCustomMemory
                                showMemoryDialog = true
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.Psychology)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Inspect & Control Local Memory",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = if (settings.userCustomMemory.isBlank()) "No custom preferences saved" else "Custom memory context active",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Storage & Knowledge
            SettingsSectionHeader(title = "Storage & Installed Data")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBadge(icon = Icons.Default.Storage)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Installed Knowledge Packs",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = "$installedPacksCount installed · ${documents.size} imported documents",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Storage visual breakdown bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.78f)
                                .fillMaxSize()
                                .background(OpenFieldPrimaryBlue)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.14f)
                                .fillMaxSize()
                                .background(Color(0xFF0D9488))
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.08f)
                                .fillMaxSize()
                                .background(Color(0xFF94A3B8))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OpenFieldPrimaryBlue))
                            Text(text = "Model: ~2.59 GB", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.5.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF0D9488)))
                            Text(text = "Packs & Docs", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.5.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF94A3B8)))
                            Text(text = "Cache", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.5.sp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                repository.clearAppCache(context)
                                Toast.makeText(context, "Temporary cache cleared", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.CleaningServices)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Clear Cache & Temporary Files",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "Frees temporary scratch and preview bitmaps",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export Local Backup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                exportBackupLauncher.launch("openfield_backup_$timestamp.json")
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.Download)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Export Local Backup (.json)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "Save chats, drafts, and lessons to your storage",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Restore from Backup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                importBackupLauncher.launch(arrayOf("application/json", "text/*"))
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.Upload)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Restore from Backup",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "Restore all knowledge and history from backup file",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Preferences & About
            SettingsSectionHeader(title = "App Information & Privacy")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAbout() }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.Info)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "About OpenField",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "Version 1.0.0",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIconBadge(icon = Icons.Default.Security)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Zero Telemetry & Local Privacy",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = "All prompts, images and documents remain on this phone",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Real-Time Performance & Diagnostics
            Text(
                text = "Performance & Diagnostics",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = OpenFieldNavyText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsIconBadge(icon = Icons.Default.Speed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Live Edge Inference Metrics",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = "Measured directly on this hardware",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (metrics.lastTokensPerSec > 0.0) Color(0xFFEAF7ED) else OpenFieldSecondaryBackground)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (metrics.lastTokensPerSec > 0.0) OpenFieldSuccess else Color(0xFF94A3B8))
                            )
                            Text(
                                text = if (metrics.lastTokensPerSec > 0.0) "Active" else "Ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (metrics.lastTokensPerSec > 0.0) OpenFieldSuccess else OpenFieldSecondaryText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Time to First Token (TTFT)", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 11.sp))
                            Text(if (metrics.lastTimeToFirstTokenMs > 0) "${metrics.lastTimeToFirstTokenMs} ms" else "—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Generation Speed", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 11.sp))
                            Text(if (metrics.lastTokensPerSec > 0.0) "${metrics.lastTokensPerSec} tok/s" else "—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Model Cold Load", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 11.sp))
                            Text(if (metrics.coldInitTimeMs > 0) "${metrics.coldInitTimeMs} ms (${metrics.activeBackend})" else "—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Memory Footprint", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 11.sp))
                            Text("${metrics.currentMemoryUsedMb} MB / ${metrics.maxAvailableMemoryMb} MB", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Retrieval Latency (FTS4)", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 11.sp))
                            Text(if (metrics.lastRetrievalLatencyMs > 0) "${metrics.lastRetrievalLatencyMs} ms" else "—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Vision Preprocess", style = MaterialTheme.typography.bodySmall.copy(color = OpenFieldSecondaryText, fontSize = 11.sp))
                            Text(if (metrics.lastImagePreprocessingMs > 0) "${metrics.lastImagePreprocessingMs} ms" else "—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showModelDialog) {
            ModelManagementDialog(
                modelRepository = repository.modelRepository,
                onDismissRequest = { showModelDialog = false }
            )
        }

        if (showMemoryDialog) {
            AlertDialog(
                onDismissRequest = { showMemoryDialog = false },
                title = {
                    Text(
                        text = "On-Device Memory & Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = OpenFieldNavyText
                        )
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "OpenField operates offline with zero cloud telemetry. You can teach the model facts about your field work or background, or wipe remembered context anytime.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OpenFieldSecondaryText,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editedMemory,
                            onValueChange = { editedMemory = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = {
                                Text(
                                    text = "e.g. 'I work in solar electrical installation', 'Always include safety warnings', 'Prefer metric units'...",
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        repository.updateUserCustomMemory(editedMemory)
                        showMemoryDialog = false
                        Toast.makeText(context, "Memory updated", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Save", color = OpenFieldPrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        repository.clearUserCustomMemory()
                        editedMemory = ""
                        showMemoryDialog = false
                        Toast.makeText(context, "Memory cleared", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Clear All", color = Color(0xFFD32F2F))
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = OpenFieldSecondaryText
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsIconBadge(icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = OpenFieldPrimaryBlueLight,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OpenFieldPrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

