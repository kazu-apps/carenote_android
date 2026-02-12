package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun AppInfoSection(
    versionName: String,
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit,
    onContactClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_app_info))
        ClickablePreference(
            title = stringResource(R.string.settings_version),
            summary = versionName,
            onClick = {}
        )
        ClickablePreference(
            title = stringResource(R.string.settings_contact_title),
            summary = stringResource(R.string.settings_contact_summary),
            onClick = onContactClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_privacy_policy),
            summary = stringResource(R.string.settings_privacy_policy_summary),
            onClick = onPrivacyPolicyClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_terms_of_service),
            summary = stringResource(R.string.settings_terms_of_service_summary),
            onClick = onTermsOfServiceClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_reset_to_defaults),
            summary = "",
            onClick = onResetClick
        )
    }
}
