package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ExpenseDao
import com.example.data.local.ExpenseRoomEntity
import com.example.data.local.GoalDao
import com.example.data.local.GoalRoomEntity
import com.example.data.local.KharcheeDatabase
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.ExpenseRecord
import com.example.data.model.InsightIntelligenceEngine
import com.example.data.model.InsightPeriodFilter
import com.example.data.model.JarvisChatMessage
import com.example.data.model.KhataDirection
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.ui.jarvis.FloatingJarvisArcReactorTrigger
import com.example.ui.jarvis.JarvisAssistantSheet
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.KharcheeTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianThemeState
import com.example.ui.theme.ThemePreferenceManager
import com.example.ui.viewmodel.KharcheeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: KharcheeDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var goalDao: GoalDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, KharcheeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()
        goalDao = database.goalDao()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Kharchee", appName)
    }

    @Test
    fun `kharcheeDatabase_expenseDao_and_goalDao_crudAndUserIsolation`() = runBlocking {
        val userA = "room_user_alpha"
        val userB = "room_user_beta"
        val now = 1_730_800_000_000L

        val salaryTx = ExpenseRecord(
            id = "exp_room_1",
            userId = userA,
            title = "Monthly Salary Credit",
            category = "Income",
            amount = 75_000L,
            paymentMode = "Bank",
            type = "income",
            note = "November Payout",
            txEpochMillis = now - 10_000L
        )
        val foodTx = ExpenseRecord(
            id = "exp_room_2",
            userId = userA,
            title = "Swiggy Instamart",
            category = "Food",
            amount = 1_450L,
            paymentMode = "UPI",
            type = "expense",
            note = "Weekly groceries",
            txEpochMillis = now
        )
        val otherUserTx = ExpenseRecord(
            id = "exp_room_3",
            userId = userB,
            title = "Other User Spend",
            category = "Shopping",
            amount = 3_200L,
            paymentMode = "Card",
            type = "expense",
            note = "Isolated",
            txEpochMillis = now
        )

        expenseDao.insertExpenses(
            listOf(
                ExpenseRoomEntity.fromDomainModel(salaryTx),
                ExpenseRoomEntity.fromDomainModel(foodTx),
                ExpenseRoomEntity.fromDomainModel(otherUserTx)
            )
        )

        // Verify reactive Flow and descending timestamp ordering for userA
        val userAExpenses = expenseDao.observeExpensesForUser(userA).first().map { it.toDomainModel() }
        assertEquals(2, userAExpenses.size)
        assertEquals("exp_room_2", userAExpenses[0].id)
        assertEquals("exp_room_1", userAExpenses[1].id)

        // Verify user isolation for userB
        val userBExpenses = expenseDao.getExpensesForUser(userB)
        assertEquals(1, userBExpenses.size)
        assertEquals("exp_room_3", userBExpenses[0].id)

        // Verify delete for userA
        expenseDao.deleteExpenseById("exp_room_2", userA)
        val afterDelete = expenseDao.getExpensesForUser(userA)
        assertEquals(1, afterDelete.size)
        assertEquals("exp_room_1", afterDelete[0].id)

        // Verify GoalDao CRUD & progress updates
        val macbookGoal = SavingsGoalRecord(
            id = "goal_room_1",
            userId = userA,
            title = "MacBook Pro M4",
            targetAmount = 180_000L,
            savedAmount = 45_000L
        )
        goalDao.insertGoal(GoalRoomEntity.fromDomainModel(macbookGoal))

        val initialGoals = goalDao.observeGoalsForUser(userA).first()
        assertEquals(1, initialGoals.size)
        assertEquals(45_000L, initialGoals[0].savedAmount)

        goalDao.updateGoalSavedAmount("goal_room_1", userA, 65_000L)
        val updatedGoals = goalDao.getGoalsForUser(userA)
        assertEquals(65_000L, updatedGoals[0].savedAmount)
        assertTrue(goalDao.getGoalsForUser(userB).isEmpty())

        goalDao.deleteGoalById("goal_room_1", userA)
        assertTrue(goalDao.getGoalsForUser(userA).isEmpty())
    }

    @Test
    fun `insightIntelligenceEngine_healthScore_periodFilters_and_verifiedSavings`() {
        val now = 1_730_800_000_000L
        val dayMs = 86_400_000L
        val settings = UserSettings(userId = "insight_robo_user", dailyLimit = 1_200L)
        val budgets = mapOf("Food" to 5_000L, "Shopping" to 4_000L)

        // 1. Unverified savings check (Expenses without recorded income -> 0 verified savings)
        val unverifiedBundle = InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = listOf(
                ExpenseRecord(id = "u1", title = "Coffee", amount = 350L, category = "Food", type = "expense", txEpochMillis = now - 3_600_000L)
            ),
            settings = settings,
            categoryBudgets = budgets,
            savingsGoals = emptyList(),
            paymentReminders = emptyList(),
            filter = InsightPeriodFilter.THIS_MONTH,
            nowMillis = now
        )
        assertFalse(unverifiedBundle.savings.hasRecordedIncome)
        assertEquals(0L, unverifiedBundle.savings.verifiedPeriodSavings)

        // 2. Verified savings, Health Score & Period Filters
        val verifiedTransactions = listOf(
            ExpenseRecord(id = "v1", title = "Salary", amount = 60_000L, category = "Salary", type = "income", txEpochMillis = now - dayMs * 2),
            ExpenseRecord(id = "v2", title = "Groceries", amount = 3_500L, category = "Food", type = "expense", txEpochMillis = now - dayMs),
            ExpenseRecord(id = "v3", title = "Jacket", amount = 2_500L, category = "Shopping", type = "expense", txEpochMillis = now - 3_600_000L),
            ExpenseRecord(id = "v4", title = "Old Expense", amount = 9_000L, category = "Shopping", type = "expense", txEpochMillis = now - dayMs * 45)
        )
        val monthBundle = InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = verifiedTransactions,
            settings = settings,
            categoryBudgets = budgets,
            savingsGoals = listOf(SavingsGoalRecord(id = "g1", title = "Emergency", targetAmount = 50_000L, savedAmount = 25_000L)),
            paymentReminders = emptyList(),
            filter = InsightPeriodFilter.THIS_MONTH,
            nowMillis = now
        )
        assertTrue(monthBundle.savings.hasRecordedIncome)
        assertEquals(60_000L, monthBundle.overview.totalIncome)
        assertEquals(6_000L, monthBundle.overview.totalExpenses)
        assertEquals(54_000L, monthBundle.overview.verifiedSavings)
        assertTrue(monthBundle.healthScore.hasSufficientData)
        assertTrue(monthBundle.healthScore.score in 1..100)
    }

    @Test
    fun `advancedKhataStore_lenaDenaBalances_auditLogs_and_userIsolation`() {
        val user1 = "khata_robo_u1"
        val user2 = "khata_robo_u2"
        val now = 1_730_800_000_000L
        AdvancedKhataStore.clearAllForUser(context, user1)
        AdvancedKhataStore.clearAllForUser(context, user2)

        val lenaEntry = AdvancedKhataStore.addUdhaarEntry(
            context = context,
            userId = user1,
            personName = "Vikram Aditya",
            phone = "9876501234",
            direction = KhataDirection.LENA_HAI,
            amount = 4_000L,
            txDateMillis = now - 86_400_000L,
            dueDateMillis = now + 86_400_000L * 3,
            description = "Project Equipment Advance",
            category = "Business",
            notes = "UPI transfer",
            bypassDedupWindow = true
        ).getOrThrow()

        AdvancedKhataStore.recordPayment(
            context = context,
            userId = user1,
            contactId = lenaEntry.contactId,
            paymentAmount = 1_500L,
            paymentDateMillis = now,
            description = "Part settlement",
            notes = "Cash",
            bypassDedupWindow = true
        ).getOrThrow()

        val states = AdvancedKhataStore.getAllContactStates(context, user1, now)
        val summary = AdvancedKhataStore.computeSummaryMetrics(states)
        assertEquals(2_500L, summary.totalLenaHai)
        assertEquals(0L, summary.totalDenaHai)
        assertTrue(AdvancedKhataStore.loadAuditHistory(context, user1).size >= 2)
        assertTrue(AdvancedKhataStore.getAllContactStates(context, user2, now).isEmpty())
    }

    @Test
    fun `jarvisAssistantSheet_and_multiThemeSwitching_renderCleanlyWithoutExceptions`() {
        val now = System.currentTimeMillis()
        val settings = UserSettings(userId = "robo_jarvis_user", dailyLimit = 1_000L)
        val expenses = listOf(
            ExpenseRecord(id = "j1", title = "Freelance Payout", amount = 50_000L, category = "Income", type = "income", txEpochMillis = now - 7_200_000L),
            ExpenseRecord(id = "j2", title = "Dining Out", amount = 1_800L, category = "Food", type = "expense", txEpochMillis = now - 1_800_000L)
        )

        val liveSnapshot = KharcheeViewModel.buildLiveFinancialContextSnapshotFromData(
            userDisplayName = "Prince",
            expenses = expenses,
            settings = settings
        )
        assertTrue(liveSnapshot.contains("Current Total Balance: ₹48200"))
        assertTrue(liveSnapshot.contains("Top Spending Category: Food"))

        val fallbackRoast = KharcheeViewModel.buildDeterministicJarvisFallbackReply(
            prompt = "Roast my food spending",
            userDisplayName = "Prince",
            mode = "roast",
            language = "hinglish",
            expenses = expenses,
            settings = settings
        )
        assertNotNull(fallbackRoast)
        assertTrue(fallbackRoast.contains("₹48200"))

        var triggerClicked = false
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            val showSheetState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            // Switch across all 3 themes (stark, neon, light) and render FloatingJarvisArcReactorTrigger -> JarvisAssistantSheet
            KharcheeTheme {
                Box(modifier = Modifier.size(400.dp)) {
                    FloatingJarvisArcReactorTrigger(
                        onClick = {
                            triggerClicked = true
                            showSheetState.value = true
                        },
                        isGenerating = false
                    )
                    if (showSheetState.value) {
                        JarvisAssistantSheet(
                            userDisplayName = "Prince",
                            todaySpent = 1_800L,
                            monthSpent = 1_800L,
                            dailyLimit = settings.dailyLimit,
                            disciplineScore = 88,
                            messages = listOf(
                                JarvisChatMessage(
                                    id = "welcome_1",
                                    role = "jarvis",
                                    text = fallbackRoast
                                )
                            ),
                            streamingReply = "",
                            isGenerating = false,
                            activeMode = "roast",
                            activeLanguage = "hinglish",
                            errorState = null,
                            latestSpokenText = null,
                            onConsumeLatestSpokenText = {},
                            onSelectMode = {},
                            onSelectLanguage = {},
                            totalBalance = 48_200L,
                            monthlyInflow = 50_000L,
                            monthlyOutflow = 1_800L,
                            topSpendingCategory = "Food",
                            remainingBudget = 28_200L,
                            onSendMessage = {},
                            onClearHistory = {},
                            onDismissError = {},
                            onClose = { showSheetState.value = false }
                        )
                    }
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(100L)

        // 1. Verify Floating Arc Reactor Trigger renders and opens JarvisAssistantSheet on click
        composeTestRule.onNodeWithTag("floating_jarvis_arc_reactor_trigger").assertExists()
        composeTestRule.onNodeWithTag("floating_jarvis_arc_reactor_trigger").performClick()
        composeTestRule.mainClock.advanceTimeBy(100L)
        assertTrue(triggerClicked)

        // 2. Verify JarvisAssistantSheet opens as a ModalBottomSheet without crash and has a close (✕) button
        composeTestRule.onNodeWithTag("jarvis_assistant_overlay").assertExists()
        composeTestRule.onNodeWithTag("jarvis_close_button").assertExists()
        composeTestRule.onNodeWithTag("jarvis_close_button").performClick()
        composeTestRule.mainClock.advanceTimeBy(100L)

        // Verify unified Purple Luxury theme lock (#05050A canvas, #A855F7 electric purple accent)
        ThemePreferenceManager.saveThemePreference(context, "neon")
        assertEquals("stark", ObsidianThemeState.activeThemeId)
        assertFalse(ObsidianThemeState.isLightMode)
        assertEquals(0xFF05050A.toInt(), ObsidianBlack.value.shr(32).toInt())
        assertEquals(0xFFA855F7.toInt(), ChampagneGold.value.shr(32).toInt())
    }

    @Test
    fun `monthlyFinancialPdfExporter_generatesMultiPageSummaryDocumentAndSavesForDownload`() {
        val now = 1_730_800_000_000L
        val settings = UserSettings(userId = "pdf_export_user", dailyLimit = 1_000L)
        val budgets = mapOf("Food" to 5_000L, "Shopping" to 4_000L)

        // Create 15 transactions (1 income + 14 expenses) to trigger 2-page pagination (>10 on page 1)
        val transactions = buildList {
            add(
                ExpenseRecord(
                    id = "pdf_inc_1",
                    userId = "pdf_export_user",
                    title = "Monthly Salary Credit",
                    category = "Salary",
                    amount = 65_000L,
                    paymentMode = "Bank",
                    type = "income",
                    txEpochMillis = now - 86_400_000L * 2
                )
            )
            for (i in 1..14) {
                add(
                    ExpenseRecord(
                        id = "pdf_exp_$i",
                        userId = "pdf_export_user",
                        title = "Merchant Purchase #$i",
                        category = if (i % 2 == 0) "Food" else "Shopping",
                        amount = 350L + (i * 50L),
                        paymentMode = "UPI",
                        type = "expense",
                        txEpochMillis = now - (i * 3_600_000L)
                    )
                )
            }
        }

        val options = com.example.util.MonthlyFinancialPdfExporter.buildMonthlyReportOptions(now)
        assertEquals(6, options.size)
        assertTrue(options.first().label.startsWith("This Month"))

        val activeBundle = InsightIntelligenceEngine.computeCompleteAnalytics(
            expenses = transactions,
            settings = settings,
            categoryBudgets = budgets,
            savingsGoals = listOf(SavingsGoalRecord(id = "g1", title = "Emergency Fund", targetAmount = 50_000L, savedAmount = 20_000L)),
            paymentReminders = emptyList(),
            filter = InsightPeriodFilter.THIS_MONTH,
            nowMillis = now
        )

        val (pdfBytes, pageCount) = com.example.util.MonthlyFinancialPdfExporter.generateMonthlyReportPdf(
            bundle = activeBundle,
            expenses = transactions,
            userDisplayName = "Prince",
            generatedAtMillis = now
        )
        assertTrue(pdfBytes.isNotEmpty())
        assertEquals(2, pageCount)

        val downloadResult = com.example.util.MonthlyFinancialPdfExporter.downloadPdfToDevice(
            context = context,
            bundle = activeBundle,
            expenses = transactions,
            userDisplayName = "Prince"
        )
        assertTrue(downloadResult.isSuccess)
        val exported = downloadResult.getOrThrow()
        assertTrue(exported.fileName.endsWith(".pdf"))
        assertTrue(exported.localCacheFile.exists())
        assertTrue(exported.byteSize > 0)
        assertEquals(2, exported.pageCount)
        assertEquals(15, exported.transactionCount)
    }
}
