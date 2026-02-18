package com.carenote.app.ui.screens.calendar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.components.CareNoteDatePickerDialog
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.components.CareNoteTimePickerDialog
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.screens.calendar.AddEditCalendarEventFormState
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal fun CalendarEventTextFields(
    formState: AddEditCalendarEventFormState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    CareNoteTextField(
        value = formState.title,
        onValueChange = onTitleChange,
        label = stringResource(R.string.calendar_event_title),
        placeholder = stringResource(R.string.calendar_event_title_placeholder),
        errorMessage = formState.titleError,
        singleLine = true
    )

    CareNoteTextField(
        value = formState.description,
        onValueChange = onDescriptionChange,
        label = stringResource(R.string.calendar_event_description),
        placeholder = stringResource(R.string.calendar_event_description_placeholder),
        errorMessage = formState.descriptionError,
        singleLine = false,
        maxLines = AppConfig.Calendar.DESCRIPTION_PREVIEW_MAX_LINES + 2
    )
}

@Composable
internal fun CalendarEventDateTimeSection(
    formState: AddEditCalendarEventFormState,
    onShowDatePicker: () -> Unit,
    onToggleAllDay: () -> Unit,
    onShowStartTimePicker: () -> Unit,
    onShowEndTimePicker: () -> Unit
) {
    DateSelector(
        date = formState.date,
        onClick = onShowDatePicker
    )

    AllDayToggle(
        isAllDay = formState.isAllDay,
        onToggle = onToggleAllDay
    )

    if (!formState.isAllDay) {
        TimeSelector(
            label = stringResource(R.string.calendar_event_start_time),
            time = formState.startTime,
            onClick = onShowStartTimePicker
        )

        TimeSelector(
            label = stringResource(R.string.calendar_event_end_time),
            time = formState.endTime,
            onClick = onShowEndTimePicker
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EventTypeSelector(
    selectedType: CalendarEventType,
    onTypeChange: (CalendarEventType) -> Unit
) {
    Text(
        text = stringResource(R.string.calendar_event_type),
        style = MaterialTheme.typography.titleMedium
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CalendarEventType.entries.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeChange(type) },
                label = {
                    Text(text = stringResource(type.labelResId()))
                }
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
internal fun CalendarEventDateTimePickers(
    formState: AddEditCalendarEventFormState,
    showDatePicker: Boolean,
    showStartTimePicker: Boolean,
    showEndTimePicker: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onDismissDatePicker: () -> Unit,
    onStartTimeSelected: (LocalTime) -> Unit,
    onDismissStartTimePicker: () -> Unit,
    onEndTimeSelected: (LocalTime) -> Unit,
    onDismissEndTimePicker: () -> Unit
) {
    if (showDatePicker) {
        CareNoteDatePickerDialog(
            initialDate = formState.date,
            onDateSelected = onDateSelected,
            onDismiss = onDismissDatePicker
        )
    }

    if (showStartTimePicker) {
        CareNoteTimePickerDialog(
            initialTime = formState.startTime ?: LocalTime.of(
                AppConfig.Calendar.DEFAULT_START_HOUR,
                AppConfig.Calendar.DEFAULT_START_MINUTE
            ),
            onTimeSelected = onStartTimeSelected,
            onDismiss = onDismissStartTimePicker
        )
    }

    if (showEndTimePicker) {
        CareNoteTimePickerDialog(
            initialTime = formState.endTime ?: LocalTime.of(
                AppConfig.Calendar.DEFAULT_END_HOUR,
                AppConfig.Calendar.DEFAULT_END_MINUTE
            ),
            onTimeSelected = onEndTimeSelected,
            onDismiss = onDismissEndTimePicker
        )
    }
}

@Composable
internal fun DateSelector(
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
        Text(
            text = DateTimeFormatters.formatDate(date),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(
                    onClick = onClick,
                    onClickLabel = stringResource(R.string.calendar_select_date),
                    role = Role.Button
                )
                .padding(12.dp)
        )
    }
}

@Composable
internal fun AllDayToggle(
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
internal fun TimeSelector(
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
        Text(
            text = time?.let { DateTimeFormatters.formatTime(it) } ?: "--:--",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable(
                    onClick = onClick,
                    onClickLabel = stringResource(R.string.calendar_select_time),
                    role = Role.Button
                )
                .padding(12.dp)
        )
    }
}

@Composable
internal fun CalendarEventType.labelResId(): Int {
    return when (this) {
        CalendarEventType.HOSPITAL -> R.string.calendar_event_type_hospital
        CalendarEventType.VISIT -> R.string.calendar_event_type_visit
        CalendarEventType.DAYSERVICE -> R.string.calendar_event_type_dayservice
        CalendarEventType.TASK -> R.string.calendar_event_type_task
        CalendarEventType.OTHER -> R.string.calendar_event_type_other
    }
}

@Composable
internal fun RecurrenceSection(
    frequency: RecurrenceFrequency,
    interval: Int,
    intervalError: UiText?,
    onFrequencySelected: (RecurrenceFrequency) -> Unit,
    onIntervalChanged: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.tasks_recurrence),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        RecurrenceFrequencyChips(
            frequency = frequency,
            onFrequencySelected = onFrequencySelected
        )
        if (frequency != RecurrenceFrequency.NONE) {
            Spacer(modifier = Modifier.height(8.dp))
            RecurrenceIntervalField(
                interval = interval,
                intervalError = intervalError,
                onIntervalChanged = onIntervalChanged
            )
        }
    }
}

@Composable
internal fun RecurrenceFrequencyChips(
    frequency: RecurrenceFrequency,
    onFrequencySelected: (RecurrenceFrequency) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
}

@Composable
internal fun RecurrenceIntervalField(
    interval: Int,
    intervalError: UiText?,
    onIntervalChanged: (Int) -> Unit
) {
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

@LightDarkPreview
@Composable
internal fun DateSelectorPreview() {
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
internal fun AllDayTogglePreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AllDayToggle(isAllDay = false, onToggle = {})
        }
    }
}

@LightDarkPreview
@Composable
internal fun TimeSelectorPreview() {
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
