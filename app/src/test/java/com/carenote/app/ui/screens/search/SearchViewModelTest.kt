package com.carenote.app.ui.screens.search

import app.cash.turbine.test
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.SearchResult
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeSearchRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aMedication
import com.carenote.app.testing.aNote
import com.carenote.app.testing.aCalendarEvent
import com.carenote.app.domain.model.CalendarEventType
import com.carenote.app.domain.model.TaskPriority
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var searchRepository: FakeSearchRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        searchRepository = FakeSearchRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(searchRepository, analyticsRepository)
    }

    private fun createMedicationResult(
        id: Long = 1L,
        name: String = "テスト薬"
    ): SearchResult.MedicationResult {
        return SearchResult.MedicationResult(aMedication(id = id, name = name))
    }

    private fun createNoteResult(
        id: Long = 2L,
        title: String = "テストメモ"
    ): SearchResult.NoteResult {
        return SearchResult.NoteResult(aNote(id = id, title = title, content = "内容"))
    }

    private fun createTaskResult(
        id: Long = 3L,
        title: String = "テストタスク"
    ): SearchResult.CalendarEventResult {
        return SearchResult.CalendarEventResult(
            aCalendarEvent(id = id, title = title, type = CalendarEventType.TASK, priority = TaskPriority.MEDIUM)
        )
    }

    @Test
    fun `initial state is Success with empty list`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(emptyList<SearchResult>(), (state as UiState.Success).data)
    }

    @Test
    fun `empty query returns Success with empty list`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSearchQuery("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(emptyList<SearchResult>(), (state as UiState.Success).data)
    }

    @Test
    fun `blank query returns Success with empty list`() = runTest(mainCoroutineRule.testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSearchQuery("   ")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(emptyList<SearchResult>(), (state as UiState.Success).data)
    }

    @Test
    fun `non-empty query triggers search and returns results`() = runTest(mainCoroutineRule.testDispatcher) {
        val medicationResult = createMedicationResult()
        searchRepository.setResults(listOf(medicationResult))
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateSearchQuery("テスト")
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals(1, data.size)
            assertTrue(data[0] is SearchResult.MedicationResult)
        }
    }

    @Test
    fun `debounce waits before search`() = runTest(mainCoroutineRule.testDispatcher) {
        val medicationResult = createMedicationResult()
        searchRepository.setResults(listOf(medicationResult))
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val initial = expectMostRecentItem()
            assertTrue(initial is UiState.Success)
            assertEquals(emptyList<SearchResult>(), (initial as UiState.Success).data)

            viewModel.updateSearchQuery("テスト")
            advanceTimeBy(100)

            // Debounce has not yet elapsed - state should still be empty
            expectNoEvents()

            advanceTimeBy(AppConfig.UI.SEARCH_DEBOUNCE_MS + 100)
            advanceUntilIdle()

            val lateState = expectMostRecentItem()
            assertTrue(lateState is UiState.Success)
            assertEquals(1, (lateState as UiState.Success).data.size)
        }
    }

    @Test
    fun `search logs analytics event`() = runTest(mainCoroutineRule.testDispatcher) {
        searchRepository.setResults(listOf(createMedicationResult()))
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateSearchQuery("テスト")
            advanceUntilIdle()

            expectMostRecentItem()

            val searchEvents = analyticsRepository.loggedEvents.filter {
                it.first == AppConfig.Analytics.EVENT_SEARCH_PERFORMED
            }
            assertTrue(searchEvents.isNotEmpty())
        }
    }

    @Test
    fun `search error emits UiState Error`() = runTest(mainCoroutineRule.testDispatcher) {
        searchRepository.shouldFail = true
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateSearchQuery("テスト")
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is UiState.Error)
        }
    }

    @Test
    fun `rapid query changes only triggers last search`() = runTest(mainCoroutineRule.testDispatcher) {
        val results = listOf(createMedicationResult(name = "abc薬"))
        searchRepository.setResults(results)
        viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.updateSearchQuery("a")
            advanceTimeBy(50)
            viewModel.updateSearchQuery("ab")
            advanceTimeBy(50)
            viewModel.updateSearchQuery("abc")
            advanceUntilIdle()

            assertEquals("abc", viewModel.searchQuery.value)
            val state = expectMostRecentItem()
            assertTrue(state is UiState.Success)
            assertEquals(1, (state as UiState.Success).data.size)
        }
    }
}
