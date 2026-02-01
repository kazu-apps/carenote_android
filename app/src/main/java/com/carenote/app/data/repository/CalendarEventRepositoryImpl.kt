package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.repository.CalendarEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarEventRepositoryImpl @Inject constructor(
    private val calendarEventDao: CalendarEventDao,
    private val mapper: CalendarEventMapper
) : CalendarEventRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getAllEvents(): Flow<List<CalendarEvent>> {
        return calendarEventDao.getAllEvents().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getEventById(id: Long): Flow<CalendarEvent?> {
        return calendarEventDao.getEventById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getEventsByDate(date: LocalDate): Flow<List<CalendarEvent>> {
        return calendarEventDao.getEventsByDate(
            date = date.format(dateFormatter)
        ).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getEventsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CalendarEvent>> {
        return calendarEventDao.getEventsByDateRange(
            startDate = startDate.format(dateFormatter),
            endDate = endDate.format(dateFormatter)
        ).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun insertEvent(event: CalendarEvent): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert calendar event", it) }
        ) {
            calendarEventDao.insertEvent(mapper.toEntity(event))
        }
    }

    override suspend fun updateEvent(event: CalendarEvent): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update calendar event", it) }
        ) {
            calendarEventDao.updateEvent(mapper.toEntity(event))
        }
    }

    override suspend fun deleteEvent(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete calendar event", it) }
        ) {
            calendarEventDao.deleteEvent(id)
        }
    }
}
