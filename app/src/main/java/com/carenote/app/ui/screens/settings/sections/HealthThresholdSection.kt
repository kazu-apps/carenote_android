package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun HealthThresholdSection(
    temperatureText: String,
    onTemperatureClick: () -> Unit,
    bpUpperText: String,
    onBpUpperClick: () -> Unit,
    bpLowerText: String,
    onBpLowerClick: () -> Unit,
    pulseHighText: String,
    onPulseHighClick: () -> Unit,
    pulseLowText: String,
    onPulseLowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_health_thresholds))
        ClickablePreference(
            title = stringResource(R.string.settings_temperature_high),
            summary = temperatureText,
            onClick = onTemperatureClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_bp_upper),
            summary = bpUpperText,
            onClick = onBpUpperClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_bp_lower),
            summary = bpLowerText,
            onClick = onBpLowerClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_pulse_high),
            summary = pulseHighText,
            onClick = onPulseHighClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_pulse_low),
            summary = pulseLowText,
            onClick = onPulseLowClick
        )
    }
}
