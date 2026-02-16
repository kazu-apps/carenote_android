package com.carenote.app.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import java.time.LocalDate

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = resolveDayCellColors(isSelected, isToday)
    val fontWeight = resolveDayFontWeight(isSelected, isToday)
    val dayCellDescription = resolveDayCellDescription(
        dayOfMonth = date.dayOfMonth,
        isSelected = isSelected,
        isToday = isToday,
        hasEvents = hasEvents
    )

    Box(
        modifier = modifier
            .size(AppConfig.Calendar.DAY_CELL_SIZE_DP.dp)
            .clip(CircleShape)
            .background(colors.first)
            .semantics { contentDescription = dayCellDescription }
            .clickable { onDateClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = colors.second
        )

        if (hasEvents && !isSelected) {
            DayCellEventIndicator()
        }
    }
}

@Composable
private fun resolveDayCellColors(
    isSelected: Boolean,
    isToday: Boolean
): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.background
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onBackground
    }
    return backgroundColor to textColor
}

private fun resolveDayFontWeight(
    isSelected: Boolean,
    isToday: Boolean
): FontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal

@Composable
private fun resolveDayCellDescription(
    dayOfMonth: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean
): String = when {
    isSelected -> stringResource(R.string.a11y_day_cell_selected, dayOfMonth)
    isToday -> stringResource(R.string.a11y_day_cell_today, dayOfMonth)
    hasEvents -> stringResource(R.string.a11y_day_cell_has_events, dayOfMonth)
    else -> stringResource(R.string.a11y_day_cell, dayOfMonth)
}

@Composable
private fun BoxScope.DayCellEventIndicator() {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .size(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}
