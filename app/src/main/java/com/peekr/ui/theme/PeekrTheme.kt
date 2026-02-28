package com.peekr.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ألوان Peekr
private val PeekrPurple = Color(0xFF5B4FCF)
private val PeekrPurpleLight = Color(0xFF7C6FF7)
private val PeekrPurpleDark = Color(0xFF3D31A8)

private val LightColorScheme = lightColorScheme(
    primary = PeekrPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE8FF),
    onPrimaryContainer = PeekrPurpleDark,
    secondary = Color(0xFF6750A4),
    tertiary = Color(0xFF0088CC),
    background = Color(0xFFFCFAFF),
    surface = Color.White,
    surfaceVariant = Color(0xFFF4F0FF),
    outline = Color(0xFFCAC4D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = PeekrPurpleLight,
    onPrimary = Color(0xFF1A0066),
    primaryContainer = PeekrPurpleDark,
    onPrimaryContainer = Color(0xFFEDE8FF),
    secondary = Color(0xFFCBBFFF),
    tertiary = Color(0xFF4FC3F7),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B2930),
    surfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF938F99)
)

@Composable
fun PeekrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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
        typography = PeekrTypography,
        content = content
    )
}
