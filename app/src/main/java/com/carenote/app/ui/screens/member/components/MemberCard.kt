package com.carenote.app.ui.screens.member.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.Member
import com.carenote.app.domain.model.MemberRole
import com.carenote.app.ui.util.DateTimeFormatters

@Composable
fun MemberCard(
    member: Member,
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.uid.take(8) + "...",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.member_joined_at,
                        DateTimeFormatters.formatDate(member.joinedAt.toLocalDate())
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = when (member.role) {
                            MemberRole.OWNER -> stringResource(R.string.member_role_owner)
                            MemberRole.MEMBER -> stringResource(R.string.member_role_member)
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}
