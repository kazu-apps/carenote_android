package com.carenote.app.ui

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.carenote.app.config.AppConfig
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.ui.navigation.BottomNavigationBar
import com.carenote.app.ui.navigation.CareNoteNavHost
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private val biometricHelper = BiometricHelper()
    private val isAuthenticated = mutableStateOf(false)
    private var lastBackgroundTime: Long = 0L

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                lastBackgroundTime = System.currentTimeMillis()
            }

            override fun onStart(owner: LifecycleOwner) {
                checkBiometricAuth()
            }
        })

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

            val authenticated by isAuthenticated

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

                if (!isLoggedIn || !settings.biometricEnabled || authenticated) {
                    val navController = rememberNavController()

                    // 通知タップ等の deep link を処理（アプリ起動中）
                    DisposableEffect(Unit) {
                        val listener = Consumer<Intent> { newIntent ->
                            navController.handleDeepLink(newIntent)
                        }
                        addOnNewIntentListener(listener)
                        onDispose {
                            removeOnNewIntentListener(listener)
                        }
                    }

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
                // When biometric is required and not authenticated, show blank themed screen
            }
        }
    }

    private fun checkBiometricAuth() {
        if (!biometricHelper.canAuthenticate(this)) {
            isAuthenticated.value = true
            return
        }

        val elapsed = System.currentTimeMillis() - lastBackgroundTime
        val isFirstLaunch = lastBackgroundTime == 0L
        val needsAuth = isFirstLaunch || elapsed > AppConfig.Biometric.BACKGROUND_TIMEOUT_MS

        if (!needsAuth) {
            isAuthenticated.value = true
            return
        }

        // Reset to false — the Compose gate checks both biometricEnabled and this value
        isAuthenticated.value = false

        biometricHelper.authenticate(
            activity = this,
            onSuccess = { isAuthenticated.value = true },
            onError = { _ ->
                // Authentication cancelled or error — keep locked
                // User can retry by bringing app to foreground again
            }
        )
    }
}
