package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.CareRecipientDao
import com.carenote.app.data.local.entity.CareRecipientEntity
import com.carenote.app.data.mapper.CareRecipientMapper
import com.carenote.app.domain.model.Gender
import com.carenote.app.testing.assertDatabaseError
import com.carenote.app.testing.assertSuccess
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CareRecipientRepositoryImplTest {

    private lateinit var dao: CareRecipientDao
    private lateinit var mapper: CareRecipientMapper
    private lateinit var repository: CareRecipientRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = CareRecipientMapper()
        repository = CareRecipientRepositoryImpl(dao, mapper)
    }

    private fun createEntity(
        id: Long = 1L,
        name: String = "山田太郎",
        birthDate: String? = "1940-05-15",
        gender: String = "MALE",
        memo: String = "テストメモ",
        firestoreId: String? = null
    ) = CareRecipientEntity(
        id = id,
        name = name,
        birthDate = birthDate,
        gender = gender,
        memo = memo,
        createdAt = "2025-03-15T10:00:00",
        updatedAt = "2025-03-15T10:00:00",
        firestoreId = firestoreId
    )

    @Test
    fun `getCareRecipient returns care recipient when exists`() = runTest {
        val entity = createEntity()
        every { dao.getCareRecipient() } returns flowOf(entity)

        repository.getCareRecipient().test {
            val result = awaitItem()
            assertEquals(1L, result?.id)
            assertEquals("山田太郎", result?.name)
            assertEquals(Gender.MALE, result?.gender)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCareRecipient returns null when not exists`() = runTest {
        every { dao.getCareRecipient() } returns flowOf(null)

        repository.getCareRecipient().test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCareRecipient maps null birthDate correctly`() = runTest {
        val entity = createEntity(birthDate = null)
        every { dao.getCareRecipient() } returns flowOf(entity)

        repository.getCareRecipient().test {
            val result = awaitItem()
            assertNull(result?.birthDate)
            assertEquals("山田太郎", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCareRecipient falls back to UNSPECIFIED for invalid gender`() = runTest {
        val entity = createEntity(gender = "INVALID_GENDER")
        every { dao.getCareRecipient() } returns flowOf(entity)

        repository.getCareRecipient().test {
            val result = awaitItem()
            assertEquals(Gender.UNSPECIFIED, result?.gender)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveCareRecipient returns Success`() = runTest {
        coEvery { dao.insertOrUpdate(any()) } returns 1L

        val careRecipient = mapper.toDomain(createEntity())
        val result = repository.saveCareRecipient(careRecipient)

        result.assertSuccess()
        coVerify { dao.insertOrUpdate(any()) }
    }

    @Test
    fun `saveCareRecipient returns Failure on db error`() = runTest {
        coEvery { dao.insertOrUpdate(any()) } throws RuntimeException("DB error")

        val careRecipient = mapper.toDomain(createEntity())
        val result = repository.saveCareRecipient(careRecipient)

        result.assertDatabaseError()
    }

    @Test
    fun `updateFirestoreId returns Success`() = runTest {
        coEvery { dao.updateFirestoreId(any(), any()) } returns Unit

        val result = repository.updateFirestoreId(1L, "firestore-abc")

        result.assertSuccess()
        coVerify { dao.updateFirestoreId(1L, "firestore-abc") }
    }

    @Test
    fun `updateFirestoreId returns Failure on db error`() = runTest {
        coEvery { dao.updateFirestoreId(any(), any()) } throws RuntimeException("DB error")

        val result = repository.updateFirestoreId(1L, "firestore-abc")

        result.assertDatabaseError()
    }
}
