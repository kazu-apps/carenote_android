package com.carenote.app.ui.screens.medication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.domain.model.Medication
import com.carenote.app.domain.model.MedicationLogStatus
import com.carenote.app.ui.components.CareNoteCard
import com.carenote.app.ui.theme.AccentSuccess

/**
 * 服薬カードコンポーネント
 *
 * 薬名、用量、タイミングチップ、ステータス、アクションボタンを表示する。
 *
 * @param medication 薬データ
 * @param status 現在のステータス（null = 未記録）
 * @param onTaken 「飲んだ」ボタン押下
 * @param onSkipped 「飲めなかった」ボタン押下
 * @param onPostponed 「後で」ボタン押下
 * @param onClick カードタップ時のコールバック
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationCard(
    medication: Medication,
    status: MedicationLogStatus?,
    onTaken: () -> Unit,
    onSkipped: () -> Unit,
    onPostponed: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CareNoteCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (medication.dosage.isNotBlank()) {
                    Text(
                        text = medication.dosage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (status != null) {
                StatusBadge(status = status)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            medication.timings.forEach { timing ->
                MedicationTimingChip(timing = timing)
            }
        }

        if (status == null) {
            Spacer(modifier = Modifier.height(12.dp))
            ActionButtons(
                onTaken = onTaken,
                onSkipped = onSkipped,
                onPostponed = onPostponed
            )
        }
    }
}

@Composable
private fun StatusBadge(status: MedicationLogStatus) {
    val (text, color) = when (status) {
        MedicationLogStatus.TAKEN -> stringResource(R.string.medication_status_taken) to AccentSuccess
        MedicationLogStatus.SKIPPED -> stringResource(R.string.medication_status_skipped) to MaterialTheme.colorScheme.error
        MedicationLogStatus.POSTPONED -> stringResource(R.string.medication_status_postponed) to MaterialTheme.colorScheme.tertiary
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}

@Composable
private fun ActionButtons(
    onTaken: () -> Unit,
    onSkipped: () -> Unit,
    onPostponed: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = onTaken,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.medication_taken),
                style = MaterialTheme.typography.labelMedium
            )
        }
        OutlinedButton(
            onClick = onSkipped,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.medication_skipped),
                style = MaterialTheme.typography.labelMedium
            )
        }
        OutlinedButton(
            onClick = onPostponed,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.medication_postponed),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
