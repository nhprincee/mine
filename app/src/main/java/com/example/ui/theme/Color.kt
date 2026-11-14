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
    // Locked to the single universal Purple/Violet Luxury theme ("stark")
    private var _activeThemeId by mutableStateOf("stark")
    var activeThemeId: String
        get() = _activeThemeId
        set(_) {
            _activeThemeId = "stark"
        }

    var isLightMode: Boolean
        get() = false
        set(_) {
            _activeThemeId = "stark"
        }
}

object ThemePreferenceManager {
    private const val PREFS_NAME = "kharchee_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"

    fun loadThemePreference(context: Context): String {
        ObsidianThemeState.activeThemeId = "stark"
        return "stark"
    }

    fun saveThemePreference(context: Context, themeId: String) {
        ObsidianThemeState.activeThemeId = "stark"
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_ID, "stark")
            .apply()
    }

    fun loadLightModePreference(context: Context): Boolean = false

    fun saveLightModePreference(context: Context, isLight: Boolean) {
        saveThemePreference(context, "stark")
    }

    fun normalizeThemeId(themeId: String?): String = "stark"

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

val CyberpunkHudTheme = StarkHudTheme
val RoyalGoldHudTheme = StarkHudTheme

val ALL_HUD_THEMES = listOf(StarkHudTheme)

fun getHudThemeById(id: String): HudThemePalette = StarkHudTheme
