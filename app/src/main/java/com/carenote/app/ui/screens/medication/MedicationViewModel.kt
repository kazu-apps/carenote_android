package com.carenote.app.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val medicationLogRepository: MedicationLogRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    val uiState: StateFlow<UiState<List<Medication>>> =
        medicationRepository.getAllMedications()
            .map { medications -> UiState.Success(medications) as UiState<List<Medication>> }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val todayLogs: StateFlow<List<MedicationLog>> =
        medicationLogRepository.getLogsForDate(LocalDate.now())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = emptyList()
            )

    fun recordMedication(medicationId: Long, status: MedicationLogStatus) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val log = MedicationLog(
                medicationId = medicationId,
                status = status,
                scheduledAt = now,
                recordedAt = now
            )
            medicationLogRepository.insertLog(log)
                .onSuccess {
                    Timber.d("Medication log recorded: medicationId=$medicationId, status=$status")
                    snackbarController.showMessage(SNACKBAR_LOG_RECORDED)
                }
                .onFailure { error ->
                    Timber.w("Failed to record medication log: $error")
                }
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(id)
                .onSuccess {
                    Timber.d("Medication deleted: id=$id")
                    snackbarController.showMessage(SNACKBAR_DELETED)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete medication: $error")
                }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        const val SNACKBAR_LOG_RECORDED = "服薬を記録しました"
        const val SNACKBAR_DELETED = "薬を削除しました"
    }
}
