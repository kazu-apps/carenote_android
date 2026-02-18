package com.carenote.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.HealthRecordRepository
import com.carenote.app.domain.repository.MedicationLogRepository
import com.carenote.app.domain.repository.MedicationRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.util.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

data class HomeUiState(
    val todayMedications: List<MedicationWithLog> = emptyList(),
    val upcomingTasks: List<CalendarEvent> = emptyList(),
    val latestHealthRecord: HealthRecord? = null,
    val recentNotes: List<Note> = emptyList(),
    val todayEvents: List<CalendarEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class MedicationWithLog(
    val medication: Medication,
    val logs: List<MedicationLog>
)

private data class FiveCombined(
    val medications: List<Medication>,
    val todayLogs: List<MedicationLog>,
    val incompleteTasks: List<CalendarEvent>,
    val allRecords: List<HealthRecord>,
    val allNotes: List<Note>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val healthRecordRepository: HealthRecordRepository,
    private val noteRepository: NoteRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val clock: Clock
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = _refreshTrigger
        .flatMapLatest {
            val today = clock.today()
            combine(
                medicationRepository.getAllMedications(),
                medicationLogRepository.getLogsForDate(today),
                calendarEventRepository.getIncompleteTaskEvents(),
                healthRecordRepository.getAllRecords(),
                noteRepository.getAllNotes()
            ) { medications, todayLogs, incompleteTasks, allRecords, allNotes ->
                FiveCombined(medications, todayLogs, incompleteTasks, allRecords, allNotes)
            }.combine(calendarEventRepository.getEventsByDate(today)) { five, todayEvents ->
                buildHomeUiState(
                    medications = five.medications,
                    todayLogs = five.todayLogs,
                    incompleteTasks = five.incompleteTasks,
                    allRecords = five.allRecords,
                    allNotes = five.allNotes,
                    todayEvents = todayEvents
                )
            }
        }
        .catch { e ->
            Timber.w("Failed to load home data: $e")
            emit(HomeUiState(isLoading = false, error = e.message))
        }
        .onEach { _isRefreshing.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = HomeUiState()
        )

    fun refresh() {
        _isRefreshing.value = true
        _refreshTrigger.value = System.nanoTime()
    }

    fun logItemClicked(section: String, itemId: Long) {
        analyticsRepository.logEvent(
            AppConfig.Analytics.EVENT_HOME_ITEM_CLICKED,
            mapOf(
                AppConfig.Analytics.PARAM_SECTION to section,
                AppConfig.Analytics.PARAM_ITEM_ID to itemId.toString()
            )
        )
    }

    fun logSeeAllClicked(section: String) {
        analyticsRepository.logEvent(
            AppConfig.Analytics.EVENT_HOME_SEE_ALL_CLICKED,
            mapOf(AppConfig.Analytics.PARAM_SECTION to section)
        )
    }

    private fun buildHomeUiState(
        medications: List<Medication>,
        todayLogs: List<MedicationLog>,
        incompleteTasks: List<CalendarEvent>,
        allRecords: List<HealthRecord>,
        allNotes: List<Note>,
        todayEvents: List<CalendarEvent>
    ): HomeUiState {
        val medicationsWithLogs = medications
            .take(AppConfig.Home.MAX_SECTION_ITEMS)
            .map { med ->
                MedicationWithLog(
                    medication = med,
                    logs = todayLogs.filter { it.medicationId == med.id }
                )
            }

        val upcomingTasks = incompleteTasks
            .sortedBy { it.date }
            .take(AppConfig.Home.MAX_SECTION_ITEMS)

        val latestRecord = allRecords
            .sortedByDescending { it.recordedAt }
            .firstOrNull()

        val recentNotes = allNotes
            .sortedByDescending { it.updatedAt }
            .take(AppConfig.Home.MAX_SECTION_ITEMS)

        val sortedEvents = todayEvents
            .sortedBy { it.startTime }
            .take(AppConfig.Home.MAX_SECTION_ITEMS)

        return HomeUiState(
            todayMedications = medicationsWithLogs,
            upcomingTasks = upcomingTasks,
            latestHealthRecord = latestRecord,
            recentNotes = recentNotes,
            todayEvents = sortedEvents,
            isLoading = false
        )
    }
}
