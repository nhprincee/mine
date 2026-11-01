package com.example.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ============================================================================
// OBSIDIAN LUXURY DESIGN SYSTEM TOKENS — DARK MODE
// ============================================================================
val ObsidianDarkBackground = Color(0xFF080A0D)
val ObsidianDarkPrimarySurface = Color(0xFF12161C)
val ObsidianDarkSecondarySurface = Color(0xFF202630)
val ObsidianDarkBorder = Color(0xFF303944)
val ObsidianDarkPrimaryText = Color(0xFFF5F7FA)
val ObsidianDarkSecondaryText = Color(0xFFA1AAB6)
val ObsidianDarkMutedText = Color(0xFF84909E)
val ObsidianDarkSilver = Color(0xFFC4CBD4)
val ObsidianDarkEmeraldAccent = Color(0xFF00DFA5)
val ObsidianDarkPositive = Color(0xFF00C98A)
val ObsidianDarkNegative = Color(0xFFFF5364)
val ObsidianDarkWarning = Color(0xFFF3B94F)

// ============================================================================
// OBSIDIAN LUXURY DESIGN SYSTEM TOKENS — LIGHT MODE
// ============================================================================
val ObsidianLightBackground = Color(0xFFF4F5F7)
val ObsidianLightPrimarySurface = Color(0xFFFFFFFF)
val ObsidianLightSecondarySurface = Color(0xFFEDF0F4)
val ObsidianLightBorder = Color(0xFFE5E9EF)
val ObsidianLightPrimaryText = Color(0xFF171B22)
val ObsidianLightSecondaryText = Color(0xFF667085)
val ObsidianLightMutedText = Color(0xFF667085)
val ObsidianLightSilver = Color(0xFFC4CBD4)
val ObsidianLightEmeraldAccent = Color(0xFF00A879)
val ObsidianLightPositive = Color(0xFF008F68)
val ObsidianLightNegative = Color(0xFFD9364F)
val ObsidianLightWarning = Color(0xFFA96B00)

/**
 * Tracks whether Obsidian Luxury Light Mode or Dark Mode is active.
 * Backed by Compose Snapshot state so any Composable or Canvas reading the semantic tokens
 * automatically recomposes and redraws when the theme switches.
 */
object ObsidianThemeState {
    var isLightMode by mutableStateOf(false)
}

// ============================================================================
// REACTIVE SEMANTIC THEME TOKENS (RESPOND TO ACTIVE DARK / LIGHT MODE)
// ============================================================================
val DeepForestAbyss: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightBackground else ObsidianDarkBackground

val ObsidianBlack: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightBackground else ObsidianDarkBackground

val RichGraphite: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightPrimarySurface else ObsidianDarkPrimarySurface

val ElevatedGraphite: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightSecondarySurface else ObsidianDarkSecondarySurface

val ForestGlassSurface: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightSecondarySurface else ObsidianDarkSecondarySurface

// Metallic Silver & Emerald Financial Action Accents
val MetallicSilver: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightSilver else ObsidianDarkSilver

val ChampagneGold: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightEmeraldAccent else ObsidianDarkEmeraldAccent

val ChampagneGoldSoft: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightSilver else ObsidianDarkSilver

val SophisticatedEmerald: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightPositive else ObsidianDarkPositive

val EmeraldNeonGlow: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightEmeraldAccent else ObsidianDarkEmeraldAccent

val DeepEmeraldAccent: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightPositive else ObsidianDarkPositive

val GoldReserveAccent: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightWarning else ObsidianDarkWarning

val WarningAmberColor: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightWarning else ObsidianDarkWarning

// Typography & Semantic Status Tokens
val WarmIvory: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightPrimaryText else ObsidianDarkPrimaryText

val MutedPlatinum: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightSecondaryText else ObsidianDarkSecondaryText

val MutedIvory: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightMutedText else ObsidianDarkMutedText

val CrimsonRuby: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightNegative else ObsidianDarkNegative

// Subtle Structural Borders & Chart Fills
val HairlinePlatinum: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightBorder else ObsidianDarkBorder

val HairlineGold: Color
    get() = if (ObsidianThemeState.isLightMode) ObsidianLightBorder else ObsidianDarkBorder

val EmeraldChartFillTop: Color
    get() = if (ObsidianThemeState.isLightMode) Color(0x3300A879) else Color(0x3800DFA5)

val EmeraldChartFillBottom: Color
    get() = if (ObsidianThemeState.isLightMode) Color(0x0000A879) else Color(0x0000DFA5)

data class KharcheeHudTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val symbol: String,
    val accent: Color,
    val secondaryAccent: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val borderGlow: Color,
    val isLight: Boolean = false
)

val StarkHudTheme = KharcheeHudTheme(
    id = "stark",
    name = "Obsidian Luxury Dark",
    subtitle = "Matte black (#080A0D), metallic silver & emerald (#00DFA5)",
    symbol = "◆",
    accent = ObsidianDarkEmeraldAccent,
    secondaryAccent = ObsidianDarkSilver,
    background = ObsidianDarkBackground,
    surface = ObsidianDarkPrimarySurface,
    surfaceElevated = ObsidianDarkSecondarySurface,
    borderGlow = ObsidianDarkBorder,
    isLight = false
)

val LightHudTheme = KharcheeHudTheme(
    id = "light",
    name = "Obsidian Luxury Light",
    subtitle = "Alabaster slate (#F4F5F7), pure white surface & deep emerald (#00A879)",
    symbol = "◇",
    accent = ObsidianLightEmeraldAccent,
    secondaryAccent = ObsidianLightPositive,
    background = ObsidianLightBackground,
    surface = ObsidianLightPrimarySurface,
    surfaceElevated = ObsidianLightSecondarySurface,
    borderGlow = ObsidianLightBorder,
    isLight = true
)

val EmeraldHudTheme = KharcheeHudTheme(
    id = "emerald",
    name = "Obsidian Metallic Silver",
    subtitle = "Brushed metallic silver (#C4CBD4) & emerald positive telemetry",
    symbol = "❖",
    accent = ObsidianDarkEmeraldAccent,
    secondaryAccent = ObsidianDarkPositive,
    background = ObsidianDarkBackground,
    surface = ObsidianDarkPrimarySurface,
    surfaceElevated = ObsidianDarkSecondarySurface,
    borderGlow = ObsidianDarkBorder,
    isLight = false
)

val MidnightHudTheme = KharcheeHudTheme(
    id = "midnight",
    name = "Obsidian Carbon Slate",
    subtitle = "Architectural carbon surfaces (#12161C) & restrained silver hairlines",
    symbol = "◈",
    accent = ObsidianDarkPositive,
    secondaryAccent = ObsidianDarkSilver,
    background = ObsidianDarkBackground,
    surface = ObsidianDarkPrimarySurface,
    surfaceElevated = ObsidianDarkSecondarySurface,
    borderGlow = ObsidianDarkBorder,
    isLight = false
)

val ArmorHudTheme = KharcheeHudTheme(
    id = "armor",
    name = "Obsidian Titanium Reserve",
    subtitle = "Matte titanium finish with precision emerald financial actions",
    symbol = "⬢",
    accent = ObsidianDarkEmeraldAccent,
    secondaryAccent = ObsidianDarkSilver,
    background = ObsidianDarkBackground,
    surface = ObsidianDarkPrimarySurface,
    surfaceElevated = ObsidianDarkSecondarySurface,
    borderGlow = ObsidianDarkBorder,
    isLight = false
)

val ALL_HUD_THEMES = listOf(
    StarkHudTheme,
    LightHudTheme,
    EmeraldHudTheme,
    MidnightHudTheme
)

fun getHudThemeById(id: String): KharcheeHudTheme {
    return when (id) {
        "light" -> LightHudTheme
        "emerald" -> EmeraldHudTheme
        "midnight" -> MidnightHudTheme
        "armor" -> ArmorHudTheme
        else -> StarkHudTheme
    }
}

fun getCategoryColor(category: String): Color = when (category) {
    "Food" -> ChampagneGold
    "Chai & Snacks" -> SophisticatedEmerald
    "Transport" -> MetallicSilver
    "Grocery" -> SophisticatedEmerald
    "Entertainment" -> MetallicSilver
    "Bills" -> CrimsonRuby
    "Shopping" -> WarningAmberColor
    "Salary" -> SophisticatedEmerald
    "Freelance" -> ChampagneGold
    "Cashback" -> SophisticatedEmerald
    else -> MetallicSilver
}

fun getCategoryIcon(category: String): ImageVector = when (category) {
    "Food" -> Icons.Default.Restaurant
    "Chai & Snacks" -> Icons.Default.LocalCafe
    "Transport" -> Icons.Default.DirectionsCar
    "Grocery" -> Icons.Default.ShoppingCart
    "Entertainment" -> Icons.Default.Movie
    "Bills" -> Icons.Default.Bolt
    "Shopping" -> Icons.Default.ShoppingBag
    "Salary" -> Icons.Default.Work
    "Freelance" -> Icons.Default.RocketLaunch
    "Cashback" -> Icons.Default.CardGiftcard
    else -> Icons.Default.AccountBalanceWallet
}

fun getCategoryEmoji(category: String): String = when (category) {
    "Food" -> "🍛"
    "Chai & Snacks" -> "☕"
    "Transport" -> "⛽"
    "Grocery" -> "🛒"
    "Entertainment" -> "🎬"
    "Bills" -> "⚡"
    "Shopping" -> "🛍️"
    "Salary" -> "💼"
    "Freelance" -> "🚀"
    "Cashback" -> "🎁"
    else -> "💳"
}
