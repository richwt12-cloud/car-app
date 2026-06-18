package com.example.carapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary               = Navy40,
    onPrimary             = Color.White,
    primaryContainer      = Navy90,
    onPrimaryContainer    = Navy10,
    secondary             = Steel40,
    onSecondary           = Color.White,
    secondaryContainer    = Steel90,
    onSecondaryContainer  = Steel30,
    tertiary              = Amber40,
    onTertiary            = Color.White,
    tertiaryContainer     = Amber90,
    onTertiaryContainer   = Amber20,
    background            = Color(0xFFF4F7FC),
    onBackground          = Color(0xFF0D1520),
    surface               = Color(0xFFFFFFFF),
    onSurface             = Color(0xFF0D1520),
    surfaceVariant        = Color(0xFFDEE8F4),
    onSurfaceVariant      = Color(0xFF44556E),
    outline               = Color(0xFF7A8EAB),
    outlineVariant        = Color(0xFFCDD6E8),
    error                 = Color(0xFFB3261E),
    onError               = Color.White,
    errorContainer        = Color(0xFFF9DEDC),
    onErrorContainer      = Color(0xFF410E0B),
    inverseSurface        = Color(0xFF1A2535),
    inverseOnSurface      = Color(0xFFDEEAF8),
    inversePrimary        = Navy80
)

private val DarkColorScheme = darkColorScheme(
    primary               = Navy80,
    onPrimary             = Navy20,
    primaryContainer      = Navy30,
    onPrimaryContainer    = Navy90,
    secondary             = Steel80,
    onSecondary           = Steel30,
    secondaryContainer    = Steel30,
    onSecondaryContainer  = Steel90,
    tertiary              = Amber80,
    onTertiary            = Amber20,
    tertiaryContainer     = Color(0xFF7C4C00),
    onTertiaryContainer   = Amber90,
    background            = Color(0xFF0B1422),
    onBackground          = Color(0xFFDDE8F8),
    surface               = Color(0xFF101C2E),
    onSurface             = Color(0xFFDDE8F8),
    surfaceVariant        = Color(0xFF1A2A3C),
    onSurfaceVariant      = Color(0xFFAFBDD0),
    outline               = Color(0xFF58687E),
    outlineVariant        = Color(0xFF263648),
    error                 = Color(0xFFF2B8B5),
    onError               = Color(0xFF601410),
    errorContainer        = Color(0xFF8C1D18),
    onErrorContainer      = Color(0xFFF9DEDC),
    inverseSurface        = Color(0xFFDDE8F8),
    inverseOnSurface      = Color(0xFF1A2535),
    inversePrimary        = Navy40
)

@Composable
fun CarMaintenanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar matches the gradient-deep color used on all headers
            window.statusBarColor = GradientDeep.toArgb()
            // Headers are always dark navy → always use white status bar icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
