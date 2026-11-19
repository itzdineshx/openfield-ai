package com.openfield.app.feature.ai.mode

/**
 * Strongly typed identifiers for OpenField AI modes.
 * Each mode represents a dedicated interaction experience sharing the same Gemma inference engine.
 */
enum class OpenFieldMode(val id: String, val displayName: String) {
    ASK_AI("ask_ai", "Ask AI"),
    ANALYZE_IMAGE("analyze_image", "Analyze Image"),
    ASK_DOCUMENT("ask_document", "Ask Document"),
    SOLVE_PROBLEM("solve_problem", "Solve a Problem"),
    LEARN_EXPLAIN("learn_explain", "Learn & Explain"),
    CREATE_WRITE("create_write", "Create & Write"),
    AI_TOOLS("ai_tools", "AI Tools");

    companion object {
        fun fromId(id: String): OpenFieldMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ASK_AI
        }
    }
}
