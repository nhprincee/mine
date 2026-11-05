package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.JarvisCommandExecutionResult
import com.example.data.model.KharcheeProModules11To20Store
import com.example.data.model.KharcheeProModules21To30Store
import com.example.data.model.LifeEventPlanRecord
import com.example.data.model.LongTermWealthScenarioInput
import com.example.data.model.SavingsGoalRecord
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
import com.example.ui.theme.ObsidianDarkSilver
import com.example.ui.theme.WarmIvory

@Composable
private fun proFieldColors21To30() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ObsidianDarkSilver,
    unfocusedBorderColor = ObsidianDarkBorder,
    focusedContainerColor = ObsidianDarkBackground,
    unfocusedContainerColor = ObsidianDarkBackground,
    focusedTextColor = ObsidianDarkPrimaryText,
    unfocusedTextColor = ObsidianDarkPrimaryText,
    focusedLabelColor = ObsidianDarkSilver,
    unfocusedLabelColor = MutedPlatinum
)

@Composable
fun ProJarvisCommandCenterScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onOpenModule: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var cmdInput by remember { mutableStateOf("") }
    var lastResult by remember { mutableStateOf<JarvisCommandExecutionResult?>(null) }
    var recentCmds by remember(userId) { mutableStateOf(KharcheeProModules21To30Store.getRecentJarvisCommands(context, userId)) }
    val colors = proFieldColors21To30()

    val suggested = listOf(
        "Show my expenses this month",
        "Open my EMI calendar",
        "Compare my spending across two months",
        "Find my largest expense",
        "Create a monthly spending report",
        "Show upcoming bills"
    )

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_21_jarvis_cmd_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 21 · JARVIS COMMAND CENTER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Natural-language command engine — separates Read-Only, Navigation & Confirmation-Gated actions", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            suggested.forEach { s ->
                Surface(
                    color = ObsidianDarkBackground,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(0.6.dp, ObsidianDarkBorder, RoundedCornerShape(50)).clickable {
                        val res = KharcheeProModules21To30Store.executeJarvisCommand(context, userId, s, expenses)
                        lastResult = res
                        recentCmds = KharcheeProModules21To30Store.getRecentJarvisCommands(context, userId)
                        if (res.targetModuleId != null) onOpenModule(res.targetModuleId)
                    }
                ) {
                    Text(s, fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = cmdInput, onValueChange = { cmdInput = it }, label = { Text("Enter Command...") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val res = KharcheeProModules21To30Store.executeJarvisCommand(context, userId, cmdInput, expenses)
                    lastResult = res
                    recentCmds = KharcheeProModules21To30Store.getRecentJarvisCommands(context, userId)
                    if (res.targetModuleId != null) onOpenModule(res.targetModuleId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack)
            ) { Text("RUN", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
        }

        lastResult?.let { res ->
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).border(0.8.dp, ObsidianDarkEmeraldAccent, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Classification: ${res.category}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ChampagneGold)
                Text(res.responseText, style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                res.pendingConfirmationPreview?.let { preview ->
                    Button(
                        onClick = {
                            val ok = KharcheeProModules21To30Store.confirmJarvisCommandAction(context, userId, preview)
                            if (ok) onShowToast("✓ Confirmed & executed action") else onShowToast("Action already executed (duplicate prevented)")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack)
                    ) { Text("CONFIRM & EXECUTE ACTION", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun ProFinancialSafetyMonitorScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val events = remember(userId, expenses, refreshKey) {
        KharcheeProModules21To30Store.buildFinancialSafetyMonitorEvents(context, userId, expenses)
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_22_safety_monitor_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 22 · FINANCIAL SAFETY MONITOR", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Possible duplicate, unusual pattern & confirmed session events — never makes unsupported fraud claims or auto-deletes records.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        events.forEach { ev ->
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${ev.title} [${ev.classification.label}]", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                Text(ev.details, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        KharcheeProModules21To30Store.reviewOrDismissSafetyEvent(context, userId, ev.id, dismiss = false)
                        refreshKey++
                        onShowToast("✓ Marked reviewed")
                    }) { Text(if (ev.isReviewed) "✓ REVIEWED" else "REVIEW", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent) }
                    OutlinedButton(onClick = {
                        KharcheeProModules21To30Store.reviewOrDismissSafetyEvent(context, userId, ev.id, dismiss = true)
                        refreshKey++
                        onShowToast("Dismissed event")
                    }) { Text("DISMISS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby) }
                }
            }
        }
    }
}

@Composable
fun ProSmartDocumentIntelligenceScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var rawText by remember { mutableStateOf("Reliance Digital\n15 Nov 2026\nTotal: ₹4500\nGST: ₹810") }
    var fileName by remember { mutableStateOf("reliance_receipt.jpg") }
    val colors = proFieldColors21To30()

    val draftResult = remember(userId, fileName, rawText) {
        KharcheeProModules21To30Store.analyzeDocumentIntelligence(context, userId, fileName, "image/jpeg", 180_000L, rawText)
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_23_doc_intelligence_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 23 · SMART DOCUMENT INTELLIGENCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Structured extraction review — highlights uncertain fields without guessing and requires confirmation before saving.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        OutlinedTextField(value = fileName, onValueChange = { fileName = it }, label = { Text("Document File Name") }, singleLine = true, colors = colors, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = rawText, onValueChange = { rawText = it }, label = { Text("Extracted / OCR Text to Review") }, colors = colors, modifier = Modifier.fillMaxWidth())

        draftResult.onSuccess { draft ->
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Merchant: ${draft.extractedMerchant ?: "UNREADABLE"} · Date: ${draft.extractedDate ?: "UNREADABLE"}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                Text("Amount: ${draft.extractedAmount?.let { "₹$it" } ?: "UNREADABLE"} · Tax: ${draft.extractedTaxAmount?.let { "₹$it" } ?: "N/A"}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkEmeraldAccent)
                if (draft.uncertainFields.isNotEmpty()) {
                    Text("Uncertain Fields Highlighted: ${draft.uncertainFields.joinToString()}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby)
                }
                Button(
                    onClick = {
                        KharcheeProModules11To20Store.uploadDocumentMetadata(
                            context, userId, draft.fileName, draft.mimeType, draft.fileSizeBytes, draft.category, listOf("ocr_reviewed"), "", "Verified Amount: ${draft.extractedAmount ?: 0L}"
                        )
                        onShowToast("✓ Confirmed & saved reviewed document")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack)
                ) { Text("CONFIRM & STORE DOCUMENT", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun ProLongTermWealthPlannerScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var years by remember { mutableIntStateOf(5) }
    var startSavings by remember { mutableStateOf("50000") }
    var monthlyContrib by remember { mutableStateOf("10000") }
    var returnPct by remember { mutableStateOf("10.0") }
    var inflationPct by remember { mutableStateOf("6.0") }
    var targetAmt by remember { mutableStateOf("1000000") }
    val colors = proFieldColors21To30()

    val scenario = LongTermWealthScenarioInput(
        durationYears = years,
        startingSavings = startSavings.toLongOrNull() ?: 0L,
        monthlyContribution = monthlyContrib.toLongOrNull() ?: 0L,
        assumedAnnualReturnPct = returnPct.toDoubleOrNull() ?: 0.0,
        annualInflationPct = inflationPct.toDoubleOrNull() ?: 0.0,
        targetAmount = targetAmt.toLongOrNull() ?: 0L
    )
    val result = remember(scenario) { KharcheeProModules21To30Store.calculateLongTermWealthPlan(scenario) }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_24_wealth_planner_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 24 · LONG-TERM WEALTH PLANNER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 3, 5, 10, 15).forEach { y ->
                val sel = years == y
                Surface(
                    color = if (sel) ObsidianDarkEmeraldAccent.copy(alpha = 0.2f) else ObsidianDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { years = y }
                ) {
                    Text("${y}Y", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = if (sel) ObsidianDarkEmeraldAccent else MutedPlatinum, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = startSavings, onValueChange = { startSavings = it.filter { c -> c.isDigit() } }, label = { Text("Starting ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = monthlyContrib, onValueChange = { monthlyContrib = it.filter { c -> c.isDigit() } }, label = { Text("Monthly ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = returnPct, onValueChange = { returnPct = it }, label = { Text("Assumed %") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
        }

        if (result.isValid) {
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("1. No-Growth Scenario (0%): ₹${result.noGrowthFutureValue}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                Text("2. Selected Assumption (${scenario.assumedAnnualReturnPct}%): ₹${result.projectedFutureValue} (Growth: +₹${result.assumedGrowthAmount})", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ObsidianDarkEmeraldAccent)
                Text("3. Inflation-Adjusted Illustrative Value: ₹${result.inflationAdjustedValue}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkSilver)
                Text("4. Alternative (+20% Monthly Contribution): ₹${result.alternativeHigherContribFutureValue} · Target Gap: ₹${result.targetGap}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                Text(result.hypotheticalDisclaimer, style = MaterialTheme.typography.bodySmall, color = MutedPlatinum, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ProRemainingModules25To30Screen(
    moduleId: String,
    userId: String,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    dailyLimit: Long,
    onDeleteAccountRequested: (() -> Unit)?,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val colors = proFieldColors21To30()

    when (moduleId) {
        "m25_tax" -> {
            var startYear by remember { mutableIntStateOf(2026) }
            var dedTitle by remember { mutableStateOf("Health Insurance Premium") }
            var dedAmt by remember { mutableStateOf("25000") }
            val summary = remember(userId, startYear, expenses, refreshKey) {
                KharcheeProModules21To30Store.buildTaxAndDeductionSummary(context, userId, startYear, expenses)
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_25_tax_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 25 · TAX & DEDUCTION ORGANIZER (${summary.financialYear})", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Recorded Income: ₹${summary.recordedIncomeTotal} · User-Labelled Deductions: ₹${summary.userLabelledDeductionTotal} · Tax Docs: ${summary.uploadedTaxDocumentsCount}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ObsidianDarkEmeraldAccent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dedTitle, onValueChange = { dedTitle = it }, label = { Text("User-Labelled Entry") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = dedAmt, onValueChange = { dedAmt = it.filter { c -> c.isDigit() } }, label = { Text("₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.7f))
                    Button(onClick = {
                        KharcheeProModules21To30Store.addUserLabelledDeduction(context, userId, summary.financialYear, dedTitle, "User-Tagged", dedAmt.toLongOrNull() ?: 0L)
                        refreshKey++
                        onShowToast("Added user-labelled deduction record")
                    }, colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack)) { Text("ADD", fontSize = 10.sp) }
                }
                summary.missingDocumentChecklist.forEach { msg ->
                    Text("⚠ $msg", style = MaterialTheme.typography.bodySmall, color = ChampagneGold)
                }
                Text(summary.legalDisclaimer, style = MaterialTheme.typography.bodySmall, color = MutedPlatinum, fontSize = 10.sp)
            }
        }
        "m26_milestones" -> {
            val milestones = remember(userId, expenses, savingsGoals) {
                KharcheeProModules21To30Store.evaluateWealthMilestones(context, userId, expenses, savingsGoals)
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_26_milestones_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 26 · WEALTH MILESTONE TRACKER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Evaluated strictly on actual recorded data (never simulated balances). Share cards protect private balances by default.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                milestones.forEach { ms ->
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ms.title, fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                            Text(if (ms.isAchieved) "✓ ACHIEVED (${ms.completionDate})" else "In Progress · Target: ${ms.targetDate}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = if (ms.isAchieved) ObsidianDarkEmeraldAccent else ChampagneGold)
                        }
                        OutlinedButton(onClick = {
                            val txt = KharcheeProModules21To30Store.buildPrivacySafeMilestoneShareText(ms, exposeExactBalances = false)
                            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, txt) }
                            context.startActivity(Intent.createChooser(intent, "Share Milestone"))
                        }) { Text("SHARE SAFE", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver) }
                    }
                }
            }
        }
        "m27_lifeevents" -> {
            var events by remember(userId) { mutableStateOf(KharcheeProModules21To30Store.getLifeEvents(context, userId)) }
            var evName by remember { mutableStateOf("Higher Education / Relocation") }
            var evCost by remember { mutableStateOf("300000") }
            var evSaved by remember { mutableStateOf("50000") }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_27_lifeevents_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 27 · LIFE EVENT PLANNER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Planned life-event budgets remain separate from actual passbook expenses.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = evName, onValueChange = { evName = it }, label = { Text("Event") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = evCost, onValueChange = { evCost = it.filter { c -> c.isDigit() } }, label = { Text("Est. Cost ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
                    Button(onClick = {
                        events = KharcheeProModules21To30Store.saveLifeEventPlan(
                            context, userId,
                            LifeEventPlanRecord(eventName = evName, category = "Education", targetDate = "Dec 2027", monthsUntilEvent = 12, estimatedCost = evCost.toLongOrNull() ?: 100000L, currentSavingsAllocated = evSaved.toLongOrNull() ?: 0L, monthlySavingsContribution = 15000L)
                        )
                        onShowToast("Saved Life Event Plan")
                    }, colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack)) { Text("PLAN", fontSize = 10.sp) }
                }
                events.forEach { e ->
                    Text("• ${e.eventName}: Est. ₹${e.estimatedCost} | Target Gap: ₹${e.targetGap} | Needed: ₹${e.requiredMonthlyToMeetTarget}/mo", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                }
            }
        }
        "m28_coach" -> {
            val tasks = remember(userId, expenses, savingsGoals, dailyLimit, refreshKey) {
                KharcheeProModules21To30Store.buildPersonalisedCoachTasks(context, userId, expenses, savingsGoals, dailyLimit)
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_28_coach_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 28 · PERSONALISED MONEY COACH", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                tasks.forEach { t ->
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${t.title} [${t.status}] ${if (t.isEstimate) "(ESTIMATE)" else "(OBSERVED)"}", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                        Text("Observed: ${t.whatWasObserved}", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                        Text("Why: ${t.whyGenerated}", style = MaterialTheme.typography.bodySmall, color = ObsidianDarkSilver)
                        Text("Next Step: ${t.whatUserCanDo}", style = MaterialTheme.typography.bodySmall, color = ObsidianDarkEmeraldAccent)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                KharcheeProModules21To30Store.updateCoachTaskState(context, userId, t.id, "COMPLETED")
                                refreshKey++
                                onShowToast("✓ Task completed")
                            }) { Text("COMPLETE", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent) }
                            OutlinedButton(onClick = {
                                KharcheeProModules21To30Store.updateCoachTaskState(context, userId, t.id, "DISMISSED")
                                refreshKey++
                                onShowToast("Dismissed suggestion")
                            }) { Text("DISMISS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby) }
                        }
                    }
                }
            }
        }
        "m29_privacy" -> {
            var secState by remember(userId, refreshKey) { mutableStateOf(KharcheeProModules21To30Store.getPrivacySecurityCenterState(context, userId)) }
            var confirmDeleteStep by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_29_privacy_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 29 · PRIVACY & SECURITY CENTER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                secState.securityEventsLog.forEach { log ->
                    Text("✓ $log", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkEmeraldAccent)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mask Balances in Shareable Cards by Default", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                    Switch(
                        checked = secState.maskBalancesInShareCards,
                        onCheckedChange = { v ->
                            KharcheeProModules21To30Store.updatePrivacySecurityConfig(context, userId, userId, secState.vaultPinEnabled, v, secState.biometricLockRequested)
                                .onSuccess { secState = it }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = ObsidianDarkEmeraldAccent)
                    )
                }
                if (!confirmDeleteStep) {
                    OutlinedButton(onClick = { confirmDeleteStep = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("REQUEST ACCOUNT DATA DELETION WORKFLOW", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { confirmDeleteStep = false; onDeleteAccountRequested?.invoke() }, colors = ButtonDefaults.buttonColors(containerColor = CrimsonRuby, contentColor = WarmIvory)) {
                            Text("CONFIRM PERMANENT DELETION", fontSize = 9.sp)
                        }
                        OutlinedButton(onClick = { confirmDeleteStep = false }) { Text("CANCEL", fontSize = 9.sp, color = WarmIvory) }
                    }
                }
            }
        }
        else -> {
            var widgets by remember(userId) { mutableStateOf(KharcheeProModules21To30Store.getWorkspaceStudioLayout(context, userId)) }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_30_workspace_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("PRO MODULE 30 · PRO WORKSPACE STUDIO", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                    OutlinedButton(onClick = {
                        widgets = KharcheeProModules21To30Store.resetWorkspaceStudioLayout(context, userId)
                        onShowToast("↺ Reset Pro Workspace to Default Layout")
                    }) { Text("RESET DEFAULT", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ChampagneGold) }
                }
                widgets.forEachIndexed { idx, w ->
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${idx + 1}. ${w.title} [${w.sizeMode}]", color = WarmIvory, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = {
                                if (idx > 0) {
                                    val mutable = widgets.toMutableList()
                                    val item = mutable.removeAt(idx)
                                    mutable.add(idx - 1, item)
                                    widgets = KharcheeProModules21To30Store.saveWorkspaceStudioLayout(context, userId, mutable)
                                }
                            }) { Text("▲", fontSize = 9.sp, color = ObsidianDarkSilver) }
                            Switch(
                                checked = w.enabled,
                                onCheckedChange = { en ->
                                    val updated = widgets.map { if (it.widgetId == w.widgetId) it.copy(enabled = en) else it }
                                    widgets = KharcheeProModules21To30Store.saveWorkspaceStudioLayout(context, userId, updated)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = ObsidianDarkEmeraldAccent)
                            )
                        }
                    }
                }
            }
        }
    }
}
