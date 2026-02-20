package com.carenote.app.ui.screens.home.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Note
import com.carenote.app.ui.screens.home.MedicationWithLog
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = onSeeAll) {
            Text(text = stringResource(R.string.home_see_all))
        }
    }
}

@Composable
fun MedicationSection(
    medications: List<MedicationWithLog>,
    onSeeAll: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppConfig.UI.CARD_ELEVATION_DP.dp)
    ) {
        Column(modifier = Modifier.padding(AppConfig.UI.CONTENT_SPACING_DP.dp)) {
            SectionHeader(
                title = stringResource(R.string.home_section_medication),
                onSeeAll = onSeeAll
            )
            if (medications.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_empty_medication),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                medications.forEach { medWithLog ->
                    MedicationItem(medWithLog, onClick = onItemClick)
                }
            }
        }
    }
}

@Composable
private fun MedicationItem(medWithLog: MedicationWithLog, onClick: (Long) -> Unit) {
    val takenCount = medWithLog.logs.size
    val totalTimings = medWithLog.medication.timings.size.coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(medWithLog.medication.id) }
            .padding(vertical = AppConfig.UI.SMALL_SPACING_DP.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = medWithLog.medication.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(AppConfig.UI.ITEM_SPACING_DP.dp))
        Text(
            text = "$takenCount/$totalTimings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TaskSection(
    tasks: List<CalendarEvent>,
    onSeeAll: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppConfig.UI.CARD_ELEVATION_DP.dp)
    ) {
        Column(modifier = Modifier.padding(AppConfig.UI.CONTENT_SPACING_DP.dp)) {
            SectionHeader(
                title = stringResource(R.string.home_section_tasks),
                onSeeAll = onSeeAll
            )
            if (tasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_empty_tasks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tasks.forEach { event -> TaskItem(event, onClick = onItemClick) }
            }
        }
    }
}

@Composable
private fun TaskItem(event: CalendarEvent, onClick: (Long) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(event.id) }
            .padding(vertical = AppConfig.UI.SMALL_SPACING_DP.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(AppConfig.UI.ITEM_SPACING_DP.dp))
        Text(
            text = dateFormatter.format(event.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HealthRecordSection(
    record: HealthRecord?,
    onSeeAll: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppConfig.UI.CARD_ELEVATION_DP.dp)
    ) {
        Column(modifier = Modifier.padding(AppConfig.UI.CONTENT_SPACING_DP.dp)) {
            SectionHeader(
                title = stringResource(R.string.home_section_health_record),
                onSeeAll = onSeeAll
            )
            if (record == null) {
                Text(
                    text = stringResource(R.string.home_empty_health_record),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                HealthRecordItem(record, onClick = onItemClick)
            }
        }
    }
}

@Composable
private fun HealthRecordItem(record: HealthRecord, onClick: (Long) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(record.id) }
    ) {
        Text(
            text = dateFormatter.format(record.recordedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppConfig.UI.CONTENT_SPACING_DP.dp)
        ) {
            record.temperature?.let { temp ->
                val tempLabel = stringResource(R.string.health_records_temperature)
                val tempUnit = stringResource(R.string.health_records_temperature_unit)
                Text(
                    text = "$tempLabel: ${"%.1f".format(temp)}$tempUnit",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            record.bloodPressureHigh?.let { high ->
                val low = record.bloodPressureLow ?: 0
                Text(
                    text = "${stringResource(R.string.health_records_blood_pressure)}: $high/$low",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            record.pulse?.let { pulse ->
                Text(
                    text = "${stringResource(R.string.health_records_pulse)}: $pulse",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun NoteSection(
    notes: List<Note>,
    onSeeAll: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppConfig.UI.CARD_ELEVATION_DP.dp)
    ) {
        Column(modifier = Modifier.padding(AppConfig.UI.CONTENT_SPACING_DP.dp)) {
            SectionHeader(
                title = stringResource(R.string.home_section_notes),
                onSeeAll = onSeeAll
            )
            if (notes.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_empty_notes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                notes.forEach { note -> NoteItem(note, onClick = onItemClick) }
            }
        }
    }
}

@Composable
private fun NoteItem(note: Note, onClick: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(note.id) }
            .padding(vertical = AppConfig.UI.SMALL_SPACING_DP.dp)
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = note.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = AppConfig.Note.CONTENT_PREVIEW_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CalendarSection(
    events: List<CalendarEvent>,
    onSeeAll: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppConfig.UI.CARD_ELEVATION_DP.dp)
    ) {
        Column(modifier = Modifier.padding(AppConfig.UI.CONTENT_SPACING_DP.dp)) {
            SectionHeader(
                title = stringResource(R.string.home_section_calendar),
                onSeeAll = onSeeAll
            )
            if (events.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_empty_calendar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                events.forEach { event -> CalendarEventItem(event, onItemClick = onItemClick) }
            }
        }
    }
}

@Composable
internal fun CalendarEventItem(event: CalendarEvent, onItemClick: (Long) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(event.id) }
            .padding(vertical = AppConfig.UI.SMALL_SPACING_DP.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = event.type.toIcon(),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(AppConfig.UI.SMALL_SPACING_DP.dp))
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        event.startTime?.let { startTime ->
            Spacer(modifier = Modifier.width(AppConfig.UI.ITEM_SPACING_DP.dp))
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val timeText = if (event.endTime != null) {
                "${startTime.format(timeFormatter)}\u2013${event.endTime.format(timeFormatter)}"
            } else {
                startTime.format(timeFormatter)
            }
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

internal fun CalendarEventType.toIcon(): ImageVector {
    return when (this) {
        CalendarEventType.HOSPITAL -> Icons.Filled.LocalHospital
        CalendarEventType.VISIT -> Icons.Filled.DirectionsCar
        CalendarEventType.DAYSERVICE -> Icons.Filled.Home
        CalendarEventType.TASK -> Icons.Filled.CheckCircle
        CalendarEventType.OTHER -> Icons.Filled.Event
    }
}
