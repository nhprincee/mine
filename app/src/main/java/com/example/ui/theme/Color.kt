package com.example.ui.theme

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ============================================================================
// KHARCHEE UNIFIED PURPLE / VIOLET LUXURY DESIGN SYSTEM
// Single Universal Theme: Deep Dark Navy (#05050A / #0D0E17) + Electric Purple/Violet (#7C3AED / #A855F7)
// ============================================================================

object ObsidianThemeState {
    private var _activeThemeId by mutableStateOf("stark")
    var activeThemeId: String
        get() = _activeThemeId
        set(value) {
            _activeThemeId = ThemePreferenceManager.normalizeThemeId(value)
        }

    var isLightMode: Boolean
        get() = false
        set(_) {
            // Strictly dark-mode holographic themes
        }
}

object ThemePreferenceManager {
    private const val PREFS_NAME = "kharchee_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"

    val HOLOGRAPHIC_THEME_IDS = setOf(
        "stark",
        "matrix_emerald",
        "cyberpunk_cyan",
        "obsidian_void",
        "sovereign_gold"
    )

    fun loadThemePreference(context: Context): String {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_ID, "stark")
        val normalized = normalizeThemeId(raw)
        ObsidianThemeState.activeThemeId = normalized
        return normalized
    }

    fun saveThemePreference(context: Context, themeId: String) {
        val normalized = normalizeThemeId(themeId)
        ObsidianThemeState.activeThemeId = normalized
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_ID, normalized)
            .apply()
    }

    fun loadLightModePreference(context: Context): Boolean = false

    fun saveLightModePreference(context: Context, isLight: Boolean) {
        // Keep active dark holographic theme
    }

    fun normalizeThemeId(themeId: String?): String {
        val clean = themeId?.trim()?.lowercase() ?: "stark"
        return when {
            clean in HOLOGRAPHIC_THEME_IDS -> clean
            clean.contains("matrix") || clean.contains("emerald") -> "matrix_emerald"
            clean.contains("cyber") || clean.contains("cyan") -> "cyberpunk_cyan"
            clean.contains("void") || clean.contains("obsidian") -> "obsidian_void"
            clean.contains("gold") || clean.contains("24k") || clean.contains("royal") -> "sovereign_gold"
            else -> "stark"
        }
    }

    fun ensureInitialized(context: Context): String {
        val loaded = loadThemePreference(context)
        saveThemePreference(context, loaded)
        return loaded
    }
}

// ============================================================================
// UNIFIED DEEP DARK NAVY + ELECTRIC PURPLE / VIOLET PALETTE TOKENS
// ============================================================================
val ObsidianDarkBackground = Color(0xFF05050A)       // Deep Dark Navy / Abyss Canvas
val ObsidianDarkPrimarySurface = Color(0xFF0D0E17)   // Rich Dark Navy Card Surface
val ObsidianDarkSecondarySurface = Color(0xFF151329) // Elevated Deep Violet-Navy Glass Surface
val ObsidianDarkBorder = Color(0xFF2B224C)           // Subtle Violet-Navy Hairline Border
val ObsidianDarkPrimaryText = Color(0xFFF7F4FF)      // Crisp White-Lavender Primary Typography
val ObsidianDarkSecondaryText = Color(0xFFB8B0D4)    // Soft Lavender Secondary Typography
val ObsidianDarkMutedText = Color(0xFF8E84AD)        // Muted Violet-Slate Caption Typography
val ObsidianDarkSilver = Color(0xFFD8B4FE)           // Luminous Lavender-Silver Metallic Accent
val ObsidianDarkEmeraldAccent = Color(0xFFA855F7)    // Electric Purple / Violet Primary Accent
val ObsidianDarkPositive = Color(0xFF2EE59D)         // Vibrant Emerald for Income & Lena
val ObsidianDarkNegative = Color(0xFFFF4D8D)         // Subtle Rose / Crimson for Expenses & Dena
val ObsidianDarkWarning = Color(0xFFC084FC)          // Soft Violet-Orchid Warning Accent
val ObsidianDarkGoldReserve = Color(0xFFA855F7)      // Electric Purple Reserve Accent

val EmeraldChartFillTop = Color(0x55A855F7)
val EmeraldChartFillBottom = Color(0x00A855F7)

// ============================================================================
// COMPATIBILITY ALIASES (MAPPED TO UNIFIED PURPLE LUXURY PALETTE)
// ============================================================================
val NeonThemeBackground = ObsidianDarkBackground
val NeonThemePrimarySurface = ObsidianDarkPrimarySurface
val NeonThemeSecondarySurface = ObsidianDarkSecondarySurface
val NeonThemeBorder = ObsidianDarkBorder
val NeonThemePrimaryText = ObsidianDarkPrimaryText
val NeonThemeSecondaryText = ObsidianDarkSecondaryText
val NeonThemeMutedText = ObsidianDarkMutedText
val NeonThemeCyanAccent = Color(0xFFA855F7)
val NeonThemeEmeraldAccent = Color(0xFF2EE59D)
val NeonThemePurpleAccent = Color(0xFF7C3AED)
val NeonThemeNegative = Color(0xFFFF4D8D)
val NeonThemeWarning = Color(0xFFC084FC)

val ObsidianLightBackground = ObsidianDarkBackground
val ObsidianLightPrimarySurface = ObsidianDarkPrimarySurface
val ObsidianLightSecondarySurface = ObsidianDarkSecondarySurface
val ObsidianLightBorder = ObsidianDarkBorder
val ObsidianLightPrimaryText = ObsidianDarkPrimaryText
val ObsidianLightSecondaryText = ObsidianDarkSecondaryText
val ObsidianLightMutedText = ObsidianDarkMutedText
val ObsidianLightSilver = ObsidianDarkSilver
val ObsidianLightEmeraldAccent = ObsidianDarkEmeraldAccent
val ObsidianLightPositive = ObsidianDarkPositive
val ObsidianLightNegative = ObsidianDarkNegative
val ObsidianLightWarning = ObsidianDarkWarning
val ObsidianLightGoldReserve = ObsidianDarkGoldReserve

// ============================================================================
// SEMANTIC TOKENS USED ACROSS ALL KHARCHEE SCREENS & MODALS
// ============================================================================
val ObsidianBlack: Color
    get() = ObsidianDarkBackground

val DeepForestAbyss: Color
    get() = ObsidianDarkBackground

val RichGraphite: Color
    get() = ObsidianDarkPrimarySurface

val ElevatedGraphite: Color
    get() = ObsidianDarkSecondarySurface

val ForestGlassSurface: Color
    get() = ObsidianDarkSecondarySurface

val HairlinePlatinum: Color
    get() = ObsidianDarkBorder

val HairlineGold: Color
    get() = Color(0xFF7C3AED)

// Primary Purple/Violet Luxury Accents
val ChampagneGold: Color
    get() = ObsidianDarkEmeraldAccent // #A855F7 Electric Purple

val ChampagneGoldSoft: Color
    get() = Color(0xFF7C3AED) // #7C3AED Deep Royal Violet

val EmeraldNeonGlow: Color
    get() = ObsidianDarkEmeraldAccent

val DeepEmeraldAccent: Color
    get() = Color(0xFF7C3AED)

val MetallicSilver: Color
    get() = ObsidianDarkSilver

val WarmIvory: Color
    get() = ObsidianDarkPrimaryText

val MutedPlatinum: Color
    get() = ObsidianDarkSecondaryText

val MutedIvory: Color
    get() = ObsidianDarkMutedText

val SophisticatedEmerald: Color
    get() = ObsidianDarkPositive // #2EE59D Vibrant Emerald for Income/Lena

val AmberWarning: Color
    get() = ObsidianDarkWarning

val CrimsonRuby: Color
    get() = ObsidianDarkNegative // #FF4D8D Subtle Rose for Expense/Dena

val GoldReserveAccent: Color
    get() = Color(0xFFA855F7)

val RoyalGold: Color
    get() = Color(0xFFA855F7)

// Category Accents (Harmonized with Deep Dark Navy + Purple/Violet Luxury Theme)
val CategoryFoodColor = Color(0xFFA855F7)          // Electric Purple
val CategoryGroceriesColor = Color(0xFF2EE59D)     // Vibrant Emerald
val CategoryShoppingColor = Color(0xFFC084FC)      // Luminous Orchid
val CategoryTravelColor = Color(0xFF818CF8)        // Indigo-Violet
val CategoryBillsColor = Color(0xFF38BDF8)         // Sky-Violet
val CategoryMedicalColor = Color(0xFFFF4D8D)       // Rose
val CategoryFamilyColor = Color(0xFFE879F9)        // Fuchsia-Violet
val CategoryEducationColor = Color(0xFF7C3AED)     // Deep Violet
val CategoryEntertainmentColor = Color(0xFFD8B4FE) // Soft Lavender
val CategorySubscriptionsColor = Color(0xFF9333EA) // Vivid Purple
val CategoryEmiColor = Color(0xFFF472B6)           // Pink-Rose
val CategoryRentColor = Color(0xFF6366F1)          // Royal Indigo
val CategoryIncomeColor = Color(0xFF2EE59D)        // Positive Emerald
val CategoryOtherColor = Color(0xFFB8B0D4)         // Lavender-Silver

fun getCategoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "food", "dining", "chai", "snacks", "swiggy", "zomato" -> CategoryFoodColor
        "groceries", "grocery", "milk", "vegetables" -> CategoryGroceriesColor
        "shopping", "clothes", "electronics", "amazon", "flipkart" -> CategoryShoppingColor
        "travel", "transport", "cab", "auto", "metro", "fuel", "petrol", "uber", "ola" -> CategoryTravelColor
        "bills", "utilities", "electricity", "recharge", "wifi", "mobile", "water" -> CategoryBillsColor
        "medical", "health", "pharmacy", "doctor", "fitness" -> CategoryMedicalColor
        "family", "parents", "home", "gifts" -> CategoryFamilyColor
        "education", "books", "course", "tuition", "college" -> CategoryEducationColor
        "entertainment", "movies", "gaming", "outing" -> CategoryEntertainmentColor
        "subscriptions", "subscription", "netflix", "spotify", "ott" -> CategorySubscriptionsColor
        "emi", "loan", "credit card" -> CategoryEmiColor
        "rent", "hostel", "pg", "maintenance" -> CategoryRentColor
        "income", "salary", "stipend", "freelance", "cashback", "refund" -> CategoryIncomeColor
        else -> CategoryOtherColor
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.trim().lowercase()) {
        "food", "dining", "chai", "snacks", "swiggy", "zomato" -> Icons.Default.LocalCafe
        "groceries", "grocery", "milk", "vegetables" -> Icons.Default.ShoppingCart
        "shopping", "clothes", "electronics", "amazon", "flipkart" -> Icons.Default.ShoppingBag
        "travel", "transport", "cab", "auto", "metro", "fuel", "petrol", "uber", "ola" -> Icons.Default.DirectionsCar
        "bills", "utilities", "electricity", "recharge", "wifi", "mobile", "water" -> Icons.Default.Bolt
        "medical", "health", "pharmacy", "doctor", "fitness" -> Icons.Default.MedicalServices
        "family", "parents", "home", "gifts" -> Icons.Default.Home
        "education", "books", "course", "tuition", "college" -> Icons.Default.School
        "entertainment", "movies", "gaming", "outing" -> Icons.Default.Movie
        "subscriptions", "subscription", "netflix", "spotify", "ott" -> Icons.Default.Autorenew
        "emi", "loan", "credit card" -> Icons.Default.CreditCard
        "rent", "hostel", "pg", "maintenance" -> Icons.Default.Key
        "income", "salary", "stipend", "freelance", "cashback", "refund" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Category
    }
}

// ============================================================================
// HUD THEME ENGINE (UNIFIED SINGLE PURPLE/VIOLET LUXURY THEME)
// ============================================================================
data class HudThemePalette(
    val id: String,
    val name: String,
    val subtitle: String,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val accent: Color,
    val secondaryAccent: Color,
    val borderGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val symbol: String
)

val StarkHudTheme = HudThemePalette(
    id = "stark",
    name = "Purple Luxury Vault",
    subtitle = "Deep Dark Navy (#05050A) · Electric Purple & Violet (#7C3AED / #A855F7)",
    background = ObsidianDarkBackground,
    surface = ObsidianDarkPrimarySurface,
    surfaceElevated = ObsidianDarkSecondarySurface,
    accent = Color(0xFFA855F7),
    secondaryAccent = Color(0xFF7C3AED),
    borderGlow = Color(0xFF7C3AED),
    textPrimary = ObsidianDarkPrimaryText,
    textSecondary = ObsidianDarkSecondaryText,
    symbol = "◆"
)

val MatrixEmeraldHudTheme = HudThemePalette(
    id = "matrix_emerald",
    name = "Matrix Emerald",
    subtitle = "Bio-Digital Carbon (#020C07) · Neon Matrix Emerald (#00E676)",
    background = Color(0xFF020C07),
    surface = Color(0xFF06160E),
    surfaceElevated = Color(0xFF0B2418),
    accent = Color(0xFF00E676),
    secondaryAccent = Color(0xFF10B981),
    borderGlow = Color(0xFF00E676),
    textPrimary = Color(0xFFECFDF5),
    textSecondary = Color(0xFF6EE7B7),
    symbol = "🟢"
)

val CyberpunkCyanHudTheme = HudThemePalette(
    id = "cyberpunk_cyan",
    name = "Cyberpunk Cyan",
    subtitle = "Night City Abyss (#030914) · High-Voltage Neon Cyan (#00E5FF)",
    background = Color(0xFF030914),
    surface = Color(0xFF071324),
    surfaceElevated = Color(0xFF0D2038),
    accent = Color(0xFF00E5FF),
    secondaryAccent = Color(0xFF38BDF8),
    borderGlow = Color(0xFF00E5FF),
    textPrimary = Color(0xFFF0F9FF),
    textSecondary = Color(0xFF7DD3FC),
    symbol = "⚡"
)

val ObsidianVoidHudTheme = HudThemePalette(
    id = "obsidian_void",
    name = "Obsidian Void",
    subtitle = "Pure OLED Pitch Void (#030712) · Brushed Titanium & Violet (#A855F7)",
    background = Color(0xFF030712),
    surface = Color(0xFF090D16),
    surfaceElevated = Color(0xFF111827),
    accent = Color(0xFFA855F7),
    secondaryAccent = Color(0xFF6366F1),
    borderGlow = Color(0xFF4F46E5),
    textPrimary = Color(0xFFF9FAFB),
    textSecondary = Color(0xFF9CA3AF),
    symbol = "🌑"
)

val SovereignGold24KHudTheme = HudThemePalette(
    id = "sovereign_gold",
    name = "24K Sovereign Gold",
    subtitle = "Imperial Bullion Vault (#0B0802) · 24K Sovereign Gold (#FFD700)",
    background = Color(0xFF0B0802),
    surface = Color(0xFF161105),
    surfaceElevated = Color(0xFF241B08),
    accent = Color(0xFFFFD700),
    secondaryAccent = Color(0xFFD4AF37),
    borderGlow = Color(0xFFFFD700),
    textPrimary = Color(0xFFFFFBEB),
    textSecondary = Color(0xFFFDE68A),
    symbol = "👑"
)

val CyberpunkHudTheme = CyberpunkCyanHudTheme
val RoyalGoldHudTheme = SovereignGold24KHudTheme

val ALL_HUD_THEMES = listOf(
    MatrixEmeraldHudTheme,
    CyberpunkCyanHudTheme,
    ObsidianVoidHudTheme,
    SovereignGold24KHudTheme,
    StarkHudTheme
)

val HOLOGRAPHIC_STUDIO_THEMES = listOf(
    MatrixEmeraldHudTheme,
    CyberpunkCyanHudTheme,
    ObsidianVoidHudTheme,
    SovereignGold24KHudTheme
)

fun getHudThemeById(id: String): HudThemePalette {
    val normalized = ThemePreferenceManager.normalizeThemeId(id)
    return ALL_HUD_THEMES.firstOrNull { it.id == normalized } ?: StarkHudTheme
}

// ============================================================================
// OFFICIAL KHARCHEE NORMAL APP DESIGN SYSTEM:
// MIDNIGHT BLACK (#080B08) × PURE WHITE (#FFFFFF) × LIME GREEN (#B7F000)
// 60% Midnight Black · 30% Pure White · 10% Lime Green
// ============================================================================
val KharcheeMidnightBlack = Color(0xFF080B08)
val KharcheePureWhite = Color(0xFFFFFFFF)
val KharcheeLimeGreen = Color(0xFFB7F000)

// Dark Surfaces (60% Midnight Black hierarchy)
val KharcheeDarkSurface = Color(0xFF101510)
val KharcheeDarkElevated = Color(0xFF161D16)
val KharcheeDarkBorder = Color(0xFF232E23)
val KharcheeSubtextOnDark = Color(0xFFC8D2C8)
val KharcheeMutedOnDark = Color(0xFF8B988B)

// White Surfaces (30% Pure White hierarchy for key financial summaries & insights)
val KharcheeWhiteSurface = Color(0xFFFFFFFF)
val KharcheeWhiteSubtle = Color(0xFFF3F6F1)
val KharcheeWhiteBorder = Color(0xFFE0E6DD)
val KharcheeInkOnWhite = Color(0xFF080B08)
val KharcheeSubtextOnWhite = Color(0xFF475247)
val KharcheeMutedOnWhite = Color(0xFF6B776B)

// Controlled Lime & Semantic Accents (10% Lime Green hierarchy)
val KharcheeLimeInk = Color(0xFF080B08)
val KharcheeLimeDeepTint = Color(0xFF172206)
val KharcheePositiveOnWhite = Color(0xFF285900)
val KharcheeOutflowCoral = Color(0xFFF87171)
val KharcheeOutflowOnWhite = Color(0xFFB91C1C)

// ============================================================================
// KHATA DESIGN SYSTEM (MIDNIGHT BLACK × PURE WHITE × LIME GREEN)
// ============================================================================

// Background & Dark Surfaces
val KhataPrimaryBackground = KharcheeMidnightBlack
val KhataSecondaryBackground = KharcheeDarkSurface
val KhataCardSurface = KharcheeDarkSurface
val KhataElevatedCardSurface = KharcheeDarkElevated

// Surface & Border Tokens
val KhataDeepBurgundy = Color(0xFF131B10)
val KhataBurgundySurface = Color(0xFF192412)
val KhataBurgundyBorder = Color(0xFF2A3826)

// Typography Tokens
val KhataPrimaryText = KharcheePureWhite
val KhataSecondaryText = KharcheeSubtextOnDark
val KhataMutedText = KharcheeMutedOnDark
val KhataPlatinumHighlight = KharcheePureWhite

// Financial Colours (Lena = Lime Green, Dena = Crisp White / Neutral Outflow)
val KhataLenaReceive = KharcheeLimeGreen
val KhataDenaPay = Color(0xFFF87171)
val KhataNetPositionHighlight = KharcheeLimeGreen

// Brand Accent
val KhataBrandPurple = KharcheeLimeGreen

// Supporting Khata Surface & Glass Tokens
val KhataLenaDarkGlassSurface = Color(0xFF121C09)
val KhataLenaGlassOverlay = Color(0x1AB7F000)
val KhataDenaDarkGlassSurface = Color(0xFF1C1212)
val KhataDenaGlassOverlay = Color(0x14F87171)
val KhataSettledRoseSlate = Color(0xFF647064)


