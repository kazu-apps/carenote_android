package com.carenote.app.ui.screens.healthrecords

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.fakes.FakeClock
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeHealthRecordRepository
import com.carenote.app.fakes.FakePhotoRepository
import io.mockk.mockk
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.SnackbarEvent
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditHealthRecordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeHealthRecordRepository
    private lateinit var photoRepository: FakePhotoRepository
    private val imageCompressor: ImageCompressorInterface = mockk(relaxed = true)
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private val fakeClock = FakeClock()
    private lateinit var viewModel: AddEditHealthRecordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHealthRecordRepository()
        photoRepository = FakePhotoRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAddViewModel(): AddEditHealthRecordViewModel {
        return AddEditHealthRecordViewModel(SavedStateHandle(), repository, photoRepository, imageCompressor, analyticsRepository, clock = fakeClock)
    }

    private fun createEditViewModel(recordId: Long): AddEditHealthRecordViewModel {
        return AddEditHealthRecordViewModel(
            SavedStateHandle(mapOf("recordId" to recordId)),
            repository,
            photoRepository,
            imageCompressor,
            analyticsRepository,
            clock = fakeClock
        )
    }

    // --- Add Mode Tests ---

    @Test
    fun `initial form state has empty fields for add mode`() {
        viewModel = createAddViewModel()

        val state = viewModel.formState.value

        assertEquals("", state.temperature)
        assertEquals("", state.bloodPressureHigh)
        assertEquals("", state.bloodPressureLow)
        assertEquals("", state.pulse)
        assertEquals("", state.weight)
        assertNull(state.meal)
        assertNull(state.excretion)
        assertEquals("", state.conditionNote)
        assertNull(state.temperatureError)
        assertNull(state.bloodPressureError)
        assertNull(state.pulseError)
        assertNull(state.weightError)
        assertNull(state.generalError)
        assertFalse(state.isSaving)
        assertFalse(state.isEditMode)
    }

    @Test
    fun `updateTemperature updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateTemperature("36.5")

        assertEquals("36.5", viewModel.formState.value.temperature)
    }

    @Test
    fun `updateBloodPressure updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateBloodPressureHigh("120")
        viewModel.updateBloodPressureLow("80")

        assertEquals("120", viewModel.formState.value.bloodPressureHigh)
        assertEquals("80", viewModel.formState.value.bloodPressureLow)
    }

    @Test
    fun `updatePulse updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updatePulse("72")

        assertEquals("72", viewModel.formState.value.pulse)
    }

    @Test
    fun `updateWeight updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateWeight("60.5")

        assertEquals("60.5", viewModel.formState.value.weight)
    }

    @Test
    fun `updateMeal updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateMeal(MealAmount.HALF)

        assertEquals(MealAmount.HALF, viewModel.formState.value.meal)
    }

    @Test
    fun `updateExcretion updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateExcretion(ExcretionType.NORMAL)

        assertEquals(ExcretionType.NORMAL, viewModel.formState.value.excretion)
    }

    @Test
    fun `updateConditionNote updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateConditionNote("体調良好")

        assertEquals("体調良好", viewModel.formState.value.conditionNote)
    }

    @Test
    fun `updateRecordedAt updates form state`() {
        viewModel = createAddViewModel()
        val newDateTime = LocalDateTime.of(2025, 6, 1, 14, 30)

        viewModel.updateRecordedAt(newDateTime)

        assertEquals(newDateTime, viewModel.formState.value.recordedAt)
    }

    @Test
    fun `saveRecord with at least one field succeeds`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateTemperature("36.5")

        viewModel.saveRecord()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            val saved = awaitItem()
            assertTrue(saved)
        }
    }

    @Test
    fun `saveRecord with all empty fields shows validation error`() {
        viewModel = createAddViewModel()

        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.generalError)
        assertEquals(
            UiText.Resource(R.string.health_records_all_empty_error),
            viewModel.formState.value.generalError
        )
    }

    @Test
    fun `saveRecord trims conditionNote`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateConditionNote("  体調良好  ")

        viewModel.saveRecord()
        advanceUntilIdle()

        repository.getAllRecords().test {
            val records = awaitItem()
            assertEquals(1, records.size)
            assertEquals("体調良好", records[0].conditionNote)
        }
    }

    @Test
    fun `saveRecord failure shows error snackbar`() = runTest(testDispatcher) {
        viewModel = createAddViewModel()
        viewModel.updateTemperature("36.5")
        repository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.saveRecord()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.health_records_save_failed,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `saveRecord failure keeps isSaving false`() = runTest(testDispatcher) {
        viewModel = createAddViewModel()
        viewModel.updateTemperature("36.5")
        repository.shouldFail = true

        viewModel.saveRecord()
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.isSaving)
    }

    // --- Edit Mode Tests ---

    @Test
    fun `edit mode is true when recordId is provided`() {
        repository.setRecords(
            listOf(
                HealthRecord(
                    id = 1L,
                    temperature = 36.5,
                    recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)

        assertTrue(viewModel.formState.value.isEditMode)
    }

    @Test
    fun `edit mode loads existing record data`() = runTest {
        repository.setRecords(
            listOf(
                HealthRecord(
                    id = 1L,
                    temperature = 37.2,
                    bloodPressureHigh = 130,
                    bloodPressureLow = 85,
                    pulse = 80,
                    weight = 65.5,
                    meal = MealAmount.MOSTLY,
                    excretion = ExcretionType.NORMAL,
                    conditionNote = "少し熱がある",
                    recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals("37.2", state.temperature)
        assertEquals("130", state.bloodPressureHigh)
        assertEquals("85", state.bloodPressureLow)
        assertEquals("80", state.pulse)
        assertEquals("65.5", state.weight)
        assertEquals(MealAmount.MOSTLY, state.meal)
        assertEquals(ExcretionType.NORMAL, state.excretion)
        assertEquals("少し熱がある", state.conditionNote)
    }

    @Test
    fun `saveRecord in edit mode updates existing record`() = runTest {
        repository.setRecords(
            listOf(
                HealthRecord(
                    id = 1L,
                    temperature = 36.5,
                    bloodPressureHigh = 120,
                    bloodPressureLow = 80,
                    recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTemperature("37.0")
        viewModel.saveRecord()
        advanceUntilIdle()

        repository.getAllRecords().test {
            val records = awaitItem()
            assertEquals(1, records.size)
            assertEquals(37.0, records[0].temperature)
        }
    }

    // --- Validation Tests ---

    @Test
    fun `temperature out of range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updateTemperature("43.0")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.temperatureError)
        assertEquals(
            UiText.ResourceWithArgs(
                R.string.health_records_temperature_range_error,
                listOf(AppConfig.HealthRecord.TEMPERATURE_MIN, AppConfig.HealthRecord.TEMPERATURE_MAX)
            ),
            viewModel.formState.value.temperatureError
        )
    }

    @Test
    fun `temperature below range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updateTemperature("33.0")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.temperatureError)
    }

    @Test
    fun `blood pressure out of range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updateBloodPressureHigh("260")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.bloodPressureError)
        assertEquals(
            UiText.ResourceWithArgs(
                R.string.health_records_blood_pressure_range_error,
                listOf(AppConfig.HealthRecord.BLOOD_PRESSURE_MIN, AppConfig.HealthRecord.BLOOD_PRESSURE_MAX)
            ),
            viewModel.formState.value.bloodPressureError
        )
    }

    @Test
    fun `blood pressure low out of range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updateBloodPressureLow("30")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.bloodPressureError)
    }

    @Test
    fun `pulse out of range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updatePulse("210")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.pulseError)
        assertEquals(
            UiText.ResourceWithArgs(
                R.string.health_records_pulse_range_error,
                listOf(AppConfig.HealthRecord.PULSE_MIN, AppConfig.HealthRecord.PULSE_MAX)
            ),
            viewModel.formState.value.pulseError
        )
    }

    @Test
    fun `pulse below range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updatePulse("20")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.pulseError)
    }

    @Test
    fun `weight out of range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updateWeight("210.0")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.weightError)
        assertEquals(
            UiText.ResourceWithArgs(
                R.string.health_records_weight_range_error,
                listOf(AppConfig.HealthRecord.WEIGHT_MIN, AppConfig.HealthRecord.WEIGHT_MAX)
            ),
            viewModel.formState.value.weightError
        )
    }

    @Test
    fun `weight below range shows error`() {
        viewModel = createAddViewModel()

        viewModel.updateWeight("10.0")
        viewModel.saveRecord()

        assertNotNull(viewModel.formState.value.weightError)
    }

    @Test
    fun `formState is immutable across updates`() {
        viewModel = createAddViewModel()
        val before = viewModel.formState.value

        viewModel.updateTemperature("36.5")
        val after = viewModel.formState.value

        assertEquals("", before.temperature)
        assertEquals("36.5", after.temperature)
    }

    @Test
    fun `updateTemperature clears temperature error`() {
        viewModel = createAddViewModel()
        viewModel.updateTemperature("43.0")
        viewModel.saveRecord()
        assertNotNull(viewModel.formState.value.temperatureError)

        viewModel.updateTemperature("36.5")

        assertNull(viewModel.formState.value.temperatureError)
    }

    @Test
    fun `updateBloodPressureHigh clears blood pressure error`() {
        viewModel = createAddViewModel()
        viewModel.updateBloodPressureHigh("260")
        viewModel.saveRecord()
        assertNotNull(viewModel.formState.value.bloodPressureError)

        viewModel.updateBloodPressureHigh("120")

        assertNull(viewModel.formState.value.bloodPressureError)
    }

    @Test
    fun `updatePulse clears pulse error`() {
        viewModel = createAddViewModel()
        viewModel.updatePulse("210")
        viewModel.saveRecord()
        assertNotNull(viewModel.formState.value.pulseError)

        viewModel.updatePulse("72")

        assertNull(viewModel.formState.value.pulseError)
    }

    @Test
    fun `updateWeight clears weight error`() {
        viewModel = createAddViewModel()
        viewModel.updateWeight("210.0")
        viewModel.saveRecord()
        assertNotNull(viewModel.formState.value.weightError)

        viewModel.updateWeight("60.0")

        assertNull(viewModel.formState.value.weightError)
    }

    @Test
    fun `saveRecord with only meal succeeds`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateMeal(MealAmount.FULL)

        viewModel.saveRecord()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            val saved = awaitItem()
            assertTrue(saved)
        }
    }

    @Test
    fun `saveRecord with only conditionNote succeeds`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateConditionNote("体調良好")

        viewModel.saveRecord()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            val saved = awaitItem()
            assertTrue(saved)
        }
    }

    @Test
    fun `isSaving is false initially`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.formState.value.isSaving)
    }

    // --- isDirty Tests ---

    @Test
    fun `isDirty is false initially in add mode`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when temperature changed`() {
        viewModel = createAddViewModel()

        viewModel.updateTemperature("36.5")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when temperature cleared`() {
        viewModel = createAddViewModel()

        viewModel.updateTemperature("36.5")
        assertTrue(viewModel.isDirty)

        viewModel.updateTemperature("")
        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty is false after loading existing data`() = runTest {
        repository.setRecords(
            listOf(
                HealthRecord(
                    id = 1L,
                    temperature = 36.5,
                    recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when field changed in edit mode`() = runTest {
        repository.setRecords(
            listOf(
                HealthRecord(
                    id = 1L,
                    temperature = 36.5,
                    recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTemperature("37.0")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when reverted to original in edit mode`() = runTest {
        repository.setRecords(
            listOf(
                HealthRecord(
                    id = 1L,
                    temperature = 36.5,
                    recordedAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    createdAt = LocalDateTime.of(2025, 3, 15, 10, 0),
                    updatedAt = LocalDateTime.of(2025, 3, 15, 10, 0)
                )
            )
        )
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateTemperature("37.0")
        assertTrue(viewModel.isDirty)

        viewModel.updateTemperature("36.5")
        assertFalse(viewModel.isDirty)
    }
}
