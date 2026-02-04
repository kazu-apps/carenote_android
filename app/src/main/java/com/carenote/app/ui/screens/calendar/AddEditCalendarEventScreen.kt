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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCalendarEventScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditCalendarEventViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
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
                    IconButton(onClick = onNavigateBack) {
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
        CalendarDatePickerDialog(
            initialDate = formState.date,
            onDateSelected = { date ->
                viewModel.updateDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showStartTimePicker) {
        CalendarTimePickerDialog(
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
        CalendarTimePickerDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = initialDate
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selected = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    onDateSelected(selected)
                }
            }) {
                Text(text = stringResource(R.string.ui_confirm_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text(text = stringResource(R.string.ui_confirm_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
