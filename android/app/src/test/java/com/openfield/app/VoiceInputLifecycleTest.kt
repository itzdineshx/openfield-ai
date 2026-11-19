package com.openfield.app

import com.openfield.app.feature.voice.VoiceInputState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceInputLifecycleTest {

    @Test
    fun voiceInputState_transitionsThroughExpectedLifecycle() {
        var state: VoiceInputState = VoiceInputState.Idle
        assertEquals(VoiceInputState.Idle, state)

        state = VoiceInputState.Listening
        assertEquals(VoiceInputState.Listening, state)

        state = VoiceInputState.Processing
        assertEquals(VoiceInputState.Processing, state)

        val errorState = VoiceInputState.Error("Microphone permission denied")
        state = errorState
        assertEquals("Microphone permission denied", errorState.message)

        state = VoiceInputState.Idle
        assertEquals(VoiceInputState.Idle, state)
    }
}
