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
import kotlin.math.roundToInt

// =============================================================================
// MODULE 31 — JARVIS VOICE STUDIO MODELS
// =============================================================================
data class JarvisVoiceTurnRecord(
    val id: String = UUID.randomUUID().toString(),
    val recognizedTranscript: String,
    val languageCode: String, // "en-IN", "hi-IN"
    val jarvisReplyText: String,
    val requiresActionConfirmation: Boolean,
    val timestampMillis: Long = System.currentTimeMillis()
)

// =============================================================================
// MODULE 32 — HOUSEHOLD FINANCE HUB MODELS
// =============================================================================
enum class HouseholdRole(val id: String, val canRead: Boolean, val canCreate: Boolean, val canEditDelete: Boolean) {
    OWNER("OWNER", true, true, true),
    ADMIN("ADMIN", true, true, true),
    CONTRIBUTOR("CONTRIBUTOR", true, true, false),
    VIEWER("VIEWER", true, false, false);

    companion object {
        fun fromId(id: String): HouseholdRole = entries.find { it.id.equals(id, ignoreCase = true) } ?: VIEWER
    }
}

data class HouseholdMemberRecord(
    val userId: String,
    val name: String,
    val role: HouseholdRole,
    val inviteStatus: String = "ACCEPTED" // PENDING_INVITE, ACCEPTED, DECLINED
)

data class HouseholdSharedExpenseRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Long,
    val createdByUserId: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class HouseholdHubState(
    val householdId: String,
    val householdName: String,
    val sharedBudget: Long,
    val members: List<HouseholdMemberRecord>,
    val sharedExpenses: List<HouseholdSharedExpenseRecord>
)

// =============================================================================
// MODULE 33 — REPORT DESIGNER PRO MODELS
// =============================================================================
data class ProReportTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val selectedCategories: Set<String> = emptySet(), // empty = all
    val selectedPaymentModes: Set<String> = emptySet(), // empty = all
    val includedSections: Set<String> = setOf("INCOME", "EXPENSE", "CATEGORIES", "CASHFLOW")
)

data class GeneratedDynamicReport(
    val templateName: String,
    val currency: String,
    val generatedAtMillis: Long,
    val matchingTransactionCount: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val netCashFlow: Long,
    val categoryTotals: Map<String, Long>
)

// =============================================================================
// MODULE 34 — INTELLIGENT BUDGET ALLOCATOR MODELS
// =============================================================================
data class BudgetAllocationPlanInput(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Monthly Plan",
    val monthlyIncome: Long,
    val essentialExpenses: Long,
    val emiObligations: Long,
    val bills: Long,
    val subscriptions: Long,
    val savingsGoals: Long,
    val discretionarySpending: Long,
    val emergencyReserveContribution: Long
)

data class BudgetAllocationEvaluation(
    val isValid: Boolean,
    val errorMessage: String?,
    val fixedObligationsTotal: Long,
    val flexibleAndSavingsTotal: Long,
    val totalAllocated: Long,
    val remainingUnallocatedBalance: Long,
    val isOverAllocated: Boolean,
    val shortfallAmount: Long
)

// =============================================================================
// MODULE 35 — MONEY HABIT LAB MODELS
// =============================================================================
data class MoneyHabitRecord(
    val id: String,
    val title: String,
    val frequency: String, // DAILY, WEEKLY, MONTHLY
    val paused: Boolean = false,
    val completedDateKeys: Set<String> = emptySet() // "yyyy-MM-dd"
) {
    val completionCount: Int get() = completedDateKeys.size
}

// =============================================================================
// MODULE 36 — FINANCIAL BENCHMARK STUDIO MODELS
// =============================================================================
data class BenchmarkComparisonResult(
    val comparisonType: String, // MoM, QoQ, YoY
    val currentPeriodLabel: String,
    val previousPeriodLabel: String,
    val hasPreviousPeriodData: Boolean,
    val currentIncome: Long,
    val previousIncome: Long,
    val currentExpense: Long,
    val previousExpense: Long,
    val expenseAbsoluteDelta: Long,
    val expensePercentChange: Double?, // null when previousExpense == 0L (safe zero denominator handling)
    val disclosureNote: String
)

// =============================================================================
// MODULE 37 — LIQUIDITY INTELLIGENCE MODELS
// =============================================================================
data class LiquidityIntelligenceBreakdown(
    val actualRecordedWalletBalance: Long,
    val reservedGoalAndEmergencyFunds: Long,
    val upcomingBillsTotal: Long,
    val upcomingEmisTotal: Long,
    val plannedPurchasesTotal: Long,
    val totalUpcomingObligations: Long,
    val estimatedFreeCashAfterObligations: Long,
    val expectedUnreceivedIncome: Long // Strictly separated from actual available balance
)

// =============================================================================
// MODULE 38 — SMART RECONCILIATION CENTER MODELS
// =============================================================================
data class ReconcileRowSuggestion(
    val rowNumber: Int,
    val rawDate: String,
    val description: String,
    val amount: Long,
    val matchStatus: String, // EXACT_MATCH, POSSIBLE_DUPLICATE, UNMATCHED_STATEMENT_ROW
    val matchedExpenseId: String?,
    val explanation: String,
    val isConfirmedByUser: Boolean = false
)

// =============================================================================
// MODULE 39 — PRO BENEFITS & MEMBERSHIP CENTER MODELS
// =============================================================================
data class ProMembershipStatusOverview(
    val entitlementState: String, // ACTIVE_PRO, STANDARD_FREE, EXPIRED
    val planName: String,
    val billingAmountInr: Int,
    val expiryDateLabel: String,
    val isBackendVerified: Boolean
)

// =============================================================================
// MODULE 40 — PERSONAL FINANCE MISSION CONTROL MODELS
// =============================================================================
data class MissionControlActionItem(
    val key: String,
    val category: String, // OBLIGATION, BUDGET_WARNING, GOAL, COACH, SECURITY
    val priority: Int, // 1 = Highest
    val title: String,
    val subtitle: String,
    val targetModuleId: String
)

object KharcheeProModules31To40Store {
    private const val PREFS_NAME = "kharchee_pro_modules_31_40_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // =========================================================================
    // MODULE 31 — JARVIS VOICE STUDIO
    // =========================================================================
    fun processVoiceStudioTranscript(
        context: Context,
        userId: String,
        recognizedTranscript: String,
        languageCode: String,
        expenses: List<ExpenseRecord>
    ): JarvisVoiceTurnRecord {
        val clean = recognizedTranscript.trim()
        val cmdRes = KharcheeProModules21To30Store.executeJarvisCommand(context, userId, clean, expenses)
        val requiresConfirm = cmdRes.category == JarvisCommandCategory.DATA_CHANGING
        val reply = if (requiresConfirm) {
            "Voice Safety Gate: '${cmdRes.responseText}' Sensitive or data-changing actions cannot execute from voice alone. Please tap Confirm on screen."
        } else {
            cmdRes.responseText
        }
        val turn = JarvisVoiceTurnRecord(
            recognizedTranscript = clean,
            languageCode = languageCode,
            jarvisReplyText = reply,
            requiresActionConfirmation = requiresConfirm
        )
        val history = getVoiceStudioHistory(context, userId).toMutableList()
        history.add(0, turn)
        val arr = JSONArray()
        history.take(15).forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id); put("recognizedTranscript", t.recognizedTranscript)
                put("languageCode", t.languageCode); put("jarvisReplyText", t.jarvisReplyText)
                put("requiresActionConfirmation", t.requiresActionConfirmation)
                put("timestampMillis", t.timestampMillis)
            })
        }
        prefs(context).edit().putString("${userId}_m31_voice_history", arr.toString()).apply()
        return turn
    }

    fun getVoiceStudioHistory(context: Context, userId: String): List<JarvisVoiceTurnRecord> {
        val raw = prefs(context).getString("${userId}_m31_voice_history", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                JarvisVoiceTurnRecord(
                    id = o.optString("id"),
                    recognizedTranscript = o.optString("recognizedTranscript"),
                    languageCode = o.optString("languageCode", "en-IN"),
                    jarvisReplyText = o.optString("jarvisReplyText"),
                    requiresActionConfirmation = o.optBoolean("requiresActionConfirmation", false),
                    timestampMillis = o.optLong("timestampMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // =========================================================================
    // MODULE 32 — HOUSEHOLD FINANCE HUB
    // =========================================================================
    fun getHouseholdHub(context: Context, ownerUserId: String, ownerName: String): HouseholdHubState {
        val p = prefs(context)
        val name = p.getString("${ownerUserId}_m32_name", "$ownerName's Household Hub") ?: "$ownerName's Household Hub"
        val budget = p.getLong("${ownerUserId}_m32_budget", 45000L)
        val memRaw = p.getString("${ownerUserId}_m32_members", null)
        val members = if (memRaw == null) {
            listOf(HouseholdMemberRecord(userId = ownerUserId, name = ownerName, role = HouseholdRole.OWNER, inviteStatus = "ACCEPTED"))
        } else {
            try {
                val arr = JSONArray(memRaw)
                (0 until arr.length()).mapNotNull { i ->
                    val o = arr.optJSONObject(i) ?: return@mapNotNull null
                    HouseholdMemberRecord(
                        userId = o.optString("userId"),
                        name = o.optString("name"),
                        role = HouseholdRole.fromId(o.optString("role")),
                        inviteStatus = o.optString("inviteStatus", "PENDING_INVITE")
                    )
                }
            } catch (_: Exception) {
                listOf(HouseholdMemberRecord(userId = ownerUserId, name = ownerName, role = HouseholdRole.OWNER, inviteStatus = "ACCEPTED"))
            }
        }
        val expRaw = p.getString("${ownerUserId}_m32_expenses", "[]") ?: "[]"
        val sharedExp = try {
            val arr = JSONArray(expRaw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                HouseholdSharedExpenseRecord(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    amount = o.optLong("amount"),
                    createdByUserId = o.optString("createdByUserId"),
                    createdAtMillis = o.optLong("createdAtMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
        return HouseholdHubState("hh_$ownerUserId", name, budget, members, sharedExp)
    }

    fun inviteHouseholdMember(
        context: Context,
        ownerUserId: String,
        ownerName: String,
        actingUserId: String,
        invitedUserId: String,
        invitedName: String,
        role: HouseholdRole
    ): Result<HouseholdHubState> {
        val state = getHouseholdHub(context, ownerUserId, ownerName)
        val actor = state.members.find { it.userId == actingUserId && it.inviteStatus == "ACCEPTED" }
            ?: return Result.failure(SecurityException("Unauthorized: Actor is not an active household member."))
        if (!actor.role.canEditDelete) {
            return Result.failure(SecurityException("Role '${actor.role.id}' is not permitted to invite members."))
        }
        val nextMembers = state.members.filterNot { it.userId == invitedUserId } +
            HouseholdMemberRecord(userId = invitedUserId, name = invitedName.trim(), role = role, inviteStatus = "PENDING_INVITE")
        val next = state.copy(members = nextMembers)
        saveHouseholdHub(context, ownerUserId, next)
        return Result.success(next)
    }

    fun acceptHouseholdInvitation(
        context: Context,
        ownerUserId: String,
        ownerName: String,
        invitedUserId: String
    ): Result<HouseholdHubState> {
        val state = getHouseholdHub(context, ownerUserId, ownerName)
        val target = state.members.find { it.userId == invitedUserId }
            ?: return Result.failure(SecurityException("No invitation found for user."))
        val nextMembers = state.members.map {
            if (it.userId == invitedUserId) target.copy(inviteStatus = "ACCEPTED") else it
        }
        val next = state.copy(members = nextMembers)
        saveHouseholdHub(context, ownerUserId, next)
        return Result.success(next)
    }

    fun addHouseholdSharedExpenseAuthorized(
        context: Context,
        ownerUserId: String,
        ownerName: String,
        actingUserId: String,
        title: String,
        amount: Long
    ): Result<HouseholdHubState> {
        if (amount <= 0L || title.isBlank()) return Result.failure(IllegalArgumentException("Valid title and positive amount required."))
        val state = getHouseholdHub(context, ownerUserId, ownerName)
        val actor = state.members.find { it.userId == actingUserId && it.inviteStatus == "ACCEPTED" }
            ?: return Result.failure(SecurityException("Unauthorized: User is not an accepted household member."))
        if (!actor.role.canCreate) {
            return Result.failure(SecurityException("Role '${actor.role.id}' has read-only access and cannot create expenses."))
        }
        val nextExp = listOf(HouseholdSharedExpenseRecord(title = title.trim(), amount = amount, createdByUserId = actingUserId)) + state.sharedExpenses
        val next = state.copy(sharedExpenses = nextExp)
        saveHouseholdHub(context, ownerUserId, next)
        return Result.success(next)
    }

    private fun saveHouseholdHub(context: Context, ownerUserId: String, state: HouseholdHubState) {
        val mArr = JSONArray()
        state.members.forEach { m ->
            mArr.put(JSONObject().apply {
                put("userId", m.userId); put("name", m.name); put("role", m.role.id); put("inviteStatus", m.inviteStatus)
            })
        }
        val eArr = JSONArray()
        state.sharedExpenses.forEach { e ->
            eArr.put(JSONObject().apply {
                put("id", e.id); put("title", e.title); put("amount", e.amount)
                put("createdByUserId", e.createdByUserId); put("createdAtMillis", e.createdAtMillis)
            })
        }
        prefs(context).edit()
            .putString("${ownerUserId}_m32_name", state.householdName)
            .putLong("${ownerUserId}_m32_budget", state.sharedBudget)
            .putString("${ownerUserId}_m32_members", mArr.toString())
            .putString("${ownerUserId}_m32_expenses", eArr.toString())
            .apply()
    }

    // =========================================================================
    // MODULE 33 — REPORT DESIGNER PRO
    // =========================================================================
    fun saveReportTemplate(context: Context, userId: String, template: ProReportTemplate): List<ProReportTemplate> {
        val current = getReportTemplates(context, userId).filterNot { it.id == template.id }.toMutableList()
        current.add(0, template)
        val arr = JSONArray()
        current.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id); put("name", t.name)
                put("startEpochMillis", t.startEpochMillis); put("endEpochMillis", t.endEpochMillis)
                put("selectedCategories", JSONArray(t.selectedCategories.toList()))
                put("selectedPaymentModes", JSONArray(t.selectedPaymentModes.toList()))
                put("includedSections", JSONArray(t.includedSections.toList()))
            })
        }
        prefs(context).edit().putString("${userId}_m33_templates", arr.toString()).apply()
        return current
    }

    fun getReportTemplates(context: Context, userId: String): List<ProReportTemplate> {
        val raw = prefs(context).getString("${userId}_m33_templates", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val cats = o.optJSONArray("selectedCategories")?.let { ca -> (0 until ca.length()).map { ca.optString(it) }.toSet() } ?: emptySet()
                val modes = o.optJSONArray("selectedPaymentModes")?.let { ma -> (0 until ma.length()).map { ma.optString(it) }.toSet() } ?: emptySet()
                val secs = o.optJSONArray("includedSections")?.let { sa -> (0 until sa.length()).map { sa.optString(it) }.toSet() } ?: setOf("INCOME", "EXPENSE")
                ProReportTemplate(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    startEpochMillis = o.optLong("startEpochMillis", 0L),
                    endEpochMillis = o.optLong("endEpochMillis", Long.MAX_VALUE),
                    selectedCategories = cats,
                    selectedPaymentModes = modes,
                    includedSections = secs
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun generateDynamicReportFromTemplate(
        template: ProReportTemplate,
        expenses: List<ExpenseRecord>,
        currency: String = "INR"
    ): GeneratedDynamicReport {
        val filtered = expenses.filter { tx ->
            tx.txEpochMillis in template.startEpochMillis..template.endEpochMillis &&
                (template.selectedCategories.isEmpty() || tx.category in template.selectedCategories) &&
                (template.selectedPaymentModes.isEmpty() || tx.paymentMode in template.selectedPaymentModes)
        }
        val inc = filtered.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val exp = filtered.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val catMap = filtered.filter { !isIncomeTransaction(it) }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }

        return GeneratedDynamicReport(
            templateName = template.name,
            currency = currency,
            generatedAtMillis = System.currentTimeMillis(),
            matchingTransactionCount = filtered.size,
            totalIncome = inc,
            totalExpense = exp,
            netCashFlow = inc - exp,
            categoryTotals = catMap
        )
    }

    // =========================================================================
    // MODULE 34 — INTELLIGENT BUDGET ALLOCATOR
    // =========================================================================
    fun evaluateBudgetAllocation(input: BudgetAllocationPlanInput): BudgetAllocationEvaluation {
        val fields = listOf(
            input.monthlyIncome, input.essentialExpenses, input.emiObligations,
            input.bills, input.subscriptions, input.savingsGoals,
            input.discretionarySpending, input.emergencyReserveContribution
        )
        if (fields.any { it < 0L }) {
            return BudgetAllocationEvaluation(
                isValid = false,
                errorMessage = "Budget allocation inputs cannot be negative.",
                fixedObligationsTotal = 0L,
                flexibleAndSavingsTotal = 0L,
                totalAllocated = 0L,
                remainingUnallocatedBalance = 0L,
                isOverAllocated = false,
                shortfallAmount = 0L
            )
        }
        val fixed = input.essentialExpenses + input.emiObligations + input.bills + input.subscriptions
        val flexible = input.savingsGoals + input.discretionarySpending + input.emergencyReserveContribution
        val total = fixed + flexible
        val remaining = input.monthlyIncome - total
        val isOver = total > input.monthlyIncome
        val shortfall = if (isOver) total - input.monthlyIncome else 0L

        return BudgetAllocationEvaluation(
            isValid = true,
            errorMessage = null,
            fixedObligationsTotal = fixed,
            flexibleAndSavingsTotal = flexible,
            totalAllocated = total,
            remainingUnallocatedBalance = remaining,
            isOverAllocated = isOver,
            shortfallAmount = shortfall
        )
    }

    // =========================================================================
    // MODULE 35 — MONEY HABIT LAB
    // =========================================================================
    fun getMoneyHabits(context: Context, userId: String): List<MoneyHabitRecord> {
        val raw = prefs(context).getString("${userId}_m35_habits", null)
        if (raw == null) {
            return listOf(
                MoneyHabitRecord(id = "h_daily_log", title = "Daily Expense Logging Check-In", frequency = "DAILY"),
                MoneyHabitRecord(id = "h_weekly_review", title = "Weekly Category & Leak Review", frequency = "WEEKLY"),
                MoneyHabitRecord(id = "h_monthly_budget", title = "Monthly Budget & Goal Check-In", frequency = "MONTHLY")
            )
        }
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val dArr = o.optJSONArray("completedDateKeys") ?: JSONArray()
                val dates = (0 until dArr.length()).map { dArr.optString(it) }.toSet()
                MoneyHabitRecord(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    frequency = o.optString("frequency", "DAILY"),
                    paused = o.optBoolean("paused", false),
                    completedDateKeys = dates
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun checkInMoneyHabit(
        context: Context,
        userId: String,
        habitId: String,
        dateKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    ): List<MoneyHabitRecord> {
        val current = getMoneyHabits(context, userId).map { h ->
            if (h.id == habitId && !h.paused && dateKey !in h.completedDateKeys) {
                h.copy(completedDateKeys = h.completedDateKeys + dateKey)
            } else h
        }
        saveMoneyHabits(context, userId, current)
        return current
    }

    private fun saveMoneyHabits(context: Context, userId: String, list: List<MoneyHabitRecord>) {
        val arr = JSONArray()
        list.forEach { h ->
            arr.put(JSONObject().apply {
                put("id", h.id); put("title", h.title); put("frequency", h.frequency)
                put("paused", h.paused); put("completedDateKeys", JSONArray(h.completedDateKeys.toList()))
            })
        }
        prefs(context).edit().putString("${userId}_m35_habits", arr.toString()).apply()
    }

    // =========================================================================
    // MODULE 36 — FINANCIAL BENCHMARK STUDIO
    // =========================================================================
    fun computeHistoricalBenchmark(
        comparisonType: String, // "MoM", "QoQ", "YoY"
        expenses: List<ExpenseRecord>,
        nowMillis: Long = System.currentTimeMillis()
    ): BenchmarkComparisonResult {
        val windowDays = when (comparisonType.uppercase(Locale.US)) {
            "QOQ" -> 90L
            "YOY" -> 365L
            else -> 30L
        }
        val windowMs = windowDays * 86_400_000L
        val currentWindowStart = nowMillis - windowMs
        val prevWindowStart = currentWindowStart - windowMs

        val curTx = expenses.filter { it.txEpochMillis in currentWindowStart..nowMillis }
        val prevTx = expenses.filter { it.txEpochMillis in prevWindowStart until currentWindowStart }

        val curInc = curTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val prevInc = prevTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val curExp = curTx.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val prevExp = prevTx.filter { !isIncomeTransaction(it) }.sumOf { it.amount }

        val pctChange: Double? = if (prevExp > 0L) {
            (((curExp - prevExp).toDouble() / prevExp.toDouble()) * 1000.0).roundToInt() / 10.0
        } else null

        return BenchmarkComparisonResult(
            comparisonType = comparisonType,
            currentPeriodLabel = "Current ${windowDays}d Window",
            previousPeriodLabel = "Previous ${windowDays}d Window",
            hasPreviousPeriodData = prevTx.isNotEmpty(),
            currentIncome = curInc,
            previousIncome = prevInc,
            currentExpense = curExp,
            previousExpense = prevExp,
            expenseAbsoluteDelta = curExp - prevExp,
            expensePercentChange = pctChange,
            disclosureNote = if (prevTx.isEmpty()) {
                "Previous equivalent period has 0 recorded transactions (labelled as incomplete baseline). Benchmarks use only your own historical records—never invented peer averages."
            } else {
                "Compared strictly against your own ${windowDays}-day historical baseline."
            }
        )
    }

    // =========================================================================
    // MODULE 37 — LIQUIDITY INTELLIGENCE
    // =========================================================================
    fun computeLiquidityIntelligence(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        savingsGoals: List<SavingsGoalRecord>,
        plannedPurchasesAmount: Long = 0L
    ): LiquidityIntelligenceBreakdown {
        val walletOverview = KharcheeProModules11To20Store.computeConsolidatedWalletOverview(context, userId, expenses)
        val actualBalance = walletOverview.consolidatedBalance

        // Deduplicate emergency reserve and savings goals so they are never counted twice
        val manualEmergency = KharcheeProfileAndToolsStore.getEmergencyFundSaved(context, userId)
        val goalsTotal = savingsGoals.sumOf { it.savedAmount }
        val reservedFunds = max(0L, manualEmergency) + max(0L, goalsTotal)

        val periodKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val unpaidBills = KharcheeProModules11To20Store.getSmartBills(context, userId)
            .filter { it.status == "ACTIVE" && !it.paidPeriods.containsKey(periodKey) }
            .sumOf { it.expectedAmount }
        val upcomingEmis = KharcheeProModules11To20Store.getProLoans(context, userId)
            .sumOf { it.monthlyEmi }

        val totalObligations = unpaidBills + upcomingEmis + max(0L, plannedPurchasesAmount)
        val freeCash = actualBalance - reservedFunds - totalObligations
        val (_, expectedSalary) = KharcheeEcosystemFeaturesStore.getExpectedSalaryConfig(context, userId)

        return LiquidityIntelligenceBreakdown(
            actualRecordedWalletBalance = actualBalance,
            reservedGoalAndEmergencyFunds = reservedFunds,
            upcomingBillsTotal = unpaidBills,
            upcomingEmisTotal = upcomingEmis,
            plannedPurchasesTotal = max(0L, plannedPurchasesAmount),
            totalUpcomingObligations = totalObligations,
            estimatedFreeCashAfterObligations = freeCash,
            expectedUnreceivedIncome = expectedSalary
        )
    }

    // =========================================================================
    // MODULE 38 — SMART RECONCILIATION CENTER
    // =========================================================================
    fun parseAndReconcileCsvStatement(
        csvContent: String,
        existingExpenses: List<ExpenseRecord>
    ): Result<List<ReconcileRowSuggestion>> {
        if (csvContent.isBlank() || csvContent.length > 2_000_000) {
            return Result.failure(IllegalArgumentException("CSV statement must be non-empty and under 2 MB."))
        }
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotBlank() }
        val suggestions = mutableListOf<ReconcileRowSuggestion>()

        lines.forEachIndexed { idx, line ->
            if (idx == 0 && line.lowercase(Locale.US).contains("amount")) return@forEachIndexed
            val parts = line.split(",").map { it.trim() }
            if (parts.size < 3) {
                return Result.failure(IllegalArgumentException("Row ${idx + 1} is malformed. Expected: Date,Description,Amount"))
            }
            val dateStr = parts[0]
            val desc = parts[1]
            val amt = parts[2].toLongOrNull()
                ?: return Result.failure(IllegalArgumentException("Row ${idx + 1} has invalid numeric amount: '${parts[2]}'"))

            val exactMatch = existingExpenses.find {
                it.amount == amt && it.title.equals(desc, ignoreCase = true)
            }
            val amountOnlyMatch = existingExpenses.find { it.amount == amt }

            val (status, matchedId, explanation) = when {
                exactMatch != null -> Triple("EXACT_MATCH", exactMatch.id, "Matches recorded transaction '${exactMatch.title}' (₹$amt)")
                amountOnlyMatch != null -> Triple("POSSIBLE_DUPLICATE", amountOnlyMatch.id, "Same amount (₹$amt) as '${amountOnlyMatch.title}' — review merchant/date")
                else -> Triple("UNMATCHED_STATEMENT_ROW", null, "Not yet recorded in KHARCHEE Passbook (awaiting manual confirmation)")
            }

            suggestions.add(
                ReconcileRowSuggestion(
                    rowNumber = idx + 1,
                    rawDate = dateStr,
                    description = desc,
                    amount = amt,
                    matchStatus = status,
                    matchedExpenseId = matchedId,
                    explanation = explanation
                )
            )
        }
        return Result.success(suggestions)
    }

    // =========================================================================
    // MODULE 39 — PRO BENEFITS & MEMBERSHIP CENTER
    // =========================================================================
    fun evaluateProMembershipStatus(
        settings: UserSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): ProMembershipStatusOverview {
        val isExpired = settings.isPro && settings.proExpiryMillis in 1L until nowMillis
        val state = when {
            isExpired -> "EXPIRED"
            settings.isPro -> "ACTIVE_PRO"
            else -> "STANDARD_FREE"
        }
        val planName = when {
            state != "ACTIVE_PRO" -> "Standard Free Tier"
            settings.proTier == "platinum" -> "PRO Obsidian Elite (Quarterly)"
            else -> "PRO Silver Edition (Monthly)"
        }
        val price = if (settings.proTier == "platinum") 99 else 30
        val expLabel = if (settings.proExpiryMillis > 0L) {
            SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(settings.proExpiryMillis))
        } else "Not Active"

        return ProMembershipStatusOverview(
            entitlementState = state,
            planName = planName,
            billingAmountInr = if (state == "ACTIVE_PRO") price else 0,
            expiryDateLabel = expLabel,
            isBackendVerified = settings.isPro && !isExpired
        )
    }

    // =========================================================================
    // MODULE 40 — PERSONAL FINANCE MISSION CONTROL
    // =========================================================================
    fun buildMissionControlFeed(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        savingsGoals: List<SavingsGoalRecord>,
        dailyLimit: Long
    ): List<MissionControlActionItem> {
        val items = mutableListOf<MissionControlActionItem>()
        val periodKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

        KharcheeProModules11To20Store.getSmartBills(context, userId)
            .filter { it.status == "ACTIVE" && !it.paidPeriods.containsKey(periodKey) }
            .forEach { bill ->
                items.add(
                    MissionControlActionItem(
                        key = "mc_bill_${bill.id}",
                        category = "OBLIGATION",
                        priority = 1,
                        title = "Upcoming Bill: ${bill.title} (₹${bill.expectedAmount})",
                        subtitle = "Due on day ${bill.dueDayOfMonth} · Tap to open Smart Bill Manager",
                        targetModuleId = "m13_bills"
                    )
                )
            }

        KharcheeProModules21To30Store.buildPersonalisedCoachTasks(context, userId, expenses, savingsGoals, dailyLimit)
            .filter { it.status == "PENDING" }
            .forEach { task ->
                items.add(
                    MissionControlActionItem(
                        key = "mc_coach_${task.id}",
                        category = "COACH",
                        priority = 2,
                        title = task.title,
                        subtitle = task.whatUserCanDo,
                        targetModuleId = "m28_coach"
                    )
                )
            }

        savingsGoals.take(2).forEach { g ->
            items.add(
                MissionControlActionItem(
                    key = "mc_goal_${g.id}",
                    category = "GOAL",
                    priority = 3,
                    title = "Goal Progress: ${g.title}",
                    subtitle = "Saved ₹${g.savedAmount} / ₹${g.targetAmount}",
                    targetModuleId = "m14_goals"
                )
            )
        }

        return items.distinctBy { it.key }.sortedBy { it.priority }
    }
}
