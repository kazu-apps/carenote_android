package com.carenote.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.BuildConfig
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.screens.settings.dialogs.SettingsDialogs
import com.carenote.app.ui.screens.settings.sections.AppInfoSection
import com.carenote.app.ui.screens.settings.sections.HealthThresholdSection
import com.carenote.app.ui.screens.settings.sections.LanguageSection
import com.carenote.app.ui.screens.settings.sections.MedicationTimeSection
import com.carenote.app.ui.screens.settings.sections.NotificationSection
import com.carenote.app.ui.screens.settings.sections.AccountSection
import com.carenote.app.ui.screens.settings.sections.CareRecipientSection
import com.carenote.app.ui.screens.settings.sections.EmergencyContactSection
import com.carenote.app.ui.screens.settings.sections.SecuritySection
import com.carenote.app.ui.screens.settings.sections.DataExportSection
import com.carenote.app.ui.screens.settings.sections.SyncSection
import com.carenote.app.ui.util.BiometricHelper
import com.carenote.app.ui.util.RootDetector
import com.carenote.app.ui.screens.settings.sections.ThemeSection
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.ExportState
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    onNavigateToCareRecipient: () -> Unit = {},
    onNavigateToEmergencyContacts: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val careRecipientName by viewModel.careRecipientName.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var dialogState by remember { mutableStateOf<SettingsDialogState>(SettingsDialogState.None) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId -> context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(exportState) {
        val state = exportState
        if (state is ExportState.Success) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = state.mimeType
                putExtra(Intent.EXTRA_STREAM, state.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, null))
            viewModel.resetExportState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item(key = "care_recipient") {
                CareRecipientSection(
                    careRecipientName = careRecipientName,
                    onProfileClick = onNavigateToCareRecipient
                )
            }
            item(key = "emergency_contact") {
                EmergencyContactSection(
                    onEmergencyContactClick = onNavigateToEmergencyContacts
                )
            }
            item(key = "theme") {
                val isDynamicColorAvailable = remember {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                }
                ThemeSection(
                    themeMode = settings.themeMode,
                    onThemeModeSelected = { viewModel.updateThemeMode(it) },
                    useDynamicColor = settings.useDynamicColor,
                    onDynamicColorChange = { viewModel.toggleDynamicColor(it) },
                    isDynamicColorAvailable = isDynamicColorAvailable
                )
            }
            item(key = "language") {
                LanguageSection(
                    appLanguage = settings.appLanguage,
                    onLanguageSelected = { viewModel.updateAppLanguage(it) }
                )
            }
            item(key = "sync") {
                val dateTimeFormatter = remember {
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                }
                val lastSyncText = settings.lastSyncTime?.let { time ->
                    dateTimeFormatter.format(time)
                } ?: stringResource(R.string.settings_last_sync_never)
                SyncSection(
                    syncEnabled = settings.syncEnabled,
                    onSyncEnabledChange = { viewModel.toggleSyncEnabled(it) },
                    isSyncing = isSyncing,
                    isLoggedIn = isLoggedIn,
                    lastSyncText = lastSyncText,
                    onSyncNowClick = { viewModel.triggerManualSync() }
                )
            }
            item(key = "security") {
                val isBiometricAvailable = remember {
                    BiometricHelper().canAuthenticate(context)
                }
                val isDeviceRooted = remember {
                    RootDetector().isDeviceRooted()
                }
                SecuritySection(
                    biometricEnabled = settings.biometricEnabled,
                    onBiometricEnabledChange = { viewModel.toggleBiometricEnabled(it) },
                    isBiometricAvailable = isBiometricAvailable,
                    isDeviceRooted = isDeviceRooted
                )
            }
            item(key = "account") {
                AccountSection(
                    isLoggedIn = isLoggedIn,
                    currentUser = currentUser,
                    onChangePasswordClick = {
                        dialogState = SettingsDialogState.ChangePassword
                    },
                    onSendEmailVerificationClick = {
                        viewModel.sendEmailVerification()
                    },
                    onSignOutClick = {
                        dialogState = SettingsDialogState.SignOutConfirm
                    },
                    onDeleteAccountClick = {
                        dialogState = SettingsDialogState.DeleteAccountConfirm
                    }
                )
            }
            item(key = "notification") {
                NotificationSection(
                    notificationsEnabled = settings.notificationsEnabled,
                    onNotificationsEnabledChange = { viewModel.toggleNotifications(it) },
                    quietHoursText = stringResource(
                        R.string.settings_quiet_hours_format,
                        settings.quietHoursStart,
                        settings.quietHoursEnd
                    ),
                    onQuietHoursClick = { dialogState = SettingsDialogState.QuietHoursStart }
                )
            }
            item(key = "health_threshold") {
                HealthThresholdSection(
                    temperatureText = stringResource(
                        R.string.settings_temperature_format,
                        settings.temperatureHigh
                    ),
                    onTemperatureClick = { dialogState = SettingsDialogState.Temperature },
                    bpUpperText = stringResource(
                        R.string.settings_bp_upper_format,
                        settings.bloodPressureHighUpper
                    ),
                    onBpUpperClick = { dialogState = SettingsDialogState.BpUpper },
                    bpLowerText = stringResource(
                        R.string.settings_bp_lower_format,
                        settings.bloodPressureHighLower
                    ),
                    onBpLowerClick = { dialogState = SettingsDialogState.BpLower },
                    pulseHighText = stringResource(
                        R.string.settings_pulse_high_format,
                        settings.pulseHigh
                    ),
                    onPulseHighClick = { dialogState = SettingsDialogState.PulseHigh },
                    pulseLowText = stringResource(
                        R.string.settings_pulse_low_format,
                        settings.pulseLow
                    ),
                    onPulseLowClick = { dialogState = SettingsDialogState.PulseLow }
                )
            }
            item(key = "medication_time") {
                MedicationTimeSection(
                    morningTimeText = stringResource(
                        R.string.settings_time_format,
                        settings.morningHour,
                        settings.morningMinute
                    ),
                    onMorningClick = { dialogState = SettingsDialogState.MorningTime },
                    noonTimeText = stringResource(
                        R.string.settings_time_format,
                        settings.noonHour,
                        settings.noonMinute
                    ),
                    onNoonClick = { dialogState = SettingsDialogState.NoonTime },
                    eveningTimeText = stringResource(
                        R.string.settings_time_format,
                        settings.eveningHour,
                        settings.eveningMinute
                    ),
                    onEveningClick = { dialogState = SettingsDialogState.EveningTime }
                )
            }
            item(key = "data_export") {
                DataExportSection(
                    onExportTasksClick = { dialogState = SettingsDialogState.DataExportTasks },
                    onExportNotesClick = { dialogState = SettingsDialogState.DataExportNotes }
                )
            }
            item(key = "app_info") {
                AppInfoSection(
                    versionName = BuildConfig.VERSION_NAME,
                    onPrivacyPolicyClick = onNavigateToPrivacyPolicy,
                    onTermsOfServiceClick = onNavigateToTermsOfService,
                    onContactClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${AppConfig.Support.CONTACT_EMAIL}")
                        }
                        context.startActivity(intent)
                    },
                    onResetClick = { dialogState = SettingsDialogState.ResetConfirm }
                )
            }
        }
    }

    SettingsDialogs(
        dialogState = dialogState,
        settings = settings,
        onDismiss = { dialogState = SettingsDialogState.None },
        onDialogStateChange = { dialogState = it },
        viewModel = viewModel
    )
}
