package com.carenote.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.ui.navigation.BottomNavigationBar
import com.carenote.app.ui.navigation.CareNoteNavHost
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.theme.CareNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.getSettings()
                .collectAsStateWithLifecycle(initialValue = UserSettings())

            val currentUser by authRepository.currentUser
                .collectAsStateWithLifecycle(initialValue = authRepository.getCurrentUser())

            val isLoggedIn = currentUser != null
            val startDestination = remember {
                if (authRepository.getCurrentUser() != null) Screen.Medication.route
                else Screen.Login.route
            }

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            CareNoteTheme(darkTheme = darkTheme) {
                var permissionRequested by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionRequested) {
                        permissionRequested = true
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isAuthScreen = currentRoute in Screen.authScreens.map { it.route }
                val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route } && !isAuthScreen

                // 認証状態変更時のナビゲーション処理
                LaunchedEffect(isLoggedIn) {
                    val currentDestination = navController.currentDestination?.route ?: return@LaunchedEffect
                    if (isLoggedIn && currentDestination in Screen.authScreens.map { it.route }) {
                        // ログイン成功: メイン画面へ遷移
                        navController.navigate(Screen.Medication.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (!isLoggedIn && currentDestination !in Screen.authScreens.map { it.route }) {
                        // ログアウト: ログイン画面へ遷移
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    CareNoteNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
