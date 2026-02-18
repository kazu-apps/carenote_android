package com.carenote.app.ui.screens.calendar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.domain.model.TaskPriority
import java.time.LocalTime

@Composable
fun TaskFields(
    priority: TaskPriority,
    onPriorityChange: (TaskPriority) -> Unit,
    reminderEnabled: Boolean,
    onToggleReminder: () -> Unit,
    reminderTime: LocalTime?,
    onReminderTimeChange: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PrioritySelector(
            selectedPriority = priority,
            onPrioritySelected = onPriorityChange
        )
        ReminderSection(
            enabled = reminderEnabled,
            time = reminderTime,
            onToggle = onToggleReminder,
            onClickTime = onReminderTimeChange
        )
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.tasks_task_priority),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedPriority == TaskPriority.LOW,
                onClick = { onPrioritySelected(TaskPriority.LOW) },
                label = { Text(text = stringResource(R.string.tasks_task_priority_low)) }
            )
            FilterChip(
                selected = selectedPriority == TaskPriority.MEDIUM,
                onClick = { onPrioritySelected(TaskPriority.MEDIUM) },
                label = { Text(text = stringResource(R.string.tasks_task_priority_medium)) }
            )
            FilterChip(
                selected = selectedPriority == TaskPriority.HIGH,
                onClick = { onPrioritySelected(TaskPriority.HIGH) },
                label = { Text(text = stringResource(R.string.tasks_task_priority_high)) }
            )
        }
    }
}

@Composable
private fun ReminderSection(
    enabled: Boolean,
    time: LocalTime?,
    onToggle: () -> Unit,
    onClickTime: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.tasks_reminder_enabled),
                style = MaterialTheme.typography.titleMedium
            )
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() }
            )
        }
        if (enabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.tasks_reminder_time),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = time?.let {
                        String.format("%02d:%02d", it.hour, it.minute)
                    } ?: stringResource(R.string.tasks_reminder_time_not_set),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(
                            onClick = onClickTime,
                            onClickLabel = stringResource(R.string.tasks_select_reminder_time),
                            role = Role.Button
                        )
                        .padding(12.dp)
                )
            }
        }
    }
}
