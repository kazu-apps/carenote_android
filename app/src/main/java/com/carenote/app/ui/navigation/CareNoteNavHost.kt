package com.carenote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.carenote.app.ui.screens.calendar.CalendarScreen
import com.carenote.app.ui.screens.healthrecords.HealthRecordsScreen
import com.carenote.app.ui.screens.medication.AddMedicationScreen
import com.carenote.app.ui.screens.medication.MedicationDetailScreen
import com.carenote.app.ui.screens.medication.MedicationScreen
import com.carenote.app.ui.screens.notes.AddEditNoteScreen
import com.carenote.app.ui.screens.notes.NotesScreen
import com.carenote.app.ui.screens.settings.SettingsScreen
import com.carenote.app.ui.screens.tasks.TasksScreen

@Composable
fun CareNoteNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Medication.route,
        modifier = modifier
    ) {
        composable(Screen.Medication.route) {
            MedicationScreen(
                onNavigateToAddMedication = {
                    navController.navigate(Screen.AddMedication.route)
                },
                onNavigateToDetail = { medicationId ->
                    navController.navigate(Screen.MedicationDetail.createRoute(medicationId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen()
        }

        composable(Screen.Tasks.route) {
            TasksScreen()
        }

        composable(Screen.HealthRecords.route) {
            HealthRecordsScreen()
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateToAddNote = {
                    navController.navigate(Screen.AddNote.route)
                },
                onNavigateToEditNote = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.AddMedication.route) {
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MedicationDetail.route,
            arguments = listOf(
                navArgument("medicationId") { type = NavType.LongType }
            )
        ) {
            MedicationDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

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
}
