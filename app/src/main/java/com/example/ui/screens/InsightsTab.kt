package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetStatusLevel
import com.example.data.model.CompleteInsightAnalyticsBundle
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.data.model.InsightCategoryDetail
import com.example.data.model.InsightIntelligenceEngine
import com.example.data.model.InsightPeriodFilter
import com.example.data.model.InsightSectionView
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.NightGuardEventRecord
import com.example.data.model.NightGuardSchedule
import com.example.data.model.NightSpendingAnalytics
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.TrendBarPoint
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.ui.components.KharcheeCompactHeaderBranding
import com.example.ui.components.KharcheeSymbolLogo
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
import com.example.ui.theme.getCategoryColor
import com.example.ui.theme.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
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

// Distinct high-contrast palette for multi-category Donut & Legend (matching reference image)
private val InsightDonutPalette = listOf(
    Color(0xFF3B82F6), // Electric Blue
    Color(0xFFF43F5E), // Vibrant Rose Pink
    Color(0xFFF59E0B), // Warm Amber Orange
    Color(0xFFA855F7), // Royal Purple
    Color(0xFF10B981), // Emerald Green
    Color(0xFF06B6D4), // Cyan Highlight
    Color(0xFFEC4899), // Magenta
    Color(0xFF8B5CF6)  // Deep Violet
)

private fun resolveInsightCategoryColor(category: String, index: Int): Color {
    return when (category.lowercase(Locale.ENGLISH)) {
        "food" -> Color(0xFF3B82F6)
        "entertainment" -> Color(0xFFF43F5E)
        "bills", "utilities" -> Color(0xFFF97316)
        "transport" -> Color(0xFFFACC15)
        "shopping" -> Color(0xFFA855F7)
        "health" -> Color(0xFF10B981)
        "education" -> Color(0xFF06B6D4)
        "travel" -> Color(0xFFEC4899)
        else -> InsightDonutPalette[index % InsightDonutPalette.size]
    }
}

@Composable
fun InsightsTab(
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    streakDays: Int,
    disciplineScore: Int,
    stealthMode: Boolean,
    settings: UserSettings = UserSettings(),
    nightGuardEvents: List<NightGuardEventRecord> = emptyList(),
    savingsGoals: List<SavingsGoalRecord> = emptyList(),
    paymentReminders: List<PaymentReminderRecord> = emptyList(),
    userDisplayName: String = "Kharchee",
    onOpenJarvisWithPrompt: ((String) -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val userIdKey = settings.userId.ifBlank { "default" }

    var selectedPeriodFilter by remember { mutableStateOf(InsightPeriodFilter.THIS_MONTH) }
    var activeSection by remember { mutableStateOf(InsightSectionView.OVERVIEW) }
    var showFilterBar by remember { mutableStateOf(true) }

    val now = System.currentTimeMillis()
    var customStartMillis by remember { mutableLongStateOf(now - 29L * 86_400_000L) }
    var customEndMillis by remember { mutableLongStateOf(now) }
    var showCustomDateDialog by remember { mutableStateOf(false) }

    var categoryBudgets by remember(userIdKey) {
        mutableStateOf(KharcheeProfileAndToolsStore.getCategoryBudgets(context, userIdKey))
    }
    var showBudgetEditor by remember { mutableStateOf(false) }
    var editBudgetCategory by remember { mutableStateOf(EXPENSE_CATEGORIES.first()) }
    var editBudgetAmountInput by remember { mutableStateOf("3000") }

    // Interactive chart selection states
    var selectedDailyPointIndex by remember { mutableIntStateOf(3) } // Default to Thurs / mid-week like reference image
    var selectedDonutCategory by remember { mutableStateOf<String?>(null) }
    var inspectingCategoryDetail by remember { mutableStateOf<InsightCategoryDetail?>(null) }
    var showReportPreviewDialog by remember { mutableStateOf(false) }
    var selectedEarningsBarIndex by remember { mutableIntStateOf(-1) }
    var selectedSpendingWeeklyIndex by remember { mutableIntStateOf(-1) }
    var selectedSpendingMonthlyIndex by remember { mutableIntStateOf(-1) }

    val effectiveSettings = remember(settings, dailyLimit) {
        if (settings.dailyLimit != dailyLimit && dailyLimit > 0L) {
            settings.copy(dailyLimit = dailyLimit)
        } else {
            settings
        }
    }

    val bundle = remember(
        expenses,
        effectiveSettings,
        categoryBudgets,
        savingsGoals,
        paymentReminders,
        selectedPeriodFilter,
        customStartMillis,
        customEndMillis
    ) {
        InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = expenses,
            settings = effectiveSettings,
            categoryBudgets = categoryBudgets,
            savingsGoals = savingsGoals,
            paymentReminders = paymentReminders,
            filter = selectedPeriodFilter,
            nowMillis = System.currentTimeMillis(),
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis
        )
    }

    val rangeDays = bundle.timeWindow.durationDays
    val totalSpent = bundle.overview.totalExpenses
    val totalIncome = bundle.overview.totalIncome

    val categoryStats = remember(bundle.categories) {
        bundle.categories.categories.mapIndexed { idx, detail ->
            CategoryBreakdownItem(
                category = detail.category,
                amount = detail.amount,
                count = detail.transactionCount,
                percent = detail.percentOfTotal,
                color = resolveInsightCategoryColor(detail.category, idx)
            )
        }
    }

    val greeting = remember { resolveTimeOfDayGreeting() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0719),
                        MaiDeepCanvas,
                        Color(0xFF06040C)
                    )
                )
            )
            .testTag("insights_tab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ====================================================================
        // 1. INSIGHT HOME — TOP HEADER + GREETING + PERIOD FILTER + SECTION NAV
        // ====================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = MaiPrimaryPurpleStart)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A1136),
                                MaiCardSurface,
                                Color(0xFF0E091E)
                            )
                        )
                    )
                    .border(1.dp, MaiCardBorderBright.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
                    .testTag("insight_top_header"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Brand + Greeting + Profile & Notification Icons + Report Export
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KharcheeCompactHeaderBranding(sectionLabel = "INSIGHT")
                            Text(
                                text = "$greeting ${userDisplayName.ifBlank { "Kharchee" }}",
                                fontSize = 11.sp,
                                color = MaiLavenderSubtext,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Analytics Overview",
                            fontFamily = headingFont,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${bundle.timeWindow.filter.label} · ${bundle.timeWindow.displayRangeLabel}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFC4B5FD)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Filter toggle icon button (matching reference top-right filter button)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaiCardSurfaceElevated)
                                .border(1.dp, MaiCardBorder, RoundedCornerShape(12.dp))
                                .clickable { showFilterBar = !showFilterBar }
                                .testTag("insight_filter_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Toggle Period Filters",
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (onOpenNotifications != null) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaiCardSurfaceElevated)
                                    .border(1.dp, MaiCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { onOpenNotifications() }
                                    .testTag("insight_notifications_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFFE9D5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (onOpenProfile != null) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaiCardSurfaceElevated)
                                    .border(1.dp, MaiCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { onOpenProfile() }
                                    .testTag("insight_profile_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFFE9D5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Export / View Report Button (preserves existing testTag)
                        Surface(
                            color = Color(0xFF231645),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, MaiPositiveEmerald.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .clickable {
                                    val reportText = InsightIntelligenceEngine.generateStructuredReportText(bundle)
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "KHARCHEE Financial Intelligence Report (${bundle.timeWindow.displayRangeLabel})"
                                            )
                                            putExtra(Intent.EXTRA_TEXT, reportText)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Export / Share Financial Report")
                                        )
                                    } catch (_: Exception) {
                                    }
                                    showReportPreviewDialog = true
                                }
                                .testTag("export_analytics_report_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export Analytics Report",
                                    tint = MaiPositiveEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Report",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaiPositiveEmerald
                                )
                            }
                        }
                    }
                }

                // Global Date Period Filter Strip (This Week, This Month, Last Month, Last 3 Months, This Year, Custom)
                if (showFilterBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("insight_period_filter_strip"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InsightPeriodFilter.entries.forEach { period ->
                            val isSelected = selectedPeriodFilter == period
                            // Preserve legacy testTags insights_range_7d and insights_range_30d for compatibility
                            val tag = when (period) {
                                InsightPeriodFilter.THIS_WEEK -> "insights_range_7d"
                                InsightPeriodFilter.THIS_MONTH -> "insights_range_30d"
                                else -> "insight_period_${period.id}"
                            }
                            Surface(
                                color = if (isSelected) MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFC084FC) else MaiCardBorder
                                ),
                                modifier = Modifier
                                    .clickable {
                                        selectedPeriodFilter = period
                                        if (period == InsightPeriodFilter.CUSTOM) {
                                            showCustomDateDialog = true
                                        }
                                    }
                                    .testTag(tag)
                            ) {
                                Text(
                                    text = period.label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaiLavenderSubtext,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Organized Analytical Section Selector Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("insight_section_selector_strip"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InsightSectionView.entries.forEach { section ->
                        val active = activeSection == section
                        Surface(
                            color = if (active) Color(0xFF3B1F7A) else Color(0xFF140D29),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (active) Color(0xFFA855F7) else Color(0xFF281B4D)
                            ),
                            modifier = Modifier
                                .clickable { activeSection = section }
                                .testTag("insight_section_${section.id}")
                        ) {
                            Text(
                                text = section.label,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (active) Color.White else MaiLavenderSubtext,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // ====================================================================
        // 2. MAIN BALANCE & FINANCIAL OVERVIEW SUMMARY CARD (MATCHES RIGHT SCREEN OF REFERENCE)
        // ====================================================================
        if (activeSection == InsightSectionView.OVERVIEW ||
            activeSection == InsightSectionView.EARNINGS ||
            activeSection == InsightSectionView.SPENDING ||
            activeSection == InsightSectionView.SAVINGS
        ) {
            item {
                InsightOverviewGlassCard(
                    bundle = bundle,
                    stealthMode = stealthMode,
                    onSelectSection = { activeSection = it }
                )
            }
        }

        // ====================================================================
        // 3. ANALYTICS OVERVIEW: MULTI-SEGMENT DONUT + INTERACTIVE SPLINE CHART WITH FLOATING TOOLTIP
        //    (MATCHES LEFT SCREEN OF UPLOADED REFERENCE IMAGE)
        // ====================================================================
        if (activeSection == InsightSectionView.OVERVIEW ||
            activeSection == InsightSectionView.SPENDING ||
            activeSection == InsightSectionView.CATEGORIES
        ) {
            item {
                InsightReferenceDonutAndSplineCard(
                    bundle = bundle,
                    categoryStats = categoryStats,
                    stealthMode = stealthMode,
                    selectedDonutCategory = selectedDonutCategory,
                    onSelectDonutCategory = { clickedCat ->
                        selectedDonutCategory = if (selectedDonutCategory == clickedCat) null else clickedCat
                        val detail = bundle.categories.categories.find { it.category == clickedCat }
                        if (detail != null) {
                            inspectingCategoryDetail = detail
                        }
                    },
                    selectedDailyIndex = selectedDailyPointIndex,
                    onSelectDailyIndex = { selectedDailyPointIndex = it }
                )
            }
        }

        // ====================================================================
        // 4. AI-POWERED JARVIS FINANCIAL INTELLIGENCE CARD (MATCHES REFERENCE GLASS CARD)
        // ====================================================================
        if (activeSection == InsightSectionView.OVERVIEW ||
            activeSection == InsightSectionView.SPENDING ||
            activeSection == InsightSectionView.BUDGET
        ) {
            item {
                InsightJarvisIntelligenceCard(
                    insights = bundle.jarvisInsights,
                    periodLabel = bundle.timeWindow.displayRangeLabel,
                    onAskJarvis = { prompt ->
                        if (onOpenJarvisWithPrompt != null) {
                            onOpenJarvisWithPrompt(prompt)
                        } else {
                            onShowToast("JARVIS Insight: $prompt")
                        }
                    }
                )
            }
        }

        // ====================================================================
        // 5. DEDICATED EARNINGS INSIGHTS VIEW (#4)
        // ====================================================================
        if (activeSection == InsightSectionView.EARNINGS || activeSection == InsightSectionView.OVERVIEW) {
            item {
                EarningsInsightsSectionCard(
                    bundle = bundle,
                    stealthMode = stealthMode,
                    selectedBarIndex = selectedEarningsBarIndex,
                    onSelectBarIndex = { selectedEarningsBarIndex = it }
                )
            }
        }

        // ====================================================================
        // 6. DEDICATED SPENDING INSIGHTS VIEW (#5)
        // ====================================================================
        if (activeSection == InsightSectionView.SPENDING || activeSection == InsightSectionView.OVERVIEW) {
            item {
                SpendingInsightsDetailedCard(
                    bundle = bundle,
                    dailyLimit = dailyLimit,
                    stealthMode = stealthMode,
                    selectedWeeklyIndex = selectedSpendingWeeklyIndex,
                    onSelectWeeklyIndex = { selectedSpendingWeeklyIndex = it },
                    selectedMonthlyIndex = selectedSpendingMonthlyIndex,
                    onSelectMonthlyIndex = { selectedSpendingMonthlyIndex = it }
                )
            }
        }

        // ====================================================================
        // 7. CATEGORY BREAKDOWN & INTERACTIVE INSPECTOR (#6)
        // ====================================================================
        if (activeSection == InsightSectionView.CATEGORIES || activeSection == InsightSectionView.OVERVIEW) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PORTFOLIO CATEGORY BREAKDOWN (${categoryStats.size})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = Color(0xFFD8B4FE)
                    )
                    bundle.categories.topCategory?.let { top ->
                        Text(
                            text = "Top: ${top.category} (${top.percentOfTotal}%)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = MaiPositiveEmerald
                        )
                    }
                }
            }

            if (bundle.categories.categories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaiCardSurface)
                            .border(1.dp, MaiCardBorder, RoundedCornerShape(18.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No expense categories recorded in ${bundle.timeWindow.filter.label} (${rangeDays}d).",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Add an expense or select another period filter above to inspect category trends.",
                                color = MaiLavenderSubtext,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(bundle.categories.categories, key = { it.category }) { detail ->
                    val colorIndex = bundle.categories.categories.indexOf(detail).coerceAtLeast(0)
                    val itemColor = resolveInsightCategoryColor(detail.category, colorIndex)
                    val changeText = detail.changePercent?.let { pct ->
                        if (pct > 0) "+$pct% vs prior" else if (pct < 0) "$pct% vs prior" else "0% vs prior"
                    } ?: "New in period"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaiCardSurface)
                            .border(1.dp, MaiCardBorder, RoundedCornerShape(18.dp))
                            .clickable { inspectingCategoryDetail = detail }
                            .padding(16.dp)
                            .testTag("insight_category_card_${detail.category}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(itemColor.copy(alpha = 0.16f))
                                    .border(1.dp, itemColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(detail.category),
                                    contentDescription = detail.category,
                                    tint = itemColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = detail.category,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Surface(
                                        color = Color(0xFF221642),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = changeText,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            color = Color(0xFFD8B4FE),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${detail.transactionCount} entries · ${detail.percentOfTotal}% of spending · Tap for transactions",
                                    fontSize = 11.sp,
                                    color = MaiLavenderSubtext
                                )
                            }
                            Text(
                                text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(detail.amount)}",
                                fontFamily = headingFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = itemColor
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (detail.percentOfTotal / 100f).coerceIn(0.03f, 1f) },
                            color = itemColor,
                            trackColor = Color(0xFF1E1438),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }

        // ====================================================================
        // 8. BUDGET INTELLIGENCE & SMART BUDGET COACH (#7)
        // ====================================================================
        if (activeSection == InsightSectionView.BUDGET || activeSection == InsightSectionView.OVERVIEW) {
            item {
                BudgetIntelligenceSectionCard(
                    bundle = bundle,
                    totalSpent = totalSpent,
                    rangeDays = rangeDays,
                    dailyLimit = dailyLimit,
                    stealthMode = stealthMode,
                    categoryBudgets = categoryBudgets,
                    showBudgetEditor = showBudgetEditor,
                    onToggleBudgetEditor = { showBudgetEditor = !showBudgetEditor },
                    editBudgetCategory = editBudgetCategory,
                    onSelectEditCategory = { cat ->
                        editBudgetCategory = cat
                        editBudgetAmountInput = (categoryBudgets[cat] ?: 3000L).toString()
                    },
                    editBudgetAmountInput = editBudgetAmountInput,
                    onEditBudgetAmountChange = { editBudgetAmountInput = it.filter { ch -> ch.isDigit() } },
                    onSaveCategoryBudget = {
                        val newLimit = editBudgetAmountInput.toLongOrNull() ?: 3000L
                        categoryBudgets = KharcheeProfileAndToolsStore.saveCategoryBudget(
                            context,
                            userIdKey,
                            editBudgetCategory,
                            newLimit
                        )
                        showBudgetEditor = false
                        onShowToast("Updated $editBudgetCategory monthly budget to ₹$newLimit")
                    }
                )
            }
        }

        // ====================================================================
        // 9. METALLIC SILVER FINANCIAL HEALTH SCORE SECTION (#8)
        // ====================================================================
        if (activeSection == InsightSectionView.OVERVIEW ||
            activeSection == InsightSectionView.BUDGET ||
            activeSection == InsightSectionView.SAVINGS
        ) {
            item {
                InsightMetallicHealthScoreSection(
                    evaluation = bundle.healthScore
                )
            }
        }

        // ====================================================================
        // 10. SAVINGS ANALYTICS SECTION (#9)
        // ====================================================================
        if (activeSection == InsightSectionView.SAVINGS || activeSection == InsightSectionView.OVERVIEW) {
            item {
                SavingsAnalyticsSectionCard(
                    savings = bundle.savings,
                    savingsGoals = savingsGoals,
                    stealthMode = stealthMode
                )
            }
        }

        // ====================================================================
        // 11. FINANCIAL REPORTS & EXPORT SECTION (#13)
        // ====================================================================
        if (activeSection == InsightSectionView.REPORTS || activeSection == InsightSectionView.OVERVIEW) {
            item {
                FinancialReportsActionCard(
                    bundle = bundle,
                    onOpenReportPreview = { showReportPreviewDialog = true },
                    onExportCsv = {
                        val csv = InsightIntelligenceEngine.generateStructuredReportCsv(bundle)
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "KHARCHEE_Insight_Report_${bundle.timeWindow.filter.id}.csv"
                                )
                                putExtra(Intent.EXTRA_TEXT, csv)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export Insight CSV"))
                        } catch (_: Exception) {
                        }
                        onShowToast("Prepared CSV report for ${bundle.timeWindow.displayRangeLabel}")
                    },
                    onShareSummary = {
                        val text = InsightIntelligenceEngine.generateStructuredReportText(bundle)
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "KHARCHEE Financial Report (${bundle.timeWindow.displayRangeLabel})"
                                )
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Financial Report"))
                        } catch (_: Exception) {
                        }
                    }
                )
            }
        }

        // ====================================================================
        // 12. PRESERVED EXISTING FEATURES (NIGHT SPENDING, WHERE DID MY MONEY GO, ASK YOUR MONEY, WRAPPED)
        // ====================================================================
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
                    .background(MaiCardSurface)
                    .border(1.dp, MaiCardBorder, RoundedCornerShape(24.dp))
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
                            color = Color(0xFFC084FC)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Late-Night Window ($windowLabel)",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = MaiPrimaryPurpleEnd.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, Color(0xFFA855F7))
                    ) {
                        Text(
                            text = if (settings.nightGuardEnabled) "GUARD ARMED" else "GUARD OFF",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE9D5FF),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InsightStatBox(
                        title = "NIGHT SPENT (ALL)",
                        value = if (stealthMode) "₹•••" else "₹${nightSummary.totalNightSpentAllTime}",
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    InsightStatBox(
                        title = "SAVED (CANCELLED)",
                        value = if (stealthMode) "₹•••" else "₹${nightSummary.savedFromCancelledAmount}",
                        color = MaiPositiveEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InsightStatBox(
                        title = "CANCELLED",
                        value = "${nightSummary.cancelledCount} purchases",
                        color = MaiPositiveEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    InsightStatBox(
                        title = "POSTPONED",
                        value = "${nightSummary.postponedCount} purchases",
                        color = Color(0xFFD8B4FE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            val subs = remember(userIdKey) { KharcheeProfileAndToolsStore.getSubscriptions(context, userIdKey) }
            WhereDidMyMoneyGoCardSection(
                userId = userIdKey,
                expenses = expenses,
                subscriptions = subs,
                onShowToast = onShowToast
            )
        }

        item {
            val subs = remember(userIdKey) { KharcheeProfileAndToolsStore.getSubscriptions(context, userIdKey) }
            val askFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFA855F7),
                unfocusedBorderColor = MaiCardBorder,
                focusedContainerColor = MaiDeepCanvas,
                unfocusedContainerColor = MaiDeepCanvas,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFD8B4FE),
                unfocusedLabelColor = MaiLavenderSubtext
            )
            AskYourMoneyCardSection(
                userId = userIdKey,
                expenses = expenses,
                subscriptions = subs,
                savingsGoals = savingsGoals,
                fieldColors = askFieldColors
            )
        }

        item {
            KharcheeWrappedCardSection(
                expenses = expenses,
                savingsGoals = savingsGoals,
                streakDays = streakDays
            )
        }
    }

    // ========================================================================
    // INTERACTIVE DIALOGS: CATEGORY TRANSACTIONS INSPECTOR, CUSTOM DATE RANGE, REPORT PREVIEW
    // ========================================================================
    inspectingCategoryDetail?.let { catDetail ->
        CategoryTransactionsInspectorDialog(
            detail = catDetail,
            periodLabel = bundle.timeWindow.displayRangeLabel,
            stealthMode = stealthMode,
            onDismiss = { inspectingCategoryDetail = null }
        )
    }

    if (showCustomDateDialog) {
        CustomDateRangePickerDialog(
            initialStartMillis = customStartMillis,
            initialEndMillis = customEndMillis,
            onApply = { start, end ->
                customStartMillis = start
                customEndMillis = end
                selectedPeriodFilter = InsightPeriodFilter.CUSTOM
                showCustomDateDialog = false
            },
            onDismiss = { showCustomDateDialog = false }
        )
    }

    if (showReportPreviewDialog) {
        FinancialReportPreviewDialog(
            bundle = bundle,
            onDismiss = { showReportPreviewDialog = false }
        )
    }
}

// ============================================================================
// COMPONENT 1: MAIN BALANCE & CASHFLOW OVERVIEW GLASS CARD (MATCHES RIGHT SCREEN OF REFERENCE)
// ============================================================================
@Composable
private fun InsightOverviewGlassCard(
    bundle: CompleteInsightAnalyticsBundle,
    stealthMode: Boolean,
    onSelectSection: (InsightSectionView) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val ov = bundle.overview
    val netBalance = ov.totalIncome - ov.totalExpenses

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = MaiPrimaryPurpleStart)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1F163D),
                        Color(0xFF130D26),
                        Color(0xFF191130)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color(0xFF8B5CF6).copy(alpha = 0.55f),
                        Color(0xFF31215C)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
            .testTag("insight_main_balance_summary_card"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Net Period Cashflow (${bundle.timeWindow.filter.label})",
                    fontSize = 12.sp,
                    color = MaiLavenderSubtext,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (stealthMode) {
                        "₹••••••"
                    } else {
                        "${if (netBalance < 0L) "- " else ""}₹${formatIndianAmount(abs(netBalance))}"
                    },
                    fontFamily = headingFont,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Surface(
                color = Color(0xFF281A4D),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color(0xFFA855F7))
            ) {
                Text(
                    text = if (ov.hasRecordedIncome) {
                        "Savings Rate: ${ov.savingsRatePercent}%"
                    } else {
                        "${ov.transactionCount} Txns"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE9D5FF),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Income & Expenses Split Row (matching reference card layout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Income Pill
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF140E28))
                    .border(0.8.dp, MaiPositiveEmerald.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { onSelectSection(InsightSectionView.EARNINGS) }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaiPositiveEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Income",
                            tint = MaiPositiveEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "Income",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
                    )
                }
                Text(
                    text = if (stealthMode) "₹••••" else "+₹${formatIndianAmount(ov.totalIncome)}",
                    fontFamily = headingFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaiPositiveEmerald
                )
                Text(
                    text = ov.incomeChangePercent?.let {
                        "${if (it >= 0) "+$it%" else "$it%"} ${bundle.timeWindow.comparisonLabel}"
                    } ?: "Tap for earnings view",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext
                )
            }

            // Expenses Pill
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF140E28))
                    .border(0.8.dp, MaiExpenseRose.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { onSelectSection(InsightSectionView.SPENDING) }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaiExpenseRose.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Expenses",
                            tint = MaiExpenseRose,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "Expenses",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
                    )
                }
                Text(
                    text = if (stealthMode) "₹••••" else "-₹${formatIndianAmount(ov.totalExpenses)}",
                    fontFamily = headingFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaiExpenseRose
                )
                Text(
                    text = ov.expenseChangePercent?.let {
                        "${if (it >= 0) "+$it%" else "$it%"} ${bundle.timeWindow.comparisonLabel}"
                    } ?: "Tap for spending view",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext
                )
            }
        }

        // Secondary Row: Verified Savings, Monthly Spending, MoM Comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightStatBox(
                title = "VERIFIED SAVINGS",
                value = if (stealthMode) {
                    "₹••••"
                } else if (!ov.hasRecordedIncome) {
                    "₹0 (Log Income)"
                } else {
                    "₹${formatIndianAmount(ov.verifiedSavings)}"
                },
                color = if (ov.verifiedSavings >= 0L) MaiPositiveEmerald else MaiExpenseRose,
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "THIS MONTH SPEND",
                value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(ov.monthlySpending)}",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "MoM CHANGE",
                value = ov.monthOverMonthExpenseChangePercent?.let {
                    if (it > 0) "+$it%" else "$it%"
                } ?: "0%",
                color = Color(0xFFD8B4FE),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================================
// COMPONENT 2: ANALYTICS OVERVIEW DONUT + SMOOTH SPLINE CHART WITH FLOATING TOOLTIP
//              (MATCHES LEFT SCREEN OF UPLOADED REFERENCE IMAGE)
// ============================================================================
@Composable
private fun InsightReferenceDonutAndSplineCard(
    bundle: CompleteInsightAnalyticsBundle,
    categoryStats: List<CategoryBreakdownItem>,
    stealthMode: Boolean,
    selectedDonutCategory: String?,
    onSelectDonutCategory: (String) -> Unit,
    selectedDailyIndex: Int,
    onSelectDailyIndex: (Int) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val dailyPoints = bundle.spending.dailyTrend
    val safeSelectedIdx = selectedDailyIndex.coerceIn(0, max(0, dailyPoints.lastIndex))
    val activeDayPoint = dailyPoints.getOrNull(safeSelectedIdx)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(26.dp), spotColor = MaiPrimaryPurpleStart)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF150E2A),
                        Color(0xFF0E091D),
                        Color(0xFF120C24)
                    )
                )
            )
            .border(1.dp, MaiCardBorderBright.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
            .padding(20.dp)
            .testTag("insight_analytics_overview_card"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Total Spending Header (matches "Total Spending $1,460.30" in reference)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Total Spending",
                    fontSize = 12.sp,
                    color = MaiLavenderSubtext,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (stealthMode) "₹••••••" else "₹${formatIndianAmount(bundle.overview.totalExpenses)}",
                    fontFamily = headingFont,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Surface(
                color = Color(0xFF21153F),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, MaiCardBorderBright)
            ) {
                Text(
                    text = bundle.timeWindow.filter.label,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE9D5FF),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // Multi-Segment Donut Chart (Left) + Category Legend (Right) — matches reference image
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .testTag("insight_donut_chart"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    val strokeWidth = 26.dp.toPx()
                    if (categoryStats.isEmpty() || bundle.overview.totalExpenses <= 0L) {
                        drawArc(
                            color = Color(0xFF281B4A),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    } else {
                        var currentStartAngle = -90f
                        val total = max(1L, categoryStats.sumOf { it.amount }).toFloat()
                        categoryStats.forEach { item ->
                            val sweep = (item.amount.toFloat() / total) * 360f
                            val isHighlighted = selectedDonutCategory == item.category
                            drawArc(
                                color = item.color,
                                startAngle = currentStartAngle,
                                sweepAngle = max(4f, sweep - 2.5f),
                                useCenter = false,
                                style = Stroke(
                                    width = if (isHighlighted) strokeWidth + 6.dp.toPx() else strokeWidth,
                                    cap = StrokeCap.Butt
                                )
                            )
                            currentStartAngle += sweep
                        }
                    }
                }

                // Center Donut Text ("Sum expenses ₹...")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 18.dp)
                ) {
                    Text(
                        text = selectedDonutCategory ?: "Sum expenses",
                        fontSize = 10.sp,
                        color = MaiLavenderSubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val centerAmount = if (selectedDonutCategory != null) {
                        categoryStats.find { it.category == selectedDonutCategory }?.amount ?: bundle.overview.totalExpenses
                    } else {
                        bundle.overview.totalExpenses
                    }
                    Text(
                        text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(centerAmount)}",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Right Legend List (Food, Entertainment, Utilities, Transport, Shopping...)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (categoryStats.isEmpty()) {
                    listOf("Food", "Entertainment", "Bills", "Transport", "Shopping").forEachIndexed { idx, placeholderCat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(resolveInsightCategoryColor(placeholderCat, idx))
                            )
                            Text(
                                text = "$placeholderCat (0%)",
                                fontSize = 12.sp,
                                color = MaiLavenderSubtext
                            )
                        }
                    }
                } else {
                    categoryStats.take(6).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectDonutCategory(item.category) }
                                .padding(vertical = 3.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Text(
                                    text = item.category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${item.percent}%",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = item.color
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaiCardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        // Smooth Area/Spline Chart with Floating Purple Tooltip Pill ("₹ 15,000" over "Thurs")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-Day Spending Velocity (Tap a day to inspect)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaiLavenderSubtext
                )
                activeDayPoint?.let { pt ->
                    Surface(
                        color = Color(0xFF4C1D95),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFC084FC)),
                        modifier = Modifier.testTag("insight_spline_tooltip_pill")
                    ) {
                        Text(
                            text = if (stealthMode) "${pt.shortLabel}: ₹••••" else "${pt.shortLabel}: ₹${formatIndianAmount(pt.amount)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            val maxDaySpend = max(100L, dailyPoints.maxOfOrNull { it.amount } ?: 100L)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .pointerInput(dailyPoints) {
                        detectTapGestures { tapOffset ->
                            if (dailyPoints.isNotEmpty() && size.width > 0) {
                                val slotWidth = size.width.toFloat() / dailyPoints.size
                                val tappedIdx = (tapOffset.x / slotWidth).toInt().coerceIn(0, dailyPoints.lastIndex)
                                onSelectDailyIndex(tappedIdx)
                            }
                        }
                    }
                    .testTag("insight_interactive_spline_canvas")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp, bottom = 28.dp, start = 8.dp, end = 8.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    if (dailyPoints.size > 1) {
                        val stepX = w / (dailyPoints.size - 1).coerceAtLeast(1)
                        val offsets = dailyPoints.mapIndexed { idx, pt ->
                            val ratio = (pt.amount.toFloat() / maxDaySpend.toFloat()).coerceIn(0.06f, 1f)
                            Offset(
                                x = stepX * idx,
                                y = h * (1f - ratio)
                            )
                        }

                        val smoothPath = Path().apply {
                            moveTo(offsets.first().x, offsets.first().y)
                            for (i in 1 until offsets.size) {
                                val prev = offsets[i - 1]
                                val curr = offsets[i]
                                val controlX = (prev.x + curr.x) / 2f
                                cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(smoothPath)
                            lineTo(offsets.last().x, h)
                            lineTo(offsets.first().x, h)
                            close()
                        }

                        // Violet/Blue-Purple gradient area fill under spline (matching reference image)
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.60f),
                                    Color(0xFF6366F1).copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = h
                            )
                        )

                        drawPath(
                            path = smoothPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF818CF8),
                                    Color(0xFFA855F7),
                                    Color(0xFFD8B4FE)
                                )
                            ),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Highlight selected day point & vertical guide line
                        val selectedOffset = offsets[safeSelectedIdx]
                        drawLine(
                            color = Color(0xFFC084FC).copy(alpha = 0.5f),
                            start = Offset(selectedOffset.x, selectedOffset.y),
                            end = Offset(selectedOffset.x, h),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                        drawCircle(
                            color = Color(0xFFA855F7).copy(alpha = 0.35f),
                            radius = 9.dp.toPx(),
                            center = selectedOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.5.dp.toPx(),
                            center = selectedOffset
                        )
                    }
                }

                // Bottom Day Labels (Mon, Tue, Wed, Thurs, Fri, Sat, Sun)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dailyPoints.forEachIndexed { idx, pt ->
                        val isSelected = idx == safeSelectedIdx
                        Text(
                            text = pt.shortLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF60A5FA) else MaiLavenderSubtext,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectDailyIndex(idx) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT 3: AI-POWERED JARVIS FINANCIAL INTELLIGENCE CARD (#10)
// ============================================================================
@Composable
private fun InsightJarvisIntelligenceCard(
    insights: List<com.example.data.model.JarvisDataBackedInsight>,
    periodLabel: String,
    onAskJarvis: (String) -> Unit
) {
    var selectedIdx by remember(insights) { mutableIntStateOf(0) }
    val activeInsight = insights.getOrNull(selectedIdx.coerceIn(0, max(0, insights.lastIndex)))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF8B5CF6))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF211642),
                        Color(0xFF150E2D),
                        Color(0xFF1D123B)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color(0xFFA855F7),
                        Color(0xFF3B82F6).copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
            .testTag("insight_jarvis_intelligence_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.22f))
                        .border(0.8.dp, Color(0xFF60A5FA), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI-Powered Insights",
                        tint = Color(0xFF93C5FD),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = "AI-Powered Insights · JARVIS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = activeInsight?.periodLabel ?: periodLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFFC4B5FD)
                    )
                }
            }

            if (insights.size > 1) {
                Surface(
                    color = Color(0xFF2B1C54),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable {
                        selectedIdx = (selectedIdx + 1) % insights.size
                    }
                ) {
                    Text(
                        text = "${selectedIdx + 1}/${insights.size} Next →",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE9D5FF),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (activeInsight != null) {
            Text(
                text = activeInsight.headline,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE9D5FF)
            )
            Text(
                text = activeInsight.observation,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Text(
                text = "Why this appeared: ${activeInsight.whyItAppeared}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = MaiLavenderSubtext,
                lineHeight = 15.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = MaiPrimaryPurpleEnd,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFFC084FC)),
                    modifier = Modifier
                        .clickable { onAskJarvis(activeInsight.suggestedJarvisPrompt) }
                        .testTag("insight_open_jarvis_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Open in JARVIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT 4: EARNINGS INSIGHTS SECTION (#4)
// ============================================================================
@Composable
private fun EarningsInsightsSectionCard(
    bundle: CompleteInsightAnalyticsBundle,
    stealthMode: Boolean,
    selectedBarIndex: Int,
    onSelectBarIndex: (Int) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val earn = bundle.earnings
    var showWeekly by remember { mutableStateOf(false) }
    val activeTrend = if (showWeekly) earn.weeklyTrend else earn.monthlyTrend
    val maxAmt = max(100L, activeTrend.maxOfOrNull { it.amount } ?: 100L)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("insight_earnings_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EARNINGS INSIGHTS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaiPositiveEmerald
                )
                Text(
                    text = "Income Sources & Growth Trend",
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(false to "Monthly", true to "Weekly").forEach { (weekly, label) ->
                    val selected = showWeekly == weekly
                    Surface(
                        color = if (selected) MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable {
                            showWeekly = weekly
                            onSelectBarIndex(-1)
                        }
                    ) {
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaiLavenderSubtext,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightStatBox(
                title = "PERIOD INCOME",
                value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(earn.totalIncomeReceived)}",
                color = MaiPositiveEmerald,
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "GROWTH VS PRIOR",
                value = earn.incomeGrowthPercent?.let { if (it >= 0) "+$it%" else "$it%" } ?: "N/A",
                color = Color(0xFFD8B4FE),
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "HIGHEST PERIOD",
                value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(earn.highestEarningPeriodAmount)}",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        // Interactive Income Trend Bar Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0C0818))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            activeTrend.forEachIndexed { idx, pt ->
                val ratio = (pt.amount.toFloat() / maxAmt.toFloat()).coerceIn(0.06f, 1f)
                val isSelected = selectedBarIndex == idx
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelectBarIndex(if (isSelected) -1 else idx) }
                ) {
                    Text(
                        text = if (stealthMode) "••" else "₹${formatIndianAmount(pt.amount)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        color = if (isSelected) MaiPositiveEmerald else MaiLavenderSubtext,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .weight(1f, fill = false)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isSelected) {
                                        listOf(MaiPositiveEmerald, Color(0xFF059669))
                                    } else {
                                        listOf(Color(0xFF34D399), Color(0xFF065F46))
                                    }
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pt.shortLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaiLavenderSubtext
                    )
                }
            }
        }

        // Income Sources Breakdown
        if (earn.sourceBreakdown.isEmpty()) {
            Text(
                text = "No recorded income transactions in this period. Tap 'Add Income' on the Home screen to track salary, freelance, or business inflows.",
                fontSize = 11.sp,
                color = MaiLavenderSubtext
            )
        } else {
            Text(
                text = "INCOME SOURCE DISTRIBUTION",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaiLavenderSubtext
            )
            earn.sourceBreakdown.forEach { src ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${src.sourceName} (${src.count}x · ${src.percentOfIncome}%)",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (stealthMode) "₹••••" else "+₹${formatIndianAmount(src.amount)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaiPositiveEmerald
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT 5: SPENDING INSIGHTS DETAILED SECTION (#5)
// ============================================================================
@Composable
private fun SpendingInsightsDetailedCard(
    bundle: CompleteInsightAnalyticsBundle,
    dailyLimit: Long,
    stealthMode: Boolean,
    selectedWeeklyIndex: Int,
    onSelectWeeklyIndex: (Int) -> Unit,
    selectedMonthlyIndex: Int,
    onSelectMonthlyIndex: (Int) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val sp = bundle.spending
    var viewMonthly by remember { mutableStateOf(false) }
    val bars = if (viewMonthly) sp.monthlyTrend else sp.weeklyTrend
    val maxBar = max(100L, bars.maxOfOrNull { it.amount } ?: 100L)
    val selectedIdx = if (viewMonthly) selectedMonthlyIndex else selectedWeeklyIndex

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("insight_spending_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SPENDING INSIGHTS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaiExpenseRose
                )
                Text(
                    text = "Daily Average, Peak Day & Trends",
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(false to "Weekly", true to "Monthly").forEach { (isMonthly, label) ->
                    val active = viewMonthly == isMonthly
                    Surface(
                        color = if (active) MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { viewMonthly = isMonthly }
                    ) {
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else MaiLavenderSubtext,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightStatBox(
                title = "AVG DAILY SPEND",
                value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(sp.averageDailySpending)}/d",
                color = if (sp.averageDailySpending > dailyLimit) MaiExpenseRose else Color.White,
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "HIGHEST SPEND DAY",
                value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(sp.highestSpendingDayAmount)}",
                color = MaiWarningAmber,
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "VS LAST MONTH",
                value = sp.monthOverMonthChangePercent?.let { if (it >= 0) "+$it%" else "$it%" } ?: "0%",
                color = Color(0xFFD8B4FE),
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Peak Spending Day: ${sp.highestSpendingDayLabel}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            color = MaiLavenderSubtext
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0C0818))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEachIndexed { idx, pt ->
                val ratio = (pt.amount.toFloat() / maxBar.toFloat()).coerceIn(0.06f, 1f)
                val isSelected = selectedIdx == idx
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (viewMonthly) onSelectMonthlyIndex(idx) else onSelectWeeklyIndex(idx)
                        }
                ) {
                    Text(
                        text = if (stealthMode) "••" else "₹${formatIndianAmount(pt.amount)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        color = if (isSelected) Color(0xFFE9D5FF) else MaiLavenderSubtext,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .weight(1f, fill = false)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isSelected) {
                                        listOf(Color(0xFFF43F5E), Color(0xFF8B5CF6))
                                    } else {
                                        listOf(Color(0xFFA855F7), Color(0xFF4C1D95))
                                    }
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pt.shortLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaiLavenderSubtext
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT 6: BUDGET INTELLIGENCE & SMART BUDGET COACH SECTION (#7)
// ============================================================================
@Composable
private fun BudgetIntelligenceSectionCard(
    bundle: CompleteInsightAnalyticsBundle,
    totalSpent: Long,
    rangeDays: Int,
    dailyLimit: Long,
    stealthMode: Boolean,
    categoryBudgets: Map<String, Long>,
    showBudgetEditor: Boolean,
    onToggleBudgetEditor: () -> Unit,
    editBudgetCategory: String,
    onSelectEditCategory: (String) -> Unit,
    editBudgetAmountInput: String,
    onEditBudgetAmountChange: (String) -> Unit,
    onSaveCategoryBudget: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val bgt = bundle.budget
    val dailyAvgBurn = if (rangeDays > 0) totalSpent / rangeDays else 0L
    val estimatedMonthEnd = max(bgt.totalSpentThisMonth, dailyAvgBurn * 30L)
    val isForecastOver = estimatedMonthEnd > bgt.totalMonthlyBudget

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("smart_budget_coach_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BUDGET INTELLIGENCE & SMART COACH",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFFC084FC)
                )
                Text(
                    text = bgt.performanceHeadline,
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Surface(
                color = MaiCardSurfaceElevated,
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color(0xFFA855F7)),
                modifier = Modifier
                    .clickable { onToggleBudgetEditor() }
                    .testTag("toggle_category_budget_editor_button")
            ) {
                Text(
                    text = if (showBudgetEditor) "Close" else "Adjust Budgets",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE9D5FF),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Circular Progress Gauge + Total Budget / Spent / Remaining Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 8.dp.toPx()
                    drawArc(
                        color = Color(0xFF231842),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    val sweep = (bgt.utilizationPercent.coerceIn(0, 100) / 100f) * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = if (bgt.utilizationPercent > 100) {
                                listOf(MaiWarningAmber, MaiExpenseRose, MaiWarningAmber)
                            } else {
                                listOf(Color(0xFF7C3AED), Color(0xFFC084FC), Color(0xFF3B82F6), Color(0xFF7C3AED))
                            }
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${bgt.utilizationPercent}%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monthly Budget", fontSize = 11.sp, color = MaiLavenderSubtext)
                    Text(
                        text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(bgt.totalMonthlyBudget)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Spent This Month", fontSize = 11.sp, color = MaiLavenderSubtext)
                    Text(
                        text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(bgt.totalSpentThisMonth)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bgt.utilizationPercent > 100) MaiExpenseRose else Color(0xFFD8B4FE)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining Budget", fontSize = 11.sp, color = MaiLavenderSubtext)
                    Text(
                        text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(bgt.remainingBudget)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaiPositiveEmerald
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. Month-End Forecast", fontSize = 11.sp, color = MaiLavenderSubtext)
                    Text(
                        text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(estimatedMonthEnd)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isForecastOver) MaiExpenseRose else MaiPositiveEmerald
                    )
                }
            }
        }

        Text(
            text = bgt.performanceExplanation,
            fontSize = 12.sp,
            color = MaiLavenderSubtext,
            lineHeight = 18.sp
        )

        if (showBudgetEditor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0614))
                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Adjust Category Monthly Limit:",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = Color(0xFFE9D5FF)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EXPENSE_CATEGORIES.take(4).forEach { cat ->
                        val active = editBudgetCategory == cat
                        Surface(
                            color = if (active) MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectEditCategory(cat) }
                        ) {
                            Text(
                                text = cat.substringBefore(" "),
                                fontSize = 10.sp,
                                color = if (active) Color.White else MaiLavenderSubtext,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = editBudgetAmountInput,
                        onValueChange = onEditBudgetAmountChange,
                        label = { Text("$editBudgetCategory Limit (₹)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA855F7),
                            unfocusedBorderColor = MaiCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onSaveCategoryBudget,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaiPositiveEmerald,
                            contentColor = Color(0xFF06120E)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Category-wise Budget Progress Bars
        bgt.categoryProgress.take(6).forEach { item ->
            val barColor = when (item.status) {
                BudgetStatusLevel.EXCEEDED_LIMIT -> MaiExpenseRose
                BudgetStatusLevel.APPROACHING_LIMIT -> MaiWarningAmber
                BudgetStatusLevel.WITHIN_LIMIT -> MaiPositiveEmerald
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = if (stealthMode) "₹••• / ₹•••" else "₹${formatIndianAmount(item.spentAmount)} / ₹${formatIndianAmount(item.budgetLimit)} (${item.utilizationPercent}%)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                LinearProgressIndicator(
                    progress = { (item.utilizationPercent / 100f).coerceIn(0f, 1f) },
                    color = barColor,
                    trackColor = Color(0xFF1E1438),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        }
    }
}

// ============================================================================
// COMPONENT 7: METALLIC SILVER FINANCIAL HEALTH SCORE SECTION (#8)
// ============================================================================
@Composable
private fun InsightMetallicHealthScoreSection(
    evaluation: com.example.data.model.FinancialHealthEvaluation
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insight_health_score_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Metallic Silver Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF94A3B8))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFCBD5E1),
                            Color(0xFF94A3B8),
                            Color(0xFFE2E8F0),
                            Color(0xFFABB6C8)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 500f)
                    )
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFF64748B), Color(0xFFF8FAFC))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 34.dp, height = 26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF334155), Color(0xFF0F172A))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            KharcheeSymbolLogo(height = 13.dp)
                        }
                        Column {
                            Text(
                                text = "KHARCHEE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "FINANCIAL HEALTH SCORE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                        }
                    }

                    Text(
                        text = "UPDATED ${evaluation.lastUpdatedDate}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                if (!evaluation.hasSufficientData) {
                    Text(
                        text = "Insufficient Financial Data",
                        fontFamily = headingFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = evaluation.summaryExplanation,
                        fontSize = 12.sp,
                        color = Color(0xFF334155)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${evaluation.score}",
                                    fontFamily = headingFont,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = " / 100",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF1E1B4B),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = evaluation.category,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE9D5FF),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                evaluation.scoreChange?.let { delta ->
                                    Text(
                                        text = "${if (delta >= 0) "+$delta" else "$delta"} pts vs last month",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }

                        // Factor Breakdown Summary
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            evaluation.factors.forEach { factor ->
                                Text(
                                    text = "${factor.name}: ${factor.earnedPoints}/${factor.maxPoints}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }

                    Text(
                        text = evaluation.bureauDisclaimer,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT 8: SAVINGS ANALYTICS SECTION (#9)
// ============================================================================
@Composable
private fun SavingsAnalyticsSectionCard(
    savings: com.example.data.model.SavingsAnalyticsData,
    savingsGoals: List<SavingsGoalRecord>,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("insight_savings_analytics_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "SAVINGS ANALYTICS",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
            color = MaiPositiveEmerald
        )
        Text(
            text = "Verified Cashflow Savings & Goal Progress",
            fontFamily = headingFont,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightStatBox(
                title = "PERIOD SAVINGS",
                value = if (stealthMode) {
                    "₹••••"
                } else if (!savings.hasRecordedIncome) {
                    "Unverified"
                } else {
                    "₹${formatIndianAmount(savings.verifiedPeriodSavings)}"
                },
                color = if (savings.verifiedPeriodSavings >= 0L) MaiPositiveEmerald else MaiExpenseRose,
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "SAVINGS RATE",
                value = if (savings.hasRecordedIncome) "${savings.savingsRatePercent}%" else "0%",
                color = Color(0xFFD8B4FE),
                modifier = Modifier.weight(1f)
            )
            InsightStatBox(
                title = "GOALS PROGRESS",
                value = "${savings.goalsProgressPercent}%",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = savings.explanationText,
            fontSize = 12.sp,
            color = MaiLavenderSubtext,
            lineHeight = 18.sp
        )

        if (savingsGoals.isNotEmpty()) {
            Text(
                text = "ACTIVE SAVINGS GOALS (${savingsGoals.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD8B4FE)
            )
            savingsGoals.take(4).forEach { goal ->
                val pct = if (goal.targetAmount > 0L) {
                    ((goal.savedAmount.toDouble() / goal.targetAmount.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
                } else 0
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(goal.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            text = if (stealthMode) "₹••• / ₹•••" else "₹${formatIndianAmount(goal.savedAmount)} / ₹${formatIndianAmount(goal.targetAmount)} ($pct%)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaiPositiveEmerald
                        )
                    }
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        color = MaiPositiveEmerald,
                        trackColor = Color(0xFF1E1438),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT 9: FINANCIAL REPORTS ACTION CARD (#13)
// ============================================================================
@Composable
private fun FinancialReportsActionCard(
    bundle: CompleteInsightAnalyticsBundle,
    onOpenReportPreview: () -> Unit,
    onExportCsv: () -> Unit,
    onShareSummary: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("insight_financial_reports_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "FINANCIAL INTELLIGENCE REPORTS",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp,
            color = Color(0xFFC084FC)
        )
        Text(
            text = "Generate, Inspect & Export (${bundle.timeWindow.filter.label})",
            fontFamily = headingFont,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Includes Income Summary, Expense Summary, Verified Savings, Category Breakdown, Budget Performance, and Period Comparison.",
            fontSize = 11.sp,
            color = MaiLavenderSubtext
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenReportPreview,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaiPrimaryPurpleEnd,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("insight_view_report_button")
            ) {
                Text("View Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onExportCsv,
                border = BorderStroke(1.dp, MaiPositiveEmerald),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("insight_export_csv_button")
            ) {
                Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaiPositiveEmerald)
            }
            OutlinedButton(
                onClick = onShareSummary,
                border = BorderStroke(1.dp, Color(0xFFA855F7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE9D5FF))
            }
        }
    }
}

// ============================================================================
// DIALOGS: CATEGORY TRANSACTIONS INSPECTOR, CUSTOM DATE RANGE, REPORT PREVIEW
// ============================================================================
@Composable
private fun CategoryTransactionsInspectorDialog(
    detail: InsightCategoryDetail,
    periodLabel: String,
    stealthMode: Boolean,
    onDismiss: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaiCardSurface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaiCardBorderBright),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("insight_category_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${detail.category} Breakdown",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "$periodLabel · ${detail.transactionCount} transactions (${detail.percentOfTotal}%)",
                            fontSize = 11.sp,
                            color = MaiLavenderSubtext
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    text = if (stealthMode) "Total: ₹••••" else "Total: ₹${formatIndianAmount(detail.amount)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD8B4FE)
                )

                HorizontalDivider(color = MaiCardBorder)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detail.transactions, key = { it.id.ifBlank { "${it.txEpochMillis}_${it.amount}" } }) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaiCardSurfaceElevated)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title.ifBlank { detail.category },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${dateFmt.format(Date(tx.txEpochMillis))} · ${tx.paymentMode}",
                                    fontSize = 10.sp,
                                    color = MaiLavenderSubtext
                                )
                            }
                            Text(
                                text = if (stealthMode) "₹••••" else "-₹${formatIndianAmount(tx.amount)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaiExpenseRose
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomDateRangePickerDialog(
    initialStartMillis: Long,
    initialEndMillis: Long,
    onApply: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }
    var startMs by remember { mutableLongStateOf(initialStartMillis) }
    var endMs by remember { mutableLongStateOf(initialEndMillis) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaiCardSurface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaiCardBorderBright),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("insight_custom_date_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Custom Reporting Period",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = startMs }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val c = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                                startMs = c.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    border = BorderStroke(1.dp, MaiCardBorderBright),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Date: ${dateFmt.format(Date(startMs))}", color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = endMs }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val c = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                                endMs = c.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    border = BorderStroke(1.dp, MaiCardBorderBright),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("End Date: ${dateFmt.format(Date(endMs))}", color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = MaiLavenderSubtext)
                    }
                    Button(
                        onClick = { onApply(min(startMs, endMs), max(startMs, endMs)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaiPrimaryPurpleEnd),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("insight_apply_custom_date_button")
                    ) {
                        Text("Apply Range", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialReportPreviewDialog(
    bundle: CompleteInsightAnalyticsBundle,
    onDismiss: () -> Unit
) {
    val reportText = remember(bundle) {
        InsightIntelligenceEngine.generateStructuredReportText(bundle)
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaiCardSurface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaiCardBorderBright),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("insight_report_preview_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Financial Intelligence Report",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090614))
                        .padding(12.dp)
                ) {
                    item {
                        Text(
                            text = reportText,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFE9D5FF),
                            lineHeight = 16.sp
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
            .background(MaiCardSurfaceElevated)
            .border(0.8.dp, MaiCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = MaiLavenderSubtext
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
