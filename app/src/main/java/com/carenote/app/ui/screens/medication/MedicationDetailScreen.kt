package com.carenote.app.ui.screens.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLog
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.ui.components.CareNoteCard
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.screens.medication.components.MedicationTimingChip
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MedicationDetailViewModel = hiltViewModel()
) {
    val medicationState by viewModel.medication.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.deletedEvent.collect { deleted ->
            if (deleted) {
                onNavigateBack()
            }
        }
    }

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
                        text = stringResource(R.string.medication_detail),
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
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = MaterialTheme.colorScheme.error
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
        when (val state = medicationState) {
            is UiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(innerPadding))
            }
            is UiState.Error -> {
                ErrorDisplay(
                    error = state.error,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                MedicationDetailContent(
                    medication = state.data,
                    logs = logs,
                    contentPadding = innerPadding
                )
            }
        }
    }

    if (showDeleteDialog) {
        val medicationName = (medicationState as? UiState.Success)?.data?.name ?: ""
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.medication_delete_confirm, medicationName),
            onConfirm = {
                viewModel.deleteMedication()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            isDestructive = true
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicationDetailContent(
    medication: Medication,
    logs: List<MedicationLog>,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "info") {
            CareNoteCard {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (medication.dosage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = medication.dosage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    medication.timings.forEach { timing ->
                        MedicationTimingChip(timing = timing)
                    }
                }
                medication.times.forEach { (timing, time) ->
                    val timingLabel = when (timing) {
                        com.carenote.app.domain.model.MedicationTiming.MORNING ->
                            stringResource(R.string.medication_morning)
                        com.carenote.app.domain.model.MedicationTiming.NOON ->
                            stringResource(R.string.medication_noon)
                        com.carenote.app.domain.model.MedicationTiming.EVENING ->
                            stringResource(R.string.medication_evening)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$timingLabel: ${DateTimeFormatters.formatTime(time)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "history_header") {
            Text(
                text = stringResource(R.string.medication_history),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (logs.isEmpty()) {
            item(key = "no_history") {
                EmptyState(
                    icon = Icons.Filled.History,
                    message = stringResource(R.string.medication_no_history),
                    modifier = Modifier.height(200.dp)
                )
            }
        } else {
            items(
                items = logs,
                key = { it.id }
            ) { log ->
                LogItem(log = log)
            }
        }
    }
}

@Composable
private fun LogItem(log: MedicationLog) {
    CareNoteCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = DateTimeFormatters.formatDateTime(log.scheduledAt),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (log.memo.isNotBlank()) {
                    Text(
                        text = log.memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LogStatusText(status = log.status)
        }
    }
}

@Composable
private fun LogStatusText(status: MedicationLogStatus) {
    val (text, color) = when (status) {
        MedicationLogStatus.TAKEN ->
            stringResource(R.string.medication_status_taken) to MaterialTheme.colorScheme.primary
        MedicationLogStatus.SKIPPED ->
            stringResource(R.string.medication_status_skipped) to MaterialTheme.colorScheme.error
        MedicationLogStatus.POSTPONED ->
            stringResource(R.string.medication_status_postponed) to MaterialTheme.colorScheme.tertiary
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color
    )
}
