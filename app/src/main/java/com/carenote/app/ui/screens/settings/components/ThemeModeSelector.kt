package com.carenote.app.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.domain.model.ThemeMode

@Composable
fun ThemeModeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentMode == mode,
                    onClick = { onModeSelected(mode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = themeModeLabel(mode),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun themeModeLabel(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
    }
}
