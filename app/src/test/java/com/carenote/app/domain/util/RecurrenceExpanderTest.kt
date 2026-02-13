package com.carenote.app.domain.util

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.RecurrenceFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class RecurrenceExpanderTest {

    private val baseDate = LocalDate.of(2025, 4, 1)
    private val fixedDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)

    private fun createEvent(
        frequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
        interval: Int = 1,
        date: LocalDate = baseDate
    ) = CalendarEvent(
        id = 1L,
        title = "テストイベント",
        date = date,
        recurrenceFrequency = frequency,
        recurrenceInterval = interval,
        createdAt = fixedDateTime,
        updatedAt = fixedDateTime
    )

    @Test
    fun `NONE frequency returns event if in range`() {
        val event = createEvent(RecurrenceFrequency.NONE)
        val result = RecurrenceExpander.expand(event, baseDate, baseDate)
        assertEquals(1, result.size)
        assertEquals(event, result[0])
    }

    @Test
    fun `NONE frequency returns empty if out of range`() {
        val event = createEvent(RecurrenceFrequency.NONE)
        val result = RecurrenceExpander.expand(
            event,
            baseDate.plusDays(1),
            baseDate.plusDays(10)
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `DAILY with interval 1 expands correctly`() {
        val event = createEvent(RecurrenceFrequency.DAILY, 1)
        val rangeStart = baseDate
        val rangeEnd = baseDate.plusDays(6)
        val result = RecurrenceExpander.expand(event, rangeStart, rangeEnd)
        assertEquals(7, result.size)
        assertEquals(baseDate, result[0].date)
        assertEquals(baseDate.plusDays(1), result[1].date)
        assertEquals(baseDate.plusDays(6), result[6].date)
    }

    @Test
    fun `DAILY with interval 3 expands correctly`() {
        val event = createEvent(RecurrenceFrequency.DAILY, 3)
        val result = RecurrenceExpander.expand(event, baseDate, baseDate.plusDays(9))
        assertEquals(4, result.size) // days 0, 3, 6, 9
        assertEquals(baseDate, result[0].date)
        assertEquals(baseDate.plusDays(3), result[1].date)
        assertEquals(baseDate.plusDays(6), result[2].date)
        assertEquals(baseDate.plusDays(9), result[3].date)
    }

    @Test
    fun `WEEKLY with interval 1 expands correctly`() {
        val event = createEvent(RecurrenceFrequency.WEEKLY, 1)
        val result = RecurrenceExpander.expand(event, baseDate, baseDate.plusWeeks(3))
        assertEquals(4, result.size) // weeks 0, 1, 2, 3
        assertEquals(baseDate, result[0].date)
        assertEquals(baseDate.plusWeeks(1), result[1].date)
        assertEquals(baseDate.plusWeeks(2), result[2].date)
        assertEquals(baseDate.plusWeeks(3), result[3].date)
    }

    @Test
    fun `WEEKLY with interval 2 expands correctly`() {
        val event = createEvent(RecurrenceFrequency.WEEKLY, 2)
        val result = RecurrenceExpander.expand(event, baseDate, baseDate.plusWeeks(6))
        assertEquals(4, result.size) // weeks 0, 2, 4, 6
        assertEquals(baseDate, result[0].date)
        assertEquals(baseDate.plusWeeks(2), result[1].date)
        assertEquals(baseDate.plusWeeks(4), result[2].date)
        assertEquals(baseDate.plusWeeks(6), result[3].date)
    }

    @Test
    fun `MONTHLY with interval 1 expands correctly`() {
        val event = createEvent(RecurrenceFrequency.MONTHLY, 1)
        val result = RecurrenceExpander.expand(event, baseDate, baseDate.plusMonths(2))
        assertEquals(3, result.size) // months 0, 1, 2
        assertEquals(baseDate, result[0].date)
        assertEquals(baseDate.plusMonths(1), result[1].date)
        assertEquals(baseDate.plusMonths(2), result[2].date)
    }

    @Test
    fun `events before range start are skipped`() {
        val event = createEvent(RecurrenceFrequency.DAILY, 1, date = baseDate.minusDays(5))
        val rangeStart = baseDate
        val rangeEnd = baseDate.plusDays(2)
        val result = RecurrenceExpander.expand(event, rangeStart, rangeEnd)
        assertEquals(3, result.size)
        assertEquals(baseDate, result[0].date)
        assertEquals(baseDate.plusDays(1), result[1].date)
        assertEquals(baseDate.plusDays(2), result[2].date)
    }

    @Test
    fun `event date after range end returns empty`() {
        val event = createEvent(RecurrenceFrequency.DAILY, 1, date = baseDate.plusDays(10))
        val result = RecurrenceExpander.expand(event, baseDate, baseDate.plusDays(5))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `expanded events preserve original event fields`() {
        val event = createEvent(RecurrenceFrequency.DAILY, 1)
        val result = RecurrenceExpander.expand(event, baseDate, baseDate.plusDays(1))
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(event.id, it.id)
            assertEquals(event.title, it.title)
            assertEquals(event.recurrenceFrequency, it.recurrenceFrequency)
            assertEquals(event.recurrenceInterval, it.recurrenceInterval)
        }
    }

    @Test
    fun `MAX_EXPANDED_OCCURRENCES limit is respected`() {
        val event = createEvent(RecurrenceFrequency.DAILY, 1)
        val farFutureEnd = baseDate.plusDays(1000)
        val result = RecurrenceExpander.expand(event, baseDate, farFutureEnd)
        assertEquals(AppConfig.Calendar.MAX_EXPANDED_OCCURRENCES, result.size)
    }
}
