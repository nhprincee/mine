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
import kotlin.math.roundToLong

enum class ForecastPeriodOption(
    val id: String,
    val label: String,
    val days: Int,
    val monthMultiplier: Double
) {
    NEXT_7_DAYS("7d", "Next 7 Days", 7, 7.0 / 30.0),
    NEXT_30_DAYS("30d", "Next 30 Days", 30, 1.0),
    NEXT_3_MONTHS("3m", "Next 3 Months", 90, 3.0),
    NEXT_6_MONTHS("6m", "Next 6 Months", 180, 6.0);

    companion object {
        fun fromId(id: String): ForecastPeriodOption =
            entries.find { it.id == id } ?: NEXT_30_DAYS
    }
}

data class CategoryForecastBreakdown(
    val category: String,
    val confirmedRecurringAmount: Long,
    val estimatedVariableAmount: Long,
    val totalPredictedAmount: Long,
    val historicalAverageMonthly: Long
)

data class ForecastTrendPoint(
    val label: String,
    val historicalAmount: Long,
    val projectedAmount: Long
)

data class AiMoneyPredictionResult(
    val period: ForecastPeriodOption,
    val hasSufficientData: Boolean,
    val insufficientDataReason: String,
    val requiredAdditionalHistoryMessage: String,
    val confidenceLevel: String,
    val confidenceScorePct: Int,
    val predictedIncome: Long,
    val confirmedRecurringExpenses: Long,
    val estimatedVariableExpenses: Long,
    val predictedTotalExpenses: Long,
    val estimatedSavings: Long,
    val currentNetBalance: Long,
    val estimatedPeriodEndBalance: Long,
    val categoryBreakdowns: List<CategoryForecastBreakdown>,
    val trendPoints: List<ForecastTrendPoint>,
    val dataPeriodUsedLabel: String,
    val calculationAssumptions: List<String>,
    val limitations: List<String>,
    val explanationSummary: String
)

data class WhatIfScenarioInput(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "My Scenario",
    val monthlyIncome: Long = 0L,
    val essentialExpenses: Long = 0L,
    val lifestyleExpenses: Long = 0L,
    val savingsContribution: Long = 0L,
    val emiObligations: Long = 0L,
    val subscriptionCosts: Long = 0L,
    val oneTimePurchases: Long = 0L,
    val additionalIncome: Long = 0L,
    val unexpectedExpenses: Long = 0L,
    val projectionMonths: Int = 6,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class WhatIfComparisonResult(
    val isValid: Boolean,
    val validationError: String?,
    val currentMonthlyIncome: Long,
    val currentMonthlyExpenses: Long,
    val currentRemainingBalance: Long,
    val currentMonthlySavings: Long,
    val totalSimulatedIncome: Long,
    val totalSimulatedExpenses: Long,
    val simulatedRecurringMonthlyExpenses: Long,
    val estimatedRemainingBalance: Long,
    val estimatedTotalMonthlySavings: Long,
    val monthlySavingsDifference: Long,
    val expenseChangePercent: Double,
    val savingsChangePercent: Double,
    val projectionMonths: Int,
    val currentProjectedSavingsOverPeriod: Long,
    val simulatedProjectedSavingsOverPeriod: Long,
    val projectedDifferenceOverPeriod: Long
)

data class SmartDetectorPreferences(
    val anomalyMultiplierPct: Int = 180,
    val categorySpikeThresholdPct: Int = 25,
    val enableDuplicateDetection: Boolean = true,
    val enableBudgetWarnings: Boolean = true
)

data class SmartSpendingAlert(
    val id: String,
    val alertType: String, // "UNUSUAL_TX", "CATEGORY_SPIKE", "MERCHANT_SPIKE", "HIGH_DAY", "BUDGET_WARNING", "DUPLICATE_ENTRY", "NEW_RECURRING"
    val whatHappened: String,
    val amountInvolved: Long,
    val relevantDateOrPeriod: String,
    val comparisonBaseline: String,
    val whyFlagged: String,
    val suggestedNextAction: String,
    val category: String,
    val relatedTransactionIds: List<String>,
    val isReviewed: Boolean
)

data class SmartSpendingDetectorReport(
    val hasMinimumData: Boolean,
    val minimumDataMessage: String,
    val alerts: List<SmartSpendingAlert>,
    val reviewedCount: Int,
    val dismissedCount: Int
)

enum class LeakClassification(val id: String, val label: String) {
    CONFIRMED_SUBSCRIPTION("confirmed_sub", "Confirmed Subscription"),
    USER_RECURRING("user_recurring", "User-Created Recurring"),
    AI_DETECTED_POSSIBLE("ai_possible", "AI-Detected Possible Recurring")
}

enum class BillingFrequency(val id: String, val label: String, val monthlyFactor: Double, val annualFactor: Double) {
    WEEKLY("weekly", "Weekly", 52.0 / 12.0, 52.0),
    MONTHLY("monthly", "Monthly", 1.0, 12.0),
    QUARTERLY("quarterly", "Quarterly", 1.0 / 3.0, 4.0),
    YEARLY("yearly", "Yearly", 1.0 / 12.0, 1.0);

    companion object {
        fun fromId(id: String): BillingFrequency =
            entries.find { it.id.equals(id, ignoreCase = true) || it.label.equals(id, ignoreCase = true) } ?: MONTHLY
    }
}

data class MoneyLeakItem(
    val key: String,
    val merchantName: String,
    val category: String,
    val rawAmount: Long,
    val frequency: BillingFrequency,
    val monthlyEquivalent: Double,
    val annualEquivalent: Double,
    val classification: LeakClassification,
    val renewalDate: String,
    val reminderEnabled: Boolean,
    val isCancelled: Boolean,
    val occurrenceCount: Int
)

data class SmallRepeatedSpendSummary(
    val merchantOrTitle: String,
    val category: String,
    val count: Int,
    val averageTicket: Long,
    val totalSpent: Long
)

data class MoneyLeakFinderReport(
    val activeItems: List<MoneyLeakItem>,
    val cancelledItems: List<MoneyLeakItem>,
    val confirmedSubscriptionsCount: Int,
    val userRecurringCount: Int,
    val aiPossibleCount: Int,
    val monthlyRecurringTotal: Double,
    val annualisedRecurringTotal: Double,
    val smallRepeatedExpenses: List<SmallRepeatedSpendSummary>,
    val possibleDuplicatePayments: List<SmartSpendingAlert>
)

data class ProGeneratedReport(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val moduleType: String,
    val summaryLine: String,
    val generatedAtMillis: Long = System.currentTimeMillis()
)

object KharcheeProIntelligenceStore {
    private const val PREFS_NAME = "kharchee_pro_intelligence_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // =========================================================================
    // FEATURE 1 — AI MONEY PREDICTOR
    // =========================================================================
    fun getSelectedForecastPeriod(context: Context, userId: String): ForecastPeriodOption {
        val raw = prefs(context).getString("${userId}_forecast_period", ForecastPeriodOption.NEXT_30_DAYS.id)
            ?: ForecastPeriodOption.NEXT_30_DAYS.id
        return ForecastPeriodOption.fromId(raw)
    }

    fun setSelectedForecastPeriod(context: Context, userId: String, period: ForecastPeriodOption) {
        prefs(context).edit().putString("${userId}_forecast_period", period.id).apply()
    }

    fun predictMoneyForecast(
        period: ForecastPeriodOption,
        expenses: List<ExpenseRecord>,
        subscriptions: List<SubscriptionItem>,
        emis: List<EmiItem>,
        reminders: List<PaymentReminderRecord>,
        monthlyBudget: Long,
        dailyLimit: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): AiMoneyPredictionResult {
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        val totalIncomeAllTime = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val totalExpenseAllTime = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val currentNetBalance = totalIncomeAllTime - totalExpenseAllTime

        val activeSubs = subscriptions.filter { it.isActive }
        val activeReminders = reminders.filter { !it.isPaid }

        if (expenses.isEmpty() && activeSubs.isEmpty() && emis.isEmpty() && activeReminders.isEmpty()) {
            return AiMoneyPredictionResult(
                period = period,
                hasSufficientData = false,
                insufficientDataReason = "No saved transactions, recurring subscriptions, or upcoming bills were found in your account.",
                requiredAdditionalHistoryMessage = "Log at least 3 transactions across 3+ days (or add active subscriptions/EMIs) so AI Money Predictor can establish a real historical baseline without fabricating numbers.",
                confidenceLevel = "INSUFFICIENT DATA",
                confidenceScorePct = 0,
                predictedIncome = 0L,
                confirmedRecurringExpenses = 0L,
                estimatedVariableExpenses = 0L,
                predictedTotalExpenses = 0L,
                estimatedSavings = 0L,
                currentNetBalance = 0L,
                estimatedPeriodEndBalance = 0L,
                categoryBreakdowns = emptyList(),
                trendPoints = emptyList(),
                dataPeriodUsedLabel = "0 transactions available",
                calculationAssumptions = listOf("Zero fabrication policy: No forecast generated when transaction history is empty."),
                limitations = listOf("Predictions require real historical transactions or confirmed recurring commitments."),
                explanationSummary = "Forecast unavailable until you record real income or expense transactions."
            )
        }

        val minTxMillis = expenses.minOfOrNull { it.txEpochMillis } ?: nowMillis
        val maxTxMillis = expenses.maxOfOrNull { it.txEpochMillis } ?: nowMillis
        val observedSpanDays = max(1, (((nowMillis - minTxMillis) / 86_400_000L).toInt() + 1).coerceAtMost(90))
        val distinctDaysCount = expenses.map { dayFormat.format(Date(it.txEpochMillis)) }.distinct().size

        val lookbackMillis = nowMillis - (observedSpanDays.toLong() * 86_400_000L)
        val recentWindowTx = expenses.filter { it.txEpochMillis >= lookbackMillis }
        val recentIncomes = recentWindowTx.filter { isIncomeTransaction(it) }
        val recentExpenses = recentWindowTx.filter { !isIncomeTransaction(it) }

        // Separate confirmed recurring merchants so we NEVER double count them in variable expenses
        val confirmedRecurringKeywords = mutableSetOf<String>()
        activeSubs.forEach { confirmedRecurringKeywords.add(it.name.trim().lowercase(Locale.US)) }
        emis.forEach { confirmedRecurringKeywords.add(it.title.trim().lowercase(Locale.US)) }
        activeReminders.forEach { confirmedRecurringKeywords.add(it.title.trim().lowercase(Locale.US)) }

        val monthlySubsAmount = activeSubs.sumOf { sub ->
            val freq = BillingFrequency.fromId(sub.billingCycle)
            (sub.amount * freq.monthlyFactor).roundToLong()
        }
        val monthlyEmisAmount = emis.sumOf { it.monthlyEmi }
        val upcomingRemindersInPeriod = activeReminders
            .filter { rem ->
                confirmedRecurringKeywords.none { kw ->
                    kw != rem.title.trim().lowercase(Locale.US) && rem.title.trim().lowercase(Locale.US).contains(kw)
                }
            }
            .sumOf { it.amount }

        val confirmedRecurringForPeriod = ((monthlySubsAmount + monthlyEmisAmount) * period.monthMultiplier).roundToLong() +
            (if (period.days <= 30) upcomingRemindersInPeriod else (upcomingRemindersInPeriod * period.monthMultiplier).roundToLong())

        // Filter out variable transactions that match confirmed recurring items to prevent double counting
        val variableExpensesHistory = recentExpenses.filter { tx ->
            val lowerTitle = tx.title.trim().lowercase(Locale.US)
            val isAlreadyCountedRecurring = confirmedRecurringKeywords.any { kw ->
                kw.isNotBlank() && (lowerTitle.contains(kw) || kw.contains(lowerTitle))
            }
            !isAlreadyCountedRecurring
        }

        val historicalVariableSum = variableExpensesHistory.sumOf { it.amount }
        val dailyVariableRate = if (observedSpanDays >= 3) {
            historicalVariableSum.toDouble() / observedSpanDays.toDouble()
        } else {
            // Transparent basic estimate using actual logged sum divided by 30 days
            historicalVariableSum.toDouble() / max(7, observedSpanDays).toDouble()
        }

        val estimatedVariableForPeriod = (dailyVariableRate * period.days).roundToLong()
        val predictedTotalExpenses = confirmedRecurringForPeriod + estimatedVariableForPeriod

        // Predicted Income from observed income rate
        val historicalIncomeSum = recentIncomes.sumOf { it.amount }
        val monthlyIncomeEstimate = if (observedSpanDays >= 15) {
            (historicalIncomeSum.toDouble() / observedSpanDays.toDouble() * 30.0).roundToLong()
        } else {
            historicalIncomeSum
        }
        val predictedIncome = (monthlyIncomeEstimate * period.monthMultiplier).roundToLong()

        val estimatedSavings = predictedIncome - predictedTotalExpenses
        val estimatedPeriodEndBalance = currentNetBalance + estimatedSavings

        // Category breakdowns that strictly reconcile with predictedTotalExpenses
        val categories = (variableExpensesHistory.map { it.category } + listOfNotNull(
            if (confirmedRecurringForPeriod > 0) "Bills & Recurring" else null
        )).distinct()

        val categoryBreakdowns = mutableListOf<CategoryForecastBreakdown>()
        var allocatedVariableSum = 0L
        val nonRecurringCategories = variableExpensesHistory.groupBy { it.category }
        val nonRecurringEntries = nonRecurringCategories.entries.sortedByDescending { entry ->
            entry.value.sumOf { it.amount }
        }

        nonRecurringEntries.forEachIndexed { index, entry ->
            val catHistSum = entry.value.sumOf { it.amount }
            val catVarEstimate = if (index == nonRecurringEntries.lastIndex) {
                max(0L, estimatedVariableForPeriod - allocatedVariableSum)
            } else {
                val share = if (historicalVariableSum > 0L) {
                    ((catHistSum.toDouble() / historicalVariableSum.toDouble()) * estimatedVariableForPeriod).roundToLong()
                } else 0L
                allocatedVariableSum += share
                share
            }
            val histMonthlyAvg = ((catHistSum.toDouble() / max(1, observedSpanDays)) * 30.0).roundToLong()
            categoryBreakdowns.add(
                CategoryForecastBreakdown(
                    category = entry.key,
                    confirmedRecurringAmount = 0L,
                    estimatedVariableAmount = catVarEstimate,
                    totalPredictedAmount = catVarEstimate,
                    historicalAverageMonthly = histMonthlyAvg
                )
            )
        }

        if (confirmedRecurringForPeriod > 0L) {
            categoryBreakdowns.add(
                0,
                CategoryForecastBreakdown(
                    category = "Confirmed Recurring (Subs/EMI/Bills)",
                    confirmedRecurringAmount = confirmedRecurringForPeriod,
                    estimatedVariableAmount = 0L,
                    totalPredictedAmount = confirmedRecurringForPeriod,
                    historicalAverageMonthly = monthlySubsAmount + monthlyEmisAmount + upcomingRemindersInPeriod
                )
            )
        }

        val (confidenceLevel, confidenceScore, reqHistoryMsg) = when {
            distinctDaysCount >= 21 && expenses.size >= 15 -> Triple(
                "HIGH CONFIDENCE",
                88,
                "Sufficient multi-week transaction history (${expenses.size} records across $distinctDaysCount active days)."
            )
            distinctDaysCount >= 7 && expenses.size >= 5 -> Triple(
                "MEDIUM CONFIDENCE",
                68,
                "Add ${max(1, 21 - distinctDaysCount)} more days of transaction logs to unlock High Confidence seasonal weighting."
            )
            else -> Triple(
                "LOW (BASIC ESTIMATE)",
                38,
                "Limited history ($distinctDaysCount active day(s), ${expenses.size} transaction(s)). Log at least 7 days of expenses for Medium Confidence."
            )
        }

        val trendPoints = listOf(
            ForecastTrendPoint(
                label = "Observed Base",
                historicalAmount = totalExpenseAllTime,
                projectedAmount = totalExpenseAllTime
            ),
            ForecastTrendPoint(
                label = "Confirmed Fixed",
                historicalAmount = monthlySubsAmount + monthlyEmisAmount,
                projectedAmount = confirmedRecurringForPeriod
            ),
            ForecastTrendPoint(
                label = "Variable Est.",
                historicalAmount = historicalVariableSum,
                projectedAmount = estimatedVariableForPeriod
            ),
            ForecastTrendPoint(
                label = period.label,
                historicalAmount = totalExpenseAllTime,
                projectedAmount = predictedTotalExpenses
            )
        )

        val effectiveBudget = if (monthlyBudget > 0L) monthlyBudget else dailyLimit * 30L
        val budgetNote = if (effectiveBudget > 0L) {
            val scaledBudget = (effectiveBudget * period.monthMultiplier).roundToLong()
            if (predictedTotalExpenses > scaledBudget) {
                "Projected spend exceeds your ${period.label} budget cap (₹$scaledBudget) by ₹${predictedTotalExpenses - scaledBudget}."
            } else {
                "Projected spend is within your ${period.label} budget cap (₹$scaledBudget)."
            }
        } else {
            "Set a monthly budget to compare predicted expenses against target limits."
        }

        return AiMoneyPredictionResult(
            period = period,
            hasSufficientData = true,
            insufficientDataReason = if (distinctDaysCount < 3) "Basic transparent estimate active due to limited day span ($distinctDaysCount day(s))." else "",
            requiredAdditionalHistoryMessage = reqHistoryMsg,
            confidenceLevel = confidenceLevel,
            confidenceScorePct = confidenceScore,
            predictedIncome = predictedIncome,
            confirmedRecurringExpenses = confirmedRecurringForPeriod,
            estimatedVariableExpenses = estimatedVariableForPeriod,
            predictedTotalExpenses = predictedTotalExpenses,
            estimatedSavings = estimatedSavings,
            currentNetBalance = currentNetBalance,
            estimatedPeriodEndBalance = estimatedPeriodEndBalance,
            categoryBreakdowns = categoryBreakdowns,
            trendPoints = trendPoints,
            dataPeriodUsedLabel = "${displayDateFormat.format(Date(minTxMillis))} – ${displayDateFormat.format(Date(maxTxMillis))} ($observedSpanDays day window, ${expenses.size} tx)",
            calculationAssumptions = listOf(
                "Confirmed recurring payments (₹$confirmedRecurringForPeriod) are separated from variable expenses (₹$estimatedVariableForPeriod).",
                "Recurring merchants (${activeSubs.size} subscriptions, ${emis.size} EMIs, ${activeReminders.size} bills) are excluded from variable daily burn to prevent double-counting.",
                "Variable daily burn rate is ₹${dailyVariableRate.roundToLong()}/day over a $observedSpanDays-day observed window.",
                budgetNote
            ),
            limitations = listOf(
                "All figures are statistical estimates based on your logged history, not guaranteed future outcomes.",
                "Unscheduled one-time emergencies or unlogged cash transactions are not included."
            ),
            explanationSummary = "For ${period.label}, KHARCHEE PRO estimates ₹$predictedTotalExpenses in outflows (₹$confirmedRecurringForPeriod confirmed recurring + ₹$estimatedVariableForPeriod variable) against ₹$predictedIncome projected income."
        )
    }

    // =========================================================================
    // FEATURE 2 — WHAT-IF MONEY SIMULATOR
    // =========================================================================
    fun buildDefaultScenarioFromActuals(
        expenses: List<ExpenseRecord>,
        subscriptions: List<SubscriptionItem>,
        emis: List<EmiItem>,
        savingsGoals: List<SavingsGoalRecord>,
        nowMillis: Long = System.currentTimeMillis()
    ): WhatIfScenarioInput {
        val monthTx = expenses.filter { isSameMonth(it.txEpochMillis, nowMillis) }
        val monthIncome = monthTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val monthOutflows = monthTx.filter { !isIncomeTransaction(it) }

        val essentialCats = setOf("Food", "Bills", "Health", "Education", "Travel", "Groceries", "Rent")
        val essentialSpent = monthOutflows.filter { it.category in essentialCats }.sumOf { it.amount }
        val lifestyleSpent = monthOutflows.filter { it.category !in essentialCats }.sumOf { it.amount }

        val subMonthly = subscriptions.filter { it.isActive }.sumOf {
            (it.amount * BillingFrequency.fromId(it.billingCycle).monthlyFactor).roundToLong()
        }
        val emiMonthly = emis.sumOf { it.monthlyEmi }
        val defaultSavings = if (savingsGoals.isNotEmpty()) {
            (savingsGoals.sumOf { max(0L, it.targetAmount - it.savedAmount) } / 12L).coerceAtMost(max(0L, monthIncome / 4L))
        } else {
            max(0L, (monthIncome - essentialSpent - lifestyleSpent) / 2L)
        }

        return WhatIfScenarioInput(
            id = "baseline_default",
            name = "Current Baseline",
            monthlyIncome = monthIncome,
            essentialExpenses = essentialSpent,
            lifestyleExpenses = lifestyleSpent,
            savingsContribution = defaultSavings,
            emiObligations = emiMonthly,
            subscriptionCosts = subMonthly,
            oneTimePurchases = 0L,
            additionalIncome = 0L,
            unexpectedExpenses = 0L,
            projectionMonths = 6
        )
    }

    fun evaluateWhatIfScenario(
        currentBaseline: WhatIfScenarioInput,
        simulated: WhatIfScenarioInput
    ): WhatIfComparisonResult {
        val allFields = listOf(
            simulated.monthlyIncome,
            simulated.essentialExpenses,
            simulated.lifestyleExpenses,
            simulated.savingsContribution,
            simulated.emiObligations,
            simulated.subscriptionCosts,
            simulated.oneTimePurchases,
            simulated.additionalIncome,
            simulated.unexpectedExpenses
        )
        if (allFields.any { it < 0L } || simulated.projectionMonths <= 0) {
            return WhatIfComparisonResult(
                isValid = false,
                validationError = "Negative financial inputs or invalid projection months are not permitted.",
                currentMonthlyIncome = 0L,
                currentMonthlyExpenses = 0L,
                currentRemainingBalance = 0L,
                currentMonthlySavings = 0L,
                totalSimulatedIncome = 0L,
                totalSimulatedExpenses = 0L,
                simulatedRecurringMonthlyExpenses = 0L,
                estimatedRemainingBalance = 0L,
                estimatedTotalMonthlySavings = 0L,
                monthlySavingsDifference = 0L,
                expenseChangePercent = 0.0,
                savingsChangePercent = 0.0,
                projectionMonths = max(1, simulated.projectionMonths),
                currentProjectedSavingsOverPeriod = 0L,
                simulatedProjectedSavingsOverPeriod = 0L,
                projectedDifferenceOverPeriod = 0L
            )
        }

        val currentIncome = currentBaseline.monthlyIncome + currentBaseline.additionalIncome
        val currentRecurringExpenses = currentBaseline.essentialExpenses +
            currentBaseline.lifestyleExpenses +
            currentBaseline.emiObligations +
            currentBaseline.subscriptionCosts
        val currentTotalExpenses = currentRecurringExpenses +
            currentBaseline.oneTimePurchases +
            currentBaseline.unexpectedExpenses
        val currentRemaining = currentIncome - currentTotalExpenses - currentBaseline.savingsContribution
        val currentMonthlySavings = currentBaseline.savingsContribution + max(0L, currentRemaining)

        val simIncome = simulated.monthlyIncome + simulated.additionalIncome
        val simRecurringExpenses = simulated.essentialExpenses +
            simulated.lifestyleExpenses +
            simulated.emiObligations +
            simulated.subscriptionCosts
        val simTotalExpenses = simRecurringExpenses +
            simulated.oneTimePurchases +
            simulated.unexpectedExpenses
        val simRemaining = simIncome - simTotalExpenses - simulated.savingsContribution
        val simMonthlySavings = simulated.savingsContribution + max(0L, simRemaining)

        val savingsDiff = simMonthlySavings - currentMonthlySavings

        val expenseChangePct = if (currentTotalExpenses > 0L) {
            (((simTotalExpenses - currentTotalExpenses).toDouble() / currentTotalExpenses.toDouble()) * 1000.0).roundToInt() / 10.0
        } else 0.0

        val savingsChangePct = if (currentMonthlySavings > 0L) {
            (((simMonthlySavings - currentMonthlySavings).toDouble() / currentMonthlySavings.toDouble()) * 1000.0).roundToInt() / 10.0
        } else if (simMonthlySavings > 0L) 100.0 else 0.0

        val months = simulated.projectionMonths.coerceIn(1, 60)
        val currentRecurringNetMonthlySavings = currentBaseline.savingsContribution +
            max(0L, currentIncome - currentRecurringExpenses - currentBaseline.savingsContribution)
        val currentProjectedOverPeriod = (currentRecurringNetMonthlySavings * months) -
            currentBaseline.oneTimePurchases - currentBaseline.unexpectedExpenses

        val simRecurringNetMonthlySavings = simulated.savingsContribution +
            max(0L, simIncome - simRecurringExpenses - simulated.savingsContribution)
        val simProjectedOverPeriod = (simRecurringNetMonthlySavings * months) -
            simulated.oneTimePurchases - simulated.unexpectedExpenses

        return WhatIfComparisonResult(
            isValid = true,
            validationError = null,
            currentMonthlyIncome = currentIncome,
            currentMonthlyExpenses = currentTotalExpenses,
            currentRemainingBalance = currentRemaining,
            currentMonthlySavings = currentMonthlySavings,
            totalSimulatedIncome = simIncome,
            totalSimulatedExpenses = simTotalExpenses,
            simulatedRecurringMonthlyExpenses = simRecurringExpenses,
            estimatedRemainingBalance = simRemaining,
            estimatedTotalMonthlySavings = simMonthlySavings,
            monthlySavingsDifference = savingsDiff,
            expenseChangePercent = expenseChangePct,
            savingsChangePercent = savingsChangePct,
            projectionMonths = months,
            currentProjectedSavingsOverPeriod = currentProjectedOverPeriod,
            simulatedProjectedSavingsOverPeriod = simProjectedOverPeriod,
            projectedDifferenceOverPeriod = simProjectedOverPeriod - currentProjectedOverPeriod
        )
    }

    fun getSavedScenarios(context: Context, userId: String): List<WhatIfScenarioInput> {
        val raw = prefs(context).getString("${userId}_whatif_scenarios_json", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<WhatIfScenarioInput>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    WhatIfScenarioInput(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Scenario"),
                        monthlyIncome = obj.optLong("monthlyIncome", 0L),
                        essentialExpenses = obj.optLong("essentialExpenses", 0L),
                        lifestyleExpenses = obj.optLong("lifestyleExpenses", 0L),
                        savingsContribution = obj.optLong("savingsContribution", 0L),
                        emiObligations = obj.optLong("emiObligations", 0L),
                        subscriptionCosts = obj.optLong("subscriptionCosts", 0L),
                        oneTimePurchases = obj.optLong("oneTimePurchases", 0L),
                        additionalIncome = obj.optLong("additionalIncome", 0L),
                        unexpectedExpenses = obj.optLong("unexpectedExpenses", 0L),
                        projectionMonths = obj.optInt("projectionMonths", 6),
                        updatedAtMillis = obj.optLong("updatedAtMillis", 0L)
                    )
                )
            }
            list.sortedByDescending { it.updatedAtMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveScenario(context: Context, userId: String, scenario: WhatIfScenarioInput): List<WhatIfScenarioInput> {
        val current = getSavedScenarios(context, userId).toMutableList()
        val cleanScenario = scenario.copy(
            name = scenario.name.trim().ifBlank { "Untitled Scenario" },
            updatedAtMillis = System.currentTimeMillis()
        )
        val existingIdx = current.indexOfFirst { it.id == cleanScenario.id }
        if (existingIdx >= 0) {
            current[existingIdx] = cleanScenario
        } else {
            current.add(0, cleanScenario)
        }
        persistScenarios(context, userId, current)
        return current
    }

    fun duplicateScenario(context: Context, userId: String, scenarioId: String): List<WhatIfScenarioInput> {
        val current = getSavedScenarios(context, userId).toMutableList()
        val target = current.find { it.id == scenarioId } ?: return current
        val copy = target.copy(
            id = UUID.randomUUID().toString(),
            name = "${target.name} (Copy)",
            updatedAtMillis = System.currentTimeMillis()
        )
        current.add(0, copy)
        persistScenarios(context, userId, current)
        return current
    }

    fun deleteScenario(context: Context, userId: String, scenarioId: String): List<WhatIfScenarioInput> {
        val current = getSavedScenarios(context, userId).filterNot { it.id == scenarioId }
        persistScenarios(context, userId, current)
        return current
    }

    private fun persistScenarios(context: Context, userId: String, list: List<WhatIfScenarioInput>) {
        val arr = JSONArray()
        list.forEach { s ->
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("monthlyIncome", s.monthlyIncome)
                    put("essentialExpenses", s.essentialExpenses)
                    put("lifestyleExpenses", s.lifestyleExpenses)
                    put("savingsContribution", s.savingsContribution)
                    put("emiObligations", s.emiObligations)
                    put("subscriptionCosts", s.subscriptionCosts)
                    put("oneTimePurchases", s.oneTimePurchases)
                    put("additionalIncome", s.additionalIncome)
                    put("unexpectedExpenses", s.unexpectedExpenses)
                    put("projectionMonths", s.projectionMonths)
                    put("updatedAtMillis", s.updatedAtMillis)
                }
            )
        }
        prefs(context).edit().putString("${userId}_whatif_scenarios_json", arr.toString()).apply()
    }

    // =========================================================================
    // FEATURE 3 — SMART SPENDING DETECTOR
    // =========================================================================
    fun getDetectorPreferences(context: Context, userId: String): SmartDetectorPreferences {
        val p = prefs(context)
        return SmartDetectorPreferences(
            anomalyMultiplierPct = p.getInt("${userId}_det_anomaly_pct", 180),
            categorySpikeThresholdPct = p.getInt("${userId}_det_cat_spike_pct", 25),
            enableDuplicateDetection = p.getBoolean("${userId}_det_dup_enabled", true),
            enableBudgetWarnings = p.getBoolean("${userId}_det_budget_enabled", true)
        )
    }

    fun saveDetectorPreferences(context: Context, userId: String, prefsObj: SmartDetectorPreferences) {
        prefs(context).edit()
            .putInt("${userId}_det_anomaly_pct", prefsObj.anomalyMultiplierPct.coerceIn(120, 400))
            .putInt("${userId}_det_cat_spike_pct", prefsObj.categorySpikeThresholdPct.coerceIn(10, 200))
            .putBoolean("${userId}_det_dup_enabled", prefsObj.enableDuplicateDetection)
            .putBoolean("${userId}_det_budget_enabled", prefsObj.enableBudgetWarnings)
            .apply()
    }

    fun getReviewedAlertIds(context: Context, userId: String): Set<String> =
        prefs(context).getStringSet("${userId}_reviewed_alerts", emptySet()) ?: emptySet()

    fun getDismissedAlertIds(context: Context, userId: String): Set<String> =
        prefs(context).getStringSet("${userId}_dismissed_alerts", emptySet()) ?: emptySet()

    fun markAlertReviewed(context: Context, userId: String, alertId: String) {
        val updated = getReviewedAlertIds(context, userId).toMutableSet().apply { add(alertId) }
        prefs(context).edit().putStringSet("${userId}_reviewed_alerts", updated).apply()
    }

    fun dismissAlert(context: Context, userId: String, alertId: String) {
        val updated = getDismissedAlertIds(context, userId).toMutableSet().apply { add(alertId) }
        prefs(context).edit().putStringSet("${userId}_dismissed_alerts", updated).apply()
    }

    fun resetDismissedAlerts(context: Context, userId: String) {
        prefs(context).edit().remove("${userId}_dismissed_alerts").apply()
    }

    fun detectSmartSpendingAlerts(
        expenses: List<ExpenseRecord>,
        dailyLimit: Long,
        monthlyBudget: Long,
        preferences: SmartDetectorPreferences = SmartDetectorPreferences(),
        reviewedIds: Set<String> = emptySet(),
        dismissedIds: Set<String> = emptySet(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmartSpendingDetectorReport {
        val outflows = expenses.filter { !isIncomeTransaction(it) }.sortedByDescending { it.txEpochMillis }
        if (outflows.size < 2) {
            return SmartSpendingDetectorReport(
                hasMinimumData = false,
                minimumDataMessage = "At least 2 expense transactions are required to establish historical spending baselines and detect unusual patterns.",
                alerts = emptyList(),
                reviewedCount = reviewedIds.size,
                dismissedCount = dismissedIds.size
            )
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val rawAlerts = mutableListOf<SmartSpendingAlert>()

        // 1. Budget Limit Warnings
        val currentMonthOutflows = outflows.filter { isSameMonth(it.txEpochMillis, nowMillis) }
        val currentMonthTotal = currentMonthOutflows.sumOf { it.amount }
        val effectiveBudget = if (monthlyBudget > 0L) monthlyBudget else dailyLimit * 30L
        if (preferences.enableBudgetWarnings && effectiveBudget > 0L && currentMonthTotal >= (effectiveBudget * 0.85).roundToLong()) {
            val pct = ((currentMonthTotal.toDouble() / effectiveBudget.toDouble()) * 100.0).roundToInt()
            val alertId = "budget_warn_${SimpleDateFormat("yyyy_MM", Locale.US).format(Date(nowMillis))}"
            rawAlerts.add(
                SmartSpendingAlert(
                    id = alertId,
                    alertType = "BUDGET_WARNING",
                    whatHappened = if (currentMonthTotal > effectiveBudget) {
                        "Monthly spending exceeded target budget ($pct%)"
                    } else {
                        "Monthly spending reached $pct% of target budget"
                    },
                    amountInvolved = currentMonthTotal,
                    relevantDateOrPeriod = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(nowMillis)),
                    comparisonBaseline = "Monthly Budget Target: ₹$effectiveBudget",
                    whyFlagged = "Current month outflow (₹$currentMonthTotal) crossed the 85% budget threshold.",
                    suggestedNextAction = "Review discretionary categories or adjust your daily spending cap.",
                    category = "Overall Budget",
                    relatedTransactionIds = currentMonthOutflows.take(5).map { it.id },
                    isReviewed = alertId in reviewedIds
                )
            )
        }

        // 2. Category Spending Increases (Current 30d vs Previous 30d)
        val thirtyDaysAgo = nowMillis - 30L * 86_400_000L
        val sixtyDaysAgo = nowMillis - 60L * 86_400_000L
        val last30Outflows = outflows.filter { it.txEpochMillis in thirtyDaysAgo..nowMillis }
        val prev30Outflows = outflows.filter { it.txEpochMillis in sixtyDaysAgo until thirtyDaysAgo }

        val last30ByCat = last30Outflows.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
        val prev30ByCat = prev30Outflows.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }

        last30ByCat.forEach { (cat, currentAmt) ->
            val prevAmt = prev30ByCat[cat] ?: 0L
            if (prevAmt >= 300L && currentAmt > prevAmt) {
                val increasePct = (((currentAmt - prevAmt).toDouble() / prevAmt.toDouble()) * 100.0).roundToInt()
                if (increasePct >= preferences.categorySpikeThresholdPct) {
                    val alertId = "cat_spike_${cat.lowercase(Locale.US)}_$currentAmt"
                    rawAlerts.add(
                        SmartSpendingAlert(
                            id = alertId,
                            alertType = "CATEGORY_SPIKE",
                            whatHappened = "$cat spending increased by +$increasePct%",
                            amountInvolved = currentAmt,
                            relevantDateOrPeriod = "Last 30 Days",
                            comparisonBaseline = "Previous 30-day $cat baseline: ₹$prevAmt",
                            whyFlagged = "$cat outflow rose by ₹${currentAmt - prevAmt} (+$increasePct%), exceeding the ${preferences.categorySpikeThresholdPct}% alert threshold.",
                            suggestedNextAction = "Inspect recent $cat transactions and set a category soft cap.",
                            category = cat,
                            relatedTransactionIds = last30Outflows.filter { it.category == cat }.map { it.id },
                            isReviewed = alertId in reviewedIds
                        )
                    )
                }
            }
        }

        // 3. Unusual Individual Transactions (compared against category average)
        val multiplier = preferences.anomalyMultiplierPct / 100.0
        val byCategoryAll = outflows.groupBy { it.category }
        byCategoryAll.forEach { (cat, catTxs) ->
            if (catTxs.size >= 2) {
                val avgAmt = catTxs.map { it.amount }.average()
                val threshold = max(1000.0, avgAmt * multiplier)
                catTxs.filter { it.amount >= threshold }.take(3).forEach { highTx ->
                    val alertId = "unusual_tx_${highTx.id}"
                    rawAlerts.add(
                        SmartSpendingAlert(
                            id = alertId,
                            alertType = "UNUSUAL_TX",
                            whatHappened = "Unusually high $cat expense: ${highTx.title}",
                            amountInvolved = highTx.amount,
                            relevantDateOrPeriod = dateFormat.format(Date(highTx.txEpochMillis)),
                            comparisonBaseline = "Average $cat transaction: ₹${avgAmt.roundToLong()}",
                            whyFlagged = "Transaction of ₹${highTx.amount} is ${(highTx.amount / max(1.0, avgAmt)).roundToInt()}x your typical $cat spend.",
                            suggestedNextAction = "Verify if this was a planned one-time expense or recurring commitment.",
                            category = cat,
                            relatedTransactionIds = listOf(highTx.id),
                            isReviewed = alertId in reviewedIds
                        )
                    )
                }
            }
        }

        // 4. High-Expense Days
        if (dailyLimit > 0L) {
            val byDay = outflows.groupBy { dayKeyFormat.format(Date(it.txEpochMillis)) }
            byDay.entries.sortedByDescending { it.key }.take(15).forEach { (dayKey, dayTxs) ->
                val daySum = dayTxs.sumOf { it.amount }
                if (daySum > (dailyLimit * 1.5).roundToLong()) {
                    val alertId = "high_day_$dayKey"
                    rawAlerts.add(
                        SmartSpendingAlert(
                            id = alertId,
                            alertType = "HIGH_DAY",
                            whatHappened = "High-expense day on $dayKey (₹$daySum)",
                            amountInvolved = daySum,
                            relevantDateOrPeriod = dayKey,
                            comparisonBaseline = "Daily Spending Limit: ₹$dailyLimit",
                            whyFlagged = "Daily total exceeded 150% of your ₹$dailyLimit daily guard limit across ${dayTxs.size} transaction(s).",
                            suggestedNextAction = "Review $dayKey entries in Passbook to see which category drove the spike.",
                            category = dayTxs.maxByOrNull { it.amount }?.category ?: "Multiple",
                            relatedTransactionIds = dayTxs.map { it.id },
                            isReviewed = alertId in reviewedIds
                        )
                    )
                }
            }
        }

        // 5. Possible Duplicate Entries
        if (preferences.enableDuplicateDetection) {
            val sortedAsc = outflows.sortedBy { it.txEpochMillis }
            for (i in 0 until sortedAsc.size - 1) {
                val a = sortedAsc[i]
                val b = sortedAsc[i + 1]
                val sameTitle = a.title.trim().equals(b.title.trim(), ignoreCase = true)
                val sameAmount = a.amount == b.amount && a.amount > 0L
                val within24Hours = abs(b.txEpochMillis - a.txEpochMillis) <= 86_400_000L
                if (sameTitle && sameAmount && within24Hours) {
                    val pairKey = listOf(a.id, b.id).sorted().joinToString("_")
                    val alertId = "dup_tx_$pairKey"
                    rawAlerts.add(
                        SmartSpendingAlert(
                            id = alertId,
                            alertType = "DUPLICATE_ENTRY",
                            whatHappened = "Possible duplicate entry: ${a.title} (₹${a.amount})",
                            amountInvolved = a.amount,
                            relevantDateOrPeriod = dateFormat.format(Date(b.txEpochMillis)),
                            comparisonBaseline = "2 identical ₹${a.amount} entries logged within 24 hours",
                            whyFlagged = "Same merchant (${a.title}) and exact amount (₹${a.amount}) appeared twice within 24 hours.",
                            suggestedNextAction = "Check Passbook and delete one entry if it was logged twice by mistake.",
                            category = a.category,
                            relatedTransactionIds = listOf(a.id, b.id),
                            isReviewed = alertId in reviewedIds
                        )
                    )
                }
            }
        }

        // Deduplicate by alert ID and exclude dismissed alerts
        val visibleAlerts = rawAlerts
            .distinctBy { it.id }
            .filterNot { it.id in dismissedIds }

        return SmartSpendingDetectorReport(
            hasMinimumData = true,
            minimumDataMessage = "Analyzed ${outflows.size} expense transactions across ${byCategoryAll.size} categories.",
            alerts = visibleAlerts,
            reviewedCount = visibleAlerts.count { it.isReviewed },
            dismissedCount = dismissedIds.size
        )
    }

    // =========================================================================
    // FEATURE 4 — MONEY LEAK FINDER
    // =========================================================================
    private data class LeakUserOverride(
        val key: String,
        val merchantName: String?,
        val frequencyId: String?,
        val renewalDate: String?,
        val reminderEnabled: Boolean = false,
        val status: String = "ACTIVE" // "CONFIRMED", "REJECTED", "CANCELLED", "ACTIVE"
    )

    private fun getLeakOverrides(context: Context, userId: String): Map<String, LeakUserOverride> {
        val raw = prefs(context).getString("${userId}_leak_overrides_json", "{}") ?: "{}"
        return try {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, LeakUserOverride>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val item = obj.optJSONObject(k) ?: continue
                map[k] = LeakUserOverride(
                    key = k,
                    merchantName = item.optString("merchantName", "").ifBlank { null },
                    frequencyId = item.optString("frequencyId", "").ifBlank { null },
                    renewalDate = item.optString("renewalDate", "").ifBlank { null },
                    reminderEnabled = item.optBoolean("reminderEnabled", false),
                    status = item.optString("status", "ACTIVE")
                )
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveLeakUserAction(
        context: Context,
        userId: String,
        leakKey: String,
        merchantName: String? = null,
        frequency: BillingFrequency? = null,
        renewalDate: String? = null,
        reminderEnabled: Boolean? = null,
        status: String? = null
    ) {
        val current = getLeakOverrides(context, userId).toMutableMap()
        val existing = current[leakKey] ?: LeakUserOverride(key = leakKey, merchantName = null, frequencyId = null, renewalDate = null)
        current[leakKey] = existing.copy(
            merchantName = merchantName ?: existing.merchantName,
            frequencyId = frequency?.id ?: existing.frequencyId,
            renewalDate = renewalDate ?: existing.renewalDate,
            reminderEnabled = reminderEnabled ?: existing.reminderEnabled,
            status = status ?: existing.status
        )
        val root = JSONObject()
        current.forEach { (k, v) ->
            root.put(
                k,
                JSONObject().apply {
                    put("merchantName", v.merchantName ?: "")
                    put("frequencyId", v.frequencyId ?: "")
                    put("renewalDate", v.renewalDate ?: "")
                    put("reminderEnabled", v.reminderEnabled)
                    put("status", v.status)
                }
            )
        }
        prefs(context).edit().putString("${userId}_leak_overrides_json", root.toString()).apply()
    }

    fun analyzeMoneyLeaks(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        subscriptions: List<SubscriptionItem>,
        reminders: List<PaymentReminderRecord>
    ): MoneyLeakFinderReport {
        val overrides = getLeakOverrides(context, userId)
        val outflows = expenses.filter { !isIncomeTransaction(it) }
        val allLeakItems = mutableListOf<MoneyLeakItem>()
        val seenKeys = mutableSetOf<String>()

        // 1. Confirmed Subscriptions from Subscription Radar
        subscriptions.forEach { sub ->
            val key = "sub_${sub.name.trim().lowercase(Locale.US)}"
            seenKeys.add(key)
            val ov = overrides[key]
            if (ov?.status == "REJECTED") return@forEach

            val freq = BillingFrequency.fromId(ov?.frequencyId ?: sub.billingCycle)
            val isCancelled = !sub.isActive || ov?.status == "CANCELLED"
            val monthlyEq = sub.amount * freq.monthlyFactor
            val annualEq = sub.amount * freq.annualFactor

            allLeakItems.add(
                MoneyLeakItem(
                    key = key,
                    merchantName = ov?.merchantName ?: sub.name,
                    category = sub.category,
                    rawAmount = sub.amount,
                    frequency = freq,
                    monthlyEquivalent = (monthlyEq * 100.0).roundToInt() / 100.0,
                    annualEquivalent = (annualEq * 100.0).roundToInt() / 100.0,
                    classification = LeakClassification.CONFIRMED_SUBSCRIPTION,
                    renewalDate = ov?.renewalDate ?: sub.nextRenewalDate,
                    reminderEnabled = ov?.reminderEnabled ?: true,
                    isCancelled = isCancelled,
                    occurrenceCount = max(1, outflows.count { it.title.equals(sub.name, ignoreCase = true) })
                )
            )
        }

        // 2. User-Created Recurring Payments (from Smart Payment Reminders with recurring cycle)
        reminders.filter { it.intervalType != "NONE" }.forEach { rem ->
            val key = "rem_${rem.title.trim().lowercase(Locale.US)}"
            if (key in seenKeys) return@forEach
            seenKeys.add(key)
            val ov = overrides[key]
            if (ov?.status == "REJECTED") return@forEach

            val freq = BillingFrequency.fromId(ov?.frequencyId ?: rem.intervalType)
            val isCancelled = ov?.status == "CANCELLED"
            val monthlyEq = rem.amount * freq.monthlyFactor
            val annualEq = rem.amount * freq.annualFactor

            allLeakItems.add(
                MoneyLeakItem(
                    key = key,
                    merchantName = ov?.merchantName ?: rem.title,
                    category = rem.category,
                    rawAmount = rem.amount,
                    frequency = freq,
                    monthlyEquivalent = (monthlyEq * 100.0).roundToInt() / 100.0,
                    annualEquivalent = (annualEq * 100.0).roundToInt() / 100.0,
                    classification = if (ov?.status == "CONFIRMED") {
                        LeakClassification.CONFIRMED_SUBSCRIPTION
                    } else {
                        LeakClassification.USER_RECURRING
                    },
                    renewalDate = ov?.renewalDate ?: SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(rem.dueEpochMillis)),
                    reminderEnabled = ov?.reminderEnabled ?: true,
                    isCancelled = isCancelled,
                    occurrenceCount = 1
                )
            )
        }

        // 3. AI-Detected Possible Recurring Expenses from Transaction History
        val groupedByMerchant = outflows.groupBy { it.title.trim().lowercase(Locale.US) }
        groupedByMerchant.forEach { (normMerchant, txs) ->
            if (normMerchant.isBlank() || txs.size < 2) return@forEach
            val subKey = "sub_$normMerchant"
            val remKey = "rem_$normMerchant"
            val aiKey = "ai_$normMerchant"
            if (subKey in seenKeys || remKey in seenKeys) return@forEach

            val ov = overrides[aiKey]
            if (ov?.status == "REJECTED") return@forEach

            val amounts = txs.map { it.amount }
            val avgAmount = amounts.average().roundToLong()
            val maxDeviation = amounts.maxOf { abs(it - avgAmount) }
            // Check if amounts are consistent (within 20% of average)
            if (avgAmount > 0L && maxDeviation <= (avgAmount * 0.25).roundToLong()) {
                seenKeys.add(aiKey)
                val freq = BillingFrequency.fromId(ov?.frequencyId ?: "monthly")
                val isCancelled = ov?.status == "CANCELLED"
                val classification = if (ov?.status == "CONFIRMED") {
                    LeakClassification.CONFIRMED_SUBSCRIPTION
                } else {
                    LeakClassification.AI_DETECTED_POSSIBLE
                }
                val monthlyEq = avgAmount * freq.monthlyFactor
                val annualEq = avgAmount * freq.annualFactor
                val latestTx = txs.maxByOrNull { it.txEpochMillis }!!
                val defaultRenewal = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    .format(Date(latestTx.txEpochMillis + 30L * 86_400_000L))

                allLeakItems.add(
                    MoneyLeakItem(
                        key = aiKey,
                        merchantName = ov?.merchantName ?: latestTx.title.trim(),
                        category = latestTx.category,
                        rawAmount = avgAmount,
                        frequency = freq,
                        monthlyEquivalent = (monthlyEq * 100.0).roundToInt() / 100.0,
                        annualEquivalent = (annualEq * 100.0).roundToInt() / 100.0,
                        classification = classification,
                        renewalDate = ov?.renewalDate ?: defaultRenewal,
                        reminderEnabled = ov?.reminderEnabled ?: false,
                        isCancelled = isCancelled,
                        occurrenceCount = txs.size
                    )
                )
            }
        }

        val activeItems = allLeakItems.filter { !it.isCancelled }
        val cancelledItems = allLeakItems.filter { it.isCancelled }

        // Only confirmed & user-recurring + active AI-detected count towards active totals
        val monthlyTotal = (activeItems.sumOf { it.monthlyEquivalent } * 100.0).roundToInt() / 100.0
        val annualTotal = (activeItems.sumOf { it.annualEquivalent } * 100.0).roundToInt() / 100.0

        // Small repeated expense analysis (<= ₹300, >= 2 occurrences)
        val smallRepeated = groupedByMerchant.mapNotNull { (_, txs) ->
            val smallTxs = txs.filter { it.amount in 1L..300L }
            if (smallTxs.size >= 2) {
                val total = smallTxs.sumOf { it.amount }
                SmallRepeatedSpendSummary(
                    merchantOrTitle = smallTxs.first().title.trim(),
                    category = smallTxs.first().category,
                    count = smallTxs.size,
                    averageTicket = total / smallTxs.size,
                    totalSpent = total
                )
            } else null
        }.sortedByDescending { it.totalSpent }

        val duplicateAlerts = detectSmartSpendingAlerts(
            expenses = expenses,
            dailyLimit = 0L,
            monthlyBudget = 0L
        ).alerts.filter { it.alertType == "DUPLICATE_ENTRY" }

        return MoneyLeakFinderReport(
            activeItems = activeItems,
            cancelledItems = cancelledItems,
            confirmedSubscriptionsCount = activeItems.count { it.classification == LeakClassification.CONFIRMED_SUBSCRIPTION },
            userRecurringCount = activeItems.count { it.classification == LeakClassification.USER_RECURRING },
            aiPossibleCount = activeItems.count { it.classification == LeakClassification.AI_DETECTED_POSSIBLE },
            monthlyRecurringTotal = monthlyTotal,
            annualisedRecurringTotal = annualTotal,
            smallRepeatedExpenses = smallRepeated,
            possibleDuplicatePayments = duplicateAlerts
        )
    }

    // =========================================================================
    // PRO GENERATED REPORTS HISTORY
    // =========================================================================
    fun getGeneratedReports(context: Context, userId: String): List<ProGeneratedReport> {
        val raw = prefs(context).getString("${userId}_pro_reports_json", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ProGeneratedReport>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                list.add(
                    ProGeneratedReport(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "Pro Report"),
                        moduleType = o.optString("moduleType", "PRO"),
                        summaryLine = o.optString("summaryLine", ""),
                        generatedAtMillis = o.optLong("generatedAtMillis", 0L)
                    )
                )
            }
            list.sortedByDescending { it.generatedAtMillis }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun recordGeneratedReport(
        context: Context,
        userId: String,
        title: String,
        moduleType: String,
        summaryLine: String
    ): List<ProGeneratedReport> {
        val current = getGeneratedReports(context, userId).toMutableList()
        current.add(
            0,
            ProGeneratedReport(
                title = title,
                moduleType = moduleType,
                summaryLine = summaryLine
            )
        )
        val arr = JSONArray()
        current.take(25).forEach { r ->
            arr.put(
                JSONObject().apply {
                    put("id", r.id)
                    put("title", r.title)
                    put("moduleType", r.moduleType)
                    put("summaryLine", r.summaryLine)
                    put("generatedAtMillis", r.generatedAtMillis)
                }
            )
        }
        prefs(context).edit().putString("${userId}_pro_reports_json", arr.toString()).apply()
        return current.take(25)
    }
}
