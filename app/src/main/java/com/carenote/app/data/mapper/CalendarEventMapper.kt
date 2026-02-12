package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.CalendarEventEntity
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarEventMapper @Inject constructor() : Mapper<CalendarEventEntity, CalendarEvent> {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    override fun toDomain(entity: CalendarEventEntity): CalendarEvent {
        return CalendarEvent(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            date = LocalDate.parse(entity.date, dateFormatter),
            startTime = entity.startTime?.let { LocalTime.parse(it, timeFormatter) },
            endTime = entity.endTime?.let { LocalTime.parse(it, timeFormatter) },
            isAllDay = entity.isAllDay == 1,
            type = CalendarEventType.valueOf(entity.type),
            completed = entity.completed == 1,
            createdAt = LocalDateTime.parse(entity.createdAt, dateTimeFormatter),
            updatedAt = LocalDateTime.parse(entity.updatedAt, dateTimeFormatter)
        )
    }

    override fun toEntity(domain: CalendarEvent): CalendarEventEntity {
        return CalendarEventEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            date = domain.date.format(dateFormatter),
            startTime = domain.startTime?.format(timeFormatter),
            endTime = domain.endTime?.format(timeFormatter),
            isAllDay = if (domain.isAllDay) 1 else 0,
            type = domain.type.name,
            completed = if (domain.completed) 1 else 0,
            createdAt = domain.createdAt.format(dateTimeFormatter),
            updatedAt = domain.updatedAt.format(dateTimeFormatter)
        )
    }
}
