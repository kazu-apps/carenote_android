package com.carenote.app.ui.navigation.routes

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.screens.calendar.CalendarScreen
import com.carenote.app.ui.screens.healthrecords.HealthRecordsScreen
import com.carenote.app.ui.screens.home.HomeScreen
import com.carenote.app.ui.screens.medication.MedicationScreen
import com.carenote.app.ui.screens.notes.NotesScreen
import com.carenote.app.ui.screens.timeline.TimelineScreen

internal fun NavGraphBuilder.bottomNavRoutes(
    navController: NavHostController
) {
    homeRoute(navController)
    medicationListRoute(navController)
    calendarListRoute(navController)
    timelineRoute(navController)
    healthRecordsListRoute(navController)
    notesListRoute(navController)
}

internal fun NavGraphBuilder.homeRoute(navController: NavHostController) {
    composable(
        Screen.Home.route,
        enterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        exitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popEnterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popExitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) }
    ) {
        HomeScreen(
            onNavigateToSettings = {
                navController.navigate(Screen.Settings.route)
            },
            onNavigateToMedication = {
                navController.navigate(Screen.Medication.route)
            },
            onNavigateToCalendar = {
                navController.navigate(Screen.Calendar.route)
            },
            onNavigateToTimeline = {
                navController.navigate(Screen.Timeline.route)
            },
            onNavigateToHealthRecords = {
                navController.navigate(Screen.HealthRecords.route)
            },
            onNavigateToNotes = {
                navController.navigate(Screen.Notes.route)
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search.route)
            },
            onMedicationClick = { id ->
                navController.navigate(Screen.MedicationDetail.createRoute(id))
            },
            onTaskClick = { id ->
                navController.navigate(Screen.EditCalendarEvent.createRoute(id))
            },
            onHealthRecordClick = { id ->
                navController.navigate(Screen.EditHealthRecord.createRoute(id))
            },
            onNoteClick = { id ->
                navController.navigate(Screen.EditNote.createRoute(id))
            },
            onCalendarEventClick = { id ->
                navController.navigate(Screen.EditCalendarEvent.createRoute(id))
            }
        )
    }
}

internal fun NavGraphBuilder.medicationListRoute(navController: NavHostController) {
    composable(
        Screen.Medication.route,
        enterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        exitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popEnterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popExitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) }
    ) {
        MedicationScreen(
            onNavigateToAddMedication = {
                navController.navigate(Screen.AddMedication.route)
            },
            onNavigateToDetail = { medicationId ->
                navController.navigate(
                    Screen.MedicationDetail.createRoute(medicationId)
                )
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search.route)
            }
        )
    }
}

internal fun NavGraphBuilder.calendarListRoute(navController: NavHostController) {
    composable(
        Screen.Calendar.route,
        enterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        exitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popEnterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popExitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) }
    ) {
        CalendarScreen(
            onNavigateToAddEvent = {
                navController.navigate(Screen.AddCalendarEvent.createRoute())
            },
            onNavigateToEditEvent = { eventId ->
                navController.navigate(
                    Screen.EditCalendarEvent.createRoute(eventId)
                )
            },
            onNavigateToTimeline = {
                navController.navigate(Screen.Timeline.route)
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search.route)
            }
        )
    }
}

internal fun NavGraphBuilder.timelineRoute(navController: NavHostController) {
    composable(
        Screen.Timeline.route,
        enterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        exitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popEnterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popExitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) }
    ) {
        TimelineScreen(
            onNavigateToAddTask = {
                navController.navigate(Screen.AddCalendarEvent.createRoute("TASK"))
            }
        )
    }
}

internal fun NavGraphBuilder.healthRecordsListRoute(navController: NavHostController) {
    composable(
        Screen.HealthRecords.route,
        enterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        exitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popEnterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popExitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) }
    ) {
        HealthRecordsScreen(
            onNavigateToAddRecord = {
                navController.navigate(Screen.AddHealthRecord.route)
            },
            onNavigateToEditRecord = { recordId ->
                navController.navigate(
                    Screen.EditHealthRecord.createRoute(recordId)
                )
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search.route)
            }
        )
    }
}

internal fun NavGraphBuilder.notesListRoute(navController: NavHostController) {
    composable(
        Screen.Notes.route,
        enterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        exitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popEnterTransition = { fadeIn(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) },
        popExitTransition = { fadeOut(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) }
    ) {
        NotesScreen(
            onNavigateToAddNote = {
                navController.navigate(Screen.AddNote.route)
            },
            onNavigateToEditNote = { noteId ->
                navController.navigate(Screen.EditNote.createRoute(noteId))
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search.route)
            }
        )
    }
}
