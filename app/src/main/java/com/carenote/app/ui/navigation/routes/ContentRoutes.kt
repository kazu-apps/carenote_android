package com.carenote.app.ui.navigation.routes

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.screens.calendar.AddEditCalendarEventScreen
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordScreen
import com.carenote.app.ui.screens.medication.AddEditMedicationScreen
import com.carenote.app.ui.screens.medication.MedicationDetailScreen
import com.carenote.app.ui.screens.notes.AddEditNoteScreen

internal fun NavGraphBuilder.medicationRoutes(
    navController: NavHostController
) {
    composable(Screen.AddMedication.route) {
        AddEditMedicationScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.EditMedication.route,
        arguments = listOf(
            navArgument("medicationId") { type = NavType.LongType }
        )
    ) {
        AddEditMedicationScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.MedicationDetail.route,
        arguments = listOf(
            navArgument("medicationId") { type = NavType.LongType }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = AppConfig.Notification.DEEP_LINK_SCHEME +
                    "://medication_detail/{medicationId}"
            }
        )
    ) {
        MedicationDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEdit = { medicationId ->
                navController.navigate(
                    Screen.EditMedication.createRoute(medicationId)
                )
            }
        )
    }
}

internal fun NavGraphBuilder.noteRoutes(
    navController: NavHostController
) {
    composable(Screen.AddNote.route) {
        AddEditNoteScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.EditNote.route,
        arguments = listOf(
            navArgument("noteId") { type = NavType.LongType }
        )
    ) {
        AddEditNoteScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

internal fun NavGraphBuilder.healthRecordRoutes(
    navController: NavHostController
) {
    composable(Screen.AddHealthRecord.route) {
        AddEditHealthRecordScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.EditHealthRecord.route,
        arguments = listOf(
            navArgument("recordId") { type = NavType.LongType }
        )
    ) {
        AddEditHealthRecordScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

internal fun NavGraphBuilder.calendarRoutes(
    navController: NavHostController
) {
    composable(
        route = Screen.AddCalendarEvent.route,
        arguments = listOf(
            navArgument("type") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        AddEditCalendarEventScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.EditCalendarEvent.route,
        arguments = listOf(
            navArgument("eventId") { type = NavType.LongType }
        )
    ) {
        AddEditCalendarEventScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

internal fun NavGraphBuilder.taskRoutes(
    navController: NavHostController
) {
    composable(Screen.AddTask.route) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.AddCalendarEvent.createRoute("TASK")) {
                popUpTo(Screen.AddTask.route) { inclusive = true }
            }
        }
    }

    composable(
        route = Screen.EditTask.route,
        arguments = listOf(
            navArgument("taskId") { type = NavType.LongType }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = AppConfig.Notification.DEEP_LINK_SCHEME +
                    "://edit_task/{taskId}"
            }
        )
    ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable
        LaunchedEffect(taskId) {
            navController.navigate(Screen.EditCalendarEvent.createRoute(taskId)) {
                popUpTo(Screen.EditTask.route) { inclusive = true }
            }
        }
    }
}
