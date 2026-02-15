package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.data.local.dao.InvitationDao
import com.carenote.app.data.local.entity.InvitationEntity
import com.carenote.app.data.mapper.InvitationMapper
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.fakes.FakeActiveCareRecipientProvider
import com.carenote.app.testing.TestDataFixtures
import com.carenote.app.testing.assertDatabaseError
import com.carenote.app.testing.assertSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.format.DateTimeFormatter

class InvitationRepositoryImplTest {

    private lateinit var dao: InvitationDao
    private lateinit var mapper: InvitationMapper
    private lateinit var activeRecipientProvider: FakeActiveCareRecipientProvider
    private lateinit var repository: InvitationRepositoryImpl

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Before
    fun setUp() {
        dao = mockk()
        mapper = InvitationMapper()
        activeRecipientProvider = FakeActiveCareRecipientProvider()
        repository = InvitationRepositoryImpl(dao, mapper, activeRecipientProvider)
    }

    private fun createEntity(
        id: Long = 1L,
        inviterUid: String = "inviter",
        inviteeEmail: String = "invitee@example.com",
        status: String = "PENDING",
        token: String = "test-token"
    ) = InvitationEntity(
        id = id,
        careRecipientId = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
        inviterUid = inviterUid,
        inviteeEmail = inviteeEmail,
        status = status,
        token = token,
        expiresAt = TestDataFixtures.NOW.plusDays(7).format(fmt),
        createdAt = TestDataFixtures.NOW.format(fmt)
    )

    @Test
    fun `getAllInvitations returns flow`() = runTest {
        val entities = listOf(
            createEntity(1L, token = "t1"),
            createEntity(2L, token = "t2")
        )
        every { dao.getAllInvitations(1L) } returns flowOf(entities)

        repository.getAllInvitations().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("t1", result[0].token)
            assertEquals("t2", result[1].token)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllInvitations returns empty list`() = runTest {
        every { dao.getAllInvitations(1L) } returns flowOf(emptyList())

        repository.getAllInvitations().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInvitationById returns invitation when found`() = runTest {
        val entity = createEntity(1L, token = "found-token")
        every { dao.getInvitationById(1L) } returns flowOf(entity)

        repository.getInvitationById(1L).test {
            val result = awaitItem()
            assertEquals("found-token", result?.token)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInvitationById returns null when not found`() = runTest {
        every { dao.getInvitationById(999L) } returns flowOf(null)

        repository.getInvitationById(999L).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInvitationByToken returns invitation`() = runTest {
        val entity = createEntity(1L, token = "unique-token")
        every { dao.getInvitationByToken("unique-token") } returns flowOf(entity)

        repository.getInvitationByToken("unique-token").test {
            val result = awaitItem()
            assertEquals("unique-token", result?.token)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInvitationByToken returns null when not found`() = runTest {
        every { dao.getInvitationByToken("missing-token") } returns flowOf(null)

        repository.getInvitationByToken("missing-token").test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInvitationsByEmail returns matching invitations`() = runTest {
        val entities = listOf(
            createEntity(1L, inviteeEmail = "match@example.com", token = "t1"),
            createEntity(2L, inviteeEmail = "match@example.com", token = "t2")
        )
        every { dao.getInvitationsByEmail("match@example.com", "PENDING") } returns flowOf(entities)

        repository.getInvitationsByEmail("match@example.com", InvitationStatus.PENDING).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInvitationsByEmail returns empty list`() = runTest {
        every { dao.getInvitationsByEmail("none@example.com", "PENDING") } returns flowOf(emptyList())

        repository.getInvitationsByEmail("none@example.com", InvitationStatus.PENDING).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertInvitation returns success with id`() = runTest {
        coEvery { dao.insertInvitation(any()) } returns 5L

        val invitation = Invitation(
            inviterUid = "inviter",
            inviteeEmail = "new@example.com",
            status = InvitationStatus.PENDING,
            token = "new-token",
            expiresAt = TestDataFixtures.NOW.plusDays(7),
            createdAt = TestDataFixtures.NOW
        )
        val result = repository.insertInvitation(invitation)

        val value = result.assertSuccess()
        assertEquals(5L, value)
        coVerify { dao.insertInvitation(any()) }
    }

    @Test
    fun `insertInvitation sets careRecipientId from provider`() = runTest {
        activeRecipientProvider.setActiveCareRecipientId(42L)
        coEvery { dao.insertInvitation(any()) } answers {
            val entity = firstArg<InvitationEntity>()
            assertEquals(42L, entity.careRecipientId)
            1L
        }

        val invitation = Invitation(
            inviterUid = "inviter",
            inviteeEmail = "new@example.com",
            status = InvitationStatus.PENDING,
            token = "new-token",
            expiresAt = TestDataFixtures.NOW.plusDays(7),
            createdAt = TestDataFixtures.NOW
        )
        repository.insertInvitation(invitation)

        coVerify { dao.insertInvitation(any()) }
    }

    @Test
    fun `updateInvitation returns success`() = runTest {
        coEvery { dao.updateInvitation(any()) } returns Unit

        val invitation = Invitation(
            id = 1L,
            inviterUid = "inviter",
            inviteeEmail = "existing@example.com",
            status = InvitationStatus.ACCEPTED,
            token = "existing-token",
            expiresAt = TestDataFixtures.NOW.plusDays(7),
            createdAt = TestDataFixtures.NOW
        )
        val result = repository.updateInvitation(invitation)

        result.assertSuccess()
        coVerify { dao.updateInvitation(any()) }
    }

    @Test
    fun `deleteInvitation returns success`() = runTest {
        coEvery { dao.deleteInvitation(1L) } returns Unit

        val result = repository.deleteInvitation(1L)

        result.assertSuccess()
        coVerify { dao.deleteInvitation(1L) }
    }

    @Test
    fun `insertInvitation returns failure on dao error`() = runTest {
        coEvery { dao.insertInvitation(any()) } throws RuntimeException("DB error")

        val invitation = Invitation(
            inviterUid = "inviter",
            inviteeEmail = "new@example.com",
            status = InvitationStatus.PENDING,
            token = "new-token",
            expiresAt = TestDataFixtures.NOW.plusDays(7),
            createdAt = TestDataFixtures.NOW
        )
        val result = repository.insertInvitation(invitation)

        result.assertDatabaseError()
    }

    @Test
    fun `updateInvitation returns failure on dao error`() = runTest {
        coEvery { dao.updateInvitation(any()) } throws RuntimeException("DB error")

        val invitation = Invitation(
            id = 1L,
            inviterUid = "inviter",
            inviteeEmail = "existing@example.com",
            status = InvitationStatus.ACCEPTED,
            token = "existing-token",
            expiresAt = TestDataFixtures.NOW.plusDays(7),
            createdAt = TestDataFixtures.NOW
        )
        val result = repository.updateInvitation(invitation)

        result.assertDatabaseError()
    }
}
