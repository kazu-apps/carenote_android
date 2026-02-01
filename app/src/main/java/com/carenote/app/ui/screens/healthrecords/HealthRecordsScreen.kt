package com.carenote.app.ui.screens.healthrecords

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.screens.healthrecords.components.HealthRecordCard
import com.carenote.app.ui.screens.healthrecords.components.HealthRecordGraphContent
import com.carenote.app.ui.viewmodel.UiState

private enum class ViewMode { LIST, GRAPH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordsScreen(
    onNavigateToAddRecord: () -> Unit = {},
    onNavigateToEditRecord: (Long) -> Unit = {},
    viewModel: HealthRecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.records.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            snackbarHostState.showSnackbar(event.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.health_records_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (viewMode == ViewMode.LIST) {
                FloatingActionButton(
                    onClick = onNavigateToAddRecord,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.health_records_add)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            ViewModeSelector(
                viewMode = viewMode,
                onViewModeChanged = { viewMode = it }
            )

            val contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding()
            )

            when (viewMode) {
                ViewMode.LIST -> {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            LoadingIndicator()
                        }
                        is UiState.Error -> {
                            ErrorDisplay(error = state.error, onRetry = null)
                        }
                        is UiState.Success -> {
                            HealthRecordListContent(
                                records = state.data,
                                onRecordClick = onNavigateToEditRecord,
                                onNavigateToAdd = onNavigateToAddRecord,
                                contentPadding = contentPadding
                            )
                        }
                    }
                }
                ViewMode.GRAPH -> {
                    val graphViewModel: HealthRecordGraphViewModel = hiltViewModel()
                    val graphState by graphViewModel.graphState.collectAsState()
                    HealthRecordGraphContent(
                        state = graphState,
                        onDateRangeSelected = graphViewModel::setDateRange,
                        contentPadding = contentPadding
                    )
                }
            }
        }
    }

    deleteRecord?.let { record ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.health_records_delete_confirm),
            onConfirm = {
                viewModel.deleteRecord(record.id)
                deleteRecord = null
            },
            onDismiss = { deleteRecord = null },
            isDestructive = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewModeSelector(
    viewMode: ViewMode,
    onViewModeChanged: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = viewMode == ViewMode.LIST,
            onClick = { onViewModeChanged(ViewMode.LIST) },
            label = { Text(stringResource(R.string.health_records_view_list)) }
        )
        FilterChip(
            selected = viewMode == ViewMode.GRAPH,
            onClick = { onViewModeChanged(ViewMode.GRAPH) },
            label = { Text(stringResource(R.string.health_records_view_graph)) }
        )
    }
}

@Composable
private fun HealthRecordListContent(
    records: List<HealthRecord>,
    onRecordClick: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (records.isEmpty()) {
            item(key = "empty_state") {
                EmptyState(
                    icon = Icons.Filled.MonitorHeart,
                    message = stringResource(R.string.health_records_empty),
                    actionLabel = stringResource(R.string.health_records_empty_action),
                    onAction = onNavigateToAdd
                )
            }
        } else {
            items(
                items = records,
                key = { it.id }
            ) { record ->
                HealthRecordCard(
                    record = record,
                    onClick = { onRecordClick(record.id) }
                )
            }
        }
    }
}
