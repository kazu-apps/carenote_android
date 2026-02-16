package com.carenote.app.ui.screens.medication

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.ui.components.CareNoteAddEditScaffold
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent

@Composable
fun AddEditMedicationScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditMedicationViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
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
        stringResource(R.string.medication_edit)
    } else {
        stringResource(R.string.medication_add)
    }

    CareNoteAddEditScaffold(
        title = title,
        onNavigateBack = onNavigateBack,
        isDirty = viewModel.isDirty,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        MedicationFormBody(
            formState = formState,
            onUpdateName = viewModel::updateName,
            onUpdateDosage = viewModel::updateDosage,
            onToggleTiming = viewModel::toggleTiming,
            onUpdateTime = viewModel::updateTime,
            onToggleReminder = viewModel::toggleReminder,
            onUpdateCurrentStock = viewModel::updateCurrentStock,
            onUpdateLowStockThreshold = viewModel::updateLowStockThreshold,
            onSave = viewModel::saveMedication,
            onCancel = onNavigateBack,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun MedicationFormBody(
    formState: AddEditMedicationFormState,
    onUpdateName: (String) -> Unit,
    onUpdateDosage: (String) -> Unit,
    onToggleTiming: (MedicationTiming) -> Unit,
    onUpdateTime: (MedicationTiming, java.time.LocalTime) -> Unit,
    onToggleReminder: () -> Unit,
    onUpdateCurrentStock: (String) -> Unit,
    onUpdateLowStockThreshold: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
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
        MedicationNameAndDosage(formState, onUpdateName, onUpdateDosage)
        MedicationTimingSection(formState, onToggleTiming, onUpdateTime, onToggleReminder)
        MedicationStockSection(formState, onUpdateCurrentStock, onUpdateLowStockThreshold)
        MedicationFormActions(formState.isSaving, onSave, onCancel)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MedicationNameAndDosage(
    formState: AddEditMedicationFormState,
    onUpdateName: (String) -> Unit,
    onUpdateDosage: (String) -> Unit
) {
    CareNoteTextField(
        value = formState.name,
        onValueChange = onUpdateName,
        label = stringResource(R.string.medication_name),
        placeholder = stringResource(R.string.medication_name_placeholder),
        errorMessage = formState.nameError
    )
    CareNoteTextField(
        value = formState.dosage,
        onValueChange = onUpdateDosage,
        label = stringResource(R.string.medication_dosage),
        placeholder = stringResource(R.string.medication_dosage_placeholder),
        errorMessage = formState.dosageError
    )
}

@Composable
private fun MedicationTimingSection(
    formState: AddEditMedicationFormState,
    onToggleTiming: (MedicationTiming) -> Unit,
    onUpdateTime: (MedicationTiming, java.time.LocalTime) -> Unit,
    onToggleReminder: () -> Unit
) {
    Text(
        text = stringResource(R.string.medication_timing),
        style = MaterialTheme.typography.titleMedium
    )
    TimingSelector(
        selectedTimings = formState.timings,
        times = formState.times,
        onToggleTiming = onToggleTiming,
        onUpdateTime = onUpdateTime
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
            onCheckedChange = { onToggleReminder() }
        )
    }
}

@Composable
private fun MedicationStockSection(
    formState: AddEditMedicationFormState,
    onUpdateCurrentStock: (String) -> Unit,
    onUpdateLowStockThreshold: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.medication_stock_section),
        style = MaterialTheme.typography.titleMedium
    )
    OutlinedTextField(
        value = formState.currentStock,
        onValueChange = onUpdateCurrentStock,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.medication_current_stock_label)) },
        placeholder = { Text(stringResource(R.string.medication_current_stock_hint)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
    OutlinedTextField(
        value = formState.lowStockThreshold,
        onValueChange = onUpdateLowStockThreshold,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.medication_low_stock_threshold_label)) },
        placeholder = { Text(stringResource(R.string.medication_low_stock_threshold_hint)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
private fun MedicationFormActions(
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))
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
                selectedTimings = PreviewData.addEditMedicationFormState.timings,
                times = PreviewData.addEditMedicationFormState.times,
                onToggleTiming = {},
                onUpdateTime = { _, _ -> }
            )
        }
    }
}
