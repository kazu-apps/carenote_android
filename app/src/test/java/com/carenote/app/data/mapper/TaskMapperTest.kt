package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.TaskEntity
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskMapperTest {

    private lateinit var mapper: TaskMapper

    @Before
    fun setUp() {
        mapper = TaskMapper()
    }

    @Test
    fun `toDomain maps entity with all fields to domain model`() {
        val entity = createEntity(
            id = 1L,
            title = "薬を買う",
            description = "処方箋を持参",
            dueDate = "2025-04-10",
            isCompleted = 0,
            priority = "HIGH",
            createdBy = "user1"
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals("薬を買う", result.title)
        assertEquals("処方箋を持参", result.description)
        assertEquals(LocalDate.of(2025, 4, 10), result.dueDate)
        assertFalse(result.isCompleted)
        assertEquals(TaskPriority.HIGH, result.priority)
        assertEquals("user1", result.createdBy)
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 0), result.createdAt)
        assertEquals(LocalDateTime.of(2025, 3, 15, 11, 0), result.updatedAt)
    }

    @Test
    fun `toDomain maps completed task correctly`() {
        val entity = createEntity(isCompleted = 1)

        val result = mapper.toDomain(entity)

        assertTrue(result.isCompleted)
    }

    @Test
    fun `toDomain maps null due date correctly`() {
        val entity = createEntity(dueDate = null)

        val result = mapper.toDomain(entity)

        assertNull(result.dueDate)
    }

    @Test
    fun `toDomain maps each priority correctly`() {
        assertEquals(TaskPriority.LOW, mapper.toDomain(createEntity(priority = "LOW")).priority)
        assertEquals(TaskPriority.MEDIUM, mapper.toDomain(createEntity(priority = "MEDIUM")).priority)
        assertEquals(TaskPriority.HIGH, mapper.toDomain(createEntity(priority = "HIGH")).priority)
    }

    @Test
    fun `toDomain falls back to MEDIUM for unknown priority`() {
        val result = mapper.toDomain(createEntity(priority = "URGENT"))
        assertEquals(TaskPriority.MEDIUM, result.priority)
    }

    @Test
    fun `toDomain falls back to MEDIUM for empty priority`() {
        val result = mapper.toDomain(createEntity(priority = ""))
        assertEquals(TaskPriority.MEDIUM, result.priority)
    }

    @Test
    fun `toDomain falls back to MEDIUM for lowercase priority`() {
        val result = mapper.toDomain(createEntity(priority = "high"))
        assertEquals(TaskPriority.MEDIUM, result.priority)
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
    fun `toDomain maps each recurrence frequency correctly`() {
        assertEquals(
            RecurrenceFrequency.NONE,
            mapper.toDomain(createEntity(recurrenceFrequency = "NONE")).recurrenceFrequency
        )
        assertEquals(
            RecurrenceFrequency.DAILY,
            mapper.toDomain(createEntity(recurrenceFrequency = "DAILY")).recurrenceFrequency
        )
        assertEquals(
            RecurrenceFrequency.WEEKLY,
            mapper.toDomain(createEntity(recurrenceFrequency = "WEEKLY")).recurrenceFrequency
        )
        assertEquals(
            RecurrenceFrequency.MONTHLY,
            mapper.toDomain(createEntity(recurrenceFrequency = "MONTHLY")).recurrenceFrequency
        )
    }

    @Test
    fun `toDomain defaults recurrenceFrequency to NONE for invalid value`() {
        val result = mapper.toDomain(createEntity(recurrenceFrequency = "YEARLY"))
        assertEquals(RecurrenceFrequency.NONE, result.recurrenceFrequency)
    }

    @Test
    fun `toDomain defaults recurrenceFrequency to NONE for empty value`() {
        val result = mapper.toDomain(createEntity(recurrenceFrequency = ""))
        assertEquals(RecurrenceFrequency.NONE, result.recurrenceFrequency)
    }

    @Test
    fun `toDomain maps reminder fields correctly`() {
        val entity = createEntity(
            reminderEnabled = 1,
            reminderTime = "09:30:00"
        )

        val result = mapper.toDomain(entity)

        assertTrue(result.reminderEnabled)
        assertEquals(LocalTime.of(9, 30), result.reminderTime)
    }

    @Test
    fun `toDomain defaults reminderTime to null when not set`() {
        val entity = createEntity(reminderEnabled = 0, reminderTime = null)

        val result = mapper.toDomain(entity)

        assertFalse(result.reminderEnabled)
        assertNull(result.reminderTime)
    }

    @Test
    fun `toEntity maps domain model with all fields to entity`() {
        val domain = createTask(
            id = 2L,
            title = "ケアマネに電話",
            description = "来月の予定確認",
            dueDate = LocalDate.of(2025, 4, 15),
            isCompleted = false,
            priority = TaskPriority.MEDIUM,
            createdBy = "user1"
        )

        val result = mapper.toEntity(domain)

        assertEquals(2L, result.id)
        assertEquals("ケアマネに電話", result.title)
        assertEquals("来月の予定確認", result.description)
        assertEquals("2025-04-15", result.dueDate)
        assertEquals(0, result.isCompleted)
        assertEquals("MEDIUM", result.priority)
        assertEquals("user1", result.createdBy)
        assertEquals("2025-03-15T10:00:00", result.createdAt)
        assertEquals("2025-03-15T10:00:00", result.updatedAt)
    }

    @Test
    fun `toEntity maps completed task to 1`() {
        val domain = createTask(isCompleted = true)

        val result = mapper.toEntity(domain)

        assertEquals(1, result.isCompleted)
    }

    @Test
    fun `toEntity maps incomplete task to 0`() {
        val domain = createTask(isCompleted = false)

        val result = mapper.toEntity(domain)

        assertEquals(0, result.isCompleted)
    }

    @Test
    fun `toEntity maps null due date correctly`() {
        val domain = createTask(dueDate = null)

        val result = mapper.toEntity(domain)

        assertNull(result.dueDate)
    }

    @Test
    fun `toEntity maps recurrence and reminder fields correctly`() {
        val domain = createTask(
            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
            recurrenceInterval = 3,
            reminderEnabled = true,
            reminderTime = LocalTime.of(14, 0)
        )

        val result = mapper.toEntity(domain)

        assertEquals("MONTHLY", result.recurrenceFrequency)
        assertEquals(3, result.recurrenceInterval)
        assertEquals(1, result.reminderEnabled)
        assertEquals("14:00:00", result.reminderTime)
    }

    @Test
    fun `toEntity maps disabled reminder correctly`() {
        val domain = createTask(
            recurrenceFrequency = RecurrenceFrequency.NONE,
            recurrenceInterval = 1,
            reminderEnabled = false,
            reminderTime = null
        )

        val result = mapper.toEntity(domain)

        assertEquals("NONE", result.recurrenceFrequency)
        assertEquals(1, result.recurrenceInterval)
        assertEquals(0, result.reminderEnabled)
        assertNull(result.reminderTime)
    }

    @Test
    fun `roundtrip entity to domain to entity preserves data`() {
        val original = createEntity(
            id = 3L,
            title = "通院予約",
            description = "内科",
            dueDate = "2025-04-20",
            isCompleted = 0,
            priority = "HIGH",
            recurrenceFrequency = "WEEKLY",
            recurrenceInterval = 2,
            reminderEnabled = 1,
            reminderTime = "08:30:00"
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.title, roundtrip.title)
        assertEquals(original.description, roundtrip.description)
        assertEquals(original.dueDate, roundtrip.dueDate)
        assertEquals(original.isCompleted, roundtrip.isCompleted)
        assertEquals(original.priority, roundtrip.priority)
        assertEquals(original.recurrenceFrequency, roundtrip.recurrenceFrequency)
        assertEquals(original.recurrenceInterval, roundtrip.recurrenceInterval)
        assertEquals(original.reminderEnabled, roundtrip.reminderEnabled)
        assertEquals(original.reminderTime, roundtrip.reminderTime)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    @Test
    fun `roundtrip with null due date preserves null`() {
        val original = createEntity(dueDate = null)

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertNull(roundtrip.dueDate)
    }

    @Test
    fun `roundtrip with null reminderTime preserves null`() {
        val original = createEntity(reminderTime = null)

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertNull(roundtrip.reminderTime)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            createEntity(id = 1L, title = "タスクA"),
            createEntity(id = 2L, title = "タスクB"),
            createEntity(id = 3L, title = "タスクC")
        )

        val result = mapper.toDomainList(entities)

        assertEquals(3, result.size)
        assertEquals("タスクA", result[0].title)
        assertEquals("タスクB", result[1].title)
        assertEquals("タスクC", result[2].title)
    }

    @Test
    fun `toDomainList maps empty list`() {
        val result = mapper.toDomainList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `toEntityList maps list of domain models`() {
        val domains = listOf(
            createTask(id = 1L, title = "タスクA"),
            createTask(id = 2L, title = "タスクB")
        )

        val result = mapper.toEntityList(domains)

        assertEquals(2, result.size)
        assertEquals("タスクA", result[0].title)
        assertEquals("タスクB", result[1].title)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = TaskEntity(
            id = 1L,
            title = "テストタスク",
            description = "テスト説明",
            dueDate = "2025-04-10",
            isCompleted = 0,
            priority = "MEDIUM",
            createdAt = "2025-03-15T10:00:00",
            updatedAt = "2025-03-15T11:00:00",
            careRecipientId = 42L
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    private fun createEntity(
        id: Long = 1L,
        title: String = "テストタスク",
        description: String = "テスト説明",
        dueDate: String? = "2025-04-10",
        isCompleted: Int = 0,
        priority: String = "MEDIUM",
        recurrenceFrequency: String = "NONE",
        recurrenceInterval: Int = 1,
        reminderEnabled: Int = 0,
        reminderTime: String? = null,
        createdBy: String = "",
        createdAt: String = "2025-03-15T10:00:00",
        updatedAt: String = "2025-03-15T11:00:00"
    ): TaskEntity = TaskEntity(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        isCompleted = isCompleted,
        priority = priority,
        recurrenceFrequency = recurrenceFrequency,
        recurrenceInterval = recurrenceInterval,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun createTask(
        id: Long = 1L,
        title: String = "テストタスク",
        description: String = "テスト説明",
        dueDate: LocalDate? = LocalDate.of(2025, 4, 10),
        isCompleted: Boolean = false,
        priority: TaskPriority = TaskPriority.MEDIUM,
        recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
        recurrenceInterval: Int = 1,
        reminderEnabled: Boolean = false,
        reminderTime: LocalTime? = null,
        createdBy: String = "",
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ): Task = Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        isCompleted = isCompleted,
        priority = priority,
        recurrenceFrequency = recurrenceFrequency,
        recurrenceInterval = recurrenceInterval,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
