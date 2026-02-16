package com.carenote.app.ui.screens.medication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.util.Clock
import com.carenote.app.domain.validator.MedicationValidator
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.repository.MedicationReminderSchedulerInterface
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.util.FormValidator.combineValidations
import com.carenote.app.ui.util.FormValidator.validateMaxLength
import com.carenote.app.ui.util.FormValidator.validateRequired
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * 薬追加・編集フォームの状態
 */
data class AddEditMedicationFormState(
    val name: String = "",
    val dosage: String = "",
    val timings: List<MedicationTiming> = emptyList(),
    val times: Map<MedicationTiming, LocalTime> = emptyMap(),
    val reminderEnabled: Boolean = true,
    val currentStock: String = "",
    val lowStockThreshold: String = "",
    val nameError: UiText? = null,
    val dosageError: UiText? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditMedicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicationRepository: MedicationRepository,
    private val reminderScheduler: MedicationReminderSchedulerInterface,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock
) : ViewModel() {

    private val medicationId: Long? = savedStateHandle.get<Long>("medicationId")

    private val _formState = MutableStateFlow(
        AddEditMedicationFormState(isEditMode = medicationId != null)
    )
    val formState: StateFlow<AddEditMedicationFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var originalMedication: Medication? = null
    private var _initialFormState: AddEditMedicationFormState? = null

    val isDirty: Boolean
        get() {
            val initial = _initialFormState ?: return false
            val current = _formState.value.copy(
                nameError = null,
                dosageError = null,
                isSaving = false,
                isEditMode = false
            )
            val baseline = initial.copy(
                nameError = null,
                dosageError = null,
                isSaving = false,
                isEditMode = false
            )
            return current != baseline
        }

    init {
        if (medicationId != null) {
            loadMedication(medicationId)
        } else {
            _initialFormState = _formState.value
        }
    }

    private fun loadMedication(id: Long) {
        viewModelScope.launch {
            val medication = medicationRepository.getMedicationById(id).firstOrNull()
            if (medication != null) {
                originalMedication = medication
                _formState.value = _formState.value.copy(
                    name = medication.name,
                    dosage = medication.dosage,
                    timings = medication.timings,
                    times = medication.times,
                    reminderEnabled = medication.reminderEnabled,
                    currentStock = medication.currentStock?.toString() ?: "",
                    lowStockThreshold = medication.lowStockThreshold?.toString() ?: ""
                )
                _initialFormState = _formState.value
            }
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun updateDosage(dosage: String) {
        _formState.value = _formState.value.copy(
            dosage = dosage,
            dosageError = null
        )
    }

    fun toggleTiming(timing: MedicationTiming) {
        val current = _formState.value
        val newTimings = if (current.timings.contains(timing)) {
            current.timings - timing
        } else {
            current.timings + timing
        }
        val newTimes = if (current.timings.contains(timing)) {
            current.times - timing
        } else {
            current.times + (timing to defaultTimeForTiming(timing))
        }
        _formState.value = current.copy(
            timings = newTimings,
            times = newTimes
        )
    }

    fun updateTime(timing: MedicationTiming, time: LocalTime) {
        _formState.value = _formState.value.copy(
            times = _formState.value.times + (timing to time)
        )
    }

    fun updateCurrentStock(stock: String) {
        _formState.value = _formState.value.copy(currentStock = stock)
    }

    fun updateLowStockThreshold(threshold: String) {
        _formState.value = _formState.value.copy(lowStockThreshold = threshold)
    }

    fun toggleReminder() {
        _formState.value = _formState.value.copy(
            reminderEnabled = !_formState.value.reminderEnabled
        )
    }

    fun saveMedication() {
        val current = _formState.value

        val nameError = combineValidations(
            validateRequired(current.name, R.string.medication_name_required),
            validateMaxLength(current.name, AppConfig.Medication.NAME_MAX_LENGTH)
        )
        val dosageError = validateMaxLength(
            current.dosage, AppConfig.Medication.DOSAGE_MAX_LENGTH
        )

        if (nameError != null || dosageError != null) {
            _formState.value = current.copy(
                nameError = nameError,
                dosageError = dosageError
            )
            return
        }

        if (!validateStockFields(current)) return

        _formState.value = current.copy(isSaving = true)
        viewModelScope.launch { persistMedication(current) }
    }

    private fun validateStockFields(
        current: AddEditMedicationFormState
    ): Boolean {
        val parsedStock = parseStockValue(current.currentStock)
        val parsedThreshold = parseStockValue(current.lowStockThreshold)
        val stockInvalid = parsedStock != null &&
            MedicationValidator.validateStock(parsedStock) != null
        val thresholdInvalid = parsedThreshold != null &&
            MedicationValidator.validateLowStockThreshold(parsedThreshold) != null
        if (stockInvalid || thresholdInvalid) {
            viewModelScope.launch {
                snackbarController.showMessage(
                    R.string.medication_stock_validation_range
                )
            }
            return false
        }
        return true
    }

    private suspend fun persistMedication(
        current: AddEditMedicationFormState
    ) {
        val parsedStock = parseStockValue(current.currentStock)
        val parsedThreshold = parseStockValue(current.lowStockThreshold)
        val original = originalMedication

        if (medicationId != null && original != null) {
            updateExistingMedication(
                original, current, parsedStock, parsedThreshold
            )
        } else {
            createNewMedication(current, parsedStock, parsedThreshold)
        }
    }

    private suspend fun updateExistingMedication(
        original: Medication,
        current: AddEditMedicationFormState,
        parsedStock: Int?,
        parsedThreshold: Int?
    ) {
        val updatedMedication = original.copy(
            name = current.name.trim(),
            dosage = current.dosage.trim(),
            timings = current.timings,
            times = current.times,
            reminderEnabled = current.reminderEnabled,
            currentStock = parsedStock,
            lowStockThreshold = parsedThreshold,
            updatedAt = clock.now()
        )
        medicationRepository.updateMedication(updatedMedication)
            .onSuccess {
                Timber.d("Medication updated: id=$medicationId")
                analyticsRepository.logEvent(
                    AppConfig.Analytics.EVENT_MEDICATION_UPDATED
                )
                scheduleRemindersIfEnabled(medicationId!!, current)
                _savedEvent.send(true)
            }
            .onFailure { error ->
                Timber.w("Failed to update medication: $error")
                _formState.value = _formState.value.copy(isSaving = false)
                snackbarController.showMessage(R.string.medication_save_failed)
            }
    }

    private suspend fun createNewMedication(
        current: AddEditMedicationFormState,
        parsedStock: Int?,
        parsedThreshold: Int?
    ) {
        val medication = Medication(
            name = current.name.trim(),
            dosage = current.dosage.trim(),
            timings = current.timings,
            times = current.times,
            reminderEnabled = current.reminderEnabled,
            currentStock = parsedStock,
            lowStockThreshold = parsedThreshold
        )
        medicationRepository.insertMedication(medication)
            .onSuccess { id ->
                Timber.d("Medication saved: id=$id")
                analyticsRepository.logEvent(
                    AppConfig.Analytics.EVENT_MEDICATION_CREATED
                )
                scheduleRemindersIfEnabled(id, current)
                _savedEvent.send(true)
            }
            .onFailure { error ->
                Timber.w("Failed to save medication: $error")
                _formState.value = _formState.value.copy(isSaving = false)
                snackbarController.showMessage(R.string.medication_save_failed)
            }
    }

    private suspend fun scheduleRemindersIfEnabled(
        id: Long,
        current: AddEditMedicationFormState
    ) {
        reminderScheduler.cancelReminders(id)
        if (current.reminderEnabled && current.times.isNotEmpty()) {
            reminderScheduler.scheduleAllReminders(
                medicationId = id,
                medicationName = current.name.trim(),
                times = current.times
            )
        }
    }

    private fun parseStockValue(value: String): Int? {
        if (value.isBlank()) return null
        return value.trim().toIntOrNull()
    }

    private fun defaultTimeForTiming(timing: MedicationTiming): LocalTime = when (timing) {
        MedicationTiming.MORNING -> LocalTime.of(
            AppConfig.Medication.DEFAULT_MORNING_HOUR,
            AppConfig.Medication.DEFAULT_MORNING_MINUTE
        )
        MedicationTiming.NOON -> LocalTime.of(
            AppConfig.Medication.DEFAULT_NOON_HOUR,
            AppConfig.Medication.DEFAULT_NOON_MINUTE
        )
        MedicationTiming.EVENING -> LocalTime.of(
            AppConfig.Medication.DEFAULT_EVENING_HOUR,
            AppConfig.Medication.DEFAULT_EVENING_MINUTE
        )
    }

}
