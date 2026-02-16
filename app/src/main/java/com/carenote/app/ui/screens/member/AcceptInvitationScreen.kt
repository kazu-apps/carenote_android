package com.carenote.app.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptInvitationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    viewModel: AcceptInvitationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    AcceptSnackbarEffect(viewModel, snackbarHostState, context)

    AcceptInvitationScaffold(
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        AcceptInvitationBody(
            uiState = uiState,
            innerPadding = innerPadding,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToHome = onNavigateToHome
        )
    }
}

@Composable
private fun AcceptSnackbarEffect(
    viewModel: AcceptInvitationViewModel,
    snackbarHostState: SnackbarHostState,
    context: android.content.Context
) {
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
private fun AcceptInvitationScaffold(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable (
        androidx.compose.foundation.layout.PaddingValues
    ) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.accept_invitation_title
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.common_close
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content
    )
}

@Suppress("LongParameterList")
@Composable
private fun AcceptInvitationBody(
    uiState: AcceptInvitationUiState,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    viewModel: AcceptInvitationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    when (val state = uiState) {
        is AcceptInvitationUiState.Loading ->
            AcceptLoadingContent(innerPadding)
        is AcceptInvitationUiState.Content ->
            AcceptContentContent(
                innerPadding = innerPadding,
                state = state,
                onAccept = viewModel::accept,
                onDecline = {
                    viewModel.decline()
                    onNavigateBack()
                }
            )
        is AcceptInvitationUiState.Error ->
            AcceptErrorContent(
                innerPadding = innerPadding,
                state = state,
                onNavigateBack = onNavigateBack
            )
        is AcceptInvitationUiState.Success ->
            AcceptSuccessContent(
                innerPadding = innerPadding,
                onNavigateToHome = onNavigateToHome
            )
    }
}

@Composable
private fun AcceptLoadingContent(
    innerPadding: androidx.compose.foundation.layout.PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(
            modifier = Modifier.height(AppConfig.UI.CONTENT_SPACING_DP.dp)
        )
        Text(
            text = stringResource(R.string.accept_invitation_loading),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun AcceptContentContent(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    state: AcceptInvitationUiState.Content,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(
                horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.accept_invitation_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(
            modifier = Modifier.height(AppConfig.UI.SECTION_SPACING_DP.dp)
        )
        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isAccepting
        ) {
            Text(stringResource(R.string.accept_invitation_accept))
        }
        Spacer(
            modifier = Modifier.height(AppConfig.UI.ITEM_SPACING_DP.dp)
        )
        OutlinedButton(
            onClick = onDecline,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isAccepting
        ) {
            Text(stringResource(R.string.accept_invitation_decline))
        }
    }
}

@Composable
private fun AcceptErrorContent(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    state: AcceptInvitationUiState.Error,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(
                horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(AppConfig.UI.ICON_SIZE_LARGE_DP.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(
            modifier = Modifier.height(AppConfig.UI.CONTENT_SPACING_DP.dp)
        )
        Text(
            text = state.message.asString(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(
            modifier = Modifier.height(AppConfig.UI.CONTENT_SPACING_DP.dp)
        )
        OutlinedButton(onClick = onNavigateBack) {
            Text(stringResource(R.string.common_close))
        }
    }
}

@Composable
private fun AcceptSuccessContent(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(
                horizontal = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(AppConfig.UI.ICON_SIZE_XLARGE_DP.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(
            modifier = Modifier.height(AppConfig.UI.CONTENT_SPACING_DP.dp)
        )
        Text(
            text = stringResource(R.string.accept_invitation_success),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(
            modifier = Modifier.height(AppConfig.UI.SECTION_SPACING_DP.dp)
        )
        Button(
            onClick = onNavigateToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.nav_home))
        }
    }
}
