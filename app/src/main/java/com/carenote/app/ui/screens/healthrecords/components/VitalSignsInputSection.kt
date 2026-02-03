package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.ui.common.UiText
import com.carenote.app.ui.components.CareNoteTextField
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordFormState
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordViewModel

@Composable
internal fun VitalSignsFormSection(
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

@Composable
private fun TemperatureField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: UiText?
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
    errorMessage: UiText?
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
                text = errorMessage.asString(),
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
    errorMessage: UiText?
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
    errorMessage: UiText?
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
