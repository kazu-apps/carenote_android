package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun DataExportSection(
    onExportTasksClick: () -> Unit,
    onExportNotesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_data_export))
        ClickablePreference(
            title = stringResource(R.string.settings_data_export_tasks),
            summary = stringResource(R.string.settings_data_export_tasks_summary),
            onClick = onExportTasksClick
        )
        ClickablePreference(
            title = stringResource(R.string.settings_data_export_notes),
            summary = stringResource(R.string.settings_data_export_notes_summary),
            onClick = onExportNotesClick
        )
    }
}

@LightDarkPreview
@Composable
private fun DataExportSectionPreview() {
    CareNoteTheme {
        DataExportSection(
            onExportTasksClick = {},
            onExportNotesClick = {}
        )
    }
}
