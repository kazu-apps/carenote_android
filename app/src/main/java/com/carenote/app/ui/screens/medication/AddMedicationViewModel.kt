package com.carenote.app.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.data.worker.MedicationReminderSchedulerInterface
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.ui.common.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

/**
 * 薬追加フォームの状態
 */
data class AddMedicationFormState(
    val name: String = "",
    val dosage: String = "",
    val timings: List<MedicationTiming> = emptyList(),
    val times: Map<MedicationTiming, LocalTime> = emptyMap(),
    val reminderEnabled: Boolean = true,
    val nameError: UiText? = null,
    val dosageError: UiText? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reminderScheduler: MedicationReminderSchedulerInterface
) : ViewModel() {

    private val _formState = MutableStateFlow(AddMedicationFormState())
    val formState: StateFlow<AddMedicationFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var _initialFormState: AddMedicationFormState = _formState.value

    val isDirty: Boolean
        get() {
            val current = _formState.value.copy(
                nameError = null,
                dosageError = null,
                isSaving = false
            )
            val initial = _initialFormState.copy(
                nameError = null,
                dosageError = null,
                isSaving = false
            )
            return current != initial
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

    fun toggleReminder() {
        _formState.value = _formState.value.copy(
            reminderEnabled = !_formState.value.reminderEnabled
        )
    }

    fun saveMedication() {
        val current = _formState.value

        if (current.name.isBlank()) {
            _formState.value = current.copy(
                nameError = UiText.Resource(R.string.medication_name_required)
            )
            return
        }

        val nameError = if (current.name.length > AppConfig.Medication.NAME_MAX_LENGTH) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Medication.NAME_MAX_LENGTH)
            )
        } else {
            null
        }
        val dosageError = if (current.dosage.length > AppConfig.Medication.DOSAGE_MAX_LENGTH) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.Medication.DOSAGE_MAX_LENGTH)
            )
        } else {
            null
        }

        if (nameError != null || dosageError != null) {
            _formState.value = current.copy(
                nameError = nameError,
                dosageError = dosageError
            )
            return
        }

        _formState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val medication = Medication(
                name = current.name.trim(),
                dosage = current.dosage.trim(),
                timings = current.timings,
                times = current.times,
                reminderEnabled = current.reminderEnabled
            )
            medicationRepository.insertMedication(medication)
                .onSuccess { id ->
                    Timber.d("Medication saved: id=$id")
                    if (current.reminderEnabled && current.times.isNotEmpty()) {
                        reminderScheduler.scheduleAllReminders(
                            medicationId = id,
                            medicationName = current.name.trim(),
                            times = current.times
                        )
                    }
                    _savedEvent.send(true)
                }
                .onFailure { error ->
                    Timber.w("Failed to save medication: $error")
                    _formState.value = _formState.value.copy(isSaving = false)
                    snackbarController.showMessage(R.string.medication_save_failed)
                }
        }
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
