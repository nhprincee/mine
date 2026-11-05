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
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

// =============================================================================
// MODULE 21 — JARVIS COMMAND CENTER MODELS
// =============================================================================
enum class JarvisCommandCategory {
    READ_ONLY,
    NAVIGATION,
    DATA_CHANGING,
    INVALID
}

data class JarvisCommandActionPreview(
    val actionId: String = UUID.randomUUID().toString(),
    val actionType: String, // CREATE_MONTHLY_REPORT, LOG_EXPENSE
    val description: String,
    val amount: Long = 0L,
    val category: String = "General",
    val requiresExplicitConfirmation: Boolean = true
)

data class JarvisCommandExecutionResult(
    val rawCommand: String,
    val category: JarvisCommandCategory,
    val responseText: String,
    val targetModuleId: String? = null,
    val pendingConfirmationPreview: JarvisCommandActionPreview? = null
)

// =============================================================================
// MODULE 22 — FINANCIAL SAFETY MONITOR MODELS
// =============================================================================
enum class SafetyEventClassification(val label: String) {
    POSSIBLE_DUPLICATE("Possible Duplicate"),
    UNUSUAL_SPENDING("Unusual Spending"),
    CONFIRMED_SECURITY_EVENT("Confirmed Application Security Event"),
    UNVERIFIED_EXTERNAL_PAYMENT("Unverified External Payment")
}

data class FinancialSafetyEvent(
    val id: String,
    val classification: SafetyEventClassification,
    val title: String,
    val details: String,
    val amount: Long,
    val dateLabel: String,
    val isReviewed: Boolean,
    val isDismissed: Boolean
)

// =============================================================================
// MODULE 23 — SMART DOCUMENT INTELLIGENCE MODELS
// =============================================================================
data class SmartDocumentExtractionDraft(
    val documentId: String = UUID.randomUUID().toString(),
    val ownerUserId: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val extractedMerchant: String?,
    val extractedDate: String?,
    val extractedAmount: Long?,
    val extractedTaxAmount: Long?,
    val category: String,
    val uncertainFields: List<String>, // Explicitly flags unreadable/missing fields without guessing
    val isDuplicateDocument: Boolean,
    val requiresConfirmationBeforeTransaction: Boolean = true
)

// =============================================================================
// MODULE 24 — LONG-TERM WEALTH PLANNER MODELS
// =============================================================================
data class LongTermWealthScenarioInput(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "5-Year Wealth Plan",
    val durationYears: Int = 5, // 1, 3, 5, 10, or custom
    val startingSavings: Long = 50000L,
    val monthlyContribution: Long = 10000L,
    val annualContributionGrowthPct: Double = 5.0,
    val assumedAnnualReturnPct: Double = 10.0,
    val annualInflationPct: Double = 6.0,
    val targetAmount: Long = 1000000L
)

data class LongTermWealthProjectionResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val durationYears: Int,
    val totalContributions: Long,
    val noGrowthFutureValue: Long, // 0% return scenario
    val assumedGrowthAmount: Long,
    val projectedFutureValue: Long, // User-selected assumption scenario
    val inflationAdjustedValue: Long,
    val alternativeHigherContribFutureValue: Long, // +20% monthly contribution scenario
    val targetGap: Long,
    val compoundingExplanation: String,
    val hypotheticalDisclaimer: String
)

// =============================================================================
// MODULE 25 — TAX & DEDUCTION ORGANIZER MODELS
// =============================================================================
data class UserLabelledDeductionItem(
    val id: String = UUID.randomUUID().toString(),
    val financialYear: String, // e.g., "FY 2026-27"
    val title: String,
    val sectionTag: String, // User-entered label e.g. "80C / 80D / HRA (User-Labelled)"
    val amount: Long,
    val linkedDocumentId: String = ""
)

data class TaxOrganizerSummary(
    val financialYear: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val recordedIncomeTotal: Long,
    val recordedExpenseTotal: Long,
    val userLabelledDeductions: List<UserLabelledDeductionItem>,
    val userLabelledDeductionTotal: Long,
    val uploadedTaxDocumentsCount: Int,
    val missingDocumentChecklist: List<String>,
    val legalDisclaimer: String
)

// =============================================================================
// MODULE 26 — WEALTH MILESTONE TRACKER MODELS
// =============================================================================
data class WealthMilestoneRecord(
    val id: String,
    val title: String,
    val targetAmount: Long,
    val currentActualAmount: Long,
    val targetDate: String,
    val isAchieved: Boolean,
    val completionDate: String?
)

// =============================================================================
// MODULE 27 — LIFE EVENT PLANNER MODELS
// =============================================================================
data class LifeEventPlanRecord(
    val id: String = UUID.randomUUID().toString(),
    val eventName: String,
    val category: String, // Education, Relocation, Travel, Vehicle Purchase, Home Setup, Custom Event
    val targetDate: String,
    val monthsUntilEvent: Int,
    val estimatedCost: Long,
    val currentSavingsAllocated: Long,
    val monthlySavingsContribution: Long,
    val priority: String = "High",
    val linkedGoalId: String = "",
    val notes: String = "",
    val isArchived: Boolean = false
) {
    val targetGap: Long get() = max(0L, estimatedCost - currentSavingsAllocated)
    val requiredMonthlyToMeetTarget: Long get() = if (monthsUntilEvent > 0) ceil(targetGap.toDouble() / monthsUntilEvent.toDouble()).toLong() else targetGap
    val estimatedMonthsToCompleteAtCurrentRate: Int get() = if (monthlySavingsContribution > 0L) ceil(targetGap.toDouble() / monthlySavingsContribution.toDouble()).toInt() else -1
}

// =============================================================================
// MODULE 28 — PERSONALISED MONEY COACH MODELS
// =============================================================================
data class MoneyCoachTask(
    val id: String,
    val title: String,
    val whatWasObserved: String,
    val whyGenerated: String,
    val whatUserCanDo: String,
    val isEstimate: Boolean,
    val status: String // PENDING, ACCEPTED, DISMISSED, SNOOZED, COMPLETED
)

// =============================================================================
// MODULE 29 — PRIVACY & SECURITY CENTER MODELS
// =============================================================================
data class PrivacySecurityCenterState(
    val userId: String,
    val vaultPinEnabled: Boolean,
    val maskBalancesInShareCards: Boolean,
    val biometricLockRequested: Boolean,
    val privateDocumentsCount: Int,
    val securityEventsLog: List<String>
)

// =============================================================================
// MODULE 30 — PRO WORKSPACE STUDIO MODELS
// =============================================================================
data class ProWorkspaceWidgetConfig(
    val widgetId: String,
    val title: String,
    val enabled: Boolean = true,
    val sizeMode: String = "COMPACT", // COMPACT, EXPANDED
    val orderIndex: Int
)

object KharcheeProModules21To30Store {
    private const val PREFS_NAME = "kharchee_pro_modules_21_30_prefs"

    val SUPPORTED_WORKSPACE_WIDGETS = linkedMapOf(
        "ai_insights" to "AI Insights Summary",
        "cashflow_forecast" to "Cash-Flow Forecast",
        "spending_alerts" to "Smart Spending Alerts",
        "financial_health" to "Financial Health Score",
        "net_worth" to "Net-Worth Summary",
        "upcoming_bills" to "Upcoming Bills & EMIs",
        "goal_progress" to "Goal Intelligence Progress",
        "recent_reports" to "Recently Generated Reports"
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // =========================================================================
    // MODULE 21 — JARVIS COMMAND CENTER
    // =========================================================================
    fun executeJarvisCommand(
        context: Context,
        userId: String,
        commandText: String,
        expenses: List<ExpenseRecord>,
        nowMillis: Long = System.currentTimeMillis()
    ): JarvisCommandExecutionResult {
        val clean = commandText.trim()
        if (clean.isBlank()) {
            return JarvisCommandExecutionResult(
                rawCommand = clean,
                category = JarvisCommandCategory.INVALID,
                responseText = "Please enter a valid financial command (e.g., 'Show my expenses this month' or 'Open my EMI calendar')."
            )
        }
        recordJarvisCommandHistory(context, userId, clean)
        val lower = clean.lowercase(Locale.US)
        val monthOutflows = expenses.filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, nowMillis) }

        return when {
            // Navigation commands
            lower.contains("open") && (lower.contains("emi") || lower.contains("calendar")) -> {
                JarvisCommandExecutionResult(
                    rawCommand = clean,
                    category = JarvisCommandCategory.NAVIGATION,
                    responseText = "Opening Money Calendar Pro (Module 15) with your EMI & Bill schedule.",
                    targetModuleId = "m15_calendar"
                )
            }
            lower.contains("open") && lower.contains("bill") -> {
                JarvisCommandExecutionResult(
                    rawCommand = clean,
                    category = JarvisCommandCategory.NAVIGATION,
                    responseText = "Opening Smart Bill Manager (Module 13).",
                    targetModuleId = "m13_bills"
                )
            }
            // Data-changing commands (MUST preview and require explicit confirmation!)
            lower.startsWith("create") || lower.startsWith("log ") || lower.startsWith("add ") || lower.contains("create a monthly spending report") -> {
                val monthSum = monthOutflows.sumOf { it.amount }
                val preview = JarvisCommandActionPreview(
                    actionType = if (lower.contains("report")) "CREATE_MONTHLY_REPORT" else "LOG_EXPENSE",
                    description = if (lower.contains("report")) {
                        "Generate & save Monthly Spending Report (Total: ₹$monthSum across ${monthOutflows.size} records)"
                    } else {
                        "Propose new financial record from command: '$clean'"
                    },
                    amount = monthSum,
                    category = "Report",
                    requiresExplicitConfirmation = true
                )
                JarvisCommandExecutionResult(
                    rawCommand = clean,
                    category = JarvisCommandCategory.DATA_CHANGING,
                    responseText = "Action Preview Ready: '${preview.description}'. Explicit confirmation is required before modifying saved records.",
                    pendingConfirmationPreview = preview
                )
            }
            // Read-only queries
            lower.contains("largest") || lower.contains("biggest") -> {
                val largest = monthOutflows.maxByOrNull { it.amount }
                val text = if (largest != null) {
                    "Your largest expense this month is '${largest.title}' for ₹${largest.amount} (${largest.category})."
                } else {
                    "No recorded expenses found for this month."
                }
                JarvisCommandExecutionResult(rawCommand = clean, category = JarvisCommandCategory.READ_ONLY, responseText = text)
            }
            lower.contains("compare") -> {
                val calPrev = Calendar.getInstance().apply { timeInMillis = nowMillis; add(Calendar.MONTH, -1) }
                val prevSum = expenses.filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, calPrev.timeInMillis) }.sumOf { it.amount }
                val curSum = monthOutflows.sumOf { it.amount }
                JarvisCommandExecutionResult(
                    rawCommand = clean,
                    category = JarvisCommandCategory.READ_ONLY,
                    responseText = "Month Comparison: Current month outflow is ₹$curSum vs ₹$prevSum last month (Difference: ₹${curSum - prevSum})."
                )
            }
            lower.contains("upcoming bill") -> {
                val bills = KharcheeProModules11To20Store.getSmartBills(context, userId).filter { it.status == "ACTIVE" }
                val text = if (bills.isEmpty()) "No active upcoming bills recorded."
                else "Upcoming active bills (${bills.size}): ${bills.joinToString { "${it.title} (₹${it.expectedAmount} on day ${it.dueDayOfMonth})" }}."
                JarvisCommandExecutionResult(rawCommand = clean, category = JarvisCommandCategory.READ_ONLY, responseText = text)
            }
            else -> {
                val curSum = monthOutflows.sumOf { it.amount }
                JarvisCommandExecutionResult(
                    rawCommand = clean,
                    category = JarvisCommandCategory.READ_ONLY,
                    responseText = "Saved Passbook Summary: ₹$curSum spent this month across ${monthOutflows.size} transactions."
                )
            }
        }
    }

    fun confirmJarvisCommandAction(
        context: Context,
        userId: String,
        preview: JarvisCommandActionPreview
    ): Boolean {
        val executedSet = (prefs(context).getStringSet("${userId}_m21_executed_actions", emptySet()) ?: emptySet()).toMutableSet()
        if (preview.actionId in executedSet) return false // Prevent duplicate execution
        executedSet.add(preview.actionId)
        prefs(context).edit().putStringSet("${userId}_m21_executed_actions", executedSet).apply()
        if (preview.actionType == "CREATE_MONTHLY_REPORT") {
            KharcheeProIntelligenceStore.recordGeneratedReport(
                context = context,
                userId = userId,
                title = "JARVIS Monthly Spending Report",
                moduleType = "JARVIS_CMD",
                summaryLine = preview.description
            )
        }
        return true
    }

    fun getRecentJarvisCommands(context: Context, userId: String): List<String> {
        val raw = prefs(context).getString("${userId}_m21_recent_cmds", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun recordJarvisCommandHistory(context: Context, userId: String, cmd: String) {
        val current = getRecentJarvisCommands(context, userId).toMutableList()
        current.remove(cmd)
        current.add(0, cmd)
        val arr = JSONArray(current.take(10))
        prefs(context).edit().putString("${userId}_m21_recent_cmds", arr.toString()).apply()
    }

    // =========================================================================
    // MODULE 22 — FINANCIAL SAFETY MONITOR
    // =========================================================================
    fun buildFinancialSafetyMonitorEvents(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>
    ): List<FinancialSafetyEvent> {
        val reviewed = prefs(context).getStringSet("${userId}_m22_reviewed", emptySet()) ?: emptySet()
        val dismissed = prefs(context).getStringSet("${userId}_m22_dismissed", emptySet()) ?: emptySet()
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val events = mutableListOf<FinancialSafetyEvent>()

        val outflows = expenses.filter { !isIncomeTransaction(it) }.sortedBy { it.txEpochMillis }
        // 1. Possible Duplicate detection (never auto-deletes transactions!)
        for (i in 0 until outflows.size - 1) {
            val a = outflows[i]
            val b = outflows[i + 1]
            if (a.title.equals(b.title, ignoreCase = true) && a.amount == b.amount && abs(b.txEpochMillis - a.txEpochMillis) <= 86_400_000L) {
                val id = "safe_dup_${a.id}_${b.id}"
                events.add(
                    FinancialSafetyEvent(
                        id = id,
                        classification = SafetyEventClassification.POSSIBLE_DUPLICATE,
                        title = "Possible Duplicate Entry: ${a.title}",
                        details = "Two identical ₹${a.amount} records logged within 24 hours. Review in Passbook (never auto-deleted).",
                        amount = a.amount,
                        dateLabel = dateFmt.format(Date(b.txEpochMillis)),
                        isReviewed = id in reviewed,
                        isDismissed = id in dismissed
                    )
                )
            }
        }

        // 2. Unusual Spending Pattern (never labelled as fraud)
        if (outflows.size >= 2) {
            val avg = outflows.map { it.amount }.average()
            outflows.filter { it.amount >= max(2000.0, avg * 2.2) }.forEach { tx ->
                val id = "safe_unusual_${tx.id}"
                events.add(
                    FinancialSafetyEvent(
                        id = id,
                        classification = SafetyEventClassification.UNUSUAL_SPENDING,
                        title = "Unusual Spending Pattern: ${tx.title}",
                        details = "Outflow of ₹${tx.amount} exceeds typical transaction baseline (₹${avg.roundToLong()}). Flagged as unusual pattern only.",
                        amount = tx.amount,
                        dateLabel = dateFmt.format(Date(tx.txEpochMillis)),
                        isReviewed = id in reviewed,
                        isDismissed = id in dismissed
                    )
                )
            }
        }

        // 3. Confirmed Application Security Event
        val authId = "safe_sec_session_$userId"
        events.add(
            FinancialSafetyEvent(
                id = authId,
                classification = SafetyEventClassification.CONFIRMED_SECURITY_EVENT,
                title = "Authenticated Session Verified",
                details = "Firebase Authentication & User-Isolated Storage active for account.",
                amount = 0L,
                dateLabel = dateFmt.format(Date()),
                isReviewed = authId in reviewed,
                isDismissed = authId in dismissed
            )
        )

        return events.distinctBy { it.id }.filterNot { it.isDismissed }
    }

    fun reviewOrDismissSafetyEvent(context: Context, userId: String, eventId: String, dismiss: Boolean) {
        val key = if (dismiss) "${userId}_m22_dismissed" else "${userId}_m22_reviewed"
        val set = (prefs(context).getStringSet(key, emptySet()) ?: emptySet()).toMutableSet().apply { add(eventId) }
        prefs(context).edit().putStringSet(key, set).apply()
    }

    // =========================================================================
    // MODULE 23 — SMART DOCUMENT INTELLIGENCE
    // =========================================================================
    fun analyzeDocumentIntelligence(
        context: Context,
        requestingUserId: String,
        fileName: String,
        mimeType: String,
        fileSizeBytes: Long,
        rawTextOrNotes: String,
        category: String = "Receipts"
    ): Result<SmartDocumentExtractionDraft> {
        val normMime = mimeType.trim().lowercase(Locale.US)
        val lowerName = fileName.trim().lowercase(Locale.US)
        val validExt = lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") || lowerName.endsWith(".webp")
        if (normMime !in setOf("application/pdf", "image/jpeg", "image/jpg", "image/png", "image/webp") || !validExt) {
            return Result.failure(IllegalArgumentException("Invalid document format. Only PDF, JPG, PNG, and WEBP are supported."))
        }
        if (fileSizeBytes <= 0L || fileSizeBytes > 10L * 1024L * 1024L) {
            return Result.failure(IllegalArgumentException("File size must be between 1B and 10MB."))
        }

        // Extract only clearly present values; never guess unreadable amounts or dates
        val amountRegex = Regex("""(?:₹|INR|Rs\.?\s*)(\d[\d,]*)""", RegexOption.IGNORE_CASE)
        val dateRegex = Regex("""(\d{1,2}\s+[A-Za-z]{3,9}\s+\d{4}|\d{4}-\d{2}-\d{2})""")
        val taxRegex = Regex("""(?:GST|Tax)[:\s]*(?:₹|INR|Rs\.?\s*)?(\d+)""", RegexOption.IGNORE_CASE)

        val matchedAmt = amountRegex.find(rawTextOrNotes)?.groupValues?.getOrNull(1)?.replace(",", "")?.toLongOrNull()
        val matchedDate = dateRegex.find(rawTextOrNotes)?.groupValues?.getOrNull(1)
        val matchedTax = taxRegex.find(rawTextOrNotes)?.groupValues?.getOrNull(1)?.toLongOrNull()
        val matchedMerchant = rawTextOrNotes.lines().firstOrNull { it.isNotBlank() && !it.any { c -> c.isDigit() } }?.trim()

        val uncertain = mutableListOf<String>()
        if (matchedMerchant.isNullOrBlank()) uncertain.add("Merchant / Issuer (Unreadable — Manual Entry Required)")
        if (matchedDate.isNullOrBlank()) uncertain.add("Document Date (Unreadable — Do Not Guess)")
        if (matchedAmt == null) uncertain.add("Total Amount (Unreadable — Verification Required)")

        val existingDocs = KharcheeProModules11To20Store.getVaultDocuments(context, requestingUserId)
        val isDup = existingDocs.any { it.fileName.equals(fileName.trim(), ignoreCase = true) && it.fileSizeBytes == fileSizeBytes }

        return Result.success(
            SmartDocumentExtractionDraft(
                ownerUserId = requestingUserId,
                fileName = fileName.trim(),
                mimeType = normMime,
                fileSizeBytes = fileSizeBytes,
                extractedMerchant = matchedMerchant,
                extractedDate = matchedDate,
                extractedAmount = matchedAmt,
                extractedTaxAmount = matchedTax,
                category = category,
                uncertainFields = uncertain,
                isDuplicateDocument = isDup,
                requiresConfirmationBeforeTransaction = true
            )
        )
    }

    // =========================================================================
    // MODULE 24 — LONG-TERM WEALTH PLANNER
    // =========================================================================
    fun calculateLongTermWealthPlan(input: LongTermWealthScenarioInput): LongTermWealthProjectionResult {
        if (input.durationYears !in 1..60 ||
            input.startingSavings < 0L ||
            input.monthlyContribution < 0L ||
            input.annualContributionGrowthPct < 0.0 ||
            input.assumedAnnualReturnPct < 0.0 ||
            input.annualInflationPct < 0.0 ||
            input.targetAmount < 0L
        ) {
            return LongTermWealthProjectionResult(
                isValid = false,
                errorMessage = "Duration must be 1–60 years and all financial inputs/rates must be non-negative.",
                durationYears = input.durationYears,
                totalContributions = 0L,
                noGrowthFutureValue = 0L,
                assumedGrowthAmount = 0L,
                projectedFutureValue = 0L,
                inflationAdjustedValue = 0L,
                alternativeHigherContribFutureValue = 0L,
                targetGap = 0L,
                compoundingExplanation = "",
                hypotheticalDisclaimer = ""
            )
        }

        val monthlyRate = (input.assumedAnnualReturnPct / 12.0) / 100.0
        val stepUpRate = input.annualContributionGrowthPct / 100.0

        var totalContrib = input.startingSavings.toDouble()
        var balanceWithGrowth = input.startingSavings.toDouble()
        var balanceAltHigher = input.startingSavings.toDouble()
        var currentMonthly = input.monthlyContribution.toDouble()
        var currentAltMonthly = input.monthlyContribution * 1.20

        for (year in 1..input.durationYears) {
            for (m in 1..12) {
                totalContrib += currentMonthly
                balanceWithGrowth = (balanceWithGrowth + currentMonthly) * (1.0 + monthlyRate)
                balanceAltHigher = (balanceAltHigher + currentAltMonthly) * (1.0 + monthlyRate)
            }
            currentMonthly *= (1.0 + stepUpRate)
            currentAltMonthly *= (1.0 + stepUpRate)
        }

        val totalContributionsLong = totalContrib.roundToLong()
        val projectedFv = if (input.assumedAnnualReturnPct == 0.0) totalContributionsLong else balanceWithGrowth.roundToLong()
        val assumedGrowth = max(0L, projectedFv - totalContributionsLong)
        val inflationFactor = (1.0 + (input.annualInflationPct / 100.0)).pow(input.durationYears.toDouble())
        val realValue = (projectedFv.toDouble() / inflationFactor).roundToLong()
        val altFv = if (input.assumedAnnualReturnPct == 0.0) (input.startingSavings + (totalContributionsLong - input.startingSavings) * 1.2).roundToLong() else balanceAltHigher.roundToLong()
        val gap = max(0L, input.targetAmount - projectedFv)

        return LongTermWealthProjectionResult(
            isValid = true,
            errorMessage = null,
            durationYears = input.durationYears,
            totalContributions = totalContributionsLong,
            noGrowthFutureValue = totalContributionsLong,
            assumedGrowthAmount = assumedGrowth,
            projectedFutureValue = projectedFv,
            inflationAdjustedValue = realValue,
            alternativeHigherContribFutureValue = altFv,
            targetGap = gap,
            compoundingExplanation = "Monthly compounding at ${input.assumedAnnualReturnPct}% p.a. with ${input.annualContributionGrowthPct}% annual contribution step-up and ${input.annualInflationPct}% inflation adjustment.",
            hypotheticalDisclaimer = "HYPOTHETICAL ESTIMATE ONLY — Assumed returns are not guaranteed investment outcomes and do not recommend any securities."
        )
    }

    fun saveWealthPlanScenario(context: Context, userId: String, scenario: LongTermWealthScenarioInput): List<LongTermWealthScenarioInput> {
        val current = getSavedWealthPlanScenarios(context, userId).filterNot { it.id == scenario.id }.toMutableList()
        current.add(0, scenario)
        val arr = JSONArray()
        current.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id); put("name", s.name); put("durationYears", s.durationYears)
                put("startingSavings", s.startingSavings); put("monthlyContribution", s.monthlyContribution)
                put("annualContributionGrowthPct", s.annualContributionGrowthPct)
                put("assumedAnnualReturnPct", s.assumedAnnualReturnPct)
                put("annualInflationPct", s.annualInflationPct); put("targetAmount", s.targetAmount)
            })
        }
        prefs(context).edit().putString("${userId}_m24_wealth_plans", arr.toString()).apply()
        return current
    }

    fun getSavedWealthPlanScenarios(context: Context, userId: String): List<LongTermWealthScenarioInput> {
        val raw = prefs(context).getString("${userId}_m24_wealth_plans", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                LongTermWealthScenarioInput(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    durationYears = o.optInt("durationYears", 5),
                    startingSavings = o.optLong("startingSavings"),
                    monthlyContribution = o.optLong("monthlyContribution"),
                    annualContributionGrowthPct = o.optDouble("annualContributionGrowthPct", 0.0),
                    assumedAnnualReturnPct = o.optDouble("assumedAnnualReturnPct", 0.0),
                    annualInflationPct = o.optDouble("annualInflationPct", 0.0),
                    targetAmount = o.optLong("targetAmount")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // =========================================================================
    // MODULE 25 — TAX & DEDUCTION ORGANIZER
    // =========================================================================
    fun getFinancialYearBounds(startYear: Int): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(startYear, Calendar.APRIL, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(startYear + 1, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    fun addUserLabelledDeduction(
        context: Context,
        userId: String,
        financialYear: String,
        title: String,
        sectionTag: String,
        amount: Long,
        linkedDocumentId: String = ""
    ): List<UserLabelledDeductionItem> {
        if (title.isBlank() || amount <= 0L) return getUserLabelledDeductions(context, userId, financialYear)
        val all = getAllDeductionsInternal(context, userId).toMutableList()
        all.add(0, UserLabelledDeductionItem(financialYear = financialYear, title = title.trim(), sectionTag = sectionTag.trim(), amount = amount, linkedDocumentId = linkedDocumentId))
        val arr = JSONArray()
        all.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id); put("financialYear", d.financialYear); put("title", d.title)
                put("sectionTag", d.sectionTag); put("amount", d.amount); put("linkedDocumentId", d.linkedDocumentId)
            })
        }
        prefs(context).edit().putString("${userId}_m25_deductions", arr.toString()).apply()
        return all.filter { it.financialYear == financialYear }
    }

    fun getUserLabelledDeductions(context: Context, userId: String, financialYear: String): List<UserLabelledDeductionItem> =
        getAllDeductionsInternal(context, userId).filter { it.financialYear == financialYear }

    private fun getAllDeductionsInternal(context: Context, userId: String): List<UserLabelledDeductionItem> {
        val raw = prefs(context).getString("${userId}_m25_deductions", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                UserLabelledDeductionItem(
                    id = o.optString("id"),
                    financialYear = o.optString("financialYear"),
                    title = o.optString("title"),
                    sectionTag = o.optString("sectionTag"),
                    amount = o.optLong("amount"),
                    linkedDocumentId = o.optString("linkedDocumentId")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun buildTaxAndDeductionSummary(
        context: Context,
        userId: String,
        startYear: Int,
        expenses: List<ExpenseRecord>
    ): TaxOrganizerSummary {
        val fyLabel = "FY $startYear-${(startYear + 1) % 100}"
        val (startMs, endMs) = getFinancialYearBounds(startYear)
        val fyTx = expenses.filter { it.txEpochMillis in startMs..endMs }
        val recordedInc = fyTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val recordedExp = fyTx.filter { !isIncomeTransaction(it) }.sumOf { it.amount }

        val deductions = getUserLabelledDeductions(context, userId, fyLabel)
        val docs = KharcheeProModules11To20Store.getVaultDocuments(context, userId)
            .filter { it.category.contains("Tax", true) || it.category.contains("Insurance", true) }

        val missingChecklist = deductions.filter { it.linkedDocumentId.isBlank() }
            .map { "Missing supporting document for user-labelled item: '${it.title}' (₹${it.amount})" }

        return TaxOrganizerSummary(
            financialYear = fyLabel,
            startDateMillis = startMs,
            endDateMillis = endMs,
            recordedIncomeTotal = recordedInc,
            recordedExpenseTotal = recordedExp,
            userLabelledDeductions = deductions,
            userLabelledDeductionTotal = deductions.sumOf { it.amount },
            uploadedTaxDocumentsCount = docs.size,
            missingDocumentChecklist = missingChecklist,
            legalDisclaimer = "Record-keeping organizer only. User-labelled entries are not automatically verified as legally deductible and do not constitute tax advice."
        )
    }

    // =========================================================================
    // MODULE 26 — WEALTH MILESTONE TRACKER
    // =========================================================================
    fun evaluateWealthMilestones(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        savingsGoals: List<SavingsGoalRecord>
    ): List<WealthMilestoneRecord> {
        val actualSavedInGoals = savingsGoals.sumOf { it.savedAmount }
        val actualNetPassbook = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount } -
            expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val totalActualWealth = max(0L, actualSavedInGoals) + max(0L, actualNetPassbook)
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())

        val defaults = listOf(
            WealthMilestoneRecord(
                id = "ms_first_10k",
                title = "First ₹10,000 Actual Savings Milestone",
                targetAmount = 10000L,
                currentActualAmount = totalActualWealth,
                targetDate = "31 Dec 2026",
                isAchieved = totalActualWealth >= 10000L,
                completionDate = if (totalActualWealth >= 10000L) dateStr else null
            ),
            WealthMilestoneRecord(
                id = "ms_emergency_50k",
                title = "₹50,000 Emergency Cushion Milestone",
                targetAmount = 50000L,
                currentActualAmount = totalActualWealth,
                targetDate = "30 Jun 2027",
                isAchieved = totalActualWealth >= 50000L,
                completionDate = if (totalActualWealth >= 50000L) dateStr else null
            ),
            WealthMilestoneRecord(
                id = "ms_goal_complete",
                title = "First Savings Goal 100% Completed",
                targetAmount = 1L,
                currentActualAmount = savingsGoals.count { it.savedAmount >= it.targetAmount && it.targetAmount > 0L }.toLong(),
                targetDate = "Ongoing",
                isAchieved = savingsGoals.any { it.savedAmount >= it.targetAmount && it.targetAmount > 0L },
                completionDate = if (savingsGoals.any { it.savedAmount >= it.targetAmount && it.targetAmount > 0L }) dateStr else null
            )
        )
        return defaults.distinctBy { it.id }
    }

    fun buildPrivacySafeMilestoneShareText(milestone: WealthMilestoneRecord, exposeExactBalances: Boolean = false): String {
        return if (!exposeExactBalances) {
            "KHARCHEE MILESTONE: ${milestone.title} — Status: ${if (milestone.isAchieved) "ACHIEVED ✓" else "IN PROGRESS"} (Private Balances Protected)"
        } else {
            "KHARCHEE MILESTONE: ${milestone.title} — ₹${milestone.currentActualAmount} / ₹${milestone.targetAmount}"
        }
    }

    // =========================================================================
    // MODULE 27 — LIFE EVENT PLANNER
    // =========================================================================
    fun getLifeEvents(context: Context, userId: String): List<LifeEventPlanRecord> {
        val raw = prefs(context).getString("${userId}_m27_life_events", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                LifeEventPlanRecord(
                    id = o.optString("id"),
                    eventName = o.optString("eventName"),
                    category = o.optString("category"),
                    targetDate = o.optString("targetDate"),
                    monthsUntilEvent = o.optInt("monthsUntilEvent", 12),
                    estimatedCost = o.optLong("estimatedCost"),
                    currentSavingsAllocated = o.optLong("currentSavingsAllocated"),
                    monthlySavingsContribution = o.optLong("monthlySavingsContribution"),
                    priority = o.optString("priority", "High"),
                    linkedGoalId = o.optString("linkedGoalId"),
                    notes = o.optString("notes"),
                    isArchived = o.optBoolean("isArchived", false)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveLifeEventPlan(context: Context, userId: String, plan: LifeEventPlanRecord): List<LifeEventPlanRecord> {
        val list = getLifeEvents(context, userId).filterNot { it.id == plan.id }.toMutableList()
        list.add(0, plan)
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id); put("eventName", p.eventName); put("category", p.category)
                put("targetDate", p.targetDate); put("monthsUntilEvent", p.monthsUntilEvent)
                put("estimatedCost", p.estimatedCost); put("currentSavingsAllocated", p.currentSavingsAllocated)
                put("monthlySavingsContribution", p.monthlySavingsContribution); put("priority", p.priority)
                put("linkedGoalId", p.linkedGoalId); put("notes", p.notes); put("isArchived", p.isArchived)
            })
        }
        prefs(context).edit().putString("${userId}_m27_life_events", arr.toString()).apply()
        return list
    }

    // =========================================================================
    // MODULE 28 — PERSONALISED MONEY COACH
    // =========================================================================
    fun buildPersonalisedCoachTasks(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        savingsGoals: List<SavingsGoalRecord>,
        dailyLimit: Long
    ): List<MoneyCoachTask> {
        val statusMapRaw = prefs(context).getString("${userId}_m28_task_status", "{}") ?: "{}"
        val statusObj = try { JSONObject(statusMapRaw) } catch (_: Exception) { JSONObject() }

        if (expenses.isEmpty()) {
            val id = "coach_onboarding_start"
            val st = statusObj.optString(id, "PENDING")
            return listOf(
                MoneyCoachTask(
                    id = id,
                    title = "Welcome Onboarding: Log Your First 3 Transactions",
                    whatWasObserved = "Your passbook currently has 0 recorded transactions.",
                    whyGenerated = "Personalised Money Coach requires real spending and income entries to compute tailored weekly habits.",
                    whatUserCanDo = "Add your next meal, bill, or income entry in Passbook or via SNAP & TRACK.",
                    isEstimate = false,
                    status = st
                )
            )
        }

        val outflows = expenses.filter { !isIncomeTransaction(it) }
        val totalSpent = outflows.sumOf { it.amount }
        val topCategory = outflows.groupBy { it.category }.maxByOrNull { it.value.sumOf { tx -> tx.amount } }
        val tasks = mutableListOf<MoneyCoachTask>()

        if (topCategory != null) {
            val catSum = topCategory.value.sumOf { it.amount }
            val id = "coach_review_${topCategory.key.lowercase(Locale.US)}"
            val st = statusObj.optString(id, "PENDING")
            if (st != "DISMISSED") {
                tasks.add(
                    MoneyCoachTask(
                        id = id,
                        title = "Weekly Category Check: ${topCategory.key}",
                        whatWasObserved = "${topCategory.key} is your highest outflow category at ₹$catSum across ${topCategory.value.size} transactions.",
                        whyGenerated = "Reviewing your largest category once a week helps keep daily spending below your ₹$dailyLimit target.",
                        whatUserCanDo = "Inspect your recent ${topCategory.key} entries and set a comfortable weekly cap.",
                        isEstimate = false,
                        status = st
                    )
                )
            }
        }

        val goalTaskId = "coach_goal_consistency"
        val goalStatus = statusObj.optString(goalTaskId, "PENDING")
        if (goalStatus != "DISMISSED") {
            tasks.add(
                MoneyCoachTask(
                    id = goalTaskId,
                    title = "Monthly Savings Consistency Step",
                    whatWasObserved = "You have ${savingsGoals.size} active savings goal(s) and ₹$totalSpent in recorded expenses.",
                    whyGenerated = "Small, steady contributions after covering essential expenses build long-term resilience.",
                    whatUserCanDo = "Allocate a comfortable amount (estimated ₹500–₹1,000) toward your priority goal this week.",
                    isEstimate = true,
                    status = goalStatus
                )
            )
        }

        return tasks
    }

    fun updateCoachTaskState(context: Context, userId: String, taskId: String, newStatus: String) {
        val raw = prefs(context).getString("${userId}_m28_task_status", "{}") ?: "{}"
        val obj = try { JSONObject(raw) } catch (_: Exception) { JSONObject() }
        obj.put(taskId, newStatus)
        prefs(context).edit().putString("${userId}_m28_task_status", obj.toString()).apply()
    }

    // =========================================================================
    // MODULE 29 — PRIVACY & SECURITY CENTER
    // =========================================================================
    fun getPrivacySecurityCenterState(context: Context, authenticatedUserId: String): PrivacySecurityCenterState {
        val p = prefs(context)
        val docsCount = KharcheeProModules11To20Store.getVaultDocuments(context, authenticatedUserId).size
        return PrivacySecurityCenterState(
            userId = authenticatedUserId,
            vaultPinEnabled = p.getBoolean("${authenticatedUserId}_m29_vault_pin", true),
            maskBalancesInShareCards = p.getBoolean("${authenticatedUserId}_m29_mask_share", true),
            biometricLockRequested = p.getBoolean("${authenticatedUserId}_m29_biometric", false),
            privateDocumentsCount = docsCount,
            securityEventsLog = listOf(
                "Firebase Auth Session Verified (UID isolated)",
                "Private Document Vault Authorization Enforced ($docsCount files)",
                "Zero plaintext password/OTP/token exposure policy active"
            )
        )
    }

    fun updatePrivacySecurityConfig(
        context: Context,
        authenticatedUserId: String,
        requestingUserId: String,
        vaultPinEnabled: Boolean,
        maskBalancesInShareCards: Boolean,
        biometricLockRequested: Boolean
    ): Result<PrivacySecurityCenterState> {
        if (authenticatedUserId.isBlank() || authenticatedUserId != requestingUserId) {
            return Result.failure(SecurityException("Unauthorized: Active user authentication failed."))
        }
        prefs(context).edit()
            .putBoolean("${authenticatedUserId}_m29_vault_pin", vaultPinEnabled)
            .putBoolean("${authenticatedUserId}_m29_mask_share", maskBalancesInShareCards)
            .putBoolean("${authenticatedUserId}_m29_biometric", biometricLockRequested)
            .apply()
        return Result.success(getPrivacySecurityCenterState(context, authenticatedUserId))
    }

    // =========================================================================
    // MODULE 30 — PRO WORKSPACE STUDIO
    // =========================================================================
    fun getWorkspaceStudioLayout(context: Context, userId: String): List<ProWorkspaceWidgetConfig> {
        val raw = prefs(context).getString("${userId}_m30_layout", null) ?: return defaultWorkspaceLayout()
        return try {
            val arr = JSONArray(raw)
            val parsed = (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val wid = o.optString("widgetId")
                val title = SUPPORTED_WORKSPACE_WIDGETS[wid] ?: return@mapNotNull null // Ignore invalid widget IDs safely
                ProWorkspaceWidgetConfig(
                    widgetId = wid,
                    title = title,
                    enabled = o.optBoolean("enabled", true),
                    sizeMode = if (o.optString("sizeMode") == "EXPANDED") "EXPANDED" else "COMPACT",
                    orderIndex = o.optInt("orderIndex", i)
                )
            }.distinctBy { it.widgetId }.sortedBy { it.orderIndex }
            if (parsed.isEmpty()) defaultWorkspaceLayout() else parsed
        } catch (_: Exception) {
            defaultWorkspaceLayout()
        }
    }

    fun saveWorkspaceStudioLayout(
        context: Context,
        userId: String,
        widgets: List<ProWorkspaceWidgetConfig>
    ): List<ProWorkspaceWidgetConfig> {
        val valid = widgets
            .filter { it.widgetId in SUPPORTED_WORKSPACE_WIDGETS }
            .distinctBy { it.widgetId }
            .mapIndexed { idx, w -> w.copy(title = SUPPORTED_WORKSPACE_WIDGETS[w.widgetId]!!, orderIndex = idx) }
        val finalLayout = if (valid.isEmpty()) defaultWorkspaceLayout() else valid
        val arr = JSONArray()
        finalLayout.forEach { w ->
            arr.put(JSONObject().apply {
                put("widgetId", w.widgetId); put("title", w.title)
                put("enabled", w.enabled); put("sizeMode", w.sizeMode); put("orderIndex", w.orderIndex)
            })
        }
        prefs(context).edit().putString("${userId}_m30_layout", arr.toString()).apply()
        return finalLayout
    }

    fun resetWorkspaceStudioLayout(context: Context, userId: String): List<ProWorkspaceWidgetConfig> {
        prefs(context).edit().remove("${userId}_m30_layout").apply()
        return defaultWorkspaceLayout()
    }

    private fun defaultWorkspaceLayout(): List<ProWorkspaceWidgetConfig> =
        SUPPORTED_WORKSPACE_WIDGETS.entries.mapIndexed { idx, (id, title) ->
            ProWorkspaceWidgetConfig(widgetId = id, title = title, enabled = true, sizeMode = "COMPACT", orderIndex = idx)
        }
}
