package com.carenote.app.ui.screens.medication

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.fakes.FakeMedicationLogRepository
import com.carenote.app.fakes.FakeMedicationRepository
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var medicationLogRepository: FakeMedicationLogRepository
    private lateinit var viewModel: MedicationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicationRepository = FakeMedicationRepository()
        medicationLogRepository = FakeMedicationLogRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MedicationViewModel {
        return MedicationViewModel(medicationRepository, medicationLogRepository)
    }

    private fun createMedication(
        id: Long = 1L,
        name: String = "テスト薬",
        dosage: String = "1錠",
        timings: List<MedicationTiming> = listOf(MedicationTiming.MORNING),
        times: Map<MedicationTiming, LocalTime> = mapOf(
            MedicationTiming.MORNING to LocalTime.of(8, 0)
        )
    ) = Medication(
        id = id,
        name = name,
        dosage = dosage,
        timings = timings,
        times = times
    )

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel = createViewModel()

        assertTrue(viewModel.uiState.value is UiState.Loading)
    }

    @Test
    fun `medications are loaded as Success state`() = runTest {
        val medications = listOf(
            createMedication(id = 1L, name = "薬A"),
            createMedication(id = 2L, name = "薬B")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `empty medication list shows Success with empty list`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `todayLogs emits today logs`() = runTest {
        val now = LocalDateTime.now()
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = now,
            recordedAt = now
        )
        medicationLogRepository.setLogs(listOf(log))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.todayLogs.test {
            val logs = awaitItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.TAKEN, logs[0].status)
        }
    }

    @Test
    fun `recordMedication with TAKEN status inserts log`() = runTest {
        val medication = createMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            val logs = awaitItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.TAKEN, logs[0].status)
            assertEquals(1L, logs[0].medicationId)
        }
    }

    @Test
    fun `recordMedication with SKIPPED status inserts log`() = runTest {
        val medication = createMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.SKIPPED)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            val logs = awaitItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.SKIPPED, logs[0].status)
        }
    }

    @Test
    fun `recordMedication with POSTPONED status inserts log`() = runTest {
        val medication = createMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.POSTPONED)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            val logs = awaitItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.POSTPONED, logs[0].status)
        }
    }

    @Test
    fun `deleteMedication removes medication from list`() = runTest {
        val medications = listOf(
            createMedication(id = 1L, name = "薬A"),
            createMedication(id = 2L, name = "薬B")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteMedication(1L)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("薬B", data[0].name)
        }
    }

    @Test
    fun `getLogStatusForMedication returns status from today logs`() = runTest {
        val now = LocalDateTime.now()
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = now,
            recordedAt = now
        )
        medicationLogRepository.setLogs(listOf(log))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.todayLogs.test {
            val logs = awaitItem()
            val status = logs.find { it.medicationId == 1L }?.status
            assertEquals(MedicationLogStatus.TAKEN, status)
        }
    }

    @Test
    fun `getLogStatusForMedication returns null when no log exists`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.todayLogs.test {
            val logs = awaitItem()
            val status = logs.find { it.medicationId == 999L }?.status
            assertNull(status)
        }
    }

    @Test
    fun `medications update reactively when repository changes`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            medicationRepository.setMedications(listOf(createMedication(id = 1L)))
            val updated = awaitItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }

    @Test
    fun `multiple medications with different timings loaded correctly`() = runTest {
        val medications = listOf(
            createMedication(
                id = 1L,
                name = "朝の薬",
                timings = listOf(MedicationTiming.MORNING)
            ),
            createMedication(
                id = 2L,
                name = "昼夕の薬",
                timings = listOf(MedicationTiming.NOON, MedicationTiming.EVENING)
            ),
            createMedication(
                id = 3L,
                name = "全時間の薬",
                timings = listOf(
                    MedicationTiming.MORNING,
                    MedicationTiming.NOON,
                    MedicationTiming.EVENING
                )
            )
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as UiState.Success
            assertEquals(3, state.data.size)
        }
    }

    @Test
    fun `recording log for different medications tracked independently`() = runTest {
        val medications = listOf(
            createMedication(id = 1L, name = "薬A"),
            createMedication(id = 2L, name = "薬B")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN)
        viewModel.recordMedication(2L, MedicationLogStatus.SKIPPED)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            val logs = awaitItem()
            assertEquals(2, logs.size)
            val log1 = logs.find { it.medicationId == 1L }
            val log2 = logs.find { it.medicationId == 2L }
            assertEquals(MedicationLogStatus.TAKEN, log1?.status)
            assertEquals(MedicationLogStatus.SKIPPED, log2?.status)
        }
    }

    @Test
    fun `snackbar events emitted on record success`() = runTest {
        val medication = createMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.recordMedication(1L, MedicationLogStatus.TAKEN)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.medication_log_recorded, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `snackbar events emitted on delete success`() = runTest {
        val medication = createMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteMedication(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.medication_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `medication with empty dosage is handled`() = runTest {
        val medication = createMedication(id = 1L, dosage = "")
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as UiState.Success
            assertEquals("", state.data[0].dosage)
        }
    }

    @Test
    fun `medication with no timings is handled`() = runTest {
        val medication = createMedication(
            id = 1L,
            timings = emptyList(),
            times = emptyMap()
        )
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as UiState.Success
            assertEquals(0, state.data[0].timings.size)
        }
    }
}
