package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.ui.screens.settings.components.SettingsSection
import com.carenote.app.ui.screens.settings.components.SwitchPreference
import com.carenote.app.ui.screens.settings.components.ThemeModeSelector

@Composable
fun ThemeSection(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    useDynamicColor: Boolean = false,
    onDynamicColorChange: (Boolean) -> Unit = {},
    isDynamicColorAvailable: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_theme))
        ThemeModeSelector(
            currentMode = themeMode,
            onModeSelected = onThemeModeSelected
        )
        SwitchPreference(
            title = stringResource(R.string.settings_dynamic_color_title),
            checked = useDynamicColor,
            onCheckedChange = onDynamicColorChange,
            summary = if (isDynamicColorAvailable) {
                stringResource(R.string.settings_dynamic_color_summary)
            } else {
                stringResource(R.string.settings_dynamic_color_unavailable)
            },
            enabled = isDynamicColorAvailable
        )
    }
}
