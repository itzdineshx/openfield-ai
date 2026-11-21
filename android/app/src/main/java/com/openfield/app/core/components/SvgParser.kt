package com.openfield.app.core.components

object SvgParser {
    fun isSvg(text: String): Boolean {
        val trimmed = text.trim()
        return (trimmed.startsWith("<svg", ignoreCase = true) ||
                trimmed.contains("<svg ", ignoreCase = true) ||
                trimmed.contains("xmlns=\"http://www.w3.org/2000/svg\"", ignoreCase = true)) &&
                trimmed.contains("</svg>", ignoreCase = true)
    }

    fun extractSvg(text: String): String {
        val start = text.indexOf("<svg", ignoreCase = true)
        val end = text.indexOf("</svg>", ignoreCase = true)
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 6)
        }
        return text
    }
}
