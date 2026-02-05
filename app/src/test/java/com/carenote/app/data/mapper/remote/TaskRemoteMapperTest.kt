package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Task
import com.carenote.app.domain.model.TaskPriority
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TaskRemoteMapperTest {

    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var mapper: TaskRemoteMapper

    private val testDateTime = LocalDateTime.of(2025, 3, 15, 10, 0, 0)
    private val dueDate = LocalDate.of(2025, 3, 20)

    @Before
    fun setUp() {
        timestampConverter = FirestoreTimestampConverter()
        mapper = TaskRemoteMapper(timestampConverter)
    }

    // region toDomain

    @Test
    fun `toDomain maps all fields correctly`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "買い物",
            "description" to "薬局で薬を受け取る",
            "dueDate" to "2025-03-20",
            "isCompleted" to false,
            "priority" to "HIGH",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(1L, result.id)
        assertEquals("買い物", result.title)
        assertEquals("薬局で薬を受け取る", result.description)
        assertEquals(dueDate, result.dueDate)
        assertEquals(false, result.isCompleted)
        assertEquals(TaskPriority.HIGH, result.priority)
        assertEquals(testDateTime, result.createdAt)
        assertEquals(testDateTime, result.updatedAt)
    }

    @Test
    fun `toDomain uses default values for optional fields`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals("", result.description)
        assertNull(result.dueDate)
        assertEquals(false, result.isCompleted)
        assertEquals(TaskPriority.MEDIUM, result.priority)
    }

    @Test
    fun `toDomain maps LOW priority`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "priority" to "LOW",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(TaskPriority.LOW, result.priority)
    }

    @Test
    fun `toDomain maps MEDIUM priority`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "priority" to "MEDIUM",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(TaskPriority.MEDIUM, result.priority)
    }

    @Test
    fun `toDomain maps HIGH priority`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "priority" to "HIGH",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(TaskPriority.HIGH, result.priority)
    }

    @Test
    fun `toDomain uses MEDIUM for invalid priority`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "priority" to "INVALID_PRIORITY",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(TaskPriority.MEDIUM, result.priority)
    }

    @Test
    fun `toDomain uses MEDIUM for null priority`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "priority" to null,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(TaskPriority.MEDIUM, result.priority)
    }

    @Test
    fun `toDomain handles null dueDate`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "dueDate" to null,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertNull(result.dueDate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when localId is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "title" to "タスク",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when title is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when createdAt is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when updatedAt is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "タスク",
            "createdAt" to timestamp
        )

        mapper.toDomain(data)
    }

    // endregion

    // region toRemote

    @Test
    fun `toRemote maps all fields correctly`() {
        val task = Task(
            id = 1L,
            title = "買い物",
            description = "薬局で薬を受け取る",
            dueDate = dueDate,
            isCompleted = false,
            priority = TaskPriority.HIGH,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(task, null)

        assertEquals(1L, result["localId"])
        assertEquals("買い物", result["title"])
        assertEquals("薬局で薬を受け取る", result["description"])
        assertEquals("2025-03-20", result["dueDate"])
        assertEquals(false, result["isCompleted"])
        assertEquals("HIGH", result["priority"])
    }

    @Test
    fun `toRemote handles null dueDate`() {
        val task = Task(
            id = 1L,
            title = "タスク",
            dueDate = null,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(task, null)

        assertNull(result["dueDate"])
    }

    @Test
    fun `toRemote maps all priority values`() {
        TaskPriority.entries.forEach { priority ->
            val task = Task(
                id = 1L,
                title = "タスク",
                priority = priority,
                createdAt = testDateTime,
                updatedAt = testDateTime
            )

            val result = mapper.toRemote(task, null)

            assertEquals(priority.name, result["priority"])
        }
    }

    @Test
    fun `toRemote adds syncMetadata when provided`() {
        val task = Task(
            id = 1L,
            title = "タスク",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = null
        )

        val result = mapper.toRemote(task, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertNull(result["deletedAt"])
    }

    @Test
    fun `toRemote with syncMetadata including deletedAt`() {
        val task = Task(
            id = 1L,
            title = "タスク",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val deletedAt = LocalDateTime.of(2025, 3, 17, 10, 0)
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = deletedAt
        )

        val result = mapper.toRemote(task, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertTrue(result.containsKey("deletedAt"))
    }

    @Test
    fun `toRemote without syncMetadata does not add metadata fields`() {
        val task = Task(
            id = 1L,
            title = "タスク",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(task, null)

        assertTrue(!result.containsKey("syncedAt"))
        assertTrue(!result.containsKey("deletedAt"))
    }

    // endregion

    // region extractSyncMetadata

    @Test
    fun `extractSyncMetadata extracts all fields correctly`() {
        val syncedAt = LocalDateTime.of(2025, 3, 16, 10, 0)
        val deletedAt = LocalDateTime.of(2025, 3, 17, 10, 0)
        val data = mapOf(
            "localId" to 1L,
            "syncedAt" to toTimestamp(syncedAt),
            "deletedAt" to toTimestamp(deletedAt)
        )

        val result = mapper.extractSyncMetadata(data)

        assertEquals(1L, result.localId)
        assertEquals(syncedAt, result.syncedAt)
        assertEquals(deletedAt, result.deletedAt)
    }

    @Test
    fun `extractSyncMetadata with null deletedAt`() {
        val syncedAt = LocalDateTime.of(2025, 3, 16, 10, 0)
        val data = mapOf(
            "localId" to 1L,
            "syncedAt" to toTimestamp(syncedAt),
            "deletedAt" to null
        )

        val result = mapper.extractSyncMetadata(data)

        assertEquals(1L, result.localId)
        assertEquals(syncedAt, result.syncedAt)
        assertNull(result.deletedAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `extractSyncMetadata throws when localId is missing`() {
        val data = mapOf(
            "syncedAt" to toTimestamp(testDateTime)
        )

        mapper.extractSyncMetadata(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `extractSyncMetadata throws when syncedAt is missing`() {
        val data = mapOf(
            "localId" to 1L
        )

        mapper.extractSyncMetadata(data)
    }

    // endregion

    // region roundtrip

    @Test
    fun `roundtrip domain to remote to domain preserves data`() {
        val original = Task(
            id = 1L,
            title = "買い物",
            description = "薬局で薬を受け取る",
            dueDate = dueDate,
            isCompleted = true,
            priority = TaskPriority.LOW,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.title, roundtrip.title)
        assertEquals(original.description, roundtrip.description)
        assertEquals(original.dueDate, roundtrip.dueDate)
        assertEquals(original.isCompleted, roundtrip.isCompleted)
        assertEquals(original.priority, roundtrip.priority)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    @Test
    fun `roundtrip with null dueDate preserves data`() {
        val original = Task(
            id = 1L,
            title = "タスク",
            dueDate = null,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertNull(roundtrip.dueDate)
    }

    // endregion

    // region toDomainList

    @Test
    fun `toDomainList maps list of data to domain list`() {
        val timestamp = toTimestamp(testDateTime)
        val dataList = listOf(
            mapOf(
                "localId" to 1L,
                "title" to "タスクA",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            ),
            mapOf(
                "localId" to 2L,
                "title" to "タスクB",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )
        )

        val result = mapper.toDomainList(dataList)

        assertEquals(2, result.size)
        assertEquals("タスクA", result[0].title)
        assertEquals("タスクB", result[1].title)
    }

    // endregion

    private fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }
}
