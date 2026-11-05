package com.example.data.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

// ============================================================================
// KHARCHEE — SMART FIRST-TIME ONBOARDING & PERSONAL FINANCIAL PROFILE STORE
// ============================================================================

enum class OnboardingLifecycleStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED_FOR_LATER,
    COMPLETED,
    EXISTING_USER_BYPASSED
}

val ONBOARDING_OCCUPATIONS = listOf(
    "Student",
    "Salaried",
    "Self-employed",
    "Freelancer",
    "Business Owner",
    "Other"
)

val ONBOARDING_AGE_RANGES = listOf(
    "", // Optional / Prefer not to say
    "Under 18",
    "18–24",
    "25–34",
    "35–44",
    "45–54",
    "55+"
)

val ONBOARDING_LANGUAGES = listOf(
    "English",
    "Hinglish",
    "Hindi"
)

val ONBOARDING_CURRENCIES = listOf(
    "INR (₹)",
    "USD ($)",
    "EUR (€)",
    "GBP (£)",
    "AED (د.إ)"
)

val ONBOARDING_INCOME_FREQUENCIES = listOf(
    "Monthly",
    "Weekly"
)

val ONBOARDING_STANDARD_EXPENSE_CATEGORIES = listOf(
    "Rent or housing",
    "Food and groceries",
    "Transport",
    "Electricity and utilities",
    "Education",
    "Medical expenses",
    "EMIs and loans",
    "Subscriptions",
    "Family expenses",
    "Other expenses"
)

val ONBOARDING_STANDARD_GOAL_TYPES = listOf(
    "Emergency fund",
    "Education",
    "Laptop or phone",
    "Travel",
    "Vehicle",
    "Other custom goals"
)

data class PersonalProfileSection(
    val fullName: String = "",
    val ageRange: String = "", // Optional
    val occupation: String = "Salaried",
    val preferredLanguage: String = "English",
    val currency: String = "INR (₹)",
    val incomeFrequency: String = "Monthly"
)

data class DeclaredAdditionalIncome(
    val id: String = UUID.randomUUID().toString(),
    val sourceName: String,
    val estimatedMonthlyAmount: Long,
    val frequency: String = "Monthly",
    val isVariable: Boolean = true
)

data class IncomeProfileSection(
    val primaryIncomeSource: String = "",
    val primaryMonthlyIncomeAmount: Long? = null, // null = unknown / skipped
    val salaryOrPaymentDay: Int = 1, // 1..31
    val incomeFrequency: String = "Monthly",
    val isVariableIncome: Boolean = false,
    val additionalIncomeSources: List<DeclaredAdditionalIncome> = emptyList()
)

data class FinancialPositionSection(
    val cashBalance: Long? = null,
    val bankBalance: Long? = null,
    val walletBalance: Long? = null,
    val existingSavings: Long? = null,
    val investmentsBalance: Long? = null,
    val outstandingLoansAndLiabilities: Long? = null
) {
    val securityAndSelfDeclaredLabel: String
        get() = "Self-Declared Starting Balances (Not Verified Bank Balances). KHARCHEE never asks for bank passwords, UPI PINs, OTPs, or card PINs."
}

data class MonthlyExpenseProfileSection(
    val standardCategoryEstimates: Map<String, Long?> = emptyMap(), // category -> optional estimated monthly amount
    val customCategoryEstimates: Map<String, Long> = emptyMap()
)

data class DeclaredFinancialGoal(
    val id: String = UUID.randomUUID().toString(),
    val goalType: String, // Emergency fund, Education, Laptop or phone, Travel, Vehicle, Other custom goals
    val title: String,
    val targetAmount: Long,
    val existingSavedAmount: Long = 0L
)

data class FinancialGoalsAndConsentSection(
    val goals: List<DeclaredFinancialGoal> = emptyList(),
    val consentNotifications: Boolean = false,
    val consentOptionalIntegrations: Boolean = false,
    val acknowledgedDataUsageNotice: Boolean = true
)

data class UserFinancialOnboardingProfile(
    val userId: String,
    val status: OnboardingLifecycleStatus = OnboardingLifecycleStatus.NOT_STARTED,
    val currentStepIndex: Int = 0, // 0..5 (6 steps total: 0=Personal, 1=Income, 2=Position, 3=Expenses, 4=Goals/Consent, 5=Summary/Confirm)
    val personal: PersonalProfileSection = PersonalProfileSection(),
    val income: IncomeProfileSection = IncomeProfileSection(),
    val financialPosition: FinancialPositionSection = FinancialPositionSection(),
    val monthlyExpenses: MonthlyExpenseProfileSection = MonthlyExpenseProfileSection(),
    val goalsAndConsent: FinancialGoalsAndConsentSection = FinancialGoalsAndConsentSection(),
    val lastUpdatedEpochMillis: Long = 0L,
    val confirmedAtEpochMillis: Long = 0L
)

data class PersonalisedFinancialPlan(
    val isIncomeKnown: Boolean,
    val isExpensesKnown: Boolean,
    val isIncompleteState: Boolean,
    val incompleteStateExplanation: String,
    val totalDeclaredMonthlyIncome: Long,
    val actualReceivedMonthlyIncome: Long, // Strictly from real logged income transactions
    val estimatedMonthlyExpenses: Long,
    val actualRecordedMonthlyExpenses: Long, // Strictly from real logged expense transactions
    val estimatedRemainingAmount: Long?, // Null when income or expenses are unknown (never invented)
    val existingDeclaredLiquidSavings: Long,
    val existingDeclaredInvestments: Long,
    val existingDeclaredTotalSavings: Long,
    val totalDeclaredLiabilities: Long,
    val estimatedEmergencyFundCoverageMonths: Double?, // Null if estimated monthly expenses are 0 or unknown
    val recommendedMonthlyBudget: Long?,
    val recommendedDailyLimit: Long?,
    val budgetRecommendations: List<String>,
    val goalContributionSuggestions: Map<String, Long>, // goalTitle -> suggested monthly contribution
    val proModuleRecommendations: List<String>
)

object KharcheeOnboardingProfileStore {
    private const val PREFS_NAME = "kharchee_onboarding_financial_profile_store"
    const val TOTAL_ONBOARDING_STEPS = 6

    const val DATA_USAGE_EXPLANATION =
        "How KHARCHEE Uses Your Financial Information: Your profile answers are stored privately in your user-scoped workspace to personalise your Dashboard, Budget Coach, JARVIS assistant, Money Timeline, and KHARCHEE PRO analytics. Declared estimates are never treated as completed transactions, and your financial data is never shared with other users or third-party advertisers."

    const val ZERO_CREDENTIAL_SECURITY_NOTICE =
        "Security Guarantee: KHARCHEE never requests bank passwords, UPI PINs, OTPs, or card PINs. All balances entered here are user-declared estimates."

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Checks whether the user should be shown the guided first-time onboarding flow.
     * - Existing users who already have recorded transactions, goals, or Khata entries are automatically
     *   marked as EXISTING_USER_BYPASSED so they retain their data and are never forced through new-user onboarding.
     * - Users who paused onboarding ("Save & Continue Later") are NOT forced immediately on screen load
     *   if they chose to continue later, or they can resume from the prominent banner at the exact saved step.
     */
    fun shouldShowGuidedOnboardingOnLaunch(
        context: Context,
        userId: String,
        existingExpensesCount: Int,
        existingGoalsCount: Int,
        existingKhataCount: Int
    ): Boolean {
        if (userId.isBlank()) return false
        val profile = getOnboardingProfile(context, userId)
        if (profile.status == OnboardingLifecycleStatus.COMPLETED ||
            profile.status == OnboardingLifecycleStatus.EXISTING_USER_BYPASSED
        ) {
            return false
        }
        // If user already has existing financial data in their workspace, preserve their existing experience
        if (existingExpensesCount > 0 || existingGoalsCount > 0 || existingKhataCount > 0) {
            if (profile.status == OnboardingLifecycleStatus.NOT_STARTED) {
                val bypassed = profile.copy(
                    status = OnboardingLifecycleStatus.EXISTING_USER_BYPASSED,
                    lastUpdatedEpochMillis = System.currentTimeMillis()
                )
                saveOnboardingProfile(context, userId, bypassed)
                return false
            }
        }
        // Show guided onboarding automatically if NOT_STARTED or IN_PROGRESS (unless explicitly paused for later)
        return profile.status == OnboardingLifecycleStatus.NOT_STARTED ||
            profile.status == OnboardingLifecycleStatus.IN_PROGRESS
    }

    fun getOnboardingProfile(context: Context, userId: String, defaultDisplayName: String = ""): UserFinancialOnboardingProfile {
        val raw = prefs(context).getString("${userId}_onboarding_profile", null)
        if (raw.isNullOrBlank()) {
            return UserFinancialOnboardingProfile(
                userId = userId,
                personal = PersonalProfileSection(fullName = defaultDisplayName)
            )
        }
        return try {
            val obj = JSONObject(raw)
            val storedOwner = obj.optString("userId", userId)
            if (storedOwner != userId) {
                // Enforce strict user isolation
                return UserFinancialOnboardingProfile(userId = userId)
            }
            val status = runCatching {
                OnboardingLifecycleStatus.valueOf(obj.optString("status", OnboardingLifecycleStatus.NOT_STARTED.name))
            }.getOrDefault(OnboardingLifecycleStatus.NOT_STARTED)

            val pObj = obj.optJSONObject("personal") ?: JSONObject()
            val personal = PersonalProfileSection(
                fullName = pObj.optString("fullName", defaultDisplayName),
                ageRange = pObj.optString("ageRange", ""),
                occupation = pObj.optString("occupation", "Salaried"),
                preferredLanguage = pObj.optString("preferredLanguage", "English"),
                currency = pObj.optString("currency", "INR (₹)"),
                incomeFrequency = pObj.optString("incomeFrequency", "Monthly")
            )

            val iObj = obj.optJSONObject("income") ?: JSONObject()
            val addArr = iObj.optJSONArray("additionalIncomeSources") ?: JSONArray()
            val addSources = (0 until addArr.length()).map { idx ->
                val ao = addArr.getJSONObject(idx)
                DeclaredAdditionalIncome(
                    id = ao.optString("id", UUID.randomUUID().toString()),
                    sourceName = ao.optString("sourceName", ""),
                    estimatedMonthlyAmount = max(0L, ao.optLong("estimatedMonthlyAmount", 0L)),
                    frequency = ao.optString("frequency", "Monthly"),
                    isVariable = ao.optBoolean("isVariable", true)
                )
            }
            val primaryAmt = if (iObj.has("primaryMonthlyIncomeAmount") && !iObj.isNull("primaryMonthlyIncomeAmount")) {
                max(0L, iObj.optLong("primaryMonthlyIncomeAmount", 0L))
            } else null

            val income = IncomeProfileSection(
                primaryIncomeSource = iObj.optString("primaryIncomeSource", ""),
                primaryMonthlyIncomeAmount = primaryAmt,
                salaryOrPaymentDay = iObj.optInt("salaryOrPaymentDay", 1).coerceIn(1, 31),
                incomeFrequency = iObj.optString("incomeFrequency", "Monthly"),
                isVariableIncome = iObj.optBoolean("isVariableIncome", false),
                additionalIncomeSources = addSources
            )

            val fpObj = obj.optJSONObject("financialPosition") ?: JSONObject()
            fun optNullableLong(o: JSONObject, key: String): Long? =
                if (o.has(key) && !o.isNull(key)) max(0L, o.optLong(key, 0L)) else null

            val financialPosition = FinancialPositionSection(
                cashBalance = optNullableLong(fpObj, "cashBalance"),
                bankBalance = optNullableLong(fpObj, "bankBalance"),
                walletBalance = optNullableLong(fpObj, "walletBalance"),
                existingSavings = optNullableLong(fpObj, "existingSavings"),
                investmentsBalance = optNullableLong(fpObj, "investmentsBalance"),
                outstandingLoansAndLiabilities = optNullableLong(fpObj, "outstandingLoansAndLiabilities")
            )

            val expObj = obj.optJSONObject("monthlyExpenses") ?: JSONObject()
            val stdObj = expObj.optJSONObject("standardCategoryEstimates") ?: JSONObject()
            val stdMap = mutableMapOf<String, Long?>()
            stdObj.keys().forEach { k ->
                stdMap[k] = if (!stdObj.isNull(k)) max(0L, stdObj.optLong(k, 0L)) else null
            }
            val custObj = expObj.optJSONObject("customCategoryEstimates") ?: JSONObject()
            val custMap = mutableMapOf<String, Long>()
            custObj.keys().forEach { k ->
                custMap[k] = max(0L, custObj.optLong(k, 0L))
            }
            val monthlyExpenses = MonthlyExpenseProfileSection(
                standardCategoryEstimates = stdMap,
                customCategoryEstimates = custMap
            )

            val gcObj = obj.optJSONObject("goalsAndConsent") ?: JSONObject()
            val gArr = gcObj.optJSONArray("goals") ?: JSONArray()
            val goals = (0 until gArr.length()).map { idx ->
                val go = gArr.getJSONObject(idx)
                DeclaredFinancialGoal(
                    id = go.optString("id", UUID.randomUUID().toString()),
                    goalType = go.optString("goalType", "Emergency fund"),
                    title = go.optString("title", "Emergency Fund"),
                    targetAmount = max(0L, go.optLong("targetAmount", 0L)),
                    existingSavedAmount = max(0L, go.optLong("existingSavedAmount", 0L))
                )
            }
            val goalsAndConsent = FinancialGoalsAndConsentSection(
                goals = goals,
                consentNotifications = gcObj.optBoolean("consentNotifications", false),
                consentOptionalIntegrations = gcObj.optBoolean("consentOptionalIntegrations", false),
                acknowledgedDataUsageNotice = gcObj.optBoolean("acknowledgedDataUsageNotice", true)
            )

            UserFinancialOnboardingProfile(
                userId = userId,
                status = status,
                currentStepIndex = obj.optInt("currentStepIndex", 0).coerceIn(0, TOTAL_ONBOARDING_STEPS - 1),
                personal = personal,
                income = income,
                financialPosition = financialPosition,
                monthlyExpenses = monthlyExpenses,
                goalsAndConsent = goalsAndConsent,
                lastUpdatedEpochMillis = obj.optLong("lastUpdatedEpochMillis", 0L),
                confirmedAtEpochMillis = obj.optLong("confirmedAtEpochMillis", 0L)
            )
        } catch (_: Exception) {
            UserFinancialOnboardingProfile(userId = userId, personal = PersonalProfileSection(fullName = defaultDisplayName))
        }
    }

    /**
     * Saves current onboarding progress (supporting "Save & Continue Later" and step navigation).
     */
    fun saveOnboardingDraftProgress(
        context: Context,
        userId: String,
        profile: UserFinancialOnboardingProfile,
        pauseForLater: Boolean = false
    ): UserFinancialOnboardingProfile {
        val nextStatus = when {
            profile.status == OnboardingLifecycleStatus.COMPLETED -> OnboardingLifecycleStatus.COMPLETED
            pauseForLater -> OnboardingLifecycleStatus.PAUSED_FOR_LATER
            else -> OnboardingLifecycleStatus.IN_PROGRESS
        }
        val updated = profile.copy(
            userId = userId,
            status = nextStatus,
            currentStepIndex = profile.currentStepIndex.coerceIn(0, TOTAL_ONBOARDING_STEPS - 1),
            lastUpdatedEpochMillis = System.currentTimeMillis()
        )
        saveOnboardingProfile(context, userId, updated)
        return updated
    }

    /**
     * Confirms and finalizes the onboarding profile, initialising the user's personalised settings
     * across Dashboard, Budget Coach, JARVIS, Goal Vault, Money Timeline, and KHARCHEE PRO
     * WITHOUT creating any fake/automatic transactions or overwriting existing transactions.
     */
    fun confirmAndActivateOnboardingProfile(
        context: Context,
        userId: String,
        profile: UserFinancialOnboardingProfile
    ): Pair<UserFinancialOnboardingProfile, PersonalisedFinancialPlan> {
        val now = System.currentTimeMillis()
        val confirmedProfile = profile.copy(
            userId = userId,
            status = OnboardingLifecycleStatus.COMPLETED,
            currentStepIndex = TOTAL_ONBOARDING_STEPS - 1,
            lastUpdatedEpochMillis = now,
            confirmedAtEpochMillis = now
        )
        saveOnboardingProfile(context, userId, confirmedProfile)

        val plan = computePersonalisedFinancialPlan(confirmedProfile, emptyList())

        // Initialise KharcheeProfileAndToolsStore metadata without overwriting any transactions
        val existingMeta = KharcheeProfileAndToolsStore.getProfileMetadata(
            context = context,
            userId = userId,
            defaultName = confirmedProfile.personal.fullName.ifBlank { "Kharchee Member" }
        )
        val resolvedMonthlyBudget = plan.recommendedMonthlyBudget ?: existingMeta.monthlyBudget
        KharcheeProfileAndToolsStore.saveProfileMetadata(
            context = context,
            userId = userId,
            displayName = confirmedProfile.personal.fullName.ifBlank { existingMeta.customDisplayName },
            phone = existingMeta.phone,
            profilePhotoUri = existingMeta.profilePhotoUri,
            monthlyBudget = resolvedMonthlyBudget,
            primaryWallet = existingMeta.primaryWallet,
            vaultPinProtectionEnabled = existingMeta.vaultPinProtectionEnabled,
            sharePrivacyHideBalances = existingMeta.sharePrivacyHideBalances
        )

        // Initialise manual wallet balances only where user explicitly entered values
        confirmedProfile.financialPosition.cashBalance?.let {
            KharcheeProfileAndToolsStore.updateManualWallet(context, userId, "Cash", it)
        }
        confirmedProfile.financialPosition.bankBalance?.let {
            KharcheeProfileAndToolsStore.updateManualWallet(context, userId, "Bank", it)
        }
        confirmedProfile.financialPosition.walletBalance?.let {
            KharcheeProfileAndToolsStore.updateManualWallet(context, userId, "UPI", it)
        }
        confirmedProfile.financialPosition.existingSavings?.let {
            KharcheeProfileAndToolsStore.setEmergencyFundSaved(context, userId, it)
        }

        // Map declared monthly expense estimates to Budget Coach category budgets where applicable
        val std = confirmedProfile.monthlyExpenses.standardCategoryEstimates
        std["Food and groceries"]?.takeIf { it > 0L }?.let {
            KharcheeProfileAndToolsStore.saveCategoryBudget(context, userId, "Food", (it * 0.6).roundToLong().coerceAtLeast(100L))
            KharcheeProfileAndToolsStore.saveCategoryBudget(context, userId, "Grocery", (it * 0.4).roundToLong().coerceAtLeast(100L))
        }
        std["Transport"]?.takeIf { it > 0L }?.let {
            KharcheeProfileAndToolsStore.saveCategoryBudget(context, userId, "Transport", it)
        }
        val billsTotal = listOfNotNull(
            std["Rent or housing"],
            std["Electricity and utilities"],
            std["Subscriptions"]
        ).sum()
        if (billsTotal > 0L) {
            KharcheeProfileAndToolsStore.saveCategoryBudget(context, userId, "Bills", billsTotal)
        }

        // Sync Money Timeline expected salary day and declared monthly income
        KharcheeEcosystemFeaturesStore.saveExpectedSalaryConfig(
            context = context,
            userId = userId,
            dayOfMonth = confirmedProfile.income.salaryOrPaymentDay,
            amount = plan.totalDeclaredMonthlyIncome
        )

        // If user is a Student and declared monthly income/allowance, initialise Module 41 Student Finance config
        if (confirmedProfile.personal.occupation.equals("Student", ignoreCase = true) && plan.totalDeclaredMonthlyIncome > 0L) {
            val studentCfg = KharcheeProModules41To50Store.getStudentConfig(context, userId)
            KharcheeProModules41To50Store.saveStudentConfig(
                context,
                userId,
                studentCfg.copy(monthlyAllowance = plan.totalDeclaredMonthlyIncome)
            )
        }

        return confirmedProfile to plan
    }

    /**
     * Core Personalisation Engine:
     * Uses strictly user-entered data. Never invents values when income or expenses are unknown.
     * Clearly distinguishes declared/estimated income & expenses from actual recorded transactions.
     */
    fun computePersonalisedFinancialPlan(
        profile: UserFinancialOnboardingProfile,
        actualRecordedTransactions: List<ExpenseRecord> = emptyList()
    ): PersonalisedFinancialPlan {
        val now = System.currentTimeMillis()
        val monthIncomeTx = actualRecordedTransactions
            .filter { isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, now) }
            .sumOf { it.amount }
        val monthExpenseTx = actualRecordedTransactions
            .filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, now) }
            .sumOf { it.amount }

        val primaryMonthly = profile.income.primaryMonthlyIncomeAmount?.let { raw ->
            if (profile.income.incomeFrequency.equals("Weekly", ignoreCase = true)) raw * 4L else raw
        }
        val additionalMonthly = profile.income.additionalIncomeSources.sumOf { src ->
            if (src.frequency.equals("Weekly", ignoreCase = true)) src.estimatedMonthlyAmount * 4L else src.estimatedMonthlyAmount
        }

        val isIncomeKnown = (primaryMonthly != null && primaryMonthly > 0L) || additionalMonthly > 0L
        val totalDeclaredMonthlyIncome = (primaryMonthly ?: 0L) + additionalMonthly

        val standardExpensesSum = profile.monthlyExpenses.standardCategoryEstimates.values.filterNotNull().sum()
        val customExpensesSum = profile.monthlyExpenses.customCategoryEstimates.values.sum()
        val estimatedMonthlyExpenses = standardExpensesSum + customExpensesSum
        val isExpensesKnown = estimatedMonthlyExpenses > 0L

        val isIncompleteState = !isIncomeKnown || !isExpensesKnown
        val incompleteStateExplanation = when {
            !isIncomeKnown && !isExpensesKnown ->
                "Income and monthly expense estimates were skipped. Enter your income or expense profile anytime to unlock full cashflow & emergency coverage projections."
            !isIncomeKnown ->
                "Monthly income estimate is not yet provided. Remaining cashflow and savings rate projections require a declared income."
            !isExpensesKnown ->
                "Monthly expense estimates were skipped. Emergency fund coverage and remaining surplus projections require estimated monthly expenses."
            else -> ""
        }

        // Never invent remaining amount if income or expenses are unknown
        val estimatedRemainingAmount: Long? = if (isIncomeKnown && isExpensesKnown) {
            totalDeclaredMonthlyIncome - estimatedMonthlyExpenses
        } else {
            null
        }

        val fp = profile.financialPosition
        val liquidSavings = (fp.cashBalance ?: 0L) + (fp.bankBalance ?: 0L) + (fp.walletBalance ?: 0L) + (fp.existingSavings ?: 0L)
        val investments = fp.investmentsBalance ?: 0L
        val totalSavings = liquidSavings + investments
        val totalLiabilities = fp.outstandingLoansAndLiabilities ?: 0L

        // Estimated emergency fund coverage in months, only if estimatedMonthlyExpenses > 0
        val emergencyCoverageMonths: Double? = if (isExpensesKnown && estimatedMonthlyExpenses > 0L) {
            ((liquidSavings.toDouble() / estimatedMonthlyExpenses.toDouble()) * 10.0).roundToInt() / 10.0
        } else {
            null
        }

        val recommendedMonthlyBudget: Long? = when {
            isExpensesKnown && estimatedMonthlyExpenses > 0L -> estimatedMonthlyExpenses
            isIncomeKnown && totalDeclaredMonthlyIncome > 0L -> (totalDeclaredMonthlyIncome * 0.70).roundToLong()
            else -> null
        }
        val recommendedDailyLimit: Long? = recommendedMonthlyBudget?.let { max(50L, it / 30L) }

        val recommendations = mutableListOf<String>()
        if (isIncompleteState) {
            recommendations.add(incompleteStateExplanation)
        } else {
            val rem = estimatedRemainingAmount ?: 0L
            if (rem > 0L) {
                recommendations.add("Estimated monthly surplus of ₹$rem available for Goal Vault & Emergency Reserve allocation.")
            } else if (rem < 0L) {
                recommendations.add("Declared monthly expenses exceed declared income by ₹${-rem}. Review discretionary categories in Budget Coach.")
            } else {
                recommendations.add("Declared monthly expenses exactly match declared income (₹0 buffer). Consider trimming 10% for emergency savings.")
            }
        }

        if (emergencyCoverageMonths != null) {
            if (emergencyCoverageMonths < 3.0) {
                recommendations.add("Emergency fund covers ${emergencyCoverageMonths} months of estimated expenses (Target: 3–6 months).")
            } else {
                recommendations.add("Strong emergency buffer: ${emergencyCoverageMonths} months of estimated expenses covered.")
            }
        }

        // Goal contribution suggestions based strictly on positive estimatedRemainingAmount
        val goalSuggestions = mutableMapOf<String, Long>()
        val positiveSurplus = max(0L, estimatedRemainingAmount ?: 0L)
        val activeGoals = profile.goalsAndConsent.goals
        if (positiveSurplus > 0L && activeGoals.isNotEmpty()) {
            val allocatable = (positiveSurplus * 0.75).roundToLong().coerceAtLeast(100L)
            val perGoal = (allocatable / activeGoals.size).coerceAtLeast(50L)
            activeGoals.forEach { g ->
                goalSuggestions[g.title] = perGoal
            }
        }

        val proRecs = buildList {
            when (profile.personal.occupation) {
                "Student" -> add("Module 41 · Student Finance Mode (Allowance & Academic Fee Tracker)")
                "Freelancer", "Self-employed", "Business Owner" -> {
                    add("Module 42 · Side-Income Tracker")
                    add("Module 43 · Invoice & Receipt Generator")
                }
                else -> add("Module 34 · Intelligent Budget Allocator")
            }
            if (totalLiabilities > 0L || (profile.monthlyExpenses.standardCategoryEstimates["EMIs and loans"] ?: 0L) > 0L) {
                add("Module 12 · Loan & EMI Intelligence")
            }
            add("Module 15 · Money Calendar Pro (Salary Day ${profile.income.salaryOrPaymentDay})")
        }

        return PersonalisedFinancialPlan(
            isIncomeKnown = isIncomeKnown,
            isExpensesKnown = isExpensesKnown,
            isIncompleteState = isIncompleteState,
            incompleteStateExplanation = incompleteStateExplanation,
            totalDeclaredMonthlyIncome = totalDeclaredMonthlyIncome,
            actualReceivedMonthlyIncome = monthIncomeTx,
            estimatedMonthlyExpenses = estimatedMonthlyExpenses,
            actualRecordedMonthlyExpenses = monthExpenseTx,
            estimatedRemainingAmount = estimatedRemainingAmount,
            existingDeclaredLiquidSavings = liquidSavings,
            existingDeclaredInvestments = investments,
            existingDeclaredTotalSavings = totalSavings,
            totalDeclaredLiabilities = totalLiabilities,
            estimatedEmergencyFundCoverageMonths = emergencyCoverageMonths,
            recommendedMonthlyBudget = recommendedMonthlyBudget,
            recommendedDailyLimit = recommendedDailyLimit,
            budgetRecommendations = recommendations,
            goalContributionSuggestions = goalSuggestions,
            proModuleRecommendations = proRecs
        )
    }

    fun saveOnboardingProfile(context: Context, userId: String, profile: UserFinancialOnboardingProfile) {
        val obj = serializeProfileToJson(profile.copy(userId = userId))
        prefs(context).edit().putString("${userId}_onboarding_profile", obj.toString()).apply()
    }

    fun exportOnboardingProfileJson(context: Context, userId: String): String {
        val profile = getOnboardingProfile(context, userId)
        return serializeProfileToJson(profile).toString(2)
    }

    fun clearOnboardingProfileForUser(context: Context, userId: String) {
        prefs(context).edit().remove("${userId}_onboarding_profile").apply()
    }

    private fun serializeProfileToJson(profile: UserFinancialOnboardingProfile): JSONObject {
        val pObj = JSONObject().apply {
            put("fullName", profile.personal.fullName.trim().take(100))
            put("ageRange", profile.personal.ageRange)
            put("occupation", profile.personal.occupation)
            put("preferredLanguage", profile.personal.preferredLanguage)
            put("currency", profile.personal.currency)
            put("incomeFrequency", profile.personal.incomeFrequency)
        }

        val addArr = JSONArray()
        profile.income.additionalIncomeSources.forEach { src ->
            addArr.put(JSONObject().apply {
                put("id", src.id)
                put("sourceName", src.sourceName.trim().take(80))
                put("estimatedMonthlyAmount", max(0L, src.estimatedMonthlyAmount))
                put("frequency", src.frequency)
                put("isVariable", src.isVariable)
            })
        }
        val iObj = JSONObject().apply {
            put("primaryIncomeSource", profile.income.primaryIncomeSource.trim().take(80))
            if (profile.income.primaryMonthlyIncomeAmount != null) {
                put("primaryMonthlyIncomeAmount", max(0L, profile.income.primaryMonthlyIncomeAmount))
            } else {
                put("primaryMonthlyIncomeAmount", JSONObject.NULL)
            }
            put("salaryOrPaymentDay", profile.income.salaryOrPaymentDay.coerceIn(1, 31))
            put("incomeFrequency", profile.income.incomeFrequency)
            put("isVariableIncome", profile.income.isVariableIncome)
            put("additionalIncomeSources", addArr)
        }

        val fpObj = JSONObject().apply {
            fun putOptLong(key: String, v: Long?) {
                if (v != null) put(key, max(0L, v)) else put(key, JSONObject.NULL)
            }
            putOptLong("cashBalance", profile.financialPosition.cashBalance)
            putOptLong("bankBalance", profile.financialPosition.bankBalance)
            putOptLong("walletBalance", profile.financialPosition.walletBalance)
            putOptLong("existingSavings", profile.financialPosition.existingSavings)
            putOptLong("investmentsBalance", profile.financialPosition.investmentsBalance)
            putOptLong("outstandingLoansAndLiabilities", profile.financialPosition.outstandingLoansAndLiabilities)
        }

        val stdObj = JSONObject()
        profile.monthlyExpenses.standardCategoryEstimates.forEach { (k, v) ->
            if (v != null) stdObj.put(k, max(0L, v)) else stdObj.put(k, JSONObject.NULL)
        }
        val custObj = JSONObject()
        profile.monthlyExpenses.customCategoryEstimates.forEach { (k, v) ->
            custObj.put(k.trim().take(60), max(0L, v))
        }
        val expObj = JSONObject().apply {
            put("standardCategoryEstimates", stdObj)
            put("customCategoryEstimates", custObj)
        }

        val gArr = JSONArray()
        profile.goalsAndConsent.goals.forEach { g ->
            gArr.put(JSONObject().apply {
                put("id", g.id)
                put("goalType", g.goalType)
                put("title", g.title.trim().take(80))
                put("targetAmount", max(0L, g.targetAmount))
                put("existingSavedAmount", max(0L, g.existingSavedAmount))
            })
        }
        val gcObj = JSONObject().apply {
            put("goals", gArr)
            put("consentNotifications", profile.goalsAndConsent.consentNotifications)
            put("consentOptionalIntegrations", profile.goalsAndConsent.consentOptionalIntegrations)
            put("acknowledgedDataUsageNotice", profile.goalsAndConsent.acknowledgedDataUsageNotice)
        }

        return JSONObject().apply {
            put("userId", profile.userId)
            put("status", profile.status.name)
            put("currentStepIndex", profile.currentStepIndex.coerceIn(0, TOTAL_ONBOARDING_STEPS - 1))
            put("personal", pObj)
            put("income", iObj)
            put("financialPosition", fpObj)
            put("monthlyExpenses", expObj)
            put("goalsAndConsent", gcObj)
            put("lastUpdatedEpochMillis", profile.lastUpdatedEpochMillis)
            put("confirmedAtEpochMillis", profile.confirmedAtEpochMillis)
        }
    }
}
