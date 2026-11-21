package com.openfield.app.core.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Fast, lightweight syntax highlighter for on-device code rendering in Compose.
 * Supports Kotlin, Python, SQL, JSON, JavaScript/TypeScript, Bash, and XML.
 */
object SyntaxHighlighter {

    private val KEYWORD_COLOR = Color(0xFF38BDF8)   // Sky blue
    private val STRING_COLOR = Color(0xFF4ADE80)    // Emerald green
    private val NUMBER_COLOR = Color(0xFFFBBF24)    // Amber
    private val COMMENT_COLOR = Color(0xFF64748B)   // Slate gray
    private val TYPE_COLOR = Color(0xFFC084FC)      // Purple
    private val OPERATOR_COLOR = Color(0xFFF472B6)  // Pink
    private val DEFAULT_TEXT_COLOR = Color(0xFFE2E8F0) // Light slate

    private val COMMON_KEYWORDS = setOf(
        "fun", "val", "var", "class", "object", "interface", "package", "import",
        "return", "if", "else", "when", "for", "while", "do", "break", "continue",
        "try", "catch", "finally", "throw", "null", "true", "false", "this", "super",
        "public", "private", "protected", "internal", "override", "open", "abstract",
        "sealed", "data", "enum", "suspend", "inline", "const", "companion",
        // Python
        "def", "from", "as", "elif", "except", "lambda", "with", "yield", "pass",
        "async", "await", "self", "None", "True", "False",
        // SQL
        "select", "from", "where", "insert", "into", "values", "update", "set",
        "delete", "join", "inner", "left", "right", "outer", "group", "by",
        "order", "having", "limit", "create", "table", "drop", "alter", "primary",
        "key", "foreign", "references", "not", "and", "or", "in", "like", "distinct",
        // JS / TS
        "const", "let", "function", "export", "default", "new", "typeof", "instanceof"
    )

    fun highlight(code: String, language: String? = null): AnnotatedString {
        if (code.isBlank()) return AnnotatedString(code)

        val lang = language?.lowercase()?.trim()
        val isJson = lang == "json" || (code.trim().startsWith("{") && code.trim().endsWith("}"))

        return buildAnnotatedString {
            val lines = code.lines()
            lines.forEachIndexed { lineIdx, line ->
                highlightLine(line, isJson)
                if (lineIdx < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.highlightLine(line: String, isJson: Boolean) {
        var idx = 0
        val len = line.length

        while (idx < len) {
            val ch = line[idx]

            // 1. Line Comments (// or # or --)
            if (idx + 1 < len && (line.substring(idx, idx + 2) == "//" || line.substring(idx, idx + 2) == "--")) {
                pushStyle(SpanStyle(color = COMMENT_COLOR, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                append(line.substring(idx))
                pop()
                return
            }
            if (ch == '#' && !line.trimStart().startsWith("#include")) {
                pushStyle(SpanStyle(color = COMMENT_COLOR, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                append(line.substring(idx))
                pop()
                return
            }

            // 2. String Literals ("..." or '...')
            if (ch == '"' || ch == '\'') {
                val quote = ch
                val start = idx
                idx++
                while (idx < len && line[idx] != quote) {
                    if (line[idx] == '\\' && idx + 1 < len) {
                        idx += 2
                    } else {
                        idx++
                    }
                }
                if (idx < len) idx++ // consume closing quote
                val str = line.substring(start, idx)

                // In JSON, keys are strings followed by colon
                val isJsonKey = isJson && line.substring(idx).trimStart().startsWith(":")
                val color = if (isJsonKey) KEYWORD_COLOR else STRING_COLOR

                pushStyle(SpanStyle(color = color))
                append(str)
                pop()
                continue
            }

            // 3. Numbers
            if (ch.isDigit()) {
                val start = idx
                while (idx < len && (line[idx].isDigit() || line[idx] == '.' || line[idx] == 'f' || line[idx] == 'L' || line[idx] == 'x' || (line[idx] in 'a'..'f') || (line[idx] in 'A'..'F'))) {
                    idx++
                }
                pushStyle(SpanStyle(color = NUMBER_COLOR))
                append(line.substring(start, idx))
                pop()
                continue
            }

            // 4. Identifiers / Keywords / Types
            if (ch.isLetter() || ch == '_') {
                val start = idx
                while (idx < len && (line[idx].isLetterOrDigit() || line[idx] == '_')) {
                    idx++
                }
                val word = line.substring(start, idx)

                when {
                    COMMON_KEYWORDS.contains(word.lowercase()) -> {
                        pushStyle(SpanStyle(color = KEYWORD_COLOR, fontWeight = FontWeight.SemiBold))
                        append(word)
                        pop()
                    }
                    word.first().isUpperCase() -> {
                        // Types / Classes / Components (PascalCase)
                        pushStyle(SpanStyle(color = TYPE_COLOR))
                        append(word)
                        pop()
                    }
                    else -> {
                        pushStyle(SpanStyle(color = DEFAULT_TEXT_COLOR))
                        append(word)
                        pop()
                    }
                }
                continue
            }

            // 5. Operators & punctuation
            if ("{}[](),;:=+-*/%&|^!<>?".contains(ch)) {
                pushStyle(SpanStyle(color = OPERATOR_COLOR))
                append(ch)
                pop()
                idx++
                continue
            }

            // Default
            pushStyle(SpanStyle(color = DEFAULT_TEXT_COLOR))
            append(ch)
            pop()
            idx++
        }
    }
}
