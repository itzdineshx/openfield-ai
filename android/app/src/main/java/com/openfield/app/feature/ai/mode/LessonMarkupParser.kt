package com.openfield.app.feature.ai.mode

import com.openfield.app.core.text.Markdown

/**
 * Parser for the line-tagged lesson format requested by [PromptBuilder.buildLearningLessonPrompt].
 *
 * The format is designed for a small on-device model and for streaming: each block starts with a
 * `TAG:` line, so the lesson can be re-parsed from the partial buffer after every few tokens and each
 * completed block rendered immediately. The parser is tolerant of the common ways small models drift
 * (bold tags, bullets before tags, list items on separate lines, numbered answers).
 *
 * Interactive content is only emitted once it is complete: a quiz is included only when it has at least
 * two options and a resolvable answer that arrived on a finished line.
 */
object LessonMarkupParser {

    enum class Block(val writingLabel: String) {
        TITLE("Writing the title"),
        OVERVIEW("Writing the overview"),
        KEY("Writing the key ideas"),
        FLOW("Drawing how it works"),
        PART("Mapping the main parts"),
        SECTION("Explaining the concepts"),
        EXAMPLE("Finding an everyday example"),
        TERM("Defining key terms"),
        QUIZ("Writing a knowledge check")
    }

    data class Result(
        val lesson: StructuredLesson,
        /** Block the model is currently writing (null once the stream is final). */
        val activeBlock: Block?,
        /** True when at least one recognised lesson tag was found. */
        val hasMarkup: Boolean,
        /** Number of quiz blocks started, including ones not yet complete. */
        val quizzesStarted: Int
    )

    private enum class QuizField { QUESTION, WHY, HINT }

    private val tagLine = Regex("""^[\s>*#_\-•]*([A-Za-z][A-Za-z ]{0,18}?)\s*\d{0,2}\s*[*_]*\s*:\s*[*_]*\s*(.*)$""")
    private val partialTag = Regex("""^[\s>*#_\-•]*[A-Z]{1,14}$""")
    private val optionLine = Regex("""^[\s*\-•]*\(?([A-Ha-h])\s*[).:]\s*(.+)$""")
    private val listMarker = Regex("""^\s*(?:[-*+•]|\d+[.)])\s+""")
    private val flowSeparator = Regex("""\s*(?:->|→|=>|⟶|\|)\s*|\s+>\s+""")

    private val tagAliases: Map<String, String> = mapOf(
        "TITLE" to "TITLE", "LESSONTITLE" to "TITLE",
        "OVERVIEW" to "OVERVIEW", "INTRO" to "OVERVIEW", "INTRODUCTION" to "OVERVIEW", "SUMMARY" to "OVERVIEW",
        "KEY" to "KEY", "KEYPOINT" to "KEY", "KEYIDEA" to "KEY", "KEYFACT" to "KEY", "POINT" to "KEY", "INSHORT" to "KEY",
        "FLOW" to "FLOW", "PROCESS" to "FLOW", "HOWITWORKS" to "FLOW", "STEPS" to "FLOW",
        "PART" to "PART", "COMPONENT" to "PART", "PARTS" to "PART",
        "SECTION" to "SECTION", "CONCEPT" to "SECTION",
        "EXAMPLE" to "EXAMPLE", "ANALOGY" to "EXAMPLE", "EVERYDAYEXAMPLE" to "EXAMPLE",
        "TERM" to "TERM", "GLOSSARY" to "TERM", "KEYTERM" to "TERM",
        "QUIZ" to "QUIZ", "QUESTION" to "QUIZ",
        "ANSWER" to "ANSWER", "CORRECT" to "ANSWER", "CORRECTANSWER" to "ANSWER",
        "WHY" to "WHY", "EXPLANATION" to "WHY", "REASON" to "WHY",
        "HINT" to "HINT", "CLUE" to "HINT"
    )

    private class QuizDraft {
        val question = StringBuilder()
        val options = mutableListOf<String>()
        var answerRaw: String? = null
        val why = StringBuilder()
        val hint = StringBuilder()
        var field = QuizField.QUESTION
    }

    private class SectionDraft(var heading: String) {
        val body = StringBuilder()
    }

    /**
     * @param isFinal true once generation has finished. While streaming, the last (unterminated) line
     * is treated as partial: it can grow text on screen but cannot complete an interactive quiz.
     */
    fun parse(raw: String, topic: String, level: LearningLevel, isFinal: Boolean): Result {
        val lines = raw.replace("\r", "").split('\n')
        val lastIndex = lines.lastIndex
        val endsWithNewline = raw.endsWith("\n")

        var title = ""
        val overview = StringBuilder()
        val preamble = StringBuilder()
        val keyPoints = mutableListOf<String>()
        val flowSteps = mutableListOf<String>()
        val parts = mutableListOf<Pair<String, String>>()
        val sections = mutableListOf<SectionDraft>()
        val example = StringBuilder()
        val terms = mutableListOf<Pair<String, String>>()
        val quizzes = mutableListOf<QuizDraft>()

        var current: Block? = null
        var hasMarkup = false

        fun appendText(sb: StringBuilder, text: String) {
            if (text.isBlank()) return
            if (sb.isNotEmpty()) sb.append('\n')
            sb.append(text.trim())
        }

        fun addFlow(value: String) {
            value.split(flowSeparator)
                .map { cleanItem(it) }
                .filter { it.isNotBlank() }
                .forEach { flowSteps.add(it) }
        }

        for ((index, rawLine) in lines.withIndex()) {
            val line = rawLine.trimEnd()
            val trimmed = line.trim()
            val isPartialLine = !isFinal && index == lastIndex && !endsWithNewline
            if (trimmed.startsWith("```")) continue
            if (trimmed.isEmpty()) continue
            // A tag that is still being typed ("QUI") must not leak into the previous block.
            if (isPartialLine && partialTag.matches(trimmed)) continue

            // Options belong to the open quiz and are checked before generic tags ("A: ..." looks like a tag).
            val openQuiz = quizzes.lastOrNull()
            if (current == Block.QUIZ && openQuiz != null && openQuiz.answerRaw == null) {
                val opt = optionLine.find(trimmed)
                if (opt != null) {
                    if (!isPartialLine) openQuiz.options.add(Markdown.stripInline(opt.groupValues[2]).trim())
                    continue
                }
            }

            val tagMatch = tagLine.find(trimmed)
            val tag = tagMatch?.let { tagAliases[it.groupValues[1].uppercase().replace(" ", "")] }
            if (tagMatch != null && tag != null) {
                val value = tagMatch.groupValues[2].trim().trim('*', '_').trim()
                when (tag) {
                    "TITLE" -> { current = Block.TITLE; title = Markdown.stripInline(value) }
                    "OVERVIEW" -> { current = Block.OVERVIEW; appendText(overview, value) }
                    "KEY" -> { current = Block.KEY; cleanItem(value).takeIf { it.isNotBlank() }?.let(keyPoints::add) }
                    "FLOW" -> { current = Block.FLOW; addFlow(value) }
                    "PART" -> { current = Block.PART; splitPair(value)?.let(parts::add) }
                    "SECTION" -> { current = Block.SECTION; sections.add(SectionDraft(Markdown.stripInline(value))) }
                    "EXAMPLE" -> { current = Block.EXAMPLE; appendText(example, value) }
                    "TERM" -> { current = Block.TERM; splitPair(value)?.let(terms::add) }
                    "QUIZ" -> {
                        current = Block.QUIZ
                        quizzes.add(QuizDraft().also { appendText(it.question, value) })
                    }
                    "ANSWER", "WHY", "HINT" -> {
                        val quiz = quizzes.lastOrNull()
                        if (quiz == null) {
                            appendContinuation(current, trimmed, overview, preamble, keyPoints, sections, example)
                            continue
                        }
                        current = Block.QUIZ
                        when (tag) {
                            "ANSWER" -> if (!isPartialLine && value.isNotBlank()) quiz.answerRaw = value
                            "WHY" -> { quiz.field = QuizField.WHY; appendText(quiz.why, value) }
                            "HINT" -> { quiz.field = QuizField.HINT; appendText(quiz.hint, value) }
                        }
                    }
                }
                if (tag != "ANSWER" && tag != "WHY" && tag != "HINT") hasMarkup = true
                continue
            }

            // Continuation line for the current block.
            when (current) {
                Block.KEY -> cleanItem(trimmed).takeIf { it.isNotBlank() }?.let(keyPoints::add)
                Block.FLOW -> addFlow(trimmed)
                Block.PART -> splitPair(trimmed.replace(listMarker, ""))?.let(parts::add)
                Block.TERM -> splitPair(trimmed.replace(listMarker, ""))?.let(terms::add)
                Block.QUIZ -> {
                    val quiz = quizzes.lastOrNull() ?: continue
                    when (quiz.field) {
                        QuizField.QUESTION -> when {
                            // Unlettered bullet options ("- Option") once the question is written.
                            quiz.answerRaw == null && quiz.question.isNotBlank() && listMarker.containsMatchIn(trimmed) ->
                                if (!isPartialLine) quiz.options.add(Markdown.stripInline(cleanItem(trimmed)))
                            quiz.options.isEmpty() -> appendText(quiz.question, trimmed)
                        }
                        QuizField.WHY -> appendText(quiz.why, trimmed)
                        QuizField.HINT -> appendText(quiz.hint, trimmed)
                    }
                }
                else -> appendContinuation(current, line, overview, preamble, keyPoints, sections, example)
            }
        }

        val builtSections = sections.mapNotNull { draft ->
            val body = draft.body.toString().trim()
            when {
                body.isNotBlank() -> LessonSection(draft.heading.ifBlank { "Key idea" }, body)
                // A heading-only section whose "heading" is really a paragraph.
                draft.heading.length > 80 -> LessonSection("Key idea", draft.heading)
                else -> null
            }
        }

        val builtQuizzes = quizzes.mapNotNull { it.toQuiz() }
        // Text before the first tag is usually chatter ("Sure! Here is your lesson"); only use it without markup.
        val overviewText = overview.toString().trim().ifBlank { if (hasMarkup) "" else preamble.toString().trim() }

        val lesson = StructuredLesson(
            topic = topic,
            level = level,
            title = title.ifBlank { if (isFinal) topic else "" },
            overview = overviewText,
            sections = builtSections,
            practicalExample = example.toString().trim(),
            quizzes = builtQuizzes,
            keyPoints = keyPoints.distinctBy { it.lowercase() },
            flowSteps = flowSteps.take(8),
            parts = parts.distinctBy { it.first.lowercase() }.take(8).map { LessonPart(it.first, it.second) },
            terms = terms.distinctBy { it.first.lowercase() }.take(8).map { LessonTerm(it.first, it.second) }
        )

        return Result(
            lesson = lesson,
            activeBlock = if (isFinal) null else current,
            hasMarkup = hasMarkup,
            quizzesStarted = quizzes.size
        )
    }

    private fun appendContinuation(
        current: Block?,
        line: String,
        overview: StringBuilder,
        preamble: StringBuilder,
        keyPoints: MutableList<String>,
        sections: MutableList<SectionDraft>,
        example: StringBuilder
    ) {
        val target = when (current) {
            null -> preamble
            Block.OVERVIEW, Block.TITLE -> overview
            Block.SECTION -> sections.lastOrNull()?.body ?: overview
            Block.EXAMPLE -> example
            Block.KEY -> { cleanItem(line).takeIf { it.isNotBlank() }?.let(keyPoints::add); return }
            else -> return
        }
        if (target.isNotEmpty()) target.append('\n')
        target.append(line.trimEnd())
    }

    private fun cleanItem(text: String): String =
        text.replace(listMarker, "").trim().trim('*', '_').trim()

    /** Splits "name = meaning" (also accepts – — - and ":" separators). */
    private fun splitPair(text: String): Pair<String, String>? {
        val clean = text.trim()
        if (clean.isBlank()) return null
        val separators = listOf(" = ", "=", " – ", " — ", " - ", ": ")
        for (sep in separators) {
            val i = clean.indexOf(sep)
            if (i > 0) {
                val name = Markdown.stripInline(clean.substring(0, i)).trim()
                val meaning = clean.substring(i + sep.length).trim()
                if (name.isNotBlank() && name.length <= 60) return name to meaning
            }
        }
        return Markdown.stripInline(clean) to ""
    }

    private fun QuizDraft.toQuiz(): LessonQuiz? {
        val q = question.toString().trim()
        // Not de-duplicated: removing an option would shift the letter the answer refers to.
        val opts = options.filter { it.isNotBlank() }
        if (q.isBlank() || opts.size < 2) return null
        val index = resolveAnswer(answerRaw ?: return null, opts) ?: return null
        return LessonQuiz(
            question = q,
            options = opts,
            correctOptionIndex = index,
            explanation = why.toString().trim(),
            hint = hint.toString().trim().ifBlank { null }
        )
    }

    /** Resolves "B", "B) text", "(b)", "Option B", "2" (1-based) or the option text itself. Null if unresolvable. */
    internal fun resolveAnswer(raw: String, options: List<String>): Int? {
        val t = Markdown.stripInline(raw).trim().trim('.', ' ')
            .replace(Regex("""^(?:option|answer)\s+""", RegexOption.IGNORE_CASE), "")
        if (t.isEmpty()) return null

        options.indexOfFirst { it.equals(t, ignoreCase = true) }.takeIf { it >= 0 }?.let { return it }

        Regex("""^\(?([A-Ha-h])\)?(?:[).:]|$)""").find(t)?.let { m ->
            val idx = m.groupValues[1].uppercase()[0] - 'A'
            return idx.takeIf { it in options.indices }
        }
        // "B Some option text" — trust the letter only when the text agrees with that option.
        Regex("""^([A-Ha-h])\s+(.+)$""").find(t)?.let { m ->
            val idx = m.groupValues[1].uppercase()[0] - 'A'
            val rest = m.groupValues[2]
            if (idx in options.indices && options[idx].contains(rest, ignoreCase = true)) return idx
        }
        t.toIntOrNull()?.let { n -> return (n - 1).takeIf { it in options.indices } }

        val contained = options.indices.filter { t.contains(options[it], ignoreCase = true) }
        return contained.singleOrNull()
    }
}
