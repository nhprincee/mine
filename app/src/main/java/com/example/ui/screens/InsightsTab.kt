package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
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
import com.example.util.ExportedPdfReportResult
import com.example.util.MonthlyFinancialPdfExporter
import com.example.util.MonthlyReportPeriodOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
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

// High-visual Fintech Neon Telemetry palette for Donut & Legend
private val InsightDonutPalette = listOf(
    Color(0xFF00E676), // Emerald (Food)
    Color(0xFFFF3366), // Crimson (Shopping)
    Color(0xFF00E5FF), // Cyan (Travel)
    Color(0xFFB388FF), // Electric Purple (Bills)
    Color(0xFFFFD600), // Gold (Entertainment)
    Color(0xFF10B981), // Deep Emerald
    Color(0xFF38BDF8), // Sky Blue
    Color(0xFFF472B6)  // Neon Rose
)

private fun resolveInsightCategoryColor(category: String, index: Int): Color {
    return when (category.lowercase(Locale.ENGLISH)) {
        "food", "dining", "groceries" -> Color(0xFF00E676)
        "shopping", "fashion" -> Color(0xFFFF3366)
        "travel", "transport", "cab", "fuel" -> Color(0xFF00E5FF)
        "bills", "utilities", "rent", "recharge" -> Color(0xFFB388FF)
        "entertainment", "movies", "subscriptions" -> Color(0xFFFFD600)
        "health" -> Color(0xFF10B981)
        "education" -> Color(0xFF38BDF8)
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

    // Monthly PDF Report state & options
    val monthlyPdfOptions = remember {
        MonthlyFinancialPdfExporter.buildMonthlyReportOptions()
    }
    var selectedMonthlyOption by remember {
        mutableStateOf(monthlyPdfOptions.first())
    }
    var useCustomFilterForPdf by remember { mutableStateOf(false) }
    var lastExportedPdfResult by remember { mutableStateOf<ExportedPdfReportResult?>(null) }

    val pdfTargetBundle = remember(
        useCustomFilterForPdf,
        selectedMonthlyOption,
        bundle,
        expenses,
        effectiveSettings,
        categoryBudgets,
        savingsGoals,
        paymentReminders
    ) {
        if (useCustomFilterForPdf) {
            bundle
        } else {
            MonthlyFinancialPdfExporter.computeBundleForMonthlyOption(
                option = selectedMonthlyOption,
                activeBundle = bundle,
                expenses = expenses,
                settings = effectiveSettings,
                categoryBudgets = categoryBudgets,
                savingsGoals = savingsGoals,
                paymentReminders = paymentReminders
            )
        }
    }

    val savePdfDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri ->
        if (targetUri != null) {
            val res = MonthlyFinancialPdfExporter.writePdfToUri(
                context = context,
                targetUri = targetUri,
                bundle = pdfTargetBundle,
                expenses = expenses,
                userDisplayName = userDisplayName
            )
            res.onSuccess { exported ->
                lastExportedPdfResult = exported
                onShowToast("Saved PDF report: ${exported.fileName} (${exported.pageCount} page(s))")
            }.onFailure { err ->
                onShowToast("Failed to save PDF: ${err.localizedMessage ?: "Unknown error"}")
            }
        }
    }

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

        val recordedExpenseCount = expenses.count { !isIncomeTransaction(it) }
        if (recordedExpenseCount < 3) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = MaiPrimaryPurpleStart)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1A1136),
                                    Color(0xFF100B22),
                                    Color(0xFF160E2E)
                                )
                            )
                        )
                        .border(1.2.dp, Color(0xFF7C3AED).copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("insight_cold_start_empty_state_card"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.22f))
                                    .border(1.dp, Color(0xFFA855F7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Insight Cold Start",
                                    tint = Color(0xFFD8B4FE),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Analytics will generate once you record your first 3 expenses.",
                                fontFamily = headingFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 19.sp
                            )
                        }
                        Surface(
                            color = Color(0xFF281A4D),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, Color(0xFFA855F7))
                        ) {
                            Text(
                                text = "$recordedExpenseCount / 3 Logged",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9D5FF),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (recordedExpenseCount.toFloat() / 3f).coerceIn(0.05f, 1f) },
                        color = Color(0xFFA855F7),
                        trackColor = Color(0xFF231742),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }

        // ====================================================================
        // 1. INTERACTIVE GLOWING DONUT RING CHART (CATEGORY BREAKDOWN)
        //    Food (#00E676), Shopping (#FF3366), Travel (#00E5FF), Bills (#B388FF), Entertainment (#FFD600)
        // ====================================================================
        item {
            InteractiveGlowingNeonDonutChartCard(
                categoryDetails = bundle.categories.categories,
                totalSpent = totalSpent,
                stealthMode = stealthMode,
                onInspectCategory = { detail ->
                    inspectingCategoryDetail = detail
                }
            )
        }

        // ====================================================================
        // 2. TRADINGVIEW-STYLE SMOOTH BEZIER CASHFLOW SPLINE GRAPH
        //    Cubic Bezier cumulative curve, #6600E5FF vertical gradient fill,
        //    dashed last-month ghost curve & horizontal touch-scrubbing crosshair
        // ====================================================================
        item {
            TradingViewBezierCashflowSplineCard(
                expenses = expenses,
                bundle = bundle,
                stealthMode = stealthMode
            )
        }

        // ====================================================================
        // 3. 30-DAY SPENDING INTENSITY HEATMAP MATRIX (CRED / GITHUB STYLE)
        //    7-column monthly grid: ₹0 Emerald (#00E676) + No-Spend Streak badge,
        //    Low (#004D40), Medium (#5E35B1), High (#FF1744) + drill-down card
        // ====================================================================
        item {
            ThirtyDaySpendingIntensityHeatmapCard(
                expenses = expenses,
                dailyLimit = dailyLimit,
                stealthMode = stealthMode
            )
        }

        // ====================================================================
        // 4. DUAL RADIAL SPEEDOMETER GAUGES & TOP 5 MONEY DRAINERS LEADERBOARD
        //    240° Budget Burn Speedometer + Savings Efficiency Score (0-100)
        //    + Top 5 Money Drainers racing horizontal neon bars
        // ====================================================================
        item {
            DualRadialSpeedometersAndTopMerchantsSection(
                expenses = expenses,
                dailyLimit = dailyLimit,
                stealthMode = stealthMode
            )
        }

        // ====================================================================
        // 4. AI-POWERED JARVIS ACTIONABLE BRIEFING (COMPACT)
        // ====================================================================
        if (bundle.jarvisInsights.isNotEmpty()) {
            item {
                InsightJarvisIntelligenceCard(
                    insights = bundle.jarvisInsights.take(2),
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
        // 5. ONE-TAP EXPORT STATEMENT ("Download Monthly Summary (PDF / CSV)")
        // ====================================================================
        item {
            OneTapMonthlyStatementExportCard(
                periodLabel = pdfTargetBundle.timeWindow.displayRangeLabel,
                lastExportedPdf = lastExportedPdfResult,
                onDownloadAndSharePdf = {
                    val res = MonthlyFinancialPdfExporter.downloadPdfToDevice(
                        context = context,
                        bundle = pdfTargetBundle,
                        expenses = expenses,
                        userDisplayName = userDisplayName
                    )
                    res.onSuccess { exported ->
                        lastExportedPdfResult = exported
                        onShowToast("📄 Downloaded ${exported.fileName} (${exported.pageCount} page(s))")
                        MonthlyFinancialPdfExporter.shareOrOpenPdf(
                            context = context,
                            result = exported,
                            periodLabel = pdfTargetBundle.timeWindow.displayRangeLabel,
                            openDirectly = false
                        )
                    }.onFailure { err ->
                        onShowToast("Could not generate PDF: ${err.localizedMessage ?: "Error"}")
                    }
                },
                onSavePdfAs = {
                    val suggestedName = MonthlyFinancialPdfExporter.buildSuggestedFileName(pdfTargetBundle)
                    runCatching {
                        savePdfDocumentLauncher.launch(suggestedName)
                    }.onFailure {
                        val res = MonthlyFinancialPdfExporter.downloadPdfToDevice(
                            context = context,
                            bundle = pdfTargetBundle,
                            expenses = expenses,
                            userDisplayName = userDisplayName
                        )
                        res.onSuccess { exported ->
                            lastExportedPdfResult = exported
                            onShowToast("Downloaded ${exported.fileName} to ${exported.savedLocationLabel}")
                        }
                    }
                },
                onExportCsv = {
                    val csv = InsightIntelligenceEngine.generateStructuredReportCsv(pdfTargetBundle)
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                "KHARCHEE_Monthly_Statement_${pdfTargetBundle.timeWindow.filter.id}.csv"
                            )
                            putExtra(Intent.EXTRA_TEXT, csv)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export Monthly CSV Statement"))
                    } catch (_: Exception) {
                    }
                    onShowToast("📊 Exported CSV statement for ${pdfTargetBundle.timeWindow.displayRangeLabel}")
                }
            )
        }

        // ====================================================================
        // 6. OPTIONAL DEEP-DIVE SECTION VIEW (WHEN USER SELECTS SPECIFIC SECTION PILL)
        // ====================================================================
        if (activeSection == InsightSectionView.BUDGET) {
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
        } else if (activeSection == InsightSectionView.SAVINGS) {
            item {
                SavingsAnalyticsSectionCard(
                    savings = bundle.savings,
                    savingsGoals = savingsGoals,
                    stealthMode = stealthMode
                )
            }
        } else if (activeSection == InsightSectionView.EARNINGS) {
            item {
                EarningsInsightsSectionCard(
                    bundle = bundle,
                    stealthMode = stealthMode,
                    selectedBarIndex = selectedEarningsBarIndex,
                    onSelectBarIndex = { selectedEarningsBarIndex = it }
                )
            }
        } else if (activeSection == InsightSectionView.SPENDING) {
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
        } else if (activeSection == InsightSectionView.REPORTS) {
            item {
                FinancialReportsActionCard(
                    bundle = pdfTargetBundle,
                    monthlyOptions = monthlyPdfOptions,
                    selectedMonthlyOption = selectedMonthlyOption,
                    useActiveFilter = useCustomFilterForPdf,
                    activeFilterLabel = "${bundle.timeWindow.filter.label} (${bundle.timeWindow.displayRangeLabel})",
                    lastExportedPdf = lastExportedPdfResult,
                    stealthMode = stealthMode,
                    onSelectMonthlyOption = { option ->
                        selectedMonthlyOption = option
                        useCustomFilterForPdf = false
                    },
                    onSelectActiveFilter = {
                        useCustomFilterForPdf = true
                    },
                    onDownloadPdf = {
                        val res = MonthlyFinancialPdfExporter.downloadPdfToDevice(
                            context = context,
                            bundle = pdfTargetBundle,
                            expenses = expenses,
                            userDisplayName = userDisplayName
                        )
                        res.onSuccess { exported ->
                            lastExportedPdfResult = exported
                            onShowToast("Downloaded ${exported.fileName} to ${exported.savedLocationLabel}")
                        }.onFailure { err ->
                            onShowToast("Could not generate PDF: ${err.localizedMessage ?: "Error"}")
                        }
                    },
                    onSavePdfAs = {
                        val suggestedName = MonthlyFinancialPdfExporter.buildSuggestedFileName(pdfTargetBundle)
                        runCatching {
                            savePdfDocumentLauncher.launch(suggestedName)
                        }.onFailure {
                            val res = MonthlyFinancialPdfExporter.downloadPdfToDevice(
                                context = context,
                                bundle = pdfTargetBundle,
                                expenses = expenses,
                                userDisplayName = userDisplayName
                            )
                            res.onSuccess { exported ->
                                lastExportedPdfResult = exported
                                onShowToast("Downloaded ${exported.fileName} to ${exported.savedLocationLabel}")
                            }
                        }
                    },
                    onOpenExportedPdf = { exported ->
                        MonthlyFinancialPdfExporter.shareOrOpenPdf(
                            context = context,
                            result = exported,
                            periodLabel = pdfTargetBundle.timeWindow.displayRangeLabel,
                            openDirectly = true
                        )
                    },
                    onShareExportedPdf = { exported ->
                        MonthlyFinancialPdfExporter.shareOrOpenPdf(
                            context = context,
                            result = exported,
                            periodLabel = pdfTargetBundle.timeWindow.displayRangeLabel,
                            openDirectly = false
                        )
                    },
                    onOpenReportPreview = { showReportPreviewDialog = true },
                    onExportCsv = {
                        val csv = InsightIntelligenceEngine.generateStructuredReportCsv(pdfTargetBundle)
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "KHARCHEE_Insight_Report_${pdfTargetBundle.timeWindow.filter.id}.csv"
                                )
                                putExtra(Intent.EXTRA_TEXT, csv)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export Insight CSV"))
                        } catch (_: Exception) {
                        }
                        onShowToast("Prepared CSV report for ${pdfTargetBundle.timeWindow.displayRangeLabel}")
                    },
                    onShareSummary = {
                        val text = InsightIntelligenceEngine.generateStructuredReportText(pdfTargetBundle)
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "KHARCHEE Financial Report (${pdfTargetBundle.timeWindow.displayRangeLabel})"
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
            bundle = pdfTargetBundle,
            onDownloadPdf = {
                val res = MonthlyFinancialPdfExporter.downloadPdfToDevice(
                    context = context,
                    bundle = pdfTargetBundle,
                    expenses = expenses,
                    userDisplayName = userDisplayName
                )
                res.onSuccess { exported ->
                    lastExportedPdfResult = exported
                    onShowToast("Downloaded ${exported.fileName} to ${exported.savedLocationLabel}")
                }.onFailure { err ->
                    onShowToast("Could not generate PDF: ${err.localizedMessage ?: "Error"}")
                }
            },
            onDismiss = { showReportPreviewDialog = false }
        )
    }
}

// ============================================================================
// 3-SECOND CLARITY CARD 1: "WHERE YOUR MONEY WENT" (TOP 4 CATEGORIES)
// ============================================================================
private data class TopFourCategoryDisplay(
    val category: String,
    val amount: Long,
    val percentShare: Int,
    val color: Color,
    val detail: InsightCategoryDetail?
)

@Composable
private fun ThreeSecondClarityWhereMoneyWentCard(
    categoryDetails: List<InsightCategoryDetail>,
    totalSpent: Long,
    stealthMode: Boolean,
    onInspectCategory: (InsightCategoryDetail) -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    val topFour = remember(categoryDetails, totalSpent) {
        val defaults = listOf("Food", "Travel", "Bills", "Shopping")
        val existingByCat = categoryDetails.sortedByDescending { it.amount }
        val chosen = mutableListOf<TopFourCategoryDisplay>()

        existingByCat.take(4).forEachIndexed { idx, item ->
            val pct = if (totalSpent > 0L) {
                ((item.amount.toDouble() / totalSpent.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
            } else 0
            chosen.add(
                TopFourCategoryDisplay(
                    category = item.category,
                    amount = item.amount,
                    percentShare = pct,
                    color = resolveInsightCategoryColor(item.category, idx),
                    detail = item
                )
            )
        }

        var fallbackIdx = chosen.size
        for (defCat in defaults) {
            if (chosen.size >= 4) break
            if (chosen.none { it.category.equals(defCat, ignoreCase = true) }) {
                chosen.add(
                    TopFourCategoryDisplay(
                        category = defCat,
                        amount = 0L,
                        percentShare = 0,
                        color = resolveInsightCategoryColor(defCat, fallbackIdx++),
                        detail = null
                    )
                )
            }
        }
        chosen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = MaiPrimaryPurpleStart)
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorderBright.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("where_your_money_went_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "1 · WHERE YOUR MONEY WENT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFFD8B4FE)
                )
                Text(
                    text = "Top 4 Spending Categories",
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Surface(
                color = Color(0xFF1F163D),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, MaiCardBorder)
            ) {
                Text(
                    text = if (stealthMode) "Total: ₹••••" else "Total: ₹${formatIndianAmount(totalSpent)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE9D5FF),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            topFour.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF120D24))
                        .border(0.8.dp, Color(0xFF261C47), RoundedCornerShape(14.dp))
                        .clickable(enabled = item.detail != null) {
                            item.detail?.let(onInspectCategory)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("top4_category_row_${item.category.lowercase(Locale.ENGLISH)}"),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
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
                                    .background(item.color.copy(alpha = 0.18f))
                                    .border(0.7.dp, item.color.copy(alpha = 0.55f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(item.category),
                                    contentDescription = item.category,
                                    tint = item.color,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = item.category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = if (stealthMode) {
                                "₹•••• · ${item.percentShare}%"
                            } else {
                                "₹${formatIndianAmount(item.amount)} · ${item.percentShare}%"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (item.percentShare / 100f).coerceIn(if (item.amount > 0L) 0.04f else 0f, 1f) },
                        color = item.color,
                        trackColor = Color(0xFF231842),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3-SECOND CLARITY CARD 2: "MONTH-OVER-MONTH PACE"
// ============================================================================
@Composable
private fun ThreeSecondClarityMonthOverMonthPaceCard(
    expenses: List<ExpenseRecord>,
    bundle: CompleteInsightAnalyticsBundle,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current

    val paceStats = remember(expenses, bundle) {
        val nowCal = Calendar.getInstance()
        val curYear = nowCal.get(Calendar.YEAR)
        val curMonth = nowCal.get(Calendar.MONTH)

        val prevCal = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }
        val prevYear = prevCal.get(Calendar.YEAR)
        val prevMonth = prevCal.get(Calendar.MONTH)

        var thisMonthTotal = 0L
        var lastMonthTotal = 0L

        val txCal = Calendar.getInstance()
        expenses.forEach { tx ->
            if (!isIncomeTransaction(tx)) {
                txCal.timeInMillis = tx.txEpochMillis
                val y = txCal.get(Calendar.YEAR)
                val m = txCal.get(Calendar.MONTH)
                if (y == curYear && m == curMonth) {
                    thisMonthTotal += tx.amount
                } else if (y == prevYear && m == prevMonth) {
                    lastMonthTotal += tx.amount
                }
            }
        }

        val deltaPct: Int = when {
            lastMonthTotal > 0L -> {
                (((thisMonthTotal - lastMonthTotal).toDouble() / lastMonthTotal.toDouble()) * 100.0).roundToInt()
            }
            bundle.overview.expenseChangePercent != null -> bundle.overview.expenseChangePercent
            else -> 0
        }

        Triple(thisMonthTotal, lastMonthTotal, deltaPct)
    }

    val thisMonthSpent = paceStats.first
    val lastMonthSpent = paceStats.second
    val deltaPct = paceStats.third
    val isLowerOrEqual = deltaPct <= 0
    val absPct = abs(deltaPct)

    val pillColor = if (isLowerOrEqual) MaiPositiveEmerald else MaiWarningAmber
    val pillText = when {
        lastMonthSpent == 0L && thisMonthSpent == 0L -> "↓ 0% vs last month — Clean slate!"
        isLowerOrEqual -> "↓ ${absPct}% less spent than last month — Great pace!"
        else -> "↑ ${absPct}% higher than last month"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = MaiPrimaryPurpleStart)
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, pillColor.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("month_over_month_pace_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "2 · MONTH-OVER-MONTH PACE",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.1.sp,
            color = Color(0xFFD8B4FE)
        )

        // Clear Comparison Pill
        Surface(
            color = pillColor.copy(alpha = 0.14f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, pillColor.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("month_over_month_pace_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isLowerOrEqual) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = "Month-over-Month Pace",
                    tint = pillColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = pillText,
                    fontFamily = headingFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pillColor,
                    lineHeight = 19.sp
                )
            }
        }

        // Side-by-side This Month vs Last Month comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF120D24))
                    .border(0.8.dp, Color(0xFF261C47), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "THIS MONTH",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(thisMonthSpent)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF120D24))
                    .border(0.8.dp, Color(0xFF261C47), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "LAST MONTH",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(lastMonthSpent)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaiLavenderSubtext
                )
            }
        }
    }
}

// ============================================================================
// 3-SECOND CLARITY CARD 3: "MONTH-END FORECAST"
// ============================================================================
@Composable
private fun ThreeSecondClarityMonthEndForecastCard(
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current

    val forecastData = remember(expenses, dailyLimit) {
        val cal = Calendar.getInstance()
        val curYear = cal.get(Calendar.YEAR)
        val curMonth = cal.get(Calendar.MONTH)
        val daysElapsed = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH).coerceAtLeast(28)

        var monthSpent = 0L
        var monthIncome = 0L
        val txCal = Calendar.getInstance()
        expenses.forEach { tx ->
            txCal.timeInMillis = tx.txEpochMillis
            if (txCal.get(Calendar.YEAR) == curYear && txCal.get(Calendar.MONTH) == curMonth) {
                if (isIncomeTransaction(tx)) {
                    monthIncome += tx.amount
                } else {
                    monthSpent += tx.amount
                }
            }
        }

        val dailyAvg = monthSpent / daysElapsed.toLong()
        val projectedMonthSpend = dailyAvg * daysInMonth.toLong()
        val monthlyTargetPool = max(monthIncome, max(500L, dailyLimit) * daysInMonth.toLong())
        val projectedSavings = max(0L, monthlyTargetPool - projectedMonthSpend)

        Triple(dailyAvg, projectedSavings, projectedMonthSpend to monthlyTargetPool)
    }

    val dailyAvg = forecastData.first
    val projectedSavings = forecastData.second
    val projectedMonthSpend = forecastData.third.first
    val monthlyTargetPool = forecastData.third.second

    val dailyAvgFormatted = if (stealthMode) "••••" else formatIndianAmount(dailyAvg)
    val projectedSavingsFormatted = if (stealthMode) "••••" else formatIndianAmount(projectedSavings)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = MaiPrimaryPurpleStart)
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiPositiveEmerald.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("month_end_forecast_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3 · MONTH-END FORECAST",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                color = MaiPositiveEmerald
            )
            Surface(
                color = MaiPositiveEmerald.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, MaiPositiveEmerald.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "LINEAR PROJECTION",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiPositiveEmerald,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }

        Text(
            text = "At your current daily average of ₹$dailyAvgFormatted, you will save ₹$projectedSavingsFormatted by month-end",
            fontFamily = headingFont,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 21.sp,
            modifier = Modifier.testTag("month_end_forecast_text")
        )

        LinearProgressIndicator(
            progress = {
                if (monthlyTargetPool > 0L) {
                    (projectedMonthSpend.toFloat() / monthlyTargetPool.toFloat()).coerceIn(0.05f, 1f)
                } else 0.1f
            },
            color = if (projectedSavings > 0L) MaiPositiveEmerald else MaiWarningAmber,
            trackColor = Color(0xFF1F163A),
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Daily Avg: ₹$dailyAvgFormatted/day",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = MaiLavenderSubtext
            )
            Text(
                text = "Projected Save: ₹$projectedSavingsFormatted",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaiPositiveEmerald
            )
        }
    }
}

// ============================================================================
// WEEKLY SPENDING HEATMAP / BAR STRIP (MON–SUN WITH TAP INSPECTION)
// ============================================================================
private data class WeeklyDaySlot(
    val dayShortLabel: String, // Mon, Tue, Wed, Thu, Fri, Sat, Sun
    val dateFormatted: String, // e.g. "14 Nov"
    val totalSpend: Long,
    val topMerchant: String,
    val topMerchantAmount: Long
)

@Composable
private fun WeeklySpendingHeatmapBarStripCard(
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current

    val weekSlots = remember(expenses) {
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dateFmt = SimpleDateFormat("dd MMM", Locale.ENGLISH)

        // Anchor to current week's Monday..Sunday
        val mondayCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val dow = get(Calendar.DAY_OF_WEEK)
            val diffToMonday = if (dow == Calendar.SUNDAY) -6 else (Calendar.MONDAY - dow)
            add(Calendar.DAY_OF_YEAR, diffToMonday)
        }

        dayLabels.mapIndexed { idx, label ->
            val dayStart = (mondayCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, idx)
            }
            val startMs = dayStart.timeInMillis
            val endMs = startMs + 86_400_000L - 1L

            val dayExpenses = expenses.filter { tx ->
                !isIncomeTransaction(tx) && tx.txEpochMillis in startMs..endMs
            }
            val sum = dayExpenses.sumOf { it.amount }
            val topMerchantEntry = dayExpenses
                .groupBy { it.title.ifBlank { it.category.ifBlank { "Spend" } } }
                .maxByOrNull { entry -> entry.value.sumOf { it.amount } }

            WeeklyDaySlot(
                dayShortLabel = label,
                dateFormatted = dateFmt.format(Date(startMs)),
                totalSpend = sum,
                topMerchant = topMerchantEntry?.key ?: "No spend",
                topMerchantAmount = topMerchantEntry?.value?.sumOf { it.amount } ?: 0L
            )
        }
    }

    val defaultSelectedIdx = remember(weekSlots) {
        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        when (todayDow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    var selectedDayIndex by remember { mutableIntStateOf(defaultSelectedIdx) }
    val selectedSlot = weekSlots.getOrElse(selectedDayIndex) { weekSlots.first() }
    val maxDaySpend = remember(weekSlots, dailyLimit) {
        max(weekSlots.maxOfOrNull { it.totalSpend } ?: 0L, max(500L, dailyLimit))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = MaiPrimaryPurpleStart)
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("weekly_spending_heatmap_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "WEEKLY SPENDING HEATMAP · MON–SUN",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFFD8B4FE)
                )
                Text(
                    text = "Tap any day for spend & top merchant",
                    fontFamily = headingFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // 7-Day Bar Chart Strip (Mon–Sun)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            weekSlots.forEachIndexed { idx, slot ->
                val isSelected = selectedDayIndex == idx
                val ratio = if (maxDaySpend > 0L) {
                    (slot.totalSpend.toFloat() / maxDaySpend.toFloat()).coerceIn(
                        if (slot.totalSpend > 0L) 0.14f else 0.06f,
                        1f
                    )
                } else 0.06f

                val barColor = when {
                    slot.totalSpend == 0L -> Color(0xFF281D46)
                    slot.totalSpend <= dailyLimit -> MaiPositiveEmerald
                    slot.totalSpend <= (dailyLimit * 150L) / 100L -> Color(0xFFA855F7)
                    else -> MaiExpenseRose
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF1D1438) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) Color(0xFFD8B4FE) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedDayIndex = idx }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .testTag("weekly_heatmap_bar_${slot.dayShortLabel.lowercase(Locale.ENGLISH)}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.68f)
                                .fillMaxHeight(ratio)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(barColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = slot.dayShortLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaiLavenderSubtext
                    )
                }
            }
        }

        // Selected Day Detail Pill (Exact Day's Total Spend & Top Merchant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF130D26))
                .border(0.9.dp, Color(0xFF3E2D6E), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("weekly_heatmap_selected_day_detail"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${selectedSlot.dayShortLabel}, ${selectedSlot.dateFormatted}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE)
                )
                Text(
                    text = if (selectedSlot.totalSpend > 0L) {
                        if (stealthMode) {
                            "Top Merchant: ${selectedSlot.topMerchant} (₹•••)"
                        } else {
                            "Top Merchant: ${selectedSlot.topMerchant} (₹${formatIndianAmount(selectedSlot.topMerchantAmount)})"
                        }
                    } else {
                        "No expenses logged on this day ✨"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Text(
                text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(selectedSlot.totalSpend)}",
                fontFamily = headingFont,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selectedSlot.totalSpend == 0L) MaiPositiveEmerald else Color.White
            )
        }
    }
}

// ============================================================================
// ONE-TAP EXPORT STATEMENT CARD ("Download Monthly Summary (PDF / CSV)")
// Sleek Obsidian Command-Center Styling
// ============================================================================
@Composable
private fun OneTapMonthlyStatementExportCard(
    periodLabel: String,
    lastExportedPdf: ExportedPdfReportResult?,
    onDownloadAndSharePdf: () -> Unit,
    onSavePdfAs: () -> Unit,
    onExportCsv: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF00E5FF))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1422),
                        Color(0xFF090D16)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.45f),
                        Color(0xFF00E676).copy(alpha = 0.45f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
            .testTag("download_monthly_summary_pdf_csv_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "OBSIDIAN TELEMETRY ARCHIVE • 1-TAP EXPORT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = "Monthly Summary ($periodLabel)",
                    fontFamily = headingFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x1F00E676))
                    .border(0.8.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PDF + CSV",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E676)
                )
            }
        }

        Button(
            onClick = onDownloadAndSharePdf,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00E5FF),
                contentColor = Color(0xFF050811)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("download_monthly_summary_pdf_csv_button")
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Download Monthly Summary",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Download Monthly Summary (PDF / CSV)",
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSavePdfAs,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.9.dp, Color(0xFF00E5FF).copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("save_monthly_pdf_button")
            ) {
                Text("📄 Save PDF File", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onExportCsv,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.9.dp, Color(0xFF00E676).copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("export_monthly_csv_button")
            ) {
                Text("📊 Share CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (lastExportedPdf != null) {
            Text(
                text = "✓ Last saved: ${lastExportedPdf.fileName}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Color(0xFF00E676)
            )
        }
    }
}


// ============================================================================
// COMPONENT 1: MAIN BALANCE & CASHFLOW OVERVIEW GLASS CARD (MATCHES RIGHT SCREEN OF REFERENCE)
// ============================================================================
@Composable
private fun InsightOverviewGlassCard(
    bundle: CompleteInsightAnalyticsBundle,
    dailyLimit: Long = 500L,
    stealthMode: Boolean,
    onSelectSection: (InsightSectionView) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val ov = bundle.overview
    val netBalance = ov.totalIncome - ov.totalExpenses
    val safeLimit = max(100L, dailyLimit)
    val avgDailyBurn = bundle.spending.averageDailySpending
    val safeSpendToday = max(0L, safeLimit - avgDailyBurn)
    val burnRatio = (avgDailyBurn.toFloat() / safeLimit.toFloat()).coerceIn(0.06f, 1f)

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

        // Income & Expenses Split Row
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
                    color = MaiPositiveEmerald,
                    maxLines = 1
                )
                Text(
                    text = ov.incomeChangePercent?.let {
                        "${if (it >= 0) "+$it%" else "$it%"} ${bundle.timeWindow.comparisonLabel}"
                    } ?: "Tap for earnings view",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext,
                    maxLines = 1
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
                    color = MaiExpenseRose,
                    maxLines = 1
                )
                Text(
                    text = ov.expenseChangePercent?.let {
                        "${if (it >= 0) "+$it%" else "$it%"} ${bundle.timeWindow.comparisonLabel}"
                    } ?: "Tap for spending view",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaiLavenderSubtext,
                    maxLines = 1
                )
            }
        }

        // Dynamic Cashflow Burn-Rate Radar & Segmented MoM KPI Bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0E091E))
                .border(0.8.dp, Color(0xFF7C3AED).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("insight_burn_rate_radar_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Pulse Indicator for Daily Safe Burn Rate
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 6.dp.toPx()
                    drawCircle(
                        color = Color(0xFF281A4D),
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF2EE59D),
                                Color(0xFFA855F7),
                                Color(0xFF7C3AED)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * (1f - burnRatio).coerceIn(0.12f, 1f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SAFE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD8B4FE)
                    )
                    Text(
                        text = if (stealthMode) "₹•••" else "₹${formatIndianAmount(safeSpendToday)}",
                        fontFamily = headingFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2EE59D),
                        maxLines = 1
                    )
                }
            }

            // Segmented Horizontal Glass Bars for Month-over-Month (MoM) Cashflow Comparison
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Safe Burn Rate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (stealthMode) "Avg ₹•••/day" else "Avg ₹${formatIndianAmount(avgDailyBurn)}/d",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC4B5FD)
                    )
                }

                val maxMoM = max(100L, max(ov.monthlySpending, ov.previousPeriodExpenses))
                val currMoMRatio = (ov.monthlySpending.toFloat() / maxMoM.toFloat()).coerceIn(0.08f, 1f)
                val prevMoMRatio = (ov.previousPeriodExpenses.toFloat() / maxMoM.toFloat()).coerceIn(0.08f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MoM Outflow Radar",
                            fontSize = 10.sp,
                            color = MaiLavenderSubtext
                        )
                        Text(
                            text = ov.monthOverMonthExpenseChangePercent?.let {
                                if (it > 0) "+$it% vs Prior" else "$it% vs Prior"
                            } ?: "0% MoM",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                    // Current vs Prior Segmented Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF1F143B)),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(currMoMRatio)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(prevMoMRatio)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF38BDF8).copy(alpha = 0.65f))
                        )
                    }
                }
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
// COMPONENT 2: 3D GLASS CATEGORY DONUT + INTERACTIVE HOLOGRAPHIC SPLINE & PREDICTIVE RUNWAY
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
    val selectedCategoryItem = remember(selectedDonutCategory, categoryStats) {
        categoryStats.find { it.category == selectedDonutCategory }
    }
    val activeSliceExpansionPx by animateFloatAsState(
        targetValue = if (selectedDonutCategory != null) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "donutSliceExpansion"
    )
    val projectedMonthEndOutflow = remember(bundle.spending.averageDailySpending, bundle.overview.monthlySpending) {
        max(bundle.overview.monthlySpending, bundle.spending.averageDailySpending * 30L)
    }

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
        // Top Total Spending Header
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

        // 3D Glass Category Donut with Active Slice Expansion (Left) + Category Legend (Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(172.dp)
                    .pointerInput(categoryStats) {
                        detectTapGestures { tapOffset ->
                            if (categoryStats.isNotEmpty()) {
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val dx = tapOffset.x - centerX
                                val dy = tapOffset.y - centerY
                                val rawDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                val normalizedAngle = ((rawDeg + 90f) % 360f + 360f) % 360f
                                val total = max(1L, categoryStats.sumOf { it.amount }).toFloat()
                                var accAngle = 0f
                                for (item in categoryStats) {
                                    val sweep = (item.amount.toFloat() / total) * 360f
                                    if (normalizedAngle >= accAngle && normalizedAngle <= accAngle + sweep) {
                                        onSelectDonutCategory(item.category)
                                        break
                                    }
                                    accAngle += sweep
                                }
                            }
                        }
                    }
                    .testTag("insight_donut_chart"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val strokeWidth = 26.dp.toPx()
                    val expandOffsetPx = 6.dp.toPx() * activeSliceExpansionPx
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
                            val extraWidth = if (isHighlighted) expandOffsetPx else 0f
                            drawArc(
                                color = item.color,
                                startAngle = currentStartAngle,
                                sweepAngle = max(4f, sweep - 2.5f),
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth + extraWidth,
                                    cap = StrokeCap.Butt
                                )
                            )
                            // 3D Glass Rim Highlight
                            drawArc(
                                color = Color.White.copy(alpha = if (isHighlighted) 0.45f else 0.16f),
                                startAngle = currentStartAngle,
                                sweepAngle = max(4f, sweep - 2.5f),
                                useCenter = false,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Butt
                                )
                            )
                            currentStartAngle += sweep
                        }
                    }
                }

                // Dynamic Center Donut Transition: Selected Category Icon, Name, ₹ Amount, and % Share
                Crossfade(
                    targetState = selectedCategoryItem,
                    animationSpec = tween(durationMillis = 220),
                    label = "donutCenterCrossfade"
                ) { activeCat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(horizontal = 18.dp)
                    ) {
                        if (activeCat != null) {
                            Icon(
                                imageVector = getCategoryIcon(activeCat.category),
                                contentDescription = activeCat.category,
                                tint = activeCat.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = activeCat.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(activeCat.amount)}",
                                fontFamily = headingFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeCat.color,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = "${activeCat.percent}% Share",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaiLavenderSubtext
                            )
                        } else {
                            Text(
                                text = "Sum expenses",
                                fontSize = 10.sp,
                                color = MaiLavenderSubtext,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(bundle.overview.totalExpenses)}",
                                fontFamily = headingFont,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Right Legend List
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
                        val isSelected = selectedDonutCategory == item.category
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) item.color.copy(alpha = 0.16f) else Color.Transparent)
                                .clickable { onSelectDonutCategory(item.category) }
                                .padding(vertical = 3.dp, horizontal = 6.dp),
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
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
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

        // Interactive Holographic Spline & Scrubber + Predictive Runway Projection (Dotted Neon Curve)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Holographic Spend Velocity & Runway",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (stealthMode) {
                            "Scrub curve · Projected Month-End: ₹••••"
                        } else {
                            "Scrub curve · Projected Month-End: ₹${formatIndianAmount(projectedMonthEndOutflow)}"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFFC4B5FD)
                    )
                }
                activeDayPoint?.let { pt ->
                    Surface(
                        color = Color(0xFF281458).copy(alpha = 0.92f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFA855F7)),
                        modifier = Modifier.testTag("insight_spline_tooltip_pill")
                    ) {
                        Text(
                            text = if (stealthMode) {
                                "${pt.shortLabel} (${pt.label}): ₹••••"
                            } else {
                                "${pt.shortLabel} · ${pt.label}: ₹${formatIndianAmount(pt.amount)}"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            val maxDaySpend = max(100L, max(dailyPoints.maxOfOrNull { it.amount } ?: 100L, bundle.spending.averageDailySpending))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .pointerInput(dailyPoints) {
                        detectTapGestures { tapOffset ->
                            if (dailyPoints.isNotEmpty() && size.width > 0) {
                                val slotWidth = size.width.toFloat() / dailyPoints.size
                                val tappedIdx = (tapOffset.x / slotWidth).toInt().coerceIn(0, dailyPoints.lastIndex)
                                onSelectDailyIndex(tappedIdx)
                            }
                        }
                    }
                    .pointerInput(dailyPoints) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            if (dailyPoints.isNotEmpty() && size.width > 0) {
                                val slotWidth = size.width.toFloat() / dailyPoints.size
                                val scrubbedIdx = (change.position.x / slotWidth).toInt().coerceIn(0, dailyPoints.lastIndex)
                                onSelectDailyIndex(scrubbedIdx)
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
                        // Reserve 82% width for actual points and 18% on the right for dotted month-end projection trail
                        val actualWidth = w * 0.82f
                        val stepX = actualWidth / (dailyPoints.size - 1).coerceAtLeast(1)
                        val offsets = dailyPoints.mapIndexed { idx, pt ->
                            val ratio = (pt.amount.toFloat() / maxDaySpend.toFloat()).coerceIn(0.08f, 1f)
                            Offset(
                                x = stepX * idx,
                                y = h * (1f - ratio)
                            )
                        }

                        // Subtle glowing vertical grid guidelines
                        offsets.forEach { ptOffset ->
                            drawLine(
                                color = Color(0xFF7C3AED).copy(alpha = 0.14f),
                                start = Offset(ptOffset.x, 0f),
                                end = Offset(ptOffset.x, h),
                                strokeWidth = 1.dp.toPx()
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

                        // Electric Purple (#7C3AED) to Neon Violet gradient area fill under spline
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED).copy(alpha = 0.58f),
                                    Color(0xFFA855F7).copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Glowing outer spline aura
                        drawPath(
                            path = smoothPath,
                            color = Color(0xFF7C3AED).copy(alpha = 0.35f),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Main Cubic-Bezier Holographic Spline
                        drawPath(
                            path = smoothPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED),
                                    Color(0xFFA855F7),
                                    Color(0xFFD8B4FE)
                                )
                            ),
                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Predictive Runway Projection (Dotted Neon Curve extending to Month-End)
                        val lastActual = offsets.last()
                        val avgRatio = (bundle.spending.averageDailySpending.toFloat() / maxDaySpend.toFloat()).coerceIn(0.12f, 0.92f)
                        val projMid = Offset(
                            x = actualWidth + (w - actualWidth) * 0.5f,
                            y = (lastActual.y + h * (1f - avgRatio)) * 0.5f
                        )
                        val projEnd = Offset(
                            x = w,
                            y = h * (1f - avgRatio)
                        )
                        val runwayPath = Path().apply {
                            moveTo(lastActual.x, lastActual.y)
                            cubicTo(
                                (lastActual.x + projMid.x) / 2f,
                                lastActual.y,
                                (projMid.x + projEnd.x) / 2f,
                                projEnd.y,
                                projEnd.x,
                                projEnd.y
                            )
                        }
                        drawPath(
                            path = runwayPath,
                            color = Color(0xFFD8B4FE).copy(alpha = 0.85f),
                            style = Stroke(
                                width = 2.4.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                            )
                        )
                        drawCircle(
                            color = Color(0xFFD8B4FE),
                            radius = 3.5.dp.toPx(),
                            center = projEnd
                        )

                        // Highlight selected day point & scrubber vertical guideline
                        val selectedOffset = offsets[safeSelectedIdx]
                        drawLine(
                            color = Color(0xFFC084FC).copy(alpha = 0.65f),
                            start = Offset(selectedOffset.x, 0f),
                            end = Offset(selectedOffset.x, h),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                        drawCircle(
                            color = Color(0xFFA855F7).copy(alpha = 0.40f),
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

                // Bottom Day Labels + Projected Runway Tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dailyPoints.forEachIndexed { idx, pt ->
                        val isSelected = idx == safeSelectedIdx
                        Text(
                            text = pt.shortLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFD8B4FE) else MaiLavenderSubtext,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectDailyIndex(idx) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "PROJ →",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA855F7)
                    )
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
// COMPONENT 9: FINANCIAL REPORTS & MONTHLY PDF EXPORT ACTION CARD (#13)
// ============================================================================
@Composable
private fun FinancialReportsActionCard(
    bundle: CompleteInsightAnalyticsBundle,
    monthlyOptions: List<MonthlyReportPeriodOption>,
    selectedMonthlyOption: MonthlyReportPeriodOption,
    useActiveFilter: Boolean,
    activeFilterLabel: String,
    lastExportedPdf: ExportedPdfReportResult?,
    stealthMode: Boolean,
    onSelectMonthlyOption: (MonthlyReportPeriodOption) -> Unit,
    onSelectActiveFilter: () -> Unit,
    onDownloadPdf: () -> Unit,
    onSavePdfAs: () -> Unit,
    onOpenExportedPdf: (ExportedPdfReportResult) -> Unit,
    onShareExportedPdf: (ExportedPdfReportResult) -> Unit,
    onOpenReportPreview: () -> Unit,
    onExportCsv: () -> Unit,
    onShareSummary: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val ov = bundle.overview

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("insight_financial_reports_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "MONTHLY FINANCIAL PDF REPORTS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFFC084FC)
                )
                Text(
                    text = "Statement & Summary Export",
                    fontFamily = headingFont,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Surface(
                color = Color(0xFF1F143B),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, MaiPositiveEmerald.copy(alpha = 0.65f))
            ) {
                Text(
                    text = "A4 PDF · ${ov.transactionCount} Txns",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiPositiveEmerald,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = "Select a month below to generate a cleanly formatted PDF summary statement with Executive KPIs, Budget Performance, Category Breakdown, and Chronological Transaction Ledger.",
            fontSize = 11.sp,
            color = MaiLavenderSubtext,
            lineHeight = 16.sp
        )

        // Month Selector Strip for Monthly PDF Export
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            monthlyOptions.forEach { option ->
                val isSelected = !useActiveFilter && selectedMonthlyOption.id == option.id
                Surface(
                    color = if (isSelected) MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        0.8.dp,
                        if (isSelected) Color(0xFFE9D5FF) else MaiCardBorder
                    ),
                    modifier = Modifier
                        .clickable { onSelectMonthlyOption(option) }
                        .testTag("pdf_month_option_${option.id}")
                ) {
                    Text(
                        text = option.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaiLavenderSubtext,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Surface(
                color = if (useActiveFilter) MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    0.8.dp,
                    if (useActiveFilter) Color(0xFFE9D5FF) else MaiCardBorder
                ),
                modifier = Modifier
                    .clickable { onSelectActiveFilter() }
                    .testTag("pdf_month_option_active_filter")
            ) {
                Text(
                    text = "Filter: $activeFilterLabel",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = if (useActiveFilter) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (useActiveFilter) Color.White else MaiLavenderSubtext,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Mini Report Summary Preview Strip for the Selected Month
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0E091E))
                .border(0.8.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PERIOD", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MaiLavenderSubtext)
                Text(
                    text = bundle.timeWindow.displayRangeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("INCOME", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MaiLavenderSubtext)
                Text(
                    text = if (stealthMode) "₹••••" else "+₹${formatIndianAmount(ov.totalIncome)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiPositiveEmerald
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("EXPENSES", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MaiLavenderSubtext)
                Text(
                    text = if (stealthMode) "₹••••" else "-₹${formatIndianAmount(ov.totalExpenses)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiExpenseRose
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("NET SAVINGS", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MaiLavenderSubtext)
                Text(
                    text = if (stealthMode) {
                        "₹••••"
                    } else if (!ov.hasRecordedIncome) {
                        "Unverified"
                    } else {
                        "₹${formatIndianAmount(ov.verifiedSavings)}"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE)
                )
            }
        }

        // Primary PDF Download + Save As Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDownloadPdf,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaiPositiveEmerald,
                    contentColor = Color(0xFF051B11)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.25f)
                    .testTag("insight_export_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Download PDF Report",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download PDF", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }

            OutlinedButton(
                onClick = onSavePdfAs,
                border = BorderStroke(1.dp, Color(0xFFD8B4FE)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("insight_save_pdf_as_button")
            ) {
                Text("Save PDF As…", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE9D5FF))
            }
        }

        // Secondary Actions Row: View Report, Export CSV, Share Text
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

        // Downloaded PDF Confirmation & Open/Share Card
        lastExportedPdf?.let { exported ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A261E))
                    .border(1.dp, MaiPositiveEmerald.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
                    .testTag("insight_pdf_downloaded_banner"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "PDF Downloaded",
                        tint = MaiPositiveEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PDF Report Generated (${exported.pageCount} page${if (exported.pageCount == 1) "" else "s"} · ${max(1, exported.byteSize / 1024)} KB)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Saved to ${exported.savedLocationLabel}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenExportedPdf(exported) },
                        border = BorderStroke(0.8.dp, MaiPositiveEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("insight_open_downloaded_pdf_button")
                    ) {
                        Text("Open PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaiPositiveEmerald)
                    }
                    OutlinedButton(
                        onClick = { onShareExportedPdf(exported) },
                        border = BorderStroke(0.8.dp, Color(0xFFE9D5FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("insight_share_downloaded_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share PDF",
                            tint = Color(0xFFE9D5FF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Share PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE9D5FF))
                    }
                }
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
    onDownloadPdf: () -> Unit,
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
                        .height(280.dp)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = MaiLavenderSubtext)
                    }
                    Button(
                        onClick = {
                            onDownloadPdf()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaiPositiveEmerald,
                            contentColor = Color(0xFF051B11)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("insight_dialog_download_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download PDF",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
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
