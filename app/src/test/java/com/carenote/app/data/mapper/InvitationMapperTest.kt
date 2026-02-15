package com.carenote.app.data.mapper

import com.carenote.app.data.local.entity.InvitationEntity
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.testing.TestDataFixtures
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.format.DateTimeFormatter

class InvitationMapperTest {

    private lateinit var mapper: InvitationMapper

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Before
    fun setUp() {
        mapper = InvitationMapper()
    }

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = InvitationEntity(
            id = 1L,
            careRecipientId = 5L,
            inviterUid = "inviter123",
            inviteeEmail = "invitee@example.com",
            status = "PENDING",
            token = "token-abc",
            expiresAt = TestDataFixtures.NOW.withHour(23).withMinute(59).format(fmt),
            createdAt = TestDataFixtures.NOW.withHour(8).withMinute(0).format(fmt)
        )

        val result = mapper.toDomain(entity)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals("inviter123", result.inviterUid)
        assertEquals("invitee@example.com", result.inviteeEmail)
        assertEquals(InvitationStatus.PENDING, result.status)
        assertEquals("token-abc", result.token)
        assertEquals(TestDataFixtures.NOW.withHour(23).withMinute(59), result.expiresAt)
        assertEquals(TestDataFixtures.NOW.withHour(8).withMinute(0), result.createdAt)
    }

    @Test
    fun `toDomain parses ACCEPTED status`() {
        val entity = InvitationEntity(
            id = 1L, inviterUid = "u", inviteeEmail = "e@e.com",
            status = "ACCEPTED", token = "t",
            expiresAt = TestDataFixtures.NOW.format(fmt),
            createdAt = TestDataFixtures.NOW.format(fmt)
        )

        assertEquals(InvitationStatus.ACCEPTED, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain parses REJECTED status`() {
        val entity = InvitationEntity(
            id = 1L, inviterUid = "u", inviteeEmail = "e@e.com",
            status = "REJECTED", token = "t",
            expiresAt = TestDataFixtures.NOW.format(fmt),
            createdAt = TestDataFixtures.NOW.format(fmt)
        )

        assertEquals(InvitationStatus.REJECTED, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain parses EXPIRED status`() {
        val entity = InvitationEntity(
            id = 1L, inviterUid = "u", inviteeEmail = "e@e.com",
            status = "EXPIRED", token = "t",
            expiresAt = TestDataFixtures.NOW.format(fmt),
            createdAt = TestDataFixtures.NOW.format(fmt)
        )

        assertEquals(InvitationStatus.EXPIRED, mapper.toDomain(entity).status)
    }

    @Test
    fun `toDomain falls back to PENDING for unknown status`() {
        val entity = InvitationEntity(
            id = 1L, inviterUid = "u", inviteeEmail = "e@e.com",
            status = "INVALID_STATUS", token = "t",
            expiresAt = TestDataFixtures.NOW.format(fmt),
            createdAt = TestDataFixtures.NOW.format(fmt)
        )

        assertEquals(InvitationStatus.PENDING, mapper.toDomain(entity).status)
    }

    @Test
    fun `toEntity maps domain to entity`() {
        val domain = Invitation(
            id = 1L,
            careRecipientId = 5L,
            inviterUid = "inviter456",
            inviteeEmail = "user@example.com",
            status = InvitationStatus.ACCEPTED,
            token = "token-xyz",
            expiresAt = TestDataFixtures.NOW.withHour(12).withMinute(0),
            createdAt = TestDataFixtures.NOW.withHour(8).withMinute(30)
        )

        val result = mapper.toEntity(domain)

        assertEquals(1L, result.id)
        assertEquals(5L, result.careRecipientId)
        assertEquals("inviter456", result.inviterUid)
        assertEquals("user@example.com", result.inviteeEmail)
        assertEquals("ACCEPTED", result.status)
        assertEquals("token-xyz", result.token)
        assertEquals(TestDataFixtures.NOW.withHour(12).withMinute(0).format(fmt), result.expiresAt)
        assertEquals(TestDataFixtures.NOW.withHour(8).withMinute(30).format(fmt), result.createdAt)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = InvitationEntity(
            id = 1L,
            careRecipientId = 3L,
            inviterUid = "inviter789",
            inviteeEmail = "test@example.com",
            status = "PENDING",
            token = "token-roundtrip",
            expiresAt = TestDataFixtures.NOW.withHour(18).withMinute(0).format(fmt),
            createdAt = TestDataFixtures.NOW.withHour(10).withMinute(0).format(fmt)
        )

        val domain = mapper.toDomain(original)
        val roundtrip = mapper.toEntity(domain)

        assertEquals(original.id, roundtrip.id)
        assertEquals(original.careRecipientId, roundtrip.careRecipientId)
        assertEquals(original.inviterUid, roundtrip.inviterUid)
        assertEquals(original.inviteeEmail, roundtrip.inviteeEmail)
        assertEquals(original.status, roundtrip.status)
        assertEquals(original.token, roundtrip.token)
        assertEquals(original.expiresAt, roundtrip.expiresAt)
        assertEquals(original.createdAt, roundtrip.createdAt)
    }

    @Test
    fun `toDomainList maps list of entities`() {
        val entities = listOf(
            InvitationEntity(
                id = 1L, inviterUid = "u1", inviteeEmail = "a@e.com",
                status = "PENDING", token = "t1",
                expiresAt = TestDataFixtures.NOW.format(fmt),
                createdAt = TestDataFixtures.NOW.format(fmt)
            ),
            InvitationEntity(
                id = 2L, inviterUid = "u2", inviteeEmail = "b@e.com",
                status = "ACCEPTED", token = "t2",
                expiresAt = TestDataFixtures.NOW.format(fmt),
                createdAt = TestDataFixtures.NOW.format(fmt)
            )
        )

        val result = mapper.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals("a@e.com", result[0].inviteeEmail)
        assertEquals("b@e.com", result[1].inviteeEmail)
    }

    @Test
    fun `careRecipientId maps correctly`() {
        val entity = InvitationEntity(
            id = 1L, careRecipientId = 42L,
            inviterUid = "u", inviteeEmail = "e@e.com",
            status = "PENDING", token = "t",
            expiresAt = TestDataFixtures.NOW.format(fmt),
            createdAt = TestDataFixtures.NOW.format(fmt)
        )
        val domain = mapper.toDomain(entity)
        assertEquals(42L, domain.careRecipientId)
        val roundtrip = mapper.toEntity(domain)
        assertEquals(42L, roundtrip.careRecipientId)
    }

    @Test
    fun `toDomain with empty inviteeEmail`() {
        val entity = InvitationEntity(
            id = 1L, inviterUid = "u", inviteeEmail = "",
            status = "PENDING", token = "t",
            expiresAt = TestDataFixtures.NOW.format(fmt),
            createdAt = TestDataFixtures.NOW.format(fmt)
        )

        assertEquals("", mapper.toDomain(entity).inviteeEmail)
    }

    @Test
    fun `toEntity with EXPIRED status`() {
        val domain = Invitation(
            id = 1L,
            careRecipientId = 5L,
            inviterUid = "u",
            inviteeEmail = "e@e.com",
            status = InvitationStatus.EXPIRED,
            token = "t",
            expiresAt = TestDataFixtures.NOW,
            createdAt = TestDataFixtures.NOW
        )

        assertEquals("EXPIRED", mapper.toEntity(domain).status)
    }

    @Test
    fun `toDomain with empty token`() {
        val entity = InvitationEntity(
            id = 1L, inviterUid = "u", inviteeEmail = "e@e.com",
            status = "PENDING", token = "",
            expiresAt = TestDataFixtures.NOW.format(fmt),
            createdAt = TestDataFixtures.NOW.format(fmt)
        )

        assertEquals("", mapper.toDomain(entity).token)
    }
}
