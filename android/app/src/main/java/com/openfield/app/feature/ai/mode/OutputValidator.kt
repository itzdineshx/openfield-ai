package com.openfield.app.feature.ai.mode

object OutputValidator {

    private val HAZARD_KEYWORDS = setOf(
        "voltage", "electric", "wiring", "shock", "battery",
        "flammable", "gas", "fuel", "leak", "pressure", "brake",
        "chemical", "acid", "fire", "combustion"
    )

    fun validateDiagnosticAssessment(
        assessment: DiagnosticAssessment?,
        rawText: String = "",
        originalProblem: String = ""
    ): ValidationResult<DiagnosticAssessment> {
        if (assessment == null) {
            return ValidationResult.Failure("Failed to parse diagnostic assessment structure.", rawText)
        }

        if (assessment.question.isBlank()) {
            return ValidationResult.Failure("Diagnostic question is missing or blank.", rawText)
        }

        if (!assessment.isResolutionReached && assessment.options.size < 2) {
            return ValidationResult.Failure(
                "Diagnostic step must have at least 2 selectable observation options.",
                rawText
            )
        }

        if (assessment.isResolutionReached && assessment.recommendedSolution.isNullOrBlank()) {
            return ValidationResult.Failure(
                "Final diagnosis reached but no recommended solution was provided.",
                rawText
            )
        }

        // Safety verification: If dangerous elements are mentioned in the problem or text,
        // ensure a safety warning is populated.
        val combinedText = "$originalProblem $rawText ${assessment.question}".lowercase()
        val hasHazard = HAZARD_KEYWORDS.any { combinedText.contains(it) }
        val adjustedAssessment = if (hasHazard && assessment.safetyNotice.isNullOrBlank()) {
            assessment.copy(
                safetyNotice = "CAUTION: Ensure power is disconnected and proper safety gear is used before inspecting hardware or electrical components."
            )
        } else {
            assessment
        }

        return ValidationResult.Success(adjustedAssessment)
    }

    fun validateStructuredLesson(
        lesson: StructuredLesson?,
        rawText: String = ""
    ): ValidationResult<StructuredLesson> {
        if (lesson == null) {
            return ValidationResult.Failure("Failed to parse structured lesson content.", rawText)
        }

        if (lesson.title.isBlank()) {
            return ValidationResult.Failure("Lesson title is blank.", rawText)
        }

        if (lesson.overview.isBlank()) {
            return ValidationResult.Failure("Lesson overview is blank.", rawText)
        }

        if (lesson.sections.isEmpty() && lesson.keyPoints.isEmpty() && lesson.flowSteps.isEmpty()) {
            return ValidationResult.Failure("Lesson must contain at least one conceptual section.", rawText)
        }

        for (quiz in lesson.quizzes) {
            validateQuiz(quiz)?.let { return ValidationResult.Failure(it, rawText) }
        }

        return ValidationResult.Success(lesson)
    }

    /** Returns a reason when a knowledge check cannot be rendered as an interactive question, else null. */
    fun validateQuiz(quiz: LessonQuiz): String? {
        if (quiz.question.isBlank()) return "Quiz question cannot be blank."
        if (quiz.options.size < 2) return "Quiz must provide at least 2 answer choices."
        if (quiz.options.any { it.isBlank() }) return "Quiz options cannot be blank."
        if (quiz.correctOptionIndex !in quiz.options.indices) {
            return "Quiz correctOptionIndex (${quiz.correctOptionIndex}) is out of bounds for options of size ${quiz.options.size}."
        }
        return null
    }

    fun validateWritingDraft(draft: WritingDraftResult): ValidationResult<WritingDraftResult> {
        if (draft.content.isBlank()) {
            return ValidationResult.Failure("Generated writing content is empty.")
        }
        if (draft.wordCount <= 0) {
            return ValidationResult.Failure("Word count must be greater than zero.")
        }
        return ValidationResult.Success(draft)
    }

    fun validateImageSections(sections: ImageAnalysisSections): ValidationResult<ImageAnalysisSections> {
        if (sections.whatISee.isBlank() && sections.subject.isBlank()) {
            return ValidationResult.Failure("Image analysis produced empty descriptions.")
        }
        return ValidationResult.Success(sections)
    }
}
