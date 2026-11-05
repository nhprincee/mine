package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.data.model.INCOME_CATEGORIES
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PAYMENT_MODES
import com.example.data.model.SAMPLE_BANK_SMS_LIST
import com.example.data.model.SmsTransactionParser
import com.example.data.model.isIncomeTransaction
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
import com.example.ui.theme.getCategoryIcon

private val SUBCATEGORIES_BY_CATEGORY = mapOf(
    "Food" to listOf("Dining Out", "Delivery", "Lunch", "Dinner"),
    "Chai & Snacks" to listOf("Tapri Chai", "Coffee", "Bakery", "Evening Snack"),
    "Transport" to listOf("Fuel / Petrol", "Metro", "Auto / Cab", "Parking"),
    "Grocery" to listOf("Daily Milk & Bread", "Vegetables", "Supermarket", "Quick Commerce"),
    "Entertainment" to listOf("Movies", "OTT Subscription", "Sports / Turf", "Gaming"),
    "Bills" to listOf("WiFi / Broadband", "Electricity", "Mobile Recharge", "Rent / EMI"),
    "Shopping" to listOf("Apparel", "Electronics", "Personal Care", "Home"),
    "Salary" to listOf("Monthly Payroll", "Bonus", "Stipend"),
    "Freelance" to listOf("Consulting", "Design / Dev", "Project Milestone"),
    "Cashback" to listOf("UPI Reward", "Card Cashback", "Refund")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    initialType: String = "expense",
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, amount: Long, paymentMode: String, type: String, note: String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var txType by remember { mutableStateOf(initialType) }
    val categories = if (txType == "income") INCOME_CATEGORIES else EXPENSE_CATEGORIES
    var selectedCategory by remember(txType) { mutableStateOf(categories.first()) }
    val subcategories = SUBCATEGORIES_BY_CATEGORY[selectedCategory] ?: listOf("General")
    var selectedSubcategory by remember(selectedCategory) { mutableStateOf(subcategories.first()) }
    var selectedMode by remember { mutableStateOf("UPI") }
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var attachedReceiptUri by remember { mutableStateOf<String?>(null) }

    val receiptPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedReceiptUri = uri.lastPathSegment ?: "Receipt_Attached"
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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (txType == "income") "CAPITAL INFLOW · MANUAL ENTRY" else "LEDGER OUTFLOW · MANUAL ENTRY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = if (txType == "income") SophisticatedEmerald else ChampagneGold
                    )
                    Text(
                        text = if (txType == "income") "Record Income Credit" else "Record Expense Entry",
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

            // Type Toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("expense" to "Expense (-₹)", "income" to "Income (+₹)").forEach { (typeKey, label) ->
                    val active = txType == typeKey
                    val accent = if (typeKey == "income") SophisticatedEmerald else ChampagneGold
                    Surface(
                        color = if (active) accent.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, if (active) accent else HairlinePlatinum, RoundedCornerShape(12.dp))
                            .clickable { txType = typeKey }
                    ) {
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (active) accent else MutedPlatinum,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount in ₹") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("modal_amount_input")
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (txType == "income") "Source (e.g. Monthly Salary)" else "Merchant / Item (e.g. Tapri Chai)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().testTag("modal_title_input")
            )

            Text(
                text = "CATEGORY & SUBCATEGORY",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = MutedPlatinum
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val active = selectedCategory == cat
                    Surface(
                        color = if (active) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(50))
                            .clickable { selectedCategory = cat }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = cat,
                                tint = if (active) ChampagneGold else MutedPlatinum,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                color = if (active) WarmIvory else MutedPlatinum,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Subcategory chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subcategories.forEach { sub ->
                    val active = selectedSubcategory == sub
                    Surface(
                        color = if (active) ElevatedGraphite else ObsidianBlack,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.6.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(50))
                            .clickable { selectedSubcategory = sub }
                    ) {
                        Text(
                            text = sub,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (active) ChampagneGold else MutedIvory,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Text(
                text = "PAYMENT CHANNEL / WALLET",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = MutedPlatinum
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (PAYMENT_MODES + listOf("Bank")).forEach { mode ->
                    val active = selectedMode == mode
                    Surface(
                        color = if (active) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { selectedMode = mode }
                    ) {
                        Text(
                            text = mode,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) ChampagneGold else MutedPlatinum,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Recurring & Receipt Attachment Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isRecurring = !isRecurring }
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ChampagneGold,
                            checkmarkColor = ObsidianBlack
                        )
                    )
                    Icon(Icons.Default.Autorenew, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recurring Monthly", fontSize = 12.sp, color = WarmIvory)
                }

                OutlinedButton(
                    onClick = {
                        receiptPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, if (attachedReceiptUri != null) SophisticatedEmerald else HairlinePlatinum),
                    modifier = Modifier.testTag("attach_receipt_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Receipt",
                        tint = if (attachedReceiptUri != null) SophisticatedEmerald else MutedPlatinum,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (attachedReceiptUri != null) "Receipt Attached" else "Attach Receipt",
                        fontSize = 11.sp,
                        color = if (attachedReceiptUri != null) SophisticatedEmerald else WarmIvory
                    )
                }
            }

            Button(
                onClick = {
                    val amt = amountStr.toLongOrNull() ?: 0L
                    if (amt > 0L) {
                        val tags = buildList {
                            add("[$selectedSubcategory]")
                            if (isRecurring) add("[RECURRING]")
                            if (attachedReceiptUri != null) add("[RECEIPT:$attachedReceiptUri]")
                            if (note.isNotBlank()) add(note.trim())
                        }.joinToString(" ")
                        onSubmit(
                            title.ifBlank { selectedCategory },
                            selectedCategory,
                            amt,
                            selectedMode,
                            txType,
                            tags
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (txType == "income") SophisticatedEmerald else ChampagneGold,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("modal_submit_transaction_button")
            ) {
                Text(
                    text = if (txType == "income") "Save Income Credit" else "Log Spend & Evaluate Reserve",
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionBottomSheet(
    expense: ExpenseRecord,
    onDismiss: () -> Unit,
    onSaveUpdated: (ExpenseRecord) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val isInc = isIncomeTransaction(expense)
    var txType by remember(expense) { mutableStateOf(if (isInc) "income" else "expense") }
    val categories = if (txType == "income") INCOME_CATEGORIES else EXPENSE_CATEGORIES
    var selectedCategory by remember(expense, txType) {
        mutableStateOf(if (expense.category in categories) expense.category else categories.first())
    }
    var selectedMode by remember(expense) { mutableStateOf(expense.paymentMode.ifBlank { "UPI" }) }
    var title by remember(expense) { mutableStateOf(expense.title) }
    var amountStr by remember(expense) { mutableStateOf(expense.amount.toString()) }
    var note by remember(expense) { mutableStateOf(expense.note) }

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
                .testTag("edit_transaction_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EDIT PASSBOOK TRANSACTION",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = ChampagneGold
                    )
                    Text(
                        text = "Update Merchant, Amount or Note",
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

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Merchant / Title") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().testTag("edit_tx_title_input")
            )

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount (₹)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().testTag("edit_tx_amount_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val active = selectedCategory == cat
                    Surface(
                        color = if (active) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(50))
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            color = if (active) WarmIvory else MutedPlatinum,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (PAYMENT_MODES + listOf("Bank")).forEach { mode ->
                    val active = selectedMode == mode
                    Surface(
                        color = if (active) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { selectedMode = mode }
                    ) {
                        Text(
                            text = mode,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) ChampagneGold else MutedPlatinum,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Transaction Note / Subcategory / Receipt Tag") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().testTag("edit_tx_note_input")
            )

            Button(
                onClick = {
                    val amt = amountStr.toLongOrNull() ?: expense.amount
                    if (amt > 0L) {
                        onSaveUpdated(
                            expense.copy(
                                title = title.trim().ifBlank { selectedCategory },
                                category = selectedCategory,
                                amount = amt,
                                paymentMode = selectedMode,
                                type = txType,
                                note = note.trim()
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_edited_tx_button")
            ) {
                Text("Save Updated Transaction", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsUpiParserBottomSheet(
    onDismiss: () -> Unit,
    onLogParsedTransaction: (title: String, category: String, amount: Long, paymentMode: String, rawNote: String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var rawSms by remember { mutableStateOf(SAMPLE_BANK_SMS_LIST.first().sms) }
    val parsed = remember(rawSms) { SmsTransactionParser.parse(rawSms) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RichGraphite,
        contentColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ON-DEVICE RECEIPT & UPI SMS SCANNER",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = ChampagneGold
            )
            Text(
                text = "Automatic Merchant & Telemetry Extraction",
                fontFamily = headingFont,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmIvory
            )
            Text(
                text = "100% on-device regex extraction for Google Pay, PhonePe, Paytm, BHIM, receipts & bank alerts. Automatically blocks OTPs & collect requests.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedIvory,
                fontSize = 12.sp
            )

            Text(
                text = "Tap a real Indian UPI / SMS sample to test:",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = ChampagneGold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SAMPLE_BANK_SMS_LIST.forEach { sample ->
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.8.dp, HairlineGold, RoundedCornerShape(50))
                            .clickable { rawSms = sample.sms }
                    ) {
                        Text(
                            text = sample.label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = WarmIvory,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = rawSms,
                onValueChange = { rawSms = it },
                label = { Text("Paste Receipt Text, Bank SMS or UPI Notification") },
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChampagneGold,
                    unfocusedBorderColor = HairlinePlatinum,
                    focusedContainerColor = ObsidianBlack,
                    unfocusedContainerColor = ObsidianBlack,
                    focusedTextColor = WarmIvory,
                    unfocusedTextColor = WarmIvory
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (parsed != null) {
                if (parsed.isIgnoredSecurityMessage) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CrimsonRuby.copy(alpha = 0.14f))
                            .border(0.8.dp, CrimsonRuby.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "🛡️ PRIVACY GUARD: IGNORED SENSITIVE / OTP MESSAGE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrimsonRuby
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kharchee never reads or logs OTPs, PIN prompts, or collect requests.",
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElevatedGraphite)
                            .border(0.8.dp, HairlineGold, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "EXTRACTED TELEMETRY (${parsed.confidence.uppercase()} CONFIDENCE)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChampagneGold
                        )
                        Text(
                            text = "Merchant: ${parsed.merchant} · ₹${parsed.amount}",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmIvory,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Category: ${parsed.category} · Mode: ${parsed.paymentMode}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedPlatinum
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                onLogParsedTransaction(
                                    parsed.merchant,
                                    parsed.category,
                                    parsed.amount,
                                    parsed.paymentMode,
                                    "Auto-parsed from UPI/SMS"
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                            modifier = Modifier.fillMaxWidth().testTag("log_parsed_sms_button")
                        ) {
                            Text("Log ₹${parsed.amount} & Trigger Accountability Check", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalBottomSheet(
    userId: String = "default",
    onDismiss: () -> Unit,
    onCreateGoal: (title: String, targetAmount: Long, savedAmount: Long) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("25000") }
    var savedStr by remember { mutableStateOf("2500") }
    var targetDateStr by remember { mutableStateOf("31 Dec 2026") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SophisticatedEmerald,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RichGraphite,
        contentColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "GOAL VAULT · CAPITAL ALLOCATION",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = SophisticatedEmerald
            )
            Text(
                text = "Establish Savings Goal & Target Date",
                fontFamily = headingFont,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmIvory
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal Name (e.g. Emergency Reserve / MacBook / Travel)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target (₹)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = savedStr,
                    onValueChange = { savedStr = it },
                    label = { Text("Initial Saved (₹)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = targetDateStr,
                onValueChange = { targetDateStr = it },
                label = { Text("Target Completion Date (e.g. 31 Dec 2026)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth().testTag("goal_target_date_input")
            )
            Button(
                onClick = {
                    val target = targetStr.toLongOrNull() ?: 10000L
                    val saved = savedStr.toLongOrNull() ?: 0L
                    val finalTitle = title.ifBlank { "Sovereign Reserve" }
                    KharcheeProfileAndToolsStore.saveGoalTargetDate(context, userId, finalTitle, targetDateStr)
                    onCreateGoal(finalTitle, target, saved)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = ObsidianBlack),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Create Capital Goal", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FakeMomCallOverlay(
    onEndCall: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var isConnected by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianBlack, RichGraphite, ObsidianBlack)
                )
            )
            .padding(32.dp)
            .testTag("fake_mom_call_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Surface(
                    color = ChampagneGold.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(0.8.dp, HairlineGold, RoundedCornerShape(50))
                ) {
                    Text(
                        text = if (isConnected) "CALL IN PROGRESS · 00:08" else "INCOMING VOICE CALL · EMERGENCY BILL ESCAPE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = ChampagneGold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(ElevatedGraphite)
                        .border(1.5.dp, ChampagneGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        fontFamily = headingFont,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChampagneGold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Mummy ❤️",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.displayMedium,
                    color = WarmIvory,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isConnected) {
                        "\"Beta, abhi ke abhi ghar aaja! फालतू खर्चा मत करना!\""
                    } else {
                        "Mobile · +91 98••• •••10"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedPlatinum,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isConnected) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { isConnected = true },
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(SophisticatedEmerald)
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Answer Mummy Call",
                                tint = ObsidianBlack,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Answer", color = SophisticatedEmerald, fontWeight = FontWeight.SemiBold)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(CrimsonRuby)
                            .testTag("end_mom_call_button")
                    ) {
                        Icon(
                            Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = WarmIvory,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Escape Split", color = CrimsonRuby, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
