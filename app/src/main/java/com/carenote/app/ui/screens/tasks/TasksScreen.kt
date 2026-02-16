package com.carenote.app.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
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
import com.carenote.app.domain.model.Task
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.components.SwipeToDismissItem
import com.carenote.app.ui.screens.tasks.components.TaskCard
import com.carenote.app.ui.screens.tasks.components.TaskFilterChips
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.domain.common.DomainError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToEditTask: (Long) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    viewModel: TasksViewModel = hiltViewModel()
) {
    val lazyPagingItems = viewModel.tasks.collectAsLazyPagingItems()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteTask by remember { mutableStateOf<Task?>(null) }

    TasksSnackbarEffect(viewModel, snackbarHostState)

    TasksScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateToAddTask = onNavigateToAddTask,
        onNavigateToSearch = onNavigateToSearch
    ) { innerPadding ->
        TasksListContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            lazyPagingItems = lazyPagingItems,
            searchQuery = searchQuery,
            filterMode = filterMode,
            isRefreshing = isRefreshing,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onFilterSelected = viewModel::setFilterMode,
            onNavigateToAddTask = onNavigateToAddTask,
            onNavigateToEditTask = onNavigateToEditTask,
            onToggleCompletion = viewModel::toggleCompletion,
            onDeleteTask = { deleteTask = it }
        )
    }

    TaskDeleteDialog(
        deleteTask = deleteTask,
        onConfirmDelete = { task ->
            viewModel.deleteTask(task.id)
            deleteTask = null
        },
        onDismiss = { deleteTask = null }
    )
}

@Composable
private fun TasksSnackbarEffect(
    viewModel: TasksViewModel,
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
private fun TaskDeleteDialog(
    deleteTask: Task?,
    onConfirmDelete: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    deleteTask?.let { task ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.tasks_delete_confirm),
            onConfirm = { onConfirmDelete(task) },
            onDismiss = onDismiss,
            isDestructive = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksScaffold(
    snackbarHostState: SnackbarHostState,
    onNavigateToAddTask: () -> Unit,
    onNavigateToSearch: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tasks_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(
                                R.string.a11y_navigate_to_search
                            )
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
                onClick = onNavigateToAddTask,
                modifier = Modifier.testTag(TestTags.TASKS_FAB),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.tasks_add)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksListContent(
    modifier: Modifier,
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<Task>,
    searchQuery: String,
    filterMode: TaskFilterMode,
    isRefreshing: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (TaskFilterMode) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { lazyPagingItems.refresh() },
        modifier = modifier
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
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(R.string.common_search))
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

            item(key = "filter_chips") {
                TaskFilterChips(
                    selectedFilter = filterMode,
                    onFilterSelected = onFilterSelected
                )
            }

            taskListBody(
                lazyPagingItems = lazyPagingItems,
                onNavigateToAddTask = onNavigateToAddTask,
                onNavigateToEditTask = onNavigateToEditTask,
                onToggleCompletion = onToggleCompletion,
                onDeleteTask = onDeleteTask
            )
        }
    }
}

@Suppress("LongParameterList")
private fun androidx.compose.foundation.lazy.LazyListScope.taskListBody(
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<Task>,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    when (val refreshState = lazyPagingItems.loadState.refresh) {
        is LoadState.Loading -> {
            item(key = "loading") { LoadingIndicator() }
        }
        is LoadState.Error -> {
            item(key = "error") {
                ErrorDisplay(
                    error = DomainError.DatabaseError(
                        refreshState.error.message
                            ?: "Unknown error"
                    ),
                    onRetry = { lazyPagingItems.refresh() }
                )
            }
        }
        is LoadState.NotLoading -> {
            taskListItems(
                lazyPagingItems = lazyPagingItems,
                onNavigateToAddTask = onNavigateToAddTask,
                onNavigateToEditTask = onNavigateToEditTask,
                onToggleCompletion = onToggleCompletion,
                onDeleteTask = onDeleteTask
            )
        }
    }
}

@Suppress("LongParameterList")
private fun androidx.compose.foundation.lazy.LazyListScope.taskListItems(
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<Task>,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    if (lazyPagingItems.itemCount == 0) {
        item(key = "empty_state") {
            EmptyState(
                icon = Icons.Filled.CheckCircle,
                message = stringResource(R.string.tasks_empty),
                actionLabel = stringResource(
                    R.string.tasks_empty_action
                ),
                onAction = onNavigateToAddTask
            )
        }
    } else {
        items(
            count = lazyPagingItems.itemCount,
            key = { index ->
                lazyPagingItems.peek(index)?.id ?: index
            },
            contentType = { "TaskCard" }
        ) { index ->
            lazyPagingItems[index]?.let { task ->
                SwipeToDismissItem(
                    item = task,
                    onDelete = { onDeleteTask(it) }
                ) {
                    TaskCard(
                        task = task,
                        onToggleCompletion = {
                            onToggleCompletion(task)
                        },
                        onClick = {
                            onNavigateToEditTask(task.id)
                        }
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun TaskCardPreview() {
    CareNoteTheme {
        androidx.compose.material3.Surface(modifier = Modifier.padding(16.dp)) {
            TaskCard(
                task = PreviewData.task1,
                onToggleCompletion = {},
                onClick = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun TaskFilterChipsPreview() {
    CareNoteTheme {
        androidx.compose.material3.Surface(modifier = Modifier.padding(16.dp)) {
            TaskFilterChips(
                selectedFilter = TaskFilterMode.ALL,
                onFilterSelected = {}
            )
        }
    }
}
