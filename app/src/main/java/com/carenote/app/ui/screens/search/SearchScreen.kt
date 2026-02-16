package com.carenote.app.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.SearchResult
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.ErrorDisplay
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.DateTimeFormatters
import com.carenote.app.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    onResultClick: (SearchResult) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.search_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        SearchContent(
            uiState = uiState,
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onResultClick = onResultClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun SearchContent(
    uiState: UiState<List<SearchResult>>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SearchTextField(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange
        )
        SearchResultsBody(
            uiState = uiState,
            searchQuery = searchQuery,
            onResultClick = onResultClick
        )
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppConfig.UI
                    .SCREEN_HORIZONTAL_PADDING_DP.dp
            ),
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder)
            )
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

@Composable
private fun SearchResultsBody(
    uiState: UiState<List<SearchResult>>,
    searchQuery: String,
    onResultClick: (SearchResult) -> Unit
) {
    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingIndicator()
        }
        is UiState.Error -> {
            ErrorDisplay(error = state.error)
        }
        is UiState.Success -> {
            SearchSuccessContent(
                data = state.data,
                searchQuery = searchQuery,
                onResultClick = onResultClick
            )
        }
    }
}

@Composable
private fun SearchSuccessContent(
    data: List<SearchResult>,
    searchQuery: String,
    onResultClick: (SearchResult) -> Unit
) {
    if (searchQuery.isBlank()) {
        EmptyState(
            icon = Icons.Filled.Search,
            message = stringResource(R.string.search_empty_hint)
        )
    } else if (data.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Search,
            message = stringResource(R.string.search_empty)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = AppConfig.UI.ITEM_SPACING_DP.dp
            )
        ) {
            items(
                items = data,
                key = {
                    "${it::class.simpleName}_${it.id}"
                }
            ) { result ->
                SearchResultItem(
                    result = result,
                    onClick = { onResultClick(result) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    val (icon, typeLabel) = resultIconAndLabel(result)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                vertical = AppConfig.UI.PREFERENCE_VERTICAL_PADDING_DP.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppConfig.UI.CONTENT_SPACING_DP.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = typeLabel,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AppConfig.UI.ICON_SIZE_MEDIUM_DP.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.title.ifBlank { typeLabel },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (result.subtitle.isNotBlank()) {
                Text(
                    text = result.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = DateTimeFormatters.formatDateTime(result.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun resultIconAndLabel(result: SearchResult): Pair<ImageVector, String> {
    return when (result) {
        is SearchResult.MedicationResult ->
            Icons.Filled.Medication to
                stringResource(R.string.search_result_medication)
        is SearchResult.NoteResult ->
            Icons.AutoMirrored.Filled.StickyNote2 to
                stringResource(R.string.search_result_note)
        is SearchResult.TaskResult ->
            Icons.Filled.CheckCircle to
                stringResource(R.string.search_result_task)
        is SearchResult.HealthRecordResult ->
            Icons.Filled.MonitorHeart to
                stringResource(R.string.search_result_health_record)
        is SearchResult.CalendarEventResult ->
            Icons.Filled.CalendarMonth to
                stringResource(R.string.search_result_calendar_event)
        is SearchResult.EmergencyContactResult ->
            Icons.Filled.Contacts to
                stringResource(R.string.search_result_emergency_contact)
    }
}

@LightDarkPreview
@Composable
private fun SearchContentPreview() {
    CareNoteTheme {
        SearchContent(
            uiState = UiState.Success(PreviewData.searchResults),
            searchQuery = "通院",
            onSearchQueryChange = {},
            onResultClick = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun SearchResultItemPreview() {
    CareNoteTheme {
        SearchResultItem(
            result = PreviewData.searchResults.first(),
            onClick = {}
        )
    }
}
