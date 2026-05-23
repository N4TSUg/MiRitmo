package com.cean.miritmo.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Forzamos el esquema claro para mantener la identidad del diseño limpio del mockup
private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = AppWhite,
    secondary = AccentPurple,
    onSecondary = AppWhite,
    background = AppBackground,
    onBackground = TextDark,
    surface = AppWhite,
    onSurface = TextDark,
    surfaceVariant = AppWhite,
    onSurfaceVariant = TextGray,
    outline = LightBorder,
    error = ErrorColor
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = AppWhite,
    secondary = AccentPurple,
    onSecondary = AppWhite,
    background = Color(0xFF121212),
    onBackground = AppWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = AppWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFA0A0A0),
    outline = Color(0xFF333333),
    error = ErrorColor
)

@Composable
fun MiRitmoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
