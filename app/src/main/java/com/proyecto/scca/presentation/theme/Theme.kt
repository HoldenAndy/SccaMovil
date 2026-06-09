package com.proyecto.scca.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val DarkColorScheme =
    darkColorScheme(
        primary = SccaAccentLight,
        onPrimary = DarkBackground,
        primaryContainer = SccaAccentSoftDark,
        onPrimaryContainer = SccaAccentLight,
        secondary = StatusNormal,
        onSecondary = DarkBackground,
        tertiary = TurbColor,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnBackground,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        error = StatusCritical,
        onError = LightBackground,
    )

val LightColorScheme =
    lightColorScheme(
        primary = SccaAccent,
        onPrimary = LightBackground,
        primaryContainer = SccaAccentSoft,
        onPrimaryContainer = SccaAccentDark,
        secondary = StatusNormal,
        onSecondary = LightBackground,
        tertiary = TurbColor,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnBackground,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        error = StatusCritical,
        onError = LightBackground,
    )

val SccaShapes =
    Shapes(
        extraSmall = RoundedCornerShape(2.dp),
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(6.dp),
        extraLarge = RoundedCornerShape(8.dp),
    )

@Composable
fun SccaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
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
        typography = SccaTypography,
        shapes = SccaShapes,
        content = content,
    )
}

// Extension helpers for sensor colors
@Composable
fun sensorColor(
    key: String,
    isDark: Boolean = isSystemInDarkTheme(),
) = when (key) {
    "ph" -> if (isDark) SccaAccentLight else PhColor
    "temperatura" -> if (isDark) Color(0xFFE09762) else TempColor
    "turbidez" -> if (isDark) Color(0xFFB06FCC) else TurbColor
    "tds" -> if (isDark) Color(0xFF65C98C) else TdsColor
    else -> MaterialTheme.colorScheme.primary
}
