package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkStudioColorScheme = darkColorScheme(
    primary = CyberEmerald,
    onPrimary = DarkStudioBg,
    primaryContainer = CyberEmeraldMuted,
    onPrimaryContainer = CyberEmerald,
    secondary = ElectricCyan,
    onSecondary = DarkStudioBg,
    secondaryContainer = ElectricCyanMuted,
    onSecondaryContainer = ElectricCyan,
    tertiary = VoltYellow,
    onTertiary = DarkStudioBg,
    background = DarkStudioBg,
    onBackground = TextPrimary,
    surface = DarkStudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkStudioSurface,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = LaserCrimson,
    onError = TextPrimary
)

private val LightStudioColorScheme = lightColorScheme(
    primary = CyberEmerald,
    onPrimary = DarkStudioBg,
    secondary = ElectricCyan,
    onSecondary = DarkStudioBg,
    background = DarkStudioBg,
    onBackground = TextPrimary,
    surface = DarkStudioSurface,
    onSurface = TextPrimary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    pureAmoled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheme = if (pureAmoled) {
        DarkStudioColorScheme.copy(
            background = AmoledBlack,
            surface = if (pureAmoled) DarkStudioSurface else AmoledBlack
        )
    } else {
        DarkStudioColorScheme
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content
    )
}
