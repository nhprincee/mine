package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmiItem
import com.example.data.model.ExpenseRecord
import com.example.data.model.ForecastPeriodOption
import com.example.data.model.KharcheeProIntelligenceStore
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.SubscriptionItem
import com.example.data.model.WhatIfScenarioInput
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
import com.example.ui.theme.ObsidianDarkBackground
import com.example.ui.theme.ObsidianDarkBorder
import com.example.ui.theme.ObsidianDarkEmeraldAccent
import com.example.ui.theme.ObsidianDarkPrimarySurface
import com.example.ui.theme.ObsidianDarkPrimaryText
import com.example.ui.theme.ObsidianDarkSecondarySurface
import com.example.ui.theme.ObsidianDarkSilver
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToLong

@Composable
fun ProAiMoneyPredictorScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    subscriptions: List<SubscriptionItem>,
    emis: List<EmiItem>,
    reminders: List<PaymentReminderRecord>,
    monthlyBudget: Long,
    dailyLimit: Long,
    onReportGenerated: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var selectedPeriod by remember(userId) {
        mutableStateOf(KharcheeProIntelligenceStore.getSelectedForecastPeriod(context, userId))
    }

    val forecast = remember(selectedPeriod, expenses, subscriptions, emis, reminders, monthlyBudget, dailyLimit) {
        KharcheeProIntelligenceStore.predictMoneyForecast(
            period = selectedPeriod,
            expenses = expenses,
            subscriptions = subscriptions,
            emis = emis,
            reminders = reminders,
            monthlyBudget = monthlyBudget,
            dailyLimit = dailyLimit
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_ai_money_predictor_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ObsidianDarkEmeraldAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PRO MODULE 1 · AI MONEY PREDICTOR",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianDarkSilver
                    )
                    Text(
                        text = "Historical cash-flow forecasting · Confirmed recurring vs variable spend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedIvory
                    )
                }
            }
            Surface(
                color = if (forecast.hasSufficientData) ObsidianDarkEmeraldAccent.copy(alpha = 0.16f) else CrimsonRuby.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.border(
                    0.8.dp,
                    if (forecast.hasSufficientData) ObsidianDarkEmeraldAccent else CrimsonRuby,
                    RoundedCornerShape(50)
                )
            ) {
                Text(
                    text = "${forecast.confidenceLevel} (${forecast.confidenceScorePct}%)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (forecast.hasSufficientData) ObsidianDarkEmeraldAccent else CrimsonRuby,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Forecast Period Selector: Next 7 days, Next 30 days, Next 3 months, Next 6 months
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ForecastPeriodOption.entries.forEach { option ->
                val isSelected = selectedPeriod == option
                Surface(
                    color = if (isSelected) ObsidianDarkSilver.copy(alpha = 0.2f) else ObsidianDarkBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .border(
                            0.8.dp,
                            if (isSelected) ObsidianDarkSilver else ObsidianDarkBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedPeriod = option
                            KharcheeProIntelligenceStore.setSelectedForecastPeriod(context, userId, option)
                        }
                        .testTag("forecast_period_${option.id}")
                ) {
                    Text(
                        text = option.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) ObsidianDarkPrimaryText else MutedPlatinum,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        if (!forecast.hasSufficientData) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDarkBackground)
                    .border(1.dp, CrimsonRuby.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("INSUFFICIENT HISTORICAL DATA — NO FABRICATED FORECAST", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonRuby)
                Text(forecast.insufficientDataReason, style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                Text("Required History: ${forecast.requiredAdditionalHistoryMessage}", style = MaterialTheme.typography.bodySmall, color = MutedPlatinum)
            }
        } else {
            // 4 Core Forecast Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp)).padding(12.dp)) {
                    Text("PREDICTED EXPENSES", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                    Text("₹${forecast.predictedTotalExpenses}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = WarmIvory)
                    Text("Fixed: ₹${forecast.confirmedRecurringExpenses} + Var: ₹${forecast.estimatedVariableExpenses}", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = ObsidianDarkSilver)
                }
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp)).padding(12.dp)) {
                    Text("PREDICTED INCOME", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                    Text("₹${forecast.predictedIncome}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = ObsidianDarkEmeraldAccent)
                    Text("Period: ${selectedPeriod.label}", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedIvory)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp)).padding(12.dp)) {
                    Text("ESTIMATED SAVINGS", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                    Text(
                        text = "₹${forecast.estimatedSavings}",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = if (forecast.estimatedSavings >= 0L) ObsidianDarkEmeraldAccent else CrimsonRuby
                    )
                    Text("Income minus predicted outflow", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedIvory)
                }
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp)).padding(12.dp)) {
                    Text("EST. PERIOD-END BALANCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                    Text("₹${forecast.estimatedPeriodEndBalance}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = ObsidianDarkSilver)
                    Text("Current Net: ₹${forecast.currentNetBalance}", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedIvory)
                }
            }

            // Spending Trend Chart
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDarkBackground)
                    .border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("SPENDING TREND & FORECAST TRAJECTORY", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                val maxBar = max(1L, forecast.trendPoints.maxOfOrNull { max(it.historicalAmount, it.projectedAmount) } ?: 1L)
                forecast.trendPoints.forEach { pt ->
                    val ratio = (pt.projectedAmount.toFloat() / maxBar.toFloat()).coerceIn(0.05f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(pt.label, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                            Text("₹${pt.projectedAmount}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkPrimaryText)
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            color = ObsidianDarkEmeraldAccent,
                            trackColor = ObsidianDarkSecondarySurface,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
                        )
                    }
                }
            }

            // Category-Wise Predicted Expenses (reconciles to predictedTotalExpenses)
            if (forecast.categoryBreakdowns.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianDarkBackground)
                        .border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("CATEGORY-WISE PREDICTED EXPENSES (RECONCILED)", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                    forecast.categoryBreakdowns.forEach { cat ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cat.category, style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                            Text("₹${cat.totalPredictedAmount}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                        }
                    }
                }
            }
        }

        // Assumptions, Data Period, Limitations & Estimate Disclaimer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianDarkSecondarySurface)
                .border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("ESTIMATE DISCLOSURE & ASSUMPTIONS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ChampagneGold)
            Text("Data Period Used: ${forecast.dataPeriodUsedLabel}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver)
            Text(forecast.explanationSummary, style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            forecast.calculationAssumptions.forEach { a ->
                Text("• $a", style = MaterialTheme.typography.bodySmall, color = MutedIvory, fontSize = 11.sp)
            }
            Text("Guidance: ${forecast.requiredAdditionalHistoryMessage}", style = MaterialTheme.typography.bodySmall, color = ObsidianDarkEmeraldAccent, fontSize = 11.sp)
            forecast.limitations.forEach { lim ->
                Text("⚠ $lim", style = MaterialTheme.typography.bodySmall, color = MutedPlatinum, fontSize = 10.sp)
            }
        }

        Button(
            onClick = {
                KharcheeProIntelligenceStore.recordGeneratedReport(
                    context = context,
                    userId = userId,
                    title = "AI Money Predictor (${selectedPeriod.label})",
                    moduleType = "PREDICTOR",
                    summaryLine = "Est. Expenses: ₹${forecast.predictedTotalExpenses} | Est. Savings: ₹${forecast.estimatedSavings} (${forecast.confidenceLevel})"
                )
                onReportGenerated()
                onShowToast("📊 Saved AI Money Predictor Report to Pro Dashboard")
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SAVE FORECAST TO PRO REPORTS", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun ProWhatIfSimulatorScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    subscriptions: List<SubscriptionItem>,
    emis: List<EmiItem>,
    savingsGoals: List<SavingsGoalRecord>,
    onReportGenerated: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    val baseline = remember(expenses, subscriptions, emis, savingsGoals) {
        KharcheeProIntelligenceStore.buildDefaultScenarioFromActuals(expenses, subscriptions, emis, savingsGoals)
    }

    var savedScenarios by remember(userId) {
        mutableStateOf(KharcheeProIntelligenceStore.getSavedScenarios(context, userId))
    }

    var activeId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var scenarioName by remember { mutableStateOf("Optimized Growth Plan") }
    var monthlyIncomeText by remember(baseline) { mutableStateOf(baseline.monthlyIncome.toString()) }
    var essentialText by remember(baseline) { mutableStateOf(baseline.essentialExpenses.toString()) }
    var lifestyleText by remember(baseline) { mutableStateOf(baseline.lifestyleExpenses.toString()) }
    var savingsText by remember(baseline) { mutableStateOf(baseline.savingsContribution.toString()) }
    var emiText by remember(baseline) { mutableStateOf(baseline.emiObligations.toString()) }
    var subsText by remember(baseline) { mutableStateOf(baseline.subscriptionCosts.toString()) }
    var oneTimeText by remember { mutableStateOf("0") }
    var addIncomeText by remember { mutableStateOf("0") }
    var unexpectedText by remember { mutableStateOf("0") }
    var projectionMonths by remember { mutableStateOf(6) }

    val simulatedScenario = WhatIfScenarioInput(
        id = activeId,
        name = scenarioName,
        monthlyIncome = monthlyIncomeText.toLongOrNull() ?: -1L,
        essentialExpenses = essentialText.toLongOrNull() ?: -1L,
        lifestyleExpenses = lifestyleText.toLongOrNull() ?: -1L,
        savingsContribution = savingsText.toLongOrNull() ?: -1L,
        emiObligations = emiText.toLongOrNull() ?: -1L,
        subscriptionCosts = subsText.toLongOrNull() ?: -1L,
        oneTimePurchases = oneTimeText.toLongOrNull() ?: -1L,
        additionalIncome = addIncomeText.toLongOrNull() ?: -1L,
        unexpectedExpenses = unexpectedText.toLongOrNull() ?: -1L,
        projectionMonths = projectionMonths
    )

    val comparison = remember(baseline, simulatedScenario) {
        KharcheeProIntelligenceStore.evaluateWhatIfScenario(baseline, simulatedScenario)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ObsidianDarkSilver,
        unfocusedBorderColor = ObsidianDarkBorder,
        focusedContainerColor = ObsidianDarkBackground,
        unfocusedContainerColor = ObsidianDarkBackground,
        focusedTextColor = ObsidianDarkPrimaryText,
        unfocusedTextColor = ObsidianDarkPrimaryText,
        focusedLabelColor = ObsidianDarkSilver,
        unfocusedLabelColor = MutedPlatinum
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_what_if_simulator_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Science, contentDescription = null, tint = ObsidianDarkSilver, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("PRO MODULE 2 · WHAT-IF MONEY SIMULATOR", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Hypothetical scenario modeling — never alters actual transactions or wallet balances", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }

        // Scenario Name & Projection Horizon
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = scenarioName,
                onValueChange = { scenarioName = it },
                label = { Text("Scenario Name") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f).testTag("whatif_scenario_name_input")
            )
            listOf(1, 3, 6, 12).forEach { m ->
                val sel = projectionMonths == m
                Surface(
                    color = if (sel) ObsidianDarkEmeraldAccent.copy(alpha = 0.2f) else ObsidianDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(0.8.dp, if (sel) ObsidianDarkEmeraldAccent else ObsidianDarkBorder, RoundedCornerShape(8.dp))
                        .clickable { projectionMonths = m }
                ) {
                    Text("${m}M", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = if (sel) ObsidianDarkEmeraldAccent else MutedPlatinum, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                }
            }
        }

        // 9 Editable Numeric Inputs arranged in 3x3 rows
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = monthlyIncomeText,
                onValueChange = { monthlyIncomeText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Monthly Income") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = essentialText,
                onValueChange = { essentialText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Essential Exp.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = lifestyleText,
                onValueChange = { lifestyleText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Lifestyle Exp.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = savingsText,
                onValueChange = { savingsText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Savings Contrib.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = emiText,
                onValueChange = { emiText = it.filter { ch -> ch.isDigit() } },
                label = { Text("EMI Obligations") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = subsText,
                onValueChange = { subsText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Subscriptions") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = oneTimeText,
                onValueChange = { oneTimeText = it.filter { ch -> ch.isDigit() } },
                label = { Text("One-Time Buy") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = addIncomeText,
                onValueChange = { addIncomeText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Extra Income") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = unexpectedText,
                onValueChange = { unexpectedText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Unexpected Exp.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
        }

        // Interactive Lifestyle & Savings Quick Sliders
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Adjust Lifestyle Spending Slider", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                Text("₹${lifestyleText.ifBlank { "0" }}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkSilver)
            }
            Slider(
                value = (lifestyleText.toLongOrNull() ?: 0L).toFloat().coerceIn(0f, 150000f),
                onValueChange = { lifestyleText = it.roundToLong().toString() },
                valueRange = 0f..150000f,
                colors = SliderDefaults.colors(thumbColor = ObsidianDarkSilver, activeTrackColor = ObsidianDarkSilver)
            )
        }

        // Live Comparison Card: Current vs Simulated
        if (!comparison.isValid) {
            Text(comparison.validationError ?: "Invalid inputs", color = CrimsonRuby, style = MaterialTheme.typography.bodySmall)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianDarkBackground)
                    .border(1.dp, ObsidianDarkEmeraldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("LIVE COMPARISON · ACTUAL BASELINE VS SIMULATED SCENARIO", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkEmeraldAccent)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Metric", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum, modifier = Modifier.weight(1.2f))
                    Text("1. Current", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum, modifier = Modifier.weight(1f))
                    Text("2. Simulated", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = ObsidianDarkBorder, thickness = 0.5.dp)

                listOf(
                    Triple("Total Income", "₹${comparison.currentMonthlyIncome}", "₹${comparison.totalSimulatedIncome}"),
                    Triple("Total Expenses", "₹${comparison.currentMonthlyExpenses}", "₹${comparison.totalSimulatedExpenses} (${if (comparison.expenseChangePercent >= 0) "+" else ""}${comparison.expenseChangePercent}%)"),
                    Triple("Remaining Balance", "₹${comparison.currentRemainingBalance}", "₹${comparison.estimatedRemainingBalance}"),
                    Triple("Est. Monthly Savings", "₹${comparison.currentMonthlySavings}", "₹${comparison.estimatedTotalMonthlySavings} (${if (comparison.savingsChangePercent >= 0) "+" else ""}${comparison.savingsChangePercent}%)"),
                    Triple("${comparison.projectionMonths}M Projected Savings", "₹${comparison.currentProjectedSavingsOverPeriod}", "₹${comparison.simulatedProjectedSavingsOverPeriod}")
                ).forEach { (metric, cur, sim) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(metric, style = MaterialTheme.typography.bodySmall, color = MutedIvory, modifier = Modifier.weight(1.2f))
                        Text(cur, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory, modifier = Modifier.weight(1f))
                        Text(sim, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkEmeraldAccent, modifier = Modifier.weight(1f))
                    }
                }

                Text(
                    text = "Monthly Savings Difference: ${if (comparison.monthlySavingsDifference >= 0L) "+₹${comparison.monthlySavingsDifference}" else "-₹${-comparison.monthlySavingsDifference}"} · ${comparison.projectionMonths}M Impact: ${if (comparison.projectedDifferenceOverPeriod >= 0L) "+₹${comparison.projectedDifferenceOverPeriod}" else "-₹${-comparison.projectedDifferenceOverPeriod}"}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (comparison.monthlySavingsDifference >= 0L) ObsidianDarkEmeraldAccent else CrimsonRuby
                )
            }
        }

        // Save / New Scenario Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (comparison.isValid) {
                        savedScenarios = KharcheeProIntelligenceStore.saveScenario(context, userId, simulatedScenario)
                        KharcheeProIntelligenceStore.recordGeneratedReport(
                            context = context,
                            userId = userId,
                            title = "What-If Scenario: $scenarioName",
                            moduleType = "WHAT_IF",
                            summaryLine = "Sim Savings: ₹${comparison.estimatedTotalMonthlySavings}/mo (${comparison.projectionMonths}M: ₹${comparison.simulatedProjectedSavingsOverPeriod})"
                        )
                        onReportGenerated()
                        onShowToast("💾 Saved Scenario '$scenarioName'")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack),
                modifier = Modifier.weight(1f).testTag("save_whatif_scenario_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE SCENARIO", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            OutlinedButton(
                onClick = {
                    activeId = UUID.randomUUID().toString()
                    scenarioName = "Scenario #${savedScenarios.size + 1}"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("NEW SCENARIO", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkSilver)
            }
        }

        // Saved Scenarios List (Load, Edit, Duplicate, Delete & Compare)
        if (savedScenarios.isNotEmpty()) {
            Text("SAVED HYPOTHETICAL SCENARIOS (${savedScenarios.size})", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
            savedScenarios.forEach { sc ->
                val scEval = KharcheeProIntelligenceStore.evaluateWhatIfScenario(baseline, sc)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianDarkBackground)
                        .border(0.6.dp, ObsidianDarkBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            activeId = sc.id
                            scenarioName = sc.name
                            monthlyIncomeText = sc.monthlyIncome.toString()
                            essentialText = sc.essentialExpenses.toString()
                            lifestyleText = sc.lifestyleExpenses.toString()
                            savingsText = sc.savingsContribution.toString()
                            emiText = sc.emiObligations.toString()
                            subsText = sc.subscriptionCosts.toString()
                            oneTimeText = sc.oneTimePurchases.toString()
                            addIncomeText = sc.additionalIncome.toString()
                            unexpectedText = sc.unexpectedExpenses.toString()
                            projectionMonths = sc.projectionMonths
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sc.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = WarmIvory)
                        Text(
                            text = "Sim Savings: ₹${scEval.estimatedTotalMonthlySavings}/mo · ${sc.projectionMonths}M Total: ₹${scEval.simulatedProjectedSavingsOverPeriod}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = ObsidianDarkEmeraldAccent
                        )
                    }
                    Row {
                        IconButton(onClick = {
                            savedScenarios = KharcheeProIntelligenceStore.duplicateScenario(context, userId, sc.id)
                            onShowToast("📋 Duplicated '${sc.name}'")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Scenario", tint = ObsidianDarkSilver, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = {
                            savedScenarios = KharcheeProIntelligenceStore.deleteScenario(context, userId, sc.id)
                            onShowToast("🗑️ Deleted simulated scenario '${sc.name}'")
                        }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Scenario", tint = CrimsonRuby, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
