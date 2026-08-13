package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProtocolDarkColorScheme = darkColorScheme(
    primary = NeonRedPrimary,
    onPrimary = PureWhite,
    primaryContainer = CyberPurple,
    onPrimaryContainer = PureWhite,
    secondary = ElectricViolet,
    onSecondary = PureWhite,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = OffWhite,
    tertiary = NeonRedSecondary,
    onTertiary = PureWhite,
    background = DarkBackground,
    onBackground = OffWhite,
    surface = DarkSurface,
    onSurface = OffWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = DarkCardBorder,
    outlineVariant = DarkDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme per design request
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProtocolDarkColorScheme,
        typography = Typography,
        content = content
    )
}

