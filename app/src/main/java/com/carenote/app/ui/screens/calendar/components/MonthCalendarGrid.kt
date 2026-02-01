package com.carenote.app.ui.screens.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    eventsForMonth: Map<LocalDate, List<CalendarEvent>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = sundayBasedOffset(firstDayOfWeek)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WeekdayHeader()

        for (row in 0 until AppConfig.Calendar.CALENDAR_ROWS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until DAYS_IN_WEEK) {
                    val dayIndex = row * DAYS_IN_WEEK + col - startOffset + 1
                    if (dayIndex in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayIndex)
                        DayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            hasEvents = eventsForMonth.containsKey(date),
                            onDateClick = onDateClick
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(AppConfig.Calendar.DAY_CELL_SIZE_DP.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val weekdayLabels = listOf(
        R.string.calendar_weekday_sun,
        R.string.calendar_weekday_mon,
        R.string.calendar_weekday_tue,
        R.string.calendar_weekday_wed,
        R.string.calendar_weekday_thu,
        R.string.calendar_weekday_fri,
        R.string.calendar_weekday_sat
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekdayLabels.forEach { resId ->
            Box(
                modifier = Modifier.size(AppConfig.Calendar.DAY_CELL_SIZE_DP.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resId),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun sundayBasedOffset(dayOfWeek: DayOfWeek): Int {
    return if (dayOfWeek == DayOfWeek.SUNDAY) 0 else dayOfWeek.value
}

private const val DAYS_IN_WEEK = 7
