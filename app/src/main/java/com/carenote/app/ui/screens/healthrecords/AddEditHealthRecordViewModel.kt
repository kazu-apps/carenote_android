package com.carenote.app.ui.screens.healthrecords

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.ImageCompressorInterface
import com.carenote.app.domain.util.Clock
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.model.Photo
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.PhotoRepository
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.viewmodel.PhotoManager
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
    val recordedAt: LocalDateTime,
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
    private val healthRecordRepository: HealthRecordRepository,
    private val photoRepository: PhotoRepository,
    private val imageCompressor: ImageCompressorInterface,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock
) : ViewModel() {

    private val recordId: Long? = savedStateHandle.get<Long>("recordId")

    private val _formState = MutableStateFlow(
        AddEditHealthRecordFormState(recordedAt = clock.now(), isEditMode = recordId != null)
    )
    val formState: StateFlow<AddEditHealthRecordFormState> = _formState.asStateFlow()

    private val _savedEvent = Channel<Boolean>(Channel.BUFFERED)
    val savedEvent: Flow<Boolean> = _savedEvent.receiveAsFlow()

    val snackbarController = SnackbarController()

    private var originalRecord: HealthRecord? = null
    private var _initialFormState: AddEditHealthRecordFormState? = null

    val photoManager = PhotoManager(
        parentType = "health_record",
        parentId = recordId ?: 0L,
        photoRepository = photoRepository,
        imageCompressor = imageCompressor,
        scope = viewModelScope,
        snackbarController = snackbarController,
        clock = clock
    )

    val photos: StateFlow<List<Photo>> get() = photoManager.photos

    val isDirty: Boolean
        get() {
            val initial = _initialFormState ?: return false
            val current = _formState.value.copy(
                temperatureError = null,
                bloodPressureError = null,
                pulseError = null,
                weightError = null,
                conditionNoteError = null,
                generalError = null,
                isSaving = false,
                isEditMode = false
            )
            val baseline = initial.copy(
                temperatureError = null,
                bloodPressureError = null,
                pulseError = null,
                weightError = null,
                conditionNoteError = null,
                generalError = null,
                isSaving = false,
                isEditMode = false
            )
            if (current != baseline) return true
            return photoManager.hasChanges
        }

    init {
        if (recordId != null) {
            loadRecord(recordId)
        } else {
            _initialFormState = _formState.value
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
                _initialFormState = _formState.value
                photoManager.loadPhotos()
            }
        }
    }

    fun addPhotos(uris: List<Uri>) = photoManager.addPhotos(uris)

    fun removePhoto(photo: Photo) = photoManager.removePhoto(photo)

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
        val parsed = HealthMetricsParser.parseFormFields(current)

        if (!parsed.hasAnyField) {
            _formState.value = current.copy(
                generalError = UiText.Resource(R.string.health_records_all_empty_error)
            )
            return
        }

        val errors = HealthMetricsParser.validateFields(current, parsed)
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
        parsed: HealthMetricsParser.ParsedFields
    ) {
        val now = clock.now()
        val original = originalRecord
        if (recordId != null && original != null) {
            updateExistingRecord(original, current, parsed, now)
        } else {
            insertNewRecord(current, parsed, now)
        }
    }

    private suspend fun updateExistingRecord(
        original: HealthRecord,
        current: AddEditHealthRecordFormState,
        parsed: HealthMetricsParser.ParsedFields,
        now: LocalDateTime
    ) {
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
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_HEALTH_RECORD_UPDATED)
                _savedEvent.send(true)
            }
            .onFailure { error ->
                Timber.w("Failed to update health record: $error")
                _formState.value = _formState.value.copy(isSaving = false)
                snackbarController.showMessage(R.string.health_records_save_failed)
            }
    }

    private suspend fun insertNewRecord(
        current: AddEditHealthRecordFormState,
        parsed: HealthMetricsParser.ParsedFields,
        now: LocalDateTime
    ) {
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
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_HEALTH_RECORD_CREATED)
                photoManager.updateParentId(id)
                _savedEvent.send(true)
            }
            .onFailure { error ->
                Timber.w("Failed to save health record: $error")
                _formState.value = _formState.value.copy(isSaving = false)
                snackbarController.showMessage(R.string.health_records_save_failed)
            }
    }

}
