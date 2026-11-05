package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillingFrequency
import com.example.data.model.EmiItem
import com.example.data.model.ExpenseRecord
import com.example.data.model.KharcheeEcosystemFeaturesStore
import com.example.data.model.KharcheeProIntelligenceStore
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.SubscriptionItem
import com.example.data.model.isIncomeTransaction
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
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
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory

@Composable
fun ProSmartSpendingDetectorScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    monthlyBudget: Long,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    val prefsObj = remember(userId, refreshKey) {
        KharcheeProIntelligenceStore.getDetectorPreferences(context, userId)
    }
    val reviewedIds = remember(userId, refreshKey) {
        KharcheeProIntelligenceStore.getReviewedAlertIds(context, userId)
    }
    val dismissedIds = remember(userId, refreshKey) {
        KharcheeProIntelligenceStore.getDismissedAlertIds(context, userId)
    }

    val report = remember(expenses, dailyLimit, monthlyBudget, prefsObj, reviewedIds, dismissedIds) {
        KharcheeProIntelligenceStore.detectSmartSpendingAlerts(
            expenses = expenses,
            dailyLimit = dailyLimit,
            monthlyBudget = monthlyBudget,
            preferences = prefsObj,
            reviewedIds = reviewedIds,
            dismissedIds = dismissedIds
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_smart_spending_detector_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("PRO MODULE 3 · SMART SPENDING DETECTOR", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Anomaly, category spike, duplicate & budget warnings from your real passbook", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }

        // Detector Preferences
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Duplicate Entry Detection", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            Switch(
                checked = prefsObj.enableDuplicateDetection,
                onCheckedChange = { enabled ->
                    KharcheeProIntelligenceStore.saveDetectorPreferences(context, userId, prefsObj.copy(enableDuplicateDetection = enabled))
                    refreshKey++
                },
                colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = ObsidianDarkEmeraldAccent)
            )
        }

        if (!report.hasMinimumData) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Text("INSUFFICIENT BASELINE DATA", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChampagneGold)
                Text(report.minimumDataMessage, style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            }
        } else if (report.alerts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkEmeraldAccent, RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Text("✓ NO UNUSUAL SPENDING ANOMALIES DETECTED", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkEmeraldAccent)
                Text(report.minimumDataMessage, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        } else {
            report.alerts.forEach { alert ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianDarkBackground)
                        .border(0.8.dp, if (alert.isReviewed) ObsidianDarkEmeraldAccent else ChampagneGold, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(alert.whatHappened, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = WarmIvory, modifier = Modifier.weight(1f))
                        Text("₹${alert.amountInvolved}", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = ChampagneGold)
                    }
                    Text("Period: ${alert.relevantDateOrPeriod} · Baseline: ${alert.comparisonBaseline}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver)
                    Text("Why Flagged: ${alert.whyFlagged}", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                    Text("Suggested Action: ${alert.suggestedNextAction}", style = MaterialTheme.typography.bodySmall, color = ObsidianDarkEmeraldAccent)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                KharcheeProIntelligenceStore.markAlertReviewed(context, userId, alert.id)
                                refreshKey++
                                onShowToast("✓ Alert marked as reviewed")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (alert.isReviewed) "✓ REVIEWED" else "MARK REVIEWED", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent)
                        }
                        OutlinedButton(
                            onClick = {
                                KharcheeProIntelligenceStore.dismissAlert(context, userId, alert.id)
                                refreshKey++
                                onShowToast("Dismissed alert")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DISMISS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProMoneyLeakFinderScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    subscriptions: List<SubscriptionItem>,
    reminders: List<PaymentReminderRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshKey by remember { mutableIntStateOf(0) }

    val report = remember(userId, expenses, subscriptions, reminders, refreshKey) {
        KharcheeProIntelligenceStore.analyzeMoneyLeaks(context, userId, expenses, subscriptions, reminders)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_money_leak_finder_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FindInPage, contentDescription = null, tint = ObsidianDarkEmeraldAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("PRO MODULE 4 · MONEY LEAK FINDER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Confirmed subscriptions, user recurring & AI-detected repeated leaks", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }

        // Monthly & Annualised Totals (Active Only)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp)).padding(12.dp)) {
                Text("MONTHLY RECURRING TOTAL", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                Text("₹${report.monthlyRecurringTotal}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = WarmIvory)
                Text("${report.activeItems.size} active · ${report.cancelledItems.size} cancelled", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = ObsidianDarkSilver)
            }
            Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp)).padding(12.dp)) {
                Text("ANNUALISED RECURRING TOTAL", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                Text("₹${report.annualisedRecurringTotal}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = ChampagneGold)
                Text("Confirmed: ${report.confirmedSubscriptionsCount} · AI: ${report.aiPossibleCount}", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = ObsidianDarkEmeraldAccent)
            }
        }

        // Active Recurring & Subscription List
        report.activeItems.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDarkBackground)
                    .border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.merchantName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = WarmIvory)
                        Text("${item.classification.label} · ${item.frequency.label} · Next: ${item.renewalDate}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${item.rawAmount}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = ChampagneGold)
                        Text("₹${item.annualEquivalent}/yr", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
                    }
                }

                // Frequency Selector & User Controls (Confirm / Reject / Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BillingFrequency.entries.forEach { freq ->
                        val sel = item.frequency == freq
                        Surface(
                            color = if (sel) ObsidianDarkSilver.copy(alpha = 0.2f) else ObsidianDarkSecondarySurface,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable {
                                KharcheeProIntelligenceStore.saveLeakUserAction(context, userId, item.key, frequency = freq)
                                refreshKey++
                            }
                        ) {
                            Text(freq.label, fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Surface(
                        color = ObsidianDarkEmeraldAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            KharcheeProIntelligenceStore.saveLeakUserAction(context, userId, item.key, status = "CONFIRMED")
                            refreshKey++
                            onShowToast("✓ Confirmed recurring expense: ${item.merchantName}")
                        }
                    ) {
                        Text("CONFIRM", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = ObsidianDarkEmeraldAccent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Surface(
                        color = CrimsonRuby.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            KharcheeProIntelligenceStore.saveLeakUserAction(context, userId, item.key, status = "CANCELLED")
                            refreshKey++
                            onShowToast("Marked ${item.merchantName} as Cancelled")
                        }
                    ) {
                        Text("MARK CANCELLED", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = CrimsonRuby, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }

        // Small Repeated Expense Analysis
        if (report.smallRepeatedExpenses.isNotEmpty()) {
            Text("SMALL REPEATED EXPENSE ANALYSIS (MICRO-LEAKS ≤ ₹300)", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ChampagneGold)
            report.smallRepeatedExpenses.take(5).forEach { sm ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${sm.merchantOrTitle} (${sm.count}x · avg ₹${sm.averageTicket})", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                    Text("₹${sm.totalSpent}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmIvory)
                }
            }
        }
    }
}

@Composable
fun ProCoreIntelligenceModule5To10Screen(
    moduleId: String,
    userId: String,
    disciplineScore: Int,
    streakDays: Int,
    expenses: List<ExpenseRecord>,
    subscriptions: List<SubscriptionItem>,
    emis: List<EmiItem>,
    savingsGoals: List<SavingsGoalRecord>,
    dailyLimit: Long,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    when (moduleId) {
        "m5_health" -> {
            val totalIn = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
            val totalOut = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
            val savingsRate = if (totalIn > 0L) (((totalIn - totalOut).toDouble() / totalIn.toDouble()) * 100.0).toInt() else 0
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("PRO MODULE 5 · FINANCIAL HEALTH REPORT", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Discipline Score: $disciplineScore / 900 · Streak: ${streakDays}d · Savings Rate: $savingsRate%", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ObsidianDarkEmeraldAccent)
                Text("Observed Income: ₹$totalIn · Observed Expenses: ₹$totalOut · Active Goals: ${savingsGoals.size}", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            }
        }
        "m6_networth" -> {
            val wallets = remember(userId) { com.example.data.model.KharcheeProModules11To20Store.getWallets(context, userId).filter { !it.isArchived } }
            val totalManualAssets = wallets.sumOf { it.openingBalance } + savingsGoals.sumOf { it.savedAmount }
            val totalDebt = emis.sumOf { it.totalPrincipal }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("PRO MODULE 6 · NET WORTH & SOVEREIGN LEDGER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Estimated Net Worth: ₹${totalManualAssets - totalDebt}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = ObsidianDarkEmeraldAccent)
                Text("Assets (Wallets + Dream Vault): ₹$totalManualAssets · Recorded Loan Principal: ₹$totalDebt", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            }
        }
        "m7_studio" -> {
            WhereDidMyMoneyGoCardSection(userId = userId, expenses = expenses, subscriptions = subscriptions, onShowToast = onShowToast)
        }
        "m8_reports" -> {
            val reports = remember(userId) { KharcheeProIntelligenceStore.getGeneratedReports(context, userId) }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("PRO MODULE 8 · EXECUTIVE REPORTS VAULT (${reports.size})", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                if (reports.isEmpty()) {
                    Text("No generated Pro reports yet. Save a forecast or What-If scenario to log reports here.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                } else {
                    reports.forEach { r ->
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp)) {
                            Text("${r.title} [${r.moduleType}]", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                            Text(r.summaryLine, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkEmeraldAccent)
                        }
                    }
                }
            }
        }
        "m9_purchase" -> {
            PurchaseDecisionAssistantCardSection(
                availableMonthlyBudget = dailyLimit * 30L,
                savingsGoals = savingsGoals,
                dailyLimit = dailyLimit
            )
        }
        else -> {
            EmergencyFundMeterCardSection(
                userId = userId,
                stealthMode = false,
                expenses = expenses,
                savingsGoals = savingsGoals,
                onShowToast = onShowToast
            )
        }
    }
}
