package com.carenote.app.ui.screens.medication

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.fakes.FakeMedicationReminderScheduler
import com.carenote.app.fakes.FakeMedicationRepository
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
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddMedicationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var reminderScheduler: FakeMedicationReminderScheduler
    private lateinit var viewModel: AddMedicationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicationRepository = FakeMedicationRepository()
        reminderScheduler = FakeMedicationReminderScheduler()
        viewModel = AddMedicationViewModel(medicationRepository, reminderScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial form state has empty fields`() {
        val state = viewModel.formState.value

        assertEquals("", state.name)
        assertEquals("", state.dosage)
        assertTrue(state.timings.isEmpty())
        assertTrue(state.times.isEmpty())
        assertTrue(state.reminderEnabled)
        assertNull(state.nameError)
    }

    @Test
    fun `updateName updates form state`() {
        viewModel.updateName("テスト薬")

        assertEquals("テスト薬", viewModel.formState.value.name)
    }

    @Test
    fun `updateName clears name error`() {
        viewModel.saveMedication()
        assertNotNull(viewModel.formState.value.nameError)

        viewModel.updateName("テスト薬")
        assertNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `updateDosage updates form state`() {
        viewModel.updateDosage("1錠")

        assertEquals("1錠", viewModel.formState.value.dosage)
    }

    @Test
    fun `toggleTiming adds timing when not present`() {
        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertTrue(viewModel.formState.value.timings.contains(MedicationTiming.MORNING))
    }

    @Test
    fun `toggleTiming removes timing when already present`() {
        viewModel.toggleTiming(MedicationTiming.MORNING)
        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertFalse(viewModel.formState.value.timings.contains(MedicationTiming.MORNING))
    }

    @Test
    fun `toggleTiming adds default time when timing added`() {
        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertNotNull(viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `toggleTiming removes time when timing removed`() {
        viewModel.toggleTiming(MedicationTiming.MORNING)
        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertNull(viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `updateTime updates time for timing`() {
        viewModel.toggleTiming(MedicationTiming.MORNING)
        val newTime = LocalTime.of(9, 30)

        viewModel.updateTime(MedicationTiming.MORNING, newTime)

        assertEquals(newTime, viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `toggleReminder toggles reminder state`() {
        assertTrue(viewModel.formState.value.reminderEnabled)

        viewModel.toggleReminder()
        assertFalse(viewModel.formState.value.reminderEnabled)

        viewModel.toggleReminder()
        assertTrue(viewModel.formState.value.reminderEnabled)
    }

    @Test
    fun `saveMedication with empty name sets error`() {
        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `saveMedication with blank name sets error`() {
        viewModel.updateName("   ")

        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `saveMedication with valid name succeeds`() = runTest {
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)

        viewModel.saveMedication()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            val saved = awaitItem()
            assertTrue(saved)
        }
    }

    @Test
    fun `saveMedication inserts medication to repository`() = runTest {
        viewModel.updateName("テスト薬")
        viewModel.updateDosage("1錠")
        viewModel.toggleTiming(MedicationTiming.MORNING)

        viewModel.saveMedication()
        advanceUntilIdle()

        medicationRepository.getAllMedications().test {
            val medications = awaitItem()
            assertEquals(1, medications.size)
            assertEquals("テスト薬", medications[0].name)
            assertEquals("1錠", medications[0].dosage)
        }
    }

    @Test
    fun `saveMedication with multiple timings`() = runTest {
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)
        viewModel.toggleTiming(MedicationTiming.EVENING)

        viewModel.saveMedication()
        advanceUntilIdle()

        medicationRepository.getAllMedications().test {
            val medications = awaitItem()
            assertEquals(2, medications[0].timings.size)
            assertTrue(medications[0].timings.contains(MedicationTiming.MORNING))
            assertTrue(medications[0].timings.contains(MedicationTiming.EVENING))
        }
    }

    @Test
    fun `default time for MORNING is 8 00`() {
        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertEquals(LocalTime.of(8, 0), viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `default time for NOON is 12 00`() {
        viewModel.toggleTiming(MedicationTiming.NOON)

        assertEquals(LocalTime.of(12, 0), viewModel.formState.value.times[MedicationTiming.NOON])
    }

    @Test
    fun `default time for EVENING is 18 00`() {
        viewModel.toggleTiming(MedicationTiming.EVENING)

        assertEquals(LocalTime.of(18, 0), viewModel.formState.value.times[MedicationTiming.EVENING])
    }

    @Test
    fun `isSaving is false initially`() {
        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `formState is immutable across updates`() {
        val before = viewModel.formState.value
        viewModel.updateName("新しい名前")
        val after = viewModel.formState.value

        assertEquals("", before.name)
        assertEquals("新しい名前", after.name)
    }

    @Test
    fun `saveMedication failure shows error snackbar`() = runTest(testDispatcher) {
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)
        medicationRepository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.saveMedication()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.medication_save_failed,
                (event as SnackbarEvent.WithResId).messageResId
            )
        }
    }

    @Test
    fun `saveMedication failure keeps isSaving false`() = runTest(testDispatcher) {
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)
        medicationRepository.shouldFail = true

        viewModel.saveMedication()
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `saveMedication with name exceeding max length sets error`() {
        val longName = "a".repeat(AppConfig.Medication.NAME_MAX_LENGTH + 1)
        viewModel.updateName(longName)

        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `saveMedication with dosage exceeding max length sets error`() {
        viewModel.updateName("テスト薬")
        val longDosage = "a".repeat(AppConfig.Medication.DOSAGE_MAX_LENGTH + 1)
        viewModel.updateDosage(longDosage)

        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.dosageError)
    }

    @Test
    fun `saveMedication failure does not emit savedEvent`() = runTest(testDispatcher) {
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)
        medicationRepository.shouldFail = true

        viewModel.saveMedication()
        advanceUntilIdle()

        viewModel.savedEvent.test {
            expectNoEvents()
        }
    }

    @Test
    fun `saveMedication with reminder enabled and times schedules reminders`() =
        runTest(testDispatcher) {
            viewModel.updateName("テスト薬")
            viewModel.toggleTiming(MedicationTiming.MORNING)
            viewModel.toggleTiming(MedicationTiming.EVENING)

            viewModel.saveMedication()
            advanceUntilIdle()

            assertEquals(1, reminderScheduler.scheduleAllCalls.size)
            val call = reminderScheduler.scheduleAllCalls[0]
            assertEquals("テスト薬", call.medicationName)
            assertEquals(2, call.times.size)
            assertTrue(call.times.containsKey(MedicationTiming.MORNING))
            assertTrue(call.times.containsKey(MedicationTiming.EVENING))
        }

    @Test
    fun `saveMedication with reminder disabled does not schedule reminders`() =
        runTest(testDispatcher) {
            viewModel.updateName("テスト薬")
            viewModel.toggleTiming(MedicationTiming.MORNING)
            viewModel.toggleReminder()

            viewModel.saveMedication()
            advanceUntilIdle()

            assertTrue(reminderScheduler.scheduleAllCalls.isEmpty())
        }

    @Test
    fun `saveMedication failure does not schedule reminders`() = runTest(testDispatcher) {
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)
        medicationRepository.shouldFail = true

        viewModel.saveMedication()
        advanceUntilIdle()

        assertTrue(reminderScheduler.scheduleAllCalls.isEmpty())
    }
}
