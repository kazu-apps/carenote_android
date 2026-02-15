package com.carenote.app.ui.screens.medication

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeClock
import com.carenote.app.fakes.FakeRootDetector
import com.carenote.app.fakes.FakeMedicationLogCsvExporter
import com.carenote.app.fakes.FakeMedicationLogPdfExporter
import com.carenote.app.fakes.FakeMedicationLogRepository
import com.carenote.app.fakes.FakeMedicationReminderScheduler
import com.carenote.app.fakes.FakeMedicationRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aMedication
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.ExportState
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val fakeClock = FakeClock()
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var medicationLogRepository: FakeMedicationLogRepository
    private lateinit var reminderScheduler: FakeMedicationReminderScheduler
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var csvExporter: FakeMedicationLogCsvExporter
    private lateinit var pdfExporter: FakeMedicationLogPdfExporter
    private lateinit var rootDetector: FakeRootDetector
    private lateinit var viewModel: MedicationViewModel

    @Before
    fun setUp() {
        medicationRepository = FakeMedicationRepository()
        medicationLogRepository = FakeMedicationLogRepository()
        reminderScheduler = FakeMedicationReminderScheduler()
        analyticsRepository = FakeAnalyticsRepository()
        csvExporter = FakeMedicationLogCsvExporter()
        pdfExporter = FakeMedicationLogPdfExporter()
        rootDetector = FakeRootDetector()
    }

    private fun createViewModel(): MedicationViewModel {
        return MedicationViewModel(
            medicationRepository,
            medicationLogRepository,
            reminderScheduler,
            analyticsRepository,
            fakeClock,
            csvExporter,
            pdfExporter,
            rootDetector
        )
    }


    @Test
    fun `initial state is Loading`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        assertTrue(viewModel.uiState.value is UiState.Loading)
    }

    @Test
    fun `uiState transitions from Loading to Success`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(aMedication(id = 1L, name = "薬A"))
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(1, (success as UiState.Success).data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `medications are loaded as Success state`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(
            aMedication(id = 1L, name = "薬A"),
            aMedication(id = 2L, name = "薬B")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `empty medication list shows Success with empty list`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `todayLogs emits today logs`() = runTest(mainCoroutineRule.testDispatcher) {
        val now = LocalDateTime.of(2026, 1, 15, 10, 0, 0)
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = now,
            recordedAt = now
        )
        medicationLogRepository.setLogs(listOf(log))
        viewModel = createViewModel()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.TAKEN, logs[0].status)
        }
    }

    @Test
    fun `recordMedication with TAKEN status inserts log`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN, MedicationTiming.MORNING)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.TAKEN, logs[0].status)
            assertEquals(1L, logs[0].medicationId)
            assertEquals(MedicationTiming.MORNING, logs[0].timing)
        }
    }

    @Test
    fun `recordMedication with SKIPPED status inserts log`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.SKIPPED, MedicationTiming.NOON)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.SKIPPED, logs[0].status)
            assertEquals(MedicationTiming.NOON, logs[0].timing)
        }
    }

    @Test
    fun `recordMedication with POSTPONED status inserts log`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.POSTPONED, MedicationTiming.EVENING)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertEquals(1, logs.size)
            assertEquals(MedicationLogStatus.POSTPONED, logs[0].status)
            assertEquals(MedicationTiming.EVENING, logs[0].timing)
        }
    }

    @Test
    fun `deleteMedication removes medication from list`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(
            aMedication(id = 1L, name = "薬A"),
            aMedication(id = 2L, name = "薬B")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteMedication(1L)
        advanceUntilIdle()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("薬B", data[0].name)
        }
    }

    @Test
    fun `getLogStatusForMedication returns status from today logs`() = runTest(mainCoroutineRule.testDispatcher) {
        val now = LocalDateTime.of(2026, 1, 15, 10, 0, 0)
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = now,
            recordedAt = now
        )
        medicationLogRepository.setLogs(listOf(log))
        viewModel = createViewModel()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            val status = logs.find { it.medicationId == 1L }?.status
            assertEquals(MedicationLogStatus.TAKEN, status)
        }
    }

    @Test
    fun `getLogStatusForMedication returns null when no log exists`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            val status = logs.find { it.medicationId == 999L }?.status
            assertNull(status)
        }
    }

    @Test
    fun `medications update reactively when repository changes`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            medicationRepository.setMedications(listOf(aMedication(id = 1L)))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }

    @Test
    fun `multiple medications with different timings loaded correctly`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(
            aMedication(
                id = 1L,
                name = "朝の薬",
                timings = listOf(MedicationTiming.MORNING)
            ),
            aMedication(
                id = 2L,
                name = "昼夕の薬",
                timings = listOf(MedicationTiming.NOON, MedicationTiming.EVENING)
            ),
            aMedication(
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

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem() as UiState.Success
            assertEquals(3, state.data.size)
        }
    }

    @Test
    fun `recording log for different medications tracked independently`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(
            aMedication(id = 1L, name = "薬A"),
            aMedication(id = 2L, name = "薬B")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN)
        viewModel.recordMedication(2L, MedicationLogStatus.SKIPPED)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertEquals(2, logs.size)
            val log1 = logs.find { it.medicationId == 1L }
            val log2 = logs.find { it.medicationId == 2L }
            assertEquals(MedicationLogStatus.TAKEN, log1?.status)
            assertEquals(MedicationLogStatus.SKIPPED, log2?.status)
        }
    }

    @Test
    fun `snackbar events emitted on record success`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.recordMedication(1L, MedicationLogStatus.TAKEN)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.medication_log_recorded,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `snackbar events emitted on delete success`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteMedication(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.medication_deleted,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `medication with empty dosage is handled`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L, dosage = "")
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem() as UiState.Success
            assertEquals("", state.data[0].dosage)
        }
    }

    @Test
    fun `recordMedication failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        medicationLogRepository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.recordMedication(1L, MedicationLogStatus.TAKEN)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.medication_log_failed,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `recordMedication failure does not add log`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        medicationLogRepository.shouldFail = true

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertTrue(logs.isEmpty())
        }
    }

    @Test
    fun `deleteMedication failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        medicationRepository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.deleteMedication(1L)
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
    fun `deleteMedication failure does not remove medication`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(
            aMedication(id = 1L, name = "薬A"),
            aMedication(id = 2L, name = "薬B")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()
        advanceUntilIdle()

        medicationRepository.shouldFail = true

        viewModel.deleteMedication(1L)
        advanceUntilIdle()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(2, data.size)
        }
    }

    @Test
    fun `medication with no timings is handled`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(
            id = 1L,
            timings = emptyList(),
            times = emptyMap()
        )
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem() as UiState.Success
            assertEquals(0, state.data[0].timings.size)
        }
    }

    @Test
    fun `same medication different timings tracked independently`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(
            id = 1L,
            name = "全時間の薬",
            timings = listOf(
                MedicationTiming.MORNING,
                MedicationTiming.NOON,
                MedicationTiming.EVENING
            )
        )
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN, MedicationTiming.MORNING)
        advanceUntilIdle()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertEquals(1, logs.size)
            val morningLog = logs.find { it.timing == MedicationTiming.MORNING }
            val noonLog = logs.find { it.timing == MedicationTiming.NOON }
            assertEquals(MedicationLogStatus.TAKEN, morningLog?.status)
            assertNull(noonLog)
        }
    }

    @Test
    fun `recordMedication TAKEN cancels follow-up`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN, MedicationTiming.MORNING)
        advanceUntilIdle()

        assertEquals(1, reminderScheduler.cancelFollowUpCalls.size)
        val call = reminderScheduler.cancelFollowUpCalls[0]
        assertEquals(1L, call.medicationId)
        assertEquals(MedicationTiming.MORNING, call.timing)
    }

    @Test
    fun `recordMedication SKIPPED does not cancel follow-up`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.SKIPPED, MedicationTiming.MORNING)
        advanceUntilIdle()

        assertTrue(reminderScheduler.cancelFollowUpCalls.isEmpty())
    }

    @Test
    fun `deleteMedication success cancels reminders`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteMedication(1L)
        advanceUntilIdle()

        assertEquals(1, reminderScheduler.cancelRemindersCalls.size)
        assertEquals(1L, reminderScheduler.cancelRemindersCalls[0].medicationId)
    }

    @Test
    fun `deleteMedication failure does not cancel reminders`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        medicationRepository.shouldFail = true

        viewModel.deleteMedication(1L)
        advanceUntilIdle()

        assertTrue(reminderScheduler.cancelRemindersCalls.isEmpty())
    }

    @Test
    fun `refreshDateIfNeeded is safe when date has not changed`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Calling refreshDateIfNeeded when date hasn't changed should not throw
        viewModel.refreshDateIfNeeded()
        advanceUntilIdle()

        // todayLogs should still be accessible
        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertTrue(logs.isEmpty())
        }
    }

    @Test
    fun `todayLogs filters by current date`() = runTest(mainCoroutineRule.testDispatcher) {
        val today = LocalDate.of(2026, 1, 15)
        val yesterday = today.minusDays(1)
        val todayLog = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = today.atTime(8, 0),
            recordedAt = today.atTime(8, 0)
        )
        val yesterdayLog = MedicationLog(
            id = 2L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = yesterday.atTime(8, 0),
            recordedAt = yesterday.atTime(8, 0)
        )
        medicationLogRepository.setLogs(listOf(todayLog, yesterdayLog))
        viewModel = createViewModel()

        viewModel.todayLogs.test {
            advanceUntilIdle()
            val logs = expectMostRecentItem()
            assertEquals(1, logs.size)
            assertEquals(today, logs[0].scheduledAt.toLocalDate())
        }
    }

    @Test
    fun `refresh triggers data reload`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(aMedication(id = 1L, name = "薬A"))
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(1, (initial as UiState.Success).data.size)

            medicationRepository.setMedications(
                listOf(aMedication(id = 1L, name = "薬A"), aMedication(id = 2L, name = "薬B"))
            )
            viewModel.refresh()
            advanceUntilIdle()

            val refreshed = expectMostRecentItem()
            assertTrue(refreshed is UiState.Success)
            assertEquals(2, (refreshed as UiState.Success).data.size)
        }
    }

    @Test
    fun `searchQuery is empty initially`() {
        viewModel = createViewModel()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `updateSearchQuery updates searchQuery`() {
        viewModel = createViewModel()

        viewModel.updateSearchQuery("ロキソニン")

        assertEquals("ロキソニン", viewModel.searchQuery.value)
    }

    @Test
    fun `search filters medications by name`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(
            aMedication(id = 1L, name = "ロキソニン", dosage = "1錠"),
            aMedication(id = 2L, name = "アスピリン", dosage = "2錠"),
            aMedication(id = 3L, name = "ロキソプロフェン", dosage = "1錠")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(3, (initial as UiState.Success).data.size)

            viewModel.updateSearchQuery("ロキソ")
            advanceUntilIdle()

            val filtered = expectMostRecentItem()
            assertTrue(filtered is UiState.Success)
            val data = (filtered as UiState.Success).data
            assertEquals(2, data.size)
            assertTrue(data.all { it.name.contains("ロキソ") })
        }
    }

    @Test
    fun `search filters medications by dosage`() = runTest(mainCoroutineRule.testDispatcher) {
        val medications = listOf(
            aMedication(id = 1L, name = "薬A", dosage = "1錠"),
            aMedication(id = 2L, name = "薬B", dosage = "2カプセル")
        )
        medicationRepository.setMedications(medications)
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateSearchQuery("カプセル")
            advanceUntilIdle()

            val filtered = expectMostRecentItem()
            assertTrue(filtered is UiState.Success)
            val data = (filtered as UiState.Success).data
            assertEquals(1, data.size)
            assertEquals("薬B", data[0].name)
        }
    }

    @Test
    fun `recordMedication TAKEN decrements stock when stock is tracked`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L).copy(currentStock = 10, lowStockThreshold = 5)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN, MedicationTiming.MORNING)
        advanceUntilIdle()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem() as UiState.Success
            assertEquals(9, state.data[0].currentStock)
        }
    }

    @Test
    fun `recordMedication TAKEN shows low stock warning when threshold reached`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L).copy(currentStock = 5, lowStockThreshold = 5)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.recordMedication(1L, MedicationLogStatus.TAKEN, MedicationTiming.MORNING)
            advanceUntilIdle()
            // First event is "log recorded"
            val event1 = awaitItem()
            assertTrue(event1 is SnackbarEvent.WithResId)
            assertEquals(R.string.medication_log_recorded, (event1 as SnackbarEvent.WithResId).messageResId)
            // Second event is "low stock warning"
            val event2 = awaitItem()
            assertTrue(event2 is SnackbarEvent.WithResId)
            assertEquals(R.string.medication_low_stock_warning, (event2 as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `recordMedication TAKEN with null stock does not decrement`() = runTest(mainCoroutineRule.testDispatcher) {
        val medication = aMedication(id = 1L)
        medicationRepository.setMedications(listOf(medication))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordMedication(1L, MedicationLogStatus.TAKEN, MedicationTiming.MORNING)
        advanceUntilIdle()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem() as UiState.Success
            assertNull(state.data[0].currentStock)
        }
    }

    @Test
    fun `isRefreshing becomes false after data loads`() = runTest(mainCoroutineRule.testDispatcher) {
        medicationRepository.setMedications(listOf(aMedication(id = 1L)))
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isRefreshing.value)

        viewModel.refresh()
        // refresh() synchronously sets isRefreshing = true
        assertTrue(viewModel.isRefreshing.value)

        // After flow re-collection completes, onEach resets isRefreshing
        viewModel.uiState.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.isRefreshing.value)
    }

    // === Export Tests ===

    @Test
    fun `exportState is initially Idle`() {
        viewModel = createViewModel()
        assertTrue(viewModel.exportState.value is ExportState.Idle)
    }

    @Test
    fun `exportCsv with empty logs shows empty snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.exportCsv()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.medication_log_export_empty,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `exportCsv with empty logs resets export state to Idle`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportCsv()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Idle)
    }

    @Test
    fun `exportCsv with logs sets Success state`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        medicationRepository.setMedications(listOf(aMedication(id = 1L, name = "テスト薬")))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportCsv()
        advanceUntilIdle()

        val state = viewModel.exportState.value
        assertTrue(state is ExportState.Success)
        assertEquals("text/csv", (state as ExportState.Success).mimeType)
        assertEquals(1, csvExporter.exportCallCount)
    }

    @Test
    fun `exportPdf with logs sets Success state`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        medicationRepository.setMedications(listOf(aMedication(id = 1L, name = "テスト薬")))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportPdf()
        advanceUntilIdle()

        val state = viewModel.exportState.value
        assertTrue(state is ExportState.Success)
        assertEquals("application/pdf", (state as ExportState.Success).mimeType)
        assertEquals(1, pdfExporter.exportCallCount)
    }

    @Test
    fun `exportCsv failure sets Error state`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        csvExporter.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportCsv()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Error)
    }

    @Test
    fun `exportPdf failure sets Error state`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        pdfExporter.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportPdf()
        advanceUntilIdle()

        assertTrue(viewModel.exportState.value is ExportState.Error)
    }

    @Test
    fun `exportCsv failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        csvExporter.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.exportCsv()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.medication_log_export_failed,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `resetExportState resets to Idle`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportCsv()
        advanceUntilIdle()
        assertTrue(viewModel.exportState.value is ExportState.Success)

        viewModel.resetExportState()
        assertTrue(viewModel.exportState.value is ExportState.Idle)
    }

    @Test
    fun `exportCsv passes correct medication names to exporter`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        medicationRepository.setMedications(listOf(aMedication(id = 1L, name = "ロキソニン")))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportCsv()
        advanceUntilIdle()

        val names = csvExporter.lastMedicationNames
        assertEquals(1, names?.size)
        assertEquals("ロキソニン", names?.get(1L))
    }

    @Test
    fun `exportCsv logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportCsv()
        advanceUntilIdle()

        assertTrue(analyticsRepository.loggedEvents.any { it.first == "medication_log_export_csv" })
    }

    @Test
    fun `exportPdf logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        val log = MedicationLog(
            id = 1L,
            medicationId = 1L,
            status = MedicationLogStatus.TAKEN,
            scheduledAt = LocalDateTime.of(2026, 1, 15, 8, 0),
            recordedAt = LocalDateTime.of(2026, 1, 15, 8, 5)
        )
        medicationLogRepository.setLogs(listOf(log))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportPdf()
        advanceUntilIdle()

        assertTrue(analyticsRepository.loggedEvents.any { it.first == "medication_log_export_pdf" })
    }

    // --- Root Detection Export Block Tests ---

    @Test
    fun `exportCsv on rooted device shows blocked snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        rootDetector.isRooted = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportCsv()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.security_root_export_blocked, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `exportPdf on rooted device shows blocked snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        rootDetector.isRooted = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportPdf()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            advanceUntilIdle()
            val event = expectMostRecentItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.security_root_export_blocked, (event as SnackbarEvent.WithResId).messageResId)
        }
    }
}
