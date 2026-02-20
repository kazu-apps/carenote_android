package com.carenote.app.ui.screens.billing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.screens.settings.sections.PremiumSection
import com.carenote.app.ui.util.SnackbarEvent

@Composable
fun BillingScreen(
    onNavigateBack: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val billingUiState by viewModel.billingUiState
        .collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    BillingEffects(viewModel, snackbarHostState)
    BillingScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        billingContent = {
            BillingBody(
                billingUiState = billingUiState,
                viewModel = viewModel,
                modifier = it
            )
        }
    )
}

@Composable
private fun BillingEffects(
    viewModel: BillingViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.connectBilling()
        viewModel.loadProducts()
    }
    LaunchedEffect(Unit) {
        viewModel.snackbarController.events.collect { event ->
            val message = when (event) {
                is SnackbarEvent.WithResId ->
                    context.getString(event.messageResId)
                is SnackbarEvent.WithString -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillingScaffold(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    billingContent: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.billing_screen_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled
                                .ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        billingContent(Modifier.padding(innerPadding))
    }
}

@Composable
private fun BillingBody(
    billingUiState: com.carenote.app.ui.viewmodel.BillingUiState,
    viewModel: BillingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LazyColumn(modifier = modifier) {
        item(key = "premium") {
            PremiumSection(
                premiumStatus = billingUiState.premiumStatus,
                connectionState = billingUiState.connectionState,
                products = billingUiState.products,
                isLoading = billingUiState.isLoading,
                onPurchaseClick = { productId ->
                    (context as? Activity)?.let { activity ->
                        viewModel.launchPurchase(activity, productId)
                    }
                },
                onRestoreClick = { viewModel.restorePurchases() },
                onManageClick = {
                    viewModel.logManageSubscription()
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            AppConfig.Billing
                                .GOOGLE_PLAY_SUBSCRIPTION_URL
                        )
                    )
                    context.startActivity(intent)
                }
            )
        }
    }
}
