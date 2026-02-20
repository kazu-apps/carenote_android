package com.carenote.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.navigation.routes.authRoutes
import com.carenote.app.ui.navigation.routes.billingRoute
import com.carenote.app.ui.navigation.routes.bottomNavRoutes
import com.carenote.app.ui.navigation.routes.calendarRoutes
import com.carenote.app.ui.navigation.routes.emergencyContactRoutes
import com.carenote.app.ui.navigation.routes.healthRecordRoutes
import com.carenote.app.ui.navigation.routes.legalRoutes
import com.carenote.app.ui.navigation.routes.medicationRoutes
import com.carenote.app.ui.navigation.routes.memberRoutes
import com.carenote.app.ui.navigation.routes.noteRoutes
import com.carenote.app.ui.navigation.routes.onboardingRoutes
import com.carenote.app.ui.navigation.routes.searchRoute
import com.carenote.app.ui.navigation.routes.settingsRoutes
import com.carenote.app.ui.navigation.routes.taskRoutes

@Composable
fun CareNoteNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { slideInHorizontally(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) { it } },
        exitTransition = { slideOutHorizontally(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) { -it } },
        popEnterTransition = { slideInHorizontally(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) { -it } },
        popExitTransition = { slideOutHorizontally(tween(AppConfig.UI.ANIMATION_DURATION_MS.toInt())) { it } }
    ) {
        bottomNavRoutes(navController)
        settingsRoutes(navController)
        billingRoute(navController)
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
