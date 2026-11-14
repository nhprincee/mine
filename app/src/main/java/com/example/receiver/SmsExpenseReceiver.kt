package com.example.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ExpenseRecord
import com.example.data.model.ParsedSmsTransaction
import com.example.data.repository.KharcheeRepository
import com.example.ui.auth.IntroAuthPreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Real-Time Incoming Transactional SMS BroadcastReceiver for Indian Banks & UPI Apps.
 *
 * - Listens for `android.provider.Telephony.SMS_RECEIVED` via `Telephony.Sms.Intents.getMessagesFromIntent(intent)`.
 * - Filters sender IDs from Indian banks & UPI handles (HDFCBK, SBIINB, AXISBK, ICICIB, PAYTM, VPA, CRED, JIOFBR, etc.).
 * - Extracts Amount, Merchant/Entity, and Debit/Spent transaction type while strictly ignoring OTPs and salary credits.
 * - Inserts directly into the local + cloud expense repository via coroutine and fires an Android Heads-Up Notification
 *   with a 1-tap Undo action.
 */
class SmsExpenseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        if (action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract SMS messages from intent", e)
            null
        }
        if (messages.isNullOrEmpty()) return

        val senderAddress = messages.firstOrNull()?.displayOriginatingAddress
            ?: messages.firstOrNull()?.originatingAddress
            ?: ""
        val combinedBody = buildString {
            messages.forEach { sms ->
                val part = sms.displayMessageBody ?: sms.messageBody ?: ""
                append(part)
            }
        }.trim()

        if (combinedBody.isBlank()) return

        val parsed = AutoExpenseDetectionStore.parseTransactionalDebitSms(
            sender = senderAddress,
            body = combinedBody,
            requireKnownBankSender = true
        ) ?: return

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                val createdExpense = AutoExpenseDetectionStore.recordAutoLoggedSmsExpense(
                    context = context.applicationContext,
                    parsed = parsed,
                    senderAddress = senderAddress
                )
                AutoExpenseDetectionStore.showAutoLoggedHeadsUpNotification(
                    context = context.applicationContext,
                    expense = createdExpense
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error auto-logging incoming SMS expense", e)
            } finally {
                runCatching { pendingResult.finish() }
            }
        }
    }

    companion object {
        private const val TAG = "SmsExpenseReceiver"
        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

/**
 * BroadcastReceiver that handles the "↶ Undo" quick-action button on the Heads-Up Notification.
 */
class SmsUndoReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != AutoExpenseDetectionStore.ACTION_UNDO_AUTO_SMS_EXPENSE) return
        val expenseId = intent.getStringExtra(AutoExpenseDetectionStore.EXTRA_AUTO_EXPENSE_ID).orEmpty()
        val merchant = intent.getStringExtra(AutoExpenseDetectionStore.EXTRA_AUTO_EXPENSE_MERCHANT).orEmpty()
        val amount = intent.getLongExtra(AutoExpenseDetectionStore.EXTRA_AUTO_EXPENSE_AMOUNT, 0L)
        val notificationId = intent.getIntExtra(AutoExpenseDetectionStore.EXTRA_NOTIFICATION_ID, 0)

        if (notificationId != 0) {
            runCatching {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }
        if (expenseId.isBlank()) return

        val pendingResult = goAsync()
        undoScope.launch {
            try {
                AutoExpenseDetectionStore.undoAutoLoggedExpense(
                    context = context.applicationContext,
                    expenseId = expenseId,
                    merchant = merchant,
                    amount = amount
                )
            } finally {
                runCatching { pendingResult.finish() }
            }
        }
    }

    companion object {
        private val undoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

/**
 * Shared engine for:
 * 1. Real-time Incoming SMS Parsing & Persistence (`SmsExpenseReceiver`)
 * 2. Smart Clipboard Auto-Detection on App Resume (`MainActivity` / `HomeTab`)
 * 3. Quick-Undo & Edit deep-linking from Heads-Up Notifications
 */
object AutoExpenseDetectionStore {

    const val ACTION_UNDO_AUTO_SMS_EXPENSE = "com.example.action.UNDO_AUTO_SMS_EXPENSE"
    const val EXTRA_AUTO_EXPENSE_ID = "extra_auto_expense_id"
    const val EXTRA_AUTO_EXPENSE_MERCHANT = "extra_auto_expense_merchant"
    const val EXTRA_AUTO_EXPENSE_AMOUNT = "extra_auto_expense_amount"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_OPEN_EDIT_EXPENSE_ID = "extra_open_edit_expense_id"

    private const val PREFS_NAME = "kharchee_auto_expense_detection_store"
    private const val KEY_AUTO_EXPENSES_JSON = "auto_expenses_json"
    private const val KEY_LAST_DISMISSED_CLIP_HASH = "last_dismissed_clip_hash"
    private const val KEY_LAST_LOGGED_CLIP_HASH = "last_logged_clip_hash"
    private const val KEY_SMS_RATIONALE_DISMISSED = "sms_rationale_dismissed"
    private const val CHANNEL_AUTO_SMS_TRACKER = "kharchee_auto_sms_tracker"

    private val _autoExpensesFlow = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    val autoExpensesFlow: StateFlow<List<ExpenseRecord>> = _autoExpensesFlow.asStateFlow()

    private val _undoToastEventFlow = MutableStateFlow<String?>(null)
    val undoToastEventFlow: StateFlow<String?> = _undoToastEventFlow.asStateFlow()

    private val _pendingEditExpenseIdFlow = MutableStateFlow<String?>(null)
    val pendingEditExpenseIdFlow: StateFlow<String?> = _pendingEditExpenseIdFlow.asStateFlow()

    // Known Indian Bank & UPI Sender Handles
    private val KNOWN_BANK_UPI_SENDER_TOKENS = listOf(
        "HDFCBK", "HDFCBN", "SBIINB", "SBIPSG", "SBIUPI", "SBIN", "AXISBK", "ICICIB",
        "ICICIT", "PAYTM", "PYTM", "VPA", "CRED", "JIOFBR", "JIOPAY", "GPAY", "PHONEPE",
        "PHONPE", "BHIM", "KOTAKB", "YESBNK", "PNBSMS", "BOBTXN", "IDFCFB", "INDUSB",
        "CANBNK", "UNIONB", "FEDBNK", "RBLBNK", "AUFBNK", "SLICE", "JUPITR", "LAZYPAY",
        "SIMPL", "AMAZON", "SWIGGY", "ZOMATO", "AIRTEL", "CENTBK", "BOIIND", "UCOBNK"
    )

    // Bank/UPI context tokens for clipboard detection where sender ID is not separate
    private val CLIPBOARD_BANK_CONTEXT_TOKENS = listOf(
        "debited", "spent", "paid", "sent rs", "sent inr", "sent ₹", "deducted",
        "upi", "vpa", "utr", "hdfc", "sbi", "icici", "axis", "kotak", "paytm",
        "phonepe", "gpay", "google pay", "cred", "bhim", "a/c", "acct", "account",
        "swiggy", "zomato", "blinkit", "zepto", "irctc", "uber", "ola"
    )

    // Strict ignore regex for OTPs, PIN prompts, collect requests, and pure credit/salary messages
    private val IGNORE_OTP_OR_SECURITY_REGEX = Regex(
        "\\b(otp|one[-\\s]?time\\s+password|verification\\s+code|auth\\s+code|upi\\s+pin|mpin|cvv|do\\s+not\\s+share|never\\s+share|collect\\s+request|requested\\s+money)\\b",
        RegexOption.IGNORE_CASE
    )

    private val DEBIT_SPEND_INDICATOR_REGEX = Regex(
        "\\b(debited|spent|paid|sent|deducted|withdrawn|purchase|charged|transferred|dr\\.?|payment\\s+of)\\b",
        RegexOption.IGNORE_CASE
    )

    private val PURE_CREDIT_SALARY_REGEX = Regex(
        "\\b(credited|salary\\s+credited|deposited|refund\\s+of|cashback\\s+received)\\b",
        RegexOption.IGNORE_CASE
    )

    // Requested Amount Regex: (?:Rs\.?|INR|\u20B9)\s*([\d,]+(?:\.\d{1,2})?)
    private val AMOUNT_REGEX = Regex(
        "(?:Rs\\.?|INR|\\u20B9)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
        RegexOption.IGNORE_CASE
    )

    // Merchant extraction patterns after "to", "at", "vpa", "info", "towards"
    private val MERCHANT_PATTERNS = listOf(
        Regex("(?:paid\\s+to|sent\\s+to|transfer(?:red)?\\s+to|spent\\s+at|debited\\s+towards|towards|to\\s+vpa|vpa[:\\s]+|info[:\\s/]+|to|at)\\s+([A-Za-z0-9@._&\\- ]{2,45}?)(?:\\s+(?:on|ref|via|using|from|avl|bal|upi|utr|txn|a/c|acct|dated|for|\\.|,|;|-|\\()|$)", RegexOption.IGNORE_CASE),
        Regex("vpa\\s+([A-Za-z0-9._\\-]+@[A-Za-z0-9]+)", RegexOption.IGNORE_CASE)
    )

    private val KNOWN_MERCHANT_KEYWORDS = listOf(
        "Swiggy" to "Food",
        "Zomato" to "Food",
        "Domino" to "Food",
        "McDonald" to "Food",
        "KFC" to "Food",
        "Burger King" to "Food",
        "Subway" to "Food",
        "Haldiram" to "Food",
        "Barbeque" to "Food",
        "Blinkit" to "Grocery",
        "Zepto" to "Grocery",
        "Instamart" to "Grocery",
        "BigBasket" to "Grocery",
        "DMart" to "Grocery",
        "Reliance Fresh" to "Grocery",
        "Nature Basket" to "Grocery",
        "Chaayos" to "Chai & Snacks",
        "Starbucks" to "Chai & Snacks",
        "Third Wave" to "Chai & Snacks",
        "Blue Tokai" to "Chai & Snacks",
        "Sharma Chai" to "Chai & Snacks",
        "Tapri" to "Chai & Snacks",
        "Chai" to "Chai & Snacks",
        "Uber" to "Transport",
        "Ola" to "Transport",
        "Rapido" to "Transport",
        "Namma Yatri" to "Transport",
        "Delhi Metro" to "Transport",
        "DMRC" to "Transport",
        "Metro" to "Transport",
        "IRCTC" to "Transport",
        "HP Petrol" to "Transport",
        "HPCL" to "Transport",
        "IndianOil" to "Transport",
        "IOCL" to "Transport",
        "Bharat Petroleum" to "Transport",
        "BPCL" to "Transport",
        "Shell" to "Transport",
        "JioFiber" to "Bills",
        "Jio" to "Bills",
        "Airtel" to "Bills",
        "Vi " to "Bills",
        "ACT Fibernet" to "Bills",
        "BESCOM" to "Bills",
        "Tata Power" to "Bills",
        "Adani" to "Bills",
        "CRED" to "Bills",
        "Amazon" to "Shopping",
        "Flipkart" to "Shopping",
        "Myntra" to "Shopping",
        "Ajio" to "Shopping",
        "Nykaa" to "Shopping",
        "Meesho" to "Shopping",
        "Decathlon" to "Shopping",
        "Croma" to "Shopping",
        "BookMyShow" to "Entertainment",
        "PVR" to "Entertainment",
        "INOX" to "Entertainment",
        "Netflix" to "Entertainment",
        "Spotify" to "Entertainment",
        "YouTube" to "Entertainment",
        "Sharma Ji" to "Food"
    )

    fun ensureLoaded(context: Context) {
        val list = loadPersistedAutoExpenses(context)
        _autoExpensesFlow.value = list
    }

    fun consumeUndoToastEvent(): String? {
        val current = _undoToastEventFlow.value
        _undoToastEventFlow.value = null
        return current
    }

    fun setPendingEditExpenseId(expenseId: String?) {
        _pendingEditExpenseIdFlow.value = expenseId
    }

    fun consumePendingEditExpenseId(): String? {
        val id = _pendingEditExpenseIdFlow.value
        _pendingEditExpenseIdFlow.value = null
        return id
    }

    fun hasSmsPermissions(context: Context): Boolean {
        val receiveGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        return receiveGranted || readGranted
    }

    fun isSmsRationaleDismissed(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SMS_RATIONALE_DISMISSED, false)
    }

    fun setSmsRationaleDismissed(context: Context, dismissed: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SMS_RATIONALE_DISMISSED, dismissed)
            .apply()
    }

    fun isSupportedBankOrUpiSender(sender: String?): Boolean {
        if (sender.isNullOrBlank()) return false
        val upper = sender.uppercase()
        if (KNOWN_BANK_UPI_SENDER_TOKENS.any { upper.contains(it) }) return true
        // Standard TRAI commercial sender format like AD-HDFCBK, VM-SBIINB, AX-ICICIB-S, JD-PAYTMB
        if (Regex("^[A-Z]{2}-[A-Z0-9]{4,10}(?:-[A-Z])?$").matches(upper)) return true
        return false
    }

    /**
     * Parses a bank/UPI transactional SMS and returns a [ParsedSmsTransaction] if it is a valid
     * debit/spent message (ignoring OTPs and credit/salary messages).
     */
    fun parseTransactionalDebitSms(
        sender: String?,
        body: String,
        requireKnownBankSender: Boolean = false
    ): ParsedSmsTransaction? {
        val cleanBody = body.trim()
        if (cleanBody.length < 10) return null

        // 1. Ignore OTPs, PIN prompts, and security messages
        if (IGNORE_OTP_OR_SECURITY_REGEX.containsMatchIn(cleanBody)) {
            return null
        }

        // 2. Check debit vs credit type (ignore pure salary/credit alerts that don't indicate debit/spent)
        val hasDebitKeyword = DEBIT_SPEND_INDICATOR_REGEX.containsMatchIn(cleanBody)
        val hasCreditKeyword = PURE_CREDIT_SALARY_REGEX.containsMatchIn(cleanBody)
        if (!hasDebitKeyword) return null
        if (hasCreditKeyword && !cleanBody.lowercase().let {
                it.contains("debited") || it.contains("spent") || it.contains("paid") || it.contains("sent")
            }
        ) {
            return null
        }

        // 3. Validate sender or bank context
        val senderMatches = isSupportedBankOrUpiSender(sender)
        val lowerBody = cleanBody.lowercase()
        val hasBankContextInBody = CLIPBOARD_BANK_CONTEXT_TOKENS.count { lowerBody.contains(it) } >= 2
        if (requireKnownBankSender && !senderMatches && !hasBankContextInBody) {
            return null
        }
        if (!requireKnownBankSender && !senderMatches && !hasBankContextInBody) {
            return null
        }

        // 4. Extract Amount via (?:Rs\.?|INR|\u20B9)\s*([\d,]+(?:\.\d{1,2})?)
        val amountMatch = AMOUNT_REGEX.find(cleanBody) ?: return null
        val rawAmountStr = amountMatch.groupValues.getOrNull(1)?.replace(",", "") ?: return null
        val parsedAmount = rawAmountStr.toDoubleOrNull()?.roundToLong() ?: return null
        if (parsedAmount <= 0L) return null

        // 5. Extract Merchant / Recipient
        var extractedMerchant = ""
        for (pattern in MERCHANT_PATTERNS) {
            val match = pattern.find(cleanBody)
            if (match != null) {
                val rawCandidate = match.groupValues.getOrNull(1).orEmpty().trim()
                val cleaned = cleanMerchantCandidate(rawCandidate)
                if (cleaned.length >= 2 && !cleaned.lowercase().startsWith("a/c") && !cleaned.lowercase().startsWith("acct")) {
                    extractedMerchant = cleaned
                    break
                }
            }
        }

        // Fallback: check if any well-known merchant appears in the SMS body
        if (extractedMerchant.isBlank()) {
            val matchedKnown = KNOWN_MERCHANT_KEYWORDS.firstOrNull { (merchantName, _) ->
                cleanBody.contains(merchantName, ignoreCase = true)
            }
            if (matchedKnown != null) {
                extractedMerchant = matchedKnown.first
            }
        }

        if (extractedMerchant.isBlank()) {
            extractedMerchant = when {
                sender?.uppercase()?.contains("JIO") == true -> "Jio Recharge / Fiber"
                sender?.uppercase()?.contains("CRED") == true -> "CRED Bill Payment"
                sender?.uppercase()?.contains("PAYTM") == true -> "Paytm Merchant"
                else -> "UPI Merchant"
            }
        }

        // 6. Auto-Assign Category
        val category = inferCategory(extractedMerchant, cleanBody)

        // 7. Payment Mode
        val paymentMode = when {
            lowerBody.contains("credit card") || lowerBody.contains("debit card") || lowerBody.contains("pos") || lowerBody.contains("card ending") -> "Card"
            lowerBody.contains("atm") || lowerBody.contains("cash") -> "Cash"
            else -> "UPI"
        }

        return ParsedSmsTransaction(
            rawText = cleanBody,
            amount = parsedAmount,
            merchant = extractedMerchant.take(42),
            category = category,
            paymentMode = paymentMode,
            confidence = "high",
            isIgnoredSecurityMessage = false
        )
    }

    private fun cleanMerchantCandidate(raw: String): String {
        var text = raw.trim()
        // If it's a VPA like swiggy@axisbank or sharmaji@okicici, strip the @handle
        if (text.contains("@")) {
            text = text.substringBefore("@")
        }
        // Strip trailing reference tokens
        text = text.replace(
            Regex("\\b(on|ref|upi|utr|txn|avl|bal|using|via|from|a/c|acct|no|id)\\b.*$", RegexOption.IGNORE_CASE),
            ""
        )
        text = text.replace(Regex("[_\\-.]+"), " ").trim(' ', '.', ',', ':', ';', '-', '/', '(', ')')
        if (text.isBlank()) return ""
        // Convert ALL CAPS or lowercase into clean Title Case unless it's a short acronym like HP, PVR, KFC
        return text.split(Regex("\\s+")).joinToString(" ") { word ->
            if (word.length <= 3 && word.all { it.isUpperCase() }) {
                word
            } else {
                word.lowercase().replaceFirstChar { ch -> ch.titlecase() }
            }
        }
    }

    private fun inferCategory(merchant: String, fullBody: String): String {
        val combined = "$merchant $fullBody"
        for ((keyword, category) in KNOWN_MERCHANT_KEYWORDS) {
            if (combined.contains(keyword, ignoreCase = true)) {
                return category
            }
        }
        val lower = combined.lowercase()
        return when {
            listOf("chai", "tea", "coffee", "snack", "samosa", "bakery", "cafe").any { lower.contains(it) } -> "Chai & Snacks"
            listOf("food", "restaurant", "dining", "lunch", "dinner", "biryani", "pizza", "burger", "kitchen", "dhaba", "hotel").any { lower.contains(it) } -> "Food"
            listOf("petrol", "fuel", "diesel", "metro", "auto", "cab", "taxi", "train", "bus", "parking", "toll", "fastag").any { lower.contains(it) } -> "Transport"
            listOf("grocery", "milk", "vegetable", "fruit", "kirana", "mart", "supermarket", "dairy").any { lower.contains(it) } -> "Grocery"
            listOf("bill", "electricity", "recharge", "broadband", "wifi", "rent", "emi", "insurance", "gas", "water").any { lower.contains(it) } -> "Bills"
            listOf("movie", "cinema", "ott", "gaming", "turf", "subscription").any { lower.contains(it) } -> "Entertainment"
            listOf("mall", "store", "apparel", "fashion", "electronics").any { lower.contains(it) } -> "Shopping"
            else -> "Food"
        }
    }

    /**
     * Checks the Android system clipboard for an unlogged Indian bank/UPI debit SMS.
     * Zero permissions required.
     */
    fun inspectClipboardForBankSms(context: Context): ParsedSmsTransaction? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
            if (!clipboard.hasPrimaryClip()) return null
            val item = clipboard.primaryClip?.getItemAt(0) ?: return null
            val rawClip = item.coerceToText(context)?.toString()?.trim().orEmpty()
            if (rawClip.length < 12 || rawClip.length > 600) return null

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val clipHash = rawClip.hashCode().toString()
            if (prefs.getString(KEY_LAST_DISMISSED_CLIP_HASH, null) == clipHash) return null
            if (prefs.getString(KEY_LAST_LOGGED_CLIP_HASH, null) == clipHash) return null

            parseTransactionalDebitSms(
                sender = null,
                body = rawClip,
                requireKnownBankSender = false
            )
        } catch (e: Exception) {
            Log.w("AutoExpenseDetect", "Clipboard inspection skipped: ${e.message}")
            null
        }
    }

    fun markClipboardSmsDismissed(context: Context, rawText: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_DISMISSED_CLIP_HASH, rawText.trim().hashCode().toString())
            .apply()
    }

    fun markClipboardSmsLoggedAndClearBuffer(context: Context, rawText: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_LOGGED_CLIP_HASH, rawText.trim().hashCode().toString())
            .apply()
        clearClipboardBuffer(context)
    }

    fun clearClipboardBuffer(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard.clearPrimaryClip()
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Persists an auto-logged SMS expense locally and, if authenticated with Firebase, in Firestore.
     */
    suspend fun recordAutoLoggedSmsExpense(
        context: Context,
        parsed: ParsedSmsTransaction,
        senderAddress: String = ""
    ): ExpenseRecord {
        val now = System.currentTimeMillis()
        val firebaseUid = runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
        val fallbackUid = runCatching {
            IntroAuthPreferences.syncLocalSessionFromDisk(context)
            IntroAuthPreferences.localSessionFlow.value?.uid
        }.getOrNull()
        val activeUserId = firebaseUid ?: fallbackUid ?: "local_user"

        var expenseId = "auto_sms_$now"
        val note = if (senderAddress.isNotBlank()) {
            "Auto-Logged from SMS ($senderAddress)"
        } else {
            "Auto-Logged from Transactional SMS"
        }

        if (firebaseUid != null) {
            runCatching {
                val databaseId = context.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repo = KharcheeRepository(db)
                val res = repo.createExpense(
                    title = parsed.merchant,
                    category = parsed.category,
                    amount = parsed.amount,
                    paymentMode = parsed.paymentMode,
                    type = "expense",
                    note = note,
                    txEpochMillis = now
                )
                res.getOrNull()?.let { remoteId ->
                    if (remoteId.isNotBlank()) {
                        expenseId = remoteId
                    }
                }
            }
        }

        val record = ExpenseRecord(
            id = expenseId,
            userId = activeUserId,
            title = parsed.merchant,
            category = parsed.category,
            amount = parsed.amount,
            paymentMode = parsed.paymentMode,
            type = "expense",
            note = note,
            txEpochMillis = now
        )

        val updated = (listOf(record) + loadPersistedAutoExpenses(context))
            .distinctBy { it.id }
            .take(200)
        savePersistedAutoExpenses(context, updated)
        _autoExpensesFlow.value = updated

        // Reconcile any in-flight UPI payment intent session or matching Khata Dena Hai due
        com.example.util.PaymentUtil.onSmsDebitIntercepted(
            context = context,
            userId = activeUserId,
            debitedAmount = parsed.amount,
            merchantOrPayee = parsed.merchant
        )

        return record
    }

    suspend fun undoAutoLoggedExpense(
        context: Context,
        expenseId: String,
        merchant: String,
        amount: Long
    ) {
        val current = loadPersistedAutoExpenses(context)
        val filtered = current.filterNot { it.id == expenseId }
        savePersistedAutoExpenses(context, filtered)
        _autoExpensesFlow.value = filtered

        val firebaseUid = runCatching { Firebase.auth.currentUser?.uid }.getOrNull()
        if (firebaseUid != null && !expenseId.startsWith("auto_sms_")) {
            runCatching {
                val databaseId = context.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                KharcheeRepository(db).deleteExpense(expenseId)
            }
        }

        val label = if (merchant.isNotBlank()) "₹$amount at $merchant" else "₹$amount"
        _undoToastEventFlow.value = "↶ Undid auto-logged expense ($label). Balance restored."
    }

    fun removeLocalAutoExpenseById(context: Context, expenseId: String) {
        val current = loadPersistedAutoExpenses(context)
        val filtered = current.filterNot { it.id == expenseId }
        if (filtered.size != current.size) {
            savePersistedAutoExpenses(context, filtered)
            _autoExpensesFlow.value = filtered
        }
    }

    fun updateLocalAutoExpense(context: Context, updatedExpense: ExpenseRecord) {
        val current = loadPersistedAutoExpenses(context)
        val index = current.indexOfFirst { it.id == updatedExpense.id }
        if (index >= 0) {
            val mutable = current.toMutableList()
            mutable[index] = updatedExpense
            savePersistedAutoExpenses(context, mutable)
            _autoExpensesFlow.value = mutable
        }
    }

    @SuppressLint("MissingPermission")
    fun showAutoLoggedHeadsUpNotification(
        context: Context,
        expense: ExpenseRecord
    ) {
        ensureAutoSmsChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val notificationId = 7000 + abs(expense.id.hashCode() % 20000)

        val editIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_EDIT_EXPENSE_ID, expense.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            editIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val undoIntent = Intent(context, SmsUndoReceiver::class.java).apply {
            action = ACTION_UNDO_AUTO_SMS_EXPENSE
            putExtra(EXTRA_AUTO_EXPENSE_ID, expense.id)
            putExtra(EXTRA_AUTO_EXPENSE_MERCHANT, expense.title)
            putExtra(EXTRA_AUTO_EXPENSE_AMOUNT, expense.amount)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val undoPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            undoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚡ Auto-Logged: ₹${expense.amount} at ${expense.title}"
        val body = "Deducted from balance. Tap to edit or undo."

        val notification = NotificationCompat.Builder(context, CHANNEL_AUTO_SMS_TRACKER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_revert,
                "Undo",
                undoPendingIntent
            )
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun ensureAutoSmsChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_AUTO_SMS_TRACKER,
                "Auto-Logged SMS Expenses",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant heads-up alerts when Kharchee auto-logs bank or UPI debit SMS messages"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun loadPersistedAutoExpenses(context: Context): List<ExpenseRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_AUTO_EXPENSES_JSON, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val result = mutableListOf<ExpenseRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                result.add(
                    ExpenseRecord(
                        id = obj.optString("id"),
                        userId = obj.optString("userId"),
                        title = obj.optString("title"),
                        category = obj.optString("category", "Food"),
                        amount = obj.optLong("amount", 0L),
                        paymentMode = obj.optString("paymentMode", "UPI"),
                        type = obj.optString("type", "expense"),
                        note = obj.optString("note", ""),
                        txEpochMillis = obj.optLong("txEpochMillis", System.currentTimeMillis())
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun savePersistedAutoExpenses(context: Context, list: List<ExpenseRecord>) {
        val arr = JSONArray()
        list.forEach { rec ->
            val obj = JSONObject().apply {
                put("id", rec.id)
                put("userId", rec.userId)
                put("title", rec.title)
                put("category", rec.category)
                put("amount", rec.amount)
                put("paymentMode", rec.paymentMode)
                put("type", rec.type)
                put("note", rec.note)
                put("txEpochMillis", rec.txEpochMillis)
            }
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AUTO_EXPENSES_JSON, arr.toString())
            .apply()
    }
}
