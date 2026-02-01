package com.carenote.app.ui.screens.healthrecords

import app.cash.turbine.test
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.fakes.FakeHealthRecordRepository
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
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HealthRecordGraphViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeHealthRecordRepository
    private lateinit var viewModel: HealthRecordGraphViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHealthRecordRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HealthRecordGraphViewModel {
        return HealthRecordGraphViewModel(repository)
    }

    private fun createRecord(
        id: Long = 1L,
        temperature: Double? = 36.5,
        bloodPressureHigh: Int? = 120,
        bloodPressureLow: Int? = 80,
        recordedAt: LocalDateTime = LocalDateTime.of(2025, 3, 15, 10, 0)
    ) = HealthRecord(
        id = id,
        temperature = temperature,
        bloodPressureHigh = bloodPressureHigh,
        bloodPressureLow = bloodPressureLow,
        pulse = 72,
        recordedAt = recordedAt,
        createdAt = recordedAt,
        updatedAt = recordedAt
    )

    @Test
    fun `initial state is loading with 7 day range`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        val state = viewModel.graphState.value
        assertTrue(state.isLoading)
        assertEquals(GraphDateRange.SEVEN_DAYS, state.dateRange)
    }

    @Test
    fun `temperature data points extracted from records`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        val records = listOf(
            createRecord(id = 1L, temperature = 36.5, recordedAt = now.minusDays(1)),
            createRecord(id = 2L, temperature = 37.2, recordedAt = now)
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.temperaturePoints.size)
            assertEquals(36.5, state.temperaturePoints[0].value, 0.01)
            assertEquals(37.2, state.temperaturePoints[1].value, 0.01)
        }
    }

    @Test
    fun `blood pressure data points extracted from records`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        val records = listOf(
            createRecord(
                id = 1L,
                bloodPressureHigh = 130,
                bloodPressureLow = 85,
                recordedAt = now.minusDays(1)
            ),
            createRecord(
                id = 2L,
                bloodPressureHigh = 120,
                bloodPressureLow = 80,
                recordedAt = now
            )
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(2, state.bpHighPoints.size)
            assertEquals(130.0, state.bpHighPoints[0].value, 0.01)
            assertEquals(120.0, state.bpHighPoints[1].value, 0.01)
            assertEquals(2, state.bpLowPoints.size)
            assertEquals(85.0, state.bpLowPoints[0].value, 0.01)
            assertEquals(80.0, state.bpLowPoints[1].value, 0.01)
        }
    }

    @Test
    fun `null temperature values are filtered out`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        val records = listOf(
            createRecord(id = 1L, temperature = 36.5, recordedAt = now.minusDays(2)),
            createRecord(id = 2L, temperature = null, recordedAt = now.minusDays(1)),
            createRecord(id = 3L, temperature = 37.0, recordedAt = now)
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(2, state.temperaturePoints.size)
        }
    }

    @Test
    fun `null blood pressure values are filtered out`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        val records = listOf(
            createRecord(
                id = 1L,
                bloodPressureHigh = 120,
                bloodPressureLow = 80,
                recordedAt = now.minusDays(1)
            ),
            createRecord(
                id = 2L,
                bloodPressureHigh = null,
                bloodPressureLow = null,
                recordedAt = now
            )
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(1, state.bpHighPoints.size)
            assertEquals(1, state.bpLowPoints.size)
        }
    }

    @Test
    fun `date range switch triggers data reload`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        val records = listOf(
            createRecord(id = 1L, recordedAt = now.minusDays(20)),
            createRecord(id = 2L, recordedAt = now.minusDays(3)),
            createRecord(id = 3L, recordedAt = now)
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val sevenDayState = expectMostRecentItem()
            assertEquals(GraphDateRange.SEVEN_DAYS, sevenDayState.dateRange)
            assertEquals(2, sevenDayState.temperaturePoints.size)

            viewModel.setDateRange(GraphDateRange.THIRTY_DAYS)
            advanceUntilIdle()
            val thirtyDayState = expectMostRecentItem()
            assertEquals(GraphDateRange.THIRTY_DAYS, thirtyDayState.dateRange)
            assertEquals(3, thirtyDayState.temperaturePoints.size)
        }
    }

    @Test
    fun `hasTemperatureData is true when temperature points exist`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        repository.setRecords(
            listOf(createRecord(id = 1L, temperature = 36.5, recordedAt = now))
        )
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.hasTemperatureData)
        }
    }

    @Test
    fun `hasTemperatureData is false when no temperature points`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        repository.setRecords(
            listOf(
                createRecord(
                    id = 1L,
                    temperature = null,
                    bloodPressureHigh = 120,
                    bloodPressureLow = 80,
                    recordedAt = now
                )
            )
        )
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.hasTemperatureData)
        }
    }

    @Test
    fun `hasBloodPressureData is true when bp points exist`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        repository.setRecords(
            listOf(
                createRecord(
                    id = 1L,
                    bloodPressureHigh = 120,
                    bloodPressureLow = 80,
                    recordedAt = now
                )
            )
        )
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.hasBloodPressureData)
        }
    }

    @Test
    fun `hasBloodPressureData is false when no bp points`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        repository.setRecords(
            listOf(
                createRecord(
                    id = 1L,
                    temperature = 36.5,
                    bloodPressureHigh = null,
                    bloodPressureLow = null,
                    recordedAt = now
                )
            )
        )
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.hasBloodPressureData)
        }
    }

    @Test
    fun `data points are sorted by date ascending`() = runTest(testDispatcher) {
        val now = LocalDateTime.now()
        val records = listOf(
            createRecord(id = 1L, temperature = 36.0, recordedAt = now),
            createRecord(id = 2L, temperature = 37.0, recordedAt = now.minusDays(2)),
            createRecord(id = 3L, temperature = 36.5, recordedAt = now.minusDays(1))
        )
        repository.setRecords(records)
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(3, state.temperaturePoints.size)
            assertEquals(37.0, state.temperaturePoints[0].value, 0.01)
            assertEquals(36.5, state.temperaturePoints[1].value, 0.01)
            assertEquals(36.0, state.temperaturePoints[2].value, 0.01)
        }
    }

    @Test
    fun `empty repository returns empty state`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.graphState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertTrue(state.temperaturePoints.isEmpty())
            assertTrue(state.bpHighPoints.isEmpty())
            assertTrue(state.bpLowPoints.isEmpty())
            assertFalse(state.hasTemperatureData)
            assertFalse(state.hasBloodPressureData)
        }
    }

    @Test
    fun `mapToGraphState correctly maps records to state`() {
        val today = LocalDate.of(2025, 3, 15)
        val records = listOf(
            createRecord(
                id = 1L,
                temperature = 36.5,
                bloodPressureHigh = 120,
                bloodPressureLow = 80,
                recordedAt = LocalDateTime.of(2025, 3, 14, 10, 0)
            ),
            createRecord(
                id = 2L,
                temperature = 37.8,
                bloodPressureHigh = 145,
                bloodPressureLow = 95,
                recordedAt = LocalDateTime.of(2025, 3, 15, 8, 0)
            )
        )

        val state = HealthRecordGraphViewModel.mapToGraphState(
            records = records,
            dateRange = GraphDateRange.SEVEN_DAYS
        )

        assertEquals(2, state.temperaturePoints.size)
        assertEquals(2, state.bpHighPoints.size)
        assertEquals(2, state.bpLowPoints.size)
        assertTrue(state.hasTemperatureData)
        assertTrue(state.hasBloodPressureData)
        assertFalse(state.isLoading)
        assertEquals(GraphDateRange.SEVEN_DAYS, state.dateRange)

        assertEquals(today.minusDays(1), state.temperaturePoints[0].date)
        assertEquals(today, state.temperaturePoints[1].date)
    }
}
