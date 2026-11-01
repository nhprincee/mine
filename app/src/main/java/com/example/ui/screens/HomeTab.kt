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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountabilityEvaluation
import com.example.data.model.ExpenseRecord
import com.example.data.model.NightGuardSchedule
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.ProactiveFinancialInsight
import com.example.data.model.QUICK_ADD_PRESETS
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
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
    modifier: Modifier = Modifier
) {
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

    val ringColor = when {
        settings.lockdownActive -> CrimsonRuby
        isOverLimit -> CrimsonRuby
        isNearLimit -> ChampagneGold
        else -> hud.accent
    }

    val totalBalance = remember(expenses, dailyLimit, monthSpent) {
        val totalIncome = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val totalExpense = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        if (totalIncome > 0L) {
            totalIncome - totalExpense
        } else {
            max(0L, (dailyLimit * 30L) - monthSpent)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("home_tab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live UPI Accountability Banner if present
        if (latestAlert != null) {
            item {
                AccountabilityBannerCard(
                    evaluation = latestAlert,
                    onDismiss = onDismissAlert
                )
            }
        }

        // Proactive Autonomous Telemetry Insight Card (FEATURE 1)
        if (proactiveInsights.isNotEmpty()) {
            val primaryInsight = proactiveInsights.first()
            item {
                ProactiveInsightBannerCard(
                    insight = primaryInsight,
                    onAskJarvis = {
                        onOpenJarvisWithPrompt?.invoke(primaryInsight.suggestedPrompt)
                    }
                )
            }
        }

        // CRED-Style Dark Emerald Sovereign Titanium Card
        item {
            CredBlackTitaniumCard(
                totalBalance = totalBalance,
                disciplineScore = disciplineScore,
                streakDays = streakDays,
                remainingToday = remainingToday,
                stealthMode = settings.stealthMode,
                isPro = settings.isPro,
                proTier = settings.proTier
            )
        }

        // Net Worth & Cashflow Velocity Strip
        item {
            val totalInflow = remember(expenses) {
                expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
            }
            val totalSavedInGoals = remember(savingsGoals) {
                savingsGoals.sumOf { it.savedAmount }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL INFLOW",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp,
                        color = MutedIvory
                    )
                    Text(
                        text = if (settings.stealthMode) "₹••••" else "+₹$totalInflow",
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChampagneGold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MONTH OUTFLOW",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp,
                        color = MutedIvory
                    )
                    Text(
                        text = if (settings.stealthMode) "₹••••" else "-₹$monthSpent",
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "GOALS RESERVE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp,
                        color = MutedIvory
                    )
                    Text(
                        text = if (settings.stealthMode) "₹••••" else "₹$totalSavedInGoals",
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SophisticatedEmerald
                    )
                }
            }
        }

        // Sovereign Daily Reserve & Runway Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = ringColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(0.8.dp, ringColor.copy(alpha = 0.38f), RoundedCornerShape(50))
                    ) {
                        Text(
                            text = when {
                                settings.lockdownActive -> "🔒 LOCKDOWN SHIELD ENGAGED"
                                isOverLimit -> "◆ DAILY RESERVE EXCEEDED"
                                isNearLimit -> "◆ 80%+ DAILY RESERVE UTILIZED"
                                else -> "◆ DAILY SOVEREIGN RESERVE"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            color = ringColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        color = ElevatedGraphite,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(50))
                            .clickable { showLimitEditor = !showLimitEditor }
                            .testTag("edit_daily_limit_chip")
                    ) {
                        Text(
                            text = if (settings.stealthMode) "Limit: ₹•••" else "Limit: ₹$dailyLimit/d ✎",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedPlatinum,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Gauge + Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier.size(134.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                            val strokeWidth = 9.dp.toPx()
                            drawArc(
                                color = ElevatedGraphite,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        ringColor.copy(alpha = 0.7f),
                                        ringColor,
                                        MutedPlatinum
                                    )
                                ),
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SAFE RESERVE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                                color = MutedIvory
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (settings.stealthMode) "₹0" else "₹$remainingToday",
                                fontFamily = headingFont,
                                style = MaterialTheme.typography.headlineMedium,
                                color = WarmIvory,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${(progressRatio * 100).toInt()}% utilized",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = ringColor
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricMiniRow(
                            label = "TODAY SPENT",
                            value = if (settings.stealthMode) "₹0.00" else "₹$todaySpent",
                            accentColor = WarmIvory
                        )
                        MetricMiniRow(
                            label = "MONTHLY OUTFLOW",
                            value = if (settings.stealthMode) "₹0.00" else "₹$monthSpent / ₹${dailyLimit * 30}",
                            accentColor = MutedPlatinum
                        )
                        MetricMiniRow(
                            label = "DISCIPLINE INDEX",
                            value = "$disciplineScore / 900 · ${streakDays}d",
                            accentColor = ChampagneGold
                        )
                    }
                }

                AnimatedVisibility(visible = showLimitEditor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, HairlineGold, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Spending Reserve",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmIvory
                            )
                            Text(
                                text = "₹${sliderLimit.roundToLong()}",
                                fontFamily = headingFont,
                                fontWeight = FontWeight.ExtraBold,
                                color = ChampagneGold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(300L, 500L, 1000L, 2000L).forEach { presetVal ->
                                Surface(
                                    color = if (sliderLimit.roundToLong() == presetVal) ChampagneGold else ElevatedGraphite,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.6.dp, HairlinePlatinum),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { sliderLimit = presetVal.toFloat() }
                                ) {
                                    Text(
                                        text = "₹$presetVal",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (sliderLimit.roundToLong() == presetVal) ObsidianBlack else WarmIvory,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        Slider(
                            value = sliderLimit,
                            onValueChange = { sliderLimit = it },
                            valueRange = 100f..5000f,
                            steps = 48,
                            colors = SliderDefaults.colors(
                                thumbColor = ChampagneGold,
                                activeTrackColor = ChampagneGold,
                                inactiveTrackColor = ElevatedGraphite
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    val rounded = ((sliderLimit / 50f).roundToLong() * 50L).coerceAtLeast(100L)
                                    onUpdateDailyLimit(rounded)
                                    showLimitEditor = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampagneGold,
                                    contentColor = ObsidianBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Save Reserve", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenAddExpense,
                        enabled = !settings.lockdownActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBlack,
                            disabledContainerColor = ElevatedGraphite,
                            disabledContentColor = MutedIvory
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_expense_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Log Spend", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (settings.lockdownActive) "Vault Locked" else "Log Spend",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenAddIncome,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.8.dp, SophisticatedEmerald.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SophisticatedEmerald.copy(alpha = 0.08f),
                            contentColor = SophisticatedEmerald
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_income_button")
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = "Add Income",
                            tint = SophisticatedEmerald,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("+Income", color = SophisticatedEmerald, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onToggleLockdown,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            0.8.dp,
                            if (settings.lockdownActive) CrimsonRuby.copy(alpha = 0.6f) else HairlinePlatinum
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ElevatedGraphite
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("toggle_lockdown_button")
                    ) {
                        Icon(
                            imageVector = if (settings.lockdownActive) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Toggle Lockdown Shield",
                            tint = if (settings.lockdownActive) CrimsonRuby else MutedPlatinum,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 1-Tap Quick Log Presets Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSTANT TELEMETRY LOG",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = ChampagneGold
                    )
                    Text(
                        text = "1-Tap UPI Entry",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedIvory
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QUICK_ADD_PRESETS.forEach { preset ->
                        val catColor = getCategoryColor(preset.category)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElevatedGraphite)
                                .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                                .clickable(enabled = !settings.lockdownActive) {
                                    onQuickAdd(preset.label, preset.category, preset.amount)
                                }
                                .padding(vertical = 11.dp, horizontal = 6.dp)
                                .testTag("quick_add_${preset.id}"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(preset.category),
                                    contentDescription = preset.label,
                                    tint = catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = preset.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = WarmIvory,
                                maxLines = 1
                            )
                            Text(
                                text = "₹${preset.amount}",
                                fontFamily = headingFont,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ChampagneGold
                            )
                        }
                    }
                }
            }
        }

        // Smart Concierge Utilities Row: SMS Parser, UPI Accountability Guard, Fake Mom Call Escape
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UtilityActionCard(
                    icon = Icons.Default.Sms,
                    tint = MutedPlatinum,
                    title = "SMS Parser",
                    subtitle = "Auto-extract UPI",
                    onClick = onOpenSmsParser,
                    modifier = Modifier.weight(1f).testTag("open_sms_parser_button")
                )
                UtilityActionCard(
                    icon = Icons.Default.NotificationsActive,
                    tint = ChampagneGold,
                    title = "UPI Guard",
                    subtitle = "Hinglish Alerts",
                    onClick = onOpenUpiGuardSimulator,
                    modifier = Modifier.weight(1f).testTag("open_upi_guard_button")
                )
                UtilityActionCard(
                    icon = Icons.Default.Call,
                    tint = SophisticatedEmerald,
                    title = "Mom Escape",
                    subtitle = "Incoming SOS",
                    onClick = onTriggerMomCall,
                    modifier = Modifier.weight(1f).testTag("trigger_mom_call_button")
                )
            }
        }

        // Night Guard & Smart Payment Reminders Status Card
        item {
            val isNightActiveNow = settings.nightGuardEnabled && NightGuardSchedule.isWithinNightGuardWindow(
                nowMillis = System.currentTimeMillis(),
                startHour = settings.nightGuardStartHour,
                startMinute = settings.nightGuardStartMinute,
                endHour = settings.nightGuardEndHour,
                endMinute = settings.nightGuardEndMinute
            )
            val windowText = NightGuardSchedule.formatTimeWindow(
                settings.nightGuardStartHour,
                settings.nightGuardStartMinute,
                settings.nightGuardEndHour,
                settings.nightGuardEndMinute
            )
            val upcomingReminders = remember(paymentReminders) {
                paymentReminders.filter { !it.isPaid }.sortedBy { it.dueEpochMillis }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RichGraphite)
                    .border(
                        0.8.dp,
                        if (isNightActiveNow) SophisticatedEmerald else HairlinePlatinum,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
                    .testTag("night_guard_dashboard_card"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (settings.nightGuardEnabled) SophisticatedEmerald.copy(alpha = 0.15f)
                                    else ElevatedGraphite
                                )
                                .border(
                                    0.8.dp,
                                    if (settings.nightGuardEnabled) SophisticatedEmerald else HairlinePlatinum,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = "Night Guard",
                                tint = if (settings.nightGuardEnabled) SophisticatedEmerald else MutedPlatinum,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when {
                                    isNightActiveNow -> "NIGHT GUARD ACTIVE NOW · RUKO ZARA"
                                    settings.nightGuardEnabled -> "NIGHT GUARD ARMED · $windowText"
                                    else -> "NIGHT GUARD DISABLED"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp,
                                color = if (settings.nightGuardEnabled) SophisticatedEmerald else MutedPlatinum
                            )
                            Text(
                                text = "Late-night impulse shield & Brain Diversion Challenge",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedIvory,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = ElevatedGraphite,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.8.dp, HairlineGold, RoundedCornerShape(50))
                            .clickable { onOpenNightGuardSettings() }
                            .testTag("open_night_guard_settings_button")
                    ) {
                        Text(
                            text = "Configure",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChampagneGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestNightGuard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, SophisticatedEmerald.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SophisticatedEmerald.copy(alpha = 0.08f),
                            contentColor = SophisticatedEmerald
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dashboard_test_night_guard_button")
                    ) {
                        Icon(Icons.Default.NightlightRound, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ruko Zara Challenge", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenSmartReminders,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, ChampagneGold.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ChampagneGold.copy(alpha = 0.08f),
                            contentColor = ChampagneGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_smart_reminders_button")
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (upcomingReminders.isEmpty()) "Bill Reminders" else "Reminders (${upcomingReminders.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (upcomingReminders.isNotEmpty()) {
                    val dueFmt = remember { SimpleDateFormat("dd MMM", Locale.ENGLISH) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElevatedGraphite)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UPCOMING BILLS",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                                color = MutedPlatinum
                            )
                            Text(
                                text = "Manage ➔",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChampagneGold,
                                modifier = Modifier.clickable { onOpenSmartReminders() }
                            )
                        }
                        upcomingReminders.take(3).forEach { bill ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bill.title} · Due ${dueFmt.format(Date(bill.dueEpochMillis))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarmIvory,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (settings.stealthMode) "₹•••" else "₹${bill.amount}",
                                    fontFamily = headingFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = ChampagneGold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Savings Goals Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOVEREIGN CAPITAL GOALS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = SophisticatedEmerald
                    )
                    Text(
                        text = "+ New Goal",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChampagneGold,
                        modifier = Modifier
                            .clickable { onOpenAddGoal() }
                            .padding(4.dp)
                            .testTag("add_savings_goal_button")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (savingsGoals.isEmpty()) {
                    Text(
                        text = "No active capital allocations yet. Tap + New Goal to establish a private reserve.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedIvory
                    )
                } else {
                    savingsGoals.forEach { goal ->
                        val ratio = if (goal.targetAmount > 0) {
                            min(1f, goal.savedAmount.toFloat() / goal.targetAmount.toFloat())
                        } else 0f
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElevatedGraphite)
                                .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WarmIvory,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (settings.stealthMode) "₹••• / ₹•••" else "₹${goal.savedAmount} / ₹${goal.targetAmount}",
                                    fontFamily = headingFont,
                                    fontSize = 13.sp,
                                    color = SophisticatedEmerald,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                color = SophisticatedEmerald,
                                trackColor = ObsidianBlack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${(ratio * 100).toInt()}% allocated",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MutedIvory
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = SophisticatedEmerald.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { onAddGoalProgress(goal, 500L) }
                                    ) {
                                        Text(
                                            text = "+₹500",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = SophisticatedEmerald,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        color = ChampagneGold.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .border(0.8.dp, ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { onAddGoalProgress(goal, 1000L) }
                                    ) {
                                        Text(
                                            text = "+₹1,000",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = ChampagneGold,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Activity Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT LEDGER ACTIVITY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp,
                    color = MutedPlatinum
                )
                Text(
                    text = "${expenses.size} entries",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChampagneGold
                )
            }
        }

        // Recent Transactions List
        val recent = expenses.take(8)
        if (recent.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your private ledger is pristine. Log a transaction or tap a quick entry above.",
                        color = MutedIvory,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(recent, key = { it.id.ifEmpty { it.title + it.txEpochMillis } }) { item ->
                TransactionRowCard(
                    expense = item,
                    stealthMode = settings.stealthMode,
                    onDelete = { onDeleteExpense(item) }
                )
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
    proTier: String = "free"
) {
    val headingFont = LocalAppFontFamily.current
    val inrFormat = remember { NumberFormat.getIntegerInstance(Locale("en", "IN")) }
    val formattedBalance = remember(totalBalance, stealthMode) {
        if (stealthMode) "₹••••••"
        else if (totalBalance >= 0) "₹${inrFormat.format(totalBalance)}"
        else "-₹${inrFormat.format(-totalBalance)}"
    }

    // Matte-Black & Metallic-Silver Obsidian Luxury Card Finishes
    val metallicBorderBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                ObsidianDarkSilver.copy(alpha = 0.55f),
                ObsidianDarkBorder,
                ObsidianDarkSilver.copy(alpha = 0.25f),
                ObsidianDarkBorder,
                ObsidianDarkSilver.copy(alpha = 0.45f)
            )
        )
    }

    val brushedMetallicSurface = remember {
        Brush.linearGradient(
            colors = listOf(
                ObsidianDarkBackground,
                ObsidianDarkPrimarySurface,
                Color(0xFF181E27),
                ObsidianDarkPrimarySurface,
                ObsidianDarkBackground
            ),
            start = Offset(0f, 0f),
            end = Offset(960f, 520f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(22.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(brushedMetallicSurface)
            .border(
                width = 1.dp,
                brush = metallicBorderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("cred_black_titanium_card")
    ) {
        // Restrained Metallic Silver Grain & Subtle Emerald Financial Sparkline
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            for (i in -4..12) {
                val startX = (w / 8f) * i
                drawLine(
                    color = ObsidianDarkSilver.copy(alpha = 0.025f),
                    start = Offset(startX, 0f),
                    end = Offset(startX + h * 0.85f, h),
                    strokeWidth = 1f
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ObsidianDarkSilver.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.84f, h * 0.20f),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.84f, h * 0.20f),
                radius = w * 0.55f
            )

            // Restrained Financial Sparkline Wave across bottom of card
            val sparkPoints = listOf(
                Offset(0f, h * 0.84f),
                Offset(w * 0.18f, h * 0.79f),
                Offset(w * 0.36f, h * 0.82f),
                Offset(w * 0.54f, h * 0.72f),
                Offset(w * 0.72f, h * 0.74f),
                Offset(w * 0.88f, h * 0.64f),
                Offset(w, h * 0.58f)
            )
            val sparkLinePath = Path().apply {
                moveTo(sparkPoints.first().x, sparkPoints.first().y)
                for (i in 1 until sparkPoints.size) {
                    val prev = sparkPoints[i - 1]
                    val curr = sparkPoints[i]
                    val midX = (prev.x + curr.x) / 2f
                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                }
            }
            drawPath(
                path = sparkLinePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ObsidianDarkSilver.copy(alpha = 0.18f),
                        ObsidianDarkSilver.copy(alpha = 0.38f),
                        ObsidianDarkEmeraldAccent.copy(alpha = 0.55f)
                    )
                ),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP ROW: Metallic Silver EMV Chip + Silver Typography + Contactless Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Brushed Metallic Silver EMV Micro-Chip
                    Canvas(modifier = Modifier.size(width = 36.dp, height = 26.dp)) {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFDCE1E7),
                                    ObsidianDarkSilver,
                                    Color(0xFF8A94A2)
                                )
                            ),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                        )
                        drawRoundRect(
                            color = ObsidianDarkBackground.copy(alpha = 0.45f),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawLine(
                            color = ObsidianDarkBackground.copy(alpha = 0.35f),
                            start = Offset(size.width * 0.33f, 2f),
                            end = Offset(size.width * 0.33f, size.height - 2f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = ObsidianDarkBackground.copy(alpha = 0.35f),
                            start = Offset(size.width * 0.66f, 2f),
                            end = Offset(size.width * 0.66f, size.height - 2f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = ObsidianDarkBackground.copy(alpha = 0.35f),
                            start = Offset(2f, size.height * 0.5f),
                            end = Offset(size.width - 2f, size.height * 0.5f),
                            strokeWidth = 1f
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isPro) {
                                    if (proTier == "platinum") "KHARCHEE OBSIDIAN PLATINUM" else "KHARCHEE OBSIDIAN PRO"
                                } else "KHARCHEE OBSIDIAN VAULT",
                                fontFamily = headingFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.6.sp,
                                color = ObsidianDarkSilver
                            )
                            if (isPro) {
                                Surface(
                                    color = ObsidianDarkEmeraldAccent.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.6.dp, ObsidianDarkEmeraldAccent.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "PRO ELITE",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ObsidianDarkEmeraldAccent,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isPro) "METALLIC SILVER · PRIVATE MEMBERSHIP" else "MATTE BLACK & METALLIC SILVER EDITION",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.2.sp,
                            color = ObsidianDarkMutedText
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ObsidianDarkSecondarySurface)
                        .border(0.8.dp, ObsidianDarkBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Contactless,
                        contentDescription = "Contactless Vault",
                        tint = ObsidianDarkSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // CENTER: Total Available Balance (Manrope ExtraBold 800) + Discipline Index
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL AVAILABLE BALANCE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.4.sp,
                        color = ObsidianDarkSilver
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedBalance,
                        fontFamily = headingFont,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ObsidianDarkPrimaryText,
                        fontSize = 31.sp
                    )
                }

                // Metallic Silver & Emerald Discipline Pill
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianDarkBackground.copy(alpha = 0.82f))
                        .border(
                            width = 0.8.dp,
                            color = ObsidianDarkBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Discipline Score",
                            tint = ObsidianDarkSilver,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "DISCIPLINE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = ObsidianDarkSilver
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$disciplineScore",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ObsidianDarkEmeraldAccent,
                            fontSize = 20.sp
                        )
                        Text(
                            text = " / 900",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ObsidianDarkMutedText,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            // BOTTOM: Spending Runway Streak + Masked Metallic Card Credentials (•••• 2026)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianDarkBackground.copy(alpha = 0.72f))
                    .border(
                        width = 0.8.dp,
                        color = ObsidianDarkBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "◆ RUNWAY STREAK: ${streakDays}D",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ObsidianDarkPositive
                    )
                    Text(
                        text = if (stealthMode) "· SAFE ₹•••" else "· SAFE ₹$remainingToday",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = ObsidianDarkMutedText
                    )
                }

                Text(
                    text = "••••  ••••  ••••  2026",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = ObsidianDarkSilver
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
    modifier: Modifier = Modifier
) {
    val isIncome = isIncomeTransaction(expense)
    val catColor = getCategoryColor(expense.category)
    val catIcon = getCategoryIcon(expense.category)
    val dateFmt = remember { SimpleDateFormat("dd MMM · hh:mm a", Locale.ENGLISH) }
    val dateString = remember(expense.txEpochMillis) { dateFmt.format(Date(expense.txEpochMillis)) }

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
            text = when {
                stealthMode -> "₹•••"
                isIncome -> "+₹${expense.amount}"
                else -> "-₹${expense.amount}"
            },
            fontFamily = headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = if (isIncome) SophisticatedEmerald else WarmIvory
        )

        if (expense.id.isNotBlank()) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
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
