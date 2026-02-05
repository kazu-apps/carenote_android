package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection
import com.carenote.app.ui.screens.settings.components.SwitchPreference

@Composable
fun SyncSection(
    syncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    isSyncing: Boolean,
    isLoggedIn: Boolean,
    lastSyncText: String,
    onSyncNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_sync))
        SwitchPreference(
            title = stringResource(R.string.settings_sync_enabled),
            summary = stringResource(R.string.settings_sync_enabled_summary),
            checked = syncEnabled,
            onCheckedChange = onSyncEnabledChange
        )
        ClickablePreference(
            title = stringResource(R.string.settings_sync_now),
            summary = if (isSyncing) {
                stringResource(R.string.settings_sync_in_progress)
            } else {
                stringResource(R.string.settings_sync_now_summary)
            },
            onClick = onSyncNowClick,
            enabled = isLoggedIn && syncEnabled && !isSyncing,
            trailingContent = if (isSyncing) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                null
            }
        )
        ClickablePreference(
            title = stringResource(R.string.settings_last_sync),
            summary = lastSyncText,
            onClick = {}
        )
    }
}
