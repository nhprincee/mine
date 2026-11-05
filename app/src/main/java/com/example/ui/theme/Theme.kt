package com.example.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val LocalHudTheme = staticCompositionLocalOf { StarkHudTheme }

@Composable
fun KharcheeTheme(
    themeId: String = "stark",
    fontIdOverride: String? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        FontPreferenceManager.ensureInitialized(context)
    }

    val hudTheme = getHudThemeById(themeId)
    val isLight = hudTheme.isLight
    ObsidianThemeState.activeThemeId = hudTheme.id
    SideEffect {
        ObsidianThemeState.activeThemeId = hudTheme.id
        ThemePreferenceManager.saveThemePreference(context, hudTheme.id)
    }

    val savedFontId by FontPreferenceManager.selectedFontId.collectAsStateWithLifecycle()
    val effectiveFontId = fontIdOverride ?: savedFontId
    val fontOption = remember(effectiveFontId) { getAppFontOptionById(effectiveFontId) }
    val appTypography = remember(fontOption) {
        buildAppTypography(
            headingFontFamily = fontOption.fontFamily,
            bodyFontFamily = fontOption.bodyFontFamily
        )
    }

    val colorScheme = if (isLight) {
        lightColorScheme(
            primary = ObsidianLightEmeraldAccent,
            onPrimary = ObsidianLightPrimarySurface,
            secondary = ObsidianLightPositive,
            onSecondary = ObsidianLightPrimarySurface,
            tertiary = ObsidianLightSilver,
            background = ObsidianLightBackground,
            onBackground = ObsidianLightPrimaryText,
            surface = ObsidianLightPrimarySurface,
            onSurface = ObsidianLightPrimaryText,
            surfaceVariant = ObsidianLightSecondarySurface,
            onSurfaceVariant = ObsidianLightSecondaryText,
            outline = ObsidianLightBorder,
            error = ObsidianLightNegative,
            onError = ObsidianLightPrimarySurface
        )
    } else {
        darkColorScheme(
            primary = hudTheme.accent,
            onPrimary = hudTheme.background,
            secondary = hudTheme.secondaryAccent,
            onSecondary = hudTheme.background,
            tertiary = SophisticatedEmerald,
            background = hudTheme.background,
            onBackground = WarmIvory,
            surface = hudTheme.surface,
            onSurface = WarmIvory,
            surfaceVariant = hudTheme.surfaceElevated,
            onSurfaceVariant = MutedPlatinum,
            outline = hudTheme.borderGlow,
            error = CrimsonRuby,
            onError = WarmIvory
        )
    }

    CompositionLocalProvider(
        LocalHudTheme provides hudTheme,
        LocalAppFontOption provides fontOption,
        LocalAppFontFamily provides fontOption.fontFamily,
        LocalAppBodyFontFamily provides fontOption.bodyFontFamily
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography
        ) {
            ProvideTextStyle(
                value = LocalTextStyle.current.merge(
                    fontFamily = fontOption.bodyFontFamily,
                    color = WarmIvory
                ),
                content = content
            )
        }
    }
}
