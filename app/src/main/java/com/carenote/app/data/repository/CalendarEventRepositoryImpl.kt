package com.carenote.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.repository.ActiveCareRecipientProvider
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.util.RecurrenceExpander
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CalendarEventRepositoryImpl @Inject constructor(
    private val calendarEventDao: CalendarEventDao,
    private val mapper: CalendarEventMapper,
    private val activeRecipientProvider: ActiveCareRecipientProvider
) : CalendarEventRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getAllEvents(): Flow<List<CalendarEvent>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            calendarEventDao.getAllEvents(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getEventById(id: Long): Flow<CalendarEvent?> {
        return calendarEventDao.getEventById(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }

    override fun getEventsByDate(date: LocalDate): Flow<List<CalendarEvent>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            calendarEventDao.getAllEvents(recipientId)
        }.map { entities ->
            val allEvents = mapper.toDomainList(entities)
            allEvents.flatMap { event ->
                RecurrenceExpander.expand(event, date, date)
            }
        }
    }

    override fun getEventsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CalendarEvent>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            calendarEventDao.getAllEvents(recipientId)
        }.map { entities ->
            val allEvents = mapper.toDomainList(entities)
            allEvents.flatMap { event ->
                RecurrenceExpander.expand(event, startDate, endDate)
            }.sortedWith(compareBy({ it.date }, { it.startTime }))
        }
    }

    override suspend fun insertEvent(event: CalendarEvent): Result<Long, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to insert calendar event", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            calendarEventDao.insertEvent(mapper.toEntity(event).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun updateEvent(event: CalendarEvent): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to update calendar event", it) }
        ) {
            val recipientId = activeRecipientProvider.getActiveCareRecipientId()
            calendarEventDao.updateEvent(mapper.toEntity(event).copy(careRecipientId = recipientId))
        }
    }

    override suspend fun deleteEvent(id: Long): Result<Unit, DomainError> {
        return Result.catchingSuspend(
            errorTransform = { DomainError.DatabaseError("Failed to delete calendar event", it) }
        ) {
            calendarEventDao.deleteEvent(id)
        }
    }

    override fun getTaskEvents(): Flow<List<CalendarEvent>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            calendarEventDao.getTaskEvents(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getIncompleteTaskEvents(): Flow<List<CalendarEvent>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            calendarEventDao.getIncompleteTaskEvents(recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getTaskEventsByDate(date: LocalDate): Flow<List<CalendarEvent>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            calendarEventDao.getTaskEventsByDate(date.format(dateFormatter), recipientId)
        }.map { entities -> mapper.toDomainList(entities) }
    }

    override fun getPagedTaskEvents(query: String): Flow<PagingData<CalendarEvent>> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            Pager(
                config = PagingConfig(pageSize = 20)
            ) {
                calendarEventDao.getPagedTaskEvents(query, recipientId)
            }.flow
        }.map { pagingData -> pagingData.map { mapper.toDomain(it) } }
    }

    override fun getIncompleteTaskCount(): Flow<Int> {
        return activeRecipientProvider.activeCareRecipientId.flatMapLatest { recipientId ->
            calendarEventDao.getIncompleteTaskCount(recipientId)
        }
    }
}
