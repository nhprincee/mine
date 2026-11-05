package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmiItem
import com.example.data.model.ExpenseRecord
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.KhataFriendRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.SubscriptionItem
import com.example.data.model.isIncomeTransaction
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
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
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceToolsBottomSheet(
    userId: String,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    khataFriends: List<KhataFriendRecord>,
    monthSpent: Long,
    onDismiss: () -> Unit,
    onLogTransaction: (title: String, category: String, amount: Long, paymentMode: String, type: String, note: String) -> Unit,
    onOpenReceiptScanner: () -> Unit,
    onOpenBillReminders: () -> Unit,
    onOpenKhataTab: () -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var activeSection by remember { mutableStateOf("voice") } // "voice", "subscriptions", "emi", "wallets", "emergency"

    var subscriptions by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getSubscriptions(context, userId))
    }
    var emis by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getEmis(context, userId))
    }
    var manualWallets by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getManualWallets(context, userId))
    }
    var emergencySaved by remember(userId) {
        mutableStateOf(KharcheeProfileAndToolsStore.getEmergencyFundSaved(context, userId))
    }

    // Voice Expense Entry State
    var spokenTranscript by remember { mutableStateOf("") }
    var voiceParsedTitle by remember { mutableStateOf("") }
    var voiceParsedAmount by remember { mutableStateOf("") }
    var voiceParsedCategory by remember { mutableStateOf("Food") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val best = matches?.firstOrNull().orEmpty()
            if (best.isNotBlank()) {
                spokenTranscript = best
                val parsed = parseSpokenExpensePhrase(best)
                voiceParsedTitle = parsed.first
                voiceParsedAmount = if (parsed.second > 0L) parsed.second.toString() else ""
                voiceParsedCategory = parsed.third
            }
        }
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RichGraphite,
        contentColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag("finance_tools_hub_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KHARCHEE WEALTH & FINANCE SUITE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.4.sp,
                        color = ChampagneGold
                    )
                    Text(
                        text = "Subscriptions, EMIs, Wallets & Voice",
                        fontFamily = headingFont,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedPlatinum)
                }
            }

            // Tool Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "voice" to "🎙️ Voice Entry",
                    "subscriptions" to "🔄 Subscriptions",
                    "emi" to "🏦 EMI Tracker",
                    "wallets" to "💼 Net Worth & Wallets",
                    "emergency" to "🛡️ Emergency Fund"
                ).forEach { (key, label) ->
                    val selected = activeSection == key
                    Surface(
                        color = if (selected) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(
                                0.8.dp,
                                if (selected) ChampagneGold else HairlinePlatinum,
                                RoundedCornerShape(50)
                            )
                            .clickable { activeSection = key }
                            .testTag("finance_tool_tab_$key")
                    ) {
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) ChampagneGold else MutedPlatinum,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Quick Links for Receipt Scanner, Bill Reminders, Split & Khata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenReceiptScanner()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, HairlinePlatinum),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt/SMS", fontSize = 10.sp, color = WarmIvory)
                }
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenBillReminders()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, HairlinePlatinum),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bill Alerts", fontSize = 10.sp, color = WarmIvory)
                }
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenKhataTab()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, HairlinePlatinum),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Split & Khata", fontSize = 10.sp, color = WarmIvory)
                }
            }

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            when (activeSection) {
                "voice" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "VOICE EXPENSE ENTRY",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChampagneGold
                                )
                                Text(
                                    text = "Speak naturally (e.g. \"Spent 150 on Chai\" or \"Paid 450 for Zomato\")",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedPlatinum,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your expense (e.g. 'Spent 120 on Chai')")
                                        }
                                        speechLauncher.launch(intent)
                                    } catch (_: Exception) {
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampagneGold,
                                    contentColor = ObsidianBlack
                                ),
                                modifier = Modifier.testTag("start_voice_expense_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Speak Expense", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Speak", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }

                        OutlinedTextField(
                            value = spokenTranscript,
                            onValueChange = {
                                spokenTranscript = it
                                val parsed = parseSpokenExpensePhrase(it)
                                voiceParsedTitle = parsed.first
                                voiceParsedAmount = if (parsed.second > 0L) parsed.second.toString() else ""
                                voiceParsedCategory = parsed.third
                            },
                            label = { Text("Spoken or Typed Phrase (e.g. Paid 220 for Uber auto)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voice_phrase_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = voiceParsedTitle,
                                onValueChange = { voiceParsedTitle = it },
                                label = { Text("Parsed Merchant") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = voiceParsedAmount,
                                onValueChange = { voiceParsedAmount = it },
                                label = { Text("Amount (₹)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val amt = voiceParsedAmount.toLongOrNull() ?: 0L
                                if (amt > 0L) {
                                    onLogTransaction(
                                        voiceParsedTitle.ifBlank { voiceParsedCategory },
                                        voiceParsedCategory,
                                        amt,
                                        "UPI",
                                        "expense",
                                        "Voice Entry · MANUAL VERIFIED"
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = (voiceParsedAmount.toLongOrNull() ?: 0L) > 0L,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedEmerald,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_voice_expense_button")
                        ) {
                            Text(
                                text = "Log Voice Expense (${voiceParsedCategory})",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                "subscriptions" -> {
                    var subName by remember { mutableStateOf("") }
                    var subAmount by remember { mutableStateOf("") }
                    var subRenewal by remember { mutableStateOf("05 Nov") }

                    val monthlySubTotal = subscriptions.sumOf {
                        if (it.billingCycle == "Yearly") it.amount / 12L else it.amount
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RECURRING SUBSCRIPTION MANAGER",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChampagneGold
                                )
                                Text(
                                    text = "MANUAL ENTRY · Monthly Burn: ₹$monthlySubTotal/mo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedPlatinum
                                )
                            }
                            Icon(Icons.Default.Autorenew, contentDescription = null, tint = ChampagneGold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = subName,
                                onValueChange = { subName = it },
                                label = { Text("Service (e.g. Netflix)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = subAmount,
                                onValueChange = { subAmount = it },
                                label = { Text("₹/mo") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = fieldColors,
                                modifier = Modifier.weight(0.8f)
                            )
                            OutlinedTextField(
                                value = subRenewal,
                                onValueChange = { subRenewal = it },
                                label = { Text("Due") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Button(
                            onClick = {
                                val amt = subAmount.toLongOrNull() ?: 0L
                                if (subName.isNotBlank() && amt > 0L) {
                                    subscriptions = KharcheeProfileAndToolsStore.addSubscription(
                                        context = context,
                                        userId = userId,
                                        item = SubscriptionItem(
                                            id = UUID.randomUUID().toString(),
                                            name = subName.trim(),
                                            amount = amt,
                                            billingCycle = "Monthly",
                                            nextRenewalDate = subRenewal.ifBlank { "Monthly" }
                                        )
                                    )
                                    subName = ""
                                    subAmount = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Subscription", fontWeight = FontWeight.SemiBold)
                        }

                        if (subscriptions.isEmpty()) {
                            Text(
                                text = "No active subscriptions tracked yet. Add OTT, cloud, gym, or SaaS memberships above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedIvory
                            )
                        } else {
                            subscriptions.forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElevatedGraphite)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sub.name, fontWeight = FontWeight.SemiBold, color = WarmIvory, fontSize = 13.sp)
                                        Text(
                                            "₹${sub.amount}/mo · Renews ${sub.nextRenewalDate} · MANUAL",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = MutedPlatinum
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = ChampagneGold.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable {
                                                onLogTransaction(
                                                    "${sub.name} Subscription",
                                                    sub.category,
                                                    sub.amount,
                                                    "UPI",
                                                    "expense",
                                                    "Recurring Subscription (${sub.billingCycle})"
                                                )
                                                onDismiss()
                                            }
                                        ) {
                                            Text(
                                                text = "Log Paid",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ChampagneGold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                subscriptions = KharcheeProfileAndToolsStore.removeSubscription(context, userId, sub.id)
                                            }
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = CrimsonRuby, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "emi" -> {
                    var emiTitle by remember { mutableStateOf("") }
                    var emiMonthly by remember { mutableStateOf("") }
                    var emiMonthsLeft by remember { mutableStateOf("6") }

                    val totalMonthlyEmi = emis.sumOf { it.monthlyEmi }
                    val totalRemainingDebt = emis.sumOf { it.monthlyEmi * it.remainingMonths }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "EMI & LOAN OBLIGATION TRACKER (MANUAL ENTRY)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChampagneGold
                        )
                        Text(
                            text = "Monthly EMI Commitment: ₹$totalMonthlyEmi/mo · Remaining Balance: ₹$totalRemainingDebt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedPlatinum
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = emiTitle,
                                onValueChange = { emiTitle = it },
                                label = { Text("Loan / Item") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = emiMonthly,
                                onValueChange = { emiMonthly = it },
                                label = { Text("EMI (₹)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = fieldColors,
                                modifier = Modifier.weight(0.9f)
                            )
                            OutlinedTextField(
                                value = emiMonthsLeft,
                                onValueChange = { emiMonthsLeft = it },
                                label = { Text("Months") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = fieldColors,
                                modifier = Modifier.weight(0.7f)
                            )
                        }

                        Button(
                            onClick = {
                                val monthly = emiMonthly.toLongOrNull() ?: 0L
                                val months = emiMonthsLeft.toIntOrNull()?.coerceIn(1, 360) ?: 6
                                if (emiTitle.isNotBlank() && monthly > 0L) {
                                    emis = KharcheeProfileAndToolsStore.addEmi(
                                        context = context,
                                        userId = userId,
                                        item = EmiItem(
                                            id = UUID.randomUUID().toString(),
                                            title = emiTitle.trim(),
                                            monthlyEmi = monthly,
                                            remainingMonths = months,
                                            totalPrincipal = monthly * months
                                        )
                                    )
                                    emiTitle = ""
                                    emiMonthly = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add EMI Schedule", fontWeight = FontWeight.SemiBold)
                        }

                        emis.forEach { emi ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElevatedGraphite)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(emi.title, fontWeight = FontWeight.SemiBold, color = WarmIvory, fontSize = 13.sp)
                                    Text(
                                        "₹${emi.monthlyEmi}/mo · ${emi.remainingMonths} months left · Total ₹${emi.monthlyEmi * emi.remainingMonths}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = MutedPlatinum
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = ChampagneGold.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable {
                                            onLogTransaction(
                                                "${emi.title} EMI",
                                                "Bills",
                                                emi.monthlyEmi,
                                                "UPI",
                                                "expense",
                                                "EMI Installment (${emi.remainingMonths}m remaining)"
                                            )
                                            onDismiss()
                                        }
                                    ) {
                                        Text(
                                            text = "Record EMI",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ChampagneGold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            emis = KharcheeProfileAndToolsStore.removeEmi(context, userId, emi.id)
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove EMI", tint = CrimsonRuby, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                "wallets" -> {
                    val ledgerIncome = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
                    val ledgerExpense = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
                    val ledgerNet = ledgerIncome - ledgerExpense
                    val goalsSaved = savingsGoals.sumOf { it.savedAmount }
                    val khataNet = khataFriends.sumOf { it.balance }
                    val manualWalletSum = manualWallets.sumOf { it.manualBalance }
                    val emiLiability = emis.sumOf { it.monthlyEmi * it.remainingMonths }
                    val computedNetWorth = ledgerNet + goalsSaved + khataNet + manualWalletSum - emiLiability

                    var selectedWalletKey by remember { mutableStateOf("UPI") }
                    var walletAmountInput by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "MULTI-WALLET & NET WORTH TRACKER",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChampagneGold
                        )
                        Text(
                            text = "Estimated Net Worth: ₹$computedNetWorth",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Calculated from Passbook Cashflow (₹$ledgerNet) + Goal Vaults (₹$goalsSaved) + Khata Net (₹$khataNet) + Manual Wallets (₹$manualWalletSum) - EMI Liabilities (₹$emiLiability)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedPlatinum,
                            fontSize = 11.sp
                        )

                        // Breakdown by Payment Mode from actual transactions + manual reserve
                        listOf("UPI", "Bank", "Cash", "Card").forEachIndexed { idx, key ->
                            val txDelta = expenses.filter { it.paymentMode.equals(key, ignoreCase = true) }
                                .sumOf { if (isIncomeTransaction(it)) it.amount else -it.amount }
                            val manualVal = manualWallets.getOrNull(idx)?.manualBalance ?: 0L
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElevatedGraphite)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("$key Wallet", fontWeight = FontWeight.SemiBold, color = WarmIvory, fontSize = 13.sp)
                                    Text(
                                        "Passbook Net: ₹$txDelta · Manual Reserve: ₹$manualVal (MANUAL ENTRY)",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = MutedPlatinum
                                    )
                                }
                                Text(
                                    text = "₹${txDelta + manualVal}",
                                    fontFamily = headingFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = ChampagneGold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            listOf("UPI", "Bank", "Cash", "Card").forEach { wKey ->
                                val active = selectedWalletKey == wKey
                                Surface(
                                    color = if (active) ChampagneGold.copy(alpha = 0.16f) else ElevatedGraphite,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                                        .clickable { selectedWalletKey = wKey }
                                ) {
                                    Text(
                                        text = wKey,
                                        fontSize = 11.sp,
                                        color = if (active) ChampagneGold else MutedPlatinum,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = walletAmountInput,
                                onValueChange = { walletAmountInput = it },
                                label = { Text("Set $selectedWalletKey Manual Balance (₹)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val amt = walletAmountInput.toLongOrNull() ?: return@Button
                                    KharcheeProfileAndToolsStore.updateManualWallet(context, userId, selectedWalletKey, amt)
                                    manualWallets = KharcheeProfileAndToolsStore.getManualWallets(context, userId)
                                    walletAmountInput = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack)
                            ) {
                                Text("Update", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                "emergency" -> {
                    val baselineMonthlyBurn = max(5000L, monthSpent)
                    val target3Months = baselineMonthlyBurn * 3L
                    val target6Months = baselineMonthlyBurn * 6L
                    val progress = min(1f, emergencySaved.toFloat() / target6Months.toFloat().coerceAtLeast(1f))
                    var emergencyInput by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = SophisticatedEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "EMERGENCY FUND PLANNER",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SophisticatedEmerald
                                )
                                Text(
                                    text = "Based on ₹$baselineMonthlyBurn/mo actual expense velocity",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedPlatinum
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("3-MONTH SAFETY NET", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedIvory)
                                Text("₹$target3Months", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = WarmIvory)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("6-MONTH FORTRESS", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedIvory)
                                Text("₹$target6Months", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ChampagneGold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("SAVED (MANUAL)", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedIvory)
                                Text("₹$emergencySaved", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SophisticatedEmerald)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            color = SophisticatedEmerald,
                            trackColor = ElevatedGraphite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = emergencyInput,
                                onValueChange = { emergencyInput = it },
                                label = { Text("Update Emergency Reserve Saved (₹)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val amt = emergencyInput.toLongOrNull() ?: return@Button
                                    KharcheeProfileAndToolsStore.setEmergencyFundSaved(context, userId, amt)
                                    emergencySaved = amt
                                    emergencyInput = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = ObsidianBlack)
                            ) {
                                Text("Save", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

internal fun parseSpokenExpensePhrase(phrase: String): Triple<String, Long, String> {
    val clean = phrase.trim()
    val numberRegex = Regex("(\\d+(?:,\\d+)*)")
    val amount = numberRegex.find(clean)?.groupValues?.get(1)?.replace(",", "")?.toLongOrNull() ?: 0L
    val lower = clean.lowercase()
    val category = when {
        lower.contains("chai") || lower.contains("coffee") || lower.contains("tea") || lower.contains("snack") -> "Chai & Snacks"
        lower.contains("uber") || lower.contains("ola") || lower.contains("petrol") || lower.contains("metro") || lower.contains("auto") -> "Transport"
        lower.contains("grocery") || lower.contains("milk") || lower.contains("blinkit") || lower.contains("zepto") -> "Grocery"
        lower.contains("movie") || lower.contains("netflix") || lower.contains("game") -> "Entertainment"
        lower.contains("bill") || lower.contains("wifi") || lower.contains("rent") || lower.contains("recharge") -> "Bills"
        lower.contains("amazon") || lower.contains("myntra") || lower.contains("shopping") -> "Shopping"
        else -> "Food"
    }
    val title = clean
        .replace(Regex("(?i)\\b(spent|paid|rs|rupees|inr|for|on|via|upi|cash)\\b"), " ")
        .replace(numberRegex, " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .ifBlank { category }
    return Triple(title, amount, category)
}
