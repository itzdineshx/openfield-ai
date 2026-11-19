package com.openfield.app

import com.openfield.app.feature.voice.VoiceInputState
import com.openfield.app.feature.voice.engine.SpeechToTextEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SpeechToTextLifecycleTest {

    private class FakeSpeechEngine : SpeechToTextEngine {
        private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
        override val state: StateFlow<VoiceInputState> = _state.asStateFlow()

        var isStarted = false
        var isStopped = false
        var isCancelled = false
        var isReleased = false
        var callback: ((String) -> Unit)? = null

        override fun isAvailable(): Boolean = true

        override fun startListening(locale: Locale, onResult: (String) -> Unit) {
            isStarted = true
            callback = onResult
            _state.value = VoiceInputState.Listening
        }

        override fun stopListening() {
            isStopped = true
            _state.value = VoiceInputState.Processing
        }

        override fun cancelListening() {
            isCancelled = true
            _state.value = VoiceInputState.Idle
        }

        override fun release() {
            isReleased = true
            _state.value = VoiceInputState.Idle
        }

        fun emitTranscript(text: String) {
            _state.value = VoiceInputState.Idle
            callback?.invoke(text)
        }
    }

    @Test
    fun engineLifecycle_startStopAndEmitTranscript() {
        val engine = FakeSpeechEngine()
        assertEquals(VoiceInputState.Idle, engine.state.value)

        var recognizedText = ""
        engine.startListening { recognizedText = it }

        assertTrue(engine.isStarted)
        assertEquals(VoiceInputState.Listening, engine.state.value)

        engine.stopListening()
        assertTrue(engine.isStopped)
        assertEquals(VoiceInputState.Processing, engine.state.value)

        engine.emitTranscript("How to fix laptop blue screen")
        assertEquals("How to fix laptop blue screen", recognizedText)
        assertEquals(VoiceInputState.Idle, engine.state.value)
    }

    @Test
    fun engineLifecycle_cancellationResetsToIdle() {
        val engine = FakeSpeechEngine()
        engine.startListening {}
        assertEquals(VoiceInputState.Listening, engine.state.value)

        engine.cancelListening()
        assertTrue(engine.isCancelled)
        assertEquals(VoiceInputState.Idle, engine.state.value)
    }

    @Test
    fun engineLifecycle_releaseCleansUpResources() {
        val engine = FakeSpeechEngine()
        engine.startListening {}
        engine.release()
        assertTrue(engine.isReleased)
        assertEquals(VoiceInputState.Idle, engine.state.value)
    }
}
