package com.carenote.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.components.LoadingIndicator
import com.carenote.app.ui.preview.LightDarkPreview
import com.carenote.app.ui.preview.PreviewData
import com.carenote.app.ui.screens.home.components.HomeTopBar
import com.carenote.app.ui.screens.home.sections.CalendarEventItem
import com.carenote.app.ui.screens.home.sections.CalendarSection
import com.carenote.app.ui.screens.home.sections.HealthRecordSection
import com.carenote.app.ui.screens.home.sections.MedicationSection
import com.carenote.app.ui.screens.home.sections.NoteSection
import com.carenote.app.ui.screens.home.sections.TaskSection
import com.carenote.app.ui.theme.CareNoteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToMedication: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToHealthRecords: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onMedicationClick: (Long) -> Unit = {},
    onTaskClick: (Long) -> Unit = {},
    onHealthRecordClick: (Long) -> Unit = {},
    onNoteClick: (Long) -> Unit = {},
    onCalendarEventClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val navigation = buildHomeNavigation(
        viewModel = viewModel,
        onNavigateToMedication = onNavigateToMedication,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToTimeline = onNavigateToTimeline,
        onNavigateToHealthRecords = onNavigateToHealthRecords,
        onNavigateToNotes = onNavigateToNotes,
        onMedicationClick = onMedicationClick,
        onTaskClick = onTaskClick,
        onHealthRecordClick = onHealthRecordClick,
        onNoteClick = onNoteClick,
        onCalendarEventClick = onCalendarEventClick
    )

    Scaffold(
        topBar = {
            HomeTopBar(
                activeRecipient = uiState.activeRecipient,
                allRecipients = uiState.allRecipients,
                onRecipientSelected = { viewModel.switchRecipient(it) },
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            navigation = navigation,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun buildHomeNavigation(
    viewModel: HomeViewModel,
    onNavigateToMedication: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToHealthRecords: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onMedicationClick: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    onHealthRecordClick: (Long) -> Unit,
    onNoteClick: (Long) -> Unit,
    onCalendarEventClick: (Long) -> Unit
): HomeNavigationCallbacks = HomeNavigationCallbacks(
    onNavigateToMedication = {
        viewModel.logSeeAllClicked("medication")
        onNavigateToMedication()
    },
    onNavigateToCalendar = {
        viewModel.logSeeAllClicked("calendar")
        onNavigateToCalendar()
    },
    onNavigateToTimeline = {
        viewModel.logSeeAllClicked("tasks")
        onNavigateToTimeline()
    },
    onNavigateToHealthRecords = {
        viewModel.logSeeAllClicked("health_records")
        onNavigateToHealthRecords()
    },
    onNavigateToNotes = {
        viewModel.logSeeAllClicked("notes")
        onNavigateToNotes()
    },
    onMedicationClick = { id ->
        viewModel.logItemClicked("medication", id)
        onMedicationClick(id)
    },
    onTaskClick = { id ->
        viewModel.logItemClicked("task", id)
        onTaskClick(id)
    },
    onHealthRecordClick = { id ->
        viewModel.logItemClicked("health_record", id)
        onHealthRecordClick(id)
    },
    onNoteClick = { id ->
        viewModel.logItemClicked("note", id)
        onNoteClick(id)
    },
    onCalendarEventClick = { id ->
        viewModel.logItemClicked("calendar", id)
        onCalendarEventClick(id)
    }
)

internal data class HomeNavigationCallbacks(
    val onNavigateToMedication: () -> Unit,
    val onNavigateToCalendar: () -> Unit,
    val onNavigateToTimeline: () -> Unit,
    val onNavigateToHealthRecords: () -> Unit,
    val onNavigateToNotes: () -> Unit,
    val onMedicationClick: (Long) -> Unit = {},
    val onTaskClick: (Long) -> Unit = {},
    val onHealthRecordClick: (Long) -> Unit = {},
    val onNoteClick: (Long) -> Unit = {},
    val onCalendarEventClick: (Long) -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    navigation: HomeNavigationCallbacks,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        LoadingIndicator(modifier = modifier)
        return
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        HomeSectionList(uiState = uiState, navigation = navigation)
    }
}

@Composable
private fun HomeSectionList(
    uiState: HomeUiState,
    navigation: HomeNavigationCallbacks
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(AppConfig.UI.CONTENT_SPACING_DP.dp)
    ) {
        item { Spacer(modifier = Modifier.height(AppConfig.UI.SMALL_SPACING_DP.dp)) }
        item {
            MedicationSection(
                medications = uiState.todayMedications,
                onSeeAll = navigation.onNavigateToMedication,
                onItemClick = navigation.onMedicationClick
            )
        }
        item {
            TaskSection(
                tasks = uiState.upcomingTasks,
                onSeeAll = navigation.onNavigateToTimeline,
                onItemClick = navigation.onTaskClick
            )
        }
        item {
            HealthRecordSection(
                record = uiState.latestHealthRecord,
                onSeeAll = navigation.onNavigateToHealthRecords,
                onItemClick = navigation.onHealthRecordClick
            )
        }
        item {
            NoteSection(
                notes = uiState.recentNotes,
                onSeeAll = navigation.onNavigateToNotes,
                onItemClick = navigation.onNoteClick
            )
        }
        item {
            CalendarSection(
                events = uiState.todayEvents,
                onSeeAll = navigation.onNavigateToCalendar,
                onItemClick = navigation.onCalendarEventClick
            )
        }
        item { Spacer(modifier = Modifier.height(AppConfig.UI.LIST_BOTTOM_PADDING_DP.dp)) }
    }
}

@LightDarkPreview
@Composable
private fun HomeContentPreview() {
    CareNoteTheme {
        HomeContent(
            uiState = PreviewData.homeUiState,
            isRefreshing = false,
            onRefresh = {},
            navigation = HomeNavigationCallbacks(
                onNavigateToMedication = {},
                onNavigateToCalendar = {},
                onNavigateToTimeline = {},
                onNavigateToHealthRecords = {},
                onNavigateToNotes = {}
            )
        )
    }
}

@LightDarkPreview
@Composable
private fun CalendarEventItemPreview() {
    CareNoteTheme {
        CalendarEventItem(event = PreviewData.calendarEvent1)
    }
}
