package com.carenote.app.ui.navigation.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.screens.auth.ForgotPasswordScreen
import com.carenote.app.ui.screens.auth.LoginScreen
import com.carenote.app.ui.screens.auth.RegisterScreen

internal fun NavGraphBuilder.authRoutes(
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
