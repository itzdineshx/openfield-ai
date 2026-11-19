package com.openfield.app.feature.tools.data

import java.util.Locale

data class CsvColumnStats(
    val columnName: String,
    val count: Int,
    val nonNullCount: Int,
    val isNumeric: Boolean,
    val min: Double? = null,
    val max: Double? = null,
    val sum: Double? = null,
    val average: Double? = null
)

data class ParsedCsvTable(
    val headers: List<String>,
    val rows: List<List<String>>,
    val totalRows: Int,
    val columnCount: Int
)

/**
 * Deterministic offline CSV table parser and statistical inspector. Zero LLM dependency.
 */
object StructuredDataInspector {

    fun parseCsv(csvText: String, maxRows: Int = 1000): ParsedCsvTable {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return ParsedCsvTable(emptyList(), emptyList(), 0, 0)
        }

        val parsedLines = lines.map { parseCsvLine(it) }
        val headers = parsedLines.first()
        val dataRows = parsedLines.drop(1).take(maxRows)

        return ParsedCsvTable(
            headers = headers,
            rows = dataRows,
            totalRows = parsedLines.size - 1,
            columnCount = headers.size
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun calculateColumnStats(table: ParsedCsvTable, columnIndex: Int): CsvColumnStats? {
        if (columnIndex !in table.headers.indices) return null
        val colName = table.headers[columnIndex]
        val values = table.rows.mapNotNull { it.getOrNull(columnIndex)?.trim() }

        val numbers = values.mapNotNull { it.toDoubleOrNull() }
        val isNumeric = numbers.isNotEmpty() && numbers.size >= (values.size * 0.7) // At least 70% numeric

        return if (isNumeric && numbers.isNotEmpty()) {
            val min = numbers.minOrNull() ?: 0.0
            val max = numbers.maxOrNull() ?: 0.0
            val sum = numbers.sum()
            val avg = sum / numbers.size
            CsvColumnStats(
                columnName = colName,
                count = values.size,
                nonNullCount = values.count { it.isNotEmpty() },
                isNumeric = true,
                min = min,
                max = max,
                sum = sum,
                average = avg
            )
        } else {
            CsvColumnStats(
                columnName = colName,
                count = values.size,
                nonNullCount = values.count { it.isNotEmpty() },
                isNumeric = false
            )
        }
    }
}
