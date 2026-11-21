package com.openfield.app.core.components

data class MermaidStep(
    val fromNode: String,
    val edgeLabel: String? = null,
    val toNode: String
)

object MermaidParser {
    fun isMermaidCode(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.startsWith("graph ", ignoreCase = true) ||
                trimmed.startsWith("flowchart ", ignoreCase = true) ||
                trimmed.startsWith("sequenceDiagram", ignoreCase = true) ||
                trimmed.startsWith("stateDiagram", ignoreCase = true) ||
                trimmed.contains("-->") ||
                trimmed.contains("->>")
    }

    fun parseSteps(mermaid: String): List<MermaidStep> {
        val lines = mermaid.lines().map { it.trim() }.filter { it.isNotBlank() }
        val steps = mutableListOf<MermaidStep>()

        val arrowRegex = Regex("([^\\[\\{\\(\\)]+?(?:\\[.*?\\]|\\{.*?\\}|\\(.*?\\))?)\\s*-->(?:\\|([^|]+)\\|)?\\s*([^\\[\\{\\(\\)]+?(?:\\[.*?\\]|\\{.*?\\}|\\(.*?\\))?)")

        for (line in lines) {
            val match = arrowRegex.find(line)
            if (match != null) {
                val from = cleanNodeText(match.groupValues[1])
                val edge = match.groupValues[2].trim().takeIf { it.isNotBlank() }
                val to = cleanNodeText(match.groupValues[3])
                if (from.isNotBlank() && to.isNotBlank()) {
                    steps.add(MermaidStep(from, edge, to))
                }
            }
        }
        return steps
    }

    private fun cleanNodeText(raw: String): String {
        var s = raw.trim()
        val bracketMatch = Regex("\\[(.*?)\\]|\\{(.*?)\\}|\\((.*?)\\)").find(s)
        if (bracketMatch != null) {
            val inner = bracketMatch.groupValues.drop(1).firstOrNull { it.isNotBlank() }
            if (!inner.isNullOrBlank()) return inner.trim()
        }
        return s.replace(Regex("^[A-Za-z0-9_]+\\s*"), "").ifBlank { s }
    }
}
