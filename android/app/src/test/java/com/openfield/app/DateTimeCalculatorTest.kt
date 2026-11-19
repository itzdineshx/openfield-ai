package com.openfield.app

import com.openfield.app.feature.tools.datetime.DateTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DateTimeCalculatorTest {

    @Test
    fun parseDate_validIsoDate_returnsLocalDate() {
        val date = DateTimeCalculator.parseDate("2026-11-20")
        assertNotNull(date)
        assertEquals(2026, date?.year)
        assertEquals(11, date?.monthValue)
        assertEquals(20, date?.dayOfMonth)
    }

    @Test
    fun parseDate_invalidString_returnsNull() {
        assertNull(DateTimeCalculator.parseDate("not-a-date"))
        assertNull(DateTimeCalculator.parseDate(""))
    }

    @Test
    fun calculateDifference_positiveFutureDate() {
        val start = LocalDate.of(2026, 11, 2) // Monday
        val end = LocalDate.of(2026, 11, 16)  // Monday (14 days = 2 weeks)
        val result = DateTimeCalculator.calculateDifference(start, end)

        assertEquals(14L, result.totalDays)
        assertEquals(10L, result.businessDays) // 2 full work weeks = 10 business days
        assertFalse(result.isPast)
        assertTrue(result.weeksAndDays.contains("2 weeks"))
    }

    @Test
    fun calculateDifference_negativePastDate() {
        val start = LocalDate.of(2026, 11, 20)
        val end = LocalDate.of(2026, 11, 15)
        val result = DateTimeCalculator.calculateDifference(start, end)

        assertEquals(-5L, result.totalDays)
        assertTrue(result.isPast)
        assertTrue(result.businessDays < 0)
    }

    @Test
    fun addDuration_addsDaysMonthsYearsCorrectly() {
        val start = LocalDate.of(2026, 1, 15)
        val result = DateTimeCalculator.addDuration(start, days = 10, months = 2, years = 1)

        assertEquals(LocalDate.of(2027, 3, 25), result)
    }

    @Test
    fun subtractDuration_subtractsDaysMonthsYearsCorrectly() {
        val start = LocalDate.of(2026, 3, 25)
        val result = DateTimeCalculator.subtractDuration(start, days = 10, months = 2, years = 1)

        assertEquals(LocalDate.of(2025, 1, 15), result)
    }

    @Test
    fun getUpcomingDayOfWeek_returnsNextMatchingDay() {
        val friday = LocalDate.of(2026, 11, 20) // Friday
        val nextMonday = DateTimeCalculator.getUpcomingDayOfWeek(DayOfWeek.MONDAY, friday)

        assertEquals(DayOfWeek.MONDAY, nextMonday.dayOfWeek)
        assertEquals(LocalDate.of(2026, 11, 23), nextMonday)
    }
}
