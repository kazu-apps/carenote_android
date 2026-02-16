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
        SettingsDialogState.QuietHoursStart ->
            QuietHoursStartDialog(settings, onDismiss, onDialogStateChange, viewModel)
        SettingsDialogState.QuietHoursEnd ->
            QuietHoursEndDialog(settings, onDismiss, viewModel)
        else -> SettingsDialogsExtended(
            dialogState, settings, onDismiss, onDialogStateChange, viewModel
        )
    }
}

@Composable
private fun SettingsDialogsExtended(
    dialogState: SettingsDialogState,
    settings: UserSettings,
    onDismiss: () -> Unit,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    viewModel: SettingsViewModel
) {
    when (dialogState) {
        SettingsDialogState.Temperature ->
            TemperatureDialog(settings, onDismiss, viewModel)
        SettingsDialogState.BpUpper ->
            BpUpperDialog(settings, onDismiss, viewModel)
        SettingsDialogState.BpLower ->
            BpLowerDialog(settings, onDismiss, viewModel)
        SettingsDialogState.PulseHigh ->
            PulseHighDialog(settings, onDismiss, viewModel)
        SettingsDialogState.PulseLow ->
            PulseLowDialog(settings, onDismiss, viewModel)
        else -> SettingsDialogsOther(
            dialogState, settings, onDismiss, onDialogStateChange, viewModel
        )
    }
}

@Composable
private fun SettingsDialogsOther(
    dialogState: SettingsDialogState,
    settings: UserSettings,
    onDismiss: () -> Unit,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    viewModel: SettingsViewModel
) {
    when (dialogState) {
        SettingsDialogState.MorningTime ->
            MedicationTimeDialog(MedicationTiming.MORNING, settings, onDismiss, viewModel)
        SettingsDialogState.NoonTime ->
            MedicationTimeDialog(MedicationTiming.NOON, settings, onDismiss, viewModel)
        SettingsDialogState.EveningTime ->
            MedicationTimeDialog(MedicationTiming.EVENING, settings, onDismiss, viewModel)
        SettingsDialogState.ResetConfirm ->
            ResetConfirmDialog(onDismiss, viewModel)
        SettingsDialogState.ChangePassword ->
            ChangePasswordDialogWrapper(onDismiss, viewModel)
        SettingsDialogState.DeleteAccountConfirm ->
            DeleteAccountConfirmDialog(onDismiss, onDialogStateChange)
        SettingsDialogState.ReauthenticateForDelete ->
            ReauthForDeleteDialog(onDismiss, viewModel)
        SettingsDialogState.DataExportTasks ->
            TaskExportDialog(onDismiss, viewModel)
        SettingsDialogState.DataExportNotes ->
            NoteExportDialog(onDismiss, viewModel)
        SettingsDialogState.SignOutConfirm ->
            SignOutConfirmDialog(onDismiss, viewModel)
        else -> { /* Already handled in parent */ }
    }
}

@Composable
private fun QuietHoursStartDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    viewModel: SettingsViewModel
) {
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

@Composable
private fun QuietHoursEndDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
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

@Composable
private fun TemperatureDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    NumberInputDialog(
        title = stringResource(R.string.settings_temperature_high),
        currentValue = settings.temperatureHigh.toString(),
        onDismiss = onDismiss,
        onConfirm = { value ->
            onDismiss()
            value.toDoubleOrNull()?.let {
                viewModel.updateTemperatureThreshold(it)
            }
        },
        keyboardType = KeyboardType.Decimal
    )
}

@Composable
private fun BpUpperDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    NumberInputDialog(
        title = stringResource(R.string.settings_bp_upper),
        currentValue = settings.bloodPressureHighUpper.toString(),
        onDismiss = onDismiss,
        onConfirm = { value ->
            onDismiss()
            value.toIntOrNull()?.let {
                viewModel.updateBloodPressureThresholds(
                    it, settings.bloodPressureHighLower
                )
            }
        }
    )
}

@Composable
private fun BpLowerDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    NumberInputDialog(
        title = stringResource(R.string.settings_bp_lower),
        currentValue = settings.bloodPressureHighLower.toString(),
        onDismiss = onDismiss,
        onConfirm = { value ->
            onDismiss()
            value.toIntOrNull()?.let {
                viewModel.updateBloodPressureThresholds(
                    settings.bloodPressureHighUpper, it
                )
            }
        }
    )
}

@Composable
private fun PulseHighDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
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

@Composable
private fun PulseLowDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
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

@Composable
private fun MedicationTimeDialog(
    timing: MedicationTiming,
    settings: UserSettings,
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    val (titleResId, hour, minute) = when (timing) {
        MedicationTiming.MORNING -> Triple(
            R.string.medication_morning,
            settings.morningHour,
            settings.morningMinute
        )
        MedicationTiming.NOON -> Triple(
            R.string.medication_noon,
            settings.noonHour,
            settings.noonMinute
        )
        MedicationTiming.EVENING -> Triple(
            R.string.medication_evening,
            settings.eveningHour,
            settings.eveningMinute
        )
    }
    CareNoteTimePickerDialog(
        title = stringResource(titleResId),
        initialHour = hour,
        initialMinute = minute,
        onDismiss = onDismiss,
        onConfirm = { h, m ->
            onDismiss()
            viewModel.updateMedicationTime(timing, h, m)
        }
    )
}

@Composable
private fun ResetConfirmDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
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

@Composable
private fun ChangePasswordDialogWrapper(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    ChangePasswordDialog(
        onConfirm = { currentPassword, newPassword ->
            onDismiss()
            viewModel.changePassword(currentPassword, newPassword)
        },
        onDismiss = onDismiss
    )
}

@Composable
private fun DeleteAccountConfirmDialog(
    onDismiss: () -> Unit,
    onDialogStateChange: (SettingsDialogState) -> Unit
) {
    ConfirmDialog(
        title = stringResource(R.string.settings_delete_account_title),
        message = stringResource(R.string.settings_delete_account_message),
        onConfirm = {
            onDialogStateChange(SettingsDialogState.ReauthenticateForDelete)
        },
        onDismiss = onDismiss,
        isDestructive = true
    )
}

@Composable
private fun ReauthForDeleteDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    ReauthenticateDialog(
        onConfirm = { password ->
            onDismiss()
            viewModel.deleteAccount(password)
        },
        onDismiss = onDismiss
    )
}

@Composable
private fun TaskExportDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    DataExportDialog(
        entityLabel = stringResource(R.string.settings_data_export_tasks),
        onExport = { format, periodDays ->
            onDismiss()
            when (format) {
                ExportFormat.CSV -> viewModel.exportTasksCsv(periodDays)
                ExportFormat.PDF -> viewModel.exportTasksPdf(periodDays)
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
private fun NoteExportDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    DataExportDialog(
        entityLabel = stringResource(R.string.settings_data_export_notes),
        onExport = { format, periodDays ->
            onDismiss()
            when (format) {
                ExportFormat.CSV -> viewModel.exportNotesCsv(periodDays)
                ExportFormat.PDF -> viewModel.exportNotesPdf(periodDays)
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
private fun SignOutConfirmDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    ConfirmDialog(
        title = stringResource(R.string.settings_sign_out_confirm_title),
        message = stringResource(R.string.settings_sign_out_confirm_message),
        onConfirm = {
            onDismiss()
            viewModel.signOut()
        },
        onDismiss = onDismiss
    )
}
