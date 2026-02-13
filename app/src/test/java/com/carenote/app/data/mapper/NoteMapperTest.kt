package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.NoteEntity
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class NoteMapperTest {

    private lateinit var mapper: NoteMapper

    @Before
    fun setUp() {
        mapper = NoteMapper()
    }

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = NoteEntity(
            id = 1L,
            title = "テストメモ",
            content = "テスト内容",
            tag = "CONDITION",
            createdBy = "user1",
            createdAt = "2025-03-15T10:00:00",
            updatedAt = "2025-03-15T11:00:00"
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals("テストメモ", result.title)
        assertEquals("テスト内容", result.content)
        assertEquals(NoteTag.CONDITION, result.tag)
        assertEquals("user1", result.createdBy)
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 0), result.createdAt)
        assertEquals(LocalDateTime.of(2025, 3, 15, 11, 0), result.updatedAt)
    }

    @Test
    fun `toDomain maps all tag types correctly`() {
        val tags = listOf("CONDITION", "MEAL", "REPORT", "OTHER")
        val expectedTags = listOf(NoteTag.CONDITION, NoteTag.MEAL, NoteTag.REPORT, NoteTag.OTHER)

        tags.forEachIndexed { index, tagString ->
            val entity = createEntity(tag = tagString)
            val result = mapper.toDomain(entity)
            assertEquals(expectedTags[index], result.tag)
        }
    }

    @Test
    fun `toDomain maps unknown tag to OTHER`() {
        val entity = createEntity(tag = "UNKNOWN_TAG")

        val result = mapper.toDomain(entity)

        assertEquals(NoteTag.OTHER, result.tag)
    }

    @Test
    fun `toDomain maps empty tag to OTHER`() {
        val entity = createEntity(tag = "")

        val result = mapper.toDomain(entity)

        assertEquals(NoteTag.OTHER, result.tag)
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = Note(
            id = 2L,
            title = "申し送り",
            content = "今日の状態は良好",
            tag = NoteTag.REPORT,
            createdBy = "user2",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 12, 0)
        )

        val result = mapper.toEntity(domain)

        assertEquals(2L, result.id)
        assertEquals("申し送り", result.title)
        assertEquals("今日の状態は良好", result.content)
        assertEquals("REPORT", result.tag)
        assertEquals("user2", result.createdBy)
        assertEquals("2025-03-15T10:00:00", result.createdAt)
        assertEquals("2025-03-15T12:00:00", result.updatedAt)
    }

    @Test
    fun `toEntity maps all tag types to correct strings`() {
        val tags = listOf(NoteTag.CONDITION, NoteTag.MEAL, NoteTag.REPORT, NoteTag.OTHER)
        val expectedStrings = listOf("CONDITION", "MEAL", "REPORT", "OTHER")

        tags.forEachIndexed { index, tag ->
            val domain = createNote(tag = tag)
            val result = mapper.toEntity(domain)
            assertEquals(expectedStrings[index], result.tag)
        }
    }

    @Test
    fun `roundtrip entity to domain to entity preserves data`() {
        val original = NoteEntity(
            id = 3L,
            title = "体調メモ",
            content = "熱が37.2度",
            tag = "CONDITION",
            createdBy = "user1",
            createdAt = "2025-03-15T10:00:00",
            updatedAt = "2025-03-15T10:00:00"
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.title, roundtrip.title)
        assertEquals(original.content, roundtrip.content)
        assertEquals(original.tag, roundtrip.tag)
        assertEquals(original.createdBy, roundtrip.createdBy)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            createEntity(id = 1L, title = "メモA"),
            createEntity(id = 2L, title = "メモB"),
            createEntity(id = 3L, title = "メモC")
        )

        val result = mapper.toDomainList(entities)

        assertEquals(3, result.size)
        assertEquals("メモA", result[0].title)
        assertEquals("メモB", result[1].title)
        assertEquals("メモC", result[2].title)
    }

    @Test
    fun `toDomainList maps empty list`() {
        val result = mapper.toDomainList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `toEntityList maps list of domain models`() {
        val domains = listOf(
            createNote(id = 1L, title = "メモA"),
            createNote(id = 2L, title = "メモB")
        )

        val result = mapper.toEntityList(domains)

        assertEquals(2, result.size)
        assertEquals("メモA", result[0].title)
        assertEquals("メモB", result[1].title)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = NoteEntity(
            id = 1L,
            title = "テストメモ",
            content = "テスト内容",
            tag = "OTHER",
            createdBy = "",
            createdAt = "2025-03-15T10:00:00",
            updatedAt = "2025-03-15T10:00:00",
            careRecipientId = 42L
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    private fun createEntity(
        id: Long = 1L,
        title: String = "テストメモ",
        content: String = "テスト内容",
        tag: String = "OTHER",
        createdBy: String = "",
        createdAt: String = "2025-03-15T10:00:00",
        updatedAt: String = "2025-03-15T10:00:00"
    ): NoteEntity = NoteEntity(
        id = id,
        title = title,
        content = content,
        tag = tag,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun createNote(
        id: Long = 1L,
        title: String = "テストメモ",
        content: String = "テスト内容",
        tag: NoteTag = NoteTag.OTHER,
        createdBy: String = "",
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ): Note = Note(
        id = id,
        title = title,
        content = content,
        tag = tag,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
