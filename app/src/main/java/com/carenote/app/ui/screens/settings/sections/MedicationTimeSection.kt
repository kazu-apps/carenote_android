package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun MedicationTimeSection(
    morningTimeText: String,
    onMorningClick: () -> Unit,
    noonTimeText: String,
    onNoonClick: () -> Unit,
    eveningTimeText: String,
    onEveningClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_medication_times))
        ClickablePreference(
            title = stringResource(R.string.medication_morning),
            summary = morningTimeText,
            onClick = onMorningClick
        )
        ClickablePreference(
            title = stringResource(R.string.medication_noon),
            summary = noonTimeText,
            onClick = onNoonClick
        )
        ClickablePreference(
            title = stringResource(R.string.medication_evening),
            summary = eveningTimeText,
            onClick = onEveningClick
        )
    }
}
