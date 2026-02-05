package com.carenote.app.ui.screens.settings.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.carenote.app.R
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.ui.screens.settings.components.LanguageSelector
import com.carenote.app.ui.screens.settings.components.SettingsSection

@Composable
fun LanguageSection(
    appLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSection(title = stringResource(R.string.settings_language))
        LanguageSelector(
            currentLanguage = appLanguage,
            onLanguageSelected = onLanguageSelected
        )
    }
}
