package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExpenseRecord
import com.example.data.model.QuickAddPreset
import com.example.data.model.isIncomeTransaction
import com.example.data.model.isSameMonth
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.util.PerformancePreferences
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import org.json.JSONArray
import org.json.JSONObject

enum class SafeBurnStatusTier {
    EMERALD_SAFE,
    AMBER_WARNING,
    CRIMSON_EXCEEDED
}

data class DailySafeBurnTelemetry(
    val monthlyBudgetTarget: Long,
    val monthSpentSoFar: Long,
    val remainingMonthlyBudget: Long,
    val daysLeftInMonth: Int,
    val dailySafeAllowance: Long,
    val todaySpent: Long,
    val todaySafeSpendLeft: Long,
    val burnRatioToday: Float,
    val statusTier: SafeBurnStatusTier,
    val pillText: String
)

data class QuickSpendUndoEvent(
    val timestampMillis: Long,
    val emoji: String,
    val label: String,
    val category: String,
    val amount: Long
)

data class HomeDonutSlice(
    val categoryKey: String,
    val displayName: String,
    val emoji: String,
    val colorHex: Long,
    val color: Color,
    val amountInr: Long,
    val percentage: Float,
    val startAngleDeg: Float,
    val sweepAngleDeg: Float
)

data class OxygenBurnTankTelemetry(
    val monthlyBudgetTarget: Long,
    val monthSpent: Long,
    val remainingMonthlyBudget: Long,
    val oxygenPercentLeft: Int,
    val oxygenFractionLeft: Float,
    val expiresInDays: Int,
    val statusTier: SafeBurnStatusTier,
    val titleText: String
)

data class HallOfShameDefaulterItem(
    val contactId: String,
    val friendName: String,
    val friendPhone: String,
    val overdueAmountInr: Long,
    val daysLate: Int,
    val itemSummary: String,
    val contactState: com.example.data.model.KhataContactComputedState
)

data class MidnightQuarantineEntry(
    val id: String,
    val title: String,
    val category: String,
    val amountInr: Long,
    val flaggedAtEpochMillis: Long
)

object HomeDailyCockpitEngine {
    private const val PREFS_NAME = "kharchee_daily_cockpit_prefs"
    private const val KEY_CUSTOM_CHIPS_PREFIX = "custom_quick_chips_"
    private const val KEY_MIDNIGHT_SHIELD_ENABLED_PREFIX = "midnight_munchies_shield_enabled_"
    private const val KEY_MIDNIGHT_QUARANTINE_QUEUE_PREFIX = "midnight_munchies_quarantine_queue_"

    const val NEON_FOOD_HEX: Long = 0xFF00E676
    const val NEON_SHOPPING_HEX: Long = 0xFFFF3366
    const val NEON_TRAVEL_HEX: Long = 0xFF00E5FF
    const val NEON_BILLS_HEX: Long = 0xFFB388FF
    const val NEON_OTHER_HEX: Long = 0xFFFFD600

    val DEFAULT_COCKPIT_QUICK_CHIPS: List<QuickAddPreset> = listOf(
        QuickAddPreset("chai_20", "☕", "Chai", 20L, "Chai & Snacks"),
        QuickAddPreset("metro_40", "🚇", "Metro", 40L, "Transport"),
        QuickAddPreset("auto_50", "🛵", "Auto", 50L, "Transport"),
        QuickAddPreset("snacks_120", "🍔", "Snacks", 120L, "Food")
    )

    val LAUNCHPAD_BUDGET_PRESETS: List<Pair<Long, String>> = listOf(
        5_000L to "₹5,000",
        10_000L to "₹10,000",
        15_000L to "₹15,000",
        25_000L to "₹25,000"
    )

    fun mapExpenseToNeonBucket(rawCategory: String): String {
        val norm = rawCategory.trim().lowercase(Locale.ENGLISH)
        return when {
            norm.contains("food") || norm.contains("chai") || norm.contains("snack") ||
                norm.contains("grocer") || norm.contains("dining") || norm.contains("restaurant") ||
                norm.contains("swiggy") || norm.contains("zomato") || norm.contains("coffee") -> "Food"
            norm.contains("shop") || norm.contains("cloth") || norm.contains("fashion") ||
                norm.contains("amazon") || norm.contains("flipkart") || norm.contains("electronics") -> "Shopping"
            norm.contains("travel") || norm.contains("transport") || norm.contains("metro") ||
                norm.contains("auto") || norm.contains("cab") || norm.contains("uber") ||
                norm.contains("ola") || norm.contains("fuel") || norm.contains("petrol") ||
                norm.contains("flight") || norm.contains("train") -> "Travel"
            norm.contains("bill") || norm.contains("rent") || norm.contains("utilit") ||
                norm.contains("recharge") || norm.contains("wifi") || norm.contains("electric") ||
                norm.contains("emi") || norm.contains("subscription") -> "Bills"
            else -> "Other"
        }
    }

    fun computeHomeDonutSlices(
        expenses: List<ExpenseRecord>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<HomeDonutSlice> {
        val bucketOrder = listOf(
            Triple("Food", "🍔", NEON_FOOD_HEX),
            Triple("Shopping", "🛍️", NEON_SHOPPING_HEX),
            Triple("Travel", "🚇", NEON_TRAVEL_HEX),
            Triple("Bills", "⚡", NEON_BILLS_HEX),
            Triple("Other", "✨", NEON_OTHER_HEX)
        )

        val nonIncome = expenses.filter { !isIncomeTransaction(it) && it.amount > 0L }
        val currentMonthTx = nonIncome.filter { isSameMonth(it.txEpochMillis, nowMillis) }
        val activeTx = if (currentMonthTx.isNotEmpty()) currentMonthTx else nonIncome

        val sums = mutableMapOf(
            "Food" to 0L,
            "Shopping" to 0L,
            "Travel" to 0L,
            "Bills" to 0L,
            "Other" to 0L
        )
        for (tx in activeTx) {
            val bucket = mapExpenseToNeonBucket(tx.category)
            sums[bucket] = (sums[bucket] ?: 0L) + tx.amount
        }

        val totalSpend = sums.values.sum()
        if (totalSpend <= 0L) {
            // Return 5 zero-spend slices with equal visual preview angles so legend + tap still work cleanly
            val sweepEach = 360f / bucketOrder.size
            return bucketOrder.mapIndexed { idx, (name, emoji, hex) ->
                HomeDonutSlice(
                    categoryKey = name,
                    displayName = name,
                    emoji = emoji,
                    colorHex = hex,
                    color = Color(hex),
                    amountInr = 0L,
                    percentage = 0f,
                    startAngleDeg = -90f + (idx * sweepEach),
                    sweepAngleDeg = sweepEach
                )
            }
        }

        val nonZeroBuckets = bucketOrder.filter { (sums[it.first] ?: 0L) > 0L }
        var runningAngle = -90f
        val result = mutableListOf<HomeDonutSlice>()

        for ((name, emoji, hex) in nonZeroBuckets) {
            val amt = sums[name] ?: 0L
            val ratio = amt.toFloat() / totalSpend.toFloat()
            val sweep = (ratio * 360f).coerceAtLeast(6f)
            result.add(
                HomeDonutSlice(
                    categoryKey = name,
                    displayName = name,
                    emoji = emoji,
                    colorHex = hex,
                    color = Color(hex),
                    amountInr = amt,
                    percentage = ratio * 100f,
                    startAngleDeg = runningAngle,
                    sweepAngleDeg = sweep
                )
            )
            runningAngle += sweep
        }

        // Normalize total sweep to 360 degrees if minimum sweep clamped any tiny slice
        val totalSweep = result.sumOf { it.sweepAngleDeg.toDouble() }.toFloat()
        if (result.isNotEmpty() && totalSweep > 0f && abs(totalSweep - 360f) > 0.01f) {
            val scale = 360f / totalSweep
            var normStart = -90f
            return result.map { slice ->
                val normSweep = slice.sweepAngleDeg * scale
                val updated = slice.copy(
                    startAngleDeg = normStart,
                    sweepAngleDeg = normSweep
                )
                normStart += normSweep
                updated
            }
        }

        return result
    }

    /**
     * Hit-tests a tap coordinate `(tapX, tapY)` against the donut ring geometry.
     * Returns the tapped `HomeDonutSlice`, or `null` if the tap landed inside the center hole or outside the ring.
     */
    fun hitTestDonutSlice(
        slices: List<HomeDonutSlice>,
        tapX: Float,
        tapY: Float,
        centerX: Float,
        centerY: Float,
        innerRadiusPx: Float,
        outerRadiusPx: Float
    ): HomeDonutSlice? {
        if (slices.isEmpty()) return null
        val dx = tapX - centerX
        val dy = tapY - centerY
        val dist = hypot(dx, dy)
        if (dist < innerRadiusPx || dist > outerRadiusPx) {
            return null
        }

        // atan2 returns angle in degrees [-180, 180] where 0 is 3 o'clock, -90 is 12 o'clock.
        // Normalize to [-90, 270) to match startAngleDeg starting at -90f.
        var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        if (deg < -90f) {
            deg += 360f
        }

        for (slice in slices) {
            val endAngle = slice.startAngleDeg + slice.sweepAngleDeg
            if (deg >= slice.startAngleDeg && deg <= endAngle) {
                return slice
            }
        }
        return slices.lastOrNull()
    }

    fun computeDaysLeftInMonth(nowMillis: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return max(1, maxDay - currentDay + 1)
    }

    /**
     * Computes Remaining Monthly Budget, Daily Safe Burn Limit (RemainingBudget / DaysLeftInMonth),
     * Today's remaining safe spend, and status tier (Emerald <80%, Amber 80..100%, Crimson >100%).
     */
    fun computeDailySafeBurnTelemetry(
        monthlyBudgetTarget: Long,
        monthSpent: Long,
        todaySpent: Long,
        fallbackDailyLimit: Long = 500L,
        nowMillis: Long = System.currentTimeMillis()
    ): DailySafeBurnTelemetry {
        val daysLeft = computeDaysLeftInMonth(nowMillis)
        val effectiveMonthlyBudget = if (monthlyBudgetTarget > 0L) {
            monthlyBudgetTarget
        } else {
            max(1500L, fallbackDailyLimit * 30L)
        }
        // Prior days' spend in this month (excluding today's spend so today's safe allowance stays stable through the day)
        val spentBeforeToday = (monthSpent - todaySpent).coerceAtLeast(0L)
        val remainingMonthlyBeforeToday = (effectiveMonthlyBudget - spentBeforeToday).coerceAtLeast(0L)
        val remainingMonthlyNow = (effectiveMonthlyBudget - monthSpent).coerceAtLeast(0L)

        val dailySafeAllowance = if (remainingMonthlyBeforeToday > 0L) {
            max(1L, remainingMonthlyBeforeToday / daysLeft.toLong())
        } else {
            0L
        }

        val todaySafeSpendLeft = (dailySafeAllowance - todaySpent).coerceAtLeast(0L)
        val burnRatio = if (dailySafeAllowance > 0L) {
            todaySpent.toFloat() / dailySafeAllowance.toFloat()
        } else {
            if (todaySpent > 0L) 1.5f else 1.0f
        }

        val statusTier = when {
            dailySafeAllowance <= 0L || todaySpent > dailySafeAllowance -> SafeBurnStatusTier.CRIMSON_EXCEEDED
            burnRatio >= 0.80f -> SafeBurnStatusTier.AMBER_WARNING
            else -> SafeBurnStatusTier.EMERALD_SAFE
        }

        val formattedSafeLeft = PerformancePreferences.preFormatInr(todaySafeSpendLeft)
        val pillText = if (statusTier == SafeBurnStatusTier.CRIMSON_EXCEEDED && todaySafeSpendLeft == 0L) {
            val overBy = (todaySpent - dailySafeAllowance).coerceAtLeast(0L)
            if (overBy > 0L) {
                "🎯 Today's Safe Spend: ₹0 left (Over by ${PerformancePreferences.preFormatInr(overBy)})"
            } else {
                "🎯 Today's Safe Spend: ₹0 left"
            }
        } else {
            "🎯 Today's Safe Spend: $formattedSafeLeft left"
        }

        return DailySafeBurnTelemetry(
            monthlyBudgetTarget = effectiveMonthlyBudget,
            monthSpentSoFar = monthSpent,
            remainingMonthlyBudget = remainingMonthlyNow,
            daysLeftInMonth = daysLeft,
            dailySafeAllowance = dailySafeAllowance,
            todaySpent = todaySpent,
            todaySafeSpendLeft = todaySafeSpendLeft,
            burnRatioToday = burnRatio,
            statusTier = statusTier,
            pillText = pillText
        )
    }

    /**
     * Formats transaction amount with exact 2-decimal paise when non-zero paise exist
     * (e.g. -₹349.50), or crisp integer INR formatting when whole rupees (e.g. -₹20).
     */
    fun formatExactTransactionAmount(
        amountRupees: Long,
        exactAmount: Double = 0.0,
        amountPaise: Long = 0L,
        isIncome: Boolean = false
    ): String {
        val sign = if (isIncome) "+" else "-"
        val resolvedPaise = when {
            amountPaise > 0L -> amountPaise
            exactAmount > 0.0 -> kotlin.math.round(exactAmount * 100.0).toLong()
            else -> abs(amountRupees) * 100L
        }
        val wholeRupees = resolvedPaise / 100L
        val remPaise = abs(resolvedPaise % 100L)
        val baseInr = PerformancePreferences.preFormatInr(wholeRupees)
        return if (remPaise != 0L) {
            String.format(Locale.ENGLISH, "%s%s.%02d", sign, baseInr, remPaise)
        } else {
            "$sign$baseInr"
        }
    }

    fun getCustomQuickChips(context: Context, userId: String = "default"): List<QuickAddPreset> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("$KEY_CUSTOM_CHIPS_PREFIX$userId", null) ?: return emptyList()
        return parseCustomQuickChipsJson(raw)
    }

    fun saveCustomQuickChip(
        context: Context,
        userId: String = "default",
        emoji: String,
        label: String,
        amount: Long,
        category: String
    ): List<QuickAddPreset> {
        val cleanLabel = label.trim().ifBlank { "Custom" }
        val cleanEmoji = emoji.trim().ifBlank { "⚡" }
        val cleanAmount = amount.coerceAtLeast(1L)
        val existing = getCustomQuickChips(context, userId).toMutableList()
        val id = "custom_${cleanLabel.lowercase(Locale.ENGLISH).replace(Regex("[^a-z0-9]"), "_")}_$cleanAmount"
        existing.removeAll { it.id == id }
        existing.add(
            QuickAddPreset(
                id = id,
                emoji = cleanEmoji,
                label = cleanLabel,
                amount = cleanAmount,
                category = category.ifBlank { "Food" }
            )
        )
        val serialized = serializeCustomQuickChipsJson(existing)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$KEY_CUSTOM_CHIPS_PREFIX$userId", serialized)
            .apply()
        return existing
    }

    fun deleteCustomQuickChip(
        context: Context,
        userId: String = "default",
        chipId: String
    ): List<QuickAddPreset> {
        val existing = getCustomQuickChips(context, userId).filterNot { it.id == chipId }
        val serialized = serializeCustomQuickChipsJson(existing)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$KEY_CUSTOM_CHIPS_PREFIX$userId", serialized)
            .apply()
        return existing
    }

    fun serializeCustomQuickChipsJson(chips: List<QuickAddPreset>): String {
        val arr = JSONArray()
        chips.forEach { chip ->
            val obj = JSONObject().apply {
                put("id", chip.id)
                put("emoji", chip.emoji)
                put("label", chip.label)
                put("amount", chip.amount)
                put("category", chip.category)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun parseCustomQuickChipsJson(json: String): List<QuickAddPreset> {
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<QuickAddPreset>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id", "custom_$i")
                val emoji = obj.optString("emoji", "⚡")
                val label = obj.optString("label", "Item")
                val amount = obj.optLong("amount", 50L).coerceAtLeast(1L)
                val category = obj.optString("category", "Food")
                list.add(QuickAddPreset(id, emoji, label, amount, category))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun findLatestMatchingExpenseForUndo(
        expenses: List<ExpenseRecord>,
        undoEvent: QuickSpendUndoEvent
    ): ExpenseRecord? {
        return expenses
            .filter { !isIncomeTransaction(it) && it.amount == undoEvent.amount && it.title.equals(undoEvent.label, ignoreCase = true) }
            .maxByOrNull { it.txEpochMillis }
    }

    /**
     * 2. OXYGEN BURN TANK (RUNWAY PROGRESS BAR):
     * Computes "🫁 Monthly Oxygen Tank: X% Left (Expires in Y days)" with Emerald -> Amber -> Crimson color tier.
     */
    fun computeOxygenBurnTankTelemetry(
        monthlyBudgetTarget: Long,
        monthSpent: Long,
        todaySpent: Long = 0L,
        fallbackDailyLimit: Long = 500L,
        nowMillis: Long = System.currentTimeMillis()
    ): OxygenBurnTankTelemetry {
        val daysLeftInCalendarMonth = computeDaysLeftInMonth(nowMillis)
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val daysElapsed = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)

        val effectiveBudget = if (monthlyBudgetTarget > 0L) {
            monthlyBudgetTarget
        } else {
            max(1500L, fallbackDailyLimit * 30L)
        }
        val remaining = (effectiveBudget - monthSpent).coerceAtLeast(0L)
        val fractionLeft = (remaining.toFloat() / effectiveBudget.toFloat()).coerceIn(0f, 1f)
        val percentLeft = kotlin.math.round(fractionLeft * 100f).toInt().coerceIn(0, 100)

        // Daily burn rate based on actual spend velocity or target daily rate
        val averageDailyBurn = if (monthSpent > 0L) {
            max(1L, monthSpent / daysElapsed.toLong())
        } else {
            max(1L, effectiveBudget / 30L)
        }
        val projectedRunwayDays = if (remaining <= 0L) {
            0
        } else {
            (remaining / averageDailyBurn).toInt().coerceIn(1, daysLeftInCalendarMonth)
        }

        val statusTier = when {
            percentLeft <= 20 -> SafeBurnStatusTier.CRIMSON_EXCEEDED
            percentLeft <= 45 -> SafeBurnStatusTier.AMBER_WARNING
            else -> SafeBurnStatusTier.EMERALD_SAFE
        }

        return OxygenBurnTankTelemetry(
            monthlyBudgetTarget = effectiveBudget,
            monthSpent = monthSpent,
            remainingMonthlyBudget = remaining,
            oxygenPercentLeft = percentLeft,
            oxygenFractionLeft = fractionLeft,
            expiresInDays = projectedRunwayDays,
            statusTier = statusTier,
            titleText = "🫁 Monthly Oxygen Tank: $percentLeft% Left (Expires in $projectedRunwayDays days)"
        )
    }

    /**
     * 3. HALL OF SHAME OVERDUE CAROUSEL:
     * Filters Khata contacts with overdue "Lena Hai" balances (> ₹500 and > 7 days late).
     */
    fun computeHallOfShameDefaulters(
        contactStates: List<com.example.data.model.KhataContactComputedState>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<HallOfShameDefaulterItem> {
        val dayMillis = 24L * 60L * 60L * 1000L
        return contactStates.mapNotNull { state ->
            if (state.isSettled || state.netDirection != com.example.data.model.KhataDirection.LENA_HAI) {
                return@mapNotNull null
            }
            if (state.outstandingAmount <= 500L) {
                return@mapNotNull null
            }
            val referencePastMillis = state.earliestUnsettledDueDateMillis
                ?: state.activeEntries.minOfOrNull { it.txDateMillis }
                ?: state.lastTransactionMillis

            val daysElapsed = if (referencePastMillis > 0L && nowMillis > referencePastMillis) {
                ((nowMillis - referencePastMillis) / dayMillis).toInt()
            } else {
                0
            }

            if (daysElapsed <= 7) {
                return@mapNotNull null
            }

            val topReason = state.activeEntries.firstOrNull()?.description?.ifBlank { "Shared Split / Udhaar" }
                ?: "Shared Split / Udhaar"

            HallOfShameDefaulterItem(
                contactId = state.contact.id,
                friendName = state.contact.name,
                friendPhone = state.contact.phone,
                overdueAmountInr = state.outstandingAmount,
                daysLate = daysElapsed,
                itemSummary = topReason,
                contactState = state
            )
        }.sortedByDescending { it.overdueAmountInr }
    }

    /**
     * 4. MIDNIGHT MUNCHIES & IMPULSE SHIELD (11 PM - 5 AM):
     * Manages shield toggle state, time-window detection, and Quarantine Hold queue.
     */
    fun isMidnightMunchiesShieldEnabled(context: Context, userId: String = "default"): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("$KEY_MIDNIGHT_SHIELD_ENABLED_PREFIX$userId", true)
    }

    fun setMidnightMunchiesShieldEnabled(context: Context, userId: String = "default", enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("$KEY_MIDNIGHT_SHIELD_ENABLED_PREFIX$userId", enabled)
            .apply()
    }

    fun isInMidnightMunchiesWindow(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        return hourOfDay >= 23 || hourOfDay < 5
    }

    fun isMidnightImpulseCategory(category: String): Boolean {
        val bucket = mapExpenseToNeonBucket(category)
        val lower = category.trim().lowercase(Locale.ENGLISH)
        return bucket == "Food" || bucket == "Shopping" ||
            lower.contains("entertainment") || lower.contains("snack") || lower.contains("swiggy") || lower.contains("zomato")
    }

    fun shouldQuarantineMidnightExpense(
        context: Context,
        userId: String = "default",
        category: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!isMidnightMunchiesShieldEnabled(context, userId)) return false
        return isInMidnightMunchiesWindow(nowMillis) && isMidnightImpulseCategory(category)
    }

    fun getQuarantinedEntries(context: Context, userId: String = "default"): List<MidnightQuarantineEntry> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("$KEY_MIDNIGHT_QUARANTINE_QUEUE_PREFIX$userId", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<MidnightQuarantineEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    MidnightQuarantineEntry(
                        id = obj.optString("id", "q_$i"),
                        title = obj.optString("title", "Late-Night Order"),
                        category = obj.optString("category", "Food"),
                        amountInr = obj.optLong("amountInr", 250L).coerceAtLeast(1L),
                        flaggedAtEpochMillis = obj.optLong("flaggedAtEpochMillis", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveQuarantinedEntries(
        context: Context,
        userId: String,
        entries: List<MidnightQuarantineEntry>
    ) {
        val arr = JSONArray()
        entries.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("category", item.category)
                    put("amountInr", item.amountInr)
                    put("flaggedAtEpochMillis", item.flaggedAtEpochMillis)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$KEY_MIDNIGHT_QUARANTINE_QUEUE_PREFIX$userId", arr.toString())
            .apply()
    }

    fun quarantineMidnightExpense(
        context: Context,
        userId: String = "default",
        title: String,
        category: String,
        amountInr: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): List<MidnightQuarantineEntry> {
        val current = getQuarantinedEntries(context, userId).toMutableList()
        val entry = MidnightQuarantineEntry(
            id = "mq_${nowMillis}_${current.size}",
            title = title.trim().ifBlank { "Late-Night Impulse" },
            category = category.trim().ifBlank { "Food" },
            amountInr = amountInr.coerceAtLeast(1L),
            flaggedAtEpochMillis = nowMillis
        )
        current.add(0, entry)
        saveQuarantinedEntries(context, userId, current)
        return current
    }

    fun dismissQuarantinedEntry(
        context: Context,
        userId: String = "default",
        entryId: String
    ): List<MidnightQuarantineEntry> {
        val updated = getQuarantinedEntries(context, userId).filterNot { it.id == entryId }
        saveQuarantinedEntries(context, userId, updated)
        return updated
    }
}

@Composable
fun UnifiedLiveKhataAndBillsTickerStrip(
    lenaTotal: Long,
    denaTotal: Long,
    pendingBillsCount: Int,
    stealthMode: Boolean,
    onRemindClicked: () -> Unit,
    onPayNowClicked: () -> Unit,
    onBillsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lenaTotal <= 0L && denaTotal <= 0L && pendingBillsCount <= 0) {
        // Always show a clean subtle status ticker even when all dues are settled
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0E0D1B).copy(alpha = 0.90f))
                .border(0.8.dp, Color(0xFF281F47), RoundedCornerShape(14.dp))
                .clickable { onRemindClicked() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("home_unified_live_status_ticker"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🟢 Khata & Bills All Settled • 0 pending dues",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF00E676),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "[Open Khata]",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD8B4FE)
            )
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0B1418).copy(alpha = 0.95f),
                        Color(0xFF140D22).copy(alpha = 0.95f)
                    )
                )
            )
            .border(1.dp, Color(0xFF34265C), RoundedCornerShape(14.dp))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("home_unified_live_status_ticker"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lenaTotal > 0L) {
            val formattedLena = if (stealthMode) "₹••••" else PerformancePreferences.preFormatInr(lenaTotal)
            Surface(
                color = Color(0xFF062417),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.9.dp, Color(0xFF00E676).copy(alpha = 0.75f)),
                modifier = Modifier
                    .clickable { onRemindClicked() }
                    .testTag("status_ticker_lena_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🟢 You get $formattedLena from Khata",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676),
                        maxLines = 1
                    )
                    Text(
                        text = "• [Remind]",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.testTag("status_ticker_remind_action")
                    )
                }
            }
        }

        if (denaTotal > 0L) {
            val formattedDena = if (stealthMode) "₹••••" else PerformancePreferences.preFormatInr(denaTotal)
            Surface(
                color = Color(0xFF280915),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.9.dp, Color(0xFFFF3366).copy(alpha = 0.75f)),
                modifier = Modifier
                    .clickable { onPayNowClicked() }
                    .testTag("status_ticker_dena_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔴 You owe $formattedDena in Khata",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3366),
                        maxLines = 1
                    )
                    Text(
                        text = "• [Pay Now]",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.testTag("status_ticker_pay_now_action")
                    )
                }
            }
        }

        if (pendingBillsCount > 0) {
            Surface(
                color = Color(0xFF1D1536),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color(0xFFA855F7).copy(alpha = 0.65f)),
                modifier = Modifier
                    .clickable { onBillsClicked() }
                    .testTag("status_ticker_bills_pill")
            ) {
                Text(
                    text = "⚡ $pendingBillsCount Bill Due(s) • [View]",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD8B4FE),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuickSpendUndoFloatingSnackbarPill(
    undoEvent: QuickSpendUndoEvent?,
    onUndoClicked: (QuickSpendUndoEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = undoEvent != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        undoEvent?.let { ev ->
            Surface(
                color = Color(0xFF100C24),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.2.dp, Color(0xFF00E676)),
                modifier = Modifier
                    .shadow(20.dp, RoundedCornerShape(50), spotColor = Color(0xFF00E676))
                    .testTag("quick_spend_undo_floating_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "✓ ₹${ev.amount} ${ev.label} logged",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = Color(0xFFB8B0D4)
                    )
                    Surface(
                        color = Color(0xFF00E676).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, Color(0xFF00E676)),
                        modifier = Modifier
                            .clickable { onUndoClicked(ev) }
                            .testTag("quick_spend_undo_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo Quick Spend",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "↩ UNDO",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Undo Pill",
                        tint = Color(0xFF8E84AD),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDismiss() }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateCustomQuickChipDialog(
    onDismiss: () -> Unit,
    onSaveChip: (emoji: String, label: String, amount: Long, category: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val emojiOptions = remember { listOf("🥛", "🏋️", "⛽", "🥗", "🚌", "🥖", "🧃", "⚡") }
    val categoryOptions = remember { listOf("Food", "Chai & Snacks", "Transport", "Groceries", "Fitness", "Bills") }

    var selectedEmoji by remember { mutableStateOf("🥛") }
    var labelInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var appliedCouponCode by remember { mutableStateOf<String?>(null) }
    val matchedQuickCoupon = remember(context, selectedCategory, labelInput) {
        com.example.util.CouponSniffer.findBestMatchingCoupon(
            context = context,
            category = selectedCategory,
            merchantQuery = labelInput
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFA855F7),
        unfocusedBorderColor = Color(0xFF33265A),
        focusedContainerColor = Color(0xFF0A0A14),
        unfocusedContainerColor = Color(0xFF0A0A14),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color(0xFFD8B4FE),
        unfocusedLabelColor = Color(0xFFB8B0D4)
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF100B22))
                .border(1.2.dp, Color(0xFFA855F7), RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag("custom_quick_chip_creator_dialog"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create 1-Tap Quick Spend Chip",
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFFB8B0D4),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDismiss() }
                )
            }

            Text(
                text = "Save your personal daily expense (e.g. 🥛 ₹35 Milk or 🏋️ ₹50 Eggs) for instant 1-tap logging.",
                fontSize = 12.sp,
                color = Color(0xFFB8B0D4),
                lineHeight = 16.sp
            )

            // Quick Preset Templates for 1-Tap Fill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("🥛", "Milk", 35L),
                    Triple("🏋️", "Eggs", 50L),
                    Triple("⛽", "Fuel", 200L),
                    Triple("🥗", "Lunch", 150L)
                ).forEach { (emo, lbl, amt) ->
                    Surface(
                        color = Color(0xFF1A1233),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, Color(0xFF7C3AED).copy(alpha = 0.6f)),
                        modifier = Modifier.clickable {
                            selectedEmoji = emo
                            labelInput = lbl
                            amountInput = amt.toString()
                        }
                    ) {
                        Text(
                            text = "$emo ₹$amt $lbl",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Emoji Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emojiOptions.forEach { emoji ->
                    val active = selectedEmoji == emoji
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (active) Color(0xFF7C3AED).copy(alpha = 0.35f) else Color(0xFF0A0A14))
                            .border(1.dp, if (active) Color(0xFFA855F7) else Color(0xFF281B4D), CircleShape)
                            .clickable { selectedEmoji = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }
            }

            OutlinedTextField(
                value = labelInput,
                onValueChange = { labelInput = it },
                label = { Text("Item Name (e.g. Milk, Eggs)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_chip_label_input")
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Amount (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_chip_amount_input")
            )

            // AI Coupon & Cashback Auto-Sniffer Pill inside Quick Spend Sheet
            if (matchedQuickCoupon != null) {
                val isApplied = appliedCouponCode == matchedQuickCoupon.code
                com.example.util.ContextualCouponSnifferPill(
                    coupon = matchedQuickCoupon,
                    isApplied = isApplied,
                    onApplyCoupon = { coupon ->
                        if (!isApplied) {
                            appliedCouponCode = coupon.code
                            com.example.util.CouponSniffer.copyCouponToClipboard(context, coupon.code)
                            val curAmt = amountInput.toLongOrNull() ?: 0L
                            if (curAmt > coupon.estimatedSavingsInr) {
                                amountInput = (curAmt - coupon.estimatedSavingsInr).toString()
                            }
                        }
                    }
                )
            }

            // Category Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categoryOptions.forEach { cat ->
                    val active = selectedCategory == cat
                    Surface(
                        color = if (active) Color(0xFF7C3AED).copy(alpha = 0.30f) else Color(0xFF0A0A14),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, if (active) Color(0xFFA855F7) else Color(0xFF281B4D)),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) Color.White else Color(0xFFB8B0D4),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF33265A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color(0xFFB8B0D4))
                }
                Button(
                    onClick = {
                        val amt = amountInput.toLongOrNull()?.coerceAtLeast(1L) ?: 35L
                        val lbl = labelInput.trim().ifBlank { "Milk" }
                        onSaveChip(selectedEmoji, lbl, amt, selectedCategory)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_chip_save_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Save Custom Chip",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Chip", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun HomeInteractiveDonutChartHero(
    expenses: List<ExpenseRecord>,
    monthlyBudgetTarget: Long,
    monthSpent: Long,
    todaySpent: Long,
    dailyLimit: Long,
    streakDays: Int,
    disciplineScore: Int,
    stealthMode: Boolean,
    safeBurnTelemetry: DailySafeBurnTelemetry? = null,
    onToggleStealth: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val resolvedTelemetry = remember(safeBurnTelemetry, monthlyBudgetTarget, monthSpent, todaySpent, dailyLimit) {
        safeBurnTelemetry ?: HomeDailyCockpitEngine.computeDailySafeBurnTelemetry(
            monthlyBudgetTarget = monthlyBudgetTarget,
            monthSpent = monthSpent,
            todaySpent = todaySpent,
            fallbackDailyLimit = max(50L, dailyLimit)
        )
    }

    val slices = remember(expenses) {
        HomeDailyCockpitEngine.computeHomeDonutSlices(expenses)
    }

    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    val selectedSlice = remember(slices, selectedCategoryKey) {
        slices.firstOrNull { it.categoryKey == selectedCategoryKey }
    }

    val remainingMonthlyBudget = resolvedTelemetry.remainingMonthlyBudget
    val formattedRemainingBudget = remember(remainingMonthlyBudget, stealthMode) {
        if (stealthMode) "👁️ ••••••"
        else PerformancePreferences.preFormatInr(remainingMonthlyBudget)
    }

    val safePillColor = when (resolvedTelemetry.statusTier) {
        SafeBurnStatusTier.EMERALD_SAFE -> Color(0xFFB7F000)
        SafeBurnStatusTier.AMBER_WARNING -> Color(0xFFFFD600)
        SafeBurnStatusTier.CRIMSON_EXCEEDED -> Color(0xFFFF3366)
    }
    val safePillBg = when (resolvedTelemetry.statusTier) {
        SafeBurnStatusTier.EMERALD_SAFE -> Color(0xFF172206)
        SafeBurnStatusTier.AMBER_WARNING -> Color(0xFF261E04)
        SafeBurnStatusTier.CRIMSON_EXCEEDED -> Color(0xFF280814)
    }

    // Animated stroke expansion for each of the 5 neon categories
    val foodExpansion by animateFloatAsState(
        targetValue = if (selectedCategoryKey == "Food") 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "foodDonutExpand"
    )
    val shoppingExpansion by animateFloatAsState(
        targetValue = if (selectedCategoryKey == "Shopping") 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "shoppingDonutExpand"
    )
    val travelExpansion by animateFloatAsState(
        targetValue = if (selectedCategoryKey == "Travel") 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "travelDonutExpand"
    )
    val billsExpansion by animateFloatAsState(
        targetValue = if (selectedCategoryKey == "Bills") 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "billsDonutExpand"
    )
    val otherExpansion by animateFloatAsState(
        targetValue = if (selectedCategoryKey == "Other") 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "otherDonutExpand"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFFB7F000).copy(alpha = 0.22f))
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF080B08),
                        Color(0xFF101510),
                        Color(0xFF080B08)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(950f, 750f)
                )
            )
            .border(
                width = 1.1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB7F000).copy(alpha = 0.80f),
                        Color(0xFF232E23),
                        Color(0xFF232E23),
                        Color(0xFFB7F000).copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .testTag("cred_black_titanium_card"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Strip: Cockpit Title + Discipline Streak Badge + Privacy Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_interactive_donut_hero"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIVE SPEND TELEMETRY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFFB7F000),
                    maxLines = 1
                )
                Text(
                    text = "Tap any category arc to inspect spend",
                    fontSize = 11.sp,
                    color = Color(0xFFC8D2C8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(0xFF161D16),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFFB7F000).copy(alpha = 0.7f)),
                    modifier = Modifier.testTag("hero_card_discipline_streak_badge")
                ) {
                    Text(
                        text = "🔥 ${streakDays.coerceAtLeast(1)}d · ${disciplineScore.coerceIn(0, 900)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB7F000),
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFF161D16),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = if (stealthMode) Color(0xFFB7F000) else Color(0xFF232E23)
                    ),
                    modifier = Modifier
                        .clickable(enabled = onToggleStealth != null) { onToggleStealth?.invoke() }
                        .testTag("hero_card_hide_balance_eye_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (stealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (stealthMode) "Reveal Balance" else "Hide Balance on Dashboard",
                            tint = if (stealthMode) Color(0xFFB7F000) else Color(0xFFC8D2C8),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (stealthMode) "👁️ ••••••" else "Hide",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (stealthMode) Color(0xFFB7F000) else Color(0xFFC8D2C8)
                        )
                    }
                }
            }
        }

        // Interactive Multi-Segment Donut/Pie Chart Canvas + Center Hole Live Remaining Budget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(236.dp)
                    .pointerInput(slices) {
                        detectTapGestures { tapOffset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val outerRadius = (size.width.coerceAtMost(size.height) / 2f) * 0.96f
                            val innerRadius = outerRadius * 0.52f
                            val tapped = HomeDailyCockpitEngine.hitTestDonutSlice(
                                slices = slices,
                                tapX = tapOffset.x,
                                tapY = tapOffset.y,
                                centerX = cx,
                                centerY = cy,
                                innerRadiusPx = innerRadius,
                                outerRadiusPx = outerRadius
                            )
                            if (tapped != null) {
                                selectedCategoryKey = if (selectedCategoryKey == tapped.categoryKey) {
                                    null
                                } else {
                                    tapped.categoryKey
                                }
                            } else {
                                selectedCategoryKey = null
                            }
                        }
                    }
                    .testTag("home_interactive_donut_canvas")
            ) {
                val baseStrokePx = 28.dp.toPx()
                val expandedExtraStrokePx = 9.dp.toPx()
                val radialPopPx = 6.dp.toPx()
                val paddingPx = baseStrokePx / 2f + expandedExtraStrokePx + radialPopPx
                val diameter = (size.minDimension - paddingPx * 2f).coerceAtLeast(40f)
                val baseTopLeft = Offset(
                    x = (size.width - diameter) / 2f,
                    y = (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                // Subtle dark track ring
                drawArc(
                    color = Color(0xFF1A162B),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = baseTopLeft,
                    size = arcSize,
                    style = Stroke(width = baseStrokePx, cap = StrokeCap.Butt)
                )

                val gapDeg = if (slices.size > 1) 3.6f else 0f

                slices.forEach { slice ->
                    val expansion = when (slice.categoryKey) {
                        "Food" -> foodExpansion
                        "Shopping" -> shoppingExpansion
                        "Travel" -> travelExpansion
                        "Bills" -> billsExpansion
                        else -> otherExpansion
                    }
                    val isDimmed = selectedCategoryKey != null && selectedCategoryKey != slice.categoryKey
                    val strokeWidth = baseStrokePx + (expandedExtraStrokePx * expansion)
                    val effectiveSweep = (slice.sweepAngleDeg - gapDeg).coerceAtLeast(2f)
                    val effectiveStart = slice.startAngleDeg + (gapDeg / 2f)

                    // Radial pop-out along bisector angle when tapped
                    val midAngleRad = ((effectiveStart + effectiveSweep / 2f) * (PI / 180.0)).toFloat()
                    val offsetX = cos(midAngleRad) * radialPopPx * expansion
                    val offsetY = sin(midAngleRad) * radialPopPx * expansion
                    val sliceTopLeft = Offset(
                        x = baseTopLeft.x + offsetX,
                        y = baseTopLeft.y + offsetY
                    )

                    // Outer neon halo glow
                    drawArc(
                        color = slice.color.copy(
                            alpha = when {
                                expansion > 0.05f -> 0.36f
                                isDimmed -> 0.05f
                                else -> 0.16f
                            }
                        ),
                        startAngle = effectiveStart,
                        sweepAngle = effectiveSweep,
                        useCenter = false,
                        topLeft = sliceTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth + 10.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Vivid Neon Segment Arc
                    drawArc(
                        color = slice.color.copy(alpha = if (isDimmed) 0.38f else 1f),
                        startAngle = effectiveStart,
                        sweepAngle = effectiveSweep,
                        useCenter = false,
                        topLeft = sliceTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Center Hole of the Donut: Live Remaining Budget + 1-Tap Privacy Toggle (👁️ ••••••)
            Column(
                modifier = Modifier
                    .width(138.dp)
                    .clip(CircleShape)
                    .clickable(enabled = onToggleStealth != null) { onToggleStealth?.invoke() }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "REMAINING BUDGET",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFFC8D2C8),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formattedRemainingBudget,
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("hero_card_remaining_monthly_budget_value")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF161D16),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFFB7F000).copy(alpha = 0.7f))
                ) {
                    Text(
                        text = if (stealthMode) "👁️ •••••• (Tap)" else "👁️ Tap to Mask",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB7F000),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Floating Tooltip when a Donut Slice is Tapped / Expanded
        AnimatedVisibility(
            visible = selectedSlice != null,
            enter = fadeIn() + slideInVertically { -it / 3 },
            exit = fadeOut() + slideOutVertically { -it / 3 }
        ) {
            val activeSlice = selectedSlice
            if (activeSlice != null) {
                Surface(
                    color = Color(0xFF0E0C1D),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, activeSlice.color),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_donut_floating_tooltip")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(activeSlice.color.copy(alpha = 0.18f))
                                    .border(1.dp, activeSlice.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = activeSlice.emoji, fontSize = 15.sp)
                            }
                            Column {
                                Text(
                                    text = activeSlice.displayName.uppercase(Locale.ENGLISH),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = activeSlice.color
                                )
                                Text(
                                    text = String.format(Locale.ENGLISH, "%.1f%% of monthly spend", activeSlice.percentage),
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8B0D4)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (stealthMode) "₹••••" else PerformancePreferences.preFormatInr(activeSlice.amountInr),
                                fontFamily = headingFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Slice Tooltip",
                                tint = Color(0xFFB8B0D4),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { selectedCategoryKey = null }
                            )
                        }
                    }
                }
            }
        }

        // Interactive Neon Category Legend Pills (Food #00E676, Shopping #FF3366, Travel #00E5FF, Bills #B388FF, Other #FFD600)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val allLegendCategories = listOf(
                Triple("Food", "🍔", Color(HomeDailyCockpitEngine.NEON_FOOD_HEX)),
                Triple("Shopping", "🛍️", Color(HomeDailyCockpitEngine.NEON_SHOPPING_HEX)),
                Triple("Travel", "🚇", Color(HomeDailyCockpitEngine.NEON_TRAVEL_HEX)),
                Triple("Bills", "⚡", Color(HomeDailyCockpitEngine.NEON_BILLS_HEX)),
                Triple("Other", "✨", Color(HomeDailyCockpitEngine.NEON_OTHER_HEX))
            )
            allLegendCategories.forEach { (catName, emoji, neonColor) ->
                val sliceMatch = slices.firstOrNull { it.categoryKey == catName }
                val isSelected = selectedCategoryKey == catName
                val amountInr = sliceMatch?.amountInr ?: 0L
                Surface(
                    color = if (isSelected) neonColor.copy(alpha = 0.20f) else Color(0xFF0A0A14),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = if (isSelected) 1.2.dp else 0.8.dp,
                        color = if (isSelected) neonColor else neonColor.copy(alpha = 0.50f)
                    ),
                    modifier = Modifier
                        .clickable {
                            selectedCategoryKey = if (selectedCategoryKey == catName) null else catName
                        }
                        .testTag("home_donut_legend_${catName.lowercase(Locale.ENGLISH)}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(neonColor)
                        )
                        Text(
                            text = "$emoji $catName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (stealthMode) "₹•••" else PerformancePreferences.preFormatInr(amountInr),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = neonColor
                        )
                    }
                }
            }
        }

        // Daily Safe Spend Pill inside Hero Cockpit
        Surface(
            color = safePillBg,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, safePillColor.copy(alpha = 0.85f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_card_daily_safe_spend_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (stealthMode) "🎯 Today's Safe Spend: ₹•••• left" else resolvedTelemetry.pillText,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = safePillColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${resolvedTelemetry.daysLeftInMonth}d left in mo",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB8B0D4)
                )
            }
        }
    }
}

/**
 * 2. OXYGEN BURN TANK (RUNWAY PROGRESS BAR):
 * Sleek glowing progress bar titled "🫁 Monthly Oxygen Tank: X% Left (Expires in Y days)".
 * Color shifts dynamically from Emerald (safe) to Amber to Crimson as the monthly budget depletes.
 */
@Composable
fun MonthlyOxygenTankProgressBar(
    monthlyBudgetTarget: Long,
    monthSpent: Long,
    todaySpent: Long,
    dailyLimit: Long,
    stealthMode: Boolean,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val telemetry = remember(monthlyBudgetTarget, monthSpent, todaySpent, dailyLimit) {
        HomeDailyCockpitEngine.computeOxygenBurnTankTelemetry(
            monthlyBudgetTarget = monthlyBudgetTarget,
            monthSpent = monthSpent,
            todaySpent = todaySpent,
            fallbackDailyLimit = dailyLimit
        )
    }

    val tankColor = when (telemetry.statusTier) {
        SafeBurnStatusTier.EMERALD_SAFE -> Color(0xFFB7F000)
        SafeBurnStatusTier.AMBER_WARNING -> Color(0xFFD97706)
        SafeBurnStatusTier.CRIMSON_EXCEEDED -> Color(0xFFDC2626)
    }

    val animatedFraction by animateFloatAsState(
        targetValue = telemetry.oxygenFractionLeft.coerceIn(0.03f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "oxygenBurnTankSweep"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFE0E6DD), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("home_monthly_oxygen_tank_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = telemetry.titleText,
                fontFamily = headingFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF080B08),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_monthly_oxygen_tank_title")
            )

            Surface(
                color = if (telemetry.statusTier == SafeBurnStatusTier.EMERALD_SAFE) Color(0xFFB7F000) else tankColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.9.dp, Color(0xFF080B08).copy(alpha = 0.16f))
            ) {
                Text(
                    text = when (telemetry.statusTier) {
                        SafeBurnStatusTier.EMERALD_SAFE -> "O₂ OPTIMAL"
                        SafeBurnStatusTier.AMBER_WARNING -> "O₂ THINNING"
                        SafeBurnStatusTier.CRIMSON_EXCEEDED -> "O₂ CRITICAL"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (telemetry.statusTier == SafeBurnStatusTier.EMERALD_SAFE) Color(0xFF080B08) else tankColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // High-Contrast Oxygen Bar Track on Pure White Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF080B08))
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(tankColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (stealthMode) "Remaining O₂: ₹••••" else "Remaining O₂: ${PerformancePreferences.preFormatInr(telemetry.remainingMonthlyBudget)}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF080B08)
            )
            Text(
                text = if (stealthMode) "Burned: ₹••••" else "Burned: ${PerformancePreferences.preFormatInr(telemetry.monthSpent)} / ${PerformancePreferences.preFormatInr(telemetry.monthlyBudgetTarget)}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Color(0xFF475247)
            )
        }
    }
}

/**
 * 3. HALL OF SHAME OVERDUE CAROUSEL:
 * Horizontal scrolling card strip showcasing friends with overdue balances (>₹500 and >7 days late)
 * featuring live red warning pills and 1-tap WhatsApp Vasooli triggers.
 */
@Composable
fun HallOfShameOverdueCarousel(
    defaulters: List<HallOfShameDefaulterItem>,
    stealthMode: Boolean,
    onTriggerWhatsAppVasooli: (HallOfShameDefaulterItem) -> Unit,
    onOpenKhataLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF120915))
            .border(1.1.dp, Color(0xFFFF3366).copy(alpha = 0.65f), RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("home_hall_of_shame_overdue_carousel"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFF330A18),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, Color(0xFFFF3366))
                    ) {
                        Text(
                            text = "🚨 HALL OF SHAME",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF3366),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "${defaulters.size} Overdue (>₹500 & >7d)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF80AB)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Friends holding your money past 7 days — fire 1-tap WhatsApp Vasooli",
                    fontSize = 11.sp,
                    color = Color(0xFFD1B3C4),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = Color(0xFF240D1B),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color(0xFFFF3366).copy(alpha = 0.6f)),
                modifier = Modifier.clickable { onOpenKhataLedger() }
            ) {
                Text(
                    text = "Khata →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF80AB),
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                )
            }
        }

        if (defaulters.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0A14))
                    .border(0.8.dp, Color(0xFF281A2E), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅ Zero friends overdue (>₹500 & >7 days late). Your Vasooli discipline is spotless!",
                    fontSize = 11.sp,
                    color = Color(0xFF00E676),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                defaulters.forEach { item ->
                    Column(
                        modifier = Modifier
                            .width(242.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1E0914),
                                        Color(0xFF11070E)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFFFF3366).copy(alpha = 0.75f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                            .testTag("hall_of_shame_card_${item.contactId}"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.friendName,
                                fontFamily = headingFont,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            // Live Red Warning Pill
                            Surface(
                                color = Color(0xFF3B0718),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color(0xFFFF3366))
                            ) {
                                Text(
                                    text = "🚨 ${item.daysLate}d LATE",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF3366),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = if (stealthMode) "₹••••" else PerformancePreferences.preFormatInr(item.overdueAmountInr),
                            fontFamily = headingFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF3366)
                        )

                        Text(
                            text = item.itemSummary,
                            fontSize = 11.sp,
                            color = Color(0xFFD1B3C4),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Button(
                            onClick = { onTriggerWhatsAppVasooli(item) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF3366),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("hall_of_shame_vasooli_btn_${item.contactId}")
                        ) {
                            Text(
                                text = "📲 1-Tap WhatsApp Vasooli",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. MIDNIGHT MUNCHIES & IMPULSE SHIELD (11 PM - 5 AM):
 * Prominent Home toggle that flags late-night food/shopping entries into a "Quarantine Hold"
 * state requiring morning confirmation and preventing impulsive logging.
 */
@Composable
fun MidnightMunchiesImpulseShieldCard(
    shieldEnabled: Boolean,
    quarantinedEntries: List<MidnightQuarantineEntry>,
    stealthMode: Boolean,
    onToggleShield: (Boolean) -> Unit,
    onConfirmQuarantinedEntry: (MidnightQuarantineEntry) -> Unit,
    onDiscardQuarantinedEntry: (MidnightQuarantineEntry) -> Unit,
    onSimulateQuarantineHold: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val isInLateNightWindow = remember { HomeDailyCockpitEngine.isInMidnightMunchiesWindow() }
    val accentCyan = Color(0xFF00E5FF)
    val accentAmber = Color(0xFFFFD600)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF09111E),
                        Color(0xFF100C24),
                        Color(0xFF080A14)
                    )
                )
            )
            .border(
                width = 1.1.dp,
                color = if (shieldEnabled) accentCyan.copy(alpha = 0.65f) else Color(0xFF2B224C),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
            .testTag("home_midnight_munchies_shield_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌙 Midnight Munchies Shield (11 PM - 5 AM)",
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (shieldEnabled) {
                        if (isInLateNightWindow) {
                            "🛡️ ACTIVE NOW: Late-night Food & Shopping orders are routed to Quarantine Hold."
                        } else {
                            "🛡️ ARMED FOR 11 PM – 5 AM: Intercepts impulsive Swiggy/Zomato & Shopping sprees."
                        }
                    } else {
                        "Shield paused — tap toggle to quarantine 11 PM – 5 AM impulse spends until morning."
                    },
                    fontSize = 11.sp,
                    color = Color(0xFFB8B0D4),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                color = if (shieldEnabled) Color(0xFF052933) else Color(0xFF1A162B),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (shieldEnabled) accentCyan else Color(0xFF4A3E6D)
                ),
                modifier = Modifier
                    .clickable { onToggleShield(!shieldEnabled) }
                    .testTag("midnight_munchies_shield_toggle")
            ) {
                Text(
                    text = if (shieldEnabled) "🛡️ ON" else "OFF",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (shieldEnabled) accentCyan else Color(0xFF9E94C4),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        if (quarantinedEntries.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141026))
                    .border(1.dp, accentAmber.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .testTag("midnight_quarantine_hold_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏳ QUARANTINE HOLD (${quarantinedEntries.size} PENDING MORNING CONFIRMATION)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentAmber
                    )
                }

                quarantinedEntries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0A14))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${entry.title} (${entry.category})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (stealthMode) "Held: ₹•••• · Needs morning review" else "Held: ${PerformancePreferences.preFormatInr(entry.amountInr)} · Prevented impulse log",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = accentAmber
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF07291E),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color(0xFF00E676)),
                                modifier = Modifier
                                    .clickable { onConfirmQuarantinedEntry(entry) }
                                    .testTag("confirm_quarantine_${entry.id}")
                            ) {
                                Text(
                                    text = "✓ Confirm",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E676),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                color = Color(0xFF2A0915),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color(0xFFFF3366)),
                                modifier = Modifier
                                    .clickable { onDiscardQuarantinedEntry(entry) }
                                    .testTag("discard_quarantine_${entry.id}")
                            ) {
                                Text(
                                    text = "✕ Cancel",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF3366),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (shieldEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No late-night impulse spends in Quarantine Hold.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = Color(0xFF8E84AD),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = Color(0xFF151228),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, accentCyan.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clickable { onSimulateQuarantineHold() }
                        .testTag("simulate_midnight_quarantine_button")
                ) {
                    Text(
                        text = "🧪 Test 1 AM Swiggy Hold",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}


