package com.openfield.app.feature.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 */
class VoiceInputManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    private var onTextRecognizedCallback: ((String) -> Unit)? = null

    companion object {
        fun isVoiceRecognitionAvailable(context: Context): Boolean {
            return SpeechRecognizer.isRecognitionAvailable(context)
        }
    }

    private fun ensureRecognizer(): SpeechRecognizer {
        return speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            speechRecognizer = it
        }
    }

    fun startListening(
        locale: Locale = Locale.getDefault(),
        onResult: (String) -> Unit
    ) {
        if (!isVoiceRecognitionAvailable(context)) {
            _state.value = VoiceInputState.Error("Speech recognition is not available on this device.")
            return
        }

        stopListening()
        onTextRecognizedCallback = onResult

        try {
            val recognizer = ensureRecognizer()
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = VoiceInputState.Listening
                }

                override fun onBeginningOfSpeech() {
                    _state.value = VoiceInputState.Listening
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _state.value = VoiceInputState.Processing
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap mic to try again."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice input."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please try again."
                        else -> "Speech recognition error ($error)."
                    }
                    _state.value = VoiceInputState.Error(message)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestMatch = matches?.firstOrNull() ?: ""
                    _state.value = VoiceInputState.Idle
                    if (bestMatch.isNotBlank()) {
                        onTextRecognizedCallback?.invoke(bestMatch)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = partialMatches?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        onTextRecognizedCallback?.invoke(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Instruct Android speech engine to prefer on-device offline recognition
                putExtra("android.speech.extra.PREFER_OFFLINE", true)
            }

            recognizer.startListening(intent)
            _state.value = VoiceInputState.Listening
        } catch (e: Exception) {
            _state.value = VoiceInputState.Error(e.message ?: "Failed to start voice recognition")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (ignored: Exception) {}
        if (_state.value is VoiceInputState.Listening) {
            _state.value = VoiceInputState.Processing
        }
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
        } catch (ignored: Exception) {}
        _state.value = VoiceInputState.Idle
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
        } catch (ignored: Exception) {}
        speechRecognizer = null
        _state.value = VoiceInputState.Idle
    }
}
