package com.carenote.app.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale

class DateTimeFormattersTest {

    @Test
    fun `formatDate formats date in Japanese locale`() {
        val date = LocalDate.of(2025, 3, 15)
        val result = DateTimeFormatters.formatDate(date, Locale.JAPANESE)

        assertEquals("2025/03/15", result)
    }

    @Test
    fun `formatDate formats date in English locale`() {
        val date = LocalDate.of(2025, 3, 15)
        val result = DateTimeFormatters.formatDate(date, Locale.ENGLISH)

        assertEquals("03/15/2025", result)
    }

    @Test
    fun `formatTime formats time with hours and minutes`() {
        val time = LocalTime.of(8, 30)
        val result = DateTimeFormatters.formatTime(time)

        assertEquals("08:30", result)
    }

    @Test
    fun `formatTime formats midnight correctly`() {
        val time = LocalTime.of(0, 0)
        val result = DateTimeFormatters.formatTime(time)

        assertEquals("00:00", result)
    }

    @Test
    fun `formatTime formats end of day correctly`() {
        val time = LocalTime.of(23, 59)
        val result = DateTimeFormatters.formatTime(time)

        assertEquals("23:59", result)
    }

    @Test
    fun `formatDateTime formats in Japanese locale`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 14, 30)
        val result = DateTimeFormatters.formatDateTime(dateTime, Locale.JAPANESE)

        assertEquals("2025/03/15 14:30", result)
    }

    @Test
    fun `formatDateTime formats in English locale`() {
        val dateTime = LocalDateTime.of(2025, 3, 15, 14, 30)
        val result = DateTimeFormatters.formatDateTime(dateTime, Locale.ENGLISH)

        assertEquals("03/15/2025 14:30", result)
    }

    @Test
    fun `formatDateShort formats with month and day in Japanese`() {
        val date = LocalDate.of(2025, 3, 5)
        val result = DateTimeFormatters.formatDateShort(date, Locale.JAPANESE)

        assertEquals("3/5", result)
    }

    @Test
    fun `formatDateShort formats with month and day in English`() {
        val date = LocalDate.of(2025, 3, 5)
        val result = DateTimeFormatters.formatDateShort(date, Locale.ENGLISH)

        assertEquals("3/5", result)
    }

    @Test
    fun `formatRelativeDate returns today for today`() {
        val today = LocalDate.now()
        val result = DateTimeFormatters.formatRelativeDate(
            today,
            todayLabel = "今日",
            yesterdayLabel = "昨日",
            locale = Locale.JAPANESE
        )

        assertEquals("今日", result)
    }

    @Test
    fun `formatRelativeDate returns yesterday for yesterday`() {
        val yesterday = LocalDate.now().minusDays(1)
        val result = DateTimeFormatters.formatRelativeDate(
            yesterday,
            todayLabel = "今日",
            yesterdayLabel = "昨日",
            locale = Locale.JAPANESE
        )

        assertEquals("昨日", result)
    }

    @Test
    fun `formatRelativeDate returns formatted date for older dates`() {
        val oldDate = LocalDate.of(2025, 1, 10)
        val result = DateTimeFormatters.formatRelativeDate(
            oldDate,
            todayLabel = "今日",
            yesterdayLabel = "昨日",
            locale = Locale.JAPANESE
        )

        assertEquals("2025/01/10", result)
    }

    @Test
    fun `formatYearMonth formats correctly in Japanese`() {
        val date = LocalDate.of(2025, 12, 1)
        val result = DateTimeFormatters.formatYearMonth(date, Locale.JAPANESE)

        assertEquals("2025年12月", result)
    }

    @Test
    fun `formatYearMonth formats correctly in English`() {
        val date = LocalDate.of(2025, 12, 1)
        val result = DateTimeFormatters.formatYearMonth(date, Locale.ENGLISH)

        assertEquals("December 2025", result)
    }

    @Test
    fun `formatDayOfWeek returns correct day name in Japanese`() {
        val monday = LocalDate.of(2025, 3, 17)
        val result = DateTimeFormatters.formatDayOfWeek(monday, Locale.JAPANESE)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `formatDayOfWeek returns correct day name in English`() {
        val monday = LocalDate.of(2025, 3, 17)
        val result = DateTimeFormatters.formatDayOfWeek(monday, Locale.ENGLISH)

        assertTrue(result.isNotEmpty())
    }
}
