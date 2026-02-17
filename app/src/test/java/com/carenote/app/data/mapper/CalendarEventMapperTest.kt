package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.CalendarEventEntity
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CalendarEventMapperTest {

    private lateinit var mapper: CalendarEventMapper

    @Before
    fun setUp() {
        mapper = CalendarEventMapper()
    }

    @Test
    fun `toDomain maps entity with all fields to domain model`() {
        val entity = createEntity(
            id = 1L,
            title = "通院",
            description = "内科検診",
            date = "2025-04-10",
            startTime = "09:00:00",
            endTime = "10:30:00",
            isAllDay = 0
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals("通院", result.title)
        assertEquals("内科検診", result.description)
        assertEquals(LocalDate.of(2025, 4, 10), result.date)
        assertEquals(LocalTime.of(9, 0), result.startTime)
        assertEquals(LocalTime.of(10, 30), result.endTime)
        assertFalse(result.isAllDay)
        assertEquals(TestDataFixtures.NOW, result.createdAt)
        assertEquals(TestDataFixtures.NOW.plusHours(1), result.updatedAt)
    }

    @Test
    fun `toDomain maps all-day event correctly`() {
        val entity = createEntity(
            isAllDay = 1,
            startTime = null,
            endTime = null
        )

        val result = mapper.toDomain(entity)

        assertTrue(result.isAllDay)
        assertNull(result.startTime)
        assertNull(result.endTime)
    }

    @Test
    fun `toDomain maps null start time and end time correctly`() {
        val entity = createEntity(startTime = null, endTime = null)

        val result = mapper.toDomain(entity)

        assertNull(result.startTime)
        assertNull(result.endTime)
    }

    @Test
    fun `toEntity maps domain model with all fields to entity`() {
        val domain = createCalendarEvent(
            id = 2L,
            title = "デイサービス",
            description = "送迎あり",
            date = LocalDate.of(2025, 4, 15),
            startTime = LocalTime.of(8, 30),
            endTime = LocalTime.of(16, 0),
            isAllDay = false
        )

        val result = mapper.toEntity(domain)

        assertEquals(2L, result.id)
        assertEquals("デイサービス", result.title)
        assertEquals("送迎あり", result.description)
        assertEquals("2025-04-15", result.date)
        assertEquals("08:30:00", result.startTime)
        assertEquals("16:00:00", result.endTime)
        assertEquals(0, result.isAllDay)
        assertEquals(TestDataFixtures.NOW_STRING, result.createdAt)
        assertEquals(TestDataFixtures.NOW_STRING, result.updatedAt)
    }

    @Test
    fun `toEntity maps all-day event correctly`() {
        val domain = createCalendarEvent(
            isAllDay = true,
            startTime = null,
            endTime = null
        )

        val result = mapper.toEntity(domain)

        assertEquals(1, result.isAllDay)
        assertNull(result.startTime)
        assertNull(result.endTime)
    }

    @Test
    fun `toEntity maps isAllDay false to 0`() {
        val domain = createCalendarEvent(isAllDay = false)

        val result = mapper.toEntity(domain)

        assertEquals(0, result.isAllDay)
    }

    @Test
    fun `toEntity maps isAllDay true to 1`() {
        val domain = createCalendarEvent(isAllDay = true)

        val result = mapper.toEntity(domain)

        assertEquals(1, result.isAllDay)
    }

    @Test
    fun `roundtrip entity to domain to entity preserves data`() {
        val original = createEntity(
            id = 3L,
            title = "ケアマネ訪問",
            description = "月次面談",
            date = "2025-04-20",
            startTime = "14:00:00",
            endTime = "15:00:00",
            isAllDay = 0
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.title, roundtrip.title)
        assertEquals(original.description, roundtrip.description)
        assertEquals(original.date, roundtrip.date)
        assertEquals(original.startTime, roundtrip.startTime)
        assertEquals(original.endTime, roundtrip.endTime)
        assertEquals(original.isAllDay, roundtrip.isAllDay)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    @Test
    fun `roundtrip with null times preserves nulls`() {
        val original = createEntity(
            startTime = null,
            endTime = null,
            isAllDay = 1
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertNull(roundtrip.startTime)
        assertNull(roundtrip.endTime)
        assertEquals(1, roundtrip.isAllDay)
    }

    @Test
    fun `toDomain maps type field correctly`() {
        val entity = createEntity(type = "HOSPITAL")

        val result = mapper.toDomain(entity)

        assertEquals(CalendarEventType.HOSPITAL, result.type)
    }

    @Test
    fun `toDomain maps completed true correctly`() {
        val entity = createEntity(completed = 1)

        val result = mapper.toDomain(entity)

        assertTrue(result.completed)
    }

    @Test
    fun `toEntity maps type to string`() {
        val domain = createCalendarEvent(type = CalendarEventType.DAYSERVICE)

        val result = mapper.toEntity(domain)

        assertEquals("DAYSERVICE", result.type)
    }

    @Test
    fun `toEntity maps completed true to 1`() {
        val domain = createCalendarEvent(completed = true)

        val result = mapper.toEntity(domain)

        assertEquals(1, result.completed)
    }

    @Test
    fun `roundtrip preserves type and completed`() {
        val original = createEntity(
            type = "VISIT",
            completed = 1
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.type, roundtrip.type)
        assertEquals(original.completed, roundtrip.completed)
    }

    @Test
    fun `toDomain maps recurrence fields correctly`() {
        val entity = createEntity(
            recurrenceFrequency = "WEEKLY",
            recurrenceInterval = 2
        )

        val result = mapper.toDomain(entity)

        assertEquals(RecurrenceFrequency.WEEKLY, result.recurrenceFrequency)
        assertEquals(2, result.recurrenceInterval)
    }

    @Test
    fun `toDomain with default recurrence values`() {
        val entity = createEntity()

        val result = mapper.toDomain(entity)

        assertEquals(RecurrenceFrequency.NONE, result.recurrenceFrequency)
        assertEquals(1, result.recurrenceInterval)
    }

    @Test
    fun `toDomain with invalid recurrence frequency defaults to NONE`() {
        val entity = createEntity(recurrenceFrequency = "INVALID")

        val result = mapper.toDomain(entity)

        assertEquals(RecurrenceFrequency.NONE, result.recurrenceFrequency)
    }

    @Test
    fun `toEntity maps recurrence fields correctly`() {
        val domain = createCalendarEvent(
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceInterval = 3
        )

        val result = mapper.toEntity(domain)

        assertEquals("MONTHLY", result.recurrenceFrequency)
        assertEquals(3, result.recurrenceInterval)
    }

    @Test
    fun `roundtrip preserves recurrence fields`() {
        val original = createEntity(
            recurrenceFrequency = "DAILY",
            recurrenceInterval = 5
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.recurrenceFrequency, roundtrip.recurrenceFrequency)
        assertEquals(original.recurrenceInterval, roundtrip.recurrenceInterval)
    }

    // region task fields

    @Test
    fun `toDomain maps priority field correctly`() {
        val entity = createEntity(priority = "HIGH")

        val result = mapper.toDomain(entity)

        assertEquals(TaskPriority.HIGH, result.priority)
    }

    @Test
    fun `toDomain maps null priority to null`() {
        val entity = createEntity(priority = null)

        val result = mapper.toDomain(entity)

        assertNull(result.priority)
    }

    @Test
    fun `toDomain maps invalid priority to null`() {
        val entity = createEntity(priority = "INVALID")

        val result = mapper.toDomain(entity)

        assertNull(result.priority)
    }

    @Test
    fun `toDomain maps reminderEnabled correctly`() {
        val entity = createEntity(reminderEnabled = 1)

        val result = mapper.toDomain(entity)

        assertTrue(result.reminderEnabled)
    }

    @Test
    fun `toDomain maps reminderTime correctly`() {
        val entity = createEntity(reminderTime = "08:30:00")

        val result = mapper.toDomain(entity)

        assertEquals(LocalTime.of(8, 30), result.reminderTime)
    }

    @Test
    fun `toDomain maps createdBy correctly`() {
        val entity = createEntity(createdBy = "testUser")

        val result = mapper.toDomain(entity)

        assertEquals("testUser", result.createdBy)
    }

    @Test
    fun `toEntity maps priority to string`() {
        val domain = createCalendarEvent(priority = TaskPriority.MEDIUM)

        val result = mapper.toEntity(domain)

        assertEquals("MEDIUM", result.priority)
    }

    @Test
    fun `toEntity maps null priority to null`() {
        val domain = createCalendarEvent(priority = null)

        val result = mapper.toEntity(domain)

        assertNull(result.priority)
    }

    @Test
    fun `toEntity maps reminderEnabled to int`() {
        val domain = createCalendarEvent(reminderEnabled = true)

        val result = mapper.toEntity(domain)

        assertEquals(1, result.reminderEnabled)
    }

    @Test
    fun `toEntity maps reminderEnabled false to 0`() {
        val domain = createCalendarEvent(reminderEnabled = false)

        val result = mapper.toEntity(domain)

        assertEquals(0, result.reminderEnabled)
    }

    @Test
    fun `toEntity maps reminderTime correctly`() {
        val domain = createCalendarEvent(reminderTime = LocalTime.of(9, 15))

        val result = mapper.toEntity(domain)

        assertEquals("09:15:00", result.reminderTime)
    }

    @Test
    fun `toEntity maps createdBy correctly`() {
        val domain = createCalendarEvent(createdBy = "testUser")

        val result = mapper.toEntity(domain)

        assertEquals("testUser", result.createdBy)
    }

    @Test
    fun `roundtrip preserves task fields`() {
        val original = createEntity(
            type = "TASK",
            priority = "HIGH",
            reminderEnabled = 1,
            reminderTime = "09:00:00",
            createdBy = "testUser"
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.priority, roundtrip.priority)
        assertEquals(original.reminderEnabled, roundtrip.reminderEnabled)
        assertEquals(original.reminderTime, roundtrip.reminderTime)
        assertEquals(original.createdBy, roundtrip.createdBy)
    }

    // endregion

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            createEntity(id = 1L, title = "予定A"),
            createEntity(id = 2L, title = "予定B"),
            createEntity(id = 3L, title = "予定C")
        )

        val result = mapper.toDomainList(entities)

        assertEquals(3, result.size)
        assertEquals("予定A", result[0].title)
        assertEquals("予定B", result[1].title)
        assertEquals("予定C", result[2].title)
    }

    @Test
    fun `toDomainList maps empty list`() {
        val result = mapper.toDomainList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `toEntityList maps list of domain models`() {
        val domains = listOf(
            createCalendarEvent(id = 1L, title = "予定A"),
            createCalendarEvent(id = 2L, title = "予定B")
        )

        val result = mapper.toEntityList(domains)

        assertEquals(2, result.size)
        assertEquals("予定A", result[0].title)
        assertEquals("予定B", result[1].title)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = CalendarEventEntity(
            id = 1L,
            title = "テスト予定",
            description = "テスト説明",
            date = "2025-04-10",
            startTime = "09:00:00",
            endTime = "10:00:00",
            isAllDay = 0,
            createdAt = TestDataFixtures.NOW_STRING,
            updatedAt = TestDataFixtures.NOW.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            careRecipientId = 42L
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    private fun createEntity(
        id: Long = 1L,
        title: String = "テスト予定",
        description: String = "テスト説明",
        date: String = "2025-04-10",
        startTime: String? = "09:00:00",
        endTime: String? = "10:00:00",
        isAllDay: Int = 0,
        type: String = "OTHER",
        completed: Int = 0,
        recurrenceFrequency: String = "NONE",
        recurrenceInterval: Int = 1,
        priority: String? = null,
        reminderEnabled: Int = 0,
        reminderTime: String? = null,
        createdBy: String = "",
        createdAt: String = TestDataFixtures.NOW_STRING,
        updatedAt: String = TestDataFixtures.NOW.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    ): CalendarEventEntity = CalendarEventEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        isAllDay = isAllDay,
        type = type,
        completed = completed,
        recurrenceFrequency = recurrenceFrequency,
        recurrenceInterval = recurrenceInterval,
        priority = priority,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun createCalendarEvent(
        id: Long = 1L,
        title: String = "テスト予定",
        description: String = "テスト説明",
        date: LocalDate = LocalDate.of(2025, 4, 10),
        startTime: LocalTime? = LocalTime.of(9, 0),
        endTime: LocalTime? = LocalTime.of(10, 0),
        isAllDay: Boolean = false,
        type: CalendarEventType = CalendarEventType.OTHER,
        completed: Boolean = false,
        recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
        recurrenceInterval: Int = 1,
        priority: TaskPriority? = null,
        reminderEnabled: Boolean = false,
        reminderTime: LocalTime? = null,
        createdBy: String = "",
        createdAt: LocalDateTime = TestDataFixtures.NOW,
        updatedAt: LocalDateTime = TestDataFixtures.NOW
    ): CalendarEvent = CalendarEvent(
        id = id,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        isAllDay = isAllDay,
        type = type,
        completed = completed,
        recurrenceFrequency = recurrenceFrequency,
        recurrenceInterval = recurrenceInterval,
        priority = priority,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
