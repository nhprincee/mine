package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KharcheeRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndQueryExpense_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KharcheeRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createExpense(
                title = "Masala Chai",
                category = "Chai & Snacks",
                amount = 40L,
                paymentMode = "UPI",
                type = "expense",
                note = "Evening chai"
            )
        }
        assertTrue(createResult.isSuccess)
        val expenseId = createResult.getOrThrow()
        assertTrue(expenseId.isNotEmpty())

        val listResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserExpenses()
        }
        assertTrue(listResult.isSuccess)
        assertTrue(listResult.getOrThrow().any { it.id == expenseId && it.userId == uid })
    }

    @Test
    fun observeExpenses_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KharcheeRepository(firestore)

        val expenseId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createExpense(
                title = "Metro Card Recharge",
                category = "Transport",
                amount = 100L,
                paymentMode = "UPI"
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeExpenses(uid).first { list -> list.any { it.id == expenseId } }
        }
        assertTrue(emitted.any { it.id == expenseId })
    }

    @Test
    fun getExpenseById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = KharcheeRepository(firestore)
        val expenseId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createExpense(
                title = "Alice Secret Dinner",
                category = "Food",
                amount = 450L,
                paymentMode = "Card"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = KharcheeRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getExpenseById(aliceUid, expenseId)
        }
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeExpenses_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = KharcheeRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeExpenses(aliceUid).first()
            }
            fail("Expected exception for unauthenticated observeExpenses")
        } catch (e: Exception) {
            val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    @Test
    fun saveAndObserveJarvisMessages_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KharcheeRepository(firestore)

        val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveJarvisMessage(
                role = "user",
                text = "Jarvis, mera aaj ka kharcha kitna hai? बताओ",
                mode = "roast",
                language = "hinglish"
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeJarvisMessages(uid).first { list -> list.any { it.id == msgId } }
        }
        assertTrue(emitted.any { it.id == msgId && it.userId == uid })
    }

    @Test
    fun createAndObservePaymentReminders_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KharcheeRepository(firestore)

        val reminderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createPaymentReminder(
                title = "Broadband Fiber Bill",
                amount = 999L,
                category = "Bills",
                dueEpochMillis = System.currentTimeMillis() + 86_400_000L,
                intervalType = "1_day_before",
                note = "Monthly fiber internet"
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePaymentReminders(uid).first { list -> list.any { it.id == reminderId } }
        }
        assertTrue(emitted.any { it.id == reminderId && !it.isPaid })

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.markPaymentReminderPaid(reminderId, true).getOrThrow()
        }

        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePaymentReminders(uid).first { list -> list.any { it.id == reminderId && it.isPaid } }
        }
        assertTrue(updated.any { it.id == reminderId && it.isPaid })
    }

    @Test
    fun recordAndObserveNightGuardEvents_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KharcheeRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertUserSettings(
                dailyLimit = 600L,
                nightGuardEnabled = true,
                nightGuardStartHour = 22,
                nightGuardEndHour = 5
            ).getOrThrow()
            repository.updateNightGuardCooldown(
                cooldownEndMillis = System.currentTimeMillis() + 300_000L,
                lastChallengeId = "math_1"
            ).getOrThrow()
        }

        val eventId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordNightGuardEvent(
                title = "Late Night Gadget Impulse",
                category = "Shopping",
                amount = 2499L,
                outcome = "cancelled",
                challengeCategory = "Quick Mathematics"
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeNightGuardEvents(uid).first { list -> list.any { it.id == eventId } }
        }
        assertTrue(emitted.any { it.id == eventId && it.outcome == "cancelled" && it.amount == 2499L })
    }

    private companion object {
        const val ALICE_EMAIL = "alice@kharchee.test"
        const val BOB_EMAIL = "bob@kharchee.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
