package com.openfield.app

import com.openfield.app.feature.ai.mode.ModeConfigRegistry
import com.openfield.app.feature.ai.mode.ModeInputType
import com.openfield.app.feature.ai.mode.ModeOutputFormat
import com.openfield.app.feature.ai.mode.OpenFieldMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeConfigurationTest {

    @Test
    fun allModes_areDefinedAndConfigured() {
        val modes = OpenFieldMode.entries
        assertEquals(7, modes.size)

        for (mode in modes) {
            val config = ModeConfigRegistry.get(mode)
            assertNotNull("Config for $mode must exist", config)
            assertEquals(mode, config.mode)
            assertTrue("System instruction for $mode must not be blank", config.systemInstruction.isNotBlank())
            assertNotNull(config.defaultGenConfig)
            assertTrue("Default topP must be positive", config.defaultGenConfig.topP > 0.0)
            assertTrue("Default topK must be positive", config.defaultGenConfig.topK > 0)
        }
    }

    @Test
    fun askAiMode_hasCorrectSettings() {
        val config = ModeConfigRegistry.get(OpenFieldMode.ASK_AI)
        assertEquals("Ask AI", config.displayName)
        assertEquals(ModeInputType.TEXT_ONLY, config.inputType)
        assertEquals(ModeOutputFormat.NATURAL_STREAMING, config.outputFormat)
        assertTrue(config.requiresSession)
        assertTrue(config.systemInstruction.contains("Do not emit quizzes"))
    }

    @Test
    fun solveProblemMode_hasStructuredOutput() {
        val config = ModeConfigRegistry.get(OpenFieldMode.SOLVE_PROBLEM)
        assertEquals(ModeOutputFormat.STRUCTURED_JSON, config.outputFormat)
        assertEquals(ModeInputType.STRUCTURED_INPUT, config.inputType)
        assertTrue(config.requiresSession)
    }

    @Test
    fun learnExplainMode_hasStructuredOutput() {
        val config = ModeConfigRegistry.get(OpenFieldMode.LEARN_EXPLAIN)
        assertEquals(ModeOutputFormat.STRUCTURED_JSON, config.outputFormat)
        assertEquals(ModeInputType.STRUCTURED_INPUT, config.inputType)
    }

    @Test
    fun createWriteMode_hasDocumentOutput() {
        val config = ModeConfigRegistry.get(OpenFieldMode.CREATE_WRITE)
        assertEquals(ModeOutputFormat.MARKDOWN_DOCUMENT, config.outputFormat)
        assertEquals(ModeInputType.TEXT_ONLY, config.inputType)
    }

    @Test
    fun modeFromId_handlesKnownAndUnknown() {
        assertEquals(OpenFieldMode.ASK_AI, OpenFieldMode.fromId("ask_ai"))
        assertEquals(OpenFieldMode.ANALYZE_IMAGE, OpenFieldMode.fromId("analyze_image"))
        assertEquals(OpenFieldMode.ASK_DOCUMENT, OpenFieldMode.fromId("ask_document"))
        assertEquals(OpenFieldMode.SOLVE_PROBLEM, OpenFieldMode.fromId("solve_problem"))
        assertEquals(OpenFieldMode.LEARN_EXPLAIN, OpenFieldMode.fromId("learn_explain"))
        assertEquals(OpenFieldMode.CREATE_WRITE, OpenFieldMode.fromId("create_write"))
        assertEquals(OpenFieldMode.AI_TOOLS, OpenFieldMode.fromId("ai_tools"))

        // Case insensitivity
        assertEquals(OpenFieldMode.SOLVE_PROBLEM, OpenFieldMode.fromId("SOLVE_PROBLEM"))

        // Unknown fallback
        assertEquals(OpenFieldMode.ASK_AI, OpenFieldMode.fromId("unknown_mode"))
    }
}
