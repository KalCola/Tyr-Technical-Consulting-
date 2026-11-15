package com.example.tyrconsulting.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MidgardMain,
    onPrimary = TextMain,
    primaryContainer = BgCard,
    onPrimaryContainer = MidgardLight,
    secondary = AsgardLight,
    onSecondary = BgObsidian,
    secondaryContainer = BgAttr,
    onSecondaryContainer = AsgardLight,
    tertiary = VanaheimLight,
    background = BgObsidian,
    onBackground = TextMain,
    surface = BgSurface,
    onSurface = TextMain,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun TYRTechnicalConsultingTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
