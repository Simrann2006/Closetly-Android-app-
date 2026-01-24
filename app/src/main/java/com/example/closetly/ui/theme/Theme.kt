package com.example.closetly.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary_Dark,
    onPrimary = OnPrimary_Dark,
    primaryContainer = PrimaryVariant_Dark,
    secondary = Secondary_Dark,
    onSecondary = OnSecondary_Dark,
    tertiary = Pink80,
    background = Background_Dark,
    onBackground = OnBackground_Dark,
    surface = Surface_Dark,
    onSurface = OnSurface_Dark,
    error = Error_Dark,
    outline = Grey,
    surfaceVariant = DarkGrey,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary_Light,
    onPrimary = OnPrimary_Light,
    primaryContainer = PrimaryVariant_Light,
    secondary = Secondary_Light,
    onSecondary = OnSecondary_Light,
    tertiary = Pink40,
    background = Background_Light,
    onBackground = OnBackground_Light,
    surface = Surface_Light,
    onSurface = OnSurface_Light,
    error = Error_Light,
    outline = Grey,
    surfaceVariant = Light_grey,
)

@Composable
fun ClosetlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
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
        typography = Typography,
        content = content
    )
}