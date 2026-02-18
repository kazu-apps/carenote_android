package com.carenote.app.ui.screens.timeline

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.TimelineFilterType
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.screens.timeline.components.TimelineItemCard
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onNavigateToAddTask: () -> Unit = {}
) {
    val selectedDate by viewModel.selectedDate
        .collectAsStateWithLifecycle()
    val timelineUiState by viewModel.timelineItems
        .collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing
        .collectAsStateWithLifecycle()
    val filterType by viewModel.filterType
        .collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    TimelineScreenSnackbarEffect(viewModel, snackbarHostState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.timeline_title))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = { TimelineFab(onClick = onNavigateToAddTask) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        TimelineContent(
            selectedDate = selectedDate,
            timelineUiState = timelineUiState,
            isRefreshing = isRefreshing,
            viewModel = viewModel,
            filterType = filterType,
            onSetFilter = { viewModel.setFilter(it) },
            onToggleCompleted = { eventId, completed -> viewModel.toggleCompleted(eventId, completed) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineContent(
    selectedDate: java.time.LocalDate,
    timelineUiState: UiState<List<TimelineItem>>,
    isRefreshing: Boolean,
    viewModel: TimelineViewModel,
    filterType: TimelineFilterType,
    onSetFilter: (TimelineFilterType) -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        TimelineList(
            selectedDate = selectedDate,
            timelineUiState = timelineUiState,
            viewModel = viewModel,
            filterType = filterType,
            onSetFilter = onSetFilter,
            onToggleCompleted = onToggleCompleted
        )
    }
}

@Composable
private fun TimelineList(
    selectedDate: java.time.LocalDate,
    timelineUiState: UiState<List<TimelineItem>>,
    viewModel: TimelineViewModel,
    filterType: TimelineFilterType,
    onSetFilter: (TimelineFilterType) -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
            end = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
            top = AppConfig.UI.ITEM_SPACING_DP.dp,
            bottom = AppConfig.UI.LIST_BOTTOM_PADDING_DP.dp
        ),
        verticalArrangement = Arrangement.spacedBy(
            AppConfig.UI.ITEM_SPACING_DP.dp
        )
    ) {
        item(key = "date_navigator") {
            DateNavigationBar(
                dateText = DateTimeFormatters.formatDate(
                    selectedDate
                ),
                onPreviousDay = { viewModel.goToPreviousDay() },
                onNextDay = { viewModel.goToNextDay() },
                onToday = { viewModel.goToToday() }
            )
        }

        item(key = "filters") {
            TimelineFilterChips(
                selectedFilter = filterType,
                onFilterSelected = onSetFilter
            )
        }

        timelineStateItems(timelineUiState, viewModel, onToggleCompleted)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope
    .timelineStateItems(
        timelineUiState: UiState<List<TimelineItem>>,
        viewModel: TimelineViewModel,
        onToggleCompleted: (Long, Boolean) -> Unit
    ) {
    when (val state = timelineUiState) {
        is UiState.Loading -> {
            item(key = "loading") {
                LoadingIndicator()
            }
        }
        is UiState.Error -> {
            item(key = "error") {
                ErrorDisplay(
                    error = state.error,
                    onRetry = { viewModel.refresh() }
                )
            }
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                item(key = "empty_state") {
                    EmptyState(
                        icon = Icons.Filled.History,
                        message = stringResource(
                            R.string.timeline_empty
                        )
                    )
                }
            } else {
                items(
                    items = state.data,
                    key = {
                        "${it::class.simpleName}_${it.timestamp}"
                    }
                ) { item ->
                    TimelineItemCard(
                        item = item,
                        onToggleCompleted = onToggleCompleted
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineScreenSnackbarEffect(
    viewModel: TimelineViewModel,
    snackbarHostState: SnackbarHostState
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
}

@Composable
private fun TimelineFilterChips(
    selectedFilter: TimelineFilterType,
    onFilterSelected: (TimelineFilterType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimelineFilterType.entries.forEach { type ->
            FilterChip(
                selected = type == selectedFilter,
                onClick = { onFilterSelected(type) },
                label = { Text(filterLabel(type)) },
                modifier = Modifier.testTag(filterTestTag(type))
            )
        }
    }
}

@Composable
private fun TimelineFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = Modifier.testTag(TestTags.TIMELINE_FAB),
        containerColor = MaterialTheme.colorScheme.primary,
        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.timeline_add_task)) }
    )
}

@Composable
private fun filterLabel(type: TimelineFilterType): String {
    return when (type) {
        TimelineFilterType.ALL -> stringResource(R.string.timeline_filter_all)
        TimelineFilterType.TASK -> stringResource(R.string.timeline_filter_tasks)
        TimelineFilterType.EVENT -> stringResource(R.string.timeline_filter_events)
        TimelineFilterType.MEDICATION -> stringResource(R.string.timeline_filter_medication)
        TimelineFilterType.HEALTH_RECORD -> stringResource(R.string.timeline_filter_health)
        TimelineFilterType.NOTE -> stringResource(R.string.timeline_filter_notes)
    }
}

private fun filterTestTag(type: TimelineFilterType): String {
    return when (type) {
        TimelineFilterType.ALL -> TestTags.TIMELINE_FILTER_ALL
        TimelineFilterType.TASK -> TestTags.TIMELINE_FILTER_TASK
        TimelineFilterType.EVENT -> TestTags.TIMELINE_FILTER_EVENT
        TimelineFilterType.MEDICATION -> TestTags.TIMELINE_FILTER_MEDICATION
        TimelineFilterType.HEALTH_RECORD -> TestTags.TIMELINE_FILTER_HEALTH_RECORD
        TimelineFilterType.NOTE -> TestTags.TIMELINE_FILTER_NOTE
    }
}

@Composable
private fun DateNavigationBar(
    dateText: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.a11y_timeline_previous_day)
                )
            }

            Text(
                text = dateText,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onNextDay) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = stringResource(R.string.a11y_timeline_next_day)
                )
            }
        }

        TextButton(onClick = onToday) {
            Text(text = stringResource(R.string.calendar_today))
        }
    }
}
