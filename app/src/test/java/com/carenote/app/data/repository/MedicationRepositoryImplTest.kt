package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.MedicationDao
import com.carenote.app.data.local.entity.MedicationEntity
import com.carenote.app.data.mapper.MedicationMapper
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationTiming
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class MedicationRepositoryImplTest {

    private lateinit var dao: MedicationDao
    private lateinit var mapper: MedicationMapper
    private lateinit var repository: MedicationRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = MedicationMapper()
        repository = MedicationRepositoryImpl(dao, mapper)
    }

    private fun createEntity(
        id: Long = 1L,
        name: String = "テスト薬"
    ) = MedicationEntity(
        id = id,
        name = name,
        dosage = "1錠",
        timings = "MORNING",
        times = "MORNING=08:00",
        reminderEnabled = true,
        createdAt = "2025-03-15T10:00:00",
        updatedAt = "2025-03-15T10:00:00"
    )

    @Test
    fun `getAllMedications returns flow of medications`() = runTest {
        val entities = listOf(createEntity(1L, "薬A"), createEntity(2L, "薬B"))
        every { dao.getAllMedications() } returns flowOf(entities)

        repository.getAllMedications().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("薬A", result[0].name)
            assertEquals("薬B", result[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllMedications returns empty list when no medications`() = runTest {
        every { dao.getAllMedications() } returns flowOf(emptyList())

        repository.getAllMedications().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMedicationById returns medication when found`() = runTest {
        val entity = createEntity(1L, "テスト薬")
        every { dao.getMedicationById(1L) } returns flowOf(entity)

        repository.getMedicationById(1L).test {
            val result = awaitItem()
            assertEquals("テスト薬", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMedicationById returns null when not found`() = runTest {
        every { dao.getMedicationById(999L) } returns flowOf(null)

        repository.getMedicationById(999L).test {
            val result = awaitItem()
            assertEquals(null, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertMedication returns Success with id`() = runTest {
        coEvery { dao.insertMedication(any()) } returns 1L

        val medication = Medication(
            name = "テスト薬",
            timings = listOf(MedicationTiming.MORNING),
            times = mapOf(MedicationTiming.MORNING to LocalTime.of(8, 0))
        )
        val result = repository.insertMedication(medication)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).value)
    }

    @Test
    fun `insertMedication returns Failure on db error`() = runTest {
        coEvery { dao.insertMedication(any()) } throws RuntimeException("DB error")

        val medication = Medication(name = "テスト薬")
        val result = repository.insertMedication(medication)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is DomainError.DatabaseError)
    }

    @Test
    fun `updateMedication returns Success`() = runTest {
        coEvery { dao.updateMedication(any()) } returns Unit

        val medication = Medication(
            id = 1L,
            name = "更新薬",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.updateMedication(medication)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateMedication returns Failure on db error`() = runTest {
        coEvery { dao.updateMedication(any()) } throws RuntimeException("DB error")

        val medication = Medication(
            id = 1L,
            name = "更新薬",
            createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
            updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
        )
        val result = repository.updateMedication(medication)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `deleteMedication returns Success`() = runTest {
        coEvery { dao.deleteMedication(1L) } returns Unit

        val result = repository.deleteMedication(1L)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteMedication(1L) }
    }

    @Test
    fun `deleteMedication returns Failure on db error`() = runTest {
        coEvery { dao.deleteMedication(1L) } throws RuntimeException("DB error")

        val result = repository.deleteMedication(1L)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `insertMedication calls dao with mapped entity`() = runTest {
        coEvery { dao.insertMedication(any()) } returns 1L

        val medication = Medication(
            name = "テスト薬",
            dosage = "2錠",
            timings = listOf(MedicationTiming.MORNING, MedicationTiming.EVENING),
            times = mapOf(
                MedicationTiming.MORNING to LocalTime.of(8, 0),
                MedicationTiming.EVENING to LocalTime.of(18, 0)
            )
        )
        repository.insertMedication(medication)

        coVerify { dao.insertMedication(match { it.name == "テスト薬" && it.dosage == "2錠" }) }
    }

    @Test
    fun `getAllMedications maps timings correctly`() = runTest {
        val entity = MedicationEntity(
            id = 1L,
            name = "テスト薬",
            timings = "MORNING,NOON,EVENING",
            times = "MORNING=08:00;NOON=12:00;EVENING=18:00",
            createdAt = "2025-03-15T10:00:00",
            updatedAt = "2025-03-15T10:00:00"
        )
        every { dao.getAllMedications() } returns flowOf(listOf(entity))

        repository.getAllMedications().test {
            val result = awaitItem()
            assertEquals(3, result[0].timings.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
