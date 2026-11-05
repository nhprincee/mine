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
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class MoneyTimelineEvent(
    val id: String,
    val title: String,
    val eventType: String, // "INCOME", "SALARY", "BILL", "EMI", "SUBSCRIPTION", "SAVINGS", "EXPENSE"
    val amount: Long,
    val dateLabel: String,
    val epochMillis: Long,
    val isCompleted: Boolean,
    val reminderEnabled: Boolean,
    val details: String
)

data class PurchaseDecisionEvaluation(
    val productName: String,
    val purchasePrice: Long,
    val availableBudget: Long,
    val remainingBudgetAfter: Long,
    val budgetImpactPercent: Int,
    val goalName: String,
    val goalDelayDays: Int,
    val goalImpactPercent: Int,
    val affordabilityVerdict: String, // "EASILY AFFORDABLE", "MODERATE STRETCH", "WAIT RECOMMENDED"
    val suggestedWaitDays: Int,
    val recommendationSummary: String
)

data class EmergencyFundMetrics(
    val manualEmergencySavings: Long,
    val dreamVaultEmergencySavings: Long,
    val totalEmergencySavings: Long,
    val verifiedLedgerCashBalance: Long,
    val monthlyEssentialExpenses: Long,
    val monthsCovered: Double,
    val targetMonths: Int,
    val userDefinedTargetAmount: Long,
    val remainingTargetAmount: Long,
    val progressPercent: Int
)

data class SalaryAllocationScenario(
    val name: String,
    val essentialPct: Int,
    val savingsPct: Int,
    val emiPct: Int,
    val billsPct: Int,
    val subscriptionsPct: Int,
    val personalPct: Int,
    val goalsPct: Int
)

data class SalarySimulationResult(
    val monthlyIncome: Long,
    val scenarioName: String,
    val essentialAmount: Long,
    val savingsAmount: Long,
    val emiAmount: Long,
    val billsAmount: Long,
    val subscriptionsAmount: Long,
    val personalAmount: Long,
    val goalsAmount: Long,
    val totalAllocatedPct: Int,
    val unallocatedAmount: Long
)

data class WhereMoneyWentAnalysis(
    val totalThisMonth: Long,
    val totalPrevMonth: Long,
    val categorySpending: List<Pair<String, Long>>,
    val merchantSpending: List<Pair<String, Long>>,
    val highestSpendingDays: List<Pair<String, Long>>,
    val recurringExpenses: List<Pair<String, Long>>,
    val unusualTransactions: List<ExpenseRecord>,
    val biggestCategoryChanges: List<Pair<String, Long>>,
    val naturalLanguageSummary: String
)

data class AskYourMoneyTurn(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class SavingsChallengeRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val durationDays: Int,
    val targetAmount: Long,
    val savedAmount: Long,
    val targetDate: String,
    val contributionsCount: Int,
    val reminderEnabled: Boolean,
    val completed: Boolean
)

data class KharcheeWrappedSummary(
    val periodType: String,
    val periodTitle: String,
    val totalIncome: Long,
    val totalExpenses: Long,
    val savingsProgress: Long,
    val mostUsedCategory: String,
    val highestSpendingCategory: String,
    val highestSpendingCategoryAmount: Long,
    val trackingStreakDays: Int,
    val unlockedAchievements: List<String>,
    val changeFromPrevPeriodPercent: Int
)

data class DataExportRecord(
    val id: String = UUID.randomUUID().toString(),
    val format: String,
    val recordCount: Int,
    val exportedAtMillis: Long = System.currentTimeMillis()
)

data class FamilyMemberItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String, // "Admin", "Contributor", "Viewer"
    val contributedAmount: Long = 0L
)

data class FamilySharedExpenseItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Long,
    val category: String,
    val paidBy: String,
    val splitMode: String, // "EQUAL", "CUSTOM"
    val perMemberShare: Long,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class FamilyWalletState(
    val groupName: String,
    val sharedMonthlyBudget: Long,
    val members: List<FamilyMemberItem>,
    val sharedExpenses: List<FamilySharedExpenseItem>
)

object KharcheeEcosystemFeaturesStore {
    private const val PREFS_NAME = "kharchee_ecosystem_10_features_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 1. MONEY TIMELINE
    fun getExpectedSalaryConfig(context: Context, userId: String): Pair<Int, Long> {
        val p = prefs(context)
        if (p.contains("${userId}_salary_amount")) {
            val day = p.getInt("${userId}_salary_day", 1).coerceIn(1, 31)
            val amount = p.getLong("${userId}_salary_amount", 0L)
            return day to amount
        }
        val onboarding = KharcheeOnboardingProfileStore.getOnboardingProfile(context, userId)
        val plan = KharcheeOnboardingProfileStore.computePersonalisedFinancialPlan(onboarding)
        return onboarding.income.salaryOrPaymentDay.coerceIn(1, 31) to plan.totalDeclaredMonthlyIncome
    }

    fun saveExpectedSalaryConfig(context: Context, userId: String, dayOfMonth: Int, amount: Long) {
        prefs(context).edit()
            .putInt("${userId}_salary_day", dayOfMonth.coerceIn(1, 31))
            .putLong("${userId}_salary_amount", max(0L, amount))
            .apply()
    }

    fun buildMoneyTimelineEvents(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        paymentReminders: List<PaymentReminderRecord>,
        savingsGoals: List<SavingsGoalRecord>,
        viewMode: String = "Month",
        nowMillis: Long = System.currentTimeMillis()
    ): List<MoneyTimelineEvent> {
        val dateFmt = SimpleDateFormat("dd MMM", Locale.US)
        val events = mutableListOf<MoneyTimelineEvent>()
        val (salaryDay, salaryAmt) = getExpectedSalaryConfig(context, userId)
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, salaryDay.coerceIn(1, maxDay))
        }
        if (salaryAmt > 0L) {
            events.add(
                MoneyTimelineEvent(
                    id = "salary_expected",
                    title = "Expected Monthly Salary",
                    eventType = "SALARY",
                    amount = salaryAmt,
                    dateLabel = dateFmt.format(Date(cal.timeInMillis)),
                    epochMillis = cal.timeInMillis,
                    isCompleted = cal.timeInMillis <= nowMillis,
                    reminderEnabled = true,
                    details = "Scheduled monthly salary credit on day $salaryDay"
                )
            )
        }
        KharcheeProfileAndToolsStore.getSubscriptions(context, userId).filter { it.isActive }.forEach { sub ->
            events.add(
                MoneyTimelineEvent(
                    id = "sub_${sub.id}",
                    title = "${sub.name} Renewal",
                    eventType = "SUBSCRIPTION",
                    amount = sub.amount,
                    dateLabel = sub.nextRenewalDate,
                    epochMillis = nowMillis + 3L * 86_400_000L,
                    isCompleted = false,
                    reminderEnabled = sub.reminderEnabled,
                    details = "${sub.billingCycle} subscription (${sub.paymentMethod})"
                )
            )
        }
        KharcheeProfileAndToolsStore.getEmis(context, userId).forEach { emi ->
            events.add(
                MoneyTimelineEvent(
                    id = "emi_${emi.id}",
                    title = "${emi.title} EMI",
                    eventType = "EMI",
                    amount = emi.monthlyEmi,
                    dateLabel = "05th Monthly",
                    epochMillis = nowMillis + 4L * 86_400_000L,
                    isCompleted = false,
                    reminderEnabled = true,
                    details = "${emi.remainingMonths} months remaining · Principal ₹${emi.totalPrincipal}"
                )
            )
        }
        paymentReminders.forEach { rem ->
            events.add(
                MoneyTimelineEvent(
                    id = "rem_${rem.id}",
                    title = rem.title,
                    eventType = "BILL",
                    amount = rem.amount,
                    dateLabel = dateFmt.format(Date(rem.dueEpochMillis)),
                    epochMillis = rem.dueEpochMillis,
                    isCompleted = rem.isPaid,
                    reminderEnabled = !rem.isPaid,
                    details = "Category: ${rem.category} · Note: ${rem.note.ifBlank { "Standard" }}"
                )
            )
        }
        savingsGoals.take(3).forEach { g ->
            val monthlyContribution = max(500L, (g.targetAmount - g.savedAmount) / 6L)
            events.add(
                MoneyTimelineEvent(
                    id = "goal_${g.id}",
                    title = "${g.title} Contribution",
                    eventType = "SAVINGS",
                    amount = monthlyContribution,
                    dateLabel = "10th Scheduled",
                    epochMillis = nowMillis + 5L * 86_400_000L,
                    isCompleted = g.savedAmount >= g.targetAmount,
                    reminderEnabled = true,
                    details = "Saved ₹${g.savedAmount} / ₹${g.targetAmount}"
                )
            )
        }
        val windowDays = if (viewMode.equals("Week", ignoreCase = true)) 7L else 31L
        val cutoffMillis = nowMillis - windowDays * 86_400_000L
        expenses.filter { it.txEpochMillis >= cutoffMillis }.take(15).forEach { tx ->
            val isInc = isIncomeTransaction(tx)
            events.add(
                MoneyTimelineEvent(
                    id = "tx_${tx.id}",
                    title = tx.title,
                    eventType = if (isInc) "INCOME" else "EXPENSE",
                    amount = tx.amount,
                    dateLabel = dateFmt.format(Date(tx.txEpochMillis)),
                    epochMillis = tx.txEpochMillis,
                    isCompleted = true,
                    reminderEnabled = false,
                    details = "${tx.category} · ${tx.paymentMode}"
                )
            )
        }
        return events.sortedByDescending { it.epochMillis }
    }

    // 2. PURCHASE DECISION ASSISTANT
    fun evaluatePurchaseDecision(
        productName: String,
        purchasePrice: Long,
        availableBudget: Long,
        urgency: String,
        goalName: String = "",
        goalTarget: Long = 0L,
        goalSaved: Long = 0L,
        dailySavingCapacity: Long = 300L
    ): PurchaseDecisionEvaluation {
        val safePrice = max(0L, purchasePrice)
        val safeBudget = max(1L, availableBudget)
        val remainingAfter = availableBudget - safePrice
        val budgetPct = ((safePrice.toDouble() / safeBudget.toDouble()) * 100.0).roundToInt()

        val goalRemaining = max(0L, goalTarget - goalSaved)
        val goalImpactPct = if (goalRemaining > 0L) {
            ((safePrice.toDouble() / goalRemaining.toDouble()) * 100.0).roundToInt()
        } else 0
        val delayDays = if (goalName.isNotBlank() && dailySavingCapacity > 0L) {
            ceil(safePrice.toDouble() / dailySavingCapacity.toDouble()).toInt()
        } else 0

        val (verdict, waitDays) = when {
            urgency.equals("Essential", ignoreCase = true) && remainingAfter >= 0L -> "EASILY AFFORDABLE" to 0
            budgetPct <= 30 && remainingAfter >= 0L -> "EASILY AFFORDABLE" to 0
            budgetPct <= 70 && remainingAfter >= 0L -> "MODERATE STRETCH" to (if (urgency == "Low") 7 else 3)
            else -> "WAIT RECOMMENDED" to max(7, ceil(max(0L, safePrice - availableBudget).toDouble() / max(100L, dailySavingCapacity).toDouble()).toInt().coerceAtMost(60))
        }

        val cleanName = productName.trim().ifBlank { "This item" }
        val summary = when (verdict) {
            "EASILY AFFORDABLE" -> "$cleanName (₹$safePrice) uses $budgetPct% of your available budget and leaves ₹$remainingAfter cushion."
            "MODERATE STRETCH" -> "$cleanName takes $budgetPct% of your remaining budget. Waiting $waitDays days helps protect your cash flow."
            else -> "$cleanName exceeds safe budget thresholds ($budgetPct% of budget). Waiting $waitDays days avoids derailing your savings."
        }
        return PurchaseDecisionEvaluation(
            productName = cleanName,
            purchasePrice = safePrice,
            availableBudget = availableBudget,
            remainingBudgetAfter = remainingAfter,
            budgetImpactPercent = budgetPct,
            goalName = goalName,
            goalDelayDays = delayDays,
            goalImpactPercent = goalImpactPct,
            affordabilityVerdict = verdict,
            suggestedWaitDays = waitDays,
            recommendationSummary = summary
        )
    }

    // 3. EMERGENCY FUND METER
    fun getEmergencyTargetMonths(context: Context, userId: String): Int =
        prefs(context).getInt("${userId}_ef_target_months", 6).coerceIn(1, 24)

    fun setEmergencyTargetMonths(context: Context, userId: String, months: Int) {
        prefs(context).edit().putInt("${userId}_ef_target_months", months.coerceIn(1, 24)).apply()
    }

    fun getCustomEmergencyTargetAmount(context: Context, userId: String): Long =
        prefs(context).getLong("${userId}_ef_custom_target", 0L)

    fun setCustomEmergencyTargetAmount(context: Context, userId: String, amount: Long) {
        prefs(context).edit().putLong("${userId}_ef_custom_target", max(0L, amount)).apply()
    }

    fun calculateEmergencyFundMetrics(
        manualEmergencySavings: Long,
        savingsGoals: List<SavingsGoalRecord>,
        expenses: List<ExpenseRecord>,
        targetMonths: Int = 6,
        customTargetOverride: Long = 0L,
        nowMillis: Long = System.currentTimeMillis()
    ): EmergencyFundMetrics {
        val dreamEmergency = savingsGoals
            .filter { it.title.contains("emergency", ignoreCase = true) }
            .sumOf { it.savedAmount }
        val totalEmergency = max(0L, manualEmergencySavings) + dreamEmergency

        val totalIn = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val totalOut = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val verifiedCash = totalIn - totalOut

        val essentialCats = setOf("Food", "Bills", "Health", "Education", "Travel", "Groceries", "Rent")
        val monthEssentials = expenses
            .filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, nowMillis) && it.category in essentialCats }
            .sumOf { it.amount }
        val effectiveMonthlyEssential = if (monthEssentials > 0L) monthEssentials else 15000L

        val monthsCovered = ((totalEmergency.toDouble() / effectiveMonthlyEssential.toDouble()) * 10.0).roundToInt() / 10.0
        val targetAmt = if (customTargetOverride > 0L) customTargetOverride else effectiveMonthlyEssential * targetMonths.coerceAtLeast(1)
        val remaining = max(0L, targetAmt - totalEmergency)
        val pct = if (targetAmt > 0L) ((totalEmergency.toDouble() / targetAmt.toDouble()) * 100.0).roundToInt().coerceIn(0, 100) else 0

        return EmergencyFundMetrics(
            manualEmergencySavings = manualEmergencySavings,
            dreamVaultEmergencySavings = dreamEmergency,
            totalEmergencySavings = totalEmergency,
            verifiedLedgerCashBalance = verifiedCash,
            monthlyEssentialExpenses = effectiveMonthlyEssential,
            monthsCovered = monthsCovered,
            targetMonths = targetMonths,
            userDefinedTargetAmount = targetAmt,
            remainingTargetAmount = remaining,
            progressPercent = pct
        )
    }

    // 4. SALARY DAY SIMULATOR
    fun simulateSalaryAllocation(monthlyIncome: Long, scenario: SalaryAllocationScenario): SalarySimulationResult {
        val inc = max(0L, monthlyIncome)
        val ess = (inc * scenario.essentialPct) / 100L
        val sav = (inc * scenario.savingsPct) / 100L
        val emi = (inc * scenario.emiPct) / 100L
        val bil = (inc * scenario.billsPct) / 100L
        val sub = (inc * scenario.subscriptionsPct) / 100L
        val per = (inc * scenario.personalPct) / 100L
        val goa = (inc * scenario.goalsPct) / 100L
        val totalPct = scenario.essentialPct + scenario.savingsPct + scenario.emiPct +
            scenario.billsPct + scenario.subscriptionsPct + scenario.personalPct + scenario.goalsPct
        val unalloc = inc - (ess + sav + emi + bil + sub + per + goa)
        return SalarySimulationResult(
            monthlyIncome = inc,
            scenarioName = scenario.name,
            essentialAmount = ess,
            savingsAmount = sav,
            emiAmount = emi,
            billsAmount = bil,
            subscriptionsAmount = sub,
            personalAmount = per,
            goalsAmount = goa,
            totalAllocatedPct = totalPct,
            unallocatedAmount = unalloc
        )
    }

    // 5. WHERE DID MY MONEY GO?
    fun analyzeWhereMoneyWent(
        expenses: List<ExpenseRecord>,
        subscriptions: List<SubscriptionItem>,
        nowMillis: Long = System.currentTimeMillis()
    ): WhereMoneyWentAnalysis {
        val outflows = expenses.filter { !isIncomeTransaction(it) }
        val thisMonthTx = outflows.filter { isSameMonth(it.txEpochMillis, nowMillis) }
        val calPrev = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MONTH, -1)
        }
        val prevMonthTx = outflows.filter { isSameMonth(it.txEpochMillis, calPrev.timeInMillis) }

        val totalThis = thisMonthTx.sumOf { it.amount }
        val totalPrev = prevMonthTx.sumOf { it.amount }

        val catSpending = thisMonthTx.groupBy { it.category }
            .map { it.key to it.value.sumOf { tx -> tx.amount } }
            .sortedByDescending { it.second }

        val merchantSpending = thisMonthTx.groupBy { it.title.trim().ifBlank { "General" } }
            .map { it.key to it.value.sumOf { tx -> tx.amount } }
            .sortedByDescending { it.second }

        val dayFmt = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val highestDays = thisMonthTx.groupBy { dayFmt.format(Date(it.txEpochMillis)) }
            .map { it.key to it.value.sumOf { tx -> tx.amount } }
            .sortedByDescending { it.second }

        val recurring = subscriptions.filter { it.isActive }.map { it.name to it.amount }
        val avgTicket = if (thisMonthTx.isNotEmpty()) thisMonthTx.map { it.amount }.average() else 0.0
        val unusual = thisMonthTx.filter { it.amount > max(1000.0, avgTicket * 2.0) }

        val prevByCat = prevMonthTx.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
        val allCats = (catSpending.map { it.first } + prevByCat.keys).distinct()
        val biggestChanges = allCats.map { c ->
            val cur = catSpending.find { it.first == c }?.second ?: 0L
            val prv = prevByCat[c] ?: 0L
            c to (cur - prv)
        }.sortedByDescending { abs(it.second) }

        val topCat = catSpending.firstOrNull()
        val summary = if (thisMonthTx.isEmpty()) {
            "No expenses recorded this month yet."
        } else {
            "You spent ₹$totalThis across ${thisMonthTx.size} transactions this month. Top category: ${topCat?.first ?: "N/A"} (₹${topCat?.second ?: 0L})."
        }

        return WhereMoneyWentAnalysis(
            totalThisMonth = totalThis,
            totalPrevMonth = totalPrev,
            categorySpending = catSpending,
            merchantSpending = merchantSpending,
            highestSpendingDays = highestDays,
            recurringExpenses = recurring,
            unusualTransactions = unusual,
            biggestCategoryChanges = biggestChanges,
            naturalLanguageSummary = summary
        )
    }

    // 6. ASK YOUR MONEY (Feature 7)
    fun answerAskYourMoneyQuery(
        query: String,
        expenses: List<ExpenseRecord>,
        subscriptions: List<SubscriptionItem>,
        savingsGoals: List<SavingsGoalRecord>,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        val q = query.trim().lowercase(Locale.US)
        val monthOutflows = expenses.filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, nowMillis) }
        val monthIncomes = expenses.filter { isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, nowMillis) }

        return when {
            q.contains("food") || q.contains("khana") -> {
                val foodSpent = monthOutflows.filter { it.category.equals("Food", ignoreCase = true) }.sumOf { it.amount }
                "You spent ₹$foodSpent on Food this month across ${monthOutflows.count { it.category.equals("Food", ignoreCase = true) }} transactions."
            }
            q.contains("biggest") || q.contains("highest") || q.contains("sabse bada") -> {
                val biggest = monthOutflows.maxByOrNull { it.amount }
                if (biggest != null) {
                    "Your biggest expense this month was '${biggest.title}' for ₹${biggest.amount} (${biggest.category})."
                } else {
                    "You have no recorded expenses this month."
                }
            }
            q.contains("save") || q.contains("bachat") -> {
                val netSaved = monthIncomes.sumOf { it.amount } - monthOutflows.sumOf { it.amount }
                val vaultSaved = savingsGoals.sumOf { it.savedAmount }
                "This month's net cash flow savings is ₹$netSaved (Income ₹${monthIncomes.sumOf { it.amount }} - Expenses ₹${monthOutflows.sumOf { it.amount }}). Total saved in Dream Vault goals: ₹$vaultSaved."
            }
            q.contains("subscription") -> {
                val activeSubs = subscriptions.filter { it.isActive }
                if (activeSubs.isEmpty()) "You have no active subscriptions recorded."
                else "Upcoming active subscriptions (${activeSubs.size}): ${activeSubs.joinToString { "${it.name} (₹${it.amount} on ${it.nextRenewalDate})" }}."
            }
            q.contains("compare") || q.contains("last month") -> {
                val analysis = analyzeWhereMoneyWent(expenses, subscriptions, nowMillis)
                val diff = analysis.totalThisMonth - analysis.totalPrevMonth
                "This month you spent ₹${analysis.totalThisMonth} compared to ₹${analysis.totalPrevMonth} last month (${if (diff >= 0) "+₹$diff" else "-₹${-diff}"})."
            }
            else -> {
                val totalSpent = monthOutflows.sumOf { it.amount }
                "Based on your passbook: This month's total expenses are ₹$totalSpent across ${monthOutflows.size} transactions, and income is ₹${monthIncomes.sumOf { it.amount }}."
            }
        }
    }

    fun getAskYourMoneyHistory(context: Context, userId: String): List<AskYourMoneyTurn> {
        val raw = prefs(context).getString("${userId}_ask_history", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                AskYourMoneyTurn(
                    id = o.optString("id"),
                    question = o.optString("question"),
                    answer = o.optString("answer"),
                    timestampMillis = o.optLong("timestampMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun recordAskYourMoneyTurn(context: Context, userId: String, question: String, answer: String): List<AskYourMoneyTurn> {
        val current = getAskYourMoneyHistory(context, userId).toMutableList()
        current.add(0, AskYourMoneyTurn(question = question, answer = answer))
        val arr = JSONArray()
        current.take(20).forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("question", it.question)
                put("answer", it.answer)
                put("timestampMillis", it.timestampMillis)
            })
        }
        prefs(context).edit().putString("${userId}_ask_history", arr.toString()).apply()
        return current.take(20)
    }

    // 7. SAVINGS CHALLENGES (Feature 9)
    fun getSavingsChallenges(context: Context, userId: String): List<SavingsChallengeRecord> {
        val raw = prefs(context).getString("${userId}_challenges_json", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                SavingsChallengeRecord(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    durationDays = o.optInt("durationDays", 30),
                    targetAmount = o.optLong("targetAmount", 5000L),
                    savedAmount = o.optLong("savedAmount", 0L),
                    targetDate = o.optString("targetDate", "30 Nov 2026"),
                    contributionsCount = o.optInt("contributionsCount", 0),
                    reminderEnabled = o.optBoolean("reminderEnabled", true),
                    completed = o.optBoolean("completed", false)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createSavingsChallenge(
        context: Context,
        userId: String,
        title: String,
        durationDays: Int,
        targetAmount: Long,
        targetDate: String,
        reminderEnabled: Boolean
    ): List<SavingsChallengeRecord> {
        val list = getSavingsChallenges(context, userId).toMutableList()
        list.add(
            0,
            SavingsChallengeRecord(
                title = title.trim().ifBlank { "$durationDays-Day Challenge" },
                durationDays = durationDays,
                targetAmount = max(100L, targetAmount),
                savedAmount = 0L,
                targetDate = targetDate,
                contributionsCount = 0,
                reminderEnabled = reminderEnabled,
                completed = false
            )
        )
        persistChallenges(context, userId, list)
        return list
    }

    fun contributeToSavingsChallenge(context: Context, userId: String, challengeId: String, amount: Long): List<SavingsChallengeRecord> {
        val list = getSavingsChallenges(context, userId).map { ch ->
            if (ch.id == challengeId) {
                val newSaved = ch.savedAmount + max(0L, amount)
                ch.copy(
                    savedAmount = newSaved,
                    contributionsCount = ch.contributionsCount + 1,
                    completed = newSaved >= ch.targetAmount
                )
            } else ch
        }
        persistChallenges(context, userId, list)
        return list
    }

    private fun persistChallenges(context: Context, userId: String, list: List<SavingsChallengeRecord>) {
        val arr = JSONArray()
        list.forEach { ch ->
            arr.put(JSONObject().apply {
                put("id", ch.id)
                put("title", ch.title)
                put("durationDays", ch.durationDays)
                put("targetAmount", ch.targetAmount)
                put("savedAmount", ch.savedAmount)
                put("targetDate", ch.targetDate)
                put("contributionsCount", ch.contributionsCount)
                put("reminderEnabled", ch.reminderEnabled)
                put("completed", ch.completed)
            })
        }
        prefs(context).edit().putString("${userId}_challenges_json", arr.toString()).apply()
    }

    // 8. KHARCHEE WRAPPED
    fun computeKharcheeWrapped(
        periodType: String,
        expenses: List<ExpenseRecord>,
        savingsGoals: List<SavingsGoalRecord>,
        streakDays: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): KharcheeWrappedSummary {
        val relevantTx = if (periodType == "YEARLY") expenses else expenses.filter { isSameMonth(it.txEpochMillis, nowMillis) }
        val inc = relevantTx.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val out = relevantTx.filter { !isIncomeTransaction(it) }
        val expTotal = out.sumOf { it.amount }
        val byCat = out.groupBy { it.category }
        val mostUsed = byCat.maxByOrNull { it.value.size }?.key ?: "None"
        val highestCat = byCat.maxByOrNull { it.value.sumOf { tx -> tx.amount } }
        val savedTotal = savingsGoals.sumOf { it.savedAmount } + max(0L, inc - expTotal)

        return KharcheeWrappedSummary(
            periodType = periodType,
            periodTitle = if (periodType == "YEARLY") "2026 Annual Recap" else SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(nowMillis)),
            totalIncome = inc,
            totalExpenses = expTotal,
            savingsProgress = savedTotal,
            mostUsedCategory = mostUsed,
            highestSpendingCategory = highestCat?.key ?: "None",
            highestSpendingCategoryAmount = highestCat?.value?.sumOf { it.amount } ?: 0L,
            trackingStreakDays = streakDays,
            unlockedAchievements = listOfNotNull(
                if (streakDays >= 7) "7d Streak Master" else "Discipline Starter",
                if (savingsGoals.isNotEmpty()) "Dream Vault Architect" else null,
                if (inc > expTotal) "Cashflow Positive" else null
            ),
            changeFromPrevPeriodPercent = 0
        )
    }

    // 9. FINANCIAL DATA CENTER
    fun getExportHistory(context: Context, userId: String): List<DataExportRecord> {
        val raw = prefs(context).getString("${userId}_export_history", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                DataExportRecord(
                    id = o.optString("id"),
                    format = o.optString("format"),
                    recordCount = o.optInt("recordCount"),
                    exportedAtMillis = o.optLong("exportedAtMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun recordDataExport(context: Context, userId: String, format: String, recordCount: Int) {
        val list = getExportHistory(context, userId).toMutableList()
        list.add(0, DataExportRecord(format = format, recordCount = recordCount))
        val arr = JSONArray()
        list.take(20).forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("format", it.format)
                put("recordCount", it.recordCount)
                put("exportedAtMillis", it.exportedAtMillis)
            })
        }
        prefs(context).edit().putString("${userId}_export_history", arr.toString()).apply()
    }

    fun buildVaultBackupJson(
        context: Context,
        userId: String,
        expenses: List<ExpenseRecord>,
        savingsGoals: List<SavingsGoalRecord>
    ): String {
        recordDataExport(context, userId, "JSON Full Backup", expenses.size + savingsGoals.size)
        return JSONObject().apply {
            put("app", "KHARCHEE")
            put("userId", userId)
            put("transactionsCount", expenses.size)
            put("goalsCount", savingsGoals.size)
        }.toString(2)
    }

    // 10. FAMILY WALLET
    fun getFamilyWallet(context: Context, userId: String, ownerName: String): FamilyWalletState {
        val p = prefs(context)
        val groupName = p.getString("${userId}_fam_group_name", "$ownerName's Family Hub") ?: "$ownerName's Family Hub"
        val budget = p.getLong("${userId}_fam_budget", 40000L)
        val membersRaw = p.getString("${userId}_fam_members", null)
        val members = if (membersRaw == null) {
            listOf(FamilyMemberItem(id = "owner", name = ownerName, role = "Admin", contributedAmount = 0L))
        } else {
            try {
                val arr = JSONArray(membersRaw)
                (0 until arr.length()).mapNotNull { i ->
                    val o = arr.optJSONObject(i) ?: return@mapNotNull null
                    FamilyMemberItem(o.optString("id"), o.optString("name"), o.optString("role"), o.optLong("contributedAmount"))
                }
            } catch (_: Exception) {
                listOf(FamilyMemberItem(id = "owner", name = ownerName, role = "Admin", contributedAmount = 0L))
            }
        }
        val expRaw = p.getString("${userId}_fam_expenses", "[]") ?: "[]"
        val sharedExp = try {
            val arr = JSONArray(expRaw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                FamilySharedExpenseItem(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    amount = o.optLong("amount"),
                    category = o.optString("category"),
                    paidBy = o.optString("paidBy"),
                    splitMode = o.optString("splitMode"),
                    perMemberShare = o.optLong("perMemberShare"),
                    createdAtMillis = o.optLong("createdAtMillis")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
        return FamilyWalletState(groupName, budget, members, sharedExp)
    }

    fun addFamilyMember(context: Context, userId: String, ownerName: String, memberName: String, role: String): FamilyWalletState {
        val state = getFamilyWallet(context, userId, ownerName)
        val updatedMembers = state.members + FamilyMemberItem(name = memberName.trim(), role = role)
        saveFamilyWallet(context, userId, state.copy(members = updatedMembers))
        return state.copy(members = updatedMembers)
    }

    fun addFamilySharedExpense(
        context: Context,
        userId: String,
        ownerName: String,
        title: String,
        amount: Long,
        category: String,
        paidBy: String,
        splitMode: String
    ): FamilyWalletState {
        val state = getFamilyWallet(context, userId, ownerName)
        val memberCount = max(1, state.members.size)
        val share = amount / memberCount
        val newExp = FamilySharedExpenseItem(
            title = title.trim(),
            amount = amount,
            category = category,
            paidBy = paidBy,
            splitMode = splitMode,
            perMemberShare = share
        )
        val updatedMembers = state.members.map { m ->
            if (m.name.equals(paidBy, ignoreCase = true)) m.copy(contributedAmount = m.contributedAmount + amount) else m
        }
        val next = state.copy(members = updatedMembers, sharedExpenses = listOf(newExp) + state.sharedExpenses)
        saveFamilyWallet(context, userId, next)
        return next
    }

    private fun saveFamilyWallet(context: Context, userId: String, state: FamilyWalletState) {
        val memArr = JSONArray()
        state.members.forEach { m ->
            memArr.put(JSONObject().apply {
                put("id", m.id); put("name", m.name); put("role", m.role); put("contributedAmount", m.contributedAmount)
            })
        }
        val expArr = JSONArray()
        state.sharedExpenses.forEach { e ->
            expArr.put(JSONObject().apply {
                put("id", e.id); put("title", e.title); put("amount", e.amount); put("category", e.category)
                put("paidBy", e.paidBy); put("splitMode", e.splitMode); put("perMemberShare", e.perMemberShare)
                put("createdAtMillis", e.createdAtMillis)
            })
        }
        prefs(context).edit()
            .putString("${userId}_fam_group_name", state.groupName)
            .putLong("${userId}_fam_budget", state.sharedMonthlyBudget)
            .putString("${userId}_fam_members", memArr.toString())
            .putString("${userId}_fam_expenses", expArr.toString())
            .apply()
    }
}
