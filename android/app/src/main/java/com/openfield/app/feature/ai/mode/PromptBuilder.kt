package com.openfield.app.feature.ai.mode

import com.openfield.app.feature.ai.context.ContextWindowOptimizer

object PromptBuilder {

    fun buildAskAiPrompt(
        userMessage: String,
        language: String? = null,
        responseLength: String? = null,
        explanationLevel: String? = null,
        measurementUnits: String? = null,
        writingTone: String? = null,
        formattingStyle: String? = null,
        customMemory: String? = null
    ): String {
        val directives = mutableListOf<String>()
        if (!language.isNullOrBlank() && !language.equals("English", ignoreCase = true)) {
            directives.add("Respond in $language.")
        }
        if (!responseLength.isNullOrBlank()) {
            when (responseLength.lowercase()) {
                "concise" -> directives.add("Keep response brief, direct, and concise.")
                "detailed" -> directives.add("Provide a comprehensive, detailed explanation.")
            }
        }
        if (!explanationLevel.isNullOrBlank()) {
            when (explanationLevel.lowercase()) {
                "beginner" -> directives.add("Explain simply for a beginner without heavy technical jargon.")
                "advanced" -> directives.add("Provide an advanced, technically rigorous explanation.")
            }
        }
        if (!measurementUnits.isNullOrBlank()) {
            when (measurementUnits.lowercase()) {
                "metric" -> directives.add("Use Metric measurement units (meters, kilograms, °C).")
                "imperial" -> directives.add("Use Imperial measurement units (feet, pounds, °F).")
            }
        }
        if (!writingTone.isNullOrBlank()) {
            directives.add("Tone: $writingTone.")
        }
        if (!formattingStyle.isNullOrBlank() && !formattingStyle.equals("Standard", ignoreCase = true)) {
            directives.add("Format with $formattingStyle.")
        }
        if (!customMemory.isNullOrBlank()) {
            directives.add("User Context: $customMemory")
        }

        return if (directives.isNotEmpty()) {
            "[Instructions: ${directives.joinToString(" ")}]\n\n${userMessage.trim()}"
        } else {
            userMessage.trim()
        }
    }

    /**
     * Bounded context limiter using smart semantic trimming (respecting sentences and paragraphs)
     * rather than mid-word slicing.
     */
    fun boundContext(text: String, maxChars: Int = 4000): String {
        val maxTokens = maxChars / 4
        return ContextWindowOptimizer.smartTrim(text, maxTokens)
    }

    fun buildImageAnalysisPrompt(userQuestion: String?): String {
        val specificQuery = if (!userQuestion.isNullOrBlank()) {
            "Focus specifically on answering the user's question: \"$userQuestion\"."
        } else {
            "Analyze this image thoroughly."
        }

        return """
$specificQuery

Describe what is visible in the image. Organize your response into the following clear sections:
## 1. What I See
(Clear, factual description of visible objects, organisms, or scene)

## 2. Important Details
(Key markings, colors, condition, text, or notable features)

## 3. Explanation
(Context, how it works, biological background, or functional meaning)

## 4. Possible Identification
(Name, subject classification, or model type if identifiable)

## 5. Suggested Next Steps
(Actionable recommendations or things to check)

## 6. Uncertainty & Limitations
(Any ambiguities, camera angle limits, or safety caveats)
""".trimIndent()
    }

    fun buildImageFollowUpPrompt(userQuestion: String, priorSummary: String?): String {
        val context = if (!priorSummary.isNullOrBlank()) {
            "Prior image analysis summary:\n$priorSummary\n\n"
        } else ""

        return "${context}User's follow-up question about this image: $userQuestion\n\nAnswer directly and clearly based on the visual content of the image."
    }

    fun buildDocumentQaPrompt(
        documentName: String,
        excerptsWithPages: List<Pair<Int, String>>,
        question: String
    ): String {
        // Recover 20-30% context window space by merging overlapping passages
        val optimizedExcerpts = ContextWindowOptimizer.mergeOverlappingChunks(excerptsWithPages)
        val contextBuilder = StringBuilder()
        for ((page, text) in optimizedExcerpts) {
            contextBuilder.append("[Page $page]:\n")
            contextBuilder.append(text.trim())
            contextBuilder.append("\n\n")
        }

        val citedPages = optimizedExcerpts.map { it.first }.distinct().sorted()
        val pageHints = if (citedPages.isNotEmpty()) {
            "Relevant excerpt pages available: ${citedPages.joinToString(", ") { "Page $it" }}."
        } else ""

        return """
Answer the following question about the document '$documentName' using ONLY the provided excerpts.

RULES:
1. Ground your answer strictly on the provided excerpts.
2. State the relevant page number (e.g. [Page ${citedPages.firstOrNull() ?: 1}]) where facts are found.
3. If the answer cannot be found in these excerpts, say: "The provided document pages do not contain information to answer this question."
4. Never invent page numbers or citations.

DOCUMENT EXCERPTS:
$contextBuilder
$pageHints

QUESTION:
$question

ANSWER:
""".trimIndent()
    }

    fun buildDocumentComparisonPrompt(
        docAName: String,
        docBName: String,
        excerptsA: List<Pair<Int, String>>,
        excerptsB: List<Pair<Int, String>>,
        focusQuery: String? = null
    ): String {
        val aBuilder = StringBuilder()
        excerptsA.forEach { (page, text) ->
            aBuilder.append("[$docAName - Page $page]:\n${text.trim()}\n\n")
        }

        val bBuilder = StringBuilder()
        excerptsB.forEach { (page, text) ->
            bBuilder.append("[$docBName - Page $page]:\n${text.trim()}\n\n")
        }

        val focus = if (!focusQuery.isNullOrBlank()) {
            "Focus specifically on comparison regarding: \"$focusQuery\"."
        } else "Compare the main themes, key distinctions, and findings between both documents."

        return """
Compare the two documents '$docAName' and '$docBName' using ONLY the provided excerpts.
$focus

RULES:
1. Ground comparisons strictly on the provided excerpts.
2. Structure your response into:
   - Key Similarities
   - Key Differences & Contrasts
   - Summary of Distinctions
3. Reference source documents and page numbers for facts cited.
4. If an aspect is missing from either document, state that transparently.

EXCERPTS FROM '$docAName':
$aBuilder

EXCERPTS FROM '$docBName':
$bBuilder

COMPARATIVE ANALYSIS:
""".trimIndent()
    }

    fun buildDocumentSummaryPrompt(
        documentName: String,
        excerptsWithPages: List<Pair<Int, String>>
    ): String {
        val contextBuilder = StringBuilder()
        excerptsWithPages.forEach { (page, text) ->
            contextBuilder.append("[Page $page]:\n${text.trim()}\n\n")
        }

        return """
Provide an executive, structured summary of the document '$documentName' based on the following excerpts.

RULES:
1. Ground the summary strictly on the excerpts.
2. Include:
   - High-level overview (2-3 sentences)
   - Core concepts / sections covered
   - Key takeaways with cited page numbers
3. Do not invent details not present in the text.

DOCUMENT EXCERPTS:
$contextBuilder

EXECUTIVE SUMMARY:
""".trimIndent()
    }

    fun buildCrossSourceQaPrompt(
        query: String,
        evidenceList: List<com.openfield.app.core.model.KnowledgeEvidence>,
        scopeName: String = "Selected Sources"
    ): String {
        val evidenceBuilder = StringBuilder()
        for (item in evidenceList) {
            val pageTag = if (item.pageNumber != null) " (Page ${item.pageNumber})" else if (item.section != null) " (${item.section})" else ""
            evidenceBuilder.append("[Source: ${item.sourceTitle}$pageTag]:\n")
            evidenceBuilder.append(item.text.trim())
            evidenceBuilder.append("\n\n")
        }

        val sourceNames = evidenceList.map { it.sourceTitle }.distinct().joinToString(", ")

        return """
Answer the following question about '$scopeName' ($sourceNames) using ONLY the provided multi-source evidence.

RULES:
1. Ground your answer strictly on the facts in the provided evidence.
2. For EVERY claim, cite the source using: [Source: <Title> (Page <P>)] or [Source: <Title>].
3. If sources present conflicting information, explicitly point out the discrepancy.
4. If the provided evidence is insufficient to answer the question, state: "The selected sources do not contain sufficient information to answer this question."
5. Never invent sources, page numbers, or external facts.

MULTI-SOURCE EVIDENCE:
$evidenceBuilder
QUESTION:
$query

GROUNDED ANSWER:
""".trimIndent()
    }

    fun buildTaskPlanningPrompt(userInput: String): String {
        return """
The user wants to plan and execute a task or workflow:
"$userInput"

Break down this goal into a structured, practical, action-oriented plan.
OUTPUT JSON FORMAT ONLY:
```json
{
  "title": "Concise actionable title for the overall task",
  "description": "Clear 1-2 sentence description of what done looks like",
  "priority": "HIGH" or "MEDIUM" or "LOW",
  "suggestedCategory": "Category (e.g. Study, Technical, Maintenance, Project, Writing)",
  "subtasks": [
    "First concrete action step",
    "Second concrete action step",
    "Third concrete action step",
    "Fourth concrete action step"
  ]
}
```
""".trimIndent()
    }

    fun buildTroubleshootIntakePrompt(problemDescription: String, hasImage: Boolean = false): String {
        val imageNotice = if (hasImage) {
            "\nNote: A contextual photo of the physical equipment/part is provided. Inspect the image for visible symptoms, burnt components, leakage, error codes, or damage, and integrate visual findings directly into your diagnostic check.\n"
        } else ""

        return """
The user is experiencing a technical, mechanical, electrical, or device problem:
"$problemDescription"
$imageNotice
You are a diagnostic expert. Analyze this problem and output a structured diagnostic response in JSON format.

OUTPUT JSON FORMAT ONLY:
```json
{
  "category": "Category of problem (e.g. Electrical, Automotive, Electronics, Plumbing, Software)",
  "safetyNotice": "Immediate safety warning if there is any hazard (high voltage, fuel, pressure, chemicals). Null if safe.",
  "currentStepNumber": 1,
  "totalEstimatedSteps": 3,
  "question": "The single most decisive diagnostic question to determine the root cause?",
  "explanation": "Brief explanation of why this check is important.",
  "options": [
    {"id": "A", "text": "First common observation or symptom"},
    {"id": "B", "text": "Second common observation or symptom"},
    {"id": "C", "text": "Third observation or none of the above"}
  ],
  "isResolutionReached": false,
  "likelyCause": null,
  "recommendedSolution": null,
  "isEscalationNeeded": false,
  "escalationReason": null
}
```

RULES:
- Provide 2 to 4 distinct selectable options (under 12 words each).
- Ask a single targeted check under 25 words.
- Highlight safety warnings if dangerous components are involved.
- Set "isEscalationNeeded": true with "escalationReason" if mains voltage, flammable gas, or toxic hazard is present.
- Respond with valid JSON only.
""".trimIndent()
    }

    fun buildTroubleshootNextStepPrompt(
        problemDescription: String,
        history: List<DiagnosticStepHistory>
    ): String {
        val historyBuilder = StringBuilder()
        history.forEach { item ->
            historyBuilder.append("- Step ${item.stepIndex}: \"${item.question}\" -> User answered: \"${item.selectedOptionText}\"\n")
        }

        val nextStepNum = history.size + 1
        val canResolve = history.size >= 2
        val hasFailedBranch = history.any { it.selectedOptionText.contains("did not resolve", ignoreCase = true) || it.selectedOptionText.contains("alternative", ignoreCase = true) }

        val branchGuidance = if (hasFailedBranch) {
            "NOTE: The user indicated that an earlier recommended fix did NOT resolve the issue. Do NOT repeat previous suggestions. Pivot to an alternative diagnostic branch exploring secondary causes or underlying component failures.\n"
        } else ""

        return """
Problem: "$problemDescription"

Diagnostic History:
$historyBuilder
$branchGuidance
Based on the user's answers so far, either generate the next targeted diagnostic check (Step $nextStepNum) OR, if sufficient evidence exists, provide the definitive diagnosis and resolution.

OUTPUT JSON FORMAT ONLY:
```json
{
  "category": "Category of problem",
  "safetyNotice": "Safety warning if applicable, else null",
  "currentStepNumber": $nextStepNum,
  "totalEstimatedSteps": ${maxOf(nextStepNum, 3)},
  "question": "Next diagnostic question (or summary question if resolved)",
  "explanation": "Why this step matters",
  "options": [
    {"id": "A", "text": "First outcome"},
    {"id": "B", "text": "Second outcome"}
  ],
  "isResolutionReached": ${if (canResolve) "true or false" else "false"},
  "likelyCause": "Identified probable cause (only if isResolutionReached is true, else null)",
  "recommendedSolution": "Step-by-step repair or fix instructions (only if isResolutionReached is true, else null)",
  "isEscalationNeeded": false,
  "escalationReason": null
}
```

RULES:
- Do not repeat a question that was already asked.
- If not resolved, give 2 to 4 short, mutually exclusive options.
- If resolved, "recommendedSolution" must be a numbered list of concrete fix steps (e.g. "1. ...\n2. ...").
- Respond with valid JSON.
""".trimIndent()
    }

    /**
     * Lesson prompt in a line-tagged format (parsed by [LessonMarkupParser]). Tags are emitted in the
     * order the screen renders them, so the title, overview and key ideas appear within seconds while the
     * rest streams in. Line tags are cheaper and far more reliable for a 2B on-device model than nested JSON.
     */
    fun buildLearningLessonPrompt(topic: String, level: LearningLevel): String {
        val (keyCount, sectionCount) = when (level) {
            LearningLevel.BEGINNER -> "3" to "2"
            LearningLevel.INTERMEDIATE -> "3 to 4" to "2 to 3"
            LearningLevel.ADVANCED -> "4" to "3"
        }
        return """
Teach a ${level.displayName} lesson about: "$topic"
Level: ${level.promptDescription}

Write the lesson using ONLY the tagged lines below, in this order. Start every tagged line with its TAG and a colon. No JSON, no markdown headings, no text before TITLE.

TITLE: short engaging title
OVERVIEW: 2-3 sentences on what it is and why it matters
KEY: one key fact in under 15 words ($keyCount KEY lines)
FLOW: first step -> next step -> next step -> result (how it works in 3-5 short steps; skip if it is not a process)
PART: part name = what it does (2-4 PART lines for its main parts or ingredients; skip if none)
SECTION: concept heading
one short paragraph explaining it ($sectionCount SECTION blocks)
EXAMPLE: one everyday example or analogy
TERM: word = simple meaning (2-3 TERM lines)
QUIZ: a question that checks understanding
A) option
B) option
C) option
D) option
ANSWER: letter of the correct option
WHY: one sentence on why it is correct
HINT: a clue that does not reveal the answer
(write 2 QUIZ blocks, each with its own ANSWER, WHY and HINT)

Be accurate. Leave out any detail you are unsure of.
""".trimIndent()
    }

    /** Asks for additional knowledge checks in the same QUIZ line format, avoiding questions already asked. */
    fun buildLessonPracticePrompt(
        topic: String,
        lessonTitle: String,
        lessonContext: String?,
        existingQuestions: List<String>,
        count: Int = 2
    ): String {
        val avoid = if (existingQuestions.isEmpty()) "" else
            "\nAlready asked (write different questions):\n" +
                existingQuestions.takeLast(6).joinToString("\n") { "- ${it.take(140)}" } + "\n"
        return """
Topic: "$topic"
Lesson: "$lessonTitle"
${lessonContextBlock(lessonContext)}$avoid
Write $count new multiple-choice questions that check understanding of this lesson. Use ONLY this format for each question:
QUIZ: the question
A) option
B) option
C) option
D) option
ANSWER: letter of the correct option
WHY: one sentence on why it is correct
HINT: a clue that does not reveal the answer
""".trimIndent()
    }

    private fun lessonContextBlock(lessonContext: String?): String =
        if (lessonContext.isNullOrBlank()) "" else "\nLesson content so far:\n${lessonContext.take(1500)}\n"

    fun buildLearningFollowUpPrompt(topic: String, lessonTitle: String, userQuestion: String, lessonContext: String? = null): String {
        return """
Topic: "$topic"
Lesson: "$lessonTitle"
${lessonContextBlock(lessonContext)}
User's follow-up question:
"$userQuestion"

Explain clearly and concisely in the context of this lesson. Use short paragraphs and bullet points where helpful.
""".trimIndent()
    }

    fun buildExplainMoreSimplyPrompt(topic: String, lessonTitle: String, lessonContext: String? = null): String {
        return """
Topic: "$topic"
Lesson: "$lessonTitle"
${lessonContextBlock(lessonContext)}
INSTRUCTION:
Explain this concept even more simply, using plain everyday words and an intuitive breakdown as if explaining to someone completely new to the subject. Avoid any unnecessary technical jargon. End with a short "In short:" bullet list.
""".trimIndent()
    }

    fun buildShowAnotherExamplePrompt(topic: String, lessonTitle: String, lessonContext: String? = null): String {
        return """
Topic: "$topic"
Lesson: "$lessonTitle"
${lessonContextBlock(lessonContext)}
INSTRUCTION:
Provide a fresh, completely different real-world analogy or practical scenario illustrating "$topic". Show step-by-step how the concept applies to this new example, using a numbered list.
""".trimIndent()
    }

    fun buildSummarizeTopicPrompt(topic: String, lessonTitle: String, lessonContext: String? = null): String {
        return """
Topic: "$topic"
Lesson: "$lessonTitle"
${lessonContextBlock(lessonContext)}
INSTRUCTION:
Provide a concise, 3-to-4 bullet-point revision summary capturing the absolute core principles and practical takeaways of this lesson.
""".trimIndent()
    }

    fun buildFreeResponseCheckPrompt(topic: String, question: String, userAnswer: String): String {
        return """
Topic: "$topic"
Knowledge Check Question: "$question"
User's Answer: "$userAnswer"

INSTRUCTION:
Evaluate the user's answer:
1. State whether their understanding is correct, partially correct, or needs clarification.
2. Highlight what they got right.
3. Correct any misconceptions gently and concisely.
4. Provide a 1-sentence reinforcing tip.
""".trimIndent()
    }

    fun buildWritingPrompt(
        intent: WritingIntent,
        tone: WritingTone,
        instruction: String,
        sourceText: String? = null
    ): String {
        val sourceSection = if (!sourceText.isNullOrBlank()) {
            "\nSOURCE / REFERENCE TEXT:\n$sourceText\n"
        } else ""

        return """
You are an expert writing assistant.
Format: ${intent.displayName}
Tone: ${tone.displayName}
$sourceSection
USER INSTRUCTION:
$instruction

Draft the complete piece of writing directly.
RULES:
1. Do not include conversational introductory remarks (e.g. "Sure, here is your email:").
2. Write ready-to-use, polished text in the requested tone.
3. Structure with appropriate headers, paragraphs, or bullet points if standard for this format.
""".trimIndent()
    }

    fun buildWritingRefinePrompt(
        refinementType: String,
        currentText: String
    ): String {
        val instruction = when (refinementType.lowercase()) {
            "shorter" -> "Condense and shorten the text while keeping all core facts and meaning."
            "professional" -> "Elevate the language to be more professional, polished, and authoritative."
            "grammar" -> "Fix all grammar, punctuation, spelling, and phrasing errors without changing the core message."
            "expand" -> "Elaborate with greater detail, clarity, and completeness."
            else -> "Improve clarity, tone, and flow."
        }

        return """
$instruction

Keep the same format and structure (greeting, headings, lists, sign-off) unless the instruction requires otherwise.

ORIGINAL CONTENT:
$currentText

REVISED CONTENT:
(Provide only the revised content without introductory or concluding conversational text)
""".trimIndent()
    }
}
