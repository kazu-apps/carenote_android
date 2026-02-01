package com.carenote.app.ui.screens.tasks.components

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
import com.carenote.app.ui.screens.tasks.TaskFilterMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilterChips(
    selectedFilter: TaskFilterMode,
    onFilterSelected: (TaskFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == TaskFilterMode.ALL,
            onClick = { onFilterSelected(TaskFilterMode.ALL) },
            label = { Text(text = stringResource(R.string.tasks_filter_all)) }
        )
        FilterChip(
            selected = selectedFilter == TaskFilterMode.INCOMPLETE,
            onClick = { onFilterSelected(TaskFilterMode.INCOMPLETE) },
            label = { Text(text = stringResource(R.string.tasks_filter_incomplete)) }
        )
        FilterChip(
            selected = selectedFilter == TaskFilterMode.COMPLETED,
            onClick = { onFilterSelected(TaskFilterMode.COMPLETED) },
            label = { Text(text = stringResource(R.string.tasks_filter_completed)) }
        )
    }
}
