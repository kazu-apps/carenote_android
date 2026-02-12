package com.carenote.app.ui.screens.healthrecords

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.repository.HealthRecordCsvExporterInterface
import com.carenote.app.domain.repository.HealthRecordPdfExporterInterface
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeHealthRecordRepository
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.ExportState
import com.carenote.app.ui.viewmodel.UiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HealthRecordsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeHealthRecordRepository
    private lateinit var csvExporter: HealthRecordCsvExporterInterface
    private lateinit var pdfExporter: HealthRecordPdfExporterInterface
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: HealthRecordsViewModel
    private val fakeUri = mockk<android.net.Uri>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHealthRecordRepository()
        csvExporter = mockk()
        pdfExporter = mockk()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HealthRecordsViewModel {
        return HealthRecordsViewModel(repository, csvExporter, pdfExporter, analyticsRepository)
    }

    private fun createRecord(
        id: Long = 1L,
        temperature: Double? = 36.5,
        bloodPressureHigh: Int? = 120,
        bloodPressureLow: Int? = 80,
        pulse: Int? = 72,
        weight: Double? = 60.0,
        meal: MealAmount? = MealAmount.FULL,
        conditionNote: String = "",
        recordedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        createdAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0),
        updatedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ) = HealthRecord(
        id = id,
        temperature = temperature,
        bloodPressureHigh = bloodPressureHigh,
        bloodPressureLow = bloodPressureLow,
        pulse = pulse,
        weight = weight,
        meal = meal,
        conditionNote = conditionNote,
        recordedAt = recordedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        assertTrue(viewModel.records.value is UiState.Loading)
    }

    @Test
    fun `records transitions from Loading to Success`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L, temperature = 36.5))
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
    fun `records are loaded as Success state`() = runTest(testDispatcher) {
        val records = listOf(
            createRecord(id = 1L, temperature = 36.5),
            createRecord(id = 2L, temperature = 37.0)
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
    fun `empty records show Success with empty list`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(0, (state as UiState.Success).data.size)
        }
    }

    @Test
    fun `records update reactively when repository changes`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(0, (initial as UiState.Success).data.size)

            repository.setRecords(listOf(createRecord(id = 1L)))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            assertTrue(updated is UiState.Success)
            assertEquals(1, (updated as UiState.Success).data.size)
        }
    }

    @Test
    fun `deleteRecord removes record and shows snackbar`() = runTest(testDispatcher) {
        val records = listOf(
            createRecord(id = 1L),
            createRecord(id = 2L)
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
    fun `deleteRecord failure shows error snackbar`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
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
    fun `records sorted by recordedAt descending`() = runTest(testDispatcher) {
        val records = listOf(
            createRecord(
                id = 1L,
                recordedAt = LocalDateTime.of(2025, 3, 15, 8, 0)
            ),
            createRecord(
                id = 2L,
                recordedAt = LocalDateTime.of(2025, 3, 15, 12, 0)
            ),
            createRecord(
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
    fun `refresh triggers data reload`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.records.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(1, (initial as UiState.Success).data.size)

            repository.setRecords(
                listOf(createRecord(id = 1L), createRecord(id = 2L))
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
    fun `search filters records by conditionNote`() = runTest(testDispatcher) {
        val records = listOf(
            createRecord(id = 1L, conditionNote = "頭痛あり"),
            createRecord(id = 2L, conditionNote = "食欲良好"),
            createRecord(id = 3L, conditionNote = "頭痛と吐き気")
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
    fun `isRefreshing becomes false after data loads`() = runTest(testDispatcher) {
        repository.setRecords(listOf(createRecord(id = 1L)))
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
    fun `exportCsv calls csvExporter and sets Success`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
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
    fun `exportPdf calls pdfExporter and sets Success`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
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
    fun `exportCsv with empty records shows snackbar`() = runTest(testDispatcher) {
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
    fun `exportPdf with empty records shows snackbar`() = runTest(testDispatcher) {
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
    fun `exportCsv failure sets Error state`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
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
    fun `exportPdf failure sets Error state`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
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
    fun `exportState resets to Idle after Error`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
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
    fun `resetExportState sets Idle`() = runTest(testDispatcher) {
        val records = listOf(createRecord(id = 1L))
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
}
