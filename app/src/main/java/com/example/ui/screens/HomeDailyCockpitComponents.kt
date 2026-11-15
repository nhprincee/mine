package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExpenseRecord
import com.example.data.model.QuickAddPreset
import com.example.data.model.isIncomeTransaction
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.util.PerformancePreferences
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
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

object HomeDailyCockpitEngine {
    private const val PREFS_NAME = "kharchee_daily_cockpit_prefs"
    private const val KEY_CUSTOM_CHIPS_PREFIX = "custom_quick_chips_"

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
