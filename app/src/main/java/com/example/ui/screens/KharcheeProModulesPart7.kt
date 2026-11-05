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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentMatchStatus
import com.example.data.model.ExpenseRecord
import com.example.data.model.KharcheeProModules41To50Store
import com.example.data.model.PersonalApiGatewayResponse
import com.example.data.model.ProEventBudgetPlan
import com.example.data.model.ProInvoiceLineItem
import com.example.data.model.ProInvoiceRecord
import com.example.data.model.ProInvoiceStatus
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.SideIncomePaymentRecord
import com.example.data.model.SideIncomePaymentStatus
import com.example.data.model.SideIncomeSourceRecord
import com.example.data.model.StudentExpenseEntry
import com.example.data.model.StudentFeeScheduleItem
import com.example.data.model.StudentSavingsGoalItem
import com.example.data.model.TravelExpenseCategory
import com.example.data.model.TravelTripPlan
import com.example.data.model.WarrantyProductRecord
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.GoldReserveAccent
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianDarkBorder
import com.example.ui.theme.ObsidianDarkPrimarySurface
import com.example.ui.theme.ObsidianDarkPrimaryText
import com.example.ui.theme.ObsidianDarkSecondarySurface
import com.example.ui.theme.ObsidianDarkSilver
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProModules41To50DispatcherScreen(
    moduleId: String,
    userId: String,
    userDisplayName: String,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    monthlyBudget: Long,
    onShowToast: (String) -> Unit
) {
    when (moduleId) {
        "m41_student" -> ProStudentFinanceModeScreen(userId = userId, expenses = expenses, onShowToast = onShowToast)
        "m42_sideincome" -> ProSideIncomeTrackerScreen(userId = userId, onShowToast = onShowToast)
        "m43_invoice" -> ProInvoiceReceiptGeneratorScreen(userId = userId, userDisplayName = userDisplayName, onShowToast = onShowToast)
        "m44_warranty" -> ProPurchaseReturnWarrantyTrackerScreen(userId = userId, onShowToast = onShowToast)
        "m45_watchlist" -> ProPriceWatchlistScreen(userId = userId, onShowToast = onShowToast)
        "m46_travel" -> ProTravelBudgetPlannerScreen(userId = userId, onShowToast = onShowToast)
        "m47_event" -> ProEventBudgetPlannerScreen(userId = userId, onShowToast = onShowToast)
        "m48_academy" -> ProFinancialLearningAcademyScreen(userId = userId, onShowToast = onShowToast)
        "m49_matcher" -> ProDocumentMatcherScreen(userId = userId, expenses = expenses, onShowToast = onShowToast)
        "m50_api" -> ProPersonalFinanceInsightsApiScreen(
            userId = userId,
            expenses = expenses,
            savingsGoals = savingsGoals,
            monthlyBudget = monthlyBudget,
            onShowToast = onShowToast
        )
        else -> ProStudentFinanceModeScreen(userId = userId, expenses = expenses, onShowToast = onShowToast)
    }
}

// ============================================================================
// MODULE 41 — STUDENT FINANCE MODE
// ============================================================================
@Composable
fun ProStudentFinanceModeScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    val report = remember(userId, expenses, refreshTick) {
        KharcheeProModules41To50Store.buildStudentFinanceReport(context, userId, expenses)
    }

    var allowanceInput by remember(report.config.monthlyAllowance) { mutableStateOf(report.config.monthlyAllowance.toString()) }
    var pocketMoneyInput by remember(report.config.extraPocketMoneyThisMonth) { mutableStateOf(report.config.extraPocketMoneyThisMonth.toString()) }
    var newCategoryInput by remember { mutableStateOf("") }

    var expTitle by remember { mutableStateOf("") }
    var expAmount by remember { mutableStateOf("") }
    var selectedCategory by remember(report.config.customCategories) {
        mutableStateOf(report.config.customCategories.firstOrNull() ?: "Books")
    }

    var feeTitle by remember { mutableStateOf("") }
    var feeInst by remember { mutableStateOf("") }
    var feeAmount by remember { mutableStateOf("") }

    var goalTitle by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalSaved by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_41_student")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.School, contentDescription = "Student Finance Mode", tint = ChampagneGold)
                Column {
                    Text("MODULE 41 · STUDENT FINANCE MODE", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Allowance, Fee Schedules & Academic Reports", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            // Allowance Remaining Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianDarkSecondarySurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Allowance + Pocket Pool", fontSize = 12.sp, color = MutedPlatinum)
                        Text("₹${report.totalAllowancePool}", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, color = WarmIvory)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Spent This Month", fontSize = 12.sp, color = MutedPlatinum)
                        Text("₹${report.monthStudentSpend}", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, color = GoldReserveAccent)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Allowance Remaining", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WarmIvory)
                        Text(
                            text = if (report.isOverAllowance) "-₹${-report.allowanceRemaining} (OVER)" else "₹${report.allowanceRemaining}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (report.isOverAllowance) CrimsonRuby else SophisticatedEmerald
                        )
                    }
                    HorizontalDivider(color = HairlinePlatinum, modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Academic Year (${report.config.currentAcademicYear}) · Spend: ₹${report.academicYearTotalSpend} · Fees Paid: ₹${report.academicYearFeePaid} · Pending Fees: ₹${report.academicYearFeePending}",
                        fontSize = 11.sp,
                        color = ObsidianDarkSilver
                    )
                }
            }

            // Update Allowance & Custom Category
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = allowanceInput,
                    onValueChange = { allowanceInput = it.filter(Char::isDigit) },
                    label = { Text("Allowance (₹)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pocketMoneyInput,
                    onValueChange = { pocketMoneyInput = it.filter(Char::isDigit) },
                    label = { Text("Pocket Money (₹)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = {
                    val updated = report.config.copy(
                        monthlyAllowance = allowanceInput.toLongOrNull() ?: 15000L,
                        extraPocketMoneyThisMonth = pocketMoneyInput.toLongOrNull() ?: 0L
                    )
                    KharcheeProModules41To50Store.saveStudentConfig(context, userId, updated)
                    refreshTick++
                    onShowToast("Updated monthly student allowance")
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Allowance & Pocket Money", fontWeight = FontWeight.Bold)
            }

            // Custom Student Category Creator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    label = { Text("Add Custom Student Category", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = {
                        if (newCategoryInput.isNotBlank()) {
                            KharcheeProModules41To50Store.addCustomStudentCategory(context, userId, newCategoryInput)
                            selectedCategory = newCategoryInput.trim()
                            newCategoryInput = ""
                            refreshTick++
                            onShowToast("Added custom student category")
                        }
                    }
                ) {
                    Text("+ Category")
                }
            }

            // Category Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                report.config.customCategories.forEach { cat ->
                    val isSel = cat == selectedCategory
                    val catSpend = report.categoryBreakdown[cat] ?: 0L
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) ChampagneGold.copy(alpha = 0.22f) else ElevatedGraphite,
                        modifier = Modifier
                            .border(1.dp, if (isSel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = "$cat (₹$catSpend)",
                            fontSize = 11.sp,
                            color = if (isSel) WarmIvory else MutedPlatinum,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Log Student Expense
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = expTitle,
                    onValueChange = { expTitle = it },
                    label = { Text("Item (${selectedCategory})", fontSize = 11.sp) },
                    modifier = Modifier.weight(1.3f)
                )
                OutlinedTextField(
                    value = expAmount,
                    onValueChange = { expAmount = it.filter(Char::isDigit) },
                    label = { Text("₹ Amount", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.9f)
                )
                Button(
                    onClick = {
                        val amt = expAmount.toLongOrNull() ?: 0L
                        if (expTitle.isNotBlank() && amt > 0L) {
                            KharcheeProModules41To50Store.addStudentExpense(
                                context,
                                userId,
                                StudentExpenseEntry(
                                    title = expTitle.trim(),
                                    category = selectedCategory,
                                    amount = amt,
                                    academicYear = report.config.currentAcademicYear
                                )
                            )
                            expTitle = ""
                            expAmount = ""
                            refreshTick++
                            onShowToast("Logged student expense in $selectedCategory")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = Color.Black),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Log")
                }
            }

            // Upcoming Education Fee Schedule
            HorizontalDivider(color = HairlinePlatinum)
            Text("Education Fees & Payment Schedule", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = WarmIvory)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = feeTitle,
                    onValueChange = { feeTitle = it },
                    label = { Text("Fee Title (e.g. Tuition)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = feeInst,
                    onValueChange = { feeInst = it },
                    label = { Text("College/Inst", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = feeAmount,
                    onValueChange = { feeAmount = it.filter(Char::isDigit) },
                    label = { Text("₹ Fee", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.8f)
                )
            }
            OutlinedButton(
                onClick = {
                    val amt = feeAmount.toLongOrNull() ?: 0L
                    if (feeTitle.isNotBlank() && amt > 0L) {
                        KharcheeProModules41To50Store.addStudentFeeSchedule(
                            context,
                            userId,
                            StudentFeeScheduleItem(
                                title = feeTitle.trim(),
                                institution = feeInst.trim().ifBlank { "University" },
                                amount = amt,
                                dueEpochMillis = System.currentTimeMillis() + 14L * 86_400_000L,
                                academicYear = report.config.currentAcademicYear
                            )
                        )
                        feeTitle = ""
                        feeInst = ""
                        feeAmount = ""
                        refreshTick++
                        onShowToast("Added education fee reminder")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Schedule Education Fee Reminder")
            }

            report.upcomingFees.forEach { fee ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${fee.title} · ${fee.institution}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = WarmIvory)
                        Text("₹${fee.amount} · Academic Year ${fee.academicYear}", fontSize = 11.sp, color = MutedPlatinum)
                    }
                    OutlinedButton(
                        onClick = {
                            KharcheeProModules41To50Store.markStudentFeePaid(context, userId, fee.id, true)
                            refreshTick++
                            onShowToast("Marked ${fee.title} as paid")
                        }
                    ) {
                        Text("Mark Paid", fontSize = 11.sp)
                    }
                }
            }

            // Education Savings Goals
            HorizontalDivider(color = HairlinePlatinum)
            Text("Education Savings Goals", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = WarmIvory)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("Goal (e.g. Laptop)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = goalTarget,
                    onValueChange = { goalTarget = it.filter(Char::isDigit) },
                    label = { Text("Target ₹", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.9f)
                )
                OutlinedTextField(
                    value = goalSaved,
                    onValueChange = { goalSaved = it.filter(Char::isDigit) },
                    label = { Text("Saved ₹", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.9f)
                )
            }
            OutlinedButton(
                onClick = {
                    val t = goalTarget.toLongOrNull() ?: 0L
                    val s = goalSaved.toLongOrNull() ?: 0L
                    if (goalTitle.isNotBlank() && t > 0L) {
                        KharcheeProModules41To50Store.addOrUpdateStudentSavingsGoal(
                            context,
                            userId,
                            StudentSavingsGoalItem(
                                title = goalTitle.trim(),
                                targetAmount = t,
                                savedAmount = s,
                                targetDateLabel = "Academic Target"
                            )
                        )
                        goalTitle = ""
                        goalTarget = ""
                        goalSaved = ""
                        refreshTick++
                        onShowToast("Saved education goal")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add Education Savings Goal")
            }

            report.goals.forEach { g ->
                val pct = if (g.targetAmount > 0) (g.savedAmount.toFloat() / g.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(g.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WarmIvory)
                        Text("₹${g.savedAmount} / ₹${g.targetAmount}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ChampagneGold)
                    }
                    LinearProgressIndicator(
                        progress = { pct },
                        color = SophisticatedEmerald,
                        trackColor = ElevatedGraphite,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

// ============================================================================
// MODULE 42 — SIDE-INCOME TRACKER
// ============================================================================
@Composable
fun ProSideIncomeTrackerScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    var statusFilter by remember { mutableStateOf<SideIncomePaymentStatus?>(null) }

    val sources = remember(userId, refreshTick) { KharcheeProModules41To50Store.getSideIncomeSources(context, userId) }
    val payments = remember(userId, refreshTick, statusFilter) {
        val all = KharcheeProModules41To50Store.getSideIncomePayments(context, userId)
        if (statusFilter == null) all else all.filter { it.status == statusFilter }
    }
    val analytics = remember(userId, refreshTick, statusFilter) {
        KharcheeProModules41To50Store.buildSideIncomeAnalytics(context, userId, statusFilter)
    }

    var srcName by remember { mutableStateOf("") }
    var srcClient by remember { mutableStateOf("") }

    var payDesc by remember { mutableStateOf("") }
    var payExpectedAmt by remember { mutableStateOf("") }
    var payDocLabel by remember { mutableStateOf("") }
    var selectedSourceName by remember(sources) { mutableStateOf(sources.firstOrNull()?.name ?: "Freelance Project") }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_42_sideincome")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.WorkOutline, contentDescription = "Side-Income Tracker", tint = ChampagneGold)
                Column {
                    Text("MODULE 42 · SIDE-INCOME TRACKER", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Freelance, Tutoring & Part-Time Cashflow", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Rule Enforced: Expected or pending payments are never marked as received until you explicitly confirm receipt.",
                fontSize = 11.sp,
                color = GoldReserveAccent
            )

            // Analytics Summary
            Surface(shape = RoundedCornerShape(14.dp), color = ObsidianDarkSecondarySurface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Confirmed Received Total", fontSize = 12.sp, color = MutedPlatinum)
                        Text("₹${analytics.totalConfirmedReceived}", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, color = SophisticatedEmerald)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pending / Expected (Unconfirmed)", fontSize = 12.sp, color = MutedPlatinum)
                        Text("₹${analytics.totalPendingAndExpected}", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, color = GoldReserveAccent)
                    }
                    if (analytics.sourceComparisonConfirmed.isNotEmpty()) {
                        HorizontalDivider(color = HairlinePlatinum, modifier = Modifier.padding(vertical = 4.dp))
                        Text("Source Comparison (Confirmed):", fontSize = 11.sp, color = ObsidianDarkSilver)
                        analytics.sourceComparisonConfirmed.forEach { (src, amt) ->
                            Text("• $src: ₹$amt", fontSize = 11.sp, color = WarmIvory)
                        }
                    }
                }
            }

            // Add Income Source
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = srcName,
                    onValueChange = { srcName = it },
                    label = { Text("Source (e.g. Design)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = srcClient,
                    onValueChange = { srcClient = it },
                    label = { Text("Client / Platform", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = {
                        if (srcName.isNotBlank()) {
                            KharcheeProModules41To50Store.addSideIncomeSource(
                                context,
                                userId,
                                SideIncomeSourceRecord(name = srcName.trim(), clientOrPlatform = srcClient.trim().ifBlank { "Direct" })
                            )
                            selectedSourceName = srcName.trim()
                            srcName = ""
                            srcClient = ""
                            refreshTick++
                            onShowToast("Added side-income source")
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("+ Source")
                }
            }

            // Record Expected Payment
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = payDesc,
                    onValueChange = { payDesc = it },
                    label = { Text("Deliverable / Milestone", fontSize = 11.sp) },
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = payExpectedAmt,
                    onValueChange = { payExpectedAmt = it.filter(Char::isDigit) },
                    label = { Text("Expected ₹", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.8f)
                )
                OutlinedTextField(
                    value = payDocLabel,
                    onValueChange = { payDocLabel = it },
                    label = { Text("Vault Doc/Ref", fontSize = 11.sp) },
                    modifier = Modifier.weight(0.9f)
                )
            }
            Button(
                onClick = {
                    val amt = payExpectedAmt.toLongOrNull() ?: 0L
                    if (payDesc.isNotBlank() && amt > 0L) {
                        KharcheeProModules41To50Store.recordExpectedSideIncomePayment(
                            context,
                            userId,
                            SideIncomePaymentRecord(
                                sourceId = selectedSourceName,
                                sourceName = selectedSourceName,
                                description = payDesc.trim(),
                                expectedAmount = amt,
                                status = SideIncomePaymentStatus.PENDING,
                                supportingDocLabel = payDocLabel.trim()
                            )
                        )
                        payDesc = ""
                        payExpectedAmt = ""
                        payDocLabel = ""
                        refreshTick++
                        onShowToast("Recorded expected payment (Pending explicit confirmation)")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Record Expected Payment (Unconfirmed)", fontWeight = FontWeight.Bold)
            }

            // Filter Pills
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "All", SideIncomePaymentStatus.PENDING to "Pending", SideIncomePaymentStatus.CONFIRMED_RECEIVED to "Confirmed").forEach { (st, label) ->
                    val sel = statusFilter == st
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (sel) ChampagneGold.copy(alpha = 0.2f) else ElevatedGraphite,
                        modifier = Modifier.clickable { statusFilter = st }
                    ) {
                        Text(label, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                }
            }

            payments.forEach { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${p.description} (${p.sourceName})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = WarmIvory)
                        Text(
                            text = if (p.confirmedByUser) "CONFIRMED RECEIVED: ₹${p.receivedAmount} via ${p.paymentMethod}"
                            else "PENDING CONFIRMATION · Expected: ₹${p.expectedAmount}",
                            fontSize = 11.sp,
                            color = if (p.confirmedByUser) SophisticatedEmerald else GoldReserveAccent
                        )
                        if (p.supportingDocLabel.isNotBlank()) {
                            Text("Attached Doc: ${p.supportingDocLabel}", fontSize = 10.sp, color = ObsidianDarkSilver)
                        }
                    }
                    if (!p.confirmedByUser) {
                        Button(
                            onClick = {
                                KharcheeProModules41To50Store.confirmSideIncomePaymentReceived(
                                    context = context,
                                    userId = userId,
                                    paymentId = p.id,
                                    explicitUserConfirmation = true,
                                    actualReceivedAmount = p.expectedAmount
                                )
                                refreshTick++
                                onShowToast("Payment explicitly confirmed as received")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = Color.Black)
                        ) {
                            Text("Confirm Received", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODULE 43 — INVOICE & RECEIPT GENERATOR
// ============================================================================
@Composable
fun ProInvoiceReceiptGeneratorScreen(
    userId: String,
    userDisplayName: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    val invoices = remember(userId, refreshTick) { KharcheeProModules41To50Store.getInvoices(context, userId) }

    var invNumber by remember { mutableStateOf("INV-${System.currentTimeMillis() % 10000}") }
    var customerName by remember { mutableStateOf("") }
    var customerContact by remember { mutableStateOf("") }
    var itemDesc by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemPrice by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("0") }
    var taxPctInput by remember { mutableStateOf("0") }
    var previewText by remember { mutableStateOf<String?>(null) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_43_invoice")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.ReceiptLong, contentDescription = "Invoice & Receipt Generator", tint = ChampagneGold)
                Column {
                    Text("MODULE 43 · INVOICE & RECEIPT GENERATOR", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Commercial Invoices & Confirmed Receipts", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Compliance Notice: Invoices are generated as standard commercial/personal statements and do not claim statutory GST/VAT tax-invoice certification unless verified.",
                fontSize = 11.sp,
                color = GoldReserveAccent
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = invNumber, onValueChange = { invNumber = it }, label = { Text("Invoice #", fontSize = 11.sp) }, modifier = Modifier.weight(0.9f))
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name", fontSize = 11.sp) }, modifier = Modifier.weight(1.1f))
                OutlinedTextField(value = customerContact, onValueChange = { customerContact = it }, label = { Text("Email/Phone", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = itemDesc, onValueChange = { itemDesc = it }, label = { Text("Item Description", fontSize = 11.sp) }, modifier = Modifier.weight(1.3f))
                OutlinedTextField(value = itemQty, onValueChange = { itemQty = it.filter(Char::isDigit) }, label = { Text("Qty", fontSize = 11.sp) }, modifier = Modifier.weight(0.5f))
                OutlinedTextField(value = itemPrice, onValueChange = { itemPrice = it.filter(Char::isDigit) }, label = { Text("Unit ₹", fontSize = 11.sp) }, modifier = Modifier.weight(0.9f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = discountInput, onValueChange = { discountInput = it.filter(Char::isDigit) }, label = { Text("Discount ₹", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = taxPctInput, onValueChange = { taxPctInput = it }, label = { Text("Tax %", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
            }

            Button(
                onClick = {
                    val price = itemPrice.toLongOrNull() ?: 0L
                    val qty = (itemQty.toIntOrNull() ?: 1).coerceAtLeast(1)
                    if (customerName.isNotBlank() && itemDesc.isNotBlank() && price > 0L) {
                        val inv = KharcheeProModules41To50Store.createInvoice(
                            context,
                            userId,
                            ProInvoiceRecord(
                                invoiceNumber = invNumber.trim().ifBlank { "INV-1001" },
                                issuerName = userDisplayName,
                                customerName = customerName.trim(),
                                customerContact = customerContact.trim(),
                                lineItems = listOf(ProInvoiceLineItem(description = itemDesc.trim(), quantity = qty, unitPrice = price)),
                                discountAmount = discountInput.toLongOrNull() ?: 0L,
                                taxRatePercent = taxPctInput.toDoubleOrNull() ?: 0.0,
                                taxRegistrationVerified = false,
                                status = ProInvoiceStatus.ISSUED_UNPAID
                            )
                        )
                        previewText = KharcheeProModules41To50Store.formatInvoiceDocumentText(inv)
                        customerName = ""
                        itemDesc = ""
                        itemPrice = ""
                        refreshTick++
                        onShowToast("Created commercial invoice ${inv.invoiceNumber}")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Commercial Invoice", fontWeight = FontWeight.Bold)
            }

            invoices.forEach { inv ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${inv.invoiceNumber} · ${inv.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmIvory)
                        Text(
                            text = if (inv.paymentConfirmedByUser) "PAID (${inv.receiptNumber})" else "UNPAID · ₹${inv.grandTotal}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = if (inv.paymentConfirmedByUser) SophisticatedEmerald else GoldReserveAccent
                        )
                    }
                    Text(inv.complianceNotice, fontSize = 10.sp, color = ObsidianDarkSilver)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { previewText = KharcheeProModules41To50Store.formatInvoiceDocumentText(inv) }
                        ) {
                            Text("View Invoice", fontSize = 11.sp)
                        }
                        if (!inv.paymentConfirmedByUser) {
                            Button(
                                onClick = {
                                    val updated = KharcheeProModules41To50Store.confirmInvoicePaidAndGenerateReceipt(
                                        context = context,
                                        userId = userId,
                                        invoiceId = inv.id,
                                        explicitUserConfirmation = true,
                                        paymentMethod = "UPI / Bank Transfer"
                                    )
                                    if (updated != null) {
                                        previewText = KharcheeProModules41To50Store.generateReceiptDocumentText(updated).getOrNull()
                                    }
                                    refreshTick++
                                    onShowToast("Payment confirmed & Receipt generated")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = Color.Black)
                            ) {
                                Text("Confirm Payment & Issue Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    previewText = KharcheeProModules41To50Store.generateReceiptDocumentText(inv).getOrNull()
                                }
                            ) {
                                Text("View Receipt", fontSize = 11.sp, color = SophisticatedEmerald)
                            }
                        }
                    }
                }
            }

            if (!previewText.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(10.dp), color = ElevatedGraphite, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(previewText!!, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, previewText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export / Share Statement"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share / Export Statement", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODULE 44 — PURCHASE RETURN & WARRANTY TRACKER
// ============================================================================
@Composable
fun ProPurchaseReturnWarrantyTrackerScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val products = remember(userId, refreshTick, searchQuery) {
        val all = KharcheeProModules41To50Store.getWarrantyProducts(context, userId)
        if (searchQuery.isBlank()) all else all.filter {
            it.productName.contains(searchQuery, ignoreCase = true) ||
                it.brandOrStore.contains(searchQuery, ignoreCase = true)
        }
    }

    var prodName by remember { mutableStateOf("") }
    var brandStore by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var returnDaysInput by remember { mutableStateOf("10") }
    var warrantyMonthsInput by remember { mutableStateOf("12") }
    var attachmentLabel by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_44_warranty")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.VerifiedUser, contentDescription = "Purchase Return & Warranty Tracker", tint = ChampagneGold)
                Column {
                    Text("MODULE 44 · PURCHASE RETURN & WARRANTY TRACKER", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Return Windows & Warranty Expiry Calendar", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Note: KHARCHEE displays return and warranty dates entered by you and does not guarantee manufacturer warranty coverage.",
                fontSize = 11.sp,
                color = ObsidianDarkSilver
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = prodName, onValueChange = { prodName = it }, label = { Text("Product Name", fontSize = 11.sp) }, modifier = Modifier.weight(1.2f))
                OutlinedTextField(value = brandStore, onValueChange = { brandStore = it }, label = { Text("Brand / Store", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = amountInput, onValueChange = { amountInput = it.filter(Char::isDigit) }, label = { Text("₹ Paid", fontSize = 11.sp) }, modifier = Modifier.weight(0.8f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = returnDaysInput, onValueChange = { returnDaysInput = it.filter(Char::isDigit) }, label = { Text("Return Days", fontSize = 11.sp) }, modifier = Modifier.weight(0.8f))
                OutlinedTextField(value = warrantyMonthsInput, onValueChange = { warrantyMonthsInput = it.filter(Char::isDigit) }, label = { Text("Warranty Mos", fontSize = 11.sp) }, modifier = Modifier.weight(0.8f))
                OutlinedTextField(value = attachmentLabel, onValueChange = { attachmentLabel = it }, label = { Text("Invoice/Receipt Ref", fontSize = 11.sp) }, modifier = Modifier.weight(1.2f))
            }

            Button(
                onClick = {
                    val amt = amountInput.toLongOrNull() ?: 0L
                    val retDays = (returnDaysInput.toLongOrNull() ?: 10L).coerceAtLeast(0L)
                    val warMonths = (warrantyMonthsInput.toLongOrNull() ?: 12L).coerceAtLeast(1L)
                    if (prodName.isNotBlank() && amt > 0L) {
                        val now = System.currentTimeMillis()
                        KharcheeProModules41To50Store.addWarrantyProduct(
                            context,
                            userId,
                            WarrantyProductRecord(
                                productName = prodName.trim(),
                                brandOrStore = brandStore.trim().ifBlank { "Retailer" },
                                serialOrOrderNo = "ORD-${now % 10000}",
                                purchaseAmount = amt,
                                purchaseEpochMillis = now,
                                returnDeadlineEpochMillis = now + retDays * 86_400_000L,
                                warrantyStartEpochMillis = now,
                                warrantyExpiryEpochMillis = now + warMonths * 30L * 86_400_000L,
                                invoiceAttachmentLabel = attachmentLabel.trim().ifBlank { "Receipt linked" }
                            )
                        )
                        prodName = ""
                        brandStore = ""
                        amountInput = ""
                        refreshTick++
                        onShowToast("Saved product return & warranty schedule")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Purchase Return & Warranty Record", fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search product or brand...", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth()
            )

            val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
            products.forEach { p ->
                val st = p.evaluateStatus()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${p.productName} (${p.brandOrStore})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmIvory)
                        Text(st.name, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    }
                    Text(
                        text = "Paid ₹${p.purchaseAmount} · Return by: ${fmt.format(Date(p.returnDeadlineEpochMillis))} · Warranty ends: ${fmt.format(Date(p.warrantyExpiryEpochMillis))}",
                        fontSize = 11.sp,
                        color = MutedPlatinum
                    )
                    Text(p.warrantyDisclaimer, fontSize = 10.sp, color = ObsidianDarkSilver)
                }
            }
        }
    }
}

// ============================================================================
// MODULE 45 — PRICE WATCHLIST
// ============================================================================
@Composable
fun ProPriceWatchlistScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    val items = remember(userId, refreshTick) { KharcheeProModules41To50Store.getPriceWatchlist(context, userId) }

    var prodName by remember { mutableStateOf("") }
    var merchantLabel by remember { mutableStateOf("") }
    var observedInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }
    var newObservationInput by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_45_watchlist")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.PriceCheck, contentDescription = "Price Watchlist", tint = ChampagneGold)
                Column {
                    Text("MODULE 45 · PRICE WATCHLIST", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Manual Price Observations & Target Alerts", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Transparency Notice: Live e-commerce price scraping is disabled unless external API credentials (PRICE_FEED_API_KEY) are configured in .env. Prices shown are strictly your manual observations.",
                fontSize = 11.sp,
                color = GoldReserveAccent
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = prodName, onValueChange = { prodName = it }, label = { Text("Product", fontSize = 11.sp) }, modifier = Modifier.weight(1.1f))
                OutlinedTextField(value = merchantLabel, onValueChange = { merchantLabel = it }, label = { Text("Store", fontSize = 11.sp) }, modifier = Modifier.weight(0.9f))
                OutlinedTextField(value = observedInput, onValueChange = { observedInput = it.filter(Char::isDigit) }, label = { Text("Current ₹", fontSize = 11.sp) }, modifier = Modifier.weight(0.8f))
                OutlinedTextField(value = targetInput, onValueChange = { targetInput = it.filter(Char::isDigit) }, label = { Text("Target ₹", fontSize = 11.sp) }, modifier = Modifier.weight(0.8f))
            }

            Button(
                onClick = {
                    val cur = observedInput.toLongOrNull() ?: 0L
                    val tgt = targetInput.toLongOrNull() ?: 0L
                    if (prodName.isNotBlank() && cur > 0L && tgt > 0L) {
                        KharcheeProModules41To50Store.addPriceWatchlistItem(
                            context,
                            userId,
                            prodName,
                            merchantLabel.ifBlank { "Online Store" },
                            cur,
                            tgt
                        )
                        prodName = ""
                        merchantLabel = ""
                        observedInput = ""
                        targetInput = ""
                        refreshTick++
                        onShowToast("Added product to manual price watchlist")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Product to Watchlist", fontWeight = FontWeight.Bold)
            }

            items.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} · ${item.merchantOrUrlLabel}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmIvory)
                        Text(
                            text = if (item.isTargetReached) "TARGET REACHED! (₹${item.currentObservedPrice})" else "Now ₹${item.currentObservedPrice} / Target ₹${item.targetPrice}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = if (item.isTargetReached) SophisticatedEmerald else GoldReserveAccent
                        )
                    }
                    Text(
                        text = "History (${item.observations.size} pts): " + item.observations.joinToString(" → ") { "₹${it.observedPrice}" },
                        fontSize = 11.sp,
                        color = MutedPlatinum
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newObservationInput,
                            onValueChange = { newObservationInput = it.filter(Char::isDigit) },
                            label = { Text("Log Observed Price ₹", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = {
                                val nextP = newObservationInput.toLongOrNull() ?: 0L
                                if (nextP > 0L) {
                                    KharcheeProModules41To50Store.recordManualPriceObservation(context, userId, item.id, nextP)
                                    newObservationInput = ""
                                    refreshTick++
                                    if (nextP <= item.targetPrice) {
                                        onShowToast("Target Price Alert! ${item.productName} reached ₹$nextP")
                                    } else {
                                        onShowToast("Logged manual price observation: ₹$nextP")
                                    }
                                }
                            }
                        ) {
                            Text("Update Price", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODULE 46 — TRAVEL BUDGET PLANNER
// ============================================================================
@Composable
fun ProTravelBudgetPlannerScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    val trips = remember(userId, refreshTick) { KharcheeProModules41To50Store.getTravelTrips(context, userId) }

    var tripName by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var tripBudgetInput by remember { mutableStateOf("") }

    var itemTitle by remember { mutableStateOf("") }
    var itemPlannedInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TravelExpenseCategory.TRANSPORT.label) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_46_travel")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = "Travel Budget Planner", tint = ChampagneGold)
                Column {
                    Text("MODULE 46 · TRAVEL BUDGET PLANNER", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Planned vs Confirmed Actual Trip Costs", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Rule Enforced: Planned trip expenses stay strictly separate from actual confirmed spending until you confirm each expense.",
                fontSize = 11.sp,
                color = GoldReserveAccent
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = tripName, onValueChange = { tripName = it }, label = { Text("Trip Name", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = destination, onValueChange = { destination = it }, label = { Text("Destination", fontSize = 11.sp) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tripBudgetInput, onValueChange = { tripBudgetInput = it.filter(Char::isDigit) }, label = { Text("Budget ₹", fontSize = 11.sp) }, modifier = Modifier.weight(0.9f))
            }

            Button(
                onClick = {
                    val b = tripBudgetInput.toLongOrNull() ?: 0L
                    if (tripName.isNotBlank() && b > 0L) {
                        KharcheeProModules41To50Store.createTravelTrip(
                            context,
                            userId,
                            TravelTripPlan(
                                tripName = tripName.trim(),
                                destination = destination.trim().ifBlank { "Domestic" },
                                startDateLabel = "Upcoming",
                                endDateLabel = "Flexible",
                                totalEstimatedBudget = b
                            )
                        )
                        tripName = ""
                        destination = ""
                        tripBudgetInput = ""
                        refreshTick++
                        onShowToast("Created trip budget plan")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create New Trip Plan", fontWeight = FontWeight.Bold)
            }

            trips.forEach { trip ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${trip.tripName} (${trip.destination})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmIvory)
                        Text("Budget: ₹${trip.totalEstimatedBudget}", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ChampagneGold)
                    }
                    Text(
                        text = "Planned: ₹${trip.totalPlannedCost} · Confirmed Actual: ₹${trip.totalActualConfirmedCost} · Remaining (Actual): ₹${trip.remainingBudgetFromActual}",
                        fontSize = 11.sp,
                        color = SophisticatedEmerald
                    )

                    // Category selector for adding planned item
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TravelExpenseCategory.entries.forEach { cat ->
                            val sel = selectedCategory == cat.label
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) ChampagneGold.copy(alpha = 0.2f) else ElevatedGraphite,
                                modifier = Modifier.clickable { selectedCategory = cat.label }
                            ) {
                                Text(cat.label, fontSize = 10.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = itemTitle, onValueChange = { itemTitle = it }, label = { Text("Item ($selectedCategory)", fontSize = 10.sp) }, modifier = Modifier.weight(1.2f))
                        OutlinedTextField(value = itemPlannedInput, onValueChange = { itemPlannedInput = it.filter(Char::isDigit) }, label = { Text("Planned ₹", fontSize = 10.sp) }, modifier = Modifier.weight(0.8f))
                        OutlinedButton(
                            onClick = {
                                val pAmt = itemPlannedInput.toLongOrNull() ?: 0L
                                if (itemTitle.isNotBlank() && pAmt > 0L) {
                                    KharcheeProModules41To50Store.addPlannedTravelExpense(context, userId, trip.id, itemTitle, selectedCategory, pAmt)
                                    itemTitle = ""
                                    itemPlannedInput = ""
                                    refreshTick++
                                    onShowToast("Added planned trip item")
                                }
                            }
                        ) {
                            Text("+ Plan", fontSize = 11.sp)
                        }
                    }

                    trip.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${item.category}] ${item.title}: Planned ₹${item.plannedAmount} | " +
                                    if (item.isConfirmedActualExpense) "Confirmed ₹${item.actualConfirmedAmount}" else "Unconfirmed Plan",
                                fontSize = 11.sp,
                                color = if (item.isConfirmedActualExpense) SophisticatedEmerald else MutedPlatinum,
                                modifier = Modifier.weight(1f)
                            )
                            if (!item.isConfirmedActualExpense) {
                                OutlinedButton(
                                    onClick = {
                                        KharcheeProModules41To50Store.confirmActualTravelExpense(
                                            context = context,
                                            userId = userId,
                                            tripId = trip.id,
                                            itemId = item.id,
                                            actualAmount = item.plannedAmount,
                                            explicitUserConfirmation = true
                                        )
                                        refreshTick++
                                        onShowToast("Confirmed actual trip expense")
                                    }
                                ) {
                                    Text("Confirm Spend", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val reportTxt = KharcheeProModules41To50Store.exportTripReportText(trip)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, reportTxt)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Trip Report"))
                        }
                    ) {
                        Text("Export Trip Report", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODULE 47 — EVENT BUDGET PLANNER
// ============================================================================
@Composable
fun ProEventBudgetPlannerScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    val events = remember(userId, refreshTick) { KharcheeProModules41To50Store.getEventBudgets(context, userId) }

    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var baseBudgetInput by remember { mutableStateOf("") }

    var contribName by remember { mutableStateOf("") }
    var contribExpectedInput by remember { mutableStateOf("") }

    var expTitle by remember { mutableStateOf("") }
    var expAmountInput by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_47_event")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Event, contentDescription = "Event Budget Planner", tint = ChampagneGold)
                Column {
                    Text("MODULE 47 · EVENT BUDGET PLANNER", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Events, Contributions & Actual Spend", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Rule Enforced: Expected contributions are tracked separately and are never counted as available received money until confirmed.",
                fontSize = 11.sp,
                color = GoldReserveAccent
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = eventName, onValueChange = { eventName = it }, label = { Text("Event Name", fontSize = 11.sp) }, modifier = Modifier.weight(1.1f))
                OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Date", fontSize = 11.sp) }, modifier = Modifier.weight(0.9f))
                OutlinedTextField(value = baseBudgetInput, onValueChange = { baseBudgetInput = it.filter(Char::isDigit) }, label = { Text("Own Budget ₹", fontSize = 11.sp) }, modifier = Modifier.weight(0.9f))
            }

            Button(
                onClick = {
                    val b = baseBudgetInput.toLongOrNull() ?: 0L
                    if (eventName.isNotBlank() && b >= 0L) {
                        KharcheeProModules41To50Store.createEventBudgetPlan(
                            context,
                            userId,
                            ProEventBudgetPlan(
                                eventName = eventName.trim(),
                                eventDateLabel = eventDate.trim().ifBlank { "Upcoming" },
                                baseAllocatedBudget = b
                            )
                        )
                        eventName = ""
                        eventDate = ""
                        baseBudgetInput = ""
                        refreshTick++
                        onShowToast("Created event budget plan")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Event Budget Plan", fontWeight = FontWeight.Bold)
            }

            events.forEach { ev ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("${ev.eventName} (${ev.eventDateLabel})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmIvory)
                    Text(
                        text = "Base Budget: ₹${ev.baseAllocatedBudget} · Confirmed Contributions: ₹${ev.totalConfirmedContributionsReceived} · Unconfirmed Expected: ₹${ev.totalExpectedContributionsUnconfirmed}",
                        fontSize = 11.sp,
                        color = MutedPlatinum
                    )
                    Text(
                        text = "Total Confirmed Available: ₹${ev.totalAvailableConfirmedFunds} · Actual Spend: ₹${ev.totalActualExpenses} · Remaining Confirmed Budget: ₹${ev.remainingConfirmedBudget}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedEmerald
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = contribName, onValueChange = { contribName = it }, label = { Text("Contributor", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = contribExpectedInput, onValueChange = { contribExpectedInput = it.filter(Char::isDigit) }, label = { Text("Expected ₹", fontSize = 10.sp) }, modifier = Modifier.weight(0.8f))
                        OutlinedButton(
                            onClick = {
                                val amt = contribExpectedInput.toLongOrNull() ?: 0L
                                if (contribName.isNotBlank() && amt > 0L) {
                                    KharcheeProModules41To50Store.addExpectedEventContribution(context, userId, ev.id, contribName, amt)
                                    contribName = ""
                                    contribExpectedInput = ""
                                    refreshTick++
                                    onShowToast("Added expected contribution (not counted as received yet)")
                                }
                            }
                        ) {
                            Text("+ Expected", fontSize = 10.sp)
                        }
                    }

                    ev.contributions.forEach { c ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${c.contributorName}: " + if (c.isConfirmedReceived) "CONFIRMED ₹${c.confirmedReceivedAmount}" else "EXPECTED ₹${c.expectedAmount} (Unconfirmed)",
                                fontSize = 11.sp,
                                color = if (c.isConfirmedReceived) SophisticatedEmerald else GoldReserveAccent
                            )
                            if (!c.isConfirmedReceived) {
                                OutlinedButton(
                                    onClick = {
                                        KharcheeProModules41To50Store.confirmEventContributionReceived(context, userId, ev.id, c.id, c.expectedAmount, true)
                                        refreshTick++
                                        onShowToast("Confirmed contribution received from ${c.contributorName}")
                                    }
                                ) {
                                    Text("Confirm Received", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = expTitle, onValueChange = { expTitle = it }, label = { Text("Event Expense", fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = expAmountInput, onValueChange = { expAmountInput = it.filter(Char::isDigit) }, label = { Text("Actual ₹", fontSize = 10.sp) }, modifier = Modifier.weight(0.8f))
                        OutlinedButton(
                            onClick = {
                                val amt = expAmountInput.toLongOrNull() ?: 0L
                                if (expTitle.isNotBlank() && amt > 0L) {
                                    KharcheeProModules41To50Store.addAndConfirmEventExpense(context, userId, ev.id, expTitle, "Venue/Catering", amt, amt, true)
                                    expTitle = ""
                                    expAmountInput = ""
                                    refreshTick++
                                    onShowToast("Logged actual event expense")
                                }
                            }
                        ) {
                            Text("+ Spend", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODULE 48 — FINANCIAL LEARNING ACADEMY
// ============================================================================
@Composable
fun ProFinancialLearningAcademyScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }
    val curriculum = remember { KharcheeProModules41To50Store.getAcademyCurriculum() }
    val progress = remember(userId, refreshTick) { KharcheeProModules41To50Store.getAcademyProgress(context, userId) }
    var activeLessonId by remember { mutableStateOf(curriculum.first().id) }
    val activeLesson = curriculum.firstOrNull { it.id == activeLessonId } ?: curriculum.first()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_48_academy")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.School, contentDescription = "Financial Learning Academy", tint = ChampagneGold)
                Column {
                    Text("MODULE 48 · FINANCIAL LEARNING ACADEMY", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("7-Topic Literacy Curriculum & Quizzes", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = KharcheeProModules41To50Store.ACADEMY_EDUCATIONAL_DISCLAIMER,
                fontSize = 11.sp,
                color = GoldReserveAccent
            )

            // Progress Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Completed: ${progress.completedLessonIds.size} / ${curriculum.size} Modules", fontSize = 12.sp, color = WarmIvory)
                Text("${progress.completionPercent}%", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = SophisticatedEmerald)
            }
            LinearProgressIndicator(
                progress = { progress.completionPercent / 100f },
                color = SophisticatedEmerald,
                trackColor = ElevatedGraphite,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )

            // Lesson Selector
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                curriculum.forEach { lesson ->
                    val done = lesson.id in progress.completedLessonIds
                    val sel = lesson.id == activeLessonId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (sel) ChampagneGold.copy(alpha = 0.22f) else ElevatedGraphite,
                        modifier = Modifier
                            .border(1.dp, if (done) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { activeLessonId = lesson.id }
                    ) {
                        Text(
                            text = "${lesson.topicOrder}. ${lesson.categoryTag}" + if (done) " ✓" else "",
                            fontSize = 11.sp,
                            color = if (sel) WarmIvory else MutedPlatinum,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Active Lesson Card
            Surface(shape = RoundedCornerShape(14.dp), color = ObsidianDarkSecondarySurface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(activeLesson.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmIvory)
                    Text(activeLesson.contentSummary, fontSize = 12.sp, color = MutedPlatinum)
                    activeLesson.keyTakeaways.forEach { takeaway ->
                        Text("• $takeaway", fontSize = 12.sp, color = WarmIvory)
                    }
                    HorizontalDivider(color = HairlinePlatinum, modifier = Modifier.padding(vertical = 6.dp))
                    val quiz = activeLesson.quizQuestions.firstOrNull()
                    if (quiz != null) {
                        Text("Interactive Quiz: ${quiz.question}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ChampagneGold)
                        quiz.options.forEachIndexed { idx, optText ->
                            OutlinedButton(
                                onClick = {
                                    val updated = KharcheeProModules41To50Store.submitAcademyQuizAndCompleteLesson(
                                        context,
                                        userId,
                                        activeLesson.id,
                                        idx
                                    )
                                    refreshTick++
                                    if (idx == quiz.correctOptionIndex) {
                                        onShowToast("Correct! ${quiz.explanation}")
                                    } else {
                                        onShowToast("Try again — review the key takeaways above.")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(optText, fontSize = 11.sp, color = WarmIvory)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODULE 49 — INCOME & EXPENSE DOCUMENT MATCHER
// ============================================================================
@Composable
fun ProDocumentMatcherScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }

    val candidates = remember(userId, refreshTick) { KharcheeProModules41To50Store.getDocumentMatchCandidates(context, userId) }
    val auditTrail = remember(userId, refreshTick) { KharcheeProModules41To50Store.getDocumentMatchAuditTrail(context, userId) }

    var docTitle by remember { mutableStateOf("") }
    var docText by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_49_matcher")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Description, contentDescription = "Document Matcher", tint = ChampagneGold)
                Column {
                    Text("MODULE 49 · INCOME & EXPENSE DOCUMENT MATCHER", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Receipt Extraction, Confidence & Audit Trail", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Rule Enforced: Original transactions are never overwritten automatically. A document never assumes payment occurred without your explicit confirmation.",
                fontSize = 11.sp,
                color = GoldReserveAccent
            )

            OutlinedTextField(
                value = docTitle,
                onValueChange = { docTitle = it },
                label = { Text("Document / Receipt Title", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = docText,
                onValueChange = { docText = it },
                label = { Text("Paste Receipt / Statement Snippet (e.g. Merchant: Swiggy Total: Rs. 450 on 01/11/2026)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (docText.isNotBlank()) {
                        val cand = KharcheeProModules41To50Store.parseAndSuggestDocumentMatch(
                            context = context,
                            userId = userId,
                            documentTitle = docTitle.ifBlank { "Imported Receipt" },
                            rawDocumentText = docText,
                            existingTransactions = expenses
                        )
                        docTitle = ""
                        docText = ""
                        refreshTick++
                        onShowToast("Extracted ₹${cand.extractedAmount} (${cand.confidencePercent}% match confidence)")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Extract & Suggest Transaction Match", fontWeight = FontWeight.Bold)
            }

            candidates.forEach { c ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${c.documentTitle} · ${c.extractedMerchant}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmIvory)
                        Text("${c.confidencePercent}% Match", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = ChampagneGold)
                    }
                    Text("Extracted: ₹${c.extractedAmount} on ${c.extractedDateLabel}", fontSize = 11.sp, color = MutedPlatinum)
                    Text(
                        text = if (c.suggestedTransactionId != null) "Suggested Tx: ${c.suggestedTransactionTitle} (₹${c.suggestedTransactionAmount})"
                        else "No matching existing transaction found",
                        fontSize = 11.sp,
                        color = SophisticatedEmerald
                    )
                    if (c.possibleDuplicateFlag) {
                        Text("⚠ ${c.duplicateReason}", fontSize = 11.sp, color = CrimsonRuby)
                    }
                    if (c.status == DocumentMatchStatus.PENDING_USER_REVIEW) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    KharcheeProModules41To50Store.reviewDocumentMatchCandidate(context, userId, c.id, true)
                                    refreshTick++
                                    onShowToast("Confirmed link & logged to audit trail")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = Color.Black)
                            ) {
                                Text("Confirm Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    KharcheeProModules41To50Store.reviewDocumentMatchCandidate(context, userId, c.id, false)
                                    refreshTick++
                                    onShowToast("Rejected match suggestion")
                                }
                            ) {
                                Text("Reject", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Text("Review Status: ${c.status.name}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkSilver)
                    }
                }
            }

            if (auditTrail.isNotEmpty()) {
                HorizontalDivider(color = HairlinePlatinum)
                Text("Document Match Audit Trail (${auditTrail.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WarmIvory)
                auditTrail.take(4).forEach { a ->
                    Text("• [${a.action}] ${a.details}", fontSize = 10.sp, color = ObsidianDarkSilver)
                }
            }
        }
    }
}

// ============================================================================
// MODULE 50 — PERSONAL FINANCE DATA INSIGHTS API
// ============================================================================
@Composable
fun ProPersonalFinanceInsightsApiScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    monthlyBudget: Long,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshTick by remember { mutableIntStateOf(0) }

    val tokens = remember(userId, refreshTick) { KharcheeProModules41To50Store.getScopedApiTokens(context, userId) }
    val logs = remember(userId, refreshTick) { KharcheeProModules41To50Store.getApiAccessLogs(context, userId) }

    var tokenLabel by remember { mutableStateOf("") }
    var scopeSummary by remember { mutableStateOf(true) }
    var scopeTx by remember { mutableStateOf(true) }
    var scopeGoals by remember { mutableStateOf(false) }
    var newlyGeneratedRawToken by remember { mutableStateOf<String?>(null) }
    var latestResponse by remember { mutableStateOf<PersonalApiGatewayResponse?>(null) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("pro_module_50_api")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Api, contentDescription = "Personal Finance Data Insights API", tint = ChampagneGold)
                Column {
                    Text("MODULE 50 · PERSONAL FINANCE DATA INSIGHTS API", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
                    Text("Scoped Read-Only Gateway, Rate Limiter & Logs", fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = WarmIvory)
                }
            }

            Text(
                text = "Security Architecture: Strictly read-only, user-isolated (requestingUserId == tokenOwnerUserId), SHA-256 hashed tokens, 15 req/min rate limit, and zero arbitrary query execution.",
                fontSize = 11.sp,
                color = SophisticatedEmerald
            )

            OutlinedTextField(
                value = tokenLabel,
                onValueChange = { tokenLabel = it },
                label = { Text("Token Label (e.g. Personal Dashboard Export)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = scopeSummary, onCheckedChange = { scopeSummary = it })
                    Text("read:summary", fontSize = 11.sp, color = WarmIvory)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = scopeTx, onCheckedChange = { scopeTx = it })
                    Text("read:transactions", fontSize = 11.sp, color = WarmIvory)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = scopeGoals, onCheckedChange = { scopeGoals = it })
                    Text("read:goals", fontSize = 11.sp, color = WarmIvory)
                }
            }

            Button(
                onClick = {
                    val scopes = buildSet {
                        if (scopeSummary) add("read:summary")
                        if (scopeTx) add("read:transactions")
                        if (scopeGoals) add("read:goals")
                    }
                    val (raw, _) = KharcheeProModules41To50Store.generateScopedApiToken(
                        context = context,
                        ownerUserId = userId,
                        label = tokenLabel.ifBlank { "Read-Only Personal Token" },
                        requestedScopes = scopes
                    )
                    newlyGeneratedRawToken = raw
                    tokenLabel = ""
                    refreshTick++
                    onShowToast("Generated scoped read-only API token")
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Scoped Read-Only Token", fontWeight = FontWeight.Bold)
            }

            if (newlyGeneratedRawToken != null) {
                Surface(shape = RoundedCornerShape(10.dp), color = ElevatedGraphite, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Copy Token Now (Shown Once, Stored as SHA-256):", fontSize = 10.sp, color = GoldReserveAccent)
                        Text(newlyGeneratedRawToken!!, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = WarmIvory)
                    }
                }
            }

            tokens.forEach { t ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianDarkSecondarySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${t.label} (${t.tokenPrefix})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WarmIvory)
                        Text(
                            text = if (t.isRevoked) "REVOKED" else "ACTIVE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (t.isRevoked) CrimsonRuby else SophisticatedEmerald
                        )
                    }
                    Text("Scopes: ${t.scopes.joinToString(", ")}", fontSize = 11.sp, color = MutedPlatinum)
                    if (!t.isRevoked) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            KharcheeProModules41To50Store.SUPPORTED_ENDPOINTS.forEach { ep ->
                                OutlinedButton(
                                    onClick = {
                                        latestResponse = KharcheeProModules41To50Store.executeReadOnlyApiRequest(
                                            context = context,
                                            authenticatedUserId = userId,
                                            tokenOwnerUserId = userId,
                                            tokenId = t.id,
                                            endpointPath = ep,
                                            userTransactions = expenses,
                                            userGoals = savingsGoals,
                                            monthlyBudget = monthlyBudget
                                        )
                                        refreshTick++
                                    }
                                ) {
                                    Text("GET $ep", fontSize = 10.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    KharcheeProModules41To50Store.revokeScopedApiToken(context, userId, t.id)
                                    refreshTick++
                                    onShowToast("Revoked token ${t.tokenPrefix}")
                                }
                            ) {
                                Text("Revoke", fontSize = 10.sp, color = CrimsonRuby)
                            }
                        }
                    }
                }
            }

            if (latestResponse != null) {
                Surface(shape = RoundedCornerShape(10.dp), color = ElevatedGraphite, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "HTTP ${latestResponse!!.statusCode} · ${latestResponse!!.endpoint} · Rate Limit Remaining: ${latestResponse!!.rateLimitRemaining}/15",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = if (latestResponse!!.isSuccess) SophisticatedEmerald else CrimsonRuby
                        )
                        if (latestResponse!!.errorExplanation.isNotBlank()) {
                            Text(latestResponse!!.errorExplanation, fontSize = 11.sp, color = GoldReserveAccent)
                        }
                        Text(latestResponse!!.jsonPayload, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = WarmIvory)
                    }
                }
            }

            if (logs.isNotEmpty()) {
                HorizontalDivider(color = HairlinePlatinum)
                Text("API Access & Security Audit Logs (${logs.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WarmIvory)
                logs.take(5).forEach { l ->
                    Text("• [HTTP ${l.statusCode}] ${l.endpoint} — ${l.statusMessage}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkSilver)
                }
            }
        }
    }
}
