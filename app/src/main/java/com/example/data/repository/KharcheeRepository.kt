package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ExpenseRecord
import com.example.data.model.JarvisChatMessage
import com.example.data.model.KhataFriendRecord
import com.example.data.model.NightGuardEventRecord
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToLong

class KharcheeRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- USER SETTINGS ---

    fun observeUserSettings(userId: String = requireUserId()): Flow<UserSettings?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                    throw error
                }
        )
    }

    suspend fun upsertUserSettings(
        dailyLimit: Long = 500L,
        lockdownActive: Boolean = false,
        autoLockdownAt90: Boolean = true,
        stealthMode: Boolean = false,
        themeId: String = "stark",
        myUpiId: String = "",
        upiGuardTone: String = "hinglish",
        isPro: Boolean = false,
        proTier: String = "free",
        proExpiryMillis: Long = 0L,
        nightGuardEnabled: Boolean = true,
        nightGuardStartHour: Int = 22,
        nightGuardStartMinute: Int = 0,
        nightGuardEndHour: Int = 5,
        nightGuardEndMinute: Int = 0,
        nightGuardCooldownEndMillis: Long = 0L,
        lastChallengeId: String = "",
        enablePreSpendConfirm: Boolean = false,
        notifyPaymentReminders: Boolean = true,
        notifyNightGuardAlert: Boolean = true,
        notifyMorningReview: Boolean = false
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "dailyLimit" to dailyLimit.coerceIn(50L, 500000L),
                    "lockdownActive" to lockdownActive,
                    "autoLockdownAt90" to autoLockdownAt90,
                    "stealthMode" to stealthMode,
                    "themeId" to themeId,
                    "myUpiId" to myUpiId.take(100),
                    "upiGuardTone" to upiGuardTone,
                    "isPro" to isPro,
                    "proTier" to proTier,
                    "proExpiryMillis" to proExpiryMillis.coerceAtLeast(0L),
                    "nightGuardEnabled" to nightGuardEnabled,
                    "nightGuardStartHour" to nightGuardStartHour.coerceIn(0, 23),
                    "nightGuardStartMinute" to nightGuardStartMinute.coerceIn(0, 59),
                    "nightGuardEndHour" to nightGuardEndHour.coerceIn(0, 23),
                    "nightGuardEndMinute" to nightGuardEndMinute.coerceIn(0, 59),
                    "nightGuardCooldownEndMillis" to nightGuardCooldownEndMillis.coerceAtLeast(0L),
                    "lastChallengeId" to lastChallengeId.take(64),
                    "enablePreSpendConfirm" to enablePreSpendConfirm,
                    "notifyPaymentReminders" to notifyPaymentReminders,
                    "notifyNightGuardAlert" to notifyNightGuardAlert,
                    "notifyMorningReview" to notifyMorningReview,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "dailyLimit" to dailyLimit.coerceIn(50L, 500000L),
                    "lockdownActive" to lockdownActive,
                    "autoLockdownAt90" to autoLockdownAt90,
                    "stealthMode" to stealthMode,
                    "themeId" to themeId,
                    "myUpiId" to myUpiId.take(100),
                    "upiGuardTone" to upiGuardTone,
                    "isPro" to isPro,
                    "proTier" to proTier,
                    "proExpiryMillis" to proExpiryMillis.coerceAtLeast(0L),
                    "nightGuardEnabled" to nightGuardEnabled,
                    "nightGuardStartHour" to nightGuardStartHour.coerceIn(0, 23),
                    "nightGuardStartMinute" to nightGuardStartMinute.coerceIn(0, 59),
                    "nightGuardEndHour" to nightGuardEndHour.coerceIn(0, 23),
                    "nightGuardEndMinute" to nightGuardEndMinute.coerceIn(0, 59),
                    "nightGuardCooldownEndMillis" to nightGuardCooldownEndMillis.coerceAtLeast(0L),
                    "lastChallengeId" to lastChallengeId.take(64),
                    "enablePreSpendConfirm" to enablePreSpendConfirm,
                    "notifyPaymentReminders" to notifyPaymentReminders,
                    "notifyNightGuardAlert" to notifyNightGuardAlert,
                    "notifyMorningReview" to notifyMorningReview,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateNightGuardCooldown(
        cooldownEndMillis: Long,
        lastChallengeId: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                val updates = mapOf(
                    "nightGuardCooldownEndMillis" to cooldownEndMillis.coerceAtLeast(0L),
                    "lastChallengeId" to lastChallengeId.take(64),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateProMembership(tier: String, durationDays: Int): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val expiryMillis = System.currentTimeMillis() + (durationDays.toLong() * 86_400_000L)
        try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "dailyLimit" to 500L,
                    "lockdownActive" to false,
                    "autoLockdownAt90" to true,
                    "stealthMode" to false,
                    "themeId" to "stark",
                    "myUpiId" to "",
                    "upiGuardTone" to "hinglish",
                    "isPro" to true,
                    "proTier" to tier,
                    "proExpiryMillis" to expiryMillis,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "isPro" to true,
                    "proTier" to tier,
                    "proExpiryMillis" to expiryMillis,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- EXPENSES & INCOME ---

    fun observeExpenses(userId: String = requireUserId()): Flow<List<ExpenseRecord>> = flow {
        val path = "users/$userId/expenses"
        emitAll(
            db.collection("users").document(userId).collection("expenses")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ExpenseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.txEpochMillis }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createExpense(
        title: String,
        category: String,
        amount: Long,
        paymentMode: String = "UPI",
        type: String = "expense",
        note: String = "",
        txEpochMillis: Long = System.currentTimeMillis()
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("expenses")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(120).ifEmpty { "Expense" },
            "category" to category,
            "amount" to amount.coerceIn(1L, 10000000L),
            "paymentMode" to paymentMode,
            "type" to type,
            "note" to note.trim().take(300),
            "txEpochMillis" to txEpochMillis,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateExpense(expense: ExpenseRecord): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(expense.id.isNotBlank()) { "Expense ID must not be blank for update" }
        val docRef = db.collection("users").document(uid).collection("expenses").document(expense.id)
        val updates = mapOf(
            "title" to expense.title.trim().take(120).ifEmpty { "Expense" },
            "category" to expense.category,
            "amount" to expense.amount.coerceIn(1L, 10000000L),
            "paymentMode" to expense.paymentMode,
            "type" to expense.type,
            "note" to expense.note.trim().take(300),
            "txEpochMillis" to expense.txEpochMillis,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("expenses").document(expenseId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun getExpenseById(ownerUserId: String, expenseId: String): Result<ExpenseRecord?> = runCatching {
        val docRef = db.collection("users").document(ownerUserId).collection("expenses").document(expenseId)
        try {
            val snap = docRef.get().await()
            snap.toObject(ExpenseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun getUserExpenses(): Result<List<ExpenseRecord>> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid/expenses"
        try {
            val snap = db.collection("users").document(uid).collection("expenses")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            snap.toObjects(ExpenseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    // --- KHATA FRIENDS (SPLIT LEDGER) ---

    fun observeKhataFriends(userId: String = requireUserId()): Flow<List<KhataFriendRecord>> = flow {
        val path = "users/$userId/khata_friends"
        emitAll(
            db.collection("users").document(userId).collection("khata_friends")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(KhataFriendRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createKhataFriend(
        name: String,
        phone: String,
        upiId: String,
        initialBalance: Long,
        description: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("khata_friends")
        val docRef = colRef.document()
        val cleanName = name.trim().take(80).ifEmpty { "Friend" }
        val initials = cleanName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "FR" }
        val payload = mapOf(
            "userId" to uid,
            "name" to cleanName,
            "phone" to phone.trim().take(20),
            "avatarInitials" to initials.take(4),
            "upiId" to upiId.trim().take(100),
            "balance" to initialBalance.coerceIn(-10000000L, 10000000L),
            "lastDescription" to description.trim().take(200).ifEmpty { "Split entry" },
            "settled" to (initialBalance == 0L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun addKhataEntry(
        friend: KhataFriendRecord,
        deltaAmount: Long,
        description: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("khata_friends").document(friend.id)
        val nextBalance = (friend.balance + deltaAmount).coerceIn(-10000000L, 10000000L)
        val updates = mapOf(
            "balance" to nextBalance,
            "lastDescription" to description.trim().take(200).ifEmpty { friend.lastDescription },
            "settled" to (nextBalance == 0L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun settleKhataFriend(friendId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("khata_friends").document(friendId)
        val updates = mapOf(
            "balance" to 0L,
            "settled" to true,
            "lastDescription" to "Settled Up ✅",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun splitBillAmongFriends(
        friends: List<KhataFriendRecord>,
        totalBill: Long,
        description: String,
        includeSelf: Boolean
    ): Result<Unit> = runCatching {
        if (friends.isEmpty() || totalBill <= 0L) return@runCatching
        val divisor = if (includeSelf) friends.size + 1 else friends.size
        val perPerson = (totalBill.toDouble() / divisor.toDouble()).roundToLong().coerceAtLeast(1L)
        for (friend in friends) {
            addKhataEntry(friend, perPerson, "$description (Split ₹$totalBill)").getOrThrow()
        }
    }

    // --- SAVINGS GOALS ---

    fun observeSavingsGoals(userId: String = requireUserId()): Flow<List<SavingsGoalRecord>> = flow {
        val path = "users/$userId/savings_goals"
        emitAll(
            db.collection("users").document(userId).collection("savings_goals")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SavingsGoalRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createSavingsGoal(
        title: String,
        targetAmount: Long,
        savedAmount: Long = 0L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("savings_goals").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(100).ifEmpty { "Savings Goal" },
            "targetAmount" to targetAmount.coerceIn(100L, 100000000L),
            "savedAmount" to savedAmount.coerceIn(0L, 100000000L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun addFundsToSavingsGoal(goal: SavingsGoalRecord, addAmount: Long): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("savings_goals").document(goal.id)
        val nextSaved = (goal.savedAmount + addAmount).coerceIn(0L, 100000000L)
        val updates = mapOf(
            "savedAmount" to nextSaved,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("savings_goals").document(goalId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- JARVIS AI CONVERSATION HISTORY ---

    fun observeJarvisMessages(userId: String = requireUserId()): Flow<List<JarvisChatMessage>> = flow {
        val path = "users/$userId/jarvis_messages"
        emitAll(
            db.collection("users").document(userId).collection("jarvis_messages")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(JarvisChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.txEpochMillis }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveJarvisMessage(
        role: String,
        text: String,
        mode: String = "roast",
        language: String = "hinglish",
        txEpochMillis: Long = System.currentTimeMillis()
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cleanText = text.trim().take(4000)
        require(cleanText.isNotEmpty()) { "Message text cannot be empty" }
        val cleanRole = if (role == "jarvis") "jarvis" else "user"
        val cleanMode = if (mode == "advice") "advice" else "roast"
        val cleanLang = when (language.lowercase()) {
            "hindi" -> "hindi"
            "english" -> "english"
            else -> "hinglish"
        }
        val docRef = db.collection("users").document(uid).collection("jarvis_messages").document()
        val payload = mapOf(
            "userId" to uid,
            "role" to cleanRole,
            "text" to cleanText,
            "mode" to cleanMode,
            "language" to cleanLang,
            "txEpochMillis" to txEpochMillis,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun clearJarvisMessages(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("jarvis_messages")
        try {
            val snap = colRef.whereEqualTo("userId", uid).get().await()
            for (doc in snap.documents) {
                doc.reference.delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/jarvis_messages")
            throw e
        }
    }

    // --- SMART PAYMENT REMINDERS ---

    fun observePaymentReminders(userId: String = requireUserId()): Flow<List<PaymentReminderRecord>> = flow {
        val path = "users/$userId/payment_reminders"
        emitAll(
            db.collection("users").document(userId).collection("payment_reminders")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PaymentReminderRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.dueEpochMillis }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createPaymentReminder(
        title: String,
        amount: Long,
        category: String = "Bills",
        dueEpochMillis: Long,
        intervalType: String = "same_day",
        note: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cleanTitle = title.trim().take(120).ifEmpty { "Scheduled Payment" }
        val cleanInterval = if (intervalType in listOf("same_day", "1_day_before", "3_days_before", "weekly")) {
            intervalType
        } else {
            "same_day"
        }
        val docRef = db.collection("users").document(uid).collection("payment_reminders").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to cleanTitle,
            "amount" to amount.coerceIn(1L, 10000000L),
            "category" to category,
            "dueEpochMillis" to dueEpochMillis.coerceAtLeast(1L),
            "intervalType" to cleanInterval,
            "isPaid" to false,
            "note" to note.trim().take(300),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun markPaymentReminderPaid(reminderId: String, isPaid: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("payment_reminders").document(reminderId)
        val updates = mapOf(
            "isPaid" to isPaid,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePaymentReminder(reminderId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("payment_reminders").document(reminderId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- NIGHT GUARD INTERVENTION EVENTS ---

    fun observeNightGuardEvents(userId: String = requireUserId()): Flow<List<NightGuardEventRecord>> = flow {
        val path = "users/$userId/night_guard_events"
        emitAll(
            db.collection("users").document(userId).collection("night_guard_events")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(NightGuardEventRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.eventEpochMillis }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun recordNightGuardEvent(
        title: String,
        category: String,
        amount: Long,
        outcome: String,
        bypassReason: String = "",
        challengeCategory: String = "",
        eventEpochMillis: Long = System.currentTimeMillis()
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cleanOutcome = if (outcome in listOf("cancelled", "postponed", "proceeded", "essential_bypass")) {
            outcome
        } else {
            "cancelled"
        }
        val docRef = db.collection("users").document(uid).collection("night_guard_events").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(120).ifEmpty { "Night Spend" },
            "category" to category,
            "amount" to amount.coerceIn(1L, 10000000L),
            "outcome" to cleanOutcome,
            "bypassReason" to bypassReason.trim().take(120),
            "challengeCategory" to challengeCategory.trim().take(60),
            "eventEpochMillis" to eventEpochMillis.coerceAtLeast(1L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- INITIAL DATA SEEDING FOR FIRST-TIME USERS ---

    suspend fun seedInitialDataIfNeeded(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid).get().await()
        if (!userDoc.exists()) {
            upsertUserSettings(
                dailyLimit = 500L,
                lockdownActive = false,
                autoLockdownAt90 = true,
                stealthMode = false,
                themeId = "stark",
                myUpiId = "",
                upiGuardTone = "hinglish"
            ).getOrThrow()

            val now = System.currentTimeMillis()
            val hourMs = 3600_000L
            val dayMs = 86_400_000L

            createExpense("Masala Chai & Bun Maska", "Chai & Snacks", 68L, "UPI", "expense", "Tapri with team", now - hourMs).getOrThrow()
            createExpense("Metro Smart Card Top-up", "Transport", 50L, "UPI", "expense", "Daily commute", now - 2 * hourMs).getOrThrow()
            createExpense("Grilled Paneer Protein Bowl", "Food", 180L, "UPI", "expense", "Post-workout", now - dayMs - 2 * hourMs).getOrThrow()
            createExpense("HP Petrol Pump Refill", "Transport", 200L, "UPI", "expense", "", now - dayMs - 5 * hourMs).getOrThrow()
            createExpense("Oats, Milk & Eggs", "Grocery", 280L, "UPI", "expense", "Weekly essentials", now - 2 * dayMs).getOrThrow()
            createExpense("Turf Football Share", "Entertainment", 250L, "UPI", "expense", "Weekend 5v5", now - 3 * dayMs).getOrThrow()
            createExpense("Monthly Salary Credit", "Salary", 25000L, "UPI", "income", "Primary monthly payroll deposit", now - 4 * dayMs).getOrThrow()
            createExpense("UI Design Sprint Payout", "Freelance", 4500L, "UPI", "income", "Client milestone invoice", now - 2 * dayMs).getOrThrow()

            createKhataFriend("Rohan Verma", "919876543210", "rohan.verma@oksbi", 590L, "Midnight Biryani & Turf Split").getOrThrow()
            createKhataFriend("Kabir Malhotra", "919812345678", "kabir.m@okhdfcbank", 420L, "IMAX Movie Tickets").getOrThrow()
            createKhataFriend("Priya Nair", "919988776655", "priyanair@paytm", -180L, "Uber Airport Pool Share").getOrThrow()

            createSavingsGoal("Emergency Rainy-Day Fund", 25000L, 8500L).getOrThrow()

            createPaymentReminder(
                title = "Fiber Broadband & WiFi Bill",
                amount = 999L,
                category = "Bills",
                dueEpochMillis = now + 2 * dayMs,
                intervalType = "1_day_before",
                note = "Monthly high-speed broadband plan"
            ).getOrThrow()
        }
    }
}
