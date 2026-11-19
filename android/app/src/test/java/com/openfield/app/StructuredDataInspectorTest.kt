package com.openfield.app

import com.openfield.app.feature.tools.data.StructuredDataInspector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredDataInspectorTest {

    @Test
    fun parseCsv_simpleCommaSeparated_parsesCorrectly() {
        val csv = """
            Name,Age,Score
            Alice,25,92.5
            Bob,30,88.0
            Charlie,22,95.5
        """.trimIndent()

        val table = StructuredDataInspector.parseCsv(csv)

        assertEquals(3, table.columnCount)
        assertEquals(listOf("Name", "Age", "Score"), table.headers)
        assertEquals(3, table.totalRows)
        assertEquals(listOf("Alice", "25", "92.5"), table.rows[0])
    }

    @Test
    fun parseCsv_withQuotesAndCommasInsideQuotes_handlesEscaping() {
        val csv = """
            Item,Description,Price
            Widget,"Small, blue widget",19.99
            Gadget,"Premium ""heavy"" gadget",49.50
        """.trimIndent()

        val table = StructuredDataInspector.parseCsv(csv)

        assertEquals(3, table.columnCount)
        assertEquals(2, table.totalRows)
        assertEquals("Small, blue widget", table.rows[0][1])
        assertEquals("Premium \"heavy\" gadget", table.rows[1][1])
    }

    @Test
    fun parseCsv_emptyText_returnsEmptyTable() {
        val table = StructuredDataInspector.parseCsv("")
        assertEquals(0, table.columnCount)
        assertEquals(0, table.totalRows)
        assertTrue(table.headers.isEmpty())
        assertTrue(table.rows.isEmpty())
    }

    @Test
    fun calculateColumnStats_numericColumn_computesAccurateStats() {
        val csv = """
            Product,Quantity,Price
            Apples,10,2.5
            Bananas,20,1.5
            Oranges,30,3.0
        """.trimIndent()

        val table = StructuredDataInspector.parseCsv(csv)
        val stats = StructuredDataInspector.calculateColumnStats(table, columnIndex = 2) // Price

        assertNotNull(stats)
        assertTrue(stats!!.isNumeric)
        assertEquals(3, stats.count)
        assertEquals(1.5, stats.min!!, 0.001)
        assertEquals(3.0, stats.max!!, 0.001)
        assertEquals(7.0, stats.sum!!, 0.001)
        assertEquals(7.0 / 3.0, stats.average!!, 0.001)
    }

    @Test
    fun calculateColumnStats_textColumn_returnsNonNumericStats() {
        val csv = """
            City,Population
            Paris,2161000
            Berlin,3645000
        """.trimIndent()

        val table = StructuredDataInspector.parseCsv(csv)
        val stats = StructuredDataInspector.calculateColumnStats(table, columnIndex = 0) // City

        assertNotNull(stats)
        assertFalse(stats!!.isNumeric)
        assertNull(stats.min)
        assertNull(stats.max)
        assertNull(stats.sum)
        assertNull(stats.average)
        assertEquals(2, stats.count)
    }

    @Test
    fun calculateColumnStats_invalidColumnIndex_returnsNull() {
        val csv = "A,B\n1,2"
        val table = StructuredDataInspector.parseCsv(csv)

        assertNull(StructuredDataInspector.calculateColumnStats(table, columnIndex = 5))
        assertNull(StructuredDataInspector.calculateColumnStats(table, columnIndex = -1))
    }
}
