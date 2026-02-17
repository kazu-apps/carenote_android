package com.carenote.app.ui.screens.tasks

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.TaskPriority
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    SavedEventEffect(viewModel, onNavigateBack)
    SnackbarCollectEffect(viewModel, snackbarHostState, context)

    val title = if (formState.isEditMode) {
        stringResource(R.string.tasks_edit)
    } else {
        stringResource(R.string.tasks_add)
    }

    CareNoteAddEditScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        isDirty = viewModel.isDirty,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        TaskFormContent(
            formState = formState,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onShowDatePicker = { showDatePicker = true },
            onShowTimePicker = { showTimePicker = true },
            modifier = Modifier.padding(innerPadding)
        )
    }

    TaskDatePickerDialog(
        showDatePicker = showDatePicker,
        dueDate = formState.dueDate,
        onDateSelected = { date ->
            viewModel.updateDueDate(date)
            showDatePicker = false
        },
        onDismiss = { showDatePicker = false }
    )

    TaskTimePickerDialog(
        showTimePicker = showTimePicker,
        reminderTime = formState.reminderTime,
        onTimeSelected = { time ->
            viewModel.updateReminderTime(time)
            showTimePicker = false
        },
        onDismiss = { showTimePicker = false }
    )
}

@Composable
private fun SavedEventEffect(
    viewModel: AddEditTaskViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { saved ->
            if (saved) onNavigateBack()
        }
    }
}

@Composable
private fun SnackbarCollectEffect(
    viewModel: AddEditTaskViewModel,
    snackbarHostState: SnackbarHostState,
    context: android.content.Context
) {
    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId ->
                    context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
private fun TaskFormContent(
    formState: AddEditTaskFormState,
    viewModel: AddEditTaskViewModel,
    onNavigateBack: () -> Unit,
    onShowDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        TaskFormFields(formState = formState, viewModel = viewModel)

        TaskFormSelectors(
            formState = formState,
            viewModel = viewModel,
            onShowDatePicker = onShowDatePicker,
            onShowTimePicker = onShowTimePicker
        )

        Spacer(modifier = Modifier.height(8.dp))

        TaskFormButtons(
            onNavigateBack = onNavigateBack,
            onSave = viewModel::saveTask,
            isSaving = formState.isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TaskFormFields(
    formState: AddEditTaskFormState,
    viewModel: AddEditTaskViewModel
) {
    CareNoteTextField(
        value = formState.title,
        onValueChange = viewModel::updateTitle,
        label = stringResource(R.string.tasks_task_title),
        placeholder = stringResource(R.string.tasks_task_title_placeholder),
        errorMessage = formState.titleError,
        singleLine = true
    )

    CareNoteTextField(
        value = formState.description,
        onValueChange = viewModel::updateDescription,
        label = stringResource(R.string.tasks_task_description),
        placeholder = stringResource(R.string.tasks_task_description_placeholder),
        errorMessage = formState.descriptionError,
        singleLine = false,
        maxLines = AppConfig.Task.DESCRIPTION_PREVIEW_MAX_LINES + 2
    )
}

@Composable
private fun TaskFormSelectors(
    formState: AddEditTaskFormState,
    viewModel: AddEditTaskViewModel,
    onShowDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit
) {
    DueDateSelector(
        dueDate = formState.dueDate,
        onClickDate = onShowDatePicker,
        onClearDate = { viewModel.updateDueDate(null) }
    )

    PrioritySelector(
        selectedPriority = formState.priority,
        onPrioritySelected = viewModel::updatePriority
    )

    RecurrenceSection(
        frequency = formState.recurrenceFrequency,
        interval = formState.recurrenceInterval,
        intervalError = formState.recurrenceIntervalError,
        onFrequencySelected = viewModel::updateRecurrenceFrequency,
        onIntervalChanged = viewModel::updateRecurrenceInterval
    )

    ReminderSection(
        enabled = formState.reminderEnabled,
        time = formState.reminderTime,
        onToggle = viewModel::toggleReminder,
        onClickTime = onShowTimePicker
    )
}

@Composable
private fun TaskFormButtons(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(
    showDatePicker: Boolean,
    dueDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDatePicker) {
        CareNoteDatePickerDialog(
            initialDate = dueDate ?: LocalDate.now(),
            onDateSelected = onDateSelected,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTimePickerDialog(
    showTimePicker: Boolean,
    reminderTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    if (showTimePicker) {
        CareNoteTimePickerDialog(
            initialTime = reminderTime ?: LocalTime.of(9, 0),
            onTimeSelected = onTimeSelected,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun DueDateSelector(
    dueDate: LocalDate?,
    onClickDate: () -> Unit,
    onClearDate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.tasks_task_due_date),
            style = MaterialTheme.typography.titleMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dueDate?.let { DateTimeFormatters.formatDate(it) }
                    ?: stringResource(R.string.tasks_task_no_due_date),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(
                        onClick = onClickDate,
                        onClickLabel = stringResource(R.string.tasks_select_due_date),
                        role = Role.Button
                    )
                    .padding(12.dp)
            )
            if (dueDate != null) {
                TextButton(onClick = onClearDate) {
                    Text(
                        text = stringResource(R.string.common_delete),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceFrequencyChips(
    frequency: RecurrenceFrequency,
    onFrequencySelected: (RecurrenceFrequency) -> Unit
) {
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

@LightDarkPreview
@Composable
private fun DueDateSelectorPreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DueDateSelector(
                dueDate = PreviewData.addEditTaskFormState.dueDate,
                onClickDate = {},
                onClearDate = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun PrioritySelectorPreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PrioritySelector(
                selectedPriority = TaskPriority.HIGH,
                onPrioritySelected = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun ReminderSectionPreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ReminderSection(
                enabled = true,
                time = LocalTime.of(9, 0),
                onToggle = {},
                onClickTime = {}
            )
        }
    }
}

