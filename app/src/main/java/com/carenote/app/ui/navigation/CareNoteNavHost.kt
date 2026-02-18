package com.carenote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.util.readAssetText
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.carenote.app.ui.screens.calendar.AddEditCalendarEventScreen
import com.carenote.app.ui.screens.calendar.CalendarScreen
import com.carenote.app.ui.screens.healthrecords.HealthRecordsScreen
import com.carenote.app.ui.screens.medication.AddEditMedicationScreen
import com.carenote.app.ui.screens.medication.MedicationDetailScreen
import com.carenote.app.ui.screens.medication.MedicationScreen
import com.carenote.app.ui.screens.healthrecords.AddEditHealthRecordScreen
import com.carenote.app.ui.screens.notes.AddEditNoteScreen
import com.carenote.app.ui.screens.notes.NotesScreen
import com.carenote.app.ui.screens.auth.ForgotPasswordScreen
import com.carenote.app.ui.screens.auth.LoginScreen
import com.carenote.app.ui.screens.auth.RegisterScreen
import com.carenote.app.ui.screens.carerecipient.CareRecipientScreen
import com.carenote.app.ui.screens.home.HomeScreen
import com.carenote.app.ui.screens.onboarding.OnboardingWelcomeScreen
import com.carenote.app.ui.screens.emergencycontact.AddEditEmergencyContactScreen
import com.carenote.app.ui.screens.emergencycontact.EmergencyContactListScreen
import com.carenote.app.ui.screens.member.AcceptInvitationScreen
import com.carenote.app.ui.screens.member.MemberManagementScreen
import com.carenote.app.ui.screens.member.SendInvitationScreen
import com.carenote.app.ui.screens.search.SearchScreen
import com.carenote.app.ui.screens.settings.LegalDocumentScreen
import com.carenote.app.ui.screens.settings.SettingsScreen
import androidx.compose.runtime.LaunchedEffect
import com.carenote.app.ui.screens.timeline.TimelineScreen
import com.carenote.app.domain.model.SearchResult

@Composable
fun CareNoteNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        bottomNavRoutes(navController)
        settingsRoutes(navController)
        searchRoute(navController)
        onboardingRoutes(navController)
        emergencyContactRoutes(navController)
        memberRoutes(navController)
        legalRoutes(navController)
        medicationRoutes(navController)
        noteRoutes(navController)
        healthRecordRoutes(navController)
        calendarRoutes(navController)
        taskRoutes(navController)
        authRoutes(navController)
    }
}

private fun NavGraphBuilder.bottomNavRoutes(
    navController: NavHostController
) {
    homeRoute(navController)
    medicationListRoute(navController)
    calendarListRoute(navController)
    timelineRoute(navController)
    healthRecordsListRoute(navController)
    notesListRoute(navController)
}

private fun NavGraphBuilder.homeRoute(navController: NavHostController) {
    composable(Screen.Home.route) {
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
            }
        )
    }
}

private fun NavGraphBuilder.medicationListRoute(navController: NavHostController) {
    composable(Screen.Medication.route) {
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

private fun NavGraphBuilder.calendarListRoute(navController: NavHostController) {
    composable(Screen.Calendar.route) {
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

private fun NavGraphBuilder.timelineRoute(navController: NavHostController) {
    composable(Screen.Timeline.route) {
        TimelineScreen(
            onNavigateToAddTask = {
                navController.navigate(Screen.AddCalendarEvent.createRoute("TASK"))
            }
        )
    }
}

private fun NavGraphBuilder.healthRecordsListRoute(navController: NavHostController) {
    composable(Screen.HealthRecords.route) {
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

private fun NavGraphBuilder.notesListRoute(navController: NavHostController) {
    composable(Screen.Notes.route) {
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

private fun NavGraphBuilder.settingsRoutes(
    navController: NavHostController
) {
    composable(Screen.Settings.route) {
        SettingsScreen(
            onNavigateToPrivacyPolicy = {
                navController.navigate(Screen.PrivacyPolicy.route)
            },
            onNavigateToTermsOfService = {
                navController.navigate(Screen.TermsOfService.route)
            },
            onNavigateToCareRecipient = {
                navController.navigate(Screen.CareRecipientProfile.route)
            },
            onNavigateToEmergencyContacts = {
                navController.navigate(Screen.EmergencyContacts.route)
            },
            onNavigateToMemberManagement = {
                navController.navigate(Screen.MemberManagement.route)
            }
        )
    }
}

private fun NavGraphBuilder.searchRoute(
    navController: NavHostController
) {
    composable(Screen.Search.route) {
        SearchScreen(
            onNavigateBack = { navController.popBackStack() },
            onResultClick = { result ->
                navigateToSearchResult(navController, result)
            }
        )
    }
}

private fun navigateToSearchResult(
    navController: NavHostController,
    result: SearchResult
) {
    val route = when (result) {
        is SearchResult.MedicationResult ->
            Screen.MedicationDetail.createRoute(result.id)
        is SearchResult.NoteResult ->
            Screen.EditNote.createRoute(result.id)
        is SearchResult.HealthRecordResult ->
            Screen.EditHealthRecord.createRoute(result.id)
        is SearchResult.CalendarEventResult ->
            Screen.EditCalendarEvent.createRoute(result.id)
        is SearchResult.EmergencyContactResult ->
            Screen.EditEmergencyContact.createRoute(result.id)
    }
    navController.navigate(route)
}

private fun NavGraphBuilder.onboardingRoutes(
    navController: NavHostController
) {
    composable(Screen.OnboardingWelcome.route) {
        OnboardingWelcomeScreen(
            onStartClick = {
                navController.navigate("onboarding_care_recipient") {
                    popUpTo(Screen.OnboardingWelcome.route) {
                        inclusive = true
                    }
                }
            }
        )
    }

    composable("onboarding_care_recipient") {
        CareRecipientScreen(
            onNavigateBack = {},
            showBackButton = false,
            onSaveSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(Screen.CareRecipientProfile.route) {
        CareRecipientScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.emergencyContactRoutes(
    navController: NavHostController
) {
    composable(Screen.EmergencyContacts.route) {
        EmergencyContactListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAdd = {
                navController.navigate(Screen.AddEmergencyContact.route)
            },
            onNavigateToEdit = { contactId ->
                navController.navigate(
                    Screen.EditEmergencyContact.createRoute(contactId)
                )
            }
        )
    }

    composable(Screen.AddEmergencyContact.route) {
        AddEditEmergencyContactScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.EditEmergencyContact.route,
        arguments = listOf(
            navArgument("contactId") { type = NavType.LongType }
        )
    ) {
        AddEditEmergencyContactScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.memberRoutes(
    navController: NavHostController
) {
    composable(Screen.MemberManagement.route) {
        MemberManagementScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSendInvitation = {
                navController.navigate(Screen.SendInvitation.route)
            }
        )
    }

    composable(Screen.SendInvitation.route) {
        SendInvitationScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.AcceptInvitation.route,
        arguments = listOf(
            navArgument("invitationToken") {
                type = NavType.StringType
            }
        ),
        deepLinks = acceptInvitationDeepLinks()
    ) {
        AcceptInvitationScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun acceptInvitationDeepLinks() = listOf(
    navDeepLink {
        uriPattern = "https://" +
            AppConfig.Member.DEEP_LINK_HOST +
            AppConfig.Member.DEEP_LINK_PATH_PREFIX +
            "/{invitationToken}"
    },
    navDeepLink {
        uriPattern = AppConfig.Notification.DEEP_LINK_SCHEME +
            "://accept_invitation/{invitationToken}"
    }
)

private fun NavGraphBuilder.legalRoutes(
    navController: NavHostController
) {
    composable(Screen.PrivacyPolicy.route) {
        PrivacyPolicyRoute(navController)
    }

    composable(Screen.TermsOfService.route) {
        TermsOfServiceRoute(navController)
    }
}

@Composable
private fun PrivacyPolicyRoute(navController: NavHostController) {
    val context = LocalContext.current
    val locale = ConfigurationCompat.getLocales(
        LocalConfiguration.current
    )[0]
    val isJapanese = locale?.language == "ja"
    val fileName = if (isJapanese) {
        "legal/privacy_policy_ja.txt"
    } else {
        "legal/privacy_policy_en.txt"
    }
    val content by produceState(initialValue = "", fileName) {
        value = withContext(Dispatchers.IO) {
            readAssetText(context, fileName)
        }
    }
    LegalDocumentScreen(
        title = stringResource(R.string.legal_privacy_policy_title),
        content = content,
        lastUpdated = AppConfig.Legal.PRIVACY_POLICY_LAST_UPDATED,
        onNavigateBack = { navController.popBackStack() }
    )
}

@Composable
private fun TermsOfServiceRoute(navController: NavHostController) {
    val context = LocalContext.current
    val locale = ConfigurationCompat.getLocales(
        LocalConfiguration.current
    )[0]
    val isJapanese = locale?.language == "ja"
    val fileName = if (isJapanese) {
        "legal/terms_of_service_ja.txt"
    } else {
        "legal/terms_of_service_en.txt"
    }
    val content by produceState(initialValue = "", fileName) {
        value = withContext(Dispatchers.IO) {
            readAssetText(context, fileName)
        }
    }
    LegalDocumentScreen(
        title = stringResource(R.string.legal_terms_of_service_title),
        content = content,
        lastUpdated = AppConfig.Legal.TERMS_OF_SERVICE_LAST_UPDATED,
        onNavigateBack = { navController.popBackStack() }
    )
}

private fun NavGraphBuilder.medicationRoutes(
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

private fun NavGraphBuilder.noteRoutes(
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

private fun NavGraphBuilder.healthRecordRoutes(
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

private fun NavGraphBuilder.calendarRoutes(
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

private fun NavGraphBuilder.taskRoutes(
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

private fun NavGraphBuilder.authRoutes(
    navController: NavHostController
) {
    composable(Screen.Login.route) {
        LoginScreen(
            onNavigateToRegister = {
                navController.navigate(Screen.Register.route)
            },
            onNavigateToForgotPassword = {
                navController.navigate(Screen.ForgotPassword.route)
            },
            onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
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
                    popUpTo(Screen.Register.route) {
                        inclusive = true
                    }
                }
            },
            onRegisterSuccess = {
                navController.navigate(Screen.Home.route) {
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
