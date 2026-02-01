package com.carenote.app.ui.screens.healthrecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.repository.HealthRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

enum class GraphDateRange(val days: Long) {
    SEVEN_DAYS(AppConfig.Graph.DATE_RANGE_SEVEN_DAYS),
    THIRTY_DAYS(AppConfig.Graph.DATE_RANGE_THIRTY_DAYS)
}

data class GraphDataPoint(
    val date: LocalDate,
    val value: Double
)

data class HealthRecordGraphState(
    val dateRange: GraphDateRange = GraphDateRange.SEVEN_DAYS,
    val temperaturePoints: List<GraphDataPoint> = emptyList(),
    val bpHighPoints: List<GraphDataPoint> = emptyList(),
    val bpLowPoints: List<GraphDataPoint> = emptyList(),
    val isLoading: Boolean = true,
    val hasTemperatureData: Boolean = false,
    val hasBloodPressureData: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HealthRecordGraphViewModel @Inject constructor(
    private val healthRecordRepository: HealthRecordRepository
) : ViewModel() {

    private val _dateRange = MutableStateFlow(GraphDateRange.SEVEN_DAYS)

    val graphState: StateFlow<HealthRecordGraphState> =
        _dateRange.flatMapLatest { range ->
            val now = LocalDateTime.now()
            val start = now.minusDays(range.days).with(LocalTime.MIN)
            healthRecordRepository.getRecordsByDateRange(start, now)
                .map { records -> mapToGraphState(records, range) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HealthRecordGraphState()
        )

    fun setDateRange(range: GraphDateRange) {
        _dateRange.value = range
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun mapToGraphState(
            records: List<HealthRecord>,
            dateRange: GraphDateRange
        ): HealthRecordGraphState {
            val sorted = records.sortedBy { it.recordedAt }

            val temperaturePoints = sorted
                .filter { it.temperature != null }
                .map { GraphDataPoint(it.recordedAt.toLocalDate(), it.temperature!!) }

            val bpHighPoints = sorted
                .filter { it.bloodPressureHigh != null && it.bloodPressureLow != null }
                .map { GraphDataPoint(it.recordedAt.toLocalDate(), it.bloodPressureHigh!!.toDouble()) }

            val bpLowPoints = sorted
                .filter { it.bloodPressureHigh != null && it.bloodPressureLow != null }
                .map { GraphDataPoint(it.recordedAt.toLocalDate(), it.bloodPressureLow!!.toDouble()) }

            return HealthRecordGraphState(
                dateRange = dateRange,
                temperaturePoints = temperaturePoints,
                bpHighPoints = bpHighPoints,
                bpLowPoints = bpLowPoints,
                isLoading = false,
                hasTemperatureData = temperaturePoints.isNotEmpty(),
                hasBloodPressureData = bpHighPoints.isNotEmpty()
            )
        }
    }
}
