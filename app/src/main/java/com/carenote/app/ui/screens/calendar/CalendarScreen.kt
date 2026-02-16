package com.carenote.app.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carenote.app.R
import com.carenote.app.domain.model.CalendarEvent
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.components.SwipeToDismissItem
import androidx.compose.material.icons.filled.History
import com.carenote.app.ui.screens.calendar.components.CalendarEventCard
import com.carenote.app.ui.screens.calendar.components.MonthCalendarGrid
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToAddEvent: () -> Unit = {},
    onNavigateToEditEvent: (Long) -> Unit = {},
    onNavigateToTimeline: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val eventsForMonth by viewModel.eventsForMonth.collectAsStateWithLifecycle()
    val eventsUiState by viewModel.eventsForSelectedDate.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    CalendarScreenSnackbarEffect(viewModel, snackbarHostState)

    CalendarScaffold(
        currentMonth = currentMonth,
        selectedDate = selectedDate,
        eventsForMonth = eventsForMonth,
        eventsUiState = eventsUiState,
        isRefreshing = isRefreshing,
        snackbarHostState = snackbarHostState,
        viewModel = viewModel,
        onNavigateToAddEvent = onNavigateToAddEvent,
        onNavigateToEditEvent = onNavigateToEditEvent,
        onNavigateToTimeline = onNavigateToTimeline,
        onNavigateToSearch = onNavigateToSearch,
        onDeleteRequest = { deleteEvent = it }
    )

    CalendarDeleteDialog(
        deleteEvent = deleteEvent,
        onConfirmDelete = { event ->
            viewModel.deleteEvent(event.id)
            deleteEvent = null
        },
        onDismiss = { deleteEvent = null }
    )
}

@Composable
private fun CalendarScreenSnackbarEffect(
    viewModel: CalendarViewModel,
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
private fun CalendarDeleteDialog(
    deleteEvent: CalendarEvent?,
    onConfirmDelete: (CalendarEvent) -> Unit,
    onDismiss: () -> Unit
) {
    deleteEvent?.let { event ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.calendar_event_delete_confirm),
            onConfirm = { onConfirmDelete(event) },
            onDismiss = onDismiss,
            isDestructive = true
        )
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScaffold(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventsForMonth: Map<LocalDate, List<CalendarEvent>>,
    eventsUiState: UiState<List<CalendarEvent>>,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    viewModel: CalendarViewModel,
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Long) -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onDeleteRequest: (CalendarEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CalendarTopBar(
                currentMonth = currentMonth,
                onPreviousMonth = { viewModel.changeMonth(currentMonth.minusMonths(1)) },
                onNextMonth = { viewModel.changeMonth(currentMonth.plusMonths(1)) },
                onToday = {
                    LocalDate.now().let { today ->
                        viewModel.changeMonth(YearMonth.from(today))
                        viewModel.selectDate(today)
                    }
                },
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToTimeline = onNavigateToTimeline
            )
        },
        floatingActionButton = {
            CalendarFab(onClick = onNavigateToAddEvent)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            CalendarContent(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                eventsForMonth = eventsForMonth,
                eventsUiState = eventsUiState,
                onDateClick = { date ->
                    viewModel.selectDate(date)
                    if (YearMonth.from(date) != currentMonth) {
                        viewModel.changeMonth(YearMonth.from(date))
                    }
                },
                onNavigateToAddEvent = onNavigateToAddEvent,
                onNavigateToEditEvent = onNavigateToEditEvent,
                onDelete = onDeleteRequest,
                onToggleCompleted = { viewModel.toggleCompleted(it) },
                onRefresh = { viewModel.refresh() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToTimeline: () -> Unit
) {
    TopAppBar(
        title = {
            MonthNavigationBar(
                currentMonth = currentMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onToday = onToday
            )
        },
        actions = {
            IconButton(onClick = onNavigateToSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.a11y_navigate_to_search)
                )
            }
            IconButton(onClick = onNavigateToTimeline) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = stringResource(R.string.a11y_timeline_icon)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Suppress("LongParameterList")
@Composable
private fun CalendarContent(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventsForMonth: Map<LocalDate, List<CalendarEvent>>,
    eventsUiState: UiState<List<CalendarEvent>>,
    onDateClick: (LocalDate) -> Unit,
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Long) -> Unit,
    onDelete: (CalendarEvent) -> Unit,
    onToggleCompleted: (CalendarEvent) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 8.dp, bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "calendar_grid") {
            MonthCalendarGrid(
                yearMonth = currentMonth,
                selectedDate = selectedDate,
                eventsForMonth = eventsForMonth,
                onDateClick = onDateClick
            )
        }

        item(key = "selected_date_header") {
            Text(
                text = DateTimeFormatters.formatDate(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        calendarEventsList(
            eventsUiState = eventsUiState,
            onNavigateToAddEvent = onNavigateToAddEvent,
            onNavigateToEditEvent = onNavigateToEditEvent,
            onDelete = onDelete,
            onToggleCompleted = onToggleCompleted,
            onRefresh = onRefresh
        )
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.calendarEventsList(
    eventsUiState: UiState<List<CalendarEvent>>,
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Long) -> Unit,
    onDelete: (CalendarEvent) -> Unit,
    onToggleCompleted: (CalendarEvent) -> Unit,
    onRefresh: () -> Unit
) {
    when (val state = eventsUiState) {
        is UiState.Loading -> {
            item(key = "loading") { LoadingIndicator() }
        }
        is UiState.Error -> {
            item(key = "error") {
                ErrorDisplay(error = state.error, onRetry = onRefresh)
            }
        }
        is UiState.Success -> {
            calendarEventsSuccessItems(
                events = state.data,
                onNavigateToAddEvent = onNavigateToAddEvent,
                onNavigateToEditEvent = onNavigateToEditEvent,
                onDelete = onDelete,
                onToggleCompleted = onToggleCompleted
            )
        }
    }
}

@Suppress("LongParameterList")
private fun LazyListScope.calendarEventsSuccessItems(
    events: List<CalendarEvent>,
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Long) -> Unit,
    onDelete: (CalendarEvent) -> Unit,
    onToggleCompleted: (CalendarEvent) -> Unit
) {
    if (events.isEmpty()) {
        item(key = "empty_state") {
            EmptyState(
                icon = Icons.Filled.CalendarMonth,
                message = stringResource(R.string.calendar_empty),
                actionLabel = stringResource(R.string.calendar_empty_action),
                onAction = onNavigateToAddEvent
            )
        }
    } else {
        items(items = events, key = { it.id }) { event ->
            SwipeToDismissItem(
                item = event,
                onDelete = onDelete
            ) {
                CalendarEventCard(
                    event = event,
                    onClick = { onNavigateToEditEvent(event.id) },
                    onToggleCompleted = onToggleCompleted
                )
            }
        }
    }
}

@Composable
private fun CalendarFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.testTag(TestTags.CALENDAR_FAB),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.calendar_add_event)
        )
    }
}

@Composable
private fun MonthNavigationBar(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.a11y_calendar_previous_month)
                )
            }

            Text(
                text = DateTimeFormatters.formatYearMonth(currentMonth.atDay(1)),
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = stringResource(R.string.a11y_calendar_next_month)
                )
            }
        }

        TextButton(onClick = onToday) {
            Text(text = stringResource(R.string.calendar_today))
        }
    }
}

@LightDarkPreview
@Composable
private fun MonthNavigationBarPreview() {
    CareNoteTheme {
        MonthNavigationBar(
            currentMonth = java.time.YearMonth.of(2025, 1),
            onPreviousMonth = {},
            onNextMonth = {},
            onToday = {}
        )
    }
}
