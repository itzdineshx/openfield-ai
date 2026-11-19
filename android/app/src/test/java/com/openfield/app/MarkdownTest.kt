package com.openfield.app

import com.openfield.app.core.text.CalloutKind
import com.openfield.app.core.text.Markdown
import com.openfield.app.core.text.MdBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    @Test
    fun parse_typicalModelAnswer_producesStructuredBlocks() {
        val raw = """
## Why it happens
This is a **Windows Blue Screen** (BSOD).

**Try these steps:**
1. Restart your laptop
2. Note the error code
3. Boot into *Safe Mode*

- Driver issue
- Hardware fault

Note: Back up your files first.
""".trimIndent()

        val blocks = Markdown.parse(raw)
        assertTrue(blocks[0] is MdBlock.Heading)
        assertEquals("Why it happens", (blocks[0] as MdBlock.Heading).text)
        assertTrue(blocks.any { it is MdBlock.NumberedList && it.items.size == 3 })
        assertTrue(blocks.any { it is MdBlock.BulletList && it.items.size == 2 })
        val callout = blocks.filterIsInstance<MdBlock.Callout>().single()
        assertEquals(CalloutKind.INFO, callout.kind)
        assertEquals("Back up your files first.", callout.text)
    }

    @Test
    fun parse_numberedListWithBlankLinesAndRepeatedNumbers_isOneSequentialList() {
        val raw = "1. First\n\n1. Second\n\n1. Third"
        val list = Markdown.parse(raw).single() as MdBlock.NumberedList
        assertEquals(listOf(1, 2, 3), list.items.map { it.number })
    }

    @Test
    fun parse_nestedBulletsUnderNumberedItem() {
        val raw = "1. Check power\n   - Cable plugged in\n   - Outlet works\n2. Restart"
        val list = Markdown.parse(raw).single() as MdBlock.NumberedList
        assertEquals(4, list.items.size)
        assertEquals(1, list.items[1].depth)
        assertEquals(2, list.items[3].number)
    }

    @Test
    fun parse_tableAndCodeAndRule() {
        val raw = "| Part | Use |\n|---|---|\n| Anode | Negative |\n\n```\ncode here\n```\n\n---"
        val blocks = Markdown.parse(raw)
        val table = blocks[0] as MdBlock.Table
        assertEquals(listOf("Part", "Use"), table.header)
        assertEquals(listOf("Anode", "Negative"), table.rows.single())
        assertEquals("code here", (blocks[1] as MdBlock.Code).text)
        assertEquals(MdBlock.Rule, blocks[2])
    }

    @Test
    fun parse_starRuleIsNotABullet() {
        assertEquals(listOf(MdBlock.Rule), Markdown.parse("* * *"))
    }

    @Test
    fun parse_noteWithoutColonIsPlainParagraph() {
        assertTrue(Markdown.parse("Note that the fuse may be fine: check it").single() is MdBlock.Paragraph)
    }

    @Test
    fun parseInline_boldItalicCode() {
        val spans = Markdown.parseInline("A **bold** and *italic* with `code`")
        assertEquals("A bold and italic with code", spans.joinToString("") { it.text })
        assertTrue(spans.single { it.text == "bold" }.bold)
        assertTrue(spans.single { it.text == "italic" }.italic)
        assertTrue(spans.single { it.text == "code" }.code)
    }

    @Test
    fun parseInline_unclosedBoldWhileStreaming_hidesMarkers() {
        val spans = Markdown.parseInline("This is **important text that is still arriv")
        assertFalse(spans.joinToString("") { it.text }.contains("*"))
        assertTrue(spans.last().bold)
    }

    @Test
    fun parseInline_keepsLiteralAsteriskInMath() {
        assertEquals("5 * 3 = 15", Markdown.parseInline("5 * 3 = 15").joinToString("") { it.text })
    }

    @Test
    fun parseInline_snakeCaseUnderscoresAreKept() {
        assertEquals("use file_name_here", Markdown.parseInline("use file_name_here").joinToString("") { it.text })
    }

    @Test
    fun parseInline_linkShowsLabelOnly() {
        assertEquals("see the guide", Markdown.parseInline("see [the guide](https://example.com)").joinToString("") { it.text })
    }

    @Test
    fun toPlainText_removesAllMarkup() {
        val plain = Markdown.toPlainText("## Title\n\n**Bold** text\n\n1. One\n2. Two\n\n- a\n- b")
        assertFalse(plain.contains("#"))
        assertFalse(plain.contains("**"))
        assertTrue(plain.contains("1. One"))
        assertTrue(plain.contains("• a"))
    }

    @Test
    fun preview_isSingleLineAndTruncated() {
        val preview = Markdown.preview("## Heading\n\nSome **long** answer text that keeps going and going", 20)
        assertFalse(preview.contains("\n"))
        assertFalse(preview.contains("#"))
        assertTrue(preview.endsWith("…"))
    }
}
