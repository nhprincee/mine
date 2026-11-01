package com.example.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val DEFAULT_FONT_ID = "manrope"

/**
 * Manrope (Google Fonts local bundle) with Noto Sans Devanagari fallback
 * for sharp, bold fintech headings, screen titles, and large financial numbers/balances (ExtraBold 800).
 */
val ManropeFontFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Bold),
    Font(R.font.manrope, FontWeight.ExtraBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold),
    Font(R.font.noto_sans_devanagari, FontWeight.ExtraBold)
)

/**
 * Playfair Display (Google Fonts local bundle) with Noto Sans Devanagari fallback
 * for luxury editorial headings and selected financial highlights.
 */
val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold),
    Font(R.font.playfair_display, FontWeight.ExtraBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold),
    Font(R.font.noto_sans_devanagari, FontWeight.ExtraBold)
)

/**
 * Inter (Google Fonts local bundle) with Noto Sans Devanagari fallback
 * for clean body text (Regular 400), secondary labels (Medium 500), and buttons/navigation labels (SemiBold 600).
 */
val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold),
    Font(R.font.inter, FontWeight.ExtraBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold),
    Font(R.font.noto_sans_devanagari, FontWeight.ExtraBold)
)

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold),
    Font(R.font.outfit, FontWeight.ExtraBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold),
    Font(R.font.noto_sans_devanagari, FontWeight.ExtraBold)
)

val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold),
    Font(R.font.noto_sans_devanagari, FontWeight.ExtraBold)
)

val MonospaceHudFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono, FontWeight.Bold),
    Font(R.font.jetbrains_mono, FontWeight.ExtraBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold),
    Font(R.font.noto_sans_devanagari, FontWeight.ExtraBold)
)

val NotoSansDevanagariFontFamily = FontFamily(
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold),
    Font(R.font.noto_sans_devanagari, FontWeight.ExtraBold)
)

data class AppFontOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val sampleText: String,
    val fontFamily: FontFamily,
    val bodyFontFamily: FontFamily = InterFontFamily
)

val ALL_APP_FONTS = listOf(
    AppFontOption(
        id = "manrope",
        name = "Manrope ExtraBold + Inter",
        subtitle = "Default · Manrope ExtraBold (800) Headings & Balances + Inter (400/500/600) UI",
        sampleText = "₹24,500 · Kharchee Sovereign Vault · बजट और बचत",
        fontFamily = ManropeFontFamily,
        bodyFontFamily = InterFontFamily
    ),
    AppFontOption(
        id = "playfair",
        name = "Playfair Display",
        subtitle = "Sovereign Editorial Serif + Inter Telemetry (Hindi/Hinglish Ready)",
        sampleText = "₹24,500 · Kharchee Sovereign Vault · बजट और बचत",
        fontFamily = PlayfairDisplayFontFamily,
        bodyFontFamily = InterFontFamily
    ),
    AppFontOption(
        id = "inter",
        name = "Inter",
        subtitle = "Precision Swiss Neo-Grotesque · Clean Financial Telemetry",
        sampleText = "₹24,500 · Kharchee Sovereign Vault · बजट और बचत",
        fontFamily = InterFontFamily,
        bodyFontFamily = InterFontFamily
    ),
    AppFontOption(
        id = "outfit",
        name = "Outfit",
        subtitle = "Geometric Display Sans · Clean Modern Numbers",
        sampleText = "₹24,500 · Kharchee Sovereign Vault · बजट और बचत",
        fontFamily = OutfitFontFamily,
        bodyFontFamily = InterFontFamily
    ),
    AppFontOption(
        id = "jakarta",
        name = "Plus Jakarta Sans",
        subtitle = "Neo-Grotesque UI Sans · High-Density Readability",
        sampleText = "₹24,500 · Kharchee Sovereign Vault · बजट और बचत",
        fontFamily = PlusJakartaSansFontFamily,
        bodyFontFamily = InterFontFamily
    ),
    AppFontOption(
        id = "jetbrains",
        name = "JetBrains Mono",
        subtitle = "Tactical HUD Monospace · Terminal Telemetry",
        sampleText = "₹24,500 · Kharchee Sovereign Vault · बजट और बचत",
        fontFamily = MonospaceHudFontFamily,
        bodyFontFamily = InterFontFamily
    )
)

fun getAppFontOptionById(fontId: String?): AppFontOption {
    if (fontId.isNullOrBlank()) return ALL_APP_FONTS.first()
    return ALL_APP_FONTS.find { it.id == fontId } ?: ALL_APP_FONTS.first()
}

/**
 * Manages the user's selected font preference locally.
 * - Defaults new users to "manrope" (Manrope ExtraBold 800 + Inter 400/500/600 pairing).
 * - Never overwrites existing users' saved font preferences.
 */
object FontPreferenceManager {
    private const val PREFS_NAME = "kharchee_font_preferences"
    private const val KEY_SELECTED_FONT_ID = "selected_font_id"

    private val _selectedFontId = MutableStateFlow(DEFAULT_FONT_ID)
    val selectedFontId: StateFlow<String> = _selectedFontId.asStateFlow()

    @Volatile
    private var initialized = false

    fun ensureInitialized(context: Context): String {
        val appContext = context.applicationContext ?: context
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SELECTED_FONT_ID, null)
        val resolved = if (!saved.isNullOrBlank() && ALL_APP_FONTS.any { it.id == saved }) {
            saved
        } else {
            DEFAULT_FONT_ID
        }
        if (!initialized || _selectedFontId.value != resolved) {
            _selectedFontId.value = resolved
            initialized = true
        }
        return resolved
    }

    fun setSelectedFont(context: Context, fontId: String) {
        val validOption = getAppFontOptionById(fontId)
        val appContext = context.applicationContext ?: context
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_FONT_ID, validOption.id).apply()
        _selectedFontId.value = validOption.id
        initialized = true
    }
}

val LocalAppFontOption = staticCompositionLocalOf { ALL_APP_FONTS.first() }
val LocalAppFontFamily = staticCompositionLocalOf { ManropeFontFamily }
val LocalAppBodyFontFamily = staticCompositionLocalOf { InterFontFamily }

/**
 * Used across screens for buttons, navigation labels, secondary labels, and body telemetry.
 * Resolves to Inter (with Noto Sans Devanagari fallback) across all screens.
 */
val JetBrainsMonoFontFamily: FontFamily
    @Composable
    get() = LocalAppBodyFontFamily.current

fun buildAppTypography(
    headingFontFamily: FontFamily = ManropeFontFamily,
    bodyFontFamily: FontFamily = InterFontFamily
): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.3).sp
        ),
        displaySmall = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 25.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.2).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.2).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 19.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.1).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            lineHeight = 24.sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            lineHeight = 25.sp,
            letterSpacing = (-0.1).sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 19.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 19.sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 17.sp
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.2.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.3.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            letterSpacing = 0.4.sp
        )
    )
}

val Typography = buildAppTypography(ManropeFontFamily, InterFontFamily)
