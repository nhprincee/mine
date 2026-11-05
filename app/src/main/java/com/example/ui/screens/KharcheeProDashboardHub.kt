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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseRecord
import com.example.data.model.ForecastPeriodOption
import com.example.data.model.KharcheeProIntelligenceStore
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianDarkBackground
import com.example.ui.theme.ObsidianDarkBorder
import com.example.ui.theme.ObsidianDarkEmeraldAccent
import com.example.ui.theme.ObsidianDarkPrimarySurface
import com.example.ui.theme.ObsidianDarkPrimaryText
import com.example.ui.theme.ObsidianDarkSecondarySurface
import com.example.ui.theme.ObsidianDarkSilver
import com.example.ui.theme.WarmIvory

private val PRO_MODULES_1_TO_10 = listOf(
    "m1_predictor" to "01 · AI Money Predictor",
    "m2_whatif" to "02 · What-If Simulator",
    "m3_detector" to "03 · Smart Spending Detector",
    "m4_leaks" to "04 · Money Leak Finder",
    "m5_health" to "05 · Financial Health Report",
    "m6_networth" to "06 · Net-Worth Ledger",
    "m7_studio" to "07 · Analytics Studio",
    "m8_reports" to "08 · Generated Reports",
    "m9_purchase" to "09 · Purchase Assistant",
    "m10_emergency" to "10 · Emergency Fund Meter"
)

private val PRO_MODULES_11_TO_20 = listOf(
    "m11_wallets" to "11 · Multi-Wallet Intelligence",
    "m12_loans" to "12 · Loan & EMI Intelligence",
    "m13_bills" to "13 · Smart Bill Manager",
    "m14_goals" to "14 · Goal Intelligence Center",
    "m15_calendar" to "15 · Money Calendar Pro",
    "m16_rules" to "16 · Smart Money Rules",
    "m17_docs" to "17 · Financial Document Vault",
    "m18_split" to "18 · Advanced Split Manager",
    "m19_fx" to "19 · Multi-Currency Intelligence",
    "m20_notifs" to "20 · Pro Notification Center"
)

private val PRO_MODULES_21_TO_30 = listOf(
    "m21_jarvis" to "21 · JARVIS Command Center",
    "m22_safety" to "22 · Financial Safety Monitor",
    "m23_doc_ai" to "23 · Smart Document Intelligence",
    "m24_wealth" to "24 · Long-Term Wealth Planner",
    "m25_tax" to "25 · Tax & Deduction Organizer",
    "m26_milestones" to "26 · Wealth Milestone Tracker",
    "m27_lifeevents" to "27 · Life Event Planner",
    "m28_coach" to "28 · Personalised Money Coach",
    "m29_privacy" to "29 · Privacy & Security Center",
    "m30_workspace" to "30 · Pro Workspace Studio"
)

private val PRO_MODULES_31_TO_40 = listOf(
    "m31_voice" to "31 · JARVIS Voice Studio",
    "m32_household" to "32 · Household Finance Hub",
    "m33_reports" to "33 · Report Designer Pro",
    "m34_allocator" to "34 · Intelligent Budget Allocator",
    "m35_habits" to "35 · Money Habit Lab",
    "m36_benchmark" to "36 · Financial Benchmark Studio",
    "m37_liquidity" to "37 · Liquidity Intelligence",
    "m38_reconcile" to "38 · Smart Reconciliation Center",
    "m39_membership" to "39 · Pro Benefits & Membership",
    "m40_mission" to "40 · Mission Control"
)

private val PRO_MODULES_41_TO_50 = listOf(
    "m41_student" to "41 · Student Finance Mode",
    "m42_sideincome" to "42 · Side-Income Tracker",
    "m43_invoice" to "43 · Invoice & Receipt Generator",
    "m44_warranty" to "44 · Return & Warranty Tracker",
    "m45_watchlist" to "45 · Price Watchlist",
    "m46_travel" to "46 · Travel Budget Planner",
    "m47_event" to "47 · Event Budget Planner",
    "m48_academy" to "48 · Financial Learning Academy",
    "m49_matcher" to "49 · Document Matcher",
    "m50_api" to "50 · Personal Finance Data API"
)

@Composable
fun KharcheePro30ModuleHubSection(
    userId: String,
    userDisplayName: String,
    isProUnlocked: Boolean,
    disciplineScore: Int,
    streakDays: Int,
    dailyLimit: Long,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    paymentReminders: List<PaymentReminderRecord>,
    proExpiryMillis: Long = 0L,
    onDeleteAccountRequested: (() -> Unit)? = null,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var activeModuleId by remember { mutableStateOf("m1_predictor") }
    var reportsRefresh by remember { mutableIntStateOf(0) }

    val subscriptions = remember(userId) { KharcheeProfileAndToolsStore.getSubscriptions(context, userId) }
    val emis = remember(userId) { KharcheeProfileAndToolsStore.getEmis(context, userId) }
    val monthlyBudget = dailyLimit * 30L

    val quickForecast = remember(expenses, subscriptions, emis, paymentReminders, monthlyBudget, dailyLimit) {
        KharcheeProIntelligenceStore.predictMoneyForecast(
            period = ForecastPeriodOption.NEXT_30_DAYS,
            expenses = expenses,
            subscriptions = subscriptions,
            emis = emis,
            reminders = paymentReminders,
            monthlyBudget = monthlyBudget,
            dailyLimit = dailyLimit
        )
    }
    val recentReports = remember(userId, reportsRefresh) {
        KharcheeProIntelligenceStore.getGeneratedReports(context, userId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kharchee_pro_30_module_hub"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pro Dashboard Intelligence Summary Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(ObsidianDarkPrimarySurface)
                .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "KHARCHEE PRO · 50-MODULE INTELLIGENCE SUITE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianDarkSilver
                    )
                    Text(
                        text = if (isProUnlocked) "PRO Entitlement Verified · All 50 Modules Unlocked" else "Standard Tier · Interactive Pro Intelligence Workspace",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = WarmIvory
                    )
                }
            }

            // Quick Shortcuts: Cash-Flow Forecast, Health Report, Analytics Studio, Mission Control
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).border(0.6.dp, ObsidianDarkBorder, RoundedCornerShape(12.dp)).clickable { activeModuleId = "m1_predictor" }.padding(10.dp)
                ) {
                    Text("30D CASH-FLOW EST.", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                    Text("₹${quickForecast.predictedTotalExpenses}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = ObsidianDarkSilver)
                    Text(quickForecast.confidenceLevel, fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = ObsidianDarkEmeraldAccent)
                }
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).border(0.6.dp, ObsidianDarkBorder, RoundedCornerShape(12.dp)).clickable { activeModuleId = "m5_health" }.padding(10.dp)
                ) {
                    Text("HEALTH REPORT", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                    Text("$disciplineScore / 900", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = ObsidianDarkEmeraldAccent)
                    Text("Tap to open", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedIvory)
                }
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).border(0.6.dp, ObsidianDarkBorder, RoundedCornerShape(12.dp)).clickable { activeModuleId = "m40_mission" }.padding(10.dp)
                ) {
                    Text("MISSION CONTROL", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                    Text("${recentReports.size} Reports", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = ChampagneGold)
                    Text("Tap to open", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedIvory)
                }
            }

            // 5 Horizontal Module Selector Rails (1–10, 11–20, 21–30, 31–40, 41–50)
            listOf(
                "MODULES 01–10 · FORECASTING & LEAKS" to PRO_MODULES_1_TO_10,
                "MODULES 11–20 · WALLETS, LOANS, BILLS & FX" to PRO_MODULES_11_TO_20,
                "MODULES 21–30 · JARVIS CMD, WEALTH, TAX & STUDIO" to PRO_MODULES_21_TO_30,
                "MODULES 31–40 · VOICE STUDIO, HOUSEHOLD, LIQUIDITY & MISSION CONTROL" to PRO_MODULES_31_TO_40,
                "MODULES 41–50 · STUDENT, SIDE-INCOME, INVOICES, TRAVEL, ACADEMY & API" to PRO_MODULES_41_TO_50
            ).forEach { (groupTitle, items) ->
                Text(groupTitle, fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = MutedPlatinum)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items.forEach { (id, label) ->
                        val selected = activeModuleId == id
                        Surface(
                            color = if (selected) ObsidianDarkEmeraldAccent.copy(alpha = 0.2f) else ObsidianDarkSecondarySurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .border(0.8.dp, if (selected) ObsidianDarkEmeraldAccent else ObsidianDarkBorder, RoundedCornerShape(10.dp))
                                .clickable { activeModuleId = id }
                                .testTag("pro_module_chip_$id")
                        ) {
                            Text(
                                text = label,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) ObsidianDarkPrimaryText else ObsidianDarkSilver,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Module Screen Dispatcher (All 40 Working Pro Modules)
        when (activeModuleId) {
            "m1_predictor" -> ProAiMoneyPredictorScreen(
                userId = userId,
                expenses = expenses,
                subscriptions = subscriptions,
                emis = emis,
                reminders = paymentReminders,
                monthlyBudget = monthlyBudget,
                dailyLimit = dailyLimit,
                onReportGenerated = { reportsRefresh++ },
                onShowToast = onShowToast
            )
            "m2_whatif" -> ProWhatIfSimulatorScreen(
                userId = userId,
                expenses = expenses,
                subscriptions = subscriptions,
                emis = emis,
                savingsGoals = savingsGoals,
                onReportGenerated = { reportsRefresh++ },
                onShowToast = onShowToast
            )
            "m3_detector" -> ProSmartSpendingDetectorScreen(
                userId = userId,
                expenses = expenses,
                dailyLimit = dailyLimit,
                monthlyBudget = monthlyBudget,
                onShowToast = onShowToast
            )
            "m4_leaks" -> ProMoneyLeakFinderScreen(
                userId = userId,
                expenses = expenses,
                subscriptions = subscriptions,
                reminders = paymentReminders,
                onShowToast = onShowToast
            )
            "m5_health", "m6_networth", "m7_studio", "m8_reports", "m9_purchase", "m10_emergency" -> ProCoreIntelligenceModule5To10Screen(
                moduleId = activeModuleId,
                userId = userId,
                disciplineScore = disciplineScore,
                streakDays = streakDays,
                expenses = expenses,
                subscriptions = subscriptions,
                emis = emis,
                savingsGoals = savingsGoals,
                dailyLimit = dailyLimit,
                onShowToast = onShowToast
            )
            "m11_wallets" -> ProMultiWalletIntelligenceScreen(userId = userId, expenses = expenses, onShowToast = onShowToast)
            "m12_loans" -> ProLoanEmiIntelligenceScreen(userId = userId, onShowToast = onShowToast)
            "m13_bills" -> ProSmartBillManagerScreen(userId = userId, onShowToast = onShowToast)
            "m14_goals" -> ProGoalIntelligenceCenterScreen(userId = userId, onShowToast = onShowToast)
            "m15_calendar" -> ProMoneyCalendarScreen(userId = userId, expenses = expenses)
            "m16_rules" -> ProSmartMoneyRulesScreen(userId = userId, expenses = expenses, onShowToast = onShowToast)
            "m17_docs" -> ProFinancialDocumentVaultScreen(userId = userId, expenses = expenses, onShowToast = onShowToast)
            "m18_split" -> ProAdvancedSplitExpenseManagerScreen(userDisplayName = userDisplayName, onShowToast = onShowToast)
            "m19_fx" -> ProMultiCurrencyIntelligenceScreen(userId = userId, onShowToast = onShowToast)
            "m20_notifs" -> ProNotificationIntelligenceScreen(
                userId = userId,
                expenses = expenses,
                dailyLimit = dailyLimit,
                monthlyBudget = monthlyBudget,
                onOpenModule = { activeModuleId = it },
                onShowToast = onShowToast
            )
            "m21_jarvis" -> ProJarvisCommandCenterScreen(
                userId = userId,
                expenses = expenses,
                onOpenModule = { activeModuleId = it },
                onShowToast = onShowToast
            )
            "m22_safety" -> ProFinancialSafetyMonitorScreen(userId = userId, expenses = expenses, onShowToast = onShowToast)
            "m23_doc_ai" -> ProSmartDocumentIntelligenceScreen(userId = userId, onShowToast = onShowToast)
            "m24_wealth" -> ProLongTermWealthPlannerScreen(userId = userId, onShowToast = onShowToast)
            "m25_tax", "m26_milestones", "m27_lifeevents", "m28_coach", "m29_privacy", "m30_workspace" -> ProRemainingModules25To30Screen(
                moduleId = activeModuleId,
                userId = userId,
                expenses = expenses,
                savingsGoals = savingsGoals,
                dailyLimit = dailyLimit,
                onDeleteAccountRequested = onDeleteAccountRequested,
                onShowToast = onShowToast
            )
            "m31_voice" -> ProJarvisVoiceStudioScreen(
                userId = userId,
                expenses = expenses,
                onShowToast = onShowToast
            )
            "m41_student", "m42_sideincome", "m43_invoice", "m44_warranty", "m45_watchlist",
            "m46_travel", "m47_event", "m48_academy", "m49_matcher", "m50_api" -> ProModules41To50DispatcherScreen(
                moduleId = activeModuleId,
                userId = userId,
                userDisplayName = userDisplayName,
                expenses = expenses,
                savingsGoals = savingsGoals,
                monthlyBudget = monthlyBudget,
                onShowToast = onShowToast
            )
            else -> ProModules32To40DispatcherScreen(
                moduleId = activeModuleId,
                userId = userId,
                userDisplayName = userDisplayName,
                settings = com.example.data.model.UserSettings(
                    userId = userId,
                    isPro = isProUnlocked,
                    proExpiryMillis = proExpiryMillis,
                    dailyLimit = dailyLimit
                ),
                expenses = expenses,
                savingsGoals = savingsGoals,
                dailyLimit = dailyLimit,
                onOpenModule = { activeModuleId = it },
                onShowToast = onShowToast
            )
        }
    }
}

