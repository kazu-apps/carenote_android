package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun MemberManagementSection(
    onMemberManagementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.member_management_section_title))
        ClickablePreference(
            title = stringResource(R.string.member_management_title),
            summary = stringResource(R.string.member_management_section_summary),
            onClick = onMemberManagementClick
        )
    }
}
