package com.carenote.app.ui.screens.calendar

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
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
fun AddEditCalendarEventScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditCalendarEventViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
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
                    onClick = handleBack,
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

