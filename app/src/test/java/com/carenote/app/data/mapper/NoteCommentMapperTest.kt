package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.NoteCommentEntity
import com.carenote.app.domain.model.NoteComment
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.format.DateTimeFormatter

class NoteCommentMapperTest {

    private lateinit var mapper: NoteCommentMapper

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

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
            createdAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            updatedAt = TestDataFixtures.NOW.withHour(8).withMinute(5).format(fmt)
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals(10L, result.noteId)
        assertEquals("テストコメント", result.content)
        assertEquals("user123", result.createdBy)
        assertEquals(TestDataFixtures.NOW.withHour(8).withMinute(0), result.createdAt)
        assertEquals(TestDataFixtures.NOW.withHour(8).withMinute(5), result.updatedAt)
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = NoteComment(
            id = 1L,
            careRecipientId = 5L,
            noteId = 10L,
            content = "コメント内容",
            createdBy = "user456",
            createdAt = TestDataFixtures.NOW.withHour(12).withMinute(0),
            updatedAt = TestDataFixtures.NOW.withHour(12).withMinute(30)
        )

        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals(10L, result.noteId)
        assertEquals("コメント内容", result.content)
        assertEquals("user456", result.createdBy)
        assertEquals(TestDataFixtures.NOW.withHour(12).withMinute(0).format(fmt), result.createdAt)
        assertEquals(TestDataFixtures.NOW.withHour(12).withMinute(30).format(fmt), result.updatedAt)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = NoteCommentEntity(
            id = 1L,
            careRecipientId = 3L,
            noteId = 10L,
            content = "往復テスト",
            createdBy = "user789",
            createdAt = TestDataFixtures.NOW.withHour(18).withMinute(0).format(fmt),
            updatedAt = TestDataFixtures.NOW.withHour(18).withMinute(0).format(fmt)
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
                createdAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
                updatedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
            ),
            NoteCommentEntity(
                id = 2L, noteId = 10L, content = "コメント2",
                createdAt = TestDataFixtures.NOW.withHour(9).withMinute(0).format(fmt),
                updatedAt = TestDataFixtures.NOW.withHour(9).withMinute(0).format(fmt)
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
            createdAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            updatedAt = TestDataFixtures.NOW.withHour(8).withMinute(5).format(fmt)
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
            createdAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            updatedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        assertEquals("", mapper.toDomain(entity).content)
    }

    @Test
    fun `toDomain with empty createdBy`() {
        val entity = NoteCommentEntity(
            id = 1L, noteId = 10L, content = "テスト", createdBy = "",
            createdAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt),
            updatedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        assertEquals("", mapper.toDomain(entity).createdBy)
    }
}
