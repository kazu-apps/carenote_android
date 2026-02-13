package com.carenote.app.data.mapper.remote

import com.carenote.app.data.remote.model.SyncMetadata
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class NoteRemoteMapperTest {

    private lateinit var timestampConverter: FirestoreTimestampConverter
    private lateinit var mapper: NoteRemoteMapper

    private val testDateTime = LocalDateTime.of(2025, 3, 15, 10, 0, 0)

    @Before
    fun setUp() {
        timestampConverter = FirestoreTimestampConverter()
        mapper = NoteRemoteMapper(timestampConverter)
    }

    // region toDomain

    @Test
    fun `toDomain maps all fields correctly`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "メモの内容です",
            "tag" to "CONDITION",
            "createdBy" to "user123",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(1L, result.id)
        assertEquals("テストメモ", result.title)
        assertEquals("メモの内容です", result.content)
        assertEquals(NoteTag.CONDITION, result.tag)
        assertEquals("user123", result.createdBy)
        assertEquals(testDateTime, result.createdAt)
        assertEquals(testDateTime, result.updatedAt)
    }

    @Test
    fun `toDomain uses default values for optional fields`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "内容",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(NoteTag.OTHER, result.tag)
        assertEquals("", result.createdBy)
    }

    @Test
    fun `toDomain reads authorId as legacy fallback for createdBy`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "内容",
            "authorId" to "legacy-user",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals("legacy-user", result.createdBy)
    }

    @Test
    fun `toDomain maps MEAL tag`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "内容",
            "tag" to "MEAL",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(NoteTag.MEAL, result.tag)
    }

    @Test
    fun `toDomain maps REPORT tag`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "内容",
            "tag" to "REPORT",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(NoteTag.REPORT, result.tag)
    }

    @Test
    fun `toDomain uses OTHER for invalid tag`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "内容",
            "tag" to "INVALID_TAG",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(NoteTag.OTHER, result.tag)
    }

    @Test
    fun `toDomain uses OTHER for null tag`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "内容",
            "tag" to null,
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        val result = mapper.toDomain(data)

        assertEquals(NoteTag.OTHER, result.tag)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when localId is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "title" to "テストメモ",
            "content" to "内容",
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
            "content" to "内容",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when content is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
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
            "title" to "テストメモ",
            "content" to "内容",
            "updatedAt" to timestamp
        )

        mapper.toDomain(data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain throws when updatedAt is missing`() {
        val timestamp = toTimestamp(testDateTime)
        val data = mapOf(
            "localId" to 1L,
            "title" to "テストメモ",
            "content" to "内容",
            "createdAt" to timestamp
        )

        mapper.toDomain(data)
    }

    // endregion

    // region toRemote

    @Test
    fun `toRemote maps all fields correctly`() {
        val note = Note(
            id = 1L,
            title = "テストメモ",
            content = "メモの内容です",
            tag = NoteTag.CONDITION,
            createdBy = "user123",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(note, null)

        assertEquals(1L, result["localId"])
        assertEquals("テストメモ", result["title"])
        assertEquals("メモの内容です", result["content"])
        assertEquals("CONDITION", result["tag"])
        assertEquals("user123", result["createdBy"])
    }

    @Test
    fun `toRemote with authorName adds authorName field`() {
        val note = Note(
            id = 1L,
            title = "テストメモ",
            content = "内容",
            createdBy = "user123",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(note, null, "田中太郎")

        assertEquals("田中太郎", result["authorName"])
    }

    @Test
    fun `toRemote without authorName does not add authorName field`() {
        val note = Note(
            id = 1L,
            title = "テストメモ",
            content = "内容",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(note, null, null)

        assertTrue(!result.containsKey("authorName"))
    }

    @Test
    fun `toRemote adds syncMetadata when provided`() {
        val note = Note(
            id = 1L,
            title = "テストメモ",
            content = "内容",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = null
        )

        val result = mapper.toRemote(note, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertNull(result["deletedAt"])
    }

    @Test
    fun `toRemote with syncMetadata including deletedAt`() {
        val note = Note(
            id = 1L,
            title = "テストメモ",
            content = "内容",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )
        val deletedAt = LocalDateTime.of(2025, 3, 17, 10, 0)
        val syncMetadata = SyncMetadata(
            localId = 1L,
            syncedAt = testDateTime,
            deletedAt = deletedAt
        )

        val result = mapper.toRemote(note, syncMetadata)

        assertTrue(result.containsKey("syncedAt"))
        assertTrue(result.containsKey("deletedAt"))
    }

    @Test
    fun `toRemote without syncMetadata does not add metadata fields`() {
        val note = Note(
            id = 1L,
            title = "テストメモ",
            content = "内容",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val result = mapper.toRemote(note, null)

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
        val original = Note(
            id = 1L,
            title = "テストメモ",
            content = "メモの内容です",
            tag = NoteTag.REPORT,
            createdBy = "user123",
            createdAt = testDateTime,
            updatedAt = testDateTime
        )

        val remote = mapper.toRemote(original, null)
        val roundtrip = mapper.toDomain(remote)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.title, roundtrip.title)
        assertEquals(original.content, roundtrip.content)
        assertEquals(original.tag, roundtrip.tag)
        assertEquals(original.createdBy, roundtrip.createdBy)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    // endregion

    // region toDomainList

    @Test
    fun `toDomainList maps list of data to domain list`() {
        val timestamp = toTimestamp(testDateTime)
        val dataList = listOf(
            mapOf(
                "localId" to 1L,
                "title" to "メモA",
                "content" to "内容A",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            ),
            mapOf(
                "localId" to 2L,
                "title" to "メモB",
                "content" to "内容B",
                "createdAt" to timestamp,
                "updatedAt" to timestamp
            )
        )

        val result = mapper.toDomainList(dataList)

        assertEquals(2, result.size)
        assertEquals("メモA", result[0].title)
        assertEquals("メモB", result[1].title)
    }

    // endregion

    private fun toTimestamp(dateTime: LocalDateTime): Timestamp {
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(instant.epochSecond, instant.nano)
    }
}
