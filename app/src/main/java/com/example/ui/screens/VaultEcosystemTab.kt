package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmiItem
import com.example.data.model.ExpenseRecord
import com.example.data.model.KhataFriendRecord
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PAYMENT_MODES
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.SubscriptionItem
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.data.model.isSameDay
import com.example.ui.components.KharcheeArchitecturalLogo
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

private data class DreamVaultPreset(
    val title: String,
    val defaultTarget: Long,
    val defaultDate: String,
    val emoji: String
)

private val DREAM_VAULT_PRESETS = listOf(
    DreamVaultPreset("Laptop", 85000L, "31 Dec 2026", "💻"),
    DreamVaultPreset("Phone", 60000L, "15 Nov 2026", "📱"),
    DreamVaultPreset("Bike", 140000L, "31 Mar 2027", "🏍️"),
    DreamVaultPreset("Education", 120000L, "30 Jun 2027", "🎓"),
    DreamVaultPreset("Travel", 45000L, "25 Dec 2026", "✈️"),
    DreamVaultPreset("Emergency Savings", 100000L, "31 Jan 2027", "🛡️")
)

private data class AchievementBadgeDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val unlocked: Boolean,
    val iconSymbol: String
)

@Composable
fun VaultEcosystemTab(
    userId: String,
    userDisplayName: String,
    settings: UserSettings,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    khataFriends: List<KhataFriendRecord>,
    streakDays: Int,
    disciplineScore: Int,
    monthSpent: Long,
    onCreateGoal: (title: String, targetAmount: Long, initialSaved: Long) -> Unit,
    onAddGoalProgress: (SavingsGoalRecord, Long) -> Unit,
    onOpenSnapAndTrack: () -> Unit,
    onUpdateMyUpiId: (String) -> Unit,
    onAddFriend: (name: String, phone: String, upiId: String, amount: Long, description: String) -> Unit,
    onAddEntry: (friend: KhataFriendRecord, deltaAmount: Long, description: String) -> Unit,
    onSettleFriend: (KhataFriendRecord) -> Unit,
    onSplitBill: (friends: List<KhataFriendRecord>, totalBill: Long, description: String, includeSelf: Boolean) -> Unit,
    onShowToast: (String) -> Unit,
    paymentReminders: List<PaymentReminderRecord> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    // Sub-sections inside Vault: "dream_vault", "streaks", "subscriptions", "net_worth", "khata"
    var activeSubSection by remember { mutableStateOf("dream_vault") }
    var refreshTrigger by remember { mutableStateOf(0) }

    val snapCount = remember(userId, refreshTrigger) {
        KharcheeProfileAndToolsStore.getSnapScanCount(context, userId)
    }
    val streakFreezeEnabled = remember(userId, refreshTrigger) {
        KharcheeProfileAndToolsStore.isStreakFreezeEnabled(context, userId)
    }
    val levelInfo = remember(streakDays, snapCount) {
        KharcheeProfileAndToolsStore.computeStreakLevel(streakDays, snapCount)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ChampagneGold,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory,
        focusedLabelColor = ChampagneGold,
        unfocusedLabelColor = MutedIvory
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("vault_ecosystem_tab")
    ) {
        // Sub-navigation pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RichGraphite)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("dream_vault", "Dream Vault", Icons.Default.Savings),
                Triple("timeline", "Money Timeline", Icons.Default.NotificationsActive),
                Triple("purchase_assistant", "Purchase Assistant", Icons.Default.AutoAwesome),
                Triple("emergency_meter", "Emergency Meter", Icons.Default.Shield),
                Triple("salary_sim", "Salary Simulator", Icons.Default.AccountBalanceWallet),
                Triple("challenges", "Savings Challenges", Icons.Default.EmojiEvents),
                Triple("family_wallet", "Family Wallet", Icons.Default.Groups),
                Triple("data_center", "Data Center", Icons.Default.Share),
                Triple("streaks", "Streaks & Badges", Icons.Default.EmojiEvents),
                Triple("subscriptions", "Subscription Radar", Icons.Default.Repeat),
                Triple("net_worth", "Net Worth & Score", Icons.Default.AccountBalanceWallet),
                Triple("khata", "Split Khata", Icons.Default.Groups)
            ).forEach { (id, label, icon) ->
                val selected = activeSubSection == id
                Surface(
                    color = if (selected) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .border(
                            0.8.dp,
                            if (selected) ChampagneGold else HairlinePlatinum,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { activeSubSection = id }
                        .testTag("vault_subtab_$id")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) ChampagneGold else MutedPlatinum,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) ChampagneGold else MutedPlatinum
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

        if (activeSubSection == "khata") {
            KhataTab(
                friends = khataFriends,
                myUpiId = settings.myUpiId,
                stealthMode = settings.stealthMode,
                onUpdateMyUpiId = onUpdateMyUpiId,
                onAddFriend = onAddFriend,
                onAddEntry = onAddEntry,
                onSettleFriend = onSettleFriend,
                onSplitBill = onSplitBill,
                onShowToast = onShowToast,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeSubSection) {
                    "dream_vault" -> {
                        item {
                            DreamVaultSection(
                                userId = userId,
                                stealthMode = settings.stealthMode,
                                savingsGoals = savingsGoals,
                                refreshTrigger = refreshTrigger,
                                fieldColors = fieldColors,
                                onCreateGoal = { title, target, saved, targetDate ->
                                    KharcheeProfileAndToolsStore.saveGoalTargetDate(context, userId, title, targetDate)
                                    if (saved > 0L) {
                                        KharcheeProfileAndToolsStore.recordGoalContribution(
                                            context,
                                            userId,
                                            title,
                                            saved,
                                            "Initial Deposit"
                                        )
                                    }
                                    onCreateGoal(title, target, saved)
                                    refreshTrigger++
                                },
                                onContribute = { goal, amount ->
                                    KharcheeProfileAndToolsStore.recordGoalContribution(
                                        context,
                                        userId,
                                        goal.title,
                                        amount,
                                        "Dream Vault Contribution"
                                    )
                                    onAddGoalProgress(goal, amount)
                                    refreshTrigger++
                                }
                            )
                        }
                    }

                    "streaks" -> {
                        item {
                            KharcheeStreaksAndBadgesSection(
                                userId = userId,
                                userDisplayName = userDisplayName,
                                expenses = expenses,
                                savingsGoals = savingsGoals,
                                streakDays = streakDays,
                                disciplineScore = disciplineScore,
                                snapCount = snapCount,
                                levelInfo = levelInfo,
                                streakFreezeEnabled = streakFreezeEnabled,
                                onToggleStreakFreeze = { enabled ->
                                    KharcheeProfileAndToolsStore.setStreakFreezeEnabled(context, userId, enabled)
                                    refreshTrigger++
                                    onShowToast(
                                        if (enabled) "🧊 Streak Freeze Armed: Protects your streak on missed days"
                                        else "Streak Freeze Paused"
                                    )
                                },
                                onOpenSnapAndTrack = onOpenSnapAndTrack
                            )
                        }
                    }

                    "subscriptions" -> {
                        item {
                            SubscriptionRadarSection(
                                userId = userId,
                                stealthMode = settings.stealthMode,
                                fieldColors = fieldColors,
                                onShowToast = onShowToast
                            )
                        }
                    }

                    "net_worth" -> {
                        item {
                            NetWorthAndFinancialHealthSection(
                                userId = userId,
                                stealthMode = settings.stealthMode,
                                expenses = expenses,
                                savingsGoals = savingsGoals,
                                khataFriends = khataFriends,
                                monthSpent = monthSpent,
                                disciplineScore = disciplineScore,
                                streakDays = streakDays,
                                fieldColors = fieldColors,
                                onShowToast = onShowToast
                            )
                        }
                    }

                    "timeline" -> {
                        item {
                            MoneyTimelineCardSection(
                                userId = userId,
                                stealthMode = settings.stealthMode,
                                expenses = expenses,
                                paymentReminders = paymentReminders,
                                savingsGoals = savingsGoals,
                                onShowToast = onShowToast
                            )
                        }
                    }

                    "purchase_assistant" -> {
                        item {
                            PurchaseDecisionAssistantCardSection(
                                availableMonthlyBudget = max(1000L, settings.dailyLimit * 30L - monthSpent),
                                savingsGoals = savingsGoals,
                                dailyLimit = settings.dailyLimit
                            )
                        }
                    }

                    "emergency_meter" -> {
                        item {
                            EmergencyFundMeterCardSection(
                                userId = userId,
                                stealthMode = settings.stealthMode,
                                expenses = expenses,
                                savingsGoals = savingsGoals,
                                onShowToast = onShowToast
                            )
                        }
                    }

                    "salary_sim" -> {
                        item {
                            SalaryDaySimulatorCardSection(
                                userId = userId,
                                fieldColors = fieldColors,
                                onShowToast = onShowToast
                            )
                        }
                    }

                    "challenges" -> {
                        item {
                            SavingsChallengesCardSection(
                                userId = userId,
                                fieldColors = fieldColors,
                                onShowToast = onShowToast
                            )
                        }
                    }

                    "family_wallet" -> {
                        item {
                            FamilyWalletCardSection(
                                userId = userId,
                                userDisplayName = userDisplayName,
                                fieldColors = fieldColors,
                                onShowToast = onShowToast
                            )
                        }
                    }

                    "data_center" -> {
                        item {
                            FinancialDataCenterCardSection(
                                userId = userId,
                                expenses = expenses,
                                savingsGoals = savingsGoals,
                                onShowToast = onShowToast
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DreamVaultSection(
    userId: String,
    stealthMode: Boolean,
    savingsGoals: List<SavingsGoalRecord>,
    refreshTrigger: Int,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onCreateGoal: (title: String, targetAmount: Long, initialSaved: Long, targetDate: String) -> Unit,
    onContribute: (SavingsGoalRecord, Long) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var customTitle by remember { mutableStateOf("") }
    var customTargetStr by remember { mutableStateOf("") }
    var customInitialSavedStr by remember { mutableStateOf("0") }
    var customTargetDate by remember { mutableStateOf("31 Dec 2026") }
    var customContributionAmounts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dream Vault Hero Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(RichGraphite)
                .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "PART 9 · DREAM VAULT SAVINGS ARCHITECTURE",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = SophisticatedEmerald
            )
            Text(
                text = "Dedicated Savings Goals & Contribution History",
                fontFamily = headingFont,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmIvory
            )
            Text(
                text = "Create structured goals for Laptop, Phone, Bike, Education, Travel, Emergency Savings, or custom milestones with real-time completion forecasting.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedPlatinum
            )

            // 1-Tap Preset Templates
            Text(
                text = "QUICK GOAL TEMPLATES",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                color = ChampagneGold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DREAM_VAULT_PRESETS.chunked(3).forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPresets.forEach { preset ->
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(12.dp))
                                    .clickable {
                                        customTitle = preset.title
                                        customTargetStr = preset.defaultTarget.toString()
                                        customTargetDate = preset.defaultDate
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = preset.emoji, fontSize = 16.sp)
                                    Text(
                                        text = preset.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WarmIvory,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "₹${preset.defaultTarget}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = SophisticatedEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Custom / Selected Goal Builder Form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Goal Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.weight(1.2f).testTag("dream_vault_title_input")
                )
                OutlinedTextField(
                    value = customTargetStr,
                    onValueChange = { customTargetStr = it },
                    label = { Text("Target (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f).testTag("dream_vault_target_input")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customInitialSavedStr,
                    onValueChange = { customInitialSavedStr = it },
                    label = { Text("Initial Saved (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = customTargetDate,
                    onValueChange = { customTargetDate = it },
                    label = { Text("Target Date") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    val target = customTargetStr.trim().toLongOrNull() ?: 0L
                    val initial = customInitialSavedStr.trim().toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                    if (customTitle.isNotBlank() && target > 0L) {
                        onCreateGoal(customTitle.trim(), target, initial, customTargetDate.trim())
                        customTitle = ""
                        customTargetStr = ""
                        customInitialSavedStr = "0"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedEmerald,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("dream_vault_create_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Establish Dream Vault Goal", fontWeight = FontWeight.ExtraBold)
            }
        }

        // Active Dream Vault Goals List with Progress, Remaining, Estimated Completion & Contribution History
        savingsGoals.forEach { goal ->
            val target = max(1L, goal.targetAmount)
            val ratio = (goal.savedAmount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
            val remaining = max(0L, goal.targetAmount - goal.savedAmount)
            val isCompleted = remaining == 0L
            val targetDate = remember(goal.title, userId, refreshTrigger) {
                KharcheeProfileAndToolsStore.getGoalTargetDate(context, userId, goal.title)
            }
            val contributions = remember(goal.title, userId, refreshTrigger, goal.savedAmount) {
                KharcheeProfileAndToolsStore.getGoalContributions(context, userId, goal.title)
            }
            // Estimated completion calculation based on average contribution or ₹2,500/mo baseline
            val avgDeposit = remember(contributions, goal.savedAmount) {
                if (contributions.isNotEmpty()) {
                    max(500L, contributions.sumOf { it.amount } / contributions.size)
                } else {
                    max(1000L, goal.savedAmount.coerceAtLeast(1500L))
                }
            }
            val estDepositsLeft = if (remaining == 0L) 0 else ceil(remaining.toDouble() / avgDeposit.toDouble()).toInt()
            val estCompletionLabel = when {
                isCompleted -> "Goal Achieved ✓"
                estDepositsLeft <= 1 -> "~1 contribution away"
                else -> "~$estDepositsLeft contributions at ₹$avgDeposit avg"
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(RichGraphite)
                    .border(
                        0.8.dp,
                        if (isCompleted) SophisticatedEmerald else HairlinePlatinum,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Target Date: $targetDate · $estCompletionLabel",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = MutedPlatinum
                        )
                    }
                    Surface(
                        color = SophisticatedEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.6.dp, SophisticatedEmerald)
                    ) {
                        Text(
                            text = "${(ratio * 100).toInt()}%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = SophisticatedEmerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { ratio },
                    color = SophisticatedEmerald,
                    trackColor = ObsidianBlack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (stealthMode) "Saved: ₹•••" else "Saved: ₹${goal.savedAmount}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = SophisticatedEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (stealthMode) "Remaining: ₹•••" else "Remaining: ₹$remaining of ₹${goal.targetAmount}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = ChampagneGold
                    )
                }

                // Quick + Custom Contribution Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(500L, 1000L, 2500L).forEach { quickAmt ->
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { onContribute(goal, quickAmt) }
                        ) {
                            Text(
                                text = "+₹$quickAmt",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SophisticatedEmerald,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }

                    val customInput = customContributionAmounts[goal.id] ?: ""
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = {
                            customContributionAmounts = customContributionAmounts.toMutableMap().apply {
                                put(goal.id, it)
                            }
                        },
                        placeholder = { Text("Custom ₹", fontSize = 10.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    if (customInput.toLongOrNull() != null && customInput.toLong() > 0L) {
                        Surface(
                            color = ChampagneGold,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                val amt = customInput.toLongOrNull() ?: 0L
                                if (amt > 0L) {
                                    onContribute(goal, amt)
                                    customContributionAmounts = customContributionAmounts.toMutableMap().apply {
                                        remove(goal.id)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "Add",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = ObsidianBlack,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Contribution History Log
                if (contributions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianBlack)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "RECENT CONTRIBUTION HISTORY",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = MutedPlatinum
                        )
                        contributions.take(3).forEach { c ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${c.note} · ${c.dateLabel}",
                                    fontSize = 11.sp,
                                    color = MutedIvory
                                )
                                Text(
                                    text = if (stealthMode) "+₹•••" else "+₹${c.amount}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SophisticatedEmerald
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
private fun KharcheeStreaksAndBadgesSection(
    userId: String,
    userDisplayName: String,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    streakDays: Int,
    disciplineScore: Int,
    snapCount: Int,
    levelInfo: com.example.data.model.KharcheeStreakLevelInfo,
    streakFreezeEnabled: Boolean,
    onToggleStreakFreeze: (Boolean) -> Unit,
    onOpenSnapAndTrack: () -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val now = System.currentTimeMillis()

    // Privacy toggle for Shareable Achievement Cards: never expose personal balances unless user explicitly chooses
    var includeScoreInShare by remember { mutableStateOf(false) }

    val activeTrackingDays = remember(expenses) {
        var count = 0
        for (i in 0 until 30) {
            val dayTarget = now - i * 86_400_000L
            if (expenses.any { isSameDay(it.txEpochMillis, dayTarget) }) {
                count++
            }
        }
        max(1, count)
    }

    val badges = remember(expenses.size, streakDays, savingsGoals, snapCount, disciplineScore) {
        listOf(
            AchievementBadgeDef(
                id = "first_step",
                title = "First Step",
                subtitle = "Logged your first KHARCHEE transaction",
                unlocked = expenses.isNotEmpty(),
                iconSymbol = "🌱"
            ),
            AchievementBadgeDef(
                id = "seven_day_warrior",
                title = "7-Day Warrior",
                subtitle = "7+ consecutive days within daily budget",
                unlocked = streakDays >= 7,
                iconSymbol = "⚔️"
            ),
            AchievementBadgeDef(
                id = "thirty_day_consistency",
                title = "30-Day Consistency",
                subtitle = "30 days of disciplined reserve control",
                unlocked = streakDays >= 30,
                iconSymbol = "🏛️"
            ),
            AchievementBadgeDef(
                id = "money_master",
                title = "Money Master",
                subtitle = "Financial Behaviour Score ≥ 780",
                unlocked = disciplineScore >= 780,
                iconSymbol = "👑"
            ),
            AchievementBadgeDef(
                id = "smart_saver",
                title = "Smart Saver",
                subtitle = "Active Dream Vault goal funded",
                unlocked = savingsGoals.any { it.savedAmount > 0L },
                iconSymbol = "💎"
            ),
            AchievementBadgeDef(
                id = "receipt_pro",
                title = "Receipt Pro",
                subtitle = "Scanned receipts with SNAP & TRACK",
                unlocked = snapCount >= 1 || expenses.any { it.note.contains("[SNAP_RECEIPT]") },
                iconSymbol = "📸"
            ),
            AchievementBadgeDef(
                id = "consistency_king",
                title = "Consistency King",
                subtitle = "14+ day streak & weekly check-ins",
                unlocked = streakDays >= 14,
                iconSymbol = "🔥"
            ),
            AchievementBadgeDef(
                id = "financial_legend",
                title = "Financial Legend",
                subtitle = "Reached Platinum / Diamond tier",
                unlocked = levelInfo.levelName in listOf("Platinum", "Diamond"),
                iconSymbol = "🏆"
            )
        )
    }

    val unlockedBadgesCount = badges.count { it.unlocked }

    fun shareAchievementCard(targetChannel: String) {
        val privacySafeMetrics = if (includeScoreInShare) {
            "• Behaviour Score: $disciplineScore / 900\n"
        } else {
            "• Private Balances Protected 🛡️\n"
        }
        val shareText = buildString {
            appendLine("◆ KHARCHEE — FINANCIAL INTELLIGENCE ECOSYSTEM ◆")
            appendLine("Member: $userDisplayName")
            appendLine("Level: ${levelInfo.levelName.uppercase()} TIER")
            appendLine("🔥 Daily Budget Streak: ${streakDays} Days")
            appendLine("🏅 Badges Unlocked: $unlockedBadgesCount / ${badges.size}")
            append(privacySafeMetrics)
            appendLine("Tracked with KHARCHEE · Smart Money Habits")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My KHARCHEE Streak & Achievement Card")
            putExtra(Intent.EXTRA_TEXT, shareText)
            when (targetChannel) {
                "whatsapp" -> setPackage("com.whatsapp")
                "instagram" -> setPackage("com.instagram.android")
                "snapchat" -> setPackage("com.snapchat.android")
            }
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Share KHARCHEE Achievement Card"))
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(fallback, "Share KHARCHEE Achievement Card"))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Progressive Level Header (Bronze -> Silver -> Gold -> Platinum -> Diamond)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(RichGraphite)
                .border(1.dp, ChampagneGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PART 11 · KHARCHEE STREAKS & TIERS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = ChampagneGold
                    )
                    Text(
                        text = "${levelInfo.levelName} Level · ${streakDays}D Active Streak",
                        fontFamily = headingFont,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                Surface(
                    color = ChampagneGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, ChampagneGold)
                ) {
                    Text(
                        text = levelInfo.levelName.uppercase(),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChampagneGold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Tier Progression Bar: Bronze -> Silver -> Gold -> Platinum -> Diamond
            Text(
                text = "TIER ROADMAP: BRONZE ➔ SILVER ➔ GOLD ➔ PLATINUM ➔ DIAMOND",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                color = MetallicSilver
            )
            LinearProgressIndicator(
                progress = { levelInfo.progressRatio },
                color = ChampagneGold,
                trackColor = ObsidianBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )

            // 5 Streak Types Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreakMetricTile("Daily Tracking", "${activeTrackingDays}d", Modifier.weight(1f))
                StreakMetricTile("Budget Check", "${streakDays}d", Modifier.weight(1f))
                StreakMetricTile("Weekly Review", "${max(1, streakDays / 7)}w", Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreakMetricTile("Monthly Budget", if (streakDays >= 25) "On Track" else "Active", Modifier.weight(1f))
                StreakMetricTile("SNAP & TRACK", "$snapCount scans", Modifier.weight(1f))
            }

            // Streak Freeze Protection Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🧊 Streak Freeze Protection",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = WarmIvory
                    )
                    Text(
                        text = "Protects your streak if you miss 1 day of manual logging.",
                        fontSize = 11.sp,
                        color = MutedPlatinum
                    )
                }
                Switch(
                    checked = streakFreezeEnabled,
                    onCheckedChange = onToggleStreakFreeze,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = SophisticatedEmerald
                    ),
                    modifier = Modifier.testTag("streak_freeze_switch")
                )
            }
        }

        // 8 Achievement Badges Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(RichGraphite)
                .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACHIEVEMENT BADGES ($unlockedBadgesCount / ${badges.size})",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SophisticatedEmerald
                )
                Text(
                    text = "Scan Receipt ➔",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = ChampagneGold,
                    modifier = Modifier.clickable { onOpenSnapAndTrack() }
                )
            }

            badges.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { badge ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (badge.unlocked) ObsidianBlack else ElevatedGraphite.copy(alpha = 0.55f))
                                .border(
                                    0.8.dp,
                                    if (badge.unlocked) SophisticatedEmerald else HairlinePlatinum,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = badge.iconSymbol, fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = badge.title,
                                        fontFamily = headingFont,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (badge.unlocked) WarmIvory else MutedIvory
                                    )
                                    Text(
                                        text = if (badge.unlocked) "UNLOCKED" else "LOCKED",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        color = if (badge.unlocked) SophisticatedEmerald else MutedPlatinum
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = badge.subtitle,
                                fontSize = 10.sp,
                                color = MutedPlatinum,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Monthly Achievement Report Card & Shareable Achievement Card (WhatsApp, Instagram Stories, Snapchat)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF080A0D),
                            Color(0xFF151922),
                            Color(0xFF1D142B)
                        )
                    )
                )
                .border(1.2.dp, Color(0xFF9D4EDD).copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                .padding(18.dp)
                .testTag("shareable_achievement_card"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KharcheeArchitecturalLogo(
                    symbolSize = 30.dp,
                    wordmarkFontSizeSp = 14,
                    subtitle = "MONTHLY ACHIEVEMENT REPORT CARD"
                )
                Surface(
                    color = SophisticatedEmerald.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.6.dp, SophisticatedEmerald)
                ) {
                    Text(
                        text = "${levelInfo.levelName.uppercase()} TIER",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SophisticatedEmerald,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MEMBER",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = MutedPlatinum
                    )
                    Text(
                        text = userDisplayName,
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = WarmIvory
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "STREAK",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = MutedPlatinum
                    )
                    Text(
                        text = "${streakDays} Days 🔥",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = ChampagneGold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BADGES",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = MutedPlatinum
                    )
                    Text(
                        text = "$unlockedBadgesCount / ${badges.size} 🏅",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = SophisticatedEmerald
                    )
                }
            }

            // Privacy Protection Notice & Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️ Privacy Protected: Account balances are never shared.",
                    fontSize = 11.sp,
                    color = MetallicSilver,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { includeScoreInShare = !includeScoreInShare }
                ) {
                    Text(
                        text = if (includeScoreInShare) "Include Score ✓" else "Hide Score",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = ChampagneGold
                    )
                }
            }

            // Share Buttons: WhatsApp, Instagram Stories, Snapchat, System Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { shareAchievementCard("whatsapp") },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, SophisticatedEmerald),
                    modifier = Modifier.weight(1f).testTag("share_streak_whatsapp_button")
                ) {
                    Text("WhatsApp", fontSize = 11.sp, color = SophisticatedEmerald, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { shareAchievementCard("instagram") },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF9D4EDD)),
                    modifier = Modifier.weight(1f).testTag("share_streak_instagram_button")
                ) {
                    Text("IG Story", fontSize = 11.sp, color = Color(0xFFD0A9F5), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { shareAchievementCard("snapchat") },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, ChampagneGold),
                    modifier = Modifier.weight(1f).testTag("share_streak_snapchat_button")
                ) {
                    Text("Snapchat", fontSize = 11.sp, color = ChampagneGold, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { shareAchievementCard("system") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("share_streak_system_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Card", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun StreakMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianBlack)
            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            color = MutedPlatinum,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = SophisticatedEmerald
        )
    }
}

@Composable
private fun SubscriptionRadarSection(
    userId: String,
    stealthMode: Boolean,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var subscriptions by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getSubscriptions(context, userId))
    }
    var subName by remember { mutableStateOf("") }
    var subAmountStr by remember { mutableStateOf("") }
    var subCycle by remember { mutableStateOf("Monthly") }
    var subRenewalDate by remember { mutableStateOf("05 Dec") }
    var subPaymentMethod by remember { mutableStateOf("UPI") }

    val activeSubs = subscriptions.filter { it.isActive }
    val monthlyTotal = activeSubs.sumOf {
        if (it.billingCycle == "Yearly") it.amount / 12L else it.amount
    }
    val annualTotal = activeSubs.sumOf {
        if (it.billingCycle == "Yearly") it.amount else it.amount * 12L
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RichGraphite)
            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PART 12 · SUBSCRIPTION RADAR",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = ChampagneGold
        )
        Text(
            text = "Recurring Subscriptions & Renewal Radar",
            fontFamily = headingFont,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmIvory
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text("MONTHLY RECURRING", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
                Text(
                    text = if (stealthMode) "₹•••/mo" else "₹$monthlyTotal/mo",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = ChampagneGold
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text("ANNUAL COMMITMENT", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
                Text(
                    text = if (stealthMode) "₹•••/yr" else "₹$annualTotal/yr",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = CrimsonRuby
                )
            }
        }

        // Quick Add Subscription Form
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = subName,
                onValueChange = { subName = it },
                label = { Text("Service (Netflix, Gym)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.weight(1.2f)
            )
            OutlinedTextField(
                value = subAmountStr,
                onValueChange = { subAmountStr = it },
                label = { Text("Amount (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.weight(0.8f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = subRenewalDate,
                onValueChange = { subRenewalDate = it },
                label = { Text("Next Renewal") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            listOf("Monthly", "Yearly").forEach { cycle ->
                val selected = subCycle == cycle
                Surface(
                    color = if (selected) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .border(0.8.dp, if (selected) ChampagneGold else HairlinePlatinum, RoundedCornerShape(12.dp))
                        .clickable { subCycle = cycle }
                ) {
                    Text(
                        text = cycle,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = if (selected) ChampagneGold else MutedPlatinum,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (PAYMENT_MODES + listOf("Bank")).forEach { mode ->
                val selected = subPaymentMethod == mode
                Surface(
                    color = if (selected) SophisticatedEmerald.copy(alpha = 0.16f) else ObsidianBlack,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(0.8.dp, if (selected) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                        .clickable { subPaymentMethod = mode }
                ) {
                    Text(
                        text = mode,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = if (selected) SophisticatedEmerald else MutedPlatinum,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                val amt = subAmountStr.trim().toLongOrNull() ?: 0L
                if (subName.isNotBlank() && amt > 0L) {
                    subscriptions = KharcheeProfileAndToolsStore.addSubscription(
                        context = context,
                        userId = userId,
                        item = SubscriptionItem(
                            id = UUID.randomUUID().toString(),
                            name = subName.trim(),
                            amount = amt,
                            billingCycle = subCycle,
                            nextRenewalDate = subRenewalDate.ifBlank { "05 Dec" },
                            paymentMethod = subPaymentMethod,
                            isActive = true,
                            reminderEnabled = true
                        )
                    )
                    subName = ""
                    subAmountStr = ""
                    onShowToast("✅ Added to Subscription Radar")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Subscription to Radar", fontWeight = FontWeight.ExtraBold)
        }

        subscriptions.forEach { sub ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(
                        0.8.dp,
                        if (sub.isActive) HairlinePlatinum else CrimsonRuby.copy(alpha = 0.4f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sub.name,
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (sub.isActive) WarmIvory else MutedIvory
                        )
                        Surface(
                            color = if (sub.isActive) SophisticatedEmerald.copy(alpha = 0.14f) else CrimsonRuby.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable {
                                subscriptions = KharcheeProfileAndToolsStore.toggleSubscriptionActive(context, userId, sub.id)
                            }
                        ) {
                            Text(
                                text = if (sub.isActive) "ACTIVE" else "CANCELLED",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sub.isActive) SophisticatedEmerald else CrimsonRuby,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${sub.billingCycle} · Next: ${sub.nextRenewalDate} · ${sub.paymentMethod}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = MutedPlatinum
                    )
                }

                Text(
                    text = if (stealthMode) "₹•••" else "₹${sub.amount}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = ChampagneGold
                )

                IconButton(
                    onClick = {
                        subscriptions = KharcheeProfileAndToolsStore.toggleSubscriptionReminder(context, userId, sub.id)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Toggle Renewal Reminder",
                        tint = if (sub.reminderEnabled) SophisticatedEmerald else MutedIvory,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        subscriptions = KharcheeProfileAndToolsStore.removeSubscription(context, userId, sub.id)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove Subscription",
                        tint = CrimsonRuby,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NetWorthAndFinancialHealthSection(
    userId: String,
    stealthMode: Boolean,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    khataFriends: List<KhataFriendRecord>,
    monthSpent: Long,
    disciplineScore: Int,
    streakDays: Int,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var wallets by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getManualWallets(context, userId))
    }
    var emis by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getEmis(context, userId))
    }

    val totalIncome = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
    val totalExpense = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
    val ledgerCash = max(0L, totalIncome - totalExpense)
    val manualWalletAssets = wallets.sumOf { it.manualBalance }
    val savingsAssets = savingsGoals.sumOf { it.savedAmount }
    val khataReceivable = khataFriends.filter { it.balance > 0L }.sumOf { it.balance }
    val khataPayable = khataFriends.filter { it.balance < 0L }.sumOf { -it.balance }
    val emiLiabilities = emis.sumOf { it.monthlyEmi * it.remainingMonths }

    val totalAssets = ledgerCash + manualWalletAssets + savingsAssets + khataReceivable
    val totalLiabilities = khataPayable + emiLiabilities
    val netWorth = totalAssets - totalLiabilities

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RichGraphite)
            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "PART 16 · FINANCIAL HEALTH & NET WORTH",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = SophisticatedEmerald
        )
        Text(
            text = if (stealthMode) "Net Worth: ₹••••••" else "Net Worth: ₹$netWorth",
            fontFamily = headingFont,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (netWorth >= 0L) SophisticatedEmerald else CrimsonRuby
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text("TOTAL ASSETS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
                Text(
                    text = if (stealthMode) "₹••••" else "₹$totalAssets",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = SophisticatedEmerald
                )
                Text(
                    text = "Cash + Manual Wallets + Dream Vault + Khata Owed",
                    fontSize = 10.sp,
                    color = MutedIvory
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, CrimsonRuby.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text("TOTAL LIABILITIES", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
                Text(
                    text = if (stealthMode) "₹••••" else "₹$totalLiabilities",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = CrimsonRuby
                )
                Text(
                    text = "Loans + EMIs + Khata Payables",
                    fontSize = 10.sp,
                    color = MutedIvory
                )
            }
        }

        // Transparent Financial Behaviour Score Card (Explicitly disclosed as NOT an official bank/CIBIL credit score)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianBlack)
                .border(0.8.dp, ChampagneGold.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KHARCHEE FINANCIAL BEHAVIOUR SCORE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChampagneGold
                )
                Text(
                    text = "$disciplineScore / 900",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = SophisticatedEmerald
                )
            }
            Text(
                text = "Calculated transparently from your daily budget adherence (${streakDays}d streak), savings goal funding, and impulse control. Note: This is an internal KHARCHEE Financial Behaviour Score and is NOT an official bank or CIBIL bureau credit score.",
                fontSize = 11.sp,
                color = MutedPlatinum,
                lineHeight = 16.sp
            )
        }
    }
}
