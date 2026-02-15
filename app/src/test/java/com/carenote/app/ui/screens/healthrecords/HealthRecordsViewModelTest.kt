package com.carenote.app.ui.screens.healthrecords

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.repository.HealthRecordCsvExporterInterface
import com.carenote.app.domain.repository.HealthRecordPdfExporterInterface
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeHealthRecordRepository
import com.carenote.app.fakes.FakeRootDetector
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aHealthRecord
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.ExportState
import com.carenote.app.ui.viewmodel.UiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HealthRecordsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: FakeHealthRecordRepository
    private lateinit var csvExporter: HealthRecordCsvExporterInterface
    private lateinit var pdfExporter: HealthRecordPdfExporterInterface
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var rootDetector: FakeRootDetector
    private lateinit var viewModel: HealthRecordsViewModel
    private val fakeUri = mockk<android.net.Uri>()

    @Before
    fun setUp() {
        repository = FakeHealthRecordRepository()
        csvExporter = mockk()
        pdfExporter = mockk()
        analyticsRepository = FakeAnalyticsRepository()
        rootDetector = FakeRootDetector()
    }

    private fun createViewModel(): HealthRecordsViewModel {
        return HealthRecordsViewModel(repository, csvExporter, pdfExporter, analyticsRepository, rootDetector)
    }


    @Test
    fun `initial state is Loading`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        assertTrue(viewModel.records.value is UiState.Loading)
    }

    @Test
    fun `records transitions from Loading to Success`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L, temperature = 36.5))
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.records.test {
            assertEquals(UiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(1, (success as UiState.Success).data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `records are loaded as Success state`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(
            aHealthRecord(id = 1L, temperature = 36.5),
            aHealthRecord(id = 2L, temperature = 37.0)
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(2, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `empty records show Success with empty list`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `records update reactively when repository changes`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            repository.setRecords(listOf(aHealthRecord(id = 1L)))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }

    @Test
    fun `deleteRecord removes record and shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(
            aHealthRecord(id = 1L),
            aHealthRecord(id = 2L)
        )
        repository.setRecords(records)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteRecord(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.health_records_deleted, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `deleteRecord failure shows error snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        repository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.snackbarController.events.test {
            viewModel.deleteRecord(1L)
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.health_records_delete_failed, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `records sorted by recordedAt descending`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(
            HealthRecord(
                id = 1L,
                recordedAt = LocalDateTime.of(2025, 3, 15, 8, 0)
            ),
            HealthRecord(
                id = 2L,
                recordedAt = LocalDateTime.of(2025, 3, 15, 12, 0)
            ),
            HealthRecord(
                id = 3L,
                recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
            )
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(3, data.size)
            assertEquals(2L, data[0].id)
            assertEquals(3L, data[1].id)
            assertEquals(1L, data[2].id)
        }
    }

    @Test
    fun `refresh triggers data reload`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(1, (initial as UiState.Success).data.size)

            repository.setRecords(
                listOf(aHealthRecord(id = 1L), aHealthRecord(id = 2L))
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

        viewModel.updateSearchQuery("頭痛")

        assertEquals("頭痛", viewModel.searchQuery.value)
    }

    @Test
    fun `search filters records by conditionNote`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(
            aHealthRecord(id = 1L, conditionNote = "頭痛あり"),
            aHealthRecord(id = 2L, conditionNote = "食欲良好"),
            aHealthRecord(id = 3L, conditionNote = "頭痛と吐き気")
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(3, (initial as UiState.Success).data.size)

            viewModel.updateSearchQuery("頭痛")
            advanceUntilIdle()

            val filtered = expectMostRecentItem()
            assertTrue(filtered is UiState.Success)
            val data = (filtered as UiState.Success).data
            assertEquals(2, data.size)
            assertTrue(data.all { it.conditionNote.contains("頭痛") })
        }
    }

    @Test
    fun `isRefreshing becomes false after data loads`() = runTest(mainCoroutineRule.testDispatcher) {
        repository.setRecords(listOf(aHealthRecord(id = 1L)))
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isRefreshing.value)

        viewModel.refresh()
        assertTrue(viewModel.isRefreshing.value)

        viewModel.records.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `exportState is Idle initially`() {
        viewModel = createViewModel()

        assertTrue(viewModel.exportState.value is ExportState.Idle)
    }

    @Test
    fun `exportCsv calls csvExporter and sets Success`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        coEvery { csvExporter.export(any()) } returns fakeUri
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.exportCsv()
        advanceUntilIdle()

        val state = viewModel.exportState.value
        assertTrue(state is ExportState.Success)
        assertEquals("text/csv", (state as ExportState.Success).mimeType)
    }

    @Test
    fun `exportPdf calls pdfExporter and sets Success`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        coEvery { pdfExporter.export(any()) } returns fakeUri
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.exportPdf()
        advanceUntilIdle()

        val state = viewModel.exportState.value
        assertTrue(state is ExportState.Success)
        assertEquals("application/pdf", (state as ExportState.Success).mimeType)
    }

    @Test
    fun `exportCsv with empty records shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.snackbarController.events.test {
            viewModel.exportCsv()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.health_records_export_empty,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `exportPdf with empty records shows snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.snackbarController.events.test {
            viewModel.exportPdf()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.health_records_export_empty,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `exportCsv failure sets Error state`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        coEvery { csvExporter.export(any()) } throws RuntimeException("Export failed")
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.exportCsv()
        advanceUntilIdle()

        val state = viewModel.exportState.value
        assertTrue(state is ExportState.Error)
    }

    @Test
    fun `exportPdf failure sets Error state`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        coEvery { pdfExporter.export(any()) } throws RuntimeException("Export failed")
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.exportPdf()
        advanceUntilIdle()

        val state = viewModel.exportState.value
        assertTrue(state is ExportState.Error)
    }

    @Test
    fun `exportState resets to Idle after Error`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        coEvery { csvExporter.export(any()) } throws RuntimeException("Export failed")
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.exportCsv()
        advanceUntilIdle()
        assertTrue(viewModel.exportState.value is ExportState.Error)

        viewModel.resetExportState()
        assertTrue(viewModel.exportState.value is ExportState.Idle)
    }

    @Test
    fun `resetExportState sets Idle`() = runTest(mainCoroutineRule.testDispatcher) {
        val records = listOf(aHealthRecord(id = 1L))
        repository.setRecords(records)
        coEvery { csvExporter.export(any()) } returns fakeUri
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            expectMostRecentItem()
        }

        viewModel.exportCsv()
        advanceUntilIdle()
        assertTrue(viewModel.exportState.value is ExportState.Success)

        viewModel.resetExportState()
        assertTrue(viewModel.exportState.value is ExportState.Idle)
    }

    // --- Root Detection Export Block Tests ---

    @Test
    fun `exportCsv on rooted device shows blocked snackbar`() = runTest(mainCoroutineRule.testDispatcher) {
        repository.insertRecord(aHealthRecord())
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
        repository.insertRecord(aHealthRecord())
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
