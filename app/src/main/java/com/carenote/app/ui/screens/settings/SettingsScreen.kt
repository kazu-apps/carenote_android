package com.carenote.app.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import com.carenote.app.ui.screens.settings.sections.MemberManagementSection
import com.carenote.app.ui.screens.settings.sections.SecuritySection
import com.carenote.app.ui.screens.settings.sections.DataExportSection
import com.carenote.app.ui.screens.settings.sections.PremiumSection
import com.carenote.app.ui.screens.settings.sections.SyncSection
import com.carenote.app.ui.util.BiometricHelper
import com.carenote.app.ui.util.RootDetector
import com.carenote.app.ui.screens.settings.sections.ThemeSection
import com.carenote.app.domain.model.User
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.BillingUiState
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
    onNavigateToMemberManagement: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val careRecipientName by viewModel.careRecipientName.collectAsStateWithLifecycle()
    val taskReminderLimitText by viewModel.taskReminderLimitText.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val billingUiState by viewModel.billingUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<SettingsDialogState>(SettingsDialogState.None) }

    LaunchedEffect(Unit) {
        viewModel.connectBilling()
        viewModel.loadProducts()
    }

    SettingsEffects(viewModel, snackbarHostState, exportState)
    SettingsScaffold(
        settings = settings,
        isLoggedIn = isLoggedIn,
        currentUser = currentUser,
        isSyncing = isSyncing,
        careRecipientName = careRecipientName,
        taskReminderLimitText = taskReminderLimitText,
        billingUiState = billingUiState,
        snackbarHostState = snackbarHostState,
        dialogState = dialogState,
        onDialogStateChange = { dialogState = it },
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateToTermsOfService = onNavigateToTermsOfService,
        onNavigateToCareRecipient = onNavigateToCareRecipient,
        onNavigateToEmergencyContacts = onNavigateToEmergencyContacts,
        onNavigateToMemberManagement = onNavigateToMemberManagement,
        viewModel = viewModel
    )
}

@Composable
private fun SettingsEffects(
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState,
    exportState: ExportState
) {
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
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScaffold(
    settings: UserSettings,
    isLoggedIn: Boolean,
    currentUser: User?,
    isSyncing: Boolean,
    careRecipientName: String?,
    taskReminderLimitText: String?,
    billingUiState: BillingUiState,
    snackbarHostState: SnackbarHostState,
    dialogState: SettingsDialogState,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    onNavigateToCareRecipient: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit,
    onNavigateToMemberManagement: () -> Unit,
    viewModel: SettingsViewModel
) {
    Scaffold(
        topBar = { SettingsTopBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        SettingsContent(
            settings = settings,
            isLoggedIn = isLoggedIn,
            currentUser = currentUser,
            isSyncing = isSyncing,
            careRecipientName = careRecipientName,
            taskReminderLimitText = taskReminderLimitText,
            billingUiState = billingUiState,
            onDialogStateChange = onDialogStateChange,
            onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
            onNavigateToTermsOfService = onNavigateToTermsOfService,
            onNavigateToCareRecipient = onNavigateToCareRecipient,
            onNavigateToEmergencyContacts = onNavigateToEmergencyContacts,
            onNavigateToMemberManagement = onNavigateToMemberManagement,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
    SettingsDialogs(
        dialogState = dialogState,
        settings = settings,
        onDismiss = { onDialogStateChange(SettingsDialogState.None) },
        onDialogStateChange = onDialogStateChange,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar() {
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
}

@Suppress("LongParameterList")
@Composable
private fun SettingsContent(
    settings: UserSettings,
    isLoggedIn: Boolean,
    currentUser: User?,
    isSyncing: Boolean,
    careRecipientName: String?,
    taskReminderLimitText: String?,
    billingUiState: BillingUiState,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    onNavigateToCareRecipient: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit,
    onNavigateToMemberManagement: () -> Unit,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        settingsNavigationItems(
            careRecipientName, onNavigateToCareRecipient,
            onNavigateToEmergencyContacts, onNavigateToMemberManagement
        )
        settingsPreferenceItems(
            settings, isLoggedIn, currentUser, isSyncing,
            taskReminderLimitText, billingUiState, onDialogStateChange,
            onNavigateToPrivacyPolicy, onNavigateToTermsOfService,
            viewModel
        )
    }
}

private fun LazyListScope.settingsNavigationItems(
    careRecipientName: String?,
    onNavigateToCareRecipient: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit,
    onNavigateToMemberManagement: () -> Unit
) {
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
    item(key = "member_management") {
        MemberManagementSection(
            onMemberManagementClick = onNavigateToMemberManagement
        )
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.settingsPreferenceItems(
    settings: UserSettings,
    isLoggedIn: Boolean,
    currentUser: User?,
    isSyncing: Boolean,
    taskReminderLimitText: String?,
    billingUiState: BillingUiState,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    viewModel: SettingsViewModel
) {
    settingsAppearanceItems(settings, viewModel)
    settingsAccountAndNotificationItems(
        settings, isLoggedIn, currentUser, isSyncing,
        taskReminderLimitText, billingUiState, onDialogStateChange,
        viewModel
    )
    settingsDataAndInfoItems(
        settings, onDialogStateChange,
        onNavigateToPrivacyPolicy, onNavigateToTermsOfService
    )
}

private fun LazyListScope.settingsAppearanceItems(
    settings: UserSettings,
    viewModel: SettingsViewModel
) {
    item(key = "theme") {
        SettingsThemeItem(settings = settings, viewModel = viewModel)
    }
    item(key = "language") {
        LanguageSection(
            appLanguage = settings.appLanguage,
            onLanguageSelected = { viewModel.updateAppLanguage(it) }
        )
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.settingsAccountAndNotificationItems(
    settings: UserSettings,
    isLoggedIn: Boolean,
    currentUser: User?,
    isSyncing: Boolean,
    taskReminderLimitText: String?,
    billingUiState: BillingUiState,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    viewModel: SettingsViewModel
) {
    item(key = "sync") {
        SettingsSyncItem(
            settings = settings, isSyncing = isSyncing,
            isLoggedIn = isLoggedIn, viewModel = viewModel
        )
    }
    item(key = "security") {
        SettingsSecurityItem(settings = settings, viewModel = viewModel)
    }
    item(key = "account") {
        SettingsAccountItem(
            isLoggedIn = isLoggedIn, currentUser = currentUser,
            onDialogStateChange = onDialogStateChange,
            viewModel = viewModel
        )
    }
    item(key = "premium") {
        SettingsPremiumItem(
            billingUiState = billingUiState,
            viewModel = viewModel
        )
    }
    item(key = "notification") {
        SettingsNotificationItem(
            settings = settings,
            taskReminderLimitText = taskReminderLimitText,
            onDialogStateChange = onDialogStateChange,
            viewModel = viewModel
        )
    }
}

private fun LazyListScope.settingsDataAndInfoItems(
    settings: UserSettings,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit
) {
    item(key = "health_threshold") {
        SettingsHealthThresholdItem(
            settings = settings,
            onDialogStateChange = onDialogStateChange
        )
    }
    item(key = "medication_time") {
        SettingsMedicationTimeItem(
            settings = settings,
            onDialogStateChange = onDialogStateChange
        )
    }
    item(key = "data_export") {
        DataExportSection(
            onExportTasksClick = {
                onDialogStateChange(SettingsDialogState.DataExportTasks)
            },
            onExportNotesClick = {
                onDialogStateChange(SettingsDialogState.DataExportNotes)
            }
        )
    }
    item(key = "app_info") {
        SettingsAppInfoItem(
            onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
            onNavigateToTermsOfService = onNavigateToTermsOfService,
            onDialogStateChange = onDialogStateChange
        )
    }
}

@Composable
private fun SettingsThemeItem(
    settings: UserSettings,
    viewModel: SettingsViewModel
) {
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

@Composable
private fun SettingsSyncItem(
    settings: UserSettings,
    isSyncing: Boolean,
    isLoggedIn: Boolean,
    viewModel: SettingsViewModel
) {
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

@Composable
private fun SettingsSecurityItem(
    settings: UserSettings,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val isBiometricAvailable = remember {
        BiometricHelper().canAuthenticate(context)
    }
    val isDeviceRooted = remember { RootDetector().isDeviceRooted() }
    SecuritySection(
        biometricEnabled = settings.biometricEnabled,
        onBiometricEnabledChange = { viewModel.toggleBiometricEnabled(it) },
        isBiometricAvailable = isBiometricAvailable,
        isDeviceRooted = isDeviceRooted
    )
}

@Composable
private fun SettingsAccountItem(
    isLoggedIn: Boolean,
    currentUser: User?,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    viewModel: SettingsViewModel
) {
    AccountSection(
        isLoggedIn = isLoggedIn,
        currentUser = currentUser,
        onChangePasswordClick = {
            onDialogStateChange(SettingsDialogState.ChangePassword)
        },
        onSendEmailVerificationClick = { viewModel.sendEmailVerification() },
        onSignOutClick = {
            onDialogStateChange(SettingsDialogState.SignOutConfirm)
        },
        onDeleteAccountClick = {
            onDialogStateChange(SettingsDialogState.DeleteAccountConfirm)
        }
    )
}

@Composable
private fun SettingsNotificationItem(
    settings: UserSettings,
    taskReminderLimitText: String?,
    onDialogStateChange: (SettingsDialogState) -> Unit,
    viewModel: SettingsViewModel
) {
    NotificationSection(
        notificationsEnabled = settings.notificationsEnabled,
        onNotificationsEnabledChange = { viewModel.toggleNotifications(it) },
        quietHoursText = stringResource(
            R.string.settings_quiet_hours_format,
            settings.quietHoursStart,
            settings.quietHoursEnd
        ),
        onQuietHoursClick = {
            onDialogStateChange(SettingsDialogState.QuietHoursStart)
        },
        taskReminderLimitText = taskReminderLimitText
    )
}

@Composable
private fun SettingsHealthThresholdItem(
    settings: UserSettings,
    onDialogStateChange: (SettingsDialogState) -> Unit
) {
    HealthThresholdSection(
        temperatureText = stringResource(
            R.string.settings_temperature_format,
            settings.temperatureHigh
        ),
        onTemperatureClick = {
            onDialogStateChange(SettingsDialogState.Temperature)
        },
        bpUpperText = stringResource(
            R.string.settings_bp_upper_format,
            settings.bloodPressureHighUpper
        ),
        onBpUpperClick = {
            onDialogStateChange(SettingsDialogState.BpUpper)
        },
        bpLowerText = stringResource(
            R.string.settings_bp_lower_format,
            settings.bloodPressureHighLower
        ),
        onBpLowerClick = {
            onDialogStateChange(SettingsDialogState.BpLower)
        },
        pulseHighText = stringResource(
            R.string.settings_pulse_high_format,
            settings.pulseHigh
        ),
        onPulseHighClick = {
            onDialogStateChange(SettingsDialogState.PulseHigh)
        },
        pulseLowText = stringResource(
            R.string.settings_pulse_low_format,
            settings.pulseLow
        ),
        onPulseLowClick = {
            onDialogStateChange(SettingsDialogState.PulseLow)
        }
    )
}

@Composable
private fun SettingsMedicationTimeItem(
    settings: UserSettings,
    onDialogStateChange: (SettingsDialogState) -> Unit
) {
    MedicationTimeSection(
        morningTimeText = stringResource(
            R.string.settings_time_format,
            settings.morningHour,
            settings.morningMinute
        ),
        onMorningClick = {
            onDialogStateChange(SettingsDialogState.MorningTime)
        },
        noonTimeText = stringResource(
            R.string.settings_time_format,
            settings.noonHour,
            settings.noonMinute
        ),
        onNoonClick = {
            onDialogStateChange(SettingsDialogState.NoonTime)
        },
        eveningTimeText = stringResource(
            R.string.settings_time_format,
            settings.eveningHour,
            settings.eveningMinute
        ),
        onEveningClick = {
            onDialogStateChange(SettingsDialogState.EveningTime)
        }
    )
}

@Composable
private fun SettingsPremiumItem(
    billingUiState: BillingUiState,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    PremiumSection(
        premiumStatus = billingUiState.premiumStatus,
        connectionState = billingUiState.connectionState,
        products = billingUiState.products,
        isLoading = billingUiState.isLoading,
        onPurchaseClick = { productId ->
            (context as? Activity)?.let { activity ->
                viewModel.launchPurchase(activity, productId)
            }
        },
        onRestoreClick = { viewModel.restorePurchases() },
        onManageClick = {
            viewModel.logManageSubscription()
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(AppConfig.Billing.GOOGLE_PLAY_SUBSCRIPTION_URL)
            )
            context.startActivity(intent)
        }
    )
}

@Composable
private fun SettingsAppInfoItem(
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    onDialogStateChange: (SettingsDialogState) -> Unit
) {
    val context = LocalContext.current
    AppInfoSection(
        versionName = BuildConfig.VERSION_NAME,
        onPrivacyPolicyClick = onNavigateToPrivacyPolicy,
        onTermsOfServiceClick = onNavigateToTermsOfService,
        onContactClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(
                    "mailto:${AppConfig.Support.CONTACT_EMAIL}"
                )
            }
            context.startActivity(intent)
        },
        onResetClick = {
            onDialogStateChange(SettingsDialogState.ResetConfirm)
        }
    )
}
