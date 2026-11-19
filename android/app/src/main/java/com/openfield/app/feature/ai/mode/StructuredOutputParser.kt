package com.openfield.app.feature.ai.mode

import com.openfield.app.core.text.Markdown
import com.openfield.app.feature.tasks.TaskPlan

object StructuredOutputParser {

    /**
     * Extracts a JSON block enclosed in markdown code fences or raw curly braces.
     */
    fun extractJsonString(raw: String): String? {
        val trimmed = raw.trim()

        val fenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val fenceMatch = fenceRegex.find(trimmed)
        if (fenceMatch != null) {
            val candidate = fenceMatch.groupValues[1].trim()
            if (candidate.startsWith("{") && candidate.endsWith("}")) {
                return candidate
            }
        }

        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }

        return null
    }

    private fun parseRootObject(raw: String): Map<String, Any?>? {
        val candidate = extractJsonString(raw) ?: raw.substring(raw.indexOf('{').takeIf { it >= 0 } ?: return null)
        return LenientJson.parseObject(candidate)
    }

    // =================================================================
    // Troubleshooting
    // =================================================================

    fun parseDiagnosticAssessment(raw: String): DiagnosticAssessment? {
        parseRootObject(raw)?.let { map -> diagnosticFromMap(map)?.let { return it } }
        return parseDiagnosticFromMarkdown(raw)
    }

    private fun diagnosticFromMap(map: Map<String, Any?>): DiagnosticAssessment? {
        val isResolved = map.bool("isResolutionReached", "resolved", "isResolved") ?: false
        val solution = map.str("recommendedSolution", "solution", "fix", "resolution")
        val cause = map.str("likelyCause", "cause", "probableCause", "diagnosis")
        val question = map.str("question", "nextQuestion", "check", "step")
            ?: if (isResolved) (cause ?: "Diagnosis complete") else return null
        val stepNumber = map.int("currentStepNumber", "stepNumber", "step") ?: 1

        return DiagnosticAssessment(
            category = map.str("category", "type")?.let(Markdown::stripInline) ?: "General Troubleshooting",
            safetyNotice = map.str("safetyNotice", "safety", "warning"),
            currentStepNumber = stepNumber,
            totalEstimatedSteps = maxOf(map.int("totalEstimatedSteps", "totalSteps") ?: 3, stepNumber),
            question = question,
            explanation = map.str("explanation", "why", "purpose", "reason"),
            options = parseOptions(map.list("options", "choices", "answers")),
            isResolutionReached = isResolved,
            likelyCause = cause,
            recommendedSolution = solution,
            isEscalationNeeded = map.bool("isEscalationNeeded", "escalate", "needsProfessional") ?: false,
            escalationReason = map.str("escalationReason", "escalation")
        )
    }

    private val optionPrefix = Regex("^\\(?([A-Da-d]|\\d)[).:\\-]\\s+")

    private fun parseOptions(raw: List<Any?>?): List<DiagnosticOption> {
        if (raw == null) return emptyList()
        val parsed = raw.mapNotNull { item ->
            when (item) {
                is String -> DiagnosticOption(id = "", text = item)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val m = item as Map<String, Any?>
                    val text = m.str("text", "label", "option", "answer", "value") ?: return@mapNotNull null
                    DiagnosticOption(id = m.str("id", "key") ?: "", text = text, detail = m.str("detail", "description"))
                }
                else -> null
            }
        }.map { it.copy(text = it.text.replace(optionPrefix, "").trim()) }
            .filter { it.text.isNotBlank() }
            .distinctBy { it.text.lowercase() }

        val idsUsable = parsed.all { it.id.isNotBlank() } && parsed.map { it.id }.toSet().size == parsed.size
        return parsed.mapIndexed { idx, option ->
            if (idsUsable) option else option.copy(id = ('A'.code + idx).toChar().toString())
        }
    }

    private fun parseDiagnosticFromMarkdown(raw: String): DiagnosticAssessment? {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("```") }
        if (lines.isEmpty()) return null

        val firstQuestion = lines.find { it.endsWith("?") } ?: lines.firstOrNull() ?: return null
        val options = mutableListOf<DiagnosticOption>()

        val optionLineRegex = Regex("^(?:[A-Da-d][\\).:]|[•\\-*]|\\d+[\\).])\\s+.*")
        lines.filter { optionLineRegex.matches(it) }.forEachIndexed { idx, line ->
            val clean = line.replaceFirst(Regex("^(?:[A-Da-d][\\).:]|[•\\-*]|\\d+[\\).])\\s*"), "").trim()
            if (clean.isNotBlank()) {
                options.add(DiagnosticOption(id = ('A'.code + idx).toChar().toString(), text = clean))
            }
        }

        val safetyLine = lines.find { it.contains("safety", ignoreCase = true) || it.contains("warning", ignoreCase = true) }

        return DiagnosticAssessment(
            category = "General Diagnostics",
            safetyNotice = safetyLine,
            currentStepNumber = 1,
            totalEstimatedSteps = 3,
            question = Markdown.stripInline(firstQuestion),
            explanation = null,
            options = if (options.isNotEmpty()) options else listOf(
                DiagnosticOption("A", "Yes, observed"),
                DiagnosticOption("B", "No, does not occur")
            ),
            isResolutionReached = false
        )
    }

    fun serializeDiagnostic(a: DiagnosticAssessment): String = LenientJson.write(
        mapOf(
            "category" to a.category,
            "safetyNotice" to a.safetyNotice,
            "currentStepNumber" to a.currentStepNumber,
            "totalEstimatedSteps" to a.totalEstimatedSteps,
            "question" to a.question,
            "explanation" to a.explanation,
            "options" to a.options.map { mapOf("id" to it.id, "text" to it.text, "detail" to it.detail) },
            "isResolutionReached" to a.isResolutionReached,
            "likelyCause" to a.likelyCause,
            "recommendedSolution" to a.recommendedSolution,
            "isEscalationNeeded" to a.isEscalationNeeded,
            "escalationReason" to a.escalationReason
        )
    )

    fun deserializeDiagnostic(json: String): DiagnosticAssessment? =
        if (json.trimStart().startsWith("{")) LenientJson.parseObject(json)?.let(::diagnosticFromMap) else null

    fun serializeHistory(history: List<DiagnosticStepHistory>): String = LenientJson.write(
        history.map { mapOf("step" to it.stepIndex, "question" to it.question, "answer" to it.selectedOptionText) }
    )

    fun deserializeHistory(json: String): List<DiagnosticStepHistory>? {
        if (!json.trimStart().startsWith("[")) return null
        val list = LenientJson.parse(json) as? List<*> ?: return null
        return list.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val m = item as? Map<String, Any?> ?: return@mapNotNull null
            DiagnosticStepHistory(
                stepIndex = m.int("step") ?: 1,
                question = m.str("question") ?: "",
                selectedOptionText = m.str("answer") ?: ""
            )
        }
    }

    // =================================================================
    // Learn & Explain
    // =================================================================

    fun parseStructuredLesson(raw: String, fallbackTopic: String, level: LearningLevel): StructuredLesson? {
        // Primary path: the line-tagged lesson format the prompt asks for.
        val markup = LessonMarkupParser.parse(raw, fallbackTopic, level, isFinal = true)
        if (markup.hasMarkup && markup.lesson.hasContent) return withDerivedOverview(markup.lesson)

        parseRootObject(raw)?.let { map -> lessonFromMap(map, fallbackTopic, level)?.let { return it } }
        return parseLessonFromMarkdown(raw, fallbackTopic, level)
    }

    /** If the model skipped OVERVIEW, open with its own first concept rather than leaving the lesson headless. */
    private fun withDerivedOverview(lesson: StructuredLesson): StructuredLesson {
        if (lesson.overview.isNotBlank()) return lesson
        val derived = lesson.sections.firstOrNull()?.content
            ?: lesson.keyPoints.takeIf { it.isNotEmpty() }?.joinToString(" ") { it.trimEnd('.') + "." }
            ?: return lesson
        return lesson.copy(overview = derived)
    }

    private fun lessonFromMap(map: Map<String, Any?>, fallbackTopic: String, level: LearningLevel): StructuredLesson? {
        val topic = map.str("topic") ?: fallbackTopic
        val sections = map.list("sections", "concepts", "keyConcepts", "parts")?.mapNotNull { item ->
            when (item) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val m = item as Map<String, Any?>
                    val content = m.str("content", "body", "text", "explanation", "description") ?: return@mapNotNull null
                    LessonSection(Markdown.stripInline(m.str("heading", "title", "name", "concept") ?: "Key idea"), content)
                }
                is String -> item.takeIf { it.isNotBlank() }?.let { LessonSection("Key idea", it) }
                else -> null
            }
        }.orEmpty()

        val overview = map.str("overview", "summary", "introduction", "intro")
            ?: sections.firstOrNull()?.content
            ?: return null

        val quizzes = buildList {
            map.list("quizzes", "questions", "knowledgeChecks")?.forEach { item ->
                @Suppress("UNCHECKED_CAST")
                (item as? Map<String, Any?>)?.let(::quizFromMap)?.let(::add)
            }
            if (isEmpty()) map.obj("quiz", "question", "knowledgeCheck")?.let(::quizFromMap)?.let(::add)
        }

        return StructuredLesson(
            topic = topic,
            level = level,
            title = Markdown.stripInline(map.str("title", "lessonTitle") ?: "$topic Explained"),
            overview = overview,
            sections = sections.ifEmpty { listOf(LessonSection("Key idea", overview)) },
            practicalExample = map.str("practicalExample", "example", "analogy", "realWorldExample") ?: "",
            quizzes = quizzes,
            keyPoints = map.list("keyPoints")?.mapNotNull { (it as? String)?.takeIf(String::isNotBlank) }.orEmpty(),
            flowSteps = map.list("flowSteps")?.mapNotNull { (it as? String)?.takeIf(String::isNotBlank) }.orEmpty(),
            parts = map.list("lessonParts")?.mapNotNull { item ->
                @Suppress("UNCHECKED_CAST")
                val m = item as? Map<String, Any?> ?: return@mapNotNull null
                LessonPart(m.str("name") ?: return@mapNotNull null, m.str("role") ?: "")
            }.orEmpty(),
            terms = map.list("terms")?.mapNotNull { item ->
                @Suppress("UNCHECKED_CAST")
                val m = item as? Map<String, Any?> ?: return@mapNotNull null
                LessonTerm(m.str("term") ?: return@mapNotNull null, m.str("meaning") ?: "")
            }.orEmpty()
        )
    }

    private fun quizFromMap(m: Map<String, Any?>): LessonQuiz? {
        val question = m.str("question", "prompt") ?: return null
        val options = m.list("options", "choices", "answers")?.mapNotNull { item ->
            when (item) {
                is String -> item
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    (item as Map<String, Any?>).str("text", "option", "label")
                }
                null -> null
                else -> item.toString()
            }
        }?.map { it.replace(optionPrefix, "").trim() }?.filter { it.isNotBlank() }.orEmpty()
        if (options.size < 2) return null

        val rawAnswer = m.entries.firstOrNull { (k, _) ->
            k.lowercase() in setOf("correctoptionindex", "correctindex", "answerindex", "correct", "answer", "correctanswer", "correctoption")
        }?.value
        val index = when (rawAnswer) {
            is Number -> rawAnswer.toInt()
            is String -> {
                val t = rawAnswer.trim()
                t.toIntOrNull()
                    ?: if (t.length == 1 && t[0].uppercaseChar() in 'A'..'H') t[0].uppercaseChar() - 'A' else null
                    ?: options.indexOfFirst { it.equals(t.replace(optionPrefix, ""), ignoreCase = true) }.takeIf { it >= 0 }
                    ?: 0
            }
            else -> 0
        }

        return LessonQuiz(
            question = question,
            options = options,
            correctOptionIndex = index.coerceIn(0, options.size - 1),
            explanation = m.str("explanation", "reason", "why") ?: "",
            hint = m.str("hint", "clue")
        )
    }

    private fun parseLessonFromMarkdown(raw: String, topic: String, level: LearningLevel): StructuredLesson? {
        val cleaned = raw.replace(Regex("```\\w*"), "").trim()
        if (cleaned.isBlank()) return null
        val lines = cleaned.lines()

        val title = lines.firstOrNull { it.trim().startsWith("# ") }?.trim()?.removePrefix("# ")?.let(Markdown::stripInline)
            ?: "$topic Explained"

        val sections = mutableListOf<LessonSection>()
        val intro = StringBuilder()
        var heading: String? = null
        val body = StringBuilder()

        fun flush() {
            val h = heading
            if (h != null && body.isNotBlank()) sections.add(LessonSection(h, body.toString().trim()))
            body.clear()
        }

        for (line in lines) {
            val t = line.trim()
            when {
                t.startsWith("# ") -> Unit
                t.startsWith("## ") || t.startsWith("### ") -> {
                    flush()
                    heading = Markdown.stripInline(t.trimStart('#'))
                }
                heading == null -> intro.append(line).append('\n')
                else -> body.append(line).append('\n')
            }
        }
        flush()

        val overview = intro.toString().trim().ifBlank { sections.firstOrNull()?.content ?: cleaned.take(600) }
        val exampleSection = sections.firstOrNull { it.heading.contains("example", true) || it.heading.contains("analogy", true) }
        val conceptSections = (sections - setOfNotNull(exampleSection)).ifEmpty {
            listOf(LessonSection("Key ideas", cleaned.take(1200)))
        }

        return StructuredLesson(
            topic = topic,
            level = level,
            title = title,
            overview = overview,
            sections = conceptSections,
            practicalExample = exampleSection?.content ?: "",
            quizzes = emptyList()
        )
    }

    private fun quizToMap(q: LessonQuiz): Map<String, Any?> = mapOf(
        "question" to q.question,
        "options" to q.options,
        "correctOptionIndex" to q.correctOptionIndex,
        "explanation" to q.explanation,
        "hint" to q.hint
    )

    /**
     * Serialises a lesson (and optionally the learner's per-question progress) for the `lessonJson` column.
     * Saved lessons from earlier versions used a single "quiz" object and still deserialise.
     */
    fun serializeLesson(lesson: StructuredLesson, progress: Map<Int, QuizProgress> = emptyMap()): String = LenientJson.write(
        mapOf(
            "topic" to lesson.topic,
            "title" to lesson.title,
            "overview" to lesson.overview,
            "sections" to lesson.sections.map { mapOf("heading" to it.heading, "content" to it.content) },
            "practicalExample" to lesson.practicalExample,
            "quizzes" to lesson.quizzes.map(::quizToMap),
            "keyPoints" to lesson.keyPoints,
            "flowSteps" to lesson.flowSteps,
            "lessonParts" to lesson.parts.map { mapOf("name" to it.name, "role" to it.role) },
            "terms" to lesson.terms.map { mapOf("term" to it.term, "meaning" to it.meaning) },
            "progress" to progress.entries.sortedBy { it.key }.map { (index, p) ->
                mapOf("index" to index, "selected" to p.selectedIndex, "submitted" to p.submitted, "attempts" to p.attempts)
            }
        )
    )

    fun deserializeLesson(json: String, fallbackTopic: String, level: LearningLevel): StructuredLesson? =
        if (json.trimStart().startsWith("{")) LenientJson.parseObject(json)?.let { lessonFromMap(it, fallbackTopic, level) } else null

    fun deserializeLessonProgress(json: String): Map<Int, QuizProgress> {
        if (!json.trimStart().startsWith("{")) return emptyMap()
        val list = LenientJson.parseObject(json)?.list("progress") ?: return emptyMap()
        return list.mapNotNull { item ->
            @Suppress("UNCHECKED_CAST")
            val m = item as? Map<String, Any?> ?: return@mapNotNull null
            val index = m.int("index") ?: return@mapNotNull null
            index to QuizProgress(
                selectedIndex = m.int("selected"),
                submitted = m.bool("submitted") ?: false,
                attempts = m.int("attempts") ?: 0
            )
        }.toMap()
    }

    // =================================================================
    // Image analysis
    // =================================================================

    private enum class ImageSection(vararg val aliases: String) {
        SEE("what i see", "what is visible", "visible content", "description", "overview", "observation"),
        DETAILS("important details", "key details", "notable details", "key features", "details", "features"),
        EXPLANATION("explanation", "context", "background", "interpretation"),
        IDENTIFICATION("possible identification", "identification", "identity", "likely identity"),
        NEXT_STEPS("suggested next steps", "next steps", "recommendations", "recommended actions", "what to do"),
        UNCERTAINTY("uncertainty & limitations", "uncertainty and limitations", "uncertainty", "limitations", "caveats")
    }

    private val headingDecoration = Regex("^(?:#{1,6}\\s*)?(?:\\*\\*|__)?\\s*(?:\\d+[.)]\\s*)?")

    /** Returns the section a line opens plus any text that follows the heading on the same line. */
    private fun matchImageHeading(line: String): Pair<ImageSection, String>? {
        val t = line.trim()
        if (t.isEmpty()) return null
        val looksLikeHeading = t.startsWith("#") || t.startsWith("**") || t.startsWith("__") ||
            Regex("^\\d+[.)]\\s+[A-Z]").containsMatchIn(t)
        if (!looksLikeHeading) return null

        val body = t.replace(headingDecoration, "")
        val lower = body.lowercase()
        for (section in ImageSection.entries) {
            for (alias in section.aliases) {
                if (!lower.startsWith(alias)) continue
                val after = body.substring(alias.length)
                val rest = after.trimStart('*', '_', ' ', ':', '-', '—').trim().trimEnd('*', '_').trim()
                val cleanAfter = after.trim().trim('*', '_', ':', ' ')
                if (cleanAfter.isEmpty() || after.trimStart().startsWith(":") || after.trimStart().startsWith("**")) {
                    return section to rest
                }
            }
        }
        return null
    }

    fun parseImageSections(raw: String): ImageAnalysisSections {
        val content = mutableMapOf<ImageSection, StringBuilder>()
        var current: ImageSection? = null

        for (line in raw.lines()) {
            val heading = matchImageHeading(line)
            if (heading != null) {
                current = heading.first
                val sb = content.getOrPut(heading.first) { StringBuilder() }
                if (heading.second.isNotBlank()) sb.append(heading.second).append('\n')
                continue
            }
            current?.let { content.getOrPut(it) { StringBuilder() }.append(line).append('\n') }
        }

        fun text(section: ImageSection) = content[section]?.toString()?.trim().orEmpty()
        fun items(section: ImageSection): List<String> {
            val lines = text(section).lines().map { it.trim() }.filter { it.isNotBlank() }
            val listMarker = Regex("^(?:[-*+•]|\\d+[.)])\\s+")
            val marked = lines.filter { listMarker.containsMatchIn(it) }
            return (marked.ifEmpty { lines }).map { it.replace(listMarker, "").trim() }.filter { it.isNotBlank() }
        }

        val hasStructure = content.isNotEmpty()
        val whatISee = if (hasStructure) text(ImageSection.SEE) else raw.trim()
        val identification = text(ImageSection.IDENTIFICATION).takeIf { it.isNotBlank() }

        val subjectSource = identification?.lines()?.firstOrNull { it.isNotBlank() }
            ?: whatISee.lines().firstOrNull { it.isNotBlank() }
            ?: ""
        val subject = Markdown.stripInline(subjectSource.replace(Regex("^(?:[-*+•]|\\d+[.)])\\s+"), ""))
            .let { s -> s.split(Regex("(?<=[.!?])\\s")).first().trimEnd('.') }
            .let { if (it.length > 60) it.take(57).trimEnd() + "…" else it }
            .ifBlank { "Image analysis" }

        return ImageAnalysisSections(
            subject = subject,
            whatISee = whatISee,
            importantDetails = items(ImageSection.DETAILS),
            explanation = text(ImageSection.EXPLANATION),
            possibleIdentification = identification,
            suggestedNextSteps = items(ImageSection.NEXT_STEPS),
            uncertaintyAndLimitations = text(ImageSection.UNCERTAINTY).takeIf { it.isNotBlank() }
        )
    }

    // =================================================================
    // Task Planning (Phase 3 Gate 6)
    // =================================================================

    fun parseTaskPlan(raw: String): TaskPlan? {
        val map = parseRootObject(raw) ?: return null
        val title = map.str("title", "taskTitle", "name") ?: return null
        val description = map.str("description", "summary", "overview") ?: ""
        val priority = map.str("priority")?.uppercase() ?: "MEDIUM"
        val category = map.str("suggestedCategory", "category") ?: "General"
        val rawSubtasks = map["subtasks"] as? List<*> ?: emptyList<Any>()
        val subtasks = rawSubtasks.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }

        return TaskPlan(
            title = title,
            description = description,
            priority = if (priority in listOf("HIGH", "MEDIUM", "LOW")) priority else "MEDIUM",
            subtasks = subtasks,
            suggestedCategory = category
        )
    }
}
