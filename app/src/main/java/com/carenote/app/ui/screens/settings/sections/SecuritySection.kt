package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.SettingsSection
import com.carenote.app.ui.screens.settings.components.SwitchPreference

@Composable
fun SecuritySection(
    biometricEnabled: Boolean,
    onBiometricEnabledChange: (Boolean) -> Unit,
    isBiometricAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_security))
        SwitchPreference(
            title = stringResource(R.string.settings_biometric_enabled),
            checked = biometricEnabled,
            onCheckedChange = onBiometricEnabledChange,
            summary = if (isBiometricAvailable) {
                stringResource(R.string.settings_biometric_summary)
            } else {
                stringResource(R.string.settings_biometric_not_available)
            },
            enabled = isBiometricAvailable
        )
    }
}
