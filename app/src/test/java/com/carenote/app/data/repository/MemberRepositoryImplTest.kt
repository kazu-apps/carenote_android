package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.data.local.dao.MemberDao
import com.carenote.app.data.local.entity.MemberEntity
import com.carenote.app.data.mapper.MemberMapper
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
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

class MemberRepositoryImplTest {

    private lateinit var dao: MemberDao
    private lateinit var mapper: MemberMapper
    private lateinit var activeRecipientProvider: FakeActiveCareRecipientProvider
    private lateinit var repository: MemberRepositoryImpl

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Before
    fun setUp() {
        dao = mockk()
        mapper = MemberMapper()
        activeRecipientProvider = FakeActiveCareRecipientProvider()
        repository = MemberRepositoryImpl(dao, mapper, activeRecipientProvider)
    }

    private fun createEntity(
        id: Long = 1L,
        uid: String = "testUid",
        role: String = "MEMBER"
    ) = MemberEntity(
        id = id,
        careRecipientId = TestDataFixtures.DEFAULT_CARE_RECIPIENT_ID,
        uid = uid,
        role = role,
        joinedAt = TestDataFixtures.NOW.format(fmt)
    )

    @Test
    fun `getAllMembers returns flow of members`() = runTest {
        val entities = listOf(
            createEntity(1L, "userA"),
            createEntity(2L, "userB")
        )
        every { dao.getAllMembers(1L) } returns flowOf(entities)

        repository.getAllMembers().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("userA", result[0].uid)
            assertEquals("userB", result[1].uid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllMembers returns empty list`() = runTest {
        every { dao.getAllMembers(1L) } returns flowOf(emptyList())

        repository.getAllMembers().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMemberById returns member when found`() = runTest {
        val entity = createEntity(1L, "testUid")
        every { dao.getMemberById(1L) } returns flowOf(entity)

        repository.getMemberById(1L).test {
            val result = awaitItem()
            assertEquals("testUid", result?.uid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMemberById returns null when not found`() = runTest {
        every { dao.getMemberById(999L) } returns flowOf(null)

        repository.getMemberById(999L).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMemberByUid returns member when found`() = runTest {
        val entity = createEntity(1L, "targetUid")
        every { dao.getMemberByUid(1L, "targetUid") } returns flowOf(entity)

        repository.getMemberByUid("targetUid").test {
            val result = awaitItem()
            assertEquals("targetUid", result?.uid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMemberByUid returns null when not found`() = runTest {
        every { dao.getMemberByUid(1L, "unknownUid") } returns flowOf(null)

        repository.getMemberByUid("unknownUid").test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertMember returns success with id`() = runTest {
        coEvery { dao.insertMember(any()) } returns 5L

        val member = Member(
            uid = "newUser",
            role = MemberRole.MEMBER,
            joinedAt = TestDataFixtures.NOW
        )
        val result = repository.insertMember(member)

        val value = result.assertSuccess()
        assertEquals(5L, value)
        coVerify { dao.insertMember(any()) }
    }

    @Test
    fun `insertMember sets careRecipientId from provider`() = runTest {
        activeRecipientProvider.setActiveCareRecipientId(42L)
        coEvery { dao.insertMember(any()) } answers {
            val entity = firstArg<MemberEntity>()
            assertEquals(42L, entity.careRecipientId)
            1L
        }

        val member = Member(
            uid = "newUser",
            role = MemberRole.MEMBER,
            joinedAt = TestDataFixtures.NOW
        )
        repository.insertMember(member)

        coVerify { dao.insertMember(any()) }
    }

    @Test
    fun `updateMember returns success`() = runTest {
        coEvery { dao.updateMember(any()) } returns Unit

        val member = Member(
            id = 1L,
            uid = "existingUser",
            role = MemberRole.OWNER,
            joinedAt = TestDataFixtures.NOW
        )
        val result = repository.updateMember(member)

        result.assertSuccess()
        coVerify { dao.updateMember(any()) }
    }

    @Test
    fun `deleteMember returns success`() = runTest {
        coEvery { dao.deleteMember(1L) } returns Unit

        val result = repository.deleteMember(1L)

        result.assertSuccess()
        coVerify { dao.deleteMember(1L) }
    }

    @Test
    fun `insertMember returns failure on dao error`() = runTest {
        coEvery { dao.insertMember(any()) } throws RuntimeException("DB error")

        val member = Member(
            uid = "newUser",
            role = MemberRole.MEMBER,
            joinedAt = TestDataFixtures.NOW
        )
        val result = repository.insertMember(member)

        result.assertDatabaseError()
    }

    @Test
    fun `updateMember returns failure on dao error`() = runTest {
        coEvery { dao.updateMember(any()) } throws RuntimeException("DB error")

        val member = Member(
            id = 1L,
            uid = "existingUser",
            role = MemberRole.MEMBER,
            joinedAt = TestDataFixtures.NOW
        )
        val result = repository.updateMember(member)

        result.assertDatabaseError()
    }
}
