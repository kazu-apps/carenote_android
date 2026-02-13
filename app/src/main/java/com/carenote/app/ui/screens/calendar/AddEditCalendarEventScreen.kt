package com.carenote.app.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.RecurrenceFrequency
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.components.CareNoteAddEditScaffold
import com.carenote.app.ui.components.CareNoteDatePickerDialog
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.components.CareNoteTimePickerDialog
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCalendarEventScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditCalendarEventViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { saved ->
            if (saved) {
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId -> context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val title = if (formState.isEditMode) {
        stringResource(R.string.calendar_edit_event)
    } else {
        stringResource(R.string.calendar_add_event)
    }

    CareNoteAddEditScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        isDirty = viewModel.isDirty,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CareNoteTextField(
                value = formState.title,
                onValueChange = viewModel::updateTitle,
                label = stringResource(R.string.calendar_event_title),
                placeholder = stringResource(R.string.calendar_event_title_placeholder),
                errorMessage = formState.titleError,
                singleLine = true
            )

            CareNoteTextField(
                value = formState.description,
                onValueChange = viewModel::updateDescription,
                label = stringResource(R.string.calendar_event_description),
                placeholder = stringResource(R.string.calendar_event_description_placeholder),
                errorMessage = formState.descriptionError,
                singleLine = false,
                maxLines = AppConfig.Calendar.DESCRIPTION_PREVIEW_MAX_LINES + 2
            )

            // Event type selector
            Text(
                text = stringResource(R.string.calendar_event_type),
                style = MaterialTheme.typography.titleMedium
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarEventType.entries.forEach { type ->
                    FilterChip(
                        selected = formState.type == type,
                        onClick = { viewModel.updateType(type) },
                        label = {
                            Text(text = stringResource(type.labelResId()))
                        }
                    )
                }
            }

            DateSelector(
                date = formState.date,
                onClick = { showDatePicker = true }
            )

            AllDayToggle(
                isAllDay = formState.isAllDay,
                onToggle = viewModel::toggleAllDay
            )

            if (!formState.isAllDay) {
                TimeSelector(
                    label = stringResource(R.string.calendar_event_start_time),
                    time = formState.startTime,
                    onClick = { showStartTimePicker = true }
                )

                TimeSelector(
                    label = stringResource(R.string.calendar_event_end_time),
                    time = formState.endTime,
                    onClick = { showEndTimePicker = true }
                )
            }

            RecurrenceSection(
                frequency = formState.recurrenceFrequency,
                interval = formState.recurrenceInterval,
                intervalError = formState.recurrenceIntervalError,
                onFrequencySelected = viewModel::updateRecurrenceFrequency,
                onIntervalChanged = viewModel::updateRecurrenceInterval
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    shape = ButtonShape
                ) {
                    Text(text = stringResource(R.string.common_cancel))
                }
                Button(
                    onClick = viewModel::saveEvent,
                    modifier = Modifier.weight(1f),
                    shape = ButtonShape,
                    enabled = !formState.isSaving
                ) {
                    if (formState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = stringResource(R.string.common_save))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        CareNoteDatePickerDialog(
            initialDate = formState.date,
            onDateSelected = { date ->
                viewModel.updateDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showStartTimePicker) {
        CareNoteTimePickerDialog(
            initialTime = formState.startTime ?: LocalTime.of(
                AppConfig.Calendar.DEFAULT_START_HOUR,
                AppConfig.Calendar.DEFAULT_START_MINUTE
            ),
            onTimeSelected = { time ->
                viewModel.updateStartTime(time)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        CareNoteTimePickerDialog(
            initialTime = formState.endTime ?: LocalTime.of(
                AppConfig.Calendar.DEFAULT_END_HOUR,
                AppConfig.Calendar.DEFAULT_END_MINUTE
            ),
            onTimeSelected = { time ->
                viewModel.updateEndTime(time)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@Composable
private fun DateSelector(
    date: LocalDate,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.calendar_event_date),
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = onClick) {
            Text(
                text = DateTimeFormatters.formatDate(date),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun AllDayToggle(
    isAllDay: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.calendar_event_all_day),
            style = MaterialTheme.typography.titleMedium
        )
        Switch(
            checked = isAllDay,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun TimeSelector(
    label: String,
    time: LocalTime?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = onClick) {
            Text(
                text = time?.let { DateTimeFormatters.formatTime(it) } ?: "--:--",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun CalendarEventType.labelResId(): Int {
    return when (this) {
        CalendarEventType.HOSPITAL -> R.string.calendar_event_type_hospital
        CalendarEventType.VISIT -> R.string.calendar_event_type_visit
        CalendarEventType.DAYSERVICE -> R.string.calendar_event_type_dayservice
        CalendarEventType.OTHER -> R.string.calendar_event_type_other
    }
}

@Composable
private fun RecurrenceSection(
    frequency: RecurrenceFrequency,
    interval: Int,
    intervalError: com.carenote.app.ui.common.UiText?,
    onFrequencySelected: (RecurrenceFrequency) -> Unit,
    onIntervalChanged: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.tasks_recurrence),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = frequency == RecurrenceFrequency.NONE,
                onClick = { onFrequencySelected(RecurrenceFrequency.NONE) },
                label = { Text(text = stringResource(R.string.tasks_recurrence_none)) }
            )
            FilterChip(
                selected = frequency == RecurrenceFrequency.DAILY,
                onClick = { onFrequencySelected(RecurrenceFrequency.DAILY) },
                label = { Text(text = stringResource(R.string.tasks_recurrence_daily)) }
            )
            FilterChip(
                selected = frequency == RecurrenceFrequency.WEEKLY,
                onClick = { onFrequencySelected(RecurrenceFrequency.WEEKLY) },
                label = { Text(text = stringResource(R.string.tasks_recurrence_weekly)) }
            )
            FilterChip(
                selected = frequency == RecurrenceFrequency.MONTHLY,
                onClick = { onFrequencySelected(RecurrenceFrequency.MONTHLY) },
                label = { Text(text = stringResource(R.string.tasks_recurrence_monthly)) }
            )
        }
        if (frequency != RecurrenceFrequency.NONE) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = interval.toString(),
                onValueChange = { text ->
                    val parsed = text.filter { it.isDigit() }.toIntOrNull()
                    if (parsed != null) {
                        onIntervalChanged(parsed)
                    }
                },
                label = { Text(text = stringResource(R.string.tasks_recurrence_interval)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = intervalError != null,
                supportingText = intervalError?.let { error ->
                    { Text(text = error.asString()) }
                },
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun DateSelectorPreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DateSelector(
                date = PreviewData.addEditCalendarEventFormState.date,
                onClick = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AllDayTogglePreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AllDayToggle(isAllDay = false, onToggle = {})
        }
    }
}

@LightDarkPreview
@Composable
private fun TimeSelectorPreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TimeSelector(
                label = "Start Time",
                time = LocalTime.of(10, 0),
                onClick = {}
            )
        }
    }
}

