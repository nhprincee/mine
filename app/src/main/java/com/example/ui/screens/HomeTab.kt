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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import kotlinx.coroutines.delay

private val DASHBOARD_REPEATING_SPEND_PILLS = listOf(
    QuickAddPreset("chai_20", "☕", "Chai", 20L, "Chai & Snacks"),
    QuickAddPreset("metro_40", "🚇", "Metro", 40L, "Transport"),
    QuickAddPreset("auto_50", "🛵", "Auto", 50L, "Transport"),
    QuickAddPreset("snacks_120", "🍔", "Snacks", 120L, "Food"),
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
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantMap ->
        val granted = grantMap.values.all { it }
        smsPermissionGranted = granted
        AutoExpenseDetectionStore.setSmsRationaleDismissed(context, true)
        showSmsPermissionRationaleBanner = false
        if (!granted) {
            // Gracefully fall back to the clipboard parser
            detectedClipboardSms = AutoExpenseDetectionStore.inspectClipboardForBankSms(context)
        }
    }
    var pendingUpiVerificationContact by remember { mutableStateOf<com.example.data.model.KhataContactComputedState?>(null) }

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
        else -> max(1500L, dailyLimit * 30L)
    }
    val dailySafeBurnTelemetry = remember(monthlyBudgetTargetTop, monthSpent, todaySpent, dailyLimit) {
        HomeDailyCockpitEngine.computeDailySafeBurnTelemetry(
            monthlyBudgetTarget = monthlyBudgetTargetTop,
            monthSpent = monthSpent,
            todaySpent = todaySpent,
            fallbackDailyLimit = dailyLimit
        )
    }

    val khataContactStates = remember(context, userId, khataStoreVersion, khataFriends) {
        AdvancedKhataStore.syncLegacyFirestoreFriendsIfNeeded(context, userId, khataFriends)
        AdvancedKhataStore.getAllContactStates(context, userId)
    }
    val khataSummaryMetrics = remember(khataContactStates) {
        AdvancedKhataStore.computeSummaryMetrics(khataContactStates)
    }
    val hallOfShameDefaulters = remember(khataContactStates) {
        HomeDailyCockpitEngine.computeHallOfShameDefaulters(khataContactStates)
    }

    var midnightShieldEnabled by remember(context, userId) {
        mutableStateOf(HomeDailyCockpitEngine.isMidnightMunchiesShieldEnabled(context, userId))
    }
    var quarantinedMidnightEntries by remember(context, userId) {
        mutableStateOf(HomeDailyCockpitEngine.getQuarantinedEntries(context, userId))
    }

    var customQuickChips by remember(context, userId) {
        mutableStateOf(HomeDailyCockpitEngine.getCustomQuickChips(context, userId))
    }
    var showCreateCustomChipDialog by remember { mutableStateOf(false) }
    var activeQuickSpendUndoEvent by remember { mutableStateOf<QuickSpendUndoEvent?>(null) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(activeQuickSpendUndoEvent?.timestampMillis) {
        if (activeQuickSpendUndoEvent != null) {
            delay(4000L)
            activeQuickSpendUndoEvent = null
        }
    }

    var isFinancialHealthExpanded by remember { mutableStateOf(true) }

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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // =========================================================================
            // SECTION 1 — INTERACTIVE PIE/DONUT CHART HERO + OXYGEN BURN TANK
            // =========================================================================
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

            val currentClipSms = detectedClipboardSms
            if (currentClipSms != null) {
                item {
                    SmartClipboardDetectedFloatingCard(
                        detected = currentClipSms,
                        selectedCategory = clipboardAssignedCategory,
                        onCycleCategory = {
                            val idx = EXPENSE_CATEGORIES.indexOf(clipboardAssignedCategory)
                            clipboardAssignedCategory = EXPENSE_CATEGORIES[(idx + 1).coerceAtLeast(0) % EXPENSE_CATEGORIES.size]
                        },
                        onLogNowOneTap = {
                            onQuickAdd(
                                currentClipSms.merchant,
                                clipboardAssignedCategory,
                                currentClipSms.amount
                            )
                            detectedClipboardSms = null
                        },
                        onDismiss = {
                            detectedClipboardSms = null
                        }
                    )
                }
            }

            // 1. GORGEOUS INTERACTIVE DONUT / PIE CHART HERO
            item {
                HomeInteractiveDonutChartHero(
                    expenses = expenses,
                    monthlyBudgetTarget = monthlyBudgetTargetTop,
                    monthSpent = monthSpent,
                    todaySpent = todaySpent,
                    dailyLimit = dailyLimit,
                    streakDays = streakDays,
                    disciplineScore = disciplineScore,
                    stealthMode = settings.stealthMode,
                    safeBurnTelemetry = dailySafeBurnTelemetry,
                    onToggleStealth = onToggleStealth
                )
            }

            // 2. OXYGEN BURN TANK (RUNWAY PROGRESS BAR) IMMEDIATELY BELOW DONUT CHART
            item {
                MonthlyOxygenTankProgressBar(
                    monthlyBudgetTarget = monthlyBudgetTargetTop,
                    monthSpent = monthSpent,
                    todaySpent = todaySpent,
                    dailyLimit = dailyLimit,
                    stealthMode = settings.stealthMode
                )
            }

            // 3. SLEEK 1-TAP QUICK SPEND CHIPS ROW IMMEDIATELY BELOW OXYGEN TANK
            item {
                OneTapQuickRepeatingSpendStrip(
                    customChips = customQuickChips,
                    onInstantLogSpend = { emoji, title, category, amount ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (HomeDailyCockpitEngine.shouldQuarantineMidnightExpense(context, userId, category)) {
                            quarantinedMidnightEntries = HomeDailyCockpitEngine.quarantineMidnightExpense(
                                context = context,
                                userId = userId,
                                title = title,
                                category = category,
                                amountInr = amount
                            )
                        } else {
                            onQuickAdd(title, category, amount)
                            activeQuickSpendUndoEvent = QuickSpendUndoEvent(
                                timestampMillis = System.currentTimeMillis(),
                                emoji = emoji,
                                label = title,
                                category = category,
                                amount = amount
                            )
                        }
                    },
                    onOpenAddCustomChip = { showCreateCustomChipDialog = true },
                    onDeleteCustomChip = { chipId ->
                        customQuickChips = HomeDailyCockpitEngine.deleteCustomQuickChip(context, userId, chipId)
                    }
                )
            }

            // 4. HALL OF SHAME OVERDUE CAROUSEL (>₹500 and >7 days late with 1-tap WhatsApp Vasooli)
            item {
                HallOfShameOverdueCarousel(
                    defaulters = hallOfShameDefaulters,
                    stealthMode = settings.stealthMode,
                    onTriggerWhatsAppVasooli = { defaulter ->
                        val profileMeta = KharcheeProfileAndToolsStore.getProfileMetadata(
                            context = context,
                            userId = userId,
                            defaultName = "Kharchee Member",
                            dailyLimit = dailyLimit
                        )
                        val senderName = profileMeta.customDisplayName.ifBlank { "Kharchee Member" }
                        val senderUpi = settings.myUpiId
                        val savageCaption = com.example.util.KhataSplitCardGenerator.buildVasooliToneMessage(
                            tone = com.example.util.VasooliReminderTone.BHAICHARA_SAVAGE,
                            friendName = defaulter.friendName,
                            amountInr = defaulter.overdueAmountInr,
                            itemSummary = "${defaulter.itemSummary} (${defaulter.daysLate}d overdue)",
                            userDisplayName = senderName,
                            userUpiId = senderUpi
                        )
                        com.example.util.KhataSplitCardGenerator.shareSplitReceiptCardOnWhatsApp(
                            context = context,
                            contactState = defaulter.contactState,
                            userDisplayName = senderName,
                            userUpiId = senderUpi,
                            specificEntryDescription = "${defaulter.itemSummary} (${defaulter.daysLate}d overdue)",
                            specificEntryAmount = defaulter.overdueAmountInr,
                            customCaptionText = savageCaption
                        )
                    },
                    onOpenKhataLedger = onNavigateToVault
                )
            }

            // 5. MIDNIGHT MUNCHIES & IMPULSE SHIELD (11 PM - 5 AM QUARANTINE HOLD)
            item {
                MidnightMunchiesImpulseShieldCard(
                    shieldEnabled = midnightShieldEnabled,
                    quarantinedEntries = quarantinedMidnightEntries,
                    stealthMode = settings.stealthMode,
                    onToggleShield = { enabled ->
                        midnightShieldEnabled = enabled
                        HomeDailyCockpitEngine.setMidnightMunchiesShieldEnabled(context, userId, enabled)
                    },
                    onConfirmQuarantinedEntry = { entry ->
                        onQuickAdd(entry.title, entry.category, entry.amountInr)
                        quarantinedMidnightEntries = HomeDailyCockpitEngine.dismissQuarantinedEntry(
                            context = context,
                            userId = userId,
                            entryId = entry.id
                        )
                    },
                    onDiscardQuarantinedEntry = { entry ->
                        quarantinedMidnightEntries = HomeDailyCockpitEngine.dismissQuarantinedEntry(
                            context = context,
                            userId = userId,
                            entryId = entry.id
                        )
                    },
                    onSimulateQuarantineHold = {
                        quarantinedMidnightEntries = HomeDailyCockpitEngine.quarantineMidnightExpense(
                            context = context,
                            userId = userId,
                            title = "1:15 AM Swiggy Midnight Combo",
                            category = "Food",
                            amountInr = 480L
                        )
                    }
                )
            }

            // =========================================================================
            // SECTION 2 — JARVIS INTELLIGENCE
            // =========================================================================
            if (latestAlert != null) {
                item {
                    AccountabilityBannerCard(
                        evaluation = latestAlert,
                        onDismiss = onDismissAlert
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_section_jarvis_intelligence"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MaiScreenJarvisInsightBanner(
                        monthSpent = monthSpent,
                        monthlyBudgetTarget = monthlyBudgetTargetTop,
                        proactiveInsights = proactiveInsights,
                        onOpenJarvis = {
                            val topPrompt = proactiveInsights.firstOrNull()?.suggestedPrompt
                            if (!topPrompt.isNullOrBlank() && onOpenJarvisWithPrompt != null) {
                                onOpenJarvisWithPrompt(topPrompt)
                            } else {
                                onOpenJarvisTab()
                            }
                        }
                    )

                    if (proactiveInsights.size > 1) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(proactiveInsights.drop(1).take(3), key = { it.id }) { insight ->
                                Surface(
                                    color = Color(0xFF0D0E17),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(0.8.dp, Color(0xFF7C3AED).copy(alpha = 0.55f)),
                                    modifier = Modifier.clickable {
                                        if (insight.suggestedPrompt.isNotBlank() && onOpenJarvisWithPrompt != null) {
                                            onOpenJarvisWithPrompt(insight.suggestedPrompt)
                                        } else {
                                            onOpenJarvisTab()
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = insight.iconEmoji, fontSize = 12.sp)
                                        Text(
                                            text = insight.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFD8B4FE),
                                            maxLines = 1
                                        )
                                        if (insight.highlightStat.isNotBlank()) {
                                            Text(
                                                text = "· ${insight.highlightStat}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2EE59D)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // SECTION 3 — QUICK ACTIONS
            // =========================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_section_quick_actions"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MaiScreenQuickActionsSection(
                        onScanAndPay = onOpenSnapAndTrack,
                        onAddExpense = onOpenAddExpense,
                        onAddIncome = onOpenAddIncome,
                        onOpenAnalytics = onNavigateToAnalytics,
                        onOpenSavingsGoals = onOpenAddGoal,
                        onOpenBills = onOpenSmartReminders,
                        onOpenVault = onNavigateToVault,
                        onOpenSettings = onOpenSettings,
                        onViewAllTools = onOpenFinanceTools
                    )

                    // Primary 3-Button Action Bar (preserves existing test tags & rapid access)
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

                    // Secondary 4-Pill Utility Row (Analytics, Savings Goals, Scan Receipt, Finance Tools)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DashboardSecondaryActionChip(
                            emoji = "📸",
                            label = "Scan Receipt",
                            onClick = onOpenSnapAndTrack,
                            testTag = "quick_chip_scan_receipt"
                        )
                        DashboardSecondaryActionChip(
                            emoji = "📊",
                            label = "Analytics",
                            onClick = onNavigateToAnalytics,
                            testTag = "quick_chip_analytics"
                        )
                        DashboardSecondaryActionChip(
                            emoji = "🎯",
                            label = "Savings Goals",
                            onClick = onOpenAddGoal,
                            testTag = "quick_chip_savings_goals"
                        )
                        DashboardSecondaryActionChip(
                            emoji = "🧮",
                            label = "Finance Tools",
                            onClick = onOpenFinanceTools,
                            testTag = "quick_chip_finance_tools"
                        )
                        DashboardSecondaryActionChip(
                            emoji = "🧾",
                            label = "SMS Parser",
                            onClick = onOpenSmsParser,
                            testTag = "quick_chip_sms_parser"
                        )
                    }

                    // 1-Tap Quick Repeating Spend Strip
                    OneTapQuickRepeatingSpendStrip(
                        customChips = customQuickChips,
                        onInstantLogSpend = { emoji, title, category, amount ->
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onQuickAdd(title, category, amount)
                            activeQuickSpendUndoEvent = QuickSpendUndoEvent(
                                timestampMillis = System.currentTimeMillis(),
                                emoji = emoji,
                                label = title,
                                category = category,
                                amount = amount
                            )
                        },
                        onOpenAddCustomChip = { showCreateCustomChipDialog = true },
                        onDeleteCustomChip = { chipId ->
                            customQuickChips = HomeDailyCockpitEngine.deleteCustomQuickChip(context, userId, chipId)
                        }
                    )
                }
            }

            // =========================================================================
            // SECTION 4 — SPENDING OVERVIEW
            // =========================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_section_spending_overview"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Today vs This Month Live Telemetry Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0D0E17))
                            .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TODAY'S SPENDING",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB8B0D4),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (settings.stealthMode) "₹••••" else "₹${formatIndianAmount(todaySpent)}",
                                fontFamily = headingFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOverLimit) Color(0xFFFF4D8D) else Color.White
                            )
                            Text(
                                text = if (settings.stealthMode) "Daily limit masked" else "Daily cap: ₹${formatIndianAmount(dailyLimit)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF2EE59D)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(38.dp)
                                .background(Color(0xFF2B224C))
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "THIS MONTH'S SPEND",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB8B0D4),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (settings.stealthMode) "₹••••" else "₹${formatIndianAmount(monthSpent)}",
                                fontFamily = headingFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD8B4FE)
                            )
                            Text(
                                text = if (settings.stealthMode) "Budget left: ₹••••" else "Left: ₹${formatIndianAmount(dailySafeBurnTelemetry.remainingMonthlyBudget)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFB8B0D4)
                            )
                        }
                    }

                    MaiScreenBudgetOverviewCard(
                        monthSpent = monthSpent,
                        monthlyBudgetTarget = monthlyBudgetTargetTop,
                        expenses = expenses,
                        stealthMode = settings.stealthMode,
                        onEditBudgetClick = { showLimitEditor = !showLimitEditor }
                    )

                    AnimatedVisibility(visible = showLimitEditor) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF120F24))
                                .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DAILY SAFE SPEND LIMIT",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD8B4FE)
                                )
                                Text(
                                    text = "₹${formatIndianAmount(sliderLimit.roundToLong())}/day",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2EE59D)
                                )
                            }
                            Slider(
                                value = sliderLimit,
                                onValueChange = { sliderLimit = it },
                                valueRange = 100f..5000f,
                                steps = 48,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFA855F7),
                                    activeTrackColor = Color(0xFF7C3AED),
                                    inactiveTrackColor = Color(0xFF241E38)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { showLimitEditor = false },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.8.dp, Color(0xFF3E2D6E))
                                ) {
                                    Text("Cancel", fontSize = 11.sp, color = Color(0xFFB8B0D4))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val updatedDaily = sliderLimit.roundToLong().coerceAtLeast(100L)
                                        onUpdateDailyLimit(updatedDaily)
                                        showLimitEditor = false
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                                ) {
                                    Text("Save Limit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    MaiScreenSpendingOverviewCard(
                        expenses = expenses,
                        stealthMode = settings.stealthMode,
                        onOpenAnalytics = onNavigateToAnalytics
                    )
                }
            }

            // =========================================================================
            // SECTION 5 — SAVINGS GOALS
            // =========================================================================
            item {
                HomeSavingsGoalsSection(
                    savingsGoals = savingsGoals,
                    stealthMode = settings.stealthMode,
                    onOpenAddGoal = onOpenAddGoal,
                    onAddGoalProgress = onAddGoalProgress
                )
            }

            // =========================================================================
            // SECTION 6 — FINANCIAL HEALTH
            // =========================================================================
            item {
                val remainingMonthlyBudget = (monthlyBudgetTargetTop - monthSpent).coerceAtLeast(0L)
                val pendingBillCount = remember(paymentReminders) {
                    paymentReminders.count { !it.isPaid }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0D0E17))
                        .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(22.dp))
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
                                    contentDescription = "Financial Health & Telemetry",
                                    tint = Color(0xFFD8B4FE),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Financial Health & KPIs",
                                    fontFamily = headingFont,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Score ${disciplineScore.coerceIn(0, 900)}/900 · ${streakDays.coerceAtLeast(1)}d Streak · ${if (pendingBillCount == 0) "All bills clear" else "$pendingBillCount due(s)"}",
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
                                text = if (isFinancialHealthExpanded) "Compact ↑" else "Expand ↓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD8B4FE),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // 2x2 KPI Grid — always visible so core health indicators are never buried
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

                    AnimatedVisibility(visible = isFinancialHealthExpanded) {
                        MaiScreenMetallicSilverHealthScoreCard(
                            disciplineScore = (disciplineScore / 9).coerceIn(0, 100),
                            monthSpent = monthSpent,
                            monthlyBudgetTarget = monthlyBudgetTargetTop,
                            onCardClick = onNavigateToAnalytics
                        )
                    }
                }
            }

            // =========================================================================
            // SECTION 7 — BILLS AND KHATA
            // =========================================================================
            item {
                val pendingBillCountTicker = remember(paymentReminders) {
                    paymentReminders.count { !it.isPaid }
                }
                val pendingDenaHaiContacts = remember(userId, khataStoreVersion, khataFriends) {
                    AdvancedKhataStore.syncLegacyFirestoreFriendsIfNeeded(context, userId, khataFriends)
                    AdvancedKhataStore.getAllContactStates(context, userId).filter {
                        !it.isSettled && it.netDirection == com.example.data.model.KhataDirection.DENA_HAI && it.outstandingAmount > 0L
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_section_bills_and_khata"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    UnifiedLiveKhataAndBillsTickerStrip(
                        lenaTotal = khataSummaryMetrics.totalLenaHai,
                        denaTotal = khataSummaryMetrics.totalDenaHai,
                        pendingBillsCount = pendingBillCountTicker,
                        stealthMode = settings.stealthMode,
                        onRemindClicked = onNavigateToVault,
                        onPayNowClicked = onNavigateToVault,
                        onBillsClicked = onOpenSmartReminders
                    )

                    // Khata Split & Ledger Summary Card (You Get vs You Owe + 1-Tap UPI Settle)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0D0E17))
                            .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                            .testTag("home_khata_summary_card"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "KHATA LEDGER SUMMARY",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.9.sp,
                                    color = Color(0xFFD8B4FE)
                                )
                                Text(
                                    text = "Shared dues, split tabs & 1-tap UPI settlements",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8B0D4)
                                )
                            }
                            Surface(
                                color = Color(0xFF17122E),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color(0xFF7C3AED).copy(alpha = 0.6f)),
                                modifier = Modifier.clickable { onNavigateToVault() }
                            ) {
                                Text(
                                    text = "Open Khata →",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD8B4FE),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Lena Hai (You Will Receive)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF071E17))
                                    .border(0.8.dp, Color(0xFF00E676).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                    .clickable { onNavigateToVault() }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "↙ LENA HAI (YOU GET)",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E676)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (settings.stealthMode) "₹••••" else "₹${formatIndianAmount(khataSummaryMetrics.totalLenaHai)}",
                                    fontFamily = headingFont,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            // Dena Hai (You Owe)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF230B16))
                                    .border(0.8.dp, Color(0xFFFF3366).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                    .clickable { onNavigateToVault() }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "↗ DENA HAI (YOU OWE)",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF4D8D)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (settings.stealthMode) "₹••••" else "₹${formatIndianAmount(khataSummaryMetrics.totalDenaHai)}",
                                    fontFamily = headingFont,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        if (pendingDenaHaiContacts.isNotEmpty()) {
                            pendingDenaHaiContacts.take(3).forEach { contactState ->
                                val verifiedVpa = com.example.util.PaymentUtil
                                    .resolvePayeeVpa(contactState.contact.phone, contactState.contact.name)
                                    .ifBlank { com.example.util.PaymentUtil.getSavedVerifiedVpa(context, contactState.contact.id) }
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
                                            text = verifiedVpa.ifBlank { "Tap ⚡ Pay to verify UPI ID or scan QR" },
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
                                                if (verifiedVpa.isNotBlank()) {
                                                    com.example.util.PaymentUtil.launchUpiPaymentIntent(
                                                        context = context,
                                                        payeeVpa = verifiedVpa,
                                                        payeeName = contactState.contact.name,
                                                        amount = contactState.outstandingAmount.toString(),
                                                        txnNote = "Settle Khata Due - Kharchee",
                                                        contactId = contactState.contact.id
                                                    )
                                                } else {
                                                    pendingUpiVerificationContact = contactState
                                                }
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

                    if (showSmsPermissionRationaleBanner && !smsPermissionGranted) {
                        SmsAutoTrackPermissionRationaleBanner(
                            onAllowClicked = {
                                smsPermissionLauncher.launch(AutoExpenseDetectionStore.REQUIRED_SMS_PERMISSIONS)
                            },
                            onFallbackClipboardClicked = {
                                AutoExpenseDetectionStore.setSmsRationaleDismissed(context, true)
                                showSmsPermissionRationaleBanner = false
                                detectedClipboardSms = AutoExpenseDetectionStore.inspectClipboardForBankSms(context)
                            }
                        )
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

            // =========================================================================
            // SECTION 8 — NIGHT GUARD
            // =========================================================================
            item {
                HomeNightGuardDashboardCard(
                    settings = settings,
                    onOpenNightGuardSettings = onOpenNightGuardSettings,
                    onTestNightGuard = onTestNightGuard,
                    onToggleLockdown = onToggleLockdown
                )
            }

            // =========================================================================
            // SECTION 9 — RECENT TRANSACTIONS
            // =========================================================================
            item {
                HomeRecentActivitySection(
                    expenses = expenses,
                    stealthMode = settings.stealthMode,
                    onViewAllClicked = onNavigateToPassbook,
                    onEditTransaction = onEditExpense,
                    onDeleteTransaction = onDeleteExpense,
                    onAddTransaction = onOpenAddExpense
                )
            }
        }

        val currentPendingKhataContact = pendingUpiVerificationContact
        if (currentPendingKhataContact != null) {
            val initialContactVpa = remember(currentPendingKhataContact.contact.id, currentPendingKhataContact.contact.phone) {
                com.example.util.PaymentUtil
                    .resolvePayeeVpa(currentPendingKhataContact.contact.phone, currentPendingKhataContact.contact.name)
                    .ifBlank { com.example.util.PaymentUtil.getSavedVerifiedVpa(context, currentPendingKhataContact.contact.id) }
            }
            com.example.util.VerifyOrEnterRecipientUpiDialog(
                recipientName = currentPendingKhataContact.contact.name,
                amountInr = currentPendingKhataContact.outstandingAmount.toString(),
                initialVpa = initialContactVpa,
                onConfirmVerifiedUpiPay = { enteredVpa ->
                    com.example.util.PaymentUtil.saveVerifiedVpa(context, currentPendingKhataContact.contact.id, enteredVpa)
                    pendingUpiVerificationContact = null
                    com.example.util.PaymentUtil.launchUpiPaymentIntent(
                        context = context,
                        payeeVpa = enteredVpa,
                        payeeName = currentPendingKhataContact.contact.name,
                        amount = currentPendingKhataContact.outstandingAmount.toString(),
                        txnNote = "Settle Khata Due - Kharchee",
                        contactId = currentPendingKhataContact.contact.id
                    )
                },
                onOpenGenericUpiScanner = {
                    pendingUpiVerificationContact = null
                    com.example.util.PaymentUtil.launchGenericUpiAppChooser(
                        context = context,
                        payeeName = currentPendingKhataContact.contact.name,
                        amount = currentPendingKhataContact.outstandingAmount.toString(),
                        txnNote = "Settle Khata Due - Kharchee",
                        contactId = currentPendingKhataContact.contact.id
                    )
                },
                onMarkPaidManually = {
                    pendingUpiVerificationContact = null
                    AdvancedKhataStore.settleContactInFull(
                        context = context,
                        userId = userId,
                        contactId = currentPendingKhataContact.contact.id
                    )
                },
                onDismiss = { pendingUpiVerificationContact = null }
            )
        }

        // 4-Second Floating Undo Snackbar Pill for 1-Tap Quick Spend Chips
        QuickSpendUndoFloatingSnackbarPill(
            undoEvent = activeQuickSpendUndoEvent,
            onUndoClicked = { ev ->
                val targetTx = HomeDailyCockpitEngine.findLatestMatchingExpenseForUndo(expenses, ev)
                if (targetTx != null) {
                    onDeleteExpense(targetTx)
                }
                activeQuickSpendUndoEvent = null
            },
            onDismiss = { activeQuickSpendUndoEvent = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 22.dp)
        )

        if (showCreateCustomChipDialog) {
            CreateCustomQuickChipDialog(
                onDismiss = { showCreateCustomChipDialog = false },
                onSaveChip = { emoji, label, amount, category ->
                    customQuickChips = HomeDailyCockpitEngine.saveCustomQuickChip(
                        context = context,
                        userId = userId,
                        emoji = emoji,
                        label = label,
                        amount = amount,
                        category = category
                    )
                    showCreateCustomChipDialog = false
                }
            )
        }
    }
}

@Composable
private fun OneTapQuickRepeatingSpendStrip(
    customChips: List<QuickAddPreset> = emptyList(),
    onInstantLogSpend: (emoji: String, title: String, category: String, amount: Long) -> Unit,
    onOpenAddCustomChip: () -> Unit = {},
    onDeleteCustomChip: (String) -> Unit = {}
) {
    val headingFont = LocalAppFontFamily.current
    val allChips = remember(customChips) {
        DASHBOARD_REPEATING_SPEND_PILLS + customChips
    }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("one_tap_quick_repeating_spend_strip"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(
            items = allChips,
            key = { it.id },
            contentType = { "quick_spend_chip" }
        ) { pill ->
            val isCustom = pill.id.startsWith("custom_")
            Surface(
                color = Color(0xFF130E24),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isCustom) Color(0xFF00E676).copy(alpha = 0.70f) else Color(0xFF7C3AED).copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        onInstantLogSpend(pill.emoji, pill.label, pill.category, pill.amount)
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
                        color = if (isCustom) Color(0xFF00E676) else Color(0xFFD8B4FE),
                        maxLines = 1,
                        softWrap = false
                    )
                    if (isCustom) {
                        Text(
                            text = "✕",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E84AD),
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .clickable { onDeleteCustomChip(pill.id) }
                                .testTag("delete_custom_chip_${pill.id}")
                        )
                    }
                }
            }
        }

        // [+ Custom Chip] button at the end of the row
        item(key = "add_custom_quick_chip_item", contentType = "add_custom_chip_button") {
            Surface(
                color = Color(0xFF0D1E19),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.75f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onOpenAddCustomChip() }
                    .testTag("add_custom_quick_chip_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Quick Spend Chip",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "+ Custom Chip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E676),
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
                    text = "Auto-Track Spends (Local On-Device Privacy)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = AutoExpenseDetectionStore.PROMINENT_SMS_PRIVACY_DISCLOSURE,
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
    onDeleteTransaction: (ExpenseRecord) -> Unit = {},
    onAddTransaction: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
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
                val formattedAmt = remember(tx.amount, tx.exactAmount, isIncome) {
                    HomeDailyCockpitEngine.formatExactTransactionAmount(
                        amountRupees = tx.amount,
                        exactAmount = tx.exactAmount,
                        isIncome = isIncome
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF151329))
                        .border(0.7.dp, Color(0xFF2B224C), RoundedCornerShape(14.dp))
                        .clickable { onEditTransaction(tx) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("recent_tx_row_${tx.id}"),
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (stealthMode) "₹••••" else formattedAmt,
                            fontFamily = headingFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = amountColor,
                            maxLines = 1,
                            softWrap = false
                        )

                        IconButton(
                            onClick = { onEditTransaction(tx) },
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("recent_tx_edit_button_${tx.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Transaction",
                                tint = Color(0xFFB8B0D4),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteTransaction(tx) },
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("recent_tx_delete_button_${tx.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Transaction",
                                tint = Color(0xFFFF4D8D),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
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
    monthlyBudgetTarget: Long = 15000L,
    safeBurnTelemetry: DailySafeBurnTelemetry? = null,
    onToggleStealth: (() -> Unit)? = null
) {
    val headingFont = LocalAppFontFamily.current
    val pauseHeavyVisuals = com.example.util.PerformancePreferences.shouldPauseAnimations()
    val resolvedTelemetry = remember(safeBurnTelemetry, monthlyBudgetTarget, monthSpent, remainingToday) {
        safeBurnTelemetry ?: HomeDailyCockpitEngine.computeDailySafeBurnTelemetry(
            monthlyBudgetTarget = monthlyBudgetTarget,
            monthSpent = monthSpent,
            todaySpent = 0L,
            fallbackDailyLimit = max(50L, remainingToday)
        )
    }

    val remainingMonthlyBudget = resolvedTelemetry.remainingMonthlyBudget
    val formattedRemainingBudget = remember(remainingMonthlyBudget, stealthMode) {
        if (stealthMode) "👁️ ••••••"
        else com.example.util.PerformancePreferences.preFormatInr(remainingMonthlyBudget)
    }
    val formattedBalance = remember(totalBalance, stealthMode) {
        if (stealthMode) "₹••••••"
        else if (totalBalance >= 0) com.example.util.PerformancePreferences.preFormatInr(totalBalance)
        else "-${com.example.util.PerformancePreferences.preFormatInr(totalBalance)}"
    }
    val formattedMonthBurn = remember(monthSpent, stealthMode) {
        if (stealthMode) "₹••••/mo" else "${com.example.util.PerformancePreferences.preFormatInr(monthSpent)}/mo"
    }

    val safePillColor = when (resolvedTelemetry.statusTier) {
        SafeBurnStatusTier.EMERALD_SAFE -> Color(0xFF00E676)
        SafeBurnStatusTier.AMBER_WARNING -> Color(0xFFFFD600)
        SafeBurnStatusTier.CRIMSON_EXCEEDED -> Color(0xFFFF3366)
    }
    val safePillBg = when (resolvedTelemetry.statusTier) {
        SafeBurnStatusTier.EMERALD_SAFE -> Color(0xFF052417)
        SafeBurnStatusTier.AMBER_WARNING -> Color(0xFF261E04)
        SafeBurnStatusTier.CRIMSON_EXCEEDED -> Color(0xFF280814)
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header: REMAINING MONTHLY BUDGET + Gamified Discipline Streak Badge + Privacy Eye Mask Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REMAINING MONTHLY BUDGET",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                color = Color(0xFFD8B4FE),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Gamified Top-Right Badge: "🔥 [X]d Discipline Streak"
                Surface(
                    color = Color(0xFF231307),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFFFF9100).copy(alpha = 0.8f)),
                    modifier = Modifier.testTag("hero_card_discipline_streak_badge")
                ) {
                    Text(
                        text = "🔥 ${streakDays.coerceAtLeast(1)}d Discipline Streak",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFB74D),
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

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
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (stealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (stealthMode) "Reveal Balance" else "Hide Balance on Dashboard",
                            tint = if (stealthMode) Color(0xFFD8B4FE) else Color(0xFF8E84AD),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (stealthMode) "👁️ ••••••" else "Hide",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (stealthMode) Color(0xFFD8B4FE) else Color(0xFF8E84AD)
                        )
                    }
                }
            }
        }

        // Bold High-Contrast Remaining Monthly Budget Amount
        Text(
            text = formattedRemainingBudget,
            fontFamily = headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 34.sp,
            color = Color.White,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("hero_card_remaining_monthly_budget_value")
        )

        // Glowing "Daily Safe Limit" Pill right inside the card
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
                    text = "NET BALANCE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = Color(0xFF8E84AD),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = formattedBalance,
                    fontFamily = headingFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.End) {
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

@Composable
private fun DashboardSecondaryActionChip(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = Color(0xFF0D0E17),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, Color(0xFF3E2D6E)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 12.sp)
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD8B4FE),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HomeSavingsGoalsSection(
    savingsGoals: List<SavingsGoalRecord>,
    stealthMode: Boolean,
    onOpenAddGoal: () -> Unit,
    onAddGoalProgress: (SavingsGoalRecord, Long) -> Unit
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0D0E17))
            .border(1.dp, Color(0xFF2B224C), RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("home_section_savings_goals"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SAVINGS GOALS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF00E676)
                )
                Text(
                    text = if (savingsGoals.isEmpty()) "Set a target & sweep spare change" else "${savingsGoals.size} active goal(s) in progress",
                    fontSize = 11.sp,
                    color = Color(0xFFB8B0D4)
                )
            }

            Surface(
                color = Color(0xFF07291E),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color(0xFF00E676).copy(alpha = 0.7f)),
                modifier = Modifier
                    .clickable { onOpenAddGoal() }
                    .testTag("home_add_goal_button")
            ) {
                Text(
                    text = "+ New Goal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        if (savingsGoals.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF090A12))
                    .border(0.8.dp, Color(0xFF231E38), RoundedCornerShape(14.dp))
                    .clickable { onOpenAddGoal() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 No savings goals yet. Tap '+ New Goal' to build your Emergency or Gadget fund.",
                    fontSize = 11.sp,
                    color = Color(0xFFB8B0D4),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            savingsGoals.take(3).forEach { goal ->
                val target = max(1L, goal.targetAmount)
                val saved = goal.savedAmount.coerceAtLeast(0L)
                val remaining = (target - saved).coerceAtLeast(0L)
                val ratio = (saved.toFloat() / target.toFloat()).coerceIn(0f, 1f)
                val pct = kotlin.math.round(ratio * 100f).toInt()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF090A12))
                        .border(0.8.dp, Color(0xFF282042), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = goal.title,
                            fontFamily = headingFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$pct%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E676)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { ratio },
                        color = Color(0xFF00E676),
                        trackColor = Color(0xFF1C1830),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (stealthMode) "Saved: ₹•••• / ₹••••" else "Saved ₹${formatIndianAmount(saved)} of ₹${formatIndianAmount(target)} (₹${formatIndianAmount(remaining)} left)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFB8B0D4),
                            modifier = Modifier.weight(1f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = Color(0xFF151228),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                                modifier = Modifier.clickable { onAddGoalProgress(goal, 100L) }
                            ) {
                                Text(
                                    text = "+₹100",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFF151228),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
                                modifier = Modifier.clickable { onAddGoalProgress(goal, 500L) }
                            ) {
                                Text(
                                    text = "+₹500",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD8B4FE),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeNightGuardDashboardCard(
    settings: UserSettings,
    onOpenNightGuardSettings: () -> Unit,
    onTestNightGuard: () -> Unit,
    onToggleLockdown: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val formattedWindowText = remember(settings) {
        String.format(
            Locale.ENGLISH,
            "%02d:%02d – %02d:%02d",
            settings.nightGuardStartHour,
            settings.nightGuardStartMinute,
            settings.nightGuardEndHour,
            settings.nightGuardEndMinute
        )
    }
    val statusColor = when {
        settings.lockdownActive -> Color(0xFFFF3366)
        settings.nightGuardEnabled -> Color(0xFF00E5FF)
        else -> Color(0xFFB8B0D4)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0D0E17))
            .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("home_section_night_guard"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF151228))
                        .border(0.8.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (settings.lockdownActive) Icons.Default.Lock else Icons.Default.NightlightRound,
                        contentDescription = "Night Guard",
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Night Guard & UPI Shield",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Window $formattedWindowText · Tone: ${settings.upiGuardTone.uppercase(Locale.ENGLISH)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFFB8B0D4)
                    )
                }
            }

            Surface(
                color = statusColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.7f))
            ) {
                Text(
                    text = when {
                        settings.lockdownActive -> "LOCKDOWN"
                        settings.nightGuardEnabled -> "ARMED"
                        else -> "STANDBY"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOpenNightGuardSettings,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF3E2D6E)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Text("Guard Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE), maxLines = 1)
            }
            OutlinedButton(
                onClick = onTestNightGuard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.55f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Text("Test Guard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), maxLines = 1)
            }
            Button(
                onClick = onToggleLockdown,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (settings.lockdownActive) Color(0xFFFF3366) else Color(0xFF231A3A),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Text(
                    text = if (settings.lockdownActive) "Unlock" else "Lockdown",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

