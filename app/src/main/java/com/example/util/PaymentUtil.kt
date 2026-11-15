package com.example.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.KhataDirection
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Metadata representing an in-flight UPI payment launched from Kharchee's 1-Tap UPI Intent Launcher.
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
 *
 * Payment Safety Guarantee:
 * - NEVER guesses, fabricates, or auto-generates placeholder UPI IDs from merchant, bill, or person names.
 * - Only uses explicitly provided & validated UPI VPAs (`handle@bank`) or verified 10-digit Indian mobile numbers.
 * - When a bill or Khata contact has no verified UPI ID, either opens a generic UPI app chooser
 *   (with no fake `pa=` parameter) or prompts the user once via [VerifyOrEnterRecipientUpiDialog]
 *   to "Enter/Scan Recipient UPI ID" or "Mark as Paid Manually".
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
    private const val KEY_VERIFIED_VPA_PREFIX = "verified_vpa_"

    private val VALID_VPA_REGEX = Regex("^[a-zA-Z0-9._\\-]{2,64}@[a-zA-Z0-9]{2,32}$")
    private val EMBEDDED_VPA_REGEX = Regex("([a-zA-Z0-9._\\-]{2,64}@[a-zA-Z0-9]{2,32})")

    private val _activePendingSessionFlow = MutableStateFlow<PendingUpiIntentSession?>(null)
    val activePendingSessionFlow: StateFlow<PendingUpiIntentSession?> = _activePendingSessionFlow.asStateFlow()

    /**
     * Returns true only if [raw] is a syntactically valid UPI VPA (`handle@provider`).
     */
    fun isValidUpiVpa(raw: String): Boolean {
        val cleaned = raw.trim().replace(" ", "")
        return VALID_VPA_REGEX.matches(cleaned)
    }

    /**
     * Resolves an explicitly verified UPI VPA from a contact's stored phone/VPA string.
     * - If the user entered an explicit VPA (e.g., "rahul@okaxis"), returns it directly.
     * - If the user entered a valid 10-digit Indian mobile number, formats as "<10digits>@upi".
     * - NEVER guesses or fabricates placeholder VPAs from [payeeName] (returns `""` if unverified).
     */
    fun resolvePayeeVpa(rawPhoneOrVpa: String, payeeName: String = ""): String {
        val trimmed = rawPhoneOrVpa.trim().replace(" ", "")
        if (isValidUpiVpa(trimmed)) {
            return trimmed
        }
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length >= 10) {
            val tenDigit = digitsOnly.takeLast(10)
            if (tenDigit.first() in '6'..'9') {
                return "$tenDigit@upi"
            }
        }
        return ""
    }

    /**
     * Extracts an explicitly saved UPI VPA from a bill's note or title if present.
     * NEVER auto-generates or guesses placeholder biller UPI IDs from [billTitle].
     * Returns `""` if no verified VPA is stored.
     */
    fun resolveBillVpa(billTitle: String, billNoteOrSavedVpa: String = ""): String {
        val candidateFromNote = EMBEDDED_VPA_REGEX.find(billNoteOrSavedVpa.trim())?.groupValues?.getOrNull(1)
        if (!candidateFromNote.isNullOrBlank() && isValidUpiVpa(candidateFromNote)) {
            return candidateFromNote
        }
        val candidateFromTitle = EMBEDDED_VPA_REGEX.find(billTitle.trim())?.groupValues?.getOrNull(1)
        if (!candidateFromTitle.isNullOrBlank() && isValidUpiVpa(candidateFromTitle)) {
            return candidateFromTitle
        }
        return ""
    }

    /**
     * Retrieves a previously user-verified UPI VPA for a specific bill ID or contact ID.
     */
    fun getSavedVerifiedVpa(context: Context, entityId: String): String {
        if (entityId.isBlank()) return ""
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString("$KEY_VERIFIED_VPA_PREFIX$entityId", "")?.trim().orEmpty()
        return if (isValidUpiVpa(stored)) stored else ""
    }

    /**
     * Saves a user-verified UPI VPA for a specific bill ID or contact ID.
     */
    fun saveVerifiedVpa(context: Context, entityId: String, vpa: String) {
        val clean = vpa.trim().replace(" ", "")
        if (entityId.isBlank() || !isValidUpiVpa(clean)) return
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$KEY_VERIFIED_VPA_PREFIX$entityId", clean)
            .apply()
    }

    /**
     * Constructs the standard NPCI UPI deep-link URI string when a verified [payeeVpa] is available:
     * "upi://pay?pa=$payeeVpa&pn=${Uri.encode(payeeName)}&am=$amount&tn=${Uri.encode(txnNote)}&cu=INR"
     *
     * If [payeeVpa] is blank, constructs a generic UPI URI without a fabricated `pa=` parameter.
     */
    fun buildUpiDeepLinkUriString(
        payeeVpa: String,
        payeeName: String,
        amount: String,
        txnNote: String
    ): String {
        val cleanVpa = payeeVpa.trim().replace(" ", "")
        val cleanName = payeeName.trim().ifBlank { "Payee" }
        val cleanAmount = amount.trim().replace(",", "").ifBlank { "0" }
        val cleanNote = txnNote.trim().ifBlank { "Settle Due - Kharchee" }
        return if (isValidUpiVpa(cleanVpa)) {
            "upi://pay?pa=$cleanVpa&pn=${Uri.encode(cleanName)}&am=$cleanAmount&tn=${Uri.encode(cleanNote)}&cu=INR"
        } else {
            "upi://pay?pn=${Uri.encode(cleanName)}&am=$cleanAmount&tn=${Uri.encode(cleanNote)}&cu=INR"
        }
    }

    /**
     * Opens a generic UPI app chooser (`upi://pay`) so the user can scan a QR code or pick a contact
     * inside GPay / PhonePe / Paytm / BHIM when no VPA is saved in Kharchee.
     */
    fun launchGenericUpiAppChooser(
        context: Context,
        payeeName: String,
        amount: String,
        txnNote: String,
        contactId: String? = null,
        billReminderId: String? = null,
        onStatusMessage: ((String) -> Unit)? = null
    ): Boolean {
        val cleanAmountLong = amount.trim().replace(",", "").substringBefore(".").toLongOrNull() ?: 0L
        val genericUri = Uri.parse("upi://pay")
        val baseIntent = Intent(Intent.ACTION_VIEW, genericUri).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return try {
            recordPendingUpiSession(
                context = context,
                payeeVpa = "",
                payeeName = payeeName,
                amount = cleanAmountLong,
                txnNote = txnNote,
                contactId = contactId,
                billReminderId = billReminderId
            )
            val chooser = Intent.createChooser(baseIntent, "Pay ₹$amount via UPI App").apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            clearPendingUpiSession(context)
            val msg = "No UPI payment apps installed. Please mark as paid manually or install GPay/PhonePe/Paytm."
            runCatching { Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
            onStatusMessage?.invoke(msg)
            false
        }
    }

    /**
     * Launches the standard Android UPI payment intent (`Intent.ACTION_VIEW`) with pre-filled
     * verified payee VPA, payee name, exact amount, and transaction note.
     *
     * If no verified VPA is present on the contact/bill, checks if the user previously saved a VPA
     * for [contactId] or [billReminderId], and otherwise opens the generic UPI app chooser without
     * fabricating a placeholder VPA.
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
        val entityKey = contactId ?: billReminderId ?: ""
        val resolvedVpa = when {
            isValidUpiVpa(payeeVpa) -> payeeVpa.trim().replace(" ", "")
            resolvePayeeVpa(payeeVpa).isNotBlank() -> resolvePayeeVpa(payeeVpa)
            entityKey.isNotBlank() -> getSavedVerifiedVpa(context, entityKey)
            else -> ""
        }

        if (resolvedVpa.isBlank()) {
            return launchGenericUpiAppChooser(
                context = context,
                payeeName = payeeName,
                amount = amount,
                txnNote = txnNote,
                contactId = contactId,
                billReminderId = billReminderId,
                onStatusMessage = onStatusMessage
            )
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
        if (payeeVpa.isBlank()) return
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

    fun onAppResumedAfterUpiIntent(
        context: Context,
        onShowToast: ((String) -> Unit)? = null
    ): PendingUpiIntentSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val launchedAt = prefs.getLong(KEY_PENDING_TIMESTAMP, 0L)
        if (launchedAt <= 0L) return null

        val now = System.currentTimeMillis()
        if (now - launchedAt > 30 * 60 * 1000L) {
            clearPendingUpiSession(context)
            return null
        }

        val session = PendingUpiIntentSession(
            payeeVpa = prefs.getString(KEY_PENDING_VPA, "") ?: "",
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

/**
 * Payment Safety Prompt when a Bill or Khata Contact does not have a verified UPI ID (VPA).
 * Never guesses a placeholder VPA — lets the user:
 * 1. Enter & save the recipient's verified UPI ID (or 10-digit UPI mobile number) and pay immediately.
 * 2. Open their installed UPI App Chooser (to scan a QR or select a contact directly).
 * 3. Mark the bill/due as Paid Manually.
 */
@Composable
fun VerifyOrEnterRecipientUpiDialog(
    recipientName: String,
    amountInr: String,
    initialVpa: String = "",
    onConfirmVerifiedUpiPay: (verifiedVpa: String) -> Unit,
    onOpenGenericUpiScanner: () -> Unit,
    onMarkPaidManually: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    VerifyOrEnterRecipientUpiDialog(
        recipientTitle = recipientName,
        amountInr = amountInr.trim().replace(",", "").substringBefore(".").toLongOrNull() ?: 0L,
        initialVpaHint = initialVpa,
        onDismiss = onDismiss,
        onConfirmVpaAndPay = onConfirmVerifiedUpiPay,
        onOpenGenericUpiChooser = onOpenGenericUpiScanner,
        onMarkPaidManually = onMarkPaidManually
    )
}

@Composable
fun VerifyOrEnterRecipientUpiDialog(
    recipientTitle: String,
    amountInr: Long,
    initialVpaHint: String = "",
    onDismiss: () -> Unit,
    onConfirmVpaAndPay: (verifiedVpa: String) -> Unit,
    onOpenGenericUpiChooser: () -> Unit,
    onMarkPaidManually: (() -> Unit)? = null
) {
    var vpaInput by remember(initialVpaHint) { mutableStateOf(initialVpaHint) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF120F24),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFD8B4FE),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🛡️ PAYMENT SAFETY · VERIFY UPI ID",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2EE59D)
                )
                Text(
                    text = "Pay ₹$amountInr to $recipientTitle",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "To protect your money, Kharchee never guesses placeholder UPI IDs. Enter the recipient's verified UPI ID once, open your UPI app to scan a QR, or mark as paid manually.",
                    fontSize = 12.sp,
                    color = Color(0xFFB8B0D4),
                    lineHeight = 17.sp
                )

                OutlinedTextField(
                    value = vpaInput,
                    onValueChange = {
                        vpaInput = it
                        validationError = null
                    },
                    label = { Text("Recipient UPI ID (e.g. name@okicici) or 10-digit mobile") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2EE59D),
                        unfocusedBorderColor = Color(0xFF3B2D5E),
                        focusedContainerColor = Color(0xFF090A10),
                        unfocusedContainerColor = Color(0xFF090A10),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_recipient_upi_input")
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF4D8D)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenGenericUpiChooser,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, Color(0xFFA855F7)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_generic_upi_chooser_button")
                    ) {
                        Text(
                            text = "Open UPI App / Scan QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE)
                        )
                    }

                    if (onMarkPaidManually != null) {
                        OutlinedButton(
                            onClick = onMarkPaidManually,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF2EE59D).copy(alpha = 0.7f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_paid_manually_dialog_button")
                        ) {
                            Text(
                                text = "Mark Paid Manually",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2EE59D)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resolved = PaymentUtil.resolvePayeeVpa(vpaInput)
                    if (resolved.isBlank()) {
                        validationError = "Please enter a valid UPI ID (e.g. merchant@okaxis) or 10-digit mobile."
                    } else {
                        onConfirmVpaAndPay(resolved)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2EE59D),
                    contentColor = Color(0xFF040508)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_verified_vpa_pay_button")
            ) {
                Text(
                    text = "Save VPA & Pay ₹$amountInr",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, Color(0xFF3B2D5E))
            ) {
                Text("Cancel", color = Color(0xFFB8B0D4), fontSize = 11.sp)
            }
        }
    )
}
