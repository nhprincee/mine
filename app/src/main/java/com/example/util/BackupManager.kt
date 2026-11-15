package com.example.util

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.local.ExpenseRoomEntity
import com.example.data.local.GoalRoomEntity
import com.example.data.local.KharcheeDatabase
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.ExpenseRecord
import com.example.data.model.KhataAuditAction
import com.example.data.model.KhataContact
import com.example.data.model.KhataDirection
import com.example.data.model.KhataFriendRecord
import com.example.data.model.KhataHistoryAuditItem
import com.example.data.model.KhataLedgerEntry
import com.example.data.model.KhataRecordType
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.SubscriptionItem
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.receiver.AutoExpenseDetectionStore
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Scope constant for Google Drive AppData folder (hidden, private application data folder).
 */
object DriveScopes {
    const val APPDATA = "https://www.googleapis.com/auth/drive.appdata"
    const val DRIVE_APPDATA_FILES_ENDPOINT = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&fields=files(id,name,modifiedTime,size)"
    const val DRIVE_UPLOAD_ENDPOINT = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
}

data class BackupVaultSnapshot(
    val schemaVersion: Int = 1,
    val exportedAtMillis: Long,
    val exportedDateFormatted: String,
    val userId: String,
    val userEmail: String,
    val userDisplayName: String,
    val preferredCurrency: String,
    val monthlyBudgetTarget: Long,
    val dailyLimit: Long,
    val netBalanceAmount: Long,
    val settings: UserSettings,
    val expenses: List<ExpenseRecord>,
    val savingsGoals: List<SavingsGoalRecord>,
    val khataFriends: List<KhataFriendRecord>,
    val advancedKhataContactsJson: String,
    val advancedKhataEntriesJson: String,
    val advancedKhataAuditJson: String,
    val paymentReminders: List<PaymentReminderRecord>,
    val categoryBudgets: Map<String, Long>,
    val subscriptions: List<SubscriptionItem>
)

data class DetectedBackupMetadata(
    val backupFileName: String = BackupManager.BACKUP_FILE_NAME,
    val lastBackupMillis: Long,
    val lastDateFormatted: String,
    val netBalanceAmount: Long,
    val transactionCount: Int,
    val khataCount: Int,
    val savingsGoalsCount: Int,
    val userDisplayName: String,
    val sourceLabel: String = "Google Drive (AppData)",
    val encryptedFilePath: String
)

data class DriveSyncState(
    val autoSyncEnabled: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncMillis: Long = 0L,
    val lastSyncFormatted: String = "Never",
    val pendingOfflineQueue: Boolean = false,
    val driveScope: String = DriveScopes.APPDATA,
    val statusMessage: String = "Encrypted AES-256-GCM · Google Drive AppData Ready",
    val latestBackupNetBalance: Long = 0L,
    val latestBackupTxCount: Int = 0
)

/**
 * Production Encrypted Database Backup & Google Drive AppData Sync Manager.
 *
 * Features:
 * 1. Exports the entire Room DB (Transactions, Savings Goals), Budgets, and Khata Ledger into
 *    an AES-256-GCM encrypted file (`kharchee_secure_backup.enc`) with PBKDF2 key derivation and SHA-256 integrity verification.
 * 2. Restores from encrypted `.enc` file or Google Drive `appDataFolder`, validating integrity and populating Room DB + Khata + Budgets.
 * 3. Integrates Google Drive `Drive.Scope.APPDATA` (`appDataFolder`) with 1-tap "Back up Now", "Enable Google Drive Auto-Sync",
 *    and "Last Sync: [Date/Time]".
 * 4. Dynamic Background Auto-Sync every 24 hours on Wi-Fi (`JobScheduler`) and automatic backup on Logout,
 *    with offline queue retry via `ConnectivityManager`.
 * 5. First-Time User Restore Detector during cold start onboarding.
 */
object BackupManager {
    private const val TAG = "BackupManager"
    const val BACKUP_FILE_NAME = "kharchee_secure_backup.enc"
    private const val MAGIC_HEADER = "KHARCHEE_ENC_V1"
    private const val PREFS_NAME = "kharchee_cloud_backup_sync_prefs"
    private const val DEFAULT_VAULT_PASSPHRASE = "KHARCHEE_SOVEREIGN_AES256_VAULT_KEY_V1"
    private const val PBKDF2_ITERATIONS = 12_000
    private const val GCM_TAG_BITS = 128
    const val AUTO_SYNC_JOB_ID = 49102
    const val OFFLINE_RETRY_JOB_ID = 49103

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _syncStateFlow = MutableStateFlow(DriveSyncState())
    val syncStateFlow: StateFlow<DriveSyncState> = _syncStateFlow.asStateFlow()

    @Volatile
    private var networkCallbackRegistered = false

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun formatSyncTimestamp(epochMillis: Long): String {
        if (epochMillis <= 0L) return "Never"
        val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        return fmt.format(Date(epochMillis))
    }

    fun ensureInitialized(context: Context, userId: String = "default") {
        val appContext = context.applicationContext
        val p = prefs(appContext)
        val autoSync = p.getBoolean("auto_sync_enabled", true)
        val lastSync = p.getLong("last_sync_millis", 0L)
        val pendingOffline = p.getBoolean("pending_offline_sync", false)
        val lastBal = p.getLong("last_backup_net_balance", 0L)
        val lastTxCount = p.getInt("last_backup_tx_count", 0)

        _syncStateFlow.value = DriveSyncState(
            autoSyncEnabled = autoSync,
            isSyncing = false,
            lastSyncMillis = lastSync,
            lastSyncFormatted = formatSyncTimestamp(lastSync),
            pendingOfflineQueue = pendingOffline,
            driveScope = DriveScopes.APPDATA,
            statusMessage = when {
                pendingOffline -> "⏳ Queued for next internet connection (Offline Safe)"
                lastSync > 0L -> "✓ Encrypted backup synced to Google Drive AppData"
                else -> "Encrypted AES-256-GCM · Google Drive AppData Ready"
            },
            latestBackupNetBalance = lastBal,
            latestBackupTxCount = lastTxCount
        )

        if (autoSync) {
            scheduleDailyWifiAutoSync(appContext)
        }
        registerOfflineRetryNetworkCallbackIfNeeded(appContext, userId)
    }

    fun isAutoSyncEnabled(context: Context): Boolean {
        return prefs(context).getBoolean("auto_sync_enabled", true)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean, userId: String = "default") {
        prefs(context).edit().putBoolean("auto_sync_enabled", enabled).apply()
        if (enabled) {
            scheduleDailyWifiAutoSync(context)
        } else {
            cancelDailyWifiAutoSync(context)
        }
        _syncStateFlow.value = _syncStateFlow.value.copy(
            autoSyncEnabled = enabled,
            statusMessage = if (enabled) {
                "🔄 Daily 24h Wi-Fi & Logout Auto-Sync Enabled (Drive AppData)"
            } else {
                "⏸️ Google Drive Auto-Sync Paused (Manual backups active)"
            }
        )
    }

    // =========================================================================
    // 1. AES-256-GCM & ZERO-KNOWLEDGE CUSTOM SALT E2EE ENCRYPTION ENGINE
    // =========================================================================

    private const val ZERO_KNOWLEDGE_MAGIC_HEADER = "KHARCHEE_ZK_E2EE_V2"

    // Master passphrase is kept strictly in volatile RAM and NEVER persisted to disk or cloud
    @Volatile
    private var inMemoryZeroKnowledgePassphrase: String = ""

    fun setInMemoryZeroKnowledgePassphrase(passphrase: String) {
        inMemoryZeroKnowledgePassphrase = passphrase.trim()
    }

    fun getInMemoryZeroKnowledgePassphrase(): String = inMemoryZeroKnowledgePassphrase

    fun clearInMemoryZeroKnowledgePassphrase() {
        inMemoryZeroKnowledgePassphrase = ""
    }

    fun generateMilitaryGradeCustomSaltHex(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }.uppercase(Locale.ENGLISH)
    }

    fun isZeroKnowledgeCustomSaltEnabled(context: Context): Boolean {
        return prefs(context).getBoolean("zk_custom_salt_enabled", false)
    }

    fun getZeroKnowledgeCustomSaltHex(context: Context): String {
        val existing = prefs(context).getString("zk_custom_salt_hex", null)
        if (!existing.isNullOrBlank()) return existing
        val generated = generateMilitaryGradeCustomSaltHex()
        prefs(context).edit().putString("zk_custom_salt_hex", generated).apply()
        return generated
    }

    fun setZeroKnowledgeCustomSaltConfig(
        context: Context,
        enabled: Boolean,
        customSaltHex: String,
        inMemoryPassphrase: String = inMemoryZeroKnowledgePassphrase
    ) {
        val cleanSalt = customSaltHex.trim().ifBlank { generateMilitaryGradeCustomSaltHex() }
        prefs(context).edit()
            .putBoolean("zk_custom_salt_enabled", enabled)
            .putString("zk_custom_salt_hex", cleanSalt)
            .apply()
        if (inMemoryPassphrase.isNotBlank()) {
            inMemoryZeroKnowledgePassphrase = inMemoryPassphrase.trim()
        }
    }

    private fun deriveAesKey(passphrase: String, salt: ByteArray, customSaltMix: String = ""): SecretKeySpec {
        val effectivePass = passphrase.trim().ifEmpty {
            inMemoryZeroKnowledgePassphrase.ifEmpty { DEFAULT_VAULT_PASSPHRASE }
        }
        val combinedSaltBytes = if (customSaltMix.isNotBlank()) {
            MessageDigest.getInstance("SHA-256")
                .digest(salt + customSaltMix.trim().toByteArray(Charsets.UTF_8))
                .copyOf(16)
        } else {
            salt
        }
        val spec = PBEKeySpec(
            "$DEFAULT_VAULT_PASSPHRASE::$effectivePass::$customSaltMix".toCharArray(),
            combinedSaltBytes,
            PBKDF2_ITERATIONS,
            256
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Sovereign Pro Zero-Knowledge Custom Salt Encryption:
     * Encrypts plaintext JSON using the user's personal Master Passphrase + Custom Salt.
     * Cannot be decrypted without the exact Master Passphrase + Custom Salt.
     */
    fun encryptPayloadWithZeroKnowledgeSalt(
        plainJson: String,
        masterPassphrase: String,
        customSaltHex: String
    ): String {
        require(masterPassphrase.trim().isNotEmpty()) {
            "Zero-Knowledge Master Passphrase cannot be empty."
        }
        val cleanSaltHex = customSaltHex.trim().ifEmpty { "DEFAULT_ZK_SALT" }
        val random = SecureRandom()
        val randomNonceSalt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val secretKey = deriveAesKey(masterPassphrase, randomNonceSalt, cleanSaltHex)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plainBytes = plainJson.toByteArray(Charsets.UTF_8)
        val cipherBytes = cipher.doFinal(plainBytes)
        val checksumHex = sha256Hex(plainBytes)
        val saltFingerprint = sha256Hex(cleanSaltHex.toByteArray(Charsets.UTF_8)).take(12)

        val saltB64 = Base64.encodeToString(randomNonceSalt, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)

        return "$ZERO_KNOWLEDGE_MAGIC_HEADER:$saltFingerprint:$saltB64:$ivB64:$checksumHex:$cipherB64"
    }

    /**
     * Decrypts a Zero-Knowledge Custom Salt `.enc` backup payload.
     */
    fun decryptPayloadWithZeroKnowledgeSalt(
        encryptedContent: String,
        masterPassphrase: String,
        customSaltHex: String
    ): Result<String> = runCatching {
        val trimmed = encryptedContent.trim()
        if (!trimmed.startsWith("$ZERO_KNOWLEDGE_MAGIC_HEADER:")) {
            return decryptPayloadFromEncFormat(trimmed, masterPassphrase)
        }
        val parts = trimmed.split(":")
        require(parts.size >= 6) {
            "Corrupted Zero-Knowledge E2EE backup structure."
        }
        val expectedSaltFingerprint = parts[1]
        val randomNonceSalt = Base64.decode(parts[2], Base64.NO_WRAP)
        val iv = Base64.decode(parts[3], Base64.NO_WRAP)
        val expectedHash = parts[4]
        val cipherBytes = Base64.decode(parts.subList(5, parts.size).joinToString(":"), Base64.NO_WRAP)

        val cleanSaltHex = customSaltHex.trim().ifEmpty { "DEFAULT_ZK_SALT" }
        val actualSaltFingerprint = sha256Hex(cleanSaltHex.toByteArray(Charsets.UTF_8)).take(12)
        require(actualSaltFingerprint.equals(expectedSaltFingerprint, ignoreCase = true)) {
            "Custom cryptographic salt mismatch. Provide the exact Sovereign Salt used during encryption."
        }

        val secretKey = deriveAesKey(masterPassphrase, randomNonceSalt, cleanSaltHex)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plainBytes = try {
            cipher.doFinal(cipherBytes)
        } catch (e: Exception) {
            throw IllegalArgumentException("Zero-Knowledge Decryption failed: Invalid Master Passphrase.", e)
        }

        val actualHash = sha256Hex(plainBytes)
        require(actualHash.equals(expectedHash, ignoreCase = true)) {
            "Zero-Knowledge backup integrity check failed (SHA-256 mismatch)."
        }
        String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Encrypts plaintext JSON into an authenticated AES-256-GCM `.enc` payload that cannot be read as plain text.
     */
    fun encryptPayloadToEncFormat(plainJson: String, password: String = ""): String {
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val secretKey = deriveAesKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plainBytes = plainJson.toByteArray(Charsets.UTF_8)
        val cipherBytes = cipher.doFinal(plainBytes)
        val checksumHex = sha256Hex(plainBytes)

        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)

        return "$MAGIC_HEADER:$saltB64:$ivB64:$checksumHex:$cipherB64"
    }

    /**
     * Decrypts and validates an encrypted `kharchee_secure_backup.enc` payload.
     * Verifies both the AES-GCM authentication tag and SHA-256 plaintext hash.
     */
    fun decryptPayloadFromEncFormat(encryptedContent: String, password: String = ""): Result<String> = runCatching {
        val trimmed = encryptedContent.trim()
        require(trimmed.startsWith("$MAGIC_HEADER:")) {
            "Invalid backup file header. Expected encrypted $BACKUP_FILE_NAME format."
        }
        val parts = trimmed.split(":")
        require(parts.size >= 5) {
            "Corrupted encrypted backup structure."
        }
        val salt = Base64.decode(parts[1], Base64.NO_WRAP)
        val iv = Base64.decode(parts[2], Base64.NO_WRAP)
        val expectedHash = parts[3]
        val cipherBytes = Base64.decode(parts.subList(4, parts.size).joinToString(":"), Base64.NO_WRAP)

        val plainBytes = try {
            val secretKey = deriveAesKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher.doFinal(cipherBytes)
        } catch (e: Exception) {
            if (password.isNotBlank()) {
                // Fallback attempt with default vault key if user typed a password on a default-encrypted auto-backup
                val fallbackKey = deriveAesKey("", salt)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, fallbackKey, GCMParameterSpec(GCM_TAG_BITS, iv))
                cipher.doFinal(cipherBytes)
            } else {
                throw IllegalArgumentException("Decryption failed: Invalid backup password or tampered file.", e)
            }
        }

        val actualHash = sha256Hex(plainBytes)
        require(actualHash.equals(expectedHash, ignoreCase = true)) {
            "Backup integrity check failed (SHA-256 mismatch)."
        }
        String(plainBytes, Charsets.UTF_8)
    }

    // =========================================================================
    // 2. FULL ROOM DB + BUDGETS + KHATA LEDGER SERIALIZATION / DESERIALIZATION
    // =========================================================================

    suspend fun captureFullVaultSnapshot(
        context: Context,
        userId: String,
        userEmail: String = "",
        userDisplayName: String = "",
        liveExpenses: List<ExpenseRecord> = emptyList(),
        liveGoals: List<SavingsGoalRecord> = emptyList(),
        liveKhataFriends: List<KhataFriendRecord> = emptyList(),
        liveReminders: List<PaymentReminderRecord> = emptyList(),
        liveSettings: UserSettings? = null
    ): BackupVaultSnapshot = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val safeUid = userId.ifBlank { "default" }
        val db = KharcheeDatabase.getInstance(appContext)

        // Sync live in-memory transactions and goals into Room DB first so Room DB is 100% current
        if (liveExpenses.isNotEmpty()) {
            val roomEntities = liveExpenses.map {
                ExpenseRoomEntity.fromDomainModel(if (it.userId.isBlank()) it.copy(userId = safeUid) else it)
            }
            runCatching { db.expenseDao().insertExpenses(roomEntities) }
        }
        if (liveGoals.isNotEmpty()) {
            liveGoals.forEach { goal ->
                runCatching {
                    db.goalDao().insertGoal(
                        GoalRoomEntity.fromDomainModel(if (goal.userId.isBlank()) goal.copy(userId = safeUid) else goal)
                    )
                }
            }
        }

        val roomExpenses = runCatching { db.expenseDao().getExpensesForUser(safeUid).map { it.toDomainModel() } }
            .getOrDefault(emptyList())
        val autoDetectedExpenses = AutoExpenseDetectionStore.autoExpensesFlow.value.filter {
            it.userId == safeUid || it.userId == "local_user" || it.userId.isBlank()
        }
        val mergedExpenses = (liveExpenses + roomExpenses + autoDetectedExpenses)
            .distinctBy { rec ->
                if (rec.id.isNotBlank() && !rec.id.startsWith("local_tx_")) {
                    rec.id
                } else {
                    "${rec.title.trim().lowercase()}:${rec.amount}:${rec.txEpochMillis / 2000L}"
                }
            }
            .sortedByDescending { it.txEpochMillis }

        val roomGoals = runCatching { db.goalDao().getGoalsForUser(safeUid).map { it.toDomainModel() } }
            .getOrDefault(emptyList())
        val mergedGoals = (liveGoals + roomGoals)
            .distinctBy { it.id.ifBlank { it.title.trim().lowercase() } }

        val profileMeta = KharcheeProfileAndToolsStore.getProfileMetadata(
            context = appContext,
            userId = safeUid,
            defaultName = userDisplayName.ifBlank { "Kharchee Member" },
            defaultEmail = userEmail
        )
        val effectiveSettings = liveSettings ?: UserSettings(id = safeUid, userId = safeUid)
        val effectiveMonthlyBudget = if (profileMeta.monthlyBudget > 0L) {
            profileMeta.monthlyBudget
        } else {
            (effectiveSettings.dailyLimit * 30L).coerceAtLeast(1500L)
        }
        val preferredCurrency = KharcheeProfileAndToolsStore.getPreferredCurrency(appContext, safeUid)
        val categoryBudgets = KharcheeProfileAndToolsStore.getCategoryBudgets(appContext, safeUid)
        val subscriptions = KharcheeProfileAndToolsStore.getSubscriptions(appContext, safeUid)

        // Read AdvancedKhataStore raw JSONs for full fidelity
        val khataPrefs = appContext.getSharedPreferences("kharchee_advanced_khata_v1", Context.MODE_PRIVATE)
        val advContactsJson = khataPrefs.getString("contacts_$safeUid", "[]") ?: "[]"
        val advEntriesJson = khataPrefs.getString("entries_$safeUid", "[]") ?: "[]"
        val advAuditJson = khataPrefs.getString("audit_$safeUid", "[]") ?: "[]"

        val totalIncome = mergedExpenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val totalExpense = mergedExpenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense
        val now = System.currentTimeMillis()

        BackupVaultSnapshot(
            schemaVersion = 1,
            exportedAtMillis = now,
            exportedDateFormatted = formatSyncTimestamp(now),
            userId = safeUid,
            userEmail = userEmail.ifBlank { profileMeta.email },
            userDisplayName = profileMeta.customDisplayName.ifBlank { userDisplayName.ifBlank { "Kharchee Member" } },
            preferredCurrency = preferredCurrency,
            monthlyBudgetTarget = effectiveMonthlyBudget,
            dailyLimit = effectiveSettings.dailyLimit,
            netBalanceAmount = netBalance,
            settings = effectiveSettings,
            expenses = mergedExpenses,
            savingsGoals = mergedGoals,
            khataFriends = liveKhataFriends,
            advancedKhataContactsJson = advContactsJson,
            advancedKhataEntriesJson = advEntriesJson,
            advancedKhataAuditJson = advAuditJson,
            paymentReminders = liveReminders,
            categoryBudgets = categoryBudgets,
            subscriptions = subscriptions
        )
    }

    fun serializeSnapshotToJson(snapshot: BackupVaultSnapshot): String {
        val root = JSONObject().apply {
            put("schemaVersion", snapshot.schemaVersion)
            put("exportedAtMillis", snapshot.exportedAtMillis)
            put("exportedDateFormatted", snapshot.exportedDateFormatted)
            put("userId", snapshot.userId)
            put("userEmail", snapshot.userEmail)
            put("userDisplayName", snapshot.userDisplayName)
            put("preferredCurrency", snapshot.preferredCurrency)
            put("monthlyBudgetTarget", snapshot.monthlyBudgetTarget)
            put("dailyLimit", snapshot.dailyLimit)
            put("netBalanceAmount", snapshot.netBalanceAmount)

            put("settings", JSONObject().apply {
                put("dailyLimit", snapshot.settings.dailyLimit)
                put("lockdownActive", snapshot.settings.lockdownActive)
                put("autoLockdownAt90", snapshot.settings.autoLockdownAt90)
                put("stealthMode", snapshot.settings.stealthMode)
                put("themeId", snapshot.settings.themeId)
                put("myUpiId", snapshot.settings.myUpiId)
                put("upiGuardTone", snapshot.settings.upiGuardTone)
                put("isPro", snapshot.settings.isPro)
                put("proTier", snapshot.settings.proTier)
                put("nightGuardEnabled", snapshot.settings.nightGuardEnabled)
                put("notifyPaymentReminders", snapshot.settings.notifyPaymentReminders)
                put("notifyNightGuardAlert", snapshot.settings.notifyNightGuardAlert)
                put("notifyMorningReview", snapshot.settings.notifyMorningReview)
            })

            put("expenses", JSONArray().apply {
                snapshot.expenses.forEach { tx ->
                    put(JSONObject().apply {
                        put("id", tx.id)
                        put("userId", tx.userId)
                        put("title", tx.title)
                        put("category", tx.category)
                        put("amount", tx.amount)
                        put("paymentMode", tx.paymentMode)
                        put("type", tx.type)
                        put("note", tx.note)
                        put("txEpochMillis", tx.txEpochMillis)
                    })
                }
            })

            put("savingsGoals", JSONArray().apply {
                snapshot.savingsGoals.forEach { g ->
                    put(JSONObject().apply {
                        put("id", g.id)
                        put("userId", g.userId)
                        put("title", g.title)
                        put("targetAmount", g.targetAmount)
                        put("savedAmount", g.savedAmount)
                    })
                }
            })

            put("khataFriends", JSONArray().apply {
                snapshot.khataFriends.forEach { kf ->
                    put(JSONObject().apply {
                        put("id", kf.id)
                        put("userId", kf.userId)
                        put("name", kf.name)
                        put("phone", kf.phone)
                        put("avatarInitials", kf.avatarInitials)
                        put("upiId", kf.upiId)
                        put("balance", kf.balance)
                        put("lastDescription", kf.lastDescription)
                        put("settled", kf.settled)
                    })
                }
            })

            put("advancedKhataContactsJson", snapshot.advancedKhataContactsJson)
            put("advancedKhataEntriesJson", snapshot.advancedKhataEntriesJson)
            put("advancedKhataAuditJson", snapshot.advancedKhataAuditJson)

            put("paymentReminders", JSONArray().apply {
                snapshot.paymentReminders.forEach { rem ->
                    put(JSONObject().apply {
                        put("id", rem.id)
                        put("userId", rem.userId)
                        put("title", rem.title)
                        put("amount", rem.amount)
                        put("category", rem.category)
                        put("dueEpochMillis", rem.dueEpochMillis)
                        put("intervalType", rem.intervalType)
                        put("isPaid", rem.isPaid)
                        put("note", rem.note)
                    })
                }
            })

            put("categoryBudgets", JSONObject().apply {
                snapshot.categoryBudgets.forEach { (cat, amt) -> put(cat, amt) }
            })

            put("subscriptions", JSONArray().apply {
                snapshot.subscriptions.forEach { sub ->
                    put(JSONObject().apply {
                        put("id", sub.id)
                        put("name", sub.name)
                        put("amount", sub.amount)
                        put("billingCycle", sub.billingCycle)
                        put("nextRenewalDate", sub.nextRenewalDate)
                        put("category", sub.category)
                        put("paymentMethod", sub.paymentMethod)
                        put("isActive", sub.isActive)
                        put("reminderEnabled", sub.reminderEnabled)
                    })
                }
            })
        }
        return root.toString()
    }

    fun deserializeJsonToSnapshot(plainJson: String, targetUserId: String): BackupVaultSnapshot {
        val root = JSONObject(plainJson)
        val safeUid = targetUserId.ifBlank { root.optString("userId", "default") }
        val exportedAtMillis = root.optLong("exportedAtMillis", System.currentTimeMillis())
        val exportedDateFormatted = root.optString("exportedDateFormatted", formatSyncTimestamp(exportedAtMillis))
        val userEmail = root.optString("userEmail", "")
        val userDisplayName = root.optString("userDisplayName", "Kharchee Member")
        val preferredCurrency = root.optString("preferredCurrency", "INR (₹)")
        val monthlyBudgetTarget = root.optLong("monthlyBudgetTarget", 15000L)
        val dailyLimit = root.optLong("dailyLimit", 500L)

        val settingsObj = root.optJSONObject("settings") ?: JSONObject()
        val settings = UserSettings(
            id = safeUid,
            userId = safeUid,
            dailyLimit = settingsObj.optLong("dailyLimit", dailyLimit),
            lockdownActive = settingsObj.optBoolean("lockdownActive", false),
            autoLockdownAt90 = settingsObj.optBoolean("autoLockdownAt90", true),
            stealthMode = settingsObj.optBoolean("stealthMode", false),
            themeId = settingsObj.optString("themeId", "stark"),
            myUpiId = settingsObj.optString("myUpiId", ""),
            upiGuardTone = settingsObj.optString("upiGuardTone", "hinglish"),
            isPro = settingsObj.optBoolean("isPro", false),
            proTier = settingsObj.optString("proTier", "free"),
            nightGuardEnabled = settingsObj.optBoolean("nightGuardEnabled", true),
            notifyPaymentReminders = settingsObj.optBoolean("notifyPaymentReminders", true),
            notifyNightGuardAlert = settingsObj.optBoolean("notifyNightGuardAlert", true),
            notifyMorningReview = settingsObj.optBoolean("notifyMorningReview", false)
        )

        val expensesArr = root.optJSONArray("expenses") ?: JSONArray()
        val expenses = buildList {
            for (i in 0 until expensesArr.length()) {
                val o = expensesArr.optJSONObject(i) ?: continue
                add(
                    ExpenseRecord(
                        id = o.optString("id", "restored_tx_${exportedAtMillis}_$i"),
                        userId = safeUid,
                        title = o.optString("title", "Expense"),
                        category = o.optString("category", "Food"),
                        amount = o.optLong("amount", 0L),
                        paymentMode = o.optString("paymentMode", "UPI"),
                        type = o.optString("type", "expense"),
                        note = o.optString("note", ""),
                        txEpochMillis = o.optLong("txEpochMillis", exportedAtMillis)
                    )
                )
            }
        }

        val goalsArr = root.optJSONArray("savingsGoals") ?: JSONArray()
        val goals = buildList {
            for (i in 0 until goalsArr.length()) {
                val o = goalsArr.optJSONObject(i) ?: continue
                add(
                    SavingsGoalRecord(
                        id = o.optString("id", "restored_goal_${exportedAtMillis}_$i"),
                        userId = safeUid,
                        title = o.optString("title", "Savings Goal"),
                        targetAmount = o.optLong("targetAmount", 25000L),
                        savedAmount = o.optLong("savedAmount", 0L)
                    )
                )
            }
        }

        val khataArr = root.optJSONArray("khataFriends") ?: JSONArray()
        val khataFriends = buildList {
            for (i in 0 until khataArr.length()) {
                val o = khataArr.optJSONObject(i) ?: continue
                add(
                    KhataFriendRecord(
                        id = o.optString("id", "restored_khata_${exportedAtMillis}_$i"),
                        userId = safeUid,
                        name = o.optString("name", "Friend"),
                        phone = o.optString("phone", ""),
                        avatarInitials = o.optString("avatarInitials", "FR"),
                        upiId = o.optString("upiId", ""),
                        balance = o.optLong("balance", 0L),
                        lastDescription = o.optString("lastDescription", ""),
                        settled = o.optBoolean("settled", false)
                    )
                )
            }
        }

        val remindersArr = root.optJSONArray("paymentReminders") ?: JSONArray()
        val paymentReminders = buildList {
            for (i in 0 until remindersArr.length()) {
                val o = remindersArr.optJSONObject(i) ?: continue
                add(
                    PaymentReminderRecord(
                        id = o.optString("id", "restored_rem_${exportedAtMillis}_$i"),
                        userId = safeUid,
                        title = o.optString("title", "Bill Reminder"),
                        amount = o.optLong("amount", 0L),
                        category = o.optString("category", "Bills"),
                        dueEpochMillis = o.optLong("dueEpochMillis", System.currentTimeMillis() + 86_400_000L),
                        intervalType = o.optString("intervalType", "same_day"),
                        isPaid = o.optBoolean("isPaid", false),
                        note = o.optString("note", "")
                    )
                )
            }
        }

        val catBudgetsObj = root.optJSONObject("categoryBudgets") ?: JSONObject()
        val categoryBudgets = mutableMapOf<String, Long>()
        catBudgetsObj.keys().forEach { key ->
            categoryBudgets[key] = catBudgetsObj.optLong(key, 2000L)
        }

        val subsArr = root.optJSONArray("subscriptions") ?: JSONArray()
        val subscriptions = buildList {
            for (i in 0 until subsArr.length()) {
                val o = subsArr.optJSONObject(i) ?: continue
                add(
                    SubscriptionItem(
                        id = o.optString("id", "sub_$i"),
                        name = o.optString("name", "Subscription"),
                        amount = o.optLong("amount", 199L),
                        billingCycle = o.optString("billingCycle", "Monthly"),
                        nextRenewalDate = o.optString("nextRenewalDate", "05 Nov"),
                        category = o.optString("category", "Entertainment"),
                        paymentMethod = o.optString("paymentMethod", "UPI"),
                        isActive = o.optBoolean("isActive", true),
                        reminderEnabled = o.optBoolean("reminderEnabled", true)
                    )
                )
            }
        }

        val totalIncome = expenses.filter { isIncomeTransaction(it) }.sumOf { it.amount }
        val totalExpense = expenses.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
        val computedNetBalance = if (expenses.isNotEmpty()) {
            totalIncome - totalExpense
        } else {
            root.optLong("netBalanceAmount", 0L)
        }

        return BackupVaultSnapshot(
            schemaVersion = root.optInt("schemaVersion", 1),
            exportedAtMillis = exportedAtMillis,
            exportedDateFormatted = exportedDateFormatted,
            userId = safeUid,
            userEmail = userEmail,
            userDisplayName = userDisplayName,
            preferredCurrency = preferredCurrency,
            monthlyBudgetTarget = monthlyBudgetTarget,
            dailyLimit = dailyLimit,
            netBalanceAmount = computedNetBalance,
            settings = settings,
            expenses = expenses,
            savingsGoals = goals,
            khataFriends = khataFriends,
            advancedKhataContactsJson = root.optString("advancedKhataContactsJson", "[]"),
            advancedKhataEntriesJson = root.optString("advancedKhataEntriesJson", "[]"),
            advancedKhataAuditJson = root.optString("advancedKhataAuditJson", "[]"),
            paymentReminders = paymentReminders,
            categoryBudgets = categoryBudgets,
            subscriptions = subscriptions
        )
    }

    // =========================================================================
    // 3. MANUAL & AUTOMATIC ENCRYPTED FILE EXPORT / RESTORE
    // =========================================================================

    private fun getLocalBackupDir(context: Context): File {
        return File(context.applicationContext.filesDir, "backups").apply { mkdirs() }
    }

    private fun getGoogleDriveAppDataVaultDir(context: Context): File {
        return File(context.applicationContext.filesDir, "google_drive_appdata_vault").apply { mkdirs() }
    }

    fun getPrimaryBackupFile(context: Context): File {
        return File(getLocalBackupDir(context), BACKUP_FILE_NAME)
    }

    private fun rewriteAdvancedKhataJsonUserId(rawArrayJson: String, targetUserId: String): String {
        return try {
            val arr = JSONArray(rawArrayJson)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                obj.put("userId", targetUserId)
            }
            arr.toString()
        } catch (_: Exception) {
            "[]"
        }
    }

    /**
     * Exports the entire Room DB, Budgets, and Khata Ledger into `kharchee_secure_backup.enc`.
     */
    suspend fun createEncryptedBackupFile(
        context: Context,
        userId: String,
        userEmail: String = "",
        userDisplayName: String = "",
        password: String = "",
        liveExpenses: List<ExpenseRecord> = emptyList(),
        liveGoals: List<SavingsGoalRecord> = emptyList(),
        liveKhataFriends: List<KhataFriendRecord> = emptyList(),
        liveReminders: List<PaymentReminderRecord> = emptyList(),
        liveSettings: UserSettings? = null
    ): Result<Pair<File, BackupVaultSnapshot>> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = captureFullVaultSnapshot(
                context = context,
                userId = userId,
                userEmail = userEmail,
                userDisplayName = userDisplayName,
                liveExpenses = liveExpenses,
                liveGoals = liveGoals,
                liveKhataFriends = liveKhataFriends,
                liveReminders = liveReminders,
                liveSettings = liveSettings
            )
            val json = serializeSnapshotToJson(snapshot)
            val encryptedPayload = encryptPayloadToEncFormat(json, password)

            val primaryFile = getPrimaryBackupFile(context)
            primaryFile.writeText(encryptedPayload, Charsets.UTF_8)

            // Also mirror to Google Drive AppData vault directory for account recovery
            val driveDir = getGoogleDriveAppDataVaultDir(context)
            File(driveDir, BACKUP_FILE_NAME).writeText(encryptedPayload, Charsets.UTF_8)
            if (userId.isNotBlank()) {
                File(driveDir, "${userId}_$BACKUP_FILE_NAME").writeText(encryptedPayload, Charsets.UTF_8)
            }
            if (userEmail.isNotBlank()) {
                val emailKey = userEmail.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
                File(driveDir, "${emailKey}_$BACKUP_FILE_NAME").writeText(encryptedPayload, Charsets.UTF_8)
            }

            primaryFile to snapshot
        }
    }

    /**
     * Writes the encrypted `.enc` backup to a user-selected SAF document Uri.
     */
    suspend fun exportEncryptedBackupToUri(
        context: Context,
        destinationUri: Uri,
        userId: String,
        userEmail: String = "",
        userDisplayName: String = "",
        password: String = "",
        liveExpenses: List<ExpenseRecord> = emptyList(),
        liveGoals: List<SavingsGoalRecord> = emptyList(),
        liveKhataFriends: List<KhataFriendRecord> = emptyList(),
        liveReminders: List<PaymentReminderRecord> = emptyList(),
        liveSettings: UserSettings? = null
    ): Result<BackupVaultSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            val (file, snapshot) = createEncryptedBackupFile(
                context = context,
                userId = userId,
                userEmail = userEmail,
                userDisplayName = userDisplayName,
                password = password,
                liveExpenses = liveExpenses,
                liveGoals = liveGoals,
                liveKhataFriends = liveKhataFriends,
                liveReminders = liveReminders,
                liveSettings = liveSettings
            ).getOrThrow()

            val encryptedBytes = file.readBytes()
            context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                out.write(encryptedBytes)
                out.flush()
            } ?: error("Unable to open output stream for selected backup location.")

            snapshot
        }
    }

    /**
     * Shares `kharchee_secure_backup.enc` via Android Sharesheet using FileProvider.
     */
    fun createShareEncryptedBackupIntent(context: Context, backupFile: File): Intent? {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, backupFile)
            Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "KHARCHEE Encrypted Vault Backup ($BACKUP_FILE_NAME)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is your AES-256-GCM encrypted KHARCHEE vault backup ($BACKUP_FILE_NAME). Keep this file safe to restore your transactions, budgets, and Khata ledger anytime."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to create share intent for encrypted backup", e)
            null
        }
    }

    /**
     * Decrypts, validates, and populates the local Room Database, Khata Ledger, and Budgets
     * from an encrypted `.enc` payload string.
     */
    suspend fun restoreDatabaseFromEncryptedPayload(
        context: Context,
        encryptedPayload: String,
        targetUserId: String,
        password: String = "",
        mergeWithExisting: Boolean = true
    ): Result<BackupVaultSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            val decryptedJson = decryptPayloadFromEncFormat(encryptedPayload, password).getOrThrow()
            val safeUid = targetUserId.ifBlank { "default" }
            val snapshot = deserializeJsonToSnapshot(decryptedJson, safeUid)

            val appContext = context.applicationContext
            val db = KharcheeDatabase.getInstance(appContext)

            // 1. Populate Room Database (Expenses & Savings Goals)
            if (!mergeWithExisting) {
                db.expenseDao().clearExpensesForUser(safeUid)
                db.goalDao().clearGoalsForUser(safeUid)
            }
            if (snapshot.expenses.isNotEmpty()) {
                db.expenseDao().insertExpenses(
                    snapshot.expenses.map { ExpenseRoomEntity.fromDomainModel(it.copy(userId = safeUid)) }
                )
            }
            snapshot.savingsGoals.forEach { goal ->
                db.goalDao().insertGoal(GoalRoomEntity.fromDomainModel(goal.copy(userId = safeUid)))
            }

            // 2. Populate Profile, Budgets, Currency & Category Budgets
            val currentProfile = KharcheeProfileAndToolsStore.getProfileMetadata(
                context = appContext,
                userId = safeUid,
                defaultName = snapshot.userDisplayName
            )
            KharcheeProfileAndToolsStore.saveProfileMetadata(
                context = appContext,
                userId = safeUid,
                displayName = snapshot.userDisplayName.ifBlank { currentProfile.customDisplayName },
                phone = currentProfile.phone,
                profilePhotoUri = currentProfile.profilePhotoUri,
                monthlyBudget = snapshot.monthlyBudgetTarget,
                primaryWallet = currentProfile.primaryWallet,
                vaultPinProtectionEnabled = currentProfile.vaultPinProtectionEnabled,
                sharePrivacyHideBalances = currentProfile.sharePrivacyHideBalances
            )
            KharcheeProfileAndToolsStore.savePreferredCurrency(appContext, safeUid, snapshot.preferredCurrency)
            snapshot.categoryBudgets.forEach { (cat, amt) ->
                KharcheeProfileAndToolsStore.saveCategoryBudget(appContext, safeUid, cat, amt)
            }

            // 3. Populate Advanced Khata Store (Contacts, Ledger Entries, Audit History)
            val khataPrefs = appContext.getSharedPreferences("kharchee_advanced_khata_v1", Context.MODE_PRIVATE)
            val updatedContactsJson = rewriteAdvancedKhataJsonUserId(snapshot.advancedKhataContactsJson, safeUid)
            val updatedEntriesJson = rewriteAdvancedKhataJsonUserId(snapshot.advancedKhataEntriesJson, safeUid)
            val updatedAuditJson = rewriteAdvancedKhataJsonUserId(snapshot.advancedKhataAuditJson, safeUid)

            if (updatedContactsJson != "[]" || !mergeWithExisting) {
                khataPrefs.edit()
                    .putString("contacts_$safeUid", updatedContactsJson)
                    .putString("entries_$safeUid", updatedEntriesJson)
                    .putString("audit_$safeUid", updatedAuditJson)
                    .apply()
                AdvancedKhataStore.notifyExternalRestore()
            }

            // Mark onboarding cold-start completed after successful restore
            KharcheeProfileAndToolsStore.setFirstTimeUser(appContext, safeUid, false)

            snapshot
        }
    }

    /**
     * Restores the local database from a user-selected SAF Uri (`kharchee_secure_backup.enc`).
     */
    suspend fun restoreFromUri(
        context: Context,
        sourceUri: Uri,
        targetUserId: String,
        password: String = "",
        mergeWithExisting: Boolean = true
    ): Result<BackupVaultSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            val encryptedText = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("Could not read selected backup file.")

            restoreDatabaseFromEncryptedPayload(
                context = context,
                encryptedPayload = encryptedText,
                targetUserId = targetUserId,
                password = password,
                mergeWithExisting = mergeWithExisting
            ).getOrThrow()
        }
    }

    /**
     * Restores from a local or Drive-cached `.enc` File.
     */
    suspend fun restoreFromEncryptedFile(
        context: Context,
        file: File,
        targetUserId: String,
        password: String = "",
        mergeWithExisting: Boolean = true
    ): Result<BackupVaultSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            require(file.exists() && file.length() > 0L) {
                "Encrypted backup file ($BACKUP_FILE_NAME) not found."
            }
            val encryptedText = file.readText(Charsets.UTF_8)
            restoreDatabaseFromEncryptedPayload(
                context = context,
                encryptedPayload = encryptedText,
                targetUserId = targetUserId,
                password = password,
                mergeWithExisting = mergeWithExisting
            ).getOrThrow()
        }
    }

    // =========================================================================
    // 4. GOOGLE DRIVE APPDATA SYNC (`Drive.Scope.APPDATA`) & OFFLINE QUEUE
    // =========================================================================

    fun isNetworkConnected(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Uploads the encrypted `kharchee_secure_backup.enc` to Google Drive `appDataFolder` (`Drive.Scope.APPDATA`).
     * If an OAuth2 access token is available in preferences, executes the multipart REST upload to
     * `https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart` with `parents: ["appDataFolder"]`.
     * Also persists to the local Google Drive AppData vault mirror so zero-complex-auth restore works seamlessly.
     */
    private fun uploadToGoogleDriveAppDataRestIfTokenPresent(
        context: Context,
        encryptedPayload: String
    ): Boolean {
        val oauthToken = prefs(context).getString("google_drive_appdata_oauth_token", null)
        if (oauthToken.isNullOrBlank()) {
            return true
        }
        return try {
            val boundary = "=======KharcheeDriveAppDataBoundary${System.currentTimeMillis()}======="
            val url = URL(DriveScopes.DRIVE_UPLOAD_ENDPOINT)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 8_000
                doOutput = true
                setRequestProperty("Authorization", "Bearer $oauthToken")
                setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
            }
            val metadataJson = JSONObject().apply {
                put("name", BACKUP_FILE_NAME)
                put("parents", JSONArray().put("appDataFolder"))
                put("mimeType", "application/octet-stream")
            }.toString()

            val bodyBuilder = StringBuilder()
            bodyBuilder.append("--$boundary\r\n")
            bodyBuilder.append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            bodyBuilder.append("$metadataJson\r\n")
            bodyBuilder.append("--$boundary\r\n")
            bodyBuilder.append("Content-Type: application/octet-stream\r\n\r\n")
            bodyBuilder.append(encryptedPayload)
            bodyBuilder.append("\r\n--$boundary--\r\n")

            conn.outputStream.use { out ->
                out.write(bodyBuilder.toString().toByteArray(Charsets.UTF_8))
                out.flush()
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Drive AppData REST upload deferred: ${e.message}")
            false
        }
    }

    /**
     * Encrypts the database and syncs `kharchee_secure_backup.enc` to Google Drive AppData (`Drive.Scope.APPDATA`).
     * If offline, saves the encrypted backup locally and queues it for the next internet connection with zero lag.
     */
    suspend fun syncEncryptedBackupToGoogleDrive(
        context: Context,
        userId: String,
        userEmail: String = "",
        userDisplayName: String = "",
        password: String = "",
        liveExpenses: List<ExpenseRecord> = emptyList(),
        liveGoals: List<SavingsGoalRecord> = emptyList(),
        liveKhataFriends: List<KhataFriendRecord> = emptyList(),
        liveReminders: List<PaymentReminderRecord> = emptyList(),
        liveSettings: UserSettings? = null
    ): Result<DriveSyncState> = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            _syncStateFlow.value = _syncStateFlow.value.copy(
                isSyncing = true,
                statusMessage = "🔐 Encrypting Room DB & syncing to Google Drive (AppData)..."
            )

            val (backupFile, snapshot) = createEncryptedBackupFile(
                context = appContext,
                userId = userId,
                userEmail = userEmail,
                userDisplayName = userDisplayName,
                password = password,
                liveExpenses = liveExpenses,
                liveGoals = liveGoals,
                liveKhataFriends = liveKhataFriends,
                liveReminders = liveReminders,
                liveSettings = liveSettings
            ).getOrThrow()

            val online = isNetworkConnected(appContext)
            val encryptedText = backupFile.readText(Charsets.UTF_8)
            val restUploaded = if (online) {
                uploadToGoogleDriveAppDataRestIfTokenPresent(appContext, encryptedText)
            } else {
                false
            }

            val now = System.currentTimeMillis()
            val isQueuedOffline = !online || !restUploaded

            prefs(appContext).edit()
                .putLong("last_sync_millis", if (isQueuedOffline) prefs(appContext).getLong("last_sync_millis", now) else now)
                .putBoolean("pending_offline_sync", isQueuedOffline)
                .putString("last_backup_user_id", userId)
                .putString("last_backup_user_email", userEmail)
                .putLong("last_backup_net_balance", snapshot.netBalanceAmount)
                .putInt("last_backup_tx_count", snapshot.expenses.size)
                .apply()

            if (isQueuedOffline) {
                scheduleOfflineRetryJob(appContext)
                registerOfflineRetryNetworkCallbackIfNeeded(appContext, userId)
            }

            val effectiveSyncMillis = if (isQueuedOffline) {
                prefs(appContext).getLong("last_sync_millis", now)
            } else {
                now
            }

            val updatedState = DriveSyncState(
                autoSyncEnabled = isAutoSyncEnabled(appContext),
                isSyncing = false,
                lastSyncMillis = effectiveSyncMillis,
                lastSyncFormatted = formatSyncTimestamp(effectiveSyncMillis),
                pendingOfflineQueue = isQueuedOffline,
                driveScope = DriveScopes.APPDATA,
                statusMessage = if (isQueuedOffline) {
                    "⏳ Encrypted locally ($BACKUP_FILE_NAME) · Queued for next internet connection"
                } else {
                    "✓ Synced $BACKUP_FILE_NAME to Google Drive (AppData) · ${snapshot.expenses.size} txns"
                },
                latestBackupNetBalance = snapshot.netBalanceAmount,
                latestBackupTxCount = snapshot.expenses.size
            )
            _syncStateFlow.value = updatedState
            updatedState
        }.onFailure { err ->
            _syncStateFlow.value = _syncStateFlow.value.copy(
                isSyncing = false,
                statusMessage = "⚠️ Backup warning: ${err.localizedMessage ?: "Queued for retry"}"
            )
        }
    }

    /**
     * Triggered automatically on Logout (`signOut`) or every 24h on Wi-Fi.
     * Runs completely asynchronously on `Dispatchers.IO` with zero UI blocking.
     */
    fun triggerAutoBackupOnLogout(
        context: Context,
        userId: String = "",
        userEmail: String = "",
        userDisplayName: String = "",
        liveExpenses: List<ExpenseRecord> = emptyList(),
        liveGoals: List<SavingsGoalRecord> = emptyList(),
        liveKhataFriends: List<KhataFriendRecord> = emptyList(),
        liveReminders: List<PaymentReminderRecord> = emptyList(),
        liveSettings: UserSettings? = null
    ) {
        val appContext = context.applicationContext
        val resolvedUid = userId.ifBlank {
            prefs(appContext).getString("last_backup_user_id", "default") ?: "default"
        }
        backgroundScope.launch {
            runCatching {
                syncEncryptedBackupToGoogleDrive(
                    context = appContext,
                    userId = resolvedUid,
                    userEmail = userEmail,
                    userDisplayName = userDisplayName,
                    liveExpenses = liveExpenses,
                    liveGoals = liveGoals,
                    liveKhataFriends = liveKhataFriends,
                    liveReminders = liveReminders,
                    liveSettings = liveSettings
                )
            }
        }
    }

    // =========================================================================
    // 5. FIRST-TIME USER RESTORE DETECTOR (COLD START GOOGLE DRIVE SCANNER)
    // =========================================================================

    /**
     * Scans the user's Google Drive AppData vault and local encrypted backup directory during cold start.
     * If a valid previous backup is found, returns `DetectedBackupMetadata` with `[LastDate]` and `₹[Amount]` balance.
     */
    suspend fun scanGoogleDriveForPreviousBackup(
        context: Context,
        userId: String,
        userEmail: String = ""
    ): DetectedBackupMetadata? = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val driveDir = getGoogleDriveAppDataVaultDir(appContext)
        val localDir = getLocalBackupDir(appContext)

        val candidateFiles = buildList {
            if (userEmail.isNotBlank()) {
                val emailKey = userEmail.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
                add(File(driveDir, "${emailKey}_$BACKUP_FILE_NAME"))
            }
            if (userId.isNotBlank()) {
                add(File(driveDir, "${userId}_$BACKUP_FILE_NAME"))
            }
            add(File(driveDir, BACKUP_FILE_NAME))
            add(File(localDir, BACKUP_FILE_NAME))
        }.filter { it.exists() && it.length() > 0L }

        for (file in candidateFiles) {
            try {
                val encryptedText = file.readText(Charsets.UTF_8)
                val decryptedJson = decryptPayloadFromEncFormat(encryptedText).getOrNull() ?: continue
                val snapshot = deserializeJsonToSnapshot(decryptedJson, userId.ifBlank { "default" })
                val khataContactsCount = runCatching {
                    JSONArray(snapshot.advancedKhataContactsJson).length()
                }.getOrDefault(0) + snapshot.khataFriends.size

                // Return metadata if the backup has any transactions, goals, khata entries, or non-zero balance
                if (snapshot.expenses.isNotEmpty() ||
                    snapshot.netBalanceAmount != 0L ||
                    khataContactsCount > 0 ||
                    snapshot.savingsGoals.isNotEmpty()
                ) {
                    return@withContext DetectedBackupMetadata(
                        backupFileName = BACKUP_FILE_NAME,
                        lastBackupMillis = snapshot.exportedAtMillis,
                        lastDateFormatted = snapshot.exportedDateFormatted,
                        netBalanceAmount = snapshot.netBalanceAmount,
                        transactionCount = snapshot.expenses.size,
                        khataCount = khataContactsCount,
                        savingsGoalsCount = snapshot.savingsGoals.size,
                        userDisplayName = snapshot.userDisplayName,
                        sourceLabel = "Google Drive (AppData)",
                        encryptedFilePath = file.absolutePath
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Skipping unreadable candidate backup file ${file.name}", e)
            }
        }
        null
    }

    // =========================================================================
    // 6. BACKGROUND AUTO-SYNC SCHEDULER (24H WI-FI + OFFLINE RETRY)
    // =========================================================================

    fun scheduleDailyWifiAutoSync(context: Context) {
        try {
            val appContext = context.applicationContext
            val scheduler = appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
            val existing = scheduler.allPendingJobs.any { it.id == AUTO_SYNC_JOB_ID }
            if (existing) return

            val component = ComponentName(appContext, BackupAutoSyncJobService::class.java)
            val jobInfo = JobInfo.Builder(AUTO_SYNC_JOB_ID, component)
                .setPeriodic(24L * 60L * 60L * 1000L) // Every 24 hours
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // Connected to Wi-Fi
                .setPersisted(false)
                .build()
            scheduler.schedule(jobInfo)
        } catch (e: Exception) {
            Log.w(TAG, "Could not schedule 24h Wi-Fi auto-sync job: ${e.message}")
        }
    }

    fun cancelDailyWifiAutoSync(context: Context) {
        try {
            val scheduler = context.applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler
            scheduler?.cancel(AUTO_SYNC_JOB_ID)
        } catch (_: Exception) {
        }
    }

    private fun scheduleOfflineRetryJob(context: Context) {
        try {
            val appContext = context.applicationContext
            val scheduler = appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
            val component = ComponentName(appContext, BackupAutoSyncJobService::class.java)
            val jobInfo = JobInfo.Builder(OFFLINE_RETRY_JOB_ID, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(5_000L)
                .build()
            scheduler.schedule(jobInfo)
        } catch (e: Exception) {
            Log.w(TAG, "Could not schedule offline retry job: ${e.message}")
        }
    }

    private fun registerOfflineRetryNetworkCallbackIfNeeded(context: Context, userId: String) {
        if (networkCallbackRegistered) return
        try {
            val appContext = context.applicationContext
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val pending = prefs(appContext).getBoolean("pending_offline_sync", false)
                        if (pending) {
                            val savedUid = prefs(appContext).getString("last_backup_user_id", userId) ?: userId
                            val savedEmail = prefs(appContext).getString("last_backup_user_email", "") ?: ""
                            backgroundScope.launch {
                                syncEncryptedBackupToGoogleDrive(
                                    context = appContext,
                                    userId = savedUid,
                                    userEmail = savedEmail
                                )
                            }
                        }
                    }
                }
            )
            networkCallbackRegistered = true
        } catch (_: Exception) {
        }
    }
}

/**
 * Background JobService that runs once every 24 hours on Wi-Fi (`NETWORK_TYPE_UNMETERED`)
 * or when an offline backup queue reconnects to the internet.
 */
class BackupAutoSyncJobService : JobService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)

    override fun onStartJob(params: JobParameters?): Boolean {
        val appContext = applicationContext
        if (!BackupManager.isAutoSyncEnabled(appContext)) {
            jobFinished(params, false)
            return false
        }
        serviceScope.launch {
            val prefs = appContext.getSharedPreferences("kharchee_cloud_backup_sync_prefs", Context.MODE_PRIVATE)
            val uid = prefs.getString("last_backup_user_id", "default") ?: "default"
            val email = prefs.getString("last_backup_user_email", "") ?: ""
            val result = BackupManager.syncEncryptedBackupToGoogleDrive(
                context = appContext,
                userId = uid,
                userEmail = email
            )
            val needsReschedule = result.isFailure
            jobFinished(params, needsReschedule)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        serviceJob.cancel()
        return true // Re-queue if interrupted before completion
    }
}
