package com.carenote.app.ui.screens.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.screens.medication.components.MedicationCard
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    onNavigateToAddMedication: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteMedication by remember { mutableStateOf<Medication?>(null) }
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
                        text = stringResource(R.string.medication_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMedication,
                modifier = Modifier.testTag(TestTags.MEDICATION_FAB),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.medication_add)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(innerPadding))
            }
            is UiState.Error -> {
                ErrorDisplay(
                    error = state.error,
                    onRetry = null,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.Medication,
                        message = stringResource(R.string.medication_empty),
                        actionLabel = stringResource(R.string.medication_empty_action),
                        onAction = onNavigateToAddMedication,
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    MedicationList(
                        medications = state.data,
                        todayLogs = todayLogs.associate { it.medicationId to it.status },
                        onTaken = { id -> viewModel.recordMedication(id, MedicationLogStatus.TAKEN) },
                        onSkipped = { id -> viewModel.recordMedication(id, MedicationLogStatus.SKIPPED) },
                        onPostponed = { id -> viewModel.recordMedication(id, MedicationLogStatus.POSTPONED) },
                        onCardClick = onNavigateToDetail,
                        contentPadding = innerPadding
                    )
                }
            }
        }
    }

    deleteMedication?.let { medication ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.medication_delete_confirm, medication.name),
            onConfirm = {
                viewModel.deleteMedication(medication.id)
                deleteMedication = null
            },
            onDismiss = { deleteMedication = null },
            isDestructive = true
        )
    }
}

@Composable
private fun MedicationList(
    medications: List<Medication>,
    todayLogs: Map<Long, MedicationLogStatus>,
    onTaken: (Long) -> Unit,
    onSkipped: (Long) -> Unit,
    onPostponed: (Long) -> Unit,
    onCardClick: (Long) -> Unit,
    contentPadding: PaddingValues
) {
    val timingOrder = listOf(MedicationTiming.MORNING, MedicationTiming.NOON, MedicationTiming.EVENING)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timingOrder.forEach { timing ->
            val medsForTiming = medications.filter { it.timings.contains(timing) }
            if (medsForTiming.isNotEmpty()) {
                item(key = "header_${timing.name}") {
                    TimingHeader(timing = timing)
                }
                items(
                    items = medsForTiming,
                    key = { "${timing.name}_${it.id}" }
                ) { medication ->
                    MedicationCard(
                        medication = medication,
                        status = todayLogs[medication.id],
                        onTaken = { onTaken(medication.id) },
                        onSkipped = { onSkipped(medication.id) },
                        onPostponed = { onPostponed(medication.id) },
                        onClick = { onCardClick(medication.id) }
                    )
                }
                item(key = "spacer_${timing.name}") {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        val noTimingMeds = medications.filter { it.timings.isEmpty() }
        if (noTimingMeds.isNotEmpty()) {
            items(
                items = noTimingMeds,
                key = { "other_${it.id}" }
            ) { medication ->
                MedicationCard(
                    medication = medication,
                    status = todayLogs[medication.id],
                    onTaken = { onTaken(medication.id) },
                    onSkipped = { onSkipped(medication.id) },
                    onPostponed = { onPostponed(medication.id) },
                    onClick = { onCardClick(medication.id) }
                )
            }
        }
    }
}

@Composable
private fun TimingHeader(timing: MedicationTiming) {
    val label = when (timing) {
        MedicationTiming.MORNING -> stringResource(R.string.medication_morning)
        MedicationTiming.NOON -> stringResource(R.string.medication_noon)
        MedicationTiming.EVENING -> stringResource(R.string.medication_evening)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
