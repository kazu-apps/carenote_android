package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.NoteCommentEntity
import com.carenote.app.domain.model.NoteComment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class NoteCommentMapperTest {

    private lateinit var mapper: NoteCommentMapper

    @Before
    fun setUp() {
        mapper = NoteCommentMapper()
    }

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = NoteCommentEntity(
            id = 1L,
            careRecipientId = 5L,
            noteId = 10L,
            content = "テストコメント",
            createdBy = "user123",
            createdAt = "2025-03-15T08:00:00",
            updatedAt = "2025-03-15T08:05:00"
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals(10L, result.noteId)
        assertEquals("テストコメント", result.content)
        assertEquals("user123", result.createdBy)
        assertEquals(LocalDateTime.of(2025, 3, 15, 8, 0), result.createdAt)
        assertEquals(LocalDateTime.of(2025, 3, 15, 8, 5), result.updatedAt)
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = NoteComment(
            id = 1L,
            careRecipientId = 5L,
            noteId = 10L,
            content = "コメント内容",
            createdBy = "user456",
            createdAt = LocalDateTime.of(2025, 3, 15, 12, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 12, 30)
        )

        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals(10L, result.noteId)
        assertEquals("コメント内容", result.content)
        assertEquals("user456", result.createdBy)
        assertEquals("2025-03-15T12:00:00", result.createdAt)
        assertEquals("2025-03-15T12:30:00", result.updatedAt)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = NoteCommentEntity(
            id = 1L,
            careRecipientId = 3L,
            noteId = 10L,
            content = "往復テスト",
            createdBy = "user789",
            createdAt = "2025-03-15T18:00:00",
            updatedAt = "2025-03-15T18:00:00"
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.careRecipientId, roundtrip.careRecipientId)
        assertEquals(original.noteId, roundtrip.noteId)
        assertEquals(original.content, roundtrip.content)
        assertEquals(original.createdBy, roundtrip.createdBy)
        assertEquals(original.createdAt, roundtrip.createdAt)
        assertEquals(original.updatedAt, roundtrip.updatedAt)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            NoteCommentEntity(
                id = 1L, noteId = 10L, content = "コメント1",
                createdAt = "2025-03-15T08:00:00", updatedAt = "2025-03-15T08:00:00"
            ),
            NoteCommentEntity(
                id = 2L, noteId = 10L, content = "コメント2",
                createdAt = "2025-03-15T09:00:00", updatedAt = "2025-03-15T09:00:00"
            )
        )

        val result = mapper.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals("コメント1", result[0].content)
        assertEquals("コメント2", result[1].content)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = NoteCommentEntity(
            id = 1L,
            careRecipientId = 42L,
            noteId = 10L,
            content = "テスト",
            createdAt = "2025-03-15T08:00:00",
            updatedAt = "2025-03-15T08:05:00"
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    @Test
    fun `toDomain with empty content`() {
        val entity = NoteCommentEntity(
            id = 1L, noteId = 10L, content = "",
            createdAt = "2025-03-15T08:00:00", updatedAt = "2025-03-15T08:00:00"
        )

        assertEquals("", mapper.toDomain(entity).content)
    }

    @Test
    fun `toDomain with empty createdBy`() {
        val entity = NoteCommentEntity(
            id = 1L, noteId = 10L, content = "テスト", createdBy = "",
            createdAt = "2025-03-15T08:00:00", updatedAt = "2025-03-15T08:00:00"
        )

        assertEquals("", mapper.toDomain(entity).createdBy)
    }
}
