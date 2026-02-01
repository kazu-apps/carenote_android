package com.carenote.app.ui.screens.healthrecords.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ExcretionType
import com.carenote.app.domain.model.HealthRecord
import com.carenote.app.domain.model.MealAmount
import com.carenote.app.ui.components.CareNoteCard
import com.carenote.app.ui.theme.AccentError
import com.carenote.app.ui.theme.ChipShape
import com.carenote.app.ui.theme.PrimaryGreen
import com.carenote.app.ui.util.DateTimeFormatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthRecordCard(
    record: HealthRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val noData = stringResource(R.string.health_records_no_data)

    CareNoteCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateTimeFormatters.formatDateTime(record.recordedAt),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        VitalSignsRow(record = record, noData = noData)

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            record.meal?.let { meal ->
                InfoChip(
                    label = stringResource(R.string.health_records_meal),
                    value = mealAmountLabel(meal)
                )
            }
            record.excretion?.let { excretion ->
                InfoChip(
                    label = stringResource(R.string.health_records_excretion),
                    value = excretionTypeLabel(excretion)
                )
            }
        }

        if (record.conditionNote.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.conditionNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VitalSignsRow(
    record: HealthRecord,
    noData: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VitalItem(
            icon = "\uD83C\uDF21",
            value = record.temperature?.let { "%.1f".format(it) } ?: noData,
            unit = stringResource(R.string.health_records_temperature_unit),
            isAbnormal = record.temperature?.let {
                it >= AppConfig.HealthThresholds.TEMPERATURE_HIGH
            } ?: false
        )

        VitalItem(
            icon = "\uD83D\uDC93",
            value = if (record.bloodPressureHigh != null && record.bloodPressureLow != null) {
                "${record.bloodPressureHigh}/${record.bloodPressureLow}"
            } else {
                noData
            },
            unit = "",
            isAbnormal = isBloodPressureAbnormal(
                record.bloodPressureHigh,
                record.bloodPressureLow
            )
        )

        VitalItem(
            icon = "\u2764\uFE0F",
            value = record.pulse?.toString() ?: noData,
            unit = "",
            isAbnormal = record.pulse?.let {
                it >= AppConfig.HealthThresholds.PULSE_HIGH ||
                    it <= AppConfig.HealthThresholds.PULSE_LOW
            } ?: false
        )

        VitalItem(
            icon = "\u2696\uFE0F",
            value = record.weight?.let { "%.1f".format(it) } ?: noData,
            unit = stringResource(R.string.health_records_weight_unit),
            isAbnormal = false
        )
    }
}

@Composable
private fun VitalItem(
    icon: String,
    value: String,
    unit: String,
    isAbnormal: Boolean
) {
    val textColor = if (isAbnormal) {
        AccentError
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = if (unit.isNotEmpty()) "$icon$value$unit" else "$icon$value",
        style = MaterialTheme.typography.bodyMedium,
        color = textColor
    )
}

@Composable
private fun InfoChip(
    label: String,
    value: String
) {
    Surface(
        shape = ChipShape,
        color = PrimaryGreen.copy(alpha = 0.1f)
    ) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryGreen,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun isBloodPressureAbnormal(high: Int?, low: Int?): Boolean {
    if (high == null || low == null) return false
    return high >= AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_UPPER ||
        low >= AppConfig.HealthThresholds.BLOOD_PRESSURE_HIGH_LOWER
}

@Composable
fun mealAmountLabel(meal: MealAmount): String {
    return when (meal) {
        MealAmount.FULL -> stringResource(R.string.health_records_meal_full)
        MealAmount.MOSTLY -> stringResource(R.string.health_records_meal_mostly)
        MealAmount.HALF -> stringResource(R.string.health_records_meal_half)
        MealAmount.LITTLE -> stringResource(R.string.health_records_meal_little)
        MealAmount.NONE -> stringResource(R.string.health_records_meal_none)
    }
}

@Composable
fun excretionTypeLabel(excretion: ExcretionType): String {
    return when (excretion) {
        ExcretionType.NORMAL -> stringResource(R.string.health_records_excretion_normal)
        ExcretionType.SOFT -> stringResource(R.string.health_records_excretion_soft)
        ExcretionType.HARD -> stringResource(R.string.health_records_excretion_hard)
        ExcretionType.DIARRHEA -> stringResource(R.string.health_records_excretion_diarrhea)
        ExcretionType.NONE -> stringResource(R.string.health_records_excretion_none)
    }
}
