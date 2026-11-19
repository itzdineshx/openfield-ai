package com.openfield.app

import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.LessonMarkupParser
import com.openfield.app.feature.ai.mode.OutputValidator
import com.openfield.app.feature.ai.mode.PromptBuilder
import com.openfield.app.feature.ai.mode.QuizProgress
import com.openfield.app.feature.ai.mode.StructuredOutputParser
import com.openfield.app.feature.ai.mode.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonMarkupParserTest {

    private val photosynthesis = """
TITLE: How Plants Make Food
OVERVIEW: Photosynthesis is how plants turn light into chemical energy. It feeds almost all life on Earth.
KEY: Plants capture sunlight with chlorophyll.
KEY: Water and carbon dioxide are the raw materials.
KEY: Glucose and oxygen are the products.
FLOW: Light is absorbed -> Water is split -> CO2 is fixed -> Glucose is made
PART: Chloroplast = where photosynthesis happens
PART: Stomata = pores that let CO2 in
SECTION: Capturing light
Chlorophyll absorbs red and blue light and reflects green.
SECTION: Making sugar
The Calvin cycle uses energy to build glucose from CO2.
EXAMPLE: A solar panel charging a battery is like a leaf storing sunlight as sugar.
TERM: Chlorophyll = green pigment that absorbs light
TERM: Glucose = a simple sugar plants use for energy
QUIZ: What gas do plants release during photosynthesis?
A) Carbon dioxide
B) Oxygen
C) Nitrogen
D) Methane
ANSWER: B
WHY: Water is split and oxygen is released as a by-product.
HINT: It is the gas we breathe in.
QUIZ: Where does photosynthesis happen?
A) Mitochondria
B) Nucleus
C) Chloroplast
ANSWER: C
WHY: Chloroplasts contain chlorophyll.
HINT: Look at the main parts above.
""".trimIndent() + "\n"

    @Test
    fun wellFormedLesson_parsesEveryBlock() {
        val result = LessonMarkupParser.parse(photosynthesis, "Photosynthesis", LearningLevel.BEGINNER, isFinal = true)
        val lesson = result.lesson

        assertTrue(result.hasMarkup)
        assertNull(result.activeBlock)
        assertEquals("How Plants Make Food", lesson.title)
        assertTrue(lesson.overview.startsWith("Photosynthesis is how"))
        assertEquals(3, lesson.keyPoints.size)
        assertEquals(listOf("Light is absorbed", "Water is split", "CO2 is fixed", "Glucose is made"), lesson.flowSteps)
        assertEquals("Chloroplast", lesson.parts[0].name)
        assertEquals("pores that let CO2 in", lesson.parts[1].role)
        assertEquals(2, lesson.sections.size)
        assertEquals("Making sugar", lesson.sections[1].heading)
        assertTrue(lesson.practicalExample.contains("solar panel"))
        assertEquals("Glucose", lesson.terms[1].term)

        assertEquals(2, lesson.quizzes.size)
        assertEquals(1, lesson.quizzes[0].correctOptionIndex)
        assertEquals("It is the gas we breathe in.", lesson.quizzes[0].hint)
        assertEquals(3, lesson.quizzes[1].options.size)
        assertEquals(2, lesson.quizzes[1].correctOptionIndex)

        assertTrue(OutputValidator.validateStructuredLesson(lesson) is ValidationResult.Success)
    }

    @Test
    fun streaming_blocksAppearProgressivelyAndQuizOnlyWhenComplete() {
        // Cut right after the overview line: title + overview visible, model is writing.
        val afterOverview = photosynthesis.substring(0, photosynthesis.indexOf("KEY:"))
        val early = LessonMarkupParser.parse(afterOverview, "Photosynthesis", LearningLevel.BEGINNER, isFinal = false)
        assertEquals("How Plants Make Food", early.lesson.title)
        assertTrue(early.lesson.overview.isNotBlank())
        assertTrue(early.lesson.keyPoints.isEmpty())
        assertEquals(LessonMarkupParser.Block.OVERVIEW, early.activeBlock)

        // Cut in the middle of the first quiz's options: the quiz must not be interactive yet.
        val midQuiz = photosynthesis.substring(0, photosynthesis.indexOf("C) Nitrogen"))
        val mid = LessonMarkupParser.parse(midQuiz, "Photosynthesis", LearningLevel.BEGINNER, isFinal = false)
        assertTrue(mid.lesson.quizzes.isEmpty())
        assertEquals(1, mid.quizzesStarted)
        assertEquals(LessonMarkupParser.Block.QUIZ, mid.activeBlock)
        assertEquals(4, mid.lesson.flowSteps.size)

        // ANSWER line still being typed (no newline yet) → still not interactive.
        val answerTyping = photosynthesis.substring(0, photosynthesis.indexOf("ANSWER: B") + "ANSWER: B".length)
        assertTrue(LessonMarkupParser.parse(answerTyping, "P", LearningLevel.BEGINNER, isFinal = false).lesson.quizzes.isEmpty())

        // Once the ANSWER line is finished the first quiz becomes available.
        val answered = photosynthesis.substring(0, photosynthesis.indexOf("WHY: Water"))
        assertEquals(1, LessonMarkupParser.parse(answered, "P", LearningLevel.BEGINNER, isFinal = false).lesson.quizzes.size)
    }

    @Test
    fun streaming_partialTagDoesNotLeakIntoPreviousBlock() {
        val raw = "TITLE: Batteries\nOVERVIEW: Batteries store chemical energy.\nKE"
        val lesson = LessonMarkupParser.parse(raw, "Batteries", LearningLevel.BEGINNER, isFinal = false).lesson
        assertEquals("Batteries store chemical energy.", lesson.overview)
    }

    @Test
    fun smallModelDrift_isTolerated() {
        val raw = """
Sure! Here is your lesson.
**TITLE:** Batteries Explained
**OVERVIEW:** A battery turns stored chemical energy into electricity.
KEY:
- It has a positive and a negative end
- A reaction pushes electrons through the circuit
FLOW:
1. Chemicals react
2. Electrons flow out of the negative end
3. Current powers the device
PART: Anode - the negative end that releases electrons
PART: Electrolyte: lets ions move inside
QUIZ 1: What moves through the wire?
a) Protons
b) Electrons
c) Neutrons
ANSWER: (b)
WHY: Electrons carry the current.
""".trimIndent()
        val lesson = LessonMarkupParser.parse(raw, "Batteries", LearningLevel.BEGINNER, isFinal = true).lesson

        assertEquals("Batteries Explained", lesson.title)
        assertFalse("preamble chatter must be dropped", lesson.overview.contains("Sure!"))
        assertEquals(2, lesson.keyPoints.size)
        assertEquals(3, lesson.flowSteps.size)
        assertEquals("Electrons flow out of the negative end", lesson.flowSteps[1])
        assertEquals("Anode", lesson.parts[0].name)
        assertEquals("Electrolyte", lesson.parts[1].name)
        assertEquals(1, lesson.quizzes.single().correctOptionIndex)
    }

    @Test
    fun unletteredBulletOptions_areAccepted() {
        val raw = "TITLE: T\nOVERVIEW: O\nQUIZ: Pick one?\n- Red\n- Green\n- Blue\nANSWER: Green\n"
        val quiz = LessonMarkupParser.parse(raw, "T", LearningLevel.BEGINNER, isFinal = true).lesson.quizzes.single()
        assertEquals(listOf("Red", "Green", "Blue"), quiz.options)
        assertEquals(1, quiz.correctOptionIndex)
    }

    @Test
    fun quizWithUnresolvableAnswer_isDroppedNotGuessed() {
        val raw = "TITLE: T\nOVERVIEW: O\nKEY: k\nQUIZ: Q?\nA) one\nB) two\nANSWER: D\nQUIZ: Q2?\nA) x\nB) y\n"
        val lesson = LessonMarkupParser.parse(raw, "T", LearningLevel.BEGINNER, isFinal = true).lesson
        assertTrue(lesson.quizzes.isEmpty())
    }

    @Test
    fun resolveAnswer_handlesCommonForms() {
        val options = listOf("A chemical reaction", "Magnetism", "Gravity")
        assertEquals(0, LessonMarkupParser.resolveAnswer("A chemical reaction", options))
        assertEquals(1, LessonMarkupParser.resolveAnswer("B", options))
        assertEquals(1, LessonMarkupParser.resolveAnswer("B) Magnetism", options))
        assertEquals(2, LessonMarkupParser.resolveAnswer("Option C", options))
        assertEquals(2, LessonMarkupParser.resolveAnswer("3", options))
        assertEquals(1, LessonMarkupParser.resolveAnswer("**b.**", options))
        assertNull(LessonMarkupParser.resolveAnswer("E", options))
        assertNull(LessonMarkupParser.resolveAnswer("7", options))
        // "A" followed by text that is not option A must not be read as the letter A.
        assertNull(LessonMarkupParser.resolveAnswer("A force we cannot see", options))
    }

    @Test
    fun structuredParser_prefersMarkupAndRoundTripsWithProgress() {
        val lesson = StructuredOutputParser.parseStructuredLesson(photosynthesis, "Photosynthesis", LearningLevel.INTERMEDIATE)!!
        assertEquals(2, lesson.quizzes.size)

        val progress = mapOf(0 to QuizProgress(selectedIndex = 1, submitted = true, attempts = 2))
        val json = StructuredOutputParser.serializeLesson(lesson, progress)
        val restored = StructuredOutputParser.deserializeLesson(json, "Photosynthesis", LearningLevel.INTERMEDIATE)

        assertEquals(lesson, restored)
        assertEquals(progress, StructuredOutputParser.deserializeLessonProgress(json))
    }

    @Test
    fun legacySavedLesson_withSingleQuiz_stillLoads() {
        val legacy = """{"topic":"Gravity","title":"Gravity","overview":"Pull","sections":[{"heading":"Mass","content":"More mass"}],
            "practicalExample":"Apple","quiz":{"question":"Q?","options":["a","b"],"correctOptionIndex":1,"explanation":"e","hint":null}}"""
        val lesson = StructuredOutputParser.deserializeLesson(legacy, "Gravity", LearningLevel.BEGINNER)!!
        assertEquals(1, lesson.quizzes.size)
        assertEquals(1, lesson.quiz!!.correctOptionIndex)
        assertTrue(lesson.keyPoints.isEmpty())
        assertTrue(StructuredOutputParser.deserializeLessonProgress(legacy).isEmpty())
    }

    @Test
    fun jsonOutput_stillParsesViaFallback() {
        val raw = """{"title": "Gravity", "overview": "Pull", "sections": [{"heading": "Mass", "content": "More mass"}],
            "quiz": {"question": "Q?", "options": ["a", "b"], "correctOptionIndex": 1}}"""
        val lesson = StructuredOutputParser.parseStructuredLesson(raw, "Gravity", LearningLevel.BEGINNER)!!
        assertEquals("Gravity", lesson.title)
        assertEquals(1, lesson.quizzes.size)
    }

    @Test
    fun practicePrompt_listsExistingQuestionsAndUsesQuizFormat() {
        val prompt = PromptBuilder.buildLessonPracticePrompt(
            topic = "Photosynthesis",
            lessonTitle = "How Plants Make Food",
            lessonContext = "Plants turn light into sugar.",
            existingQuestions = listOf("What gas do plants release?")
        )
        assertTrue(prompt.contains("- What gas do plants release?"))
        assertTrue(prompt.contains("QUIZ:"))
        assertTrue(prompt.contains("ANSWER:"))
        assertFalse(prompt.contains("{"))
    }
}
