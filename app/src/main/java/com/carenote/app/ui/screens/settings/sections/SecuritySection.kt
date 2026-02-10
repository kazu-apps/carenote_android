package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.screens.settings.components.SettingsSection
import com.carenote.app.ui.screens.settings.components.SwitchPreference

@Composable
fun SecuritySection(
    biometricEnabled: Boolean,
    onBiometricEnabledChange: (Boolean) -> Unit,
    isBiometricAvailable: Boolean,
    isDeviceRooted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_security))
        if (isDeviceRooted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                        vertical = AppConfig.UI.PREFERENCE_VERTICAL_PADDING_DP.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.security_root_detected_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.security_root_detected_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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
