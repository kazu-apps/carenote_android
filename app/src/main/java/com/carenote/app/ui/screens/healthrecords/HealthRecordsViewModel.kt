package com.carenote.app.ui.screens.healthrecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.HealthRecordCsvExporterInterface
import com.carenote.app.domain.repository.HealthRecordPdfExporterInterface
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.ui.util.RootDetectionChecker
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.ExportState
import com.carenote.app.ui.viewmodel.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HealthRecordsViewModel @Inject constructor(
    private val healthRecordRepository: HealthRecordRepository,
    private val csvExporter: HealthRecordCsvExporterInterface,
    private val pdfExporter: HealthRecordPdfExporterInterface,
    private val analyticsRepository: AnalyticsRepository,
    private val rootDetector: RootDetectionChecker
) : ViewModel() {

    val snackbarController = SnackbarController()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagedRecords: Flow<PagingData<HealthRecord>> =
        _searchQuery.debounce(AppConfig.UI.SEARCH_DEBOUNCE_MS)
            .flatMapLatest { query ->
                healthRecordRepository.getPagedRecords(query)
            }
            .cachedIn(viewModelScope)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val records: StateFlow<UiState<List<HealthRecord>>> =
        combine(
            _searchQuery.debounce(AppConfig.UI.SEARCH_DEBOUNCE_MS),
            _refreshTrigger
        ) { query, _ -> query }
            .flatMapLatest { query ->
                healthRecordRepository.getAllRecords()
                    .map { records ->
                        val sorted = records.sortedByDescending { it.recordedAt }
                        if (query.isBlank()) sorted
                        else sorted.filter { record ->
                            record.conditionNote.contains(query, ignoreCase = true)
                        }
                    }
            }
            .map { records ->
                // Required: stateIn needs explicit UiState<List<T>> type
                @Suppress("USELESS_CAST")
                UiState.Success(records) as UiState<List<HealthRecord>>
            }
            .catch { e ->
                Timber.w("Failed to observe health records: $e")
                emit(UiState.Error(DomainError.DatabaseError(e.message ?: "Unknown error")))
            }
            .onEach { _isRefreshing.value = false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    fun refresh() {
        _isRefreshing.value = true
        _refreshTrigger.value = System.nanoTime()
    }

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun exportCsv() {
        if (rootDetector.isDeviceRooted()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.security_root_export_blocked)
            }
            return
        }
        val currentRecords = getCurrentRecords()
        if (currentRecords.isEmpty()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.health_records_export_empty)
            }
            return
        }
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val uri = csvExporter.export(currentRecords)
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_HEALTH_RECORD_EXPORT_CSV)
                _exportState.value = ExportState.Success(uri, "text/csv")
            } catch (e: Exception) {
                Timber.w("CSV export failed: $e")
                _exportState.value = ExportState.Error(e.message ?: "Unknown error")
                snackbarController.showMessage(R.string.health_records_export_failed)
            }
        }
    }

    fun exportPdf() {
        if (rootDetector.isDeviceRooted()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.security_root_export_blocked)
            }
            return
        }
        val currentRecords = getCurrentRecords()
        if (currentRecords.isEmpty()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.health_records_export_empty)
            }
            return
        }
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val uri = pdfExporter.export(currentRecords)
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_HEALTH_RECORD_EXPORT_PDF)
                _exportState.value = ExportState.Success(uri, "application/pdf")
            } catch (e: Exception) {
                Timber.w("PDF export failed: $e")
                _exportState.value = ExportState.Error(e.message ?: "Unknown error")
                snackbarController.showMessage(R.string.health_records_export_failed)
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    private fun getCurrentRecords(): List<HealthRecord> {
        val state = records.value
        return if (state is UiState.Success) state.data else emptyList()
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            healthRecordRepository.deleteRecord(id)
                .onSuccess {
                    Timber.d("Health record deleted: id=$id")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_HEALTH_RECORD_DELETED)
                    snackbarController.showMessage(R.string.health_records_deleted)
                }
                .onFailure { error ->
                    Timber.w("Failed to delete health record: $error")
                    snackbarController.showMessage(R.string.health_records_delete_failed)
                }
        }
    }

}
