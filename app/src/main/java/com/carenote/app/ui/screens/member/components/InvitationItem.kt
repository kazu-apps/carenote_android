package com.carenote.app.ui.screens.member.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Invitation
import com.carenote.app.domain.model.InvitationStatus
import com.carenote.app.ui.util.DateTimeFormatters

@Composable
fun InvitationItem(
    invitation: Invitation,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppConfig.UI.CARD_ELEVATION_DP.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConfig.UI.CONTENT_SPACING_DP.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InvitationItemDetails(
                invitation = invitation,
                modifier = Modifier.weight(1f)
            )
            InvitationCancelButton(
                status = invitation.status,
                onCancelClick = onCancelClick
            )
        }
    }
}

@Composable
private fun InvitationItemDetails(
    invitation: Invitation,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppConfig.UI.SMALL_SPACING_DP.dp)
    ) {
        Text(
            text = maskEmail(invitation.inviteeEmail),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppConfig.UI.ITEM_SPACING_DP.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(status = invitation.status)
            Text(
                text = stringResource(
                    R.string.invitation_expires_at,
                    DateTimeFormatters.formatDate(invitation.expiresAt.toLocalDate())
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InvitationCancelButton(
    status: InvitationStatus,
    onCancelClick: () -> Unit
) {
    if (status == InvitationStatus.PENDING) {
        IconButton(onClick = onCancelClick) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.common_cancel),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun maskEmail(email: String): String {
    val atIndex = email.indexOf('@')
    if (atIndex <= 0) return email
    val localPart = email.substring(0, atIndex)
    val domainPart = email.substring(atIndex)
    return localPart.first() + "***" + domainPart
}

@Composable
private fun StatusChip(
    status: InvitationStatus,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (status) {
        InvitationStatus.PENDING ->
            stringResource(R.string.invitation_status_pending) to
                MaterialTheme.colorScheme.tertiary
        InvitationStatus.ACCEPTED ->
            stringResource(R.string.invitation_status_accepted) to
                MaterialTheme.colorScheme.primary
        InvitationStatus.REJECTED ->
            stringResource(R.string.invitation_status_rejected) to
                MaterialTheme.colorScheme.error
        InvitationStatus.EXPIRED ->
            stringResource(R.string.invitation_status_expired) to
                MaterialTheme.colorScheme.outline
    }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color
        ),
        modifier = modifier
    )
}
