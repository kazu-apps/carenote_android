package com.carenote.app.ui.navigation.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.carenote.app.R
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.SearchResult
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.util.readAssetText
import com.carenote.app.ui.screens.billing.BillingScreen
import com.carenote.app.ui.screens.carerecipient.CareRecipientScreen
import com.carenote.app.ui.screens.emergencycontact.AddEditEmergencyContactScreen
import com.carenote.app.ui.screens.emergencycontact.EmergencyContactListScreen
import com.carenote.app.ui.screens.member.AcceptInvitationScreen
import com.carenote.app.ui.screens.member.MemberManagementScreen
import com.carenote.app.ui.screens.member.SendInvitationScreen
import com.carenote.app.ui.screens.onboarding.OnboardingWelcomeScreen
import com.carenote.app.ui.screens.search.SearchScreen
import com.carenote.app.ui.screens.settings.LegalDocumentScreen
import com.carenote.app.ui.screens.settings.SettingsScreen

internal fun NavGraphBuilder.settingsRoutes(
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
            },
            onNavigateToBilling = {
                navController.navigate(Screen.Billing.route)
            }
        )
    }
}

internal fun NavGraphBuilder.billingRoute(
    navController: NavHostController
) {
    composable(Screen.Billing.route) {
        BillingScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

internal fun NavGraphBuilder.searchRoute(
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

internal fun navigateToSearchResult(
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

internal fun NavGraphBuilder.onboardingRoutes(
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

internal fun NavGraphBuilder.emergencyContactRoutes(
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

internal fun NavGraphBuilder.memberRoutes(
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

internal fun acceptInvitationDeepLinks() = listOf(
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

internal fun NavGraphBuilder.legalRoutes(
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
internal fun PrivacyPolicyRoute(navController: NavHostController) {
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
internal fun TermsOfServiceRoute(navController: NavHostController) {
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
