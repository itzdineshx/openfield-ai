package com.openfield.app

import com.openfield.app.core.components.MermaidParser
import com.openfield.app.core.components.SvgParser
import com.openfield.app.core.text.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntaxHighlighterAndMermaidTest {

    @Test
    fun testSyntaxHighlighterTokenizesKotlin() {
        val code = """
            fun calculateEfficiency(input: Int): Double {
                val ratio = 12.5
                // Compute ratio
                return input * ratio
            }
        """.trimIndent()

        val highlighted = SyntaxHighlighter.highlight(code, "kotlin")
        assertTrue(highlighted.text.contains("fun calculateEfficiency"))
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }

    @Test
    fun testMermaidParserDetectsDiagrams() {
        val mermaid = """
            graph TD
                A[Start Engine] --> B[Check Oil Level]
                B -->|Low| C[Top Up 5W-30]
                B -->|Normal| D[Verify Coolant]
        """.trimIndent()

        assertTrue(MermaidParser.isMermaidCode(mermaid))
        val steps = MermaidParser.parseSteps(mermaid)
        assertEquals(3, steps.size)
        assertEquals("Start Engine", steps[0].fromNode)
        assertEquals("Check Oil Level", steps[0].toNode)
        assertEquals("Low", steps[1].edgeLabel)
        assertEquals("Top Up 5W-30", steps[1].toNode)
    }

    @Test
    fun testSvgParserDetectsSvgXml() {
        val svg = """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
                <circle cx="50" cy="50" r="40" stroke="green" stroke-width="4" fill="yellow" />
            </svg>
        """.trimIndent()

        assertTrue(SvgParser.isSvg(svg))
        assertFalse(SvgParser.isSvg("plain text without svg"))
        val extracted = SvgParser.extractSvg(svg)
        assertTrue(extracted.startsWith("<svg"))
        assertTrue(extracted.endsWith("</svg>"))
    }
}
