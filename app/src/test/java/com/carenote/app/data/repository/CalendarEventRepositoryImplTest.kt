package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.CalendarEventDao
import com.carenote.app.data.local.entity.CalendarEventEntity
import com.carenote.app.data.mapper.CalendarEventMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.CalendarEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CalendarEventRepositoryImplTest {

    private lateinit var dao: CalendarEventDao
    private lateinit var mapper: CalendarEventMapper
    private lateinit var repository: CalendarEventRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = CalendarEventMapper()
        repository = CalendarEventRepositoryImpl(dao, mapper)
    }

    private fun createEntity(
        id: Long = 1L,
        title: String = "テスト予定",
        description: String = "テスト説明",
        date: String = "2025-04-10",
        startTime: String? = "09:00:00",
        endTime: String? = "10:00:00",
        isAllDay: Int = 0
    ) = CalendarEventEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        isAllDay = isAllDay,
        createdAt = "2025-03-15T10:00:00",
        updatedAt = "2025-03-15T10:00:00"
    )

    @Test
    fun `getAllEvents returns flow of events`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "予定A"),
            createEntity(2L, title = "予定B")
        )
        every { dao.getAllEvents() } returns flowOf(entities)

        val result = repository.getAllEvents().first()

        assertEquals(2, result.size)
        assertEquals("予定A", result[0].title)
        assertEquals("予定B", result[1].title)
    }

    @Test
    fun `getAllEvents returns empty list when no events`() = runTest {
        every { dao.getAllEvents() } returns flowOf(emptyList())

        val result = repository.getAllEvents().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getEventById returns event when found`() = runTest {
        val entity = createEntity(1L, title = "通院")
        every { dao.getEventById(1L) } returns flowOf(entity)

        val result = repository.getEventById(1L).first()

        assertEquals("通院", result?.title)
    }

    @Test
    fun `getEventById returns null when not found`() = runTest {
        every { dao.getEventById(999L) } returns flowOf(null)

        val result = repository.getEventById(999L).first()

        assertNull(result)
    }

    @Test
    fun `getEventsByDate returns events for specific date`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "朝の予定", date = "2025-04-10"),
            createEntity(2L, title = "午後の予定", date = "2025-04-10")
        )
        every { dao.getEventsByDate("2025-04-10") } returns flowOf(entities)

        val result = repository.getEventsByDate(LocalDate.of(2025, 4, 10)).first()

        assertEquals(2, result.size)
        assertEquals("朝の予定", result[0].title)
        assertEquals("午後の予定", result[1].title)
    }

    @Test
    fun `getEventsByDateRange returns events in range`() = runTest {
        val entities = listOf(
            createEntity(1L, title = "予定A", date = "2025-04-01"),
            createEntity(2L, title = "予定B", date = "2025-04-15")
        )
        every {
            dao.getEventsByDateRange("2025-04-01", "2025-04-30")
        } returns flowOf(entities)

        val start = LocalDate.of(2025, 4, 1)
        val end = LocalDate.of(2025, 4, 30)
        val result = repository.getEventsByDateRange(start, end).first()

        assertEquals(2, result.size)
    }

    @Test
    fun `getEventsByDateRange returns empty list when no events in range`() = runTest {
        every {
            dao.getEventsByDateRange(any(), any())
        } returns flowOf(emptyList())

        val start = LocalDate.of(2025, 1, 1)
        val end = LocalDate.of(2025, 1, 31)
        val result = repository.getEventsByDateRange(start, end).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `insertEvent returns Success with id`() = runTest {
        coEvery { dao.insertEvent(any()) } returns 1L

        val event = CalendarEvent(
            title = "テスト予定",
            date = LocalDate.of(2025, 4, 10),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertEvent(event)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).value)
    }

    @Test
    fun `insertEvent returns Failure on db error`() = runTest {
        coEvery { dao.insertEvent(any()) } throws RuntimeException("DB error")

        val event = CalendarEvent(
            title = "テスト予定",
            date = LocalDate.of(2025, 4, 10),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertEvent(event)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `updateEvent returns Success`() = runTest {
        coEvery { dao.updateEvent(any()) } returns Unit

        val event = CalendarEvent(
            id = 1L,
            title = "更新予定",
            date = LocalDate.of(2025, 4, 10),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 11, 0)
        )
        val result = repository.updateEvent(event)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateEvent returns Failure on db error`() = runTest {
        coEvery { dao.updateEvent(any()) } throws RuntimeException("DB error")

        val event = CalendarEvent(
            id = 1L,
            title = "更新予定",
            date = LocalDate.of(2025, 4, 10),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 11, 0)
        )
        val result = repository.updateEvent(event)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `deleteEvent returns Success`() = runTest {
        coEvery { dao.deleteEvent(1L) } returns Unit

        val result = repository.deleteEvent(1L)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteEvent(1L) }
    }

    @Test
    fun `deleteEvent returns Failure on db error`() = runTest {
        coEvery { dao.deleteEvent(1L) } throws RuntimeException("DB error")

        val result = repository.deleteEvent(1L)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }
}
