package com.carenote.app.ui.screens.medication

import androidx.lifecycle.ViewModel
import com.carenote.app.R
import androidx.lifecycle.viewModelScope
import com.carenote.app.config.AppConfig
import com.carenote.app.data.worker.MedicationReminderSchedulerInterface
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val reminderScheduler: MedicationReminderSchedulerInterface
) : ViewModel() {

    val snackbarController = SnackbarController()

    val uiState: StateFlow<UiState<List<Medication>>> =
        medicationRepository.getAllMedications()
            .map { medications -> UiState.Success(medications) as UiState<List<Medication>> }
            .catch { e ->
                Timber.w("Failed to observe medications: $e")
                emit(UiState.Error(DomainError.DatabaseError(e.message ?: "Unknown error")))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    private val _currentDate = MutableStateFlow(LocalDate.now())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayLogs: StateFlow<List<MedicationLog>> =
        _currentDate.flatMapLatest { date ->
            medicationLogRepository.getLogsForDate(date)
        }.catch { e ->
            Timber.w("Failed to observe today logs: $e")
            emit(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    fun refreshDateIfNeeded() {
        val today = LocalDate.now()
        if (_currentDate.value != today) {
            _currentDate.value = today
        }
    }

    fun recordMedication(
        medicationId: Long,
        status: MedicationLogStatus,
        timing: MedicationTiming? = null
    ) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val log = MedicationLog(
                medicationId = medicationId,
                status = status,
                scheduledAt = now,
                recordedAt = now,
                timing = timing
            )
            medicationLogRepository.insertLog(log)
                .onSuccess {
                    Timber.d("Medication log recorded: medicationId=$medicationId, status=$status")
                    if (status == MedicationLogStatus.TAKEN) {
                        reminderScheduler.cancelFollowUp(medicationId, timing)
                    }
                    snackbarController.showMessage(R.string.medication_log_recorded)
                }
                .onFailure { error ->
                    Timber.w("Failed to record medication log: $error")
                    snackbarController.showMessage(R.string.medication_log_failed)
                }
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(id)
                .onSuccess {
                    Timber.d("Medication deleted: id=$id")
                    reminderScheduler.cancelReminders(id)
                    snackbarController.showMessage(R.string.medication_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete medication: $error")
                    snackbarController.showMessage(R.string.medication_delete_failed)
                }
        }
    }

}
