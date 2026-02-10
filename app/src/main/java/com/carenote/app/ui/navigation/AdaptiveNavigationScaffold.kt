package com.carenote.app.ui.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.carenote.app.R
import com.carenote.app.config.AppConfig

@Composable
fun AdaptiveNavigationScaffold(
    navController: NavController,
    incompleteTaskCount: Int = 0,
    content: @Composable () -> Unit
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val layoutType = with(adaptiveInfo) {
        if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            NavigationSuiteType.NavigationDrawer
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            Screen.bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                val badgeContent: (@Composable () -> Unit)? =
                    if (screen.route == Screen.Tasks.route && incompleteTaskCount > 0) {
                        {
                            val displayText = if (incompleteTaskCount > AppConfig.UI.BADGE_MAX_COUNT) {
                                "${AppConfig.UI.BADGE_MAX_COUNT}+"
                            } else {
                                incompleteTaskCount.toString()
                            }
                            val badgeDescription = stringResource(R.string.nav_tasks_badge, incompleteTaskCount)
                            Badge(
                                modifier = Modifier.semantics {
                                    contentDescription = badgeDescription
                                }
                            ) { Text(displayText) }
                        }
                    } else {
                        null
                    }

                item(
                    icon = {
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = stringResource(screen.titleResId)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(screen.titleResId),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    badge = badgeContent,
                    selected = selected,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContentColor = MaterialTheme.colorScheme.onSurface,
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
            navigationRailContentColor = MaterialTheme.colorScheme.onSurface,
            navigationRailContainerColor = MaterialTheme.colorScheme.surface,
            navigationDrawerContentColor = MaterialTheme.colorScheme.onSurface,
            navigationDrawerContainerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}
