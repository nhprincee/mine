package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExpenseRecord
import com.example.data.model.NightGuardChallengeEngine
import com.example.data.model.NightGuardCooldownStore
import com.example.data.model.NightGuardEventRecord
import com.example.data.model.NightGuardSchedule
import com.example.data.model.NightSpendingAnalytics
import com.example.data.model.UserSettings
import com.example.ui.auth.normalizeE164PhoneNumber
import com.example.ui.theme.DEFAULT_FONT_ID
import com.example.ui.theme.FontPreferenceManager
import com.example.ui.theme.getAppFontOptionById
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    @Test
    fun defaultFont_newUser_isManropeExtraBoldAndInter() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("kharchee_font_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        val resolved = FontPreferenceManager.ensureInitialized(context)
        assertEquals("manrope", resolved)
        assertEquals("Manrope ExtraBold + Inter", getAppFontOptionById(DEFAULT_FONT_ID).name)
    }

    @Test
    fun existingUserSavedFontPreference_isNotOverwritten() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("kharchee_font_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_font_id", "outfit").commit()

        val resolved = FontPreferenceManager.ensureInitialized(context)
        assertEquals("outfit", resolved)
        assertEquals("Outfit", getAppFontOptionById(resolved).name)
    }

    @Test
    fun normalizeE164PhoneNumber_validatesIndianAndInternationalNumbers() {
        assertEquals("+919876543210", normalizeE164PhoneNumber("98765 43210"))
        assertEquals("+919876543210", normalizeE164PhoneNumber("09876543210"))
        assertEquals("+919876543210", normalizeE164PhoneNumber("+91-98765-43210"))
        assertNull(normalizeE164PhoneNumber("12345"))
        assertNull(normalizeE164PhoneNumber("abcdefghij"))
    }

    @Test
    fun nightGuardSchedule_overnightWindow_detectsLateNightAndEarlyMorning() {
        val cal2330 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 30)
        }
        val cal0215 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 15)
        }
        val cal1400 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
        }

        assertTrue(NightGuardSchedule.isWithinNightGuardWindow(cal2330.timeInMillis, 22, 0, 5, 0))
        assertTrue(NightGuardSchedule.isWithinNightGuardWindow(cal0215.timeInMillis, 22, 0, 5, 0))
        assertFalse(NightGuardSchedule.isWithinNightGuardWindow(cal1400.timeInMillis, 22, 0, 5, 0))
    }

    @Test
    fun nightGuardChallengeEngine_hasAllFiveCategoriesAndAvoidsImmediateRepeats() {
        val allCategories = NightGuardChallengeEngine.CHALLENGE_CATEGORIES.toSet()
        val presentCategories = NightGuardChallengeEngine.getAllChallenges().map { it.category }.toSet()
        assertEquals(allCategories, presentCategories)

        val recentIds = mutableSetOf<String>()
        repeat(10) {
            val next = NightGuardChallengeEngine.pickNextChallenge(excludeIds = recentIds)
            assertFalse("Should not immediately repeat excluded challenge", recentIds.contains(next.id))
            recentIds.add(next.id)
            assertTrue(NightGuardChallengeEngine.validateAnswer(next, next.correctAnswer))
            val wrongAnswer = next.options.first { it != next.correctAnswer }
            assertFalse(NightGuardChallengeEngine.validateAnswer(next, wrongAnswer))
        }
    }

    @Test
    fun nightGuardCooldownStore_persistsFiveMinuteWallClockCooldown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userId = "test_cooldown_user"

        val now = 1_700_000_000_000L
        val endMillis = now + NightGuardChallengeEngine.FIVE_MINUTES_MS
        NightGuardCooldownStore.saveLocalCooldownEndMillis(context, userId, endMillis)

        val resolvedEnd = NightGuardCooldownStore.resolveEffectiveCooldownEndMillis(
            context = context,
            userId = userId,
            firestoreCooldownEndMillis = 0L
        )
        assertEquals(endMillis, resolvedEnd)

        val remainingHalfway = NightGuardCooldownStore.getRemainingCooldownMillis(
            nowMillis = now + 120_000L,
            effectiveCooldownEndMillis = resolvedEnd
        )
        assertEquals(180_000L, remainingHalfway)
        assertEquals("03:00", NightGuardCooldownStore.formatRemainingCooldown(remainingHalfway))
    }

    @Test
    fun nightSpendingAnalytics_computesStrictSavingsFromCancelledEventsOnly() {
        val settings = UserSettings(
            nightGuardEnabled = true,
            nightGuardStartHour = 22,
            nightGuardStartMinute = 0,
            nightGuardEndHour = 5,
            nightGuardEndMinute = 0
        )
        val nightCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 15)
        }
        val dayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 0)
        }

        val expenses = listOf(
            ExpenseRecord(id = "1", title = "Late Zomato", amount = 650L, category = "Food", type = "expense", txEpochMillis = nightCal.timeInMillis),
            ExpenseRecord(id = "2", title = "Lunch", amount = 300L, category = "Food", type = "expense", txEpochMillis = dayCal.timeInMillis)
        )
        val events = listOf(
            NightGuardEventRecord(id = "e1", title = "Gaming Skin", amount = 1200L, outcome = "cancelled", eventEpochMillis = nightCal.timeInMillis),
            NightGuardEventRecord(id = "e2", title = "Sneakers", amount = 2500L, outcome = "postponed", eventEpochMillis = nightCal.timeInMillis),
            NightGuardEventRecord(id = "e3", title = "Late Zomato", amount = 650L, outcome = "proceeded", eventEpochMillis = nightCal.timeInMillis)
        )

        val summary = NightSpendingAnalytics.computeSummary(
            expenses = expenses,
            nightGuardEvents = events,
            settings = settings,
            nowMillis = nightCal.timeInMillis + 60_000L
        )

        assertNotNull(summary)
        assertEquals(650L, summary.totalNightSpentAllTime)
        assertEquals(1, summary.cancelledCount)
        assertEquals(1, summary.postponedCount)
        assertEquals(1200L, summary.savedFromCancelledAmount)
        assertEquals(2500L, summary.postponedAmount)
    }

    @Test
    fun jarvisReactorCore_statusLabelsAndUploadedAssetBundled() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = context.getDrawable(R.drawable.jarvis_reactor_core)
        assertNotNull("Uploaded JARVIS reactor image must be bundled as R.drawable.jarvis_reactor_core", drawable)

        assertEquals(
            "ONLINE",
            com.example.ui.jarvis.resolveJarvisCoreStatus(isListening = false, isThinking = false, isSpeaking = false).label
        )
        assertEquals(
            "LISTENING",
            com.example.ui.jarvis.resolveJarvisCoreStatus(isListening = true, isThinking = false, isSpeaking = false).label
        )
        assertEquals(
            "THINKING",
            com.example.ui.jarvis.resolveJarvisCoreStatus(isListening = false, isThinking = true, isSpeaking = false).label
        )
        assertEquals(
            "SPEAKING",
            com.example.ui.jarvis.resolveJarvisCoreStatus(isListening = false, isThinking = false, isSpeaking = true).label
        )
    }

    @Test
    fun obsidianLuxuryDesignSystem_darkAndLightModeTokens_matchSpecification() {
        // Dark Mode tokens
        com.example.ui.theme.ObsidianThemeState.isLightMode = false
        assertEquals(0xFF080A0D.toInt(), com.example.ui.theme.ObsidianBlack.value.shr(32).toInt())
        assertEquals(0xFF12161C.toInt(), com.example.ui.theme.RichGraphite.value.shr(32).toInt())
        assertEquals(0xFF202630.toInt(), com.example.ui.theme.ElevatedGraphite.value.shr(32).toInt())
        assertEquals(0xFF303944.toInt(), com.example.ui.theme.HairlinePlatinum.value.shr(32).toInt())
        assertEquals(0xFFF5F7FA.toInt(), com.example.ui.theme.WarmIvory.value.shr(32).toInt())
        assertEquals(0xFFA1AAB6.toInt(), com.example.ui.theme.MutedPlatinum.value.shr(32).toInt())
        assertEquals(0xFF84909E.toInt(), com.example.ui.theme.MutedIvory.value.shr(32).toInt())
        assertEquals(0xFFC4CBD4.toInt(), com.example.ui.theme.MetallicSilver.value.shr(32).toInt())
        assertEquals(0xFF00DFA5.toInt(), com.example.ui.theme.ChampagneGold.value.shr(32).toInt())
        assertEquals(0xFF00C98A.toInt(), com.example.ui.theme.SophisticatedEmerald.value.shr(32).toInt())
        assertEquals(0xFFFF5364.toInt(), com.example.ui.theme.CrimsonRuby.value.shr(32).toInt())
        assertEquals(0xFFF3B94F.toInt(), com.example.ui.theme.GoldReserveAccent.value.shr(32).toInt())

        // Light Mode tokens
        com.example.ui.theme.ObsidianThemeState.isLightMode = true
        assertEquals(0xFFF4F5F7.toInt(), com.example.ui.theme.ObsidianBlack.value.shr(32).toInt())
        assertEquals(0xFFFFFFFF.toInt(), com.example.ui.theme.RichGraphite.value.shr(32).toInt())
        assertEquals(0xFFEDF0F4.toInt(), com.example.ui.theme.ElevatedGraphite.value.shr(32).toInt())
        assertEquals(0xFFE5E9EF.toInt(), com.example.ui.theme.HairlinePlatinum.value.shr(32).toInt())
        assertEquals(0xFF171B22.toInt(), com.example.ui.theme.WarmIvory.value.shr(32).toInt())
        assertEquals(0xFF667085.toInt(), com.example.ui.theme.MutedPlatinum.value.shr(32).toInt())
        assertEquals(0xFF00A879.toInt(), com.example.ui.theme.ChampagneGold.value.shr(32).toInt())
        assertEquals(0xFF008F68.toInt(), com.example.ui.theme.SophisticatedEmerald.value.shr(32).toInt())
        assertEquals(0xFFD9364F.toInt(), com.example.ui.theme.CrimsonRuby.value.shr(32).toInt())
        assertEquals(0xFFA96B00.toInt(), com.example.ui.theme.GoldReserveAccent.value.shr(32).toInt())

        // Reset to Dark Mode default
        com.example.ui.theme.ObsidianThemeState.isLightMode = false
    }

    @Test
    fun threeIndependentThemes_andVoiceExpenseParser_workAndPersist() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Verify all 3 themes exist and resolve properly
        val themeA = com.example.ui.theme.getHudThemeById("stark")
        val themeB = com.example.ui.theme.getHudThemeById("neon")
        val themeC = com.example.ui.theme.getHudThemeById("light")
        assertTrue(themeA.name.contains("Theme A"))
        assertTrue(themeB.name.contains("Theme B"))
        assertTrue(themeC.name.contains("Theme C"))

        // Theme B: Neon Intelligence surface and accent check
        com.example.ui.theme.ObsidianThemeState.activeThemeId = "neon"
        assertEquals(0xFF060A17.toInt(), com.example.ui.theme.ObsidianBlack.value.shr(32).toInt())
        assertEquals(0xFF00E5FF.toInt(), com.example.ui.theme.ChampagneGold.value.shr(32).toInt())

        // Persistence check
        com.example.ui.theme.ThemePreferenceManager.saveThemePreference(context, "neon")
        assertEquals("neon", com.example.ui.theme.ThemePreferenceManager.loadThemePreference(context))

        // Reset to Theme A
        com.example.ui.theme.ThemePreferenceManager.saveThemePreference(context, "stark")
        assertEquals("stark", com.example.ui.theme.ObsidianThemeState.activeThemeId)

        // Voice expense entry parser check
        val parsed = com.example.ui.screens.parseSpokenExpensePhrase("Spent 350 on Zomato")
        assertNotNull(parsed)
        assertEquals(350L, parsed!!.second)
        assertEquals("Food", parsed.third)
    }

    @Test
    fun kharcheeStreaksLevels_andSnapDuplicateDetection_workProperly() {
        assertEquals("Bronze", com.example.data.model.KharcheeProfileAndToolsStore.computeStreakLevel(2, 0).levelName)
        assertEquals("Silver", com.example.data.model.KharcheeProfileAndToolsStore.computeStreakLevel(7, 0).levelName)
        assertEquals("Gold", com.example.data.model.KharcheeProfileAndToolsStore.computeStreakLevel(14, 0).levelName)
        assertEquals("Platinum", com.example.data.model.KharcheeProfileAndToolsStore.computeStreakLevel(30, 0).levelName)
        assertEquals("Diamond", com.example.data.model.KharcheeProfileAndToolsStore.computeStreakLevel(60, 0).levelName)

        val now = 1_730_000_000_000L
        val existing = listOf(
            ExpenseRecord(
                id = "tx1",
                title = "Haldiram Sweets",
                amount = 480L,
                category = "Food",
                txEpochMillis = now - 3600_000L
            )
        )
        val duplicate = com.example.ui.screens.detectDuplicateReceipt(
            existingExpenses = existing,
            amount = 480L,
            merchantName = "Haldiram",
            nowEpochMillis = now
        )
        assertNotNull("Should detect duplicate receipt with matching amount and merchant", duplicate)
        assertEquals("tx1", duplicate?.id)

        val nonDuplicate = com.example.ui.screens.detectDuplicateReceipt(
            existingExpenses = existing,
            amount = 920L,
            merchantName = "Apollo Pharmacy",
            nowEpochMillis = now
        )
        assertNull("Different amount should not trigger duplicate warning", nonDuplicate)
    }

    @Test
    fun kharcheeProModules1To40_andEcosystemFeatures_businessLogicAndMathVerification() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userId = "test_pro_suite_user"

        // Module 1: AI Money Predictor — empty history safe handling & reconciliation
        val emptyForecast = com.example.data.model.KharcheeProIntelligenceStore.predictMoneyForecast(
            period = com.example.data.model.ForecastPeriodOption.NEXT_30_DAYS,
            expenses = emptyList(),
            subscriptions = emptyList(),
            emis = emptyList(),
            reminders = emptyList(),
            monthlyBudget = 30000L,
            dailyLimit = 1000L
        )
        assertFalse(emptyForecast.hasSufficientData)
        assertEquals(
            emptyForecast.confirmedRecurringExpenses + emptyForecast.estimatedVariableExpenses,
            emptyForecast.predictedTotalExpenses
        )

        // Module 2: What-If Money Simulator — negative input rejection & net savings delta
        val baseline = com.example.data.model.WhatIfScenarioInput(
            monthlyIncome = 80000L,
            essentialExpenses = 25000L,
            lifestyleExpenses = 15000L,
            savingsContribution = 10000L
        )
        val invalidWhatIf = com.example.data.model.KharcheeProIntelligenceStore.evaluateWhatIfScenario(
            currentBaseline = baseline,
            simulated = baseline.copy(monthlyIncome = -5000L)
        )
        assertFalse(invalidWhatIf.isValid)

        val validWhatIf = com.example.data.model.KharcheeProIntelligenceStore.evaluateWhatIfScenario(
            currentBaseline = baseline,
            simulated = baseline.copy(lifestyleExpenses = 10000L, savingsContribution = 15000L)
        )
        assertTrue(validWhatIf.isValid)
        assertTrue(validWhatIf.estimatedTotalMonthlySavings >= validWhatIf.currentMonthlySavings)

        // Module 11: Multi-Wallet transfers do not inflate income or expenses
        val wallets = com.example.data.model.KharcheeProModules11To20Store.createOrUpdateWallet(
            context = context,
            userId = userId,
            walletId = "w_bank_$userId",
            name = "Primary Bank Account",
            walletType = com.example.data.model.ProWalletType.BANK,
            openingBalance = 50000L
        )
        com.example.data.model.KharcheeProModules11To20Store.createOrUpdateWallet(
            context = context,
            userId = userId,
            walletId = "w_cash_$userId",
            name = "Cash Wallet",
            walletType = com.example.data.model.ProWalletType.CASH,
            openingBalance = 5000L
        )
        assertTrue(wallets.isNotEmpty())
        val txRes = com.example.data.model.KharcheeProModules11To20Store.transferBetweenWallets(
            context = context,
            userId = userId,
            fromWalletId = "w_bank_$userId",
            toWalletId = "w_cash_$userId",
            amount = 2000L,
            note = "ATM Withdrawal"
        )
        assertTrue(txRes.isSuccess)
        val overview = com.example.data.model.KharcheeProModules11To20Store.computeConsolidatedWalletOverview(context, userId, emptyList())
        assertEquals(55000L, overview.consolidatedBalance)
        assertEquals(0L, overview.consolidatedIncome)
        assertEquals(0L, overview.consolidatedExpense)

        // Module 12: Loan & EMI Intelligence reducing-balance & zero-interest formulas
        val zeroInterestEmi = com.example.data.model.KharcheeProModules11To20Store.calculateLoanAmortization(12000L, 0.0, 12)
        assertTrue(zeroInterestEmi.isValid)
        assertEquals(1000L, zeroInterestEmi.monthlyEmi)
        val standardEmi = com.example.data.model.KharcheeProModules11To20Store.calculateLoanAmortization(100000L, 12.0, 12)
        assertTrue(standardEmi.monthlyEmi in 8880L..8890L)

        // Module 13: Smart Bill Manager valid day clamping (e.g. Feb 31 -> 28/29)
        assertEquals(28, com.example.data.model.KharcheeProModules11To20Store.resolveValidDayOfMonth(2025, Calendar.FEBRUARY, 31))
        assertEquals(29, com.example.data.model.KharcheeProModules11To20Store.resolveValidDayOfMonth(2024, Calendar.FEBRUARY, 31))

        // Module 17: Financial Document Vault format & size validation
        val badDoc = com.example.data.model.KharcheeProModules11To20Store.uploadDocumentMetadata(
            context = context,
            requestingUserId = userId,
            fileName = "malware.exe",
            mimeType = "application/x-msdownload",
            fileSizeBytes = 1024L,
            category = "Receipt",
            tags = emptyList()
        )
        assertTrue(badDoc.isFailure)

        // Module 24: Long-Term Wealth Planner compounding > 0% no-growth baseline
        val wealth = com.example.data.model.KharcheeProModules21To30Store.calculateLongTermWealthPlan(
            com.example.data.model.LongTermWealthScenarioInput(
                name = "5Y Plan",
                durationYears = 5,
                startingSavings = 100000L,
                monthlyContribution = 10000L,
                annualContributionGrowthPct = 0.0,
                assumedAnnualReturnPct = 12.0,
                annualInflationPct = 6.0,
                targetAmount = 800000L
            )
        )
        assertTrue(wealth.isValid)
        assertEquals(700000L, wealth.noGrowthFutureValue)
        assertTrue(wealth.projectedFutureValue > wealth.noGrowthFutureValue)

        // Module 34: Intelligent Budget Allocator shortfall detection
        val overPlan = com.example.data.model.KharcheeProModules31To40Store.evaluateBudgetAllocation(
            com.example.data.model.BudgetAllocationPlanInput(
                monthlyIncome = 50000L,
                essentialExpenses = 30000L,
                emiObligations = 15000L,
                bills = 5000L,
                subscriptions = 2000L,
                savingsGoals = 5000L,
                discretionarySpending = 3000L,
                emergencyReserveContribution = 2000L
            )
        )
        assertTrue(overPlan.isValid)
        assertTrue(overPlan.isOverAllocated)
        assertEquals(12000L, overPlan.shortfallAmount)

        // Module 36: Financial Benchmark Studio safe zero-denominator handling
        val benchEmpty = com.example.data.model.KharcheeProModules31To40Store.computeHistoricalBenchmark("MoM", emptyList())
        assertFalse(benchEmpty.hasPreviousPeriodData)
        assertNull(benchEmpty.expensePercentChange)

        // Module 38: Smart Reconciliation CSV parser validation
        val csvRes = com.example.data.model.KharcheeProModules31To40Store.parseAndReconcileCsvStatement(
            csvContent = "Date,Description,Amount\n2026-11-01,Swiggy,450\n2026-11-02,Amazon,1299",
            existingExpenses = listOf(ExpenseRecord(id = "e1", title = "Swiggy", amount = 450L))
        )
        assertTrue(csvRes.isSuccess)
        val rows = csvRes.getOrThrow()
        assertEquals(2, rows.size)
        assertEquals("EXACT_MATCH", rows[0].matchStatus)
        assertEquals("UNMATCHED_STATEMENT_ROW", rows[1].matchStatus)
    }

    @Test
    fun smartFirstTimeOnboarding_acceptanceTests_cleanWorkspaceSkipResumeAndPersonalisation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val freshUserId = "fresh_new_user_001"
        val existingUserId = "existing_veteran_user_002"

        com.example.data.model.KharcheeOnboardingProfileStore.clearOnboardingProfileForUser(context, freshUserId)
        com.example.data.model.KharcheeOnboardingProfileStore.clearOnboardingProfileForUser(context, existingUserId)
        com.example.data.model.KharcheeProfileAndToolsStore.clearAllForUser(context, freshUserId)

        // 1. Fresh account starts with zero fake balances and triggers guided onboarding
        val manualWallets = com.example.data.model.KharcheeProfileAndToolsStore.getManualWallets(context, freshUserId)
        assertTrue("New user manual wallets must start at 0", manualWallets.all { it.manualBalance == 0L })
        assertEquals(0L, com.example.data.model.KharcheeProfileAndToolsStore.getEmergencyFundSaved(context, freshUserId))

        val shouldLaunchForFresh = com.example.data.model.KharcheeOnboardingProfileStore.shouldShowGuidedOnboardingOnLaunch(
            context = context,
            userId = freshUserId,
            existingExpensesCount = 0,
            existingGoalsCount = 0,
            existingKhataCount = 0
        )
        assertTrue("Brand new user with 0 transactions must see guided onboarding", shouldLaunchForFresh)

        // Existing user with recorded transactions must NOT be forced through new-user onboarding
        val shouldLaunchForExisting = com.example.data.model.KharcheeOnboardingProfileStore.shouldShowGuidedOnboardingOnLaunch(
            context = context,
            userId = existingUserId,
            existingExpensesCount = 5,
            existingGoalsCount = 1,
            existingKhataCount = 0
        )
        assertFalse("Existing user with data must not be forced through onboarding", shouldLaunchForExisting)
        assertEquals(
            com.example.data.model.OnboardingLifecycleStatus.EXISTING_USER_BYPASSED,
            com.example.data.model.KharcheeOnboardingProfileStore.getOnboardingProfile(context, existingUserId).status
        )

        // 2. User can skip optional questions -> Incomplete state shown instead of inventing values
        val skippedDraft = com.example.data.model.UserFinancialOnboardingProfile(
            userId = freshUserId,
            currentStepIndex = 2,
            personal = com.example.data.model.PersonalProfileSection(
                fullName = "Aarav Sharma",
                occupation = "Student",
                preferredLanguage = "Hinglish",
                currency = "INR (₹)"
            )
            // Income, financialPosition, and monthlyExpenses all left empty/null (skipped)
        )
        val skippedPlan = com.example.data.model.KharcheeOnboardingProfileStore.computePersonalisedFinancialPlan(
            profile = skippedDraft,
            actualRecordedTransactions = emptyList()
        )
        assertFalse(skippedPlan.isIncomeKnown)
        assertFalse(skippedPlan.isExpensesKnown)
        assertTrue("Skipping income and expenses must produce an incomplete state", skippedPlan.isIncompleteState)
        assertNull("Must not invent remaining amount when income/expenses are unknown", skippedPlan.estimatedRemainingAmount)
        assertNull("Must not invent emergency coverage when expenses are unknown", skippedPlan.estimatedEmergencyFundCoverageMonths)

        // 3. User can leave and resume onboarding at exact saved step and answers
        com.example.data.model.KharcheeOnboardingProfileStore.saveOnboardingDraftProgress(
            context = context,
            userId = freshUserId,
            profile = skippedDraft.copy(currentStepIndex = 3),
            pauseForLater = true
        )
        val resumedProfile = com.example.data.model.KharcheeOnboardingProfileStore.getOnboardingProfile(context, freshUserId)
        assertEquals(com.example.data.model.OnboardingLifecycleStatus.PAUSED_FOR_LATER, resumedProfile.status)
        assertEquals(3, resumedProfile.currentStepIndex)
        assertEquals("Aarav Sharma", resumedProfile.personal.fullName)
        assertEquals("Student", resumedProfile.personal.occupation)

        // 4. User can complete onboarding, confirm profile, and edit later without overwriting transactions
        val fullProfile = resumedProfile.copy(
            income = com.example.data.model.IncomeProfileSection(
                primaryIncomeSource = "Monthly Stipend",
                primaryMonthlyIncomeAmount = 30000L,
                salaryOrPaymentDay = 5,
                incomeFrequency = "Monthly",
                isVariableIncome = false,
                additionalIncomeSources = listOf(
                    com.example.data.model.DeclaredAdditionalIncome(
                        sourceName = "Weekend Tutoring",
                        estimatedMonthlyAmount = 10000L
                    )
                )
            ),
            financialPosition = com.example.data.model.FinancialPositionSection(
                cashBalance = 2000L,
                bankBalance = 40000L,
                walletBalance = 3000L,
                existingSavings = 15000L,
                investmentsBalance = 20000L,
                outstandingLoansAndLiabilities = 5000L
            ),
            monthlyExpenses = com.example.data.model.MonthlyExpenseProfileSection(
                standardCategoryEstimates = mapOf(
                    "Rent or housing" to 10000L,
                    "Food and groceries" to 6000L,
                    "Transport" to 2000L,
                    "Education" to 2000L
                ),
                customCategoryEstimates = mapOf("Lab Supplies" to 1000L)
            ),
            goalsAndConsent = com.example.data.model.FinancialGoalsAndConsentSection(
                goals = listOf(
                    com.example.data.model.DeclaredFinancialGoal(
                        goalType = "Laptop or phone",
                        title = "Engineering Laptop",
                        targetAmount = 60000L,
                        existingSavedAmount = 10000L
                    )
                ),
                consentNotifications = true
            )
        )

        val existingTx = listOf(
            ExpenseRecord(id = "tx_real_1", userId = freshUserId, title = "Campus Cafe", amount = 120L, category = "Food", type = "expense")
        )
        val (confirmedProfile, confirmedPlan) = com.example.data.model.KharcheeOnboardingProfileStore.confirmAndActivateOnboardingProfile(
            context = context,
            userId = freshUserId,
            profile = fullProfile
        )
        assertEquals(com.example.data.model.OnboardingLifecycleStatus.COMPLETED, confirmedProfile.status)
        assertTrue(confirmedPlan.isIncomeKnown)
        assertTrue(confirmedPlan.isExpensesKnown)
        assertFalse(confirmedPlan.isIncompleteState)
        assertEquals(40000L, confirmedPlan.totalDeclaredMonthlyIncome)
        assertEquals(0L, confirmedPlan.actualReceivedMonthlyIncome) // Declared income is NOT treated as received transaction!
        assertEquals(21000L, confirmedPlan.estimatedMonthlyExpenses)
        assertEquals(19000L, confirmedPlan.estimatedRemainingAmount)
        assertEquals(60000L, confirmedPlan.existingDeclaredLiquidSavings)
        assertEquals(80000L, confirmedPlan.existingDeclaredTotalSavings)
        assertNotNull(confirmedPlan.estimatedEmergencyFundCoverageMonths)
        assertTrue(confirmedPlan.estimatedEmergencyFundCoverageMonths!! >= 2.8)
        assertTrue(confirmedPlan.goalContributionSuggestions.containsKey("Engineering Laptop"))

        // Verify existing transaction list is untouched when recomputing with edited profile
        val planWithRealTx = com.example.data.model.KharcheeOnboardingProfileStore.computePersonalisedFinancialPlan(
            profile = confirmedProfile,
            actualRecordedTransactions = existingTx
        )
        assertEquals(120L, planWithRealTx.actualRecordedMonthlyExpenses)
        assertEquals(21000L, planWithRealTx.estimatedMonthlyExpenses)
    }

    @Test
    fun kharcheeProModules41To50_businessRulesSecurityAndCalculations_workProperly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userId = "test_pro_41_to_50_user"

        // Module 41: Student Finance Mode
        com.example.data.model.KharcheeProModules41To50Store.saveStudentConfig(
            context,
            userId,
            com.example.data.model.StudentFinanceConfig(monthlyAllowance = 12000L, extraPocketMoneyThisMonth = 3000L)
        )
        com.example.data.model.KharcheeProModules41To50Store.addStudentExpense(
            context,
            userId,
            com.example.data.model.StudentExpenseEntry(title = "Algorithms Textbook", category = "Books", amount = 1500L)
        )
        val studentRep = com.example.data.model.KharcheeProModules41To50Store.buildStudentFinanceReport(context, userId)
        assertEquals(15000L, studentRep.totalAllowancePool)
        assertEquals(1500L, studentRep.monthStudentSpend)
        assertEquals(13500L, studentRep.allowanceRemaining)

        // Module 42: Side-Income Tracker — never marks payment as received without explicit user confirmation
        val recordedPayments = com.example.data.model.KharcheeProModules41To50Store.recordExpectedSideIncomePayment(
            context,
            userId,
            com.example.data.model.SideIncomePaymentRecord(
                sourceId = "src1",
                sourceName = "Freelance UI",
                description = "App Redesign Milestone",
                expectedAmount = 18000L,
                status = com.example.data.model.SideIncomePaymentStatus.CONFIRMED_RECEIVED // Attempt to pass confirmed without explicit confirmation
            )
        )
        val savedPayment = recordedPayments.first()
        assertFalse("Must not be confirmed without explicit user confirmation", savedPayment.confirmedByUser)
        assertEquals(0L, savedPayment.receivedAmount)
        assertEquals(com.example.data.model.SideIncomePaymentStatus.PENDING, savedPayment.status)

        // Confirm explicitly
        val afterConfirm = com.example.data.model.KharcheeProModules41To50Store.confirmSideIncomePaymentReceived(
            context = context,
            userId = userId,
            paymentId = savedPayment.id,
            explicitUserConfirmation = true,
            actualReceivedAmount = 18000L
        ).first()
        assertTrue(afterConfirm.confirmedByUser)
        assertEquals(18000L, afterConfirm.receivedAmount)

        // Module 43: Invoice & Receipt Generator — receipt gated behind payment confirmation & non-tax disclaimer
        val unpaidInvoice = com.example.data.model.KharcheeProModules41To50Store.createInvoice(
            context,
            userId,
            com.example.data.model.ProInvoiceRecord(
                invoiceNumber = "INV-2026-01",
                issuerName = "Kharchee Freelancer",
                customerName = "Acme Corp",
                customerContact = "billing@acme.example",
                lineItems = listOf(com.example.data.model.ProInvoiceLineItem(description = "Sprint 1", quantity = 2, unitPrice = 10000L)),
                discountAmount = 2000L,
                taxRatePercent = 18.0
            )
        )
        assertEquals(20000L, unpaidInvoice.subtotal)
        assertEquals(18000L, unpaidInvoice.taxableAmount)
        assertEquals(3240L, unpaidInvoice.taxAmount)
        assertEquals(21240L, unpaidInvoice.grandTotal)
        assertTrue(unpaidInvoice.complianceNotice.contains("Not a legally certified statutory GST/VAT tax invoice"))
        assertTrue(com.example.data.model.KharcheeProModules41To50Store.generateReceiptDocumentText(unpaidInvoice).isFailure)

        val paidInvoice = com.example.data.model.KharcheeProModules41To50Store.confirmInvoicePaidAndGenerateReceipt(
            context,
            userId,
            unpaidInvoice.id,
            explicitUserConfirmation = true,
            paymentMethod = "NEFT"
        )
        assertNotNull(paidInvoice)
        assertTrue(com.example.data.model.KharcheeProModules41To50Store.generateReceiptDocumentText(paidInvoice!!).isSuccess)

        // Module 45: Price Watchlist target detection
        val watchlistAfterAdd = com.example.data.model.KharcheeProModules41To50Store.addPriceWatchlistItem(
            context,
            userId,
            productName = "Noise Cancelling Headphones",
            merchantLabel = "Online Store",
            initialObservedPrice = 24999L,
            targetPrice = 19999L
        )
        val watchItem = watchlistAfterAdd.first()
        assertFalse(watchItem.isTargetReached)
        val watchUpdated = com.example.data.model.KharcheeProModules41To50Store.recordManualPriceObservation(
            context,
            userId,
            itemId = watchItem.id,
            newObservedPrice = 18999L
        ).first()
        assertTrue(watchUpdated.isTargetReached)

        // Module 46 & 47: Travel Planned vs Actual & Event Expected vs Confirmed Contributions separation
        val trips = com.example.data.model.KharcheeProModules41To50Store.createTravelTrip(
            context,
            userId,
            com.example.data.model.TravelTripPlan(
                tripName = "Goa Retreat",
                destination = "Goa",
                startDateLabel = "10 Dec",
                endDateLabel = "15 Dec",
                totalEstimatedBudget = 30000L
            )
        )
        val tripId = trips.first().id
        val tripsWithPlanned = com.example.data.model.KharcheeProModules41To50Store.addPlannedTravelExpense(
            context,
            userId,
            tripId = tripId,
            title = "Flight Tickets",
            category = "Transport",
            plannedAmount = 9000L
        )
        assertEquals(9000L, tripsWithPlanned.first().totalPlannedCost)
        assertEquals(0L, tripsWithPlanned.first().totalActualConfirmedCost)

        val events = com.example.data.model.KharcheeProModules41To50Store.createEventBudgetPlan(
            context,
            userId,
            com.example.data.model.ProEventBudgetPlan(
                eventName = "Family Anniversary",
                eventDateLabel = "20 Nov",
                baseAllocatedBudget = 25000L
            )
        )
        val evId = events.first().id
        val evWithExpected = com.example.data.model.KharcheeProModules41To50Store.addExpectedEventContribution(
            context,
            userId,
            eventId = evId,
            contributorName = "Cousin Rohit",
            expectedAmount = 5000L
        ).first()
        assertEquals(5000L, evWithExpected.totalExpectedContributionsUnconfirmed)
        assertEquals(0L, evWithExpected.totalConfirmedContributionsReceived)
        assertEquals(25000L, evWithExpected.totalAvailableConfirmedFunds) // Expected contribution NEVER inflates available funds!

        // Module 48: Financial Learning Academy quiz completion
        val progress = com.example.data.model.KharcheeProModules41To50Store.submitAcademyQuizAndCompleteLesson(
            context,
            userId,
            lessonId = "lesson_4_upi_safety",
            selectedOptionIndex = 1
        )
        assertTrue(progress.completedLessonIds.contains("lesson_4_upi_safety"))

        // Module 49: Document Matcher preserves original transaction & logs audit trail
        val originalTx = ExpenseRecord(id = "tx_99", title = "Swiggy", amount = 450L, category = "Food")
        val candidate = com.example.data.model.KharcheeProModules41To50Store.parseAndSuggestDocumentMatch(
            context,
            userId,
            documentTitle = "Dinner Receipt",
            rawDocumentText = "Merchant: Swiggy Total: Rs. 450 on 01/11/2026",
            existingTransactions = listOf(originalTx)
        )
        assertEquals(450L, candidate.extractedAmount)
        assertEquals("tx_99", candidate.suggestedTransactionId)
        com.example.data.model.KharcheeProModules41To50Store.reviewDocumentMatchCandidate(context, userId, candidate.id, confirmMatch = true)
        assertTrue(com.example.data.model.KharcheeProModules41To50Store.getDocumentMatchAuditTrail(context, userId).isNotEmpty())

        // Module 50: Personal Finance Data Insights API security (user isolation, scope check, revocation)
        val (_, tokenRecord) = com.example.data.model.KharcheeProModules41To50Store.generateScopedApiToken(
            context = context,
            ownerUserId = userId,
            label = "Summary Only Token",
            requestedScopes = setOf("read:summary")
        )
        // Cross-user access blocked (403)
        val crossUserResp = com.example.data.model.KharcheeProModules41To50Store.executeReadOnlyApiRequest(
            context = context,
            authenticatedUserId = "attacker_user",
            tokenOwnerUserId = userId,
            tokenId = tokenRecord.id,
            endpointPath = "/v1/me/summary",
            userTransactions = listOf(originalTx),
            userGoals = emptyList(),
            monthlyBudget = 30000L
        )
        assertEquals(403, crossUserResp.statusCode)

        // Missing scope blocked (403)
        val outOfScopeResp = com.example.data.model.KharcheeProModules41To50Store.executeReadOnlyApiRequest(
            context = context,
            authenticatedUserId = userId,
            tokenOwnerUserId = userId,
            tokenId = tokenRecord.id,
            endpointPath = "/v1/me/transactions",
            userTransactions = listOf(originalTx),
            userGoals = emptyList(),
            monthlyBudget = 30000L
        )
        assertEquals(403, outOfScopeResp.statusCode)

        // Authorized read-only summary succeeds (200)
        val okResp = com.example.data.model.KharcheeProModules41To50Store.executeReadOnlyApiRequest(
            context = context,
            authenticatedUserId = userId,
            tokenOwnerUserId = userId,
            tokenId = tokenRecord.id,
            endpointPath = "/v1/me/summary",
            userTransactions = listOf(originalTx),
            userGoals = emptyList(),
            monthlyBudget = 30000L
        )
        assertEquals(200, okResp.statusCode)
        assertTrue(okResp.isSuccess)

        // Revoked token blocked (401)
        com.example.data.model.KharcheeProModules41To50Store.revokeScopedApiToken(context, userId, tokenRecord.id)
        val revokedResp = com.example.data.model.KharcheeProModules41To50Store.executeReadOnlyApiRequest(
            context = context,
            authenticatedUserId = userId,
            tokenOwnerUserId = userId,
            tokenId = tokenRecord.id,
            endpointPath = "/v1/me/summary",
            userTransactions = listOf(originalTx),
            userGoals = emptyList(),
            monthlyBudget = 30000L
        )
        assertEquals(401, revokedResp.statusCode)
    }

    @Test
    fun introAndAuthenticationFlow_welcomeNavigationLoginRegistrationAndRouting_workProperly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.ui.auth.IntroAuthPreferences.resetWelcomeIntroForTesting(context)

        // 1. Onboarding navigation & copy verification
        assertEquals(3, com.example.ui.auth.KHARCHEE_WELCOME_SLIDES.size)
        assertEquals("Welcome to KHARCHEE", com.example.ui.auth.KHARCHEE_WELCOME_SLIDES.first().title)
        assertTrue(com.example.ui.auth.KHARCHEE_WELCOME_SLIDES.first().description.contains("Take control of your money"))

        // Unauthenticated first-launch user starts at WELCOME intro screen
        assertFalse(com.example.ui.auth.IntroAuthPreferences.hasCompletedWelcomeIntro(context))
        assertEquals(
            com.example.ui.auth.IntroAuthScreenStep.WELCOME,
            com.example.ui.auth.IntroAuthPreferences.resolveInitialStep(context)
        )

        // Clicking Skip or completing Next slides marks welcome intro completed and routes directly to LOGIN
        com.example.ui.auth.IntroAuthPreferences.markWelcomeIntroCompleted(context)
        assertTrue(com.example.ui.auth.IntroAuthPreferences.hasCompletedWelcomeIntro(context))
        assertEquals(
            com.example.ui.auth.IntroAuthScreenStep.LOGIN,
            com.example.ui.auth.IntroAuthPreferences.resolveInitialStep(context)
        )

        // 2. Login validation (empty email, invalid email, short password, valid login)
        val emptyEmailLogin = com.example.ui.auth.IntroAuthValidator.validateLogin("", "Secret123")
        assertFalse(emptyEmailLogin.isValid)

        val invalidEmailLogin = com.example.ui.auth.IntroAuthValidator.validateLogin("not-an-email", "Secret123")
        assertFalse(invalidEmailLogin.isValid)

        val shortPasswordLogin = com.example.ui.auth.IntroAuthValidator.validateLogin("arjun@kharchee.in", "123")
        assertFalse(shortPasswordLogin.isValid)

        val validLogin = com.example.ui.auth.IntroAuthValidator.validateLogin("arjun@kharchee.in", "Secret123")
        assertTrue(validLogin.isValid)
        assertNull(validLogin.errorMessage)

        // 3. Create Account registration validation
        val missingNameReg = com.example.ui.auth.IntroAuthValidator.validateRegistration(
            fullName = "A",
            email = "arjun@kharchee.in",
            password = "Password123",
            confirmPassword = "Password123",
            acceptedTerms = true
        )
        assertFalse(missingNameReg.isValid)

        val mismatchPasswordReg = com.example.ui.auth.IntroAuthValidator.validateRegistration(
            fullName = "Arjun Mehta",
            email = "arjun@kharchee.in",
            password = "Password123",
            confirmPassword = "DifferentPassword",
            acceptedTerms = true
        )
        assertFalse(mismatchPasswordReg.isValid)

        val unacceptedTermsReg = com.example.ui.auth.IntroAuthValidator.validateRegistration(
            fullName = "Arjun Mehta",
            email = "arjun@kharchee.in",
            password = "Password123",
            confirmPassword = "Password123",
            acceptedTerms = false
        )
        assertFalse(unacceptedTermsReg.isValid)

        val validRegistration = com.example.ui.auth.IntroAuthValidator.validateRegistration(
            fullName = "Arjun Mehta",
            email = "arjun@kharchee.in",
            password = "Password123",
            confirmPassword = "Password123",
            acceptedTerms = true
        )
        assertTrue(validRegistration.isValid)
        assertNull(validRegistration.errorMessage)

        // 4. Forgot password email validation
        assertFalse(com.example.ui.auth.IntroAuthValidator.validatePasswordResetEmail("").isValid)
        assertFalse(com.example.ui.auth.IntroAuthValidator.validatePasswordResetEmail("bad_email").isValid)
        assertTrue(com.example.ui.auth.IntroAuthValidator.validatePasswordResetEmail("arjun@kharchee.in").isValid)

        // 5. Remember me persistence & logout routing
        com.example.ui.auth.IntroAuthPreferences.saveRememberMeState(context, rememberMe = true, email = "  arjun@kharchee.in ")
        assertTrue(com.example.ui.auth.IntroAuthPreferences.isRememberMeEnabled(context))
        assertEquals("arjun@kharchee.in", com.example.ui.auth.IntroAuthPreferences.getRememberedEmail(context))

        com.example.ui.auth.IntroAuthPreferences.saveRememberMeState(context, rememberMe = false, email = "arjun@kharchee.in")
        assertFalse(com.example.ui.auth.IntroAuthPreferences.isRememberMeEnabled(context))
        assertEquals("", com.example.ui.auth.IntroAuthPreferences.getRememberedEmail(context))

        // 6. Verified SHA-256 Email/Password fallback account store & Apple ID session fallback
        val regSessionResult = com.example.ui.auth.IntroAuthPreferences.registerVerifiedFallbackAccount(
            context = context,
            fullName = "Arjun Mehta",
            email = "arjun@kharchee.in",
            password = "SecretPassword123"
        )
        assertTrue(regSessionResult.isSuccess)
        assertEquals("Arjun Mehta", regSessionResult.getOrNull()?.displayName)
        assertNotNull(com.example.ui.auth.IntroAuthPreferences.syncLocalSessionFromDisk(context))

        // Duplicate registration blocked
        val dupRegResult = com.example.ui.auth.IntroAuthPreferences.registerVerifiedFallbackAccount(
            context = context,
            fullName = "Arjun Mehta",
            email = "arjun@kharchee.in",
            password = "SecretPassword123"
        )
        assertTrue(dupRegResult.isFailure)

        // Wrong password rejected, correct password accepted
        com.example.ui.auth.IntroAuthPreferences.clearActiveLocalSession(context)
        assertNull(com.example.ui.auth.IntroAuthPreferences.syncLocalSessionFromDisk(context))

        val wrongPassLogin = com.example.ui.auth.IntroAuthPreferences.authenticateVerifiedFallbackAccount(
            context = context,
            email = "arjun@kharchee.in",
            password = "WrongPassword"
        )
        assertTrue(wrongPassLogin.isFailure)

        val rightPassLogin = com.example.ui.auth.IntroAuthPreferences.authenticateVerifiedFallbackAccount(
            context = context,
            email = "arjun@kharchee.in",
            password = "SecretPassword123"
        )
        assertTrue(rightPassLogin.isSuccess)
        assertEquals("arjun@kharchee.in", rightPassLogin.getOrNull()?.email)

        // Apple ID session authentication & Main Screen Indian currency formatter
        val appleSession = com.example.ui.auth.IntroAuthPreferences.authenticateAppleIdSession(
            context = context,
            appleEmail = "arjun@icloud.com",
            displayName = "Arjun Apple"
        )
        assertEquals("Arjun Apple", appleSession.displayName)
        assertEquals("24,525", com.example.ui.screens.formatIndianAmount(24525L))
    }

    @Test
    fun advancedKhataModule_andGlobalBranding_fullEndToEndVerification() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val userA = "test_khata_user_a"
        val userB = "test_khata_user_b"
        com.example.data.model.AdvancedKhataStore.clearAllForUser(context, userA)
        com.example.data.model.AdvancedKhataStore.clearAllForUser(context, userB)

        // 1. Verify Central Logo Assets are bundled and non-null
        assertNotNull(context.getDrawable(R.drawable.kharchee_logo_full))
        assertNotNull(context.getDrawable(R.drawable.kharchee_logo_symbol))

        val now = 1_730_700_000_000L

        // 2. Create a Lena Hai entry (₹5,000 receivable from Rahul)
        val lenaRes = com.example.data.model.AdvancedKhataStore.addUdhaarEntry(
            context = context,
            userId = userA,
            personName = "Rahul Sharma",
            phone = "9876543210",
            direction = com.example.data.model.KhataDirection.LENA_HAI,
            amount = 5000L,
            txDateMillis = now - 86_400_000L * 3,
            dueDateMillis = now - 86_400_000L, // overdue by 1 day
            description = "Emergency cash lent",
            category = "Personal Loan",
            notes = "Return after salary",
            bypassDedupWindow = true
        )
        assertTrue(lenaRes.isSuccess)
        val rahulContactId = lenaRes.getOrThrow().contactId

        // 3. Create a Dena Hai entry (₹1,800 payable to Priya)
        val denaRes = com.example.data.model.AdvancedKhataStore.addUdhaarEntry(
            context = context,
            userId = userA,
            personName = "Priya Verma",
            phone = "9123456780",
            direction = com.example.data.model.KhataDirection.DENA_HAI,
            amount = 1800L,
            txDateMillis = now - 86_400_000L * 2,
            dueDateMillis = now + 86_400_000L * 5,
            description = "Shared dinner bill",
            category = "Shared Bill",
            notes = "Settle via UPI",
            bypassDedupWindow = true
        )
        assertTrue(denaRes.isSuccess)

        // 4. Record a partial payment of ₹2,000 from Rahul against the ₹5,000 Lena Hai
        val partialRes = com.example.data.model.AdvancedKhataStore.recordPayment(
            context = context,
            userId = userA,
            contactId = rahulContactId,
            paymentAmount = 2000L,
            paymentDateMillis = now - 3_600_000L,
            description = "First installment received",
            notes = "Received via GPay",
            bypassDedupWindow = true
        )
        assertTrue(partialRes.isSuccess)

        val statesAfterPartial = com.example.data.model.AdvancedKhataStore.getAllContactStates(context, userA, now)
        val afterPartial = statesAfterPartial.first { it.contact.id == rahulContactId }

        // Remaining balance must be ₹3,000 Lena Hai, and original ₹5,000 entry must be preserved!
        assertEquals(3000L, afterPartial.outstandingAmount)
        assertEquals(com.example.data.model.KhataDirection.LENA_HAI, afterPartial.netDirection)
        assertEquals(5000L, afterPartial.totalOriginalLenaHai)
        assertEquals(2000L, afterPartial.totalPaymentsReceived)
        assertFalse(afterPartial.isSettled)
        assertEquals(2, afterPartial.activeEntries.size)

        // 5. Verify Dashboard Khata Summary Metrics
        val summaryMetrics = com.example.data.model.AdvancedKhataStore.computeSummaryMetrics(statesAfterPartial)
        assertEquals(3000L, summaryMetrics.totalLenaHai)
        assertEquals(1, summaryMetrics.lenaHaiPeopleCount)
        assertEquals(1800L, summaryMetrics.totalDenaHai)
        assertEquals(1, summaryMetrics.denaHaiPeopleCount)
        assertEquals(1200L, summaryMetrics.netOutstandingBalance) // 3000 - 1800
        assertEquals(2, summaryMetrics.totalActiveContacts)
        assertEquals(3000L, summaryMetrics.overdueAmount)

        // 6. Complete full settlement for Rahul and verify Settled status & reopening
        val settleRes = com.example.data.model.AdvancedKhataStore.settleContactInFull(
            context = context,
            userId = userA,
            contactId = rahulContactId,
            note = "Cleared remaining ₹3,000"
        )
        assertTrue(settleRes.isSuccess)
        val settledState = com.example.data.model.AdvancedKhataStore.getAllContactStates(context, userA, now)
            .first { it.contact.id == rahulContactId }
        assertTrue(settledState.isSettled)
        assertEquals(0L, settledState.outstandingAmount)
        assertEquals(5000L, settledState.totalOriginalLenaHai)
        assertEquals(5000L, settledState.totalPaymentsReceived)

        // Reopen settled ledger by adding a new ₹1,200 Lena Hai entry
        val reopenRes = com.example.data.model.AdvancedKhataStore.addUdhaarEntry(
            context = context,
            userId = userA,
            personName = "Rahul Sharma",
            phone = "9876543210",
            direction = com.example.data.model.KhataDirection.LENA_HAI,
            amount = 1200L,
            txDateMillis = now + 1000L,
            dueDateMillis = null,
            description = "Movie tickets",
            category = "Shared Bill",
            notes = "",
            bypassDedupWindow = true
        )
        assertTrue(reopenRes.isSuccess)
        val reopenedEntry = reopenRes.getOrThrow()
        val reopenedStates = com.example.data.model.AdvancedKhataStore.getAllContactStates(context, userA, now + 2000L)
        val rahulReopened = reopenedStates.first { it.contact.id == rahulContactId }
        assertFalse(rahulReopened.isSettled)
        assertEquals(1200L, rahulReopened.outstandingAmount)

        // 7. Edit & Delete entry verification
        val editRes = com.example.data.model.AdvancedKhataStore.editEntry(
            context = context,
            userId = userA,
            entryId = reopenedEntry.id,
            newAmount = 1500L,
            newDescription = "Movie + Snacks",
            newCategory = "Shared Bill",
            newDueDateMillis = null,
            newNotes = "Updated amount"
        )
        assertTrue(editRes.isSuccess)
        val rahulAfterEdit = com.example.data.model.AdvancedKhataStore.getAllContactStates(context, userA, now + 3000L)
            .first { it.contact.id == rahulContactId }
        assertEquals(1500L, rahulAfterEdit.outstandingAmount)

        val deleteRes = com.example.data.model.AdvancedKhataStore.deleteEntry(
            context = context,
            userId = userA,
            entryId = reopenedEntry.id
        )
        assertTrue(deleteRes.isSuccess)
        val rahulAfterDelete = com.example.data.model.AdvancedKhataStore.getAllContactStates(context, userA, now + 4000L)
            .first { it.contact.id == rahulContactId }
        assertEquals(0L, rahulAfterDelete.outstandingAmount)
        assertTrue(rahulAfterDelete.isSettled)

        // 8. Verify Unified Recent History across Income, Expense, Bills, Khata Entries & Khata Payments
        val audits = com.example.data.model.AdvancedKhataStore.loadAuditHistory(context, userA)
        val unified = com.example.data.model.AdvancedKhataStore.buildUnifiedRecentHistory(
            expenses = listOf(
                ExpenseRecord(id = "tx1", title = "Salary", amount = 45000L, category = "Salary", type = "income", txEpochMillis = now),
                ExpenseRecord(id = "tx2", title = "Groceries", amount = 850L, category = "Food", type = "expense", txEpochMillis = now - 5000L)
            ),
            paymentReminders = listOf(
                com.example.data.model.PaymentReminderRecord(id = "b1", title = "Netflix", amount = 649L, category = "Subscription", dueEpochMillis = now, isPaid = true)
            ),
            khataAuditItems = audits
        )
        assertTrue(unified.size >= 5)
        assertTrue(unified.any { it.kind == com.example.data.model.UnifiedActivityKind.INCOME })
        assertTrue(unified.any { it.kind == com.example.data.model.UnifiedActivityKind.EXPENSE })
        assertTrue(unified.any { it.kind == com.example.data.model.UnifiedActivityKind.BILL_PAYMENT })
        assertTrue(unified.any { it.kind == com.example.data.model.UnifiedActivityKind.KHATA_LENA_ENTRY })
        assertTrue(unified.any { it.kind == com.example.data.model.UnifiedActivityKind.KHATA_PAYMENT_RECEIVED })

        // 9. Verify user isolation (userB has 0 contacts and 0 entries)
        assertTrue(com.example.data.model.AdvancedKhataStore.getAllContactStates(context, userB, now).isEmpty())
    }

    @Test
    fun insightModule_financialIntelligenceAnalyticsAndReports_fullEndToEndVerification() {
        val now = 1_730_720_000_000L
        val dayMs = 86_400_000L

        val settings = UserSettings(
            userId = "insight_test_user",
            dailyLimit = 1000L
        )
        val categoryBudgets = mapOf(
            "Food" to 4000L,
            "Shopping" to 3000L,
            "Transport" to 2000L,
            "Bills" to 5000L
        )

        // 1. Verify Empty Dataset Handling (0 fabricated stats, insufficient data for health score)
        val emptyBundle = com.example.data.model.InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = emptyList(),
            settings = settings,
            categoryBudgets = categoryBudgets,
            savingsGoals = emptyList(),
            paymentReminders = emptyList(),
            filter = com.example.data.model.InsightPeriodFilter.THIS_MONTH,
            nowMillis = now
        )
        assertEquals(0L, emptyBundle.overview.totalIncome)
        assertEquals(0L, emptyBundle.overview.totalExpenses)
        assertEquals(0L, emptyBundle.overview.verifiedSavings)
        assertFalse(emptyBundle.overview.hasRecordedIncome)
        assertFalse(emptyBundle.healthScore.hasSufficientData)
        assertEquals("INSUFFICIENT DATA", emptyBundle.healthScore.category)

        // 2. Verify Expense-only Dataset never fabricates verified savings when income is unrecorded
        val expenseOnlyList = listOf(
            ExpenseRecord(id = "e1", title = "Zomato Dinner", amount = 1200L, category = "Food", type = "expense", txEpochMillis = now - dayMs)
        )
        val expenseOnlyBundle = com.example.data.model.InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = expenseOnlyList,
            settings = settings,
            categoryBudgets = categoryBudgets,
            savingsGoals = emptyList(),
            paymentReminders = emptyList(),
            filter = com.example.data.model.InsightPeriodFilter.THIS_MONTH,
            nowMillis = now
        )
        assertFalse(expenseOnlyBundle.savings.hasRecordedIncome)
        assertEquals(0L, expenseOnlyBundle.savings.verifiedPeriodSavings)

        // 3. Verify Complete Dataset (Income, Expenses, Categories, Budget Exceeded, Health Score, JARVIS, Reports)
        val fullTransactions = listOf(
            ExpenseRecord(id = "inc1", title = "Monthly Salary", amount = 50000L, category = "Salary", type = "income", txEpochMillis = now - dayMs * 2),
            ExpenseRecord(id = "exp1", title = "Swiggy & Groceries", amount = 4500L, category = "Food", type = "expense", txEpochMillis = now - dayMs * 2), // Exceeds 4000 Food budget
            ExpenseRecord(id = "exp2", title = "Amazon Headphones", amount = 2600L, category = "Shopping", type = "expense", txEpochMillis = now - dayMs), // Approaching 3000 Shopping budget (87%)
            ExpenseRecord(id = "exp3", title = "Metro & Cab", amount = 900L, category = "Transport", type = "expense", txEpochMillis = now),
            // Prior month transaction for MoM comparison
            ExpenseRecord(id = "prev1", title = "Last Month Rent", amount = 4000L, category = "Bills", type = "expense", txEpochMillis = now - dayMs * 35)
        )
        val goals = listOf(
            com.example.data.model.SavingsGoalRecord(id = "g1", title = "Emergency Fund", targetAmount = 20000L, savedAmount = 10000L)
        )
        val bills = listOf(
            com.example.data.model.PaymentReminderRecord(id = "r1", title = "WiFi Bill", amount = 999L, category = "Bills", dueEpochMillis = now - dayMs, isPaid = true)
        )

        val bundle = com.example.data.model.InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = fullTransactions,
            settings = settings,
            categoryBudgets = categoryBudgets,
            savingsGoals = goals,
            paymentReminders = bills,
            filter = com.example.data.model.InsightPeriodFilter.THIS_MONTH,
            nowMillis = now
        )

        // Income, Expense & Verified Savings
        assertEquals(50000L, bundle.overview.totalIncome)
        assertEquals(8000L, bundle.overview.totalExpenses)
        assertTrue(bundle.overview.hasRecordedIncome)
        assertEquals(42000L, bundle.overview.verifiedSavings)
        assertEquals(84, bundle.overview.savingsRatePercent)

        // Category breakdown & normalization
        assertEquals(3, bundle.categories.categories.size)
        assertEquals("Food", bundle.categories.topCategory?.category)
        assertEquals(4500L, bundle.categories.topCategory?.amount)
        assertEquals(56, bundle.categories.topCategory?.percentOfTotal)

        // Budget Intelligence: Food exceeded (4500 vs 4000), Shopping approaching (2600 vs 3000 = 87%)
        assertTrue(bundle.budget.exceedingCategories.any { it.category == "Food" && it.overageAmount == 500L })
        assertTrue(bundle.budget.approachingCategories.any { it.category == "Shopping" && it.utilizationPercent == 87 })

        // Financial Health Score
        assertTrue(bundle.healthScore.hasSufficientData)
        assertTrue(bundle.healthScore.score in 1..100)
        assertEquals(5, bundle.healthScore.factors.size)

        // JARVIS Insights
        assertTrue(bundle.jarvisInsights.isNotEmpty())
        assertTrue(bundle.jarvisInsights.all { it.whyItAppeared.isNotBlank() && it.periodLabel.isNotBlank() })

        // Period Filtering (This Week, Last Month, Custom Range)
        val customBundle = com.example.data.model.InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = fullTransactions,
            settings = settings,
            categoryBudgets = categoryBudgets,
            savingsGoals = goals,
            paymentReminders = bills,
            filter = com.example.data.model.InsightPeriodFilter.CUSTOM,
            nowMillis = now,
            customStartMillis = now - dayMs * 3,
            customEndMillis = now
        )
        assertEquals(8000L, customBundle.overview.totalExpenses)

        // Financial Reports (Structured text & CSV)
        val reportText = com.example.data.model.InsightIntelligenceEngine.generateStructuredReportText(bundle)
        val reportCsv = com.example.data.model.InsightIntelligenceEngine.generateStructuredReportCsv(bundle)
        assertTrue(reportText.contains("KHARCHEE FINANCIAL INTELLIGENCE REPORT"))
        assertTrue(reportText.contains("INR 50000"))
        assertTrue(reportCsv.contains("\"Category Breakdown\",\"Food\",4500"))
    }
}






