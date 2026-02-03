package com.carenote.app.ui.screens.healthrecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
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
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            healthRecordRepository.deleteRecord(id)
                .onSuccess {
                    Timber.d("Health record deleted: id=$id")
                    snackbarController.showMessage(R.string.health_records_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete health record: $error")
                    snackbarController.showMessage(R.string.health_records_delete_failed)
                }
        }
    }

}
