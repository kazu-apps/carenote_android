package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.domain.model.User
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun AccountSection(
    isLoggedIn: Boolean,
    currentUser: User?,
    onChangePasswordClick: () -> Unit,
    onSendEmailVerificationClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isLoggedIn) return

    val isEmailVerified = currentUser?.isEmailVerified ?: false

    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_account))
        ClickablePreference(
            title = stringResource(R.string.settings_change_password),
            summary = stringResource(R.string.settings_change_password_summary),
            onClick = onChangePasswordClick
        )
        if (!isEmailVerified) {
            ClickablePreference(
                title = stringResource(R.string.settings_send_email_verification),
                summary = stringResource(R.string.settings_email_verification_summary),
                onClick = onSendEmailVerificationClick
            )
        }
        ClickablePreference(
            title = stringResource(R.string.settings_sign_out),
            summary = stringResource(R.string.settings_sign_out_summary),
            onClick = onSignOutClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_delete_account),
            summary = stringResource(R.string.settings_delete_account_summary),
            onClick = onDeleteAccountClick
        )
    }
}
