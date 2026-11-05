package com.example.ui.theme

import android.content.Context
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
// THEME A: OBSIDIAN LUXURY TOKENS (DARK MODE DEFAULT)
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
// THEME B: NEON INTELLIGENCE TOKENS (DEEP NAVY + ELECTRIC BLUE & PURPLE)
// ============================================================================
val NeonNavyBackground = Color(0xFF060A17)
val NeonPrimarySurface = Color(0xFF0D152C)
val NeonSecondarySurface = Color(0xFF162244)
val NeonBorder = Color(0xFF253B6E)
val NeonPrimaryText = Color(0xFFF0F6FF)
val NeonSecondaryText = Color(0xFF9BB1D6)
val NeonMutedText = Color(0xFF768FB8)
val NeonHolographicSilver = Color(0xFFC5D8F5)
val NeonElectricBlue = Color(0xFF00E5FF)
val NeonPurpleAccent = Color(0xFF9D4EDD)
val NeonPositive = Color(0xFF00F5D4)
val NeonNegative = Color(0xFFFF4D6D)
val NeonWarning = Color(0xFFFFB703)

// ============================================================================
// THEME C: LIGHT LUXURY TOKENS (WHITE + SILVER SURFACES + SUBTLE EMERALD)
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
 * Tracks the active independent theme across KHARCHEE:
 * - "stark" / "obsidian" -> Theme A: Obsidian Luxury
 * - "neon" / "emerald" -> Theme B: Neon Intelligence
 * - "light" -> Theme C: Light Luxury
 */
object ObsidianThemeState {
    var activeThemeId by mutableStateOf("stark")

    val isNeonMode: Boolean
        get() = activeThemeId == "neon" || activeThemeId == "emerald"

    var isLightMode: Boolean
        get() = activeThemeId == "light"
        set(value) {
            activeThemeId = if (value) "light" else "stark"
        }
}

object ThemePreferenceManager {
    private const val PREFS_NAME = "kharchee_theme_preferences"
    private const val KEY_THEME_ID = "selected_theme_id"

    fun ensureInitialized(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME_ID, null)
        val resolved = normalizeThemeId(saved ?: "stark")
        ObsidianThemeState.activeThemeId = resolved
        return resolved
    }

    fun loadThemePreference(context: Context): String = ensureInitialized(context)

    fun saveThemePreference(context: Context, themeId: String) {
        val normalized = normalizeThemeId(themeId)
        ObsidianThemeState.activeThemeId = normalized
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_ID, normalized)
            .apply()
    }

    fun normalizeThemeId(raw: String): String = when (raw.lowercase()) {
        "light", "light_luxury" -> "light"
        "neon", "neon_intelligence", "emerald" -> "neon"
        "midnight" -> "midnight"
        "armor" -> "armor"
        else -> "stark"
    }
}

// ============================================================================
// REACTIVE SEMANTIC THEME TOKENS (RESPOND TO ALL 3 INDEPENDENT THEMES)
// ============================================================================
val DeepForestAbyss: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightBackground
        ObsidianThemeState.isNeonMode -> NeonNavyBackground
        else -> ObsidianDarkBackground
    }

val ObsidianBlack: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightBackground
        ObsidianThemeState.isNeonMode -> NeonNavyBackground
        else -> ObsidianDarkBackground
    }

val RichGraphite: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightPrimarySurface
        ObsidianThemeState.isNeonMode -> NeonPrimarySurface
        else -> ObsidianDarkPrimarySurface
    }

val ElevatedGraphite: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightSecondarySurface
        ObsidianThemeState.isNeonMode -> NeonSecondarySurface
        else -> ObsidianDarkSecondarySurface
    }

val ForestGlassSurface: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightSecondarySurface
        ObsidianThemeState.isNeonMode -> NeonSecondarySurface
        else -> ObsidianDarkSecondarySurface
    }

val MetallicSilver: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightSilver
        ObsidianThemeState.isNeonMode -> NeonHolographicSilver
        else -> ObsidianDarkSilver
    }

val ChampagneGold: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightEmeraldAccent
        ObsidianThemeState.isNeonMode -> NeonElectricBlue
        else -> ObsidianDarkEmeraldAccent
    }

val ChampagneGoldSoft: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightSilver
        ObsidianThemeState.isNeonMode -> NeonPurpleAccent
        else -> ObsidianDarkSilver
    }

val SophisticatedEmerald: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightPositive
        ObsidianThemeState.isNeonMode -> NeonPositive
        else -> ObsidianDarkPositive
    }

val EmeraldNeonGlow: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightEmeraldAccent
        ObsidianThemeState.isNeonMode -> NeonElectricBlue
        else -> ObsidianDarkEmeraldAccent
    }

val DeepEmeraldAccent: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightPositive
        ObsidianThemeState.isNeonMode -> NeonPurpleAccent
        else -> ObsidianDarkPositive
    }

val GoldReserveAccent: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightWarning
        ObsidianThemeState.isNeonMode -> NeonWarning
        else -> ObsidianDarkWarning
    }

val WarningAmberColor: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightWarning
        ObsidianThemeState.isNeonMode -> NeonWarning
        else -> ObsidianDarkWarning
    }

// Typography & Semantic Status Tokens
val WarmIvory: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightPrimaryText
        ObsidianThemeState.isNeonMode -> NeonPrimaryText
        else -> ObsidianDarkPrimaryText
    }

val MutedPlatinum: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightSecondaryText
        ObsidianThemeState.isNeonMode -> NeonSecondaryText
        else -> ObsidianDarkSecondaryText
    }

val MutedIvory: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightMutedText
        ObsidianThemeState.isNeonMode -> NeonMutedText
        else -> ObsidianDarkMutedText
    }

val CrimsonRuby: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightNegative
        ObsidianThemeState.isNeonMode -> NeonNegative
        else -> ObsidianDarkNegative
    }

// Subtle Structural Borders & Chart Fills
val HairlinePlatinum: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightBorder
        ObsidianThemeState.isNeonMode -> NeonBorder
        else -> ObsidianDarkBorder
    }

val HairlineGold: Color
    get() = when {
        ObsidianThemeState.isLightMode -> ObsidianLightBorder
        ObsidianThemeState.isNeonMode -> NeonPurpleAccent.copy(alpha = 0.55f)
        else -> ObsidianDarkBorder
    }

val EmeraldChartFillTop: Color
    get() = when {
        ObsidianThemeState.isLightMode -> Color(0x3300A879)
        ObsidianThemeState.isNeonMode -> Color(0x4400E5FF)
        else -> Color(0x3800DFA5)
    }

val EmeraldChartFillBottom: Color
    get() = when {
        ObsidianThemeState.isLightMode -> Color(0x0000A879)
        ObsidianThemeState.isNeonMode -> Color(0x009D4EDD)
        else -> Color(0x0000DFA5)
    }

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

// Theme A: Obsidian Luxury
val StarkHudTheme = KharcheeHudTheme(
    id = "stark",
    name = "Theme A · Obsidian Luxury",
    subtitle = "Matte black (#080A0D), metallic silver (#C4CBD4) & emerald (#00DFA5)",
    symbol = "◆",
    accent = ObsidianDarkEmeraldAccent,
    secondaryAccent = ObsidianDarkSilver,
    background = ObsidianDarkBackground,
    surface = ObsidianDarkPrimarySurface,
    surfaceElevated = ObsidianDarkSecondarySurface,
    borderGlow = ObsidianDarkBorder,
    isLight = false
)

// Theme B: Neon Intelligence
val NeonHudTheme = KharcheeHudTheme(
    id = "neon",
    name = "Theme B · Neon Intelligence",
    subtitle = "Deep navy (#060A17), electric cyan (#00E5FF) & holographic purple (#9D4EDD)",
    symbol = "❖",
    accent = NeonElectricBlue,
    secondaryAccent = NeonPurpleAccent,
    background = NeonNavyBackground,
    surface = NeonPrimarySurface,
    surfaceElevated = NeonSecondarySurface,
    borderGlow = NeonBorder,
    isLight = false
)

// Theme C: Light Luxury
val LightHudTheme = KharcheeHudTheme(
    id = "light",
    name = "Theme C · Light Luxury",
    subtitle = "White & silver surfaces (#FFFFFF / #EDF0F4) with subtle emerald (#00A879)",
    symbol = "◇",
    accent = ObsidianLightEmeraldAccent,
    secondaryAccent = ObsidianLightPositive,
    background = ObsidianLightBackground,
    surface = ObsidianLightPrimarySurface,
    surfaceElevated = ObsidianLightSecondarySurface,
    borderGlow = ObsidianLightBorder,
    isLight = true
)

val EmeraldHudTheme = NeonHudTheme

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
    NeonHudTheme,
    LightHudTheme
)

fun getHudThemeById(id: String): KharcheeHudTheme {
    return when (id.lowercase()) {
        "light", "light_luxury" -> LightHudTheme
        "neon", "neon_intelligence", "emerald" -> NeonHudTheme
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
    "Entertainment" -> ChampagneGoldSoft
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
