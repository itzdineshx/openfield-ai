package com.openfield.app.feature.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * Manages decoupled local Text-To-Speech (TTS) using Android's native on-device TTS engine.
 * Consumes zero NPU/GPU model memory, preserving all resources for Gemma inference.
 */
class SpeechSynthesisManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.getDefault())
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isInitialized = true
                }
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtteranceId.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentUtteranceId.value = null
                    }
                })
            }
        }
    }

    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isInitialized || tts == null) return

        stop()
        _currentUtteranceId.value = utteranceId

        // Strip markdown asterisks and backticks for smoother acoustic synthesis
        val cleanText = text
            .replace(Regex("[*#`_~>]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (ignored: Exception) {}
        _isSpeaking.value = false
        _currentUtteranceId.value = null
    }

    fun release() {
        stop()
        try {
            tts?.shutdown()
        } catch (ignored: Exception) {}
        tts = null
        isInitialized = false
    }
}
