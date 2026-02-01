package com.carenote.app.ui.screens.healthrecords

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.screens.healthrecords.components.excretionTypeLabel
import com.carenote.app.ui.screens.healthrecords.components.mealAmountLabel
import com.carenote.app.ui.theme.ButtonShape
import com.carenote.app.ui.theme.ChipShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHealthRecordScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddEditHealthRecordViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { saved ->
            if (saved) {
                onNavigateBack()
            }
        }
    }

    val title = if (formState.isEditMode) {
        stringResource(R.string.health_records_edit)
    } else {
        stringResource(R.string.health_records_add)
    }

    Scaffold(
        topBar = { AddEditHealthRecordTopBar(title = title, onNavigateBack = onNavigateBack) }
    ) { innerPadding ->
        AddEditHealthRecordContent(
            formState = formState,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditHealthRecordTopBar(
    title: String,
    onNavigateBack: () -> Unit
) {
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

@Composable
private fun AddEditHealthRecordContent(
    formState: AddEditHealthRecordFormState,
    viewModel: AddEditHealthRecordViewModel,
    onNavigateBack: () -> Unit,
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

        if (formState.generalError != null) {
            GeneralErrorBanner(errorMessage = formState.generalError)
        }

        VitalSignsFormSection(formState = formState, viewModel = viewModel)

        SelectionFormSection(formState = formState, viewModel = viewModel)

        ConditionNoteField(
            value = formState.conditionNote,
            onValueChange = viewModel::updateConditionNote
        )

        FormActionButtons(
            isSaving = formState.isSaving,
            onCancel = onNavigateBack,
            onSave = viewModel::saveRecord
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun VitalSignsFormSection(
    formState: AddEditHealthRecordFormState,
    viewModel: AddEditHealthRecordViewModel
) {
    TemperatureField(
        value = formState.temperature,
        onValueChange = viewModel::updateTemperature,
        errorMessage = formState.temperatureError
    )

    BloodPressureFields(
        highValue = formState.bloodPressureHigh,
        lowValue = formState.bloodPressureLow,
        onHighValueChange = viewModel::updateBloodPressureHigh,
        onLowValueChange = viewModel::updateBloodPressureLow,
        errorMessage = formState.bloodPressureError
    )

    PulseField(
        value = formState.pulse,
        onValueChange = viewModel::updatePulse,
        errorMessage = formState.pulseError
    )

    WeightField(
        value = formState.weight,
        onValueChange = viewModel::updateWeight,
        errorMessage = formState.weightError
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectionFormSection(
    formState: AddEditHealthRecordFormState,
    viewModel: AddEditHealthRecordViewModel
) {
    MealSection(
        selectedMeal = formState.meal,
        onMealSelected = viewModel::updateMeal
    )

    ExcretionSection(
        selectedExcretion = formState.excretion,
        onExcretionSelected = viewModel::updateExcretion
    )
}

@Composable
private fun GeneralErrorBanner(errorMessage: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun TemperatureField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?
) {
    CareNoteTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.health_records_temperature),
        errorMessage = errorMessage,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        trailingIcon = {
            Text(
                text = stringResource(R.string.health_records_temperature_unit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun BloodPressureFields(
    highValue: String,
    lowValue: String,
    onHighValueChange: (String) -> Unit,
    onLowValueChange: (String) -> Unit,
    errorMessage: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.health_records_blood_pressure),
            style = MaterialTheme.typography.titleMedium
        )

        BloodPressureInputRow(
            highValue = highValue,
            lowValue = lowValue,
            onHighValueChange = onHighValueChange,
            onLowValueChange = onLowValueChange
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun BloodPressureInputRow(
    highValue: String,
    lowValue: String,
    onHighValueChange: (String) -> Unit,
    onLowValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CareNoteTextField(
            value = highValue,
            onValueChange = onHighValueChange,
            label = stringResource(R.string.health_records_bp_high),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = stringResource(R.string.health_records_bp_separator),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp)
        )

        CareNoteTextField(
            value = lowValue,
            onValueChange = onLowValueChange,
            label = stringResource(R.string.health_records_bp_low),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = stringResource(R.string.health_records_blood_pressure_unit),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun PulseField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?
) {
    CareNoteTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.health_records_pulse),
        errorMessage = errorMessage,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            Text(
                text = stringResource(R.string.health_records_pulse_unit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun WeightField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?
) {
    CareNoteTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.health_records_weight),
        errorMessage = errorMessage,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        trailingIcon = {
            Text(
                text = stringResource(R.string.health_records_weight_unit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealSection(
    selectedMeal: MealAmount?,
    onMealSelected: (MealAmount?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.health_records_meal),
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MealAmount.entries.forEach { meal ->
                FilterChip(
                    selected = selectedMeal == meal,
                    onClick = {
                        onMealSelected(if (selectedMeal == meal) null else meal)
                    },
                    label = { Text(text = mealAmountLabel(meal)) },
                    shape = ChipShape
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExcretionSection(
    selectedExcretion: ExcretionType?,
    onExcretionSelected: (ExcretionType?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.health_records_excretion),
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExcretionType.entries.forEach { excretion ->
                FilterChip(
                    selected = selectedExcretion == excretion,
                    onClick = {
                        onExcretionSelected(
                            if (selectedExcretion == excretion) null else excretion
                        )
                    },
                    label = { Text(text = excretionTypeLabel(excretion)) },
                    shape = ChipShape
                )
            }
        }
    }
}

@Composable
private fun ConditionNoteField(
    value: String,
    onValueChange: (String) -> Unit
) {
    CareNoteTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.health_records_condition),
        placeholder = stringResource(R.string.health_records_condition_placeholder),
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        modifier = Modifier.height((AppConfig.Note.CONTENT_MIN_LINES * 28).dp)
    )
}

@Composable
private fun FormActionButtons(
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
