package com.carenote.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val snackbarController = SnackbarController()

    val settings: StateFlow<UserSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = UserSettings()
        )

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotifications(enabled)
                .onSuccess {
                    Timber.d("Notifications updated: $enabled")
                    snackbarController.showMessage(R.string.settings_saved)
                }
                .onFailure { error ->
                    Timber.w("Failed to update notifications: $error")
                    snackbarController.showMessage(R.string.settings_error_save_failed)
                }
        }
    }

    fun updateQuietHours(start: Int, end: Int) {
        viewModelScope.launch {
            settingsRepository.updateQuietHours(start, end)
                .onSuccess {
                    Timber.d("Quiet hours updated: $start - $end")
                    snackbarController.showMessage(R.string.settings_saved)
                }
                .onFailure { error ->
                    Timber.w("Failed to update quiet hours: $error")
                    snackbarController.showMessage(R.string.settings_error_validation)
                }
        }
    }

    fun updateTemperatureThreshold(value: Double) {
        viewModelScope.launch {
            settingsRepository.updateTemperatureThreshold(value)
                .onSuccess {
                    Timber.d("Temperature threshold updated: $value")
                    snackbarController.showMessage(R.string.settings_saved)
                }
                .onFailure { error ->
                    Timber.w("Failed to update temperature: $error")
                    snackbarController.showMessage(R.string.settings_error_validation)
                }
        }
    }

    fun updateBloodPressureThresholds(upper: Int, lower: Int) {
        viewModelScope.launch {
            settingsRepository.updateBloodPressureThresholds(upper, lower)
                .onSuccess {
                    Timber.d("Blood pressure thresholds updated: $upper / $lower")
                    snackbarController.showMessage(R.string.settings_saved)
                }
                .onFailure { error ->
                    Timber.w("Failed to update blood pressure: $error")
                    snackbarController.showMessage(R.string.settings_error_validation)
                }
        }
    }

    fun updatePulseThresholds(high: Int, low: Int) {
        viewModelScope.launch {
            settingsRepository.updatePulseThresholds(high, low)
                .onSuccess {
                    Timber.d("Pulse thresholds updated: $high / $low")
                    snackbarController.showMessage(R.string.settings_saved)
                }
                .onFailure { error ->
                    Timber.w("Failed to update pulse: $error")
                    snackbarController.showMessage(R.string.settings_error_validation)
                }
        }
    }

    fun updateMedicationTime(timing: MedicationTiming, hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.updateMedicationTime(timing, hour, minute)
                .onSuccess {
                    Timber.d("Medication time updated ($timing): $hour:$minute")
                    snackbarController.showMessage(R.string.settings_saved)
                }
                .onFailure { error ->
                    Timber.w("Failed to update medication time: $error")
                    snackbarController.showMessage(R.string.settings_error_validation)
                }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
                .onSuccess {
                    Timber.d("Theme mode updated: $mode")
                    snackbarController.showMessage(R.string.settings_saved)
                }
                .onFailure { error ->
                    Timber.w("Failed to update theme mode: $error")
                    snackbarController.showMessage(R.string.settings_error_save_failed)
                }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
                .onSuccess {
                    Timber.d("Settings reset to defaults")
                    snackbarController.showMessage(R.string.settings_reset_done)
                }
                .onFailure { error ->
                    Timber.w("Failed to reset settings: $error")
                    snackbarController.showMessage(R.string.settings_error_save_failed)
                }
        }
    }

}
