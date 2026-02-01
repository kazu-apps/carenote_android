package com.carenote.app.ui.screens.medication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository
) : ViewModel() {

    private val medicationId: Long = checkNotNull(savedStateHandle["medicationId"])

    val snackbarController = SnackbarController()

    private val _deletedEvent = MutableSharedFlow<Boolean>(replay = 1)
    val deletedEvent: SharedFlow<Boolean> = _deletedEvent.asSharedFlow()

    val medication: StateFlow<UiState<Medication>> =
        medicationRepository.getMedicationById(medicationId)
            .map { medication ->
                if (medication != null) {
                    UiState.Success(medication) as UiState<Medication>
                } else {
                    UiState.Error(
                        com.carenote.app.domain.common.DomainError.NotFoundError(
                            "Medication not found: $medicationId"
                        )
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val logs: StateFlow<List<MedicationLog>> =
        medicationLogRepository.getLogsForMedication(medicationId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = emptyList()
            )

    fun deleteMedication() {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medicationId)
                .onSuccess {
                    Timber.d("Medication deleted: id=$medicationId")
                    _deletedEvent.emit(true)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete medication: $error")
                    snackbarController.showMessage(SNACKBAR_DELETE_FAILED)
                }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        const val SNACKBAR_DELETE_FAILED = "削除に失敗しました"
    }
}
