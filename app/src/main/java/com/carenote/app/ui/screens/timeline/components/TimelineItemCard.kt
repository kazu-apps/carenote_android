package com.carenote.app.ui.screens.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.ui.util.DateTimeFormatters

@Composable
fun TimelineItemCard(
    item: TimelineItem,
    modifier: Modifier = Modifier,
    onToggleCompleted: ((Long, Boolean) -> Unit)? = null
) {
    val (icon, tint, label) = resolveItemStyle(item)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppConfig.UI.CARD_ELEVATION_DP.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConfig.UI.CONTENT_SPACING_DP.dp),
            horizontalArrangement = Arrangement.spacedBy(AppConfig.UI.ITEM_SPACING_DP.dp),
            verticalAlignment = Alignment.Top
        ) {
            TimelineItemLeadingIcon(
                item = item,
                icon = icon,
                label = label,
                tint = tint,
                onToggleCompleted = onToggleCompleted
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppConfig.UI.SMALL_SPACING_DP.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint
                )

                when (item) {
                    is TimelineItem.MedicationLogItem -> MedicationLogContent(item)
                    is TimelineItem.CalendarEventItem -> CalendarEventContent(item)
                    is TimelineItem.HealthRecordItem -> HealthRecordContent(item)
                    is TimelineItem.NoteItem -> NoteContent(item)
                }
            }

            Text(
                text = DateTimeFormatters.formatTime(item.timestamp.toLocalTime()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimelineItemLeadingIcon(
    item: TimelineItem,
    icon: ImageVector,
    label: String,
    tint: Color,
    onToggleCompleted: ((Long, Boolean) -> Unit)?
) {
    if (item is TimelineItem.CalendarEventItem && item.event.isTask && onToggleCompleted != null) {
        Checkbox(
            checked = item.event.completed,
            onCheckedChange = { checked ->
                onToggleCompleted(item.event.id, checked)
            },
            modifier = Modifier.size(AppConfig.UI.ICON_SIZE_MEDIUM_DP.dp)
        )
    } else {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(AppConfig.UI.ICON_SIZE_MEDIUM_DP.dp)
        )
    }
}

@Composable
private fun MedicationLogContent(item: TimelineItem.MedicationLogItem) {
    val name = item.medicationName.ifBlank {
        stringResource(R.string.timeline_unknown_medication)
    }
    Text(
        text = name,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    val timingText = item.log.timing?.let { timing ->
        when (timing) {
            MedicationTiming.MORNING -> stringResource(R.string.medication_morning)
            MedicationTiming.NOON -> stringResource(R.string.medication_noon)
            MedicationTiming.EVENING -> stringResource(R.string.medication_evening)
        }
    }
    val statusText = when (item.log.status) {
        MedicationLogStatus.TAKEN -> stringResource(R.string.timeline_medication_taken)
        MedicationLogStatus.SKIPPED -> stringResource(R.string.timeline_medication_skipped)
        MedicationLogStatus.POSTPONED -> stringResource(R.string.timeline_medication_postponed)
    }
    val displayText = if (timingText != null) {
        "$timingText / $statusText"
    } else {
        statusText
    }
    Text(
        text = displayText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CalendarEventContent(item: TimelineItem.CalendarEventItem) {
    Text(
        text = item.event.title,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (item.event.isTask && item.event.completed) {
            TextDecoration.LineThrough
        } else {
            TextDecoration.None
        }
    )
    if (item.event.isTask && item.event.description.isNotBlank()) {
        Text(
            text = item.event.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = AppConfig.Timeline.PREVIEW_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )
    } else if (item.event.startTime != null) {
        val timeRange = buildString {
            append(DateTimeFormatters.formatTime(item.event.startTime))
            item.event.endTime?.let { end ->
                append(" - ")
                append(DateTimeFormatters.formatTime(end))
            }
        }
        Text(
            text = timeRange,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HealthRecordContent(item: TimelineItem.HealthRecordItem) {
    val record = item.record
    val parts = mutableListOf<String>()
    record.temperature?.let { parts.add("${it}\u2103") }
    if (record.bloodPressureHigh != null && record.bloodPressureLow != null) {
        parts.add("${record.bloodPressureHigh}/${record.bloodPressureLow}mmHg")
    }
    record.pulse?.let { parts.add("${it}bpm") }
    record.weight?.let { parts.add("${it}kg") }

    if (parts.isNotEmpty()) {
        Text(
            text = parts.joinToString(" / "),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (record.conditionNote.isNotBlank()) {
        Text(
            text = record.conditionNote,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = AppConfig.Timeline.PREVIEW_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NoteContent(item: TimelineItem.NoteItem) {
    Text(
        text = item.note.title,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    if (item.note.content.isNotBlank()) {
        Text(
            text = item.note.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = AppConfig.Timeline.PREVIEW_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class ItemStyle(
    val icon: ImageVector,
    val tint: Color,
    val label: String
)

@Composable
private fun resolveItemStyle(item: TimelineItem): ItemStyle {
    return when (item) {
        is TimelineItem.MedicationLogItem -> ItemStyle(
            icon = Icons.Filled.Medication,
            tint = Color(0xFF2E7D32),
            label = stringResource(R.string.timeline_medication_log)
        )
        is TimelineItem.CalendarEventItem -> if (item.event.isTask) {
            ItemStyle(
                icon = Icons.Filled.CheckCircle,
                tint = Color(0xFFE65100),
                label = stringResource(R.string.timeline_task)
            )
        } else {
            ItemStyle(
                icon = Icons.Filled.CalendarMonth,
                tint = Color(0xFF1565C0),
                label = stringResource(R.string.timeline_calendar_event)
            )
        }
        is TimelineItem.HealthRecordItem -> ItemStyle(
            icon = Icons.Filled.MonitorHeart,
            tint = Color(0xFFC62828),
            label = stringResource(R.string.timeline_health_record)
        )
        is TimelineItem.NoteItem -> ItemStyle(
            icon = Icons.AutoMirrored.Filled.StickyNote2,
            tint = Color(0xFF6A1B9A),
            label = stringResource(R.string.timeline_note)
        )
    }
}
