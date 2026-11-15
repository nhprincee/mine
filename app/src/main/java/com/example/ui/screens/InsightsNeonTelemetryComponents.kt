package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompleteInsightAnalyticsBundle
import com.example.data.model.ExpenseRecord
import com.example.data.model.InsightCategoryDetail
import com.example.data.model.isIncomeTransaction
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================================
// NEON TELEMETRY PALETTE (HIGH-VISUAL FINTECH ANALYTICS COMMAND CENTER)
// ============================================================================
val NeonEmeraldFood = Color(0xFF00E676)
val NeonCrimsonShopping = Color(0xFFFF3366)
val NeonCyanTravel = Color(0xFF00E5FF)
val NeonPurpleBills = Color(0xFFB388FF)
val NeonGoldEntertainment = Color(0xFFFFD600)

val HeatmapZeroSpendEmerald = Color(0xFF00E676)
val HeatmapLowSpendTeal = Color(0xFF004D40)
val HeatmapMediumSpendIndigo = Color(0xFF5E35B1)
val HeatmapHighSpikeCrimson = Color(0xFFFF1744)

private val NeonFallbackPalette = listOf(
    NeonEmeraldFood,
    NeonCrimsonShopping,
    NeonCyanTravel,
    NeonPurpleBills,
    NeonGoldEntertainment,
    Color(0xFFFF9100),
    Color(0xFF18FFFF),
    Color(0xFFF50057)
)

fun resolveNeonCategoryColor(category: String, index: Int = 0): Color {
    val norm = category.trim().lowercase(Locale.ENGLISH)
    return when {
        norm.contains("food") || norm.contains("grocery") || norm.contains("dining") || norm.contains("chai") -> NeonEmeraldFood
        norm.contains("shop") -> NeonCrimsonShopping
        norm.contains("travel") || norm.contains("transport") || norm.contains("cab") || norm.contains("fuel") -> NeonCyanTravel
        norm.contains("bill") || norm.contains("util") || norm.contains("rent") || norm.contains("recharge") -> NeonPurpleBills
        norm.contains("entertain") || norm.contains("movie") || norm.contains("sub") -> NeonGoldEntertainment
        else -> NeonFallbackPalette[index.coerceAtLeast(0) % NeonFallbackPalette.size]
    }
}

data class NeonDonutSlice(
    val category: String,
    val amount: Long,
    val percent: Int,
    val sweepAngle: Float,
    val startAngle: Float,
    val color: Color,
    val detail: InsightCategoryDetail?
)

data class DailyCashflowSplinePoint(
    val dayOfMonth: Int,
    val daySpend: Long,
    val cumulativeSpend: Long,
    val lastMonthDaySpend: Long,
    val lastMonthCumulativeSpend: Long
)

enum class SpendHeatmapIntensity(val label: String, val cellColor: Color, val textColor: Color) {
    ZERO_SPEND("₹0 Spend Day", HeatmapZeroSpendEmerald, Color(0xFF031A0E)),
    LOW_SPEND("Low Spend", HeatmapLowSpendTeal, Color(0xFFA7F3D0)),
    MEDIUM_SPEND("Medium Spend", HeatmapMediumSpendIndigo, Color(0xFFEDE9FE)),
    HIGH_SPIKE("High Spend Spike", HeatmapHighSpikeCrimson, Color.White)
}

data class CalendarHeatmapDayCell(
    val dayOfMonth: Int,
    val dateFormatted: String,
    val isFutureDay: Boolean,
    val totalSpend: Long,
    val intensity: SpendHeatmapIntensity,
    val transactions: List<ExpenseRecord>
)

data class TopMerchantDrainerItem(
    val rank: Int,
    val merchantName: String,
    val category: String,
    val totalAmount: Long,
    val transactionCount: Int,
    val shareOfMonthPercent: Int,
    val barRatio: Float,
    val accentColor: Color
)

data class DualSpeedometerMetrics(
    val budgetConsumedPercent: Int,
    val daysElapsedPercent: Int,
    val daysElapsed: Int,
    val daysInMonth: Int,
    val monthlyBudgetTarget: Long,
    val monthSpent: Long,
    val dailyAverageSpend: Long,
    val projectedMonthEndSavings: Long,
    val savingsEfficiencyScore: Int,
    val savingsStatusLabel: String
)

object InsightsNeonTelemetryEngine {

    fun buildNeonDonutSlices(
        categoryDetails: List<InsightCategoryDetail>,
        totalSpent: Long
    ): List<NeonDonutSlice> {
        val coreCategories = listOf("Food", "Shopping", "Travel", "Bills", "Entertainment")
        val sortedDetails = categoryDetails.filter { it.amount > 0L }.sortedByDescending { it.amount }

        if (sortedDetails.isEmpty() || totalSpent <= 0L) {
            val equalSweep = 360f / coreCategories.size
            return coreCategories.mapIndexed { idx, cat ->
                NeonDonutSlice(
                    category = cat,
                    amount = 0L,
                    percent = 0,
                    sweepAngle = equalSweep,
                    startAngle = -90f + (idx * equalSweep),
                    color = resolveNeonCategoryColor(cat, idx),
                    detail = null
                )
            }
        }

        val topSlices = sortedDetails.take(6)
        val sumTop = topSlices.sumOf { it.amount }.coerceAtLeast(1L)
        var runningAngle = -90f

        return topSlices.mapIndexed { idx, item ->
            val rawRatio = item.amount.toFloat() / sumTop.toFloat()
            val sweep = if (idx == topSlices.lastIndex) {
                (270f - runningAngle).coerceAtLeast(8f)
            } else {
                (rawRatio * 360f).coerceAtLeast(10f)
            }
            val pct = ((item.amount.toDouble() / totalSpent.toDouble()) * 100.0).roundToInt().coerceIn(1, 100)
            val slice = NeonDonutSlice(
                category = item.category,
                amount = item.amount,
                percent = pct,
                sweepAngle = sweep,
                startAngle = runningAngle,
                color = resolveNeonCategoryColor(item.category, idx),
                detail = item
            )
            runningAngle += sweep
            slice
        }
    }

    fun buildMonthlyCashflowSplinePoints(
        expenses: List<ExpenseRecord>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<DailyCashflowSplinePoint> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val curYear = nowCal.get(Calendar.YEAR)
        val curMonth = nowCal.get(Calendar.MONTH)
        val daysInCurMonth = nowCal.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(28)

        val prevCal = (nowCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val prevYear = prevCal.get(Calendar.YEAR)
        val prevMonth = prevCal.get(Calendar.MONTH)

        val curMonthDaily = LongArray(daysInCurMonth + 1)
        val prevMonthDaily = LongArray(daysInCurMonth + 1)

        val txCal = Calendar.getInstance()
        expenses.forEach { tx ->
            if (!isIncomeTransaction(tx)) {
                txCal.timeInMillis = tx.txEpochMillis
                val y = txCal.get(Calendar.YEAR)
                val m = txCal.get(Calendar.MONTH)
                val dom = txCal.get(Calendar.DAY_OF_MONTH).coerceIn(1, daysInCurMonth)
                if (y == curYear && m == curMonth) {
                    curMonthDaily[dom] += tx.amount
                } else if (y == prevYear && m == prevMonth) {
                    prevMonthDaily[dom] += tx.amount
                }
            }
        }

        var runningCur = 0L
        var runningPrev = 0L
        return (1..daysInCurMonth).map { day ->
            val dSpend = curMonthDaily[day]
            val pSpend = prevMonthDaily[day]
            runningCur += dSpend
            runningPrev += pSpend
            DailyCashflowSplinePoint(
                dayOfMonth = day,
                daySpend = dSpend,
                cumulativeSpend = runningCur,
                lastMonthDaySpend = pSpend,
                lastMonthCumulativeSpend = runningPrev
            )
        }
    }

    fun classifyHeatmapIntensity(
        daySpend: Long,
        dailyLimit: Long
    ): SpendHeatmapIntensity {
        val safeLimit = max(300L, dailyLimit)
        return when {
            daySpend <= 0L -> SpendHeatmapIntensity.ZERO_SPEND
            daySpend <= (safeLimit * 60L) / 100L -> SpendHeatmapIntensity.LOW_SPEND
            daySpend <= (safeLimit * 120L) / 100L -> SpendHeatmapIntensity.MEDIUM_SPEND
            else -> SpendHeatmapIntensity.HIGH_SPIKE
        }
    }

    fun buildThirtyDayHeatmapMatrix(
        expenses: List<ExpenseRecord>,
        dailyLimit: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<List<CalendarHeatmapDayCell>, Int> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val curYear = nowCal.get(Calendar.YEAR)
        val curMonth = nowCal.get(Calendar.MONTH)
        val todayDom = nowCal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = nowCal.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(28)
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        val txByDay = mutableMapOf<Int, MutableList<ExpenseRecord>>()
        val txCal = Calendar.getInstance()
        expenses.forEach { tx ->
            if (!isIncomeTransaction(tx)) {
                txCal.timeInMillis = tx.txEpochMillis
                if (txCal.get(Calendar.YEAR) == curYear && txCal.get(Calendar.MONTH) == curMonth) {
                    val dom = txCal.get(Calendar.DAY_OF_MONTH)
                    txByDay.getOrPut(dom) { mutableListOf() }.add(tx)
                }
            }
        }

        var noSpendDaysCount = 0
        val cells = (1..daysInMonth).map { dom ->
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.DAY_OF_MONTH, dom)
            }
            val dayTxs = txByDay[dom].orEmpty().sortedByDescending { it.txEpochMillis }
            val daySpend = dayTxs.sumOf { it.amount }
            val isFuture = dom > todayDom
            if (!isFuture && daySpend == 0L) {
                noSpendDaysCount++
            }
            CalendarHeatmapDayCell(
                dayOfMonth = dom,
                dateFormatted = dateFmt.format(Date(dayCal.timeInMillis)),
                isFutureDay = isFuture,
                totalSpend = daySpend,
                intensity = classifyHeatmapIntensity(daySpend, dailyLimit),
                transactions = dayTxs
            )
        }
        return cells to noSpendDaysCount
    }

    fun computeDualSpeedometersAndTopMerchants(
        expenses: List<ExpenseRecord>,
        dailyLimit: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<DualSpeedometerMetrics, List<TopMerchantDrainerItem>> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val curYear = nowCal.get(Calendar.YEAR)
        val curMonth = nowCal.get(Calendar.MONTH)
        val daysElapsed = nowCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val daysInMonth = nowCal.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(28)

        var monthSpent = 0L
        var monthIncome = 0L
        val monthExpenseRecords = mutableListOf<ExpenseRecord>()
        val txCal = Calendar.getInstance()

        expenses.forEach { tx ->
            txCal.timeInMillis = tx.txEpochMillis
            if (txCal.get(Calendar.YEAR) == curYear && txCal.get(Calendar.MONTH) == curMonth) {
                if (isIncomeTransaction(tx)) {
                    monthIncome += tx.amount
                } else {
                    monthSpent += tx.amount
                    monthExpenseRecords.add(tx)
                }
            }
        }

        val monthlyBudgetTarget = max(3000L, max(500L, dailyLimit) * daysInMonth.toLong())
        val budgetConsumedPct = ((monthSpent.toDouble() / monthlyBudgetTarget.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
        val daysElapsedPct = ((daysElapsed.toDouble() / daysInMonth.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(1, 100)

        val dailyAvg = monthSpent / daysElapsed.toLong()
        val projectedMonthSpend = dailyAvg * daysInMonth.toLong()
        val baselinePool = max(monthIncome, monthlyBudgetTarget)
        val projectedSavings = max(0L, baselinePool - projectedMonthSpend)

        val rawEfficiency = if (baselinePool > 0L) {
            val burnRatio = projectedMonthSpend.toDouble() / baselinePool.toDouble()
            ((1.15 - burnRatio) * 85.0).roundToInt().coerceIn(8, 99)
        } else 75

        val statusLabel = when {
            rawEfficiency >= 75 -> "Elite Saver"
            rawEfficiency >= 45 -> "On Track"
            else -> "Critical Burn"
        }

        val groupedMerchants = monthExpenseRecords
            .groupBy { it.title.trim().ifBlank { it.category.trim().ifBlank { "Merchant Spend" } } }
            .map { (merchant, txs) ->
                Triple(
                    merchant,
                    txs.firstOrNull()?.category?.ifBlank { "General" } ?: "General",
                    txs.sumOf { it.amount } to txs.size
                )
            }
            .sortedByDescending { it.third.first }
            .take(5)

        val maxMerchantAmt = (groupedMerchants.maxOfOrNull { it.third.first } ?: 1L).coerceAtLeast(1L)
        val topMerchants = groupedMerchants.mapIndexed { idx, (merchant, category, pair) ->
            val amt = pair.first
            val count = pair.second
            val pct = if (monthSpent > 0L) {
                ((amt.toDouble() / monthSpent.toDouble()) * 100.0).roundToInt().coerceIn(1, 100)
            } else 0
            TopMerchantDrainerItem(
                rank = idx + 1,
                merchantName = merchant,
                category = category,
                totalAmount = amt,
                transactionCount = count,
                shareOfMonthPercent = pct,
                barRatio = (amt.toFloat() / maxMerchantAmt.toFloat()).coerceIn(0.12f, 1f),
                accentColor = resolveNeonCategoryColor(category, idx)
            )
        }

        return DualSpeedometerMetrics(
            budgetConsumedPercent = budgetConsumedPct,
            daysElapsedPercent = daysElapsedPct,
            daysElapsed = daysElapsed,
            daysInMonth = daysInMonth,
            monthlyBudgetTarget = monthlyBudgetTarget,
            monthSpent = monthSpent,
            dailyAverageSpend = dailyAvg,
            projectedMonthEndSavings = projectedSavings,
            savingsEfficiencyScore = rawEfficiency,
            savingsStatusLabel = statusLabel
        ) to topMerchants
    }
}

// ============================================================================
// 1. INTERACTIVE GLOWING DONUT RING CHART (CATEGORY BREAKDOWN)
// ============================================================================
@Composable
fun InteractiveGlowingNeonDonutChartCard(
    categoryDetails: List<InsightCategoryDetail>,
    totalSpent: Long,
    stealthMode: Boolean,
    onInspectCategory: (InsightCategoryDetail) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val slices = remember(categoryDetails, totalSpent) {
        InsightsNeonTelemetryEngine.buildNeonDonutSlices(categoryDetails, totalSpent)
    }

    var selectedSliceIndex by remember(slices) { mutableIntStateOf(0) }
    val activeSlice = slices.getOrElse(selectedSliceIndex) { slices.first() }

    val animatedSelectedStrokeDp by animateFloatAsState(
        targetValue = 34f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "donutSelectedStroke"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = activeSlice.color.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C101D),
                        Color(0xFF080A12),
                        Color(0xFF0A0D18)
                    )
                )
            )
            .border(1.2.dp, activeSlice.color.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("where_your_money_went_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "1 · NEON CATEGORY TELEMETRY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    color = activeSlice.color
                )
                Text(
                    text = "Where Your Money Went",
                    fontFamily = headingFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Surface(
                color = Color(0xFF13192B),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.9.dp, activeSlice.color.copy(alpha = 0.6f))
            ) {
                Text(
                    text = if (stealthMode) "Total: ₹••••" else "Total: ₹${formatIndianAmount(totalSpent)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // Interactive Canvas Neon Donut Ring + Dynamic Center Hub
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(238.dp)
                .testTag("interactive_neon_donut_canvas_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(220.dp)
                    .pointerInput(slices) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            val dist = hypot(dx, dy)
                            val radius = min(size.width, size.height) / 2f
                            if (dist in (radius * 0.42f)..(radius * 1.08f)) {
                                var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (deg < -90f) deg += 360f
                                val hitIdx = slices.indexOfFirst { s ->
                                    deg >= s.startAngle && deg <= (s.startAngle + s.sweepAngle)
                                }
                                if (hitIdx >= 0) {
                                    selectedSliceIndex = hitIdx
                                }
                            }
                        }
                    }
            ) {
                val diameter = size.minDimension * 0.78f
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val gapDegrees = if (slices.size > 1) 5f else 0f

                // Subtle Dark Ring Track
                drawArc(
                    color = Color(0xFF1A2234),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                )

                slices.forEachIndexed { idx, slice ->
                    val isSelected = idx == selectedSliceIndex
                    val strokePx = if (isSelected) animatedSelectedStrokeDp.dp.toPx() else 22.dp.toPx()
                    val effectiveSweep = (slice.sweepAngle - gapDegrees).coerceAtLeast(4f)
                    val effectiveStart = slice.startAngle + (gapDegrees / 2f)

                    // Outer Neon Glow Halo
                    drawArc(
                        color = slice.color.copy(alpha = if (isSelected) 0.34f else 0.14f),
                        startAngle = effectiveStart,
                        sweepAngle = effectiveSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx + 14.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Crisp Core Neon Arc
                    drawArc(
                        color = slice.color,
                        startAngle = effectiveStart,
                        sweepAngle = effectiveSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Center Hub Dynamic Telemetry
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .width(136.dp)
                    .clickable(enabled = activeSlice.detail != null) {
                        activeSlice.detail?.let(onInspectCategory)
                    }
                    .testTag("neon_donut_center_hub")
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(activeSlice.color.copy(alpha = 0.18f))
                        .border(1.dp, activeSlice.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(activeSlice.category),
                        contentDescription = activeSlice.category,
                        tint = activeSlice.color,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = activeSlice.category.uppercase(Locale.ENGLISH),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeSlice.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(activeSlice.amount)}",
                    fontFamily = headingFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = activeSlice.color.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.7.dp, activeSlice.color.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "${activeSlice.percent}% share",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeSlice.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Sleek Interactive Category Legend Pills below the Donut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEachIndexed { idx, slice ->
                val isSelected = idx == selectedSliceIndex
                Surface(
                    color = if (isSelected) slice.color.copy(alpha = 0.20f) else Color(0xFF111625),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        if (isSelected) 1.4.dp else 0.9.dp,
                        if (isSelected) slice.color else Color(0xFF283249)
                    ),
                    modifier = Modifier
                        .clickable {
                            selectedSliceIndex = idx
                        }
                        .testTag("top4_category_row_${slice.category.lowercase(Locale.ENGLISH)}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Text(
                            text = slice.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )
                        Text(
                            text = if (stealthMode) "₹•••" else "₹${formatIndianAmount(slice.amount)} (${slice.percent}%)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = slice.color
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. TRADINGVIEW-STYLE SMOOTH BEZIER CASHFLOW SPLINE GRAPH
// ============================================================================
@Composable
fun TradingViewBezierCashflowSplineCard(
    expenses: List<ExpenseRecord>,
    bundle: CompleteInsightAnalyticsBundle,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current
    val splinePoints = remember(expenses) {
        InsightsNeonTelemetryEngine.buildMonthlyCashflowSplinePoints(expenses)
    }

    val defaultDayIdx = remember(splinePoints) {
        val todayDom = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        (todayDom - 1).coerceIn(0, (splinePoints.size - 1).coerceAtLeast(0))
    }
    var scrubbedIndex by remember(splinePoints) { mutableIntStateOf(defaultDayIdx) }
    val activePoint = splinePoints.getOrElse(scrubbedIndex) {
        DailyCashflowSplinePoint(1, 0L, 0L, 0L, 0L)
    }

    val thisMonthCumulative = splinePoints.lastOrNull()?.cumulativeSpend ?: 0L
    val lastMonthCumulative = splinePoints.lastOrNull()?.lastMonthCumulativeSpend ?: 0L
    val paceDeltaPct = remember(thisMonthCumulative, lastMonthCumulative, bundle) {
        when {
            lastMonthCumulative > 0L -> {
                (((thisMonthCumulative - lastMonthCumulative).toDouble() / lastMonthCumulative.toDouble()) * 100.0).roundToInt()
            }
            bundle.overview.expenseChangePercent != null -> bundle.overview.expenseChangePercent
            else -> 0
        }
    }
    val isGreatPace = paceDeltaPct <= 0
    val pacePillColor = if (isGreatPace) NeonEmeraldFood else NeonGoldEntertainment
    val pacePillText = when {
        thisMonthCumulative == 0L && lastMonthCumulative == 0L -> "↓ 0% vs last month — Clean slate!"
        isGreatPace -> "↓ ${abs(paceDeltaPct)}% less spent than last month — Great pace!"
        else -> "↑ ${abs(paceDeltaPct)}% higher than last month"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = NeonCyanTravel.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A111F),
                        Color(0xFF070B14),
                        Color(0xFF090E1A)
                    )
                )
            )
            .border(1.2.dp, NeonCyanTravel.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("month_over_month_pace_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Month-over-Month Pace Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "2 · TRADINGVIEW CASHFLOW VELOCITY SPLINE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = NeonCyanTravel
                )
                Text(
                    text = "Cumulative Spend vs Last Month",
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Comparison Pill (retaining testTag = month_over_month_pace_pill)
        Surface(
            color = pacePillColor.copy(alpha = 0.14f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, pacePillColor.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("month_over_month_pace_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isGreatPace) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = "Pace",
                    tint = pacePillColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = pacePillText,
                    fontFamily = headingFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pacePillColor
                )
            }
        }

        // Floating Scrubbing Tooltip Pill: "Day [X]: ₹[Spent]"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0E1729))
                .border(1.dp, NeonCyanTravel.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("cashflow_spline_scrub_tooltip"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (stealthMode) {
                        "Day ${activePoint.dayOfMonth}: ₹••••"
                    } else {
                        "Day ${activePoint.dayOfMonth}: ₹${formatIndianAmount(activePoint.daySpend)}"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyanTravel
                )
                Text(
                    text = if (stealthMode) {
                        "Cumulative: ₹••••  •  Last Month: ₹••••"
                    } else {
                        "Cumulative: ₹${formatIndianAmount(activePoint.cumulativeSpend)}  •  Last Mo: ₹${formatIndianAmount(activePoint.lastMonthCumulativeSpend)}"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Surface(
                color = NeonCyanTravel.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.7.dp, NeonCyanTravel)
            ) {
                Text(
                    text = "⇄ DRAG TO SCRUB",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyanTravel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Smooth Cubic Bezier Spline Canvas with #6600E5FF Gradient Fill & Dashed Ghost Curve
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(186.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF060A12))
                .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 12.dp)
                .testTag("tradingview_bezier_spline_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(splinePoints) {
                        detectTapGestures { offset ->
                            if (splinePoints.isNotEmpty() && size.width > 0) {
                                val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                scrubbedIndex = (fraction * (splinePoints.size - 1)).roundToInt()
                                    .coerceIn(0, splinePoints.lastIndex)
                            }
                        }
                    }
                    .pointerInput(splinePoints) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            if (splinePoints.isNotEmpty() && size.width > 0) {
                                val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                scrubbedIndex = (fraction * (splinePoints.size - 1)).roundToInt()
                                    .coerceIn(0, splinePoints.lastIndex)
                            }
                        }
                    }
            ) {
                if (splinePoints.isEmpty()) return@Canvas
                val w = size.width
                val h = size.height
                val topPad = 14.dp.toPx()
                val bottomPad = 12.dp.toPx()
                val chartH = (h - topPad - bottomPad).coerceAtLeast(10f)

                val maxY = max(
                    splinePoints.maxOfOrNull { max(it.cumulativeSpend, it.lastMonthCumulativeSpend) } ?: 1000L,
                    1000L
                ).toFloat()

                // Horizontal subtle grid lines
                val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                for (row in 0..3) {
                    val gy = topPad + (chartH * row / 3f)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = gridDash
                    )
                }

                fun pointOffset(index: Int, value: Long): Offset {
                    val x = if (splinePoints.size > 1) {
                        (index.toFloat() / (splinePoints.size - 1).toFloat()) * w
                    } else w / 2f
                    val normY = (value.toFloat() / maxY).coerceIn(0f, 1f)
                    val y = topPad + chartH * (1f - normY)
                    return Offset(x, y)
                }

                val curOffsets = splinePoints.mapIndexed { idx, pt -> pointOffset(idx, pt.cumulativeSpend) }
                val ghostOffsets = splinePoints.mapIndexed { idx, pt -> pointOffset(idx, pt.lastMonthCumulativeSpend) }

                // 1. Dashed Ghost Curve for Last Month's Spending Pace
                val ghostPath = Path().apply {
                    ghostOffsets.forEachIndexed { idx, pt ->
                        if (idx == 0) {
                            moveTo(pt.x, pt.y)
                        } else {
                            val prev = ghostOffsets[idx - 1]
                            val ctrlX = (prev.x + pt.x) / 2f
                            cubicTo(ctrlX, prev.y, ctrlX, pt.y, pt.x, pt.y)
                        }
                    }
                }
                drawPath(
                    path = ghostPath,
                    color = NeonPurpleBills.copy(alpha = 0.55f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                )

                // 2. Smooth Cubic Bezier Curve + Vertical Area Gradient (#6600E5FF to Transparent)
                val splinePath = Path().apply {
                    curOffsets.forEachIndexed { idx, pt ->
                        if (idx == 0) {
                            moveTo(pt.x, pt.y)
                        } else {
                            val prev = curOffsets[idx - 1]
                            val ctrlX = (prev.x + pt.x) / 2f
                            cubicTo(ctrlX, prev.y, ctrlX, pt.y, pt.x, pt.y)
                        }
                    }
                }

                val fillPath = Path().apply {
                    addPath(splinePath)
                    lineTo(curOffsets.last().x, topPad + chartH)
                    lineTo(curOffsets.first().x, topPad + chartH)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x6600E5FF),
                            Color(0x1A00E5FF),
                            Color.Transparent
                        ),
                        startY = topPad,
                        endY = topPad + chartH
                    )
                )

                // Outer glow on the spline
                drawPath(
                    path = splinePath,
                    color = NeonCyanTravel.copy(alpha = 0.28f),
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )

                // Core Cyan Spline
                drawPath(
                    path = splinePath,
                    color = NeonCyanTravel,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // 3. Glowing Vertical Crosshair Line & Node at Scrubbed Day
                val activeOffset = curOffsets.getOrElse(scrubbedIndex) { curOffsets.last() }
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonCyanTravel,
                            NeonCyanTravel.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(activeOffset.x, 0f),
                    end = Offset(activeOffset.x, h),
                    strokeWidth = 1.8.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                drawCircle(
                    color = NeonCyanTravel.copy(alpha = 0.35f),
                    radius = 10.dp.toPx(),
                    center = activeOffset
                )
                drawCircle(
                    color = Color(0xFF060A12),
                    radius = 5.dp.toPx(),
                    center = activeOffset
                )
                drawCircle(
                    color = NeonCyanTravel,
                    radius = 4.dp.toPx(),
                    center = activeOffset
                )
            }
        }

        // Legend Footer (Current Month Cyan Curve vs Last Month Ghost Dashed Curve)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NeonCyanTravel)
                )
                Text(
                    text = if (stealthMode) "This Month: ₹••••" else "This Month: ₹${formatIndianAmount(thisMonthCumulative)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .background(NeonPurpleBills.copy(alpha = 0.8f))
                )
                Text(
                    text = if (stealthMode) "Last Month Ghost: ₹••••" else "Last Month Pace: ₹${formatIndianAmount(lastMonthCumulative)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeonPurpleBills
                )
            }
        }
    }
}

// ============================================================================
// 3. 30-DAY SPENDING INTENSITY HEATMAP MATRIX (CRED / GITHUB STYLE)
// ============================================================================
@Composable
fun ThirtyDaySpendingIntensityHeatmapCard(
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current
    val (cells, noSpendStreakCount) = remember(expenses, dailyLimit) {
        InsightsNeonTelemetryEngine.buildThirtyDayHeatmapMatrix(expenses, dailyLimit)
    }

    val defaultSelectedDom = remember(cells) {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        today.coerceIn(1, cells.size.coerceAtLeast(1))
    }
    var selectedDom by remember { mutableIntStateOf(defaultSelectedDom) }
    val selectedCell = cells.firstOrNull { it.dayOfMonth == selectedDom } ?: cells.firstOrNull()

    val rows = remember(cells) { cells.chunked(7) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = HeatmapZeroSpendEmerald.copy(alpha = 0.22f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B111C),
                        Color(0xFF070A12),
                        Color(0xFF0A0E19)
                    )
                )
            )
            .border(1.2.dp, Color(0xFF28354E), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("weekly_spending_heatmap_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + "🔥 No-Spend Streak" Counter Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "3 · 30-DAY SPENDING INTENSITY HEATMAP",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = HeatmapZeroSpendEmerald
                )
                Text(
                    text = "CRED / GitHub Monthly Spend Matrix",
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Surface(
                color = HeatmapZeroSpendEmerald.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, HeatmapZeroSpendEmerald),
                modifier = Modifier.testTag("heatmap_no_spend_streak_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "No-Spend Streak",
                        tint = HeatmapZeroSpendEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "🔥 $noSpendStreakCount No-Spend Days",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HeatmapZeroSpendEmerald
                    )
                }
            }
        }

        // 7-Column Day-of-Week Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                Text(
                    text = d,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7-Column Monthly Calendar Heatmap Grid
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { weekRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until 7) {
                        val cell = weekRow.getOrNull(col)
                        if (cell != null) {
                            val isSelected = cell.dayOfMonth == selectedDom
                            val bg = if (cell.isFutureDay && cell.totalSpend == 0L) {
                                Color(0xFF111827)
                            } else {
                                cell.intensity.cellColor
                            }
                            val txtColor = if (cell.isFutureDay && cell.totalSpend == 0L) {
                                Color(0xFF64748B)
                            } else {
                                cell.intensity.textColor
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.8.dp,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDom = cell.dayOfMonth }
                                    .testTag("heatmap_day_cell_${cell.dayOfMonth}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cell.dayOfMonth}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = txtColor
                                )
                            }
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            )
                        }
                    }
                }
            }
        }

        // Intensity Legend Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpendHeatmapIntensity.entries.forEach { level ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(level.cellColor)
                    )
                    Text(
                        text = level.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Mini Drill-Down Card right underneath showing the tapped date's total spend & transactions
        if (selectedCell != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0E1525))
                    .border(
                        1.dp,
                        selectedCell.intensity.cellColor.copy(alpha = 0.75f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
                    .testTag("weekly_heatmap_selected_day_detail"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = selectedCell.dateFormatted.uppercase(Locale.ENGLISH),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyanTravel
                        )
                        Text(
                            text = selectedCell.intensity.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(selectedCell.totalSpend)}",
                        fontFamily = headingFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selectedCell.totalSpend == 0L) HeatmapZeroSpendEmerald else Color.White
                    )
                }

                if (selectedCell.transactions.isEmpty()) {
                    Text(
                        text = "🔥 Zero spend recorded on this date — disciplined streak intact!",
                        fontSize = 11.sp,
                        color = Color(0xFFA7F3D0)
                    )
                } else {
                    selectedCell.transactions.take(4).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF080C15))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${tx.title.ifBlank { tx.category }} (${tx.category})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (stealthMode) "-₹•••" else "-₹${formatIndianAmount(tx.amount)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCrimsonShopping
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. DUAL RADIAL SPEEDOMETER GAUGES & TOP 5 MONEY DRAINERS (MERCHANT LEADERBOARD)
// ============================================================================
@Composable
fun DualRadialSpeedometersAndTopMerchantsSection(
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current
    val (metrics, topMerchants) = remember(expenses, dailyLimit) {
        InsightsNeonTelemetryEngine.computeDualSpeedometersAndTopMerchants(expenses, dailyLimit)
    }

    val animatedBurnSweep by animateFloatAsState(
        targetValue = (metrics.budgetConsumedPercent / 100f) * 240f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "burnSpeedometerSweep"
    )
    val animatedEfficiencySweep by animateFloatAsState(
        targetValue = (metrics.savingsEfficiencyScore / 100f) * 300f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "savingsEfficiencySweep"
    )

    val burnColor = when {
        metrics.budgetConsumedPercent <= metrics.daysElapsedPercent -> NeonEmeraldFood
        metrics.budgetConsumedPercent <= metrics.daysElapsedPercent + 15 -> NeonGoldEntertainment
        else -> NeonCrimsonShopping
    }
    val efficiencyColor = when {
        metrics.savingsEfficiencyScore >= 75 -> NeonEmeraldFood
        metrics.savingsEfficiencyScore >= 45 -> NeonCyanTravel
        else -> NeonCrimsonShopping
    }

    val dailyAvgFormatted = if (stealthMode) "••••" else formatIndianAmount(metrics.dailyAverageSpend)
    val projectedSavingsFormatted = if (stealthMode) "••••" else formatIndianAmount(metrics.projectedMonthEndSavings)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("month_end_forecast_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 2-Column Visual Row: Left ("Budget Burn Speedometer") & Right ("Savings Efficiency Score")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Budget Burn Speedometer (240-degree circular arc gauge)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = burnColor.copy(alpha = 0.25f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0A0F1B))
                    .border(1.2.dp, burnColor.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(14.dp)
                    .testTag("budget_burn_speedometer_card"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BUDGET BURN SPEEDOMETER",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = burnColor,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier.size(132.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        val diameter = size.minDimension - stroke * 1.6f
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = Size(diameter, diameter)

                        // 240-degree Track Arc
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Elapsed Month Days Marker Tick on 240-deg arc
                        val elapsedAngleDeg = 150f + (metrics.daysElapsedPercent / 100f) * 240f
                        val elapsedRad = elapsedAngleDeg * (PI / 180.0)
                        val r = diameter / 2f
                        val cx = size.width / 2f + (r * cos(elapsedRad)).toFloat()
                        val cy = size.height / 2f + (r * sin(elapsedRad)).toFloat()

                        // Glowing Consumed Arc
                        drawArc(
                            color = burnColor.copy(alpha = 0.28f),
                            startAngle = 150f,
                            sweepAngle = animatedBurnSweep.coerceIn(4f, 240f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke + 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = burnColor,
                            startAngle = 150f,
                            sweepAngle = animatedBurnSweep.coerceIn(4f, 240f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Elapsed day benchmark dot
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metrics.budgetConsumedPercent}%",
                            fontFamily = headingFont,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Budget Used",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            color = burnColor
                        )
                    }
                }

                Text(
                    text = "Day ${metrics.daysElapsed}/${metrics.daysInMonth} (${metrics.daysElapsedPercent}% month elapsed)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }

            // Right: Savings Efficiency Score (0-100 Radial Ring Meter)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = efficiencyColor.copy(alpha = 0.25f))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0A0F1B))
                    .border(1.2.dp, efficiencyColor.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(14.dp)
                    .testTag("savings_efficiency_score_card"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SAVINGS EFFICIENCY SCORE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = efficiencyColor,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier.size(132.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        val diameter = size.minDimension - stroke * 1.6f
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = Size(diameter, diameter)

                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = 120f,
                            sweepAngle = 300f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        drawArc(
                            color = efficiencyColor.copy(alpha = 0.28f),
                            startAngle = 120f,
                            sweepAngle = animatedEfficiencySweep.coerceIn(6f, 300f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke + 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = efficiencyColor,
                            startAngle = 120f,
                            sweepAngle = animatedEfficiencySweep.coerceIn(6f, 300f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metrics.savingsEfficiencyScore}",
                            fontFamily = headingFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "/ 100",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    color = efficiencyColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, efficiencyColor)
                ) {
                    Text(
                        text = metrics.savingsStatusLabel.uppercase(Locale.ENGLISH),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = efficiencyColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Linear Month-End Forecast Pill
        Surface(
            color = Color(0xFF0A141A),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, NeonEmeraldFood.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "At your current daily average of ₹$dailyAvgFormatted, you will save ₹$projectedSavingsFormatted by month-end",
                fontFamily = headingFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA7F3D0),
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("month_end_forecast_text")
            )
        }

        // Top 5 Money Drainers (Merchants) Racing Bar Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = NeonCrimsonShopping.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0B0F1A))
                .border(1.2.dp, Color(0xFF28354E), RoundedCornerShape(24.dp))
                .padding(18.dp)
                .testTag("top_5_money_drainers_card"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "4 · MERCHANT RACING LEADERBOARD",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCrimsonShopping
                    )
                    Text(
                        text = "Top 5 Money Drainers (Merchants)",
                        fontFamily = headingFont,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            if (topMerchants.isEmpty()) {
                Text(
                    text = "Log expenses to see which merchants or apps take the most money this month.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            } else {
                topMerchants.forEach { item ->
                    val animRatio by animateFloatAsState(
                        targetValue = item.barRatio,
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                        label = "merchantRaceBar_${item.rank}"
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF111827))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = item.accentColor.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    border = BorderStroke(0.8.dp, item.accentColor)
                                ) {
                                    Text(
                                        text = "#${item.rank}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = item.accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = item.merchantName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "• ${item.transactionCount}x",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Text(
                                text = if (stealthMode) "₹•••• (${item.shareOfMonthPercent}%)" else "₹${formatIndianAmount(item.totalAmount)} (${item.shareOfMonthPercent}%)",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = item.accentColor
                            )
                        }

                        // Animated Neon Racing Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animRatio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                item.accentColor.copy(alpha = 0.7f),
                                                item.accentColor
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
