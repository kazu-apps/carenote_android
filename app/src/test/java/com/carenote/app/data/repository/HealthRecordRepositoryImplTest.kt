package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.HealthRecordDao
import com.carenote.app.data.local.entity.HealthRecordEntity
import com.carenote.app.data.mapper.HealthRecordMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.repository.PhotoRepository
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class HealthRecordRepositoryImplTest {

    private lateinit var dao: HealthRecordDao
    private lateinit var mapper: HealthRecordMapper
    private lateinit var photoRepository: PhotoRepository
    private lateinit var repository: HealthRecordRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = HealthRecordMapper()
        photoRepository = mockk()
        repository = HealthRecordRepositoryImpl(dao, mapper, photoRepository)
    }

    private fun createEntity(
        id: Long = 1L,
        temperature: Double? = 36.5,
        bloodPressureHigh: Int? = 120,
        bloodPressureLow: Int? = 80,
        pulse: Int? = 72,
        weight: Double? = 65.0,
        meal: String? = "FULL",
        excretion: String? = "NORMAL",
        conditionNote: String = "体調良好"
    ) = HealthRecordEntity(
        id = id,
        temperature = temperature,
        bloodPressureHigh = bloodPressureHigh,
        bloodPressureLow = bloodPressureLow,
        pulse = pulse,
        weight = weight,
        meal = meal,
        excretion = excretion,
        conditionNote = conditionNote,
        recordedAt = "2025-03-15T10:00:00",
        createdAt = "2025-03-15T10:00:00",
        updatedAt = "2025-03-15T10:00:00"
    )

    @Test
    fun `getAllRecords returns flow of health records`() = runTest {
        val entities = listOf(
            createEntity(1L, conditionNote = "記録A"),
            createEntity(2L, conditionNote = "記録B")
        )
        every { dao.getAllRecords() } returns flowOf(entities)

        repository.getAllRecords().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("記録A", result[0].conditionNote)
            assertEquals("記録B", result[1].conditionNote)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllRecords returns empty list when no records`() = runTest {
        every { dao.getAllRecords() } returns flowOf(emptyList())

        repository.getAllRecords().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecordById returns record when found`() = runTest {
        val entity = createEntity(1L, conditionNote = "テスト記録")
        every { dao.getRecordById(1L) } returns flowOf(entity)

        repository.getRecordById(1L).test {
            val result = awaitItem()
            assertEquals("テスト記録", result?.conditionNote)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecordById returns null when not found`() = runTest {
        every { dao.getRecordById(999L) } returns flowOf(null)

        repository.getRecordById(999L).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecordsByDateRange returns records in range`() = runTest {
        val entities = listOf(
            createEntity(1L, conditionNote = "記録A"),
            createEntity(2L, conditionNote = "記録B")
        )
        every {
            dao.getRecordsByDateRange(
                "2025-03-01T00:00:00",
                "2025-03-31T23:59:59"
            )
        } returns flowOf(entities)

        val start = LocalDateTime.of(2025, 3, 1, 0, 0, 0)
        val end = LocalDateTime.of(2025, 3, 31, 23, 59, 59)
        repository.getRecordsByDateRange(start, end).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecordsByDateRange returns empty list when no records in range`() = runTest {
        every {
            dao.getRecordsByDateRange(any(), any())
        } returns flowOf(emptyList())

        val start = LocalDateTime.of(2025, 1, 1, 0, 0)
        val end = LocalDateTime.of(2025, 1, 31, 23, 59, 59)
        repository.getRecordsByDateRange(start, end).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertRecord returns Success with id`() = runTest {
        coEvery { dao.insertRecord(any()) } returns 1L

        val record = HealthRecord(
            temperature = 36.5,
            conditionNote = "テスト",
            recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertRecord(record)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).value)
    }

    @Test
    fun `insertRecord returns Failure on db error`() = runTest {
        coEvery { dao.insertRecord(any()) } throws RuntimeException("DB error")

        val record = HealthRecord(
            temperature = 36.5,
            conditionNote = "テスト",
            recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.insertRecord(record)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `updateRecord returns Success`() = runTest {
        coEvery { dao.updateRecord(any()) } returns Unit

        val record = HealthRecord(
            id = 1L,
            temperature = 37.0,
            conditionNote = "更新テスト",
            recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 11, 0)
        )
        val result = repository.updateRecord(record)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateRecord returns Failure on db error`() = runTest {
        coEvery { dao.updateRecord(any()) } throws RuntimeException("DB error")

        val record = HealthRecord(
            id = 1L,
            temperature = 37.0,
            conditionNote = "更新テスト",
            recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 11, 0)
        )
        val result = repository.updateRecord(record)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `deleteRecord returns Success`() = runTest {
        coEvery { photoRepository.deletePhotosForParent("health_record", 1L) } returns Result.Success(Unit)
        coEvery { dao.deleteRecord(1L) } returns Unit

        val result = repository.deleteRecord(1L)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteRecord(1L) }
    }

    @Test
    fun `deleteRecord returns Failure on db error`() = runTest {
        coEvery { photoRepository.deletePhotosForParent("health_record", 1L) } returns Result.Success(Unit)
        coEvery { dao.deleteRecord(1L) } throws RuntimeException("DB error")

        val result = repository.deleteRecord(1L)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `deleteRecord cascades photo deletion`() = runTest {
        coEvery { photoRepository.deletePhotosForParent("health_record", 5L) } returns Result.Success(Unit)
        coEvery { dao.deleteRecord(5L) } returns Unit

        repository.deleteRecord(5L)

        coVerify { photoRepository.deletePhotosForParent("health_record", 5L) }
        coVerify { dao.deleteRecord(5L) }
    }

    @Test
    fun `deleteRecord deletes photos before record`() = runTest {
        val callOrder = mutableListOf<String>()
        coEvery { photoRepository.deletePhotosForParent("health_record", 1L) } coAnswers {
            callOrder.add("photos")
            Result.Success(Unit)
        }
        coEvery { dao.deleteRecord(1L) } coAnswers {
            callOrder.add("record")
            Unit
        }

        repository.deleteRecord(1L)

        assertEquals(listOf("photos", "record"), callOrder)
    }

    @Test
    fun `deleteRecord still succeeds when photo deletion fails`() = runTest {
        coEvery {
            photoRepository.deletePhotosForParent("health_record", 1L)
        } returns Result.Failure(DomainError.DatabaseError("Photo DB error"))
        coEvery { dao.deleteRecord(1L) } returns Unit

        val result = repository.deleteRecord(1L)

        assertTrue(result is Result.Success)
        coVerify { photoRepository.deletePhotosForParent("health_record", 1L) }
        coVerify { dao.deleteRecord(1L) }
    }
}
