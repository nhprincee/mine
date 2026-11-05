package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseRecord
import com.example.data.model.KharcheeEcosystemFeaturesStore
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
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
import kotlin.math.max

@Composable
fun MoneyTimelineCardSection(
    userId: String,
    stealthMode: Boolean,
    expenses: List<ExpenseRecord>,
    paymentReminders: List<PaymentReminderRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var viewMode by remember { mutableStateOf("Month") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val (savedSalaryDay, savedSalaryAmt) = remember(userId, refreshKey) {
        KharcheeEcosystemFeaturesStore.getExpectedSalaryConfig(context, userId)
    }
    var salaryDayInput by remember(savedSalaryDay) { mutableStateOf(savedSalaryDay.toString()) }
    var salaryAmountInput by remember(savedSalaryAmt) { mutableStateOf(savedSalaryAmt.toString()) }

    val timelineEvents = remember(userId, expenses, paymentReminders, savingsGoals, viewMode, refreshKey) {
        KharcheeEcosystemFeaturesStore.buildMoneyTimelineEvents(
            context = context,
            userId = userId,
            expenses = expenses,
            paymentReminders = paymentReminders,
            savingsGoals = savingsGoals,
            viewMode = viewMode
        )
    }
    val filteredEvents = remember(timelineEvents, selectedTypeFilter) {
        if (selectedTypeFilter == "ALL") timelineEvents
        else timelineEvents.filter { it.eventType == selectedTypeFilter }
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RichGraphite)
            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("feature_money_timeline_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FEATURE 1 · MONEY TIMELINE & CALENDAR",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = ChampagneGold
                )
                Text(
                    text = "Income, Salary, Bills, EMIs & Savings",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Month", "Week").forEach { mode ->
                    val active = viewMode == mode
                    Surface(
                        color = if (active) ChampagneGold.copy(alpha = 0.18f) else ObsidianBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { viewMode = mode }
                            .testTag("timeline_view_$mode")
                    ) {
                        Text(
                            text = mode,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) ChampagneGold else MutedPlatinum,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = salaryDayInput,
                onValueChange = { salaryDayInput = it },
                label = { Text("Salary Day (1-31)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                modifier = Modifier.weight(0.9f)
            )
            OutlinedTextField(
                value = salaryAmountInput,
                onValueChange = { salaryAmountInput = it },
                label = { Text("Expected Salary (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                modifier = Modifier.weight(1.1f)
            )
            Button(
                onClick = {
                    val d = salaryDayInput.trim().toIntOrNull()?.coerceIn(1, 31) ?: 1
                    val a = salaryAmountInput.trim().toLongOrNull()?.coerceAtLeast(0L) ?: 60000L
                    KharcheeEcosystemFeaturesStore.saveExpectedSalaryConfig(context, userId, d, a)
                    refreshKey++
                    onShowToast("📅 Expected Salary scheduled for Day $d (₹$a)")
                },
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = ObsidianBlack),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "SALARY", "INCOME", "BILL", "EMI", "SUBSCRIPTION", "SAVINGS", "EXPENSE").forEach { type ->
                val selected = selectedTypeFilter == type
                Surface(
                    color = if (selected) SophisticatedEmerald.copy(alpha = 0.16f) else ObsidianBlack,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .border(0.8.dp, if (selected) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(50))
                        .clickable { selectedTypeFilter = type }
                ) {
                    Text(
                        text = type,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) SophisticatedEmerald else MutedPlatinum,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        filteredEvents.forEach { ev ->
            val isInflow = ev.eventType in listOf("INCOME", "SALARY")
            val badgeColor = when (ev.eventType) {
                "INCOME", "SALARY" -> SophisticatedEmerald
                "SAVINGS" -> ChampagneGold
                else -> CrimsonRuby
            }
            val isExpanded = selectedEventId == ev.id

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, if (isExpanded) badgeColor else HairlinePlatinum, RoundedCornerShape(14.dp))
                    .clickable { selectedEventId = if (isExpanded) null else ev.id }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        Surface(
                            color = badgeColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.6.dp, badgeColor)
                        ) {
                            Text(
                                text = ev.dateLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = ev.title,
                                fontFamily = headingFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = WarmIvory
                            )
                            Text(
                                text = "${ev.eventType} · ${if (ev.isCompleted) "Completed ✓" else "Scheduled / Upcoming"}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                color = MutedPlatinum
                            )
                        }
                    }

                    Text(
                        text = if (stealthMode) "₹•••" else "${if (isInflow) "+" else "-"}₹${ev.amount}",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (isInflow) SophisticatedEmerald else WarmIvory
                    )
                }

                if (isExpanded && ev.details.isNotBlank()) {
                    HorizontalDivider(color = HairlinePlatinum, thickness = 0.5.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ev.details,
                            fontSize = 11.sp,
                            color = MetallicSilver,
                            modifier = Modifier.weight(1f)
                        )
                        if (ev.reminderEnabled) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Reminder Armed",
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reminder ON",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = ChampagneGold
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
fun PurchaseDecisionAssistantCardSection(
    availableMonthlyBudget: Long,
    savingsGoals: List<SavingsGoalRecord>,
    dailyLimit: Long
) {
    val headingFont = LocalAppFontFamily.current

    var productName by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var budgetInput by remember(availableMonthlyBudget) {
        mutableStateOf(max(1000L, availableMonthlyBudget).toString())
    }
    var urgency by remember { mutableStateOf("Medium") }
    var selectedGoalIndex by remember { mutableIntStateOf(0) }

    val activeGoal = savingsGoals.getOrNull(selectedGoalIndex)
    val parsedPrice = priceInput.trim().toLongOrNull() ?: 0L
    val parsedBudget = budgetInput.trim().toLongOrNull() ?: max(1000L, availableMonthlyBudget)

    val evaluation = remember(productName, parsedPrice, parsedBudget, urgency, activeGoal, dailyLimit) {
        KharcheeEcosystemFeaturesStore.evaluatePurchaseDecision(
            productName = productName,
            purchasePrice = parsedPrice,
            availableBudget = parsedBudget,
            urgency = urgency,
            goalName = activeGoal?.title ?: "",
            goalTarget = activeGoal?.targetAmount ?: 0L,
            goalSaved = activeGoal?.savedAmount ?: 0L,
            dailySavingCapacity = max(150L, dailyLimit / 2L)
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SophisticatedEmerald,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory,
        focusedLabelColor = SophisticatedEmerald,
        unfocusedLabelColor = MutedIvory
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RichGraphite)
            .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("feature_purchase_decision_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "FEATURE 2 · PURCHASE DECISION ASSISTANT",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = SophisticatedEmerald
        )
        Text(
            text = "Real-Time Affordability & Goal Impact Calculator",
            fontFamily = headingFont,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmIvory
        )
        Text(
            text = "Evaluate how a planned purchase affects your remaining budget and Dream Vault goals. Advisory only—never triggers a purchase or payment automatically.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedPlatinum
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                placeholder = { Text("e.g., Sony Headphones") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.weight(1.2f).testTag("pda_product_name_input")
            )
            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it },
                label = { Text("Price (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.weight(0.8f).testTag("pda_price_input")
            )
        }

        OutlinedTextField(
            value = budgetInput,
            onValueChange = { budgetInput = it },
            label = { Text("Available Budget (₹)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth().testTag("pda_budget_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Low", "Medium", "High", "Essential").forEach { level ->
                val selected = urgency == level
                Surface(
                    color = if (selected) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(0.8.dp, if (selected) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                        .clickable { urgency = level }
                ) {
                    Text(
                        text = level,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) ChampagneGold else MutedPlatinum,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                    )
                }
            }
        }

        if (savingsGoals.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                savingsGoals.forEachIndexed { index, g ->
                    val selected = selectedGoalIndex == index
                    Surface(
                        color = if (selected) SophisticatedEmerald.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .border(0.8.dp, if (selected) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { selectedGoalIndex = index }
                    ) {
                        Text(
                            text = "${g.title} (₹${g.savedAmount}/₹${g.targetAmount})",
                            fontSize = 11.sp,
                            color = if (selected) SophisticatedEmerald else MutedPlatinum,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (parsedPrice > 0L) {
            val verdictColor = when (evaluation.affordabilityVerdict) {
                "EASILY AFFORDABLE" -> SophisticatedEmerald
                "MODERATE STRETCH" -> ChampagneGold
                else -> CrimsonRuby
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, verdictColor.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("pda_result_box"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = evaluation.affordabilityVerdict,
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = verdictColor
                    )
                    if (evaluation.suggestedWaitDays > 0) {
                        Text(
                            text = "Suggested Wait: ${evaluation.suggestedWaitDays} Days",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = verdictColor
                        )
                    }
                }

                Text(
                    text = "Remaining Budget After: ₹${evaluation.remainingBudgetAfter} (${evaluation.budgetImpactPercent}% used)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = if (evaluation.remainingBudgetAfter >= 0L) SophisticatedEmerald else CrimsonRuby
                )
                if (evaluation.goalName.isNotBlank()) {
                    Text(
                        text = "Effect on '${evaluation.goalName}': ~${evaluation.goalDelayDays} days delay (${evaluation.goalImpactPercent}% of remaining target)",
                        fontSize = 11.sp,
                        color = MetallicSilver
                    )
                }
                Text(
                    text = evaluation.recommendationSummary,
                    fontSize = 12.sp,
                    color = WarmIvory,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun EmergencyFundMeterCardSection(
    userId: String,
    stealthMode: Boolean,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var manualSaved by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getEmergencyFundSaved(context, userId))
    }
    var targetMonths by remember(userId) {
        mutableIntStateOf(KharcheeEcosystemFeaturesStore.getEmergencyTargetMonths(context, userId))
    }
    var customTargetOverride by remember(userId) {
        mutableStateOf(KharcheeEcosystemFeaturesStore.getCustomEmergencyTargetAmount(context, userId))
    }

    var manualInputStr by remember(manualSaved) { mutableStateOf(manualSaved.toString()) }
    var customTargetInputStr by remember(customTargetOverride) {
        mutableStateOf(if (customTargetOverride > 0L) customTargetOverride.toString() else "")
    }

    val metrics = remember(manualSaved, savingsGoals, expenses, targetMonths, customTargetOverride) {
        KharcheeEcosystemFeaturesStore.calculateEmergencyFundMetrics(
            manualEmergencySavings = manualSaved,
            savingsGoals = savingsGoals,
            expenses = expenses,
            targetMonths = targetMonths,
            customTargetOverride = customTargetOverride
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SophisticatedEmerald,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory,
        focusedLabelColor = SophisticatedEmerald,
        unfocusedLabelColor = MutedIvory
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RichGraphite)
            .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("feature_emergency_fund_meter_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FEATURE 3 · EMERGENCY FUND METER",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = SophisticatedEmerald
                )
                Text(
                    text = "${metrics.monthsCovered} Months of Essentials Covered",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
            }
            Surface(
                color = SophisticatedEmerald.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.6.dp, SophisticatedEmerald)
            ) {
                Text(
                    text = "${metrics.progressPercent}%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = SophisticatedEmerald,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { (metrics.progressPercent / 100f).coerceIn(0f, 1f) },
            color = SophisticatedEmerald,
            trackColor = ObsidianBlack,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianBlack)
                .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Manual Emergency Reserve (MANUAL ENTRY):", fontSize = 11.sp, color = MutedPlatinum)
                Text(
                    text = if (stealthMode) "₹••••" else "₹${metrics.manualEmergencySavings}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChampagneGold
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dream Vault Emergency Goal Saved:", fontSize = 11.sp, color = MutedPlatinum)
                Text(
                    text = if (stealthMode) "₹••••" else "₹${metrics.dreamVaultEmergencySavings}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SophisticatedEmerald
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Verified Passbook Net Cash (Separate):", fontSize = 11.sp, color = MutedPlatinum)
                Text(
                    text = if (stealthMode) "₹••••" else "₹${metrics.verifiedLedgerCashBalance}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = MetallicSilver
                )
            }
            HorizontalDivider(color = HairlinePlatinum, thickness = 0.5.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Monthly Essential Expenses:", fontSize = 11.sp, color = WarmIvory, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (stealthMode) "₹••••/mo" else "₹${metrics.monthlyEssentialExpenses}/mo",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target (${metrics.targetMonths}M) & Remaining:", fontSize = 11.sp, color = WarmIvory)
                Text(
                    text = if (stealthMode) "₹••••" else "₹${metrics.userDefinedTargetAmount} (Rem: ₹${metrics.remainingTargetAmount})",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = ChampagneGold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(3, 6, 9, 12).forEach { m ->
                val selected = targetMonths == m && customTargetOverride == 0L
                Surface(
                    color = if (selected) SophisticatedEmerald.copy(alpha = 0.16f) else ObsidianBlack,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(0.8.dp, if (selected) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                        .clickable {
                            targetMonths = m
                            customTargetOverride = 0L
                            customTargetInputStr = ""
                            KharcheeEcosystemFeaturesStore.setEmergencyTargetMonths(context, userId, m)
                            KharcheeEcosystemFeaturesStore.setCustomEmergencyTargetAmount(context, userId, 0L)
                        }
                ) {
                    Text(
                        text = "${m}M Target",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) SophisticatedEmerald else MutedPlatinum,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
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
                value = manualInputStr,
                onValueChange = { manualInputStr = it },
                label = { Text("Manual Reserve (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = customTargetInputStr,
                onValueChange = { customTargetInputStr = it },
                label = { Text("Custom Target (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val newManual = manualInputStr.trim().toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                    val newCustom = customTargetInputStr.trim().toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                    manualSaved = newManual
                    customTargetOverride = newCustom
                    KharcheeProfileAndToolsStore.setEmergencyFundSaved(context, userId, newManual)
                    KharcheeEcosystemFeaturesStore.setCustomEmergencyTargetAmount(context, userId, newCustom)
                    onShowToast("🛡️ Emergency Fund Meter updated")
                },
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = ObsidianBlack),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
        }
    }
}
