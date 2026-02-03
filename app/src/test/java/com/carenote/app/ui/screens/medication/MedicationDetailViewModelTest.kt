package com.carenote.app.ui.screens.medication

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var medicationLogRepository: FakeMedicationLogRepository

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

    private fun createViewModel(medicationId: Long = 1L): MedicationDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("medicationId" to medicationId))
        return MedicationDetailViewModel(
            savedStateHandle = savedStateHandle,
            medicationRepository = medicationRepository,
            medicationLogRepository = medicationLogRepository
        )
    }

    private fun createMedication(
        id: Long = 1L,
        name: String = "テスト薬",
        dosage: String = "1錠",
        timings: List<MedicationTiming> = listOf(MedicationTiming.MORNING),
        times: Map<MedicationTiming, LocalTime> = mapOf(
            MedicationTiming.MORNING to LocalTime.of(8, 0)
        ),
        reminderEnabled: Boolean = true,
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ) = Medication(
        id = id,
        name = name,
        dosage = dosage,
        timings = timings,
        times = times,
        reminderEnabled = reminderEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun createMedicationLog(
        id: Long = 1L,
        medicationId: Long = 1L,
        status: MedicationLogStatus = MedicationLogStatus.TAKEN,
        scheduledAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 8, 0),
        recordedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 8, 5),
        memo: String = ""
    ) = MedicationLog(
        id = id,
        medicationId = medicationId,
        status = status,
        scheduledAt = scheduledAt,
        recordedAt = recordedAt,
        memo = memo
    )

    @Test
    fun `initial state is Loading`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.medication.value is UiState.Loading)
    }

    @Test
    fun `medication detail loaded successfully`() = runTest(testDispatcher) {
        val medication = createMedication(id = 1L, name = "アスピリン")
        medicationRepository.setMedications(listOf(medication))
        val viewModel = createViewModel(medicationId = 1L)

        viewModel.medication.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals("アスピリン", data.name)
            assertEquals(1L, data.id)
        }
    }

    @Test
    fun `medication not found returns Error`() = runTest(testDispatcher) {
        medicationRepository.setMedications(emptyList())
        val viewModel = createViewModel(medicationId = 999L)

        viewModel.medication.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Error)
            val error = (state as UiState.Error).error
            assertTrue(error is DomainError.NotFoundError)
        }
    }

    @Test
    fun `logs are loaded for medication`() = runTest(testDispatcher) {
        val logs = listOf(
            createMedicationLog(id = 1L, medicationId = 1L, status = MedicationLogStatus.TAKEN),
            createMedicationLog(id = 2L, medicationId = 1L, status = MedicationLogStatus.SKIPPED),
            createMedicationLog(id = 3L, medicationId = 2L, status = MedicationLogStatus.TAKEN)
        )
        medicationLogRepository.setLogs(logs)
        val viewModel = createViewModel(medicationId = 1L)

        viewModel.logs.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.medicationId == 1L })
        }
    }

    @Test
    fun `delete success emits deletedEvent`() = runTest(testDispatcher) {
        medicationRepository.setMedications(listOf(createMedication(id = 1L)))
        val viewModel = createViewModel(medicationId = 1L)
        advanceUntilIdle()

        viewModel.deletedEvent.test {
            viewModel.deleteMedication()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event)
        }
    }

    @Test
    fun `delete failure shows snackbar error`() = runTest(testDispatcher) {
        medicationRepository.setMedications(listOf(createMedication(id = 1L)))
        medicationRepository.shouldFail = true
        val viewModel = createViewModel(medicationId = 1L)
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteMedication()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.medication_delete_failed,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }
}
