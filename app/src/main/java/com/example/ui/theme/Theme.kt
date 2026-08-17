package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VaultIndigo80,
    onPrimary = VaultIndigo20,
    primaryContainer = VaultIndigo20,
    onPrimaryContainer = VaultIndigo80,
    secondary = VaultTeal80,
    onSecondary = VaultTeal40,
    tertiary = VaultAmber80,
    background = VaultSurfaceDark,
    onBackground = VaultOnDark,
    surface = VaultSurfaceDark,
    onSurface = VaultOnDark,
    surfaceVariant = VaultSurfaceDarkElevated,
    onSurfaceVariant = VaultOnDark,
)

private val LightColorScheme = lightColorScheme(
    primary = VaultIndigo40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = VaultIndigo80,
    onPrimaryContainer = VaultIndigo20,
    secondary = VaultTeal40,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = VaultAmber40,
    background = VaultSurfaceLight,
    onBackground = VaultOnLight,
    surface = VaultSurfaceLight,
    onSurface = VaultOnLight,
    surfaceVariant = VaultSurfaceLightElevated,
    onSurfaceVariant = VaultOnLight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color off by default now — the app has its own distinct identity.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
