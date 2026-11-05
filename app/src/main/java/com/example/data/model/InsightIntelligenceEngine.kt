package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class InsightPeriodFilter(val id: String, val label: String) {
    THIS_WEEK("this_week", "This Week"),
    THIS_MONTH("this_month", "This Month"),
    LAST_MONTH("last_month", "Last Month"),
    LAST_3_MONTHS("last_3_months", "Last 3 Months"),
    THIS_YEAR("this_year", "This Year"),
    CUSTOM("custom", "Custom Range")
}

enum class InsightSectionView(val id: String, val label: String) {
    OVERVIEW("overview", "Overview"),
    EARNINGS("earnings", "Earnings"),
    SPENDING("spending", "Spending"),
    CATEGORIES("categories", "Categories"),
    BUDGET("budget", "Budget"),
    SAVINGS("savings", "Savings"),
    REPORTS("reports", "Reports")
}

data class InsightTimeWindow(
    val filter: InsightPeriodFilter,
    val startMillis: Long,
    val endMillis: Long,
    val prevStartMillis: Long,
    val prevEndMillis: Long,
    val displayRangeLabel: String,
    val comparisonLabel: String,
    val durationDays: Int
)

data class TrendBarPoint(
    val label: String,
    val shortLabel: String,
    val amount: Long,
    val secondaryAmount: Long = 0L,
    val count: Int = 0,
    val startMillis: Long = 0L,
    val endMillis: Long = 0L
)

data class IncomeSourceItem(
    val sourceName: String,
    val amount: Long,
    val count: Int,
    val percentOfIncome: Int
)

data class InsightOverviewSummary(
    val totalIncome: Long,
    val totalExpenses: Long,
    val verifiedSavings: Long,
    val hasRecordedIncome: Boolean,
    val savingsRatePercent: Int,
    val monthlySpending: Long,
    val previousPeriodIncome: Long,
    val previousPeriodExpenses: Long,
    val previousPeriodSavings: Long,
    val incomeChangePercent: Int?,
    val expenseChangePercent: Int?,
    val monthOverMonthExpenseChangePercent: Int?,
    val transactionCount: Int
)

data class EarningsInsightsData(
    val totalIncomeReceived: Long,
    val previousPeriodIncome: Long,
    val incomeGrowthPercent: Int?,
    val sourceBreakdown: List<IncomeSourceItem>,
    val monthlyTrend: List<TrendBarPoint>,
    val weeklyTrend: List<TrendBarPoint>,
    val highestEarningPeriodLabel: String,
    val highestEarningPeriodAmount: Long,
    val incomeTransactionCount: Int
)

data class SpendingInsightsData(
    val totalSpendingPeriod: Long,
    val thisMonthSpending: Long,
    val lastMonthSpending: Long,
    val monthOverMonthChangePercent: Int?,
    val previousPeriodSpending: Long,
    val periodComparisonPercent: Int?,
    val averageDailySpending: Long,
    val highestSpendingDayLabel: String,
    val highestSpendingDayAmount: Long,
    val dailyTrend: List<TrendBarPoint>,
    val weeklyTrend: List<TrendBarPoint>,
    val monthlyTrend: List<TrendBarPoint>,
    val expenseTransactionCount: Int
)

data class InsightCategoryDetail(
    val category: String,
    val amount: Long,
    val previousAmount: Long,
    val changePercent: Int?,
    val transactionCount: Int,
    val percentOfTotal: Int,
    val transactions: List<ExpenseRecord>
)

data class CategoryInsightsData(
    val totalCategorizedSpend: Long,
    val topCategory: InsightCategoryDetail?,
    val categories: List<InsightCategoryDetail>
)

enum class BudgetStatusLevel {
    WITHIN_LIMIT,
    APPROACHING_LIMIT,
    EXCEEDED_LIMIT
}

data class CategoryBudgetProgressItem(
    val category: String,
    val budgetLimit: Long,
    val spentAmount: Long,
    val remainingAmount: Long,
    val overageAmount: Long,
    val utilizationPercent: Int,
    val status: BudgetStatusLevel
)

data class BudgetIntelligenceData(
    val totalMonthlyBudget: Long,
    val totalSpentThisMonth: Long,
    val remainingBudget: Long,
    val utilizationPercent: Int,
    val categoryProgress: List<CategoryBudgetProgressItem>,
    val approachingCategories: List<CategoryBudgetProgressItem>,
    val exceedingCategories: List<CategoryBudgetProgressItem>,
    val performanceHeadline: String,
    val performanceExplanation: String
)

data class HealthScoreFactor(
    val name: String,
    val earnedPoints: Int,
    val maxPoints: Int,
    val statusLabel: String,
    val explanation: String
)

data class FinancialHealthEvaluation(
    val hasSufficientData: Boolean,
    val score: Int,
    val category: String,
    val previousScore: Int?,
    val scoreChange: Int?,
    val lastUpdatedDate: String,
    val summaryExplanation: String,
    val factors: List<HealthScoreFactor>,
    val bureauDisclaimer: String = "KHARCHEE Internal Financial Wellness Index — based on recorded app activity, not a credit bureau score."
)

data class SavingsAnalyticsData(
    val hasRecordedIncome: Boolean,
    val verifiedPeriodSavings: Long,
    val verifiedMonthlySavings: Long,
    val savingsRatePercent: Int,
    val periodIncome: Long,
    val periodExpenses: Long,
    val previousPeriodSavings: Long,
    val savingsChangeAmount: Long,
    val totalGoalsSaved: Long,
    val totalGoalsTarget: Long,
    val goalsProgressPercent: Int,
    val monthlySavingsTrend: List<TrendBarPoint>,
    val explanationText: String
)

data class JarvisDataBackedInsight(
    val id: String,
    val categoryTag: String,
    val headline: String,
    val observation: String,
    val whyItAppeared: String,
    val periodLabel: String,
    val suggestedJarvisPrompt: String
)

data class CompleteInsightAnalyticsBundle(
    val timeWindow: InsightTimeWindow,
    val overview: InsightOverviewSummary,
    val earnings: EarningsInsightsData,
    val spending: SpendingInsightsData,
    val categories: CategoryInsightsData,
    val budget: BudgetIntelligenceData,
    val healthScore: FinancialHealthEvaluation,
    val savings: SavingsAnalyticsData,
    val jarvisInsights: List<JarvisDataBackedInsight>
)

object InsightIntelligenceEngine {

    private const val DAY_MS = 86_400_000L

    fun resolveTimeWindow(
        filter: InsightPeriodFilter,
        nowMillis: Long = System.currentTimeMillis(),
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): InsightTimeWindow {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val shortFmt = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }

        fun startOfDay(c: Calendar): Long {
            val copy = c.clone() as Calendar
            copy.set(Calendar.HOUR_OF_DAY, 0)
            copy.set(Calendar.MINUTE, 0)
            copy.set(Calendar.SECOND, 0)
            copy.set(Calendar.MILLISECOND, 0)
            return copy.timeInMillis
        }

        fun endOfDay(c: Calendar): Long {
            val copy = c.clone() as Calendar
            copy.set(Calendar.HOUR_OF_DAY, 23)
            copy.set(Calendar.MINUTE, 59)
            copy.set(Calendar.SECOND, 59)
            copy.set(Calendar.MILLISECOND, 999)
            return copy.timeInMillis
        }

        return when (filter) {
            InsightPeriodFilter.THIS_WEEK -> {
                val end = endOfDay(cal)
                val startCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }
                val start = startOfDay(startCal)
                val prevEnd = start - 1L
                val prevStartCal = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -7) }
                val prevStart = startOfDay(prevStartCal)
                InsightTimeWindow(
                    filter = filter,
                    startMillis = start,
                    endMillis = end,
                    prevStartMillis = prevStart,
                    prevEndMillis = prevEnd,
                    displayRangeLabel = "${shortFmt.format(Date(start))} – ${shortFmt.format(Date(end))}",
                    comparisonLabel = "vs previous 7 days",
                    durationDays = 7
                )
            }

            InsightPeriodFilter.THIS_MONTH -> {
                val startCal = (cal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val start = startOfDay(startCal)
                val end = endOfDay(cal)
                val prevMonthStartCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val prevMonthEndCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.MILLISECOND, -1)
                }
                val daysElapsed = max(1, ((end - start) / DAY_MS).toInt() + 1)
                InsightTimeWindow(
                    filter = filter,
                    startMillis = start,
                    endMillis = end,
                    prevStartMillis = startOfDay(prevMonthStartCal),
                    prevEndMillis = endOfDay(prevMonthEndCal),
                    displayRangeLabel = monthFmt.format(Date(start)),
                    comparisonLabel = "vs last month",
                    durationDays = daysElapsed
                )
            }

            InsightPeriodFilter.LAST_MONTH -> {
                val lastMonthStartCal = (cal.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val lastMonthEndCal = (lastMonthStartCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }
                val twoMonthsAgoStartCal = (lastMonthStartCal.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val twoMonthsAgoEndCal = (twoMonthsAgoStartCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }
                val start = startOfDay(lastMonthStartCal)
                val end = endOfDay(lastMonthEndCal)
                val days = max(1, ((end - start) / DAY_MS).toInt() + 1)
                InsightTimeWindow(
                    filter = filter,
                    startMillis = start,
                    endMillis = end,
                    prevStartMillis = startOfDay(twoMonthsAgoStartCal),
                    prevEndMillis = endOfDay(twoMonthsAgoEndCal),
                    displayRangeLabel = monthFmt.format(Date(start)),
                    comparisonLabel = "vs 2 months ago",
                    durationDays = days
                )
            }

            InsightPeriodFilter.LAST_3_MONTHS -> {
                val end = endOfDay(cal)
                val startCal = (cal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -89)
                }
                val start = startOfDay(startCal)
                val prevEnd = start - 1L
                val prevStartCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -90)
                }
                val prevStart = startOfDay(prevStartCal)
                InsightTimeWindow(
                    filter = filter,
                    startMillis = start,
                    endMillis = end,
                    prevStartMillis = prevStart,
                    prevEndMillis = prevEnd,
                    displayRangeLabel = "${shortFmt.format(Date(start))} – ${dateFmt.format(Date(end))}",
                    comparisonLabel = "vs prior 3 months",
                    durationDays = 90
                )
            }

            InsightPeriodFilter.THIS_YEAR -> {
                val startCal = (cal.clone() as Calendar).apply {
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val start = startOfDay(startCal)
                val end = endOfDay(cal)
                val prevYearStartCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.YEAR, -1)
                }
                val prevYearEndCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.MILLISECOND, -1)
                }
                val days = max(1, ((end - start) / DAY_MS).toInt() + 1)
                InsightTimeWindow(
                    filter = filter,
                    startMillis = start,
                    endMillis = end,
                    prevStartMillis = startOfDay(prevYearStartCal),
                    prevEndMillis = endOfDay(prevYearEndCal),
                    displayRangeLabel = "Year ${cal.get(Calendar.YEAR)}",
                    comparisonLabel = "vs last year",
                    durationDays = days
                )
            }

            InsightPeriodFilter.CUSTOM -> {
                val rawStart = customStartMillis ?: (nowMillis - 29L * DAY_MS)
                val rawEnd = customEndMillis ?: nowMillis
                val orderedStart = min(rawStart, rawEnd)
                val orderedEnd = max(rawStart, rawEnd)
                val cStart = Calendar.getInstance().apply { timeInMillis = orderedStart }
                val cEnd = Calendar.getInstance().apply { timeInMillis = orderedEnd }
                val start = startOfDay(cStart)
                val end = endOfDay(cEnd)
                val spanMs = max(DAY_MS, end - start + 1L)
                val days = max(1, (spanMs / DAY_MS).toInt())
                val prevEnd = start - 1L
                val prevStart = start - spanMs
                InsightTimeWindow(
                    filter = filter,
                    startMillis = start,
                    endMillis = end,
                    prevStartMillis = prevStart,
                    prevEndMillis = prevEnd,
                    displayRangeLabel = "${dateFmt.format(Date(start))} – ${dateFmt.format(Date(end))}",
                    comparisonLabel = "vs previous $days days",
                    durationDays = days
                )
            }
        }
    }

    private fun computePercentageChange(current: Long, previous: Long): Int? {
        if (previous <= 0L) {
            return if (current > 0L) 100 else null
        }
        return (((current - previous).toDouble() / previous.toDouble()) * 100.0).roundToInt()
    }

    fun normalizeInsightCategory(rawCategory: String): String {
        val trimmed = rawCategory.trim()
        if (trimmed.isEmpty()) return "Other"
        val lower = trimmed.lowercase(Locale.ENGLISH)
        return when {
            lower.contains("food") || lower.contains("chai") || lower.contains("snack") ||
                lower.contains("grocery") || lower.contains("groceries") || lower.contains("dining") ||
                lower.contains("restaurant") || lower.contains("swiggy") || lower.contains("zomato") -> "Food"
            lower.contains("shop") || lower.contains("amazon") || lower.contains("flipkart") ||
                lower.contains("apparel") || lower.contains("clothes") -> "Shopping"
            lower.contains("transport") || lower.contains("commute") || lower.contains("fuel") ||
                lower.contains("cab") || lower.contains("uber") || lower.contains("ola") ||
                lower.contains("metro") || lower.contains("auto") -> "Transport"
            lower.contains("bill") || lower.contains("rent") || lower.contains("emi") ||
                lower.contains("utility") || lower.contains("utilities") || lower.contains("recharge") ||
                lower.contains("wifi") || lower.contains("electricity") -> "Bills"
            lower.contains("entertain") || lower.contains("movie") || lower.contains("ott") ||
                lower.contains("netflix") || lower.contains("spotify") || lower.contains("game") -> "Entertainment"
            lower.contains("health") || lower.contains("medical") || lower.contains("pharma") ||
                lower.contains("gym") || lower.contains("fitness") || lower.contains("doctor") -> "Health"
            lower.contains("educat") || lower.contains("book") || lower.contains("course") ||
                lower.contains("tuition") || lower.contains("college") || lower.contains("study") -> "Education"
            lower.contains("travel") || lower.contains("trip") || lower.contains("flight") ||
                lower.contains("hotel") || lower.contains("train") -> "Travel"
            else -> trimmed
        }
    }

    fun computeCompleteAnalytics(
        expenses: List<ExpenseRecord>,
        settings: UserSettings,
        categoryBudgets: Map<String, Long>,
        savingsGoals: List<SavingsGoalRecord>,
        paymentReminders: List<PaymentReminderRecord>,
        filter: InsightPeriodFilter,
        nowMillis: Long = System.currentTimeMillis(),
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): CompleteInsightAnalyticsBundle {
        // Deduplicate by ID in case any caller passes duplicate list references
        val uniqueRecords = expenses.distinctBy { it.id.ifBlank { "${it.title}_${it.txEpochMillis}_${it.amount}" } }

        val window = resolveTimeWindow(
            filter = filter,
            nowMillis = nowMillis,
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis
        )

        val periodTx = uniqueRecords.filter { it.txEpochMillis in window.startMillis..window.endMillis }
        val prevPeriodTx = uniqueRecords.filter { it.txEpochMillis in window.prevStartMillis..window.prevEndMillis }

        val periodIncomeTx = periodTx.filter { isIncomeTransaction(it) }
        val periodExpenseTx = periodTx.filter { !isIncomeTransaction(it) }

        val prevIncomeTx = prevPeriodTx.filter { isIncomeTransaction(it) }
        val prevExpenseTx = prevPeriodTx.filter { !isIncomeTransaction(it) }

        val totalIncome = periodIncomeTx.sumOf { it.amount }
        val totalExpenses = periodExpenseTx.sumOf { it.amount }
        val prevIncome = prevIncomeTx.sumOf { it.amount }
        val prevExpenses = prevExpenseTx.sumOf { it.amount }

        val hasRecordedIncome = totalIncome > 0L
        // Verified savings strictly uses recorded income minus recorded expenses
        val verifiedSavings = if (hasRecordedIncome) totalIncome - totalExpenses else 0L
        val prevSavings = if (prevIncome > 0L) prevIncome - prevExpenses else 0L
        val savingsRatePct = if (hasRecordedIncome) {
            (((totalIncome - totalExpenses).toDouble() / totalIncome.toDouble()) * 100.0).roundToInt().coerceIn(-999, 100)
        } else 0

        // Current calendar month vs last calendar month
        val thisMonthWindow = resolveTimeWindow(InsightPeriodFilter.THIS_MONTH, nowMillis)
        val thisMonthTx = uniqueRecords.filter { it.txEpochMillis in thisMonthWindow.startMillis..thisMonthWindow.endMillis }
        val lastMonthTx = uniqueRecords.filter { it.txEpochMillis in thisMonthWindow.prevStartMillis..thisMonthWindow.prevEndMillis }

        val thisMonthIncome = thisMonthTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val thisMonthExpense = thisMonthTx.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val lastMonthExpense = lastMonthTx.filter { !isIncomeTransaction(it) }.sumOf { it.amount }

        val overview = InsightOverviewSummary(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            verifiedSavings = verifiedSavings,
            hasRecordedIncome = hasRecordedIncome,
            savingsRatePercent = savingsRatePct,
            monthlySpending = thisMonthExpense,
            previousPeriodIncome = prevIncome,
            previousPeriodExpenses = prevExpenses,
            previousPeriodSavings = prevSavings,
            incomeChangePercent = computePercentageChange(totalIncome, prevIncome),
            expenseChangePercent = computePercentageChange(totalExpenses, prevExpenses),
            monthOverMonthExpenseChangePercent = computePercentageChange(thisMonthExpense, lastMonthExpense),
            transactionCount = periodTx.size
        )

        val earnings = buildEarningsInsights(
            allRecords = uniqueRecords,
            periodIncomeTx = periodIncomeTx,
            totalIncome = totalIncome,
            prevIncome = prevIncome,
            nowMillis = nowMillis
        )

        val spending = buildSpendingInsights(
            allRecords = uniqueRecords,
            periodExpenseTx = periodExpenseTx,
            totalExpenses = totalExpenses,
            prevExpenses = prevExpenses,
            thisMonthExpense = thisMonthExpense,
            lastMonthExpense = lastMonthExpense,
            window = window,
            nowMillis = nowMillis
        )

        val categories = buildCategoryInsights(
            periodExpenseTx = periodExpenseTx,
            prevExpenseTx = prevExpenseTx,
            totalExpenses = totalExpenses
        )

        val budget = buildBudgetIntelligence(
            thisMonthExpenseTx = thisMonthTx.filter { !isIncomeTransaction(it) },
            settings = settings,
            categoryBudgets = categoryBudgets
        )

        val healthScore = buildFinancialHealthEvaluation(
            allRecords = uniqueRecords,
            thisMonthIncome = thisMonthIncome,
            thisMonthExpense = thisMonthExpense,
            lastMonthTx = lastMonthTx,
            budget = budget,
            settings = settings,
            savingsGoals = savingsGoals,
            paymentReminders = paymentReminders,
            nowMillis = nowMillis
        )

        val savings = buildSavingsAnalytics(
            allRecords = uniqueRecords,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            prevSavings = prevSavings,
            thisMonthIncome = thisMonthIncome,
            thisMonthExpense = thisMonthExpense,
            savingsGoals = savingsGoals,
            nowMillis = nowMillis
        )

        val jarvisInsights = buildJarvisDataBackedInsights(
            window = window,
            overview = overview,
            spending = spending,
            categories = categories,
            budget = budget,
            savings = savings
        )

        return CompleteInsightAnalyticsBundle(
            timeWindow = window,
            overview = overview,
            earnings = earnings,
            spending = spending,
            categories = categories,
            budget = budget,
            healthScore = healthScore,
            savings = savings,
            jarvisInsights = jarvisInsights
        )
    }

    private fun buildEarningsInsights(
        allRecords: List<ExpenseRecord>,
        periodIncomeTx: List<ExpenseRecord>,
        totalIncome: Long,
        prevIncome: Long,
        nowMillis: Long
    ): EarningsInsightsData {
        val sources = periodIncomeTx
            .groupBy { tx ->
                tx.category.trim().ifBlank { tx.title.trim().ifBlank { "Income" } }
            }
            .map { (source, list) ->
                val sum = list.sumOf { it.amount }
                val pct = if (totalIncome > 0L) ((sum.toDouble() / totalIncome.toDouble()) * 100.0).roundToInt() else 0
                IncomeSourceItem(
                    sourceName = source,
                    amount = sum,
                    count = list.size,
                    percentOfIncome = pct
                )
            }
            .sortedByDescending { it.amount }

        val monthlyTrend = buildLast6MonthsTrend(allRecords, incomeOnly = true, nowMillis = nowMillis)
        val weeklyTrend = buildLast4WeeksTrend(allRecords, incomeOnly = true, nowMillis = nowMillis)

        val bestPeriod = monthlyTrend.maxByOrNull { it.amount }
        val bestLabel = if (bestPeriod != null && bestPeriod.amount > 0L) bestPeriod.label else "No recorded period"
        val bestAmount = bestPeriod?.amount ?: 0L

        return EarningsInsightsData(
            totalIncomeReceived = totalIncome,
            previousPeriodIncome = prevIncome,
            incomeGrowthPercent = computePercentageChange(totalIncome, prevIncome),
            sourceBreakdown = sources,
            monthlyTrend = monthlyTrend,
            weeklyTrend = weeklyTrend,
            highestEarningPeriodLabel = bestLabel,
            highestEarningPeriodAmount = bestAmount,
            incomeTransactionCount = periodIncomeTx.size
        )
    }

    private fun buildSpendingInsights(
        allRecords: List<ExpenseRecord>,
        periodExpenseTx: List<ExpenseRecord>,
        totalExpenses: Long,
        prevExpenses: Long,
        thisMonthExpense: Long,
        lastMonthExpense: Long,
        window: InsightTimeWindow,
        nowMillis: Long
    ): SpendingInsightsData {
        val avgDaily = if (window.durationDays > 0) totalExpenses / window.durationDays else 0L

        // Highest spending day inside the selected period
        val dayFmt = SimpleDateFormat("dd MMM yyyy (EEE)", Locale.ENGLISH)
        val byDay = periodExpenseTx.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.txEpochMillis }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        }
        val highestDayEntry = byDay.maxByOrNull { (_, list) -> list.sumOf { it.amount } }
        val highestDayAmount = highestDayEntry?.value?.sumOf { it.amount } ?: 0L
        val highestDayLabel = highestDayEntry?.value?.firstOrNull()?.let {
            dayFmt.format(Date(it.txEpochMillis))
        } ?: "No spending days"

        // 7-Day Daily Trend (Mon..Sun labels for the last 7 days)
        val shortDayFmt = SimpleDateFormat("EEE", Locale.ENGLISH)
        val fullDateFmt = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        val dailyTrend = (6 downTo 0).map { daysAgo ->
            val targetTime = nowMillis - daysAgo * DAY_MS
            val dayTx = allRecords.filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, targetTime) }
            val dayLabel = shortDayFmt.format(Date(targetTime))
            val displayDay = if (dayLabel.equals("Thu", ignoreCase = true)) "Thurs" else dayLabel
            TrendBarPoint(
                label = "${fullDateFmt.format(Date(targetTime))} ($displayDay)",
                shortLabel = displayDay,
                amount = dayTx.sumOf { it.amount },
                count = dayTx.size,
                startMillis = targetTime,
                endMillis = targetTime
            )
        }

        val weeklyTrend = buildLast4WeeksTrend(allRecords, incomeOnly = false, nowMillis = nowMillis)
        val monthlyTrend = buildLast6MonthsTrend(allRecords, incomeOnly = false, nowMillis = nowMillis)

        return SpendingInsightsData(
            totalSpendingPeriod = totalExpenses,
            thisMonthSpending = thisMonthExpense,
            lastMonthSpending = lastMonthExpense,
            monthOverMonthChangePercent = computePercentageChange(thisMonthExpense, lastMonthExpense),
            previousPeriodSpending = prevExpenses,
            periodComparisonPercent = computePercentageChange(totalExpenses, prevExpenses),
            averageDailySpending = avgDaily,
            highestSpendingDayLabel = highestDayLabel,
            highestSpendingDayAmount = highestDayAmount,
            dailyTrend = dailyTrend,
            weeklyTrend = weeklyTrend,
            monthlyTrend = monthlyTrend,
            expenseTransactionCount = periodExpenseTx.size
        )
    }

    private fun buildCategoryInsights(
        periodExpenseTx: List<ExpenseRecord>,
        prevExpenseTx: List<ExpenseRecord>,
        totalExpenses: Long
    ): CategoryInsightsData {
        val prevByNormalized = prevExpenseTx
            .groupBy { normalizeInsightCategory(it.category) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val grouped = periodExpenseTx.groupBy { normalizeInsightCategory(it.category) }

        val details = grouped.map { (catName, list) ->
            val sum = list.sumOf { it.amount }
            val prevSum = prevByNormalized[catName] ?: 0L
            val pct = if (totalExpenses > 0L) {
                ((sum.toDouble() / totalExpenses.toDouble()) * 100.0).roundToInt()
            } else 0
            InsightCategoryDetail(
                category = catName,
                amount = sum,
                previousAmount = prevSum,
                changePercent = computePercentageChange(sum, prevSum),
                transactionCount = list.size,
                percentOfTotal = pct,
                transactions = list.sortedByDescending { it.txEpochMillis }
            )
        }.sortedByDescending { it.amount }

        return CategoryInsightsData(
            totalCategorizedSpend = totalExpenses,
            topCategory = details.firstOrNull(),
            categories = details
        )
    }

    private fun buildBudgetIntelligence(
        thisMonthExpenseTx: List<ExpenseRecord>,
        settings: UserSettings,
        categoryBudgets: Map<String, Long>
    ): BudgetIntelligenceData {
        val dailyLimit = max(50L, settings.dailyLimit)
        val configuredCatTotal = categoryBudgets.values.sum()
        val totalMonthlyBudget = max(dailyLimit * 30L, configuredCatTotal).coerceAtLeast(1500L)
        val totalSpentThisMonth = thisMonthExpenseTx.sumOf { it.amount }
        val remainingBudget = max(0L, totalMonthlyBudget - totalSpentThisMonth)
        val utilizationPct = ((totalSpentThisMonth.toDouble() / totalMonthlyBudget.toDouble()) * 100.0).roundToInt()

        val spentByNormalized = thisMonthExpenseTx
            .groupBy { normalizeInsightCategory(it.category) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val rawSpentByExact = thisMonthExpenseTx
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        // Combine configured category budgets with any active spending categories
        val mergedCategories = linkedSetOf<String>().apply {
            addAll(categoryBudgets.keys)
            addAll(spentByNormalized.keys)
        }

        val progressItems = mergedCategories.map { cat ->
            val limit = categoryBudgets[cat]
                ?: categoryBudgets.entries.firstOrNull { normalizeInsightCategory(it.key).equals(cat, ignoreCase = true) }?.value
                ?: max(1500L, totalMonthlyBudget / 6L)
            val spent = rawSpentByExact[cat] ?: spentByNormalized[normalizeInsightCategory(cat)] ?: 0L
            val util = if (limit > 0L) ((spent.toDouble() / limit.toDouble()) * 100.0).roundToInt() else 0
            val status = when {
                spent > limit -> BudgetStatusLevel.EXCEEDED_LIMIT
                util >= 80 -> BudgetStatusLevel.APPROACHING_LIMIT
                else -> BudgetStatusLevel.WITHIN_LIMIT
            }
            CategoryBudgetProgressItem(
                category = cat,
                budgetLimit = limit,
                spentAmount = spent,
                remainingAmount = max(0L, limit - spent),
                overageAmount = max(0L, spent - limit),
                utilizationPercent = util,
                status = status
            )
        }.sortedByDescending { it.utilizationPercent }

        val approaching = progressItems.filter { it.status == BudgetStatusLevel.APPROACHING_LIMIT }
        val exceeding = progressItems.filter { it.status == BudgetStatusLevel.EXCEEDED_LIMIT }

        val headline = when {
            exceeding.isNotEmpty() -> "${exceeding.size} Category Limit${if (exceeding.size > 1) "s" else ""} Exceeded"
            utilizationPct > 100 -> "Monthly Budget Exceeded ($utilizationPct%)"
            approaching.isNotEmpty() -> "${approaching.size} Categor${if (approaching.size > 1) "ies" else "y"} Approaching Limit"
            totalSpentThisMonth == 0L -> "No Monthly Spend Recorded Yet"
            else -> "Budget Discipline On Track ($utilizationPct% Used)"
        }

        val explanation = when {
            totalSpentThisMonth == 0L ->
                "You have ₹$totalMonthlyBudget available for this month. Record expenses to track real-time budget utilization."
            exceeding.isNotEmpty() -> {
                val topOver = exceeding.first()
                "${topOver.category} has exceeded its ₹${topOver.budgetLimit} limit by ₹${topOver.overageAmount} (${topOver.utilizationPercent}%). Overall monthly utilization is $utilizationPct%."
            }
            approaching.isNotEmpty() -> {
                val topNear = approaching.first()
                "${topNear.category} is at ${topNear.utilizationPercent}% (₹${topNear.spentAmount} of ₹${topNear.budgetLimit}). ₹$remainingBudget remains in your overall monthly budget."
            }
            else ->
                "You have used ₹$totalSpentThisMonth of your ₹$totalMonthlyBudget monthly budget ($utilizationPct%), leaving ₹$remainingBudget available."
        }

        return BudgetIntelligenceData(
            totalMonthlyBudget = totalMonthlyBudget,
            totalSpentThisMonth = totalSpentThisMonth,
            remainingBudget = remainingBudget,
            utilizationPercent = utilizationPct,
            categoryProgress = progressItems,
            approachingCategories = approaching,
            exceedingCategories = exceeding,
            performanceHeadline = headline,
            performanceExplanation = explanation
        )
    }

    private fun buildFinancialHealthEvaluation(
        allRecords: List<ExpenseRecord>,
        thisMonthIncome: Long,
        thisMonthExpense: Long,
        lastMonthTx: List<ExpenseRecord>,
        budget: BudgetIntelligenceData,
        settings: UserSettings,
        savingsGoals: List<SavingsGoalRecord>,
        paymentReminders: List<PaymentReminderRecord>,
        nowMillis: Long
    ): FinancialHealthEvaluation {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(nowMillis))
        if (allRecords.isEmpty() && savingsGoals.isEmpty() && paymentReminders.isEmpty()) {
            return FinancialHealthEvaluation(
                hasSufficientData = false,
                score = 0,
                category = "INSUFFICIENT DATA",
                previousScore = null,
                scoreChange = null,
                lastUpdatedDate = dateStr,
                summaryExplanation = "Record your first income, expense, bill reminder, or savings goal so KHARCHEE can calculate your verified Financial Health Score.",
                factors = emptyList()
            )
        }

        // Factor 1: Budget Discipline (0–30 pts)
        val util = budget.utilizationPercent
        val exceededCount = budget.exceedingCategories.size
        val budgetPoints = when {
            util <= 60 && exceededCount == 0 -> 30
            util <= 80 && exceededCount == 0 -> 26
            util <= 95 && exceededCount <= 1 -> 21
            util <= 100 -> 17
            util <= 125 -> 11
            else -> 6
        }
        val budgetFactor = HealthScoreFactor(
            name = "Budget Discipline",
            earnedPoints = budgetPoints,
            maxPoints = 30,
            statusLabel = if (budgetPoints >= 24) "Strong" else if (budgetPoints >= 17) "Moderate" else "Needs Attention",
            explanation = "Monthly budget utilization is $util% with $exceededCount exceeded categor${if (exceededCount == 1) "y" else "ies"}."
        )

        // Factor 2: Expense-to-Income & Savings Consistency (0–25 pts)
        val savingsPoints: Int
        val savingsExplanation: String
        if (thisMonthIncome > 0L) {
            val rate = (((thisMonthIncome - thisMonthExpense).toDouble() / thisMonthIncome.toDouble()) * 100.0).roundToInt()
            savingsPoints = when {
                rate >= 30 -> 25
                rate >= 20 -> 22
                rate >= 10 -> 17
                rate >= 0 -> 12
                else -> 5
            }
            savingsExplanation = "Recorded monthly savings rate is $rate% (Income ₹$thisMonthIncome vs Expense ₹$thisMonthExpense)."
        } else {
            savingsPoints = 12
            savingsExplanation = "No income recorded this month yet; scored at neutral baseline until income is logged."
        }
        val savingsFactor = HealthScoreFactor(
            name = "Savings & Cashflow Ratio",
            earnedPoints = savingsPoints,
            maxPoints = 25,
            statusLabel = if (thisMonthIncome == 0L) "Unverified Income" else if (savingsPoints >= 20) "Healthy Surplus" else "Tight Margin",
            explanation = savingsExplanation
        )

        // Factor 3: Daily Spending Pattern Consistency (0–20 pts)
        val dailyLimit = max(50L, settings.dailyLimit)
        val daysWithinLimit = (0 until 14).count { daysAgo ->
            val targetDay = nowMillis - daysAgo * DAY_MS
            val daySpend = allRecords
                .filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, targetDay) }
                .sumOf { it.amount }
            daySpend <= dailyLimit
        }
        val patternPoints = ((daysWithinLimit / 14.0) * 20.0).roundToInt().coerceIn(4, 20)
        val patternFactor = HealthScoreFactor(
            name = "Daily Spending Consistency",
            earnedPoints = patternPoints,
            maxPoints = 20,
            statusLabel = "$daysWithinLimit/14 Days On Target",
            explanation = "$daysWithinLimit of the last 14 days stayed within your ₹$dailyLimit daily spending limit."
        )

        // Factor 4: Financial Goal Progress (0–15 pts)
        val goalPoints: Int
        val goalExplanation: String
        if (savingsGoals.isNotEmpty()) {
            val totalTarget = savingsGoals.sumOf { max(1L, it.targetAmount) }
            val totalSaved = savingsGoals.sumOf { it.savedAmount }
            val ratio = (totalSaved.toDouble() / totalTarget.toDouble()).coerceIn(0.0, 1.0)
            goalPoints = (5 + (ratio * 10.0).roundToInt()).coerceIn(5, 15)
            goalExplanation = "${savingsGoals.size} active goal(s) with ₹$totalSaved saved toward ₹$totalTarget target."
        } else {
            goalPoints = 9
            goalExplanation = "No active savings goals configured yet; add a goal to boost this score factor."
        }
        val goalFactor = HealthScoreFactor(
            name = "Financial Goal Progress",
            earnedPoints = goalPoints,
            maxPoints = 15,
            statusLabel = if (savingsGoals.isEmpty()) "No Active Goals" else "${((goalPoints / 15.0) * 100).roundToInt()}% Weight",
            explanation = goalExplanation
        )

        // Factor 5: Bill Payment Consistency (0–10 pts)
        val billPoints: Int
        val billExplanation: String
        if (paymentReminders.isNotEmpty()) {
            val overdueUnpaid = paymentReminders.count { !it.isPaid && it.dueEpochMillis < nowMillis }
            val paidCount = paymentReminders.count { it.isPaid }
            billPoints = when {
                overdueUnpaid == 0 && paidCount > 0 -> 10
                overdueUnpaid == 0 -> 9
                overdueUnpaid == 1 -> 6
                else -> 3
            }
            billExplanation = "$paidCount bill(s) paid on time, $overdueUnpaid overdue bill(s)."
        } else {
            billPoints = 8
            billExplanation = "No overdue bills recorded."
        }
        val billFactor = HealthScoreFactor(
            name = "Bill Payment Consistency",
            earnedPoints = billPoints,
            maxPoints = 10,
            statusLabel = if (billPoints >= 9) "On Time" else "Overdue Alert",
            explanation = billExplanation
        )

        val totalScore = (budgetPoints + savingsPoints + patternPoints + goalPoints + billPoints).coerceIn(1, 100)
        val category = when {
            totalScore >= 80 -> "EXCELLENT"
            totalScore >= 65 -> "GOOD"
            totalScore >= 45 -> "FAIR"
            else -> "NEEDS ATTENTION"
        }

        // Previous month comparison if lastMonthTx exists
        val prevScore: Int? = if (lastMonthTx.isNotEmpty()) {
            val lastInc = lastMonthTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
            val lastExp = lastMonthTx.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
            val lastUtil = ((lastExp.toDouble() / max(1L, budget.totalMonthlyBudget).toDouble()) * 100.0).roundToInt()
            val prevBudgetPts = if (lastUtil <= 80) 26 else if (lastUtil <= 100) 18 else 10
            val prevSavPts = if (lastInc > 0L && lastInc >= lastExp) 20 else 12
            (prevBudgetPts + prevSavPts + patternPoints + goalPoints + billPoints).coerceIn(1, 100)
        } else null

        val scoreDelta = if (prevScore != null) totalScore - prevScore else null

        val factors = listOf(budgetFactor, savingsFactor, patternFactor, goalFactor, billFactor)

        return FinancialHealthEvaluation(
            hasSufficientData = true,
            score = totalScore,
            category = category,
            previousScore = prevScore,
            scoreChange = scoreDelta,
            lastUpdatedDate = dateStr,
            summaryExplanation = "Calculated transparently from 5 real financial factors: Budget Discipline ($budgetPoints/30), Cashflow & Savings ($savingsPoints/25), Daily Consistency ($patternPoints/20), Goal Progress ($goalPoints/15), and Bill Timeliness ($billPoints/10).",
            factors = factors
        )
    }

    private fun buildSavingsAnalytics(
        allRecords: List<ExpenseRecord>,
        totalIncome: Long,
        totalExpenses: Long,
        prevSavings: Long,
        thisMonthIncome: Long,
        thisMonthExpense: Long,
        savingsGoals: List<SavingsGoalRecord>,
        nowMillis: Long
    ): SavingsAnalyticsData {
        val hasIncome = totalIncome > 0L
        val verifiedPeriodSavings = if (hasIncome) totalIncome - totalExpenses else 0L
        val verifiedMonthlySavings = if (thisMonthIncome > 0L) thisMonthIncome - thisMonthExpense else 0L
        val ratePct = if (hasIncome) {
            (((totalIncome - totalExpenses).toDouble() / totalIncome.toDouble()) * 100.0).roundToInt().coerceIn(-999, 100)
        } else 0

        val totalGoalsSaved = savingsGoals.sumOf { it.savedAmount }
        val totalGoalsTarget = savingsGoals.sumOf { it.targetAmount }
        val goalsPct = if (totalGoalsTarget > 0L) {
            ((totalGoalsSaved.toDouble() / totalGoalsTarget.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
        } else 0

        // 6-month verified savings trend
        val monthFmt = SimpleDateFormat("MMM", Locale.ENGLISH)
        val fullMonthFmt = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
        val monthlySavingsTrend = (5 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.MONTH, -offset)
            }
            val yr = cal.get(Calendar.YEAR)
            val mo = cal.get(Calendar.MONTH)
            val monthTx = allRecords.filter { tx ->
                val c = Calendar.getInstance().apply { timeInMillis = tx.txEpochMillis }
                c.get(Calendar.YEAR) == yr && c.get(Calendar.MONTH) == mo
            }
            val inc = monthTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
            val exp = monthTx.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
            val net = if (inc > 0L) inc - exp else 0L
            TrendBarPoint(
                label = fullMonthFmt.format(cal.time),
                shortLabel = monthFmt.format(cal.time),
                amount = net,
                secondaryAmount = exp,
                count = monthTx.size
            )
        }

        val explanation = when {
            !hasIncome && totalExpenses == 0L ->
                "No income or expenses recorded in this period yet. Log both income and expenses to compute verified savings."
            !hasIncome ->
                "Unrecorded money is never treated as verified savings. Log your income entries so KHARCHEE can calculate net savings against ₹$totalExpenses in recorded expenses."
            verifiedPeriodSavings >= 0L ->
                "Verified net savings of ₹$verifiedPeriodSavings ($ratePct% of ₹$totalIncome recorded income after ₹$totalExpenses in expenses)."
            else ->
                "Recorded expenses (₹$totalExpenses) exceeded recorded income (₹$totalIncome) by ₹${abs(verifiedPeriodSavings)} in this period."
        }

        return SavingsAnalyticsData(
            hasRecordedIncome = hasIncome,
            verifiedPeriodSavings = verifiedPeriodSavings,
            verifiedMonthlySavings = verifiedMonthlySavings,
            savingsRatePercent = ratePct,
            periodIncome = totalIncome,
            periodExpenses = totalExpenses,
            previousPeriodSavings = prevSavings,
            savingsChangeAmount = verifiedPeriodSavings - prevSavings,
            totalGoalsSaved = totalGoalsSaved,
            totalGoalsTarget = totalGoalsTarget,
            goalsProgressPercent = goalsPct,
            monthlySavingsTrend = monthlySavingsTrend,
            explanationText = explanation
        )
    }

    private fun buildJarvisDataBackedInsights(
        window: InsightTimeWindow,
        overview: InsightOverviewSummary,
        spending: SpendingInsightsData,
        categories: CategoryInsightsData,
        budget: BudgetIntelligenceData,
        savings: SavingsAnalyticsData
    ): List<JarvisDataBackedInsight> {
        val list = mutableListOf<JarvisDataBackedInsight>()
        val periodName = "${window.filter.label} (${window.displayRangeLabel})"

        if (overview.transactionCount == 0) {
            list.add(
                JarvisDataBackedInsight(
                    id = "empty_period_observation",
                    categoryTag = "DATA OBSERVATION",
                    headline = "No transactions recorded for ${window.filter.label}",
                    observation = "There are 0 recorded income or expense entries between ${window.displayRangeLabel}.",
                    whyItAppeared = "Triggered because the selected period contains 0 transaction records.",
                    periodLabel = periodName,
                    suggestedJarvisPrompt = "How should I structure my monthly budget and track expenses in KHARCHEE?"
                )
            )
            return list
        }

        // 1. Top Category Spending Analysis
        categories.topCategory?.let { topCat ->
            list.add(
                JarvisDataBackedInsight(
                    id = "top_category_${topCat.category}",
                    categoryTag = "CATEGORY ANALYSIS",
                    headline = "${topCat.category} leads at ${topCat.percentOfTotal}% of spending",
                    observation = "You spent ₹${topCat.amount} across ${topCat.transactionCount} transaction(s) in ${topCat.category} during ${window.displayRangeLabel}.",
                    whyItAppeared = "Triggered because ${topCat.category} is your #1 expenditure category out of ₹${overview.totalExpenses} total spend.",
                    periodLabel = periodName,
                    suggestedJarvisPrompt = "Analyze my ${topCat.category} spending of ₹${topCat.amount} during ${window.displayRangeLabel} and suggest ways to optimize it."
                )
            )
        }

        // 2. Unusual Spending / Peak Day Observation
        if (spending.highestSpendingDayAmount > 0L && spending.averageDailySpending > 0L &&
            spending.highestSpendingDayAmount >= spending.averageDailySpending * 2L
        ) {
            list.add(
                JarvisDataBackedInsight(
                    id = "peak_day_spike",
                    categoryTag = "UNUSUAL SPENDING ALERT",
                    headline = "Peak outflow on ${spending.highestSpendingDayLabel}",
                    observation = "Your highest single-day spend was ₹${spending.highestSpendingDayAmount} on ${spending.highestSpendingDayLabel}, compared to your ₹${spending.averageDailySpending}/day average.",
                    whyItAppeared = "Triggered because peak day spending exceeded 2x your average daily spending (₹${spending.averageDailySpending}/day) in $periodName.",
                    periodLabel = periodName,
                    suggestedJarvisPrompt = "Review my peak spending day (${spending.highestSpendingDayLabel}: ₹${spending.highestSpendingDayAmount}) and help me smooth out spikes."
                )
            )
        }

        // 3. Budget Improvement Suggestion
        if (budget.exceedingCategories.isNotEmpty()) {
            val breached = budget.exceedingCategories.first()
            list.add(
                JarvisDataBackedInsight(
                    id = "budget_breach_${breached.category}",
                    categoryTag = "BUDGET IMPROVEMENT",
                    headline = "${breached.category} exceeded budget by ₹${breached.overageAmount}",
                    observation = "${breached.category} is at ${breached.utilizationPercent}% utilization (₹${breached.spentAmount} spent vs ₹${breached.budgetLimit} monthly limit).",
                    whyItAppeared = "Triggered because recorded ${breached.category} expenses this month surpassed the configured ₹${breached.budgetLimit} limit.",
                    periodLabel = "Current Month Budget",
                    suggestedJarvisPrompt = "Help me rebalance my ${breached.category} budget after spending ₹${breached.spentAmount} against a ₹${breached.budgetLimit} limit."
                )
            )
        } else if (budget.approachingCategories.isNotEmpty()) {
            val near = budget.approachingCategories.first()
            list.add(
                JarvisDataBackedInsight(
                    id = "budget_near_${near.category}",
                    categoryTag = "BUDGET IMPROVEMENT",
                    headline = "${near.category} is at ${near.utilizationPercent}% of monthly limit",
                    observation = "₹${near.remainingAmount} remains in your ${near.category} budget (₹${near.spentAmount} of ₹${near.budgetLimit} used).",
                    whyItAppeared = "Triggered because ${near.category} utilization crossed the 80% threshold this month.",
                    periodLabel = "Current Month Budget",
                    suggestedJarvisPrompt = "How can I keep my ${near.category} spending within the remaining ₹${near.remainingAmount} this month?"
                )
            )
        }

        // 4. Period-over-Period / Month-over-Month Observation
        val expChange = overview.expenseChangePercent
        if (expChange != null && overview.previousPeriodExpenses > 0L) {
            val direction = if (expChange > 0) "increased by $expChange%" else if (expChange < 0) "decreased by ${abs(expChange)}%" else "remained unchanged"
            list.add(
                JarvisDataBackedInsight(
                    id = "period_comparison",
                    categoryTag = "PERIOD COMPARISON",
                    headline = "Spending $direction ${window.comparisonLabel}",
                    observation = "Current period spending is ₹${overview.totalExpenses} compared with ₹${overview.previousPeriodExpenses} in the prior equivalent period.",
                    whyItAppeared = "Computed directly by comparing ${window.displayRangeLabel} (₹${overview.totalExpenses}) against the prior period (₹${overview.previousPeriodExpenses}).",
                    periodLabel = periodName,
                    suggestedJarvisPrompt = "Compare my current spending of ₹${overview.totalExpenses} with the previous period's ₹${overview.previousPeriodExpenses}."
                )
            )
        }

        // 5. Savings Opportunity / Cashflow Habit Recommendation
        if (savings.hasRecordedIncome) {
            list.add(
                JarvisDataBackedInsight(
                    id = "savings_rate_observation",
                    categoryTag = "SAVINGS OPPORTUNITY",
                    headline = "Verified savings rate is ${savings.savingsRatePercent}%",
                    observation = "From ₹${savings.periodIncome} recorded income and ₹${savings.periodExpenses} recorded expenses, net cashflow is ₹${savings.verifiedPeriodSavings}.",
                    whyItAppeared = "Calculated from actual recorded income and expense transactions in $periodName.",
                    periodLabel = periodName,
                    suggestedJarvisPrompt = "Based on my ₹${savings.periodIncome} income and ₹${savings.periodExpenses} expenses, how can I boost my savings rate?"
                )
            )
        } else if (overview.totalExpenses > 0L) {
            list.add(
                JarvisDataBackedInsight(
                    id = "log_income_recommendation",
                    categoryTag = "HABIT RECOMMENDATION",
                    headline = "Record income to unlock verified savings rate",
                    observation = "You have logged ₹${overview.totalExpenses} in expenses during ${window.displayRangeLabel}, but 0 income entries.",
                    whyItAppeared = "KHARCHEE never treats unrecorded money as verified savings; logging income enables exact cashflow & savings rate tracking.",
                    periodLabel = periodName,
                    suggestedJarvisPrompt = "Show me how logging my monthly income improves my KHARCHEE analytics and savings tracking."
                )
            )
        }

        return list
    }

    private fun buildLast6MonthsTrend(
        allRecords: List<ExpenseRecord>,
        incomeOnly: Boolean,
        nowMillis: Long
    ): List<TrendBarPoint> {
        val shortMonthFmt = SimpleDateFormat("MMM", Locale.ENGLISH)
        val fullMonthFmt = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
        return (5 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.MONTH, -offset)
            }
            val yr = cal.get(Calendar.YEAR)
            val mo = cal.get(Calendar.MONTH)
            val matched = allRecords.filter { tx ->
                val isInc = isIncomeTransaction(tx)
                if (incomeOnly != isInc) return@filter false
                val c = Calendar.getInstance().apply { timeInMillis = tx.txEpochMillis }
                c.get(Calendar.YEAR) == yr && c.get(Calendar.MONTH) == mo
            }
            TrendBarPoint(
                label = fullMonthFmt.format(cal.time),
                shortLabel = shortMonthFmt.format(cal.time),
                amount = matched.sumOf { it.amount },
                count = matched.size
            )
        }
    }

    private fun buildLast4WeeksTrend(
        allRecords: List<ExpenseRecord>,
        incomeOnly: Boolean,
        nowMillis: Long
    ): List<TrendBarPoint> {
        val shortDateFmt = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        return (3 downTo 0).mapIndexed { idx, weeksAgo ->
            val endMillis = nowMillis - (weeksAgo * 7L * DAY_MS)
            val startMillis = endMillis - (6L * DAY_MS)
            val cStart = Calendar.getInstance().apply {
                timeInMillis = startMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val cEnd = Calendar.getInstance().apply {
                timeInMillis = endMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            val matched = allRecords.filter { tx ->
                val isInc = isIncomeTransaction(tx)
                (incomeOnly == isInc) && tx.txEpochMillis in cStart..cEnd
            }
            TrendBarPoint(
                label = "W${idx + 1} (${shortDateFmt.format(Date(cStart))}–${shortDateFmt.format(Date(cEnd))})",
                shortLabel = "W${idx + 1}",
                amount = matched.sumOf { it.amount },
                count = matched.size,
                startMillis = cStart,
                endMillis = cEnd
            )
        }
    }

    fun generateStructuredReportText(bundle: CompleteInsightAnalyticsBundle): String {
        val sb = StringBuilder()
        sb.appendLine("====================================================")
        sb.appendLine("KHARCHEE FINANCIAL INTELLIGENCE REPORT")
        sb.appendLine("Period: ${bundle.timeWindow.filter.label} (${bundle.timeWindow.displayRangeLabel})")
        sb.appendLine("====================================================")
        sb.appendLine()
        sb.appendLine("1. EXECUTIVE SUMMARY")
        sb.appendLine("- Total Recorded Income: INR ${bundle.overview.totalIncome}")
        sb.appendLine("- Total Recorded Expenses: INR ${bundle.overview.totalExpenses}")
        sb.appendLine("- Verified Net Savings: INR ${bundle.overview.verifiedSavings} (${bundle.overview.savingsRatePercent}% savings rate)")
        sb.appendLine("- Total Transactions Analyzed: ${bundle.overview.transactionCount}")
        sb.appendLine()
        sb.appendLine("2. PERIOD COMPARISON (${bundle.timeWindow.comparisonLabel})")
        sb.appendLine("- Prior Period Income: INR ${bundle.overview.previousPeriodIncome} (${bundle.overview.incomeChangePercent?.let { "$it%" } ?: "N/A"})")
        sb.appendLine("- Prior Period Expenses: INR ${bundle.overview.previousPeriodExpenses} (${bundle.overview.expenseChangePercent?.let { "$it%" } ?: "N/A"})")
        sb.appendLine("- Prior Period Net Savings: INR ${bundle.overview.previousPeriodSavings}")
        sb.appendLine()
        sb.appendLine("3. CATEGORY BREAKDOWN")
        if (bundle.categories.categories.isEmpty()) {
            sb.appendLine("- No expense categories recorded in this period.")
        } else {
            bundle.categories.categories.forEach { cat ->
                sb.appendLine("- ${cat.category}: INR ${cat.amount} (${cat.percentOfTotal}% of spend, ${cat.transactionCount} txns)")
            }
        }
        sb.appendLine()
        sb.appendLine("4. BUDGET PERFORMANCE (CURRENT MONTH)")
        sb.appendLine("- Monthly Budget Target: INR ${bundle.budget.totalMonthlyBudget}")
        sb.appendLine("- Monthly Amount Spent: INR ${bundle.budget.totalSpentThisMonth} (${bundle.budget.utilizationPercent}%)")
        sb.appendLine("- Remaining Monthly Budget: INR ${bundle.budget.remainingBudget}")
        sb.appendLine("- Status: ${bundle.budget.performanceHeadline}")
        sb.appendLine()
        sb.appendLine("5. FINANCIAL HEALTH SCORE")
        if (bundle.healthScore.hasSufficientData) {
            sb.appendLine("- Score: ${bundle.healthScore.score}/100 (${bundle.healthScore.category})")
            bundle.healthScore.factors.forEach { factor ->
                sb.appendLine("  * ${factor.name}: ${factor.earnedPoints}/${factor.maxPoints} (${factor.statusLabel})")
            }
        } else {
            sb.appendLine("- Status: Insufficient Data")
        }
        sb.appendLine("Note: ${bundle.healthScore.bureauDisclaimer}")
        return sb.toString()
    }

    fun generateStructuredReportCsv(bundle: CompleteInsightAnalyticsBundle): String {
        val sb = StringBuilder()
        sb.appendLine("Section,Metric,Value_INR_Or_Count,Share_Or_Comparison,Notes")
        sb.appendLine("\"Overview\",\"Period\",\"${bundle.timeWindow.filter.label}\",\"${bundle.timeWindow.displayRangeLabel}\",\"${bundle.timeWindow.comparisonLabel}\"")
        sb.appendLine("\"Income Summary\",\"Total Income\",${bundle.overview.totalIncome},\"${bundle.overview.incomeChangePercent ?: 0}%\",\"Prev: INR ${bundle.overview.previousPeriodIncome}\"")
        sb.appendLine("\"Expense Summary\",\"Total Expenses\",${bundle.overview.totalExpenses},\"${bundle.overview.expenseChangePercent ?: 0}%\",\"Prev: INR ${bundle.overview.previousPeriodExpenses}\"")
        sb.appendLine("\"Savings Summary\",\"Verified Net Savings\",${bundle.overview.verifiedSavings},\"${bundle.overview.savingsRatePercent}%\",\"HasRecordedIncome=${bundle.overview.hasRecordedIncome}\"")
        sb.appendLine("\"Budget Performance\",\"Monthly Budget\",${bundle.budget.totalMonthlyBudget},\"${bundle.budget.utilizationPercent}%\",\"Remaining: INR ${bundle.budget.remainingBudget}\"")
        sb.appendLine("\"Health Score\",\"Financial Health Score\",${bundle.healthScore.score},\"${bundle.healthScore.category}\",\"SufficientData=${bundle.healthScore.hasSufficientData}\"")
        bundle.categories.categories.forEach { cat ->
            sb.appendLine("\"Category Breakdown\",\"${cat.category.replace("\"", "\"\"")}\",${cat.amount},\"${cat.percentOfTotal}%\",\"${cat.transactionCount} transactions\"")
        }
        return sb.toString()
    }
}
