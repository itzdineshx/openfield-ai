package com.openfield.app.feature.voice

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

class VoiceInputHandle internal constructor(
    private val isListeningState: State<Boolean>,
    val toggle: () -> Unit,
    val stop: () -> Unit,
    val cancel: () -> Unit
) {
    val isListening: Boolean get() = isListeningState.value
}

/**
 * Microphone dictation for any text field: asks for permission on first use and appends
 * the recognized text through [onText].
 */
@Composable
fun rememberVoiceInput(onText: (String) -> Unit): VoiceInputHandle {
    val context = LocalContext.current
    val manager = remember { VoiceInputManager(context) }
    val state = manager.state.collectAsState()
    val listening = remember { derivedStateOf { state.value is VoiceInputState.Listening } }
    val latestOnText by rememberUpdatedState(onText)

    DisposableEffect(Unit) {
        onDispose { manager.release() }
    }

    LaunchedEffect(state.value) {
        val current = state.value
        if (current is VoiceInputState.Error) {
            Toast.makeText(context, current.message, Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            manager.startListening { latestOnText(it) }
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    return remember {
        VoiceInputHandle(
            isListeningState = listening,
            toggle = {
                if (state.value is VoiceInputState.Listening) manager.stopListening()
                else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            stop = { manager.stopListening() },
            cancel = { manager.cancelListening() }
        )
    }
}
