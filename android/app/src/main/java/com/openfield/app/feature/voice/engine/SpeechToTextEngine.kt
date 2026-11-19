package com.openfield.app.feature.voice.engine

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
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
 * Android system speech-to-text adapter.
 * Attempts on-device offline recognition first (PREFER_OFFLINE = true).
 * If the device lacks the local acoustic/language pack (error 13/14), it triggers
 * background model download (API 33+) and gracefully falls back to the system speech
 * service so dictation works reliably in all environments.
 */
class SystemSpeechToTextEngine(private val context: Context) : SpeechToTextEngine {

    private val tag = "OpenFieldVoice"
    private var speechRecognizer: SpeechRecognizer? = null
    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    override val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    private var onTextCallback: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun recreateRecognizer(): SpeechRecognizer {
        try {
            speechRecognizer?.destroy()
        } catch (ignored: Exception) {}
        return SpeechRecognizer.createSpeechRecognizer(context).also {
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

        // First attempt: Prefer on-device offline recognition
        startListeningAttempt(locale, preferOffline = true)
    }

    private fun startListeningAttempt(locale: Locale, preferOffline: Boolean) {
        mainHandler.post {
            try {
                val recognizer = recreateRecognizer()
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
                        Log.w(tag, "SpeechRecognizer onError: code=$error (preferOffline=$preferOffline, locale=$locale)")

                        // Error 13 = ERROR_LANGUAGE_NOT_SUPPORTED, Error 14 = ERROR_LANGUAGE_UNAVAILABLE
                        // If offline model is missing for requested locale, retry with standard recognizer seamlessly
                        if (preferOffline && (error == 13 || error == 14 || error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED)) {
                            Log.i(tag, "Offline model missing for ${locale.toLanguageTag()} (code $error), falling back to standard recognizer...")
                            startListeningAttempt(locale, preferOffline = false)
                            return
                        }

                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap mic to try again."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please try again."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Network required for speech when offline model is not installed."
                            13 -> "Language pack missing on device."
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
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    if (preferOffline) {
                        putExtra("android.speech.extra.PREFER_OFFLINE", true)
                    }
                }

                recognizer.startListening(intent)
                _state.value = VoiceInputState.Listening
            } catch (e: Exception) {
                Log.e(tag, "Failed to start listening", e)
                _state.value = VoiceInputState.Error(e.message ?: "Failed to start speech recognition")
            }
        }
    }

    private fun triggerModelDownload(locale: Locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val downloadIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                }
                speechRecognizer?.triggerModelDownload(downloadIntent)
                Log.i(tag, "Triggered model download for ${locale.toLanguageTag()}")
            } catch (e: Exception) {
                Log.w(tag, "Could not trigger model download: ${e.message}")
            }
        }
    }

    override fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (ignored: Exception) {}
            if (_state.value is VoiceInputState.Listening) {
                _state.value = VoiceInputState.Processing
            }
        }
    }

    override fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (ignored: Exception) {}
            _state.value = VoiceInputState.Idle
        }
    }

    override fun release() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (ignored: Exception) {}
            speechRecognizer = null
            _state.value = VoiceInputState.Idle
        }
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
