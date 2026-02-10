package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun EmergencyContactSection(
    onEmergencyContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.emergency_contact_section_title))
        ClickablePreference(
            title = stringResource(R.string.emergency_contact_title),
            summary = stringResource(R.string.emergency_contact_section_summary),
            onClick = onEmergencyContactClick
        )
    }
}
