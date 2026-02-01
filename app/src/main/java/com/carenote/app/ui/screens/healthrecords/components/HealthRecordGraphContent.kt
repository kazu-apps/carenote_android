package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.screens.healthrecords.HealthRecordGraphState

@Composable
fun HealthRecordGraphContent(
    state: HealthRecordGraphState,
    onDateRangeSelected: (com.carenote.app.ui.screens.healthrecords.GraphDateRange) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        LoadingIndicator(modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "date_range_selector") {
            GraphDateRangeSelector(
                selectedRange = state.dateRange,
                onRangeSelected = onDateRangeSelected
            )
        }

        if (!state.hasTemperatureData && !state.hasBloodPressureData) {
            item(key = "empty_state") {
                GraphEmptyState(
                    message = stringResource(R.string.health_records_graph_no_data)
                )
            }
        }

        if (state.hasTemperatureData) {
            item(key = "temperature_chart") {
                TemperatureChart(points = state.temperaturePoints)
            }
        } else if (state.hasBloodPressureData) {
            item(key = "no_temperature") {
                GraphEmptyState(
                    message = stringResource(R.string.health_records_graph_no_temperature)
                )
            }
        }

        if (state.hasBloodPressureData) {
            item(key = "bp_chart") {
                BloodPressureChart(
                    highPoints = state.bpHighPoints,
                    lowPoints = state.bpLowPoints
                )
            }
        } else if (state.hasTemperatureData) {
            item(key = "no_bp") {
                GraphEmptyState(
                    message = stringResource(R.string.health_records_graph_no_bp)
                )
            }
        }
    }
}
