package com.carenote.app.data.repository

import com.carenote.app.data.local.dao.MedicationLogDao
import com.carenote.app.data.local.entity.MedicationLogEntity
import com.carenote.app.data.mapper.MedicationLogMapper
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.testing.assertDatabaseError
import com.carenote.app.testing.assertFailure
import com.carenote.app.testing.assertSuccess
import com.carenote.app.domain.model.MedicationLogStatus
import app.cash.turbine.test
import com.carenote.app.fakes.FakeActiveCareRecipientProvider
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
import java.time.LocalDate
import java.time.LocalDateTime

class MedicationLogRepositoryImplTest {

    private lateinit var dao: MedicationLogDao
    private lateinit var mapper: MedicationLogMapper
    private lateinit var activeRecipientProvider: FakeActiveCareRecipientProvider
    private lateinit var repository: MedicationLogRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        mapper = MedicationLogMapper()
        activeRecipientProvider = FakeActiveCareRecipientProvider()
        repository = MedicationLogRepositoryImpl(dao, mapper, activeRecipientProvider)
    }

    private fun createEntity(
        id: Long = 1L,
        medicationId: Long = 10L,
        status: String = "TAKEN"
    ) = MedicationLogEntity(
        id = id,
        medicationId = medicationId,
        status = status,
        scheduledAt = "2025-03-15T08:00:00",
        recordedAt = "2025-03-15T08:05:00",
        memo = ""
    )

    @Test
    fun `getLogsForMedication returns flow of logs`() = runTest {
        val entities = listOf(
            createEntity(1L, 10L, "TAKEN"),
            createEntity(2L, 10L, "SKIPPED")
        )
        every { dao.getLogsForMedication(10L) } returns flowOf(entities)

        repository.getLogsForMedication(10L).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(MedicationLogStatus.TAKEN, result[0].status)
            assertEquals(MedicationLogStatus.SKIPPED, result[1].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getLogsForMedication returns empty list when no logs`() = runTest {
        every { dao.getLogsForMedication(999L) } returns flowOf(emptyList())

        repository.getLogsForMedication(999L).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getLogsForDate queries with correct date range`() = runTest {
        val date = LocalDate.of(2025, 3, 15)
        every { dao.getLogsForDateRange(any(), any(), 1L) } returns flowOf(emptyList())

        repository.getLogsForDate(date).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            dao.getLogsForDateRange(
                match { it.startsWith("2025-03-15T") },
                match { it.startsWith("2025-03-16T") },
                any()
            )
        }
    }

    @Test
    fun `getLogsForDate returns mapped logs`() = runTest {
        val entities = listOf(createEntity(1L, 10L, "TAKEN"))
        every { dao.getLogsForDateRange(any(), any(), 1L) } returns flowOf(entities)

        repository.getLogsForDate(LocalDate.of(2025, 3, 15)).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(MedicationLogStatus.TAKEN, result[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertLog returns Success with id`() = runTest {
        coEvery { dao.insertLog(any()) } returns 1L

        val log = MedicationLog(
            medicationId = 10L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2025, 3, 15, 8, 0),
            recordedAt = LocalDateTime.of(2025, 3, 15, 8, 5)
        )
        val result = repository.insertLog(log)

        val value = result.assertSuccess()
        assertEquals(1L, value)
    }

    @Test
    fun `insertLog returns Failure on db error`() = runTest {
        coEvery { dao.insertLog(any()) } throws RuntimeException("DB error")

        val log = MedicationLog(
            medicationId = 10L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.now(),
            recordedAt = LocalDateTime.now()
        )
        val result = repository.insertLog(log)

        result.assertDatabaseError()
    }

    @Test
    fun `updateLog returns Success`() = runTest {
        coEvery { dao.updateLog(any()) } returns Unit

        val log = MedicationLog(
            id = 1L,
            medicationId = 10L,
            status = MedicationLogStatus.SKIPPED,
            scheduledAt = LocalDateTime.of(2025, 3, 15, 8, 0),
            recordedAt = LocalDateTime.of(2025, 3, 15, 8, 5)
        )
        val result = repository.updateLog(log)

        result.assertSuccess()
    }

    @Test
    fun `updateLog returns Failure on db error`() = runTest {
        coEvery { dao.updateLog(any()) } throws RuntimeException("DB error")

        val log = MedicationLog(
            id = 1L,
            medicationId = 10L,
            status = MedicationLogStatus.SKIPPED,
            scheduledAt = LocalDateTime.now(),
            recordedAt = LocalDateTime.now()
        )
        val result = repository.updateLog(log)

        result.assertFailure()
    }

    @Test
    fun `deleteLog returns Success`() = runTest {
        coEvery { dao.deleteLog(1L) } returns Unit

        val result = repository.deleteLog(1L)

        result.assertSuccess()
        coVerify { dao.deleteLog(1L) }
    }

    @Test
    fun `deleteLog returns Failure on db error`() = runTest {
        coEvery { dao.deleteLog(1L) } throws RuntimeException("DB error")

        val result = repository.deleteLog(1L)

        result.assertFailure()
    }

    @Test
    fun `insertLog calls dao with mapped entity`() = runTest {
        coEvery { dao.insertLog(any()) } returns 1L

        val log = MedicationLog(
            medicationId = 10L,
            status = MedicationLogStatus.POSTPONED,
            scheduledAt = LocalDateTime.of(2025, 3, 15, 18, 0),
            recordedAt = LocalDateTime.of(2025, 3, 15, 18, 0),
            memo = "後で飲む"
        )
        repository.insertLog(log)

        coVerify {
            dao.insertLog(match {
                it.medicationId == 10L &&
                    it.status == "POSTPONED" &&
                    it.memo == "後で飲む"
            })
        }
    }

    @Test
    fun `getLogsForMedication maps all fields correctly`() = runTest {
        val entity = MedicationLogEntity(
            id = 5L,
            medicationId = 20L,
            status = "POSTPONED",
            scheduledAt = "2025-03-15T18:00:00",
            recordedAt = "2025-03-15T18:30:00",
            memo = "テストメモ"
        )
        every { dao.getLogsForMedication(20L) } returns flowOf(listOf(entity))

        repository.getLogsForMedication(20L).test {
            val result = awaitItem()
            assertEquals(5L, result[0].id)
            assertEquals(20L, result[0].medicationId)
            assertEquals(MedicationLogStatus.POSTPONED, result[0].status)
            assertEquals("テストメモ", result[0].memo)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
