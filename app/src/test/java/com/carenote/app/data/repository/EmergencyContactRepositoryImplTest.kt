package com.carenote.app.data.repository

import app.cash.turbine.test
import com.carenote.app.data.local.dao.EmergencyContactDao
import com.carenote.app.data.local.entity.EmergencyContactEntity
import com.carenote.app.data.mapper.EmergencyContactMapper
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.EmergencyContact
import com.carenote.app.domain.model.RelationshipType
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
import java.time.LocalDateTime

class EmergencyContactRepositoryImplTest {

    private lateinit var dao: EmergencyContactDao
    private lateinit var mapper: EmergencyContactMapper
    private lateinit var repository: EmergencyContactRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = EmergencyContactMapper()
        repository = EmergencyContactRepositoryImpl(dao, mapper)
    }

    private fun createEntity(
        id: Long = 1L,
        name: String = "テスト太郎",
        phoneNumber: String = "090-1234-5678",
        relationship: String = "FAMILY"
    ) = EmergencyContactEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        memo = "",
        createdAt = "2026-01-01T10:00:00",
        updatedAt = "2026-01-01T10:00:00"
    )

    @Test
    fun `getAllContacts returns flow of contacts`() = runTest {
        val entities = listOf(
            createEntity(1L, "A"),
            createEntity(2L, "B")
        )
        every { dao.getAll() } returns flowOf(entities)

        repository.getAllContacts().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("A", result[0].name)
            assertEquals("B", result[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllContacts returns empty list when no contacts`() = runTest {
        every { dao.getAll() } returns flowOf(emptyList())

        repository.getAllContacts().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getContactById returns contact when found`() = runTest {
        val entity = createEntity(1L, "テスト太郎")
        every { dao.getById(1L) } returns flowOf(entity)

        repository.getContactById(1L).test {
            val result = awaitItem()
            assertEquals("テスト太郎", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getContactById returns null when not found`() = runTest {
        every { dao.getById(999L) } returns flowOf(null)

        repository.getContactById(999L).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertContact returns success with id`() = runTest {
        coEvery { dao.insert(any()) } returns 5L

        val contact = EmergencyContact(
            name = "新規連絡先",
            phoneNumber = "090-0000-0000",
            relationship = RelationshipType.DOCTOR,
            createdAt = LocalDateTime.of(2026, 1, 1, 10, 0),
            updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0)
        )
        val result = repository.insertContact(contact)

        assertTrue(result.isSuccess)
        assertEquals(5L, (result as Result.Success).value)
        coVerify { dao.insert(any()) }
    }

    @Test
    fun `deleteContact returns success`() = runTest {
        coEvery { dao.deleteById(1L) } returns Unit

        val result = repository.deleteContact(1L)

        assertTrue(result.isSuccess)
        coVerify { dao.deleteById(1L) }
    }
}
