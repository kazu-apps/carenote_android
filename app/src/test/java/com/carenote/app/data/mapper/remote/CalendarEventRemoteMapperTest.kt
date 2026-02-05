package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.CalendarEvent
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class CalendarEventRemoteMapperTest {

    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var mapper: CalendarEventRemoteMapper

    private val testDateTime = LocalDateTime.of(2025, 3, 15, 10, 0, 0)
    private val eventDate = LocalDate.of(2025, 3, 20)

    @Before
    fun setUp() {
        timestampConverter = FirestoreTimestampConverter()
        mapper = CalendarEventRemoteMapper(timestampConverter)
    }

    // region toDomain

    @Test
    fun `toDomain maps all fields correctly`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "通院予定",
            "description" to "定期検診",
            "date" to "2025-03-20",
            "startTime" to "10:00",
            "endTime" to "11:30",
            "isAllDay" to false,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(1L, result.id)
        assertEquals("通院予定", result.title)
        assertEquals("定期検診", result.description)
        assertEquals(eventDate, result.date)
        assertEquals(LocalTime.of(10, 0), result.startTime)
        assertEquals(LocalTime.of(11, 30), result.endTime)
        assertEquals(false, result.isAllDay)
        assertEquals(testDateTime, result.createdAt)
        assertEquals(testDateTime, result.updatedAt)
    }

    @Test
    fun `toDomain maps all-day event with default values`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "終日イベント",
            "date" to "2025-03-20",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals("", result.description)
        assertNull(result.startTime)
        assertNull(result.endTime)
        assertTrue(result.isAllDay)
    }

    @Test
    fun `toDomain handles null startTime and endTime`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストイベント",
            "date" to "2025-03-20",
            "startTime" to null,
            "endTime" to null,
            "isAllDay" to true,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertNull(result.startTime)
        assertNull(result.endTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when localId is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "title" to "イベント",
            "date" to "2025-03-20",
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
            "date" to "2025-03-20",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when date is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "イベント",
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
            "title" to "イベント",
            "date" to "2025-03-20",
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when updatedAt is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "イベント",
            "date" to "2025-03-20",
            "createdAt" to timestamp
        )

        mapper.toDomain(data)
    }

    // endregion

    // region toRemote

    @Test
    fun `toRemote maps all fields correctly`() {
        val event = CalendarEvent(
            id = 1L,
            title = "通院予定",
            description = "定期検診",
            date = eventDate,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 30),
            isAllDay = false,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(event, null)

        assertEquals(1L, result["localId"])
        assertEquals("通院予定", result["title"])
        assertEquals("定期検診", result["description"])
        assertEquals("2025-03-20", result["date"])
        assertEquals("10:00", result["startTime"])
        assertEquals("11:30", result["endTime"])
        assertEquals(false, result["isAllDay"])
    }

    @Test
    fun `toRemote handles null startTime and endTime`() {
        val event = CalendarEvent(
            id = 1L,
            title = "終日イベント",
            date = eventDate,
            startTime = null,
            endTime = null,
            isAllDay = true,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(event, null)

        assertNull(result["startTime"])
        assertNull(result["endTime"])
    }

    @Test
    fun `toRemote adds syncMetadata when provided`() {
        val event = CalendarEvent(
            id = 1L,
            title = "イベント",
            date = eventDate,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = null
        )

        val result = mapper.toRemote(event, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertNull(result["deletedAt"])
    }

    @Test
    fun `toRemote with syncMetadata including deletedAt`() {
        val event = CalendarEvent(
            id = 1L,
            title = "イベント",
            date = eventDate,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val deletedAt = LocalDateTime.of(2025, 3, 17, 10, 0)
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = deletedAt
        )

        val result = mapper.toRemote(event, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertTrue(result.containsKey("deletedAt"))
    }

    @Test
    fun `toRemote without syncMetadata does not add metadata fields`() {
        val event = CalendarEvent(
            id = 1L,
            title = "イベント",
            date = eventDate,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(event, null)

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
        val original = CalendarEvent(
            id = 1L,
            title = "通院予定",
            description = "定期検診",
            date = eventDate,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 30),
            isAllDay = false,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

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
    fun `roundtrip with null times preserves data`() {
        val original = CalendarEvent(
            id = 1L,
            title = "終日イベント",
            description = "",
            date = eventDate,
            startTime = null,
            endTime = null,
            isAllDay = true,
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.title, roundtrip.title)
        assertNull(roundtrip.startTime)
        assertNull(roundtrip.endTime)
        assertTrue(roundtrip.isAllDay)
    }

    // endregion

    // region toDomainList

    @Test
    fun `toDomainList maps list of data to domain list`() {
        val timestamp = toTimestamp(testDateTime)
        val dataList = listOf(
            mapOf(
                "localId" to 1L,
                "title" to "イベントA",
                "date" to "2025-03-20",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            ),
            mapOf(
                "localId" to 2L,
                "title" to "イベントB",
                "date" to "2025-03-21",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )
        )

        val result = mapper.toDomainList(dataList)

        assertEquals(2, result.size)
        assertEquals("イベントA", result[0].title)
        assertEquals("イベントB", result[1].title)
    }

    // endregion

    private fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }
}
