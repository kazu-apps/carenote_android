package com.carenote.app.ui.screens.search

import app.cash.turbine.test
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.Note
import com.carenote.app.domain.model.SearchResult
import com.carenote.app.domain.model.Task
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeSearchRepository
import com.carenote.app.ui.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchRepository: FakeSearchRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchRepository = FakeSearchRepository()
        analyticsRepository = FakeAnalyticsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(searchRepository, analyticsRepository)
    }

    private fun createMedicationResult(
        id: Long = 1L,
        name: String = "テスト薬"
    ): SearchResult.MedicationResult {
        return SearchResult.MedicationResult(
            Medication(
                id = id,
                name = name,
                createdAt = LocalDateTime.of(2026, 1, 1, 10, 0),
                updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0)
            )
        )
    }

    private fun createNoteResult(
        id: Long = 2L,
        title: String = "テストメモ"
    ): SearchResult.NoteResult {
        return SearchResult.NoteResult(
            Note(
                id = id,
                title = title,
                content = "内容",
                createdAt = LocalDateTime.of(2026, 1, 1, 11, 0),
                updatedAt = LocalDateTime.of(2026, 1, 1, 11, 0)
            )
        )
    }

    private fun createTaskResult(
        id: Long = 3L,
        title: String = "テストタスク"
    ): SearchResult.TaskResult {
        return SearchResult.TaskResult(
            Task(
                id = id,
                title = title,
                createdAt = LocalDateTime.of(2026, 1, 1, 12, 0),
                updatedAt = LocalDateTime.of(2026, 1, 1, 12, 0)
            )
        )
    }

    @Test
    fun `initial state is Success with empty list`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(emptyList<SearchResult>(), (state as UiState.Success).data)
    }

    @Test
    fun `empty query returns Success with empty list`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSearchQuery("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(emptyList<SearchResult>(), (state as UiState.Success).data)
    }

    @Test
    fun `blank query returns Success with empty list`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSearchQuery("   ")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(emptyList<SearchResult>(), (state as UiState.Success).data)
    }

    @Test
    fun `non-empty query triggers search and returns results`() = runTest(testDispatcher) {
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
    fun `debounce waits before search`() = runTest(testDispatcher) {
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
    fun `search logs analytics event`() = runTest(testDispatcher) {
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
    fun `search error emits UiState Error`() = runTest(testDispatcher) {
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
    fun `rapid query changes only triggers last search`() = runTest(testDispatcher) {
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
