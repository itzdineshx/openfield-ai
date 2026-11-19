package com.openfield.app.feature.ai.mode

object StructuredOutputParser {

    /**
     * Extracts a JSON block enclosed in markdown code fences or raw curly braces.
     */
    fun extractJsonString(raw: String): String? {
        val trimmed = raw.trim()

        // 1. Check for ```json ... ``` or ``` ... ```
        val fenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val fenceMatch = fenceRegex.find(trimmed)
        if (fenceMatch != null) {
            val candidate = fenceMatch.groupValues[1].trim()
            if (candidate.startsWith("{") && candidate.endsWith("}")) {
                return candidate
            }
        }

        // 2. Look for outermost matching curly braces
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }

        return null
    }

    /**
     * Parses a DiagnosticAssessment from raw model output.
     */
    fun parseDiagnosticAssessment(raw: String): DiagnosticAssessment? {
        val jsonStr = extractJsonString(raw)
        if (jsonStr != null) {
            val parsed = parseDiagnosticJson(jsonStr)
            if (parsed != null) return parsed
        }

        // Fallback: Parse markdown lines if JSON is unavailable or malformed
        return parseDiagnosticFromMarkdown(raw)
    }

    private fun parseDiagnosticJson(json: String): DiagnosticAssessment? {
        try {
            val category = extractStringField(json, "category") ?: "General Troubleshooting"
            val safety = extractStringField(json, "safetyNotice")
            val stepNumber = extractIntField(json, "currentStepNumber") ?: 1
            val totalSteps = extractIntField(json, "totalEstimatedSteps") ?: 3
            val question = extractStringField(json, "question") ?: return null
            val explanation = extractStringField(json, "explanation")
            val isResolved = extractBooleanField(json, "isResolutionReached") ?: false
            val likelyCause = extractStringField(json, "likelyCause")
            val solution = extractStringField(json, "recommendedSolution")
            val isEscalation = extractBooleanField(json, "isEscalationNeeded") ?: false
            val escalationReason = extractStringField(json, "escalationReason")

            val options = extractOptionsArray(json)

            return DiagnosticAssessment(
                category = category,
                safetyNotice = safety.takeIf { !it.equals("null", ignoreCase = true) && !it.isNullOrBlank() },
                currentStepNumber = stepNumber,
                totalEstimatedSteps = maxOf(totalSteps, stepNumber),
                question = question,
                explanation = explanation,
                options = options,
                isResolutionReached = isResolved,
                likelyCause = likelyCause.takeIf { !it.equals("null", ignoreCase = true) && !it.isNullOrBlank() },
                recommendedSolution = solution.takeIf { !it.equals("null", ignoreCase = true) && !it.isNullOrBlank() },
                isEscalationNeeded = isEscalation,
                escalationReason = escalationReason.takeIf { !it.equals("null", ignoreCase = true) && !it.isNullOrBlank() }
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseDiagnosticFromMarkdown(raw: String): DiagnosticAssessment? {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        val firstQuestion = lines.find { it.endsWith("?") } ?: lines.firstOrNull() ?: return null
        val options = mutableListOf<DiagnosticOption>()

        val optionLines = lines.filter { line ->
            line.matches(Regex("^(?:[A-Da-d][\\).:]|[•\\-*]|\\d+[\\).])\\s+.*"))
        }

        optionLines.forEachIndexed { idx, line ->
            val clean = line.replaceFirst(Regex("^(?:[A-Da-d][\\).:]|[•\\-*]|\\d+[\\).])\\s*"), "").trim()
            if (clean.isNotBlank()) {
                val letter = ('A'.code + idx).toChar().toString()
                options.add(DiagnosticOption(id = letter, text = clean))
            }
        }

        val safetyLine = lines.find { it.contains("safety", ignoreCase = true) || it.contains("warning", ignoreCase = true) }

        return DiagnosticAssessment(
            category = "General Diagnostics",
            safetyNotice = safetyLine,
            currentStepNumber = 1,
            totalEstimatedSteps = 3,
            question = firstQuestion,
            explanation = null,
            options = if (options.isNotEmpty()) options else listOf(
                DiagnosticOption("A", "Yes, observed"),
                DiagnosticOption("B", "No, does not occur")
            ),
            isResolutionReached = false
        )
    }

    /**
     * Parses a StructuredLesson from raw model output.
     */
    fun parseStructuredLesson(raw: String, fallbackTopic: String, level: LearningLevel): StructuredLesson? {
        val jsonStr = extractJsonString(raw)
        if (jsonStr != null) {
            val parsed = parseLessonJson(jsonStr, fallbackTopic, level)
            if (parsed != null) return parsed
        }

        return parseLessonFromMarkdown(raw, fallbackTopic, level)
    }

    private fun parseLessonJson(json: String, fallbackTopic: String, level: LearningLevel): StructuredLesson? {
        try {
            val topic = extractStringField(json, "topic") ?: fallbackTopic
            val title = extractStringField(json, "title") ?: "$topic Explained"
            val overview = extractStringField(json, "overview") ?: return null
            val example = extractStringField(json, "practicalExample") ?: ""

            val sections = extractSectionsArray(json)
            val quiz = extractQuizObject(json)

            return StructuredLesson(
                topic = topic,
                level = level,
                title = title,
                overview = overview,
                sections = sections,
                practicalExample = example,
                quiz = quiz
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseLessonFromMarkdown(raw: String, topic: String, level: LearningLevel): StructuredLesson? {
        val lines = raw.lines().map { it.trim() }
        val nonBlank = lines.filter { it.isNotBlank() }
        if (nonBlank.isEmpty()) return null

        val title = nonBlank.firstOrNull { it.startsWith("#") }?.removePrefix("#")?.trim()
            ?: "$topic Explained"

        val overview = nonBlank.firstOrNull { !it.startsWith("#") && it.length > 20 }
            ?: "Overview of $topic."

        val sections = mutableListOf<LessonSection>()
        var currentHeading: String? = null
        val currentContent = StringBuilder()

        for (line in lines) {
            if (line.startsWith("## ") || line.startsWith("### ")) {
                if (currentHeading != null && currentContent.isNotBlank()) {
                    sections.add(LessonSection(currentHeading, currentContent.toString().trim()))
                    currentContent.clear()
                }
                currentHeading = line.removePrefix("### ").removePrefix("## ").trim()
            } else if (currentHeading != null) {
                currentContent.append(line).append("\n")
            }
        }
        if (currentHeading != null && currentContent.isNotBlank()) {
            sections.add(LessonSection(currentHeading, currentContent.toString().trim()))
        }

        if (sections.isEmpty()) {
            sections.add(LessonSection("Key Concepts", raw.take(600)))
        }

        return StructuredLesson(
            topic = topic,
            level = level,
            title = title,
            overview = overview,
            sections = sections,
            practicalExample = "Consider how $topic functions in everyday systems.",
            quiz = null
        )
    }

    /**
     * Parses ImageAnalysisSections from structured markdown text.
     */
    fun parseImageSections(raw: String): ImageAnalysisSections {
        fun extractSection(title: String): String {
            val lines = raw.lines()
            val startIdx = lines.indexOfFirst { it.contains(title, ignoreCase = true) }
            if (startIdx == -1) return ""

            val content = StringBuilder()
            for (i in (startIdx + 1) until lines.size) {
                val line = lines[i].trim()
                if (line.startsWith("#") || line.matches(Regex("^\\d+\\.\\s+[A-Z].*"))) {
                    break
                }
                content.append(lines[i]).append("\n")
            }
            return content.toString().trim()
        }

        val whatISee = extractSection("What I See").ifBlank { raw.lines().take(4).joinToString("\n").trim() }
        val details = extractSection("Important Details")
        val explanation = extractSection("Explanation")
        val identification = extractSection("Possible Identification").takeIf { it.isNotBlank() }
        val nextSteps = extractSection("Suggested Next Steps")
        val uncertainty = extractSection("Uncertainty").takeIf { it.isNotBlank() }

        val detailList = details.lines()
            .map { it.trim().trimStart('-', '•', '*', ' ') }
            .filter { it.isNotBlank() }

        val nextStepsList = nextSteps.lines()
            .map { it.trim().trimStart('-', '•', '*', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.') }
            .filter { it.isNotBlank() }

        val subject = identification?.lines()?.firstOrNull()?.trim()
            ?: whatISee.lines().firstOrNull()?.take(40)
            ?: "Visual Analysis"

        return ImageAnalysisSections(
            subject = subject,
            whatISee = whatISee,
            importantDetails = detailList,
            explanation = explanation,
            possibleIdentification = identification,
            suggestedNextSteps = nextStepsList,
            uncertaintyAndLimitations = uncertainty
        )
    }

    // =================================================================
    // Helper Extractors for Lightweight JSON parsing
    // =================================================================

    private fun extractStringField(json: String, key: String): String? {
        val regex = Regex("\"$key\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(json) ?: return null
        return unescapeJsonString(match.groupValues[1])
    }

    private fun unescapeJsonString(str: String): String {
        return str
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
    }

    private fun extractIntField(json: String, key: String): Int? {
        val regex = Regex("\"$key\"\\s*:\\s*(\\d+)")
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractBooleanField(json: String, key: String): Boolean? {
        val regex = Regex("\"$key\"\\s*:\\s*(true|false)", RegexOption.IGNORE_CASE)
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toBooleanStrictOrNull()
    }

    private fun extractOptionsArray(json: String): List<DiagnosticOption> {
        val options = mutableListOf<DiagnosticOption>()
        val blockRegex = Regex("\"options\"\\s*:\\s*\\[([\\s\\S]*?)\\]")
        val blockMatch = blockRegex.find(json) ?: return emptyList()
        val arrayContent = blockMatch.groupValues[1]

        val objRegex = Regex("\\{([^}]*)\\}")
        objRegex.findAll(arrayContent).forEach { match ->
            val obj = match.groupValues[1]
            val id = extractStringField(obj, "id") ?: (('A'.code + options.size).toChar().toString())
            val text = extractStringField(obj, "text") ?: ""
            val detail = extractStringField(obj, "detail")
            if (text.isNotBlank()) {
                options.add(DiagnosticOption(id, text, detail))
            }
        }
        return options
    }

    private fun extractSectionsArray(json: String): List<LessonSection> {
        val sections = mutableListOf<LessonSection>()
        val blockRegex = Regex("\"sections\"\\s*:\\s*\\[([\\s\\S]*?)\\]")
        val blockMatch = blockRegex.find(json) ?: return emptyList()
        val arrayContent = blockMatch.groupValues[1]

        val objRegex = Regex("\\{([^}]*)\\}")
        objRegex.findAll(arrayContent).forEach { match ->
            val obj = match.groupValues[1]
            val heading = extractStringField(obj, "heading") ?: "Concept"
            val content = extractStringField(obj, "content") ?: ""
            if (content.isNotBlank()) {
                sections.add(LessonSection(heading, content))
            }
        }
        return sections
    }

    private fun extractQuizObject(json: String): LessonQuiz? {
        val blockRegex = Regex("\"quiz\"\\s*:\\s*\\{([\\s\\S]*?)\\}")
        val match = blockRegex.find(json) ?: return null
        val quizContent = match.groupValues[1]

        val question = extractStringField(quizContent, "question") ?: return null
        val correctIndex = extractIntField(quizContent, "correctOptionIndex") ?: 0
        val explanation = extractStringField(quizContent, "explanation") ?: ""
        val hint = extractStringField(quizContent, "hint")

        val options = mutableListOf<String>()
        val optionsRegex = Regex("\"options\"\\s*:\\s*\\[([\\s\\S]*?)\\]")
        val optionsMatch = optionsRegex.find(quizContent)
        if (optionsMatch != null) {
            val arrayStr = optionsMatch.groupValues[1]
            val itemRegex = Regex("\"((?:\\\\\"|[^\"])*)\"", RegexOption.DOT_MATCHES_ALL)
            itemRegex.findAll(arrayStr).forEach { itemMatch ->
                val str = unescapeJsonString(itemMatch.groupValues[1].trim())
                if (str.isNotBlank()) options.add(str)
            }
        }

        if (options.isEmpty()) return null

        return LessonQuiz(
            question = question,
            options = options,
            correctOptionIndex = correctIndex.coerceIn(0, options.size - 1),
            explanation = explanation,
            hint = hint
        )
    }
}
