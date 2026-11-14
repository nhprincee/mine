package com.example.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.KhataDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Metadata representing an in-flight UPI payment launched from Kharchee's 1-Tap UPI Intent Launcher.
 * Used to display the post-payment sync confirmation on return to the app and to auto-reconcile
 * Khata dues when [com.example.receiver.SmsExpenseReceiver] intercepts the bank/UPI debit SMS.
 */
data class PendingUpiIntentSession(
    val payeeVpa: String,
    val payeeName: String,
    val amount: Long,
    val txnNote: String,
    val contactId: String? = null,
    val billReminderId: String? = null,
    val launchedAtMillis: Long = System.currentTimeMillis(),
    val hasReturnedToApp: Boolean = false
)

/**
 * 1-Tap Instant UPI Payment Intent Launcher & Post-Payment SMS Auto-Reconciliation Utility.
 */
object PaymentUtil {

    const val POST_PAYMENT_SYNC_MESSAGE =
        "Payment initiated. Kharchee will auto-detect confirmation SMS once settled."

    private const val PREFS_NAME = "kharchee_upi_payment_util_prefs"
    private const val KEY_PENDING_VPA = "pending_vpa"
    private const val KEY_PENDING_NAME = "pending_name"
    private const val KEY_PENDING_AMOUNT = "pending_amount"
    private const val KEY_PENDING_NOTE = "pending_note"
    private const val KEY_PENDING_CONTACT_ID = "pending_contact_id"
    private const val KEY_PENDING_BILL_ID = "pending_bill_id"
    private const val KEY_PENDING_TIMESTAMP = "pending_timestamp"
    private const val KEY_AWAITING_RETURN_TOAST = "awaiting_return_toast"

    private val _activePendingSessionFlow = MutableStateFlow<PendingUpiIntentSession?>(null)
    val activePendingSessionFlow: StateFlow<PendingUpiIntentSession?> = _activePendingSessionFlow.asStateFlow()

    /**
     * Resolves a clean UPI VPA from a contact's stored phone/VPA string or name.
     * - If the user entered a VPA (e.g., "rahul@okaxis" or "friend@upi"), uses it directly.
     * - If the user entered a 10-digit Indian mobile number, formats as "<10digits>@upi".
     * - Otherwise falls back to a clean handle or "friend@upi".
     */
    fun resolvePayeeVpa(rawPhoneOrVpa: String, payeeName: String): String {
        val trimmed = rawPhoneOrVpa.trim()
        if (trimmed.contains("@") && trimmed.length >= 5) {
            return trimmed.replace(" ", "")
        }
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length >= 10) {
            val tenDigit = digitsOnly.takeLast(10)
            return "$tenDigit@upi"
        }
        val cleanNameSlug = payeeName.trim().lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(18)
        return if (cleanNameSlug.isNotBlank()) {
            "$cleanNameSlug@upi"
        } else {
            "friend@upi"
        }
    }

    /**
     * Resolves a default biller UPI VPA for Smart Bill Radar items when no custom VPA is stored.
     */
    fun resolveBillVpa(billTitle: String): String {
        val lower = billTitle.lowercase()
        return when {
            lower.contains("electric") || lower.contains("power") || lower.contains("bescom") -> "electricity.biller@upi"
            lower.contains("wifi") || lower.contains("broadband") || lower.contains("fiber") -> "broadband.biller@upi"
            lower.contains("mobile") || lower.contains("sim") || lower.contains("recharge") -> "mobilerecharge.biller@upi"
            lower.contains("ott") || lower.contains("netflix") || lower.contains("subscription") -> "subscriptions.biller@upi"
            lower.contains("rent") -> "landlord.rent@upi"
            else -> {
                val slug = lower.replace(Regex("[^a-z0-9]"), "").take(16).ifBlank { "biller" }
                "$slug.pay@upi"
            }
        }
    }

    /**
     * Constructs the standard NPCI UPI deep-link URI string:
     * "upi://pay?pa=$payeeVpa&pn=${Uri.encode(payeeName)}&am=$amount&tn=${Uri.encode(txnNote)}&cu=INR"
     */
    fun buildUpiDeepLinkUriString(
        payeeVpa: String,
        payeeName: String,
        amount: String,
        txnNote: String
    ): String {
        val cleanVpa = payeeVpa.trim().ifBlank { "friend@upi" }
        val cleanName = payeeName.trim().ifBlank { "Payee" }
        val cleanAmount = amount.trim().replace(",", "").ifBlank { "0" }
        val cleanNote = txnNote.trim().ifBlank { "Settle Khata Due - Kharchee" }
        return "upi://pay?pa=$cleanVpa&pn=${Uri.encode(cleanName)}&am=$cleanAmount&tn=${Uri.encode(cleanNote)}&cu=INR"
    }

    /**
     * Launches the standard Android UPI payment intent (`Intent.ACTION_VIEW`) with pre-filled
     * payee VPA, payee name, exact amount, and transaction note.
     *
     * Gracefully catches [ActivityNotFoundException] when no UPI apps (GPay, PhonePe, Paytm, BHIM)
     * are installed on the device, copies the UPI ID to the clipboard, and shows:
     * `"No UPI payment apps found. Copy UPI ID: [payeeVpa]"`.
     */
    fun launchUpiPaymentIntent(
        context: Context,
        payeeVpa: String,
        payeeName: String,
        amount: String,
        txnNote: String,
        contactId: String? = null,
        billReminderId: String? = null,
        onStatusMessage: ((String) -> Unit)? = null
    ): Boolean {
        val resolvedVpa = if (payeeVpa.contains("@")) {
            payeeVpa.trim()
        } else {
            resolvePayeeVpa(payeeVpa, payeeName)
        }
        val cleanAmountLong = amount.trim().replace(",", "").substringBefore(".").toLongOrNull() ?: 0L
        val formattedAmount = if (cleanAmountLong > 0L) cleanAmountLong.toString() else amount.trim()

        val upiUriString = buildUpiDeepLinkUriString(
            payeeVpa = resolvedVpa,
            payeeName = payeeName,
            amount = formattedAmount,
            txnNote = txnNote
        )

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUriString)).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        return try {
            recordPendingUpiSession(
                context = context,
                payeeVpa = resolvedVpa,
                payeeName = payeeName,
                amount = cleanAmountLong,
                txnNote = txnNote,
                contactId = contactId,
                billReminderId = billReminderId
            )
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            clearPendingUpiSession(context)
            copyVpaToClipboard(context, resolvedVpa)
            val fallbackMsg = "No UPI payment apps found. Copy UPI ID: $resolvedVpa"
            runCatching {
                Toast.makeText(context, fallbackMsg, Toast.LENGTH_LONG).show()
            }
            onStatusMessage?.invoke(fallbackMsg)
            false
        } catch (_: Exception) {
            clearPendingUpiSession(context)
            copyVpaToClipboard(context, resolvedVpa)
            val fallbackMsg = "No UPI payment apps found. Copy UPI ID: $resolvedVpa"
            runCatching {
                Toast.makeText(context, fallbackMsg, Toast.LENGTH_LONG).show()
            }
            onStatusMessage?.invoke(fallbackMsg)
            false
        }
    }

    /**
     * Convenience overload accepting numeric `amount: Long`.
     */
    fun launchUpiPaymentIntent(
        context: Context,
        payeeVpa: String,
        payeeName: String,
        amount: Long,
        txnNote: String,
        contactId: String? = null,
        billReminderId: String? = null,
        onStatusMessage: ((String) -> Unit)? = null
    ): Boolean {
        return launchUpiPaymentIntent(
            context = context,
            payeeVpa = payeeVpa,
            payeeName = payeeName,
            amount = amount.coerceAtLeast(1L).toString(),
            txnNote = txnNote,
            contactId = contactId,
            billReminderId = billReminderId,
            onStatusMessage = onStatusMessage
        )
    }

    private fun copyVpaToClipboard(context: Context, payeeVpa: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", payeeVpa))
        } catch (_: Exception) {
        }
    }

    private fun recordPendingUpiSession(
        context: Context,
        payeeVpa: String,
        payeeName: String,
        amount: Long,
        txnNote: String,
        contactId: String?,
        billReminderId: String?
    ) {
        val now = System.currentTimeMillis()
        val session = PendingUpiIntentSession(
            payeeVpa = payeeVpa,
            payeeName = payeeName,
            amount = amount,
            txnNote = txnNote,
            contactId = contactId,
            billReminderId = billReminderId,
            launchedAtMillis = now,
            hasReturnedToApp = false
        )
        _activePendingSessionFlow.value = session
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_VPA, payeeVpa)
            .putString(KEY_PENDING_NAME, payeeName)
            .putLong(KEY_PENDING_AMOUNT, amount)
            .putString(KEY_PENDING_NOTE, txnNote)
            .putString(KEY_PENDING_CONTACT_ID, contactId)
            .putString(KEY_PENDING_BILL_ID, billReminderId)
            .putLong(KEY_PENDING_TIMESTAMP, now)
            .putBoolean(KEY_AWAITING_RETURN_TOAST, true)
            .apply()
    }

    /**
     * Called from `ON_RESUME` when the user returns to Kharchee after launching a UPI payment intent.
     * Fires the required confirmation toast once and flips `hasReturnedToApp = true` so the UI badge renders.
     */
    fun onAppResumedAfterUpiIntent(
        context: Context,
        onShowToast: ((String) -> Unit)? = null
    ): PendingUpiIntentSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val launchedAt = prefs.getLong(KEY_PENDING_TIMESTAMP, 0L)
        if (launchedAt <= 0L) return null

        val now = System.currentTimeMillis()
        // Expire stale sessions older than 30 minutes
        if (now - launchedAt > 30 * 60 * 1000L) {
            clearPendingUpiSession(context)
            return null
        }

        val session = PendingUpiIntentSession(
            payeeVpa = prefs.getString(KEY_PENDING_VPA, "friend@upi") ?: "friend@upi",
            payeeName = prefs.getString(KEY_PENDING_NAME, "Payee") ?: "Payee",
            amount = prefs.getLong(KEY_PENDING_AMOUNT, 0L),
            txnNote = prefs.getString(KEY_PENDING_NOTE, "") ?: "",
            contactId = prefs.getString(KEY_PENDING_CONTACT_ID, null),
            billReminderId = prefs.getString(KEY_PENDING_BILL_ID, null),
            launchedAtMillis = launchedAt,
            hasReturnedToApp = true
        )
        _activePendingSessionFlow.value = session

        val awaitingToast = prefs.getBoolean(KEY_AWAITING_RETURN_TOAST, false)
        if (awaitingToast) {
            prefs.edit().putBoolean(KEY_AWAITING_RETURN_TOAST, false).apply()
            runCatching {
                Toast.makeText(context, POST_PAYMENT_SYNC_MESSAGE, Toast.LENGTH_LONG).show()
            }
            onShowToast?.invoke(POST_PAYMENT_SYNC_MESSAGE)
        }
        return session
    }

    /**
     * Invoked by [com.example.receiver.SmsExpenseReceiver] when a debit SMS is intercepted.
     * If there is an active pending UPI session (or matching Dena Hai Khata contact),
     * automatically records the Khata settlement and clears the pending badge.
     */
    fun onSmsDebitIntercepted(
        context: Context,
        userId: String,
        debitedAmount: Long,
        merchantOrPayee: String
    ) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val launchedAt = prefs.getLong(KEY_PENDING_TIMESTAMP, 0L)
            val now = System.currentTimeMillis()

            if (launchedAt > 0L && now - launchedAt <= 30 * 60 * 1000L) {
                val pendingContactId = prefs.getString(KEY_PENDING_CONTACT_ID, null)
                val pendingAmount = prefs.getLong(KEY_PENDING_AMOUNT, 0L)

                if (!pendingContactId.isNullOrBlank()) {
                    val amountMatches = pendingAmount <= 0L || abs(pendingAmount - debitedAmount) <= 5L
                    if (amountMatches) {
                        AdvancedKhataStore.recordPayment(
                            context = context,
                            userId = userId,
                            contactId = pendingContactId,
                            paymentAmount = debitedAmount,
                            description = "Auto-Settled via UPI SMS Confirmation",
                            notes = "Intercepted by SmsExpenseReceiver ($merchantOrPayee)"
                        )
                    }
                }
                clearPendingUpiSession(context)
                return
            }

            // Even if no explicit pending session ID was stored, check if any DENA_HAI Khata contact
            // matches the merchant/payee name and exact amount
            val allContacts = AdvancedKhataStore.getAllContactStates(context, userId)
            val matchedDenaContact = allContacts.firstOrNull { state ->
                !state.isSettled &&
                    state.netDirection == KhataDirection.DENA_HAI &&
                    state.outstandingAmount >= debitedAmount &&
                    merchantOrPayee.isNotBlank() &&
                    (state.contact.name.equals(merchantOrPayee, ignoreCase = true) ||
                        merchantOrPayee.contains(state.contact.name, ignoreCase = true))
            }
            if (matchedDenaContact != null) {
                AdvancedKhataStore.recordPayment(
                    context = context,
                    userId = userId,
                    contactId = matchedDenaContact.contact.id,
                    paymentAmount = debitedAmount,
                    description = "Auto-Settled via UPI SMS Confirmation",
                    notes = "Intercepted by SmsExpenseReceiver ($merchantOrPayee)"
                )
            }
        } catch (_: Exception) {
        }
    }

    fun clearPendingUpiSession(context: Context) {
        _activePendingSessionFlow.value = null
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PENDING_VPA)
            .remove(KEY_PENDING_NAME)
            .remove(KEY_PENDING_AMOUNT)
            .remove(KEY_PENDING_NOTE)
            .remove(KEY_PENDING_CONTACT_ID)
            .remove(KEY_PENDING_BILL_ID)
            .remove(KEY_PENDING_TIMESTAMP)
            .putBoolean(KEY_AWAITING_RETURN_TOAST, false)
            .apply()
    }
}
