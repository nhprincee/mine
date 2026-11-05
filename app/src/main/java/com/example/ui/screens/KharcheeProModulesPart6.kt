package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.data.model.BudgetAllocationPlanInput
import com.example.data.model.ExpenseRecord
import com.example.data.model.HouseholdRole
import com.example.data.model.KharcheeProModules31To40Store
import com.example.data.model.ProReportTemplate
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
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
import java.util.Locale

@Composable
private fun proFieldColors31To40() = OutlinedTextFieldDefaults.colors(
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
fun ProJarvisVoiceStudioScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var transcriptInput by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("en-IN") }
    var micStatus by remember { mutableStateOf("READY (TAP MIC OR TYPE BELOW)") }
    var history by remember(userId) { mutableStateOf(KharcheeProModules31To40Store.getVoiceStudioHistory(context, userId)) }
    val colors = proFieldColors31To40()

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale.forLanguageTag(selectedLang)
                ttsEngine = instance
            }
        }
        onDispose { instance?.shutdown() }
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val matches = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                transcriptInput = spoken
                micStatus = "RECOGNIZED — REVIEW & SUBMIT"
            }
        } else {
            micStatus = "SPEECH CANCELLED — USE TEXT FALLBACK"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            micStatus = "LISTENING..."
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLang)
                }
                speechLauncher.launch(intent)
            } catch (_: Exception) {
                micStatus = "SPEECH SERVICE UNAVAILABLE — USE TEXT FALLBACK"
            }
        } else {
            micStatus = "MIC PERMISSION DENIED — TEXT FALLBACK ACTIVE"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_31_voice_studio_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 31 · JARVIS VOICE STUDIO", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("State: $micStatus · Sensitive actions require explicit confirmation.", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack)
            ) { Text("🎙️ ACTIVATE MIC", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold) }

            listOf("en-IN" to "English", "hi-IN" to "Hindi / Hinglish").forEach { (code, label) ->
                val sel = selectedLang == code
                Surface(
                    color = if (sel) ObsidianDarkEmeraldAccent.copy(alpha = 0.2f) else ObsidianDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { selectedLang = code }
                ) {
                    Text(label, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = if (sel) ObsidianDarkEmeraldAccent else MutedPlatinum, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = transcriptInput,
                onValueChange = { transcriptInput = it },
                label = { Text("Review Recognized Speech or Type Fallback") },
                singleLine = true,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (transcriptInput.isNotBlank()) {
                        val turn = KharcheeProModules31To40Store.processVoiceStudioTranscript(context, userId, transcriptInput, selectedLang, expenses)
                        history = KharcheeProModules31To40Store.getVoiceStudioHistory(context, userId)
                        transcriptInput = ""
                        micStatus = "PROCESSED ✓"
                        ttsEngine?.speak(turn.jarvisReplyText, TextToSpeech.QUEUE_FLUSH, null, turn.id)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack)
            ) { Text("ASK", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }

        history.take(3).forEach { turn ->
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("You (${turn.languageCode}): ${turn.recognizedTranscript}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                Text("JARVIS: ${turn.jarvisReplyText}", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                OutlinedButton(onClick = {
                    ttsEngine?.speak(turn.jarvisReplyText, TextToSpeech.QUEUE_FLUSH, null, turn.id)
                    onShowToast("🔊 Playing spoken response")
                }) { Text("🔊 PLAY VOICE REPLY", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver) }
            }
        }
    }
}

@Composable
fun ProModules32To40DispatcherScreen(
    moduleId: String,
    userId: String,
    userDisplayName: String,
    settings: UserSettings,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    dailyLimit: Long,
    onOpenModule: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val colors = proFieldColors31To40()

    when (moduleId) {
        "m32_household" -> {
            var hhState by remember(userId, refreshKey) { mutableStateOf(KharcheeProModules31To40Store.getHouseholdHub(context, userId, userDisplayName)) }
            var inviteName by remember { mutableStateOf("") }
            var selectedRole by remember { mutableStateOf(HouseholdRole.CONTRIBUTOR) }
            var expTitle by remember { mutableStateOf("") }
            var expAmt by remember { mutableStateOf("") }

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_32_household_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 32 · HOUSEHOLD FINANCE HUB", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("${hhState.householdName} · Budget: ₹${hhState.sharedBudget} · Personal transactions remain private.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = inviteName, onValueChange = { inviteName = it }, label = { Text("Invite Member") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                    Button(onClick = {
                        if (inviteName.isNotBlank()) {
                            val invId = "usr_${inviteName.trim().lowercase()}"
                            KharcheeProModules31To40Store.inviteHouseholdMember(context, userId, userDisplayName, userId, invId, inviteName, selectedRole)
                                .onSuccess { hhState = it; inviteName = ""; onShowToast("Sent invitation (Pending Acceptance)") }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack)) { Text("INVITE", fontSize = 9.sp) }
                }

                hhState.members.forEach { m ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${m.name} (${m.role.id}) · [${m.inviteStatus}]", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                        if (m.inviteStatus == "PENDING_INVITE") {
                            OutlinedButton(onClick = {
                                KharcheeProModules31To40Store.acceptHouseholdInvitation(context, userId, userDisplayName, m.userId)
                                    .onSuccess { hhState = it; onShowToast("✓ Invitation accepted") }
                            }) { Text("ACCEPT", fontSize = 8.sp, color = ObsidianDarkEmeraldAccent) }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = expTitle, onValueChange = { expTitle = it }, label = { Text("Shared Expense") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = expAmt, onValueChange = { expAmt = it.filter { c -> c.isDigit() } }, label = { Text("₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.7f))
                    Button(onClick = {
                        KharcheeProModules31To40Store.addHouseholdSharedExpenseAuthorized(context, userId, userDisplayName, userId, expTitle, expAmt.toLongOrNull() ?: 0L)
                            .onSuccess { hhState = it; expTitle = ""; expAmt = ""; onShowToast("Added household expense") }
                    }, colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack)) { Text("ADD", fontSize = 9.sp) }
                }
            }
        }
        "m33_report_designer" -> {
            var tplName by remember { mutableStateOf("Executive Monthly Audit") }
            var templates by remember(userId) { mutableStateOf(KharcheeProModules31To40Store.getReportTemplates(context, userId)) }
            val activeTpl = templates.firstOrNull() ?: ProReportTemplate(name = tplName, startEpochMillis = 0L, endEpochMillis = Long.MAX_VALUE)
            val generated = remember(activeTpl, expenses) {
                KharcheeProModules31To40Store.generateDynamicReportFromTemplate(activeTpl, expenses)
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_33_report_designer_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 33 · REPORT DESIGNER PRO", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Live Preview (${generated.templateName}): ${generated.matchingTransactionCount} records · Income ₹${generated.totalIncome} · Expense ₹${generated.totalExpense} · Net ₹${generated.netCashFlow}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkEmeraldAccent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = tplName, onValueChange = { tplName = it }, label = { Text("Template Name") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                    Button(onClick = {
                        templates = KharcheeProModules31To40Store.saveReportTemplate(context, userId, ProReportTemplate(name = tplName, startEpochMillis = 0L, endEpochMillis = Long.MAX_VALUE))
                        onShowToast("Saved Report Template")
                    }, colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack)) { Text("SAVE TEMPLATE", fontSize = 9.sp) }
                }
            }
        }
        "m34_allocator" -> {
            var incText by remember { mutableStateOf("80000") }
            var essText by remember { mutableStateOf("25000") }
            var emiText by remember { mutableStateOf("15000") }
            var savText by remember { mutableStateOf("20000") }
            val eval = remember(incText, essText, emiText, savText) {
                KharcheeProModules31To40Store.evaluateBudgetAllocation(
                    BudgetAllocationPlanInput(
                        monthlyIncome = incText.toLongOrNull() ?: 0L,
                        essentialExpenses = essText.toLongOrNull() ?: 0L,
                        emiObligations = emiText.toLongOrNull() ?: 0L,
                        bills = 5000L,
                        subscriptions = 1500L,
                        savingsGoals = savText.toLongOrNull() ?: 0L,
                        discretionarySpending = 8000L,
                        emergencyReserveContribution = 5000L
                    )
                )
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_34_allocator_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 34 · INTELLIGENT BUDGET ALLOCATOR", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = incText, onValueChange = { incText = it.filter { c -> c.isDigit() } }, label = { Text("Income ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = essText, onValueChange = { essText = it.filter { c -> c.isDigit() } }, label = { Text("Essential ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = savText, onValueChange = { savText = it.filter { c -> c.isDigit() } }, label = { Text("Savings ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
                }
                Text("Fixed Obligations: ₹${eval.fixedObligationsTotal} · Flexible & Savings: ₹${eval.flexibleAndSavingsTotal}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                Text(
                    text = if (eval.isOverAllocated) "⚠ SHORTFALL DETECTED: Over-allocated by ₹${eval.shortfallAmount}" else "✓ Remaining Unallocated Buffer: ₹${eval.remainingUnallocatedBalance}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (eval.isOverAllocated) CrimsonRuby else ObsidianDarkEmeraldAccent
                )
            }
        }
        "m35_habits" -> {
            var habits by remember(userId) { mutableStateOf(KharcheeProModules31To40Store.getMoneyHabits(context, userId)) }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_35_habits_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 35 · MONEY HABIT LAB", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                habits.forEach { h ->
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${h.title} (${h.frequency})", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                            Text("Completions: ${h.completionCount} days", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent)
                        }
                        OutlinedButton(onClick = {
                            habits = KharcheeProModules31To40Store.checkInMoneyHabit(context, userId, h.id)
                            onShowToast("✓ Checked in habit (duplicate check-ins prevented)")
                        }) { Text("CHECK IN", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver) }
                    }
                }
            }
        }
        "m36_benchmark" -> {
            var mode by remember { mutableStateOf("MoM") }
            val res = remember(mode, expenses) { KharcheeProModules31To40Store.computeHistoricalBenchmark(mode, expenses) }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_36_benchmark_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 36 · FINANCIAL BENCHMARK STUDIO", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MoM", "QoQ", "YoY").forEach { m ->
                        Surface(color = if (mode == m) ObsidianDarkEmeraldAccent.copy(alpha = 0.2f) else ObsidianDarkBackground, shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable { mode = m }) {
                            Text(m, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
                Text("Current Expense: ₹${res.currentExpense} vs Previous: ₹${res.previousExpense} (Delta: ₹${res.expenseAbsoluteDelta}, Change: ${res.expensePercentChange?.let { "$it%" } ?: "N/A - Zero Baseline"})", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ObsidianDarkEmeraldAccent)
                Text(res.disclosureNote, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }
        "m37_liquidity" -> {
            val liq = remember(userId, expenses, savingsGoals) {
                KharcheeProModules31To40Store.computeLiquidityIntelligence(context, userId, expenses, savingsGoals)
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_37_liquidity_screen"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRO MODULE 37 · LIQUIDITY INTELLIGENCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("1. Actual Recorded Wallet Balance: ₹${liq.actualRecordedWalletBalance}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = WarmIvory)
                Text("2. Reserved Funds (Goals + Emergency, Deduplicated): ₹${liq.reservedGoalAndEmergencyFunds}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ChampagneGold)
                Text("3. Upcoming Obligations (Bills ₹${liq.upcomingBillsTotal} + EMIs ₹${liq.upcomingEmisTotal}): ₹${liq.totalUpcomingObligations}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = CrimsonRuby)
                Text("4. Estimated Free Cash After Obligations: ₹${liq.estimatedFreeCashAfterObligations}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = ObsidianDarkEmeraldAccent)
                Text("5. Unreceived Expected Salary (Not Counted in Current Cash): ₹${liq.expectedUnreceivedIncome}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = MutedPlatinum)
            }
        }
        "m38_reconcile" -> {
            var csvText by remember { mutableStateOf("2026-11-01,Zomato,450\n2026-11-02,Metro Recharge,300") }
            val parsed = remember(csvText, expenses) { KharcheeProModules31To40Store.parseAndReconcileCsvStatement(csvText, expenses) }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_38_reconcile_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 38 · SMART RECONCILIATION CENTER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Compare CSV statement rows against recorded Passbook transactions without overwriting actual balances.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                OutlinedTextField(value = csvText, onValueChange = { csvText = it }, label = { Text("CSV Rows (Date,Description,Amount)") }, colors = colors, modifier = Modifier.fillMaxWidth())
                parsed.onSuccess { rows ->
                    rows.forEach { r ->
                        Text("Row #${r.rowNumber}: ${r.description} (₹${r.amount}) → [${r.matchStatus}] ${r.explanation}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                    }
                }.onFailure { err ->
                    Text("Validation Error: ${err.message}", color = CrimsonRuby, fontSize = 11.sp)
                }
            }
        }
        "m39_membership" -> {
            val status = remember(settings) { KharcheeProModules31To40Store.evaluateProMembershipStatus(settings) }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_39_membership_screen"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRO MODULE 39 · PRO BENEFITS & MEMBERSHIP CENTER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Entitlement State: ${status.entitlementState} · Plan: ${status.planName}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = ObsidianDarkEmeraldAccent)
                Text("Billing Amount: ₹${status.billingAmountInr} INR · Renewal/Expiry: ${status.expiryDateLabel}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                Text("Server-Authorised Firestore Entitlement Verified: ${if (status.isBackendVerified) "YES ✓" else "STANDARD FREE TIER"}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkSilver)
            }
        }
        else -> {
            val feed = remember(userId, expenses, savingsGoals, dailyLimit) {
                KharcheeProModules31To40Store.buildMissionControlFeed(context, userId, expenses, savingsGoals, dailyLimit)
            }
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ObsidianDarkPrimarySurface).border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp)).padding(18.dp).testTag("pro_module_40_mission_control_screen"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PRO MODULE 40 · PERSONAL FINANCE MISSION CONTROL", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
                Text("Unified action centre prioritizing obligations, coach tasks & goals without duplicate alerts.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                if (feed.isEmpty()) {
                    Text("All urgent mission control items are clear! Add a bill, goal, or transaction to populate action items.", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                } else {
                    feed.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).clickable { onOpenModule(item.targetModuleId) }.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("[P${item.priority} · ${item.category}] ${item.title}", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = ObsidianDarkSilver)
                            }
                            Text("OPEN ▶", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent)
                        }
                    }
                }
            }
        }
    }
}
