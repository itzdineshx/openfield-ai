package com.openfield.app.core.text

sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class BulletList(val items: List<MdListItem>) : MdBlock
    data class NumberedList(val items: List<MdListItem>) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Code(val text: String, val language: String?) : MdBlock
    data class Table(val header: List<String>, val rows: List<List<String>>) : MdBlock
    data class Callout(val kind: CalloutKind, val label: String, val text: String) : MdBlock
    data object Rule : MdBlock
}

data class MdListItem(val text: String, val depth: Int = 0, val number: Int = 0)

enum class CalloutKind { INFO, TIP, WARNING }

data class MdSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val strike: Boolean = false
)

object Markdown {

    private val headingRegex = Regex("^(#{1,6})\\s*(.+?)\\s*#*\\s*$")
    private val bulletRegex = Regex("^(\\s*)[-*+•●▪◦]\\s+(.*)$")
    private val numberedRegex = Regex("^(\\s*)(\\d{1,3})[.)]\\s+(.*)$")
    private val ruleRegex = Regex("^\\s*([-*_])(\\s*\\1){2,}\\s*$")
    private val fenceRegex = Regex("^\\s*(```|~~~)\\s*([\\w+-]*)\\s*$")
    private val tableSeparatorRegex = Regex("^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?\\s*$")
    private val calloutRegex = Regex(
        "^(?:\\*\\*|__)?(note|tip|warning|caution|important|safety|disclaimer)\\s*(?::\\s*(?:\\*\\*|__)?|(?:\\*\\*|__)\\s*:)\\s*(.*)$",
        RegexOption.IGNORE_CASE
    )
    private val htmlBreakRegex = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)

    fun parse(source: String): List<MdBlock> {
        val lines = source.replace("\r\n", "\n").replace(htmlBreakRegex, "\n").split('\n')
        val blocks = mutableListOf<MdBlock>()
        val paragraph = StringBuilder()
        var i = 0

        fun flushParagraph() {
            if (paragraph.isBlank()) {
                paragraph.clear()
                return
            }
            blocks.add(toParagraphOrCallout(paragraph.toString().trim()))
            paragraph.clear()
        }

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                flushParagraph()
                i++
                continue
            }

            val fence = fenceRegex.matchEntire(line)
            if (fence != null) {
                flushParagraph()
                val marker = fence.groupValues[1]
                val lang = fence.groupValues[2].ifBlank { null }
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith(marker)) {
                    if (code.isNotEmpty()) code.append('\n')
                    code.append(lines[i])
                    i++
                }
                i++
                blocks.add(MdBlock.Code(code.toString().trimEnd(), lang))
                continue
            }

            if (ruleRegex.matches(trimmed)) {
                flushParagraph()
                blocks.add(MdBlock.Rule)
                i++
                continue
            }

            val heading = headingRegex.matchEntire(trimmed)
            if (heading != null && !trimmed.startsWith("#!")) {
                flushParagraph()
                val text = heading.groupValues[2].trim()
                if (text.isNotEmpty()) {
                    blocks.add(MdBlock.Heading(heading.groupValues[1].length, text))
                }
                i++
                continue
            }

            if (trimmed.startsWith("|") && i + 1 < lines.size && tableSeparatorRegex.matches(lines[i + 1])) {
                flushParagraph()
                val header = splitTableRow(trimmed)
                val rows = mutableListOf<List<String>>()
                i += 2
                while (i < lines.size && lines[i].trim().startsWith("|")) {
                    rows.add(splitTableRow(lines[i].trim()))
                    i++
                }
                blocks.add(MdBlock.Table(header, rows))
                continue
            }

            if (trimmed.startsWith(">")) {
                flushParagraph()
                val quote = StringBuilder()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    if (quote.isNotEmpty()) quote.append('\n')
                    quote.append(lines[i].trim().removePrefix(">").trim())
                    i++
                }
                blocks.add(MdBlock.Quote(quote.toString()))
                continue
            }

            val isBullet = bulletRegex.matches(line) && !ruleRegex.matches(trimmed)
            val isNumbered = numberedRegex.matches(line)
            if (isBullet || isNumbered) {
                flushParagraph()
                val (block, next) = readList(lines, i, numbered = isNumbered)
                blocks.add(block)
                i = next
                continue
            }

            if (paragraph.isNotEmpty()) paragraph.append('\n')
            paragraph.append(trimmed)
            i++
        }
        flushParagraph()
        return blocks
    }

    private fun readList(lines: List<String>, start: Int, numbered: Boolean): Pair<MdBlock, Int> {
        val items = mutableListOf<MdListItem>()
        var i = start
        var baseIndent = -1

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                val nextIdx = (i + 1 until lines.size).firstOrNull { lines[it].isNotBlank() } ?: break
                val nextLine = lines[nextIdx]
                val continuesSameList = if (numbered) numberedRegex.matches(nextLine) else bulletRegex.matches(nextLine)
                val nestedOther = (leadingSpaces(nextLine) > maxOf(baseIndent, 0)) &&
                    (bulletRegex.matches(nextLine) || numberedRegex.matches(nextLine))
                if (continuesSameList || nestedOther) {
                    i = nextIdx
                    continue
                }
                break
            }

            val bullet = bulletRegex.matchEntire(line)
            val number = numberedRegex.matchEntire(line)
            val indent = leadingSpaces(line)
            if (baseIndent < 0) baseIndent = indent

            when {
                numbered && number != null && indent <= baseIndent -> {
                    items.add(MdListItem(number.groupValues[3].trim(), 0, number.groupValues[2].toIntOrNull() ?: (items.size + 1)))
                }
                !numbered && bullet != null && indent <= baseIndent -> {
                    items.add(MdListItem(bullet.groupValues[2].trim(), 0))
                }
                (bullet != null || number != null) && indent > baseIndent -> {
                    val content = bullet?.groupValues?.get(2) ?: number!!.groupValues[3]
                    val depth = ((indent - baseIndent) / 2).coerceIn(1, 3)
                    items.add(MdListItem(content.trim(), depth))
                }
                bullet != null || number != null -> break
                headingRegex.matches(trimmed) || fenceRegex.matches(line) || trimmed.startsWith("|") || trimmed.startsWith(">") -> break
                items.isNotEmpty() && (indent > baseIndent || !lines[i - 1].isBlank()) -> {
                    val last = items.removeAt(items.lastIndex)
                    items.add(last.copy(text = last.text + "\n" + trimmed))
                }
                else -> break
            }
            i++
        }

        if (!numbered) return MdBlock.BulletList(items) to i

        val topLevel = items.filter { it.depth == 0 }
        val sequential = topLevel.zipWithNext().all { (a, b) -> b.number == a.number + 1 }
        val renumbered = if (sequential) items else {
            var n = topLevel.firstOrNull()?.number?.coerceAtLeast(1) ?: 1
            items.map { if (it.depth == 0) it.copy(number = n++) else it }
        }
        return MdBlock.NumberedList(renumbered) to i
    }

    private fun toParagraphOrCallout(text: String): MdBlock {
        val firstLine = text.lineSequence().first()
        val match = calloutRegex.matchEntire(firstLine) ?: return MdBlock.Paragraph(text)
        val word = match.groupValues[1].lowercase()
        val rest = (listOf(match.groupValues[2]) + text.lines().drop(1)).joinToString("\n").trim()
        if (rest.isEmpty()) return MdBlock.Paragraph(text)
        val kind = when (word) {
            "warning", "caution", "safety", "important" -> CalloutKind.WARNING
            "tip" -> CalloutKind.TIP
            else -> CalloutKind.INFO
        }
        return MdBlock.Callout(kind, word.replaceFirstChar { it.uppercase() }, rest)
    }

    private fun splitTableRow(row: String): List<String> =
        row.trim().removePrefix("|").removeSuffix("|").split('|').map { it.trim() }

    private fun leadingSpaces(line: String): Int {
        var count = 0
        for (c in line) {
            when (c) {
                ' ' -> count++
                '\t' -> count += 4
                else -> return count
            }
        }
        return count
    }

    /**
     * Splits inline markdown into styled spans. Unclosed markers (common while a
     * response is still streaming) style the remaining text instead of leaking raw symbols.
     */
    fun parseInline(text: String): List<MdSpan> {
        val spans = mutableListOf<MdSpan>()
        val buf = StringBuilder()
        var bold = false
        var italic = false
        var strike = false
        var i = 0

        fun flush() {
            if (buf.isNotEmpty()) {
                spans.add(MdSpan(buf.toString(), bold = bold, italic = italic, strike = strike))
                buf.clear()
            }
        }

        while (i < text.length) {
            val c = text[i]
            val next = text.getOrNull(i + 1)
            val prev = text.getOrNull(i - 1)

            if (c == '\\' && next != null && next in "*_`~#[]()\\") {
                buf.append(next)
                i += 2
                continue
            }

            if (c == '`') {
                val end = text.indexOf('`', i + 1)
                if (end > i + 1) {
                    flush()
                    spans.add(MdSpan(text.substring(i + 1, end), code = true))
                    i = end + 1
                    continue
                }
                i++
                continue
            }

            if (c == '[') {
                val close = text.indexOf("](", i + 1)
                val end = if (close > 0) text.indexOf(')', close + 2) else -1
                if (close > i && end > close) {
                    buf.append(text, i + 1, close)
                    i = end + 1
                    continue
                }
            }

            if (c == '~' && next == '~') {
                flush()
                strike = !strike
                i += 2
                continue
            }

            if ((c == '*' || c == '_') && next == c) {
                val tripled = text.getOrNull(i + 2) == c
                if (c == '_' && prev != null && prev.isLetterOrDigit()) {
                    buf.append(c)
                    i++
                    continue
                }
                flush()
                bold = !bold
                if (tripled) italic = !italic
                i += if (tripled) 3 else 2
                continue
            }

            if (c == '*' || c == '_') {
                val opensItalic = !italic && next != null && !next.isWhitespace() &&
                    (c == '*' || prev == null || !prev.isLetterOrDigit()) &&
                    hasClosingMarker(text, i + 1, c)
                val closesItalic = italic && prev != null && !prev.isWhitespace() &&
                    (c == '*' || next == null || !next.isLetterOrDigit())
                if (opensItalic || closesItalic) {
                    flush()
                    italic = !italic
                    i++
                    continue
                }
            }

            buf.append(c)
            i++
        }
        flush()
        return spans
    }

    private fun hasClosingMarker(text: String, from: Int, marker: Char): Boolean {
        var j = from
        while (j < text.length) {
            if (text[j] == marker && text.getOrNull(j + 1) != marker && !text[j - 1].isWhitespace()) return true
            if (text[j] == '\n') return false
            j++
        }
        return false
    }

    /** Inline markdown removed — for titles, chips, previews, notifications. */
    fun stripInline(text: String): String =
        parseInline(text.trim())
            .joinToString("") { it.text }
            .replace(Regex("^#{1,6}\\s*"), "")
            .replace(Regex("^[-*+•]\\s+"), "")
            .trim()

    /** Readable plain text for copy, share, previews and text-to-speech. */
    fun toPlainText(source: String): String {
        val out = StringBuilder()
        parse(source).forEach { block ->
            if (out.isNotEmpty()) out.append("\n\n")
            when (block) {
                is MdBlock.Heading -> out.append(stripInline(block.text))
                is MdBlock.Paragraph -> out.append(stripInline(block.text))
                is MdBlock.Quote -> out.append(stripInline(block.text))
                is MdBlock.Code -> out.append(block.text)
                is MdBlock.Callout -> out.append(block.label).append(": ").append(stripInline(block.text))
                is MdBlock.Rule -> out.append("—")
                is MdBlock.BulletList -> out.append(block.items.joinToString("\n") {
                    "  ".repeat(it.depth) + "• " + stripInline(it.text)
                })
                is MdBlock.NumberedList -> out.append(block.items.joinToString("\n") {
                    if (it.depth == 0) "${it.number}. ${stripInline(it.text)}"
                    else "  ".repeat(it.depth) + "• " + stripInline(it.text)
                })
                is MdBlock.Table -> {
                    out.append(block.header.joinToString(" | ") { stripInline(it) })
                    block.rows.forEach { row -> out.append('\n').append(row.joinToString(" | ") { stripInline(it) }) }
                }
            }
        }
        return out.toString()
    }

    /** One-line preview for lists (session previews, recent drafts). */
    fun preview(source: String, maxChars: Int = 80): String {
        val flat = toPlainText(source).replace(Regex("\\s+"), " ").trim()
        return if (flat.length > maxChars) flat.take(maxChars).trimEnd() + "…" else flat
    }
}
