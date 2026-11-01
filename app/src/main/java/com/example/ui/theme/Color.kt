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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Dark Emerald Fintech & Holographic Sovereign Palette
val DeepForestAbyss = Color(0xFF030806)
val ObsidianBlack = Color(0xFF040B08)
val RichGraphite = Color(0xFF091712)
val ElevatedGraphite = Color(0xFF10261D)
val ForestGlassSurface = Color(0xFF143024)

// Primary Luminous Emerald Highlights (replaces gold overload with authentic emerald fintech glow)
val ChampagneGold = Color(0xFF00E699)
val ChampagneGoldSoft = Color(0xFF6FF7C8)
val SophisticatedEmerald = Color(0xFF10B981)
val EmeraldNeonGlow = Color(0xFF34D399)
val DeepEmeraldAccent = Color(0xFF059669)
val GoldReserveAccent = Color(0xFFD6B77A)

// Crisp Alabaster & Frosted Sage Typography/Surface Accents
val WarmIvory = Color(0xFFF0FDF7)
val MutedPlatinum = Color(0xFFA7C7B9)
val MutedIvory = Color(0xFF6C9282)
val CrimsonRuby = Color(0xFFFF5A79)

// Refined Glowing Hairlines & Chart Gradient Stops
val HairlinePlatinum = Color(0x2E34D399)
val HairlineGold = Color(0x5900E699)
val EmeraldChartFillTop = Color(0x5500E699)
val EmeraldChartFillBottom = Color(0x0000E699)

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
)

val StarkHudTheme = KharcheeHudTheme(
    id = "stark",
    name = "Sovereign Emerald",
    subtitle = "Deep forest obsidian & glowing emerald matrix",
    symbol = "◆",
    accent = ChampagneGold,
    secondaryAccent = SophisticatedEmerald,
    background = ObsidianBlack,
    surface = RichGraphite,
    surfaceElevated = ElevatedGraphite,
    borderGlow = Color(0x5900E699)
)

val EmeraldHudTheme = KharcheeHudTheme(
    id = "emerald",
    name = "Geneva Mint Reserve",
    subtitle = "Private banking neon mint & dark botanical glass",
    symbol = "❖",
    accent = EmeraldNeonGlow,
    secondaryAccent = ChampagneGold,
    background = ObsidianBlack,
    surface = RichGraphite,
    surfaceElevated = ElevatedGraphite,
    borderGlow = Color(0x5934D399)
)

val MidnightHudTheme = KharcheeHudTheme(
    id = "midnight",
    name = "Cyber Jade HUD",
    subtitle = "Tony Stark holographic teal-emerald matrix",
    symbol = "◈",
    accent = Color(0xFF00F5D4),
    secondaryAccent = ChampagneGold,
    background = ObsidianBlack,
    surface = RichGraphite,
    surfaceElevated = ElevatedGraphite,
    borderGlow = Color(0x5900F5D4)
)

val ArmorHudTheme = KharcheeHudTheme(
    id = "armor",
    name = "Emerald & Gold Bullion",
    subtitle = "Dark forest vault with sovereign gold trim",
    symbol = "⬢",
    accent = GoldReserveAccent,
    secondaryAccent = ChampagneGold,
    background = ObsidianBlack,
    surface = RichGraphite,
    surfaceElevated = ElevatedGraphite,
    borderGlow = Color(0x4DD6B77A)
)

val ALL_HUD_THEMES = listOf(
    StarkHudTheme,
    EmeraldHudTheme,
    MidnightHudTheme,
    ArmorHudTheme
)

fun getHudThemeById(id: String): KharcheeHudTheme {
    return ALL_HUD_THEMES.find { it.id == id } ?: StarkHudTheme
}

fun getCategoryColor(category: String): Color = when (category) {
    "Food" -> Color(0xFF00E699) // Luminous Emerald
    "Chai & Snacks" -> Color(0xFF34D399) // Mint Emerald
    "Transport" -> Color(0xFF2DD4BF) // Cyber Teal
    "Grocery" -> Color(0xFF10B981) // Forest Emerald
    "Entertainment" -> Color(0xFF38BDF8) // Holographic Cyan
    "Bills" -> CrimsonRuby // Coral Rose Warning
    "Shopping" -> Color(0xFFA3E635) // Electric Lime-Emerald
    "Salary" -> Color(0xFF00E699)
    "Freelance" -> Color(0xFF2DD4BF)
    "Cashback" -> GoldReserveAccent
    else -> MutedPlatinum
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
