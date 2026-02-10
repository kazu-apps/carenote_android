package com.carenote.app.ui.screens.medication

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Medication
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
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditMedicationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var reminderScheduler: FakeMedicationReminderScheduler
    private lateinit var viewModel: AddEditMedicationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        medicationRepository = FakeMedicationRepository()
        reminderScheduler = FakeMedicationReminderScheduler()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createAddViewModel(): AddEditMedicationViewModel {
        return AddEditMedicationViewModel(
            SavedStateHandle(),
            medicationRepository,
            reminderScheduler
        )
    }

    private fun createEditViewModel(medicationId: Long): AddEditMedicationViewModel {
        return AddEditMedicationViewModel(
            SavedStateHandle(mapOf("medicationId" to medicationId)),
            medicationRepository,
            reminderScheduler
        )
    }

    // --- Add Mode Tests ---

    @Test
    fun `initial form state has empty fields`() {
        viewModel = createAddViewModel()
        val state = viewModel.formState.value

        assertEquals("", state.name)
        assertEquals("", state.dosage)
        assertTrue(state.timings.isEmpty())
        assertTrue(state.times.isEmpty())
        assertTrue(state.reminderEnabled)
        assertNull(state.nameError)
        assertFalse(state.isEditMode)
    }

    @Test
    fun `updateName updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateName("テスト薬")

        assertEquals("テスト薬", viewModel.formState.value.name)
    }

    @Test
    fun `updateName clears name error`() {
        viewModel = createAddViewModel()
        viewModel.saveMedication()
        assertNotNull(viewModel.formState.value.nameError)

        viewModel.updateName("テスト薬")
        assertNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `updateDosage updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateDosage("1錠")

        assertEquals("1錠", viewModel.formState.value.dosage)
    }

    @Test
    fun `toggleTiming adds timing when not present`() {
        viewModel = createAddViewModel()

        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertTrue(viewModel.formState.value.timings.contains(MedicationTiming.MORNING))
    }

    @Test
    fun `toggleTiming removes timing when already present`() {
        viewModel = createAddViewModel()
        viewModel.toggleTiming(MedicationTiming.MORNING)
        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertFalse(viewModel.formState.value.timings.contains(MedicationTiming.MORNING))
    }

    @Test
    fun `toggleTiming adds default time when timing added`() {
        viewModel = createAddViewModel()

        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertNotNull(viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `toggleTiming removes time when timing removed`() {
        viewModel = createAddViewModel()
        viewModel.toggleTiming(MedicationTiming.MORNING)
        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertNull(viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `updateTime updates time for timing`() {
        viewModel = createAddViewModel()
        viewModel.toggleTiming(MedicationTiming.MORNING)
        val newTime = LocalTime.of(9, 30)

        viewModel.updateTime(MedicationTiming.MORNING, newTime)

        assertEquals(newTime, viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `toggleReminder toggles reminder state`() {
        viewModel = createAddViewModel()
        assertTrue(viewModel.formState.value.reminderEnabled)

        viewModel.toggleReminder()
        assertFalse(viewModel.formState.value.reminderEnabled)

        viewModel.toggleReminder()
        assertTrue(viewModel.formState.value.reminderEnabled)
    }

    @Test
    fun `saveMedication with empty name sets error`() {
        viewModel = createAddViewModel()

        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `saveMedication with blank name sets error`() {
        viewModel = createAddViewModel()
        viewModel.updateName("   ")

        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `saveMedication with valid name succeeds`() = runTest {
        viewModel = createAddViewModel()
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
        viewModel = createAddViewModel()
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
        viewModel = createAddViewModel()
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
        viewModel = createAddViewModel()

        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertEquals(LocalTime.of(8, 0), viewModel.formState.value.times[MedicationTiming.MORNING])
    }

    @Test
    fun `default time for NOON is 12 00`() {
        viewModel = createAddViewModel()

        viewModel.toggleTiming(MedicationTiming.NOON)

        assertEquals(LocalTime.of(12, 0), viewModel.formState.value.times[MedicationTiming.NOON])
    }

    @Test
    fun `default time for EVENING is 18 00`() {
        viewModel = createAddViewModel()

        viewModel.toggleTiming(MedicationTiming.EVENING)

        assertEquals(LocalTime.of(18, 0), viewModel.formState.value.times[MedicationTiming.EVENING])
    }

    @Test
    fun `isSaving is false initially`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `formState is immutable across updates`() {
        viewModel = createAddViewModel()
        val before = viewModel.formState.value
        viewModel.updateName("新しい名前")
        val after = viewModel.formState.value

        assertEquals("", before.name)
        assertEquals("新しい名前", after.name)
    }

    @Test
    fun `saveMedication failure shows error snackbar`() = runTest(testDispatcher) {
        viewModel = createAddViewModel()
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
        viewModel = createAddViewModel()
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)
        medicationRepository.shouldFail = true

        viewModel.saveMedication()
        advanceUntilIdle()

        assertFalse(viewModel.formState.value.isSaving)
    }

    @Test
    fun `saveMedication with name exceeding max length sets error`() {
        viewModel = createAddViewModel()
        val longName = "a".repeat(AppConfig.Medication.NAME_MAX_LENGTH + 1)
        viewModel.updateName(longName)

        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.nameError)
    }

    @Test
    fun `saveMedication with dosage exceeding max length sets error`() {
        viewModel = createAddViewModel()
        viewModel.updateName("テスト薬")
        val longDosage = "a".repeat(AppConfig.Medication.DOSAGE_MAX_LENGTH + 1)
        viewModel.updateDosage(longDosage)

        viewModel.saveMedication()

        assertNotNull(viewModel.formState.value.dosageError)
    }

    @Test
    fun `saveMedication failure does not emit savedEvent`() = runTest(testDispatcher) {
        viewModel = createAddViewModel()
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
            viewModel = createAddViewModel()
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
            viewModel = createAddViewModel()
            viewModel.updateName("テスト薬")
            viewModel.toggleTiming(MedicationTiming.MORNING)
            viewModel.toggleReminder()

            viewModel.saveMedication()
            advanceUntilIdle()

            assertTrue(reminderScheduler.scheduleAllCalls.isEmpty())
        }

    @Test
    fun `saveMedication failure does not schedule reminders`() = runTest(testDispatcher) {
        viewModel = createAddViewModel()
        viewModel.updateName("テスト薬")
        viewModel.toggleTiming(MedicationTiming.MORNING)
        medicationRepository.shouldFail = true

        viewModel.saveMedication()
        advanceUntilIdle()

        assertTrue(reminderScheduler.scheduleAllCalls.isEmpty())
    }

    // --- Stock Tests ---

    @Test
    fun `updateCurrentStock updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateCurrentStock("30")

        assertEquals("30", viewModel.formState.value.currentStock)
    }

    @Test
    fun `updateLowStockThreshold updates form state`() {
        viewModel = createAddViewModel()

        viewModel.updateLowStockThreshold("10")

        assertEquals("10", viewModel.formState.value.lowStockThreshold)
    }

    @Test
    fun `saveMedication maps stock fields to domain`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateName("テスト薬")
        viewModel.updateCurrentStock("20")
        viewModel.updateLowStockThreshold("3")

        viewModel.saveMedication()
        advanceUntilIdle()

        medicationRepository.getAllMedications().test {
            val medications = awaitItem()
            assertEquals(1, medications.size)
            assertEquals(20, medications[0].currentStock)
            assertEquals(3, medications[0].lowStockThreshold)
        }
    }

    @Test
    fun `saveMedication with empty stock maps to null`() = runTest {
        viewModel = createAddViewModel()
        viewModel.updateName("テスト薬")

        viewModel.saveMedication()
        advanceUntilIdle()

        medicationRepository.getAllMedications().test {
            val medications = awaitItem()
            assertEquals(1, medications.size)
            assertNull(medications[0].currentStock)
            assertNull(medications[0].lowStockThreshold)
        }
    }

    // --- isDirty Tests (Add Mode) ---

    @Test
    fun `isDirty is false initially in add mode`() {
        viewModel = createAddViewModel()

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when name changed`() {
        viewModel = createAddViewModel()

        viewModel.updateName("テスト薬")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when name cleared`() {
        viewModel = createAddViewModel()
        viewModel.updateName("テスト薬")
        assertTrue(viewModel.isDirty)

        viewModel.updateName("")
        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when dosage changed`() {
        viewModel = createAddViewModel()

        viewModel.updateDosage("1錠")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when timing toggled`() {
        viewModel = createAddViewModel()

        viewModel.toggleTiming(MedicationTiming.MORNING)

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty ignores error fields`() {
        viewModel = createAddViewModel()
        viewModel.saveMedication()
        assertNotNull(viewModel.formState.value.nameError)

        assertFalse(viewModel.isDirty)
    }

    // --- Edit Mode Tests ---

    private val existingMedication = Medication(
        id = 1L,
        name = "アムロジピン",
        dosage = "5mg",
        timings = listOf(MedicationTiming.MORNING),
        times = mapOf(MedicationTiming.MORNING to LocalTime.of(8, 0)),
        reminderEnabled = true,
        createdAt = LocalDateTime.of(2025, 1, 15, 9, 0),
        updatedAt = LocalDateTime.of(2025, 1, 15, 9, 0),
        currentStock = 15,
        lowStockThreshold = 5
    )

    @Test
    fun `edit mode is true when medicationId is provided`() {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)

        assertTrue(viewModel.formState.value.isEditMode)
    }

    @Test
    fun `edit mode loads existing medication data`() = runTest {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals("アムロジピン", state.name)
        assertEquals("5mg", state.dosage)
        assertEquals(listOf(MedicationTiming.MORNING), state.timings)
        assertEquals(mapOf(MedicationTiming.MORNING to LocalTime.of(8, 0)), state.times)
        assertTrue(state.reminderEnabled)
    }

    @Test
    fun `edit mode loads existing stock data`() = runTest {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        val state = viewModel.formState.value
        assertEquals("15", state.currentStock)
        assertEquals("5", state.lowStockThreshold)
    }

    @Test
    fun `saveMedication updates existing medication in edit mode`() = runTest {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateName("新しい薬名")
        viewModel.updateDosage("10mg")
        viewModel.saveMedication()
        advanceUntilIdle()

        medicationRepository.getAllMedications().test {
            val medications = awaitItem()
            assertEquals(1, medications.size)
            assertEquals("新しい薬名", medications[0].name)
            assertEquals("10mg", medications[0].dosage)
        }
    }

    @Test
    fun `saveMedication in edit mode reschedules reminders`() = runTest(testDispatcher) {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.toggleTiming(MedicationTiming.EVENING)
        viewModel.saveMedication()
        advanceUntilIdle()

        assertEquals(1, reminderScheduler.cancelRemindersCalls.size)
        assertEquals(1L, reminderScheduler.cancelRemindersCalls[0].medicationId)
        assertEquals(1, reminderScheduler.scheduleAllCalls.size)
        val call = reminderScheduler.scheduleAllCalls[0]
        assertEquals(1L, call.medicationId)
        assertEquals(2, call.times.size)
        assertTrue(call.times.containsKey(MedicationTiming.MORNING))
        assertTrue(call.times.containsKey(MedicationTiming.EVENING))
    }

    @Test
    fun `saveMedication in edit mode with reminder disabled cancels existing reminders`() =
        runTest(testDispatcher) {
            medicationRepository.setMedications(listOf(existingMedication))
            viewModel = createEditViewModel(1L)
            advanceUntilIdle()

            viewModel.toggleReminder()
            viewModel.saveMedication()
            advanceUntilIdle()

            assertEquals(1, reminderScheduler.cancelRemindersCalls.size)
            assertEquals(1L, reminderScheduler.cancelRemindersCalls[0].medicationId)
            assertTrue(reminderScheduler.scheduleAllCalls.isEmpty())
        }

    @Test
    fun `saveMedication failure in edit mode shows error snackbar`() = runTest(testDispatcher) {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()
        medicationRepository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.updateName("変更名")
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
    fun `isDirty is false after loading existing data in edit mode`() = runTest {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        assertFalse(viewModel.isDirty)
    }

    @Test
    fun `isDirty becomes true when field changed in edit mode`() = runTest {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateName("変更薬名")

        assertTrue(viewModel.isDirty)
    }

    @Test
    fun `isDirty returns to false when reverted to original in edit mode`() = runTest {
        medicationRepository.setMedications(listOf(existingMedication))
        viewModel = createEditViewModel(1L)
        advanceUntilIdle()

        viewModel.updateName("変更薬名")
        assertTrue(viewModel.isDirty)

        viewModel.updateName("アムロジピン")
        assertFalse(viewModel.isDirty)
    }
}
