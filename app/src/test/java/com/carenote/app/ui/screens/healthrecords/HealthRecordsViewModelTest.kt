package com.carenote.app.ui.screens.healthrecords

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.fakes.FakeHealthRecordRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class HealthRecordsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeHealthRecordRepository
    private lateinit var viewModel: HealthRecordsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHealthRecordRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HealthRecordsViewModel {
        return HealthRecordsViewModel(repository)
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
}
