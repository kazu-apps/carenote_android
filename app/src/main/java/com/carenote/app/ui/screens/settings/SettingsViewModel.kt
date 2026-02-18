package com.carenote.app.ui.screens.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.repository.SyncWorkSchedulerInterface
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.Result
import com.carenote.app.domain.model.AppLanguage
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.User
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.CareRecipientRepository
import com.carenote.app.domain.repository.NoteRepository
import com.carenote.app.domain.repository.NoteCsvExporterInterface
import com.carenote.app.domain.repository.NotePdfExporterInterface
import com.carenote.app.domain.repository.BillingRepository
import com.carenote.app.domain.repository.PremiumFeatureGuard
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.TaskCsvExporterInterface
import com.carenote.app.domain.repository.TaskPdfExporterInterface
import com.carenote.app.ui.util.LocaleManager
import com.carenote.app.ui.util.RootDetectionChecker
import com.carenote.app.ui.util.SnackbarController
import com.carenote.app.ui.viewmodel.ExportState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import timber.log.Timber
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
class SettingsViewModel @Suppress("LongParameterList") @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncWorkScheduler: SyncWorkSchedulerInterface,
    private val analyticsRepository: AnalyticsRepository,
    private val careRecipientRepository: CareRecipientRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val noteRepository: NoteRepository,
    private val taskCsvExporter: TaskCsvExporterInterface,
    private val taskPdfExporter: TaskPdfExporterInterface,
    private val noteCsvExporter: NoteCsvExporterInterface,
    private val notePdfExporter: NotePdfExporterInterface,
    private val rootDetector: RootDetectionChecker,
    private val billingRepository: BillingRepository,
    private val premiumFeatureGuard: PremiumFeatureGuard
) : ViewModel() {

    val snackbarController = SnackbarController()

    val settings: StateFlow<UserSettings> = settingsRepository.getSettings()
        .catch { e ->
            Timber.w("Failed to observe settings: $e")
            emit(UserSettings())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = UserSettings()
        )

    /** 現在ログイン中かどうか */
    val isLoggedIn: StateFlow<Boolean> = authRepository.currentUser
        .map { it != null }
        .catch { e ->
            Timber.w("Failed to observe auth state: $e")
            emit(false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = authRepository.getCurrentUser() != null
        )

    /** 現在のユーザー（メール確認状態の判定用） */
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .catch { e ->
            Timber.w("Failed to observe current user: $e")
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = authRepository.getCurrentUser()
        )

    /** 同期中かどうか */
    val isSyncing: StateFlow<Boolean> = combine(
        syncWorkScheduler.getSyncWorkInfo().asFlow(),
        syncWorkScheduler.getImmediateSyncWorkInfo().asFlow()
    ) { periodicWorkInfos, immediateWorkInfos ->
        val allWorkInfos = periodicWorkInfos + immediateWorkInfos
        allWorkInfos.any { it.state == WorkInfo.State.RUNNING }
    }.catch { e ->
        Timber.w("Failed to observe sync state: $e")
        emit(false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
        initialValue = false
    )

    val careRecipientName: StateFlow<String?> = careRecipientRepository.getCareRecipient()
        .map { it?.name }
        .catch { e ->
            Timber.w("Failed to observe care recipient: $e")
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = null
        )

    val taskReminderLimitText: StateFlow<String?> = billingRepository.premiumStatus
        .map { status ->
            if (status.isActive) null
            else {
                val count = premiumFeatureGuard.getTaskReminderCountToday()
                val limit = premiumFeatureGuard.getTaskReminderDailyLimit()
                val remaining = (limit - count).coerceAtLeast(0)
                "$remaining/$limit"
            }
        }
        .catch { e ->
            Timber.w("Failed to observe premium status: $e")
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConfig.UI.FLOW_STOP_TIMEOUT_MS),
            initialValue = null
        )

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

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

    fun updateAppLanguage(language: AppLanguage) = updateSetting(
        logTag = "App language updated: $language",
        onSuccess = { LocaleManager.applyLanguage(language) }
    ) { settingsRepository.updateAppLanguage(language) }

    fun toggleBiometricEnabled(enabled: Boolean) = updateSetting(
        logTag = "Biometric enabled updated: $enabled"
    ) { settingsRepository.updateBiometricEnabled(enabled) }

    fun updateSessionTimeout(minutes: Int) = updateSetting(
        logTag = "Session timeout updated: $minutes minutes",
        failureMessageResId = R.string.settings_error_validation
    ) { settingsRepository.updateSessionTimeout(minutes) }

    fun toggleDynamicColor(enabled: Boolean) = updateSetting(
        logTag = "Dynamic color updated: $enabled"
    ) { settingsRepository.updateDynamicColor(enabled) }

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
        analyticsRepository.logEvent(AppConfig.Analytics.EVENT_MANUAL_SYNC)
        syncWorkScheduler.triggerImmediateSync()
        viewModelScope.launch {
            snackbarController.showMessage(R.string.settings_sync_started)
        }
    }

    fun exportTasksCsv(periodDays: Long? = null) {
        if (rootDetector.isDeviceRooted()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.security_root_export_blocked)
            }
            return
        }
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val events = calendarEventRepository.getTaskEvents().first()
                val filtered = filterByPeriod(events, periodDays) { it.createdAt }
                if (filtered.isEmpty()) {
                    snackbarController.showMessage(R.string.task_export_empty)
                    _exportState.value = ExportState.Idle
                    return@launch
                }
                val uri = taskCsvExporter.export(filtered)
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_TASK_EXPORT_CSV)
                _exportState.value = ExportState.Success(uri, "text/csv")
            } catch (e: Exception) {
                Timber.w("Task CSV export failed: $e")
                snackbarController.showMessage(R.string.export_failed)
                _exportState.value = ExportState.Error(e.message ?: "")
            }
        }
    }

    fun exportTasksPdf(periodDays: Long? = null) {
        if (rootDetector.isDeviceRooted()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.security_root_export_blocked)
            }
            return
        }
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val events = calendarEventRepository.getTaskEvents().first()
                val filtered = filterByPeriod(events, periodDays) { it.createdAt }
                if (filtered.isEmpty()) {
                    snackbarController.showMessage(R.string.task_export_empty)
                    _exportState.value = ExportState.Idle
                    return@launch
                }
                val uri = taskPdfExporter.export(filtered)
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_TASK_EXPORT_PDF)
                _exportState.value = ExportState.Success(uri, "application/pdf")
            } catch (e: Exception) {
                Timber.w("Task PDF export failed: $e")
                snackbarController.showMessage(R.string.export_failed)
                _exportState.value = ExportState.Error(e.message ?: "")
            }
        }
    }

    fun exportNotesCsv(periodDays: Long? = null) {
        if (rootDetector.isDeviceRooted()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.security_root_export_blocked)
            }
            return
        }
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val notes = noteRepository.getAllNotes().first()
                val filtered = filterByPeriod(notes, periodDays) { it.createdAt }
                if (filtered.isEmpty()) {
                    snackbarController.showMessage(R.string.note_export_empty)
                    _exportState.value = ExportState.Idle
                    return@launch
                }
                val uri = noteCsvExporter.export(filtered)
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_NOTE_EXPORT_CSV)
                _exportState.value = ExportState.Success(uri, "text/csv")
            } catch (e: Exception) {
                Timber.w("Note CSV export failed: $e")
                snackbarController.showMessage(R.string.export_failed)
                _exportState.value = ExportState.Error(e.message ?: "")
            }
        }
    }

    fun exportNotesPdf(periodDays: Long? = null) {
        if (rootDetector.isDeviceRooted()) {
            viewModelScope.launch {
                snackbarController.showMessage(R.string.security_root_export_blocked)
            }
            return
        }
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val notes = noteRepository.getAllNotes().first()
                val filtered = filterByPeriod(notes, periodDays) { it.createdAt }
                if (filtered.isEmpty()) {
                    snackbarController.showMessage(R.string.note_export_empty)
                    _exportState.value = ExportState.Idle
                    return@launch
                }
                val uri = notePdfExporter.export(filtered)
                analyticsRepository.logEvent(AppConfig.Analytics.EVENT_NOTE_EXPORT_PDF)
                _exportState.value = ExportState.Success(uri, "application/pdf")
            } catch (e: Exception) {
                Timber.w("Note PDF export failed: $e")
                snackbarController.showMessage(R.string.export_failed)
                _exportState.value = ExportState.Error(e.message ?: "")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    private fun <T> filterByPeriod(
        items: List<T>,
        periodDays: Long?,
        getCreatedAt: (T) -> LocalDateTime
    ): List<T> {
        if (periodDays == null) return items
        val cutoff = LocalDateTime.now().minusDays(periodDays)
        return items.filter { getCreatedAt(it) >= cutoff }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
                .onSuccess {
                    Timber.d("User signed out")
                    analyticsRepository.logEvent(AppConfig.Analytics.EVENT_SIGN_OUT)
                    snackbarController.showMessage(R.string.settings_signed_out)
                }
                .onFailure { error ->
                    Timber.w("Sign out failed: $error")
                    snackbarController.showMessage(R.string.settings_sign_out_failed)
                }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            authRepository.reauthenticate(currentPassword)
                .onSuccess {
                    authRepository.updatePassword(newPassword)
                        .onSuccess {
                            Timber.d("Password changed")
                            analyticsRepository.logEvent(AppConfig.Analytics.EVENT_PASSWORD_CHANGED)
                            snackbarController.showMessage(R.string.settings_password_changed)
                        }
                        .onFailure { error ->
                            Timber.w("Password update failed: $error")
                            snackbarController.showMessage(R.string.settings_password_change_failed)
                        }
                }
                .onFailure { error ->
                    Timber.w("Reauthentication failed for password change: $error")
                    snackbarController.showMessage(R.string.settings_reauthenticate_failed)
                }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            authRepository.reauthenticate(password)
                .onSuccess {
                    authRepository.deleteAccount()
                        .onSuccess {
                            Timber.d("Account deleted")
                            analyticsRepository.logEvent(AppConfig.Analytics.EVENT_ACCOUNT_DELETED)
                            snackbarController.showMessage(R.string.settings_account_deleted)
                        }
                        .onFailure { error ->
                            Timber.w("Account deletion failed: $error")
                            snackbarController.showMessage(R.string.settings_account_delete_failed)
                        }
                }
                .onFailure { error ->
                    Timber.w("Reauthentication failed for account deletion: $error")
                    snackbarController.showMessage(R.string.settings_reauthenticate_failed)
                }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            authRepository.sendEmailVerification()
                .onSuccess {
                    Timber.d("Email verification sent")
                    snackbarController.showMessage(R.string.settings_email_verification_sent)
                }
                .onFailure { error ->
                    Timber.w("Email verification failed: $error")
                    snackbarController.showMessage(R.string.settings_email_verification_failed)
                }
        }
    }
}
