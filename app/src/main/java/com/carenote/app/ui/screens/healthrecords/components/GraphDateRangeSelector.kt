package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.ui.screens.healthrecords.GraphDateRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphDateRangeSelector(
    selectedRange: GraphDateRange,
    onRangeSelected: (GraphDateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedRange == GraphDateRange.SEVEN_DAYS,
            onClick = { onRangeSelected(GraphDateRange.SEVEN_DAYS) },
            label = { Text(stringResource(R.string.health_records_graph_7days)) }
        )
        FilterChip(
            selected = selectedRange == GraphDateRange.THIRTY_DAYS,
            onClick = { onRangeSelected(GraphDateRange.THIRTY_DAYS) },
            label = { Text(stringResource(R.string.health_records_graph_30days)) }
        )
    }
}
