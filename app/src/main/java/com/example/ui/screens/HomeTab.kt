package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountabilityEvaluation
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.data.model.KhataFriendRecord
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.NightGuardSchedule
import com.example.data.model.ParsedSmsTransaction
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.ProactiveFinancialInsight
import com.example.data.model.QUICK_ADD_PRESETS
import com.example.data.model.QuickAddPreset
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.data.model.isSameMonth
import com.example.receiver.AutoExpenseDetectionStore
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.DeepForestAbyss
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.EmeraldChartFillBottom
import com.example.ui.theme.EmeraldChartFillTop
import com.example.ui.theme.EmeraldNeonGlow
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.LocalHudTheme
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianDarkBackground
import com.example.ui.theme.ObsidianDarkBorder
import com.example.ui.theme.ObsidianDarkEmeraldAccent
import com.example.ui.theme.ObsidianDarkMutedText
import com.example.ui.theme.ObsidianDarkPositive
import com.example.ui.theme.ObsidianDarkPrimarySurface
import com.example.ui.theme.ObsidianDarkPrimaryText
import com.example.ui.theme.ObsidianDarkSecondarySurface
import com.example.ui.theme.ObsidianDarkSilver
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.getCategoryColor
import com.example.ui.theme.getCategoryIcon
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

private val DASHBOARD_REPEATING_SPEND_PILLS = listOf(
    QuickAddPreset("chai_20", "☕", "Chai", 20L, "Chai & Snacks"),
    QuickAddPreset("metro_40", "🚇", "Metro", 40L, "Transport"),
    QuickAddPreset("lunch_120", "🍔", "Food", 120L, "Food"),
    QuickAddPreset("fuel_200", "⛽", "Fuel", 200L, "Transport")
)

@Composable
fun HomeTab(
    settings: UserSettings,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    todaySpent: Long,
    monthSpent: Long,
    disciplineScore: Int,
    streakDays: Int,
    latestAlert: AccountabilityEvaluation?,
    proactiveInsights: List<ProactiveFinancialInsight> = emptyList(),
    onOpenJarvisWithPrompt: ((String) -> Unit)? = null,
    onDismissAlert: () -> Unit,
    onQuickAdd: (title: String, category: String, amount: Long) -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAddIncome: () -> Unit,
    onOpenSmsParser: () -> Unit,
    onOpenUpiGuardSimulator: () -> Unit,
    onOpenAddGoal: () -> Unit,
    onAddGoalProgress: (SavingsGoalRecord, Long) -> Unit,
    onDeleteExpense: (ExpenseRecord) -> Unit,
    onToggleLockdown: () -> Unit,
    onUpdateDailyLimit: (Long) -> Unit,
    onTriggerMomCall: () -> Unit,
    paymentReminders: List<PaymentReminderRecord> = emptyList(),
    onOpenNightGuardSettings: () -> Unit = {},
    onTestNightGuard: () -> Unit = {},
    onOpenSmartReminders: () -> Unit = {},
    userId: String = "default",
    onEditExpense: (ExpenseRecord) -> Unit = {},
    onOpenFinanceTools: () -> Unit = {},
    onOpenSnapAndTrack: () -> Unit = {},
    onOpenJarvisTab: () -> Unit = {},
    onOpenOnboardingProfileEditor: () -> Unit = {},
    onToggleStealth: () -> Unit = {},
    onNavigateToPassbook: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onCreateBillReminder: ((title: String, amount: Long, category: String, dueEpochMillis: Long) -> Unit)? = null,
    onToggleBillPaid: ((PaymentReminderRecord, Boolean) -> Unit)? = null,
    onDeleteBillReminder: ((PaymentReminderRecord) -> Unit)? = null,
    khataFriends: List<KhataFriendRecord> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hud = LocalHudTheme.current
    val headingFont = LocalAppFontFamily.current
    val dailyLimit = max(50L, settings.dailyLimit)
    val remainingToday = max(0L, dailyLimit - todaySpent)
    val progressRatio = min(1f, todaySpent.toFloat() / dailyLimit.toFloat())
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "dailyBudgetSweep"
    )
    val isOverLimit = todaySpent > dailyLimit
    val isNearLimit = !isOverLimit && progressRatio >= 0.8f
    var showLimitEditor by remember { mutableStateOf(false) }
    var sliderLimit by remember(dailyLimit) { mutableFloatStateOf(dailyLimit.toFloat().coerceIn(100f, 5000f)) }

    // Smart Clipboard Auto-Detection State (checked on ON_RESUME)
    var detectedClipboardSms by remember {
        mutableStateOf<ParsedSmsTransaction?>(AutoExpenseDetectionStore.inspectClipboardForBankSms(context))
    }
    var clipboardAssignedCategory by remember(detectedClipboardSms) {
        mutableStateOf(detectedClipboardSms?.category ?: "Food")
    }

    // Runtime SMS Permission State & Fallback
    var smsPermissionGranted by remember {
        mutableStateOf(AutoExpenseDetectionStore.hasSmsPermissions(context))
    }
    var showSmsPermissionRationaleBanner by remember {
        mutableStateOf(!smsPermissionGranted && !AutoExpenseDetectionStore.isSmsRationaleDismissed(context))
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        smsPermissionGranted = granted
        AutoExpenseDetectionStore.setSmsRationaleDismissed(context, true)
        showSmsPermissionRationaleBanner = false
        if (!granted) {
            // Gracefully fall back to the clipboard parser
            detectedClipboardSms = AutoExpenseDetectionStore.inspectClipboardForBankSms(context)
        }
    }

    val pendingUpiSession by com.example.util.PaymentUtil.activePendingSessionFlow.collectAsStateWithLifecycle()
    val khataStoreVersion by AdvancedKhataStore.versionFlow.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                smsPermissionGranted = AutoExpenseDetectionStore.hasSmsPermissions(context)
                com.example.util.PaymentUtil.onAppResumedAfterUpiIntent(context)
                val clipParsed = AutoExpenseDetectionStore.inspectClipboardForBankSms(context)
                if (clipParsed != null) {
                    detectedClipboardSms = clipParsed
                    clipboardAssignedCategory = clipParsed.category
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val ringColor = when {
        settings.lockdownActive -> CrimsonRuby
        isOverLimit -> CrimsonRuby
        isNearLimit -> ChampagneGold
        else -> hud.accent
    }

    val totalIncomeAll = remember(expenses) {
        expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
    }
    val totalExpenseAll = remember(expenses) {
        expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
    }
    val totalBalance = remember(totalIncomeAll, totalExpenseAll) {
        totalIncomeAll - totalExpenseAll
    }

    val nowMillisTop = System.currentTimeMillis()
    val monthlyNetDelta = remember(expenses, monthSpent, dailyLimit) {
        val thisMonthIncome = expenses.filter { isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, nowMillisTop) }.sumOf { it.amount }
        if (thisMonthIncome > 0L || monthSpent > 0L) {
            thisMonthIncome - monthSpent
        } else {
            0L
        }
    }
    val savedMonthlyBudget = remember(userId, dailyLimit, expenses.size) {
        KharcheeProfileAndToolsStore.getProfileMetadata(context, userId, "", dailyLimit = dailyLimit).monthlyBudget
    }
    val monthlyBudgetTargetTop = when {
        savedMonthlyBudget > 0L -> savedMonthlyBudget
        expenses.isEmpty() -> 0L
        else -> max(1500L, dailyLimit * 30L)
    }

    var isFinancialHealthExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaiDeepCanvas)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaiDeepCanvas)
                .testTag("home_tab_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Post-Payment UPI Sync Confirmation Badge (shown when returning from UPI app)
            if (pendingUpiSession?.hasReturnedToApp == true) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0A241C))
                            .border(1.dp, Color(0xFF2EE59D).copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("home_post_payment_sync_badge"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ ${com.example.util.PaymentUtil.POST_PAYMENT_SYNC_MESSAGE}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2EE59D),
                            lineHeight = 15.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB8B0D4),
                            modifier = Modifier
                                .clickable { com.example.util.PaymentUtil.clearPendingUpiSession(context) }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // 1. HERO BALANCE CARD: Ultra-clean obsidian card showing Net Balance, monthly burn rate, and subtle glowing edge
            item {
                CredBlackTitaniumCard(
                    totalBalance = totalBalance,
                    disciplineScore = disciplineScore,
                    streakDays = streakDays,
                    remainingToday = remainingToday,
                    stealthMode = settings.stealthMode,
                    isPro = settings.isPro,
                    proTier = settings.proTier,
                    monthSpent = monthSpent,
                    onToggleStealth = onToggleStealth
                )
            }

            // 2. 1-TAP QUICK SPEND STRIP: Compact horizontal chips [☕ ₹20 Chai] [🚇 ₹40 Metro] [🍔 ₹120 Food] directly under the card
            item {
                OneTapQuickRepeatingSpendStrip(
                    onInstantLogSpend = { title, category, amount ->
                        onQuickAdd(title, category, amount)
                    }
                )
            }

            // 3. PRIMARY ACTION ROW: 3 clean, evenly-spaced rounded buttons: [+ Add Spend] [+ Add Income] [Split/Khata]
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_primary_action_row"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onOpenAddExpense,
                        enabled = !settings.lockdownActive,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White,
                            disabledContainerColor = ElevatedGraphite,
                            disabledContentColor = MutedIvory
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_expense_button")
                    ) {
                        Text(
                            text = "+ Add Spend",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenAddIncome,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF2EE59D).copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF0D1F1B),
                            contentColor = Color(0xFF2EE59D)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_income_button")
                    ) {
                        Text(
                            text = "+ Add Income",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToVault,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF151128),
                            contentColor = Color(0xFFD8B4FE)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("open_khata_split_button")
                    ) {
                        Text(
                            text = "Split/Khata",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 4. RECENT SPENDS: Strictly limited to the last 3 transactions with clean category icons and single "View All →" button
            item {
                HomeRecentActivitySection(
                    expenses = expenses,
                    stealthMode = settings.stealthMode,
                    onViewAllClicked = onNavigateToPassbook,
                    onEditTransaction = onEditExpense,
                    onAddTransaction = onOpenAddExpense
                )
            }

            // 5. EXPANDABLE "FINANCIAL HEALTH & DUES" DRAWER CARD (Collapses 2x2 KPI Grid & Smart Bill Radar out of main scroll)
            item {
                val remainingMonthlyBudget = (monthlyBudgetTargetTop - monthSpent).coerceAtLeast(0L)
                val pendingBillCount = remember(paymentReminders) {
                    paymentReminders.count { !it.isPaid }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0D0E17))
                        .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .testTag("financial_health_and_bills_drawer"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFinancialHealthExpanded = !isFinancialHealthExpanded }
                            .testTag("financial_health_drawer_toggle"),
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF17122E))
                                    .border(0.8.dp, Color(0xFF7C3AED).copy(alpha = 0.55f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Financial Health & Dues",
                                    tint = Color(0xFFD8B4FE),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Financial Health & Dues",
                                    fontFamily = headingFont,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Score ${disciplineScore.coerceIn(0, 900)}/900 · ${if (pendingBillCount == 0) "No pending dues" else "$pendingBillCount due(s) pending"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8B0D4),
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF151329),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, Color(0xFF7C3AED).copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = if (isFinancialHealthExpanded) "Hide ↑" else "Expand ↓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD8B4FE),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isFinancialHealthExpanded) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 2x2 KPI Grid
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_core_kpis_grid"),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HomeCoreKpiCard(
                                        icon = Icons.Default.TrendingUp,
                                        label = "Total Income",
                                        value = if (settings.stealthMode) "₹••••" else "+₹${formatIndianAmount(totalIncomeAll)}",
                                        accentColor = Color(0xFF2EE59D),
                                        onClick = onOpenAddIncome,
                                        modifier = Modifier.weight(1f)
                                    )
                                    HomeCoreKpiCard(
                                        icon = Icons.Default.TrendingDown,
                                        label = "Total Outflow",
                                        value = if (settings.stealthMode) "₹••••" else "-₹${formatIndianAmount(totalExpenseAll)}",
                                        accentColor = Color(0xFFFF4D8D),
                                        onClick = onOpenAddExpense,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HomeCoreKpiCard(
                                        icon = Icons.Default.AccountBalanceWallet,
                                        label = "Budget Left",
                                        value = if (settings.stealthMode) "₹••••" else "₹${formatIndianAmount(remainingMonthlyBudget)}",
                                        accentColor = Color(0xFFA855F7),
                                        onClick = { showLimitEditor = !showLimitEditor },
                                        modifier = Modifier.weight(1f)
                                    )
                                    HomeCoreKpiCard(
                                        icon = Icons.Default.WorkspacePremium,
                                        label = "Discipline",
                                        value = "${disciplineScore.coerceIn(0, 900)}/900",
                                        accentColor = Color(0xFFD8B4FE),
                                        onClick = onNavigateToAnalytics,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Pending Khata Payouts (Dena Hai) with 1-Tap [⚡ Pay ₹Amount] UPI Intent
                            val pendingDenaHaiContacts = remember(userId, khataStoreVersion, khataFriends) {
                                AdvancedKhataStore.syncLegacyFirestoreFriendsIfNeeded(context, userId, khataFriends)
                                AdvancedKhataStore.getAllContactStates(context, userId).filter {
                                    !it.isSettled && it.netDirection == com.example.data.model.KhataDirection.DENA_HAI && it.outstandingAmount > 0L
                                }
                            }
                            if (pendingDenaHaiContacts.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0xFF130D24))
                                        .border(1.dp, Color(0xFFFF4D8D).copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PENDING KHATA PAYOUTS (DENA HAI)",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.8.sp,
                                            color = Color(0xFFFF4D8D)
                                        )
                                        Text(
                                            text = "Open Khata →",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD8B4FE),
                                            modifier = Modifier.clickable { onNavigateToVault() }
                                        )
                                    }
                                    pendingDenaHaiContacts.take(3).forEach { contactState ->
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
                                                    text = contactState.contact.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = com.example.util.PaymentUtil.resolvePayeeVpa(
                                                        contactState.contact.phone,
                                                        contactState.contact.name
                                                    ),
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFB8B0D4)
                                                )
                                            }
                                            Surface(
                                                color = Color(0xFF2EE59D),
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                                                modifier = Modifier
                                                    .clickable {
                                                        val vpa = com.example.util.PaymentUtil.resolvePayeeVpa(
                                                            contactState.contact.phone,
                                                            contactState.contact.name
                                                        )
                                                        com.example.util.PaymentUtil.launchUpiPaymentIntent(
                                                            context = context,
                                                            payeeVpa = vpa,
                                                            payeeName = contactState.contact.name,
                                                            amount = contactState.outstandingAmount.toString(),
                                                            txnNote = "Settle Khata Due - Kharchee",
                                                            contactId = contactState.contact.id
                                                        )
                                                    }
                                                    .testTag("home_khata_due_instant_pay_${contactState.contact.name}")
                                            ) {
                                                Text(
                                                    text = if (settings.stealthMode) "⚡ Pay ₹•••" else "⚡ Pay ₹${formatIndianAmount(contactState.outstandingAmount)}",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF040508),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Smart Bill Radar / Upcoming Bills
                            MaiScreenUpcomingBillsCard(
                                paymentReminders = paymentReminders,
                                stealthMode = settings.stealthMode,
                                onOpenBillsManager = onOpenSmartReminders,
                                onCreateBill = onCreateBillReminder,
                                onToggleBillPaid = onToggleBillPaid,
                                onDeleteBill = onDeleteBillReminder
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OneTapQuickRepeatingSpendStrip(
    onInstantLogSpend: (title: String, category: String, amount: Long) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("one_tap_quick_repeating_spend_strip"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(
            items = DASHBOARD_REPEATING_SPEND_PILLS,
            key = { it.id },
            contentType = { "quick_spend_chip" }
        ) { pill ->
            Surface(
                color = Color(0xFF130E24),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF7C3AED).copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        onInstantLogSpend(pill.label, pill.category, pill.amount)
                    }
                    .testTag("instant_spend_pill_${pill.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = pill.emoji,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "₹${pill.amount}",
                        fontFamily = headingFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = pill.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD8B4FE),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun SmsAutoTrackPermissionRationaleBanner(
    onAllowClicked: () -> Unit,
    onFallbackClipboardClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A1038), Color(0xFF0E0B1C))
                )
            )
            .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("home_sms_permission_rationale_banner"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF7C3AED).copy(alpha = 0.24f))
                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = "Auto-Track SMS",
                    tint = Color(0xFFD8B4FE),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto-Track Spends",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Allow Kharchee to read transactional SMS so you never have to log expenses manually.",
                    fontSize = 11.sp,
                    color = Color(0xFFB8B0D4),
                    lineHeight = 15.sp
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAllowClicked,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_allow_sms_permission_button")
            ) {
                Text(
                    text = "Allow Auto-Track SMS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            OutlinedButton(
                onClick = onFallbackClipboardClicked,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, Color(0xFF3E2D6E)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                modifier = Modifier.testTag("home_sms_fallback_clipboard_button")
            ) {
                Text(
                    text = "Use Clipboard",
                    fontSize = 11.sp,
                    color = Color(0xFFD8B4FE),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SmartClipboardDetectedFloatingCard(
    detected: ParsedSmsTransaction,
    selectedCategory: String,
    onCycleCategory: () -> Unit,
    onLogNowOneTap: () -> Unit,
    onDismiss: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF7C3AED))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1342).copy(alpha = 0.96f),
                        Color(0xFF0E0A1F).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFA855F7), Color(0xFF38BDF8), Color(0xFF7C3AED))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
            .testTag("smart_clipboard_sms_detected_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "⚡ SMS Detected: Spent ₹${detected.amount} at ${detected.merchant}. Log this expense?",
            fontFamily = headingFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 18.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // [Auto-Assign: Food]
            Surface(
                color = Color(0xFF140D2A),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, Color(0xFFA855F7).copy(alpha = 0.7f)),
                modifier = Modifier
                    .clickable { onCycleCategory() }
                    .testTag("clipboard_auto_assign_category_pill")
            ) {
                Text(
                    text = "Auto-Assign: $selectedCategory",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }

            // [✓ Log Now (1-Tap)]
            Button(
                onClick = onLogNowOneTap,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("clipboard_log_now_one_tap_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Log Now",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Log Now (1-Tap)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            // [✕ Dismiss]
            Surface(
                color = Color(0xFF1A142D),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, Color(0xFF3A2D5C)),
                modifier = Modifier
                    .clickable { onDismiss() }
                    .testTag("clipboard_dismiss_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFFB8B0D4),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Dismiss",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB8B0D4),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCoreKpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D0E17))
            .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFB8B0D4),
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Text(
            text = value,
            fontFamily = headingFont,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeRecentActivitySection(
    expenses: List<ExpenseRecord>,
    stealthMode: Boolean,
    onViewAllClicked: () -> Unit,
    onEditTransaction: (ExpenseRecord) -> Unit,
    onAddTransaction: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val dateFmt = remember { SimpleDateFormat("dd MMM, h:mm a", Locale.ENGLISH) }
    val recentThree = remember(expenses) {
        expenses.sortedByDescending { it.txEpochMillis }.take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0D0E17))
            .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("home_recent_activity_widget"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Spends",
                fontFamily = headingFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )

            Surface(
                color = Color(0xFF151329),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color(0xFF7C3AED)),
                modifier = Modifier
                    .clickable { onViewAllClicked() }
                    .testTag("home_recent_activity_view_all_button")
            ) {
                Text(
                    text = "View All →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        if (recentThree.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF151329))
                    .clickable { onAddTransaction() }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .testTag("home_empty_transactions_state"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No transactions yet. Tap '+' to log your first spend.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEAE4F8),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFA855F7), Color(0xFF7C3AED))
                            )
                        )
                        .testTag("home_empty_state_add_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        } else {
            recentThree.forEach { tx ->
                val isIncome = isIncomeTransaction(tx)
                val amountColor = if (isIncome) Color(0xFF2EE59D) else Color(0xFFFF4D8D)
                val formattedTime = remember(tx.txEpochMillis) {
                    com.example.util.PerformancePreferences.preFormatDate(tx.txEpochMillis)
                }
                val formattedAmt = remember(tx.amount, isIncome) {
                    val base = com.example.util.PerformancePreferences.preFormatInr(tx.amount)
                    "${if (isIncome) "+" else "-"}$base"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF151329))
                        .border(0.7.dp, Color(0xFF2B224C), RoundedCornerShape(14.dp))
                        .clickable { onEditTransaction(tx) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        RecentSpendBrandVectorBadge(
                            title = tx.title,
                            category = tx.category,
                            isIncome = isIncome
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = tx.title.ifBlank { tx.category },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${tx.category} · $formattedTime",
                                fontSize = 11.sp,
                                color = Color(0xFFB8B0D4),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = if (stealthMode) "₹••••" else formattedAmt,
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSpendBrandVectorBadge(
    title: String,
    category: String,
    isIncome: Boolean
) {
    val combined = "${title.lowercase()} ${category.lowercase()}"
    val badgeType = when {
        combined.contains("chai") || combined.contains("tea") || combined.contains("coffee") || combined.contains("snack") || combined.contains("tapri") || combined.contains("samosa") -> "chai"
        combined.contains("food") || combined.contains("zomato") || combined.contains("swiggy") || combined.contains("dinner") || combined.contains("lunch") || combined.contains("restaurant") -> "food_delivery"
        combined.contains("grocery") || combined.contains("mart") || combined.contains("blinkit") || combined.contains("zepto") || combined.contains("instamart") || combined.contains("milk") -> "grocery"
        combined.contains("recharge") || combined.contains("tech") || combined.contains("bill") || combined.contains("wifi") || combined.contains("electricity") || combined.contains("sim") -> "recharge_tech"
        combined.contains("friend") || combined.contains("p2p") || combined.contains("khata") || combined.contains("transfer") || combined.contains("split") || isIncome -> "p2p"
        else -> "recharge_tech"
    }

    val badgeAccent = when (badgeType) {
        "chai" -> Color(0xFFF59E0B)          // Warm Amber
        "food_delivery" -> Color(0xFFFF6B35) // Neon Orange
        "grocery" -> Color(0xFF10B981)       // Emerald
        "recharge_tech" -> Color(0xFF3B82F6) // Electric Blue
        else -> Color(0xFFA855F7)            // Neon Violet (P2P / Friends)
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        badgeAccent.copy(alpha = 0.26f),
                        badgeAccent.copy(alpha = 0.10f)
                    )
                )
            )
            .border(0.9.dp, badgeAccent.copy(alpha = 0.55f), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            when (badgeType) {
                "chai" -> {
                    // Cutting Tea Glass icon + steam waves
                    val glassPath = Path().apply {
                        moveTo(w * 0.22f, h * 0.34f)
                        lineTo(w * 0.78f, h * 0.34f)
                        lineTo(w * 0.68f, h * 0.90f)
                        lineTo(w * 0.32f, h * 0.90f)
                        close()
                    }
                    drawPath(path = glassPath, color = badgeAccent, style = stroke)
                    drawLine(
                        color = badgeAccent.copy(alpha = 0.75f),
                        start = Offset(w * 0.27f, h * 0.56f),
                        end = Offset(w * 0.73f, h * 0.56f),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Steam waves
                    drawLine(
                        color = badgeAccent,
                        start = Offset(w * 0.38f, h * 0.22f),
                        end = Offset(w * 0.38f, h * 0.10f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = badgeAccent,
                        start = Offset(w * 0.62f, h * 0.22f),
                        end = Offset(w * 0.62f, h * 0.10f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                "food_delivery" -> {
                    // Delivery Box + Cloche / Utensil silhouette
                    drawRoundRect(
                        color = badgeAccent,
                        topLeft = Offset(w * 0.16f, h * 0.36f),
                        size = Size(w * 0.68f, h * 0.52f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                        style = stroke
                    )
                    val handlePath = Path().apply {
                        moveTo(w * 0.34f, h * 0.36f)
                        quadraticTo(w * 0.50f, h * 0.12f, w * 0.66f, h * 0.36f)
                    }
                    drawPath(path = handlePath, color = badgeAccent, style = stroke)
                    drawCircle(
                        color = badgeAccent,
                        radius = 2.dp.toPx(),
                        center = Offset(w * 0.50f, h * 0.62f)
                    )
                }
                "grocery" -> {
                    // Shopping Basket in Emerald
                    val basketPath = Path().apply {
                        moveTo(w * 0.14f, h * 0.44f)
                        lineTo(w * 0.86f, h * 0.44f)
                        lineTo(w * 0.76f, h * 0.88f)
                        lineTo(w * 0.24f, h * 0.88f)
                        close()
                    }
                    drawPath(path = basketPath, color = badgeAccent, style = stroke)
                    drawLine(
                        color = badgeAccent,
                        start = Offset(w * 0.34f, h * 0.44f),
                        end = Offset(w * 0.46f, h * 0.18f),
                        strokeWidth = 1.7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = badgeAccent,
                        start = Offset(w * 0.66f, h * 0.44f),
                        end = Offset(w * 0.54f, h * 0.18f),
                        strokeWidth = 1.7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                "recharge_tech" -> {
                    // Lightning Bolt in Electric Blue
                    val boltPath = Path().apply {
                        moveTo(w * 0.56f, h * 0.10f)
                        lineTo(w * 0.24f, h * 0.54f)
                        lineTo(w * 0.48f, h * 0.54f)
                        lineTo(w * 0.42f, h * 0.90f)
                        lineTo(w * 0.76f, h * 0.44f)
                        lineTo(w * 0.52f, h * 0.44f)
                        close()
                    }
                    drawPath(path = boltPath, color = badgeAccent, style = stroke)
                }
                else -> {
                    // P2P / Friends User Transfer Badge in Neon Violet
                    drawCircle(
                        color = badgeAccent,
                        radius = w * 0.18f,
                        center = Offset(w * 0.42f, h * 0.32f),
                        style = stroke
                    )
                    val shoulderPath = Path().apply {
                        moveTo(w * 0.16f, h * 0.82f)
                        quadraticTo(w * 0.42f, h * 0.54f, w * 0.68f, h * 0.82f)
                    }
                    drawPath(path = shoulderPath, color = badgeAccent, style = stroke)
                    // Transfer arrow
                    drawLine(
                        color = badgeAccent,
                        start = Offset(w * 0.64f, h * 0.34f),
                        end = Offset(w * 0.88f, h * 0.34f),
                        strokeWidth = 1.7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun CredBlackTitaniumCard(
    totalBalance: Long,
    disciplineScore: Int,
    streakDays: Int,
    remainingToday: Long,
    stealthMode: Boolean,
    isPro: Boolean = false,
    proTier: String = "free",
    monthSpent: Long = 0L,
    onToggleStealth: (() -> Unit)? = null
) {
    val headingFont = LocalAppFontFamily.current
    val pauseHeavyVisuals = com.example.util.PerformancePreferences.shouldPauseAnimations()
    val formattedBalance = remember(totalBalance, stealthMode) {
        if (stealthMode) "₹••••••"
        else if (totalBalance >= 0) com.example.util.PerformancePreferences.preFormatInr(totalBalance)
        else "-${com.example.util.PerformancePreferences.preFormatInr(totalBalance)}"
    }
    val formattedMonthBurn = remember(monthSpent, stealthMode) {
        if (stealthMode) "₹••••/mo" else "${com.example.util.PerformancePreferences.preFormatInr(monthSpent)}/mo"
    }
    val formattedSafeToday = remember(remainingToday, stealthMode) {
        if (stealthMode) "₹•••" else com.example.util.PerformancePreferences.preFormatInr(remainingToday)
    }

    val glowingEdgeBrush = remember(pauseHeavyVisuals) {
        if (pauseHeavyVisuals) {
            Brush.linearGradient(
                colors = listOf(Color(0xFF7C3AED), Color(0xFF7C3AED))
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFA855F7).copy(alpha = 0.85f),
                    Color(0xFF7C3AED).copy(alpha = 0.55f),
                    Color(0xFF3E2D6E).copy(alpha = 0.45f),
                    Color(0xFFA855F7).copy(alpha = 0.80f)
                )
            )
        }
    }

    val cleanObsidianSurface = remember(pauseHeavyVisuals) {
        if (pauseHeavyVisuals) {
            Brush.linearGradient(
                colors = listOf(Color(0xFF0C0A18), Color(0xFF0C0A18))
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0A0B14),
                    Color(0xFF120E24),
                    Color(0xFF080911)
                ),
                start = Offset(0f, 0f),
                end = Offset(900f, 420f)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (pauseHeavyVisuals) Modifier
                else Modifier.shadow(18.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF7C3AED).copy(alpha = 0.45f))
            )
            .clip(RoundedCornerShape(22.dp))
            .background(cleanObsidianSurface)
            .border(
                width = 1.2.dp,
                brush = glowingEdgeBrush,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(enabled = onToggleStealth != null) { onToggleStealth?.invoke() }
            .padding(20.dp)
            .testTag("cred_black_titanium_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NET BALANCE",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFFB8B0D4)
            )
            Surface(
                color = Color(0xFF191330),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 0.8.dp,
                    color = if (stealthMode) Color(0xFFA855F7) else Color(0xFF3E2D6E)
                ),
                modifier = Modifier
                    .clickable(enabled = onToggleStealth != null) { onToggleStealth?.invoke() }
                    .testTag("hero_card_hide_balance_eye_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (stealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (stealthMode) "Reveal Balance" else "Hide Balance on Dashboard",
                        tint = if (stealthMode) Color(0xFFD8B4FE) else Color(0xFF8E84AD),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (stealthMode) "Hidden ••••" else "Hide",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stealthMode) Color(0xFFD8B4FE) else Color(0xFF8E84AD)
                    )
                }
            }
        }

        Text(
            text = formattedBalance,
            fontFamily = headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 34.sp,
            color = Color.White,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF07070E).copy(alpha = 0.72f))
                .border(0.8.dp, Color(0xFF2B224C), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MONTHLY BURN RATE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = Color(0xFF8E84AD),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = formattedMonthBurn,
                    fontFamily = headingFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD8B4FE)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SAFE TODAY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = Color(0xFF8E84AD),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = formattedSafeToday,
                    fontFamily = headingFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2EE59D)
                )
            }
        }
    }
}

@Composable
fun ProactiveInsightBannerCard(
    insight: ProactiveFinancialInsight,
    onAskJarvis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val jarvisCyan = Color(0xFF00E5FF)

    val accentBorder = when (insight.severity) {
        "warning" -> CrimsonRuby
        "surge" -> jarvisCyan
        "positive" -> SophisticatedEmerald
        else -> jarvisCyan
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(RichGraphite)
            .border(
                width = 1.dp,
                color = HairlinePlatinum,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
            .testTag("proactive_insight_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = insight.iconEmoji,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "J.A.R.V.I.S. PROACTIVE TELEMETRY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = jarvisCyan
                    )
                }

                if (insight.highlightStat.isNotBlank()) {
                    Surface(
                        color = accentBorder.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.6.dp, accentBorder.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = insight.highlightStat,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = accentBorder,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Text(
                text = insight.title,
                fontFamily = headingFont,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WarmIvory,
                fontSize = 15.sp
            )

            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodySmall,
                color = MutedPlatinum,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = ObsidianDarkBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, jarvisCyan.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clickable { onAskJarvis() }
                        .testTag("ask_jarvis_proactive_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Ask J.A.R.V.I.S.",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = jarvisCyan
                        )
                        Text(
                            text = "➔",
                            color = jarvisCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountabilityBannerCard(
    evaluation: AccountabilityEvaluation,
    onDismiss: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val borderColor = when (evaluation.urgency) {
        "critical" -> CrimsonRuby
        "warning" -> ChampagneGold
        else -> SophisticatedEmerald
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RichGraphite)
            .border(1.dp, borderColor.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "UPI Accountability Alert",
            tint = borderColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = evaluation.headline,
                fontFamily = headingFont,
                style = MaterialTheme.typography.titleMedium,
                color = WarmIvory,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = evaluation.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedPlatinum,
                fontSize = 12.sp
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss Alert",
                tint = MutedIvory,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MetricMiniRow(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ElevatedGraphite)
            .border(0.6.dp, HairlinePlatinum, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            letterSpacing = 0.8.sp,
            color = MutedIvory
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor
        )
    }
}

@Composable
private fun UtilityActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(RichGraphite)
            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(ElevatedGraphite)
                .border(0.8.dp, tint.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = WarmIvory,
            maxLines = 1
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = MutedIvory,
            maxLines = 1
        )
    }
}

@Composable
fun TransactionRowCard(
    expense: ExpenseRecord,
    stealthMode: Boolean,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isIncome = isIncomeTransaction(expense)
    val catColor = getCategoryColor(expense.category)
    val catIcon = getCategoryIcon(expense.category)
    val dateString = remember(expense.txEpochMillis) {
        com.example.util.PerformancePreferences.preFormatDate(expense.txEpochMillis)
    }
    val formattedAmount = remember(expense.amount, isIncome, stealthMode) {
        when {
            stealthMode -> "₹••••••"
            isIncome -> "+${com.example.util.PerformancePreferences.preFormatInr(expense.amount)}"
            else -> "-${com.example.util.PerformancePreferences.preFormatInr(expense.amount)}"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RichGraphite)
            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(ElevatedGraphite)
                .border(0.8.dp, catColor.copy(alpha = 0.38f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = catIcon,
                contentDescription = expense.category,
                tint = catColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.titleMedium,
                color = WarmIvory,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${expense.category} · ${expense.paymentMode} · $dateString",
                style = MaterialTheme.typography.bodySmall,
                color = MutedIvory,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (expense.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = expense.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedPlatinum.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        val headingFont = LocalAppFontFamily.current
        Text(
            text = formattedAmount,
            fontFamily = headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = if (isIncome) SophisticatedEmerald else WarmIvory
        )

        if (expense.id.isNotBlank()) {
            if (onEdit != null) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp).testTag("edit_tx_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${expense.title}",
                        tint = ChampagneGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp).testTag("delete_tx_${expense.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete ${expense.title}",
                    tint = MutedIvory,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
