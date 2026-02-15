package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection
import com.carenote.app.ui.screens.settings.components.SwitchPreference

@Composable
fun NotificationSection(
    notificationsEnabled: Boolean,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    quietHoursText: String,
    onQuietHoursClick: () -> Unit,
    taskReminderLimitText: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_notifications))
        SwitchPreference(
            title = stringResource(R.string.settings_notifications_enabled),
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsEnabledChange
        )
        ClickablePreference(
            title = stringResource(R.string.settings_quiet_hours),
            summary = quietHoursText,
            onClick = onQuietHoursClick
        )
        if (taskReminderLimitText != null) {
            ClickablePreference(
                title = stringResource(R.string.settings_task_reminder_limit),
                summary = taskReminderLimitText,
                onClick = {}
            )
        }
    }
}
