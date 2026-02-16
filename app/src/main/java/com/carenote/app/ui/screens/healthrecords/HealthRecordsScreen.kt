package com.carenote.app.ui.screens.healthrecords

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.components.SwipeToDismissItem
import com.carenote.app.ui.screens.healthrecords.components.HealthRecordCard
import com.carenote.app.ui.screens.healthrecords.components.HealthRecordGraphContent
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.ExportState
import com.carenote.app.ui.viewmodel.UiState

private enum class ViewMode { LIST, GRAPH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordsScreen(
    onNavigateToAddRecord: () -> Unit = {},
    onNavigateToEditRecord: (Long) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    viewModel: HealthRecordsViewModel = hiltViewModel()
) {
    val lazyPagingItems = viewModel.pagedRecords.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteRecord by remember { mutableStateOf<HealthRecord?>(null) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var showExportMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    HealthRecordsSnackbarEffect(viewModel, snackbarHostState, context)
    HealthRecordsExportEffect(exportState, context, viewModel)

    HealthRecordsScaffold(
        viewMode = viewMode,
        onNavigateToSearch = onNavigateToSearch,
        showExportMenu = showExportMenu,
        onShowExportMenu = { showExportMenu = true },
        onDismissExportMenu = { showExportMenu = false },
        onExportCsv = { showExportMenu = false; viewModel.exportCsv() },
        onExportPdf = { showExportMenu = false; viewModel.exportPdf() },
        onNavigateToAddRecord = onNavigateToAddRecord,
        snackbarHostState = snackbarHostState,
        onViewModeChanged = { viewMode = it },
        lazyPagingItems = lazyPagingItems,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onNavigateToEditRecord = onNavigateToEditRecord,
        onDeleteRequest = { deleteRecord = it }
    )

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

@Composable
private fun HealthRecordsSnackbarEffect(
    viewModel: HealthRecordsViewModel,
    snackbarHostState: SnackbarHostState,
    context: android.content.Context
) {
    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId -> context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
private fun HealthRecordsExportEffect(
    exportState: ExportState,
    context: android.content.Context,
    viewModel: HealthRecordsViewModel
) {
    LaunchedEffect(exportState) {
        val state = exportState
        when (state) {
            is ExportState.Success -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = state.mimeType
                    putExtra(Intent.EXTRA_STREAM, state.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        context.getString(R.string.health_records_export)
                    )
                )
                viewModel.resetExportState()
            }
            is ExportState.Error -> {
                viewModel.resetExportState()
            }
            else -> { /* Idle, Exporting — no action needed */ }
        }
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthRecordsScaffold(
    viewMode: ViewMode,
    onNavigateToSearch: () -> Unit,
    showExportMenu: Boolean,
    onShowExportMenu: () -> Unit,
    onDismissExportMenu: () -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onNavigateToAddRecord: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onViewModeChanged: (ViewMode) -> Unit,
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<HealthRecord>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToEditRecord: (Long) -> Unit,
    onDeleteRequest: (HealthRecord) -> Unit
) {
    Scaffold(
        topBar = {
            HealthRecordsTopBar(
                onNavigateToSearch = onNavigateToSearch,
                showExportMenu = showExportMenu,
                onShowExportMenu = onShowExportMenu,
                onDismissExportMenu = onDismissExportMenu,
                onExportCsv = onExportCsv,
                onExportPdf = onExportPdf
            )
        },
        floatingActionButton = {
            if (viewMode == ViewMode.LIST) {
                HealthRecordsFab(onClick = onNavigateToAddRecord)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            ViewModeSelector(
                viewMode = viewMode,
                onViewModeChanged = onViewModeChanged
            )
            val contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding()
            )
            when (viewMode) {
                ViewMode.LIST -> HealthRecordListView(
                    lazyPagingItems = lazyPagingItems,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    contentPadding = contentPadding,
                    onNavigateToAddRecord = onNavigateToAddRecord,
                    onNavigateToEditRecord = onNavigateToEditRecord,
                    onDeleteRequest = onDeleteRequest
                )
                ViewMode.GRAPH -> HealthRecordGraphView(
                    contentPadding = contentPadding
                )
            }
        }
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthRecordListView(
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<HealthRecord>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    contentPadding: PaddingValues,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToEditRecord: (Long) -> Unit,
    onDeleteRequest: (HealthRecord) -> Unit
) {
    val listIsRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading
    PullToRefreshBox(
        isRefreshing = listIsRefreshing,
        onRefresh = { lazyPagingItems.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "search_bar") {
                HealthRecordSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange
                )
            }
            healthRecordPagingContent(
                lazyPagingItems = lazyPagingItems,
                onNavigateToAddRecord = onNavigateToAddRecord,
                onNavigateToEditRecord = onNavigateToEditRecord,
                onDeleteRequest = onDeleteRequest
            )
        }
    }
}

@Composable
private fun HealthRecordSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
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

@Suppress("LongParameterList")
private fun LazyListScope.healthRecordPagingContent(
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<HealthRecord>,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToEditRecord: (Long) -> Unit,
    onDeleteRequest: (HealthRecord) -> Unit
) {
    when (val refreshState = lazyPagingItems.loadState.refresh) {
        is LoadState.Loading -> {
            item(key = "loading") { LoadingIndicator() }
        }
        is LoadState.Error -> {
            item(key = "error") {
                ErrorDisplay(
                    error = DomainError.DatabaseError(
                        refreshState.error.message ?: "Unknown error"
                    ),
                    onRetry = { lazyPagingItems.refresh() }
                )
            }
        }
        is LoadState.NotLoading -> {
            healthRecordNotLoadingContent(
                lazyPagingItems = lazyPagingItems,
                onNavigateToAddRecord = onNavigateToAddRecord,
                onNavigateToEditRecord = onNavigateToEditRecord,
                onDeleteRequest = onDeleteRequest
            )
        }
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.healthRecordNotLoadingContent(
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<HealthRecord>,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToEditRecord: (Long) -> Unit,
    onDeleteRequest: (HealthRecord) -> Unit
) {
    if (lazyPagingItems.itemCount == 0) {
        item(key = "empty_state") {
            EmptyState(
                icon = Icons.Filled.MonitorHeart,
                message = stringResource(R.string.health_records_empty),
                actionLabel = stringResource(
                    R.string.health_records_empty_action
                ),
                onAction = onNavigateToAddRecord
            )
        }
    } else {
        items(
            count = lazyPagingItems.itemCount,
            key = { index ->
                lazyPagingItems.peek(index)?.id ?: index
            }
        ) { index ->
            lazyPagingItems[index]?.let { record ->
                SwipeToDismissItem(
                    item = record,
                    onDelete = { onDeleteRequest(it) }
                ) {
                    HealthRecordCard(
                        record = record,
                        onClick = {
                            onNavigateToEditRecord(record.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthRecordGraphView(contentPadding: PaddingValues) {
    val graphViewModel: HealthRecordGraphViewModel = hiltViewModel()
    val graphState by graphViewModel.graphState.collectAsStateWithLifecycle()
    HealthRecordGraphContent(
        state = graphState,
        onDateRangeSelected = graphViewModel::setDateRange,
        contentPadding = contentPadding
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthRecordsTopBar(
    onNavigateToSearch: () -> Unit,
    showExportMenu: Boolean,
    onShowExportMenu: () -> Unit,
    onDismissExportMenu: () -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.health_records_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = onNavigateToSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.a11y_navigate_to_search)
                )
            }
            IconButton(
                onClick = onShowExportMenu,
                modifier = Modifier.testTag(TestTags.EXPORT_BUTTON)
            ) {
                Icon(
                    imageVector = Icons.Filled.FileDownload,
                    contentDescription = stringResource(R.string.health_records_export)
                )
            }
            DropdownMenu(
                expanded = showExportMenu,
                onDismissRequest = onDismissExportMenu
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.health_records_export_csv)) },
                    onClick = onExportCsv,
                    modifier = Modifier.testTag(TestTags.EXPORT_CSV_ITEM)
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.health_records_export_pdf)) },
                    onClick = onExportPdf,
                    modifier = Modifier.testTag(TestTags.EXPORT_PDF_ITEM)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun HealthRecordsFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.testTag(TestTags.HEALTH_RECORDS_FAB),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.health_records_add)
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

@LightDarkPreview
@Composable
private fun HealthRecordListContentPreview() {
    CareNoteTheme {
        HealthRecordListPreviewBody(
            records = PreviewData.healthRecords
        )
    }
}

@Composable
private fun HealthRecordListPreviewBody(
    records: List<HealthRecord>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "search_bar") {
            OutlinedTextField(
                value = "",
                onValueChange = {},
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
        items(
            items = records,
            key = { it.id }
        ) { record ->
            HealthRecordCard(
                record = record,
                onClick = {}
            )
        }
    }
}
