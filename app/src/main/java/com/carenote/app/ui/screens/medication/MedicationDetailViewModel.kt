package com.carenote.app.ui.screens.medication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.MedicationReminderSchedulerInterface
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val reminderScheduler: MedicationReminderSchedulerInterface,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val medicationId: Long = checkNotNull(savedStateHandle["medicationId"])

    val snackbarController = SnackbarController()

    private val _deletedEvent = Channel<Boolean>(Channel.BUFFERED)
    val deletedEvent: Flow<Boolean> = _deletedEvent.receiveAsFlow()

    val medication: StateFlow<UiState<Medication>> =
        medicationRepository.getMedicationById(medicationId)
            .map { medication ->
                if (medication != null) {
                    UiState.Success(medication) as UiState<Medication>
                } else {
                    UiState.Error(
                        DomainError.NotFoundError(
                            "Medication not found: $medicationId"
                        )
                    )
                }
            }
            .catch { e ->
                Timber.w("Failed to observe medication detail: $e")
                emit(UiState.Error(DomainError.DatabaseError(e.message ?: "Unknown error")))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val logs: StateFlow<List<MedicationLog>> =
        medicationLogRepository.getLogsForMedication(medicationId)
            .catch { e ->
                Timber.w("Failed to observe medication logs: $e")
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = emptyList()
            )

    fun deleteMedication() {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medicationId)
                .onSuccess {
                    Timber.d("Medication deleted: id=$medicationId")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_MEDICATION_DELETED)
                    reminderScheduler.cancelReminders(medicationId)
                    _deletedEvent.send(true)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete medication: $error")
                    snackbarController.showMessage(R.string.medication_delete_failed)
                }
        }
    }

}
