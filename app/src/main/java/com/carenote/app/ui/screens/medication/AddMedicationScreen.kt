package com.carenote.app.ui.screens.medication

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.medication_add),
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
                value = formState.name,
                onValueChange = viewModel::updateName,
                label = stringResource(R.string.medication_name),
                placeholder = stringResource(R.string.medication_name_placeholder),
                errorMessage = formState.nameError
            )

            CareNoteTextField(
                value = formState.dosage,
                onValueChange = viewModel::updateDosage,
                label = stringResource(R.string.medication_dosage),
                placeholder = stringResource(R.string.medication_dosage_placeholder),
                errorMessage = formState.dosageError
            )

            Text(
                text = stringResource(R.string.medication_timing),
                style = MaterialTheme.typography.titleMedium
            )

            TimingSelector(
                selectedTimings = formState.timings,
                times = formState.times,
                onToggleTiming = viewModel::toggleTiming,
                onUpdateTime = viewModel::updateTime
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.common_reminder),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = formState.reminderEnabled,
                    onCheckedChange = { viewModel.toggleReminder() }
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
                    onClick = viewModel::saveMedication,
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
}

@Composable
private fun TimingSelector(
    selectedTimings: List<MedicationTiming>,
    times: Map<MedicationTiming, java.time.LocalTime>,
    onToggleTiming: (MedicationTiming) -> Unit,
    onUpdateTime: (MedicationTiming, java.time.LocalTime) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MedicationTiming.entries.forEach { timing ->
            val isSelected = selectedTimings.contains(timing)
            val label = when (timing) {
                MedicationTiming.MORNING -> stringResource(R.string.medication_morning)
                MedicationTiming.NOON -> stringResource(R.string.medication_noon)
                MedicationTiming.EVENING -> stringResource(R.string.medication_evening)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleTiming(timing) }
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (isSelected) {
                    times[timing]?.let { time ->
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = {
                                Text(
                                    text = DateTimeFormatters.formatTime(time),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun TimingSelectorPreview() {
    CareNoteTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TimingSelector(
                selectedTimings = PreviewData.addMedicationFormState.timings,
                times = PreviewData.addMedicationFormState.times,
                onToggleTiming = {},
                onUpdateTime = { _, _ -> }
            )
        }
    }
}
