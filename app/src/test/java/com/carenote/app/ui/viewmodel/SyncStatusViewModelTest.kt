package com.carenote.app.ui.viewmodel

import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.domain.common.DomainError
import com.carenote.app.domain.common.SyncState
import com.carenote.app.fakes.FakeSyncRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.ui.util.SnackbarEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SyncStatusViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeSyncRepository: FakeSyncRepository
    private lateinit var viewModel: SyncStatusViewModel

    @Before
    fun setUp() {
        fakeSyncRepository = FakeSyncRepository()
    }

    private fun createViewModel(): SyncStatusViewModel {
        return SyncStatusViewModel(fakeSyncRepository)
    }

    @Test
    fun `Error state emits snackbar event`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.snackbarController.events.test {
            fakeSyncRepository.setSyncState(
                SyncState.Error(DomainError.NetworkError("Network error"))
            )
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.sync_error_snackbar, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `Success after Syncing emits success snackbar`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When: first transition to Syncing, then Success
        viewModel.snackbarController.events.test {
            fakeSyncRepository.setSyncState(SyncState.Syncing(0.5f, "medications"))
            advanceUntilIdle()

            fakeSyncRepository.setSyncState(SyncState.Success(LocalDateTime.now()))
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(R.string.sync_complete_snackbar, (event as SnackbarEvent.WithResId).messageResId)
        }
    }

    @Test
    fun `Success without prior Syncing does NOT emit snackbar`() = runTest {
        // Given: cold start guard -- Success emitted without Syncing first
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.snackbarController.events.test {
            fakeSyncRepository.setSyncState(SyncState.Success(LocalDateTime.now()))
            advanceUntilIdle()

            // Then: no event should be emitted
            expectNoEvents()
        }
    }

    @Test
    fun `Idle does not emit snackbar`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.snackbarController.events.test {
            // Idle is already the initial state; set it again to be explicit
            fakeSyncRepository.setSyncState(SyncState.Idle)
            advanceUntilIdle()

            // Then
            expectNoEvents()
        }
    }

    @Test
    fun `Syncing does not emit snackbar`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.snackbarController.events.test {
            fakeSyncRepository.setSyncState(SyncState.Syncing(0.3f, "notes"))
            advanceUntilIdle()

            // Then
            expectNoEvents()
        }
    }
}
