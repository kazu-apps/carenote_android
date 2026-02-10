package com.carenote.app.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val eventsForMonth by viewModel.eventsForMonth.collectAsStateWithLifecycle()
    val eventsUiState by viewModel.eventsForSelectedDate.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteEvent by remember { mutableStateOf<CalendarEvent?>(null) }
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
                    MonthNavigationBar(
                        currentMonth = currentMonth,
                        onPreviousMonth = {
                            viewModel.changeMonth(currentMonth.minusMonths(1))
                        },
                        onNextMonth = {
                            viewModel.changeMonth(currentMonth.plusMonths(1))
                        },
                        onToday = {
                            val today = LocalDate.now()
                            viewModel.changeMonth(YearMonth.from(today))
                            viewModel.selectDate(today)
                        }
                    )
                },
                actions = {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEvent,
                modifier = Modifier.testTag(TestTags.CALENDAR_FAB),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.calendar_add_event)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
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
            item(key = "calendar_grid") {
                MonthCalendarGrid(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    eventsForMonth = eventsForMonth,
                    onDateClick = { date ->
                        viewModel.selectDate(date)
                        if (YearMonth.from(date) != currentMonth) {
                            viewModel.changeMonth(YearMonth.from(date))
                        }
                    }
                )
            }

            item(key = "selected_date_header") {
                Text(
                    text = DateTimeFormatters.formatDate(selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            when (val state = eventsUiState) {
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
                                icon = Icons.Filled.CalendarMonth,
                                message = stringResource(R.string.calendar_empty),
                                actionLabel = stringResource(R.string.calendar_empty_action),
                                onAction = onNavigateToAddEvent
                            )
                        }
                    } else {
                        items(
                            items = state.data,
                            key = { it.id }
                        ) { event ->
                            SwipeToDismissItem(
                                item = event,
                                onDelete = { deleteEvent = it }
                            ) {
                                CalendarEventCard(
                                    event = event,
                                    onClick = { onNavigateToEditEvent(event.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    deleteEvent?.let { event ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.calendar_event_delete_confirm),
            onConfirm = {
                viewModel.deleteEvent(event.id)
                deleteEvent = null
            },
            onDismiss = { deleteEvent = null },
            isDestructive = true
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
