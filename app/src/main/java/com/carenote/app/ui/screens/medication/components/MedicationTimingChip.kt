package com.carenote.app.ui.screens.medication.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.domain.model.MedicationTiming
import com.carenote.app.ui.theme.ChipShape
import com.carenote.app.ui.theme.EveningColor
import com.carenote.app.ui.theme.MorningColor
import com.carenote.app.ui.theme.NoonColor

/**
 * タイミング表示チップ
 *
 * 朝/昼/夕のタイミングを色分けして表示する。
 */
@Composable
fun MedicationTimingChip(
    timing: MedicationTiming,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (timing) {
        MedicationTiming.MORNING -> MorningColor
        MedicationTiming.NOON -> NoonColor
        MedicationTiming.EVENING -> EveningColor
    }

    val label = when (timing) {
        MedicationTiming.MORNING -> stringResource(R.string.medication_morning)
        MedicationTiming.NOON -> stringResource(R.string.medication_noon)
        MedicationTiming.EVENING -> stringResource(R.string.medication_evening)
    }

    Surface(
        modifier = modifier,
        shape = ChipShape,
        color = backgroundColor.copy(alpha = 0.2f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = backgroundColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
