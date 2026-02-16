package com.carenote.app.ui.screens.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.ui.components.CareNoteCard
import com.carenote.app.ui.util.DateTimeFormatters

@Composable
fun CalendarEventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    onToggleCompleted: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    CareNoteCard(
        onClick = onClick,
        modifier = modifier
    ) {
        EventCardHeader(event = event, onToggleCompleted = onToggleCompleted)

        if (event.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = AppConfig.Calendar.DESCRIPTION_PREVIEW_MAX_LINES,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventCardHeader(
    event: CalendarEvent,
    onToggleCompleted: (CalendarEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EventTitleRow(event = event, modifier = Modifier.weight(1f))
        EventTimeAndCheckbox(event = event, onToggleCompleted = onToggleCompleted)
    }
}

@Composable
private fun EventTitleRow(
    event: CalendarEvent,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = event.type.icon(),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (event.completed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = event.title,
            style = MaterialTheme.typography.titleMedium.copy(
                textDecoration = if (event.completed) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            ),
            color = if (event.completed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = AppConfig.Calendar.TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EventTimeAndCheckbox(
    event: CalendarEvent,
    onToggleCompleted: (CalendarEvent) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val timeText = if (event.isAllDay) {
            stringResource(R.string.calendar_all_day_label)
        } else {
            event.startTime?.let { DateTimeFormatters.formatTime(it) } ?: ""
        }
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Checkbox(
            checked = event.completed,
            onCheckedChange = { onToggleCompleted(event) }
        )
    }
}

private fun CalendarEventType.icon(): ImageVector {
    return when (this) {
        CalendarEventType.HOSPITAL -> Icons.Filled.LocalHospital
        CalendarEventType.VISIT -> Icons.Filled.DirectionsCar
        CalendarEventType.DAYSERVICE -> Icons.Filled.Home
        CalendarEventType.OTHER -> Icons.Filled.Event
    }
}
