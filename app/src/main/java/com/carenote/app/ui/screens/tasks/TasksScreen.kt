package com.carenote.app.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.carenote.app.domain.model.Task
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.screens.tasks.components.TaskCard
import com.carenote.app.ui.screens.tasks.components.TaskFilterChips
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToEditTask: (Long) -> Unit = {},
    viewModel: TasksViewModel = hiltViewModel()
) {
    val tasksUiState by viewModel.tasks.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteTask by remember { mutableStateOf<Task?>(null) }
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
                        text = stringResource(R.string.tasks_title),
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "filter_chips") {
                TaskFilterChips(
                    selectedFilter = filterMode,
                    onFilterSelected = viewModel::setFilterMode
                )
            }

            when (val state = tasksUiState) {
                is UiState.Loading -> {
                    item(key = "loading") {
                        LoadingIndicator()
                    }
                }
                is UiState.Error -> {
                    item(key = "error") {
                        ErrorDisplay(error = state.error, onRetry = null)
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item(key = "empty_state") {
                            EmptyState(
                                icon = Icons.Filled.CheckCircle,
                                message = stringResource(R.string.tasks_empty),
                                actionLabel = stringResource(R.string.tasks_empty_action),
                                onAction = onNavigateToAddTask
                            )
                        }
                    } else {
                        items(
                            items = state.data,
                            key = { it.id }
                        ) { task ->
                            TaskCard(
                                task = task,
                                onToggleCompletion = { viewModel.toggleCompletion(task) },
                                onClick = { onNavigateToEditTask(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    deleteTask?.let { task ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.tasks_delete_confirm),
            onConfirm = {
                viewModel.deleteTask(task.id)
                deleteTask = null
            },
            onDismiss = { deleteTask = null },
            isDestructive = true
        )
    }
}
