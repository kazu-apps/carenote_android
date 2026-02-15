package com.carenote.app.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.Member
import com.carenote.app.ui.components.ConfirmDialog
import com.carenote.app.ui.components.EmptyState
import com.carenote.app.ui.components.SwipeToDismissItem
import com.carenote.app.ui.testing.TestTags
import com.carenote.app.ui.screens.member.components.InvitationItem
import com.carenote.app.ui.screens.member.components.MemberCard
import com.carenote.app.ui.util.SnackbarEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSendInvitation: () -> Unit = {},
    viewModel: MemberManagementViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val pendingInvitations by viewModel.pendingInvitations.collectAsStateWithLifecycle()
    val isOwner by viewModel.isOwner.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteMember by remember { mutableStateOf<Member?>(null) }
    var cancelInvitation by remember { mutableStateOf<Invitation?>(null) }
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
                        text = stringResource(R.string.member_management_title),
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSendInvitation,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(TestTags.MEMBER_MANAGEMENT_FAB)
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = stringResource(R.string.send_invitation_title)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (members.isEmpty() && pendingInvitations.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Group,
                message = stringResource(R.string.member_management_empty),
                actionLabel = stringResource(R.string.member_management_empty_action),
                onAction = onNavigateToSendInvitation,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                    end = AppConfig.UI.SCREEN_HORIZONTAL_PADDING_DP.dp,
                    top = AppConfig.UI.ITEM_SPACING_DP.dp,
                    bottom = AppConfig.UI.LIST_BOTTOM_PADDING_DP.dp
                ),
                verticalArrangement = Arrangement.spacedBy(AppConfig.UI.ITEM_SPACING_DP.dp)
            ) {
                if (members.isNotEmpty()) {
                    item(key = "members_header") {
                        Text(
                            text = stringResource(R.string.member_management_section_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                top = AppConfig.UI.ITEM_SPACING_DP.dp,
                                bottom = AppConfig.UI.SMALL_SPACING_DP.dp
                            )
                        )
                    }
                    items(
                        items = members,
                        key = { it.id }
                    ) { member ->
                        if (isOwner) {
                            SwipeToDismissItem(
                                item = member,
                                onDelete = { deleteMember = it }
                            ) {
                                MemberCard(member = member)
                            }
                        } else {
                            MemberCard(member = member)
                        }
                    }
                }

                if (pendingInvitations.isNotEmpty()) {
                    item(key = "invitations_header") {
                        Text(
                            text = stringResource(R.string.invitation_pending_section),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                top = AppConfig.UI.CONTENT_SPACING_DP.dp,
                                bottom = AppConfig.UI.SMALL_SPACING_DP.dp
                            )
                        )
                    }
                    items(
                        items = pendingInvitations,
                        key = { "inv_${it.id}" }
                    ) { invitation ->
                        InvitationItem(
                            invitation = invitation,
                            onCancelClick = { cancelInvitation = invitation }
                        )
                    }
                }
            }
        }
    }

    deleteMember?.let { member ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_delete_title),
            message = stringResource(R.string.member_delete_confirm),
            onConfirm = {
                viewModel.deleteMember(member.id)
                deleteMember = null
            },
            onDismiss = { deleteMember = null },
            isDestructive = true
        )
    }

    cancelInvitation?.let { invitation ->
        ConfirmDialog(
            title = stringResource(R.string.ui_confirm_title),
            message = stringResource(R.string.invitation_cancel_confirm),
            onConfirm = {
                viewModel.cancelInvitation(invitation.id)
                cancelInvitation = null
            },
            onDismiss = { cancelInvitation = null },
            isDestructive = true
        )
    }
}
