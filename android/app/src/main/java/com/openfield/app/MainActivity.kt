package com.openfield.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldTheme
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.navigation.AppNavigation
import com.openfield.app.navigation.AppRoutes
import com.openfield.app.navigation.NavigationDispatcher

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val settings by OpenFieldRepository.getInstance().settings.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (settings.themeMode) {
                "Dark", "Dark (AMOLED)", "Dark (Obsidian AMOLED)" -> true
                "Light" -> false
                else -> systemInDark
            }

            OpenFieldTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = OpenFieldBackground
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        when {
            // App Launcher Shortcuts
            action == "com.openfield.app.ACTION_CHAT" -> {
                NavigationDispatcher.navigateTo(AppRoutes.CHATS)
            }
            action == "com.openfield.app.ACTION_IMAGE" -> {
                NavigationDispatcher.navigateTo(AppRoutes.IMAGE_ANALYSIS)
            }
            action == "com.openfield.app.ACTION_DOCUMENT" -> {
                NavigationDispatcher.navigateTo(AppRoutes.DOCUMENT_QA)
            }
            action == "com.openfield.app.ACTION_PROBLEM" -> {
                NavigationDispatcher.navigateTo(AppRoutes.PROBLEM_SOLVER)
            }

            // Android System Share Target (ACTION_SEND)
            action == Intent.ACTION_SEND -> {
                val streamUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }

                if (type?.startsWith("image/") == true && streamUri != null) {
                    OpenFieldRepository.getInstance().imageAnalysisController.setImage(this, streamUri)
                    NavigationDispatcher.navigateTo(AppRoutes.IMAGE_ANALYSIS)
                } else if ((type == "application/pdf" || type?.contains("pdf") == true) && streamUri != null) {
                    OpenFieldRepository.getInstance().documentQaController.importPdf(this, streamUri)
                    NavigationDispatcher.navigateTo(AppRoutes.DOCUMENT_QA)
                } else if (type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        OpenFieldRepository.getInstance().sharedContextManager.handoffToWriter(
                            title = "Shared Text",
                            initialDraft = sharedText
                        )
                        NavigationDispatcher.navigateTo(AppRoutes.CREATE_WRITE)
                    }
                }
            }
        }
    }
}
