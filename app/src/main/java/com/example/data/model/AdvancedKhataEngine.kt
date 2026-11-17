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
    ARCHIVED_ENTRY("Entry Archived"),
    ATTACHED_PROOF("Proof Attached"),
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
    REPAYMENTS("Repayments"),
    PAYMENTS_RECEIVED("Payments Received"),
    PAYMENTS_MADE("Payments Made"),
    SETTLED("Settled"),
    PENDING("Pending"),
    OVERDUE("Overdue"),
    ARCHIVED("Archived"),
    YEAR_WISE("Year-wise"),
    MONTH_WISE("Month-wise"),
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
    val isArchived: Boolean = false,
    val isEdited: Boolean = false,
    val previousAmountBeforeEdit: Long? = null,
    val previousDescriptionBeforeEdit: String? = null,
    val editCount: Int = 0,
    val proofAttachmentUri: String = "",
    val proofAttachmentLabel: String = "",
    val idempotencyKey: String = "",
    val syncStatus: String = "SYNCED",
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
    val paymentStatus: String, // "UNPAID", "PARTIALLY_PAID", "SETTLED", "OVERDUE", "EDITED", "ARCHIVED", "VOIDED"
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
 * PHASE B — Personal Khata Universe: Lifetime Khata Profile Metrics for an individual contact.
 */
data class LifetimeContactProfileMetrics(
    val contactId: String,
    val contactName: String,
    val firstRecordedDateMillis: Long?,
    val mostRecentDateMillis: Long?,
    val relationshipStartBanner: String,
    val totalMoneyGiven: Long,
    val totalMoneyReceived: Long,
    val totalMoneyLent: Long,
    val totalMoneyBorrowed: Long,
    val currentOutstandingBalance: Long,
    val currentDirection: KhataDirection?,
    val totalTransactionsCount: Int,
    val totalRepaymentsCount: Int,
    val totalRepaymentsAmount: Long,
    val totalSettlementsCount: Int,
    val archivedEntriesCount: Int,
    val editedEntriesCount: Int,
    val proofAttachmentsCount: Int
)

/**
 * PHASE C — Period (Year-wise or Month-wise) Khata Financial Activity breakdown.
 */
data class PeriodKhataActivity(
    val periodKey: String,
    val periodLabel: String,
    val year: Int,
    val month: Int? = null,
    val totalLent: Long,
    val totalBorrowed: Long,
    val totalRepaymentsReceived: Long,
    val totalRepaymentsMade: Long,
    val transactionCount: Int,
    val settlementCount: Int
) {
    val totalMoneyGiven: Long get() = totalLent + totalRepaymentsMade
    val totalMoneyReceived: Long get() = totalBorrowed + totalRepaymentsReceived
}

data class KhataBalanceTrendPoint(
    val timestampMillis: Long,
    val label: String,
    val runningNetSignedBalance: Long,
    val deltaAmount: Long,
    val contactName: String
)

/**
 * PHASE C — Lifetime Financial Intelligence Summary (strictly separated from personal spending/income).
 */
data class LifetimeKhataIntelligenceSummary(
    val lifetimeTotalGiven: Long = 0L,
    val lifetimeTotalReceived: Long = 0L,
    val totalMoneyLent: Long = 0L,
    val totalMoneyBorrowed: Long = 0L,
    val totalAmountRepaid: Long = 0L,
    val totalRepaymentsReceived: Long = 0L,
    val totalRepaymentsMade: Long = 0L,
    val currentOutstandingBalance: Long = 0L,
    val netPositionSigned: Long = 0L,
    val totalTransactionsCount: Int = 0,
    val totalSettlementsCount: Int = 0,
    val firstEverTransactionMillis: Long? = null,
    val yearlyActivity: List<PeriodKhataActivity> = emptyList(),
    val monthlyActivity: List<PeriodKhataActivity> = emptyList(),
    val balanceTrendPoints: List<KhataBalanceTrendPoint> = emptyList(),
    val jarvisIntelligenceSummary: String = ""
)

/**
 * PHASE E — Smart Settle recommendation item.
 */
data class SmartSettleRecommendation(
    val contactId: String,
    val contactName: String,
    val phone: String,
    val direction: KhataDirection,
    val outstandingAmount: Long,
    val priorityReason: String,
    val suggestedActionLabel: String,
    val isOverdue: Boolean
)

/**
 * PHASE E — Voice Entry Draft for confirmation before saving.
 */
data class VoiceKhataDraft(
    val rawTranscript: String,
    val personName: String,
    val amount: Long,
    val direction: KhataDirection,
    val isRepayment: Boolean,
    val description: String,
    val category: String,
    val isValid: Boolean,
    val parseSummary: String
)

/**
 * PHASE E — Khata Calendar event (recorded transaction or upcoming/overdue due date).
 */
data class KhataCalendarEvent(
    val id: String,
    val dateMillis: Long,
    val dateLabel: String,
    val contactId: String,
    val contactName: String,
    val amount: Long,
    val direction: KhataDirection,
    val isDueReminder: Boolean,
    val isOverdue: Boolean,
    val title: String,
    val subtitle: String
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

    private val _cloudSyncStatusFlow = MutableStateFlow("Lifetime Memory Ready")
    val cloudSyncStatusFlow: StateFlow<String> = _cloudSyncStatusFlow.asStateFlow()

    private var lastSubmissionHash: String = ""
    private var lastSubmissionTimeMs: Long = 0L

    /**
     * Optional listener invoked whenever a contact or entry is created or updated locally so
     * that the repository / ViewModel can immediately persist it to Firebase Firestore.
     */
    @Volatile
    var onCloudWriteHook: ((userId: String, contact: KhataContact?, entry: KhataLedgerEntry?) -> Unit)? = null

    private fun bumpVersion() {
        _versionFlow.value = System.currentTimeMillis()
    }

    fun updateCloudSyncStatus(status: String) {
        _cloudSyncStatusFlow.value = status
    }

    fun notifyExternalRestore() {
        bumpVersion()
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

    fun formatShortDate(epochMillis: Long): String {
        return try {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
            sdf.format(java.util.Date(epochMillis))
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * PHASE A — Safe idempotent migration from "default" local slot to authenticated userId slot,
     * ensuring zero data loss if a user recorded entries before signing in or upgrading.
     */
    @Synchronized
    fun migrateDefaultSlotToUserIfNeeded(context: Context, userId: String) {
        val safeUid = userId.trim()
        if (safeUid.isEmpty() || safeUid == "default") return
        val defaultContacts = loadContactsRaw(context, "default")
        val defaultEntries = loadEntriesRaw(context, "default")
        if (defaultContacts.isEmpty() && defaultEntries.isEmpty()) return

        val userContacts = loadContactsRaw(context, safeUid).toMutableList()
        val userEntries = loadEntriesRaw(context, safeUid).toMutableList()
        var changed = false

        defaultContacts.forEach { dc ->
            if (userContacts.none { it.id == dc.id || it.name.equals(dc.name, ignoreCase = true) }) {
                userContacts.add(dc.copy(userId = safeUid))
                changed = true
            }
        }
        defaultEntries.forEach { de ->
            if (userEntries.none { it.id == de.id }) {
                userEntries.add(de.copy(userId = safeUid))
                changed = true
            }
        }
        if (changed) {
            saveContacts(context, safeUid, userContacts)
            saveEntries(context, safeUid, userEntries)
            bumpVersion()
        }
    }

    private fun loadContactsRaw(context: Context, safeUid: String): List<KhataContact> {
        val raw = prefs(context).getString(contactsKey(safeUid), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val recordUid = obj.optString("userId", safeUid).ifBlank { safeUid }
                    val contactId = obj.optString("id").ifBlank { obj.optString("contactId") }
                    val name = obj.optString("name").trim()
                    if (contactId.isBlank() || name.isBlank()) continue
                    val created = obj.optLong("createdAtMillis", System.currentTimeMillis()).let {
                        if (it > 0L) it else System.currentTimeMillis()
                    }
                    val updated = obj.optLong("updatedAtMillis", created).let {
                        if (it > 0L) it else created
                    }
                    add(
                        KhataContact(
                            id = contactId,
                            userId = safeUid,
                            name = name,
                            phone = obj.optString("phone", ""),
                            avatarInitials = obj.optString("avatarInitials", computeInitials(name)).ifBlank { computeInitials(name) },
                            notes = obj.optString("notes", ""),
                            reminderEnabled = obj.optBoolean("reminderEnabled", true),
                            createdAtMillis = if (recordUid == safeUid) created else created,
                            updatedAtMillis = updated
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun loadContacts(context: Context, userId: String): List<KhataContact> {
        val safeUid = userId.ifBlank { "default" }
        if (safeUid != "default") {
            migrateDefaultSlotToUserIfNeeded(context, safeUid)
        }
        return loadContactsRaw(context, safeUid)
    }

    private fun saveContacts(context: Context, userId: String, contacts: List<KhataContact>) {
        val safeUid = userId.ifBlank { "default" }
        val arr = JSONArray()
        contacts.filter { it.userId == safeUid }.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("contactId", c.id)
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

    private fun loadEntriesRaw(context: Context, safeUid: String): List<KhataLedgerEntry> {
        val raw = prefs(context).getString(entriesKey(safeUid), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val entryId = obj.optString("id").ifBlank { obj.optString("entryId") }
                    val contactId = obj.optString("contactId")
                    if (entryId.isBlank() || contactId.isBlank()) continue
                    val dueRaw = obj.optLong("dueDateMillis", -1L)
                    val prevAmtRaw = obj.optLong("previousAmountBeforeEdit", -1L)
                    val prevDescRaw = obj.optString("previousDescriptionBeforeEdit", "")
                    val parentRaw = obj.optString("parentEntryId", "")
                    val txMillis = obj.optLong("txDateMillis", System.currentTimeMillis()).let {
                        if (it > 0L) it else System.currentTimeMillis()
                    }
                    val createdMillis = obj.optLong("createdAtMillis", txMillis).let {
                        if (it > 0L) it else txMillis
                    }
                    val updatedMillis = obj.optLong("updatedAtMillis", createdMillis).let {
                        if (it > 0L) it else createdMillis
                    }
                    val isDel = obj.optBoolean("isDeleted", false) || obj.optBoolean("deleted", false)
                    val isArch = obj.optBoolean("isArchived", false) || obj.optBoolean("archived", false)
                    val isEd = obj.optBoolean("isEdited", false) || obj.optBoolean("edited", false)
                    add(
                        KhataLedgerEntry(
                            id = entryId,
                            userId = safeUid,
                            contactId = contactId,
                            contactName = obj.optString("contactName", "Contact"),
                            direction = runCatching {
                                KhataDirection.valueOf(obj.optString("direction", KhataDirection.LENA_HAI.name))
                            }.getOrDefault(KhataDirection.LENA_HAI),
                            recordType = runCatching {
                                KhataRecordType.valueOf(obj.optString("recordType", KhataRecordType.ORIGINAL_ENTRY.name))
                            }.getOrDefault(KhataRecordType.ORIGINAL_ENTRY),
                            parentEntryId = parentRaw.ifBlank { null },
                            amount = obj.optLong("amount", 0L).coerceAtLeast(0L),
                            txDateMillis = txMillis,
                            dueDateMillis = if (dueRaw > 0L) dueRaw else null,
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Personal Udhaar").ifBlank { "Personal Udhaar" },
                            notes = obj.optString("notes", ""),
                            isDeleted = isDel,
                            isArchived = isArch,
                            isEdited = isEd,
                            previousAmountBeforeEdit = if (prevAmtRaw > 0L) prevAmtRaw else null,
                            previousDescriptionBeforeEdit = prevDescRaw.ifBlank { null },
                            editCount = obj.optInt("editCount", if (isEd) 1 else 0),
                            proofAttachmentUri = obj.optString("proofAttachmentUri", ""),
                            proofAttachmentLabel = obj.optString("proofAttachmentLabel", ""),
                            idempotencyKey = obj.optString("idempotencyKey", ""),
                            syncStatus = obj.optString("syncStatus", "SYNCED"),
                            createdAtMillis = createdMillis,
                            updatedAtMillis = updatedMillis
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun loadEntries(context: Context, userId: String): List<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        if (safeUid != "default") {
            migrateDefaultSlotToUserIfNeeded(context, safeUid)
        }
        return loadEntriesRaw(context, safeUid)
    }

    private fun saveEntries(context: Context, userId: String, entries: List<KhataLedgerEntry>) {
        val safeUid = userId.ifBlank { "default" }
        val arr = JSONArray()
        entries.filter { it.userId == safeUid }.forEach { e ->
            arr.put(
                JSONObject().apply {
                    put("id", e.id)
                    put("entryId", e.id)
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
                    put("deleted", e.isDeleted)
                    put("isArchived", e.isArchived)
                    put("archived", e.isArchived)
                    put("isEdited", e.isEdited)
                    put("edited", e.isEdited)
                    put("previousAmountBeforeEdit", e.previousAmountBeforeEdit ?: -1L)
                    put("previousDescriptionBeforeEdit", e.previousDescriptionBeforeEdit ?: "")
                    put("editCount", e.editCount)
                    put("proofAttachmentUri", e.proofAttachmentUri)
                    put("proofAttachmentLabel", e.proofAttachmentLabel)
                    put("idempotencyKey", e.idempotencyKey)
                    put("syncStatus", e.syncStatus)
                    put("createdAtMillis", e.createdAtMillis)
                    put("updatedAtMillis", e.updatedAtMillis)
                }
            )
        }
        prefs(context).edit().putString(entriesKey(safeUid), arr.toString()).apply()
    }

    fun loadAuditHistory(context: Context, userId: String): List<KhataHistoryAuditItem> {
        val safeUid = userId.ifBlank { "default" }
        val raw = prefs(context).getString(auditKey(safeUid), null)
        val parsed = if (raw != null) {
            try {
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
                }
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        // Ensure every ledger entry is represented in the timeline even if restored from cloud without local audit logs
        val entries = loadEntries(context, safeUid)
        if (entries.isEmpty()) return parsed.sortedByDescending { it.timestampMillis }
        val existingEntryIds = parsed.map { it.entryId }.toSet()
        val missingEntries = entries.filter { it.id !in existingEntryIds }
        if (missingEntries.isEmpty()) return parsed.sortedByDescending { it.timestampMillis }

        val contactsById = loadContacts(context, safeUid).associateBy { it.id }
        val synthesized = missingEntries.map { entry ->
            val contact = contactsById[entry.contactId]
            val state = if (contact != null) {
                computeContactState(contact, entries.filter { it.contactId == contact.id })
            } else null
            val status = when {
                entry.isDeleted -> "VOIDED"
                entry.isArchived -> "ARCHIVED"
                entry.isEdited -> "EDITED"
                state?.isSettled == true -> "SETTLED"
                state?.isOverdue == true -> "OVERDUE"
                entry.recordType == KhataRecordType.PARTIAL_PAYMENT -> "PARTIALLY_PAID"
                else -> "UNPAID"
            }
            val action = when {
                entry.isDeleted -> KhataAuditAction.DELETED_ENTRY
                entry.isArchived -> KhataAuditAction.ARCHIVED_ENTRY
                entry.recordType == KhataRecordType.FULL_SETTLEMENT -> KhataAuditAction.FULL_SETTLEMENT
                entry.recordType == KhataRecordType.PARTIAL_PAYMENT -> KhataAuditAction.RECORDED_PAYMENT
                else -> KhataAuditAction.CREATED_ENTRY
            }
            KhataHistoryAuditItem(
                id = "audit_synth_${entry.id}",
                userId = safeUid,
                contactId = entry.contactId,
                contactName = entry.contactName,
                entryId = entry.id,
                direction = entry.direction,
                recordType = entry.recordType,
                action = action,
                amount = entry.amount,
                remainingBalanceAfter = state?.outstandingAmount ?: 0L,
                paymentStatus = status,
                description = entry.description,
                category = entry.category,
                dueDateMillis = entry.dueDateMillis,
                timestampMillis = entry.txDateMillis
            )
        }
        return (parsed + synthesized).sortedByDescending { it.timestampMillis }
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
     * PHASE A — Bidirectional Non-Destructive Cloud Merge:
     * Merges contacts & entries from Firebase Firestore into the local store without ever deleting
     * or overwriting newer local records, and returns any local-only records that need to be pushed to Firestore.
     */
    @Synchronized
    fun mergeCloudSnapshot(
        context: Context,
        userId: String,
        cloudContacts: List<KhataContact>,
        cloudEntries: List<KhataLedgerEntry>
    ): Pair<List<KhataContact>, List<KhataLedgerEntry>> {
        val safeUid = userId.ifBlank { "default" }
        migrateDefaultSlotToUserIfNeeded(context, safeUid)

        val localContacts = loadContactsRaw(context, safeUid).toMutableList()
        val localEntries = loadEntriesRaw(context, safeUid).toMutableList()
        var modified = false

        cloudContacts.forEach { cloudContact ->
            val idx = localContacts.indexOfFirst {
                it.id == cloudContact.id || it.name.equals(cloudContact.name, ignoreCase = true)
            }
            if (idx < 0) {
                localContacts.add(cloudContact.copy(userId = safeUid))
                modified = true
            } else {
                val existing = localContacts[idx]
                if (cloudContact.updatedAtMillis > existing.updatedAtMillis) {
                    localContacts[idx] = cloudContact.copy(
                        id = existing.id,
                        userId = safeUid,
                        createdAtMillis = minOf(existing.createdAtMillis, cloudContact.createdAtMillis)
                    )
                    modified = true
                }
            }
        }

        cloudEntries.forEach { cloudEntry ->
            val idx = localEntries.indexOfFirst {
                it.id == cloudEntry.id ||
                    (cloudEntry.idempotencyKey.isNotBlank() && it.idempotencyKey == cloudEntry.idempotencyKey)
            }
            if (idx < 0) {
                localEntries.add(cloudEntry.copy(userId = safeUid, syncStatus = "SYNCED"))
                modified = true
            } else {
                val existing = localEntries[idx]
                if (cloudEntry.updatedAtMillis > existing.updatedAtMillis) {
                    localEntries[idx] = cloudEntry.copy(
                        id = existing.id,
                        userId = safeUid,
                        createdAtMillis = minOf(existing.createdAtMillis, cloudEntry.createdAtMillis),
                        syncStatus = "SYNCED"
                    )
                    modified = true
                }
            }
        }

        if (modified) {
            saveContacts(context, safeUid, localContacts)
            saveEntries(context, safeUid, localEntries)
            bumpVersion()
        }

        val cloudContactIds = cloudContacts.map { it.id }.toSet()
        val cloudEntryIds = cloudEntries.map { it.id }.toSet()
        val contactsToUpload = localContacts.filter { it.id !in cloudContactIds }
        val entriesToUpload = localEntries.filter { it.id !in cloudEntryIds }
        return contactsToUpload to entriesToUpload
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
        proofAttachmentUri: String = "",
        proofAttachmentLabel: String = "",
        idempotencyKey: String = "",
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
        val submissionKey = idempotencyKey.trim().ifBlank {
            "$safeUid|ENTRY|${cleanName.lowercase()}|${direction.name}|$amount|${description.trim()}"
        }
        if (!bypassDedupWindow &&
            submissionKey == lastSubmissionHash &&
            (now - lastSubmissionTimeMs) < DEDUP_WINDOW_MS
        ) {
            return Result.failure(IllegalStateException("Duplicate entry prevented"))
        }

        val contacts = loadContacts(context, safeUid).toMutableList()
        val entries = loadEntries(context, safeUid).toMutableList()
        val audits = loadAuditHistory(context, safeUid).toMutableList()

        if (idempotencyKey.isNotBlank()) {
            val existingByIdempotency = entries.find { it.idempotencyKey == idempotencyKey.trim() && !it.isDeleted }
            if (existingByIdempotency != null) {
                return Result.failure(IllegalStateException("Duplicate entry prevented"))
            }
        }

        lastSubmissionHash = submissionKey
        lastSubmissionTimeMs = now

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
            proofAttachmentUri = proofAttachmentUri.trim(),
            proofAttachmentLabel = proofAttachmentLabel.trim(),
            idempotencyKey = submissionKey.take(160),
            syncStatus = "SYNCED",
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
        onCloudWriteHook?.invoke(safeUid, contact, newEntry)
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
        idempotencyKey: String = "",
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
        val submissionKey = idempotencyKey.trim().ifBlank {
            "$safeUid|PAYMENT|$contactId|$paymentAmount"
        }
        if (!bypassDedupWindow &&
            submissionKey == lastSubmissionHash &&
            (now - lastSubmissionTimeMs) < DEDUP_WINDOW_MS
        ) {
            return Result.failure(IllegalStateException("Duplicate payment submission prevented"))
        }
        if (idempotencyKey.isNotBlank()) {
            val existingByIdem = entries.find { it.idempotencyKey == idempotencyKey.trim() && !it.isDeleted }
            if (existingByIdem != null) {
                return Result.failure(IllegalStateException("Duplicate payment submission prevented"))
            }
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
            idempotencyKey = submissionKey.take(160),
            syncStatus = "SYNCED",
            createdAtMillis = now,
            updatedAtMillis = now
        )
        entries.add(paymentEntry)
        val updatedContact = contact.copy(updatedAtMillis = now)
        contacts[contactIdx] = updatedContact

        val updatedState = computeContactState(updatedContact, entries.filter { it.contactId == contact.id })
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
        onCloudWriteHook?.invoke(safeUid, updatedContact, paymentEntry)
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
     * Preserves original createdAtMillis, previousAmountBeforeEdit, previousDescriptionBeforeEdit, and editCount.
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
        val now = max(System.currentTimeMillis(), old.updatedAtMillis + 1L)
        val updated = old.copy(
            amount = newAmount,
            description = newDescription.trim().ifBlank { old.description },
            category = newCategory.trim().ifBlank { old.category },
            dueDateMillis = newDueDateMillis,
            notes = newNotes.trim(),
            isEdited = true,
            previousAmountBeforeEdit = old.amount,
            previousDescriptionBeforeEdit = old.description,
            editCount = old.editCount + 1,
            createdAtMillis = old.createdAtMillis, // strictly immutable origin timestamp
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
        onCloudWriteHook?.invoke(safeUid, contact, updated)
        return Result.success(updated)
    }

    /**
     * Soft-archives or unarchives an entry without permanently deleting it.
     */
    @Synchronized
    fun archiveEntry(
        context: Context,
        userId: String,
        entryId: String,
        archived: Boolean = true
    ): Result<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        val contacts = loadContacts(context, safeUid)
        val entries = loadEntries(context, safeUid).toMutableList()
        val audits = loadAuditHistory(context, safeUid).toMutableList()

        val idx = entries.indexOfFirst { it.id == entryId }
        if (idx < 0) {
            return Result.failure(IllegalArgumentException("Entry not found"))
        }
        val target = entries[idx]
        val now = max(System.currentTimeMillis(), target.updatedAtMillis + 1L)
        val updated = target.copy(
            isArchived = archived,
            createdAtMillis = target.createdAtMillis,
            updatedAtMillis = now
        )
        entries[idx] = updated

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
                action = KhataAuditAction.ARCHIVED_ENTRY,
                amount = target.amount,
                remainingBalanceAfter = remainingAfter,
                paymentStatus = if (archived) "ARCHIVED" else "UNPAID",
                description = if (archived) "Archived: ${target.description}" else "Restored from Archive: ${target.description}",
                category = target.category,
                dueDateMillis = target.dueDateMillis,
                timestampMillis = now
            )
        )

        saveEntries(context, safeUid, entries)
        saveAuditHistory(context, safeUid, audits)
        bumpVersion()
        onCloudWriteHook?.invoke(safeUid, contact, updated)
        return Result.success(updated)
    }

    /**
     * PHASE E — Proof Vault: Attaches a receipt image or document URI + label to an existing Khata entry.
     */
    @Synchronized
    fun attachProofToEntry(
        context: Context,
        userId: String,
        entryId: String,
        proofUri: String,
        proofLabel: String = "Receipt Proof"
    ): Result<KhataLedgerEntry> {
        val safeUid = userId.ifBlank { "default" }
        val contacts = loadContacts(context, safeUid)
        val entries = loadEntries(context, safeUid).toMutableList()
        val audits = loadAuditHistory(context, safeUid).toMutableList()

        val idx = entries.indexOfFirst { it.id == entryId }
        if (idx < 0) {
            return Result.failure(IllegalArgumentException("Entry not found"))
        }
        val target = entries[idx]
        val now = max(System.currentTimeMillis(), target.updatedAtMillis + 1L)
        val cleanLabel = proofLabel.trim().ifBlank { "Receipt Proof" }
        val updated = target.copy(
            proofAttachmentUri = proofUri.trim(),
            proofAttachmentLabel = cleanLabel,
            createdAtMillis = target.createdAtMillis,
            updatedAtMillis = now
        )
        entries[idx] = updated

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
                action = KhataAuditAction.ATTACHED_PROOF,
                amount = target.amount,
                remainingBalanceAfter = remainingAfter,
                paymentStatus = "PROOF_ATTACHED",
                description = "${target.description} [Proof: $cleanLabel]",
                category = target.category,
                dueDateMillis = target.dueDateMillis,
                timestampMillis = now
            )
        )

        saveEntries(context, safeUid, entries)
        saveAuditHistory(context, safeUid, audits)
        bumpVersion()
        onCloudWriteHook?.invoke(safeUid, contact, updated)
        return Result.success(updated)
    }

    /**
     * Soft-deletes/archives an entry (with audit trail preservation; never hard-erases historical record)
     * and recalculates all balances.
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
        val now = max(System.currentTimeMillis(), target.updatedAtMillis + 1L)
        val updated = target.copy(
            isDeleted = true,
            isArchived = true,
            createdAtMillis = target.createdAtMillis,
            updatedAtMillis = now
        )
        entries[idx] = updated

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
                description = "Archived/Voided: ${target.description}",
                category = target.category,
                dueDateMillis = target.dueDateMillis,
                timestampMillis = now
            )
        )

        saveEntries(context, safeUid, entries)
        saveAuditHistory(context, safeUid, audits)
        bumpVersion()
        onCloudWriteHook?.invoke(safeUid, contact, updated)
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
            val updated = contacts[idx].copy(
                reminderEnabled = enabled,
                updatedAtMillis = System.currentTimeMillis()
            )
            contacts[idx] = updated
            saveContacts(context, safeUid, contacts)
            bumpVersion()
            onCloudWriteHook?.invoke(safeUid, updated, null)
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
        val active = contactEntries.filter { !it.isDeleted && !it.isArchived }.sortedByDescending { it.txDateMillis }
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

    /**
     * PHASE B — Computes the LIFETIME KHATA PROFILE for an individual contact using their actual
     * earliest and latest recorded transactions, lifetime lent/borrowed/given/received totals,
     * repayments, and settlements.
     */
    fun computeLifetimeContactProfile(
        state: KhataContactComputedState
    ): LifetimeContactProfileMetrics {
        val active = state.activeEntries
        val allEntries = state.allEntriesIncludingDeleted

        val firstMillis = active.minOfOrNull { it.txDateMillis }
            ?: allEntries.minOfOrNull { it.txDateMillis }
        val latestMillis = active.maxOfOrNull { it.txDateMillis }
            ?: allEntries.maxOfOrNull { it.txDateMillis }

        val relationshipStartBanner = if (firstMillis != null) {
            "Your financial history with ${state.contact.name} started on ${formatShortDate(firstMillis)}."
        } else {
            "Your financial history with ${state.contact.name} started on ${formatShortDate(state.contact.createdAtMillis)}."
        }

        val totalMoneyLent = active
            .filter { it.direction == KhataDirection.LENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }
            .sumOf { it.amount }
        val totalMoneyBorrowed = active
            .filter { it.direction == KhataDirection.DENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }
            .sumOf { it.amount }

        val repaymentsReceived = active
            .filter {
                it.direction == KhataDirection.LENA_HAI &&
                    (it.recordType == KhataRecordType.PARTIAL_PAYMENT || it.recordType == KhataRecordType.FULL_SETTLEMENT)
            }
            .sumOf { it.amount }
        val repaymentsMade = active
            .filter {
                it.direction == KhataDirection.DENA_HAI &&
                    (it.recordType == KhataRecordType.PARTIAL_PAYMENT || it.recordType == KhataRecordType.FULL_SETTLEMENT)
            }
            .sumOf { it.amount }

        val totalMoneyGiven = totalMoneyLent + repaymentsMade
        val totalMoneyReceived = totalMoneyBorrowed + repaymentsReceived

        val repaymentEntries = active.filter {
            it.recordType == KhataRecordType.PARTIAL_PAYMENT || it.recordType == KhataRecordType.FULL_SETTLEMENT
        }
        val settlementEntries = active.filter {
            it.recordType == KhataRecordType.FULL_SETTLEMENT
        }

        return LifetimeContactProfileMetrics(
            contactId = state.contact.id,
            contactName = state.contact.name,
            firstRecordedDateMillis = firstMillis,
            mostRecentDateMillis = latestMillis,
            relationshipStartBanner = relationshipStartBanner,
            totalMoneyGiven = totalMoneyGiven,
            totalMoneyReceived = totalMoneyReceived,
            totalMoneyLent = totalMoneyLent,
            totalMoneyBorrowed = totalMoneyBorrowed,
            currentOutstandingBalance = state.outstandingAmount,
            currentDirection = state.netDirection,
            totalTransactionsCount = active.size,
            totalRepaymentsCount = repaymentEntries.size,
            totalRepaymentsAmount = repaymentsReceived + repaymentsMade,
            totalSettlementsCount = settlementEntries.size + (if (state.isSettled && state.hasHistory && settlementEntries.isEmpty()) 1 else 0),
            archivedEntriesCount = allEntries.count { it.isArchived || it.isDeleted },
            editedEntriesCount = allEntries.count { it.isEdited },
            proofAttachmentsCount = allEntries.count { it.proofAttachmentUri.isNotBlank() }
        )
    }

    /**
     * PHASE C — Computes the complete LIFETIME FINANCIAL INTELLIGENCE summary across all years
     * and months in the user's Khata ledger (strictly separate from personal spending/income).
     */
    fun computeLifetimeKhataIntelligence(
        contactStates: List<KhataContactComputedState>
    ): LifetimeKhataIntelligenceSummary {
        val allActiveEntries = contactStates
            .flatMap { it.activeEntries }
            .sortedBy { it.txDateMillis }

        val totalLent = allActiveEntries
            .filter { it.direction == KhataDirection.LENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }
            .sumOf { it.amount }
        val totalBorrowed = allActiveEntries
            .filter { it.direction == KhataDirection.DENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }
            .sumOf { it.amount }
        val repaymentsReceived = allActiveEntries
            .filter {
                it.direction == KhataDirection.LENA_HAI &&
                    (it.recordType == KhataRecordType.PARTIAL_PAYMENT || it.recordType == KhataRecordType.FULL_SETTLEMENT)
            }
            .sumOf { it.amount }
        val repaymentsMade = allActiveEntries
            .filter {
                it.direction == KhataDirection.DENA_HAI &&
                    (it.recordType == KhataRecordType.PARTIAL_PAYMENT || it.recordType == KhataRecordType.FULL_SETTLEMENT)
            }
            .sumOf { it.amount }

        val totalGiven = totalLent + repaymentsMade
        val totalReceived = totalBorrowed + repaymentsReceived
        val totalRepaid = repaymentsReceived + repaymentsMade

        val summaryMetrics = computeSummaryMetrics(contactStates)
        val currentOutstanding = summaryMetrics.totalLenaHai + summaryMetrics.totalDenaHai
        val netSigned = summaryMetrics.netOutstandingBalance

        val settlementsCount = allActiveEntries.count { it.recordType == KhataRecordType.FULL_SETTLEMENT } +
            contactStates.count { it.isSettled && it.hasHistory && it.activeEntries.none { e -> e.recordType == KhataRecordType.FULL_SETTLEMENT } }

        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        // Year-wise breakdown across all years (never resets across years!)
        val byYear = allActiveEntries.groupBy { entry ->
            Calendar.getInstance().apply { timeInMillis = entry.txDateMillis }.get(Calendar.YEAR)
        }
        val yearlyActivity = byYear.entries
            .sortedByDescending { it.key }
            .map { (year, entries) ->
                val yLent = entries.filter { it.direction == KhataDirection.LENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                val yBorrowed = entries.filter { it.direction == KhataDirection.DENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                val yRepRec = entries.filter { it.direction == KhataDirection.LENA_HAI && it.recordType != KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                val yRepMade = entries.filter { it.direction == KhataDirection.DENA_HAI && it.recordType != KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                PeriodKhataActivity(
                    periodKey = "$year",
                    periodLabel = "FY / Year $year",
                    year = year,
                    month = null,
                    totalLent = yLent,
                    totalBorrowed = yBorrowed,
                    totalRepaymentsReceived = yRepRec,
                    totalRepaymentsMade = yRepMade,
                    transactionCount = entries.size,
                    settlementCount = entries.count { it.recordType == KhataRecordType.FULL_SETTLEMENT }
                )
            }

        // Month-wise breakdown across all recorded months
        val byYearMonth = allActiveEntries.groupBy { entry ->
            val cal = Calendar.getInstance().apply { timeInMillis = entry.txDateMillis }
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            y to m
        }
        val monthlyActivity = byYearMonth.entries
            .sortedWith(compareByDescending<Map.Entry<Pair<Int, Int>, List<KhataLedgerEntry>>> { it.key.first }.thenByDescending { it.key.second })
            .map { (ym, entries) ->
                val (year, monthIdx) = ym
                val mLent = entries.filter { it.direction == KhataDirection.LENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                val mBorrowed = entries.filter { it.direction == KhataDirection.DENA_HAI && it.recordType == KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                val mRepRec = entries.filter { it.direction == KhataDirection.LENA_HAI && it.recordType != KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                val mRepMade = entries.filter { it.direction == KhataDirection.DENA_HAI && it.recordType != KhataRecordType.ORIGINAL_ENTRY }.sumOf { it.amount }
                val mName = monthNames.getOrElse(monthIdx) { "M" }
                PeriodKhataActivity(
                    periodKey = "%04d-%02d".format(year, monthIdx + 1),
                    periodLabel = "$mName $year",
                    year = year,
                    month = monthIdx + 1,
                    totalLent = mLent,
                    totalBorrowed = mBorrowed,
                    totalRepaymentsReceived = mRepRec,
                    totalRepaymentsMade = mRepMade,
                    transactionCount = entries.size,
                    settlementCount = entries.count { it.recordType == KhataRecordType.FULL_SETTLEMENT }
                )
            }

        // Running Net Balance Trend over time
        var runningSigned = 0L
        val trendPoints = allActiveEntries.map { entry ->
            val signedDelta = when {
                entry.direction == KhataDirection.LENA_HAI && entry.recordType == KhataRecordType.ORIGINAL_ENTRY -> entry.amount
                entry.direction == KhataDirection.LENA_HAI && entry.recordType != KhataRecordType.ORIGINAL_ENTRY -> -entry.amount
                entry.direction == KhataDirection.DENA_HAI && entry.recordType == KhataRecordType.ORIGINAL_ENTRY -> -entry.amount
                else -> entry.amount
            }
            runningSigned += signedDelta
            KhataBalanceTrendPoint(
                timestampMillis = entry.txDateMillis,
                label = formatShortDate(entry.txDateMillis),
                runningNetSignedBalance = runningSigned,
                deltaAmount = signedDelta,
                contactName = entry.contactName
            )
        }

        val firstEverMillis = allActiveEntries.firstOrNull()?.txDateMillis
        val jarvisSummary = if (allActiveEntries.isEmpty()) {
            "Lifetime Khata Memory is initialized. Add your first Lena Hai or Dena Hai entry to start building your permanent relationship ledger."
        } else {
            val topReceivable = contactStates
                .filter { !it.isSettled && it.netDirection == KhataDirection.LENA_HAI }
                .maxByOrNull { it.outstandingAmount }
            val topPayable = contactStates
                .filter { !it.isSettled && it.netDirection == KhataDirection.DENA_HAI }
                .maxByOrNull { it.outstandingAmount }
            buildString {
                append("Across ${allActiveEntries.size} lifetime Khata records since ${formatShortDate(firstEverMillis ?: System.currentTimeMillis())}, ")
                append("you have lent ₹$totalLent and borrowed ₹$totalBorrowed, with ₹$totalRepaid settled in repayments. ")
                if (topReceivable != null) {
                    append("Highest receivable: ${topReceivable.contact.name} (₹${topReceivable.outstandingAmount} Lena Hai). ")
                }
                if (topPayable != null) {
                    append("Highest payable: ${topPayable.contact.name} (₹${topPayable.outstandingAmount} Dena Hai).")
                }
            }.trim()
        }

        return LifetimeKhataIntelligenceSummary(
            lifetimeTotalGiven = totalGiven,
            lifetimeTotalReceived = totalReceived,
            totalMoneyLent = totalLent,
            totalMoneyBorrowed = totalBorrowed,
            totalAmountRepaid = totalRepaid,
            totalRepaymentsReceived = repaymentsReceived,
            totalRepaymentsMade = repaymentsMade,
            currentOutstandingBalance = currentOutstanding,
            netPositionSigned = netSigned,
            totalTransactionsCount = allActiveEntries.size,
            totalSettlementsCount = settlementsCount,
            firstEverTransactionMillis = firstEverMillis,
            yearlyActivity = yearlyActivity,
            monthlyActivity = monthlyActivity,
            balanceTrendPoints = trendPoints,
            jarvisIntelligenceSummary = jarvisSummary
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
                state.contact.phone.lowercase().contains(q) ||
                state.contact.notes.lowercase().contains(q) ||
                state.outstandingAmount.toString().contains(q)
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
        endDateMillis: Long? = null,
        selectedYear: Int? = null,
        selectedMonthOneBased: Int? = null
    ): List<KhataHistoryAuditItem> {
        val q = searchQuery.trim().lowercase().removePrefix("₹").replace(",", "")
        return items.filter { item ->
            val dateStr = formatShortDate(item.timestampMillis).lowercase()
            val matchesQuery = q.isEmpty() ||
                item.contactName.lowercase().contains(q) ||
                item.description.lowercase().contains(q) ||
                item.category.lowercase().contains(q) ||
                item.amount.toString().contains(q) ||
                dateStr.contains(q)
            val contactState = contactStatesById[item.contactId]
            val cal = Calendar.getInstance().apply { timeInMillis = item.timestampMillis }
            val itemYear = cal.get(Calendar.YEAR)
            val itemMonth = cal.get(Calendar.MONTH) + 1

            val matchesFilter = when (filter) {
                KhataHistoryFilter.ALL -> item.paymentStatus != "VOIDED"
                KhataHistoryFilter.LENA_HAI ->
                    item.direction == KhataDirection.LENA_HAI && item.recordType == KhataRecordType.ORIGINAL_ENTRY && item.paymentStatus != "VOIDED"
                KhataHistoryFilter.DENA_HAI ->
                    item.direction == KhataDirection.DENA_HAI && item.recordType == KhataRecordType.ORIGINAL_ENTRY && item.paymentStatus != "VOIDED"
                KhataHistoryFilter.REPAYMENTS ->
                    item.recordType == KhataRecordType.PARTIAL_PAYMENT || item.recordType == KhataRecordType.FULL_SETTLEMENT
                KhataHistoryFilter.PAYMENTS_RECEIVED ->
                    item.direction == KhataDirection.LENA_HAI &&
                        (item.recordType == KhataRecordType.PARTIAL_PAYMENT || item.recordType == KhataRecordType.FULL_SETTLEMENT)
                KhataHistoryFilter.PAYMENTS_MADE ->
                    item.direction == KhataDirection.DENA_HAI &&
                        (item.recordType == KhataRecordType.PARTIAL_PAYMENT || item.recordType == KhataRecordType.FULL_SETTLEMENT)
                KhataHistoryFilter.SETTLED ->
                    item.paymentStatus == "SETTLED" || item.recordType == KhataRecordType.FULL_SETTLEMENT || (contactState?.isSettled == true)
                KhataHistoryFilter.PENDING ->
                    item.paymentStatus != "VOIDED" && item.paymentStatus != "ARCHIVED" && (contactState?.isSettled == false)
                KhataHistoryFilter.OVERDUE ->
                    item.paymentStatus == "OVERDUE" || (contactState?.isOverdue == true)
                KhataHistoryFilter.ARCHIVED ->
                    item.paymentStatus == "ARCHIVED" || item.paymentStatus == "VOIDED" || item.action == KhataAuditAction.ARCHIVED_ENTRY || item.action == KhataAuditAction.DELETED_ENTRY
                KhataHistoryFilter.YEAR_WISE ->
                    selectedYear == null || itemYear == selectedYear
                KhataHistoryFilter.MONTH_WISE ->
                    (selectedYear == null || itemYear == selectedYear) &&
                        (selectedMonthOneBased == null || itemMonth == selectedMonthOneBased)
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
     * PHASE E — Smart Settle: Ranks and simplifies unsettled balances into clear, high-priority
     * settlement actions (Overdue first, then smallest quick-clear balances, then largest balances).
     */
    fun computeSmartSettleRecommendations(
        states: List<KhataContactComputedState>
    ): List<SmartSettleRecommendation> {
        val activeUnsettled = states.filter { !it.isSettled && it.outstandingAmount > 0L && it.netDirection != null }
        return activeUnsettled
            .sortedWith(
                compareByDescending<KhataContactComputedState> { it.isOverdue }
                    .thenBy { it.outstandingAmount }
            )
            .map { state ->
                val dir = state.netDirection ?: KhataDirection.LENA_HAI
                val reason = when {
                    state.isOverdue -> "Overdue balance • Clear immediately"
                    state.outstandingAmount <= 500L -> "Quick micro-balance • Settle in 1 tap"
                    dir == KhataDirection.DENA_HAI -> "Pending payable • Maintain 98% Dosti Score"
                    else -> "Active receivable • Send polite nudge or record payment"
                }
                val actionLabel = if (dir == KhataDirection.LENA_HAI) {
                    "Collect ₹${state.outstandingAmount}"
                } else {
                    "Pay ₹${state.outstandingAmount}"
                }
                SmartSettleRecommendation(
                    contactId = state.contact.id,
                    contactName = state.contact.name,
                    phone = state.contact.phone,
                    direction = dir,
                    outstandingAmount = state.outstandingAmount,
                    priorityReason = reason,
                    suggestedActionLabel = actionLabel,
                    isOverdue = state.isOverdue
                )
            }
    }

    /**
     * PHASE E — Smart Nudge: Generates polite, relationship-friendly WhatsApp / SMS reminder messages.
     */
    fun buildSmartNudgeMessage(
        contactName: String,
        outstandingAmount: Long,
        tone: String = "friendly",
        myUpiId: String = ""
    ): String {
        val upiSuffix = if (myUpiId.isNotBlank()) " You can UPI directly to $myUpiId." else ""
        return when (tone.lowercase()) {
            "polite" -> "Hi $contactName! Hope you're doing great. Just a gentle note from my Kharchee Khata regarding ₹$outstandingAmount pending between us.$upiSuffix No rush, let me know whenever convenient!"
            "firm" -> "Hi $contactName, a quick reminder that ₹$outstandingAmount is due in our Khata ledger.$upiSuffix Please let me know once settled so I can update our record."
            else -> "Hey $contactName! 👋 Quick friendly reminder for ₹$outstandingAmount from our Kharchee Khata.$upiSuffix Ping me once done!"
        }
    }

    /**
     * PHASE E — Voice / Natural Language Entry Parser:
     * Converts phrases like "Gave 500 to Aman for dinner", "Lent 1200 to Rohan",
     * "Borrowed 300 from Sneha for cab", or "Aman paid back 400" into a structured VoiceKhataDraft
     * for user confirmation before saving.
     */
    fun parseVoiceEntryDraft(spokenText: String): VoiceKhataDraft {
        val clean = spokenText.trim()
        if (clean.isEmpty()) {
            return VoiceKhataDraft(
                rawTranscript = "",
                personName = "",
                amount = 0L,
                direction = KhataDirection.LENA_HAI,
                isRepayment = false,
                description = "",
                category = "Personal Udhaar",
                isValid = false,
                parseSummary = "Speak or type a phrase like: 'Gave 500 to Aman for dinner'"
            )
        }

        val lower = clean.lowercase()
        val amountMatch = Regex("""(?:₹|rs\.?|inr)?\s*(\d[\d,]*)""").find(lower)
        val parsedAmount = amountMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toLongOrNull() ?: 0L

        val isRepayment = lower.contains("paid back") ||
            lower.contains("returned") ||
            lower.contains("settled") ||
            lower.contains("received back") ||
            lower.contains("repaid")

        val direction = when {
            lower.contains("borrowed") ||
                lower.contains("took") ||
                lower.contains("owe ") ||
                lower.contains("dena hai") ||
                lower.contains("from ") && !lower.contains("received") -> KhataDirection.DENA_HAI
            else -> KhataDirection.LENA_HAI
        }

        // Extract person name after "to " or "from " or before "paid"
        val toFromRegex = Regex("""(?:to|from|with)\s+([a-zA-Z]+(?:\s+[a-zA-Z]+)?)""", RegexOption.IGNORE_CASE)
        val beforePaidRegex = Regex("""^([a-zA-Z]+)\s+(?:paid|returned|gave|owes)""", RegexOption.IGNORE_CASE)
        val rawPerson = toFromRegex.find(clean)?.groupValues?.getOrNull(1)
            ?.replace(Regex("""\b(for|on|in|at|because|via)\b.*""", RegexOption.IGNORE_CASE), "")
            ?.trim()
            ?: beforePaidRegex.find(clean)?.groupValues?.getOrNull(1)?.trim()
            ?: ""

        val personName = rawPerson.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString(" ") { part ->
                part.lowercase().replaceFirstChar { it.uppercaseChar() }
            }

        val reasonRegex = Regex("""\bfor\s+(.+)$""", RegexOption.IGNORE_CASE)
        val reason = reasonRegex.find(clean)?.groupValues?.getOrNull(1)?.trim()
            ?: clean

        val category = when {
            lower.contains("dinner") || lower.contains("lunch") || lower.contains("food") || lower.contains("chai") || lower.contains("coffee") -> "Food & Outing"
            lower.contains("cab") || lower.contains("uber") || lower.contains("ola") || lower.contains("trip") || lower.contains("flight") || lower.contains("petrol") -> "Travel & Trip"
            lower.contains("rent") || lower.contains("wifi") || lower.contains("room") || lower.contains("electricity") -> "Rent & Room Split"
            lower.contains("medical") || lower.contains("hospital") || lower.contains("urgent") -> "Emergency Help"
            else -> "Personal Udhaar"
        }

        val isValid = personName.isNotBlank() && parsedAmount > 0L
        val summary = if (isValid) {
            if (isRepayment) {
                "Repayment of ₹$parsedAmount with $personName ($reason)"
            } else {
                "${direction.label}: ₹$parsedAmount with $personName • $category"
            }
        } else {
            "Detected ₹$parsedAmount ${if (personName.isNotBlank()) "for $personName" else "(add contact name below)"} — review & confirm before saving."
        }

        return VoiceKhataDraft(
            rawTranscript = clean,
            personName = personName,
            amount = parsedAmount,
            direction = direction,
            isRepayment = isRepayment,
            description = reason,
            category = category,
            isValid = isValid,
            parseSummary = summary
        )
    }

    /**
     * PHASE E — Khata Calendar: Builds a chronological list of upcoming/overdue due dates and
     * recorded transactions for the Khata Calendar view.
     */
    fun computeKhataCalendarEvents(
        states: List<KhataContactComputedState>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<KhataCalendarEvent> {
        val events = mutableListOf<KhataCalendarEvent>()
        val todayStart = startOfTodayMillis(nowMillis)

        states.forEach { state ->
            state.activeEntries.forEach { entry ->
                if (entry.dueDateMillis != null && !state.isSettled && entry.recordType == KhataRecordType.ORIGINAL_ENTRY) {
                    val isOverdue = entry.dueDateMillis < todayStart
                    events.add(
                        KhataCalendarEvent(
                            id = "due_${entry.id}",
                            dateMillis = entry.dueDateMillis,
                            dateLabel = formatShortDate(entry.dueDateMillis),
                            contactId = state.contact.id,
                            contactName = state.contact.name,
                            amount = state.outstandingAmount,
                            direction = state.netDirection ?: entry.direction,
                            isDueReminder = true,
                            isOverdue = isOverdue,
                            title = if (isOverdue) "Overdue Reminder · ${state.contact.name}" else "Promise Due · ${state.contact.name}",
                            subtitle = entry.description
                        )
                    )
                }
                events.add(
                    KhataCalendarEvent(
                        id = "tx_${entry.id}",
                        dateMillis = entry.txDateMillis,
                        dateLabel = formatShortDate(entry.txDateMillis),
                        contactId = state.contact.id,
                        contactName = state.contact.name,
                        amount = entry.amount,
                        direction = entry.direction,
                        isDueReminder = false,
                        isOverdue = false,
                        title = "${entry.recordType.label} · ${state.contact.name}",
                        subtitle = entry.description
                    )
                )
            }
        }
        return events.sortedWith(
            compareByDescending<KhataCalendarEvent> { it.isDueReminder && it.isOverdue }
                .thenByDescending { it.isDueReminder }
                .thenByDescending { it.dateMillis }
        )
    }

    /**
     * PHASE E — Khata Passport: Generates a clean, formatted Lifetime Relationship Statement
     * for sharing or archiving.
     */
    fun generateKhataPassportText(
        state: KhataContactComputedState,
        profile: LifetimeContactProfileMetrics
    ): String {
        val statusText = when {
            state.isSettled -> "ALL SETTLED (₹0 Pending)"
            state.netDirection == KhataDirection.LENA_HAI -> "LENA HAI: ₹${state.outstandingAmount} (You Receive)"
            else -> "DENA HAI: ₹${state.outstandingAmount} (You Pay)"
        }
        return buildString {
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🏛️ KHARCHEE • LIFETIME KHATA PASSPORT")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Contact: ${state.contact.name} ${if (state.contact.phone.isNotBlank()) "(${state.contact.phone})" else ""}")
            appendLine(profile.relationshipStartBanner)
            appendLine("Current Position: $statusText")
            appendLine("──────────────────────────────────")
            appendLine("• Total Money Given: ₹${profile.totalMoneyGiven}")
            appendLine("• Total Money Received: ₹${profile.totalMoneyReceived}")
            appendLine("• Lifetime Lent (Lena Hai): ₹${profile.totalMoneyLent}")
            appendLine("• Lifetime Borrowed (Dena Hai): ₹${profile.totalMoneyBorrowed}")
            appendLine("• Total Repayments: ₹${profile.totalRepaymentsAmount} (${profile.totalRepaymentsCount} payments)")
            appendLine("• Total Transactions: ${profile.totalTransactionsCount} | Settlements: ${profile.totalSettlementsCount}")
            if (profile.firstRecordedDateMillis != null) {
                appendLine("• First Recorded Entry: ${formatShortDate(profile.firstRecordedDateMillis)}")
            }
            if (profile.mostRecentDateMillis != null) {
                appendLine("• Latest Activity: ${formatShortDate(profile.mostRecentDateMillis)}")
            }
            appendLine("──────────────────────────────────")
            appendLine("Recent Ledger Timeline:")
            state.activeEntries.take(8).forEach { e ->
                val sign = if (e.direction == KhataDirection.LENA_HAI) "+" else "-"
                appendLine("• [${formatShortDate(e.txDateMillis)}] ${e.recordType.label}: ${sign}₹${e.amount} — ${e.description}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Verified by KHARCHEE Lifetime Financial Memory")
        }.trim()
    }

    /**
     * PHASE E — Exports the user's complete Lifetime Khata Memory (contacts, entries, audit trail,
     * and lifetime intelligence summary) as a portable JSON backup string.
     */
    fun exportLifetimeKhataJsonBackup(
        context: Context,
        userId: String
    ): String {
        val safeUid = userId.ifBlank { "default" }
        val contacts = loadContacts(context, safeUid)
        val entries = loadEntries(context, safeUid)
        val states = getAllContactStates(context, safeUid)
        val intel = computeLifetimeKhataIntelligence(states)

        val root = JSONObject().apply {
            put("schemaVersion", 2)
            put("exportedAtMillis", System.currentTimeMillis())
            put("userId", safeUid)
            put("lifetimeSummary", JSONObject().apply {
                put("lifetimeTotalGiven", intel.lifetimeTotalGiven)
                put("lifetimeTotalReceived", intel.lifetimeTotalReceived)
                put("totalMoneyLent", intel.totalMoneyLent)
                put("totalMoneyBorrowed", intel.totalMoneyBorrowed)
                put("totalAmountRepaid", intel.totalAmountRepaid)
                put("currentOutstandingBalance", intel.currentOutstandingBalance)
                put("totalTransactionsCount", intel.totalTransactionsCount)
                put("totalSettlementsCount", intel.totalSettlementsCount)
            })
            put("contacts", JSONArray(prefs(context).getString(contactsKey(safeUid), "[]") ?: "[]"))
            put("entries", JSONArray(prefs(context).getString(entriesKey(safeUid), "[]") ?: "[]"))
            put("contactsCount", contacts.size)
            put("entriesCount", entries.size)
        }
        return root.toString(2)
    }

    /**
     * Restores/merges a JSON backup into the user's Lifetime Khata Memory without deleting existing records.
     */
    @Synchronized
    fun importLifetimeKhataJsonBackup(
        context: Context,
        userId: String,
        jsonBackup: String
    ): Result<Pair<Int, Int>> = runCatching {
        val safeUid = userId.ifBlank { "default" }
        val root = JSONObject(jsonBackup)
        val contactsArr = root.optJSONArray("contacts") ?: JSONArray()
        val entriesArr = root.optJSONArray("entries") ?: JSONArray()

        val importedContacts = buildList {
            for (i in 0 until contactsArr.length()) {
                val obj = contactsArr.optJSONObject(i) ?: continue
                val cid = obj.optString("id").ifBlank { obj.optString("contactId") }
                val name = obj.optString("name").trim()
                if (cid.isBlank() || name.isBlank()) continue
                add(
                    KhataContact(
                        id = cid,
                        userId = safeUid,
                        name = name,
                        phone = obj.optString("phone", ""),
                        avatarInitials = obj.optString("avatarInitials", computeInitials(name)),
                        notes = obj.optString("notes", ""),
                        reminderEnabled = obj.optBoolean("reminderEnabled", true),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                        updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
                    )
                )
            }
        }

        val importedEntries = buildList {
            for (i in 0 until entriesArr.length()) {
                val obj = entriesArr.optJSONObject(i) ?: continue
                val eid = obj.optString("id").ifBlank { obj.optString("entryId") }
                val cid = obj.optString("contactId")
                if (eid.isBlank() || cid.isBlank()) continue
                val dueRaw = obj.optLong("dueDateMillis", -1L)
                val prevAmtRaw = obj.optLong("previousAmountBeforeEdit", -1L)
                add(
                    KhataLedgerEntry(
                        id = eid,
                        userId = safeUid,
                        contactId = cid,
                        contactName = obj.optString("contactName", "Contact"),
                        direction = runCatching {
                            KhataDirection.valueOf(obj.optString("direction", KhataDirection.LENA_HAI.name))
                        }.getOrDefault(KhataDirection.LENA_HAI),
                        recordType = runCatching {
                            KhataRecordType.valueOf(obj.optString("recordType", KhataRecordType.ORIGINAL_ENTRY.name))
                        }.getOrDefault(KhataRecordType.ORIGINAL_ENTRY),
                        parentEntryId = obj.optString("parentEntryId", "").ifBlank { null },
                        amount = obj.optLong("amount", 0L),
                        txDateMillis = obj.optLong("txDateMillis", System.currentTimeMillis()),
                        dueDateMillis = if (dueRaw > 0L) dueRaw else null,
                        description = obj.optString("description", ""),
                        category = obj.optString("category", "Personal Udhaar"),
                        notes = obj.optString("notes", ""),
                        isDeleted = obj.optBoolean("isDeleted", false) || obj.optBoolean("deleted", false),
                        isArchived = obj.optBoolean("isArchived", false) || obj.optBoolean("archived", false),
                        isEdited = obj.optBoolean("isEdited", false) || obj.optBoolean("edited", false),
                        previousAmountBeforeEdit = if (prevAmtRaw > 0L) prevAmtRaw else null,
                        previousDescriptionBeforeEdit = obj.optString("previousDescriptionBeforeEdit", "").ifBlank { null },
                        editCount = obj.optInt("editCount", 0),
                        proofAttachmentUri = obj.optString("proofAttachmentUri", ""),
                        proofAttachmentLabel = obj.optString("proofAttachmentLabel", ""),
                        idempotencyKey = obj.optString("idempotencyKey", ""),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                        updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
                    )
                )
            }
        }

        mergeCloudSnapshot(context, safeUid, importedContacts, importedEntries)
        importedContacts.size to importedEntries.size
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

    /**
     * FEATURE #1 — "Dosti Credit Score" & Payback Reliability Badge.
     * Computes a contact's reliability pill based on their ledger settlement history & pending age:
     * - ⚡ 98% Fast Payer (Emerald badge — settles within 3 days)
     * - ⏳ Moderate (Cyan/Gold badge)
     * - 🐢 Late Payer • 15d+ (Amber/Red warning badge for overdue balances)
     */
    fun computeDostiCreditScore(
        state: KhataContactComputedState,
        nowMillis: Long = System.currentTimeMillis()
    ): DostiCreditScoreInfo {
        val activeOriginalEntries = state.activeEntries.filter {
            it.recordType == KhataRecordType.ORIGINAL_ENTRY &&
                (state.netDirection == null || it.direction == state.netDirection)
        }
        val oldestActiveTxMillis = activeOriginalEntries.minOfOrNull { it.txDateMillis } ?: state.lastTransactionMillis
        val pendingDays = if (state.isSettled) {
            0L
        } else {
            ((nowMillis - oldestActiveTxMillis).coerceAtLeast(0L)) / 86_400_000L
        }

        val overdueDays = if (!state.isSettled && state.earliestUnsettledDueDateMillis != null && state.earliestUnsettledDueDateMillis < nowMillis) {
            ((nowMillis - state.earliestUnsettledDueDateMillis).coerceAtLeast(0L)) / 86_400_000L
        } else {
            0L
        }

        return when {
            state.isOverdue || pendingDays >= 11L || overdueDays >= 1L -> {
                val displayDays = max(15L, max(pendingDays, overdueDays))
                DostiCreditScoreInfo(
                    tier = DostiReliabilityTier.LATE_PAYER,
                    badgeLabel = "🐢 Late Payer • ${displayDays}d+",
                    scorePercent = (65 - (pendingDays.toInt().coerceAtMost(30))).coerceIn(32, 65),
                    pendingDays = pendingDays.toInt()
                )
            }
            !state.isSettled && pendingDays in 4L..10L -> {
                DostiCreditScoreInfo(
                    tier = DostiReliabilityTier.MODERATE,
                    badgeLabel = "⏳ Moderate",
                    scorePercent = (88 - (pendingDays.toInt() * 2)).coerceIn(70, 86),
                    pendingDays = pendingDays.toInt()
                )
            }
            else -> {
                DostiCreditScoreInfo(
                    tier = DostiReliabilityTier.FAST_PAYER,
                    badgeLabel = "⚡ 98% Fast Payer",
                    scorePercent = 98,
                    pendingDays = pendingDays.toInt()
                )
            }
        }
    }

    /**
     * FEATURE #3 — Promise-to-Pay Live Countdown Pill Formatter.
     * Formats dueDateMillis into `⏰ Due in 2 days`, `⏰ Due Today`, or `🚨 Overdue by 4 days`.
     */
    fun formatPromiseToPayCountdown(
        dueDateMillis: Long?,
        isSettled: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): PromiseCountdownBadge? {
        if (isSettled || dueDateMillis == null) return null
        val todayStart = startOfTodayMillis(nowMillis)
        val dueStart = startOfTodayMillis(dueDateMillis)
        val diffDays = ((dueStart - todayStart) / 86_400_000L).toInt()
        return when {
            diffDays < 0 -> {
                val overdueDays = abs(diffDays).coerceAtLeast(1)
                PromiseCountdownBadge(
                    label = if (overdueDays == 1) "🚨 Overdue by 1 day" else "🚨 Overdue by $overdueDays days",
                    isOverdue = true,
                    daysDelta = diffDays
                )
            }
            diffDays == 0 -> PromiseCountdownBadge(
                label = "⏰ Due Today",
                isOverdue = false,
                daysDelta = 0
            )
            diffDays == 1 -> PromiseCountdownBadge(
                label = "⏰ Due in 1 day",
                isOverdue = false,
                daysDelta = 1
            )
            else -> PromiseCountdownBadge(
                label = "⏰ Due in $diffDays days",
                isOverdue = false,
                daysDelta = diffDays
            )
        }
    }

    /**
     * FEATURE #4 — Pure calculation for 1-Tap Group Bill & Trip Splitter (Equal Split or Custom Amounts).
     */
    fun computeGroupBillSplit(
        totalBillAmount: Long,
        selectedFriends: List<Pair<String, String>>, // Pair(name, phone)
        isEqualSplit: Boolean,
        includeSelf: Boolean = true,
        customFriendAmounts: Map<String, Long> = emptyMap(),
        customSelfAmount: Long? = null
    ): GroupSplitComputationResult {
        val cleanTotal = totalBillAmount.coerceAtLeast(0L)
        val cleanFriends = selectedFriends
            .map { it.first.trim() to it.second.trim() }
            .filter { it.first.isNotBlank() }
            .distinctBy { it.first.lowercase() }

        if (cleanTotal <= 0L || cleanFriends.isEmpty()) {
            return GroupSplitComputationResult(
                totalBillAmount = cleanTotal,
                myPersonalShare = 0L,
                friendShares = emptyList(),
                isValid = false,
                validationMessage = if (cleanTotal <= 0L) "Enter a valid bill amount (> ₹0)" else "Select at least 1 friend to split with"
            )
        }

        return if (isEqualSplit) {
            val totalParticipants = cleanFriends.size + (if (includeSelf) 1 else 0)
            val baseShare = cleanTotal / totalParticipants
            val remainder = cleanTotal - (baseShare * totalParticipants)
            val myShare = if (includeSelf) baseShare + remainder else 0L
            val friendRegistrations = cleanFriends.mapIndexed { idx, (name, phone) ->
                val amt = if (!includeSelf && idx == 0) baseShare + remainder else baseShare
                GroupSplitParticipantShare(name = name, phone = phone, amount = amt)
            }.filter { it.amount > 0L }

            GroupSplitComputationResult(
                totalBillAmount = cleanTotal,
                myPersonalShare = myShare,
                friendShares = friendRegistrations,
                isValid = friendRegistrations.isNotEmpty(),
                validationMessage = "Splitting ₹$cleanTotal equally across $totalParticipants people"
            )
        } else {
            val shares = cleanFriends.map { (name, phone) ->
                val amt = (customFriendAmounts[name] ?: 0L).coerceAtLeast(0L)
                GroupSplitParticipantShare(name = name, phone = phone, amount = amt)
            }
            val friendsSum = shares.sumOf { it.amount }
            val myShare = customSelfAmount ?: (cleanTotal - friendsSum).coerceAtLeast(0L)
            val allocatedTotal = friendsSum + (if (includeSelf) myShare else 0L)
            val isValid = shares.any { it.amount > 0L } && friendsSum <= cleanTotal

            val msg = when {
                shares.all { it.amount <= 0L } -> "Enter custom split amounts for selected friends"
                friendsSum > cleanTotal -> "Friends' shares (₹$friendsSum) exceed total bill (₹$cleanTotal)"
                else -> "Your share: ₹$myShare • Friends owe: ₹$friendsSum (Total ₹$allocatedTotal)"
            }
            GroupSplitComputationResult(
                totalBillAmount = cleanTotal,
                myPersonalShare = if (includeSelf) myShare else 0L,
                friendShares = shares.filter { it.amount > 0L },
                isValid = isValid,
                validationMessage = msg
            )
        }
    }

    /**
     * Executes the 1-Tap Group Bill & Trip Split by creating individual "Lena Hai" entries
     * for each selected friend in `AdvancedKhataStore`.
     */
    @Synchronized
    fun recordGroupBillSplit(
        context: Context,
        userId: String,
        billTitle: String,
        category: String,
        splitResult: GroupSplitComputationResult,
        dueDateMillis: Long? = null,
        notes: String = ""
    ): Result<Int> {
        if (!splitResult.isValid || splitResult.friendShares.isEmpty()) {
            return Result.failure(IllegalArgumentException(splitResult.validationMessage))
        }
        val cleanTitle = billTitle.trim().ifBlank { "Group Split Bill" }
        var createdCount = 0
        splitResult.friendShares.forEach { participant ->
            if (participant.amount > 0L) {
                val res = addUdhaarEntry(
                    context = context,
                    userId = userId,
                    personName = participant.name,
                    phone = participant.phone,
                    direction = KhataDirection.LENA_HAI,
                    amount = participant.amount,
                    txDateMillis = System.currentTimeMillis(),
                    dueDateMillis = dueDateMillis,
                    description = "$cleanTitle (Split of ₹${splitResult.totalBillAmount})",
                    category = category.ifBlank { "Food & Outing" },
                    notes = notes.ifBlank { "1-Tap Group Bill Split via Kharchee" },
                    bypassDedupWindow = true
                )
                if (res.isSuccess) {
                    createdCount++
                }
            }
        }
        return Result.success(createdCount)
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

enum class DostiReliabilityTier {
    FAST_PAYER,
    MODERATE,
    LATE_PAYER
}

data class DostiCreditScoreInfo(
    val tier: DostiReliabilityTier,
    val badgeLabel: String,
    val scorePercent: Int,
    val pendingDays: Int
)

data class PromiseCountdownBadge(
    val label: String,
    val isOverdue: Boolean,
    val daysDelta: Int
)

data class GroupSplitParticipantShare(
    val name: String,
    val phone: String = "",
    val amount: Long
)

data class GroupSplitComputationResult(
    val totalBillAmount: Long,
    val myPersonalShare: Long,
    val friendShares: List<GroupSplitParticipantShare>,
    val isValid: Boolean,
    val validationMessage: String
)
