package com.carenote.app.ui.screens.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.BuildConfig
import com.carenote.app.R
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.screens.settings.components.ClickablePreference
import com.carenote.app.ui.screens.settings.components.NumberInputDialog
import com.carenote.app.ui.screens.settings.components.SettingsSection
import com.carenote.app.ui.screens.settings.components.SwitchPreference
import com.carenote.app.ui.screens.settings.components.ThemeModeSelector
import com.carenote.app.ui.screens.settings.components.TimePickerDialog
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showQuietHoursStartDialog by remember { mutableStateOf(false) }
    var showQuietHoursEndDialog by remember { mutableStateOf(false) }
    var showTempDialog by remember { mutableStateOf(false) }
    var showBpUpperDialog by remember { mutableStateOf(false) }
    var showBpLowerDialog by remember { mutableStateOf(false) }
    var showPulseHighDialog by remember { mutableStateOf(false) }
    var showPulseLowDialog by remember { mutableStateOf(false) }
    var showMorningTimeDialog by remember { mutableStateOf(false) }
    var showNoonTimeDialog by remember { mutableStateOf(false) }
    var showEveningTimeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            // Section 0: Theme
            item {
                SettingsSection(title = stringResource(R.string.settings_theme))
            }
            item {
                ThemeModeSelector(
                    currentMode = settings.themeMode,
                    onModeSelected = { viewModel.updateThemeMode(it) }
                )
            }

            // Section 1: Notifications
            item {
                SettingsSection(title = stringResource(R.string.settings_notifications))
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_notifications_enabled),
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_quiet_hours),
                    summary = stringResource(
                        R.string.settings_quiet_hours_format,
                        settings.quietHoursStart,
                        settings.quietHoursEnd
                    ),
                    onClick = { showQuietHoursStartDialog = true }
                )
            }

            // Section 2: Health Thresholds
            item {
                SettingsSection(title = stringResource(R.string.settings_health_thresholds))
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_temperature_high),
                    summary = stringResource(
                        R.string.settings_temperature_format,
                        settings.temperatureHigh
                    ),
                    onClick = { showTempDialog = true }
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_bp_upper),
                    summary = stringResource(
                        R.string.settings_bp_upper_format,
                        settings.bloodPressureHighUpper
                    ),
                    onClick = { showBpUpperDialog = true }
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_bp_lower),
                    summary = stringResource(
                        R.string.settings_bp_lower_format,
                        settings.bloodPressureHighLower
                    ),
                    onClick = { showBpLowerDialog = true }
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_pulse_high),
                    summary = stringResource(
                        R.string.settings_pulse_high_format,
                        settings.pulseHigh
                    ),
                    onClick = { showPulseHighDialog = true }
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_pulse_low),
                    summary = stringResource(
                        R.string.settings_pulse_low_format,
                        settings.pulseLow
                    ),
                    onClick = { showPulseLowDialog = true }
                )
            }

            // Section 3: Medication Times
            item {
                SettingsSection(title = stringResource(R.string.settings_medication_times))
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.medication_morning),
                    summary = stringResource(
                        R.string.settings_time_format,
                        settings.morningHour,
                        settings.morningMinute
                    ),
                    onClick = { showMorningTimeDialog = true }
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.medication_noon),
                    summary = stringResource(
                        R.string.settings_time_format,
                        settings.noonHour,
                        settings.noonMinute
                    ),
                    onClick = { showNoonTimeDialog = true }
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.medication_evening),
                    summary = stringResource(
                        R.string.settings_time_format,
                        settings.eveningHour,
                        settings.eveningMinute
                    ),
                    onClick = { showEveningTimeDialog = true }
                )
            }

            // Section 4: App Info
            item {
                SettingsSection(title = stringResource(R.string.settings_app_info))
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_version),
                    summary = BuildConfig.VERSION_NAME,
                    onClick = {}
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_privacy_policy),
                    summary = stringResource(R.string.settings_privacy_policy_summary),
                    onClick = onNavigateToPrivacyPolicy
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_terms_of_service),
                    summary = stringResource(R.string.settings_terms_of_service_summary),
                    onClick = onNavigateToTermsOfService
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.settings_reset_to_defaults),
                    summary = "",
                    onClick = { showResetDialog = true }
                )
            }
        }
    }

    // Dialogs
    if (showQuietHoursStartDialog) {
        TimePickerDialog(
            title = stringResource(R.string.settings_quiet_hours),
            initialHour = settings.quietHoursStart,
            initialMinute = 0,
            onDismiss = { showQuietHoursStartDialog = false },
            onConfirm = { hour, _ ->
                showQuietHoursStartDialog = false
                showQuietHoursEndDialog = true
                viewModel.updateQuietHours(hour, settings.quietHoursEnd)
            }
        )
    }

    if (showQuietHoursEndDialog) {
        TimePickerDialog(
            title = stringResource(R.string.settings_quiet_hours),
            initialHour = settings.quietHoursEnd,
            initialMinute = 0,
            onDismiss = { showQuietHoursEndDialog = false },
            onConfirm = { hour, _ ->
                showQuietHoursEndDialog = false
                viewModel.updateQuietHours(settings.quietHoursStart, hour)
            }
        )
    }

    if (showTempDialog) {
        NumberInputDialog(
            title = stringResource(R.string.settings_temperature_high),
            currentValue = settings.temperatureHigh.toString(),
            onDismiss = { showTempDialog = false },
            onConfirm = { value ->
                showTempDialog = false
                value.toDoubleOrNull()?.let { viewModel.updateTemperatureThreshold(it) }
            },
            keyboardType = KeyboardType.Decimal
        )
    }

    if (showBpUpperDialog) {
        NumberInputDialog(
            title = stringResource(R.string.settings_bp_upper),
            currentValue = settings.bloodPressureHighUpper.toString(),
            onDismiss = { showBpUpperDialog = false },
            onConfirm = { value ->
                showBpUpperDialog = false
                value.toIntOrNull()?.let {
                    viewModel.updateBloodPressureThresholds(it, settings.bloodPressureHighLower)
                }
            }
        )
    }

    if (showBpLowerDialog) {
        NumberInputDialog(
            title = stringResource(R.string.settings_bp_lower),
            currentValue = settings.bloodPressureHighLower.toString(),
            onDismiss = { showBpLowerDialog = false },
            onConfirm = { value ->
                showBpLowerDialog = false
                value.toIntOrNull()?.let {
                    viewModel.updateBloodPressureThresholds(settings.bloodPressureHighUpper, it)
                }
            }
        )
    }

    if (showPulseHighDialog) {
        NumberInputDialog(
            title = stringResource(R.string.settings_pulse_high),
            currentValue = settings.pulseHigh.toString(),
            onDismiss = { showPulseHighDialog = false },
            onConfirm = { value ->
                showPulseHighDialog = false
                value.toIntOrNull()?.let {
                    viewModel.updatePulseThresholds(it, settings.pulseLow)
                }
            }
        )
    }

    if (showPulseLowDialog) {
        NumberInputDialog(
            title = stringResource(R.string.settings_pulse_low),
            currentValue = settings.pulseLow.toString(),
            onDismiss = { showPulseLowDialog = false },
            onConfirm = { value ->
                showPulseLowDialog = false
                value.toIntOrNull()?.let {
                    viewModel.updatePulseThresholds(settings.pulseHigh, it)
                }
            }
        )
    }

    if (showMorningTimeDialog) {
        TimePickerDialog(
            title = stringResource(R.string.medication_morning),
            initialHour = settings.morningHour,
            initialMinute = settings.morningMinute,
            onDismiss = { showMorningTimeDialog = false },
            onConfirm = { hour, minute ->
                showMorningTimeDialog = false
                viewModel.updateMedicationTime(MedicationTiming.MORNING, hour, minute)
            }
        )
    }

    if (showNoonTimeDialog) {
        TimePickerDialog(
            title = stringResource(R.string.medication_noon),
            initialHour = settings.noonHour,
            initialMinute = settings.noonMinute,
            onDismiss = { showNoonTimeDialog = false },
            onConfirm = { hour, minute ->
                showNoonTimeDialog = false
                viewModel.updateMedicationTime(MedicationTiming.NOON, hour, minute)
            }
        )
    }

    if (showEveningTimeDialog) {
        TimePickerDialog(
            title = stringResource(R.string.medication_evening),
            initialHour = settings.eveningHour,
            initialMinute = settings.eveningMinute,
            onDismiss = { showEveningTimeDialog = false },
            onConfirm = { hour, minute ->
                showEveningTimeDialog = false
                viewModel.updateMedicationTime(MedicationTiming.EVENING, hour, minute)
            }
        )
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.settings_reset_confirm_title),
            message = stringResource(R.string.settings_reset_confirm_message),
            onConfirm = {
                showResetDialog = false
                viewModel.resetToDefaults()
            },
            onDismiss = { showResetDialog = false }
        )
    }
}
