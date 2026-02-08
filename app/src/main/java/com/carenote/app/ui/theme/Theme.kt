package com.carenote.app.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = PrimaryGreenDark,
    secondary = AccentGreen,
    onSecondary = SurfaceWhite,
    secondaryContainer = PrimaryGreenLight,
    onSecondaryContainer = PrimaryGreenDark,
    tertiary = AccentGreen,
    onTertiary = SurfaceWhite,
    background = BackgroundCream,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceWarmGray,
    onSurfaceVariant = TextSecondary,
    error = AccentError,
    onError = SurfaceWhite,
    outline = TextDisabled,
    outlineVariant = SurfaceWarmGray
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkPrimaryGreenLight,
    onPrimaryContainer = DarkPrimaryGreenDark,
    secondary = DarkAccentGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkPrimaryGreenLight,
    onSecondaryContainer = DarkPrimaryGreenDark,
    tertiary = DarkAccentGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    error = DarkAccentError,
    onError = DarkBackground,
    outline = DarkTextDisabled,
    outlineVariant = DarkSurfaceVariant
)

val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val ChipShape = RoundedCornerShape(8.dp)

@Composable
fun CareNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val isDynamicColorActive = useDynamicColor &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        !view.isInEditMode

    val colorScheme = when {
        isDynamicColorActive && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        isDynamicColorActive && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val careNoteColors = if (darkTheme) DarkCareNoteColors else LightCareNoteColors

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as ComponentActivity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalCareNoteColors provides careNoteColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
