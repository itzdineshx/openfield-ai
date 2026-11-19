package com.openfield.app.feature.ai.mode

import com.openfield.app.feature.ai.engine.AiGenerationConfig

enum class ModeInputType {
    TEXT_ONLY,
    TEXT_AND_IMAGE,
    TEXT_AND_DOCUMENT,
    STRUCTURED_INPUT
}

enum class ModeOutputFormat {
    NATURAL_STREAMING,
    STRUCTURED_JSON,
    MARKDOWN_DOCUMENT
}

data class ModeConfiguration(
    val mode: OpenFieldMode,
    val displayName: String,
    val description: String,
    val systemInstruction: String,
    val defaultGenConfig: AiGenerationConfig,
    val inputType: ModeInputType,
    val outputFormat: ModeOutputFormat,
    val requiresSession: Boolean = true
)

object ModeConfigRegistry {

    private const val FORMAT_GUIDE = "Format answers for a phone screen: short paragraphs, numbered steps for procedures, " +
        "bullet points for lists, **bold** only for key terms, and a line starting with 'Note:' for important caveats. " +
        "Avoid tables unless asked."

    private val configurations: Map<OpenFieldMode, ModeConfiguration> = mapOf(
        OpenFieldMode.ASK_AI to ModeConfiguration(
            mode = OpenFieldMode.ASK_AI,
            displayName = "Ask AI",
            description = "Natural conversational assistant for questions and general dialogue.",
            systemInstruction = "You are OpenField's offline AI assistant powered by local Gemma. " +
                    "Answer clearly, naturally, and helpfully. Keep answers conversational, factual, and direct. " +
                    "Do not emit quizzes, interactive forms, or troubleshooting menus in this conversation mode.",
            defaultGenConfig = AiGenerationConfig(
                temperature = 0.7,
                topK = 40,
                topP = 0.9,
                systemPrompt = "You are OpenField, a helpful offline assistant. Answer accurately and directly. " +
                    FORMAT_GUIDE
            ),
            inputType = ModeInputType.TEXT_ONLY,
            outputFormat = ModeOutputFormat.NATURAL_STREAMING,
            requiresSession = true
        ),
        OpenFieldMode.ANALYZE_IMAGE to ModeConfiguration(
            mode = OpenFieldMode.ANALYZE_IMAGE,
            displayName = "Analyze Image",
            description = "Dedicated visual analysis workspace for identification, inspection, and explanation.",
            systemInstruction = "You are OpenField's offline image analysis expert. " +
                    "Analyze the provided image thoroughly. Ground your analysis only in what is visible. " +
                    "Organize findings into: What I see, Important details, Explanation, Possible identification, Suggested next steps, and Uncertainty/limitations.",
            defaultGenConfig = AiGenerationConfig(
                temperature = 0.3,
                topK = 30,
                topP = 0.85,
                systemPrompt = "You are OpenField's offline image analysis assistant. Describe only what is actually visible, " +
                    "say clearly when you are unsure, and follow the requested section headings exactly. " + FORMAT_GUIDE
            ),
            inputType = ModeInputType.TEXT_AND_IMAGE,
            outputFormat = ModeOutputFormat.NATURAL_STREAMING,
            requiresSession = false
        ),
        OpenFieldMode.ASK_DOCUMENT to ModeConfiguration(
            mode = OpenFieldMode.ASK_DOCUMENT,
            displayName = "Ask Document",
            description = "Ground-truth Q&A on indexed local PDF documents.",
            systemInstruction = "You are OpenField's offline document assistant. " +
                    "Answer strictly based on the provided document excerpts. " +
                    "Cite the exact page numbers from which facts are drawn. " +
                    "If the excerpts do not contain enough information, explicitly state that.",
            defaultGenConfig = AiGenerationConfig(
                temperature = 0.2,
                topK = 30,
                topP = 0.85,
                systemPrompt = "You are OpenField's offline document Q&A assistant. Ground answers strictly in the provided text " +
                    "and cite pages like (Page 4). " + FORMAT_GUIDE
            ),
            inputType = ModeInputType.TEXT_AND_DOCUMENT,
            outputFormat = ModeOutputFormat.NATURAL_STREAMING,
            requiresSession = true
        ),
        OpenFieldMode.SOLVE_PROBLEM to ModeConfiguration(
            mode = OpenFieldMode.SOLVE_PROBLEM,
            displayName = "Solve a Problem",
            description = "Interactive guided troubleshooting state machine.",
            systemInstruction = "You are OpenField's diagnostic assistant. " +
                    "Given a user's problem and observation history, provide a structured diagnostic assessment with selectable answer choices. " +
                    "Never state uncertain diagnoses as guaranteed facts. Always highlight electrical or hardware safety warnings.",
            defaultGenConfig = AiGenerationConfig(
                temperature = 0.3,
                topK = 30,
                topP = 0.85,
                systemPrompt = "You are OpenField's diagnostic troubleshooting expert. Generate systematic diagnostic steps. " +
                    "Reply with a single valid JSON object only, with no text before or after it."
            ),
            inputType = ModeInputType.STRUCTURED_INPUT,
            outputFormat = ModeOutputFormat.STRUCTURED_JSON,
            requiresSession = true
        ),
        OpenFieldMode.LEARN_EXPLAIN to ModeConfiguration(
            mode = OpenFieldMode.LEARN_EXPLAIN,
            displayName = "Learn & Explain",
            description = "Interactive tutoring with concept breakdown, examples, and knowledge checks.",
            systemInstruction = "You are OpenField's interactive educational tutor. " +
                    "Generate a structured lesson tailored to the requested learning level. " +
                    "Include clear explanations, intuitive analogies, and an interactive multiple-choice quiz with explanations.",
            defaultGenConfig = AiGenerationConfig(
                temperature = 0.5,
                topK = 40,
                topP = 0.9,
                systemPrompt = "You are OpenField's interactive tutor. Teach clearly with everyday analogies and short paragraphs. " +
                    "When asked for a lesson in JSON, reply with a single valid JSON object only. Otherwise " + FORMAT_GUIDE
            ),
            inputType = ModeInputType.STRUCTURED_INPUT,
            outputFormat = ModeOutputFormat.STRUCTURED_JSON,
            requiresSession = true
        ),
        OpenFieldMode.CREATE_WRITE to ModeConfiguration(
            mode = OpenFieldMode.CREATE_WRITE,
            displayName = "Create & Write",
            description = "Document-oriented writing, drafting, and polishing workspace.",
            systemInstruction = "You are OpenField's offline writing and editing assistant. " +
                    "Produce well-crafted, clean content formatted directly for document use. " +
                    "Adopt the requested tone and format without conversational preamble.",
            defaultGenConfig = AiGenerationConfig(
                temperature = 0.7,
                topK = 40,
                topP = 0.9,
                systemPrompt = "You are OpenField's offline writing assistant. Output only the finished text itself, " +
                    "never an introduction such as 'Here is your draft' and never a closing remark. Use markdown headings and " +
                    "lists only where the format normally uses them."
            ),
            inputType = ModeInputType.TEXT_ONLY,
            outputFormat = ModeOutputFormat.MARKDOWN_DOCUMENT,
            requiresSession = true
        ),
        OpenFieldMode.AI_TOOLS to ModeConfiguration(
            mode = OpenFieldMode.AI_TOOLS,
            displayName = "AI Tools",
            description = "Specialized single-purpose text and media utilities.",
            systemInstruction = "You are OpenField's utility assistant. Execute the specific transformation requested directly and accurately.",
            defaultGenConfig = AiGenerationConfig(
                temperature = 0.4,
                topK = 30,
                topP = 0.9,
                systemPrompt = "You are OpenField's offline utility assistant. Perform the requested task directly " +
                    "with no preamble. " + FORMAT_GUIDE
            ),
            inputType = ModeInputType.TEXT_ONLY,
            outputFormat = ModeOutputFormat.NATURAL_STREAMING,
            requiresSession = false
        )
    )

    fun get(mode: OpenFieldMode): ModeConfiguration {
        return configurations[mode] ?: configurations.getValue(OpenFieldMode.ASK_AI)
    }

    fun getAll(): List<ModeConfiguration> = configurations.values.toList()
}
