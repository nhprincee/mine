package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseRecord
import com.example.data.model.NightGuardEventRecord
import com.example.data.model.NightGuardSchedule
import com.example.data.model.NightSpendingAnalytics
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.data.model.isSameDay
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.EmeraldChartFillBottom
import com.example.ui.theme.EmeraldChartFillTop
import com.example.ui.theme.EmeraldNeonGlow
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
import com.example.ui.theme.getCategoryColor
import com.example.ui.theme.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class CategoryBreakdownItem(
    val category: String,
    val amount: Long,
    val count: Int,
    val percent: Int,
    val color: Color
)

data class DaySpendPoint(
    val dayLabel: String,
    val spent: Long,
    val withinLimit: Boolean
)

@Composable
fun InsightsTab(
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    streakDays: Int,
    disciplineScore: Int,
    stealthMode: Boolean,
    settings: UserSettings = UserSettings(),
    nightGuardEvents: List<NightGuardEventRecord> = emptyList(),
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    var rangeDays by remember { mutableIntStateOf(7) }

    val now = System.currentTimeMillis()
    val cutoff = now - rangeDays * 86_400_000L

    val expenseRecordsInRange = remember(expenses, rangeDays) {
        expenses.filter { !isIncomeTransaction(it) && it.txEpochMillis >= cutoff }
    }
    val incomeRecordsInRange = remember(expenses, rangeDays) {
        expenses.filter { isIncomeTransaction(it) && it.txEpochMillis >= cutoff }
    }

    val totalSpent = remember(expenseRecordsInRange) { expenseRecordsInRange.sumOf { it.amount } }
    val totalIncome = remember(incomeRecordsInRange) { incomeRecordsInRange.sumOf { it.amount } }

    val categoryStats = remember(expenseRecordsInRange, totalSpent) {
        expenseRecordsInRange
            .groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val pct = if (totalSpent > 0L) ((sum.toDouble() / totalSpent.toDouble()) * 100.0).roundToInt() else 0
                CategoryBreakdownItem(
                    category = cat,
                    amount = sum,
                    count = list.size,
                    percent = pct,
                    color = getCategoryColor(cat)
                )
            }
            .sortedByDescending { it.amount }
    }

    val last7Days = remember(expenses, dailyLimit) {
        val dayFmt = SimpleDateFormat("EEE", Locale.ENGLISH)
        (6 downTo 0).map { daysAgo ->
            val targetTime = now - daysAgo * 86_400_000L
            val daySum = expenses
                .filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, targetTime) }
                .sumOf { it.amount }
            DaySpendPoint(
                dayLabel = dayFmt.format(Date(targetTime)),
                spent = daySum,
                withinLimit = daySum <= dailyLimit
            )
        }
    }

    val topCategory = categoryStats.firstOrNull()
    val roastVerdict = remember(topCategory, totalSpent, dailyLimit, rangeDays) {
        when {
            totalSpent == 0L -> "Zero spend logged! Either you achieved sovereign monk-level discipline or haven't logged your tapri chai yet."
            topCategory?.category == "Chai & Snacks" -> "☕ Tapri Advisory: '${topCategory.category}' leads your allocation at ${topCategory.percent}% (₹${topCategory.amount}). Bhai, chai ki tapri mein equity kharid li kya?"
            topCategory?.category == "Food" -> "🍛 Culinary Velocity Alert: ${topCategory.percent}% (₹${topCategory.amount}) went to Food! Ghar ka daal-chawal miss kar raha hai tujhe."
            totalSpent > dailyLimit * rangeDays -> "◆ Reserve Breach: Spent ₹$totalSpent in $rangeDays days vs ₹${dailyLimit * rangeDays} target. Time to tighten the daily runway."
            else -> "◆ Sovereign Control: ₹$totalSpent deployed across $rangeDays days — comfortably within your ₹${dailyLimit * rangeDays} runway."
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("insights_tab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Range Selector + Net Cashflow Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WEALTH ANALYTICS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.4.sp,
                            color = ChampagneGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Paisa Kahan Gaya?",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmIvory
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(7 to "7 Days", 30 to "30 Days").forEach { (days, label) ->
                            val active = rangeDays == days
                            Surface(
                                color = if (active) ChampagneGold else ElevatedGraphite,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .border(
                                        0.8.dp,
                                        if (active) ChampagneGold else HairlinePlatinum,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { rangeDays = days }
                                    .testTag("insights_range_${days}d")
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (active) ObsidianBlack else MutedPlatinum,
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InsightStatBox(
                        title = "${rangeDays}D OUTFLOW",
                        value = if (stealthMode) "₹0" else "₹$totalSpent",
                        color = WarmIvory,
                        modifier = Modifier.weight(1f)
                    )
                    InsightStatBox(
                        title = "${rangeDays}D INFLOW",
                        value = if (stealthMode) "₹0" else "₹$totalIncome",
                        color = SophisticatedEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    InsightStatBox(
                        title = "CRED SCORE",
                        value = "$disciplineScore · ${streakDays}d",
                        color = ChampagneGold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hinglish Roast & Advisory Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElevatedGraphite)
                        .border(0.8.dp, HairlineGold, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "◆ PRIVATE WEALTH VERDICT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = ChampagneGold
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = roastVerdict,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmIvory,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Luxury 7-Day Daily Velocity Curve + Bar Chart
        item {
            val maxBar = max(dailyLimit, last7Days.maxOfOrNull { it.spent } ?: dailyLimit).coerceAtLeast(100L)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7-DAY CAPITAL VELOCITY",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.2.sp,
                            color = MutedPlatinum
                        )
                        Text(
                            text = "Daily Outflow vs Reserve Limit",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmIvory
                        )
                    }
                    Surface(
                        color = ElevatedGraphite,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(0.8.dp, HairlineGold, RoundedCornerShape(50))
                    ) {
                        Text(
                            text = "Target: ₹$dailyLimit/d",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChampagneGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Smooth Trend Line + Threshold Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 26.dp, top = 16.dp)) {
                        val w = size.width
                        val h = size.height
                        val limitY = h * (1f - (dailyLimit.toFloat() / maxBar.toFloat()).coerceIn(0f, 1f))
                        drawLine(
                            color = ChampagneGold.copy(alpha = 0.35f),
                            start = Offset(0f, limitY),
                            end = Offset(w, limitY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )

                        if (last7Days.size > 1) {
                            val stepX = w / last7Days.size
                            val points = last7Days.mapIndexed { idx, pt ->
                                val ratio = (pt.spent.toFloat() / maxBar.toFloat()).coerceIn(0f, 1f)
                                Offset(
                                    x = stepX * idx + stepX * 0.5f,
                                    y = h * (1f - ratio)
                                )
                            }
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val midX = (prev.x + curr.x) / 2f
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }
                            val areaPath = Path().apply {
                                addPath(linePath)
                                lineTo(points.last().x, h)
                                lineTo(points.first().x, h)
                                close()
                            }
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(EmeraldChartFillTop, EmeraldChartFillBottom),
                                    startY = 0f,
                                    endY = h
                                )
                            )
                            drawPath(
                                path = linePath,
                                brush = Brush.horizontalGradient(
                                    listOf(SophisticatedEmerald, EmeraldNeonGlow, ChampagneGold)
                                ),
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            points.forEachIndexed { idx, ptOffset ->
                                val nodeColor = if (last7Days[idx].withinLimit) ChampagneGold else CrimsonRuby
                                drawCircle(
                                    color = nodeColor.copy(alpha = 0.28f),
                                    radius = 6.dp.toPx(),
                                    center = ptOffset
                                )
                                drawCircle(
                                    color = nodeColor,
                                    radius = 3.dp.toPx(),
                                    center = ptOffset
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        last7Days.forEach { pt ->
                            val ratio = min(1f, pt.spent.toFloat() / maxBar.toFloat()).coerceAtLeast(0.06f)
                            val barBrush = if (pt.withinLimit) {
                                Brush.verticalGradient(
                                    listOf(ChampagneGold, ChampagneGold.copy(alpha = 0.35f))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(CrimsonRuby, CrimsonRuby.copy(alpha = 0.4f))
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = if (stealthMode) "••" else "₹${pt.spent}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = MutedPlatinum
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .weight(1f, fill = false)
                                        .fillMaxHeight(ratio)
                                        .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                        .background(barBrush)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pt.dayLabel,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Breakdown Header
        item {
            Text(
                text = "PORTFOLIO CATEGORY BREAKDOWN (${categoryStats.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = MutedPlatinum
            )
        }

        if (categoryStats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                        .padding(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expense categories recorded in the last $rangeDays days.",
                        color = MutedIvory,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(categoryStats, key = { it.category }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElevatedGraphite)
                                .border(0.8.dp, item.color.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(item.category),
                                contentDescription = item.category,
                                tint = item.color,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.titleMedium,
                                color = WarmIvory,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${item.count} entries · ${item.percent}% allocation",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MutedIvory
                            )
                        }
                        Text(
                            text = if (stealthMode) "₹•••" else "₹${item.amount}",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = item.color
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (item.percent / 100f).coerceIn(0.02f, 1f) },
                        color = item.color,
                        trackColor = ElevatedGraphite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }

        // Dedicated Night Spending Insights Section
        item {
            val nightSummary = remember(expenses, nightGuardEvents, settings) {
                NightSpendingAnalytics.computeSummary(
                    expenses = expenses,
                    nightGuardEvents = nightGuardEvents,
                    settings = settings
                )
            }
            val windowLabel = NightGuardSchedule.formatTimeWindow(
                settings.nightGuardStartHour,
                settings.nightGuardStartMinute,
                settings.nightGuardEndHour,
                settings.nightGuardEndMinute
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .testTag("night_spending_insights_section"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NIGHT SPENDING INSIGHTS · RUKO ZARA",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = SophisticatedEmerald
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Late-Night Window ($windowLabel)",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmIvory
                        )
                    }

                    Surface(
                        color = SophisticatedEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.45f), RoundedCornerShape(50))
                    ) {
                        Text(
                            text = if (settings.nightGuardEnabled) "GUARD ARMED" else "GUARD OFF",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SophisticatedEmerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Primary Night Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InsightStatBox(
                        title = "NIGHT SPENT (ALL)",
                        value = if (stealthMode) "₹•••" else "₹${nightSummary.totalNightSpentAllTime}",
                        color = WarmIvory,
                        modifier = Modifier.weight(1f)
                    )
                    InsightStatBox(
                        title = "SAVED (CANCELLED)",
                        value = if (stealthMode) "₹•••" else "₹${nightSummary.savedFromCancelledAmount}",
                        color = SophisticatedEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Intervention Counts Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InsightStatBox(
                        title = "CANCELLED",
                        value = "${nightSummary.cancelledCount} purchases",
                        color = SophisticatedEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    InsightStatBox(
                        title = "POSTPONED",
                        value = "${nightSummary.postponedCount} purchases",
                        color = ChampagneGold,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Weekly & Monthly Night-Spending Trends
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "WEEKLY & MONTHLY NIGHT-SPENDING TRENDS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChampagneGold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last 7 Days Night Spend", style = MaterialTheme.typography.bodySmall, color = MutedPlatinum)
                        Text(
                            text = if (stealthMode) "₹•••" else "₹${nightSummary.nightSpentLast7Days} (${if (nightSummary.weeklyTrendPercent > 0) "+${nightSummary.weeklyTrendPercent}%" else "${nightSummary.weeklyTrendPercent}%"} vs prev 7d)",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (nightSummary.weeklyTrendPercent <= 0) SophisticatedEmerald else CrimsonRuby
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last 30 Days Night Spend", style = MaterialTheme.typography.bodySmall, color = MutedPlatinum)
                        Text(
                            text = if (stealthMode) "₹•••" else "₹${nightSummary.nightSpentLast30Days} (${nightSummary.nightTxCountLast30Days} txns)",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = WarmIvory
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightStatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ElevatedGraphite)
            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = MutedIvory
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = color
        )
    }
}
