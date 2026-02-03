package com.carenote.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * CareNote 固有のカスタムカラー
 * Material3 ColorScheme に含まれないアプリ固有色を保持する
 */
@Immutable
data class CareNoteColorScheme(
    // Medication Timing
    val morningColor: Color,
    val noonColor: Color,
    val eveningColor: Color,
    val morningTextColor: Color,
    val noonTextColor: Color,
    val eveningTextColor: Color,
    // Note Tag
    val noteTagConditionColor: Color,
    val noteTagMealColor: Color,
    val noteTagReportColor: Color,
    val noteTagOtherColor: Color,
    val noteTagConditionTextColor: Color,
    val noteTagMealTextColor: Color,
    val noteTagReportTextColor: Color,
    val noteTagOtherTextColor: Color,
    // Semantic
    val accentSuccess: Color,
    val accentWarning: Color,
    val accentError: Color,
    // Task Priority
    val taskPriorityHighColor: Color,
    val taskPriorityLowColor: Color,
    // Chart
    val chartLineColor: Color,
    val chartSecondaryLineColor: Color,
    val chartLabelColor: Color
)

val LightCareNoteColors = CareNoteColorScheme(
    morningColor = MorningColor,
    noonColor = NoonColor,
    eveningColor = EveningColor,
    morningTextColor = MorningTextColor,
    noonTextColor = NoonTextColor,
    eveningTextColor = EveningTextColor,
    noteTagConditionColor = NoteTagConditionColor,
    noteTagMealColor = NoteTagMealColor,
    noteTagReportColor = NoteTagReportColor,
    noteTagOtherColor = NoteTagOtherColor,
    noteTagConditionTextColor = NoteTagConditionTextColor,
    noteTagMealTextColor = NoteTagMealTextColor,
    noteTagReportTextColor = NoteTagReportTextColor,
    noteTagOtherTextColor = NoteTagOtherTextColor,
    accentSuccess = AccentSuccess,
    accentWarning = AccentWarning,
    accentError = AccentError,
    taskPriorityHighColor = AccentError,
    taskPriorityLowColor = TaskPriorityLowColor,
    chartLineColor = PrimaryGreen,
    chartSecondaryLineColor = AccentGreen,
    chartLabelColor = TextSecondary
)

val DarkCareNoteColors = CareNoteColorScheme(
    morningColor = DarkMorningColor,
    noonColor = DarkNoonColor,
    eveningColor = DarkEveningColor,
    morningTextColor = DarkMorningTextColor,
    noonTextColor = DarkNoonTextColor,
    eveningTextColor = DarkEveningTextColor,
    noteTagConditionColor = DarkNoteTagConditionColor,
    noteTagMealColor = DarkNoteTagMealColor,
    noteTagReportColor = DarkNoteTagReportColor,
    noteTagOtherColor = DarkNoteTagOtherColor,
    noteTagConditionTextColor = DarkNoteTagConditionTextColor,
    noteTagMealTextColor = DarkNoteTagMealTextColor,
    noteTagReportTextColor = DarkNoteTagReportTextColor,
    noteTagOtherTextColor = DarkNoteTagOtherTextColor,
    accentSuccess = DarkAccentSuccess,
    accentWarning = DarkAccentWarning,
    accentError = DarkAccentError,
    taskPriorityHighColor = DarkAccentError,
    taskPriorityLowColor = DarkTaskPriorityLowColor,
    chartLineColor = DarkPrimaryGreen,
    chartSecondaryLineColor = DarkAccentGreen,
    chartLabelColor = DarkTextSecondary
)

val LocalCareNoteColors = staticCompositionLocalOf { LightCareNoteColors }

/**
 * CareNote 固有カラーへのアクセスオブジェクト
 * Usage: CareNoteColors.current.morningColor
 */
object CareNoteColors {
    val current: CareNoteColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCareNoteColors.current
}
