package com.example.ui.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ExpenseRoomEntity
import com.example.data.local.KharcheeDatabase
import com.example.data.local.KhataLifetimeRoomEntity
import com.example.data.local.LifetimeFinancialMemorySystem
import com.example.data.model.ExpenseRecord
import com.example.data.model.JarvisChatMessage
import com.example.data.model.KhataFriendRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.data.repository.KharcheeRepository
import com.example.util.DailyLimitAlertTier
import com.example.util.EmergencyLegacyConfig
import com.example.util.EmergencyLegacyModeManager
import com.example.util.ImpulseCooldownManager
import com.example.util.KharcheeNotificationManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class JarvisGeminiIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var repository: KharcheeRepository
    private lateinit var database: KharcheeDatabase

    private data class RecordedGeminiCall(
        val systemPrompt: String,
        val history: List<JarvisConversationTurn>,
        val userPrompt: String
    )

    private class FakeJarvisGeminiGateway(
        var behavior: (RecordedGeminiCall) -> Flow<String>
    ) : JarvisGeminiGateway {
        val calls = mutableListOf<RecordedGeminiCall>()

        override fun streamReply(
            systemPrompt: String,
            conversationHistory: List<JarvisConversationTurn>,
            userPrompt: String
        ): Flow<String> {
            val call = RecordedGeminiCall(systemPrompt, conversationHistory, userPrompt)
            calls.add(call)
            return behavior(call)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:123456789:android:testkharchee")
                    .setApiKey("test-local-only-placeholder")
                    .setProjectId("test-kharchee-project")
                    .build()
            )
        }
        repository = KharcheeRepository(FirebaseFirestore.getInstance())
        database = Room.inMemoryDatabaseBuilder(context, KharcheeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun configuredGeminiModel_isSupportedGemini38Flash() {
        assertEquals("gemini-3.8-flash", FirebaseAiJarvisGeminiGateway.JARVIS_GEMINI_MODEL_ID)
    }

    @Test
    fun sendJarvisMessage_streamsRealGeminiReplyForGreetingsAndFollowUpMemory() = runTest(testDispatcher) {
        val gateway = FakeJarvisGeminiGateway { call ->
            flow {
                when {
                    call.userPrompt.equals("Hello", ignoreCase = true) -> {
                        emit("Hello Commander! ")
                        emit("How can I help with your finances today?")
                    }
                    call.userPrompt.contains("reduce it", ignoreCase = true) -> {
                        emit("Based on the ₹2,000 weekly spend you mentioned, try capping dining and shopping.")
                    }
                    else -> {
                        emit("Got it — noted your ₹2,000 weekly spend.")
                    }
                }
            }
        }
        val vm = KharcheeViewModel(repository = repository, currentUserId = "user_a", geminiGateway = gateway)
        vm.updateSettings(dailyLimit = 600L)
        vm.instantOneTapLogExpense("Swiggy Dinner", "Food", 350L, "UPI", "Dinner")
        advanceUntilIdle()

        // 1. Greeting ("Hello") goes through real Gemini gateway, NOT canned spend text.
        vm.sendJarvisMessage("Hello", userDisplayName = "Aarav")
        advanceUntilIdle()

        assertEquals(1, gateway.calls.size)
        val firstCall = gateway.calls.first()
        assertEquals("Hello", firstCall.userPrompt)
        assertTrue(firstCall.systemPrompt.contains("Today's Total Expense: ₹350"))
        assertTrue(firstCall.systemPrompt.contains("Daily Spending Limit: ₹600"))
        assertTrue(firstCall.systemPrompt.contains("Monthly Budget Target (30x Daily): ₹18000"))

        val msgsAfterHello = (vm.jarvisMessagesState.first { (it as? UiState.Success)?.data?.size == 2 } as UiState.Success).data
        assertEquals("Hello Commander! How can I help with your finances today?", msgsAfterHello.last().text)
        assertFalse(vm.jarvisIsGenerating.value)
        assertNull(vm.jarvisError.value)

        // 2. Multi-turn context: user states a spend, then asks a follow-up referring to "it".
        vm.sendJarvisMessage("I spent ₹2,000 this week.", userDisplayName = "Aarav")
        advanceUntilIdle()
        vm.sendJarvisMessage("How can I reduce it?", userDisplayName = "Aarav")
        advanceUntilIdle()

        assertEquals(3, gateway.calls.size)
        val followUpCall = gateway.calls.last()
        assertEquals("How can I reduce it?", followUpCall.userPrompt)
        assertTrue(
            "Conversation memory must include prior user message",
            followUpCall.history.any { it.role == "user" && it.text.contains("₹2,000 this week") }
        )
        assertTrue(
            "Conversation memory must include prior model reply",
            followUpCall.history.any { it.role == "model" && it.text.contains("noted your ₹2,000 weekly spend") }
        )
    }

    @Test
    fun sendJarvisMessage_ignoresEmptyInputAndDeduplicatesRapidRepeatedTaps() = runTest(testDispatcher) {
        val gateway = FakeJarvisGeminiGateway { _ ->
            flow {
                delay(500L)
                emit("Real Gemini response")
            }
        }
        val vm = KharcheeViewModel(repository = repository, currentUserId = "user_a", geminiGateway = gateway)

        // Empty / whitespace input produces zero requests
        vm.sendJarvisMessage("   ")
        advanceUntilIdle()
        assertEquals(0, gateway.calls.size)

        // Rapid repeated taps while generating only invoke Gemini once
        vm.sendJarvisMessage("How are you?")
        vm.sendJarvisMessage("How are you?")
        vm.sendJarvisMessage("How are you?")
        advanceUntilIdle()
        assertEquals(1, gateway.calls.size)
    }

    @Test
    fun stopJarvisGeneration_cancelsActiveStreamAndPreservesPartialReply() = runTest(testDispatcher) {
        val gateway = FakeJarvisGeminiGateway { _ ->
            flow {
                emit("First half of the analysis")
                delay(10_000L)
                emit(" should not appear after stop")
            }
        }
        val vm = KharcheeViewModel(repository = repository, currentUserId = "user_a", geminiGateway = gateway)

        vm.sendJarvisMessage("Analyze my spending")
        advanceTimeBy(100L)
        assertTrue(vm.jarvisIsGenerating.value)
        assertEquals("First half of the analysis", vm.jarvisStreamingReply.value)

        vm.stopJarvisGeneration()
        advanceUntilIdle()

        assertFalse(vm.jarvisIsGenerating.value)
        assertEquals("", vm.jarvisStreamingReply.value)
        val finalMessages = (vm.jarvisMessagesState.first {
            (it as? UiState.Success)?.data?.any { m -> m.role == "jarvis" } == true
        } as UiState.Success).data
        assertTrue(finalMessages.last().text.contains("First half of the analysis"))
        assertTrue(finalMessages.last().text.contains("Generation stopped"))
    }

    @Test
    fun sendJarvisMessage_surfacesRealOfflineAndRateLimitErrorsAndRetriesCleanly() = runTest(testDispatcher) {
        var attempt = 0
        val gateway = FakeJarvisGeminiGateway { _ ->
            attempt++
            when (attempt) {
                1 -> flow { throw UnknownHostException("Unable to resolve host generativelanguage.googleapis.com") }
                2 -> flow { throw RuntimeException("HTTP 429 RESOURCE_EXHAUSTED quota exceeded") }
                else -> flow { emit("Recovered Gemini response after retry!") }
            }
        }
        val vm = KharcheeViewModel(repository = repository, currentUserId = "user_a", geminiGateway = gateway)

        // Attempt 1: Offline error — surfaced honestly, never fake Gemini reply
        vm.sendJarvisMessage("What is KHARCHEE?")
        advanceUntilIdle()
        val offlineError = vm.jarvisError.value
        assertNotNull(offlineError)
        assertFalse(offlineError!!.isRateLimit)
        assertTrue(offlineError.message.contains("No internet connection", ignoreCase = true))
        assertEquals("What is KHARCHEE?", offlineError.lastFailedPrompt)

        // Attempt 2 (Retry): Rate limit error
        vm.retryLastFailedJarvisMessage()
        advanceUntilIdle()
        val rateError = vm.jarvisError.value
        assertNotNull(rateError)
        assertTrue(rateError!!.isRateLimit)
        assertTrue(rateError.message.contains("rate-limited", ignoreCase = true))

        // Attempt 3 (Retry): Succeeds and clears error
        vm.retryLastFailedJarvisMessage()
        advanceUntilIdle()
        assertNull(vm.jarvisError.value)
        val messages = (vm.jarvisMessagesState.first {
            (it as? UiState.Success)?.data?.any { m -> m.role == "jarvis" } == true
        } as UiState.Success).data
        assertEquals("Recovered Gemini response after retry!", messages.last().text)
    }

    @Test
    fun setJarvisMode_supportsBothCoachAndAdviceAndRoast() {
        val gateway = FakeJarvisGeminiGateway { flow { emit("ok") } }
        val vm = KharcheeViewModel(repository = repository, currentUserId = "user_a", geminiGateway = gateway)

        vm.setJarvisMode("coach")
        assertEquals("coach", vm.jarvisMode.value)
        vm.setJarvisMode("advice")
        assertEquals("coach", vm.jarvisMode.value)
        vm.setJarvisMode("roast")
        assertEquals("roast", vm.jarvisMode.value)
    }

    @Test
    fun conversationMemoryWindow_compressesOlderTurnsBeyondEight() {
        val longHistory = (1..12).map { i ->
            JarvisChatMessage(
                id = "m_$i",
                role = if (i % 2 == 1) "user" else "jarvis",
                text = "Message number $i about budgeting"
            )
        }
        val (summary, recent) = KharcheeViewModel.buildConversationMemoryWindow(longHistory)
        assertEquals(8, recent.size)
        assertTrue(summary.contains("4 prior messages compressed"))
        assertEquals("Message number 5 about budgeting", recent.first().text)
    }

    @Test
    fun lifetimeFinancialMemorySystem_separatesMoneyMovementFromPersonalSpendingAndNeverDeletesKhata() = runBlocking {
        val now = System.currentTimeMillis()
        val foodExpense = ExpenseRoomEntity.fromDomainModel(
            ExpenseRecord(id = "e1", userId = "u1", title = "Lunch", category = "Food", amount = 450L, type = "expense", txEpochMillis = now)
        )
        val khataLending = ExpenseRoomEntity.fromDomainModel(
            ExpenseRecord(id = "e2", userId = "u1", title = "Lent to Rohan", category = "Khata", amount = 2000L, type = "moneymovement", txEpochMillis = now)
        )
        val salaryIncome = ExpenseRoomEntity.fromDomainModel(
            ExpenseRecord(id = "e3", userId = "u1", title = "Salary", category = "Salary", amount = 50000L, type = "income", txEpochMillis = now)
        )

        database.expenseDao().insertExpenses(listOf(foodExpense, khataLending, salaryIncome))
        val personalSum = database.expenseDao().getPersonalSpendingTotalInRange("u1", now - 60_000L, now + 60_000L)
        val movementSum = database.expenseDao().getMoneyMovementTotalInRange("u1", now - 60_000L, now + 60_000L)
        assertEquals(450L, personalSum)
        assertEquals(2000L, movementSum)

        val khataEntry = KhataLifetimeRoomEntity(
            id = "kh_1",
            userId = "u1",
            contactId = "c1",
            contactName = "Rohan",
            direction = "LENA_HAI",
            recordType = "ORIGINAL_ENTRY",
            amount = 2000L,
            description = "Trip advance",
            txDateMillis = now,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        database.khataLifetimeDao().upsertEntry(khataEntry)
        database.khataLifetimeDao().softArchiveEntry("kh_1", "u1", now + 5_000L)

        val allPreserved = database.khataLifetimeDao().getAllLifetimeKhataEntries("u1")
        val activeOnly = database.khataLifetimeDao().getActiveKhataEntries("u1")
        assertEquals(1, allPreserved.size)
        assertTrue(allPreserved.first().isArchived)
        assertEquals(0, activeOnly.size)
    }

    @Test
    fun unfairAdvantageSuite_impulseLock_dailyLimitTracker_and_emergencyLegacyEncryptionWork() {
        // 1. Impulse-Buy Cooldown Lock: ₹500+ Shopping/Entertainment after 10 PM
        assertTrue(ImpulseCooldownManager.shouldAutoTriggerCooldownLock(500L, "Shopping", hourOfDay24 = 23))
        assertTrue(ImpulseCooldownManager.shouldAutoTriggerCooldownLock(1200L, "Entertainment", hourOfDay24 = 1))
        assertFalse(ImpulseCooldownManager.shouldAutoTriggerCooldownLock(499L, "Shopping", hourOfDay24 = 23))
        assertFalse(ImpulseCooldownManager.shouldAutoTriggerCooldownLock(1200L, "Shopping", hourOfDay24 = 15))
        assertFalse(ImpulseCooldownManager.shouldAutoTriggerCooldownLock(1200L, "Food", hourOfDay24 = 23))

        // 2. Daily Spending Limit Tracker
        val now = System.currentTimeMillis()
        val approachingStatus = KharcheeNotificationManager.evaluateDailySpendingLimit(
            expenses = listOf(ExpenseRecord(id = "d1", amount = 420L, type = "expense", txEpochMillis = now)),
            dailyLimit = 500L,
            nowMillis = now
        )
        assertEquals(DailyLimitAlertTier.APPROACHING_LIMIT, approachingStatus.statusTier)
        val exceededStatus = KharcheeNotificationManager.evaluateDailySpendingLimit(
            expenses = listOf(ExpenseRecord(id = "d2", amount = 650L, type = "expense", txEpochMillis = now)),
            dailyLimit = 500L,
            nowMillis = now
        )
        assertEquals(DailyLimitAlertTier.EXCEEDED_LIMIT, exceededStatus.statusTier)

        // 3. Emergency Legacy Mode 14-day threshold + AES-256-GCM encryption/decryption round-trip
        val fifteenDaysAgo = now - (15L * 86_400_000L)
        val legacyConfig = EmergencyLegacyConfig(
            enabled = true,
            trustedContactName = "Ananya",
            trustedContactDestination = "ananya@example.com",
            inactivityDaysThreshold = 14,
            lastActiveEpochMillis = fifteenDaysAgo
        )
        val eval = EmergencyLegacyModeManager.evaluateInactivityStatus(legacyConfig, nowMillis = now)
        assertTrue(eval.isInactivityTriggerReached)
        assertEquals(0L, eval.daysRemaining)

        val plainJson = EmergencyLegacyModeManager.buildPlainFinancialSummaryJson(
            ownerDisplayName = "Aarav",
            expenses = listOf(ExpenseRecord(id = "x1", amount = 500L, category = "Food", type = "expense", txEpochMillis = now)),
            khataFriends = listOf(KhataFriendRecord(id = "k1", name = "Rohan", balance = 1500L)),
            savingsGoals = listOf(SavingsGoalRecord(id = "g1", title = "Emergency", targetAmount = 50000L, savedAmount = 12000L)),
            customNote = "Contact CA for tax folder"
        )
        val encrypted = EmergencyLegacyModeManager.encryptSummaryPayload(plainJson, "SovereignPass2026")
        assertTrue(encrypted.startsWith("KHARCHEE_LEGACY_AES256_V1:"))
        assertFalse(encrypted.contains("Rohan"))
        val decrypted = EmergencyLegacyModeManager.decryptSummaryPayload(encrypted, "SovereignPass2026")
        assertEquals(plainJson, decrypted)
        assertTrue(decrypted.contains("\"ghostVaultIncluded\": false"))
    }

    @Test
    fun jarvisPromptAndTelemetry_enforcesConciseStyleAndSeparatesKhataFromPersonalSpend() {
        val now = System.currentTimeMillis()
        val expenses = listOf(
            ExpenseRecord(id = "p1", title = "Coffee", category = "Food", amount = 250L, type = "expense", txEpochMillis = now),
            ExpenseRecord(id = "m1", title = "Lent to Kabir", category = "Khata", amount = 3000L, type = "moneymovement", txEpochMillis = now),
            ExpenseRecord(id = "i1", title = "Freelance", category = "Income", amount = 10000L, type = "income", txEpochMillis = now)
        )
        val khata = listOf(
            KhataFriendRecord(id = "k1", name = "Kabir", balance = 3000L, lastDescription = "Loan"),
            KhataFriendRecord(id = "k2", name = "Riya", balance = -700L, lastDescription = "Dinner split")
        )
        val telemetry = KharcheeViewModel.buildLiveFinancialContextSnapshotFromData(
            userDisplayName = "Aarav",
            expenses = expenses,
            settings = UserSettings(dailyLimit = 500L),
            khata = khata
        )

        // Personal spending must strictly be ₹250, NOT ₹3,250!
        assertTrue(telemetry.contains("Today's Total Expense: ₹250 (Remaining today: ₹250)"))
        assertTrue(telemetry.contains("Monthly Personal Spending (Excludes Khata/Lending): ₹250"))
        assertTrue(telemetry.contains("Monthly Khata / Money Movement (Lending/Borrowing — NOT Personal Spending): ₹3000"))
        assertTrue(telemetry.contains("To Receive (Lena Hai) ₹3000 | To Pay (Dena Hai) ₹700"))
        assertTrue(telemetry.contains("[KHATA MONEY MOVEMENT (NOT PERSONAL SPEND)]"))

        val prompt = KharcheeViewModel.buildJarvisSystemPrompt(
            userDisplayName = "Aarav",
            mode = "coach",
            language = "hinglish",
            liveFinancialTelemetry = telemetry
        )
        assertTrue(prompt.contains("ANSWER FIRST"))
        assertTrue(prompt.contains("HIGH-SIGNAL BREVITY"))
        assertTrue(prompt.contains("KHATA VS PERSONAL SPENDING INVARIANT"))
    }

    @Test
    fun retryLastFailedJarvisMessage_doesNotDuplicateUserTurnInHistoryAndFlagsOffline() = runTest(testDispatcher) {
        var attempt = 0
        val gateway = FakeJarvisGeminiGateway { _ ->
            attempt++
            if (attempt == 1) {
                flow { throw UnknownHostException("Unable to resolve host") }
            } else {
                flow { emit("₹250 spent today, ₹350 left.") }
            }
        }
        val vm = KharcheeViewModel(repository = repository, currentUserId = "user_a", geminiGateway = gateway)

        vm.sendJarvisMessage("Aaj ka kharcha?")
        advanceUntilIdle()
        val err = vm.jarvisError.value
        assertNotNull(err)
        assertTrue(err!!.isOffline)

        vm.retryLastFailedJarvisMessage()
        advanceUntilIdle()

        assertEquals(2, gateway.calls.size)
        val retryCall = gateway.calls.last()
        assertEquals("Aaj ka kharcha?", retryCall.userPrompt)
        assertTrue(
            "Retry must not inject the failed user prompt into conversationHistory",
            retryCall.history.none { it.role == "user" && it.text == "Aaj ka kharcha?" }
        )
        assertNull(vm.jarvisError.value)
    }

    @Test
    fun dailySpendingLimit_excludesKhataMoneyMovementAndUpdatesReactively() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val evaluated = KharcheeNotificationManager.evaluateDailySpendingLimit(
            expenses = listOf(
                ExpenseRecord(id = "e1", amount = 300L, category = "Food", type = "expense", txEpochMillis = now),
                ExpenseRecord(id = "e2", amount = 4000L, category = "Khata", type = "moneymovement", txEpochMillis = now),
                ExpenseRecord(id = "e3", amount = 5000L, category = "Salary", type = "income", txEpochMillis = now)
            ),
            dailyLimit = 500L,
            nowMillis = now
        )
        assertEquals(300L, evaluated.todaySpent)
        assertEquals(200L, evaluated.remainingBudget)
        assertEquals(DailyLimitAlertTier.SAFE, evaluated.statusTier)

        val gateway = FakeJarvisGeminiGateway { flow { emit("ok") } }
        val vm = KharcheeViewModel(repository = repository, currentUserId = "user_a", geminiGateway = gateway)
        vm.updateDailyLimit(500L)
        vm.instantOneTapLogExpense("Lunch", "Food", 450L, "UPI", "Lunch")
        advanceUntilIdle()

        val status = vm.dailySpendingLimitState.first { it.todaySpent == 450L }
        assertEquals(500L, status.dailyLimit)
        assertEquals(50L, status.remainingBudget)
        assertEquals(DailyLimitAlertTier.APPROACHING_LIMIT, status.statusTier)
    }
}
