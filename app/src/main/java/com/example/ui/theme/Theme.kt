package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

val LocalHudTheme = staticCompositionLocalOf { StarkHudTheme }

@Composable
fun KharcheeTheme(
    themeId: String = "stark",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    FontPreferenceManager.ensureInitialized(context)
    val resolvedFontId by FontPreferenceManager.selectedFontId.collectAsState()
    val selectedFontOption = getAppFontOptionById(resolvedFontId)
    val dynamicTypography = buildAppTypography(
        headingFontFamily = selectedFontOption.fontFamily,
        bodyFontFamily = selectedFontOption.bodyFontFamily
    )

    val normalizedThemeId = ThemePreferenceManager.normalizeThemeId(themeId)
    ObsidianThemeState.activeThemeId = normalizedThemeId
    val hudPalette = getHudThemeById(normalizedThemeId)

    val colorScheme = darkColorScheme(
        primary = hudPalette.accent,
        onPrimary = Color(0xFF040508),
        primaryContainer = hudPalette.surfaceElevated,
        onPrimaryContainer = hudPalette.textPrimary,
        secondary = hudPalette.secondaryAccent,
        onSecondary = Color.White,
        secondaryContainer = hudPalette.surfaceElevated,
        onSecondaryContainer = hudPalette.textPrimary,
        tertiary = ObsidianDarkPositive,
        onTertiary = ObsidianDarkBackground,
        background = hudPalette.background,
        onBackground = hudPalette.textPrimary,
        surface = hudPalette.surface,
        onSurface = hudPalette.textPrimary,
        surfaceVariant = hudPalette.surfaceElevated,
        onSurfaceVariant = hudPalette.textSecondary,
        outline = hudPalette.borderGlow.copy(alpha = 0.45f),
        error = ObsidianDarkNegative,
        onError = Color.White
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = ObsidianDarkBackground.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = ObsidianDarkBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalHudTheme provides hudPalette,
        LocalAppFontFamily provides selectedFontOption.fontFamily,
        LocalAppBodyFontFamily provides selectedFontOption.bodyFontFamily
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = dynamicTypography,
            content = content
        )
    }
}
