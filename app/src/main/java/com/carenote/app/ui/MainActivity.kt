package com.carenote.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.carenote.app.R
import com.carenote.app.domain.model.ThemeMode
import com.carenote.app.domain.model.UserSettings
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.CalendarEventRepository
import com.carenote.app.domain.repository.ConnectivityRepository
import com.carenote.app.domain.repository.SettingsRepository
import com.carenote.app.domain.repository.SyncWorkSchedulerInterface
import kotlinx.coroutines.flow.flowOf
import com.carenote.app.ui.navigation.AdaptiveNavigationScaffold
import com.carenote.app.ui.navigation.CareNoteNavHost
import com.carenote.app.ui.navigation.Screen
import com.carenote.app.ui.theme.CareNoteTheme
import com.carenote.app.config.AppConfig
import com.carenote.app.ui.util.BiometricAuthenticator
import com.carenote.app.ui.util.RootDetector
import timber.log.Timber
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var calendarEventRepository: CalendarEventRepository

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    @Inject
    lateinit var connectivityRepository: ConnectivityRepository

    @Inject
    lateinit var syncWorkScheduler: SyncWorkSchedulerInterface

    @Inject
    lateinit var biometricHelper: BiometricAuthenticator
    private val isAuthenticated = mutableStateOf(false)
    private var lastBackgroundTime: Long = 0L
    private var isRootDetected: Boolean = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        isRootDetected = RootDetector().isDeviceRooted()
        setupLifecycleObserver()
        enableEdgeToEdge()
        setContent { MainContent() }
    }

    private fun setupLifecycleObserver() {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                lastBackgroundTime = System.currentTimeMillis()
            }

            override fun onStart(owner: LifecycleOwner) {
                checkSessionTimeout()
            }
        })
    }

    @Composable
    private fun MainContent() {
        val settings by settingsRepository.getSettings()
            .collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )
        val currentUser by authRepository.currentUser
            .collectAsStateWithLifecycle(
                initialValue = authRepository.getCurrentUser()
            )
        val isLoggedIn = currentUser != null
        val startDestination = remember {
            resolveStartDestination()
        }
        val darkTheme = resolveDarkTheme(settings)
        val authenticated by isAuthenticated

        CareNoteTheme(
            darkTheme = darkTheme,
            useDynamicColor = settings.useDynamicColor
        ) {
            NotificationPermissionEffect()
            RootWarningEffect()

            if (!isLoggedIn || !settings.biometricEnabled ||
                authenticated
            ) {
                MainNavigation(
                    isLoggedIn = isLoggedIn,
                    startDestination = startDestination
                )
            }
        }
    }

    @Composable
    private fun NotificationPermissionEffect() {
        var permissionRequested by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !permissionRequested
            ) {
                permissionRequested = true
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    @Composable
    private fun RootWarningEffect() {
        var showRootWarning by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (isRootDetected) {
                showRootWarning = true
            }
        }
        if (showRootWarning) {
            RootWarningDialog(
                onContinue = { showRootWarning = false },
                onExit = { finish() }
            )
        }
    }

    @Composable
    private fun MainNavigation(
        isLoggedIn: Boolean,
        startDestination: String
    ) {
        val navController = rememberNavController()

        DeepLinkEffect(navController)
        AnalyticsTrackingEffect(navController)

        val navBackStackEntry by
            navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = shouldShowBottomBar(currentRoute)

        AuthNavigationEffect(isLoggedIn, navController)

        val incompleteTaskCount by (
            if (isLoggedIn) {
                calendarEventRepository.getIncompleteTaskCount()
            } else {
                flowOf(0)
            }
            ).collectAsStateWithLifecycle(initialValue = 0)

        val isOnline by connectivityRepository.isOnline
            .collectAsStateWithLifecycle(initialValue = true)

        var previousIsOnline by remember { mutableStateOf(true) }
        LaunchedEffect(isOnline) {
            if (isOnline && !previousIsOnline) {
                Timber.d("Connectivity restored, triggering immediate sync")
                syncWorkScheduler.triggerImmediateSync()
            }
            previousIsOnline = isOnline
        }

        if (showBottomBar) {
            AdaptiveNavigationScaffold(
                navController = navController,
                incompleteTaskCount = incompleteTaskCount,
                isOffline = !isOnline
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

    @Composable
    private fun DeepLinkEffect(navController: NavHostController) {
        DisposableEffect(Unit) {
            val listener = Consumer<Intent> { newIntent ->
                navController.handleDeepLink(newIntent)
            }
            addOnNewIntentListener(listener)
            onDispose {
                removeOnNewIntentListener(listener)
            }
        }
    }

    @Composable
    private fun AnalyticsTrackingEffect(
        navController: NavHostController
    ) {
        DisposableEffect(navController) {
            val listener =
                NavController.OnDestinationChangedListener { _, dest, _ ->
                    val screenName =
                        dest.route ?: return@OnDestinationChangedListener
                    analyticsRepository.logScreenView(screenName)
                }
            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }
    }

    @Composable
    private fun AuthNavigationEffect(
        isLoggedIn: Boolean,
        navController: NavHostController
    ) {
        LaunchedEffect(isLoggedIn) {
            val currentDest =
                navController.currentDestination?.route
                    ?: return@LaunchedEffect
            val authRoutes = Screen.authScreens.map { it.route }
            if (isLoggedIn && currentDest in authRoutes) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            } else if (!isLoggedIn && currentDest !in authRoutes) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    private fun resolveStartDestination(): String = when {
        !settingsRepository.isOnboardingCompleted() ->
            Screen.OnboardingWelcome.route
        authRepository.getCurrentUser() != null ->
            Screen.Home.route
        else -> Screen.Login.route
    }

    @Composable
    private fun resolveDarkTheme(settings: UserSettings): Boolean =
        when (settings.themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

    private fun shouldShowBottomBar(currentRoute: String?): Boolean {
        val authRoutes = Screen.authScreens.map { it.route }
        val isAuthScreen = currentRoute in authRoutes
        val isOnboardingScreen = currentRoute ==
            Screen.OnboardingWelcome.route ||
            currentRoute == "onboarding_care_recipient"
        val bottomNavRoutes = Screen.bottomNavItems.map { it.route }
        return currentRoute in bottomNavRoutes &&
            !isAuthScreen && !isOnboardingScreen
    }

    private fun checkSessionTimeout() {
        if (!biometricHelper.canAuthenticate(this)) {
            isAuthenticated.value = true
            return
        }

        val elapsed = System.currentTimeMillis() - lastBackgroundTime
        val isFirstLaunch = lastBackgroundTime == 0L
        val timeoutMs = if (isRootDetected) {
            AppConfig.Security.ROOT_SESSION_TIMEOUT_MS
        } else {
            settingsRepository.getSessionTimeoutMs()
        }
        val needsAuth = isFirstLaunch || elapsed > timeoutMs

        if (!needsAuth) {
            isAuthenticated.value = true
            return
        }

        isAuthenticated.value = false

        biometricHelper.authenticate(
            activity = this,
            onSuccess = { isAuthenticated.value = true },
            onError = { errorCode, _ ->
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        finish()
                    }
                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                        isAuthenticated.value = true
                    }
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Timber.w("Biometric lockout: code=$errorCode")
                    }
                    else -> {
                        Timber.w("Biometric error: code=$errorCode")
                    }
                }
            }
        )
    }
}

@Composable
private fun RootWarningDialog(
    onContinue: () -> Unit,
    onExit: () -> Unit
) {
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
            Text(
                text = stringResource(
                    R.string.security_root_warning_dialog_title
                )
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string
                        .security_root_warning_dialog_message_restricted
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(text = stringResource(R.string.common_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text(text = stringResource(R.string.common_exit))
            }
        }
    )
}
