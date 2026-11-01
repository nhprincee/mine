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
    ObsidianThemeState.isLightMode = isLight
    SideEffect {
        ObsidianThemeState.isLightMode = isLight
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
            onPrimary = ObsidianDarkBackground,
            secondary = hudTheme.secondaryAccent,
            onSecondary = ObsidianDarkBackground,
            tertiary = ObsidianDarkPositive,
            background = hudTheme.background,
            onBackground = ObsidianDarkPrimaryText,
            surface = hudTheme.surface,
            onSurface = ObsidianDarkPrimaryText,
            surfaceVariant = hudTheme.surfaceElevated,
            onSurfaceVariant = ObsidianDarkSecondaryText,
            outline = ObsidianDarkBorder,
            error = ObsidianDarkNegative,
            onError = ObsidianDarkPrimaryText
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
