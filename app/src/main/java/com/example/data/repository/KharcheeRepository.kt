package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ExpenseRecord
import com.example.data.model.JarvisChatMessage
import com.example.data.model.KhataFriendRecord
import com.example.data.model.NightGuardEventRecord
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.PaymentVerificationRequestRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    /**
     * Creates or updates user profile settings WITHOUT touching privileged Pro membership fields
     * (`isPro`, `proTier`, `proExpiryMillis`, `proExpiresAtMillis`).
     * Existing legitimate Pro membership data on `/users/{uid}` is preserved across profile updates.
     */
    suspend fun upsertUserSettings(
        dailyLimit: Long = 500L,
        lockdownActive: Boolean = false,
        autoLockdownAt90: Boolean = true,
        stealthMode: Boolean = false,
        themeId: String = "stark",
        myUpiId: String = "",
        upiGuardTone: String = "hinglish",
        @Suppress("UNUSED_PARAMETER") isPro: Boolean = false,
        @Suppress("UNUSED_PARAMETER") proTier: String = "free",
        @Suppress("UNUSED_PARAMETER") proExpiryMillis: Long = 0L,
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

    /**
     * Submits an idempotent server-side payment verification request to `/users/{uid}/payment_verifications/{idempotencyKey}`.
     *
     * SECURITY INVARIANT:
     * - Never writes `isPro`, `proTier`, `proExpiryMillis`, or `proExpiresAtMillis` from the client.
     * - Only trusted Firebase Cloud Functions (using Admin SDK after verifying the payment provider's
     *   webhook/order signature, amount, currency, and idempotency lock) may grant or renew Pro access.
     */
    suspend fun submitPaymentVerificationRequest(
        tier: String,
        orderId: String = "",
        paymentId: String = "",
        utrNumber: String = "",
        deviceHash: String = "KHARCHEE"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val isLifetime = tier.equals("platinum", ignoreCase = true) ||
            tier.equals("sovereign", ignoreCase = true) ||
            tier.equals("sovereign_lifetime", ignoreCase = true)
        val resolvedPlanId = if (isLifetime) "sovereign_lifetime" else "starter_monthly"
        val resolvedTierKey = if (isLifetime) "platinum" else "gold"
        val resolvedAmountInr = if (isLifetime) 299L else 30L

        val cleanOrder = orderId.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(96)
            .ifEmpty { "ord_${resolvedPlanId}_${uid.take(12)}" }
        val cleanPayment = paymentId.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(96)
        val cleanUtr = utrNumber.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(48)
        val cleanDeviceHash = deviceHash.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "").take(16)
            .ifEmpty { "KHARCHEE" }

        val rawIdempotencySeed = when {
            cleanPayment.isNotEmpty() -> "pay_${cleanOrder}_${cleanPayment}"
            cleanUtr.isNotEmpty() -> "utr_${cleanOrder}_${cleanUtr}"
            else -> "req_${cleanOrder}_${cleanDeviceHash}"
        }
        val verificationId = sanitizeDocId(rawIdempotencySeed)
        val docRef = db.collection("users")
            .document(uid)
            .collection("payment_verifications")
            .document(verificationId)

        val payload = mapOf(
            "userId" to uid,
            "verificationId" to verificationId,
            "orderId" to cleanOrder,
            "paymentId" to cleanPayment,
            "utrNumber" to cleanUtr,
            "planId" to resolvedPlanId,
            "tierKey" to resolvedTierKey,
            "amountInr" to resolvedAmountInr,
            "currency" to "INR",
            "deviceHash" to cleanDeviceHash,
            "status" to "PENDING_SERVER_VERIFICATION",
            "idempotencyKey" to verificationId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            verificationId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    /**
     * Legacy entrypoint retained for binary/call-site compatibility:
     * Never self-grants Pro on `/users/{uid}`. Instead, queues a `PENDING_SERVER_VERIFICATION`
     * request for server-side Cloud Functions payment verification.
     */
    suspend fun updateProMembership(
        tier: String,
        @Suppress("UNUSED_PARAMETER") durationDays: Int,
        orderId: String = "",
        paymentId: String = "",
        utrNumber: String = "",
        deviceHash: String = "KHARCHEE"
    ): Result<Unit> = runCatching {
        submitPaymentVerificationRequest(
            tier = tier,
            orderId = orderId,
            paymentId = paymentId,
            utrNumber = utrNumber,
            deviceHash = deviceHash
        ).getOrThrow()
    }

    fun observePaymentVerifications(userId: String = requireUserId()): Flow<List<PaymentVerificationRequestRecord>> = flow {
        val path = "users/$userId/payment_verifications"
        emitAll(
            db.collection("users").document(userId).collection("payment_verifications")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PaymentVerificationRequestRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

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
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    /**
     * Paginated dashboard query limited to the top [limitCount] (default 20) recent items
     * executing strictly on Dispatchers.IO to prevent memory spikes on large ledgers.
     */
    fun observeDashboardRecentExpenses(
        userId: String = requireUserId(),
        limitCount: Int = 20
    ): Flow<List<ExpenseRecord>> = flow {
        val path = "users/$userId/expenses"
        emitAll(
            db.collection("users").document(userId).collection("expenses")
                .whereEqualTo("userId", userId)
                .limit(limitCount.coerceIn(20, 500).toLong())
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ExpenseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.txEpochMillis }
                        .take(limitCount.coerceAtLeast(20))
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

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
        val validCategories = setOf(
            "Food", "Chai & Snacks", "Transport", "Grocery", "Shopping",
            "Subscriptions", "Entertainment", "Bills", "Bills & Rent",
            "Salary", "Freelance", "Cashback", "Refund", "Investment", "Other"
        )
        val safeCategory = when {
            category in validCategories -> category
            type == "income" || category.contains("Income", ignoreCase = true) || category.contains("Balance", ignoreCase = true) -> "Salary"
            else -> "Other"
        }
        val safePaymentMode = if (paymentMode in setOf("UPI", "Cash", "Card")) paymentMode else "UPI"
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(120).ifEmpty { "Expense" },
            "category" to safeCategory,
            "amount" to amount.coerceIn(1L, 10000000L),
            "paymentMode" to safePaymentMode,
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
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
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

    // --- LIFETIME KHATA UNIVERSE (FIRESTORE CLOUD MEMORY SYNC) ---

    private fun sanitizeDocId(rawId: String): String {
        val cleaned = rawId.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifBlank { "khata_${System.currentTimeMillis()}" }.take(128)
    }

    suspend fun upsertKhataContactToCloud(contact: com.example.data.model.KhataContact): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val docId = sanitizeDocId(contact.id)
        val docRef = db.collection("users").document(uid).collection("khata_contacts").document(docId)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "contactId" to docId,
                    "name" to contact.name.trim().take(80).ifEmpty { "Contact" },
                    "phone" to contact.phone.trim().take(40),
                    "avatarInitials" to contact.avatarInitials.trim().take(8).ifEmpty { "KH" },
                    "notes" to contact.notes.trim().take(400),
                    "reminderEnabled" to contact.reminderEnabled,
                    "createdAtMillis" to contact.createdAtMillis.coerceAtLeast(1L),
                    "updatedAtMillis" to contact.updatedAtMillis.coerceAtLeast(1L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "name" to contact.name.trim().take(80).ifEmpty { "Contact" },
                    "phone" to contact.phone.trim().take(40),
                    "avatarInitials" to contact.avatarInitials.trim().take(8).ifEmpty { "KH" },
                    "notes" to contact.notes.trim().take(400),
                    "reminderEnabled" to contact.reminderEnabled,
                    "updatedAtMillis" to contact.updatedAtMillis.coerceAtLeast(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            com.example.data.model.AdvancedKhataStore.updateCloudSyncStatus("☁️ Lifetime Memory Synced")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun upsertKhataEntryToCloud(
        userId: String,
        entry: com.example.data.model.KhataLedgerEntry
    ): Result<Unit> = upsertKhataEntryToCloud(entry)

    suspend fun upsertKhataEntryToCloud(entry: com.example.data.model.KhataLedgerEntry): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val docId = sanitizeDocId(entry.id)
        val cleanContactId = sanitizeDocId(entry.contactId)
        val docRef = db.collection("users").document(uid).collection("khata_entries").document(docId)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "entryId" to docId,
                    "contactId" to cleanContactId,
                    "contactName" to entry.contactName.trim().take(80).ifEmpty { "Contact" },
                    "direction" to entry.direction.name,
                    "recordType" to entry.recordType.name,
                    "parentEntryId" to (entry.parentEntryId?.take(128) ?: ""),
                    "amount" to entry.amount.coerceIn(1L, 100000000L),
                    "txDateMillis" to entry.txDateMillis.coerceAtLeast(1L),
                    "dueDateMillis" to (entry.dueDateMillis ?: -1L),
                    "description" to entry.description.trim().take(300),
                    "category" to entry.category.trim().take(80).ifEmpty { "Personal Udhaar" },
                    "notes" to entry.notes.trim().take(500),
                    "deleted" to entry.isDeleted,
                    "archived" to entry.isArchived,
                    "edited" to entry.isEdited,
                    "previousAmountBeforeEdit" to (entry.previousAmountBeforeEdit ?: -1L),
                    "previousDescriptionBeforeEdit" to (entry.previousDescriptionBeforeEdit?.take(300) ?: ""),
                    "editCount" to entry.editCount.coerceAtLeast(0),
                    "proofAttachmentUri" to entry.proofAttachmentUri.trim().take(1000),
                    "proofAttachmentLabel" to entry.proofAttachmentLabel.trim().take(160),
                    "idempotencyKey" to entry.idempotencyKey.trim().take(180),
                    "createdAtMillis" to entry.createdAtMillis.coerceAtLeast(1L),
                    "updatedAtMillis" to entry.updatedAtMillis.coerceAtLeast(1L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "contactName" to entry.contactName.trim().take(80).ifEmpty { "Contact" },
                    "amount" to entry.amount.coerceIn(1L, 100000000L),
                    "dueDateMillis" to (entry.dueDateMillis ?: -1L),
                    "description" to entry.description.trim().take(300),
                    "category" to entry.category.trim().take(80).ifEmpty { "Personal Udhaar" },
                    "notes" to entry.notes.trim().take(500),
                    "deleted" to entry.isDeleted,
                    "archived" to entry.isArchived,
                    "edited" to entry.isEdited,
                    "previousAmountBeforeEdit" to (entry.previousAmountBeforeEdit ?: -1L),
                    "previousDescriptionBeforeEdit" to (entry.previousDescriptionBeforeEdit?.take(300) ?: ""),
                    "editCount" to entry.editCount.coerceAtLeast(0),
                    "proofAttachmentUri" to entry.proofAttachmentUri.trim().take(1000),
                    "proofAttachmentLabel" to entry.proofAttachmentLabel.trim().take(160),
                    "updatedAtMillis" to entry.updatedAtMillis.coerceAtLeast(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            com.example.data.model.AdvancedKhataStore.updateCloudSyncStatus("☁️ Lifetime Memory Synced")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun syncLifetimeKhataMemoryWithCloud(context: Context): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val userDoc = db.collection("users").document(uid)
        val contactsSnap = userDoc.collection("khata_contacts").whereEqualTo("userId", uid).get().await()
        val entriesSnap = userDoc.collection("khata_entries").whereEqualTo("userId", uid).get().await()

        val cloudContacts = contactsSnap.documents.mapNotNull { doc ->
            val cid = doc.getString("contactId") ?: doc.id
            val name = doc.getString("name")?.trim() ?: return@mapNotNull null
            if (name.isEmpty()) return@mapNotNull null
            com.example.data.model.KhataContact(
                id = cid,
                userId = uid,
                name = name,
                phone = doc.getString("phone") ?: "",
                avatarInitials = doc.getString("avatarInitials") ?: com.example.data.model.AdvancedKhataStore.computeInitials(name),
                notes = doc.getString("notes") ?: "",
                reminderEnabled = doc.getBoolean("reminderEnabled") ?: true,
                createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis(),
                updatedAtMillis = doc.getLong("updatedAtMillis") ?: System.currentTimeMillis()
            )
        }

        val cloudEntries = entriesSnap.documents.mapNotNull { doc ->
            val eid = doc.getString("entryId") ?: doc.id
            val cid = doc.getString("contactId") ?: return@mapNotNull null
            val cname = doc.getString("contactName") ?: "Contact"
            val dirStr = doc.getString("direction") ?: "LENA_HAI"
            val typeStr = doc.getString("recordType") ?: "ORIGINAL_ENTRY"
            val dueRaw = doc.getLong("dueDateMillis") ?: -1L
            val prevAmtRaw = doc.getLong("previousAmountBeforeEdit") ?: -1L
            val prevDescRaw = doc.getString("previousDescriptionBeforeEdit") ?: ""
            val parentRaw = doc.getString("parentEntryId") ?: ""
            com.example.data.model.KhataLedgerEntry(
                id = eid,
                userId = uid,
                contactId = cid,
                contactName = cname,
                direction = runCatching { com.example.data.model.KhataDirection.valueOf(dirStr) }
                    .getOrDefault(com.example.data.model.KhataDirection.LENA_HAI),
                recordType = runCatching { com.example.data.model.KhataRecordType.valueOf(typeStr) }
                    .getOrDefault(com.example.data.model.KhataRecordType.ORIGINAL_ENTRY),
                parentEntryId = parentRaw.ifBlank { null },
                amount = (doc.getLong("amount") ?: 0L).coerceAtLeast(1L),
                txDateMillis = doc.getLong("txDateMillis") ?: System.currentTimeMillis(),
                dueDateMillis = if (dueRaw > 0L) dueRaw else null,
                description = doc.getString("description") ?: "",
                category = doc.getString("category") ?: "Personal Udhaar",
                notes = doc.getString("notes") ?: "",
                isDeleted = doc.getBoolean("deleted") ?: false,
                isArchived = doc.getBoolean("archived") ?: false,
                isEdited = doc.getBoolean("edited") ?: false,
                previousAmountBeforeEdit = if (prevAmtRaw > 0L) prevAmtRaw else null,
                previousDescriptionBeforeEdit = prevDescRaw.ifBlank { null },
                editCount = (doc.getLong("editCount") ?: 0L).toInt(),
                proofAttachmentUri = doc.getString("proofAttachmentUri") ?: "",
                proofAttachmentLabel = doc.getString("proofAttachmentLabel") ?: "",
                idempotencyKey = doc.getString("idempotencyKey") ?: "",
                syncStatus = "SYNCED",
                createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis(),
                updatedAtMillis = doc.getLong("updatedAtMillis") ?: System.currentTimeMillis()
            )
        }

        val (contactsToUpload, entriesToUpload) = com.example.data.model.AdvancedKhataStore.mergeCloudSnapshot(
            context = context,
            userId = uid,
            cloudContacts = cloudContacts,
            cloudEntries = cloudEntries
        )

        contactsToUpload.forEach { c ->
            upsertKhataContactToCloud(c)
        }
        entriesToUpload.forEach { e ->
            upsertKhataEntryToCloud(e)
        }
        com.example.data.model.AdvancedKhataStore.updateCloudSyncStatus("☁️ Lifetime Memory Synced")
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
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
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
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
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
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
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
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
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

    // --- INITIAL WORKSPACE FOR FIRST-TIME USERS (CLEAN WORKSPACE, NO FAKE TRANSACTIONS) ---

    suspend fun seedInitialDataIfNeeded(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid).get().await()
        if (!userDoc.exists()) {
            // New account starts with a completely clean financial workspace:
            // Zero demo transactions, zero fake income/expenses, zero sample balances,
            // and notifications off until explicit user consent during onboarding.
            upsertUserSettings(
                dailyLimit = 500L,
                lockdownActive = false,
                autoLockdownAt90 = true,
                stealthMode = false,
                themeId = "stark",
                myUpiId = "",
                upiGuardTone = "hinglish",
                notifyPaymentReminders = false,
                notifyNightGuardAlert = false,
                notifyMorningReview = false
            ).getOrThrow()
        }
    }

    suspend fun deleteAllUserData(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)
        val subcollections = listOf(
            "expenses",
            "khata_friends",
            "savings_goals",
            "jarvis_messages",
            "payment_reminders",
            "night_guard_events"
        )
        for (sub in subcollections) {
            try {
                val snap = userDoc.collection(sub).whereEqualTo("userId", uid).get().await()
                for (doc in snap.documents) {
                    doc.reference.delete().await()
                }
            } catch (_: Exception) {
            }
        }
        try {
            userDoc.delete().await()
        } catch (_: Exception) {
        }
    }
}

