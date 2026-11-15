package com.example.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ExpenseRecord
import com.example.data.model.KhataContact
import com.example.data.model.KhataContactComputedState
import com.example.data.model.KhataDirection
import com.example.data.model.isIncomeTransaction
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import com.example.util.KhataSplitCardGenerator
import com.example.util.PerformancePreferences
import com.example.util.SovereignActivationManager
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

// ============================================================================
// 1. SOVEREIGN METAL CARD FINISHES (HERO STATUS FLEX)
// ============================================================================
enum class SovereignMetalFinish(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val badgeColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val borderColors: List<Color>,
    val gradientStops: List<Color>
) {
    OBSIDIAN_TITANIUM(
        id = "obsidian_titanium",
        displayName = "Obsidian Titanium",
        subtitle = "Matte Black + Neon Cyan Edge",
        badgeColor = Color(0xFF00E5FF),
        primaryTextColor = Color(0xFFF8FAFC),
        secondaryTextColor = Color(0xFF94A3B8),
        borderColors = listOf(Color(0xFF00E5FF), Color(0xFF38BDF8), Color(0xFF1E293B), Color(0xFF00E5FF)),
        gradientStops = listOf(Color(0xFF070B14), Color(0xFF131C2E), Color(0xFF090E1A), Color(0xFF050811))
    ),
    SOVEREIGN_GOLD_24K(
        id = "sovereign_gold_24k",
        displayName = "24K Sovereign Gold",
        subtitle = "Brushed Gold + Black Embossed",
        badgeColor = Color(0xFF141004),
        primaryTextColor = Color(0xFF0F0B02),
        secondaryTextColor = Color(0xFF3E2D06),
        borderColors = listOf(Color(0xFFFFF8E1), Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFE082)),
        gradientStops = listOf(Color(0xFFFFE57F), Color(0xFFD4AF37), Color(0xFFFFC107), Color(0xFFB8860B))
    ),
    CYBERPUNK_EMERALD(
        id = "cyberpunk_emerald",
        displayName = "Cyberpunk Emerald",
        subtitle = "Matrix Carbon + Neon Green Glow",
        badgeColor = Color(0xFF00E676),
        primaryTextColor = Color(0xFFECFDF5),
        secondaryTextColor = Color(0xFF6EE7B7),
        borderColors = listOf(Color(0xFF00E676), Color(0xFF10B981), Color(0xFF064E3B), Color(0xFF00E676)),
        gradientStops = listOf(Color(0xFF03140D), Color(0xFF072B1C), Color(0xFF041B11), Color(0xFF020E09))
    )
}

// ============================================================================
// 2. VAMPIRE MONEY-LEAK SCANNER DATA MODELS
// ============================================================================
data class VampireLeakItem(
    val id: String,
    val title: String,
    val leakTypeLabel: String,
    val occurrencesPerMonth: Int,
    val monthlyLeakAmount: Long,
    val yearlyDrainAmount: Long,
    val recommendation: String
)

data class VampireLeakScanReport(
    val totalMonthlyLeakInr: Long,
    val totalYearlyDrainInr: Long,
    val bannerHeadline: String,
    val leaks: List<VampireLeakItem>,
    val isLiveFromUserTransactions: Boolean
)

// ============================================================================
// 3. SECRET GHOST VAULT (DECOY HIDDEN LEDGER) MODELS
// ============================================================================
data class GhostVaultSecretEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Long,
    val category: String = "Classified",
    val isIncome: Boolean = false,
    val secretNote: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

// ============================================================================
// 4. JARVIS UNFILTERED PERSONAS
// ============================================================================
enum class JarvisUnfilteredPersona(
    val id: String,
    val badgeTitle: String,
    val subtitle: String,
    val accentColor: Color
) {
    BRUTAL_SHARK_ROAST(
        id = "shark_roast",
        badgeTitle = "🦈 Brutal Shark Roast",
        subtitle = "Zero-mercy reality check on impulse spending & lifestyle creep",
        accentColor = Color(0xFFFF3366)
    ),
    SWISS_WEALTH_BANKER(
        id = "swiss_banker",
        badgeTitle = "🎩 Swiss Wealth Banker",
        subtitle = "Ultra-discreet Zurich private banking & compounding strategy",
        accentColor = Color(0xFFFFD700)
    ),
    DESI_BIG_BROTHER(
        id = "desi_bade_bhaiya",
        badgeTitle = "🔥 Desi Big-Brother",
        subtitle = "Street-smart Hinglish elder brother guidance to build solid wealth",
        accentColor = Color(0xFF00E676)
    )
}

// ============================================================================
// SOVEREIGN BLACK CLUB ENGINE (PURE LOGIC + ENCRYPTED/ISOLATED STORES)
// ============================================================================
object SovereignBlackClubEngine {

    private const val PREFS_BLACK_CLUB = "kharchee_sovereign_black_club_prefs"
    private const val KEY_SELECTED_METAL_FINISH = "selected_metal_finish_v1"
    private const val KEY_GHOST_PIN_HASH = "ghost_vault_pin_sha256_v1"
    private const val KEY_GHOST_VAULT_ENTRIES_JSON = "ghost_vault_entries_json_v1"
    private const val KEY_DRAIN_STOPPER_ENABLED = "vampire_drain_stopper_alerts_v1"
    private const val KEY_SELECTED_JARVIS_PERSONA = "selected_jarvis_persona_v1"

    fun getSelectedMetalFinish(context: Context): SovereignMetalFinish {
        val raw = context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_METAL_FINISH, SovereignMetalFinish.SOVEREIGN_GOLD_24K.id)
        return SovereignMetalFinish.entries.find { it.id == raw } ?: SovereignMetalFinish.SOVEREIGN_GOLD_24K
    }

    fun setSelectedMetalFinish(context: Context, finish: SovereignMetalFinish) {
        context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_METAL_FINISH, finish.id)
            .apply()
    }

    fun isVampireDrainStopperEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .getBoolean(KEY_DRAIN_STOPPER_ENABLED, true)
    }

    fun setVampireDrainStopperEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DRAIN_STOPPER_ENABLED, enabled)
            .apply()
    }

    fun getSelectedJarvisPersona(context: Context): JarvisUnfilteredPersona {
        val raw = context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_JARVIS_PERSONA, JarvisUnfilteredPersona.BRUTAL_SHARK_ROAST.id)
        return JarvisUnfilteredPersona.entries.find { it.id == raw } ?: JarvisUnfilteredPersona.BRUTAL_SHARK_ROAST
    }

    fun setSelectedJarvisPersona(context: Context, persona: JarvisUnfilteredPersona) {
        context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_JARVIS_PERSONA, persona.id)
            .apply()
    }

    /**
     * Feature A: Scans expenses for recurring OTT subscriptions, Swiggy/Zomato food delivery loops,
     * late-night impulse spends (11 PM - 4 AM), and repeated discretionary micro-drains.
     */
    fun scanVampireMoneyLeaks(expenses: List<ExpenseRecord>): VampireLeakScanReport {
        val outflows = expenses.filter { !isIncomeTransaction(it) && it.amount > 0L }
        val detectedLeaks = mutableListOf<VampireLeakItem>()

        val ottKeywords = listOf("netflix", "spotify", "prime", "hotstar", "youtube", "apple", "icloud", "sony", "zee5", "chatgpt", "subscription")
        val deliveryKeywords = listOf("swiggy", "zomato", "blinkit", "zepto", "instamart", "dominos", "pizza", "starbucks", "burger")

        // 1. Check OTT & Recurring Subscriptions
        val ottTxns = outflows.filter { tx ->
            val t = tx.title.lowercase(Locale.ENGLISH)
            val c = tx.category.lowercase(Locale.ENGLISH)
            ottKeywords.any { t.contains(it) } || c.contains("subscription") || c.contains("entertain")
        }
        if (ottTxns.isNotEmpty()) {
            val sumOtt = ottTxns.sumOf { it.amount }
            detectedLeaks.add(
                VampireLeakItem(
                    id = "leak_ott",
                    title = "OTT & Auto-Renewal Subscriptions",
                    leakTypeLabel = "🧛‍♂️ Recurring Subscription",
                    occurrencesPerMonth = ottTxns.size,
                    monthlyLeakAmount = sumOtt,
                    yearlyDrainAmount = sumOtt * 12L,
                    recommendation = "Audit unused OTT plans & switch to annual shared tiers."
                )
            )
        }

        // 2. Check Food Delivery & Quick-Commerce Loops
        val deliveryTxns = outflows.filter { tx ->
            val t = tx.title.lowercase(Locale.ENGLISH)
            val c = tx.category.lowercase(Locale.ENGLISH)
            deliveryKeywords.any { t.contains(it) } || (c.contains("food") && tx.amount >= 180L)
        }
        if (deliveryTxns.isNotEmpty()) {
            val totalFood = deliveryTxns.sumOf { it.amount }
            val avoidablePortion = max(150L, (totalFood * 45L) / 100L)
            detectedLeaks.add(
                VampireLeakItem(
                    id = "leak_delivery",
                    title = "Swiggy / Zomato & Convenience Loop",
                    leakTypeLabel = "🍔 Impulse Delivery Loop",
                    occurrencesPerMonth = deliveryTxns.size,
                    monthlyLeakAmount = avoidablePortion,
                    yearlyDrainAmount = avoidablePortion * 12L,
                    recommendation = "Platform fees + surge pricing add ~45% hidden drain."
                )
            )
        }

        // 3. Check Late-Night Impulse Spends (11 PM to 4 AM)
        val cal = Calendar.getInstance()
        val lateNightTxns = outflows.filter { tx ->
            cal.timeInMillis = tx.txEpochMillis
            val hr = cal.get(Calendar.HOUR_OF_DAY)
            hr >= 23 || hr <= 4
        }
        if (lateNightTxns.isNotEmpty()) {
            val nightSum = lateNightTxns.sumOf { it.amount }
            val avoidableNight = max(100L, (nightSum * 60L) / 100L)
            detectedLeaks.add(
                VampireLeakItem(
                    id = "leak_night",
                    title = "Late-Night Dopamine Spends (11PM–4AM)",
                    leakTypeLabel = "🌙 Midnight Impulse",
                    occurrencesPerMonth = lateNightTxns.size,
                    monthlyLeakAmount = avoidableNight,
                    yearlyDrainAmount = avoidableNight * 12L,
                    recommendation = "Enable Night-Guard cooldown to block 11 PM impulse checkouts."
                )
            )
        }

        // 4. Check Frequent Merchant Loops (any merchant appearing >= 2 times not already counted)
        val repeatedMerchants = outflows
            .groupBy { it.title.trim().lowercase(Locale.ENGLISH) }
            .filter { (name, list) ->
                name.isNotBlank() &&
                    list.size >= 2 &&
                    ottKeywords.none { name.contains(it) } &&
                    deliveryKeywords.none { name.contains(it) }
            }
        repeatedMerchants.entries.take(2).forEachIndexed { idx, (_, txList) ->
            val displayTitle = txList.first().title.trim()
            val sumAmt = txList.sumOf { it.amount }
            val leakAmt = max(100L, (sumAmt * 35L) / 100L)
            detectedLeaks.add(
                VampireLeakItem(
                    id = "leak_repeat_$idx",
                    title = "$displayTitle Repeat Micro-Drain",
                    leakTypeLabel = "🔁 Frequent Micro-Spend",
                    occurrencesPerMonth = txList.size,
                    monthlyLeakAmount = leakAmt,
                    yearlyDrainAmount = leakAmt * 12L,
                    recommendation = "Consolidate or cap weekly spending at $displayTitle."
                )
            )
        }

        // If user has only 1-2 generic transactions or empty ledger, provide a deterministic baseline leak scan
        val isLive = detectedLeaks.isNotEmpty()
        if (detectedLeaks.isEmpty()) {
            val fallbackTotal = if (outflows.isNotEmpty()) {
                max(450L, (outflows.sumOf { it.amount } * 28L) / 100L)
            } else {
                2140L
            }
            val item1 = (fallbackTotal * 45L) / 100L
            val item2 = (fallbackTotal * 35L) / 100L
            val item3 = fallbackTotal - item1 - item2
            detectedLeaks.add(
                VampireLeakItem(
                    id = "sample_leak_1",
                    title = "Swiggy / Zomato Late-Evening Orders",
                    leakTypeLabel = "🍔 Delivery Surge Drain",
                    occurrencesPerMonth = 6,
                    monthlyLeakAmount = item1,
                    yearlyDrainAmount = item1 * 12L,
                    recommendation = "Packing + delivery surge fees quietly drain ₹${item1 * 12L}/yr."
                )
            )
            detectedLeaks.add(
                VampireLeakItem(
                    id = "sample_leak_2",
                    title = "Dormant OTT & App Auto-Renewals",
                    leakTypeLabel = "🧛‍♂️ Silent UPI Mandate",
                    occurrencesPerMonth = 3,
                    monthlyLeakAmount = item2,
                    yearlyDrainAmount = item2 * 12L,
                    recommendation = "Cancel unused auto-pay mandates before next billing cycle."
                )
            )
            detectedLeaks.add(
                VampireLeakItem(
                    id = "sample_leak_3",
                    title = "Unplanned Micro-UPI Impulse Taps",
                    leakTypeLabel = "⚡ Micro-UPI Leak",
                    occurrencesPerMonth = 9,
                    monthlyLeakAmount = item3,
                    yearlyDrainAmount = item3 * 12L,
                    recommendation = "Batch small daily UPI outflows into a fixed weekly allowance."
                )
            )
        }

        val totalMonthlyLeak = detectedLeaks.sumOf { it.monthlyLeakAmount }.coerceAtLeast(150L)
        val totalYearlyDrain = totalMonthlyLeak * 12L
        val formattedLeak = PerformancePreferences.preFormatInr(totalMonthlyLeak)

        return VampireLeakScanReport(
            totalMonthlyLeakInr = totalMonthlyLeak,
            totalYearlyDrainInr = totalYearlyDrain,
            bannerHeadline = "Detected $formattedLeak/mo in avoidable money leaks!",
            leaks = detectedLeaks,
            isLiveFromUserTransactions = isLive
        )
    }

    /**
     * Feature B: Secret Ghost Vault (Decoy Hidden Ledger) protected by a separate 4-digit Ghost PIN.
     * Transactions stored here NEVER appear on the main Home or Khata screens.
     */
    private fun hashGhostPin(pin: String): String {
        val clean = pin.trim()
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("KHARCHEE_GHOST_VAULT_SALT_V1:$clean".toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun hasGhostVaultPin(context: Context, userId: String = "default_user"): Boolean {
        val prefs = context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
        return !prefs.getString("${KEY_GHOST_PIN_HASH}_$userId", null).isNullOrBlank()
    }

    fun setGhostVaultPin(context: Context, userId: String = "default_user", pin: String): Boolean {
        val clean = pin.trim()
        if (clean.length != 4 || !clean.all { it.isDigit() }) return false
        context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .edit()
            .putString("${KEY_GHOST_PIN_HASH}_$userId", hashGhostPin(clean))
            .apply()
        return true
    }

    fun verifyGhostVaultPin(context: Context, userId: String = "default_user", enteredPin: String): Boolean {
        val clean = enteredPin.trim()
        if (clean.length != 4) return false
        val savedHash = context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .getString("${KEY_GHOST_PIN_HASH}_$userId", null) ?: return false
        return savedHash == hashGhostPin(clean)
    }

    fun loadGhostVaultEntries(context: Context, userId: String = "default_user"): List<GhostVaultSecretEntry> {
        val raw = context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .getString("${KEY_GHOST_VAULT_ENTRIES_JSON}_$userId", "[]")
            .orEmpty()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        GhostVaultSecretEntry(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", "Classified Entry"),
                            amount = obj.optLong("amount", 0L),
                            category = obj.optString("category", "Classified"),
                            isIncome = obj.optBoolean("isIncome", false),
                            secretNote = obj.optString("secretNote", ""),
                            timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis())
                        )
                    )
                }
            }.sortedByDescending { it.timestampMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addGhostVaultEntry(
        context: Context,
        userId: String = "default_user",
        title: String,
        amount: Long,
        category: String = "Classified",
        isIncome: Boolean = false,
        secretNote: String = ""
    ): List<GhostVaultSecretEntry> {
        val cleanTitle = title.trim().ifBlank { "Secret Entry" }
        val cleanAmount = amount.coerceAtLeast(1L)
        val current = loadGhostVaultEntries(context, userId).toMutableList()
        current.add(
            0,
            GhostVaultSecretEntry(
                title = cleanTitle,
                amount = cleanAmount,
                category = category.trim().ifBlank { "Classified" },
                isIncome = isIncome,
                secretNote = secretNote.trim()
            )
        )
        saveGhostVaultEntries(context, userId, current)
        return current
    }

    fun deleteGhostVaultEntry(
        context: Context,
        userId: String = "default_user",
        entryId: String
    ): List<GhostVaultSecretEntry> {
        val updated = loadGhostVaultEntries(context, userId).filterNot { it.id == entryId }
        saveGhostVaultEntries(context, userId, updated)
        return updated
    }

    private fun saveGhostVaultEntries(
        context: Context,
        userId: String,
        entries: List<GhostVaultSecretEntry>
    ) {
        val arr = JSONArray()
        entries.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("amount", item.amount)
                    put("category", item.category)
                    put("isIncome", item.isIncome)
                    put("secretNote", item.secretNote)
                    put("timestampMillis", item.timestampMillis)
                }
            )
        }
        context.getSharedPreferences(PREFS_BLACK_CLUB, Context.MODE_PRIVATE)
            .edit()
            .putString("${KEY_GHOST_VAULT_ENTRIES_JSON}_$userId", arr.toString())
            .apply()
    }

    /**
     * Feature D: Builds the personalized Jarvis Unfiltered Persona briefing & spoken TTS script.
     */
    fun buildJarvisPersonaBriefing(
        persona: JarvisUnfilteredPersona,
        userDisplayName: String,
        expenses: List<ExpenseRecord>,
        disciplineScore: Int,
        streakDays: Int,
        monthlyLeakInr: Long
    ): String {
        val name = userDisplayName.trim().ifBlank { "Chief" }
        val outflows = expenses.filter { !isIncomeTransaction(it) }
        val totalSpent = outflows.sumOf { it.amount }
        val topCategory = outflows.groupBy { it.category }
            .maxByOrNull { it.value.sumOf { tx -> tx.amount } }
            ?.key ?: "Food & Convenience"

        val formattedSpent = PerformancePreferences.preFormatInr(totalSpent)
        val formattedLeak = PerformancePreferences.preFormatInr(monthlyLeakInr)
        val yearlyCompounded = PerformancePreferences.preFormatInr(monthlyLeakInr * 12L)

        return when (persona) {
            JarvisUnfilteredPersona.BRUTAL_SHARK_ROAST -> {
                "Listen carefully, $name. You've burned $formattedSpent with $topCategory leading the bleed, and my Vampire Scanner caught $formattedLeak a month in pure avoidable leaks. That's $yearlyCompounded a year vanishing into thin air! Cut the impulse taps today, lock your daily cap, and turn that leak into real equity."
            }
            JarvisUnfilteredPersona.SWISS_WEALTH_BANKER -> {
                "Good evening, $name. Your Zurich Private Desk audit records a Discipline Index of $disciplineScore over a $streakDays-day streak. Reallocating your $formattedLeak monthly discretionary leakage into a 12 percent compounding reserve will preserve $yearlyCompounded this year alone."
            }
            JarvisUnfilteredPersona.DESI_BIG_BROTHER -> {
                "Sun chhote $name! $formattedSpent kharch ho chuke hain aur $topCategory mein sabse zyada paisa ja raha hai. Mahine ka $formattedLeak faltu leaks mein mat uda — saal ka seedha $yearlyCompounded bachega! Aaj se फालतू UPI taps band, Sovereign discipline chalu!"
            }
        }
    }
}

// ============================================================================
// COMPONENT 1: INTERACTIVE 3D SHIMMERING SOVEREIGN METAL CARD (HERO STATUS FLEX)
// ============================================================================
@Composable
fun Interactive3dSovereignMetalCardSection(
    userDisplayName: String,
    deviceHash: String,
    isSovereignPro: Boolean,
    disciplineScore: Int,
    streakDays: Int,
    selectedFinish: SovereignMetalFinish,
    onSelectFinish: (SovereignMetalFinish) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLifecycleResumed by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isLifecycleResumed = event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 3D Touch-tilt angles
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    var touchSheenBias by remember { mutableFloatStateOf(0f) }

    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "metalCardTiltX"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "metalCardTiltY"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sovereignCardSheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isLifecycleResumed) 3600 else 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diagonalSheenProgress"
    )

    val effectiveSheen = if (touchSheenBias != 0f) {
        (sheenProgress + touchSheenBias).coerceIn(-0.4f, 1.4f)
    } else {
        sheenProgress
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sovereign_3d_metal_card_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row above Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "👑 SOVEREIGN BLACK CLUB • HERO STATUS FLEX",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = ChampagneGold
                )
                Text(
                    text = "Touch & drag card to tilt in 3D • Tap finishes below to preview live",
                    fontSize = 11.sp,
                    color = MutedPlatinum
                )
            }
            Surface(
                color = if (isSovereignPro) Color(0x2600E676) else Color(0x26FFD700),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    0.8.dp,
                    if (isSovereignPro) Color(0xFF00E676) else ChampagneGold
                )
            ) {
                Text(
                    text = if (isSovereignPro) "👑 SOVEREIGN ACTIVE" else "✨ LIVE PREVIEW",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSovereignPro) Color(0xFF00E676) else ChampagneGold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Interactive 3D Shimmering Metal Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(222.dp)
                .graphicsLayer {
                    rotationX = animatedTiltX
                    rotationY = animatedTiltY
                    cameraDistance = 14f * density
                }
                .shadow(
                    elevation = 22.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = selectedFinish.borderColors.first()
                )
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.8.dp,
                    brush = Brush.linearGradient(selectedFinish.borderColors),
                    shape = RoundedCornerShape(24.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            tiltX = 0f
                            tiltY = 0f
                            touchSheenBias = 0f
                        },
                        onDragCancel = {
                            tiltX = 0f
                            tiltY = 0f
                            touchSheenBias = 0f
                        },
                        onDrag = { change, _ ->
                            val w = size.width.coerceAtLeast(1).toFloat()
                            val h = size.height.coerceAtLeast(1).toFloat()
                            val normX = ((change.position.x / w) - 0.5f) * 2f
                            val normY = ((change.position.y / h) - 0.5f) * 2f
                            tiltY = (normX * 12f).coerceIn(-14f, 14f)
                            tiltX = (-normY * 10f).coerceIn(-12f, 12f)
                            touchSheenBias = normX * 0.35f
                        }
                    )
                }
                .pointerInput(selectedFinish) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.coerceAtLeast(1).toFloat()
                        val h = size.height.coerceAtLeast(1).toFloat()
                        val normX = ((tapOffset.x / w) - 0.5f) * 2f
                        val normY = ((tapOffset.y / h) - 0.5f) * 2f
                        tiltY = (normX * 9f).coerceIn(-12f, 12f)
                        tiltX = (-normY * 8f).coerceIn(-10f, 10f)
                    }
                }
                .testTag("sovereign_interactive_3d_metal_card")
        ) {
            // Canvas Brushed Metal / Matrix Carbon + Diagonal Specular Metallic Sheen
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = selectedFinish.gradientStops,
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )

                // Fine texture lines (Brushed grain for Gold/Titanium, Carbon grid for Cyberpunk Emerald)
                if (selectedFinish == SovereignMetalFinish.CYBERPUNK_EMERALD) {
                    val step = 28f
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFF00E676).copy(alpha = 0.08f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFF00E676).copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }
                } else {
                    val lineCount = 18
                    for (i in 0..lineCount) {
                        val y = (size.height / lineCount) * i
                        drawLine(
                            color = Color.White.copy(
                                alpha = if (selectedFinish == SovereignMetalFinish.SOVEREIGN_GOLD_24K) 0.12f else 0.04f
                            ),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.2f
                        )
                    }
                }

                // Diagonal Specular Metallic Sheen Sweep
                val sheenCenterX = size.width * effectiveSheen
                val sheenWidth = size.width * 0.38f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(
                                alpha = if (selectedFinish == SovereignMetalFinish.SOVEREIGN_GOLD_24K) 0.36f else 0.18f
                            ),
                            Color.Transparent
                        ),
                        start = Offset(sheenCenterX - sheenWidth, 0f),
                        end = Offset(sheenCenterX + sheenWidth, size.height)
                    )
                )

                // Subtle EMV Metallic Chip Graphic
                val chipLeft = 64f
                val chipTop = size.height * 0.42f
                val chipW = 110f
                val chipH = 78f
                drawRoundRect(
                    color = if (selectedFinish == SovereignMetalFinish.SOVEREIGN_GOLD_24K) {
                        Color(0xFF1F1603).copy(alpha = 0.85f)
                    } else {
                        Color(0xFFD4AF37).copy(alpha = 0.85f)
                    },
                    topLeft = Offset(chipLeft, chipTop),
                    size = Size(chipW, chipH),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(chipLeft + 14f, chipTop + 14f),
                    size = Size(chipW - 28f, chipH - 28f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 2f)
                )
            }

            // Card Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: KHARCHEE BLACK CLUB + Glowing 👑 SOVEREIGN TIER Crest
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KHARCHEE PRIVATE RESERVE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.4.sp,
                            color = selectedFinish.secondaryTextColor
                        )
                        Text(
                            text = selectedFinish.displayName.uppercase(Locale.ENGLISH),
                            fontFamily = headingFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = selectedFinish.primaryTextColor
                        )
                    }

                    // Glowing 👑 SOVEREIGN TIER Crest
                    Surface(
                        color = if (selectedFinish == SovereignMetalFinish.SOVEREIGN_GOLD_24K) {
                            Color(0xFF141004)
                        } else {
                            selectedFinish.badgeColor.copy(alpha = 0.16f)
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (selectedFinish == SovereignMetalFinish.SOVEREIGN_GOLD_24K) {
                                Color(0xFFFFF8E1)
                            } else {
                                selectedFinish.badgeColor
                            }
                        ),
                        modifier = Modifier.testTag("sovereign_tier_crest_badge")
                    ) {
                        Text(
                            text = "👑 SOVEREIGN TIER",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.9.sp,
                            color = if (selectedFinish == SovereignMetalFinish.SOVEREIGN_GOLD_24K) {
                                Color(0xFFFFD700)
                            } else {
                                selectedFinish.badgeColor
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                // Middle Row: Member ID #KHR-[DeviceHash] & Discipline Index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 58.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Member ID #KHR-$deviceHash",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.1.sp,
                        color = selectedFinish.primaryTextColor,
                        modifier = Modifier.testTag("sovereign_member_id_text")
                    )
                }

                // Bottom Row: Cardholder Name + Discipline / Runway
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "FOUNDING MEMBER",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = selectedFinish.secondaryTextColor
                        )
                        Text(
                            text = userDisplayName.ifBlank { "KHARCHEE SOVEREIGN" }.uppercase(Locale.ENGLISH),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = selectedFinish.primaryTextColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DISCIPLINE INDEX",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = selectedFinish.secondaryTextColor
                        )
                        Text(
                            text = "$disciplineScore / 900 • ${streakDays}d",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = selectedFinish.primaryTextColor
                        )
                    }
                }
            }
        }

        // 3 Live-Switchable Metal Finishes Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sovereign_metal_finish_selector_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SovereignMetalFinish.entries.forEach { finish ->
                val isSelected = selectedFinish == finish
                val pillAccent = when (finish) {
                    SovereignMetalFinish.OBSIDIAN_TITANIUM -> Color(0xFF00E5FF)
                    SovereignMetalFinish.SOVEREIGN_GOLD_24K -> Color(0xFFFFD700)
                    SovereignMetalFinish.CYBERPUNK_EMERALD -> Color(0xFF00E676)
                }
                Surface(
                    color = if (isSelected) pillAccent.copy(alpha = 0.18f) else RichGraphite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (isSelected) 1.4.dp else 0.8.dp,
                        color = if (isSelected) pillAccent else HairlinePlatinum
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectFinish(finish) }
                        .testTag("metal_finish_pill_${finish.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = finish.displayName,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) pillAccent else WarmIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = finish.subtitle,
                            fontSize = 9.sp,
                            color = MutedPlatinum,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT 2: 4 REAL WORKING SOVEREIGN PRO POWER FEATURES (WITH LIVE TEASERS)
// ============================================================================
@Composable
fun SovereignFourPowerFeaturesShowcaseSection(
    userId: String,
    userDisplayName: String,
    isSovereignPro: Boolean,
    expenses: List<ExpenseRecord>,
    disciplineScore: Int,
    streakDays: Int,
    onRequestScrollToUnlock: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    // Feature A state: Vampire Scanner
    val leakReport = remember(expenses) {
        SovereignBlackClubEngine.scanVampireMoneyLeaks(expenses)
    }
    var drainStopperEnabled by remember {
        mutableStateOf(SovereignBlackClubEngine.isVampireDrainStopperEnabled(context))
    }
    var expandedVampirePreviewForFree by remember { mutableStateOf(false) }

    // Feature B state: Secret Ghost Vault (Decoy Hidden Ledger)
    var hasGhostPin by remember(userId) {
        mutableStateOf(SovereignBlackClubEngine.hasGhostVaultPin(context, userId))
    }
    var isGhostVaultUnlocked by remember { mutableStateOf(false) }
    var ghostPinInput by remember { mutableStateOf("") }
    var ghostEntries by remember(userId, isGhostVaultUnlocked) {
        mutableStateOf(SovereignBlackClubEngine.loadGhostVaultEntries(context, userId))
    }
    var newGhostTitle by remember { mutableStateOf("") }
    var newGhostAmount by remember { mutableStateOf("") }
    var newGhostNote by remember { mutableStateOf("") }

    // Feature C state: 24K Gold VIP WhatsApp Split Cards
    var goldVipReceiptEnabled by remember {
        mutableStateOf(KhataSplitCardGenerator.isSovereignGoldVipThemeEnabled(context))
    }

    // Feature D state: Jarvis Unfiltered Personas & Voice CFO Briefing
    var selectedPersona by remember {
        mutableStateOf(SovereignBlackClubEngine.getSelectedJarvisPersona(context))
    }
    val personaBriefingText = remember(
        selectedPersona,
        userDisplayName,
        expenses,
        disciplineScore,
        streakDays,
        leakReport.totalMonthlyLeakInr
    ) {
        SovereignBlackClubEngine.buildJarvisPersonaBriefing(
            persona = selectedPersona,
            userDisplayName = userDisplayName,
            expenses = expenses,
            disciplineScore = disciplineScore,
            streakDays = streakDays,
            monthlyLeakInr = leakReport.totalMonthlyLeakInr
        )
    }

    var isSpeakingTts by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        runCatching {
            instance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    instance?.language = Locale.forLanguageTag("en-IN")
                    ttsReady = true
                }
            }
            ttsEngine = instance
        }
        onDispose {
            runCatching {
                instance?.stop()
                instance?.shutdown()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sovereign_four_power_features_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "4 EXCLUSIVE SOVEREIGN POWER WEAPONS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = ChampagneGold
                )
                Text(
                    text = "Live Telemetry & Interactive Previews Active",
                    fontFamily = headingFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
            }
        }

        // ====================================================================
        // FEATURE A: 🧛‍♂️ SUBSCRIPTION & MONEY-LEAK VAMPIRE SCANNER
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RichGraphite)
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFFF3366), ChampagneGold)
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
                .testTag("feature_vampire_scanner_card"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FEATURE A • AUTONOMOUS DRAIN RADAR",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF3366)
                    )
                    Text(
                        text = "🧛‍♂️ Subscription & Money-Leak Vampire Scanner",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                Surface(
                    color = Color(0x26FF3366),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFFFF3366))
                ) {
                    Text(
                        text = if (isSovereignPro) "👑 UNLOCKED" else "LIVE TEASER",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF3366),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Live Banner: "Detected ₹[LeakedAmount]/mo in avoidable money leaks!"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1F0913))
                    .border(1.dp, Color(0xFFFF3366).copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("vampire_scanner_live_banner")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "🚨 ${leakReport.bannerHeadline}",
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF80AB)
                    )
                    Text(
                        text = "Annual 12-Month Compounding Drain: ${PerformancePreferences.preFormatInr(leakReport.totalYearlyDrainInr)}/yr across ${leakReport.leaks.size} detected leak vectors (OTT, Swiggy/Zomato loops & impulse spends).",
                        fontSize = 11.sp,
                        color = MutedIvory,
                        lineHeight = 15.sp
                    )
                }
            }

            if (isSovereignPro || expandedVampirePreviewForFree) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    leakReport.leaks.forEach { leak ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianBlack)
                                .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${leak.leakTypeLabel} • ${leak.occurrencesPerMonth}x/mo",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChampagneGold
                                )
                                Text(
                                    text = leak.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = WarmIvory
                                )
                                Text(
                                    text = leak.recommendation,
                                    fontSize = 11.sp,
                                    color = MutedPlatinum
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "-${PerformancePreferences.preFormatInr(leak.monthlyLeakAmount)}/mo",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF3366)
                                )
                                Text(
                                    text = "${PerformancePreferences.preFormatInr(leak.yearlyDrainAmount)}/yr",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = MutedPlatinum
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛡️ Monthly Drain Stopper Alerts",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = WarmIvory
                            )
                            Text(
                                text = "Warn before 3rd weekly Swiggy/Zomato or late-night impulse checkout",
                                fontSize = 10.sp,
                                color = MutedPlatinum
                            )
                        }
                        Switch(
                            checked = drainStopperEnabled,
                            onCheckedChange = { enabled ->
                                drainStopperEnabled = enabled
                                SovereignBlackClubEngine.setVampireDrainStopperEnabled(context, enabled)
                                onShowToast(
                                    if (enabled) "🧛‍♂️ Monthly Drain Stopper Armed!"
                                    else "Monthly Drain Stopper Paused"
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = SophisticatedEmerald
                            )
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { expandedVampirePreviewForFree = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF3366)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vampire_scanner_preview_breakdown_button")
                    ) {
                        Text(
                            text = "👁️ Peek Leak Breakdown",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF80AB)
                        )
                    }
                    Button(
                        onClick = onRequestScrollToUnlock,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "👑 Stop Leaks (₹1/day)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // ====================================================================
        // FEATURE B: 🕵️‍♂️ SECRET GHOST VAULT (DECOY HIDDEN LEDGER)
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RichGraphite)
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFFA855F7))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
                .testTag("feature_secret_ghost_vault_card"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FEATURE B • ZERO MAIN-SCREEN FOOTPRINT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "🕵️‍♂️ Secret Ghost Vault (Decoy Hidden Ledger)",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                Surface(
                    color = Color(0x2600E5FF),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFF00E5FF))
                ) {
                    Text(
                        text = if (isGhostVaultUnlocked) "🔓 VAULT OPEN" else "🔒 4-DIGIT GHOST PIN",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Hide sensitive personal transactions, surprise gifts, or private stashes behind a separate 4-digit Ghost PIN. Entries here NEVER appear on your main Home or Khata screens.",
                fontSize = 11.sp,
                color = MutedIvory,
                lineHeight = 16.sp
            )

            if (!isGhostVaultUnlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (hasGhostPin) {
                            "Enter your 4-Digit Ghost PIN to decrypt private ledger:"
                        } else {
                            "Create a 4-Digit Ghost PIN to initialize your hidden ledger:"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ghostPinInput,
                            onValueChange = { input ->
                                ghostPinInput = input.filter { it.isDigit() }.take(4)
                            },
                            label = {
                                Text(
                                    if (hasGhostPin) "4-Digit Ghost PIN" else "Set 4-Digit Ghost PIN",
                                    fontSize = 11.sp
                                )
                            },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = HairlinePlatinum,
                                focusedContainerColor = RichGraphite,
                                unfocusedContainerColor = RichGraphite,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ghost_vault_pin_input")
                        )

                        Button(
                            onClick = {
                                if (ghostPinInput.length != 4) {
                                    onShowToast("Enter a 4-digit numeric Ghost PIN")
                                    return@Button
                                }
                                if (!hasGhostPin) {
                                    SovereignBlackClubEngine.setGhostVaultPin(context, userId, ghostPinInput)
                                    hasGhostPin = true
                                    isGhostVaultUnlocked = true
                                    ghostPinInput = ""
                                    onShowToast("🕵️‍♂️ Ghost Vault Initialized & Unlocked!")
                                } else {
                                    if (SovereignBlackClubEngine.verifyGhostVaultPin(context, userId, ghostPinInput)) {
                                        isGhostVaultUnlocked = true
                                        ghostPinInput = ""
                                        onShowToast("🔓 Secret Ghost Vault Decrypted")
                                    } else {
                                        onShowToast("❌ Invalid Ghost PIN")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("ghost_vault_unlock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Unlock Ghost Vault",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasGhostPin) "Unlock" else "Set PIN",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                // Unlocked Secret Ghost Vault Mini-Ledger
                val totalSecretOutflow = ghostEntries.filter { !it.isIncome }.sumOf { it.amount }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBlack)
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("ghost_vault_unlocked_ledger"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CLASSIFIED GHOST LEDGER (${ghostEntries.size} HIDDEN)",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = "Hidden Total: ${PerformancePreferences.preFormatInr(totalSecretOutflow)}",
                                fontFamily = headingFont,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmIvory
                            )
                        }
                        OutlinedButton(
                            onClick = { isGhostVaultUnlocked = false },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, Color(0xFFFF3366)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Lock Vault",
                                tint = Color(0xFFFF3366),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lock Now",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3366)
                            )
                        }
                    }

                    // Add new hidden transaction row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newGhostTitle,
                            onValueChange = { newGhostTitle = it },
                            label = { Text("Secret Item", fontSize = 10.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = HairlinePlatinum,
                                focusedContainerColor = RichGraphite,
                                unfocusedContainerColor = RichGraphite,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("ghost_vault_title_input")
                        )
                        OutlinedTextField(
                            value = newGhostAmount,
                            onValueChange = { newGhostAmount = it.filter { c -> c.isDigit() }.take(8) },
                            label = { Text("₹ Amt", fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = HairlinePlatinum,
                                focusedContainerColor = RichGraphite,
                                unfocusedContainerColor = RichGraphite,
                                focusedTextColor = WarmIvory,
                                unfocusedTextColor = WarmIvory
                            ),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("ghost_vault_amount_input")
                        )
                        Button(
                            onClick = {
                                val amt = newGhostAmount.toLongOrNull() ?: 0L
                                if (newGhostTitle.isBlank() || amt <= 0L) {
                                    onShowToast("Enter secret item title and ₹ amount")
                                    return@Button
                                }
                                ghostEntries = SovereignBlackClubEngine.addGhostVaultEntry(
                                    context = context,
                                    userId = userId,
                                    title = newGhostTitle,
                                    amount = amt,
                                    secretNote = newGhostNote
                                )
                                newGhostTitle = ""
                                newGhostAmount = ""
                                newGhostNote = ""
                                onShowToast("🕵️‍♂️ Saved to Secret Ghost Vault (Invisible on Home/Khata)")
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = ObsidianBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("ghost_vault_add_entry_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Secret Entry", modifier = Modifier.size(18.dp))
                        }
                    }

                    if (ghostEntries.isEmpty()) {
                        Text(
                            text = "No hidden entries yet. Add a private entry above — it stays 100% off your main Home & Khata ledgers.",
                            fontSize = 11.sp,
                            color = MutedPlatinum
                        )
                    } else {
                        ghostEntries.take(5).forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RichGraphite)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = WarmIvory
                                    )
                                    Text(
                                        text = "🕵️‍♂️ Ghost Protected • Invisible on Home",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                                Text(
                                    text = PerformancePreferences.preFormatInr(entry.amount),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = ChampagneGold
                                )
                                IconButton(
                                    onClick = {
                                        ghostEntries = SovereignBlackClubEngine.deleteGhostVaultEntry(
                                            context = context,
                                            userId = userId,
                                            entryId = entry.id
                                        )
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Secret Entry",
                                        tint = MutedPlatinum,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // FEATURE C: 👑 24K GOLD VIP WHATSAPP SPLIT CARDS
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RichGraphite)
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
                .testTag("feature_gold_vip_whatsapp_card"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FEATURE C • SOCIAL STATUS FLEX ON WHATSAPP",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "👑 24K Gold VIP WhatsApp Split Cards",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                Switch(
                    checked = goldVipReceiptEnabled,
                    onCheckedChange = { enabled ->
                        goldVipReceiptEnabled = enabled
                        KhataSplitCardGenerator.setSovereignGoldVipThemeEnabled(context, enabled)
                        onShowToast(
                            if (enabled) "👑 24K Gold VIP Receipt Theme Enabled!"
                            else "Standard Dark Split Receipt Theme Selected"
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = Color(0xFFFFD700)
                    ),
                    modifier = Modifier.testTag("toggle_gold_vip_receipt_theme_switch")
                )
            }

            // Live Visual Preview of the 24K Gold VIP Split Card Watermark
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF141008), Color(0xFF241B08), Color(0xFF120E05))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFFE082), Color(0xFFD4AF37), Color(0xFFFFB300))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👑 24K GOLD VIP SPLIT RECEIPT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "LENA HAI • ₹1,250",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFF8E1)
                    )
                }
                Text(
                    text = "Adds a brushed 24K Gold metallic border, golden QR frame, and '👑 Verified Kharchee Sovereign' watermark to every WhatsApp Vasooli & Split Receipt card.",
                    fontSize = 11.sp,
                    color = Color(0xFFFFE082),
                    lineHeight = 15.sp
                )
                Text(
                    text = "👑 Verified Kharchee Sovereign  •  24K Gold VIP Ledger",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
            }

            OutlinedButton(
                onClick = {
                    val sampleContact = KhataContactComputedState(
                        contact = KhataContact(
                            id = "vip_preview",
                            userId = userId,
                            name = "Rohan (VIP Split)",
                            phone = ""
                        ),
                        totalOriginalLenaHai = 1250L,
                        totalPaymentsReceived = 0L,
                        totalOriginalDenaHai = 0L,
                        totalPaymentsMade = 0L,
                        netSignedBalance = 1250L,
                        netDirection = KhataDirection.LENA_HAI,
                        outstandingAmount = 1250L,
                        isSettled = false,
                        hasHistory = true,
                        lastTransactionMillis = System.currentTimeMillis(),
                        earliestUnsettledDueDateMillis = null,
                        isOverdue = false,
                        isUpcomingDue = false,
                        overdueAmount = 0L,
                        activeEntries = emptyList(),
                        allEntriesIncludingDeleted = emptyList()
                    )
                    KhataSplitCardGenerator.shareSplitReceiptCardOnWhatsApp(
                        context = context,
                        contactState = sampleContact,
                        userDisplayName = userDisplayName.ifBlank { "Sovereign Member" },
                        specificEntryDescription = "Weekend Dinner & Club Split",
                        specificEntryAmount = 1250L,
                        forceSovereignGoldVip = true,
                        onStatusToast = onShowToast
                    )
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_gold_vip_split_card_preview_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Test 24K Gold VIP Card on WhatsApp",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📲 Share 24K Gold VIP Split Receipt Preview",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
            }
        }

        // ====================================================================
        // FEATURE D: 🦈 JARVIS UNFILTERED PERSONAS & VOICE CFO BRIEFING
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(RichGraphite)
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(selectedPersona.accentColor, ChampagneGold)
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
                .testTag("feature_jarvis_unfiltered_personas_card"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FEATURE D • AI CFO VOICE & PERSONALITY ENGINE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = selectedPersona.accentColor
                    )
                    Text(
                        text = "🦈 Jarvis Unfiltered Personas & Voice CFO Briefing",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
            }

            // 3 Selectable AI Persona Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                JarvisUnfilteredPersona.entries.forEach { persona ->
                    val active = selectedPersona == persona
                    Surface(
                        color = if (active) persona.accentColor.copy(alpha = 0.2f) else ObsidianBlack,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = if (active) 1.3.dp else 0.8.dp,
                            color = if (active) persona.accentColor else HairlinePlatinum
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedPersona = persona
                                SovereignBlackClubEngine.setSelectedJarvisPersona(context, persona)
                                onShowToast("🎙️ Jarvis Persona switched to ${persona.badgeTitle}")
                            }
                            .testTag("jarvis_persona_pill_${persona.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = persona.badgeTitle,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (active) persona.accentColor else WarmIvory,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Live Persona Financial Briefing Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.9.dp, selectedPersona.accentColor.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("jarvis_persona_live_briefing_box"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${selectedPersona.badgeTitle.uppercase(Locale.ENGLISH)} • LIVE BRIEFING",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = selectedPersona.accentColor
                )
                Text(
                    text = "\"$personaBriefingText\"",
                    fontSize = 12.sp,
                    color = WarmIvory,
                    lineHeight = 17.sp
                )
            }

            // 1-Tap Spoken TTS Audio Financial Briefing Button
            Button(
                onClick = {
                    val engine = ttsEngine
                    if (isSpeakingTts) {
                        runCatching { engine?.stop() }
                        isSpeakingTts = false
                        onShowToast("🔇 Stopped Jarvis Voice Briefing")
                    } else {
                        isSpeakingTts = true
                        if (engine != null && ttsReady) {
                            runCatching {
                                engine.speak(
                                    personaBriefingText,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "jarvis_cfo_briefing_${System.currentTimeMillis()}"
                                )
                            }
                            onShowToast("🎙️ Playing ${selectedPersona.badgeTitle} Spoken Audio Briefing...")
                        } else {
                            onShowToast("🎙️ ${selectedPersona.badgeTitle}: $personaBriefingText")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedPersona.accentColor,
                    contentColor = ObsidianBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("jarvis_play_voice_briefing_button")
            ) {
                Icon(
                    imageVector = if (isSpeakingTts) Icons.Default.StopCircle else Icons.Default.RecordVoiceOver,
                    contentDescription = "Spoken TTS Audio Financial Briefing",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpeakingTts) {
                        "⏹ Stop Spoken CFO Audio Briefing"
                    } else {
                        "🔊 Play 1-Tap Spoken TTS Audio Financial Briefing"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ============================================================================
// COMPONENT 3: PSYCHOLOGICAL VALUE COMPARISON & "₹1/DAY" CONVERSION ENGINE
// ============================================================================
private data class ComparisonMatrixRow(
    val featureTitle: String,
    val standardFreeValue: String,
    val sovereignProValue: String,
    val isExclusiveToPro: Boolean
)

private val SOVEREIGN_COMPARISON_MATRIX = listOf(
    ComparisonMatrixRow(
        featureTitle = "Unlimited Expenses, Khata & 1-Tap UPI",
        standardFreeValue = "✓ Free Forever",
        sovereignProValue = "✓ Free Forever",
        isExclusiveToPro = false
    ),
    ComparisonMatrixRow(
        featureTitle = "SMS Auto-Detect, Cloud Backup & App-Lock",
        standardFreeValue = "✓ Free Forever",
        sovereignProValue = "✓ Free Forever",
        isExclusiveToPro = false
    ),
    ComparisonMatrixRow(
        featureTitle = "3D Shimmering Metal Card Themes (24K Gold)",
        standardFreeValue = "Preview Only",
        sovereignProValue = "👑 Full Unlock",
        isExclusiveToPro = true
    ),
    ComparisonMatrixRow(
        featureTitle = "🧛‍♂️ Vampire Leak Breakdown & Drain Stopper",
        standardFreeValue = "Banner Teaser",
        sovereignProValue = "👑 Full Radar",
        isExclusiveToPro = true
    ),
    ComparisonMatrixRow(
        featureTitle = "🕵️‍♂️ Secret Ghost Vault (Decoy PIN Ledger)",
        standardFreeValue = "—",
        sovereignProValue = "👑 Included",
        isExclusiveToPro = true
    ),
    ComparisonMatrixRow(
        featureTitle = "👑 24K Gold VIP WhatsApp Split Receipts",
        standardFreeValue = "Standard Dark",
        sovereignProValue = "👑 24K Gold VIP",
        isExclusiveToPro = true
    ),
    ComparisonMatrixRow(
        featureTitle = "🦈 Jarvis Unfiltered Personas + Voice CFO",
        standardFreeValue = "Standard Chat",
        sovereignProValue = "👑 3 Personas + TTS",
        isExclusiveToPro = true
    )
)

@Composable
fun SovereignValueComparisonAndConversionSection(
    selectedPlanId: String,
    isProActive: Boolean,
    activeProTier: String?,
    onSelectPlan: (String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(RichGraphite)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(ChampagneGold, Color(0xFF00E676), ChampagneGold)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
            .testTag("sovereign_comparison_and_conversion_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "PSYCHOLOGICAL VALUE COMPARISON • ₹1/DAY ROI ENGINE",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                color = ChampagneGold
            )
            Text(
                text = "Standard Free vs 👑 Sovereign Pro",
                fontFamily = headingFont,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WarmIvory
            )
        }

        // Side-by-Side Comparison Matrix
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianBlack)
                .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                .padding(12.dp)
                .testTag("standard_vs_sovereign_comparison_matrix"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAPABILITY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MutedPlatinum,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "STANDARD FREE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SophisticatedEmerald,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = "👑 SOVEREIGN PRO",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ChampagneGold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            SOVEREIGN_COMPARISON_MATRIX.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (row.isExclusiveToPro) Color(0xFF15120A) else RichGraphite)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.featureTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmIvory,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = row.standardFreeValue,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = if (row.isExclusiveToPro) MutedPlatinum else SophisticatedEmerald,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.9f)
                    )
                    Text(
                        text = row.sovereignProValue,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (row.isExclusiveToPro) ChampagneGold else SophisticatedEmerald,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // High-Conversion Anchor Pricing Cards
        // Plan 1: ⚡ ₹30 / Month Starter Pass
        val isStarterSelected = selectedPlanId == "starter_monthly"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isStarterSelected) ElevatedGraphite else ObsidianBlack)
                .border(
                    width = if (isStarterSelected) 1.6.dp else 0.9.dp,
                    color = if (isStarterSelected) Color(0xFF00E5FF) else HairlinePlatinum,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { onSelectPlan("starter_monthly") }
                .padding(16.dp)
                .testTag("pro_tier_card_starter_monthly")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0x2600E5FF),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, Color(0xFF00E5FF))
                    ) {
                        Text(
                            text = if (isProActive && activeProTier == "gold") "✓ ACTIVE PLAN" else "⚡ JUST ₹1/DAY • 2 CHAIS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "₹30 / Mo",
                        fontFamily = headingFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                Text(
                    text = "⚡ ₹30 / Month Starter Pass — Just ₹1/day (Price of 2 Chais) to save ₹2,000+ every month.",
                    fontFamily = headingFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
                Text(
                    text = "30-Day Full Sovereign Access • Zero Auto-Debit • Cancel or Upgrade Anytime",
                    fontSize = 11.sp,
                    color = MutedPlatinum
                )
            }
        }

        // Plan 2 (BEST VALUE BADGE): 👑 ₹299 Lifetime Sovereign
        val isLifetimeSelected = selectedPlanId == "sovereign_lifetime"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isLifetimeSelected) {
                        Brush.linearGradient(listOf(Color(0xFF211907), Color(0xFF141005)))
                    } else {
                        Brush.linearGradient(listOf(ObsidianBlack, ObsidianBlack))
                    }
                )
                .border(
                    width = if (isLifetimeSelected) 2.dp else 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFF00E676))
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { onSelectPlan("sovereign_lifetime") }
                .padding(16.dp)
                .testTag("pro_tier_card_sovereign_lifetime")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0x33FFD700),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = if (isProActive && activeProTier == "platinum") {
                                "✓ LIFETIME SOVEREIGN UNLOCKED"
                            } else {
                                "👑 BEST VALUE • PAY ONCE, OWN FOREVER"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "₹999",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = MutedPlatinum,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = "₹299",
                            fontFamily = headingFont,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
                Text(
                    text = "👑 ₹299 Lifetime Sovereign — Pay Once, Own Forever (No monthly tensions).",
                    fontFamily = headingFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
                Text(
                    text = "Permanent Founding Member Status • All 4 Power Weapons + Future Updates Included Forever",
                    fontSize = 11.sp,
                    color = Color(0xFFFFE082)
                )
            }
        }
    }
}

// ============================================================================
// COMPONENT 4: STREAMLINED 3-STEP INSTANT UPI + WHATSAPP KEY UNLOCK BAR
//              + CELEBRATORY GOLD CONFETTI BURST
// ============================================================================
@Composable
fun StreamlinedThreeStepUpiWhatsappUnlockBar(
    deviceHash: String,
    selectedPlanTitle: String,
    selectedPlanAmountInr: Int,
    isProActive: Boolean,
    utrInputText: String,
    activationCodeInput: String,
    onActivationCodeChange: (String) -> Unit,
    activationStatusMessage: String?,
    activationIsError: Boolean,
    onStep1PayViaUpi: () -> Unit,
    onStep2GetKeyOnWhatsApp: () -> Unit,
    onStep3VerifyAndUnlock: () -> Unit,
    onCopyDeviceHash: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var showGoldConfettiBurst by remember { mutableStateOf(false) }

    LaunchedEffect(isProActive, activationStatusMessage) {
        if (activationStatusMessage?.startsWith("✅") == true) {
            showGoldConfettiBurst = true
            delay(4200L)
            showGoldConfettiBurst = false
        }
    }

    val confettiTransition = rememberInfiniteTransition(label = "goldConfettiTransition")
    val confettiPhase by confettiTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "goldConfettiPhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF141008), Color(0xFF0A0D14))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFF00E676), Color(0xFF00E5FF))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
            .testTag("sovereign_offline_activation_box")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔐 3-STEP INSTANT UPI + WHATSAPP KEY UNLOCK",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "Member ID #KHR-$deviceHash",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory,
                        modifier = Modifier.testTag("sovereign_device_hash_text")
                    )
                }

                Surface(
                    color = ElevatedGraphite,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.9.dp, ChampagneGold),
                    modifier = Modifier
                        .clickable { onCopyDeviceHash() }
                        .testTag("copy_device_hash_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Device ID",
                            tint = ChampagneGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Copy ID",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ChampagneGold
                        )
                    }
                }
            }

            // STEP 1: [⚡ Pay ₹30 or ₹299 via Any UPI App]
            Button(
                onClick = onStep1PayViaUpi,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF0A0802)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("activate_pro_razorpay_upi_button")
            ) {
                Text(
                    text = "⚡ Step 1: Pay ₹$selectedPlanAmountInr ($selectedPlanTitle) via Any UPI App",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // STEP 2: [💬 Get Instant Key on WhatsApp (Auto-sends Device ID)]
            OutlinedButton(
                onClick = onStep2GetKeyOnWhatsApp,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, Color(0xFF00E676)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("get_activation_key_whatsapp_button")
            ) {
                Text(
                    text = "💬 Step 2: Get Instant Key on WhatsApp (Auto-sends #KHR-$deviceHash)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    color = Color(0xFF00E676),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // STEP 3: Sleek Glowing Input Box to Paste the Key -> Celebratory Gold Confetti Burst
            OutlinedTextField(
                value = activationCodeInput,
                onValueChange = { onActivationCodeChange(it.uppercase(Locale.US)) },
                label = {
                    Text(
                        text = "Step 3: Paste Sovereign Key (e.g. KHR-SOV-XXXX-XXXX)",
                        fontSize = 11.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = ChampagneGold.copy(alpha = 0.6f),
                    focusedContainerColor = ObsidianBlack,
                    unfocusedContainerColor = ObsidianBlack,
                    focusedTextColor = WarmIvory,
                    unfocusedTextColor = WarmIvory,
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = MutedPlatinum
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sovereign_activation_code_input")
            )

            Button(
                onClick = {
                    showGoldConfettiBurst = true
                    onStep3VerifyAndUnlock()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676),
                    contentColor = Color(0xFF04140B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("verify_sovereign_activation_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verify Sovereign Key",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isProActive) {
                        "👑 SOVEREIGN BLACK CLUB UNLOCKED & VERIFIED"
                    } else {
                        "👑 Verify Key & Unlock Sovereign Black Club"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }

            if (activationStatusMessage != null) {
                Text(
                    text = activationStatusMessage,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (activationIsError) CrimsonRuby else Color(0xFF00E676),
                    modifier = Modifier.testTag("sovereign_activation_status_text")
                )
            }
        }

        // Celebratory Gold Confetti Burst Canvas Overlay when unlocked!
        if (showGoldConfettiBurst && !activationIsError && (isProActive || activationStatusMessage?.startsWith("✅") == true)) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .testTag("sovereign_gold_confetti_canvas")
            ) {
                val particleColors = listOf(
                    Color(0xFFFFD700),
                    Color(0xFF00E676),
                    Color(0xFF00E5FF),
                    Color(0xFFFFE082),
                    Color(0xFFFF3366)
                )
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = max(size.width, size.height) * 0.65f
                for (i in 0 until 36) {
                    val angleRad = Math.toRadians((i * 10.0) + (confettiPhase * 45.0))
                    val dist = (0.15f + confettiPhase * 0.85f) * maxRadius * ((i % 3 + 1) / 3f)
                    val px = center.x + (cos(angleRad) * dist).toFloat()
                    val py = center.y + (sin(angleRad) * dist).toFloat()
                    drawCircle(
                        color = particleColors[i % particleColors.size].copy(alpha = (1f - confettiPhase * 0.7f).coerceIn(0.2f, 1f)),
                        radius = (6f + (i % 4) * 2.5f),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
