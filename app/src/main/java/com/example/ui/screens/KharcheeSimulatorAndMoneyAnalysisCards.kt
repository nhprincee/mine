package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.EmiItem
import com.example.data.model.ExpenseRecord
import com.example.data.model.KharcheeEcosystemFeaturesStore
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SalaryAllocationScenario
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.SubscriptionItem
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
import kotlin.math.roundToInt

@Composable
fun SalaryDaySimulatorCardSection(
    userId: String,
    fieldColors: TextFieldColors,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val salaryCfg = remember(userId) { KharcheeEcosystemFeaturesStore.getExpectedSalaryConfig(context, userId) }
    var incomeInput by remember { mutableStateOf(if (salaryCfg.first > 0L) salaryCfg.first.toString() else "75000") }

    var essentialPct by remember { mutableStateOf(35f) }
    var savingsPct by remember { mutableStateOf(20f) }
    var emiPct by remember { mutableStateOf(15f) }
    var billsPct by remember { mutableStateOf(10f) }
    var subsPct by remember { mutableStateOf(5f) }
    var personalPct by remember { mutableStateOf(10f) }
    var goalsPct by remember { mutableStateOf(5f) }

    val activeScenario = SalaryAllocationScenario(
        name = "Custom Allocation",
        essentialPct = essentialPct.roundToInt(),
        savingsPct = savingsPct.roundToInt(),
        emiPct = emiPct.roundToInt(),
        billsPct = billsPct.roundToInt(),
        subscriptionsPct = subsPct.roundToInt(),
        personalPct = personalPct.roundToInt(),
        goalsPct = goalsPct.roundToInt()
    )
    val conservativeScenario = SalaryAllocationScenario("High Savings (35%)", 30, 25, 15, 10, 3, 7, 10)
    val result = remember(incomeInput, activeScenario) {
        KharcheeEcosystemFeaturesStore.simulateSalaryAllocation(incomeInput.toLongOrNull() ?: 0L, activeScenario)
    }
    val compareResult = remember(incomeInput) {
        KharcheeEcosystemFeaturesStore.simulateSalaryAllocation(incomeInput.toLongOrNull() ?: 0L, conservativeScenario)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RichGraphite)
            .border(1.dp, HairlineGold, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("salary_day_simulator_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("SALARY DAY SIMULATOR", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChampagneGold)
        Text("Simulate monthly salary splits across 7 buckets without moving real funds", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        OutlinedTextField(
            value = incomeInput,
            onValueChange = { incomeInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Expected Monthly Salary (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth().testTag("salary_sim_income_input")
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    essentialPct = 35f; savingsPct = 20f; emiPct = 15f; billsPct = 10f; subsPct = 5f; personalPct = 10f; goalsPct = 5f
                },
                modifier = Modifier.weight(1f)
            ) { Text("BALANCED 50/30/20", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ChampagneGold) }
            OutlinedButton(
                onClick = {
                    essentialPct = 30f; savingsPct = 25f; emiPct = 15f; billsPct = 10f; subsPct = 3f; personalPct = 7f; goalsPct = 10f
                },
                modifier = Modifier.weight(1f)
            ) { Text("WEALTH BUILDER", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = SophisticatedEmerald) }
        }

        listOf(
            Triple("Essential Expenses (${essentialPct.roundToInt()}%)", result.essentialAmount) { v: Float -> essentialPct = v },
            Triple("Savings Reserve (${savingsPct.roundToInt()}%)", result.savingsAmount) { v: Float -> savingsPct = v },
            Triple("EMI Payments (${emiPct.roundToInt()}%)", result.emiAmount) { v: Float -> emiPct = v },
            Triple("Utility Bills (${billsPct.roundToInt()}%)", result.billsAmount) { v: Float -> billsPct = v },
            Triple("Subscriptions (${subsPct.roundToInt()}%)", result.subscriptionsAmount) { v: Float -> subsPct = v },
            Triple("Personal Spending (${personalPct.roundToInt()}%)", result.personalAmount) { v: Float -> personalPct = v },
            Triple("Financial Goals (${goalsPct.roundToInt()}%)", result.goalsAmount) { v: Float -> goalsPct = v }
        ).forEachIndexed { idx, (label, amount, onChange) ->
            val currentPct = when (idx) {
                0 -> essentialPct; 1 -> savingsPct; 2 -> emiPct; 3 -> billsPct; 4 -> subsPct; 5 -> personalPct; else -> goalsPct
            }
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                    Text("₹$amount", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ChampagneGold)
                }
                Slider(
                    value = currentPct,
                    onValueChange = onChange,
                    valueRange = 0f..70f,
                    colors = SliderDefaults.colors(thumbColor = ChampagneGold, activeTrackColor = ChampagneGold)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianBlack)
                .border(0.8.dp, if (result.unallocatedAmount >= 0L) SophisticatedEmerald else CrimsonRuby, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "SCENARIO COMPARISON (SIMULATION ONLY)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ChampagneGold
            )
            Text(
                text = "Your Split Savings+Goals: ₹${result.savingsAmount + result.goalsAmount}/mo vs Wealth Builder: ₹${compareResult.savingsAmount + compareResult.goalsAmount}/mo",
                style = MaterialTheme.typography.bodySmall,
                color = WarmIvory
            )
            Text(
                text = if (result.unallocatedAmount >= 0L) "Unallocated Buffer: ₹${result.unallocatedAmount}" else "Over-allocated by ₹${-result.unallocatedAmount} (${result.totalAllocatedPct}%)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = if (result.unallocatedAmount >= 0L) SophisticatedEmerald else CrimsonRuby
            )
        }
    }
}

@Composable
fun WhereDidMyMoneyGoCardSection(
    userId: String,
    expenses: List<ExpenseRecord>,
    subscriptions: List<SubscriptionItem>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val analysis = remember(expenses, subscriptions) {
        KharcheeEcosystemFeaturesStore.analyzeWhereMoneyWent(expenses, subscriptions)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RichGraphite)
            .border(1.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("where_did_my_money_go_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WHERE DID MY MONEY GO?", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChampagneGold)
            }
            OutlinedButton(
                onClick = {
                    val reportText = buildString {
                        appendLine("KHARCHEE — WHERE DID MY MONEY GO REPORT")
                        appendLine("This Month Total: ₹${analysis.totalThisMonth} (Prev Month: ₹${analysis.totalPrevMonth})")
                        appendLine("Summary: ${analysis.naturalLanguageSummary}")
                        appendLine("Top Categories: ${analysis.categorySpending.take(4).joinToString { "${it.first}: ₹${it.second}" }}")
                        appendLine("Top Merchants: ${analysis.merchantSpending.take(4).joinToString { "${it.first}: ₹${it.second}" }}")
                    }
                    KharcheeEcosystemFeaturesStore.recordDataExport(context, userId, "Where Did My Money Go Report", analysis.categorySpending.size)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, reportText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Export Spending Breakdown"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share Report", tint = ChampagneGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("EXPORT", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ChampagneGold)
            }
        }

        Text(analysis.naturalLanguageSummary, style = MaterialTheme.typography.bodySmall, color = WarmIvory)

        if (analysis.merchantSpending.isNotEmpty()) {
            Text("TOP MERCHANTS THIS MONTH", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
            analysis.merchantSpending.take(4).forEach { (merchant, amt) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(merchant, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                    Text("₹$amt", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                }
            }
        }

        if (analysis.highestSpendingDays.isNotEmpty()) {
            Text("HIGHEST SPENDING DAYS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
            analysis.highestSpendingDays.take(3).forEach { (day, amt) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(day, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                    Text("₹$amt", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ChampagneGold)
                }
            }
        }

        if (analysis.biggestCategoryChanges.isNotEmpty()) {
            Text("BIGGEST CHANGES VS PREVIOUS MONTH", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
            analysis.biggestCategoryChanges.take(3).forEach { (cat, diff) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(cat, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                    Text(
                        text = if (diff >= 0L) "+₹$diff" else "-₹${-diff}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = if (diff > 0L) CrimsonRuby else SophisticatedEmerald
                    )
                }
            }
        }

        if (analysis.unusualTransactions.isNotEmpty()) {
            Text("UNUSUAL SPENDING PATTERNS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby)
            analysis.unusualTransactions.take(2).forEach { tx ->
                Text("• ${tx.title} (₹${tx.amount} in ${tx.category})", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            }
        }
    }
}

@Composable
fun AskYourMoneyCardSection(
    userId: String,
    expenses: List<ExpenseRecord>,
    subscriptions: List<SubscriptionItem>,
    savingsGoals: List<SavingsGoalRecord>,
    fieldColors: TextFieldColors
) {
    val context = LocalContext.current
    var queryInput by remember { mutableStateOf("") }
    var qaHistory by remember(userId) {
        mutableStateOf(KharcheeEcosystemFeaturesStore.getAskYourMoneyHistory(context, userId))
    }

    val samplePrompts = listOf(
        "How much did I spend on food this month?",
        "What was my biggest expense?",
        "How much did I save this month?",
        "Which subscriptions are coming up?",
        "Compare this month with last month"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RichGraphite)
            .border(1.dp, HairlineGold, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("ask_your_money_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("ASK YOUR MONEY (REAL-DATA Q&A)", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChampagneGold)
                Text("Instant factual answers in English, Hindi & Hinglish from your actual passbook", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            samplePrompts.forEach { q ->
                Surface(
                    color = ObsidianBlack,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(50))
                        .clickable {
                            val ans = KharcheeEcosystemFeaturesStore.answerAskYourMoneyQuery(q, expenses, subscriptions, savingsGoals)
                            qaHistory = KharcheeEcosystemFeaturesStore.recordAskYourMoneyTurn(context, userId, q, ans)
                        }
                ) {
                    Text(q, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = queryInput,
                onValueChange = { queryInput = it },
                label = { Text("Ask in English or Hindi...") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f).testTag("ask_your_money_input")
            )
            Button(
                onClick = {
                    val q = queryInput.trim()
                    if (q.isNotBlank()) {
                        val ans = KharcheeEcosystemFeaturesStore.answerAskYourMoneyQuery(q, expenses, subscriptions, savingsGoals)
                        qaHistory = KharcheeEcosystemFeaturesStore.recordAskYourMoneyTurn(context, userId, q, ans)
                        queryInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("ask_your_money_send_btn")
            ) {
                Text("ASK", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        qaHistory.take(4).forEach { turn ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianBlack)
                    .border(0.6.dp, HairlinePlatinum, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Q: ${turn.question}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChampagneGold)
                Text(turn.answer, style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            }
        }
    }
}
