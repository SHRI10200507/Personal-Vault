package com.example.personalvault.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TerminalColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = TerminalBackground,
    primaryContainer = TerminalSurfaceVariant,
    onPrimaryContainer = AccentCyan,
    secondary = BullishGreen,
    onSecondary = TerminalBackground,
    secondaryContainer = BullishBg,
    onSecondaryContainer = BullishGreen,
    tertiary = GoldAccent,
    onTertiary = TerminalBackground,
    error = BearishRed,
    onError = TerminalBackground,
    background = TerminalBackground,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TerminalOutline,
    outlineVariant = TerminalCardBorder
)

@Composable
fun MarketTerminalTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = TerminalBackground.toArgb()
                window.navigationBarColor = TerminalBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = TerminalColorScheme,
        content = content
    )
}
