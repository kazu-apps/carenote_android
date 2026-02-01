package com.carenote.app.ui.screens.healthrecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HealthRecordsViewModel @Inject constructor(
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    val records: StateFlow<UiState<List<HealthRecord>>> =
        healthRecordRepository.getAllRecords()
            .map { records ->
                val sorted = records.sortedByDescending { it.recordedAt }
                @Suppress("USELESS_CAST")
                UiState.Success(sorted) as UiState<List<HealthRecord>>
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            healthRecordRepository.deleteRecord(id)
                .onSuccess {
                    Timber.d("Health record deleted: id=$id")
                    snackbarController.showMessage(SNACKBAR_DELETED)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete health record: $error")
                    snackbarController.showMessage(SNACKBAR_DELETE_FAILED)
                }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        const val SNACKBAR_DELETED = "記録を削除しました"
        const val SNACKBAR_DELETE_FAILED = "削除に失敗しました"
    }
}
