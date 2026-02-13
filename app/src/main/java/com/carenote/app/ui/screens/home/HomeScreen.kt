package com.carenote.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.carenote.app.domain.model.CalendarEventType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import androidx.compose.ui.graphics.vector.ImageVector
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.Task
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.CareNoteTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToMedication: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHealthRecords: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.a11y_navigate_to_search)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            onNavigateToMedication = {
                viewModel.logSeeAllClicked("medication")
                onNavigateToMedication()
            },
            onNavigateToCalendar = {
                viewModel.logSeeAllClicked("calendar")
                onNavigateToCalendar()
            },
            onNavigateToTasks = {
                viewModel.logSeeAllClicked("tasks")
                onNavigateToTasks()
            },
            onNavigateToHealthRecords = {
                viewModel.logSeeAllClicked("health_records")
                onNavigateToHealthRecords()
            },
            onNavigateToNotes = {
                viewModel.logSeeAllClicked("notes")
                onNavigateToNotes()
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToMedication: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHealthRecords: () -> Unit,
    onNavigateToNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        LoadingIndicator(modifier = modifier)
        return
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(AppConfig.UI.CONTENT_SPACING_DP.dp)
        ) {
            item { Spacer(modifier = Modifier.height(AppConfig.UI.SMALL_SPACING_DP.dp)) }

            item {
                MedicationSection(
                    medications = uiState.todayMedications,
                    onSeeAll = onNavigateToMedication
                )
            }

            item {
                TaskSection(
                    tasks = uiState.upcomingTasks,
                    onSeeAll = onNavigateToTasks
                )
            }

            item {
                HealthRecordSection(
                    record = uiState.latestHealthRecord,
                    onSeeAll = onNavigateToHealthRecords
                )
            }

            item {
                NoteSection(
                    notes = uiState.recentNotes,
                    onSeeAll = onNavigateToNotes
                )
            }

            item {
                CalendarSection(
                    events = uiState.todayEvents,
                    onSeeAll = onNavigateToCalendar
                )
            }

            item { Spacer(modifier = Modifier.height(AppConfig.UI.LIST_BOTTOM_PADDING_DP.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(
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
private fun MedicationSection(
    medications: List<MedicationWithLog>,
    onSeeAll: () -> Unit
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
                    MedicationItem(medWithLog)
                }
            }
        }
    }
}

@Composable
private fun MedicationItem(medWithLog: MedicationWithLog) {
    val takenCount = medWithLog.logs.size
    val totalTimings = medWithLog.medication.timings.size.coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
private fun TaskSection(
    tasks: List<Task>,
    onSeeAll: () -> Unit
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
                tasks.forEach { task -> TaskItem(task) }
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppConfig.UI.SMALL_SPACING_DP.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        task.dueDate?.let { date ->
            Spacer(modifier = Modifier.width(AppConfig.UI.ITEM_SPACING_DP.dp))
            Text(
                text = dateFormatter.format(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthRecordSection(
    record: HealthRecord?,
    onSeeAll: () -> Unit
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
                HealthRecordItem(record)
            }
        }
    }
}

@Composable
private fun HealthRecordItem(record: HealthRecord) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    Column(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    text = "${stringResource(R.string.health_records_temperature)}: ${"%.1f".format(temp)}${stringResource(R.string.health_records_temperature_unit)}",
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
private fun NoteSection(
    notes: List<Note>,
    onSeeAll: () -> Unit
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
                notes.forEach { note -> NoteItem(note) }
            }
        }
    }
}

@Composable
private fun NoteItem(note: Note) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
private fun CalendarSection(
    events: List<CalendarEvent>,
    onSeeAll: () -> Unit
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
                events.forEach { event -> CalendarEventItem(event) }
            }
        }
    }
}

@Composable
internal fun CalendarEventItem(event: CalendarEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

private fun CalendarEventType.toIcon(): ImageVector {
    return when (this) {
        CalendarEventType.HOSPITAL -> Icons.Filled.LocalHospital
        CalendarEventType.VISIT -> Icons.Filled.DirectionsCar
        CalendarEventType.DAYSERVICE -> Icons.Filled.Home
        CalendarEventType.OTHER -> Icons.Filled.Event
    }
}

@LightDarkPreview
@Composable
private fun HomeContentPreview() {
    CareNoteTheme {
        HomeContent(
            uiState = PreviewData.homeUiState,
            isRefreshing = false,
            onRefresh = {},
            onNavigateToMedication = {},
            onNavigateToCalendar = {},
            onNavigateToTasks = {},
            onNavigateToHealthRecords = {},
            onNavigateToNotes = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun CalendarEventItemPreview() {
    CareNoteTheme {
        CalendarEventItem(event = PreviewData.calendarEvent1)
    }
}
