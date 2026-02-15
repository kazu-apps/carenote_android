package com.carenote.app.ui.screens.medication

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeMedicationLogRepository
import com.carenote.app.fakes.FakeMedicationReminderScheduler
import com.carenote.app.fakes.FakeMedicationRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aMedication
import com.carenote.app.testing.aMedicationLog
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var medicationLogRepository: FakeMedicationLogRepository
    private lateinit var reminderScheduler: FakeMedicationReminderScheduler
    private lateinit var analyticsRepository: FakeAnalyticsRepository

    @Before
    fun setUp() {
        medicationRepository = FakeMedicationRepository()
        medicationLogRepository = FakeMedicationLogRepository()
        reminderScheduler = FakeMedicationReminderScheduler()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(medicationId: Long = 1L): MedicationDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("medicationId" to medicationId))
        return MedicationDetailViewModel(
            savedStateHandle = savedStateHandle,
            medicationRepository = medicationRepository,
            medicationLogRepository = medicationLogRepository,
            reminderScheduler = reminderScheduler,
            analyticsRepository = analyticsRepository
        )
    }


    @Test
    fun `initial state is Loading`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.medication.value is UiState.Loading)
    }

    @Test
    fun `medication transitions from Loading to Success`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L, name = "アスピリン")
        medicationRepository.setMedications(listOf(medication))
        val viewModel = createViewModel(medicationId = 1L)

        viewModel.medication.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals("アスピリン", (success as UiState.Success).data.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `medication detail loaded successfully`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L, name = "アスピリン")
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
    fun `medication not found returns Error`() = runTest(mainCoroutineRule.testDispatcher) {
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
    fun `logs are loaded for medication`() = runTest(mainCoroutineRule.testDispatcher) {
        val logs = listOf(
            aMedicationLog(id = 1L, medicationId = 1L, status = MedicationLogStatus.TAKEN),
            aMedicationLog(id = 2L, medicationId = 1L, status = MedicationLogStatus.SKIPPED),
            aMedicationLog(id = 3L, medicationId = 2L, status = MedicationLogStatus.TAKEN)
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
    fun `delete success emits deletedEvent`() = runTest(mainCoroutineRule.testDispatcher) {
        medicationRepository.setMedications(listOf(aMedication(id = 1L)))
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
    fun `delete failure shows snackbar error`() = runTest(mainCoroutineRule.testDispatcher) {
        medicationRepository.setMedications(listOf(aMedication(id = 1L)))
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

    @Test
    fun `delete success cancels reminders`() = runTest(mainCoroutineRule.testDispatcher) {
        medicationRepository.setMedications(listOf(aMedication(id = 1L)))
        val viewModel = createViewModel(medicationId = 1L)
        advanceUntilIdle()

        viewModel.deleteMedication()
        advanceUntilIdle()

        assertEquals(1, reminderScheduler.cancelRemindersCalls.size)
        assertEquals(1L, reminderScheduler.cancelRemindersCalls[0].medicationId)
    }
}
