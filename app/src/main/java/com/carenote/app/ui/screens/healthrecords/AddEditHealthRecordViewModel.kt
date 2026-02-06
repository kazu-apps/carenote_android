package com.carenote.app.ui.screens.healthrecords

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.ui.common.UiText
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
import javax.inject.Inject

data class AddEditHealthRecordFormState(
    val temperature: String = "",
    val bloodPressureHigh: String = "",
    val bloodPressureLow: String = "",
    val pulse: String = "",
    val weight: String = "",
    val meal: MealAmount? = null,
    val excretion: ExcretionType? = null,
    val conditionNote: String = "",
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val temperatureError: UiText? = null,
    val bloodPressureError: UiText? = null,
    val pulseError: UiText? = null,
    val weightError: UiText? = null,
    val conditionNoteError: UiText? = null,
    val generalError: UiText? = null,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddEditHealthRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val recordId: Long? = savedStateHandle.get<Long>("recordId")

    private val _formState = MutableStateFlow(
        AddEditHealthRecordFormState(isEditMode = recordId != null)
    )
    val formState: StateFlow<AddEditHealthRecordFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var originalRecord: HealthRecord? = null

    init {
        if (recordId != null) {
            loadRecord(recordId)
        }
    }

    private fun loadRecord(id: Long) {
        viewModelScope.launch {
            val record = healthRecordRepository.getRecordById(id).firstOrNull()
            if (record != null) {
                originalRecord = record
                _formState.value = _formState.value.copy(
                    temperature = record.temperature?.toString() ?: "",
                    bloodPressureHigh = record.bloodPressureHigh?.toString() ?: "",
                    bloodPressureLow = record.bloodPressureLow?.toString() ?: "",
                    pulse = record.pulse?.toString() ?: "",
                    weight = record.weight?.toString() ?: "",
                    meal = record.meal,
                    excretion = record.excretion,
                    conditionNote = record.conditionNote,
                    recordedAt = record.recordedAt
                )
            }
        }
    }

    fun updateTemperature(value: String) {
        _formState.value = _formState.value.copy(
            temperature = value,
            temperatureError = null,
            generalError = null
        )
    }

    fun updateBloodPressureHigh(value: String) {
        _formState.value = _formState.value.copy(
            bloodPressureHigh = value,
            bloodPressureError = null,
            generalError = null
        )
    }

    fun updateBloodPressureLow(value: String) {
        _formState.value = _formState.value.copy(
            bloodPressureLow = value,
            bloodPressureError = null,
            generalError = null
        )
    }

    fun updatePulse(value: String) {
        _formState.value = _formState.value.copy(
            pulse = value,
            pulseError = null,
            generalError = null
        )
    }

    fun updateWeight(value: String) {
        _formState.value = _formState.value.copy(
            weight = value,
            weightError = null,
            generalError = null
        )
    }

    fun updateMeal(value: MealAmount?) {
        _formState.value = _formState.value.copy(
            meal = value,
            generalError = null
        )
    }

    fun updateExcretion(value: ExcretionType?) {
        _formState.value = _formState.value.copy(
            excretion = value,
            generalError = null
        )
    }

    fun updateConditionNote(value: String) {
        _formState.value = _formState.value.copy(
            conditionNote = value,
            conditionNoteError = null,
            generalError = null
        )
    }

    fun updateRecordedAt(value: LocalDateTime) {
        _formState.value = _formState.value.copy(recordedAt = value)
    }

    fun saveRecord() {
        val current = _formState.value
        val parsed = parseFormFields(current)

        if (!parsed.hasAnyField) {
            _formState.value = current.copy(
                generalError = UiText.Resource(R.string.health_records_all_empty_error)
            )
            return
        }

        val errors = validateFields(current, parsed)
        if (errors != null) {
            _formState.value = current.copy(
                temperatureError = errors.temperatureError,
                bloodPressureError = errors.bloodPressureError,
                pulseError = errors.pulseError,
                weightError = errors.weightError,
                conditionNoteError = errors.conditionNoteError
            )
            return
        }

        _formState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            persistRecord(current, parsed)
        }
    }

    private suspend fun persistRecord(
        current: AddEditHealthRecordFormState,
        parsed: ParsedFields
    ) {
        val now = LocalDateTime.now()
        val original = originalRecord

        if (recordId != null && original != null) {
            val updatedRecord = original.copy(
                temperature = parsed.temperature,
                bloodPressureHigh = parsed.bpHigh,
                bloodPressureLow = parsed.bpLow,
                pulse = parsed.pulse,
                weight = parsed.weight,
                meal = current.meal,
                excretion = current.excretion,
                conditionNote = parsed.trimmedNote,
                recordedAt = current.recordedAt,
                updatedAt = now
            )
            healthRecordRepository.updateRecord(updatedRecord)
                .onSuccess {
                    Timber.d("Health record updated: id=$recordId")
                    _savedEvent.send(true)
                }
                .onFailure { error ->
                    Timber.w("Failed to update health record: $error")
                    _formState.value = _formState.value.copy(isSaving = false)
                    snackbarController.showMessage(R.string.health_records_save_failed)
                }
        } else {
            val newRecord = HealthRecord(
                temperature = parsed.temperature,
                bloodPressureHigh = parsed.bpHigh,
                bloodPressureLow = parsed.bpLow,
                pulse = parsed.pulse,
                weight = parsed.weight,
                meal = current.meal,
                excretion = current.excretion,
                conditionNote = parsed.trimmedNote,
                recordedAt = current.recordedAt,
                createdAt = now,
                updatedAt = now
            )
            healthRecordRepository.insertRecord(newRecord)
                .onSuccess { id ->
                    Timber.d("Health record saved: id=$id")
                    _savedEvent.send(true)
                }
                .onFailure { error ->
                    Timber.w("Failed to save health record: $error")
                    _formState.value = _formState.value.copy(isSaving = false)
                    snackbarController.showMessage(R.string.health_records_save_failed)
                }
        }
    }

    private fun parseFormFields(state: AddEditHealthRecordFormState): ParsedFields {
        val temperature = state.temperature.trim().toDoubleOrNull()
        val bpHigh = state.bloodPressureHigh.trim().toIntOrNull()
        val bpLow = state.bloodPressureLow.trim().toIntOrNull()
        val pulse = state.pulse.trim().toIntOrNull()
        val weight = state.weight.trim().toDoubleOrNull()
        val trimmedNote = state.conditionNote.trim()

        val hasAnyField = temperature != null || bpHigh != null || bpLow != null ||
            pulse != null || weight != null || state.meal != null ||
            state.excretion != null || trimmedNote.isNotBlank()

        return ParsedFields(temperature, bpHigh, bpLow, pulse, weight, trimmedNote, hasAnyField)
    }

    private fun validateFields(
        state: AddEditHealthRecordFormState,
        parsed: ParsedFields
    ): ValidationErrors? {
        val tempErr = validateRange(
            state.temperature, parsed.temperature,
            AppConfig.HealthRecord.TEMPERATURE_MIN, AppConfig.HealthRecord.TEMPERATURE_MAX,
            UiText.ResourceWithArgs(
                R.string.health_records_temperature_range_error,
                listOf(AppConfig.HealthRecord.TEMPERATURE_MIN, AppConfig.HealthRecord.TEMPERATURE_MAX)
            )
        )
        val bpHighErr = validateRange(
            state.bloodPressureHigh, parsed.bpHigh?.toDouble(),
            AppConfig.HealthRecord.BLOOD_PRESSURE_MIN.toDouble(),
            AppConfig.HealthRecord.BLOOD_PRESSURE_MAX.toDouble(),
            UiText.ResourceWithArgs(
                R.string.health_records_blood_pressure_range_error,
                listOf(AppConfig.HealthRecord.BLOOD_PRESSURE_MIN, AppConfig.HealthRecord.BLOOD_PRESSURE_MAX)
            )
        )
        val bpLowErr = if (bpHighErr == null) {
            validateRange(
                state.bloodPressureLow, parsed.bpLow?.toDouble(),
                AppConfig.HealthRecord.BLOOD_PRESSURE_MIN.toDouble(),
                AppConfig.HealthRecord.BLOOD_PRESSURE_MAX.toDouble(),
                UiText.ResourceWithArgs(
                    R.string.health_records_blood_pressure_range_error,
                    listOf(AppConfig.HealthRecord.BLOOD_PRESSURE_MIN, AppConfig.HealthRecord.BLOOD_PRESSURE_MAX)
                )
            )
        } else {
            null
        }
        val bpErr = bpHighErr ?: bpLowErr
        val pulseErr = validateRange(
            state.pulse, parsed.pulse?.toDouble(),
            AppConfig.HealthRecord.PULSE_MIN.toDouble(),
            AppConfig.HealthRecord.PULSE_MAX.toDouble(),
            UiText.ResourceWithArgs(
                R.string.health_records_pulse_range_error,
                listOf(AppConfig.HealthRecord.PULSE_MIN, AppConfig.HealthRecord.PULSE_MAX)
            )
        )
        val weightErr = validateRange(
            state.weight, parsed.weight,
            AppConfig.HealthRecord.WEIGHT_MIN, AppConfig.HealthRecord.WEIGHT_MAX,
            UiText.ResourceWithArgs(
                R.string.health_records_weight_range_error,
                listOf(AppConfig.HealthRecord.WEIGHT_MIN, AppConfig.HealthRecord.WEIGHT_MAX)
            )
        )
        val conditionNoteErr = if (
            parsed.trimmedNote.length > AppConfig.HealthRecord.CONDITION_NOTE_MAX_LENGTH
        ) {
            UiText.ResourceWithArgs(
                R.string.ui_validation_too_long,
                listOf(AppConfig.HealthRecord.CONDITION_NOTE_MAX_LENGTH)
            )
        } else {
            null
        }

        return if (
            tempErr != null || bpErr != null || pulseErr != null ||
            weightErr != null || conditionNoteErr != null
        ) {
            ValidationErrors(tempErr, bpErr, pulseErr, weightErr, conditionNoteErr)
        } else {
            null
        }
    }

    private fun validateRange(
        rawValue: String,
        parsedValue: Double?,
        min: Double,
        max: Double,
        error: UiText
    ): UiText? {
        if (rawValue.isBlank()) return null
        if (parsedValue == null || parsedValue < min || parsedValue > max) return error
        return null
    }

    private data class ParsedFields(
        val temperature: Double?,
        val bpHigh: Int?,
        val bpLow: Int?,
        val pulse: Int?,
        val weight: Double?,
        val trimmedNote: String,
        val hasAnyField: Boolean
    )

    private data class ValidationErrors(
        val temperatureError: UiText?,
        val bloodPressureError: UiText?,
        val pulseError: UiText?,
        val weightError: UiText?,
        val conditionNoteError: UiText?
    )
}
