package com.openfield.app.feature.voice

import android.content.Context
import com.openfield.app.feature.voice.engine.LocalModelSpeechToTextEngine
import com.openfield.app.feature.voice.engine.SpeechToTextEngine
import com.openfield.app.feature.voice.engine.SystemSpeechToTextEngine
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.Locale

sealed interface VoiceInputState {
    data object Idle : VoiceInputState
    data object Listening : VoiceInputState
    data object Processing : VoiceInputState
    data class Error(val message: String) : VoiceInputState
}

/**
 * Manages explicit, user-triggered on-device speech recognition.
 * Strictly enforces:
 * - User-initiated recording only (zero background or always-listening loops).
 * - Immediate termination on silence, completion, or user cancellation.
 * - Transcript emitted for preview and editing before dispatch.
 * - Priority: Dedicated local acoustic model if installed, otherwise offline System engine.
 */
class VoiceInputManager(
    private val context: Context,
    customEngine: SpeechToTextEngine? = null
) {
    private val speechModelDir = File(context.filesDir, "speech_models/default")

    private val engine: SpeechToTextEngine = customEngine ?: run {
        val localEngine = LocalModelSpeechToTextEngine(context, speechModelDir)
        if (localEngine.isAvailable()) {
            localEngine
        } else {
            SystemSpeechToTextEngine(context)
        }
    }

    val state: StateFlow<VoiceInputState> = engine.state

    companion object {
        fun isVoiceRecognitionAvailable(context: Context): Boolean {
            val speechModelDir = File(context.filesDir, "speech_models/default")
            val localEngine = LocalModelSpeechToTextEngine(context, speechModelDir)
            return localEngine.isAvailable() || SystemSpeechToTextEngine(context).isAvailable()
        }
    }

    fun startListening(
        locale: Locale = Locale.getDefault(),
        onResult: (String) -> Unit
    ) {
        engine.startListening(locale, onResult)
    }

    fun stopListening() {
        engine.stopListening()
    }

    fun cancelListening() {
        engine.cancelListening()
    }

    fun release() {
        engine.release()
    }
}
