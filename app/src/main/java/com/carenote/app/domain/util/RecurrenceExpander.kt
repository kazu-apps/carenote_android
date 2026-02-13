package com.carenote.app.domain.util

import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.RecurrenceFrequency
import java.time.LocalDate

object RecurrenceExpander {

    fun expand(
        event: CalendarEvent,
        rangeStart: LocalDate,
        rangeEnd: LocalDate
    ): List<CalendarEvent> {
        if (event.recurrenceFrequency == RecurrenceFrequency.NONE) {
            return if (event.date in rangeStart..rangeEnd) listOf(event) else emptyList()
        }

        val result = mutableListOf<CalendarEvent>()
        var currentDate = event.date
        var count = 0

        while (currentDate <= rangeEnd && count < AppConfig.Calendar.MAX_EXPANDED_OCCURRENCES) {
            if (currentDate >= rangeStart) {
                result.add(event.copy(date = currentDate))
            }
            currentDate = nextDate(currentDate, event.recurrenceFrequency, event.recurrenceInterval)
            count++
        }

        return result
    }

    private fun nextDate(
        current: LocalDate,
        frequency: RecurrenceFrequency,
        interval: Int
    ): LocalDate {
        return when (frequency) {
            RecurrenceFrequency.DAILY -> current.plusDays(interval.toLong())
            RecurrenceFrequency.WEEKLY -> current.plusWeeks(interval.toLong())
            RecurrenceFrequency.MONTHLY -> current.plusMonths(interval.toLong())
            RecurrenceFrequency.NONE -> current
        }
    }
}
