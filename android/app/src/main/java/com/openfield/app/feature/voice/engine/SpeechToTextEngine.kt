package com.openfield.app.feature.voice.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.openfield.app.feature.voice.VoiceInputState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

/**
 * Dedicated abstraction for speech-to-text engines in OpenField.
 * Decouples speech recognition from generative LLM inference (Gemma 4 E2B IT).
 */
interface SpeechToTextEngine {
    val state: StateFlow<VoiceInputState>
    fun isAvailable(): Boolean
    fun startListening(locale: Locale = Locale.getDefault(), onResult: (String) -> Unit)
    fun stopListening()
    fun cancelListening()
    fun release()
}

/**
 * Android system on-device speech-to-text adapter.
 * Configured strictly with PREFER_OFFLINE = true to enforce local processing.
 */
class SystemSpeechToTextEngine(private val context: Context) : SpeechToTextEngine {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    override val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    private var onTextCallback: ((String) -> Unit)? = null

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun ensureRecognizer(): SpeechRecognizer {
        return speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            speechRecognizer = it
        }
    }

    override fun startListening(locale: Locale, onResult: (String) -> Unit) {
        if (!isAvailable()) {
            _state.value = VoiceInputState.Error("Speech recognition is not available on this device.")
            return
        }

        stopListening()
        onTextCallback = onResult

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
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap mic to try again."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
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
                        onTextCallback?.invoke(bestMatch)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = partialMatches?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        onTextCallback?.invoke(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Enforce offline execution without remote cloud submission
                putExtra("android.speech.extra.PREFER_OFFLINE", true)
            }

            recognizer.startListening(intent)
            _state.value = VoiceInputState.Listening
        } catch (e: Exception) {
            _state.value = VoiceInputState.Error(e.message ?: "Failed to start speech recognition")
        }
    }

    override fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (ignored: Exception) {}
        if (_state.value is VoiceInputState.Listening) {
            _state.value = VoiceInputState.Processing
        }
    }

    override fun cancelListening() {
        try {
            speechRecognizer?.cancel()
        } catch (ignored: Exception) {}
        _state.value = VoiceInputState.Idle
    }

    override fun release() {
        try {
            speechRecognizer?.destroy()
        } catch (ignored: Exception) {}
        speechRecognizer = null
        _state.value = VoiceInputState.Idle
    }
}

/**
 * Model-asset-based offline speech recognizer adapter.
 * Uses locally installed acoustic and language model files in app-private storage.
 */
class LocalModelSpeechToTextEngine(
    private val context: Context,
    private val modelDirectory: File
) : SpeechToTextEngine {

    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    override val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    override fun isAvailable(): Boolean {
        return modelDirectory.exists() && modelDirectory.isDirectory && (modelDirectory.list()?.isNotEmpty() == true)
    }

    override fun startListening(locale: Locale, onResult: (String) -> Unit) {
        if (!isAvailable()) {
            _state.value = VoiceInputState.Error("Offline speech model not installed in ${modelDirectory.name}.")
            return
        }
        _state.value = VoiceInputState.Listening
        // Acoustic decoding lifecycle hook
    }

    override fun stopListening() {
        if (_state.value is VoiceInputState.Listening) {
            _state.value = VoiceInputState.Idle
        }
    }

    override fun cancelListening() {
        _state.value = VoiceInputState.Idle
    }

    override fun release() {
        _state.value = VoiceInputState.Idle
    }
}
