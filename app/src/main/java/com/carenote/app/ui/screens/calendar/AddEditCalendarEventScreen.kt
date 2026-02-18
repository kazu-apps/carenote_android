package com.carenote.app.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.ui.components.CareNoteAddEditScaffold
import com.carenote.app.ui.components.CareNoteTimePickerDialog
import com.carenote.app.ui.screens.calendar.components.CalendarEventDateTimeSection
import com.carenote.app.ui.screens.calendar.components.CalendarEventDateTimePickers
import com.carenote.app.ui.screens.calendar.components.CalendarEventReminderSection
import com.carenote.app.ui.screens.calendar.components.CalendarEventTextFields
import com.carenote.app.ui.screens.calendar.components.EventTypeSelector
import com.carenote.app.ui.screens.calendar.components.RecurrenceSection
import com.carenote.app.ui.screens.calendar.components.TaskFields
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.util.SnackbarEvent
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

        CalendarEventOptionalSections(
            formState = formState,
            onFrequencySelected = onFrequencySelected,
            onIntervalChanged = onIntervalChanged,
            onPriorityChange = onPriorityChange,
            onToggleReminder = onToggleReminder,
            onReminderTimeChange = onReminderTimeChange
        )

        SaveCancelButtons(
            isSaving = formState.isSaving,
            onCancel = onCancel,
            onSave = onSave
        )
    }
}

@Composable
private fun CalendarEventOptionalSections(
    formState: AddEditCalendarEventFormState,
    onFrequencySelected: (RecurrenceFrequency) -> Unit,
    onIntervalChanged: (Int) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onToggleReminder: () -> Unit,
    onReminderTimeChange: () -> Unit
) {
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
            onPriorityChange = onPriorityChange
        )
    }

    CalendarEventReminderSection(
        enabled = formState.reminderEnabled,
        time = formState.reminderTime,
        onToggle = onToggleReminder,
        onClickTime = onReminderTimeChange
    )
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
