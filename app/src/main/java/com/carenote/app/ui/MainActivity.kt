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
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.util.Consumer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.carenote.app.R
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.flowOf
import com.carenote.app.ui.navigation.AdaptiveNavigationScaffold
import com.carenote.app.ui.navigation.CareNoteNavHost
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.ui.util.BiometricHelper
import com.carenote.app.ui.util.RootDetector
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

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
                checkSessionTimeout()
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
                when {
                    !settingsRepository.isOnboardingCompleted() -> Screen.OnboardingWelcome.route
                    authRepository.getCurrentUser() != null -> Screen.Home.route
                    else -> Screen.Login.route
                }
            }

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val authenticated by isAuthenticated

            CareNoteTheme(darkTheme = darkTheme, useDynamicColor = settings.useDynamicColor) {
                var permissionRequested by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionRequested) {
                        permissionRequested = true
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }

                var showRootWarning by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (RootDetector().isDeviceRooted()) {
                        showRootWarning = true
                    }
                }

                if (showRootWarning) {
                    AlertDialog(
                        onDismissRequest = {},
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        title = {
                            Text(text = stringResource(R.string.security_root_warning_dialog_title))
                        },
                        text = {
                            Text(text = stringResource(R.string.security_root_warning_dialog_message))
                        },
                        confirmButton = {
                            TextButton(onClick = { showRootWarning = false }) {
                                Text(text = stringResource(R.string.common_continue))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { finish() }) {
                                Text(text = stringResource(R.string.common_exit))
                            }
                        }
                    )
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

                    // 画面遷移の自動トラッキング
                    DisposableEffect(navController) {
                        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                            val screenName = destination.route ?: return@OnDestinationChangedListener
                            analyticsRepository.logScreenView(screenName)
                        }
                        navController.addOnDestinationChangedListener(listener)
                        onDispose {
                            navController.removeOnDestinationChangedListener(listener)
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val isAuthScreen = currentRoute in Screen.authScreens.map { it.route }
                    val isOnboardingScreen = currentRoute == Screen.OnboardingWelcome.route || currentRoute == "onboarding_care_recipient"
                    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route } && !isAuthScreen && !isOnboardingScreen

                    // 認証状態変更時のナビゲーション処理
                    LaunchedEffect(isLoggedIn) {
                        val currentDestination = navController.currentDestination?.route ?: return@LaunchedEffect
                        if (isLoggedIn && currentDestination in Screen.authScreens.map { it.route }) {
                            // ログイン成功: メイン画面へ遷移
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else if (!isLoggedIn && currentDestination !in Screen.authScreens.map { it.route }) {
                            // ログアウト: ログイン画面へ遷移
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    val incompleteTaskCount by (
                        if (isLoggedIn) taskRepository.getIncompleteTaskCount()
                        else flowOf(0)
                    ).collectAsStateWithLifecycle(initialValue = 0)

                    if (showBottomBar) {
                        AdaptiveNavigationScaffold(
                            navController = navController,
                            incompleteTaskCount = incompleteTaskCount
                        ) {
                            CareNoteNavHost(
                                navController = navController,
                                startDestination = startDestination
                            )
                        }
                    } else {
                        CareNoteNavHost(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
                // When biometric is required and not authenticated, show blank themed screen
            }
        }
    }

    private fun checkSessionTimeout() {
        if (!biometricHelper.canAuthenticate(this)) {
            isAuthenticated.value = true
            return
        }

        val elapsed = System.currentTimeMillis() - lastBackgroundTime
        val isFirstLaunch = lastBackgroundTime == 0L
        val needsAuth = isFirstLaunch || elapsed > settingsRepository.getSessionTimeoutMs()

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
