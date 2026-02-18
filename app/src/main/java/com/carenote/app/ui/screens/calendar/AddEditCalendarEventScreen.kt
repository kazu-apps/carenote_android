package com.carenote.app.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import com.carenote.app.domain.model.TaskPriority
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.components.CareNoteAddEditScaffold
import com.carenote.app.ui.components.CareNoteDatePickerDialog
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.components.CareNoteTimePickerDialog
import com.carenote.app.ui.screens.calendar.components.TaskFields
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCalendarEventScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditCalendarEventViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    CalendarEventScreenEffects(viewModel, snackbarHostState, onNavigateBack)

    val titleRes = if (formState.isEditMode) R.string.calendar_edit_event else R.string.calendar_add_event

    CalendarEventScreenScaffold(
        title = stringResource(titleRes),
        formState = formState,
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onShowDatePicker = { showDatePicker = true },
        onShowStartTimePicker = { showStartTimePicker = true },
        onShowEndTimePicker = { showEndTimePicker = true },
        onShowReminderTimePicker = { showReminderTimePicker = true }
    )

    CalendarEventDialogs(
        formState = formState,
        viewModel = viewModel,
        showDatePicker = showDatePicker,
        showStartTimePicker = showStartTimePicker,
        showEndTimePicker = showEndTimePicker,
        showReminderTimePicker = showReminderTimePicker,
        onDismissDatePicker = { showDatePicker = false },
        onDismissStartTimePicker = { showStartTimePicker = false },
        onDismissEndTimePicker = { showEndTimePicker = false },
        onDismissReminderTimePicker = { showReminderTimePicker = false }
    )
}

@Suppress("LongParameterList")
@Composable
private fun CalendarEventDialogs(
    formState: AddEditCalendarEventFormState,
    viewModel: AddEditCalendarEventViewModel,
    showDatePicker: Boolean,
    showStartTimePicker: Boolean,
    showEndTimePicker: Boolean,
    showReminderTimePicker: Boolean,
    onDismissDatePicker: () -> Unit,
    onDismissStartTimePicker: () -> Unit,
    onDismissEndTimePicker: () -> Unit,
    onDismissReminderTimePicker: () -> Unit
) {
    CalendarEventDateTimePickers(
        formState = formState,
        showDatePicker = showDatePicker,
        showStartTimePicker = showStartTimePicker,
        showEndTimePicker = showEndTimePicker,
        onDateSelected = { date ->
            viewModel.updateDate(date)
            onDismissDatePicker()
        },
        onDismissDatePicker = onDismissDatePicker,
        onStartTimeSelected = { time ->
            viewModel.updateStartTime(time)
            onDismissStartTimePicker()
        },
        onDismissStartTimePicker = onDismissStartTimePicker,
        onEndTimeSelected = { time ->
            viewModel.updateEndTime(time)
            onDismissEndTimePicker()
        },
        onDismissEndTimePicker = onDismissEndTimePicker
    )

    ReminderTimePicker(
        visible = showReminderTimePicker,
        currentTime = formState.reminderTime,
        onTimeSelected = { time ->
            viewModel.updateReminderTime(time)
            onDismissReminderTimePicker()
        },
        onDismiss = onDismissReminderTimePicker
    )
}

@Composable
private fun ReminderTimePicker(
    visible: Boolean,
    currentTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        CareNoteTimePickerDialog(
            initialTime = currentTime ?: LocalTime.of(9, 0),
            onTimeSelected = onTimeSelected,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun CalendarEventScreenEffects(
    viewModel: AddEditCalendarEventViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { saved ->
            if (saved) { onNavigateBack() }
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
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarEventScreenScaffold(
    title: String,
    formState: AddEditCalendarEventFormState,
    viewModel: AddEditCalendarEventViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onShowDatePicker: () -> Unit,
    onShowStartTimePicker: () -> Unit,
    onShowEndTimePicker: () -> Unit,
    onShowReminderTimePicker: () -> Unit
) {
    CareNoteAddEditScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        isDirty = viewModel.isDirty,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        CalendarEventFormBody(
            formState = formState,
            onTitleChange = viewModel::updateTitle,
            onDescriptionChange = viewModel::updateDescription,
            onTypeChange = viewModel::updateType,
            onShowDatePicker = onShowDatePicker,
            onToggleAllDay = viewModel::toggleAllDay,
            onShowStartTimePicker = onShowStartTimePicker,
            onShowEndTimePicker = onShowEndTimePicker,
            onFrequencySelected = viewModel::updateRecurrenceFrequency,
            onIntervalChanged = viewModel::updateRecurrenceInterval,
            onPriorityChange = viewModel::updatePriority,
            onToggleReminder = viewModel::toggleReminder,
            onReminderTimeChange = onShowReminderTimePicker,
            onCancel = onNavigateBack,
            onSave = viewModel::saveEvent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarEventFormBody(
    formState: AddEditCalendarEventFormState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (CalendarEventType) -> Unit,
    onShowDatePicker: () -> Unit,
    onToggleAllDay: () -> Unit,
    onShowStartTimePicker: () -> Unit,
    onShowEndTimePicker: () -> Unit,
    onFrequencySelected: (RecurrenceFrequency) -> Unit,
    onIntervalChanged: (Int) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onToggleReminder: () -> Unit,
    onReminderTimeChange: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CalendarEventTextFields(
            formState = formState,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange
        )

        EventTypeSelector(
            selectedType = formState.type,
            onTypeChange = onTypeChange
        )

        CalendarEventDateTimeSection(
            formState = formState,
            onShowDatePicker = onShowDatePicker,
            onToggleAllDay = onToggleAllDay,
            onShowStartTimePicker = onShowStartTimePicker,
            onShowEndTimePicker = onShowEndTimePicker
        )

        RecurrenceSection(
            frequency = formState.recurrenceFrequency,
            interval = formState.recurrenceInterval,
            intervalError = formState.recurrenceIntervalError,
            onFrequencySelected = onFrequencySelected,
            onIntervalChanged = onIntervalChanged
        )

        AnimatedVisibility(visible = formState.type == CalendarEventType.TASK) {
            TaskFields(
                priority = formState.priority,
                onPriorityChange = onPriorityChange,
                reminderEnabled = formState.reminderEnabled,
                onToggleReminder = onToggleReminder,
                reminderTime = formState.reminderTime,
                onReminderTimeChange = onReminderTimeChange
            )
        }

        SaveCancelButtons(
            isSaving = formState.isSaving,
            onCancel = onCancel,
            onSave = onSave
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CalendarEventTextFields(
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
private fun CalendarEventDateTimeSection(
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
private fun EventTypeSelector(
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

@Composable
private fun SaveCancelButtons(
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = ButtonShape
        ) {
            Text(text = stringResource(R.string.common_cancel))
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            shape = ButtonShape,
            enabled = !isSaving
        ) {
            if (isSaving) {
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
}

@Suppress("LongParameterList")
@Composable
private fun CalendarEventDateTimePickers(
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
private fun CalendarEventType.labelResId(): Int {
    return when (this) {
        CalendarEventType.HOSPITAL -> R.string.calendar_event_type_hospital
        CalendarEventType.VISIT -> R.string.calendar_event_type_visit
        CalendarEventType.DAYSERVICE -> R.string.calendar_event_type_dayservice
        CalendarEventType.TASK -> R.string.calendar_event_type_task
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
private fun RecurrenceFrequencyChips(
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
private fun RecurrenceIntervalField(
    interval: Int,
    intervalError: com.carenote.app.ui.common.UiText?,
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

