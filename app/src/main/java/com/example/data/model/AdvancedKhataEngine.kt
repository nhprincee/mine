package com.example.data.model

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

enum class KhataDirection(val label: String) {
    LENA_HAI("Lena Hai"),
    DENA_HAI("Dena Hai")
}

enum class KhataRecordType(val label: String) {
    ORIGINAL_ENTRY("Original Entry"),
    PARTIAL_PAYMENT("Partial Payment"),
    FULL_SETTLEMENT("Full Settlement")
}

enum class KhataAuditAction(val label: String) {
    CREATED_ENTRY("Entry Recorded"),
    RECORDED_PAYMENT("Partial Payment"),
    FULL_SETTLEMENT("Settled in Full"),
    EDITED_ENTRY("Entry Edited"),
    DELETED_ENTRY("Entry Deleted"),
    REOPENED_LEDGER("Ledger Reopened")
}

enum class KhataContactFilter(val label: String) {
    ALL("All"),
    LENA_HAI("Lena Hai"),
    DENA_HAI("Dena Hai"),
    OVERDUE("Overdue"),
    SETTLED("Settled")
}

enum class KhataContactSort(val label: String) {
    LATEST_ACTIVITY("Latest Activity"),
    AMOUNT_HIGH_TO_LOW("Amount"),
    NAME_A_TO_Z("Name")
}

enum class KhataHistoryFilter(val label: String) {
    ALL("All"),
    LENA_HAI("Lena Hai"),
    DENA_HAI("Dena Hai"),
    PAYMENTS_RECEIVED("Payments Received"),
    PAYMENTS_MADE("Payments Made"),
    SETTLED("Settled"),
    OVERDUE("Overdue"),
    CUSTOM_DATE_RANGE("Custom Date Range")
}

data class KhataContact(
    val id: String,
    val userId: String,
    val name: String,
    val phone: String = "",
    val avatarInitials: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class KhataLedgerEntry(
    val id: String,
    val userId: String,
    val contactId: String,
    val contactName: String,
    val direction: KhataDirection,
    val recordType: KhataRecordType,
    val parentEntryId: String? = null,
    val amount: Long,
    val txDateMillis: Long = System.currentTimeMillis(),
    val dueDateMillis: Long? = null,
    val description: String = "",
    val category: String = "Personal Udhaar",
    val notes: String = "",
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val previousAmountBeforeEdit: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class KhataHistoryAuditItem(
    val id: String,
    val userId: String,
    val contactId: String,
    val contactName: String,
    val entryId: String,
    val direction: KhataDirection,
    val recordType: KhataRecordType,
    val action: KhataAuditAction,
    val amount: Long,
    val remainingBalanceAfter: Long,
    val paymentStatus: String, // "UNPAID", "PARTIALLY_PAID", "SETTLED", "OVERDUE", "EDITED", "VOIDED"
    val description: String,
    val category: String = "Personal Udhaar",
    val dueDateMillis: Long? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class KhataContactComputedState(
    val contact: KhataContact,
    val totalOriginalLenaHai: Long,
    val totalPaymentsReceived: Long,
    val totalOriginalDenaHai: Long,
    val totalPaymentsMade: Long,
    val netSignedBalance: Long, // > 0 => Lena Hai, < 0 => Dena Hai, 0 => Settled
    val outstandingAmount: Long,
    val netDirection: KhataDirection?, // null if 0 (Settled), else LENA_HAI / DENA_HAI
    val isSettled: Boolean,
    val hasHistory: Boolean,
    val lastTransactionMillis: Long,
    val earliestUnsettledDueDateMillis: Long?,
    val isOverdue: Boolean,
    val isUpcomingDue: Boolean,
    val overdueAmount: Long,
    val activeEntries: List<KhataLedgerEntry>,
    val allEntriesIncludingDeleted: List<KhataLedgerEntry>
) {
    val totalOriginalRecordedForActiveDirection: Long
        get() = when (netDirection) {
            KhataDirection.LENA_HAI -> totalOriginalLenaHai
            KhataDirection.DENA_HAI -> totalOriginalDenaHai
            null -> max(totalOriginalLenaHai, totalOriginalDenaHai)
        }

    val totalPaidOrReceivedForActiveDirection: Long
        get() = when (netDirection) {
            KhataDirection.LENA_HAI -> totalPaymentsReceived
            KhataDirection.DENA_HAI -> totalPaymentsMade
            null -> totalPaymentsReceived + totalPaymentsMade
        }
}

data class KhataSummaryMetrics(
    val totalLenaHai: Long = 0L,
    val lenaHaiPeopleCount: Int = 0,
    val totalDenaHai: Long = 0L,
    val denaHaiPeopleCount: Int = 0,
    val netOutstandingBalance: Long = 0L, // totalLenaHai - totalDenaHai
    val totalActiveContacts: Int = 0,
    val totalSettledContacts: Int = 0,
    val totalContactsCount: Int = 0,
    val overdueAmount: Long = 0L,
    val overdueContactsCount: Int = 0
)

/**
 * Unified activity item for the Home Dashboard's RECENT HISTORY section and full History screen,
 * combining Income, Expenses, Bill Payments, KHATA Entries, and KHATA Payments.
 */
enum class UnifiedActivityKind(val badgeLabel: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
    BILL_PAYMENT("Bill Payment"),
    KHATA_LENA_ENTRY("Khata · Lena Hai"),
    KHATA_DENA_ENTRY("Khata · Dena Hai"),
    KHATA_PAYMENT_RECEIVED("Khata · Payment In"),
    KHATA_PAYMENT_MADE("Khata · Payment Out"),
    KHATA_SETTLEMENT("Khata · Settled")
}

data class UnifiedRecentActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: UnifiedActivityKind,
    val amount: Long,
    val isPositiveInflow: Boolean,
    val timestampMillis: Long,
    val statusBadge: String = ""
)

val KHATA_CATEGORIES = listOf(
    "Personal Udhaar",
    "Emergency Help",
    "Rent & Room Split",
    "Food & Outing",
    "Travel & Trip",
    "Business / Shop",
    "Family Support",
    "Other"
)

object AdvancedKhataStore {
    private const val PREFS_NAME = "kharchee_advanced_khata_v1"
    private const val DEDUP_WINDOW_MS = 2500L

    private val _versionFlow = MutableStateFlow(0L)
    val versionFlow: StateFlow<Long> = _versionFlow.asStateFlow()

    private var lastSubmissionHash: String = ""
    private var lastSubmissionTimeMs: Long = 0L

    private fun bumpVersion() {
        _versionFlow.value = System.currentTimeMillis()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun contactsKey(userId: String) = "contacts_${userId.ifBlank { "default" }}"
    private fun entriesKey(userId: String) = "entries_${userId.ifBlank { "default" }}"
    private fun auditKey(userId: String) = "audit_${userId.ifBlank { "default" }}"

    fun computeInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "KH"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
        }
    }

    fun startOfTodayMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun loadContacts(context: Context, userId: String): List<KhataContact> {
        val safeUid = userId.ifBlank { "default" }
        val raw = prefs(context).getString(contactsKey(safeUid), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val recordUid = obj.optString("userId", safeUid)
                    if (recordUid != safeUid) continue
                    add(
                        KhataContact(
                            id = obj.optString("id"),
                            userId = recordUid,
                            name = obj.optString("name"),
                            phone = obj.optString("phone", ""),
                            avatarInitials = obj.optString("avatarInitials", computeInitials(obj.optString("name"))),
                            notes = obj.optString("notes", ""),
                            reminderEnabled = obj.optBoolean("reminderEnabled", true),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                            updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveContacts(context: Context, userId: String, contacts: List<KhataContact>) {
        val safeUid = userId.ifBlank { "default" }
        val arr = JSONArray()
        contacts.filter { it.userId == safeUid }.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("userId", safeUid)
                    put("name", c.name)
                    put("phone", c.phone)
                    put("avatarInitials", c.avatarInitials)
                    put("notes", c.notes)
                    put("reminderEnabled", c.reminderEnabled)
                    put("createdAtMillis", c.createdAtMillis)
                    put("updatedAtMillis", c.updatedAtMillis)
                }
            )
        }
        prefs(context).edit().putString(contactsKey(safeUid), arr.toString()).apply()
    }

    fun loadEntries(context: Context, userId: String): List<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        val raw = prefs(context).getString(entriesKey(safeUid), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val recordUid = obj.optString("userId", safeUid)
                    if (recordUid != safeUid) continue
                    val dueRaw = obj.optLong("dueDateMillis", -1L)
                    val prevAmtRaw = obj.optLong("previousAmountBeforeEdit", -1L)
                    val parentRaw = obj.optString("parentEntryId", "")
                    add(
                        KhataLedgerEntry(
                            id = obj.optString("id"),
                            userId = recordUid,
                            contactId = obj.optString("contactId"),
                            contactName = obj.optString("contactName"),
                            direction = runCatching {
                                KhataDirection.valueOf(obj.optString("direction", KhataDirection.LENA_HAI.name))
                            }.getOrDefault(KhataDirection.LENA_HAI),
                            recordType = runCatching {
                                KhataRecordType.valueOf(obj.optString("recordType", KhataRecordType.ORIGINAL_ENTRY.name))
                            }.getOrDefault(KhataRecordType.ORIGINAL_ENTRY),
                            parentEntryId = parentRaw.ifBlank { null },
                            amount = obj.optLong("amount", 0L),
                            txDateMillis = obj.optLong("txDateMillis", System.currentTimeMillis()),
                            dueDateMillis = if (dueRaw > 0L) dueRaw else null,
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Personal Udhaar"),
                            notes = obj.optString("notes", ""),
                            isDeleted = obj.optBoolean("isDeleted", false),
                            isEdited = obj.optBoolean("isEdited", false),
                            previousAmountBeforeEdit = if (prevAmtRaw > 0L) prevAmtRaw else null,
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                            updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveEntries(context: Context, userId: String, entries: List<KhataLedgerEntry>) {
        val safeUid = userId.ifBlank { "default" }
        val arr = JSONArray()
        entries.filter { it.userId == safeUid }.forEach { e ->
            arr.put(
                JSONObject().apply {
                    put("id", e.id)
                    put("userId", safeUid)
                    put("contactId", e.contactId)
                    put("contactName", e.contactName)
                    put("direction", e.direction.name)
                    put("recordType", e.recordType.name)
                    put("parentEntryId", e.parentEntryId ?: "")
                    put("amount", e.amount)
                    put("txDateMillis", e.txDateMillis)
                    put("dueDateMillis", e.dueDateMillis ?: -1L)
                    put("description", e.description)
                    put("category", e.category)
                    put("notes", e.notes)
                    put("isDeleted", e.isDeleted)
                    put("isEdited", e.isEdited)
                    put("previousAmountBeforeEdit", e.previousAmountBeforeEdit ?: -1L)
                    put("createdAtMillis", e.createdAtMillis)
                    put("updatedAtMillis", e.updatedAtMillis)
                }
            )
        }
        prefs(context).edit().putString(entriesKey(safeUid), arr.toString()).apply()
    }

    fun loadAuditHistory(context: Context, userId: String): List<KhataHistoryAuditItem> {
        val safeUid = userId.ifBlank { "default" }
        val raw = prefs(context).getString(auditKey(safeUid), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val recordUid = obj.optString("userId", safeUid)
                    if (recordUid != safeUid) continue
                    val dueRaw = obj.optLong("dueDateMillis", -1L)
                    add(
                        KhataHistoryAuditItem(
                            id = obj.optString("id"),
                            userId = recordUid,
                            contactId = obj.optString("contactId"),
                            contactName = obj.optString("contactName"),
                            entryId = obj.optString("entryId"),
                            direction = runCatching {
                                KhataDirection.valueOf(obj.optString("direction", KhataDirection.LENA_HAI.name))
                            }.getOrDefault(KhataDirection.LENA_HAI),
                            recordType = runCatching {
                                KhataRecordType.valueOf(obj.optString("recordType", KhataRecordType.ORIGINAL_ENTRY.name))
                            }.getOrDefault(KhataRecordType.ORIGINAL_ENTRY),
                            action = runCatching {
                                KhataAuditAction.valueOf(obj.optString("action", KhataAuditAction.CREATED_ENTRY.name))
                            }.getOrDefault(KhataAuditAction.CREATED_ENTRY),
                            amount = obj.optLong("amount", 0L),
                            remainingBalanceAfter = obj.optLong("remainingBalanceAfter", 0L),
                            paymentStatus = obj.optString("paymentStatus", "UNPAID"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Personal Udhaar"),
                            dueDateMillis = if (dueRaw > 0L) dueRaw else null,
                            timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis())
                        )
                    )
                }
            }.sortedByDescending { it.timestampMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveAuditHistory(context: Context, userId: String, items: List<KhataHistoryAuditItem>) {
        val safeUid = userId.ifBlank { "default" }
        val arr = JSONArray()
        items.filter { it.userId == safeUid }.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("userId", safeUid)
                    put("contactId", item.contactId)
                    put("contactName", item.contactName)
                    put("entryId", item.entryId)
                    put("direction", item.direction.name)
                    put("recordType", item.recordType.name)
                    put("action", item.action.name)
                    put("amount", item.amount)
                    put("remainingBalanceAfter", item.remainingBalanceAfter)
                    put("paymentStatus", item.paymentStatus)
                    put("description", item.description)
                    put("category", item.category)
                    put("dueDateMillis", item.dueDateMillis ?: -1L)
                    put("timestampMillis", item.timestampMillis)
                }
            )
        }
        prefs(context).edit().putString(auditKey(safeUid), arr.toString()).apply()
    }

    /**
     * Synchronizes any legacy `KhataFriendRecord` items from Firestore into `AdvancedKhataStore`
     * without overwriting existing contacts or duplicating entries.
     */
    fun syncLegacyFirestoreFriendsIfNeeded(
        context: Context,
        userId: String,
        legacyFriends: List<KhataFriendRecord>
    ) {
        if (legacyFriends.isEmpty()) return
        val safeUid = userId.ifBlank { "default" }
        val existingContacts = loadContacts(context, safeUid).toMutableList()
        val existingEntries = loadEntries(context, safeUid).toMutableList()
        var modified = false

        legacyFriends.forEach { legacy ->
            val cleanName = legacy.name.trim()
            if (cleanName.isNotEmpty() && existingContacts.none { it.name.equals(cleanName, ignoreCase = true) }) {
                val now = System.currentTimeMillis()
                val contactId = legacy.id.ifBlank { "khata_contact_${UUID.randomUUID()}" }
                val newContact = KhataContact(
                    id = contactId,
                    userId = safeUid,
                    name = cleanName,
                    phone = legacy.phone,
                    avatarInitials = legacy.avatarInitials.ifBlank { computeInitials(cleanName) },
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
                existingContacts.add(newContact)
                if (legacy.balance != 0L) {
                    val dir = if (legacy.balance > 0L) KhataDirection.LENA_HAI else KhataDirection.DENA_HAI
                    existingEntries.add(
                        KhataLedgerEntry(
                            id = "legacy_entry_$contactId",
                            userId = safeUid,
                            contactId = contactId,
                            contactName = cleanName,
                            direction = dir,
                            recordType = KhataRecordType.ORIGINAL_ENTRY,
                            amount = abs(legacy.balance),
                            txDateMillis = now,
                            description = legacy.lastDescription.ifBlank { "Opening Khata Balance" },
                            category = "Personal Udhaar",
                            createdAtMillis = now,
                            updatedAtMillis = now
                        )
                    )
                }
                modified = true
            }
        }

        if (modified) {
            saveContacts(context, safeUid, existingContacts)
            saveEntries(context, safeUid, existingEntries)
            bumpVersion()
        }
    }

    /**
     * Adds a new Lena Hai or Dena Hai entry.
     * Automatically creates or reuses the person's contact record, and reopens settled ledgers.
     */
    @Synchronized
    fun addUdhaarEntry(
        context: Context,
        userId: String,
        personName: String,
        phone: String = "",
        amount: Long,
        direction: KhataDirection,
        txDateMillis: Long = System.currentTimeMillis(),
        dueDateMillis: Long? = null,
        description: String = "",
        category: String = "Personal Udhaar",
        notes: String = "",
        bypassDedupWindow: Boolean = false
    ): Result<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        val cleanName = personName.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Person's name is required"))
        }
        if (amount <= 0L) {
            return Result.failure(IllegalArgumentException("Amount must be greater than ₹0"))
        }

        val now = System.currentTimeMillis()
        val submissionKey = "$safeUid|ENTRY|${cleanName.lowercase()}|${direction.name}|$amount|${description.trim()}"
        if (!bypassDedupWindow &&
            submissionKey == lastSubmissionHash &&
            (now - lastSubmissionTimeMs) < DEDUP_WINDOW_MS
        ) {
            return Result.failure(IllegalStateException("Duplicate entry prevented"))
        }
        lastSubmissionHash = submissionKey
        lastSubmissionTimeMs = now

        val contacts = loadContacts(context, safeUid).toMutableList()
        val entries = loadEntries(context, safeUid).toMutableList()
        val audits = loadAuditHistory(context, safeUid).toMutableList()

        val existingContactIndex = contacts.indexOfFirst {
            it.name.equals(cleanName, ignoreCase = true)
        }
        val wasPreviouslySettled: Boolean
        val contact: KhataContact = if (existingContactIndex >= 0) {
            val prev = contacts[existingContactIndex]
            val prevState = computeContactState(prev, entries.filter { it.contactId == prev.id })
            wasPreviouslySettled = prevState.hasHistory && prevState.isSettled
            val updated = prev.copy(
                phone = if (phone.isNotBlank()) phone.trim() else prev.phone,
                notes = if (notes.isNotBlank()) notes.trim() else prev.notes,
                updatedAtMillis = now
            )
            contacts[existingContactIndex] = updated
            updated
        } else {
            wasPreviouslySettled = false
            val created = KhataContact(
                id = "contact_${UUID.randomUUID()}",
                userId = safeUid,
                name = cleanName,
                phone = phone.trim(),
                avatarInitials = computeInitials(cleanName),
                notes = notes.trim(),
                createdAtMillis = now,
                updatedAtMillis = now
            )
            contacts.add(created)
            created
        }

        val newEntry = KhataLedgerEntry(
            id = "entry_${UUID.randomUUID()}",
            userId = safeUid,
            contactId = contact.id,
            contactName = contact.name,
            direction = direction,
            recordType = KhataRecordType.ORIGINAL_ENTRY,
            amount = amount,
            txDateMillis = txDateMillis,
            dueDateMillis = dueDateMillis,
            description = description.trim().ifBlank {
                if (direction == KhataDirection.LENA_HAI) "Lena Hai (Receivable)" else "Dena Hai (Payable)"
            },
            category = category.trim().ifBlank { "Personal Udhaar" },
            notes = notes.trim(),
            createdAtMillis = now,
            updatedAtMillis = now
        )
        entries.add(newEntry)

        val updatedState = computeContactState(contact, entries.filter { it.contactId == contact.id })
        val statusLabel = when {
            updatedState.isSettled -> "SETTLED"
            updatedState.isOverdue -> "OVERDUE"
            else -> "UNPAID"
        }

        audits.add(
            0,
            KhataHistoryAuditItem(
                id = "audit_${UUID.randomUUID()}",
                userId = safeUid,
                contactId = contact.id,
                contactName = contact.name,
                entryId = newEntry.id,
                direction = direction,
                recordType = KhataRecordType.ORIGINAL_ENTRY,
                action = if (wasPreviouslySettled) KhataAuditAction.REOPENED_LEDGER else KhataAuditAction.CREATED_ENTRY,
                amount = amount,
                remainingBalanceAfter = updatedState.outstandingAmount,
                paymentStatus = statusLabel,
                description = newEntry.description,
                category = newEntry.category,
                dueDateMillis = dueDateMillis,
                timestampMillis = txDateMillis
            )
        )

        saveContacts(context, safeUid, contacts)
        saveEntries(context, safeUid, entries)
        saveAuditHistory(context, safeUid, audits)
        bumpVersion()
        return Result.success(newEntry)
    }

    /**
     * Records a partial payment or full settlement for a contact.
     * NEVER overwrites the original entry; creates a separate payment record and recalculates balances.
     */
    @Synchronized
    fun recordPayment(
        context: Context,
        userId: String,
        contactId: String,
        paymentAmount: Long,
        paymentDateMillis: Long = System.currentTimeMillis(),
        description: String = "",
        notes: String = "",
        parentEntryId: String? = null,
        bypassDedupWindow: Boolean = false
    ): Result<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        if (paymentAmount <= 0L) {
            return Result.failure(IllegalArgumentException("Payment amount must be greater than ₹0"))
        }

        val contacts = loadContacts(context, safeUid).toMutableList()
        val entries = loadEntries(context, safeUid).toMutableList()
        val audits = loadAuditHistory(context, safeUid).toMutableList()

        val contactIdx = contacts.indexOfFirst { it.id == contactId }
        if (contactIdx < 0) {
            return Result.failure(IllegalArgumentException("Contact not found"))
        }
        val contact = contacts[contactIdx]
        val currentState = computeContactState(contact, entries.filter { it.contactId == contactId })

        if (currentState.isSettled || currentState.outstandingAmount <= 0L) {
            return Result.failure(IllegalStateException("${contact.name}'s account is already settled"))
        }
        if (paymentAmount > currentState.outstandingAmount) {
            return Result.failure(
                IllegalArgumentException(
                    "Payment (₹$paymentAmount) cannot exceed outstanding balance (₹${currentState.outstandingAmount})"
                )
            )
        }

        val now = System.currentTimeMillis()
        val submissionKey = "$safeUid|PAYMENT|$contactId|$paymentAmount"
        if (!bypassDedupWindow &&
            submissionKey == lastSubmissionHash &&
            (now - lastSubmissionTimeMs) < DEDUP_WINDOW_MS
        ) {
            return Result.failure(IllegalStateException("Duplicate payment submission prevented"))
        }
        lastSubmissionHash = submissionKey
        lastSubmissionTimeMs = now

        val activeDir = currentState.netDirection ?: KhataDirection.LENA_HAI
        val isFullSettlement = paymentAmount == currentState.outstandingAmount
        val recordType = if (isFullSettlement) KhataRecordType.FULL_SETTLEMENT else KhataRecordType.PARTIAL_PAYMENT

        val defaultDesc = when {
            isFullSettlement && activeDir == KhataDirection.LENA_HAI -> "Full settlement received"
            isFullSettlement && activeDir == KhataDirection.DENA_HAI -> "Full settlement paid"
            activeDir == KhataDirection.LENA_HAI -> "Partial payment received"
            else -> "Partial payment made"
        }

        val paymentEntry = KhataLedgerEntry(
            id = "payment_${UUID.randomUUID()}",
            userId = safeUid,
            contactId = contact.id,
            contactName = contact.name,
            direction = activeDir,
            recordType = recordType,
            parentEntryId = parentEntryId,
            amount = paymentAmount,
            txDateMillis = paymentDateMillis,
            dueDateMillis = null,
            description = description.trim().ifBlank { defaultDesc },
            category = if (activeDir == KhataDirection.LENA_HAI) "Payment Received" else "Payment Made",
            notes = notes.trim(),
            createdAtMillis = now,
            updatedAtMillis = now
        )
        entries.add(paymentEntry)
        contacts[contactIdx] = contact.copy(updatedAtMillis = now)

        val updatedState = computeContactState(contacts[contactIdx], entries.filter { it.contactId == contact.id })
        val paymentStatus = if (updatedState.isSettled) "SETTLED" else "PARTIALLY_PAID"

        audits.add(
            0,
            KhataHistoryAuditItem(
                id = "audit_${UUID.randomUUID()}",
                userId = safeUid,
                contactId = contact.id,
                contactName = contact.name,
                entryId = paymentEntry.id,
                direction = activeDir,
                recordType = recordType,
                action = if (isFullSettlement) KhataAuditAction.FULL_SETTLEMENT else KhataAuditAction.RECORDED_PAYMENT,
                amount = paymentAmount,
                remainingBalanceAfter = updatedState.outstandingAmount,
                paymentStatus = paymentStatus,
                description = paymentEntry.description,
                category = paymentEntry.category,
                timestampMillis = paymentDateMillis
            )
        )

        saveContacts(context, safeUid, contacts)
        saveEntries(context, safeUid, entries)
        saveAuditHistory(context, safeUid, audits)
        bumpVersion()
        return Result.success(paymentEntry)
    }

    /**
     * Settles the entire remaining balance for a contact in one tap while preserving all original entries.
     */
    @Synchronized
    fun settleContactInFull(
        context: Context,
        userId: String,
        contactId: String,
        note: String = "Settled in full"
    ): Result<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        val contacts = loadContacts(context, safeUid)
        val entries = loadEntries(context, safeUid)
        val contact = contacts.find { it.id == contactId }
            ?: return Result.failure(IllegalArgumentException("Contact not found"))
        val state = computeContactState(contact, entries.filter { it.contactId == contactId })
        if (state.isSettled || state.outstandingAmount <= 0L) {
            return Result.failure(IllegalStateException("Already settled"))
        }
        return recordPayment(
            context = context,
            userId = safeUid,
            contactId = contactId,
            paymentAmount = state.outstandingAmount,
            description = note,
            bypassDedupWindow = true
        )
    }

    /**
     * Edits an existing Khata entry and recalculates all balances and history audit trails.
     */
    @Synchronized
    fun editEntry(
        context: Context,
        userId: String,
        entryId: String,
        newAmount: Long,
        newDescription: String,
        newCategory: String,
        newDueDateMillis: Long?,
        newNotes: String
    ): Result<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        if (newAmount <= 0L) {
            return Result.failure(IllegalArgumentException("Amount must be greater than ₹0"))
        }

        val contacts = loadContacts(context, safeUid).toMutableList()
        val entries = loadEntries(context, safeUid).toMutableList()
        val audits = loadAuditHistory(context, safeUid).toMutableList()

        val idx = entries.indexOfFirst { it.id == entryId && !it.isDeleted }
        if (idx < 0) {
            return Result.failure(IllegalArgumentException("Entry not found"))
        }
        val old = entries[idx]
        val now = System.currentTimeMillis()
        val updated = old.copy(
            amount = newAmount,
            description = newDescription.trim().ifBlank { old.description },
            category = newCategory.trim().ifBlank { old.category },
            dueDateMillis = newDueDateMillis,
            notes = newNotes.trim(),
            isEdited = true,
            previousAmountBeforeEdit = old.amount,
            updatedAtMillis = now
        )
        entries[idx] = updated

        val contact = contacts.find { it.id == old.contactId }
        val remainingAfter = if (contact != null) {
            computeContactState(contact, entries.filter { it.contactId == contact.id }).outstandingAmount
        } else 0L

        audits.add(
            0,
            KhataHistoryAuditItem(
                id = "audit_${UUID.randomUUID()}",
                userId = safeUid,
                contactId = old.contactId,
                contactName = old.contactName,
                entryId = updated.id,
                direction = updated.direction,
                recordType = updated.recordType,
                action = KhataAuditAction.EDITED_ENTRY,
                amount = newAmount,
                remainingBalanceAfter = remainingAfter,
                paymentStatus = "EDITED",
                description = "${updated.description} (Edited from ₹${old.amount} to ₹$newAmount)",
                category = updated.category,
                dueDateMillis = newDueDateMillis,
                timestampMillis = now
            )
        )

        saveEntries(context, safeUid, entries)
        saveAuditHistory(context, safeUid, audits)
        bumpVersion()
        return Result.success(updated)
    }

    /**
     * Soft-deletes an entry (with audit trail preservation) and recalculates all balances.
     */
    @Synchronized
    fun deleteEntry(
        context: Context,
        userId: String,
        entryId: String
    ): Result<Unit> {
        val safeUid = userId.ifBlank { "default" }
        val contacts = loadContacts(context, safeUid)
        val entries = loadEntries(context, safeUid).toMutableList()
        val audits = loadAuditHistory(context, safeUid).toMutableList()

        val idx = entries.indexOfFirst { it.id == entryId && !it.isDeleted }
        if (idx < 0) {
            return Result.failure(IllegalArgumentException("Entry not found"))
        }
        val target = entries[idx]
        val now = System.currentTimeMillis()
        entries[idx] = target.copy(isDeleted = true, updatedAtMillis = now)

        val contact = contacts.find { it.id == target.contactId }
        val remainingAfter = if (contact != null) {
            computeContactState(contact, entries.filter { it.contactId == contact.id }).outstandingAmount
        } else 0L

        audits.add(
            0,
            KhataHistoryAuditItem(
                id = "audit_${UUID.randomUUID()}",
                userId = safeUid,
                contactId = target.contactId,
                contactName = target.contactName,
                entryId = target.id,
                direction = target.direction,
                recordType = target.recordType,
                action = KhataAuditAction.DELETED_ENTRY,
                amount = target.amount,
                remainingBalanceAfter = remainingAfter,
                paymentStatus = "VOIDED",
                description = "Deleted: ${target.description}",
                category = target.category,
                dueDateMillis = target.dueDateMillis,
                timestampMillis = now
            )
        )

        saveEntries(context, safeUid, entries)
        saveAuditHistory(context, safeUid, audits)
        bumpVersion()
        return Result.success(Unit)
    }

    fun toggleContactReminder(
        context: Context,
        userId: String,
        contactId: String,
        enabled: Boolean
    ) {
        val safeUid = userId.ifBlank { "default" }
        val contacts = loadContacts(context, safeUid).toMutableList()
        val idx = contacts.indexOfFirst { it.id == contactId }
        if (idx >= 0) {
            contacts[idx] = contacts[idx].copy(
                reminderEnabled = enabled,
                updatedAtMillis = System.currentTimeMillis()
            )
            saveContacts(context, safeUid, contacts)
            bumpVersion()
        }
    }

    /**
     * Computes exact financial state for a single contact:
     * Lena Hai outstanding = Original receivable entries - payments received.
     * Dena Hai outstanding = Original payable entries - payments made.
     */
    fun computeContactState(
        contact: KhataContact,
        contactEntries: List<KhataLedgerEntry>,
        nowMillis: Long = System.currentTimeMillis()
    ): KhataContactComputedState {
        val active = contactEntries.filter { !it.isDeleted }.sortedByDescending { it.txDateMillis }
        val allSorted = contactEntries.sortedByDescending { it.txDateMillis }

        val origLena = active
            .filter { it.direction == KhataDirection.LENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }
            .sumOf { it.amount }
        val payReceived = active
            .filter {
                it.direction == KhataDirection.LENA_HAI &&
                    (it.recordType == KhataRecordType.PARTIAL_PAYMENT || it.recordType == KhataRecordType.FULL_SETTLEMENT)
            }
            .sumOf { it.amount }

        val origDena = active
            .filter { it.direction == KhataDirection.DENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }
            .sumOf { it.amount }
        val payMade = active
            .filter {
                it.direction == KhataDirection.DENA_HAI &&
                    (it.recordType == KhataRecordType.PARTIAL_PAYMENT || it.recordType == KhataRecordType.FULL_SETTLEMENT)
            }
            .sumOf { it.amount }

        val netLena = max(0L, origLena - payReceived)
        val netDena = max(0L, origDena - payMade)
        val signedBalance = netLena - netDena
        val outstanding = abs(signedBalance)
        val netDir = when {
            signedBalance > 0L -> KhataDirection.LENA_HAI
            signedBalance < 0L -> KhataDirection.DENA_HAI
            else -> null
        }
        val isSettled = outstanding == 0L

        val todayStart = startOfTodayMillis(nowMillis)
        val upcomingHorizon = todayStart + (3L * 86_400_000L)

        val unsettledDueDates = if (isSettled) {
            emptyList()
        } else {
            active.filter {
                it.recordType == KhataRecordType.ORIGINAL_ENTRY &&
                    it.direction == netDir &&
                    it.dueDateMillis != null
            }.mapNotNull { it.dueDateMillis }
        }

        val earliestDue = unsettledDueDates.minOrNull()
        val isOverdue = !isSettled && earliestDue != null && earliestDue < todayStart
        val isUpcomingDue = !isSettled && !isOverdue && earliestDue != null && earliestDue <= upcomingHorizon

        val lastTxTime = active.maxOfOrNull { it.txDateMillis } ?: contact.updatedAtMillis

        return KhataContactComputedState(
            contact = contact,
            totalOriginalLenaHai = origLena,
            totalPaymentsReceived = payReceived,
            totalOriginalDenaHai = origDena,
            totalPaymentsMade = payMade,
            netSignedBalance = signedBalance,
            outstandingAmount = outstanding,
            netDirection = netDir,
            isSettled = isSettled,
            hasHistory = allSorted.isNotEmpty(),
            lastTransactionMillis = lastTxTime,
            earliestUnsettledDueDateMillis = earliestDue,
            isOverdue = isOverdue,
            isUpcomingDue = isUpcomingDue,
            overdueAmount = if (isOverdue) outstanding else 0L,
            activeEntries = active,
            allEntriesIncludingDeleted = allSorted
        )
    }

    fun getAllContactStates(
        context: Context,
        userId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): List<KhataContactComputedState> {
        val safeUid = userId.ifBlank { "default" }
        val contacts = loadContacts(context, safeUid)
        val entries = loadEntries(context, safeUid)
        val grouped = entries.groupBy { it.contactId }
        return contacts.map { contact ->
            computeContactState(contact, grouped[contact.id].orEmpty(), nowMillis)
        }
    }

    fun computeSummaryMetrics(
        contactStates: List<KhataContactComputedState>
    ): KhataSummaryMetrics {
        val lenaContacts = contactStates.filter { !it.isSettled && it.netDirection == KhataDirection.LENA_HAI }
        val denaContacts = contactStates.filter { !it.isSettled && it.netDirection == KhataDirection.DENA_HAI }
        val settledContacts = contactStates.filter { it.isSettled && it.hasHistory }
        val overdueContacts = contactStates.filter { it.isOverdue }

        val totalLena = lenaContacts.sumOf { it.outstandingAmount }
        val totalDena = denaContacts.sumOf { it.outstandingAmount }

        return KhataSummaryMetrics(
            totalLenaHai = totalLena,
            lenaHaiPeopleCount = lenaContacts.size,
            totalDenaHai = totalDena,
            denaHaiPeopleCount = denaContacts.size,
            netOutstandingBalance = totalLena - totalDena,
            totalActiveContacts = lenaContacts.size + denaContacts.size,
            totalSettledContacts = settledContacts.size,
            totalContactsCount = contactStates.size,
            overdueAmount = overdueContacts.sumOf { it.overdueAmount },
            overdueContactsCount = overdueContacts.size
        )
    }

    fun filterAndSortContacts(
        states: List<KhataContactComputedState>,
        searchQuery: String,
        filter: KhataContactFilter,
        sort: KhataContactSort
    ): List<KhataContactComputedState> {
        val q = searchQuery.trim().lowercase()
        val filtered = states.filter { state ->
            val matchesSearch = q.isEmpty() ||
                state.contact.name.lowercase().contains(q) ||
                state.contact.phone.lowercase().contains(q)
            val matchesFilter = when (filter) {
                KhataContactFilter.ALL -> true
                KhataContactFilter.LENA_HAI -> !state.isSettled && state.netDirection == KhataDirection.LENA_HAI
                KhataContactFilter.DENA_HAI -> !state.isSettled && state.netDirection == KhataDirection.DENA_HAI
                KhataContactFilter.OVERDUE -> state.isOverdue
                KhataContactFilter.SETTLED -> state.isSettled && state.hasHistory
            }
            matchesSearch && matchesFilter
        }

        return when (sort) {
            KhataContactSort.AMOUNT_HIGH_TO_LOW -> filtered.sortedWith(
                compareByDescending<KhataContactComputedState> { it.outstandingAmount }
                    .thenByDescending { it.lastTransactionMillis }
            )
            KhataContactSort.NAME_A_TO_Z -> filtered.sortedBy { it.contact.name.lowercase() }
            KhataContactSort.LATEST_ACTIVITY -> filtered.sortedByDescending { it.lastTransactionMillis }
        }
    }

    fun filterHistoryItems(
        items: List<KhataHistoryAuditItem>,
        contactStatesById: Map<String, KhataContactComputedState>,
        searchQuery: String,
        filter: KhataHistoryFilter,
        startDateMillis: Long? = null,
        endDateMillis: Long? = null
    ): List<KhataHistoryAuditItem> {
        val q = searchQuery.trim().lowercase()
        return items.filter { item ->
            val matchesQuery = q.isEmpty() ||
                item.contactName.lowercase().contains(q) ||
                item.description.lowercase().contains(q) ||
                item.category.lowercase().contains(q)
            val contactState = contactStatesById[item.contactId]
            val matchesFilter = when (filter) {
                KhataHistoryFilter.ALL -> true
                KhataHistoryFilter.LENA_HAI ->
                    item.direction == KhataDirection.LENA_HAI && item.recordType == KhataRecordType.ORIGINAL_ENTRY
                KhataHistoryFilter.DENA_HAI ->
                    item.direction == KhataDirection.DENA_HAI && item.recordType == KhataRecordType.ORIGINAL_ENTRY
                KhataHistoryFilter.PAYMENTS_RECEIVED ->
                    item.direction == KhataDirection.LENA_HAI &&
                        (item.recordType == KhataRecordType.PARTIAL_PAYMENT || item.recordType == KhataRecordType.FULL_SETTLEMENT)
                KhataHistoryFilter.PAYMENTS_MADE ->
                    item.direction == KhataDirection.DENA_HAI &&
                        (item.recordType == KhataRecordType.PARTIAL_PAYMENT || item.recordType == KhataRecordType.FULL_SETTLEMENT)
                KhataHistoryFilter.SETTLED ->
                    item.paymentStatus == "SETTLED" || (contactState?.isSettled == true)
                KhataHistoryFilter.OVERDUE ->
                    item.paymentStatus == "OVERDUE" || (contactState?.isOverdue == true)
                KhataHistoryFilter.CUSTOM_DATE_RANGE -> {
                    val afterStart = startDateMillis == null || item.timestampMillis >= startDateMillis
                    val beforeEnd = endDateMillis == null || item.timestampMillis <= endDateMillis
                    afterStart && beforeEnd
                }
            }
            matchesQuery && matchesFilter
        }.sortedByDescending { it.timestampMillis }
    }

    /**
     * Builds the unified chronological activity stream across:
     * 1. Income
     * 2. Expenses
     * 3. Bill payments
     * 4. KHATA entries
     * 5. KHATA payments
     */
    fun buildUnifiedRecentHistory(
        expenses: List<ExpenseRecord>,
        paymentReminders: List<PaymentReminderRecord>,
        khataAuditItems: List<KhataHistoryAuditItem>
    ): List<UnifiedRecentActivityItem> {
        val list = mutableListOf<UnifiedRecentActivityItem>()

        expenses.forEach { tx ->
            val isInc = isIncomeTransaction(tx)
            list.add(
                UnifiedRecentActivityItem(
                    id = "tx_${tx.id}_${tx.txEpochMillis}",
                    title = tx.title.ifBlank { if (isInc) "Income" else "Expense" },
                    subtitle = "${tx.category.ifBlank { "General" }} · ${tx.paymentMode}",
                    kind = if (isInc) UnifiedActivityKind.INCOME else UnifiedActivityKind.EXPENSE,
                    amount = tx.amount,
                    isPositiveInflow = isInc,
                    timestampMillis = tx.txEpochMillis,
                    statusBadge = if (isInc) "INCOME" else "EXPENSE"
                )
            )
        }

        paymentReminders.filter { it.isPaid }.forEach { bill ->
            val ts = bill.updatedAt?.toDate()?.time ?: bill.dueEpochMillis
            list.add(
                UnifiedRecentActivityItem(
                    id = "bill_${bill.id}_$ts",
                    title = bill.title,
                    subtitle = "Bill Payment · ${bill.category}",
                    kind = UnifiedActivityKind.BILL_PAYMENT,
                    amount = bill.amount,
                    isPositiveInflow = false,
                    timestampMillis = ts,
                    statusBadge = "BILL PAID"
                )
            )
        }

        khataAuditItems
            .filter { it.action != KhataAuditAction.DELETED_ENTRY }
            .forEach { k ->
                val kind = when {
                    k.recordType == KhataRecordType.FULL_SETTLEMENT -> UnifiedActivityKind.KHATA_SETTLEMENT
                    k.recordType == KhataRecordType.PARTIAL_PAYMENT && k.direction == KhataDirection.LENA_HAI ->
                        UnifiedActivityKind.KHATA_PAYMENT_RECEIVED
                    k.recordType == KhataRecordType.PARTIAL_PAYMENT && k.direction == KhataDirection.DENA_HAI ->
                        UnifiedActivityKind.KHATA_PAYMENT_MADE
                    k.direction == KhataDirection.LENA_HAI -> UnifiedActivityKind.KHATA_LENA_ENTRY
                    else -> UnifiedActivityKind.KHATA_DENA_ENTRY
                }
                val isPositive = k.direction == KhataDirection.LENA_HAI
                list.add(
                    UnifiedRecentActivityItem(
                        id = "khata_${k.id}",
                        title = k.contactName,
                        subtitle = "${kind.badgeLabel} · ${k.description}",
                        kind = kind,
                        amount = k.amount,
                        isPositiveInflow = isPositive,
                        timestampMillis = k.timestampMillis,
                        statusBadge = k.paymentStatus
                    )
                )
            }

        return list.sortedByDescending { it.timestampMillis }
    }

    fun clearAllForUser(context: Context, userId: String) {
        val safeUid = userId.ifBlank { "default" }
        prefs(context).edit()
            .remove(contactsKey(safeUid))
            .remove(entriesKey(safeUid))
            .remove(auditKey(safeUid))
            .apply()
        bumpVersion()
    }
}
