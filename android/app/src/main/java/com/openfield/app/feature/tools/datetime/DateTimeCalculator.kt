package com.openfield.app.feature.tools.datetime

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class DateDifferenceResult(
    val totalDays: Long,
    val weeksAndDays: String,
    val businessDays: Long,
    val isPast: Boolean
)

/**
 * Deterministic offline date, calendar, and time calculations using standard java.time APIs.
 * Zero LLM dependency.
 */
object DateTimeCalculator {

    private val STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    fun parseDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr.trim(), STANDARD_FORMATTER)
        } catch (e: Exception) {
            try {
                LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun formatDate(date: LocalDate): String = date.format(DISPLAY_FORMATTER)

    fun calculateDifference(startDate: LocalDate, endDate: LocalDate): DateDifferenceResult {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate)
        val absDays = kotlin.math.abs(totalDays)
        val weeks = absDays / 7
        val remDays = absDays % 7
        val weeksDaysStr = if (weeks > 0) "$weeks weeks, $remDays days" else "$remDays days"

        // Calculate business days (Monday to Friday)
        val minDate = if (startDate.isBefore(endDate)) startDate else endDate
        val maxDate = if (startDate.isBefore(endDate)) endDate else startDate
        var businessDays = 0L
        var cur = minDate
        while (!cur.isEqual(maxDate)) {
            val dow = cur.dayOfWeek
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                businessDays++
            }
            cur = cur.plusDays(1)
        }

        return DateDifferenceResult(
            totalDays = totalDays,
            weeksAndDays = weeksDaysStr,
            businessDays = if (totalDays < 0) -businessDays else businessDays,
            isPast = totalDays < 0
        )
    }

    fun addDuration(startDate: LocalDate, days: Long, months: Long = 0, years: Long = 0): LocalDate {
        return startDate.plusYears(years).plusMonths(months).plusDays(days)
    }

    fun subtractDuration(startDate: LocalDate, days: Long, months: Long = 0, years: Long = 0): LocalDate {
        return startDate.minusYears(years).minusMonths(months).minusDays(days)
    }

    fun getUpcomingDayOfWeek(dayOfWeek: DayOfWeek, fromDate: LocalDate = LocalDate.now()): LocalDate {
        var d = fromDate
        while (d.dayOfWeek != dayOfWeek || d.isEqual(fromDate)) {
            d = d.plusDays(1)
        }
        return d
    }
}
