package com.carenote.app.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.NoteTag
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.components.SwipeToDismissItem
import com.carenote.app.ui.screens.notes.components.NoteCard
import com.carenote.app.ui.screens.notes.components.NoteTagChip
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.util.SnackbarEvent
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToAddNote: () -> Unit = {},
    onNavigateToEditNote: (Long) -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteNote by remember { mutableStateOf<Note?>(null) }
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
                        text = stringResource(R.string.notes_title),
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
                onClick = onNavigateToAddNote,
                modifier = Modifier.testTag(TestTags.NOTES_FAB),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.notes_add)
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
                    NoteListContent(
                        notes = state.data,
                        searchQuery = searchQuery,
                        selectedTag = selectedTag,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onTagSelect = viewModel::selectTag,
                        onNoteClick = onNavigateToEditNote,
                        onDeleteNote = { note -> deleteNote = note },
                        onNavigateToAdd = onNavigateToAddNote,
                        contentPadding = PaddingValues(bottom = 80.dp)
                    )
                }
            }
        }
    }

    deleteNote?.let { note ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.notes_delete_confirm),
            onConfirm = {
                viewModel.deleteNote(note.id)
                deleteNote = null
            },
            onDismiss = { deleteNote = null },
            isDestructive = true
        )
    }
}

@Composable
private fun NoteListContent(
    notes: List<Note>,
    searchQuery: String,
    selectedTag: NoteTag?,
    onSearchQueryChange: (String) -> Unit,
    onTagSelect: (NoteTag?) -> Unit,
    onNoteClick: (Long) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onNavigateToAdd: () -> Unit,
    contentPadding: PaddingValues
) {
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
                    Text(text = stringResource(R.string.notes_search))
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

        item(key = "tag_filter") {
            Spacer(modifier = Modifier.height(4.dp))
            TagFilterRow(
                selectedTag = selectedTag,
                onTagSelect = onTagSelect
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (notes.isEmpty()) {
            item(key = "empty_state") {
                EmptyState(
                    icon = Icons.Filled.NoteAlt,
                    message = stringResource(R.string.notes_empty),
                    actionLabel = stringResource(R.string.notes_empty_action),
                    onAction = onNavigateToAdd
                )
            }
        } else {
            items(
                items = notes,
                key = { it.id }
            ) { note ->
                SwipeToDismissItem(
                    item = note,
                    onDelete = onDeleteNote
                ) {
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TagFilterRow(
    selectedTag: NoteTag?,
    onTagSelect: (NoteTag?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            NoteTagAllChip(
                selected = selectedTag == null,
                onClick = { onTagSelect(null) }
            )
        }
        items(NoteTag.entries.toList()) { tag ->
            NoteTagChip(
                tag = tag,
                selected = selectedTag == tag,
                onClick = { onTagSelect(tag) }
            )
        }
    }
}

@Composable
private fun NoteTagAllChip(
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = stringResource(R.string.notes_all_tags),
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = com.carenote.app.ui.theme.ChipShape
    )
}
