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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.KharcheeProModules11To20Store
import com.example.data.model.ProSmartBillRecord
import com.example.data.model.ProWalletType
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
private fun proFieldColors() = OutlinedTextFieldDefaults.colors(
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
fun ProMultiWalletIntelligenceScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val overview = remember(userId, expenses, refreshKey) {
        KharcheeProModules11To20Store.computeConsolidatedWalletOverview(context, userId, expenses)
    }

    var newWalletName by remember { mutableStateOf("") }
    var newWalletOpening by remember { mutableStateOf("10000") }
    var selectedType by remember { mutableStateOf(ProWalletType.BANK) }

    var fromIdx by remember { mutableIntStateOf(0) }
    var toIdx by remember { mutableIntStateOf(1) }
    var transferAmt by remember { mutableStateOf("1000") }

    val colors = proFieldColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_11_wallets_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 11 · MULTI-WALLET INTELLIGENCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text(
            text = "Consolidated Balance: ₹${overview.consolidatedBalance} · Income: ₹${overview.consolidatedIncome} · Expense: ₹${overview.consolidatedExpense}",
            fontFamily = headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = ObsidianDarkEmeraldAccent
        )
        Text("Wallet transfers adjust source & destination balances atomically without inflating income or expense totals.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        // Active Wallets List
        overview.activeWalletSummaries.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).border(0.6.dp, ObsidianDarkBorder, RoundedCornerShape(12.dp)).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${item.wallet.name} (${item.wallet.walletType.label})", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 13.sp)
                    Text("Open: ₹${item.wallet.openingBalance} · In: +₹${item.totalIncome + item.transfersIn} · Out: -₹${item.totalExpense + item.transfersOut}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
                }
                Text("₹${item.currentBalance}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = ObsidianDarkSilver)
            }
        }

        // Create New Wallet
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = newWalletName, onValueChange = { newWalletName = it }, label = { Text("New Wallet Name") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = newWalletOpening, onValueChange = { newWalletOpening = it.filter { c -> c.isDigit() } }, label = { Text("Opening ₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
            Button(
                onClick = {
                    val open = newWalletOpening.toLongOrNull() ?: 0L
                    if (newWalletName.isNotBlank()) {
                        KharcheeProModules11To20Store.createOrUpdateWallet(context, userId, name = newWalletName, walletType = selectedType, openingBalance = open)
                        newWalletName = ""
                        refreshKey++
                        onShowToast("✅ Created wallet")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack)
            ) { Text("ADD", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }

        // Atomic Wallet Transfer
        if (overview.activeWalletSummaries.size >= 2) {
            val activeWallets = overview.activeWalletSummaries.map { it.wallet }
            val safeFrom = activeWallets.getOrElse(fromIdx % activeWallets.size) { activeWallets.first() }
            val safeTo = activeWallets.getOrElse(toIdx % activeWallets.size) { activeWallets.last() }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { fromIdx = (fromIdx + 1) % activeWallets.size }, modifier = Modifier.weight(1f)) {
                    Text("From: ${safeFrom.name}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = WarmIvory)
                }
                OutlinedButton(onClick = { toIdx = (toIdx + 1) % activeWallets.size }, modifier = Modifier.weight(1f)) {
                    Text("To: ${safeTo.name}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent)
                }
                OutlinedTextField(value = transferAmt, onValueChange = { transferAmt = it.filter { c -> c.isDigit() } }, label = { Text("₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.7f))
                Button(
                    onClick = {
                        val amt = transferAmt.toLongOrNull() ?: 0L
                        val res = KharcheeProModules11To20Store.transferBetweenWallets(context, userId, safeFrom.id, safeTo.id, amt, "Internal Transfer")
                        res.onSuccess {
                            refreshKey++
                            onShowToast("🔄 Transferred ₹$amt from ${safeFrom.name} to ${safeTo.name}")
                        }.onFailure { err ->
                            onShowToast("⚠️ ${err.message}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack)
                ) { Text("TRANSFER", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun ProLoanEmiIntelligenceScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var loans by remember(userId) { mutableStateOf(KharcheeProModules11To20Store.getProLoans(context, userId)) }

    var loanName by remember { mutableStateOf("Home / Vehicle Loan") }
    var principalText by remember { mutableStateOf("500000") }
    var rateText by remember { mutableStateOf("9.5") }
    var tenureText by remember { mutableStateOf("24") }

    val previewCalc = remember(principalText, rateText, tenureText) {
        KharcheeProModules11To20Store.calculateLoanAmortization(
            principal = principalText.toLongOrNull() ?: 0L,
            annualInterestRatePct = rateText.toDoubleOrNull() ?: 0.0,
            tenureMonths = tenureText.toIntOrNull() ?: 12
        )
    }
    val colors = proFieldColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_12_loans_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 12 · LOAN & EMI INTELLIGENCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Standard reducing-balance & zero-interest EMI calculator with full amortisation schedule", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = loanName, onValueChange = { loanName = it }, label = { Text("Loan Name") }, singleLine = true, colors = colors, modifier = Modifier.weight(1.2f))
            OutlinedTextField(value = principalText, onValueChange = { principalText = it.filter { c -> c.isDigit() } }, label = { Text("Principal ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = rateText, onValueChange = { rateText = it }, label = { Text("Annual Rate %") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = tenureText, onValueChange = { tenureText = it.filter { c -> c.isDigit() } }, label = { Text("Tenure (Months)") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
        }

        if (previewCalc.isValid) {
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Monthly EMI: ₹${previewCalc.monthlyEmi} · Total Interest: ₹${previewCalc.totalInterest} · Total Payable: ₹${previewCalc.totalPayable}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ObsidianDarkEmeraldAccent)
                Text("First 3 Amortisation Installments:", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver)
                previewCalc.schedule.take(3).forEach { row ->
                    Text("#${row.installmentNumber}: EMI ₹${row.emiAmount} (Principal ₹${row.principalComponent} + Interest ₹${row.interestComponent}) → Bal ₹${row.closingBalance}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = WarmIvory)
                }
            }
        }

        Button(
            onClick = {
                val p = principalText.toLongOrNull() ?: 0L
                val r = rateText.toDoubleOrNull() ?: 0.0
                val n = tenureText.toIntOrNull() ?: 12
                loans = KharcheeProModules11To20Store.addProLoan(context, userId, loanName, p, r, "01 Nov 2026", n)
                onShowToast("🏦 Saved Loan & Amortisation Schedule")
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) { Text("SAVE LOAN TO PRO LEDGER", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }

        loans.forEach { l ->
            val calc = KharcheeProModules11To20Store.calculateLoanAmortization(l.principalAmount, l.annualInterestRatePct, l.tenureMonths, l.paidInstallments.toSet())
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${l.name} (EMI ₹${l.monthlyEmi}/mo)", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 13.sp)
                    Text("Outstanding: ₹${calc.outstandingBalance} · Paid Installments: ${l.paidInstallments.size}/${l.tenureMonths}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent)
                }
                OutlinedButton(
                    onClick = {
                        val nextInst = (l.paidInstallments.maxOrNull() ?: 0) + 1
                        loans = KharcheeProModules11To20Store.recordLoanInstallmentPaid(context, userId, l.id, nextInst)
                        onShowToast("✓ Recorded Installment #$nextInst for ${l.name}")
                    }
                ) { Text("RECORD EMI", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver) }
            }
        }
    }
}

@Composable
fun ProSmartBillManagerScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var bills by remember(userId) { mutableStateOf(KharcheeProModules11To20Store.getSmartBills(context, userId)) }
    var billTitle by remember { mutableStateOf("") }
    var billCat by remember { mutableStateOf("Electricity") }
    var billAmt by remember { mutableStateOf("1800") }
    var billDay by remember { mutableStateOf("10") }
    val periodKey = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }
    val colors = proFieldColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_13_bills_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 13 · SMART BILL MANAGER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Electricity", "Water", "Mobile", "Internet", "Rent", "Education", "Insurance").forEach { c ->
                val sel = billCat == c
                Surface(
                    color = if (sel) ObsidianDarkSilver.copy(alpha = 0.2f) else ObsidianDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { billCat = c }
                ) {
                    Text(c, fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = billTitle, onValueChange = { billTitle = it }, label = { Text("Bill Name") }, singleLine = true, colors = colors, modifier = Modifier.weight(1.2f))
            OutlinedTextField(value = billAmt, onValueChange = { billAmt = it.filter { c -> c.isDigit() } }, label = { Text("Expected ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
            OutlinedTextField(value = billDay, onValueChange = { billDay = it.filter { c -> c.isDigit() } }, label = { Text("Day") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.6f))
        }

        Button(
            onClick = {
                val amt = billAmt.toLongOrNull() ?: 0L
                val day = billDay.toIntOrNull()?.coerceIn(1, 31) ?: 10
                if (billTitle.isNotBlank() && amt > 0L) {
                    bills = KharcheeProModules11To20Store.saveSmartBill(
                        context, userId,
                        ProSmartBillRecord(title = billTitle.trim(), category = billCat, dueDayOfMonth = day, expectedAmount = amt)
                    )
                    billTitle = ""
                    onShowToast("✅ Added recurring bill")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) { Text("ADD SMART BILL", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }

        bills.forEach { b ->
            val paidThisPeriod = b.paidPeriods[periodKey]
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${b.title} (${b.category})", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 13.sp)
                    Text("Due Day ${b.dueDayOfMonth} · Expected ₹${b.expectedAmount} · ${if (paidThisPeriod != null) "PAID ✓ (₹$paidThisPeriod)" else "UNPAID"}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = if (paidThisPeriod != null) ObsidianDarkEmeraldAccent else ChampagneGold)
                }
                if (paidThisPeriod == null) {
                    OutlinedButton(onClick = {
                        bills = KharcheeProModules11To20Store.markSmartBillPaid(context, userId, b.id, periodKey, b.expectedAmount)
                        onShowToast("✓ Marked ${b.title} Paid for $periodKey")
                    }) { Text("MARK PAID", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent) }
                }
            }
        }
    }
}

@Composable
fun ProGoalIntelligenceCenterScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var goals by remember(userId) { mutableStateOf(KharcheeProModules11To20Store.getProGoals(context, userId)) }
    var nameInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("100000") }
    var startInput by remember { mutableStateOf("5000") }
    val colors = proFieldColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_14_goals_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 14 · GOAL INTELLIGENCE CENTER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Contribution-backed savings goals with reversal support (ledger tracked — does not move bank funds)", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Goal Name") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = targetInput, onValueChange = { targetInput = it.filter { c -> c.isDigit() } }, label = { Text("Target ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
            OutlinedTextField(value = startInput, onValueChange = { startInput = it.filter { c -> c.isDigit() } }, label = { Text("Start ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
        }
        Button(
            onClick = {
                val t = targetInput.toLongOrNull() ?: 0L
                val s = startInput.toLongOrNull() ?: 0L
                if (nameInput.isNotBlank() && t > 0L) {
                    goals = KharcheeProModules11To20Store.createProGoal(context, userId, nameInput, "Pro Goal", "Wealth", "High", t, "31 Dec 2027", s)
                    nameInput = ""
                    onShowToast("🎯 Created Pro Goal")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) { Text("CREATE PRO GOAL", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }

        goals.forEach { g ->
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ObsidianDarkBackground).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${g.name} [${g.status}]", fontWeight = FontWeight.Bold, color = WarmIvory)
                    Text("₹${g.savedAmount} / ₹${g.targetAmount} (${g.progressPct}%)", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ObsidianDarkEmeraldAccent)
                }
                LinearProgressIndicator(progress = { g.progressPct / 100f }, color = ObsidianDarkEmeraldAccent, trackColor = ObsidianDarkPrimarySurface, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        goals = KharcheeProModules11To20Store.addProGoalContribution(context, userId, g.id, 1000L, "Top-up")
                        onShowToast("+₹1,000 added to ${g.name}")
                    }) { Text("+₹1,000", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent) }
                    if (g.contributions.isNotEmpty()) {
                        OutlinedButton(onClick = {
                            val lastId = g.contributions.last().id
                            goals = KharcheeProModules11To20Store.editOrReverseGoalContribution(context, userId, g.id, lastId, 0L)
                            onShowToast("↩️ Reversed last contribution on ${g.name}")
                        }) { Text("REVERSE LAST", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby) }
                    }
                }
            }
        }
    }
}

@Composable
fun ProMoneyCalendarScreen(
    userId: String,
    expenses: List<ExpenseRecord>
) {
    val context = LocalContext.current
    val calNow = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(calNow.get(Calendar.YEAR)) }
    var monthZero by remember { mutableIntStateOf(calNow.get(Calendar.MONTH)) }
    var filterType by remember { mutableStateOf("ALL") }

    val entries = remember(userId, year, monthZero, expenses, filterType) {
        KharcheeProModules11To20Store.buildProCalendarEntriesForMonth(context, userId, year, monthZero, expenses, filterType)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_15_calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("PRO MODULE 15 · MONEY CALENDAR PRO ($year-${monthZero + 1})", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = {
                    if (monthZero == 0) { monthZero = 11; year-- } else monthZero--
                }) { Text("◀", fontSize = 10.sp, color = ObsidianDarkSilver) }
                OutlinedButton(onClick = {
                    if (monthZero == 11) { monthZero = 0; year++ } else monthZero++
                }) { Text("▶", fontSize = 10.sp, color = ObsidianDarkSilver) }
            }
        }

        entries.forEach { e ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${e.dateKey} · ${e.title}", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                    Text("${e.entryType} · Status: ${e.status} · ${e.details}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver)
                }
                Text("₹${e.amount}", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (e.status == "COMPLETED") ObsidianDarkEmeraldAccent else ChampagneGold)
            }
        }
    }
}
