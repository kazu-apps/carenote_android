package com.carenote.app.ui.screens.healthrecords

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.domain.repository.HealthRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    val temperatureError: String? = null,
    val bloodPressureError: String? = null,
    val pulseError: String? = null,
    val weightError: String? = null,
    val generalError: String? = null,
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

    private val _savedEvent = MutableSharedFlow<Boolean>(replay = 1)
    val savedEvent: SharedFlow<Boolean> = _savedEvent.asSharedFlow()

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
            _formState.value = current.copy(generalError = ALL_FIELDS_EMPTY_ERROR)
            return
        }

        val errors = validateFields(current, parsed)
        if (errors != null) {
            _formState.value = current.copy(
                temperatureError = errors.temperatureError,
                bloodPressureError = errors.bloodPressureError,
                pulseError = errors.pulseError,
                weightError = errors.weightError
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
                    _savedEvent.emit(true)
                }
                .onFailure { error ->
                    Timber.w("Failed to update health record: $error")
                    _formState.value = _formState.value.copy(isSaving = false)
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
                    _savedEvent.emit(true)
                }
                .onFailure { error ->
                    Timber.w("Failed to save health record: $error")
                    _formState.value = _formState.value.copy(isSaving = false)
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
            TEMPERATURE_RANGE_ERROR
        )
        val bpHighErr = validateRange(
            state.bloodPressureHigh, parsed.bpHigh?.toDouble(),
            AppConfig.HealthRecord.BLOOD_PRESSURE_MIN.toDouble(),
            AppConfig.HealthRecord.BLOOD_PRESSURE_MAX.toDouble(),
            BLOOD_PRESSURE_RANGE_ERROR
        )
        val bpLowErr = if (bpHighErr == null) {
            validateRange(
                state.bloodPressureLow, parsed.bpLow?.toDouble(),
                AppConfig.HealthRecord.BLOOD_PRESSURE_MIN.toDouble(),
                AppConfig.HealthRecord.BLOOD_PRESSURE_MAX.toDouble(),
                BLOOD_PRESSURE_RANGE_ERROR
            )
        } else {
            null
        }
        val bpErr = bpHighErr ?: bpLowErr
        val pulseErr = validateRange(
            state.pulse, parsed.pulse?.toDouble(),
            AppConfig.HealthRecord.PULSE_MIN.toDouble(),
            AppConfig.HealthRecord.PULSE_MAX.toDouble(),
            PULSE_RANGE_ERROR
        )
        val weightErr = validateRange(
            state.weight, parsed.weight,
            AppConfig.HealthRecord.WEIGHT_MIN, AppConfig.HealthRecord.WEIGHT_MAX,
            WEIGHT_RANGE_ERROR
        )

        return if (tempErr != null || bpErr != null || pulseErr != null || weightErr != null) {
            ValidationErrors(tempErr, bpErr, pulseErr, weightErr)
        } else {
            null
        }
    }

    private fun validateRange(
        rawValue: String,
        parsedValue: Double?,
        min: Double,
        max: Double,
        errorMessage: String
    ): String? {
        if (rawValue.isBlank()) return null
        if (parsedValue == null || parsedValue < min || parsedValue > max) return errorMessage
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
        val temperatureError: String?,
        val bloodPressureError: String?,
        val pulseError: String?,
        val weightError: String?
    )

    companion object {
        const val ALL_FIELDS_EMPTY_ERROR = "少なくとも1つの項目を入力してください"
        const val TEMPERATURE_RANGE_ERROR =
            "${AppConfig.HealthRecord.TEMPERATURE_MIN}〜${AppConfig.HealthRecord.TEMPERATURE_MAX}℃の範囲で入力してください"
        const val BLOOD_PRESSURE_RANGE_ERROR =
            "${AppConfig.HealthRecord.BLOOD_PRESSURE_MIN}〜${AppConfig.HealthRecord.BLOOD_PRESSURE_MAX}mmHgの範囲で入力してください"
        const val PULSE_RANGE_ERROR =
            "${AppConfig.HealthRecord.PULSE_MIN}〜${AppConfig.HealthRecord.PULSE_MAX}回/分の範囲で入力してください"
        const val WEIGHT_RANGE_ERROR =
            "${AppConfig.HealthRecord.WEIGHT_MIN}〜${AppConfig.HealthRecord.WEIGHT_MAX}kgの範囲で入力してください"
    }
}
