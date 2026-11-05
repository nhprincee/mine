package com.example.data.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

// ============================================================================
// KHARCHEE PRO — MODULES 41 TO 50 DATA MODELS & PERSISTENT INTELLIGENCE STORE
// ============================================================================

// --- MODULE 41: STUDENT FINANCE MODE ---
data class StudentFeeScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String, // e.g. "Semester 2 Tuition", "Hostel Fee", "Exam Registration"
    val institution: String,
    val amount: Long,
    val dueEpochMillis: Long,
    val academicYear: String, // e.g. "2026-2027"
    val isPaid: Boolean = false,
    val paidEpochMillis: Long = 0L,
    val notes: String = ""
)

data class StudentSavingsGoalItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String, // e.g. "Engineering Laptop", "Certification Exam", "Study Abroad Fund"
    val targetAmount: Long,
    val savedAmount: Long,
    val targetDateLabel: String
)

data class StudentExpenseEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // Books, Stationery, Transport, Food, or Custom
    val amount: Long,
    val epochMillis: Long = System.currentTimeMillis(),
    val academicYear: String = "2026-2027"
)

data class StudentFinanceConfig(
    val monthlyAllowance: Long = 15000L,
    val extraPocketMoneyThisMonth: Long = 0L,
    val currentAcademicYear: String = "2026-2027",
    val customCategories: List<String> = listOf("Books", "Stationery", "Transport", "Food", "Hostel & Mess", "Exam & Lab Fees")
)

data class StudentFinanceReport(
    val config: StudentFinanceConfig,
    val totalAllowancePool: Long,
    val monthStudentSpend: Long,
    val allowanceRemaining: Long,
    val isOverAllowance: Boolean,
    val categoryBreakdown: Map<String, Long>,
    val academicYearTotalSpend: Long,
    val academicYearFeePaid: Long,
    val academicYearFeePending: Long,
    val upcomingFees: List<StudentFeeScheduleItem>,
    val goals: List<StudentSavingsGoalItem>,
    val expenses: List<StudentExpenseEntry>
)

// --- MODULE 42: SIDE-INCOME TRACKER ---
enum class SideIncomePaymentStatus {
    EXPECTED,
    PENDING,
    CONFIRMED_RECEIVED
}

data class SideIncomeSourceRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // e.g. "UI/UX Freelance", "Weekend Tutoring", "Content Creation"
    val clientOrPlatform: String,
    val defaultPaymentMethod: String = "UPI / Bank Transfer"
)

data class SideIncomePaymentRecord(
    val id: String = UUID.randomUUID().toString(),
    val sourceId: String,
    val sourceName: String,
    val description: String,
    val expectedAmount: Long,
    val receivedAmount: Long = 0L, // Only populated when CONFIRMED_RECEIVED by explicit user action
    val status: SideIncomePaymentStatus = SideIncomePaymentStatus.EXPECTED,
    val dueOrExpectedEpochMillis: Long = System.currentTimeMillis(),
    val receivedEpochMillis: Long = 0L,
    val paymentMethod: String = "UPI",
    val supportingDocId: String = "", // Links to Module 17 Document Vault ID or label
    val supportingDocLabel: String = "",
    val confirmedByUser: Boolean = false
)

data class SideIncomeAnalyticsSummary(
    val totalConfirmedReceived: Long,
    val totalPendingAndExpected: Long,
    val currentMonthConfirmed: Long,
    val sourceComparisonConfirmed: Map<String, Long>,
    val sourceComparisonPending: Map<String, Long>,
    val monthlyConfirmedBreakdown: Map<String, Long>
)

// --- MODULE 43: INVOICE & RECEIPT GENERATOR ---
enum class ProInvoiceStatus {
    DRAFT,
    ISSUED_UNPAID,
    PAID_CONFIRMED
}

data class ProInvoiceLineItem(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val quantity: Int,
    val unitPrice: Long
) {
    val lineTotal: Long get() = max(0, quantity).toLong() * max(0L, unitPrice)
}

data class ProInvoiceRecord(
    val id: String = UUID.randomUUID().toString(),
    val invoiceNumber: String,
    val issuerName: String,
    val customerName: String,
    val customerContact: String,
    val issueEpochMillis: Long = System.currentTimeMillis(),
    val dueEpochMillis: Long = System.currentTimeMillis() + 7 * 86_400_000L,
    val lineItems: List<ProInvoiceLineItem>,
    val discountAmount: Long = 0L,
    val taxRatePercent: Double = 0.0,
    val taxRegistrationVerified: Boolean = false, // Must be false unless verified; controls non-tax invoice disclaimer
    val status: ProInvoiceStatus = ProInvoiceStatus.ISSUED_UNPAID,
    val paymentConfirmedByUser: Boolean = false,
    val paidEpochMillis: Long = 0L,
    val paymentMethodUsed: String = "",
    val receiptNumber: String = "",
    val notes: String = ""
) {
    val subtotal: Long get() = lineItems.sumOf { it.lineTotal }
    val taxableAmount: Long get() = max(0L, subtotal - max(0L, discountAmount))
    val taxAmount: Long get() = (taxableAmount * (max(0.0, taxRatePercent) / 100.0)).roundToLong()
    val grandTotal: Long get() = taxableAmount + taxAmount
    val complianceNotice: String
        get() = if (taxRegistrationVerified) {
            "Tax Configuration Verified by User. Verify statutory jurisdiction rules before filing."
        } else {
            "STANDARD COMMERCIAL / PERSONAL INVOICE — Not a legally certified statutory GST/VAT tax invoice unless statutory registration details are verified."
        }
}

// --- MODULE 44: PURCHASE RETURN & WARRANTY TRACKER ---
enum class WarrantyProductStatus {
    ACTIVE_RETURN_WINDOW,
    UNDER_WARRANTY,
    EXPIRING_SOON,
    WARRANTY_EXPIRED,
    RETURNED_OR_CLAIMED
}

data class WarrantyProductRecord(
    val id: String = UUID.randomUUID().toString(),
    val productName: String,
    val brandOrStore: String,
    val serialOrOrderNo: String,
    val purchaseAmount: Long,
    val purchaseEpochMillis: Long,
    val returnDeadlineEpochMillis: Long,
    val warrantyStartEpochMillis: Long,
    val warrantyExpiryEpochMillis: Long,
    val invoiceAttachmentLabel: String = "",
    val userStatusOverride: String = "", // e.g. "RETURNED" or ""
    val coverageNotes: String = ""
) {
    fun evaluateStatus(nowMillis: Long = System.currentTimeMillis()): WarrantyProductStatus {
        if (userStatusOverride.equals("RETURNED", ignoreCase = true)) {
            return WarrantyProductStatus.RETURNED_OR_CLAIMED
        }
        val thirtyDaysMs = 30L * 86_400_000L
        return when {
            nowMillis <= returnDeadlineEpochMillis -> WarrantyProductStatus.ACTIVE_RETURN_WINDOW
            nowMillis > warrantyExpiryEpochMillis -> WarrantyProductStatus.WARRANTY_EXPIRED
            warrantyExpiryEpochMillis - nowMillis <= thirtyDaysMs -> WarrantyProductStatus.EXPIRING_SOON
            else -> WarrantyProductStatus.UNDER_WARRANTY
        }
    }

    val warrantyDisclaimer: String
        get() = "User-recorded warranty & return schedule. KHARCHEE displays dates entered by you and does not guarantee manufacturer or seller warranty coverage."
}

// --- MODULE 45: PRICE WATCHLIST ---
data class PriceObservationPoint(
    val epochMillis: Long = System.currentTimeMillis(),
    val observedPrice: Long,
    val sourceNote: String = "Manual entry"
)

data class PriceWatchlistItem(
    val id: String = UUID.randomUUID().toString(),
    val productName: String,
    val merchantOrUrlLabel: String,
    val targetPrice: Long,
    val currentObservedPrice: Long,
    val observations: List<PriceObservationPoint>,
    val externalApiConfigured: Boolean = false
) {
    val isTargetReached: Boolean get() = currentObservedPrice in 1..targetPrice
    val lowestPriceObserved: Long get() = observations.minOfOrNull { it.observedPrice } ?: currentObservedPrice
    val highestPriceObserved: Long get() = observations.maxOfOrNull { it.observedPrice } ?: currentObservedPrice
    val priceDeltaFromFirst: Long
        get() {
            val first = observations.firstOrNull()?.observedPrice ?: currentObservedPrice
            return currentObservedPrice - first
        }
}

// --- MODULE 46: TRAVEL BUDGET PLANNER ---
enum class TravelExpenseCategory(val label: String) {
    TRANSPORT("Transport"),
    ACCOMMODATION("Accommodation"),
    FOOD("Food & Dining"),
    ACTIVITIES("Activities & Sightseeing"),
    OTHER("Misc / Emergency")
}

data class TravelExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // Transport, Accommodation, Food & Dining, Activities & Sightseeing, Misc
    val plannedAmount: Long,
    val actualConfirmedAmount: Long = 0L,
    val isConfirmedActualExpense: Boolean = false, // Strictly separated from actual transactions until confirmed
    val confirmedEpochMillis: Long = 0L
)

data class TravelTripPlan(
    val id: String = UUID.randomUUID().toString(),
    val tripName: String,
    val destination: String,
    val startDateLabel: String,
    val endDateLabel: String,
    val totalEstimatedBudget: Long,
    val items: List<TravelExpenseItem> = emptyList()
) {
    val totalPlannedCost: Long get() = items.sumOf { it.plannedAmount }
    val totalActualConfirmedCost: Long get() = items.filter { it.isConfirmedActualExpense }.sumOf { it.actualConfirmedAmount }
    val remainingBudgetFromActual: Long get() = totalEstimatedBudget - totalActualConfirmedCost
    val remainingBudgetFromPlanned: Long get() = totalEstimatedBudget - totalPlannedCost
    val varianceActualVsPlanned: Long get() = totalActualConfirmedCost - totalPlannedCost
}

// --- MODULE 47: EVENT BUDGET PLANNER ---
data class EventContributionEntry(
    val id: String = UUID.randomUUID().toString(),
    val contributorName: String,
    val expectedAmount: Long,
    val confirmedReceivedAmount: Long = 0L,
    val isConfirmedReceived: Boolean = false // Expected contributions NEVER count as received money until confirmed
)

data class EventExpenseEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // Venue, Catering, Decor, Gifts, Logistics, Custom
    val plannedCost: Long,
    val actualSpent: Long = 0L,
    val isExpenseConfirmed: Boolean = false
)

data class ProEventBudgetPlan(
    val id: String = UUID.randomUUID().toString(),
    val eventName: String,
    val eventDateLabel: String,
    val baseAllocatedBudget: Long,
    val categories: List<String> = listOf("Venue", "Catering", "Decor", "Gifts", "Logistics"),
    val contributions: List<EventContributionEntry> = emptyList(),
    val expenses: List<EventExpenseEntry> = emptyList()
) {
    val totalExpectedContributionsUnconfirmed: Long
        get() = contributions.filter { !it.isConfirmedReceived }.sumOf { it.expectedAmount }
    val totalConfirmedContributionsReceived: Long
        get() = contributions.filter { it.isConfirmedReceived }.sumOf { it.confirmedReceivedAmount }
    // Critical rule: Available confirmed funds ONLY include baseAllocatedBudget + confirmed contributions (never expected)
    val totalAvailableConfirmedFunds: Long
        get() = baseAllocatedBudget + totalConfirmedContributionsReceived
    val totalActualExpenses: Long
        get() = expenses.filter { it.isExpenseConfirmed }.sumOf { it.actualSpent }
    val totalPlannedExpenses: Long
        get() = expenses.sumOf { it.plannedCost }
    val remainingConfirmedBudget: Long
        get() = totalAvailableConfirmedFunds - totalActualExpenses
}

// --- MODULE 48: FINANCIAL LEARNING ACADEMY ---
data class AcademyQuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class AcademyLessonModule(
    val id: String,
    val topicOrder: Int,
    val title: String,
    val categoryTag: String,
    val readingTimeMinutes: Int,
    val keyTakeaways: List<String>,
    val contentSummary: String,
    val quizQuestions: List<AcademyQuizQuestion>
)

data class AcademyUserProgress(
    val completedLessonIds: Set<String> = emptySet(),
    val quizScoresByLessonId: Map<String, Int> = emptyMap(), // score percentage 0..100
    val lastStudiedEpochMillis: Long = 0L
) {
    val completionPercent: Int
        get() = ((completedLessonIds.size.toDouble() / 7.0) * 100.0).toInt().coerceIn(0, 100)
}

// --- MODULE 49: INCOME & EXPENSE DOCUMENT MATCHER ---
enum class DocumentMatchStatus {
    PENDING_USER_REVIEW,
    CONFIRMED_LINKED,
    REJECTED_BY_USER
}

data class ExtractedDocumentCandidate(
    val id: String = UUID.randomUUID().toString(),
    val documentTitle: String,
    val rawSnippet: String,
    val extractedMerchant: String,
    val extractedAmount: Long,
    val extractedDateLabel: String,
    val extractedEpochMillis: Long,
    val suggestedTransactionId: String?,
    val suggestedTransactionTitle: String?,
    val suggestedTransactionAmount: Long?,
    val confidencePercent: Int,
    val possibleDuplicateFlag: Boolean,
    val duplicateReason: String = "",
    val status: DocumentMatchStatus = DocumentMatchStatus.PENDING_USER_REVIEW,
    val reviewedAtMillis: Long = 0L
)

data class DocumentMatchAuditEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestampMillis: Long = System.currentTimeMillis(),
    val candidateId: String,
    val documentTitle: String,
    val transactionId: String,
    val action: String, // "CONFIRMED_LINK" or "REJECTED_MATCH"
    val details: String
)

// --- MODULE 50: PERSONAL FINANCE DATA INSIGHTS API ---
data class ScopedApiTokenRecord(
    val id: String = UUID.randomUUID().toString(),
    val ownerUserId: String,
    val label: String,
    val tokenPrefix: String, // e.g. "khp_live_8f92..."
    val tokenSha256Hash: String,
    val scopes: Set<String>, // "read:summary", "read:transactions", "read:goals"
    val createdAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long = System.currentTimeMillis() + 30L * 86_400_000L,
    val isRevoked: Boolean = false,
    val revokedAtMillis: Long = 0L
)

data class ApiAccessLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestampMillis: Long = System.currentTimeMillis(),
    val ownerUserId: String,
    val tokenId: String,
    val endpoint: String,
    val statusCode: Int,
    val statusMessage: String
)

data class PersonalApiGatewayResponse(
    val statusCode: Int,
    val endpoint: String,
    val isSuccess: Boolean,
    val rateLimitRemaining: Int,
    val jsonPayload: String,
    val errorExplanation: String = ""
)

object KharcheeProModules41To50Store {
    private const val PREFS_NAME = "kharchee_pro_modules_41_to_50_prefs"
    private const val RATE_LIMIT_MAX_REQUESTS_PER_MINUTE = 15

    // In-memory sliding window tracker per tokenId for rate limiting
    private val tokenRequestTimestamps = mutableMapOf<String, MutableList<Long>>()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ========================================================================
    // MODULE 41: STUDENT FINANCE MODE
    // ========================================================================

    fun getStudentConfig(context: Context, userId: String): StudentFinanceConfig {
        val raw = prefs(context).getString("${userId}_m41_config", null) ?: return StudentFinanceConfig()
        return try {
            val obj = JSONObject(raw)
            val catsArr = obj.optJSONArray("customCategories")
            val cats = mutableListOf<String>()
            if (catsArr != null) {
                for (i in 0 until catsArr.length()) {
                    cats.add(catsArr.getString(i))
                }
            }
            if (cats.isEmpty()) {
                cats.addAll(listOf("Books", "Stationery", "Transport", "Food", "Hostel & Mess", "Exam & Lab Fees"))
            }
            StudentFinanceConfig(
                monthlyAllowance = obj.optLong("monthlyAllowance", 15000L),
                extraPocketMoneyThisMonth = obj.optLong("extraPocketMoneyThisMonth", 0L),
                currentAcademicYear = obj.optString("currentAcademicYear", "2026-2027"),
                customCategories = cats.distinct()
            )
        } catch (_: Exception) {
            StudentFinanceConfig()
        }
    }

    fun saveStudentConfig(context: Context, userId: String, config: StudentFinanceConfig) {
        val obj = JSONObject().apply {
            put("monthlyAllowance", max(0L, config.monthlyAllowance))
            put("extraPocketMoneyThisMonth", max(0L, config.extraPocketMoneyThisMonth))
            put("currentAcademicYear", config.currentAcademicYear.ifBlank { "2026-2027" })
            put("customCategories", JSONArray(config.customCategories.distinct()))
        }
        prefs(context).edit().putString("${userId}_m41_config", obj.toString()).apply()
    }

    fun addCustomStudentCategory(context: Context, userId: String, category: String): StudentFinanceConfig {
        val clean = category.trim()
        val current = getStudentConfig(context, userId)
        if (clean.isBlank()) return current
        val updated = current.copy(customCategories = (current.customCategories + clean).distinct())
        saveStudentConfig(context, userId, updated)
        return updated
    }

    fun getStudentFeeSchedules(context: Context, userId: String): List<StudentFeeScheduleItem> {
        val raw = prefs(context).getString("${userId}_m41_fees", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                StudentFeeScheduleItem(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    institution = o.optString("institution"),
                    amount = o.optLong("amount"),
                    dueEpochMillis = o.optLong("dueEpochMillis"),
                    academicYear = o.optString("academicYear", "2026-2027"),
                    isPaid = o.optBoolean("isPaid", false),
                    paidEpochMillis = o.optLong("paidEpochMillis", 0L),
                    notes = o.optString("notes", "")
                )
            }.sortedBy { it.dueEpochMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addStudentFeeSchedule(context: Context, userId: String, item: StudentFeeScheduleItem): List<StudentFeeScheduleItem> {
        val list = getStudentFeeSchedules(context, userId).toMutableList()
        list.add(item.copy(amount = max(0L, item.amount)))
        saveStudentFees(context, userId, list)
        return list.sortedBy { it.dueEpochMillis }
    }

    fun markStudentFeePaid(context: Context, userId: String, feeId: String, paid: Boolean): List<StudentFeeScheduleItem> {
        val updated = getStudentFeeSchedules(context, userId).map {
            if (it.id == feeId) {
                it.copy(
                    isPaid = paid,
                    paidEpochMillis = if (paid) System.currentTimeMillis() else 0L
                )
            } else it
        }
        saveStudentFees(context, userId, updated)
        return updated
    }

    private fun saveStudentFees(context: Context, userId: String, list: List<StudentFeeScheduleItem>) {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("institution", item.institution)
                put("amount", item.amount)
                put("dueEpochMillis", item.dueEpochMillis)
                put("academicYear", item.academicYear)
                put("isPaid", item.isPaid)
                put("paidEpochMillis", item.paidEpochMillis)
                put("notes", item.notes)
            })
        }
        prefs(context).edit().putString("${userId}_m41_fees", arr.toString()).apply()
    }

    fun getStudentExpenses(context: Context, userId: String): List<StudentExpenseEntry> {
        val raw = prefs(context).getString("${userId}_m41_expenses", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                StudentExpenseEntry(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    category = o.optString("category", "Books"),
                    amount = o.optLong("amount", 0L),
                    epochMillis = o.optLong("epochMillis", System.currentTimeMillis()),
                    academicYear = o.optString("academicYear", "2026-2027")
                )
            }.sortedByDescending { it.epochMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addStudentExpense(context: Context, userId: String, entry: StudentExpenseEntry): List<StudentExpenseEntry> {
        val list = getStudentExpenses(context, userId).toMutableList()
        list.add(0, entry.copy(amount = max(0L, entry.amount)))
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("category", item.category)
                put("amount", item.amount)
                put("epochMillis", item.epochMillis)
                put("academicYear", item.academicYear)
            })
        }
        prefs(context).edit().putString("${userId}_m41_expenses", arr.toString()).apply()
        return list
    }

    fun getStudentSavingsGoals(context: Context, userId: String): List<StudentSavingsGoalItem> {
        val raw = prefs(context).getString("${userId}_m41_goals", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                StudentSavingsGoalItem(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    targetAmount = o.optLong("targetAmount", 0L),
                    savedAmount = o.optLong("savedAmount", 0L),
                    targetDateLabel = o.optString("targetDateLabel", "")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addOrUpdateStudentSavingsGoal(context: Context, userId: String, goal: StudentSavingsGoalItem): List<StudentSavingsGoalItem> {
        val list = getStudentSavingsGoals(context, userId).toMutableList()
        val idx = list.indexOfFirst { it.id == goal.id }
        if (idx >= 0) list[idx] = goal else list.add(goal)
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("targetAmount", item.targetAmount)
                put("savedAmount", item.savedAmount)
                put("targetDateLabel", item.targetDateLabel)
            })
        }
        prefs(context).edit().putString("${userId}_m41_goals", arr.toString()).apply()
        return list
    }

    fun buildStudentFinanceReport(
        context: Context,
        userId: String,
        existingExpenses: List<ExpenseRecord> = emptyList()
    ): StudentFinanceReport {
        val config = getStudentConfig(context, userId)
        val studentEntries = getStudentExpenses(context, userId)
        val fees = getStudentFeeSchedules(context, userId)
        val goals = getStudentSavingsGoals(context, userId)

        val cal = Calendar.getInstance()
        val curMonth = cal.get(Calendar.MONTH)
        val curYear = cal.get(Calendar.YEAR)

        val monthEntries = studentEntries.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.epochMillis }
            c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear
        }

        // Also include main app transactions tagged in student categories if any
        val matchingMainTx = existingExpenses.filter { tx ->
            !isIncomeTransaction(tx) &&
                config.customCategories.any { cat -> tx.category.equals(cat, ignoreCase = true) }
        }

        val breakdown = mutableMapOf<String, Long>()
        config.customCategories.forEach { breakdown[it] = 0L }
        monthEntries.forEach { e ->
            breakdown[e.category] = (breakdown[e.category] ?: 0L) + e.amount
        }
        matchingMainTx.forEach { tx ->
            val key = config.customCategories.firstOrNull { it.equals(tx.category, ignoreCase = true) } ?: tx.category
            breakdown[key] = (breakdown[key] ?: 0L) + tx.amount
        }

        val monthSpend = breakdown.values.sum()
        val pool = config.monthlyAllowance + config.extraPocketMoneyThisMonth
        val remaining = pool - monthSpend

        val academicYearEntries = studentEntries.filter { it.academicYear == config.currentAcademicYear }
        val academicYearFees = fees.filter { it.academicYear == config.currentAcademicYear }

        return StudentFinanceReport(
            config = config,
            totalAllowancePool = pool,
            monthStudentSpend = monthSpend,
            allowanceRemaining = remaining,
            isOverAllowance = remaining < 0L,
            categoryBreakdown = breakdown,
            academicYearTotalSpend = academicYearEntries.sumOf { it.amount } + matchingMainTx.sumOf { it.amount },
            academicYearFeePaid = academicYearFees.filter { it.isPaid }.sumOf { it.amount },
            academicYearFeePending = academicYearFees.filter { !it.isPaid }.sumOf { it.amount },
            upcomingFees = fees.filter { !it.isPaid }.sortedBy { it.dueEpochMillis },
            goals = goals,
            expenses = studentEntries
        )
    }

    // ========================================================================
    // MODULE 42: SIDE-INCOME TRACKER
    // Rule: Never mark a payment as received without explicit user confirmation.
    // ========================================================================

    fun getSideIncomeSources(context: Context, userId: String): List<SideIncomeSourceRecord> {
        val raw = prefs(context).getString("${userId}_m42_sources", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                SideIncomeSourceRecord(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    clientOrPlatform = o.optString("clientOrPlatform"),
                    defaultPaymentMethod = o.optString("defaultPaymentMethod", "UPI")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addSideIncomeSource(context: Context, userId: String, source: SideIncomeSourceRecord): List<SideIncomeSourceRecord> {
        val list = getSideIncomeSources(context, userId).toMutableList()
        list.add(source)
        val arr = JSONArray()
        list.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("clientOrPlatform", s.clientOrPlatform)
                put("defaultPaymentMethod", s.defaultPaymentMethod)
            })
        }
        prefs(context).edit().putString("${userId}_m42_sources", arr.toString()).apply()
        return list
    }

    fun getSideIncomePayments(context: Context, userId: String): List<SideIncomePaymentRecord> {
        val raw = prefs(context).getString("${userId}_m42_payments", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                val statusStr = o.optString("status", SideIncomePaymentStatus.EXPECTED.name)
                val confirmed = o.optBoolean("confirmedByUser", false)
                // Enforce rule: if not explicitly confirmed by user, status cannot be CONFIRMED_RECEIVED
                val resolvedStatus = if (!confirmed && statusStr == SideIncomePaymentStatus.CONFIRMED_RECEIVED.name) {
                    SideIncomePaymentStatus.PENDING
                } else {
                    runCatching { SideIncomePaymentStatus.valueOf(statusStr) }.getOrDefault(SideIncomePaymentStatus.EXPECTED)
                }
                SideIncomePaymentRecord(
                    id = o.optString("id"),
                    sourceId = o.optString("sourceId"),
                    sourceName = o.optString("sourceName"),
                    description = o.optString("description"),
                    expectedAmount = o.optLong("expectedAmount", 0L),
                    receivedAmount = if (confirmed) o.optLong("receivedAmount", 0L) else 0L,
                    status = resolvedStatus,
                    dueOrExpectedEpochMillis = o.optLong("dueOrExpectedEpochMillis", System.currentTimeMillis()),
                    receivedEpochMillis = if (confirmed) o.optLong("receivedEpochMillis", 0L) else 0L,
                    paymentMethod = o.optString("paymentMethod", "UPI"),
                    supportingDocId = o.optString("supportingDocId", ""),
                    supportingDocLabel = o.optString("supportingDocLabel", ""),
                    confirmedByUser = confirmed
                )
            }.sortedByDescending { it.dueOrExpectedEpochMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Records a new expected or pending payment.
     * Even if caller passes CONFIRMED_RECEIVED without explicit confirmation, it is stored as EXPECTED/PENDING.
     */
    fun recordExpectedSideIncomePayment(
        context: Context,
        userId: String,
        payment: SideIncomePaymentRecord
    ): List<SideIncomePaymentRecord> {
        val safePayment = payment.copy(
            expectedAmount = max(0L, payment.expectedAmount),
            receivedAmount = 0L,
            status = if (payment.status == SideIncomePaymentStatus.CONFIRMED_RECEIVED) {
                SideIncomePaymentStatus.PENDING
            } else payment.status,
            confirmedByUser = false,
            receivedEpochMillis = 0L
        )
        val list = getSideIncomePayments(context, userId).toMutableList()
        list.add(0, safePayment)
        saveSideIncomePayments(context, userId, list)
        return list
    }

    /**
     * Explicitly confirms a side-income payment as received after user confirmation action.
     */
    fun confirmSideIncomePaymentReceived(
        context: Context,
        userId: String,
        paymentId: String,
        explicitUserConfirmation: Boolean,
        actualReceivedAmount: Long? = null,
        paymentMethod: String? = null
    ): List<SideIncomePaymentRecord> {
        if (!explicitUserConfirmation) {
            return getSideIncomePayments(context, userId)
        }
        val now = System.currentTimeMillis()
        val updated = getSideIncomePayments(context, userId).map { item ->
            if (item.id == paymentId) {
                val finalAmount = actualReceivedAmount?.let { max(0L, it) } ?: item.expectedAmount
                item.copy(
                    status = SideIncomePaymentStatus.CONFIRMED_RECEIVED,
                    receivedAmount = finalAmount,
                    receivedEpochMillis = now,
                    paymentMethod = paymentMethod ?: item.paymentMethod,
                    confirmedByUser = true
                )
            } else item
        }
        saveSideIncomePayments(context, userId, updated)
        return updated
    }

    private fun saveSideIncomePayments(context: Context, userId: String, list: List<SideIncomePaymentRecord>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("sourceId", p.sourceId)
                put("sourceName", p.sourceName)
                put("description", p.description)
                put("expectedAmount", p.expectedAmount)
                put("receivedAmount", if (p.confirmedByUser) p.receivedAmount else 0L)
                put("status", p.status.name)
                put("dueOrExpectedEpochMillis", p.dueOrExpectedEpochMillis)
                put("receivedEpochMillis", p.receivedEpochMillis)
                put("paymentMethod", p.paymentMethod)
                put("supportingDocId", p.supportingDocId)
                put("supportingDocLabel", p.supportingDocLabel)
                put("confirmedByUser", p.confirmedByUser)
            })
        }
        prefs(context).edit().putString("${userId}_m42_payments", arr.toString()).apply()
    }

    fun buildSideIncomeAnalytics(
        context: Context,
        userId: String,
        statusFilter: SideIncomePaymentStatus? = null,
        fromEpochMillis: Long? = null,
        toEpochMillis: Long? = null
    ): SideIncomeAnalyticsSummary {
        val all = getSideIncomePayments(context, userId).filter { p ->
            val statusMatches = statusFilter == null || p.status == statusFilter
            val refTime = if (p.confirmedByUser && p.receivedEpochMillis > 0L) p.receivedEpochMillis else p.dueOrExpectedEpochMillis
            val fromMatches = fromEpochMillis == null || refTime >= fromEpochMillis
            val toMatches = toEpochMillis == null || refTime <= toEpochMillis
            statusMatches && fromMatches && toMatches
        }

        val confirmed = all.filter { it.confirmedByUser && it.status == SideIncomePaymentStatus.CONFIRMED_RECEIVED }
        val pending = all.filter { !it.confirmedByUser || it.status != SideIncomePaymentStatus.CONFIRMED_RECEIVED }

        val cal = Calendar.getInstance()
        val curMonth = cal.get(Calendar.MONTH)
        val curYear = cal.get(Calendar.YEAR)

        val monthConfirmed = confirmed.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.receivedEpochMillis }
            c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear
        }.sumOf { it.receivedAmount }

        val sourceConfirmed = confirmed.groupBy { it.sourceName }
            .mapValues { (_, items) -> items.sumOf { it.receivedAmount } }

        val sourcePending = pending.groupBy { it.sourceName }
            .mapValues { (_, items) -> items.sumOf { it.expectedAmount } }

        val fmt = SimpleDateFormat("MMM yyyy", Locale.US)
        val monthlyMap = confirmed.groupBy { fmt.format(Date(it.receivedEpochMillis)) }
            .mapValues { (_, items) -> items.sumOf { it.receivedAmount } }

        return SideIncomeAnalyticsSummary(
            totalConfirmedReceived = confirmed.sumOf { it.receivedAmount },
            totalPendingAndExpected = pending.sumOf { it.expectedAmount },
            currentMonthConfirmed = monthConfirmed,
            sourceComparisonConfirmed = sourceConfirmed,
            sourceComparisonPending = sourcePending,
            monthlyConfirmedBreakdown = monthlyMap
        )
    }

    // ========================================================================
    // MODULE 43: INVOICE & RECEIPT GENERATOR
    // Rule: Receipt generation ONLY after user confirms payment.
    // Rule: Do not claim legal tax compliance unless verified.
    // ========================================================================

    fun getInvoices(context: Context, userId: String): List<ProInvoiceRecord> {
        val raw = prefs(context).getString("${userId}_m43_invoices", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                val itemsArr = o.optJSONArray("lineItems") ?: JSONArray()
                val items = (0 until itemsArr.length()).map { j ->
                    val li = itemsArr.getJSONObject(j)
                    ProInvoiceLineItem(
                        id = li.optString("id"),
                        description = li.optString("description"),
                        quantity = li.optInt("quantity", 1),
                        unitPrice = li.optLong("unitPrice", 0L)
                    )
                }
                val confirmed = o.optBoolean("paymentConfirmedByUser", false)
                val rawStatus = o.optString("status", ProInvoiceStatus.ISSUED_UNPAID.name)
                val status = if (!confirmed && rawStatus == ProInvoiceStatus.PAID_CONFIRMED.name) {
                    ProInvoiceStatus.ISSUED_UNPAID
                } else {
                    runCatching { ProInvoiceStatus.valueOf(rawStatus) }.getOrDefault(ProInvoiceStatus.ISSUED_UNPAID)
                }
                ProInvoiceRecord(
                    id = o.optString("id"),
                    invoiceNumber = o.optString("invoiceNumber"),
                    issuerName = o.optString("issuerName"),
                    customerName = o.optString("customerName"),
                    customerContact = o.optString("customerContact"),
                    issueEpochMillis = o.optLong("issueEpochMillis"),
                    dueEpochMillis = o.optLong("dueEpochMillis"),
                    lineItems = items,
                    discountAmount = o.optLong("discountAmount", 0L),
                    taxRatePercent = o.optDouble("taxRatePercent", 0.0),
                    taxRegistrationVerified = o.optBoolean("taxRegistrationVerified", false),
                    status = status,
                    paymentConfirmedByUser = confirmed,
                    paidEpochMillis = if (confirmed) o.optLong("paidEpochMillis", 0L) else 0L,
                    paymentMethodUsed = o.optString("paymentMethodUsed", ""),
                    receiptNumber = if (confirmed) o.optString("receiptNumber", "") else "",
                    notes = o.optString("notes", "")
                )
            }.sortedByDescending { it.issueEpochMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createInvoice(context: Context, userId: String, invoice: ProInvoiceRecord): ProInvoiceRecord {
        val safeInvoice = invoice.copy(
            status = if (invoice.status == ProInvoiceStatus.PAID_CONFIRMED) ProInvoiceStatus.ISSUED_UNPAID else invoice.status,
            paymentConfirmedByUser = false,
            paidEpochMillis = 0L,
            receiptNumber = ""
        )
        val list = getInvoices(context, userId).toMutableList()
        list.add(0, safeInvoice)
        saveInvoices(context, userId, list)
        return safeInvoice
    }

    fun confirmInvoicePaidAndGenerateReceipt(
        context: Context,
        userId: String,
        invoiceId: String,
        explicitUserConfirmation: Boolean,
        paymentMethod: String
    ): ProInvoiceRecord? {
        if (!explicitUserConfirmation) return null
        val list = getInvoices(context, userId).toMutableList()
        val idx = list.indexOfFirst { it.id == invoiceId }
        if (idx < 0) return null
        val current = list[idx]
        val receiptNo = "RCPT-${current.invoiceNumber.replace("INV-", "")}-${System.currentTimeMillis() % 10000}"
        val updated = current.copy(
            status = ProInvoiceStatus.PAID_CONFIRMED,
            paymentConfirmedByUser = true,
            paidEpochMillis = System.currentTimeMillis(),
            paymentMethodUsed = paymentMethod.ifBlank { "Bank / UPI" },
            receiptNumber = receiptNo
        )
        list[idx] = updated
        saveInvoices(context, userId, list)
        return updated
    }

    /**
     * Generates a formatted receipt text ONLY if the invoice has been explicitly confirmed as paid by the user.
     */
    fun generateReceiptDocumentText(invoice: ProInvoiceRecord): Result<String> {
        if (!invoice.paymentConfirmedByUser || invoice.status != ProInvoiceStatus.PAID_CONFIRMED) {
            return Result.failure(
                IllegalStateException("Receipt cannot be generated until payment is explicitly confirmed by the user.")
            )
        }
        val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
        val text = buildString {
            appendLine("==========================================")
            appendLine("         KHARCHEE PAYMENT RECEIPT         ")
            appendLine("==========================================")
            appendLine("Receipt No   : ${invoice.receiptNumber}")
            appendLine("Against Inv  : ${invoice.invoiceNumber}")
            appendLine("Paid Date    : ${fmt.format(Date(invoice.paidEpochMillis))}")
            appendLine("Payment Mode : ${invoice.paymentMethodUsed}")
            appendLine("Issued By    : ${invoice.issuerName}")
            appendLine("Received From: ${invoice.customerName} (${invoice.customerContact})")
            appendLine("------------------------------------------")
            invoice.lineItems.forEachIndexed { idx, item ->
                appendLine("${idx + 1}. ${item.description} x${item.quantity} @ Rs.${item.unitPrice} = Rs.${item.lineTotal}")
            }
            appendLine("------------------------------------------")
            appendLine("Subtotal     : Rs.${invoice.subtotal}")
            appendLine("Discount     : -Rs.${invoice.discountAmount}")
            appendLine("Tax (${invoice.taxRatePercent}%): +Rs.${invoice.taxAmount}")
            appendLine("TOTAL PAID   : Rs.${invoice.grandTotal}")
            appendLine("------------------------------------------")
            appendLine("Notice: ${invoice.complianceNotice}")
            appendLine("==========================================")
        }
        return Result.success(text)
    }

    fun formatInvoiceDocumentText(invoice: ProInvoiceRecord): String {
        val fmt = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return buildString {
            appendLine("==========================================")
            appendLine("        COMMERCIAL INVOICE STATEMENT      ")
            appendLine("==========================================")
            appendLine("Invoice No : ${invoice.invoiceNumber}")
            appendLine("Status     : ${invoice.status.name}")
            appendLine("Issue Date : ${fmt.format(Date(invoice.issueEpochMillis))}")
            appendLine("Due Date   : ${fmt.format(Date(invoice.dueEpochMillis))}")
            appendLine("From       : ${invoice.issuerName}")
            appendLine("Bill To    : ${invoice.customerName} (${invoice.customerContact})")
            appendLine("------------------------------------------")
            invoice.lineItems.forEachIndexed { idx, item ->
                appendLine("${idx + 1}. ${item.description} | Qty ${item.quantity} x Rs.${item.unitPrice} = Rs.${item.lineTotal}")
            }
            appendLine("------------------------------------------")
            appendLine("Subtotal   : Rs.${invoice.subtotal}")
            appendLine("Discount   : -Rs.${invoice.discountAmount}")
            appendLine("Tax (${invoice.taxRatePercent}%): +Rs.${invoice.taxAmount}")
            appendLine("GRAND TOTAL: Rs.${invoice.grandTotal}")
            appendLine("------------------------------------------")
            appendLine("Disclaimer : ${invoice.complianceNotice}")
            appendLine("==========================================")
        }
    }

    private fun saveInvoices(context: Context, userId: String, list: List<ProInvoiceRecord>) {
        val arr = JSONArray()
        list.forEach { inv ->
            val liArr = JSONArray()
            inv.lineItems.forEach { li ->
                liArr.put(JSONObject().apply {
                    put("id", li.id)
                    put("description", li.description)
                    put("quantity", li.quantity)
                    put("unitPrice", li.unitPrice)
                })
            }
            arr.put(JSONObject().apply {
                put("id", inv.id)
                put("invoiceNumber", inv.invoiceNumber)
                put("issuerName", inv.issuerName)
                put("customerName", inv.customerName)
                put("customerContact", inv.customerContact)
                put("issueEpochMillis", inv.issueEpochMillis)
                put("dueEpochMillis", inv.dueEpochMillis)
                put("lineItems", liArr)
                put("discountAmount", inv.discountAmount)
                put("taxRatePercent", inv.taxRatePercent)
                put("taxRegistrationVerified", inv.taxRegistrationVerified)
                put("status", inv.status.name)
                put("paymentConfirmedByUser", inv.paymentConfirmedByUser)
                put("paidEpochMillis", inv.paidEpochMillis)
                put("paymentMethodUsed", inv.paymentMethodUsed)
                put("receiptNumber", inv.receiptNumber)
                put("notes", inv.notes)
            })
        }
        prefs(context).edit().putString("${userId}_m43_invoices", arr.toString()).apply()
    }

    // ========================================================================
    // MODULE 44: PURCHASE RETURN & WARRANTY TRACKER
    // Rule: Do not guarantee warranty coverage; display details entered by the user.
    // ========================================================================

    fun getWarrantyProducts(context: Context, userId: String): List<WarrantyProductRecord> {
        val raw = prefs(context).getString("${userId}_m44_warranties", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                WarrantyProductRecord(
                    id = o.optString("id"),
                    productName = o.optString("productName"),
                    brandOrStore = o.optString("brandOrStore"),
                    serialOrOrderNo = o.optString("serialOrOrderNo"),
                    purchaseAmount = o.optLong("purchaseAmount", 0L),
                    purchaseEpochMillis = o.optLong("purchaseEpochMillis"),
                    returnDeadlineEpochMillis = o.optLong("returnDeadlineEpochMillis"),
                    warrantyStartEpochMillis = o.optLong("warrantyStartEpochMillis"),
                    warrantyExpiryEpochMillis = o.optLong("warrantyExpiryEpochMillis"),
                    invoiceAttachmentLabel = o.optString("invoiceAttachmentLabel", ""),
                    userStatusOverride = o.optString("userStatusOverride", ""),
                    coverageNotes = o.optString("coverageNotes", "")
                )
            }.sortedBy { it.warrantyExpiryEpochMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addWarrantyProduct(context: Context, userId: String, record: WarrantyProductRecord): List<WarrantyProductRecord> {
        val list = getWarrantyProducts(context, userId).toMutableList()
        list.add(record.copy(purchaseAmount = max(0L, record.purchaseAmount)))
        saveWarrantyProducts(context, userId, list)
        return list.sortedBy { it.warrantyExpiryEpochMillis }
    }

    fun markWarrantyProductReturnedOrClaimed(context: Context, userId: String, productId: String): List<WarrantyProductRecord> {
        val updated = getWarrantyProducts(context, userId).map {
            if (it.id == productId) it.copy(userStatusOverride = "RETURNED") else it
        }
        saveWarrantyProducts(context, userId, updated)
        return updated
    }

    private fun saveWarrantyProducts(context: Context, userId: String, list: List<WarrantyProductRecord>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("productName", p.productName)
                put("brandOrStore", p.brandOrStore)
                put("serialOrOrderNo", p.serialOrOrderNo)
                put("purchaseAmount", p.purchaseAmount)
                put("purchaseEpochMillis", p.purchaseEpochMillis)
                put("returnDeadlineEpochMillis", p.returnDeadlineEpochMillis)
                put("warrantyStartEpochMillis", p.warrantyStartEpochMillis)
                put("warrantyExpiryEpochMillis", p.warrantyExpiryEpochMillis)
                put("invoiceAttachmentLabel", p.invoiceAttachmentLabel)
                put("userStatusOverride", p.userStatusOverride)
                put("coverageNotes", p.coverageNotes)
            })
        }
        prefs(context).edit().putString("${userId}_m44_warranties", arr.toString()).apply()
    }

    // ========================================================================
    // MODULE 45: PRICE WATCHLIST
    // Rule: Never fabricate live prices or claim continuous price monitoring without a working integration.
    // ========================================================================

    fun getPriceWatchlist(context: Context, userId: String): List<PriceWatchlistItem> {
        val raw = prefs(context).getString("${userId}_m45_watchlist", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                val obsArr = o.optJSONArray("observations") ?: JSONArray()
                val obs = (0 until obsArr.length()).map { j ->
                    val bo = obsArr.getJSONObject(j)
                    PriceObservationPoint(
                        epochMillis = bo.optLong("epochMillis"),
                        observedPrice = bo.optLong("observedPrice"),
                        sourceNote = bo.optString("sourceNote", "Manual entry")
                    )
                }
                PriceWatchlistItem(
                    id = o.optString("id"),
                    productName = o.optString("productName"),
                    merchantOrUrlLabel = o.optString("merchantOrUrlLabel"),
                    targetPrice = o.optLong("targetPrice"),
                    currentObservedPrice = o.optLong("currentObservedPrice"),
                    observations = obs,
                    externalApiConfigured = false // External price feed API credentials not configured in .env
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addPriceWatchlistItem(
        context: Context,
        userId: String,
        productName: String,
        merchantLabel: String,
        initialObservedPrice: Long,
        targetPrice: Long
    ): List<PriceWatchlistItem> {
        val now = System.currentTimeMillis()
        val cleanObserved = max(1L, initialObservedPrice)
        val cleanTarget = max(1L, targetPrice)
        val item = PriceWatchlistItem(
            productName = productName.trim(),
            merchantOrUrlLabel = merchantLabel.trim(),
            targetPrice = cleanTarget,
            currentObservedPrice = cleanObserved,
            observations = listOf(
                PriceObservationPoint(
                    epochMillis = now,
                    observedPrice = cleanObserved,
                    sourceNote = "Initial manual price entry"
                )
            ),
            externalApiConfigured = false
        )
        val list = getPriceWatchlist(context, userId).toMutableList()
        list.add(0, item)
        savePriceWatchlist(context, userId, list)
        return list
    }

    fun recordManualPriceObservation(
        context: Context,
        userId: String,
        itemId: String,
        newObservedPrice: Long,
        note: String = "Manual observation update"
    ): List<PriceWatchlistItem> {
        val cleanPrice = max(1L, newObservedPrice)
        val updated = getPriceWatchlist(context, userId).map { item ->
            if (item.id == itemId) {
                val newObs = item.observations + PriceObservationPoint(
                    epochMillis = System.currentTimeMillis(),
                    observedPrice = cleanPrice,
                    sourceNote = note.ifBlank { "Manual entry" }
                )
                item.copy(
                    currentObservedPrice = cleanPrice,
                    observations = newObs
                )
            } else item
        }
        savePriceWatchlist(context, userId, updated)
        return updated
    }

    private fun savePriceWatchlist(context: Context, userId: String, list: List<PriceWatchlistItem>) {
        val arr = JSONArray()
        list.forEach { item ->
            val obsArr = JSONArray()
            item.observations.forEach { pt ->
                obsArr.put(JSONObject().apply {
                    put("epochMillis", pt.epochMillis)
                    put("observedPrice", pt.observedPrice)
                    put("sourceNote", pt.sourceNote)
                })
            }
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("productName", item.productName)
                put("merchantOrUrlLabel", item.merchantOrUrlLabel)
                put("targetPrice", item.targetPrice)
                put("currentObservedPrice", item.currentObservedPrice)
                put("observations", obsArr)
                put("externalApiConfigured", false)
            })
        }
        prefs(context).edit().putString("${userId}_m45_watchlist", arr.toString()).apply()
    }

    // ========================================================================
    // MODULE 46: TRAVEL BUDGET PLANNER
    // Rule: Keep trip plans separate from actual transactions until the user confirms an expense.
    // ========================================================================

    fun getTravelTrips(context: Context, userId: String): List<TravelTripPlan> {
        val raw = prefs(context).getString("${userId}_m46_trips", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                val itemsArr = o.optJSONArray("items") ?: JSONArray()
                val items = (0 until itemsArr.length()).map { j ->
                    val io = itemsArr.getJSONObject(j)
                    val confirmed = io.optBoolean("isConfirmedActualExpense", false)
                    TravelExpenseItem(
                        id = io.optString("id"),
                        title = io.optString("title"),
                        category = io.optString("category", TravelExpenseCategory.TRANSPORT.label),
                        plannedAmount = io.optLong("plannedAmount", 0L),
                        actualConfirmedAmount = if (confirmed) io.optLong("actualConfirmedAmount", 0L) else 0L,
                        isConfirmedActualExpense = confirmed,
                        confirmedEpochMillis = if (confirmed) io.optLong("confirmedEpochMillis", 0L) else 0L
                    )
                }
                TravelTripPlan(
                    id = o.optString("id"),
                    tripName = o.optString("tripName"),
                    destination = o.optString("destination"),
                    startDateLabel = o.optString("startDateLabel"),
                    endDateLabel = o.optString("endDateLabel"),
                    totalEstimatedBudget = o.optLong("totalEstimatedBudget", 0L),
                    items = items
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createTravelTrip(context: Context, userId: String, trip: TravelTripPlan): List<TravelTripPlan> {
        val list = getTravelTrips(context, userId).toMutableList()
        list.add(0, trip.copy(totalEstimatedBudget = max(0L, trip.totalEstimatedBudget)))
        saveTravelTrips(context, userId, list)
        return list
    }

    fun addPlannedTravelExpense(
        context: Context,
        userId: String,
        tripId: String,
        title: String,
        category: String,
        plannedAmount: Long
    ): List<TravelTripPlan> {
        val list = getTravelTrips(context, userId).map { trip ->
            if (trip.id == tripId) {
                val newItem = TravelExpenseItem(
                    title = title.trim(),
                    category = category,
                    plannedAmount = max(0L, plannedAmount),
                    actualConfirmedAmount = 0L,
                    isConfirmedActualExpense = false
                )
                trip.copy(items = trip.items + newItem)
            } else trip
        }
        saveTravelTrips(context, userId, list)
        return list
    }

    fun confirmActualTravelExpense(
        context: Context,
        userId: String,
        tripId: String,
        itemId: String,
        actualAmount: Long,
        explicitUserConfirmation: Boolean
    ): List<TravelTripPlan> {
        if (!explicitUserConfirmation) return getTravelTrips(context, userId)
        val list = getTravelTrips(context, userId).map { trip ->
            if (trip.id == tripId) {
                val updatedItems = trip.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(
                            actualConfirmedAmount = max(0L, actualAmount),
                            isConfirmedActualExpense = true,
                            confirmedEpochMillis = System.currentTimeMillis()
                        )
                    } else item
                }
                trip.copy(items = updatedItems)
            } else trip
        }
        saveTravelTrips(context, userId, list)
        return list
    }

    fun exportTripReportText(trip: TravelTripPlan): String {
        return buildString {
            appendLine("=== KHARCHEE TRAVEL BUDGET REPORT ===")
            appendLine("Trip        : ${trip.tripName} (${trip.destination})")
            appendLine("Dates       : ${trip.startDateLabel} to ${trip.endDateLabel}")
            appendLine("Total Budget: Rs.${trip.totalEstimatedBudget}")
            appendLine("Planned Cost: Rs.${trip.totalPlannedCost}")
            appendLine("Confirmed Spend: Rs.${trip.totalActualConfirmedCost}")
            appendLine("Remaining (vs Confirmed): Rs.${trip.remainingBudgetFromActual}")
            appendLine("-------------------------------------")
            trip.items.forEach { item ->
                val status = if (item.isConfirmedActualExpense) "CONFIRMED Rs.${item.actualConfirmedAmount}" else "PLANNED ONLY"
                appendLine("- [${item.category}] ${item.title}: Planned Rs.${item.plannedAmount} | $status")
            }
        }
    }

    private fun saveTravelTrips(context: Context, userId: String, list: List<TravelTripPlan>) {
        val arr = JSONArray()
        list.forEach { trip ->
            val itemsArr = JSONArray()
            trip.items.forEach { item ->
                itemsArr.put(JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("category", item.category)
                    put("plannedAmount", item.plannedAmount)
                    put("actualConfirmedAmount", if (item.isConfirmedActualExpense) item.actualConfirmedAmount else 0L)
                    put("isConfirmedActualExpense", item.isConfirmedActualExpense)
                    put("confirmedEpochMillis", item.confirmedEpochMillis)
                })
            }
            arr.put(JSONObject().apply {
                put("id", trip.id)
                put("tripName", trip.tripName)
                put("destination", trip.destination)
                put("startDateLabel", trip.startDateLabel)
                put("endDateLabel", trip.endDateLabel)
                put("totalEstimatedBudget", trip.totalEstimatedBudget)
                put("items", itemsArr)
            })
        }
        prefs(context).edit().putString("${userId}_m46_trips", arr.toString()).apply()
    }

    // ========================================================================
    // MODULE 47: EVENT BUDGET PLANNER
    // Rule: Do not treat expected contributions as received money.
    // ========================================================================

    fun getEventBudgets(context: Context, userId: String): List<ProEventBudgetPlan> {
        val raw = prefs(context).getString("${userId}_m47_events", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                val contribArr = o.optJSONArray("contributions") ?: JSONArray()
                val contribs = (0 until contribArr.length()).map { j ->
                    val co = contribArr.getJSONObject(j)
                    val confirmed = co.optBoolean("isConfirmedReceived", false)
                    EventContributionEntry(
                        id = co.optString("id"),
                        contributorName = co.optString("contributorName"),
                        expectedAmount = co.optLong("expectedAmount", 0L),
                        confirmedReceivedAmount = if (confirmed) co.optLong("confirmedReceivedAmount", 0L) else 0L,
                        isConfirmedReceived = confirmed
                    )
                }
                val expArr = o.optJSONArray("expenses") ?: JSONArray()
                val exps = (0 until expArr.length()).map { j ->
                    val eo = expArr.getJSONObject(j)
                    val confirmed = eo.optBoolean("isExpenseConfirmed", false)
                    EventExpenseEntry(
                        id = eo.optString("id"),
                        title = eo.optString("title"),
                        category = eo.optString("category", "Venue"),
                        plannedCost = eo.optLong("plannedCost", 0L),
                        actualSpent = if (confirmed) eo.optLong("actualSpent", 0L) else 0L,
                        isExpenseConfirmed = confirmed
                    )
                }
                ProEventBudgetPlan(
                    id = o.optString("id"),
                    eventName = o.optString("eventName"),
                    eventDateLabel = o.optString("eventDateLabel"),
                    baseAllocatedBudget = o.optLong("baseAllocatedBudget", 0L),
                    contributions = contribs,
                    expenses = exps
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createEventBudgetPlan(context: Context, userId: String, plan: ProEventBudgetPlan): List<ProEventBudgetPlan> {
        val list = getEventBudgets(context, userId).toMutableList()
        list.add(0, plan.copy(baseAllocatedBudget = max(0L, plan.baseAllocatedBudget)))
        saveEventBudgets(context, userId, list)
        return list
    }

    fun addExpectedEventContribution(
        context: Context,
        userId: String,
        eventId: String,
        contributorName: String,
        expectedAmount: Long
    ): List<ProEventBudgetPlan> {
        val updated = getEventBudgets(context, userId).map { ev ->
            if (ev.id == eventId) {
                val entry = EventContributionEntry(
                    contributorName = contributorName.trim(),
                    expectedAmount = max(0L, expectedAmount),
                    confirmedReceivedAmount = 0L,
                    isConfirmedReceived = false
                )
                ev.copy(contributions = ev.contributions + entry)
            } else ev
        }
        saveEventBudgets(context, userId, updated)
        return updated
    }

    fun confirmEventContributionReceived(
        context: Context,
        userId: String,
        eventId: String,
        contributionId: String,
        receivedAmount: Long,
        explicitConfirmation: Boolean
    ): List<ProEventBudgetPlan> {
        if (!explicitConfirmation) return getEventBudgets(context, userId)
        val updated = getEventBudgets(context, userId).map { ev ->
            if (ev.id == eventId) {
                val newContribs = ev.contributions.map { c ->
                    if (c.id == contributionId) {
                        c.copy(
                            confirmedReceivedAmount = max(0L, receivedAmount),
                            isConfirmedReceived = true
                        )
                    } else c
                }
                ev.copy(contributions = newContribs)
            } else ev
        }
        saveEventBudgets(context, userId, updated)
        return updated
    }

    fun addAndConfirmEventExpense(
        context: Context,
        userId: String,
        eventId: String,
        title: String,
        category: String,
        plannedCost: Long,
        actualSpent: Long,
        isConfirmed: Boolean
    ): List<ProEventBudgetPlan> {
        val updated = getEventBudgets(context, userId).map { ev ->
            if (ev.id == eventId) {
                val exp = EventExpenseEntry(
                    title = title.trim(),
                    category = category,
                    plannedCost = max(0L, plannedCost),
                    actualSpent = if (isConfirmed) max(0L, actualSpent) else 0L,
                    isExpenseConfirmed = isConfirmed
                )
                ev.copy(expenses = ev.expenses + exp)
            } else ev
        }
        saveEventBudgets(context, userId, updated)
        return updated
    }

    private fun saveEventBudgets(context: Context, userId: String, list: List<ProEventBudgetPlan>) {
        val arr = JSONArray()
        list.forEach { ev ->
            val cArr = JSONArray()
            ev.contributions.forEach { c ->
                cArr.put(JSONObject().apply {
                    put("id", c.id)
                    put("contributorName", c.contributorName)
                    put("expectedAmount", c.expectedAmount)
                    put("confirmedReceivedAmount", if (c.isConfirmedReceived) c.confirmedReceivedAmount else 0L)
                    put("isConfirmedReceived", c.isConfirmedReceived)
                })
            }
            val eArr = JSONArray()
            ev.expenses.forEach { e ->
                eArr.put(JSONObject().apply {
                    put("id", e.id)
                    put("title", e.title)
                    put("category", e.category)
                    put("plannedCost", e.plannedCost)
                    put("actualSpent", if (e.isExpenseConfirmed) e.actualSpent else 0L)
                    put("isExpenseConfirmed", e.isExpenseConfirmed)
                })
            }
            arr.put(JSONObject().apply {
                put("id", ev.id)
                put("eventName", ev.eventName)
                put("eventDateLabel", ev.eventDateLabel)
                put("baseAllocatedBudget", ev.baseAllocatedBudget)
                put("contributions", cArr)
                put("expenses", eArr)
            })
        }
        prefs(context).edit().putString("${userId}_m47_events", arr.toString()).apply()
    }

    // ========================================================================
    // MODULE 48: FINANCIAL LEARNING ACADEMY
    // Rule: Educational content must not be presented as personalised professional financial advice.
    // ========================================================================

    const val ACADEMY_EDUCATIONAL_DISCLAIMER =
        "EDUCATIONAL CONTENT ONLY: Modules in the Financial Learning Academy provide general financial literacy concepts and must not be construed as personalised investment, legal, or tax advice."

    fun getAcademyCurriculum(): List<AcademyLessonModule> = listOf(
        AcademyLessonModule(
            id = "lesson_1_budgeting",
            topicOrder = 1,
            title = "Budgeting Fundamentals (50/30/20 & Zero-Based)",
            categoryTag = "Budgeting",
            readingTimeMinutes = 4,
            keyTakeaways = listOf(
                "Track net take-home cashflow before assigning category caps.",
                "Separate fixed essentials (rent, utilities, groceries) from discretionary lifestyle spend.",
                "Review actual vs planned variance every week to avoid end-of-month surprises."
            ),
            contentSummary = "A sustainable budget gives every rupee a clear role before the month begins. Whether using the 50/30/20 guideline or zero-based envelopes, consistency matters more than perfection.",
            quizQuestions = listOf(
                AcademyQuizQuestion(
                    id = "q1_1",
                    question = "In a standard 50/30/20 budget framework, what does the 20% bucket typically represent?",
                    options = listOf("Entertainment & Dining", "Savings, Emergency Fund & Debt Prepayment", "Luxury Shopping"),
                    correctOptionIndex = 1,
                    explanation = "The 20% bucket prioritizes long-term financial resilience through savings and debt reduction."
                )
            )
        ),
        AcademyLessonModule(
            id = "lesson_2_saving",
            topicOrder = 2,
            title = "Saving Fundamentals & Emergency Buffers",
            categoryTag = "Saving Fundamentals",
            readingTimeMinutes = 5,
            keyTakeaways = listOf(
                "Build a 3 to 6 month essential-expense emergency buffer in liquid accounts.",
                "Automate savings transfers on payday ('Pay Yourself First').",
                "Separate sinking funds (upcoming annual fees, repairs) from true emergency reserves."
            ),
            contentSummary = "An emergency buffer protects you from taking high-interest debt when unexpected medical, career, or home repairs occur.",
            quizQuestions = listOf(
                AcademyQuizQuestion(
                    id = "q2_1",
                    question = "How many months of essential living expenses is commonly recommended for a baseline emergency fund?",
                    options = listOf("3 to 6 months", "1 week", "10 years"),
                    correctOptionIndex = 0,
                    explanation = "3 to 6 months of essential expenses provides a practical cushion against income disruptions."
                )
            )
        ),
        AcademyLessonModule(
            id = "lesson_3_banking",
            topicOrder = 3,
            title = "Banking Basics: Savings, Current & Deposit Accounts",
            categoryTag = "Banking Basics",
            readingTimeMinutes = 4,
            keyTakeaways = listOf(
                "Understand minimum average balance (MAB) rules to avoid bank penalty charges.",
                "Compare liquidity and lock-in rules between regular savings accounts and Fixed/Recurring Deposits.",
                "Always enable transaction alerts and nomination details on bank accounts."
            ),
            contentSummary = "Knowing how bank accounts accrue interest, impose service fees, and protect deposits helps you organize cash across operating and reserve accounts.",
            quizQuestions = listOf(
                AcademyQuizQuestion(
                    id = "q3_1",
                    question = "Why is registering a nominee on your bank account important?",
                    options = listOf("It increases interest rates", "It simplifies claim settlement for family members in unforeseen events", "It waives ATM fees"),
                    correctOptionIndex = 1,
                    explanation = "Nomination ensures smooth transfer of account proceeds to designated beneficiaries."
                )
            )
        ),
        AcademyLessonModule(
            id = "lesson_4_upi_safety",
            topicOrder = 4,
            title = "UPI & Digital-Payment Safety",
            categoryTag = "UPI & Digital Safety",
            readingTimeMinutes = 5,
            keyTakeaways = listOf(
                "A UPI PIN is ONLY required when sending money—never enter your UPI PIN to receive money.",
                "Verify the recipient name and VPA handle carefully before authorizing transfers.",
                "Never share OTPs, screen-sharing access, or QR codes sent by unknown callers claiming refunds."
            ),
            contentSummary = "Digital payments offer instant convenience, but social-engineering scams rely on urgency. Understanding collect-request mechanics prevents unauthorized debits.",
            quizQuestions = listOf(
                AcademyQuizQuestion(
                    id = "q4_1",
                    question = "Is a UPI PIN ever required to RECEIVE money into your bank account?",
                    options = listOf("Yes, for amounts above Rs.5,000", "Never — entering a UPI PIN always authorizes a debit from your account", "Only on weekends"),
                    correctOptionIndex = 1,
                    explanation = "Entering a UPI PIN always authorizes money leaving your account, never incoming credits."
                )
            )
        ),
        AcademyLessonModule(
            id = "lesson_5_loans_interest",
            topicOrder = 5,
            title = "Understanding Loans, APR & Compounding Interest",
            categoryTag = "Loans & Interest",
            readingTimeMinutes = 6,
            keyTakeaways = listOf(
                "Compare reducing-balance interest rates vs flat interest rates before signing loan agreements.",
                "Check processing fees, prepayment penalties, and total interest outgo over the loan tenure.",
                "Avoid rolling over credit card minimum dues, as revolving APR compounds rapidly."
            ),
            contentSummary = "Every EMI comprises principal and interest. Early in a loan's tenure, interest forms a larger portion of each installment.",
            quizQuestions = listOf(
                AcademyQuizQuestion(
                    id = "q5_1",
                    question = "What happens when you only pay the 'Minimum Amount Due' on a credit card bill?",
                    options = listOf("The remaining balance becomes interest-free", "Interest is charged on the unpaid balance and often on new purchases immediately", "Your credit limit doubles"),
                    correctOptionIndex = 1,
                    explanation = "Paying only the minimum avoids late fees but triggers high annualized interest on the carried balance."
                )
            )
        ),
        AcademyLessonModule(
            id = "lesson_6_credit_scores",
            topicOrder = 6,
            title = "Understanding Credit Scores & Credit Hygiene",
            categoryTag = "Credit Scores",
            readingTimeMinutes = 5,
            keyTakeaways = listOf(
                "On-time EMI and credit card bill payment history is the single largest factor in credit scores.",
                "Keep credit utilization ratio (CUR) below 30% of your total sanctioned credit limit.",
                "Avoid submitting multiple loan/card applications in a short window (hard inquiries)."
            ),
            contentSummary = "A strong credit profile lowers borrowing costs when you apply for education, vehicle, or home financing.",
            quizQuestions = listOf(
                AcademyQuizQuestion(
                    id = "q6_1",
                    question = "Which habit best supports a healthy credit score over time?",
                    options = listOf("Maxing out credit cards every month", "Paying bills in full on time and keeping credit utilization under 30%", "Closing your oldest credit account every 3 months"),
                    correctOptionIndex = 1,
                    explanation = "Timely full payments and low utilization demonstrate disciplined credit management."
                )
            )
        ),
        AcademyLessonModule(
            id = "lesson_7_tax_basics",
            topicOrder = 7,
            title = "Basic Tax Concepts: Gross vs Taxable Income & Record Keeping",
            categoryTag = "Basic Tax Concepts",
            readingTimeMinutes = 6,
            keyTakeaways = listOf(
                "Distinguish between Gross Income, eligible deductions/exemptions, and Net Taxable Income.",
                "Keep organized digital records of salary slips, bank interest certificates, and fee/donation receipts.",
                "Verify applicable tax regime rules and statutory deadlines from official tax authority portals."
            ),
            contentSummary = "Understanding how tax slabs, TDS (Tax Deducted at Source), and annual information statements work helps you maintain accurate financial documentation year-round.",
            quizQuestions = listOf(
                AcademyQuizQuestion(
                    id = "q7_1",
                    question = "What is the difference between Gross Income and Taxable Income?",
                    options = listOf("They are always identical", "Taxable Income is Gross Income minus eligible exemptions and statutory deductions", "Gross Income is calculated after paying tax"),
                    correctOptionIndex = 1,
                    explanation = "Taxable income is arrived at after applying eligible exemptions and deductions to gross total income."
                )
            )
        )
    )

    fun getAcademyProgress(context: Context, userId: String): AcademyUserProgress {
        val raw = prefs(context).getString("${userId}_m48_academy", null) ?: return AcademyUserProgress()
        return try {
            val o = JSONObject(raw)
            val completedArr = o.optJSONArray("completedLessonIds") ?: JSONArray()
            val completed = (0 until completedArr.length()).map { completedArr.getString(it) }.toSet()
            val scoresObj = o.optJSONObject("quizScores") ?: JSONObject()
            val scores = mutableMapOf<String, Int>()
            scoresObj.keys().forEach { k ->
                scores[k] = scoresObj.optInt(k, 0)
            }
            AcademyUserProgress(
                completedLessonIds = completed,
                quizScoresByLessonId = scores,
                lastStudiedEpochMillis = o.optLong("lastStudiedEpochMillis", 0L)
            )
        } catch (_: Exception) {
            AcademyUserProgress()
        }
    }

    fun submitAcademyQuizAndCompleteLesson(
        context: Context,
        userId: String,
        lessonId: String,
        selectedOptionIndex: Int
    ): AcademyUserProgress {
        val lesson = getAcademyCurriculum().firstOrNull { it.id == lessonId } ?: return getAcademyProgress(context, userId)
        val firstQuestion = lesson.quizQuestions.firstOrNull()
        val score = if (firstQuestion != null && firstQuestion.correctOptionIndex == selectedOptionIndex) 100 else 0
        val current = getAcademyProgress(context, userId)
        val newCompleted = if (score == 100) current.completedLessonIds + lessonId else current.completedLessonIds
        val newScores = current.quizScoresByLessonId.toMutableMap().apply { put(lessonId, score) }
        val updated = AcademyUserProgress(
            completedLessonIds = newCompleted,
            quizScoresByLessonId = newScores,
            lastStudiedEpochMillis = System.currentTimeMillis()
        )
        val scoresJson = JSONObject()
        newScores.forEach { (k, v) -> scoresJson.put(k, v) }
        val obj = JSONObject().apply {
            put("completedLessonIds", JSONArray(newCompleted.toList()))
            put("quizScores", scoresJson)
            put("lastStudiedEpochMillis", updated.lastStudiedEpochMillis)
        }
        prefs(context).edit().putString("${userId}_m48_academy", obj.toString()).apply()
        return updated
    }

    // ========================================================================
    // MODULE 49: INCOME & EXPENSE DOCUMENT MATCHER
    // Rule: Never automatically overwrite a transaction or assume a document proves a payment occurred.
    // ========================================================================

    fun parseAndSuggestDocumentMatch(
        context: Context,
        userId: String,
        documentTitle: String,
        rawDocumentText: String,
        existingTransactions: List<ExpenseRecord>
    ): ExtractedDocumentCandidate {
        // Extract amount (e.g., Rs. 1,250 or INR 1250 or Total: 1250)
        val amountRegex = Regex("""(?:Rs\.?|INR|₹|Total[:\s]*|Amount[:\s]*)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
        val amountMatch = amountRegex.find(rawDocumentText)
        val extractedAmount = amountMatch?.groupValues?.getOrNull(1)
            ?.replace(",", "")
            ?.substringBefore(".")
            ?.toLongOrNull() ?: 0L

        // Extract merchant (first line or after Merchant:/Store:)
        val merchantRegex = Regex("""(?:Merchant|Store|Vendor|Paid to|Biller)[:\s]+([A-Za-z0-9 &._-]+)""", RegexOption.IGNORE_CASE)
        val extractedMerchant = merchantRegex.find(rawDocumentText)?.groupValues?.getOrNull(1)?.trim()
            ?: documentTitle.trim().ifBlank { "Unknown Merchant" }

        // Extract date label if present
        val dateRegex = Regex("""(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\d{1,2}\s+[A-Za-z]{3,9}\s+\d{4})""")
        val extractedDateLabel = dateRegex.find(rawDocumentText)?.value ?: "Date in receipt"

        // Find best candidate among existing transactions without modifying any transaction
        var bestTx: ExpenseRecord? = null
        var bestScore = 0
        existingTransactions.forEach { tx ->
            var score = 0
            if (extractedAmount > 0L && tx.amount == extractedAmount) {
                score += 60
            } else if (extractedAmount > 0L && abs(tx.amount - extractedAmount) <= max(10L, extractedAmount / 20)) {
                score += 30
            }
            if (tx.title.contains(extractedMerchant, ignoreCase = true) ||
                extractedMerchant.contains(tx.title, ignoreCase = true)
            ) {
                score += 35
            }
            if (score > bestScore) {
                bestScore = score
                bestTx = tx
            }
        }

        // Check if another candidate already matched this same amount & merchant or transaction
        val existingCandidates = getDocumentMatchCandidates(context, userId)
        val duplicateFound = existingCandidates.any {
            (bestTx != null && it.suggestedTransactionId == bestTx?.id && it.status == DocumentMatchStatus.CONFIRMED_LINKED) ||
                (extractedAmount > 0L && it.extractedAmount == extractedAmount && it.extractedMerchant.equals(extractedMerchant, ignoreCase = true))
        }

        val candidate = ExtractedDocumentCandidate(
            documentTitle = documentTitle.ifBlank { "Imported Receipt" },
            rawSnippet = rawDocumentText.take(240),
            extractedMerchant = extractedMerchant,
            extractedAmount = extractedAmount,
            extractedDateLabel = extractedDateLabel,
            extractedEpochMillis = System.currentTimeMillis(),
            suggestedTransactionId = bestTx?.id,
            suggestedTransactionTitle = bestTx?.title,
            suggestedTransactionAmount = bestTx?.amount,
            confidencePercent = bestScore.coerceIn(0, 98),
            possibleDuplicateFlag = duplicateFound,
            duplicateReason = if (duplicateFound) "Possible duplicate receipt or already-linked transaction detected." else "",
            status = DocumentMatchStatus.PENDING_USER_REVIEW
        )

        val list = existingCandidates.toMutableList()
        list.add(0, candidate)
        saveDocumentMatchCandidates(context, userId, list)
        return candidate
    }

    fun getDocumentMatchCandidates(context: Context, userId: String): List<ExtractedDocumentCandidate> {
        val raw = prefs(context).getString("${userId}_m49_candidates", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                ExtractedDocumentCandidate(
                    id = o.optString("id"),
                    documentTitle = o.optString("documentTitle"),
                    rawSnippet = o.optString("rawSnippet"),
                    extractedMerchant = o.optString("extractedMerchant"),
                    extractedAmount = o.optLong("extractedAmount"),
                    extractedDateLabel = o.optString("extractedDateLabel"),
                    extractedEpochMillis = o.optLong("extractedEpochMillis"),
                    suggestedTransactionId = o.optString("suggestedTransactionId").takeIf { it.isNotBlank() },
                    suggestedTransactionTitle = o.optString("suggestedTransactionTitle").takeIf { it.isNotBlank() },
                    suggestedTransactionAmount = if (o.has("suggestedTransactionAmount")) o.optLong("suggestedTransactionAmount") else null,
                    confidencePercent = o.optInt("confidencePercent", 0),
                    possibleDuplicateFlag = o.optBoolean("possibleDuplicateFlag", false),
                    duplicateReason = o.optString("duplicateReason", ""),
                    status = runCatching { DocumentMatchStatus.valueOf(o.optString("status")) }
                        .getOrDefault(DocumentMatchStatus.PENDING_USER_REVIEW),
                    reviewedAtMillis = o.optLong("reviewedAtMillis", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Reviews and confirms or rejects a document match candidate.
     * Preserves original transaction records untouched and records an immutable audit trail entry.
     */
    fun reviewDocumentMatchCandidate(
        context: Context,
        userId: String,
        candidateId: String,
        confirmMatch: Boolean
    ): List<ExtractedDocumentCandidate> {
        val now = System.currentTimeMillis()
        var targetCandidate: ExtractedDocumentCandidate? = null
        val updated = getDocumentMatchCandidates(context, userId).map { c ->
            if (c.id == candidateId) {
                val next = c.copy(
                    status = if (confirmMatch) DocumentMatchStatus.CONFIRMED_LINKED else DocumentMatchStatus.REJECTED_BY_USER,
                    reviewedAtMillis = now
                )
                targetCandidate = next
                next
            } else c
        }
        saveDocumentMatchCandidates(context, userId, updated)
        targetCandidate?.let { tc ->
            appendDocumentMatchAudit(
                context = context,
                userId = userId,
                entry = DocumentMatchAuditEntry(
                    timestampMillis = now,
                    candidateId = tc.id,
                    documentTitle = tc.documentTitle,
                    transactionId = tc.suggestedTransactionId ?: "UNLINKED",
                    action = if (confirmMatch) "CONFIRMED_LINK" else "REJECTED_MATCH",
                    details = "User explicitly ${if (confirmMatch) "confirmed" else "rejected"} link for '${tc.documentTitle}' (Rs.${tc.extractedAmount}). Original transaction preserved."
                )
            )
        }
        return updated
    }

    fun getDocumentMatchAuditTrail(context: Context, userId: String): List<DocumentMatchAuditEntry> {
        val raw = prefs(context).getString("${userId}_m49_audit", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                DocumentMatchAuditEntry(
                    id = o.optString("id"),
                    timestampMillis = o.optLong("timestampMillis"),
                    candidateId = o.optString("candidateId"),
                    documentTitle = o.optString("documentTitle"),
                    transactionId = o.optString("transactionId"),
                    action = o.optString("action"),
                    details = o.optString("details")
                )
            }.sortedByDescending { it.timestampMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun appendDocumentMatchAudit(context: Context, userId: String, entry: DocumentMatchAuditEntry) {
        val list = getDocumentMatchAuditTrail(context, userId).toMutableList()
        list.add(0, entry)
        val arr = JSONArray()
        list.take(200).forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("timestampMillis", e.timestampMillis)
                put("candidateId", e.candidateId)
                put("documentTitle", e.documentTitle)
                put("transactionId", e.transactionId)
                put("action", e.action)
                put("details", e.details)
            })
        }
        prefs(context).edit().putString("${userId}_m49_audit", arr.toString()).apply()
    }

    private fun saveDocumentMatchCandidates(context: Context, userId: String, list: List<ExtractedDocumentCandidate>) {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(JSONObject().apply {
                put("id", c.id)
                put("documentTitle", c.documentTitle)
                put("rawSnippet", c.rawSnippet)
                put("extractedMerchant", c.extractedMerchant)
                put("extractedAmount", c.extractedAmount)
                put("extractedDateLabel", c.extractedDateLabel)
                put("extractedEpochMillis", c.extractedEpochMillis)
                put("suggestedTransactionId", c.suggestedTransactionId ?: "")
                put("suggestedTransactionTitle", c.suggestedTransactionTitle ?: "")
                if (c.suggestedTransactionAmount != null) put("suggestedTransactionAmount", c.suggestedTransactionAmount)
                put("confidencePercent", c.confidencePercent)
                put("possibleDuplicateFlag", c.possibleDuplicateFlag)
                put("duplicateReason", c.duplicateReason)
                put("status", c.status.name)
                put("reviewedAtMillis", c.reviewedAtMillis)
            })
        }
        prefs(context).edit().putString("${userId}_m49_candidates", arr.toString()).apply()
    }

    // ========================================================================
    // MODULE 50: PERSONAL FINANCE DATA INSIGHTS API
    // Rule: Read-only, user-scoped, authenticated, token-revocable, rate-limited, logged.
    // Never expose another user's data or allow arbitrary queries.
    // ========================================================================

    val ALLOWED_READ_ONLY_SCOPES = setOf("read:summary", "read:transactions", "read:goals")
    val SUPPORTED_ENDPOINTS = listOf("/v1/me/summary", "/v1/me/transactions", "/v1/me/goals")

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a user-scoped, read-only personal access token and returns Pair(rawPlaintextTokenOnce, ScopedApiTokenRecord).
     */
    fun generateScopedApiToken(
        context: Context,
        ownerUserId: String,
        label: String,
        requestedScopes: Set<String>
    ): Pair<String, ScopedApiTokenRecord> {
        val validScopes = requestedScopes.intersect(ALLOWED_READ_ONLY_SCOPES).ifEmpty { setOf("read:summary") }
        val randomPart = UUID.randomUUID().toString().replace("-", "")
        val rawToken = "khp_ro_${ownerUserId.take(6)}_$randomPart"
        val record = ScopedApiTokenRecord(
            ownerUserId = ownerUserId,
            label = label.ifBlank { "Personal Read-Only Token" },
            tokenPrefix = rawToken.take(16) + "...",
            tokenSha256Hash = sha256Hex(rawToken),
            scopes = validScopes
        )
        val tokens = getScopedApiTokens(context, ownerUserId).toMutableList()
        tokens.add(0, record)
        saveScopedApiTokens(context, ownerUserId, tokens)
        return rawToken to record
    }

    fun getScopedApiTokens(context: Context, ownerUserId: String): List<ScopedApiTokenRecord> {
        val raw = prefs(context).getString("${ownerUserId}_m50_tokens", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { idx ->
                val o = arr.getJSONObject(idx)
                val tokenOwner = o.optString("ownerUserId")
                if (tokenOwner != ownerUserId) return@mapNotNull null // Strict user isolation
                val scopesArr = o.optJSONArray("scopes") ?: JSONArray()
                val scopes = (0 until scopesArr.length()).map { scopesArr.getString(it) }.toSet()
                ScopedApiTokenRecord(
                    id = o.optString("id"),
                    ownerUserId = tokenOwner,
                    label = o.optString("label"),
                    tokenPrefix = o.optString("tokenPrefix"),
                    tokenSha256Hash = o.optString("tokenSha256Hash"),
                    scopes = scopes,
                    createdAtMillis = o.optLong("createdAtMillis"),
                    expiresAtMillis = o.optLong("expiresAtMillis"),
                    isRevoked = o.optBoolean("isRevoked", false),
                    revokedAtMillis = o.optLong("revokedAtMillis", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun revokeScopedApiToken(context: Context, ownerUserId: String, tokenId: String): List<ScopedApiTokenRecord> {
        val now = System.currentTimeMillis()
        val updated = getScopedApiTokens(context, ownerUserId).map { t ->
            if (t.id == tokenId && t.ownerUserId == ownerUserId) {
                t.copy(isRevoked = true, revokedAtMillis = now)
            } else t
        }
        saveScopedApiTokens(context, ownerUserId, updated)
        return updated
    }

    /**
     * Executes a read-only personal API request after validating:
     * 1. Authenticated user identity == token owner (strictly blocks cross-user access)
     * 2. Token validity & non-revocation
     * 3. Endpoint input validation (blocks arbitrary queries/SQL/mutations; only allows /v1/me/summary, /v1/me/transactions, /v1/me/goals)
     * 4. Scope authorization
     * 5. Sliding-window rate limiting
     */
    fun executeReadOnlyApiRequest(
        context: Context,
        authenticatedUserId: String,
        tokenOwnerUserId: String,
        tokenId: String,
        endpointPath: String,
        userTransactions: List<ExpenseRecord>,
        userGoals: List<SavingsGoalRecord>,
        monthlyBudget: Long
    ): PersonalApiGatewayResponse {
        val cleanEndpoint = endpointPath.trim()

        // 1. Strict cross-user isolation check
        if (authenticatedUserId.isBlank() || authenticatedUserId != tokenOwnerUserId) {
            logApiAccess(context, authenticatedUserId.ifBlank { "anonymous" }, tokenId, cleanEndpoint, 403, "Forbidden: Cross-user access denied")
            return PersonalApiGatewayResponse(
                statusCode = 403,
                endpoint = cleanEndpoint,
                isSuccess = false,
                rateLimitRemaining = 0,
                jsonPayload = """{"error":"FORBIDDEN_USER_MISMATCH"}""",
                errorExplanation = "Access denied: Token owner does not match the authenticated user session."
            )
        }

        // 2. Endpoint whitelist & input validation (never allow arbitrary query execution or write paths)
        if (cleanEndpoint !in SUPPORTED_ENDPOINTS) {
            logApiAccess(context, authenticatedUserId, tokenId, cleanEndpoint, 400, "Bad Request: Unsupported or non-read-only endpoint")
            return PersonalApiGatewayResponse(
                statusCode = 400,
                endpoint = cleanEndpoint,
                isSuccess = false,
                rateLimitRemaining = RATE_LIMIT_MAX_REQUESTS_PER_MINUTE,
                jsonPayload = """{"error":"INVALID_ENDPOINT","allowed":["/v1/me/summary","/v1/me/transactions","/v1/me/goals"]}""",
                errorExplanation = "Only whitelisted read-only endpoints are permitted. Arbitrary queries and mutations are blocked."
            )
        }

        // 3. Token lookup & revocation check
        val token = getScopedApiTokens(context, authenticatedUserId).firstOrNull { it.id == tokenId }
        if (token == null || token.isRevoked || System.currentTimeMillis() > token.expiresAtMillis) {
            logApiAccess(context, authenticatedUserId, tokenId, cleanEndpoint, 401, "Unauthorized: Token missing, expired, or revoked")
            return PersonalApiGatewayResponse(
                statusCode = 401,
                endpoint = cleanEndpoint,
                isSuccess = false,
                rateLimitRemaining = 0,
                jsonPayload = """{"error":"TOKEN_REVOKED_OR_INVALID"}""",
                errorExplanation = "The provided API token is revoked, expired, or invalid."
            )
        }

        // 4. Rate limit check (sliding 60s window)
        val now = System.currentTimeMillis()
        val windowStart = now - 60_000L
        val timestamps = tokenRequestTimestamps.getOrPut(token.id) { mutableListOf() }
        timestamps.removeAll { it < windowStart }
        if (timestamps.size >= RATE_LIMIT_MAX_REQUESTS_PER_MINUTE) {
            logApiAccess(context, authenticatedUserId, tokenId, cleanEndpoint, 429, "Too Many Requests: Rate limit exceeded")
            return PersonalApiGatewayResponse(
                statusCode = 429,
                endpoint = cleanEndpoint,
                isSuccess = false,
                rateLimitRemaining = 0,
                jsonPayload = """{"error":"RATE_LIMIT_EXCEEDED","retryAfterSeconds":60}""",
                errorExplanation = "Rate limit of $RATE_LIMIT_MAX_REQUESTS_PER_MINUTE requests/minute reached for this token."
            )
        }

        // 5. Required scope verification
        val requiredScope = when (cleanEndpoint) {
            "/v1/me/summary" -> "read:summary"
            "/v1/me/transactions" -> "read:transactions"
            "/v1/me/goals" -> "read:goals"
            else -> "read:summary"
        }
        if (requiredScope !in token.scopes) {
            logApiAccess(context, authenticatedUserId, tokenId, cleanEndpoint, 403, "Forbidden: Missing required scope $requiredScope")
            return PersonalApiGatewayResponse(
                statusCode = 403,
                endpoint = cleanEndpoint,
                isSuccess = false,
                rateLimitRemaining = RATE_LIMIT_MAX_REQUESTS_PER_MINUTE - timestamps.size,
                jsonPayload = """{"error":"INSUFFICIENT_SCOPE","requiredScope":"$requiredScope"}""",
                errorExplanation = "Token does not have the '$requiredScope' permission."
            )
        }

        // Record valid request in rate limiter
        timestamps.add(now)
        val remaining = max(0, RATE_LIMIT_MAX_REQUESTS_PER_MINUTE - timestamps.size)

        // 6. Build read-only user-scoped JSON response
        val payloadObj = JSONObject().apply {
            put("userId", authenticatedUserId)
            put("mode", "READ_ONLY")
            put("endpoint", cleanEndpoint)
            put("generatedAtEpochMillis", now)
            when (cleanEndpoint) {
                "/v1/me/summary" -> {
                    val totalIncome = userTransactions.filter { isIncomeTransaction(it) }.sumOf { it.amount }
                    val totalExpense = userTransactions.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
                    put("summary", JSONObject().apply {
                        put("monthlyBudget", monthlyBudget)
                        put("totalRecordedIncome", totalIncome)
                        put("totalRecordedExpense", totalExpense)
                        put("netCashflow", totalIncome - totalExpense)
                        put("transactionCount", userTransactions.size)
                        put("activeGoalsCount", userGoals.size)
                    })
                }
                "/v1/me/transactions" -> {
                    val txArr = JSONArray()
                    userTransactions.take(25).forEach { tx ->
                        txArr.put(JSONObject().apply {
                            put("id", tx.id)
                            put("title", tx.title)
                            put("category", tx.category)
                            put("amount", tx.amount)
                            put("type", if (isIncomeTransaction(tx)) "INCOME" else "EXPENSE")
                            put("txEpochMillis", tx.txEpochMillis)
                        })
                    }
                    put("transactions", txArr)
                }
                "/v1/me/goals" -> {
                    val gArr = JSONArray()
                    userGoals.forEach { g ->
                        gArr.put(JSONObject().apply {
                            put("id", g.id)
                            put("title", g.title)
                            put("targetAmount", g.targetAmount)
                            put("savedAmount", g.savedAmount)
                        })
                    }
                    put("goals", gArr)
                }
            }
        }

        logApiAccess(context, authenticatedUserId, tokenId, cleanEndpoint, 200, "OK (Read-Only)")
        return PersonalApiGatewayResponse(
            statusCode = 200,
            endpoint = cleanEndpoint,
            isSuccess = true,
            rateLimitRemaining = remaining,
            jsonPayload = payloadObj.toString(2)
        )
    }

    fun getApiAccessLogs(context: Context, ownerUserId: String): List<ApiAccessLogEntry> {
        val raw = prefs(context).getString("${ownerUserId}_m50_logs", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val o = arr.getJSONObject(idx)
                ApiAccessLogEntry(
                    id = o.optString("id"),
                    timestampMillis = o.optLong("timestampMillis"),
                    ownerUserId = o.optString("ownerUserId"),
                    tokenId = o.optString("tokenId"),
                    endpoint = o.optString("endpoint"),
                    statusCode = o.optInt("statusCode"),
                    statusMessage = o.optString("statusMessage")
                )
            }.sortedByDescending { it.timestampMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun logApiAccess(
        context: Context,
        ownerUserId: String,
        tokenId: String,
        endpoint: String,
        statusCode: Int,
        statusMessage: String
    ) {
        val logs = getApiAccessLogs(context, ownerUserId).toMutableList()
        logs.add(
            0,
            ApiAccessLogEntry(
                ownerUserId = ownerUserId,
                tokenId = tokenId,
                endpoint = endpoint,
                statusCode = statusCode,
                statusMessage = statusMessage
            )
        )
        val arr = JSONArray()
        logs.take(150).forEach { l ->
            arr.put(JSONObject().apply {
                put("id", l.id)
                put("timestampMillis", l.timestampMillis)
                put("ownerUserId", l.ownerUserId)
                put("tokenId", l.tokenId)
                put("endpoint", l.endpoint)
                put("statusCode", l.statusCode)
                put("statusMessage", l.statusMessage)
            })
        }
        prefs(context).edit().putString("${ownerUserId}_m50_logs", arr.toString()).apply()
    }

    private fun saveScopedApiTokens(context: Context, ownerUserId: String, list: List<ScopedApiTokenRecord>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("ownerUserId", t.ownerUserId)
                put("label", t.label)
                put("tokenPrefix", t.tokenPrefix)
                put("tokenSha256Hash", t.tokenSha256Hash)
                put("scopes", JSONArray(t.scopes.toList()))
                put("createdAtMillis", t.createdAtMillis)
                put("expiresAtMillis", t.expiresAtMillis)
                put("isRevoked", t.isRevoked)
                put("revokedAtMillis", t.revokedAtMillis)
            })
        }
        prefs(context).edit().putString("${ownerUserId}_m50_tokens", arr.toString()).apply()
    }
}
