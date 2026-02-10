package com.carenote.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Contacts
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

    data object Login : Screen(
        route = "login",
        titleResId = R.string.auth_login_title,
        selectedIcon = Icons.AutoMirrored.Filled.Login,
        unselectedIcon = Icons.AutoMirrored.Outlined.Login
    )

    data object Register : Screen(
        route = "register",
        titleResId = R.string.auth_register_title,
        selectedIcon = Icons.Filled.PersonAdd,
        unselectedIcon = Icons.Outlined.PersonAdd
    )

    data object ForgotPassword : Screen(
        route = "forgot_password",
        titleResId = R.string.auth_forgot_password_title,
        selectedIcon = Icons.Filled.LockReset,
        unselectedIcon = Icons.Outlined.LockReset
    )

    data object PrivacyPolicy : Screen(
        route = "privacy_policy",
        titleResId = R.string.legal_privacy_policy_title,
        selectedIcon = Icons.Filled.Policy,
        unselectedIcon = Icons.Outlined.Policy
    )

    data object TermsOfService : Screen(
        route = "terms_of_service",
        titleResId = R.string.legal_terms_of_service_title,
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    )

    data object CareRecipientProfile : Screen(
        route = "care_recipient_profile",
        titleResId = R.string.care_recipient_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    data object Timeline : Screen(
        route = "timeline",
        titleResId = R.string.timeline_title,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )

    data object AddMedication : Screen(
        route = "add_medication",
        titleResId = R.string.medication_add,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    )

    data object EditMedication : Screen(
        route = "edit_medication/{medicationId}",
        titleResId = R.string.medication_edit,
        selectedIcon = Icons.Filled.Medication,
        unselectedIcon = Icons.Outlined.Medication
    ) {
        fun createRoute(medicationId: Long): String = "edit_medication/$medicationId"
    }

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

    data object EmergencyContacts : Screen(
        route = "emergency_contacts",
        titleResId = R.string.emergency_contact_title,
        selectedIcon = Icons.Filled.Contacts,
        unselectedIcon = Icons.Outlined.Contacts
    )

    data object AddEmergencyContact : Screen(
        route = "add_emergency_contact",
        titleResId = R.string.emergency_contact_add,
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    )

    data object EditEmergencyContact : Screen(
        route = "edit_emergency_contact/{contactId}",
        titleResId = R.string.emergency_contact_edit,
        selectedIcon = Icons.Filled.Contacts,
        unselectedIcon = Icons.Outlined.Contacts
    ) {
        fun createRoute(contactId: Long): String = "edit_emergency_contact/$contactId"
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
        val bottomNavItems get() = listOf(Medication, Calendar, Tasks, HealthRecords, Notes, Settings)
        val authScreens get() = listOf(Login, Register, ForgotPassword)
    }
}
