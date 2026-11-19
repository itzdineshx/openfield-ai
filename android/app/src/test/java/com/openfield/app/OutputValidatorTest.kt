package com.openfield.app

import com.openfield.app.feature.ai.mode.DiagnosticAssessment
import com.openfield.app.feature.ai.mode.DiagnosticOption
import com.openfield.app.feature.ai.mode.LearningLevel
import com.openfield.app.feature.ai.mode.LessonQuiz
import com.openfield.app.feature.ai.mode.LessonSection
import com.openfield.app.feature.ai.mode.OutputValidator
import com.openfield.app.feature.ai.mode.StructuredLesson
import com.openfield.app.feature.ai.mode.ValidationResult
import com.openfield.app.feature.ai.mode.WritingDraftResult
import com.openfield.app.feature.ai.mode.WritingIntent
import com.openfield.app.feature.ai.mode.WritingTone
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutputValidatorTest {

    @Test
    fun validateDiagnostic_success() {
        val assessment = DiagnosticAssessment(
            category = "Plumbing",
            question = "Is water dripping under the sink?",
            options = listOf(
                DiagnosticOption("A", "Yes, constantly"),
                DiagnosticOption("B", "Only when faucet runs")
            )
        )
        val result = OutputValidator.validateDiagnosticAssessment(assessment)
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun validateDiagnostic_failsOnEmptyOptionsWhenUnresolved() {
        val assessment = DiagnosticAssessment(
            category = "Plumbing",
            question = "Is water dripping under the sink?",
            options = emptyList(),
            isResolutionReached = false
        )
        val result = OutputValidator.validateDiagnosticAssessment(assessment)
        assertTrue(result is ValidationResult.Failure)
    }

    @Test
    fun validateDiagnostic_injectsSafetyWarningForHazards() {
        val assessment = DiagnosticAssessment(
            category = "Electrical",
            question = "Do you smell burning near the main breaker box?",
            options = listOf(
                DiagnosticOption("A", "Yes"),
                DiagnosticOption("B", "No")
            ),
            safetyNotice = null
        )
        val result = OutputValidator.validateDiagnosticAssessment(
            assessment = assessment,
            rawText = "high voltage breaker sparking",
            originalProblem = "Main power box sparking with burning smell"
        )
        assertTrue(result is ValidationResult.Success)
        val validated = (result as ValidationResult.Success).data
        assertNotNull(validated.safetyNotice)
        assertTrue(validated.safetyNotice!!.contains("CAUTION"))
    }

    @Test
    fun validateDiagnostic_failsWhenResolvedWithoutSolution() {
        val assessment = DiagnosticAssessment(
            category = "Hardware",
            question = "Diagnosis Complete",
            options = emptyList(),
            isResolutionReached = true,
            likelyCause = "Blown fuse",
            recommendedSolution = null
        )
        val result = OutputValidator.validateDiagnosticAssessment(assessment)
        assertTrue(result is ValidationResult.Failure)
    }

    @Test
    fun validateLesson_success() {
        val lesson = StructuredLesson(
            topic = "Binary Search",
            level = LearningLevel.INTERMEDIATE,
            title = "Binary Search Algorithm",
            overview = "Divide-and-conquer search in sorted arrays with O(log n) time complexity.",
            sections = listOf(
                LessonSection("Prerequisites", "Array must be sorted.")
            ),
            practicalExample = "Looking up a name in a physical phone book.",
            quiz = LessonQuiz(
                question = "What is the time complexity of Binary Search?",
                options = listOf("O(log n)", "O(n)", "O(n^2)"),
                correctOptionIndex = 0,
                explanation = "Halving the search space each step produces logarithmic time."
            )
        )
        val result = OutputValidator.validateStructuredLesson(lesson)
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun validateLesson_failsOnOutOfBoundsQuizIndex() {
        val lesson = StructuredLesson(
            topic = "Binary Search",
            level = LearningLevel.INTERMEDIATE,
            title = "Binary Search",
            overview = "Overview text",
            sections = listOf(LessonSection("Section 1", "Content")),
            practicalExample = "Example",
            quiz = LessonQuiz(
                question = "Test Question?",
                options = listOf("Option A", "Option B"),
                correctOptionIndex = 5, // Out of bounds
                explanation = "Explanation"
            )
        )
        val result = OutputValidator.validateStructuredLesson(lesson)
        assertTrue(result is ValidationResult.Failure)
    }

    @Test
    fun validateWritingDraft_validatesContentAndWordCount() {
        val validDraft = WritingDraftResult(
            intent = WritingIntent.EMAIL,
            tone = WritingTone.PROFESSIONAL,
            title = "Test Email",
            content = "Dear Team, please find attached the weekly progress report.",
            wordCount = 9
        )
        assertTrue(OutputValidator.validateWritingDraft(validDraft) is ValidationResult.Success)

        val emptyDraft = WritingDraftResult(
            intent = WritingIntent.EMAIL,
            tone = WritingTone.PROFESSIONAL,
            title = "Empty",
            content = "",
            wordCount = 0
        )
        assertTrue(OutputValidator.validateWritingDraft(emptyDraft) is ValidationResult.Failure)
    }
}
