package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeclaredAdditionalIncome
import com.example.data.model.DeclaredFinancialGoal
import com.example.data.model.ExpenseRecord
import com.example.data.model.FinancialGoalsAndConsentSection
import com.example.data.model.FinancialPositionSection
import com.example.data.model.IncomeProfileSection
import com.example.data.model.KharcheeOnboardingProfileStore
import com.example.data.model.MonthlyExpenseProfileSection
import com.example.data.model.ONBOARDING_AGE_RANGES
import com.example.data.model.ONBOARDING_CURRENCIES
import com.example.data.model.ONBOARDING_INCOME_FREQUENCIES
import com.example.data.model.ONBOARDING_LANGUAGES
import com.example.data.model.ONBOARDING_OCCUPATIONS
import com.example.data.model.ONBOARDING_STANDARD_EXPENSE_CATEGORIES
import com.example.data.model.ONBOARDING_STANDARD_GOAL_TYPES
import com.example.data.model.OnboardingLifecycleStatus
import com.example.data.model.PersonalProfileSection
import com.example.data.model.PersonalisedFinancialPlan
import com.example.data.model.UserFinancialOnboardingProfile
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.GoldReserveAccent
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianDarkBorder
import com.example.ui.theme.ObsidianDarkPrimarySurface
import com.example.ui.theme.ObsidianDarkSecondarySurface
import com.example.ui.theme.ObsidianDarkSilver
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory

@Composable
fun KharcheeSmartOnboardingOverlay(
    userId: String,
    defaultDisplayName: String,
    actualRecordedTransactions: List<ExpenseRecord>,
    onSaveAndContinueLater: (UserFinancialOnboardingProfile) -> Unit,
    onProfileConfirmed: (UserFinancialOnboardingProfile, PersonalisedFinancialPlan) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    val initialProfile = remember(userId) {
        KharcheeOnboardingProfileStore.getOnboardingProfile(context, userId, defaultDisplayName)
    }

    var stepIndex by remember { mutableIntStateOf(initialProfile.currentStepIndex.coerceIn(0, 5)) }

    // Step 1: Personal Profile State
    var fullName by remember { mutableStateOf(initialProfile.personal.fullName.ifBlank { defaultDisplayName }) }
    var ageRange by remember { mutableStateOf(initialProfile.personal.ageRange) }
    var occupation by remember { mutableStateOf(initialProfile.personal.occupation) }
    var preferredLanguage by remember { mutableStateOf(initialProfile.personal.preferredLanguage) }
    var currency by remember { mutableStateOf(initialProfile.personal.currency) }
    var personalIncomeFreq by remember { mutableStateOf(initialProfile.personal.incomeFrequency) }

    // Step 2: Income Profile State
    var primaryIncomeSource by remember { mutableStateOf(initialProfile.income.primaryIncomeSource) }
    var primaryIncomeAmountStr by remember { mutableStateOf(initialProfile.income.primaryMonthlyIncomeAmount?.toString() ?: "") }
    var salaryDayStr by remember { mutableStateOf(initialProfile.income.salaryOrPaymentDay.toString()) }
    var incomeFrequency by remember { mutableStateOf(initialProfile.income.incomeFrequency) }
    var isVariableIncome by remember { mutableStateOf(initialProfile.income.isVariableIncome) }
    var additionalSources by remember { mutableStateOf(initialProfile.income.additionalIncomeSources) }
    var newAddSourceName by remember { mutableStateOf("") }
    var newAddSourceAmt by remember { mutableStateOf("") }

    // Step 3: Current Financial Position State (All Optional, User-Declared)
    var cashBalStr by remember { mutableStateOf(initialProfile.financialPosition.cashBalance?.toString() ?: "") }
    var bankBalStr by remember { mutableStateOf(initialProfile.financialPosition.bankBalance?.toString() ?: "") }
    var walletBalStr by remember { mutableStateOf(initialProfile.financialPosition.walletBalance?.toString() ?: "") }
    var savingsBalStr by remember { mutableStateOf(initialProfile.financialPosition.existingSavings?.toString() ?: "") }
    var investBalStr by remember { mutableStateOf(initialProfile.financialPosition.investmentsBalance?.toString() ?: "") }
    var loansBalStr by remember { mutableStateOf(initialProfile.financialPosition.outstandingLoansAndLiabilities?.toString() ?: "") }

    // Step 4: Monthly Expense Profile State
    var standardExpensesMap by remember {
        val map = mutableMapOf<String, String>()
        ONBOARDING_STANDARD_EXPENSE_CATEGORIES.forEach { cat ->
            map[cat] = initialProfile.monthlyExpenses.standardCategoryEstimates[cat]?.toString() ?: ""
        }
        mutableStateOf(map.toMap())
    }
    var customExpensesMap by remember { mutableStateOf(initialProfile.monthlyExpenses.customCategoryEstimates) }
    var newCustomExpCat by remember { mutableStateOf("") }
    var newCustomExpAmt by remember { mutableStateOf("") }

    // Step 5: Financial Goals & Consent State
    var goalsList by remember { mutableStateOf(initialProfile.goalsAndConsent.goals) }
    var selectedGoalType by remember { mutableStateOf(ONBOARDING_STANDARD_GOAL_TYPES.first()) }
    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }
    var newGoalExistingSaved by remember { mutableStateOf("") }
    var consentNotifications by remember { mutableStateOf(initialProfile.goalsAndConsent.consentNotifications) }
    var consentIntegrations by remember { mutableStateOf(initialProfile.goalsAndConsent.consentOptionalIntegrations) }

    fun buildCurrentDraft(targetStep: Int = stepIndex): UserFinancialOnboardingProfile {
        val parsedPrimaryAmt = primaryIncomeAmountStr.trim().toLongOrNull()
        val parsedStdExpenses = standardExpensesMap.mapValues { (_, v) -> v.trim().toLongOrNull() }
        return UserFinancialOnboardingProfile(
            userId = userId,
            status = initialProfile.status,
            currentStepIndex = targetStep.coerceIn(0, 5),
            personal = PersonalProfileSection(
                fullName = fullName.trim().ifBlank { defaultDisplayName },
                ageRange = ageRange,
                occupation = occupation,
                preferredLanguage = preferredLanguage,
                currency = currency,
                incomeFrequency = personalIncomeFreq
            ),
            income = IncomeProfileSection(
                primaryIncomeSource = primaryIncomeSource.trim(),
                primaryMonthlyIncomeAmount = parsedPrimaryAmt,
                salaryOrPaymentDay = (salaryDayStr.toIntOrNull() ?: 1).coerceIn(1, 31),
                incomeFrequency = incomeFrequency,
                isVariableIncome = isVariableIncome,
                additionalIncomeSources = additionalSources
            ),
            financialPosition = FinancialPositionSection(
                cashBalance = cashBalStr.trim().toLongOrNull(),
                bankBalance = bankBalStr.trim().toLongOrNull(),
                walletBalance = walletBalStr.trim().toLongOrNull(),
                existingSavings = savingsBalStr.trim().toLongOrNull(),
                investmentsBalance = investBalStr.trim().toLongOrNull(),
                outstandingLoansAndLiabilities = loansBalStr.trim().toLongOrNull()
            ),
            monthlyExpenses = MonthlyExpenseProfileSection(
                standardCategoryEstimates = parsedStdExpenses,
                customCategoryEstimates = customExpensesMap
            ),
            goalsAndConsent = FinancialGoalsAndConsentSection(
                goals = goalsList,
                consentNotifications = consentNotifications,
                consentOptionalIntegrations = consentIntegrations,
                acknowledgedDataUsageNotice = true
            )
        )
    }

    val stepTitles = listOf(
        "1. Personal Profile",
        "2. Income Profile (Declared vs Received)",
        "3. Current Financial Position (Self-Declared)",
        "4. Monthly Expense Profile",
        "5. Financial Goals & Consent",
        "6. Financial Profile Summary & Confirmation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("smart_onboarding_overlay")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Progress Header + Save & Continue Later
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianDarkPrimarySurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "KHARCHEE PERSONAL FINANCIAL PROFILE · STEP ${stepIndex + 1} OF 6",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChampagneGold
                                )
                                Text(
                                    text = stepTitles[stepIndex],
                                    fontFamily = headingFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = WarmIvory
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val saved = KharcheeOnboardingProfileStore.saveOnboardingDraftProgress(
                                        context = context,
                                        userId = userId,
                                        profile = buildCurrentDraft(stepIndex),
                                        pauseForLater = true
                                    )
                                    onSaveAndContinueLater(saved)
                                },
                                modifier = Modifier.testTag("onboarding_save_continue_later_btn")
                            ) {
                                Text("Save & Continue Later", fontSize = 11.sp, color = ChampagneGold)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (stepIndex + 1) / 6f },
                            color = SophisticatedEmerald,
                            trackColor = ElevatedGraphite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // Step Body
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianDarkPrimarySurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        when (stepIndex) {
                            // ----------------------------------------------------------
                            // STEP 0: PERSONAL PROFILE
                            // ----------------------------------------------------------
                            0 -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = ChampagneGold)
                                    Text("Tell us about yourself to tailor your workspace", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmIvory)
                                }

                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                                )

                                Text("Age Range (Optional — Tap to select or skip)", fontSize = 12.sp, color = MutedPlatinum)
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ONBOARDING_AGE_RANGES.forEach { range ->
                                        val label = range.ifBlank { "Prefer not to say" }
                                        val sel = ageRange == range
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (sel) ChampagneGold.copy(alpha = 0.22f) else ElevatedGraphite,
                                            modifier = Modifier
                                                .border(1.dp, if (sel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                                                .clickable { ageRange = range }
                                        ) {
                                            Text(label, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                                        }
                                    }
                                }

                                Text("Occupation", fontSize = 12.sp, color = MutedPlatinum)
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ONBOARDING_OCCUPATIONS.forEach { occ ->
                                        val sel = occupation == occ
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (sel) SophisticatedEmerald.copy(alpha = 0.22f) else ElevatedGraphite,
                                            modifier = Modifier
                                                .border(1.dp, if (sel) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                                                .clickable { occupation = occ }
                                                .testTag("onboarding_occ_$occ")
                                        ) {
                                            Text(occ, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                                        }
                                    }
                                }

                                Text("Preferred Language (Initialises JARVIS & Coaching)", fontSize = 12.sp, color = MutedPlatinum)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ONBOARDING_LANGUAGES.forEach { lang ->
                                        val sel = preferredLanguage == lang
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (sel) ChampagneGold.copy(alpha = 0.22f) else ElevatedGraphite,
                                            modifier = Modifier
                                                .border(1.dp, if (sel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                                                .clickable { preferredLanguage = lang }
                                        ) {
                                            Text(lang, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                                        }
                                    }
                                }

                                Text("Primary Currency", fontSize = 12.sp, color = MutedPlatinum)
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ONBOARDING_CURRENCIES.forEach { cur ->
                                        val sel = currency == cur
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (sel) ChampagneGold.copy(alpha = 0.22f) else ElevatedGraphite,
                                            modifier = Modifier
                                                .border(1.dp, if (sel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                                                .clickable { currency = cur }
                                        ) {
                                            Text(cur, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                                        }
                                    }
                                }

                                Text("Income Frequency", fontSize = 12.sp, color = MutedPlatinum)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ONBOARDING_INCOME_FREQUENCIES.forEach { freq ->
                                        val sel = personalIncomeFreq == freq
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (sel) ChampagneGold.copy(alpha = 0.22f) else ElevatedGraphite,
                                            modifier = Modifier
                                                .border(1.dp, if (sel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    personalIncomeFreq = freq
                                                    incomeFrequency = freq
                                                }
                                        ) {
                                            Text(freq, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                                        }
                                    }
                                }
                            }

                            // ----------------------------------------------------------
                            // STEP 1: INCOME PROFILE
                            // ----------------------------------------------------------
                            1 -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ChampagneGold)
                                    Text("Declared Income Profile (Optional)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmIvory)
                                }
                                Text(
                                    text = "Important: Declared income is used only for budget planning & cashflow estimates. It is never recorded as a completed transaction until you actually receive and log a payment.",
                                    fontSize = 11.sp,
                                    color = GoldReserveAccent
                                )

                                OutlinedTextField(
                                    value = primaryIncomeSource,
                                    onValueChange = { primaryIncomeSource = it },
                                    label = { Text("Primary Income Source (e.g. Salary, Stipend, Business)") },
                                    modifier = Modifier.fillMaxWidth().testTag("onboarding_primary_income_source")
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = primaryIncomeAmountStr,
                                        onValueChange = { primaryIncomeAmountStr = it.filter(Char::isDigit) },
                                        label = { Text("Estimated ${incomeFrequency} Income (₹)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1.3f).testTag("onboarding_primary_income_amount")
                                    )
                                    OutlinedTextField(
                                        value = salaryDayStr,
                                        onValueChange = { salaryDayStr = it.filter(Char::isDigit) },
                                        label = { Text("Pay Day (1–31)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(0.7f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(false to "Fixed Income", true to "Variable Income").forEach { (isVar, label) ->
                                        val sel = isVariableIncome == isVar
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (sel) SophisticatedEmerald.copy(alpha = 0.22f) else ElevatedGraphite,
                                            modifier = Modifier
                                                .border(1.dp, if (sel) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                                                .clickable { isVariableIncome = isVar }
                                        ) {
                                            Text(label, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                                        }
                                    }
                                }

                                HorizontalDivider(color = HairlinePlatinum)
                                Text("Additional Income Sources (Multiple Allowed)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = WarmIvory)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newAddSourceName,
                                        onValueChange = { newAddSourceName = it },
                                        label = { Text("Source (e.g. Freelance, Rent)", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    OutlinedTextField(
                                        value = newAddSourceAmt,
                                        onValueChange = { newAddSourceAmt = it.filter(Char::isDigit) },
                                        label = { Text("Monthly ₹", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(0.9f)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            val amt = newAddSourceAmt.toLongOrNull() ?: 0L
                                            if (newAddSourceName.isNotBlank() && amt > 0L) {
                                                additionalSources = additionalSources + DeclaredAdditionalIncome(
                                                    sourceName = newAddSourceName.trim(),
                                                    estimatedMonthlyAmount = amt
                                                )
                                                newAddSourceName = ""
                                                newAddSourceAmt = ""
                                            }
                                        }
                                    ) {
                                        Text("+ Add", fontSize = 11.sp)
                                    }
                                }

                                additionalSources.forEach { src ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(ObsidianDarkSecondarySurface, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${src.sourceName} (Estimated)", fontSize = 12.sp, color = WarmIvory)
                                        Text("₹${src.estimatedMonthlyAmount}/mo", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp, color = ChampagneGold)
                                    }
                                }
                            }

                            // ----------------------------------------------------------
                            // STEP 2: CURRENT FINANCIAL POSITION
                            // ----------------------------------------------------------
                            2 -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ChampagneGold)
                                    Text("Current Financial Position (All Optional)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmIvory)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ObsidianDarkSecondarySurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = SophisticatedEmerald, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = KharcheeOnboardingProfileStore.ZERO_CREDENTIAL_SECURITY_NOTICE,
                                            fontSize = 11.sp,
                                            color = SophisticatedEmerald
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = cashBalStr,
                                        onValueChange = { cashBalStr = it.filter(Char::isDigit) },
                                        label = { Text("Cash Balance (₹)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = bankBalStr,
                                        onValueChange = { bankBalStr = it.filter(Char::isDigit) },
                                        label = { Text("Bank Balances (₹)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = walletBalStr,
                                        onValueChange = { walletBalStr = it.filter(Char::isDigit) },
                                        label = { Text("Wallet Balances (₹)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = savingsBalStr,
                                        onValueChange = { savingsBalStr = it.filter(Char::isDigit) },
                                        label = { Text("Existing Savings (₹)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = investBalStr,
                                        onValueChange = { investBalStr = it.filter(Char::isDigit) },
                                        label = { Text("Investments (₹)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = loansBalStr,
                                        onValueChange = { loansBalStr = it.filter(Char::isDigit) },
                                        label = { Text("Loans / Liabilities (₹)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // ----------------------------------------------------------
                            // STEP 3: MONTHLY EXPENSE PROFILE
                            // ----------------------------------------------------------
                            3 -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = ChampagneGold)
                                    Text("Estimated Monthly Expense Profile (Optional)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmIvory)
                                }
                                Text(
                                    text = "Enter estimated monthly amounts for categories that apply to you, or leave blank to skip.",
                                    fontSize = 11.sp,
                                    color = MutedPlatinum
                                )

                                ONBOARDING_STANDARD_EXPENSE_CATEGORIES.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        pair.forEach { categoryName ->
                                            OutlinedTextField(
                                                value = standardExpensesMap[categoryName] ?: "",
                                                onValueChange = { newVal ->
                                                    val next = standardExpensesMap.toMutableMap()
                                                    next[categoryName] = newVal.filter(Char::isDigit)
                                                    standardExpensesMap = next
                                                },
                                                label = { Text("$categoryName (₹)", fontSize = 10.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = HairlinePlatinum)
                                Text("Add Custom Expense Category", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = WarmIvory)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newCustomExpCat,
                                        onValueChange = { newCustomExpCat = it },
                                        label = { Text("Custom Category", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    OutlinedTextField(
                                        value = newCustomExpAmt,
                                        onValueChange = { newCustomExpAmt = it.filter(Char::isDigit) },
                                        label = { Text("Monthly ₹", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(0.9f)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            val amt = newCustomExpAmt.toLongOrNull() ?: 0L
                                            if (newCustomExpCat.isNotBlank() && amt >= 0L) {
                                                val next = customExpensesMap.toMutableMap()
                                                next[newCustomExpCat.trim()] = amt
                                                customExpensesMap = next
                                                newCustomExpCat = ""
                                                newCustomExpAmt = ""
                                            }
                                        }
                                    ) {
                                        Text("+ Add", fontSize = 11.sp)
                                    }
                                }
                                customExpensesMap.forEach { (cat, amt) ->
                                    Text("• $cat: ₹$amt/mo", fontSize = 12.sp, color = ChampagneGold)
                                }
                            }

                            // ----------------------------------------------------------
                            // STEP 4: FINANCIAL GOALS & CONSENT
                            // ----------------------------------------------------------
                            4 -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Flag, contentDescription = null, tint = ChampagneGold)
                                    Text("Financial Goals & Privacy Consent (Optional)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmIvory)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ONBOARDING_STANDARD_GOAL_TYPES.forEach { gType ->
                                        val sel = selectedGoalType == gType
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (sel) ChampagneGold.copy(alpha = 0.22f) else ElevatedGraphite,
                                            modifier = Modifier
                                                .border(1.dp, if (sel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    selectedGoalType = gType
                                                    if (newGoalTitle.isBlank()) newGoalTitle = gType
                                                }
                                        ) {
                                            Text(gType, fontSize = 11.sp, color = if (sel) WarmIvory else MutedPlatinum, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newGoalTitle,
                                        onValueChange = { newGoalTitle = it },
                                        label = { Text("Goal Name (${selectedGoalType})", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1.1f)
                                    )
                                    OutlinedTextField(
                                        value = newGoalTarget,
                                        onValueChange = { newGoalTarget = it.filter(Char::isDigit) },
                                        label = { Text("Target ₹", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(0.8f)
                                    )
                                    OutlinedTextField(
                                        value = newGoalExistingSaved,
                                        onValueChange = { newGoalExistingSaved = it.filter(Char::isDigit) },
                                        label = { Text("Saved ₹", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(0.8f)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        val t = newGoalTarget.toLongOrNull() ?: 0L
                                        val s = newGoalExistingSaved.toLongOrNull() ?: 0L
                                        if (t > 0L) {
                                            goalsList = goalsList + DeclaredFinancialGoal(
                                                goalType = selectedGoalType,
                                                title = newGoalTitle.trim().ifBlank { selectedGoalType },
                                                targetAmount = t,
                                                existingSavedAmount = s
                                            )
                                            newGoalTitle = ""
                                            newGoalTarget = ""
                                            newGoalExistingSaved = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("+ Add Financial Goal to Profile")
                                }

                                goalsList.forEach { g ->
                                    Text("• [${g.goalType}] ${g.title}: Target ₹${g.targetAmount} (Already saved: ₹${g.existingSavedAmount})", fontSize = 12.sp, color = WarmIvory)
                                }

                                HorizontalDivider(color = HairlinePlatinum)
                                Text("Data Usage & Explicit Consent", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ChampagneGold)
                                Text(KharcheeOnboardingProfileStore.DATA_USAGE_EXPLANATION, fontSize = 11.sp, color = ObsidianDarkSilver)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = consentNotifications, onCheckedChange = { consentNotifications = it })
                                    Text("I explicitly consent to payment reminders & smart budget notifications", fontSize = 12.sp, color = WarmIvory)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = consentIntegrations, onCheckedChange = { consentIntegrations = it })
                                    Text("I consent to optional local SMS/Receipt parsing helpers when I invoke them", fontSize = 12.sp, color = WarmIvory)
                                }
                            }

                            // ----------------------------------------------------------
                            // STEP 5: FINANCIAL PROFILE SUMMARY & CONFIRMATION
                            // ----------------------------------------------------------
                            5 -> {
                                val draftProfile = buildCurrentDraft(5)
                                val plan = KharcheeOnboardingProfileStore.computePersonalisedFinancialPlan(
                                    profile = draftProfile,
                                    actualRecordedTransactions = actualRecordedTransactions
                                )

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SophisticatedEmerald)
                                    Text("Review Your Personal Financial Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmIvory)
                                }

                                // Section 1 Summary
                                SummarySectionCard(
                                    title = "1. Personal Profile",
                                    lines = listOf(
                                        "Name: ${draftProfile.personal.fullName.ifBlank { "Not specified" }} · Age: ${draftProfile.personal.ageRange.ifBlank { "Skipped" }}",
                                        "Occupation: ${draftProfile.personal.occupation} · Language: ${draftProfile.personal.preferredLanguage} · Currency: ${draftProfile.personal.currency}"
                                    ),
                                    onEdit = { stepIndex = 0 }
                                )

                                // Section 2 Summary
                                SummarySectionCard(
                                    title = "2. Declared Income Profile (Estimated, Not Completed Transactions)",
                                    lines = listOf(
                                        if (plan.isIncomeKnown) "Total Declared Monthly Income: ₹${plan.totalDeclaredMonthlyIncome} (Pay Day ${draftProfile.income.salaryOrPaymentDay})"
                                        else "Declared Monthly Income: Unknown / Skipped",
                                        "Income Type: ${if (draftProfile.income.isVariableIncome) "Variable" else "Fixed"} · Additional Sources: ${draftProfile.income.additionalIncomeSources.size}"
                                    ),
                                    onEdit = { stepIndex = 1 }
                                )

                                // Section 3 Summary
                                SummarySectionCard(
                                    title = "3. Self-Declared Financial Position (Unverified User-Entered Values)",
                                    lines = listOf(
                                        "Liquid Savings (Cash+Bank+Wallet+Savings): ₹${plan.existingDeclaredLiquidSavings}",
                                        "Investments: ₹${plan.existingDeclaredInvestments} · Outstanding Liabilities: ₹${plan.totalDeclaredLiabilities}"
                                    ),
                                    onEdit = { stepIndex = 2 }
                                )

                                // Section 4 Summary
                                SummarySectionCard(
                                    title = "4. Estimated Monthly Expense Profile",
                                    lines = listOf(
                                        if (plan.isExpensesKnown) "Total Estimated Monthly Expenses: ₹${plan.estimatedMonthlyExpenses}"
                                        else "Estimated Monthly Expenses: Unknown / Skipped",
                                        "Custom Categories Added: ${draftProfile.monthlyExpenses.customCategoryEstimates.size}"
                                    ),
                                    onEdit = { stepIndex = 3 }
                                )

                                // Section 5 Summary
                                SummarySectionCard(
                                    title = "5. Financial Goals & Consent",
                                    lines = listOf(
                                        "Declared Goals: ${draftProfile.goalsAndConsent.goals.size}",
                                        "Notifications Consent: ${if (draftProfile.goalsAndConsent.consentNotifications) "Granted" else "Not Enabled"}"
                                    ),
                                    onEdit = { stepIndex = 4 }
                                )

                                // Personalisation Engine Preview
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = ObsidianDarkSecondarySurface,
                                    modifier = Modifier.fillMaxWidth().border(1.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("PERSONALISATION ENGINE PREVIEW", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChampagneGold)
                                        if (plan.isIncompleteState) {
                                            Text("Incomplete Profile State (No Invented Values):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GoldReserveAccent)
                                            Text(plan.incompleteStateExplanation, fontSize = 11.sp, color = WarmIvory)
                                        } else {
                                            Text(
                                                text = "Estimated Monthly Remaining Surplus: ₹${plan.estimatedRemainingAmount ?: 0L}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if ((plan.estimatedRemainingAmount ?: 0L) >= 0L) SophisticatedEmerald else CrimsonRuby
                                            )
                                        }
                                        Text(
                                            text = "Emergency Fund Coverage: " + (plan.estimatedEmergencyFundCoverageMonths?.let { "$it months of estimated expenses" } ?: "Insufficient expense data to calculate"),
                                            fontSize = 12.sp,
                                            color = WarmIvory
                                        )
                                        plan.budgetRecommendations.forEach { rec ->
                                            Text("• $rec", fontSize = 11.sp, color = MutedPlatinum)
                                        }
                                        if (plan.goalContributionSuggestions.isNotEmpty()) {
                                            Text("Suggested Monthly Goal Contributions:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = ChampagneGold)
                                            plan.goalContributionSuggestions.forEach { (gTitle, amt) ->
                                                Text("  - $gTitle: ₹$amt/month", fontSize = 11.sp, color = WarmIvory)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Step Navigation Controls (Back / Skip Optional / Next / Confirm)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (stepIndex > 0) {
                                OutlinedButton(
                                    onClick = {
                                        val prev = (stepIndex - 1).coerceAtLeast(0)
                                        KharcheeOnboardingProfileStore.saveOnboardingDraftProgress(context, userId, buildCurrentDraft(prev))
                                        stepIndex = prev
                                    }
                                ) {
                                    Text("Back")
                                }
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (stepIndex in 1..4) {
                                    OutlinedButton(
                                        onClick = {
                                            val next = (stepIndex + 1).coerceAtMost(5)
                                            KharcheeOnboardingProfileStore.saveOnboardingDraftProgress(context, userId, buildCurrentDraft(next))
                                            stepIndex = next
                                        },
                                        modifier = Modifier.testTag("onboarding_skip_step_btn")
                                    ) {
                                        Text("Skip Optional Step", fontSize = 11.sp)
                                    }
                                }

                                if (stepIndex < 5) {
                                    Button(
                                        onClick = {
                                            val next = (stepIndex + 1).coerceAtMost(5)
                                            KharcheeOnboardingProfileStore.saveOnboardingDraftProgress(context, userId, buildCurrentDraft(next))
                                            stepIndex = next
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = Color.Black),
                                        modifier = Modifier.testTag("onboarding_next_step_btn")
                                    ) {
                                        Text("Save & Next", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val (confirmed, plan) = KharcheeOnboardingProfileStore.confirmAndActivateOnboardingProfile(
                                                context = context,
                                                userId = userId,
                                                profile = buildCurrentDraft(5)
                                            )
                                            onProfileConfirmed(confirmed, plan)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = Color.Black),
                                        modifier = Modifier.testTag("onboarding_confirm_finish_btn")
                                    ) {
                                        Text("Confirm & Personalise KHARCHEE", fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySectionCard(
    title: String,
    lines: List<String>,
    onEdit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ObsidianDarkSecondarySurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ChampagneGold)
                lines.forEach { l ->
                    Text(l, fontSize = 11.sp, color = WarmIvory)
                }
            }
            OutlinedButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit section", modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PersonalFinancialProfileDashboardCard(
    userId: String,
    actualRecordedTransactions: List<ExpenseRecord>,
    onOpenOnboardingEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val profile = remember(userId, actualRecordedTransactions) {
        KharcheeOnboardingProfileStore.getOnboardingProfile(context, userId)
    }
    val plan = remember(profile, actualRecordedTransactions) {
        KharcheeOnboardingProfileStore.computePersonalisedFinancialPlan(profile, actualRecordedTransactions)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianDarkPrimarySurface,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ObsidianDarkBorder, RoundedCornerShape(20.dp))
            .testTag("personal_financial_profile_card")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (profile.status == OnboardingLifecycleStatus.PAUSED_FOR_LATER || profile.status == OnboardingLifecycleStatus.IN_PROGRESS) {
                            "ONBOARDING IN PROGRESS · STEP ${profile.currentStepIndex + 1} OF 6"
                        } else {
                            "PERSONAL FINANCIAL PROFILE · ${profile.personal.occupation.uppercase()} (${profile.personal.currency})"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                    Text(
                        text = if (profile.status == OnboardingLifecycleStatus.PAUSED_FOR_LATER || profile.status == OnboardingLifecycleStatus.IN_PROGRESS) {
                            "Resume Your Financial Setup Anytime"
                        } else if (plan.isIncompleteState) {
                            "Financial Profile Active (Partial Estimates)"
                        } else {
                            "Declared Plan vs Actual Recorded Activity"
                        },
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = WarmIvory
                    )
                }
                OutlinedButton(
                    onClick = onOpenOnboardingEditor,
                    modifier = Modifier.testTag("edit_or_resume_financial_profile_btn")
                ) {
                    Text(
                        text = if (profile.status == OnboardingLifecycleStatus.PAUSED_FOR_LATER || profile.status == OnboardingLifecycleStatus.IN_PROGRESS) "Resume Setup" else "Edit Profile",
                        fontSize = 11.sp,
                        color = ChampagneGold
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = ObsidianDarkSecondarySurface, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("DECLARED VS RECEIVED INCOME", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                        Text(
                            text = if (plan.isIncomeKnown) "Est: ₹${plan.totalDeclaredMonthlyIncome}" else "Est: Unknown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Received Tx: ₹${plan.actualReceivedMonthlyIncome}",
                            fontSize = 11.sp,
                            color = SophisticatedEmerald
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(10.dp), color = ObsidianDarkSecondarySurface, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("ESTIMATED VS ACTUAL SPEND", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                        Text(
                            text = if (plan.isExpensesKnown) "Est: ₹${plan.estimatedMonthlyExpenses}" else "Est: Unknown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Logged Tx: ₹${plan.actualRecordedMonthlyExpenses}",
                            fontSize = 11.sp,
                            color = GoldReserveAccent
                        )
                    }
                }
            }

            if (plan.isIncompleteState) {
                Text(
                    text = plan.incompleteStateExplanation,
                    fontSize = 11.sp,
                    color = GoldReserveAccent
                )
            } else {
                Text(
                    text = "Estimated Remaining Monthly Surplus: ₹${plan.estimatedRemainingAmount ?: 0L} · Self-Declared Liquid Savings: ₹${plan.existingDeclaredLiquidSavings}" +
                        (plan.estimatedEmergencyFundCoverageMonths?.let { " · Emergency Coverage: ${it} mos" } ?: ""),
                    fontSize = 11.sp,
                    color = SophisticatedEmerald
                )
            }
        }
    }
}
