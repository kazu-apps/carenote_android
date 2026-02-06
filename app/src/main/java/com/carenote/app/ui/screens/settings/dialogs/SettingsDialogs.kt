package com.carenote.app.ui.screens.settings.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.carenote.app.R
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.screens.settings.SettingsDialogState
import com.carenote.app.ui.screens.settings.SettingsViewModel
import com.carenote.app.ui.screens.settings.components.NumberInputDialog
import com.carenote.app.ui.components.CareNoteTimePickerDialog

@Composable
fun SettingsDialogs(
    dialogState: SettingsDialogState,
    settings: UserSettings,
    onDismiss: () -> Unit,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    viewModel: SettingsViewModel
) {
    when (dialogState) {
        SettingsDialogState.None -> { /* No dialog shown */ }

        SettingsDialogState.QuietHoursStart -> {
            CareNoteTimePickerDialog(
                title = stringResource(R.string.settings_quiet_hours),
                initialHour = settings.quietHoursStart,
                initialMinute = 0,
                onDismiss = onDismiss,
                onConfirm = { hour, _ ->
                    onDialogStateChange(SettingsDialogState.QuietHoursEnd)
                    viewModel.updateQuietHours(hour, settings.quietHoursEnd)
                }
            )
        }

        SettingsDialogState.QuietHoursEnd -> {
            CareNoteTimePickerDialog(
                title = stringResource(R.string.settings_quiet_hours),
                initialHour = settings.quietHoursEnd,
                initialMinute = 0,
                onDismiss = onDismiss,
                onConfirm = { hour, _ ->
                    onDismiss()
                    viewModel.updateQuietHours(settings.quietHoursStart, hour)
                }
            )
        }

        SettingsDialogState.Temperature -> {
            NumberInputDialog(
                title = stringResource(R.string.settings_temperature_high),
                currentValue = settings.temperatureHigh.toString(),
                onDismiss = onDismiss,
                onConfirm = { value ->
                    onDismiss()
                    value.toDoubleOrNull()?.let { viewModel.updateTemperatureThreshold(it) }
                },
                keyboardType = KeyboardType.Decimal
            )
        }

        SettingsDialogState.BpUpper -> {
            NumberInputDialog(
                title = stringResource(R.string.settings_bp_upper),
                currentValue = settings.bloodPressureHighUpper.toString(),
                onDismiss = onDismiss,
                onConfirm = { value ->
                    onDismiss()
                    value.toIntOrNull()?.let {
                        viewModel.updateBloodPressureThresholds(it, settings.bloodPressureHighLower)
                    }
                }
            )
        }

        SettingsDialogState.BpLower -> {
            NumberInputDialog(
                title = stringResource(R.string.settings_bp_lower),
                currentValue = settings.bloodPressureHighLower.toString(),
                onDismiss = onDismiss,
                onConfirm = { value ->
                    onDismiss()
                    value.toIntOrNull()?.let {
                        viewModel.updateBloodPressureThresholds(settings.bloodPressureHighUpper, it)
                    }
                }
            )
        }

        SettingsDialogState.PulseHigh -> {
            NumberInputDialog(
                title = stringResource(R.string.settings_pulse_high),
                currentValue = settings.pulseHigh.toString(),
                onDismiss = onDismiss,
                onConfirm = { value ->
                    onDismiss()
                    value.toIntOrNull()?.let {
                        viewModel.updatePulseThresholds(it, settings.pulseLow)
                    }
                }
            )
        }

        SettingsDialogState.PulseLow -> {
            NumberInputDialog(
                title = stringResource(R.string.settings_pulse_low),
                currentValue = settings.pulseLow.toString(),
                onDismiss = onDismiss,
                onConfirm = { value ->
                    onDismiss()
                    value.toIntOrNull()?.let {
                        viewModel.updatePulseThresholds(settings.pulseHigh, it)
                    }
                }
            )
        }

        SettingsDialogState.MorningTime -> {
            CareNoteTimePickerDialog(
                title = stringResource(R.string.medication_morning),
                initialHour = settings.morningHour,
                initialMinute = settings.morningMinute,
                onDismiss = onDismiss,
                onConfirm = { hour, minute ->
                    onDismiss()
                    viewModel.updateMedicationTime(MedicationTiming.MORNING, hour, minute)
                }
            )
        }

        SettingsDialogState.NoonTime -> {
            CareNoteTimePickerDialog(
                title = stringResource(R.string.medication_noon),
                initialHour = settings.noonHour,
                initialMinute = settings.noonMinute,
                onDismiss = onDismiss,
                onConfirm = { hour, minute ->
                    onDismiss()
                    viewModel.updateMedicationTime(MedicationTiming.NOON, hour, minute)
                }
            )
        }

        SettingsDialogState.EveningTime -> {
            CareNoteTimePickerDialog(
                title = stringResource(R.string.medication_evening),
                initialHour = settings.eveningHour,
                initialMinute = settings.eveningMinute,
                onDismiss = onDismiss,
                onConfirm = { hour, minute ->
                    onDismiss()
                    viewModel.updateMedicationTime(MedicationTiming.EVENING, hour, minute)
                }
            )
        }

        SettingsDialogState.ResetConfirm -> {
            ConfirmDialog(
                title = stringResource(R.string.settings_reset_confirm_title),
                message = stringResource(R.string.settings_reset_confirm_message),
                onConfirm = {
                    onDismiss()
                    viewModel.resetToDefaults()
                },
                onDismiss = onDismiss
            )
        }
    }
}
