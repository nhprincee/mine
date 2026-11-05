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
import com.example.data.model.KharcheeProModules11To20Store
import com.example.data.model.ProSmartMoneyRule
import com.example.data.model.ProSplitExpenseRecord
import com.example.data.model.ProSplitParticipant
import com.example.data.model.ProSplitSettlementRecord
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
private fun proFieldColors16To20() = OutlinedTextFieldDefaults.colors(
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
fun ProSmartMoneyRulesScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var rules by remember(userId) { mutableStateOf(KharcheeProModules11To20Store.getSmartRules(context, userId)) }
    var ruleName by remember { mutableStateOf("Auto-Tag Zomato/Swiggy") }
    var criteria by remember { mutableStateOf("Zomato") }
    var actionVal by remember { mutableStateOf("Food") }
    val colors = proFieldColors16To20()

    val suggestions = remember(rules, expenses) {
        KharcheeProModules11To20Store.evaluateSmartRulesOnTransactions(rules, expenses)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_16_rules_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 16 · SMART MONEY RULES", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Deterministic rule suggestions — never silently alters existing transactions without confirmation", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = ruleName, onValueChange = { ruleName = it }, label = { Text("Rule Name") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = criteria, onValueChange = { criteria = it }, label = { Text("Merchant Match") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = actionVal, onValueChange = { actionVal = it }, label = { Text("Suggest Category") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
        }
        Button(
            onClick = {
                val res = KharcheeProModules11To20Store.saveSmartRule(
                    context, userId,
                    ProSmartMoneyRule(name = ruleName, triggerType = "MERCHANT_CONTAINS", matchingCriteria = criteria, suggestedActionType = "SUGGEST_CATEGORY", suggestedActionValue = actionVal)
                )
                res.onSuccess {
                    rules = it
                    onShowToast("✅ Saved Smart Money Rule")
                }.onFailure { err ->
                    onShowToast("⚠️ ${err.message}")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) { Text("SAVE RULE", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }

        rules.forEach { r ->
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${r.name} (Priority ${r.priority})", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                    Text("If ${r.triggerType} '${r.matchingCriteria}' → ${r.suggestedActionType}: ${r.suggestedActionValue}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkEmeraldAccent)
                }
                Switch(
                    checked = r.enabled,
                    onCheckedChange = { en ->
                        KharcheeProModules11To20Store.saveSmartRule(context, userId, r.copy(enabled = en)).onSuccess { rules = it }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = ObsidianDarkEmeraldAccent)
                )
            }
        }

        if (suggestions.isNotEmpty()) {
            Text("PENDING CONFIRMATION SUGGESTIONS (${suggestions.size})", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ChampagneGold)
            suggestions.take(4).forEach { s ->
                Text("• '${s.transactionTitle}' matched '${s.ruleName}' → Suggest ${s.suggestedActionValue} (Awaiting Confirmation)", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
            }
        }
    }
}

@Composable
fun ProFinancialDocumentVaultScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var docs by remember(userId) { mutableStateOf(KharcheeProModules11To20Store.getVaultDocuments(context, userId)) }
    var fileName by remember { mutableStateOf("invoice_nov2026.pdf") }
    var category by remember { mutableStateOf("Invoices") }
    var notes by remember { mutableStateOf("Warranty & GST invoice") }
    val colors = proFieldColors16To20()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_17_doc_vault_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 17 · FINANCIAL DOCUMENT VAULT", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Private user-isolated document registry (PDF/JPG/PNG/WEBP ≤ 10 MB). Manual entry — never auto-creates transactions without confirmation.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = fileName, onValueChange = { fileName = it }, label = { Text("File Name (.pdf/.jpg/.png)") }, singleLine = true, colors = colors, modifier = Modifier.weight(1.2f))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
        }
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Document Notes / Tags") }, singleLine = true, colors = colors, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val mime = if (fileName.endsWith(".pdf", true)) "application/pdf" else "image/jpeg"
                val txId = expenses.firstOrNull()?.id ?: ""
                val res = KharcheeProModules11To20Store.uploadDocumentMetadata(
                    context = context,
                    requestingUserId = userId,
                    fileName = fileName,
                    mimeType = mime,
                    fileSizeBytes = 245_000L,
                    category = category,
                    tags = listOf(category.lowercase()),
                    attachedTransactionId = txId,
                    notes = notes
                )
                res.onSuccess {
                    docs = KharcheeProModules11To20Store.getVaultDocuments(context, userId)
                    onShowToast("🔒 Stored document privately in Vault")
                }.onFailure { err ->
                    onShowToast("⚠️ ${err.message}")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) { Text("STORE PRIVATE DOCUMENT RECORD", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }

        docs.forEach { d ->
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${d.fileName} [${d.category}]", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                    Text("${d.notes} · Linked Tx: ${d.attachedTransactionId.ifBlank { "None" }}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver)
                }
                OutlinedButton(onClick = {
                    docs = KharcheeProModules11To20Store.deleteVaultDocument(context, userId, d.id)
                    onShowToast("Deleted document")
                }) { Text("DELETE", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = CrimsonRuby) }
            }
        }
    }
}

@Composable
fun ProAdvancedSplitExpenseManagerScreen(
    userDisplayName: String,
    onShowToast: (String) -> Unit
) {
    val participants = remember(userDisplayName) {
        listOf(
            ProSplitParticipant(id = "p1", name = userDisplayName),
            ProSplitParticipant(id = "p2", name = "Aarav"),
            ProSplitParticipant(id = "p3", name = "Rohan")
        )
    }
    var splitExpenses by remember { mutableStateOf(listOf<ProSplitExpenseRecord>()) }
    var settlements by remember { mutableStateOf(listOf<ProSplitSettlementRecord>()) }
    var descInput by remember { mutableStateOf("Team Dinner") }
    var amountInput by remember { mutableStateOf("1500") }
    var splitMode by remember { mutableStateOf("EQUAL") }
    val colors = proFieldColors16To20()

    val netBalances = remember(participants, splitExpenses, settlements) {
        KharcheeProModules11To20Store.calculateSplitNetBalances(participants, splitExpenses, settlements)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_18_split_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 18 · ADVANCED SPLIT EXPENSE MANAGER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Exact share reconciliation (Equal, Custom, Percentage) — private wallet balances are never exposed", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = descInput, onValueChange = { descInput = it }, label = { Text("Shared Expense") }, singleLine = true, colors = colors, modifier = Modifier.weight(1.2f))
            OutlinedTextField(value = amountInput, onValueChange = { amountInput = it.filter { c -> c.isDigit() } }, label = { Text("Total ₹") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.8f))
        }

        Button(
            onClick = {
                val total = amountInput.toLongOrNull() ?: 0L
                val sharesRes = KharcheeProModules11To20Store.computeSplitShares(total, participants.map { it.id }, splitMode)
                sharesRes.onSuccess { shares ->
                    splitExpenses = listOf(
                        ProSplitExpenseRecord(description = descInput, totalAmount = total, paidByParticipantId = "p1", splitType = splitMode, sharesByParticipantId = shares)
                    ) + splitExpenses
                    onShowToast("✅ Split ₹$total (${shares.values.sum()} reconciled)")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkEmeraldAccent, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) { Text("RECORD EQUAL SPLIT EXPENSE", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }

        participants.forEach { p ->
            val bal = netBalances[p.id] ?: 0L
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p.name, color = WarmIvory, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (bal >= 0L) "Gets back +₹$bal" else "Owes -₹${-bal}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = if (bal >= 0L) ObsidianDarkEmeraldAccent else CrimsonRuby
                )
            }
        }
    }
}

@Composable
fun ProMultiCurrencyIntelligenceScreen(
    userId: String,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var fxTxs by remember(userId) { mutableStateOf(KharcheeProModules11To20Store.getMultiCurrencyTransactions(context, userId)) }
    var titleInput by remember { mutableStateOf("Flight / Hotel Booking") }
    var origAmtInput by remember { mutableStateOf("100") }
    var currencyInput by remember { mutableStateOf("USD") }
    var rateInput by remember { mutableStateOf("84.5") }
    val colors = proFieldColors16To20()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_19_multicurrency_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 19 · MULTI-CURRENCY INTELLIGENCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Stores original amount, currency, and locked manual conversion rate — historical records never mutate when rates change.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = titleInput, onValueChange = { titleInput = it }, label = { Text("Description") }, singleLine = true, colors = colors, modifier = Modifier.weight(1.2f))
            OutlinedTextField(value = currencyInput, onValueChange = { currencyInput = it }, label = { Text("Currency") }, singleLine = true, colors = colors, modifier = Modifier.weight(0.6f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = origAmtInput, onValueChange = { origAmtInput = it }, label = { Text("Foreign Amount") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
            OutlinedTextField(value = rateInput, onValueChange = { rateInput = it }, label = { Text("Manual Rate to INR") }, singleLine = true, colors = colors, modifier = Modifier.weight(1f))
        }

        Button(
            onClick = {
                val orig = origAmtInput.toDoubleOrNull() ?: 0.0
                val rate = rateInput.toDoubleOrNull() ?: 0.0
                KharcheeProModules11To20Store.recordMultiCurrencyTransaction(context, userId, titleInput, "Travel", orig, currencyInput, rate)
                    .onSuccess {
                        fxTxs = KharcheeProModules11To20Store.getMultiCurrencyTransactions(context, userId)
                        onShowToast("🌍 Logged ${it.originalAmount} ${it.currencyCode} = ₹${it.convertedBaseAmount}")
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianDarkSilver, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth()
        ) { Text("RECORD FOREIGN CURRENCY EXPENSE", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp) }

        fxTxs.forEach { tx ->
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ObsidianDarkBackground).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${tx.title} (${tx.originalAmount} ${tx.currencyCode})", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                    Text(tx.rateSourceLabel, fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ObsidianDarkSilver)
                }
                Text("₹${tx.convertedBaseAmount}", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ObsidianDarkEmeraldAccent)
            }
        }
    }
}

@Composable
fun ProNotificationIntelligenceScreen(
    userId: String,
    expenses: List<ExpenseRecord>,
    dailyLimit: Long,
    monthlyBudget: Long,
    onOpenModule: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val notifications = remember(userId, expenses, dailyLimit, monthlyBudget, refreshKey) {
        KharcheeProModules11To20Store.buildProNotifications(context, userId, expenses, dailyLimit, monthlyBudget)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianDarkPrimarySurface)
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("pro_module_20_notifications_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PRO MODULE 20 · PRO NOTIFICATION INTELLIGENCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ObsidianDarkSilver)
        Text("Deduplicated in-app notification center with quiet hours (22:00–07:00) & deep links. Cancelled bills never generate reminders.", style = MaterialTheme.typography.bodySmall, color = MutedIvory)

        if (notifications.isEmpty()) {
            Text("No active notifications. Add an upcoming bill, loan EMI, or savings milestone to receive intelligent alerts.", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
        } else {
            notifications.forEach { n ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianDarkBackground)
                        .border(0.8.dp, if (n.isRead) ObsidianDarkBorder else ChampagneGold, RoundedCornerShape(12.dp))
                        .clickable {
                            KharcheeProModules11To20Store.markNotificationRead(context, userId, n.id)
                            refreshKey++
                            onOpenModule(n.deepLinkModuleId)
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${n.title} [${n.priority}]", fontWeight = FontWeight.Bold, color = WarmIvory, fontSize = 12.sp)
                        Text(n.message, style = MaterialTheme.typography.bodySmall, color = MutedIvory)
                    }
                    Text(if (n.isRead) "READ" else "OPEN ▶", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = if (n.isRead) MutedPlatinum else ObsidianDarkEmeraldAccent)
                }
            }
        }
    }
}
