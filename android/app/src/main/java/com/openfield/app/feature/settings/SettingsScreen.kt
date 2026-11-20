package com.openfield.app.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.openfield.app.core.util.PerformanceTracker
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.ai.model.ModelManagementDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showClearAllDataDialog by remember { mutableStateOf(false) }
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
            Spacer(modifier = Modifier.height(8.dp))

            // =================================================================
            // 1. SYSTEM & AI ENGINE
            // =================================================================
            SettingsSectionHeader(title = "AI Engine & Core System")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Offline Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                    text = "100% on-device local execution",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldSecondaryText,
                                        fontSize = 11.5.sp
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

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = OpenFieldCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Model Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showModelDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                        "Verifying checksum..."
                                    is com.openfield.app.feature.ai.model.ModelInstallationState.Error ->
                                        "Setup needed — tap to resolve"
                                    else -> "Gemma 4 E2B IT (Local)"
                                }
                                Text(
                                    text = modelLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OpenFieldPrimaryBlue,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.45f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =================================================================
            // 2. DISPLAY & THEME (AMOLED)
            // =================================================================
            SettingsSectionHeader(title = "Display & Appearance")

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
                        SettingsIconBadge(icon = Icons.Default.Lightbulb)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Theme & Battery Saver",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = "Obsidian AMOLED uses true pitch black (#000000) for OLED power saving",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Light" to "Light",
                            "Dark (AMOLED)" to "Obsidian AMOLED",
                            "System" to "System"
                        ).forEach { (modeValue, displayLabel) ->
                            val isSelected = when (modeValue) {
                                "Dark (AMOLED)" -> settings.themeMode.contains("Dark") || settings.themeMode.contains("Obsidian") || settings.themeMode.contains("AMOLED")
                                "Light" -> settings.themeMode == "Light"
                                else -> settings.themeMode.contains("System") || settings.themeMode.isBlank()
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryBackground,
                                border = BorderStroke(1.dp, if (isSelected) OpenFieldPrimaryBlue else OpenFieldCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val saveValue = when (modeValue) {
                                            "Dark (AMOLED)" -> "Dark (AMOLED)"
                                            "Light" -> "Light"
                                            else -> "System Default"
                                        }
                                        repository.updateThemeMode(saveValue)
                                    }
                            ) {
                                Text(
                                    text = displayLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else OpenFieldNavyText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =================================================================
            // 3. AI BEHAVIOR & ON-DEVICE MEMORY
            // =================================================================
            SettingsSectionHeader(title = "AI Behavior & Personalization")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Explanation Depth",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OpenFieldNavyText,
                            fontSize = 13.sp
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
                                shape = RoundedCornerShape(12.dp),
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
                                        fontSize = 11.5.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Response Length",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = OpenFieldNavyText,
                            fontSize = 13.sp
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
                                shape = RoundedCornerShape(12.dp),
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
                                        fontSize = 11.5.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OpenFieldCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Local Memory Inspector & Control
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                editedMemory = settings.userCustomMemory
                                showMemoryDialog = true
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            SettingsIconBadge(icon = Icons.Default.Psychology)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "On-Device Memory & Preferences",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenFieldNavyText
                                    )
                                )
                                Text(
                                    text = if (settings.userCustomMemory.isBlank()) "Tap to teach model facts about your work" else "Custom memory context active",
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
                            tint = OpenFieldSecondaryText.copy(alpha = 0.45f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =================================================================
            // 4. STORAGE & LOCAL DATA MANAGEMENT
            // =================================================================
            SettingsSectionHeader(title = "Storage & Local Data")

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
                                text = "Installed Knowledge & Documents",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = "$installedPacksCount knowledge packs · ${documents.size} personal documents",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Storage visual breakdown bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.80f)
                                .height(6.dp)
                                .background(OpenFieldPrimaryBlue)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.12f)
                                .height(6.dp)
                                .background(Color(0xFF0D9488))
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.08f)
                                .height(6.dp)
                                .background(Color(0xFF94A3B8))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(OpenFieldPrimaryBlue))
                            Text(text = "Model: ~2.59 GB", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF0D9488)))
                            Text(text = "Packs & Docs", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF94A3B8)))
                            Text(text = "Cache", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.sp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = OpenFieldCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Action Rows
                    SettingsActionRow(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear Cache & Temporary Files",
                        subtitle = "Frees temporary scratch and preview bitmaps",
                        onClick = {
                            repository.clearAppCache(context)
                            Toast.makeText(context, "Temporary cache cleared", Toast.LENGTH_SHORT).show()
                        }
                    )

                    SettingsActionRow(
                        icon = Icons.Default.Download,
                        title = "Export Local Backup (.json)",
                        subtitle = "Save chats, drafts, and lessons to your storage",
                        onClick = {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            exportBackupLauncher.launch("crux_backup_$timestamp.json")
                        }
                    )

                    SettingsActionRow(
                        icon = Icons.Default.Upload,
                        title = "Restore from Backup",
                        subtitle = "Restore knowledge and history from backup file",
                        onClick = {
                            importBackupLauncher.launch(arrayOf("application/json", "text/*"))
                        }
                    )

                    SettingsActionRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear All CRUX Data",
                        subtitle = "Reset all chats, documents, drafts, and local indexes",
                        tint = Color(0xFFD32F2F),
                        onClick = { showClearAllDataDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =================================================================
            // 5. PRIVACY, ABOUT & INFERENCE DIAGNOSTICS
            // =================================================================
            SettingsSectionHeader(title = "App Information & Privacy")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingsActionRow(
                        icon = Icons.Default.Info,
                        title = "About CRUX",
                        subtitle = "Version 1.0.0 · Apache 2.0 Open Source",
                        onClick = onNavigateToAbout
                    )

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
                                text = "Zero Telemetry Guarantee",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = "All prompts, images and documents remain private on this phone",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Minimal Live Edge Metrics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = OpenFieldSecondaryBackground),
                border = BorderStroke(1.dp, OpenFieldCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = OpenFieldPrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hardware Inference Diagnostics",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = OpenFieldNavyText
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (metrics.lastTokensPerSec > 0.0) OpenFieldSuccess else Color(0xFF94A3B8))
                            )
                            Text(
                                text = if (metrics.lastTokensPerSec > 0.0) "Active" else "Idle",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (metrics.lastTokensPerSec > 0.0) OpenFieldSuccess else OpenFieldSecondaryText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TTFT", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.sp))
                            Text(if (metrics.lastTimeToFirstTokenMs > 0) "${metrics.lastTimeToFirstTokenMs} ms" else "—", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Generation Speed", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.sp))
                            Text(if (metrics.lastTokensPerSec > 0.0) "${metrics.lastTokensPerSec} tok/s" else "—", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("RAM Footprint", style = MaterialTheme.typography.labelSmall.copy(color = OpenFieldSecondaryText, fontSize = 10.sp))
                            Text("${metrics.currentMemoryUsedMb} MB", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = OpenFieldNavyText))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // =====================================================================
        // DIALOGS (Preserved 100% Functionality)
        // =====================================================================
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
                            text = "CRUX operates offline with zero cloud telemetry. You can teach the model facts about your work or field context, or wipe remembered facts anytime.",
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
                                    text = "e.g. 'I work in solar electrical systems', 'Always provide safety cautions first'...",
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
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = OpenFieldBackground
            )
        }

        if (showClearAllDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDataDialog = false },
                title = {
                    Text(
                        text = "Clear All CRUX Data?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                },
                text = {
                    Text(
                        text = "This will permanently delete all local conversations, imported documents, extracted search indexes, saved answers, and custom memories.\n\nYour installed Gemma 4 AI model (~2.58 GB) will remain preserved in storage so you do not need to download it again.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = OpenFieldNavyText
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                repository.clearAllUserData()
                                showClearAllDataDialog = false
                                Toast.makeText(context, "All CRUX user data has been reset", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text("Reset Everything", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllDataDialog = false }) {
                        Text("Cancel", color = OpenFieldSecondaryText)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = OpenFieldBackground
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (tint != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(tint.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                SettingsIconBadge(icon = icon)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = tint ?: OpenFieldNavyText,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
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
            tint = (tint ?: OpenFieldSecondaryText).copy(alpha = 0.45f),
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = OpenFieldSecondaryText
        ),
        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsIconBadge(icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = OpenFieldPrimaryBlueLight,
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OpenFieldPrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
