package com.carenote.app.ui.screens.billing

import android.app.Activity
import app.cash.turbine.test
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.BillingConnectionState
import com.carenote.app.fakes.FakeAnalyticsRepository
import com.carenote.app.fakes.FakeBillingRepository
import com.carenote.app.testing.MainCoroutineRule
import com.carenote.app.testing.aProductInfo
import com.carenote.app.ui.util.SnackbarEvent
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var billingRepository: FakeBillingRepository
    private lateinit var analyticsRepository: FakeAnalyticsRepository
    private lateinit var viewModel: BillingViewModel

    @Before
    fun setUp() {
        billingRepository = FakeBillingRepository()
        analyticsRepository = FakeAnalyticsRepository()
        viewModel = BillingViewModel(
            billingRepository = billingRepository,
            analyticsRepository = analyticsRepository
        )
    }

    @Test
    fun `initial state is Inactive and DISCONNECTED`() = runTest {
        viewModel.billingUiState.test {
            val state = awaitItem()
            assertFalse(state.premiumStatus.isActive)
            assertEquals(
                BillingConnectionState.DISCONNECTED,
                state.connectionState
            )
            assertTrue(state.products.isEmpty())
            assertFalse(state.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `connectBilling calls startConnection`() = runTest {
        viewModel.billingUiState.test {
            skipItems(1)

            viewModel.connectBilling()
            advanceUntilIdle()

            val state = awaitItem()
            assertEquals(
                BillingConnectionState.CONNECTED,
                state.connectionState
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadProducts success updates products`() = runTest {
        val products = listOf(
            aProductInfo(productId = "monthly"),
            aProductInfo(productId = "yearly", billingPeriod = "P1Y")
        )
        billingRepository.setProducts(products)

        viewModel.billingUiState.test {
            skipItems(1)

            viewModel.loadProducts()
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(2, state.products.size)
            assertEquals("monthly", state.products[0].productId)
            assertFalse(state.isLoading)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadProducts failure shows snackbar error`() = runTest {
        billingRepository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.loadProducts()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.billing_error_load_products,
                (event as SnackbarEvent.WithResId).messageResId
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `launchPurchase calls billingRepository`() = runTest {
        val activity = mockk<Activity>()

        viewModel.launchPurchase(activity, "monthly")
        advanceUntilIdle()

        assertEquals(1, billingRepository.launchBillingFlowCallCount)
        assertTrue(
            analyticsRepository.loggedEvents.any {
                it.first == AppConfig.Analytics.EVENT_PURCHASE_STARTED
            }
        )
    }

    @Test
    fun `restorePurchases success active shows success snackbar`() =
        runTest {
            billingRepository.setPremiumActive()

            viewModel.snackbarController.events.test {
                viewModel.restorePurchases()
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is SnackbarEvent.WithResId)
                assertEquals(
                    R.string.billing_restore_success,
                    (event as SnackbarEvent.WithResId).messageResId
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `restorePurchases no purchases shows message`() = runTest {
        viewModel.snackbarController.events.test {
            viewModel.restorePurchases()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.billing_restore_no_purchases,
                (event as SnackbarEvent.WithResId).messageResId
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `restorePurchases failure shows error snackbar`() = runTest {
        billingRepository.shouldFail = true

        viewModel.snackbarController.events.test {
            viewModel.restorePurchases()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SnackbarEvent.WithResId)
            assertEquals(
                R.string.billing_error_restore,
                (event as SnackbarEvent.WithResId).messageResId
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `premiumStatus Active reflects in billingUiState`() = runTest {
        viewModel.billingUiState.test {
            skipItems(1)

            billingRepository.setPremiumActive()
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state.premiumStatus.isActive)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `logManageSubscription logs analytics event`() {
        viewModel.logManageSubscription()

        assertTrue(
            analyticsRepository.loggedEvents.any {
                it.first == AppConfig.Analytics.EVENT_MANAGE_SUBSCRIPTION
            }
        )
    }
}
