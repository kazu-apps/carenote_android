package com.carenote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.util.readAssetText
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.carenote.app.ui.screens.calendar.AddEditCalendarEventScreen
import com.carenote.app.ui.screens.calendar.CalendarScreen
import com.carenote.app.ui.screens.healthrecords.HealthRecordsScreen
import com.carenote.app.ui.screens.medication.AddMedicationScreen
import com.carenote.app.ui.screens.medication.MedicationDetailScreen
import com.carenote.app.ui.screens.medication.MedicationScreen
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordScreen
import com.carenote.app.ui.screens.notes.AddEditNoteScreen
import com.carenote.app.ui.screens.notes.NotesScreen
import com.carenote.app.ui.screens.auth.ForgotPasswordScreen
import com.carenote.app.ui.screens.auth.LoginScreen
import com.carenote.app.ui.screens.auth.RegisterScreen
import com.carenote.app.ui.screens.settings.LegalDocumentScreen
import com.carenote.app.ui.screens.settings.SettingsScreen
import com.carenote.app.ui.screens.tasks.AddEditTaskScreen
import com.carenote.app.ui.screens.tasks.TasksScreen

@Composable
fun CareNoteNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Medication.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
            CalendarScreen(
                onNavigateToAddEvent = {
                    navController.navigate(Screen.AddCalendarEvent.route)
                },
                onNavigateToEditEvent = { eventId ->
                    navController.navigate(Screen.EditCalendarEvent.createRoute(eventId))
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddTask.route)
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                }
            )
        }

        composable(Screen.HealthRecords.route) {
            HealthRecordsScreen(
                onNavigateToAddRecord = {
                    navController.navigate(Screen.AddHealthRecord.route)
                },
                onNavigateToEditRecord = { recordId ->
                    navController.navigate(Screen.EditHealthRecord.createRoute(recordId))
                }
            )
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
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacyPolicy = {
                    navController.navigate(Screen.PrivacyPolicy.route)
                },
                onNavigateToTermsOfService = {
                    navController.navigate(Screen.TermsOfService.route)
                }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            val context = LocalContext.current
            val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]
            val isJapanese = locale?.language == "ja"
            val fileName = if (isJapanese) "legal/privacy_policy_ja.txt" else "legal/privacy_policy_en.txt"
            val content = remember(fileName) { readAssetText(context, fileName) }
            LegalDocumentScreen(
                title = stringResource(R.string.legal_privacy_policy_title),
                content = content,
                lastUpdated = AppConfig.Legal.PRIVACY_POLICY_LAST_UPDATED,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TermsOfService.route) {
            val context = LocalContext.current
            val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0]
            val isJapanese = locale?.language == "ja"
            val fileName = if (isJapanese) "legal/terms_of_service_ja.txt" else "legal/terms_of_service_en.txt"
            val content = remember(fileName) { readAssetText(context, fileName) }
            LegalDocumentScreen(
                title = stringResource(R.string.legal_terms_of_service_title),
                content = content,
                lastUpdated = AppConfig.Legal.TERMS_OF_SERVICE_LAST_UPDATED,
                onNavigateBack = { navController.popBackStack() }
            )
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

        composable(Screen.AddCalendarEvent.route) {
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

        composable(Screen.AddTask.route) {
            AddEditTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType }
            )
        ) {
            AddEditTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Medication.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Medication.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
