package com.carenote.app.ui.screens.calendar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import java.time.LocalTime

@Composable
fun CalendarEventReminderSection(
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
