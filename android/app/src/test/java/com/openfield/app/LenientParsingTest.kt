package com.openfield.app

import com.openfield.app.feature.ai.controller.WritingController
import com.openfield.app.feature.ai.mode.DiagnosticStepHistory
import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.LenientJson
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LenientParsingTest {

    @Test
    fun lenientJson_toleratesTrailingCommasUnquotedKeysAndComments() {
        val map = LenientJson.parseObject("{ question: 'Is it plugged in?', // check\n \"options\": [\"Yes\", \"No\",], }")
        assertNotNull(map)
        assertEquals("Is it plugged in?", map!!["question"])
        assertEquals(listOf("Yes", "No"), map["options"])
    }

    @Test
    fun lenientJson_unescapedInnerQuotesStayInString() {
        val map = LenientJson.parseObject("{\"text\": \"Press the \"Reset\" button\", \"n\": 2}")
        assertEquals("Press the \"Reset\" button", map!!["text"])
        assertEquals(2L, map["n"])
    }

    @Test
    fun lenientJson_truncatedOutputKeepsCompletedFields() {
        val map = LenientJson.parseObject("{\"question\": \"Does it beep?\", \"options\": [{\"id\": \"A\", \"text\": \"Yes\"}, {\"id\": \"B\", \"te")
        assertEquals("Does it beep?", map!!["question"])
        assertTrue((map["options"] as List<*>).isNotEmpty())
    }

    @Test
    fun lenientJson_writeThenParseRoundTrip() {
        val original = mapOf("a" to "line1\nline2 \"q\"", "b" to listOf(1, 2), "c" to null, "d" to true)
        val parsed = LenientJson.parseObject(LenientJson.write(original))!!
        assertEquals("line1\nline2 \"q\"", parsed["a"])
        assertEquals(listOf(1L, 2L), parsed["b"])
        assertNull(parsed["c"])
        assertEquals(true, parsed["d"])
    }

    @Test
    fun diagnostic_optionsAsPlainStringsGetLetterIds() {
        val raw = "{\"question\": \"What color is the LED?\", \"options\": [\"A) Red\", \"B) Green\", \"Off\"]}"
        val a = StructuredOutputParser.parseDiagnosticAssessment(raw)!!
        assertEquals(listOf("A", "B", "C"), a.options.map { it.id })
        assertEquals("Red", a.options[0].text)
    }

    @Test
    fun diagnostic_solutionArrayBecomesNumberedSteps() {
        val raw = "{\"question\": \"Done\", \"isResolutionReached\": true, \"likelyCause\": \"Dead battery\", " +
            "\"recommendedSolution\": [\"Charge the battery\", \"Test the voltage\"]}"
        val a = StructuredOutputParser.parseDiagnosticAssessment(raw)!!
        assertEquals("1. Charge the battery\n2. Test the voltage", a.recommendedSolution)
    }

    @Test
    fun diagnostic_serializationRoundTripsForResume() {
        val raw = "{\"category\": \"Network\", \"question\": \"Is the router light on?\", \"options\": [{\"id\":\"A\",\"text\":\"Yes\"},{\"id\":\"B\",\"text\":\"No\"}], \"currentStepNumber\": 2}"
        val a = StructuredOutputParser.parseDiagnosticAssessment(raw)!!
        val restored = StructuredOutputParser.deserializeDiagnostic(StructuredOutputParser.serializeDiagnostic(a))
        assertEquals(a, restored)
    }

    @Test
    fun diagnostic_legacyPlainQuestionIsNotDeserialized() {
        assertNull(StructuredOutputParser.deserializeDiagnostic("Is the router light on?"))
    }

    @Test
    fun history_roundTripsWithSpecialCharacters() {
        val history = listOf(DiagnosticStepHistory(1, "Sound?", "Clicks; then: silence"))
        assertEquals(history, StructuredOutputParser.deserializeHistory(StructuredOutputParser.serializeHistory(history)))
    }

    @Test
    fun lesson_quizAnswerGivenAsLetterOrText() {
        val byLetter = """{"overview": "o", "sections": [{"title": "S", "body": "b"}],
            "quiz": {"question": "Q?", "choices": ["x", "y", "z"], "answer": "B"}}"""
        assertEquals(1, StructuredOutputParser.parseStructuredLesson(byLetter, "T", LearningLevel.BEGINNER)!!.quiz!!.correctOptionIndex)

        val byText = """{"overview": "o", "sections": [{"heading": "S", "content": "b"}],
            "quiz": {"question": "Q?", "options": ["x", "y", "z"], "correctAnswer": "z"}}"""
        assertEquals(2, StructuredOutputParser.parseStructuredLesson(byText, "T", LearningLevel.BEGINNER)!!.quiz!!.correctOptionIndex)
    }

    @Test
    fun lesson_markdownFallbackSplitsSectionsAndExample() {
        val raw = "# Batteries\nA battery stores energy.\n\n## Chemistry\nReactions move electrons.\n\n## Everyday example\nA torch."
        val lesson = StructuredOutputParser.parseStructuredLesson(raw, "Batteries", LearningLevel.BEGINNER)!!
        assertEquals("Batteries", lesson.title)
        assertEquals("Chemistry", lesson.sections.single().heading)
        assertEquals("A torch.", lesson.practicalExample)
    }

    @Test
    fun lesson_serializationRoundTripsForResume() {
        val raw = """{"title": "Gravity", "overview": "Pull", "sections": [{"heading": "Mass", "content": "More mass"}],
            "practicalExample": "Apple", "quiz": {"question": "Q?", "options": ["a", "b"], "correctOptionIndex": 1, "explanation": "e"}}"""
        val lesson = StructuredOutputParser.parseStructuredLesson(raw, "Gravity", LearningLevel.ADVANCED)!!
        val restored = StructuredOutputParser.deserializeLesson(StructuredOutputParser.serializeLesson(lesson), "Gravity", LearningLevel.ADVANCED)
        assertEquals(lesson, restored)
    }

    @Test
    fun imageSections_numberedNextStepsAreNotCutOff() {
        val raw = """
**What I See:** A wilted houseplant in a pot.

**Suggested Next Steps:**
1. Check the soil moisture
2. Move it away from direct heat
3. Trim yellow leaves

**Uncertainty & Limitations:** Roots are not visible.
""".trimIndent()
        val s = StructuredOutputParser.parseImageSections(raw)
        assertEquals("A wilted houseplant in a pot.", s.whatISee)
        assertEquals(3, s.suggestedNextSteps.size)
        assertEquals("Roots are not visible.", s.uncertaintyAndLimitations)
    }

    @Test
    fun imageSections_unstructuredAnswerKeepsFullText() {
        val raw = "This is a red bicycle leaning on a wall.\nThe chain looks rusty."
        val s = StructuredOutputParser.parseImageSections(raw)
        assertEquals(raw, s.whatISee)
        assertEquals("This is a red bicycle leaning on a wall", s.subject)
    }

    @Test
    fun writing_cleanupRemovesPreambleAndClosingRemark() {
        val raw = "Sure, here is your email:\n\nSubject: Leave request\n\nHi Sam,\n\nThanks.\n\nLet me know if you'd like changes!"
        assertEquals("Subject: Leave request\n\nHi Sam,\n\nThanks.", WritingController.cleanGeneratedText(raw))
    }

    @Test
    fun writing_cleanupKeepsNormalOpeningLine() {
        val raw = "Here at Acme we value safety.\nSecond line."
        assertEquals(raw, WritingController.cleanGeneratedText(raw))
    }
}
