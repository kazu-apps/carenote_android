package com.carenote.app.ui.screens.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.screens.timeline.components.TimelineItemCard
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.domain.model.TimelineItem
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate
        .collectAsStateWithLifecycle()
    val timelineUiState by viewModel.timelineItems
        .collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing
        .collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.timeline_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.common_close
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        TimelineContent(
            selectedDate = selectedDate,
            timelineUiState = timelineUiState,
            isRefreshing = isRefreshing,
            viewModel = viewModel,
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
            viewModel = viewModel
        )
    }
}

@Composable
private fun TimelineList(
    selectedDate: java.time.LocalDate,
    timelineUiState: UiState<List<TimelineItem>>,
    viewModel: TimelineViewModel
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

        timelineStateItems(timelineUiState, viewModel)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope
    .timelineStateItems(
        timelineUiState: UiState<List<TimelineItem>>,
        viewModel: TimelineViewModel
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
                    TimelineItemCard(item = item)
                }
            }
        }
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
