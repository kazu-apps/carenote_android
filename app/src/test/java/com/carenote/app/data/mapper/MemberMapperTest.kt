package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.MemberEntity
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.format.DateTimeFormatter

class MemberMapperTest {

    private lateinit var mapper: MemberMapper

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Before
    fun setUp() {
        mapper = MemberMapper()
    }

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = MemberEntity(
            id = 1L,
            careRecipientId = 5L,
            uid = "user123",
            role = "MEMBER",
            joinedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals("user123", result.uid)
        assertEquals(MemberRole.MEMBER, result.role)
        assertEquals(TestDataFixtures.NOW.withHour(8).withMinute(0), result.joinedAt)
    }

    @Test
    fun `toDomain parses OWNER role`() {
        val entity = MemberEntity(
            id = 1L,
            careRecipientId = 5L,
            uid = "owner123",
            role = "OWNER",
            joinedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        val result = mapper.toDomain(entity)

        assertEquals(MemberRole.OWNER, result.role)
    }

    @Test
    fun `toDomain falls back to MEMBER for unknown role`() {
        val entity = MemberEntity(
            id = 1L,
            careRecipientId = 5L,
            uid = "user123",
            role = "INVALID_ROLE",
            joinedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        val result = mapper.toDomain(entity)

        assertEquals(MemberRole.MEMBER, result.role)
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = Member(
            id = 1L,
            careRecipientId = 5L,
            uid = "user456",
            role = MemberRole.OWNER,
            joinedAt = TestDataFixtures.NOW.withHour(12).withMinute(0)
        )

        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals("user456", result.uid)
        assertEquals("OWNER", result.role)
        assertEquals(TestDataFixtures.NOW.withHour(12).withMinute(0).format(fmt), result.joinedAt)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = MemberEntity(
            id = 1L,
            careRecipientId = 3L,
            uid = "user789",
            role = "OWNER",
            joinedAt = TestDataFixtures.NOW.withHour(18).withMinute(0).format(fmt)
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.careRecipientId, roundtrip.careRecipientId)
        assertEquals(original.uid, roundtrip.uid)
        assertEquals(original.role, roundtrip.role)
        assertEquals(original.joinedAt, roundtrip.joinedAt)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            MemberEntity(
                id = 1L, uid = "user1", role = "MEMBER",
                joinedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
            ),
            MemberEntity(
                id = 2L, uid = "user2", role = "OWNER",
                joinedAt = TestDataFixtures.NOW.withHour(9).withMinute(0).format(fmt)
            )
        )

        val result = mapper.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals("user1", result[0].uid)
        assertEquals("user2", result[1].uid)
    }

    @Test
    fun `careRecipientId maps correctly in roundtrip`() {
        val entity = MemberEntity(
            id = 1L,
            careRecipientId = 42L,
            uid = "user123",
            role = "MEMBER",
            joinedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    @Test
    fun `toDomain with empty uid`() {
        val entity = MemberEntity(
            id = 1L, uid = "", role = "MEMBER",
            joinedAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        assertEquals("", mapper.toDomain(entity).uid)
    }
}
