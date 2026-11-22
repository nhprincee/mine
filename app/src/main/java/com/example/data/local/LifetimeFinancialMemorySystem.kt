package com.example.data.local

import android.content.Context
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.ExpenseRecord
import com.example.data.model.KhataLedgerEntry
import com.example.data.model.isIncomeTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Separated Room & Lifetime financial totals ensuring lending/borrowing (`moneymovement`)
 * is never mixed into personal spending (`personalspending`) metrics.
 */
data class SeparatedFinancialTotals(
    val personalSpendingTotal: Long,
    val moneyMovementTotal: Long,
    val incomeTotal: Long,
    val personalSpendingCount: Int,
    val moneyMovementCount: Int,
    val incomeCount: Int
)

/**
 * Snapshot of the Lifetime Financial Memory System status for a user.
 */
data class LifetimeMemoryAuditStatus(
    val totalPreservedKhataRecords: Int,
    val activeKhataRecords: Int,
    val softArchivedKhataRecords: Int,
    val personalSpendingTotal: Long,
    val moneyMovementTotal: Long
)

/**
 * KHARCHEE — ABSOLUTE DATA PERMANENCE LAYER (`LifetimeFinancialMemorySystem`)
 *
 * Strict invariants:
 * 1. Historical Khata transactions are NEVER deleted during UI updates, restarts, or Room migrations.
 *    Removals strictly use soft-archival (`isArchived = true`, preserving `createdAtMillis` and `updatedAtMillis`).
 * 2. All Room DB & financial calculations strictly separate `moneymovement` (lending, borrowing, Khata splits,
 *    repayments, settlements) from `personalspending` (true personal consumption expenses).
 */
object LifetimeFinancialMemorySystem {

    const val SCOPE_PERSONAL_SPENDING = "personalspending"
    const val SCOPE_MONEY_MOVEMENT = "moneymovement"

    private val MONEY_MOVEMENT_CATEGORIES = setOf(
        "khata",
        "lending",
        "borrowing",
        "loan",
        "split",
        "udhar",
        "udhaar",
        "settlement",
        "lena hai",
        "dena hai",
        "repayment",
        "money movement",
        "moneymovement"
    )

    private val MONEY_MOVEMENT_TYPES = setOf(
        "moneymovement",
        "money_movement",
        "lending",
        "borrowing",
        "khata",
        "settlement"
    )

    /**
     * Classifies any transaction strictly into `moneymovement` (lending/borrowing/Khata)
     * or `personalspending` (true personal spending).
     */
    fun classifyTransactionScope(
        category: String,
        type: String = "expense",
        note: String = ""
    ): String {
        val normCat = category.trim().lowercase()
        val normType = type.trim().lowercase()
        val normNote = note.trim().lowercase()
        val isMovement = normType in MONEY_MOVEMENT_TYPES ||
            normCat in MONEY_MOVEMENT_CATEGORIES ||
            normNote.contains("[moneymovement]") ||
            normNote.contains("[khata]") ||
            normNote.contains("khata settlement") ||
            normNote.contains("udhar") ||
            normNote.contains("udhaar")
        return if (isMovement) SCOPE_MONEY_MOVEMENT else SCOPE_PERSONAL_SPENDING
    }

    fun isMoneyMovement(record: ExpenseRecord): Boolean {
        return classifyTransactionScope(record.category, record.type, record.note) == SCOPE_MONEY_MOVEMENT
    }

    fun isPersonalSpending(record: ExpenseRecord): Boolean {
        return !isIncomeTransaction(record) && !isMoneyMovement(record)
    }

    /**
     * Computes separated totals over Room `ExpenseRoomEntity` rows so lending/borrowing
     * (`moneymovement`) never distorts personal spending (`personalspending`).
     */
    fun computeSeparatedRoomTotals(entities: List<ExpenseRoomEntity>): SeparatedFinancialTotals {
        var personalTotal = 0L
        var personalCount = 0
        var movementTotal = 0L
        var movementCount = 0
        var incomeTotal = 0L
        var incomeCount = 0

        for (entity in entities) {
            val isIncome = entity.type.equals("income", ignoreCase = true) ||
                entity.category.equals("income", ignoreCase = true) ||
                entity.category.equals("salary", ignoreCase = true)
            if (isIncome) {
                incomeTotal += entity.amount
                incomeCount++
            } else {
                val resolvedScope = entity.transactionScope.ifBlank {
                    classifyTransactionScope(entity.category, entity.type, entity.note)
                }
                if (resolvedScope.equals(SCOPE_MONEY_MOVEMENT, ignoreCase = true)) {
                    movementTotal += entity.amount
                    movementCount++
                } else {
                    personalTotal += entity.amount
                    personalCount++
                }
            }
        }

        return SeparatedFinancialTotals(
            personalSpendingTotal = personalTotal,
            moneyMovementTotal = movementTotal,
            incomeTotal = incomeTotal,
            personalSpendingCount = personalCount,
            moneyMovementCount = movementCount,
            incomeCount = incomeCount
        )
    }

    /**
     * Computes separated totals over domain `ExpenseRecord` items.
     */
    fun computeSeparatedDomainTotals(records: List<ExpenseRecord>): SeparatedFinancialTotals {
        return computeSeparatedRoomTotals(records.map { ExpenseRoomEntity.fromDomainModel(it) })
    }

    /**
     * Converts a `KhataLedgerEntry` to an append-safe Room lifetime record.
     */
    fun toRoomEntity(
        userId: String,
        contactName: String,
        entry: KhataLedgerEntry
    ): KhataLifetimeRoomEntity {
        val createdAt = if (entry.createdAtMillis > 0L) entry.createdAtMillis else entry.txDateMillis
        val updatedAt = if (entry.updatedAtMillis > 0L) entry.updatedAtMillis else createdAt
        return KhataLifetimeRoomEntity(
            id = entry.id,
            userId = userId,
            contactId = entry.contactId,
            contactName = contactName.ifBlank { "Contact" },
            direction = entry.direction.name,
            recordType = entry.recordType.name,
            amount = entry.amount,
            description = entry.description,
            txDateMillis = entry.txDateMillis,
            createdAtMillis = createdAt,
            updatedAtMillis = updatedAt,
            isArchived = entry.isArchived,
            isDeleted = entry.isDeleted,
            transactionScope = SCOPE_MONEY_MOVEMENT
        )
    }

    /**
     * Permanently upserts a Khata transaction into the Room `khata_lifetime_ledger` table.
     * Never deletes existing history.
     */
    suspend fun recordKhataTransactionPermanently(
        context: Context,
        userId: String,
        contactName: String,
        entry: KhataLedgerEntry
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || entry.id.isBlank()) return@withContext
        val dao = KharcheeDatabase.getInstance(context).khataLifetimeDao()
        dao.upsertEntry(toRoomEntity(userId, contactName, entry))
    }

    /**
     * Soft-archives a Khata transaction in Room without ever permanently deleting it.
     */
    suspend fun softArchiveKhataTransaction(
        context: Context,
        userId: String,
        entryId: String,
        nowMillis: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || entryId.isBlank()) return@withContext
        val dao = KharcheeDatabase.getInstance(context).khataLifetimeDao()
        dao.softArchiveEntry(entryId = entryId, userId = userId, updatedAtMillis = nowMillis)
    }

    /**
     * Synchronizes all historical Khata records from `AdvancedKhataStore` into Room's
     * permanent `khata_lifetime_ledger` table and returns a complete lifetime audit snapshot.
     * Preserves 100% of existing Room entries even if local UI caches change or restart.
     */
    suspend fun synchronizeLifetimeMemory(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord> = emptyList()
    ): LifetimeMemoryAuditStatus = withContext(Dispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext LifetimeMemoryAuditStatus(0, 0, 0, 0L, 0L)
        }
        val db = KharcheeDatabase.getInstance(context)
        val khataDao = db.khataLifetimeDao()
        val expenseDao = db.expenseDao()

        // 1. Upsert all current AdvancedKhataStore entries (active + archived/deleted) into permanent Room ledger
        val states = AdvancedKhataStore.getAllContactStates(context, userId)
        val toPersist = states.flatMap { contactState ->
            contactState.allEntriesIncludingDeleted.map { entry ->
                toRoomEntity(userId, contactState.contact.name, entry)
            }
        }
        if (toPersist.isNotEmpty()) {
            khataDao.upsertEntries(toPersist)
        }

        // 2. Sync expenses into Room with strict personalspending vs moneymovement classification
        if (expenses.isNotEmpty()) {
            expenseDao.insertExpenses(expenses.map { ExpenseRoomEntity.fromDomainModel(it) })
        }

        // 3. Read back full immutable lifetime history from Room
        val allLifetimeKhata = khataDao.getAllLifetimeKhataEntries(userId)
        val allRoomExpenses = expenseDao.getExpensesForUser(userId)
        val totals = computeSeparatedRoomTotals(allRoomExpenses)

        val activeCount = allLifetimeKhata.count { !it.isArchived && !it.isDeleted }
        val archivedCount = allLifetimeKhata.count { it.isArchived || it.isDeleted }

        LifetimeMemoryAuditStatus(
            totalPreservedKhataRecords = allLifetimeKhata.size,
            activeKhataRecords = activeCount,
            softArchivedKhataRecords = archivedCount,
            personalSpendingTotal = totals.personalSpendingTotal,
            moneyMovementTotal = totals.moneyMovementTotal
        )
    }
}
