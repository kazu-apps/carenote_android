package com.carenote.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.carenote.app.R

sealed class Screen(
    val route: String,
    @StringRes val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Medication : Screen(
        route = "medication",
        titleResId = R.string.nav_medication,
        selectedIcon = Icons.Filled.Medication,
        unselectedIcon = Icons.Outlined.Medication
    )

    data object Calendar : Screen(
        route = "calendar",
        titleResId = R.string.nav_calendar,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    data object Tasks : Screen(
        route = "tasks",
        titleResId = R.string.nav_tasks,
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    )

    data object HealthRecords : Screen(
        route = "health_records",
        titleResId = R.string.nav_health_records,
        selectedIcon = Icons.Filled.MonitorHeart,
        unselectedIcon = Icons.Outlined.MonitorHeart
    )

    data object Notes : Screen(
        route = "notes",
        titleResId = R.string.nav_notes,
        selectedIcon = Icons.AutoMirrored.Filled.StickyNote2,
        unselectedIcon = Icons.AutoMirrored.Outlined.StickyNote2
    )

    data object Settings : Screen(
        route = "settings",
        titleResId = R.string.settings_title,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object AddMedication : Screen(
        route = "add_medication",
        titleResId = R.string.medication_add,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    )

    data object MedicationDetail : Screen(
        route = "medication_detail/{medicationId}",
        titleResId = R.string.medication_detail,
        selectedIcon = Icons.Filled.Medication,
        unselectedIcon = Icons.Outlined.Medication
    ) {
        fun createRoute(medicationId: Long): String = "medication_detail/$medicationId"
    }

    data object AddNote : Screen(
        route = "add_note",
        titleResId = R.string.notes_add,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    )

    data object EditNote : Screen(
        route = "edit_note/{noteId}",
        titleResId = R.string.notes_edit,
        selectedIcon = Icons.AutoMirrored.Filled.StickyNote2,
        unselectedIcon = Icons.AutoMirrored.Outlined.StickyNote2
    ) {
        fun createRoute(noteId: Long): String = "edit_note/$noteId"
    }

    data object AddHealthRecord : Screen(
        route = "add_health_record",
        titleResId = R.string.health_records_add,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    )

    data object EditHealthRecord : Screen(
        route = "edit_health_record/{recordId}",
        titleResId = R.string.health_records_edit,
        selectedIcon = Icons.Filled.MonitorHeart,
        unselectedIcon = Icons.Outlined.MonitorHeart
    ) {
        fun createRoute(recordId: Long): String = "edit_health_record/$recordId"
    }

    data object AddCalendarEvent : Screen(
        route = "add_calendar_event",
        titleResId = R.string.calendar_add_event,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    )

    data object EditCalendarEvent : Screen(
        route = "edit_calendar_event/{eventId}",
        titleResId = R.string.calendar_edit_event,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ) {
        fun createRoute(eventId: Long): String = "edit_calendar_event/$eventId"
    }

    data object AddTask : Screen(
        route = "add_task",
        titleResId = R.string.tasks_add,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    )

    data object EditTask : Screen(
        route = "edit_task/{taskId}",
        titleResId = R.string.tasks_edit,
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    ) {
        fun createRoute(taskId: Long): String = "edit_task/$taskId"
    }

    companion object {
        val bottomNavItems = listOf(Medication, Calendar, Tasks, HealthRecords, Notes)
    }
}
