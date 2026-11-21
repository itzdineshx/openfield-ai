package com.openfield.app.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.openfield.app.core.components.PrimaryButton
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldError
import com.openfield.app.core.theme.OpenFieldMutedText
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.core.theme.OpenFieldSuccess
import com.openfield.app.core.theme.OpenFieldSuccessBg

data class OnboardingFeatureItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

data class OnboardingPageData(
    val stepLabel: String,
    val pageIndex: Int,
    val title: String,
    val description: String,
    val features: List<OnboardingFeatureItem>
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPage by remember { mutableIntStateOf(0) }

    val permissionsToRequest = remember {
        val list = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onFinishOnboarding()
    }

    val pages = listOf(
        OnboardingPageData(
            stepLabel = "1/4",
            pageIndex = 0,
            title = "Ask Anything",
            description = "Get help, learn new things, solve problems and understand the world around you — even offline.",
            features = listOf(
                OnboardingFeatureItem(
                    icon = Icons.Default.Image,
                    title = "Text, Image & Voice",
                    subtitle = "Ask in the way you prefer"
                ),
                OnboardingFeatureItem(
                    icon = Icons.Default.WifiOff,
                    title = "Offline AI",
                    subtitle = "Works without internet"
                ),
                OnboardingFeatureItem(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Your Knowledge",
                    subtitle = "Use built-in and downloaded resources"
                )
            )
        ),
        OnboardingPageData(
            stepLabel = "2/4",
            pageIndex = 1,
            title = "Understand & Solve",
            description = "From everyday questions to technical issues, get clear, step-by-step guidance.",
            features = listOf(
                OnboardingFeatureItem(
                    icon = Icons.Default.Build,
                    title = "Troubleshoot & Fix",
                    subtitle = "Diagnose and get steps"
                ),
                OnboardingFeatureItem(
                    icon = Icons.Default.Psychology,
                    title = "Learn & Explore",
                    subtitle = "Understand concepts easily"
                ),
                OnboardingFeatureItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Analyze Information",
                    subtitle = "Summarize, explain and extract key points"
                )
            )
        ),
        OnboardingPageData(
            stepLabel = "3/4",
            pageIndex = 2,
            title = "Use Your Own Knowledge",
            description = "Add your documents, manuals or notes and ask questions about them.",
            features = listOf(
                OnboardingFeatureItem(
                    icon = Icons.Default.Description,
                    title = "Ask Your Documents",
                    subtitle = "Get answers from PDFs and notes"
                ),
                OnboardingFeatureItem(
                    icon = Icons.Default.FolderSpecial,
                    title = "Build Your Library",
                    subtitle = "Keep important resources offline"
                ),
                OnboardingFeatureItem(
                    icon = Icons.Default.Security,
                    title = "Private & Local",
                    subtitle = "Your data stays on your device"
                )
            )
        ),
        OnboardingPageData(
            stepLabel = "4/4",
            pageIndex = 3,
            title = "App Permissions",
            description = "Grant essential device permissions for offline voice queries and background model download tracking.",
            features = listOf(
                OnboardingFeatureItem(
                    icon = Icons.Default.Mic,
                    title = "Microphone (Voice Input)",
                    subtitle = "Ask questions naturally using offline Speech-to-Text"
                ),
                OnboardingFeatureItem(
                    icon = Icons.Default.Notifications,
                    title = "Download Notifications",
                    subtitle = "Get notified when the on-device AI model completes"
                ),
                OnboardingFeatureItem(
                    icon = Icons.Default.Security,
                    title = "100% On-Device Privacy",
                    subtitle = "Zero audio, prompts, or documents are ever uploaded"
                )
            )
        )
    )

    val currentData = pages[currentPage]

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
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Bar: Step Indicator (1/4), Dots, and Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentData.stepLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = OpenFieldSecondaryText,
                        fontSize = 13.sp
                    )
                )

                // Dot Indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == currentPage) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentPage) OpenFieldPrimaryBlue
                                    else OpenFieldMutedText.copy(alpha = 0.4f)
                                )
                        )
                        if (idx < 3) Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = OpenFieldSecondaryText,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.clickable { onFinishOnboarding() }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Text(
                text = currentData.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = OpenFieldNavyText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = currentData.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = OpenFieldSecondaryText
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Center Visual Illustration matching each screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(OpenFieldSecondaryBackground),
                contentAlignment = Alignment.Center
            ) {
                when (currentPage) {
                    0 -> OnboardingIllustrationChat()
                    1 -> OnboardingIllustrationTroubleshoot()
                    2 -> OnboardingIllustrationDocument()
                    3 -> OnboardingIllustrationPermissions()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3 Feature Bullet Rows
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                currentData.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OpenFieldPrimaryBlueLight,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = null,
                                    tint = OpenFieldPrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = OpenFieldNavyText
                                )
                            )
                            Text(
                                text = feature.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = OpenFieldSecondaryText
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Button: "Next →" or "Grant Permissions & Continue →"
            PrimaryButton(
                text = if (currentPage == 3) "Grant Permissions & Continue" else "Next",
                onClick = {
                    if (currentPage < 3) {
                        currentPage += 1
                    } else {
                        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        } else true

                        if (hasMic && hasNotif) {
                            onFinishOnboarding()
                        } else {
                            permissionLauncher.launch(permissionsToRequest)
                        }
                    }
                },
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun OnboardingIllustrationChat() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chat bubble card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
            modifier = Modifier.size(72.dp, 56.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OpenFieldPrimaryBlue)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OpenFieldCardBorder)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OpenFieldCardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Image picture card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OpenFieldPrimaryBlueLight,
            modifier = Modifier.size(68.dp, 56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = OpenFieldPrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingIllustrationTroubleshoot() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.size(130.dp, 78.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp, 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(OpenFieldNavyText.copy(alpha = 0.7f))
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp, 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(OpenFieldNavyText.copy(alpha = 0.7f))
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = OpenFieldPrimaryBlue,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun OnboardingIllustrationDocument() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.size(80.dp, 94.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = OpenFieldError,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OpenFieldCardBorder)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(28.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OpenFieldCardBorder)
            )
        }
    }
}

@Composable
private fun OnboardingIllustrationPermissions() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, OpenFieldCardBorder),
        modifier = Modifier.size(170.dp, 80.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Surface(
                shape = CircleShape,
                color = OpenFieldPrimaryBlueLight,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = OpenFieldPrimaryBlueLight,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = OpenFieldSuccessBg,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = OpenFieldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
