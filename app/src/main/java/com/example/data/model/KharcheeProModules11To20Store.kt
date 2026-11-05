package com.example.data.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

// =============================================================================
// MODULE 11 — MULTI-WALLET INTELLIGENCE MODELS
// =============================================================================
enum class ProWalletType(val id: String, val label: String) {
    CASH("cash", "Cash Wallet"),
    BANK("bank", "Bank Account"),
    SAVINGS("savings", "Savings Account"),
    DIGITAL("digital", "Digital Wallet"),
    CUSTOM("custom", "Custom Wallet");

    companion object {
        fun fromId(id: String): ProWalletType = entries.find { it.id == id } ?: CUSTOM
    }
}

data class ProWalletRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val walletType: ProWalletType,
    val openingBalance: Long,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class ProWalletTransferRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val fromWalletId: String,
    val fromWalletName: String,
    val toWalletId: String,
    val toWalletName: String,
    val amount: Long,
    val note: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

data class ProWalletSummaryItem(
    val wallet: ProWalletRecord,
    val totalIncome: Long,
    val totalExpense: Long,
    val transfersIn: Long,
    val transfersOut: Long,
    val currentBalance: Long,
    val transactionCount: Int
)

data class ProConsolidatedWalletOverview(
    val activeWalletSummaries: List<ProWalletSummaryItem>,
    val archivedWalletSummaries: List<ProWalletSummaryItem>,
    val consolidatedBalance: Long,
    val consolidatedIncome: Long, // Excludes wallet-to-wallet transfers
    val consolidatedExpense: Long, // Excludes wallet-to-wallet transfers
    val transfers: List<ProWalletTransferRecord>
)

// =============================================================================
// MODULE 12 — LOAN & EMI INTELLIGENCE MODELS
// =============================================================================
data class ProLoanRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val principalAmount: Long,
    val annualInterestRatePct: Double,
    val startDate: String,
    val tenureMonths: Int,
    val emiFrequency: String = "Monthly",
    val emiPaymentDay: Int = 5,
    val processingFee: Long = 0L,
    val monthlyEmi: Long,
    val totalInterestEstimate: Long,
    val totalPayable: Long,
    val paidInstallments: List<Int> = emptyList()
)

data class ProAmortizationRow(
    val installmentNumber: Int,
    val openingBalance: Long,
    val emiAmount: Long,
    val principalComponent: Long,
    val interestComponent: Long,
    val closingBalance: Long,
    val isPaid: Boolean
)

data class ProLoanCalculationResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val monthlyEmi: Long,
    val totalPrincipal: Long,
    val totalInterest: Long,
    val totalPayable: Long,
    val outstandingBalance: Long,
    val schedule: List<ProAmortizationRow>
)

// =============================================================================
// MODULE 13 — SMART BILL MANAGER MODELS
// =============================================================================
data class ProSmartBillRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // Electricity, Water, Mobile, Internet, Rent, Education, Insurance, Other
    val frequency: String = "Monthly", // Monthly, Quarterly, Yearly
    val dueDayOfMonth: Int = 10,
    val expectedAmount: Long,
    val isVariableAmount: Boolean = false,
    val reminderEnabled: Boolean = true,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, CANCELLED
    val paidPeriods: Map<String, Long> = emptyMap() // periodKey ("2026-11") -> actualPaidAmount
)

// =============================================================================
// MODULE 14 — GOAL INTELLIGENCE CENTER MODELS
// =============================================================================
data class ProGoalContributionRecord(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amount: Long,
    val note: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class ProGoalRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: String,
    val priority: String = "High", // High, Medium, Low
    val targetAmount: Long,
    val targetDate: String,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, ARCHIVED
    val contributions: List<ProGoalContributionRecord> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val savedAmount: Long get() = contributions.sumOf { it.amount }.coerceAtLeast(0L)
    val remainingAmount: Long get() = max(0L, targetAmount - savedAmount)
    val progressPct: Int get() = if (targetAmount > 0L) ((savedAmount.toDouble() / targetAmount.toDouble()) * 100.0).roundToInt().coerceIn(0, 100) else 0
}

// =============================================================================
// MODULE 15 — MONEY CALENDAR PRO MODELS
// =============================================================================
data class ProCalendarEntry(
    val id: String,
    val title: String,
    val entryType: String, // INCOME, EXPENSE, BILL, EMI, SUBSCRIPTION, SAVINGS, EXPECTED, REMINDER
    val status: String, // COMPLETED, SCHEDULED, PREDICTED
    val amount: Long,
    val dayOfMonth: Int,
    val dateKey: String, // yyyy-MM-dd
    val details: String
)

// =============================================================================
// MODULE 16 — SMART MONEY RULES MODELS
// =============================================================================
data class ProSmartMoneyRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val triggerType: String, // MERCHANT_CONTAINS, PAYMENT_MODE_EQUALS, AMOUNT_ABOVE, CATEGORY_EQUALS
    val matchingCriteria: String,
    val suggestedActionType: String, // SUGGEST_CATEGORY, SUGGEST_WALLET, FLAG_HIGH_AMOUNT, SUGGEST_TAG, REMIND_RECURRING
    val suggestedActionValue: String,
    val priority: Int = 50,
    val enabled: Boolean = true
)

data class ProRuleSuggestion(
    val ruleId: String,
    val ruleName: String,
    val transactionId: String,
    val transactionTitle: String,
    val suggestedActionType: String,
    val suggestedActionValue: String,
    val requiresUserConfirmation: Boolean = true
)

// =============================================================================
// MODULE 17 — FINANCIAL DOCUMENT VAULT MODELS
// =============================================================================
data class ProDocumentVaultItem(
    val id: String = UUID.randomUUID().toString(),
    val ownerUserId: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val category: String, // Bills, Receipts, Invoices, Loan Documents, Insurance, Warranty, Tax Records, Other
    val tags: List<String>,
    val attachedTransactionId: String = "",
    val notes: String = "",
    val entryMethod: String = "MANUAL_VERIFIED", // MANUAL_VERIFIED or SNAP_OCR_LINKED
    val uploadedAtMillis: Long = System.currentTimeMillis()
)

// =============================================================================
// MODULE 18 — ADVANCED SPLIT EXPENSE MANAGER MODELS
// =============================================================================
data class ProSplitParticipant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val invitationStatus: String = "ACCEPTED" // INVITED, ACCEPTED
)

data class ProSplitExpenseRecord(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val totalAmount: Long,
    val paidByParticipantId: String,
    val splitType: String, // EQUAL, CUSTOM, PERCENTAGE
    val sharesByParticipantId: Map<String, Long>, // reconciled to totalAmount
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class ProSplitSettlementRecord(
    val id: String = UUID.randomUUID().toString(),
    val fromParticipantId: String,
    val toParticipantId: String,
    val amount: Long,
    val settledAtMillis: Long = System.currentTimeMillis()
)

data class ProSplitGroupState(
    val groupId: String,
    val groupName: String,
    val participants: List<ProSplitParticipant>,
    val expenses: List<ProSplitExpenseRecord>,
    val settlements: List<ProSplitSettlementRecord>,
    val netBalances: Map<String, Long> // positive = owed to participant, negative = participant owes group
)

// =============================================================================
// MODULE 19 — MULTI-CURRENCY INTELLIGENCE MODELS
// =============================================================================
data class ProMultiCurrencyTransaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val originalAmount: Double,
    val currencyCode: String,
    val baseCurrencyCode: String,
    val exchangeRateUsed: Double,
    val convertedBaseAmount: Long,
    val rateSourceLabel: String = "Manual Rate Entry",
    val isForeignTravel: Boolean = true,
    val recordedAtMillis: Long = System.currentTimeMillis()
)

// =============================================================================
// MODULE 20 — PRO NOTIFICATION INTELLIGENCE MODELS
// =============================================================================
data class ProNotificationItem(
    val id: String,
    val category: String, // UPCOMING_BILL, EMI_REMINDER, SUBSCRIPTION_RENEWAL, BUDGET_ALERT, SPENDING_ANOMALY, GOAL_MILESTONE, WEEKLY_SUMMARY, MONTHLY_REPORT, SYSTEM_BACKUP
    val title: String,
    val message: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val deepLinkModuleId: String,
    val isRead: Boolean,
    val createdAtMillis: Long
)

data class ProNotificationPreferences(
    val quietHoursEnabled: Boolean = true,
    val quietStartHour: Int = 22,
    val quietEndHour: Int = 7,
    val enabledCategories: Set<String> = setOf(
        "UPCOMING_BILL", "EMI_REMINDER", "SUBSCRIPTION_RENEWAL", "BUDGET_ALERT",
        "SPENDING_ANOMALY", "GOAL_MILESTONE", "WEEKLY_SUMMARY", "MONTHLY_REPORT", "SYSTEM_BACKUP"
    )
)

object KharcheeProModules11To20Store {
    private const val PREFS_NAME = "kharchee_pro_modules_11_20_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // =========================================================================
    // MODULE 11 — MULTI-WALLET INTELLIGENCE
    // =========================================================================
    fun getWallets(context: Context, userId: String): List<ProWalletRecord> {
        val raw = prefs(context).getString("${userId}_m11_wallets", null)
        if (raw == null) {
            val defaults = listOf(
                ProWalletRecord(id = "w_bank_$userId", userId = userId, name = "Primary Bank Account", walletType = ProWalletType.BANK, openingBalance = 0L),
                ProWalletRecord(id = "w_upi_$userId", userId = userId, name = "UPI Digital Wallet", walletType = ProWalletType.DIGITAL, openingBalance = 0L),
                ProWalletRecord(id = "w_cash_$userId", userId = userId, name = "Cash Wallet", walletType = ProWalletType.CASH, openingBalance = 0L)
            )
            saveWallets(context, userId, defaults)
            return defaults
        }
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                if (o.optString("userId") != userId) return@mapNotNull null
                ProWalletRecord(
                    id = o.optString("id"),
                    userId = userId,
                    name = o.optString("name"),
                    walletType = ProWalletType.fromId(o.optString("walletType")),
                    openingBalance = o.optLong("openingBalance", 0L),
                    isArchived = o.optBoolean("isArchived", false),
                    createdAtMillis = o.optLong("createdAtMillis", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createOrUpdateWallet(
        context: Context,
        userId: String,
        walletId: String? = null,
        name: String,
        walletType: ProWalletType,
        openingBalance: Long,
        isArchived: Boolean = false
    ): List<ProWalletRecord> {
        if (name.isBlank() || openingBalance < 0L) return getWallets(context, userId)
        val current = getWallets(context, userId).toMutableList()
        val existingIdx = if (walletId != null) current.indexOfFirst { it.id == walletId && it.userId == userId } else -1
        if (existingIdx >= 0) {
            current[existingIdx] = current[existingIdx].copy(
                name = name.trim(),
                walletType = walletType,
                openingBalance = openingBalance,
                isArchived = isArchived
            )
        } else {
            current.add(
                ProWalletRecord(
                    userId = userId,
                    name = name.trim(),
                    walletType = walletType,
                    openingBalance = openingBalance,
                    isArchived = isArchived
                )
            )
        }
        saveWallets(context, userId, current)
        return current
    }

    private fun saveWallets(context: Context, userId: String, list: List<ProWalletRecord>) {
        val arr = JSONArray()
        list.filter { it.userId == userId }.forEach { w ->
            arr.put(JSONObject().apply {
                put("id", w.id); put("userId", w.userId); put("name", w.name)
                put("walletType", w.walletType.id); put("openingBalance", w.openingBalance)
                put("isArchived", w.isArchived); put("createdAtMillis", w.createdAtMillis)
            })
        }
        prefs(context).edit().putString("${userId}_m11_wallets", arr.toString()).apply()
    }

    fun getWalletTransfers(context: Context, userId: String): List<ProWalletTransferRecord> {
        val raw = prefs(context).getString("${userId}_m11_transfers", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                if (o.optString("userId") != userId) return@mapNotNull null
                ProWalletTransferRecord(
                    id = o.optString("id"),
                    userId = userId,
                    fromWalletId = o.optString("fromWalletId"),
                    fromWalletName = o.optString("fromWalletName"),
                    toWalletId = o.optString("toWalletId"),
                    toWalletName = o.optString("toWalletName"),
                    amount = o.optLong("amount"),
                    note = o.optString("note"),
                    timestampMillis = o.optLong("timestampMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun transferBetweenWallets(
        context: Context,
        userId: String,
        fromWalletId: String,
        toWalletId: String,
        amount: Long,
        note: String = ""
    ): Result<ProWalletTransferRecord> {
        if (amount <= 0L) return Result.failure(IllegalArgumentException("Transfer amount must be positive"))
        if (fromWalletId == toWalletId) return Result.failure(IllegalArgumentException("Cannot transfer to the same wallet"))
        val wallets = getWallets(context, userId)
        val source = wallets.find { it.id == fromWalletId && it.userId == userId && !it.isArchived }
            ?: return Result.failure(IllegalArgumentException("Source wallet not found or archived"))
        val dest = wallets.find { it.id == toWalletId && it.userId == userId && !it.isArchived }
            ?: return Result.failure(IllegalArgumentException("Destination wallet not found or archived"))

        val record = ProWalletTransferRecord(
            userId = userId,
            fromWalletId = source.id,
            fromWalletName = source.name,
            toWalletId = dest.id,
            toWalletName = dest.name,
            amount = amount,
            note = note.trim()
        )
        val updated = listOf(record) + getWalletTransfers(context, userId)
        val arr = JSONArray()
        updated.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id); put("userId", t.userId)
                put("fromWalletId", t.fromWalletId); put("fromWalletName", t.fromWalletName)
                put("toWalletId", t.toWalletId); put("toWalletName", t.toWalletName)
                put("amount", t.amount); put("note", t.note); put("timestampMillis", t.timestampMillis)
            })
        }
        // Single atomic commit in SharedPreferences
        prefs(context).edit().putString("${userId}_m11_transfers", arr.toString()).commit()
        return Result.success(record)
    }

    fun computeConsolidatedWalletOverview(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>
    ): ProConsolidatedWalletOverview {
        val wallets = getWallets(context, userId)
        val transfers = getWalletTransfers(context, userId)

        val summaries = wallets.map { w ->
            val matchingTxs = expenses.filter { tx ->
                when (w.walletType) {
                    ProWalletType.CASH -> tx.paymentMode.equals("Cash", ignoreCase = true)
                    ProWalletType.DIGITAL -> tx.paymentMode.equals("UPI", ignoreCase = true)
                    ProWalletType.BANK -> tx.paymentMode.equals("Card", ignoreCase = true) || tx.paymentMode.equals("Bank", ignoreCase = true)
                    else -> tx.paymentMode.equals(w.name, ignoreCase = true)
                }
            }
            val inc = matchingTxs.filter { isIncomeTransaction(it) }.sumOf { it.amount }
            val exp = matchingTxs.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
            val tIn = transfers.filter { it.toWalletId == w.id }.sumOf { it.amount }
            val tOut = transfers.filter { it.fromWalletId == w.id }.sumOf { it.amount }
            val bal = w.openingBalance + inc - exp + tIn - tOut
            ProWalletSummaryItem(
                wallet = w,
                totalIncome = inc,
                totalExpense = exp,
                transfersIn = tIn,
                transfersOut = tOut,
                currentBalance = bal,
                transactionCount = matchingTxs.size
            )
        }

        val active = summaries.filter { !it.wallet.isArchived }
        val archived = summaries.filter { it.wallet.isArchived }
        val totalInc = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val totalExp = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val totalBal = active.sumOf { it.currentBalance }

        return ProConsolidatedWalletOverview(
            activeWalletSummaries = active,
            archivedWalletSummaries = archived,
            consolidatedBalance = totalBal,
            consolidatedIncome = totalInc,
            consolidatedExpense = totalExp,
            transfers = transfers
        )
    }

    // =========================================================================
    // MODULE 12 — LOAN & EMI INTELLIGENCE
    // =========================================================================
    fun calculateLoanAmortization(
        principal: Long,
        annualInterestRatePct: Double,
        tenureMonths: Int,
        paidInstallments: Set<Int> = emptySet()
    ): ProLoanCalculationResult {
        if (principal <= 0L || tenureMonths <= 0 || annualInterestRatePct < 0.0) {
            return ProLoanCalculationResult(
                isValid = false,
                errorMessage = "Principal and tenure must be positive, and interest rate cannot be negative.",
                monthlyEmi = 0L,
                totalPrincipal = 0L,
                totalInterest = 0L,
                totalPayable = 0L,
                outstandingBalance = 0L,
                schedule = emptyList()
            )
        }

        val monthlyEmiDouble = if (annualInterestRatePct == 0.0) {
            principal.toDouble() / tenureMonths.toDouble()
        } else {
            val r = (annualInterestRatePct / 12.0) / 100.0
            val factor = (1.0 + r).pow(tenureMonths.toDouble())
            principal.toDouble() * r * factor / (factor - 1.0)
        }
        val monthlyEmi = monthlyEmiDouble.roundToLong()

        val schedule = mutableListOf<ProAmortizationRow>()
        var remainingPrincipal = principal
        var totalInterestSum = 0L
        val r = (annualInterestRatePct / 12.0) / 100.0

        for (m in 1..tenureMonths) {
            val interestForMonth = if (annualInterestRatePct == 0.0) 0L else (remainingPrincipal * r).roundToLong()
            val principalForMonth = if (m == tenureMonths) {
                remainingPrincipal
            } else {
                (monthlyEmi - interestForMonth).coerceIn(0L, remainingPrincipal)
            }
            val actualInstallment = principalForMonth + interestForMonth
            val closing = max(0L, remainingPrincipal - principalForMonth)
            totalInterestSum += interestForMonth

            schedule.add(
                ProAmortizationRow(
                    installmentNumber = m,
                    openingBalance = remainingPrincipal,
                    emiAmount = actualInstallment,
                    principalComponent = principalForMonth,
                    interestComponent = interestForMonth,
                    closingBalance = closing,
                    isPaid = m in paidInstallments
                )
            )
            remainingPrincipal = closing
        }

        val paidPrincipal = schedule.filter { it.isPaid }.sumOf { it.principalComponent }
        val outstanding = max(0L, principal - paidPrincipal)

        return ProLoanCalculationResult(
            isValid = true,
            errorMessage = null,
            monthlyEmi = monthlyEmi,
            totalPrincipal = principal,
            totalInterest = totalInterestSum,
            totalPayable = principal + totalInterestSum,
            outstandingBalance = outstanding,
            schedule = schedule
        )
    }

    fun getProLoans(context: Context, userId: String): List<ProLoanRecord> {
        val raw = prefs(context).getString("${userId}_m12_loans", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val paidArr = o.optJSONArray("paidInstallments") ?: JSONArray()
                val paidList = (0 until paidArr.length()).map { paidArr.optInt(it) }
                ProLoanRecord(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    principalAmount = o.optLong("principalAmount"),
                    annualInterestRatePct = o.optDouble("annualInterestRatePct", 0.0),
                    startDate = o.optString("startDate", "01 Nov 2026"),
                    tenureMonths = o.optInt("tenureMonths", 12),
                    emiFrequency = o.optString("emiFrequency", "Monthly"),
                    emiPaymentDay = o.optInt("emiPaymentDay", 5),
                    processingFee = o.optLong("processingFee", 0L),
                    monthlyEmi = o.optLong("monthlyEmi", 0L),
                    totalInterestEstimate = o.optLong("totalInterestEstimate", 0L),
                    totalPayable = o.optLong("totalPayable", 0L),
                    paidInstallments = paidList
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addProLoan(
        context: Context,
        userId: String,
        name: String,
        principal: Long,
        annualRatePct: Double,
        startDate: String,
        tenureMonths: Int,
        emiPaymentDay: Int = 5,
        processingFee: Long = 0L
    ): List<ProLoanRecord> {
        val calc = calculateLoanAmortization(principal, annualRatePct, tenureMonths)
        if (!calc.isValid) return getProLoans(context, userId)
        val record = ProLoanRecord(
            name = name.trim().ifBlank { "Loan" },
            principalAmount = principal,
            annualInterestRatePct = annualRatePct,
            startDate = startDate,
            tenureMonths = tenureMonths,
            emiPaymentDay = emiPaymentDay.coerceIn(1, 28),
            processingFee = max(0L, processingFee),
            monthlyEmi = calc.monthlyEmi,
            totalInterestEstimate = calc.totalInterest,
            totalPayable = calc.totalPayable
        )
        val updated = listOf(record) + getProLoans(context, userId)
        saveProLoans(context, userId, updated)
        return updated
    }

    fun recordLoanInstallmentPaid(context: Context, userId: String, loanId: String, installmentNumber: Int): List<ProLoanRecord> {
        val updated = getProLoans(context, userId).map { loan ->
            if (loan.id == loanId && installmentNumber in 1..loan.tenureMonths && installmentNumber !in loan.paidInstallments) {
                loan.copy(paidInstallments = (loan.paidInstallments + installmentNumber).distinct().sorted())
            } else loan
        }
        saveProLoans(context, userId, updated)
        return updated
    }

    private fun saveProLoans(context: Context, userId: String, list: List<ProLoanRecord>) {
        val arr = JSONArray()
        list.forEach { l ->
            arr.put(JSONObject().apply {
                put("id", l.id); put("name", l.name); put("principalAmount", l.principalAmount)
                put("annualInterestRatePct", l.annualInterestRatePct); put("startDate", l.startDate)
                put("tenureMonths", l.tenureMonths); put("emiFrequency", l.emiFrequency)
                put("emiPaymentDay", l.emiPaymentDay); put("processingFee", l.processingFee)
                put("monthlyEmi", l.monthlyEmi); put("totalInterestEstimate", l.totalInterestEstimate)
                put("totalPayable", l.totalPayable)
                put("paidInstallments", JSONArray(l.paidInstallments))
            })
        }
        prefs(context).edit().putString("${userId}_m12_loans", arr.toString()).apply()
    }

    // =========================================================================
    // MODULE 13 — SMART BILL MANAGER
    // =========================================================================
    fun resolveValidDayOfMonth(year: Int, monthZeroBased: Int, requestedDay: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased.coerceIn(0, 11))
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return requestedDay.coerceIn(1, maxDay)
    }

    fun getSmartBills(context: Context, userId: String): List<ProSmartBillRecord> {
        val raw = prefs(context).getString("${userId}_m13_bills", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val paidObj = o.optJSONObject("paidPeriods") ?: JSONObject()
                val paidMap = mutableMapOf<String, Long>()
                val keys = paidObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    paidMap[k] = paidObj.optLong(k, 0L)
                }
                ProSmartBillRecord(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    category = o.optString("category", "Electricity"),
                    frequency = o.optString("frequency", "Monthly"),
                    dueDayOfMonth = o.optInt("dueDayOfMonth", 10),
                    expectedAmount = o.optLong("expectedAmount", 0L),
                    isVariableAmount = o.optBoolean("isVariableAmount", false),
                    reminderEnabled = o.optBoolean("reminderEnabled", true),
                    status = o.optString("status", "ACTIVE"),
                    paidPeriods = paidMap
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSmartBill(context: Context, userId: String, bill: ProSmartBillRecord): List<ProSmartBillRecord> {
        val list = getSmartBills(context, userId).toMutableList()
        val idx = list.indexOfFirst { it.id == bill.id }
        if (idx >= 0) {
            // Preserve historical paidPeriods when editing schedule
            val mergedPaid = list[idx].paidPeriods + bill.paidPeriods
            list[idx] = bill.copy(paidPeriods = mergedPaid)
        } else {
            list.add(0, bill)
        }
        persistSmartBills(context, userId, list)
        return list
    }

    fun markSmartBillPaid(
        context: Context,
        userId: String,
        billId: String,
        periodKey: String,
        actualPaidAmount: Long
    ): List<ProSmartBillRecord> {
        val list = getSmartBills(context, userId).map { b ->
            if (b.id == billId) {
                val nextPaid = b.paidPeriods.toMutableMap().apply { put(periodKey, max(0L, actualPaidAmount)) }
                b.copy(paidPeriods = nextPaid)
            } else b
        }
        persistSmartBills(context, userId, list)
        return list
    }

    private fun persistSmartBills(context: Context, userId: String, list: List<ProSmartBillRecord>) {
        val arr = JSONArray()
        list.forEach { b ->
            val paidJson = JSONObject()
            b.paidPeriods.forEach { (k, v) -> paidJson.put(k, v) }
            arr.put(JSONObject().apply {
                put("id", b.id); put("title", b.title); put("category", b.category)
                put("frequency", b.frequency); put("dueDayOfMonth", b.dueDayOfMonth)
                put("expectedAmount", b.expectedAmount); put("isVariableAmount", b.isVariableAmount)
                put("reminderEnabled", b.reminderEnabled); put("status", b.status)
                put("paidPeriods", paidJson)
            })
        }
        prefs(context).edit().putString("${userId}_m13_bills", arr.toString()).apply()
    }

    // =========================================================================
    // MODULE 14 — GOAL INTELLIGENCE CENTER
    // =========================================================================
    fun getProGoals(context: Context, userId: String): List<ProGoalRecord> {
        val raw = prefs(context).getString("${userId}_m14_goals", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val cArr = o.optJSONArray("contributions") ?: JSONArray()
                val contribs = (0 until cArr.length()).mapNotNull { j ->
                    val co = cArr.optJSONObject(j) ?: return@mapNotNull null
                    ProGoalContributionRecord(
                        id = co.optString("id"),
                        goalId = co.optString("goalId"),
                        amount = co.optLong("amount"),
                        note = co.optString("note"),
                        timestampMillis = co.optLong("timestampMillis")
                    )
                }.distinctBy { it.id }
                ProGoalRecord(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    description = o.optString("description"),
                    category = o.optString("category", "Wealth"),
                    priority = o.optString("priority", "High"),
                    targetAmount = o.optLong("targetAmount", 10000L),
                    targetDate = o.optString("targetDate", "31 Dec 2027"),
                    status = o.optString("status", "ACTIVE"),
                    contributions = contribs,
                    createdAtMillis = o.optLong("createdAtMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createProGoal(
        context: Context,
        userId: String,
        name: String,
        description: String,
        category: String,
        priority: String,
        targetAmount: Long,
        targetDate: String,
        startingContribution: Long = 0L
    ): List<ProGoalRecord> {
        if (name.isBlank() || targetAmount <= 0L || startingContribution < 0L) return getProGoals(context, userId)
        val goalId = UUID.randomUUID().toString()
        val initialContribs = if (startingContribution > 0L) {
            listOf(ProGoalContributionRecord(goalId = goalId, amount = startingContribution, note = "Starting Contribution"))
        } else emptyList()
        val newGoal = ProGoalRecord(
            id = goalId,
            name = name.trim(),
            description = description.trim(),
            category = category,
            priority = priority,
            targetAmount = targetAmount,
            targetDate = targetDate,
            contributions = initialContribs
        )
        val updated = listOf(newGoal) + getProGoals(context, userId)
        saveProGoals(context, userId, updated)
        return updated
    }

    fun addProGoalContribution(
        context: Context,
        userId: String,
        goalId: String,
        amount: Long,
        note: String = "Contribution",
        contributionId: String = UUID.randomUUID().toString()
    ): List<ProGoalRecord> {
        if (amount <= 0L) return getProGoals(context, userId)
        val updated = getProGoals(context, userId).map { g ->
            if (g.id == goalId && g.contributions.none { it.id == contributionId }) {
                g.copy(contributions = g.contributions + ProGoalContributionRecord(id = contributionId, goalId = goalId, amount = amount, note = note))
            } else g
        }
        saveProGoals(context, userId, updated)
        return updated
    }

    fun editOrReverseGoalContribution(
        context: Context,
        userId: String,
        goalId: String,
        contributionId: String,
        newAmountOrZeroToReverse: Long
    ): List<ProGoalRecord> {
        if (newAmountOrZeroToReverse < 0L) return getProGoals(context, userId)
        val updated = getProGoals(context, userId).map { g ->
            if (g.id == goalId) {
                val nextContribs = if (newAmountOrZeroToReverse == 0L) {
                    g.contributions.filterNot { it.id == contributionId }
                } else {
                    g.contributions.map { c -> if (c.id == contributionId) c.copy(amount = newAmountOrZeroToReverse) else c }
                }
                g.copy(contributions = nextContribs)
            } else g
        }
        saveProGoals(context, userId, updated)
        return updated
    }

    fun updateProGoalStatus(context: Context, userId: String, goalId: String, newStatus: String): List<ProGoalRecord> {
        val updated = getProGoals(context, userId).map { g ->
            if (g.id == goalId) g.copy(status = newStatus) else g
        }
        saveProGoals(context, userId, updated)
        return updated
    }

    private fun saveProGoals(context: Context, userId: String, list: List<ProGoalRecord>) {
        val arr = JSONArray()
        list.forEach { g ->
            val cArr = JSONArray()
            g.contributions.distinctBy { it.id }.forEach { c ->
                cArr.put(JSONObject().apply {
                    put("id", c.id); put("goalId", c.goalId); put("amount", c.amount)
                    put("note", c.note); put("timestampMillis", c.timestampMillis)
                })
            }
            arr.put(JSONObject().apply {
                put("id", g.id); put("name", g.name); put("description", g.description)
                put("category", g.category); put("priority", g.priority)
                put("targetAmount", g.targetAmount); put("targetDate", g.targetDate)
                put("status", g.status); put("contributions", cArr)
                put("createdAtMillis", g.createdAtMillis)
            })
        }
        prefs(context).edit().putString("${userId}_m14_goals", arr.toString()).apply()
    }

    // =========================================================================
    // MODULE 15 — MONEY CALENDAR PRO
    // =========================================================================
    fun buildProCalendarEntriesForMonth(
        context: Context,
        userId: String,
        year: Int,
        monthZeroBased: Int,
        expenses: List<ExpenseRecord>,
        filterType: String = "ALL"
    ): List<ProCalendarEntry> {
        val entries = mutableListOf<ProCalendarEntry>()
        val cal = Calendar.getInstance()
        val periodKey = String.format(Locale.US, "%04d-%02d", year, monthZeroBased + 1)

        // 1. Actual Transactions (COMPLETED)
        expenses.forEach { tx ->
            cal.timeInMillis = tx.txEpochMillis
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == monthZeroBased) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, day)
                val type = if (isIncomeTransaction(tx)) "INCOME" else "EXPENSE"
                entries.add(
                    ProCalendarEntry(
                        id = "tx_${tx.id}",
                        title = tx.title,
                        entryType = type,
                        status = "COMPLETED",
                        amount = tx.amount,
                        dayOfMonth = day,
                        dateKey = dateKey,
                        details = "${tx.category} · ${tx.paymentMode}"
                    )
                )
            }
        }

        // 2. Smart Bills (COMPLETED if paid for periodKey, else SCHEDULED)
        getSmartBills(context, userId).filter { it.status == "ACTIVE" }.forEach { bill ->
            val validDay = resolveValidDayOfMonth(year, monthZeroBased, bill.dueDayOfMonth)
            val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, validDay)
            val paidAmt = bill.paidPeriods[periodKey]
            entries.add(
                ProCalendarEntry(
                    id = "bill_${bill.id}_$periodKey",
                    title = bill.title,
                    entryType = "BILL",
                    status = if (paidAmt != null) "COMPLETED" else "SCHEDULED",
                    amount = paidAmt ?: bill.expectedAmount,
                    dayOfMonth = validDay,
                    dateKey = dateKey,
                    details = "${bill.category} (${bill.frequency})"
                )
            )
        }

        // 3. Pro Loans / EMIs (SCHEDULED)
        getProLoans(context, userId).forEach { loan ->
            val validDay = resolveValidDayOfMonth(year, monthZeroBased, loan.emiPaymentDay)
            val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, validDay)
            entries.add(
                ProCalendarEntry(
                    id = "emi_${loan.id}_$periodKey",
                    title = "${loan.name} EMI",
                    entryType = "EMI",
                    status = "SCHEDULED",
                    amount = loan.monthlyEmi,
                    dayOfMonth = validDay,
                    dateKey = dateKey,
                    details = "EMI Day $validDay · Rate ${loan.annualInterestRatePct}%"
                )
            )
        }

        val deduped = entries.distinctBy { it.id }.sortedBy { it.dayOfMonth }
        return if (filterType == "ALL") deduped else deduped.filter { it.entryType == filterType }
    }

    // =========================================================================
    // MODULE 16 — SMART MONEY RULES
    // =========================================================================
    fun getSmartRules(context: Context, userId: String): List<ProSmartMoneyRule> {
        val raw = prefs(context).getString("${userId}_m16_rules", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                ProSmartMoneyRule(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    triggerType = o.optString("triggerType"),
                    matchingCriteria = o.optString("matchingCriteria"),
                    suggestedActionType = o.optString("suggestedActionType"),
                    suggestedActionValue = o.optString("suggestedActionValue"),
                    priority = o.optInt("priority", 50),
                    enabled = o.optBoolean("enabled", true)
                )
            }.sortedByDescending { it.priority }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSmartRule(context: Context, userId: String, rule: ProSmartMoneyRule): Result<List<ProSmartMoneyRule>> {
        if (rule.name.isBlank() || rule.matchingCriteria.isBlank() || rule.suggestedActionValue.isBlank()) {
            return Result.failure(IllegalArgumentException("Rule name, criteria, and action value are required."))
        }
        val existing = getSmartRules(context, userId).toMutableList()
        val hasConflict = existing.any {
            it.id != rule.id &&
                it.triggerType == rule.triggerType &&
                it.matchingCriteria.equals(rule.matchingCriteria, ignoreCase = true) &&
                it.suggestedActionType == rule.suggestedActionType &&
                !it.suggestedActionValue.equals(rule.suggestedActionValue, ignoreCase = true)
        }
        if (hasConflict) {
            return Result.failure(IllegalArgumentException("Conflicting rule detected for the same trigger and matching criteria."))
        }
        val idx = existing.indexOfFirst { it.id == rule.id }
        if (idx >= 0) existing[idx] = rule else existing.add(rule)
        val sorted = existing.sortedByDescending { it.priority }
        val arr = JSONArray()
        sorted.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id); put("name", r.name); put("triggerType", r.triggerType)
                put("matchingCriteria", r.matchingCriteria); put("suggestedActionType", r.suggestedActionType)
                put("suggestedActionValue", r.suggestedActionValue); put("priority", r.priority)
                put("enabled", r.enabled)
            })
        }
        prefs(context).edit().putString("${userId}_m16_rules", arr.toString()).apply()
        return Result.success(sorted)
    }

    fun evaluateSmartRulesOnTransactions(
        rules: List<ProSmartMoneyRule>,
        expenses: List<ExpenseRecord>
    ): List<ProRuleSuggestion> {
        val activeRules = rules.filter { it.enabled }.sortedByDescending { it.priority }
        val seenPairs = mutableSetOf<String>()
        val suggestions = mutableListOf<ProRuleSuggestion>()

        expenses.forEach { tx ->
            activeRules.forEach { r ->
                val pairKey = "${r.id}_${tx.id}"
                if (pairKey !in seenPairs) {
                    val matches = when (r.triggerType) {
                        "MERCHANT_CONTAINS" -> tx.title.contains(r.matchingCriteria, ignoreCase = true)
                        "PAYMENT_MODE_EQUALS" -> tx.paymentMode.equals(r.matchingCriteria, ignoreCase = true)
                        "AMOUNT_ABOVE" -> tx.amount > (r.matchingCriteria.toLongOrNull() ?: Long.MAX_VALUE)
                        "CATEGORY_EQUALS" -> tx.category.equals(r.matchingCriteria, ignoreCase = true)
                        else -> false
                    }
                    if (matches) {
                        seenPairs.add(pairKey)
                        suggestions.add(
                            ProRuleSuggestion(
                                ruleId = r.id,
                                ruleName = r.name,
                                transactionId = tx.id,
                                transactionTitle = tx.title,
                                suggestedActionType = r.suggestedActionType,
                                suggestedActionValue = r.suggestedActionValue,
                                requiresUserConfirmation = true
                            )
                        )
                    }
                }
            }
        }
        return suggestions
    }

    // =========================================================================
    // MODULE 17 — FINANCIAL DOCUMENT VAULT
    // =========================================================================
    private val ALLOWED_DOC_MIMES = setOf(
        "application/pdf", "image/jpeg", "image/jpg", "image/png", "image/webp"
    )
    private const val MAX_DOC_SIZE_BYTES = 10L * 1024L * 1024L // 10 MB

    fun uploadDocumentMetadata(
        context: Context,
        requestingUserId: String,
        fileName: String,
        mimeType: String,
        fileSizeBytes: Long,
        category: String,
        tags: List<String>,
        attachedTransactionId: String = "",
        notes: String = ""
    ): Result<ProDocumentVaultItem> {
        val normMime = mimeType.trim().lowercase(Locale.US)
        val lowerName = fileName.trim().lowercase(Locale.US)
        val validExt = lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") || lowerName.endsWith(".webp")
        if (normMime !in ALLOWED_DOC_MIMES || !validExt) {
            return Result.failure(IllegalArgumentException("Unsupported file type. Only PDF, JPG, PNG, and WEBP documents are permitted."))
        }
        if (fileSizeBytes <= 0L || fileSizeBytes > MAX_DOC_SIZE_BYTES) {
            return Result.failure(IllegalArgumentException("File size must be between 1 byte and 10 MB."))
        }
        val item = ProDocumentVaultItem(
            ownerUserId = requestingUserId,
            fileName = fileName.trim(),
            mimeType = normMime,
            fileSizeBytes = fileSizeBytes,
            category = category,
            tags = tags,
            attachedTransactionId = attachedTransactionId,
            notes = notes.trim()
        )
        val current = getVaultDocuments(context, requestingUserId).toMutableList()
        current.add(0, item)
        saveVaultDocuments(context, requestingUserId, current)
        return Result.success(item)
    }

    fun getVaultDocuments(context: Context, requestingUserId: String): List<ProDocumentVaultItem> {
        val raw = prefs(context).getString("${requestingUserId}_m17_docs", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val owner = o.optString("ownerUserId")
                if (owner != requestingUserId) return@mapNotNull null // Strict user-level authorization
                val tagsArr = o.optJSONArray("tags") ?: JSONArray()
                val tags = (0 until tagsArr.length()).map { tagsArr.optString(it) }
                ProDocumentVaultItem(
                    id = o.optString("id"),
                    ownerUserId = owner,
                    fileName = o.optString("fileName"),
                    mimeType = o.optString("mimeType"),
                    fileSizeBytes = o.optLong("fileSizeBytes"),
                    category = o.optString("category"),
                    tags = tags,
                    attachedTransactionId = o.optString("attachedTransactionId"),
                    notes = o.optString("notes"),
                    entryMethod = o.optString("entryMethod", "MANUAL_VERIFIED"),
                    uploadedAtMillis = o.optLong("uploadedAtMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getDocumentAuthorized(context: Context, requestingUserId: String, documentId: String): ProDocumentVaultItem? =
        getVaultDocuments(context, requestingUserId).find { it.id == documentId && it.ownerUserId == requestingUserId }

    fun deleteVaultDocument(context: Context, requestingUserId: String, documentId: String): List<ProDocumentVaultItem> {
        val next = getVaultDocuments(context, requestingUserId).filterNot { it.id == documentId && it.ownerUserId == requestingUserId }
        saveVaultDocuments(context, requestingUserId, next)
        return next
    }

    private fun saveVaultDocuments(context: Context, userId: String, list: List<ProDocumentVaultItem>) {
        val arr = JSONArray()
        list.filter { it.ownerUserId == userId }.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id); put("ownerUserId", d.ownerUserId); put("fileName", d.fileName)
                put("mimeType", d.mimeType); put("fileSizeBytes", d.fileSizeBytes); put("category", d.category)
                put("tags", JSONArray(d.tags)); put("attachedTransactionId", d.attachedTransactionId)
                put("notes", d.notes); put("entryMethod", d.entryMethod); put("uploadedAtMillis", d.uploadedAtMillis)
            })
        }
        prefs(context).edit().putString("${userId}_m17_docs", arr.toString()).apply()
    }

    // =========================================================================
    // MODULE 18 — ADVANCED SPLIT EXPENSE MANAGER
    // =========================================================================
    fun computeSplitShares(
        totalAmount: Long,
        participantIds: List<String>,
        splitType: String,
        customAmountsOrPercentages: Map<String, Long> = emptyMap()
    ): Result<Map<String, Long>> {
        if (totalAmount <= 0L || participantIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("Amount must be positive and participants non-empty."))
        }
        return when (splitType.uppercase(Locale.US)) {
            "EQUAL" -> {
                val n = participantIds.size
                val baseShare = totalAmount / n
                val remainder = totalAmount % n
                val shares = mutableMapOf<String, Long>()
                participantIds.forEachIndexed { idx, pid ->
                    shares[pid] = baseShare + (if (idx == 0) remainder else 0L)
                }
                Result.success(shares)
            }
            "CUSTOM" -> {
                val sum = participantIds.sumOf { customAmountsOrPercentages[it] ?: 0L }
                if (sum != totalAmount) {
                    Result.failure(IllegalArgumentException("Custom split shares (₹$sum) must equal total expense (₹$totalAmount)."))
                } else {
                    Result.success(participantIds.associateWith { customAmountsOrPercentages[it] ?: 0L })
                }
            }
            "PERCENTAGE" -> {
                val pctSum = participantIds.sumOf { customAmountsOrPercentages[it] ?: 0L }
                if (pctSum != 100L) {
                    Result.failure(IllegalArgumentException("Percentage shares ($pctSum%) must total 100%."))
                } else {
                    val shares = mutableMapOf<String, Long>()
                    var allocated = 0L
                    participantIds.forEachIndexed { idx, pid ->
                        val pct = customAmountsOrPercentages[pid] ?: 0L
                        val amt = if (idx == participantIds.lastIndex) {
                            totalAmount - allocated
                        } else {
                            val s = (totalAmount * pct) / 100L
                            allocated += s
                            s
                        }
                        shares[pid] = amt
                    }
                    Result.success(shares)
                }
            }
            else -> Result.failure(IllegalArgumentException("Unsupported split type: $splitType"))
        }
    }

    fun calculateSplitNetBalances(
        participants: List<ProSplitParticipant>,
        expenses: List<ProSplitExpenseRecord>,
        settlements: List<ProSplitSettlementRecord>
    ): Map<String, Long> {
        val net = participants.associate { it.id to 0L }.toMutableMap()
        expenses.forEach { exp ->
            net[exp.paidByParticipantId] = (net[exp.paidByParticipantId] ?: 0L) + exp.totalAmount
            exp.sharesByParticipantId.forEach { (pid, share) ->
                net[pid] = (net[pid] ?: 0L) - share
            }
        }
        settlements.forEach { st ->
            net[st.fromParticipantId] = (net[st.fromParticipantId] ?: 0L) + st.amount
            net[st.toParticipantId] = (net[st.toParticipantId] ?: 0L) - st.amount
        }
        return net
    }

    // =========================================================================
    // MODULE 19 — MULTI-CURRENCY INTELLIGENCE
    // =========================================================================
    fun getBaseCurrency(context: Context, userId: String): String =
        prefs(context).getString("${userId}_m19_base_currency", "INR") ?: "INR"

    fun setBaseCurrency(context: Context, userId: String, currencyCode: String) {
        prefs(context).edit().putString("${userId}_m19_base_currency", currencyCode.trim().uppercase(Locale.US)).apply()
    }

    fun recordMultiCurrencyTransaction(
        context: Context,
        userId: String,
        title: String,
        category: String,
        originalAmount: Double,
        currencyCode: String,
        manualExchangeRateToBase: Double,
        isForeignTravel: Boolean = true
    ): Result<ProMultiCurrencyTransaction> {
        if (originalAmount <= 0.0 || manualExchangeRateToBase <= 0.0) {
            return Result.failure(IllegalArgumentException("Amount and exchange rate must be positive."))
        }
        val baseCode = getBaseCurrency(context, userId)
        val converted = (originalAmount * manualExchangeRateToBase).roundToLong()
        val tx = ProMultiCurrencyTransaction(
            title = title.trim().ifBlank { "FX Transaction" },
            category = category,
            originalAmount = (originalAmount * 100.0).roundToInt() / 100.0,
            currencyCode = currencyCode.trim().uppercase(Locale.US),
            baseCurrencyCode = baseCode,
            exchangeRateUsed = manualExchangeRateToBase,
            convertedBaseAmount = converted,
            rateSourceLabel = "Manual Verified Rate (1 ${currencyCode.uppercase(Locale.US)} = $manualExchangeRateToBase $baseCode)",
            isForeignTravel = isForeignTravel
        )
        val current = getMultiCurrencyTransactions(context, userId).toMutableList()
        current.add(0, tx)
        val arr = JSONArray()
        current.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id); put("title", item.title); put("category", item.category)
                put("originalAmount", item.originalAmount); put("currencyCode", item.currencyCode)
                put("baseCurrencyCode", item.baseCurrencyCode); put("exchangeRateUsed", item.exchangeRateUsed)
                put("convertedBaseAmount", item.convertedBaseAmount); put("rateSourceLabel", item.rateSourceLabel)
                put("isForeignTravel", item.isForeignTravel); put("recordedAtMillis", item.recordedAtMillis)
            })
        }
        prefs(context).edit().putString("${userId}_m19_fx_txs", arr.toString()).apply()
        return Result.success(tx)
    }

    fun getMultiCurrencyTransactions(context: Context, userId: String): List<ProMultiCurrencyTransaction> {
        val raw = prefs(context).getString("${userId}_m19_fx_txs", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                ProMultiCurrencyTransaction(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    category = o.optString("category"),
                    originalAmount = o.optDouble("originalAmount"),
                    currencyCode = o.optString("currencyCode"),
                    baseCurrencyCode = o.optString("baseCurrencyCode", "INR"),
                    exchangeRateUsed = o.optDouble("exchangeRateUsed"),
                    convertedBaseAmount = o.optLong("convertedBaseAmount"),
                    rateSourceLabel = o.optString("rateSourceLabel"),
                    isForeignTravel = o.optBoolean("isForeignTravel", true),
                    recordedAtMillis = o.optLong("recordedAtMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // =========================================================================
    // MODULE 20 — PRO NOTIFICATION INTELLIGENCE
    // =========================================================================
    fun isWithinQuietHours(currentHour24: Int, quietStartHour: Int, quietEndHour: Int): Boolean {
        val h = currentHour24.coerceIn(0, 23)
        return if (quietStartHour <= quietEndHour) {
            h in quietStartHour until quietEndHour
        } else {
            h >= quietStartHour || h < quietEndHour
        }
    }

    fun getReadNotificationIds(context: Context, userId: String): Set<String> =
        prefs(context).getStringSet("${userId}_m20_read_ids", emptySet()) ?: emptySet()

    fun markNotificationRead(context: Context, userId: String, notificationId: String) {
        val next = getReadNotificationIds(context, userId).toMutableSet().apply { add(notificationId) }
        prefs(context).edit().putStringSet("${userId}_m20_read_ids", next).apply()
    }

    fun buildProNotifications(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        dailyLimit: Long,
        monthlyBudget: Long,
        preferences: ProNotificationPreferences = ProNotificationPreferences()
    ): List<ProNotificationItem> {
        val readIds = getReadNotificationIds(context, userId)
        val items = mutableListOf<ProNotificationItem>()
        val now = System.currentTimeMillis()
        val periodKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))

        // Only active (non-cancelled, non-paused) bills generate reminders
        getSmartBills(context, userId)
            .filter { it.status == "ACTIVE" && it.reminderEnabled && !it.paidPeriods.containsKey(periodKey) }
            .forEach { bill ->
                val id = "notif_bill_${bill.id}_$periodKey"
                items.add(
                    ProNotificationItem(
                        id = id,
                        category = "UPCOMING_BILL",
                        title = "Upcoming Bill: ${bill.title}",
                        message = "Due on day ${bill.dueDayOfMonth} · Expected ₹${bill.expectedAmount}",
                        priority = "HIGH",
                        deepLinkModuleId = "m13_bills",
                        isRead = id in readIds,
                        createdAtMillis = now
                    )
                )
            }

        getProLoans(context, userId).forEach { loan ->
            val id = "notif_emi_${loan.id}_$periodKey"
            items.add(
                ProNotificationItem(
                    id = id,
                    category = "EMI_REMINDER",
                    title = "EMI Reminder: ${loan.name}",
                    message = "Monthly EMI of ₹${loan.monthlyEmi} scheduled on day ${loan.emiPaymentDay}",
                    priority = "HIGH",
                    deepLinkModuleId = "m12_loans",
                    isRead = id in readIds,
                    createdAtMillis = now - 60_000L
                )
            )
        }

        getProGoals(context, userId).filter { it.status == "ACTIVE" && it.progressPct >= 50 }.forEach { goal ->
            val id = "notif_goal_${goal.id}_${goal.progressPct / 25}"
            items.add(
                ProNotificationItem(
                    id = id,
                    category = "GOAL_MILESTONE",
                    title = "Goal Milestone: ${goal.name} at ${goal.progressPct}%",
                    message = "Saved ₹${goal.savedAmount} of ₹${goal.targetAmount} target.",
                    priority = "MEDIUM",
                    deepLinkModuleId = "m14_goals",
                    isRead = id in readIds,
                    createdAtMillis = now - 120_000L
                )
            )
        }

        return items
            .distinctBy { it.id }
            .filter { it.category in preferences.enabledCategories }
    }
}
