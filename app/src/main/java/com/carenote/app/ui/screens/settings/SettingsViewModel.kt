package com.carenote.app.ui.screens.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.data.worker.SyncWorkSchedulerInterface
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.ui.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncWorkScheduler: SyncWorkSchedulerInterface
) : ViewModel() {

    val snackbarController = SnackbarController()

    val settings: StateFlow<UserSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = UserSettings()
        )

    /** 現在ログイン中かどうか */
    val isLoggedIn: StateFlow<Boolean> = authRepository.currentUser
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = authRepository.getCurrentUser() != null
        )

    /** 同期中かどうか */
    val isSyncing: StateFlow<Boolean> = combine(
        syncWorkScheduler.getSyncWorkInfo().asFlow(),
        syncWorkScheduler.getImmediateSyncWorkInfo().asFlow()
    ) { periodicWorkInfos, immediateWorkInfos ->
        val allWorkInfos = periodicWorkInfos + immediateWorkInfos
        allWorkInfos.any { it.state == WorkInfo.State.RUNNING }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
        initialValue = false
    )

    private fun updateSetting(
        logTag: String,
        @StringRes successMessageResId: Int = R.string.settings_saved,
        @StringRes failureMessageResId: Int = R.string.settings_error_save_failed,
        onSuccess: (() -> Unit)? = null,
        action: suspend () -> Result<Unit, DomainError>
    ) {
        viewModelScope.launch {
            action()
                .onSuccess {
                    Timber.d(logTag)
                    onSuccess?.invoke()
                    snackbarController.showMessage(successMessageResId)
                }
                .onFailure { error ->
                    Timber.w("Failed: $logTag: $error")
                    snackbarController.showMessage(failureMessageResId)
                }
        }
    }

    fun toggleNotifications(enabled: Boolean) = updateSetting(
        logTag = "Notifications updated: $enabled"
    ) { settingsRepository.updateNotifications(enabled) }

    fun updateQuietHours(start: Int, end: Int) = updateSetting(
        logTag = "Quiet hours updated: $start - $end",
        failureMessageResId = R.string.settings_error_validation
    ) { settingsRepository.updateQuietHours(start, end) }

    fun updateTemperatureThreshold(value: Double) = updateSetting(
        logTag = "Temperature threshold updated: $value",
        failureMessageResId = R.string.settings_error_validation
    ) { settingsRepository.updateTemperatureThreshold(value) }

    fun updateBloodPressureThresholds(upper: Int, lower: Int) = updateSetting(
        logTag = "Blood pressure thresholds updated: $upper / $lower",
        failureMessageResId = R.string.settings_error_validation
    ) { settingsRepository.updateBloodPressureThresholds(upper, lower) }

    fun updatePulseThresholds(high: Int, low: Int) = updateSetting(
        logTag = "Pulse thresholds updated: $high / $low",
        failureMessageResId = R.string.settings_error_validation
    ) { settingsRepository.updatePulseThresholds(high, low) }

    fun updateMedicationTime(timing: MedicationTiming, hour: Int, minute: Int) = updateSetting(
        logTag = "Medication time updated ($timing): $hour:$minute",
        failureMessageResId = R.string.settings_error_validation
    ) { settingsRepository.updateMedicationTime(timing, hour, minute) }

    fun updateThemeMode(mode: ThemeMode) = updateSetting(
        logTag = "Theme mode updated: $mode"
    ) { settingsRepository.updateThemeMode(mode) }

    fun toggleSyncEnabled(enabled: Boolean) = updateSetting(
        logTag = "Sync enabled updated: $enabled",
        onSuccess = {
            if (enabled && isLoggedIn.value) {
                syncWorkScheduler.schedulePeriodicSync()
            } else if (!enabled) {
                syncWorkScheduler.cancelAllSyncWork()
            }
        }
    ) { settingsRepository.updateSyncEnabled(enabled) }

    fun resetToDefaults() = updateSetting(
        logTag = "Settings reset to defaults",
        successMessageResId = R.string.settings_reset_done
    ) { settingsRepository.resetToDefaults() }

    fun triggerManualSync() {
        if (!isLoggedIn.value) {
            Timber.w("Cannot trigger sync: not logged in")
            return
        }
        syncWorkScheduler.triggerImmediateSync()
        viewModelScope.launch {
            snackbarController.showMessage(R.string.settings_sync_started)
        }
    }
}
