package com.carenote.app.ui.screens.settings

sealed class SettingsDialogState {
    data object None : SettingsDialogState()
    data object QuietHoursStart : SettingsDialogState()
    data object QuietHoursEnd : SettingsDialogState()
    data object Temperature : SettingsDialogState()
    data object BpUpper : SettingsDialogState()
    data object BpLower : SettingsDialogState()
    data object PulseHigh : SettingsDialogState()
    data object PulseLow : SettingsDialogState()
    data object MorningTime : SettingsDialogState()
    data object NoonTime : SettingsDialogState()
    data object EveningTime : SettingsDialogState()
    data object ResetConfirm : SettingsDialogState()
    data object ChangePassword : SettingsDialogState()
    data object DeleteAccountConfirm : SettingsDialogState()
    data object ReauthenticateForDelete : SettingsDialogState()
    data object SignOutConfirm : SettingsDialogState()
    data object DataExportTasks : SettingsDialogState()
    data object DataExportNotes : SettingsDialogState()
}

val SettingsDialogState.isTimePicker: Boolean
    get() = this is SettingsDialogState.QuietHoursStart ||
            this is SettingsDialogState.QuietHoursEnd ||
            this is SettingsDialogState.MorningTime ||
            this is SettingsDialogState.NoonTime ||
            this is SettingsDialogState.EveningTime

val SettingsDialogState.isNumberInput: Boolean
    get() = this is SettingsDialogState.Temperature ||
            this is SettingsDialogState.BpUpper ||
            this is SettingsDialogState.BpLower ||
            this is SettingsDialogState.PulseHigh ||
            this is SettingsDialogState.PulseLow

val SettingsDialogState.isConfirm: Boolean
    get() = this is SettingsDialogState.ResetConfirm ||
            this is SettingsDialogState.DeleteAccountConfirm ||
            this is SettingsDialogState.SignOutConfirm
