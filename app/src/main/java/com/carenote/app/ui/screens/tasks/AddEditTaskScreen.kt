package com.carenote.app.ui.screens.tasks

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.carenote.app.domain.model.RecurrenceFrequency
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.ui.components.CareNoteDatePickerDialog
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.components.CareNoteTimePickerDialog
import com.carenote.app.ui.components.ConfirmDialog
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
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val handleBack: () -> Unit = {
        if (viewModel.isDirty) showDiscardDialog = true else onNavigateBack()
    }

    BackHandler(enabled = viewModel.isDirty) {
        showDiscardDialog = true
    }

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
        stringResource(R.string.tasks_edit)
    } else {
        stringResource(R.string.tasks_add)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
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

            DueDateSelector(
                dueDate = formState.dueDate,
                onClickDate = { showDatePicker = true },
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
                onClickTime = { showTimePicker = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = handleBack,
                    modifier = Modifier.weight(1f),
                    shape = ButtonShape
                ) {
                    Text(text = stringResource(R.string.common_cancel))
                }
                Button(
                    onClick = viewModel::saveTask,
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

    if (showDiscardDialog) {
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_discard_title),
            message = stringResource(R.string.ui_confirm_discard_message),
            confirmLabel = stringResource(R.string.ui_confirm_discard_yes),
            dismissLabel = stringResource(R.string.ui_confirm_discard_no),
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false },
            isDestructive = true
        )
    }

    if (showDatePicker) {
        CareNoteDatePickerDialog(
            initialDate = formState.dueDate ?: LocalDate.now(),
            onDateSelected = { date ->
                viewModel.updateDueDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        CareNoteTimePickerDialog(
            initialTime = formState.reminderTime ?: LocalTime.of(9, 0),
            onTimeSelected = { time ->
                viewModel.updateReminderTime(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
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
            TextButton(onClick = onClickDate) {
                Text(
                    text = dueDate?.let { DateTimeFormatters.formatDate(it) }
                        ?: stringResource(R.string.tasks_task_no_due_date),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
                TextButton(onClick = onClickTime) {
                    Text(
                        text = time?.let {
                            String.format("%02d:%02d", it.hour, it.minute)
                        } ?: stringResource(R.string.tasks_reminder_time_not_set),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
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

