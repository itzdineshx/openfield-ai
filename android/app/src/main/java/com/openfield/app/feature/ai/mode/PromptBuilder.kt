package com.openfield.app.feature.ai.mode

object PromptBuilder {

    fun buildAskAiPrompt(
        userMessage: String,
        language: String? = null,
        responseLength: String? = null,
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
        if (!customMemory.isNullOrBlank()) {
            directives.add("User Context: $customMemory")
        }

        return if (directives.isNotEmpty()) {
            "[Instructions: ${directives.joinToString(" ")}]\n\n${userMessage.trim()}"
        } else {
            userMessage.trim()
        }
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
        val contextBuilder = StringBuilder()
        for ((page, text) in excerptsWithPages) {
            contextBuilder.append("[Page $page]:\n")
            contextBuilder.append(text.trim())
            contextBuilder.append("\n\n")
        }

        val citedPages = excerptsWithPages.map { it.first }.distinct().sorted()
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

    fun buildLearningLessonPrompt(topic: String, level: LearningLevel): String {
        return """
Create an interactive lesson on the topic: "$topic"
Target Learning Level: ${level.displayName} (${level.promptDescription})

OUTPUT JSON FORMAT ONLY:
```json
{
  "topic": "$topic",
  "title": "Clear Engaging Lesson Title",
  "overview": "2-3 sentence core summary of what this concept is and why it matters.",
  "sections": [
    {
      "heading": "First Key Principle",
      "content": "Clear explanation of how this part works."
    },
    {
      "heading": "Second Key Principle",
      "content": "Further details or real-world application."
    }
  ],
  "practicalExample": "Everyday intuitive analogy or practical real-world scenario demonstrating this concept.",
  "quiz": {
    "question": "A conceptual multiple-choice question to test understanding of this lesson?",
    "options": [
      "Option A text",
      "Option B text",
      "Option C text",
      "Option D text"
    ],
    "correctOptionIndex": 0,
    "explanation": "Why the correct option is right and common pitfalls of wrong answers.",
    "hint": "A helpful clue to think about before answering."
  }
}
```

RULES:
- Match the depth to the ${level.displayName} level.
- Provide 2 to 3 well-written sections.
- The quiz must have 3 or 4 options with exactly 1 correct option index (0-based: 0, 1, 2, or 3).
- Respond with valid JSON.
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
