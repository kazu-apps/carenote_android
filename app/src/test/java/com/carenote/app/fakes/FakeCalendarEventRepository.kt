package com.carenote.app.fakes

import androidx.paging.PagingData
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.repository.CalendarEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeCalendarEventRepository : CalendarEventRepository {

    private val events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    private var nextId = 1L
    var shouldFail = false

    fun setEvents(list: List<CalendarEvent>) {
        events.value = list
    }

    fun clear() {
        events.value = emptyList()
        nextId = 1L
        shouldFail = false
    }

    override fun getAllEvents(): Flow<List<CalendarEvent>> = events

    override fun getEventById(id: Long): Flow<CalendarEvent?> {
        return events.map { list -> list.find { it.id == id } }
    }

    override fun getEventsByDate(date: LocalDate): Flow<List<CalendarEvent>> {
        return events.map { list -> list.filter { it.date == date } }
    }

    override fun getEventsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<CalendarEvent>> {
        return events.map { list ->
            list.filter { event -> event.date in startDate..endDate }
        }
    }

    override suspend fun insertEvent(event: CalendarEvent): Result<Long, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake insert error"))
        }
        val id = nextId++
        val newEvent = event.copy(id = id)
        events.value = events.value + newEvent
        return Result.Success(id)
    }

    override suspend fun updateEvent(event: CalendarEvent): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake update error"))
        }
        events.value = events.value.map {
            if (it.id == event.id) event else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteEvent(id: Long): Result<Unit, DomainError> {
        if (shouldFail) {
            return Result.Failure(DomainError.DatabaseError("Fake delete error"))
        }
        events.value = events.value.filter { it.id != id }
        return Result.Success(Unit)
    }

    override fun getTaskEvents(): Flow<List<CalendarEvent>> {
        return events.map { list ->
            list.filter { it.type == CalendarEventType.TASK }
        }
    }

    override fun getIncompleteTaskEvents(): Flow<List<CalendarEvent>> {
        return events.map { list ->
            list.filter { it.type == CalendarEventType.TASK && !it.completed }
        }
    }

    override fun getTaskEventsByDate(date: LocalDate): Flow<List<CalendarEvent>> {
        return events.map { list ->
            list.filter { it.type == CalendarEventType.TASK && it.date == date }
        }
    }

    override fun getPagedTaskEvents(query: String): Flow<PagingData<CalendarEvent>> {
        return flowOf(
            PagingData.from(
                events.value.filter {
                    it.type == CalendarEventType.TASK && it.title.contains(query)
                }
            )
        )
    }

    override fun getIncompleteTaskCount(): Flow<Int> {
        return events.map { list ->
            list.count { it.type == CalendarEventType.TASK && !it.completed }
        }
    }
}
