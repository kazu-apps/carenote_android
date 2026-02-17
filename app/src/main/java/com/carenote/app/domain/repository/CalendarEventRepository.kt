package com.carenote.app.domain.repository

import androidx.paging.PagingData
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CalendarEventRepository {

    fun getAllEvents(): Flow<List<CalendarEvent>>

    fun getEventById(id: Long): Flow<CalendarEvent?>

    fun getEventsByDate(date: LocalDate): Flow<List<CalendarEvent>>

    fun getEventsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarEvent>>

    suspend fun insertEvent(event: CalendarEvent): Result<Long, DomainError>

    suspend fun updateEvent(event: CalendarEvent): Result<Unit, DomainError>

    suspend fun deleteEvent(id: Long): Result<Unit, DomainError>

    fun getTaskEvents(): Flow<List<CalendarEvent>>

    fun getIncompleteTaskEvents(): Flow<List<CalendarEvent>>

    fun getTaskEventsByDate(date: LocalDate): Flow<List<CalendarEvent>>

    fun getPagedTaskEvents(query: String): Flow<PagingData<CalendarEvent>>

    fun getIncompleteTaskCount(): Flow<Int>
}
