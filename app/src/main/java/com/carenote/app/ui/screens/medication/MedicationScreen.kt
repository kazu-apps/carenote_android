package com.carenote.app.ui.screens.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.carenote.app.ui.components.SwipeToDismissItem
import com.carenote.app.ui.screens.medication.components.MedicationCard
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    onNavigateToAddMedication: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteMedication by remember { mutableStateOf<Medication?>(null) }
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.refreshDateIfNeeded()
        onPauseOrDispose {}
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
                        text = stringResource(R.string.medication_title),
                        style = MaterialTheme.typography.titleLarge
                    )
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
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    if (state.data.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.Medication,
                            message = stringResource(R.string.medication_empty),
                            actionLabel = stringResource(R.string.medication_empty_action),
                            onAction = onNavigateToAddMedication
                        )
                    } else {
                        val todayLogsMap = remember(todayLogs) {
                            todayLogs.associate {
                                (it.medicationId to it.timing?.name) to it.status
                            }
                        }
                        MedicationList(
                            medications = state.data,
                            todayLogs = todayLogsMap,
                            searchQuery = searchQuery,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onTaken = { id, timing ->
                                viewModel.recordMedication(id, MedicationLogStatus.TAKEN, timing)
                            },
                            onSkipped = { id, timing ->
                                viewModel.recordMedication(id, MedicationLogStatus.SKIPPED, timing)
                            },
                            onPostponed = { id, timing ->
                                viewModel.recordMedication(id, MedicationLogStatus.POSTPONED, timing)
                            },
                            onCardClick = onNavigateToDetail,
                            onDelete = { deleteMedication = it },
                            contentPadding = PaddingValues(bottom = 80.dp)
                        )
                    }
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
    todayLogs: Map<Pair<Long, String?>, MedicationLogStatus>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onTaken: (Long, MedicationTiming?) -> Unit,
    onSkipped: (Long, MedicationTiming?) -> Unit,
    onPostponed: (Long, MedicationTiming?) -> Unit,
    onCardClick: (Long) -> Unit,
    onDelete: (Medication) -> Unit,
    contentPadding: PaddingValues
) {
    val timingOrder = remember { listOf(MedicationTiming.MORNING, MedicationTiming.NOON, MedicationTiming.EVENING) }
    val groupedMedications = remember(medications) {
        timingOrder.associateWith { timing ->
            medications.filter { it.timings.contains(timing) }
        }
    }
    val noTimingMeds = remember(medications) {
        medications.filter { it.timings.isEmpty() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = contentPadding.calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "search_bar") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(R.string.common_search))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                singleLine = true
            )
        }
        timingOrder.forEach { timing ->
            val medsForTiming = groupedMedications[timing] ?: emptyList()
            if (medsForTiming.isNotEmpty()) {
                item(key = "header_${timing.name}") {
                    TimingHeader(timing = timing)
                }
                items(
                    items = medsForTiming,
                    key = { "${timing.name}_${it.id}" },
                    contentType = { "MedicationCard" }
                ) { medication ->
                    SwipeToDismissItem(
                        item = medication,
                        onDelete = onDelete
                    ) {
                        MedicationCard(
                            medication = medication,
                            status = todayLogs[medication.id to timing.name],
                            onTaken = { onTaken(medication.id, timing) },
                            onSkipped = { onSkipped(medication.id, timing) },
                            onPostponed = { onPostponed(medication.id, timing) },
                            onClick = { onCardClick(medication.id) }
                        )
                    }
                }
                item(key = "spacer_${timing.name}") {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (noTimingMeds.isNotEmpty()) {
            items(
                items = noTimingMeds,
                key = { "other_${it.id}" },
                contentType = { "MedicationCard" }
            ) { medication ->
                SwipeToDismissItem(
                    item = medication,
                    onDelete = onDelete
                ) {
                    MedicationCard(
                        medication = medication,
                        status = todayLogs[medication.id to null],
                        onTaken = { onTaken(medication.id, null) },
                        onSkipped = { onSkipped(medication.id, null) },
                        onPostponed = { onPostponed(medication.id, null) },
                        onClick = { onCardClick(medication.id) }
                    )
                }
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

@LightDarkPreview
@Composable
private fun MedicationListPreview() {
    CareNoteTheme {
        MedicationList(
            medications = PreviewData.medications,
            todayLogs = PreviewData.todayLogs,
            searchQuery = "",
            onSearchQueryChange = {},
            onTaken = { _, _ -> },
            onSkipped = { _, _ -> },
            onPostponed = { _, _ -> },
            onCardClick = {},
            onDelete = {},
            contentPadding = PaddingValues(bottom = 80.dp)
        )
    }
}
