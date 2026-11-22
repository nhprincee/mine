package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LifetimeFinancialMemorySystem
import com.example.data.model.ExpenseRecord
import com.example.data.model.KhataFriendRecord
import com.example.data.model.SavingsGoalRecord
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONArray
import org.json.JSONObject

data class EmergencyLegacyConfig(
    val enabled: Boolean = false,
    val trustedContactName: String = "",
    val trustedContactDestination: String = "", // Email or phone/handle
    val inactivityDaysThreshold: Int = EmergencyLegacyModeManager.DEFAULT_INACTIVITY_DAYS,
    val lastActiveEpochMillis: Long = System.currentTimeMillis(),
    val lastEncryptedDispatchMillis: Long = 0L,
    val customNote: String = ""
)

data class EmergencyLegacyEvaluation(
    val isEnabled: Boolean,
    val daysInactive: Long,
    val daysRemaining: Long,
    val isInactivityTriggerReached: Boolean,
    val trustedContactConfigured: Boolean
)

/**
 * KHARCHEE — EMERGENCY LEGACY MODE (14-Day Inactivity Encrypted Handover)
 *
 * - Tracks last authenticated app activity timestamp (`recordUserActivityHeartbeat`).
 * - Monitors a 14-day inactivity threshold for configured trusted contacts.
 * - Encrypts the user's financial summary (Net Balance, Khata Lena/Dena, Savings Goals, Personal Spending)
 *   using AES-256-GCM + PBKDF2WithHmacSHA256 before sharing or archiving.
 * - Strictly EXCLUDES Secret Ghost Vault transactions so private hidden ledgers remain isolated.
 * - Dispatches via standard Android share/email intent (zero dangerous SMS permissions; Play Policy compliant).
 */
object EmergencyLegacyModeManager {

    const val DEFAULT_INACTIVITY_DAYS = 14
    private const val MILLIS_PER_DAY = 86_400_000L
    private const val PREFS_NAME = "kharchee_emergency_legacy_mode_prefs"
    private const val LEGACY_MAGIC_HEADER = "KHARCHEE_LEGACY_AES256_V1"

    private fun keyPrefix(userId: String): String = "legacy_${userId.ifBlank { "default" }}_"

    fun recordUserActivityHeartbeat(
        context: Context,
        userId: String,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong("${keyPrefix(userId)}last_active", nowMillis).apply()
    }

    fun loadConfig(context: Context, userId: String): EmergencyLegacyConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val p = keyPrefix(userId)
        val now = System.currentTimeMillis()
        return EmergencyLegacyConfig(
            enabled = prefs.getBoolean("${p}enabled", false),
            trustedContactName = prefs.getString("${p}contact_name", "").orEmpty(),
            trustedContactDestination = prefs.getString("${p}contact_dest", "").orEmpty(),
            inactivityDaysThreshold = prefs.getInt("${p}threshold_days", DEFAULT_INACTIVITY_DAYS).coerceIn(7, 90),
            lastActiveEpochMillis = prefs.getLong("${p}last_active", now),
            lastEncryptedDispatchMillis = prefs.getLong("${p}last_dispatch", 0L),
            customNote = prefs.getString("${p}custom_note", "").orEmpty()
        )
    }

    fun saveConfig(
        context: Context,
        userId: String,
        config: EmergencyLegacyConfig
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val p = keyPrefix(userId)
        prefs.edit()
            .putBoolean("${p}enabled", config.enabled)
            .putString("${p}contact_name", config.trustedContactName.trim())
            .putString("${p}contact_dest", config.trustedContactDestination.trim())
            .putInt("${p}threshold_days", config.inactivityDaysThreshold.coerceIn(7, 90))
            .putLong("${p}last_active", config.lastActiveEpochMillis)
            .putLong("${p}last_dispatch", config.lastEncryptedDispatchMillis)
            .putString("${p}custom_note", config.customNote.trim())
            .apply()
    }

    fun evaluateInactivityStatus(
        config: EmergencyLegacyConfig,
        nowMillis: Long = System.currentTimeMillis()
    ): EmergencyLegacyEvaluation {
        val elapsedMillis = (nowMillis - config.lastActiveEpochMillis).coerceAtLeast(0L)
        val daysInactive = elapsedMillis / MILLIS_PER_DAY
        val threshold = config.inactivityDaysThreshold.coerceAtLeast(1).toLong()
        val daysRemaining = (threshold - daysInactive).coerceAtLeast(0L)
        val contactReady = config.trustedContactName.isNotBlank() && config.trustedContactDestination.isNotBlank()
        val triggered = config.enabled && contactReady && daysInactive >= threshold
        return EmergencyLegacyEvaluation(
            isEnabled = config.enabled,
            daysInactive = daysInactive,
            daysRemaining = daysRemaining,
            isInactivityTriggerReached = triggered,
            trustedContactConfigured = contactReady
        )
    }

    /**
     * Builds the plain JSON financial summary (strictly excluding Secret Ghost Vault entries).
     */
    fun buildPlainFinancialSummaryJson(
        ownerDisplayName: String,
        expenses: List<ExpenseRecord>,
        khataFriends: List<KhataFriendRecord>,
        savingsGoals: List<SavingsGoalRecord>,
        customNote: String = "",
        generatedAtMillis: Long = System.currentTimeMillis()
    ): String {
        val separated = LifetimeFinancialMemorySystem.computeSeparatedDomainTotals(expenses)
        val netBalance = separated.incomeTotal - separated.personalSpendingTotal
        val totalLenaHai = khataFriends.filter { it.balance > 0L }.sumOf { it.balance }
        val totalDenaHai = khataFriends.filter { it.balance < 0L }.sumOf { -it.balance }
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(generatedAtMillis))

        val khataArray = JSONArray()
        khataFriends.filter { it.balance != 0L }.forEach { friend ->
            khataArray.put(
                JSONObject().apply {
                    put("name", friend.name)
                    put("direction", if (friend.balance > 0L) "LENA_HAI" else "DENA_HAI")
                    put("amountInr", kotlin.math.abs(friend.balance))
                    put("note", friend.lastDescription)
                }
            )
        }

        val goalsArray = JSONArray()
        savingsGoals.forEach { goal ->
            goalsArray.put(
                JSONObject().apply {
                    put("title", goal.title)
                    put("savedInr", goal.savedAmount)
                    put("targetInr", goal.targetAmount)
                }
            )
        }

        return JSONObject().apply {
            put("app", "KHARCHEE")
            put("type", "EMERGENCY_LEGACY_FINANCIAL_SUMMARY")
            put("owner", ownerDisplayName)
            put("generatedAt", dateStr)
            put("netBalanceInr", netBalance)
            put("totalIncomeInr", separated.incomeTotal)
            put("totalPersonalSpendingInr", separated.personalSpendingTotal)
            put("totalMoneyMovementInr", separated.moneyMovementTotal)
            put("khataTotalLenaHaiInr", totalLenaHai)
            put("khataTotalDenaHaiInr", totalDenaHai)
            put("khataOutstanding", khataArray)
            put("savingsGoals", goalsArray)
            put("ghostVaultIncluded", false)
            put("ownerLegacyNote", customNote.trim())
        }.toString(2)
    }

    /**
     * Encrypts a financial summary JSON payload using AES-256-GCM with a PBKDF2-derived key.
     */
    fun encryptSummaryPayload(plainJson: String, passphrase: String): String {
        require(passphrase.trim().length >= 4) { "Passphrase must be at least 4 characters." }
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }
        val secretKey = deriveAesKey(passphrase.trim(), salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val cipherBytes = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))

        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val dataB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
        return "$LEGACY_MAGIC_HEADER:$saltB64:$ivB64:$dataB64"
    }

    /**
     * Decrypts an AES-256-GCM Emergency Legacy payload back into the original JSON summary.
     */
    fun decryptSummaryPayload(encryptedBlob: String, passphrase: String): String {
        val parts = encryptedBlob.trim().split(":")
        require(parts.size == 4 && parts[0] == LEGACY_MAGIC_HEADER) {
            "Invalid KHARCHEE Emergency Legacy encrypted package format."
        }
        val salt = Base64.decode(parts[1], Base64.NO_WRAP)
        val iv = Base64.decode(parts[2], Base64.NO_WRAP)
        val cipherBytes = Base64.decode(parts[3], Base64.NO_WRAP)
        val secretKey = deriveAesKey(passphrase.trim(), salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    private fun deriveAesKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, 12_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Generates the encrypted summary package, saves a local archive copy, and launches
     * a secure share/email chooser addressed to the trusted contact.
     */
    fun exportAndShareEncryptedSummary(
        context: Context,
        userId: String,
        ownerDisplayName: String,
        config: EmergencyLegacyConfig,
        passphrase: String,
        expenses: List<ExpenseRecord>,
        khataFriends: List<KhataFriendRecord>,
        savingsGoals: List<SavingsGoalRecord>
    ): Result<String> = runCatching {
        val plainJson = buildPlainFinancialSummaryJson(
            ownerDisplayName = ownerDisplayName,
            expenses = expenses,
            khataFriends = khataFriends,
            savingsGoals = savingsGoals,
            customNote = config.customNote
        )
        val encryptedBlob = encryptSummaryPayload(plainJson, passphrase)

        // Archive encrypted package locally inside app-private files directory
        val outFile = File(context.filesDir, "kharchee_legacy_encrypted_${userId.take(8)}.khl")
        outFile.writeText(encryptedBlob, Charsets.UTF_8)

        val now = System.currentTimeMillis()
        saveConfig(context, userId, config.copy(lastEncryptedDispatchMillis = now))

        val shareBody = buildString {
            appendLine("KHARCHEE — Encrypted Emergency Legacy Financial Summary")
            appendLine("Owner: $ownerDisplayName")
            appendLine("Trusted Contact: ${config.trustedContactName} (${config.trustedContactDestination})")
            appendLine("Inactivity Rule: ${config.inactivityDaysThreshold} days")
            appendLine("Encryption: AES-256-GCM (Secret Ghost Vault excluded)")
            appendLine()
            appendLine("--- BEGIN ENCRYPTED PAYLOAD ---")
            appendLine(encryptedBlob)
            appendLine("--- END ENCRYPTED PAYLOAD ---")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "KHARCHEE Encrypted Legacy Summary — $ownerDisplayName")
            if (config.trustedContactDestination.contains("@")) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(config.trustedContactDestination.trim()))
            }
            putExtra(Intent.EXTRA_TEXT, shareBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, "Send Encrypted Legacy Backup to ${config.trustedContactName}")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)

        encryptedBlob
    }
}

@Composable
fun EmergencyLegacyModeCard(
    userId: String,
    ownerDisplayName: String,
    expenses: List<ExpenseRecord>,
    khataFriends: List<KhataFriendRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var config by remember(userId) { mutableStateOf(EmergencyLegacyModeManager.loadConfig(context, userId)) }
    var contactName by remember(userId) { mutableStateOf(config.trustedContactName) }
    var contactDest by remember(userId) { mutableStateOf(config.trustedContactDestination) }
    var customNote by remember(userId) { mutableStateOf(config.customNote) }
    var passphrase by remember { mutableStateOf("") }

    val evaluation = remember(config) {
        EmergencyLegacyModeManager.evaluateInactivityStatus(config)
    }

    val midnightBlack = Color(0xFF080B08)
    val pureWhite = Color(0xFFFFFFFF)
    val limeGreen = Color(0xFFB7F000)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF101510))
            .border(1.dp, if (config.enabled) limeGreen.copy(alpha = 0.75f) else Color(0xFF263026), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("emergency_legacy_mode_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(midnightBlack)
                        .border(1.dp, limeGreen, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Emergency Legacy Mode",
                        tint = limeGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "EMERGENCY LEGACY MODE · 14-DAY VAULT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = limeGreen
                    )
                    Text(
                        text = "Encrypted Trusted-Contact Handover",
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = pureWhite
                    )
                }
            }

            Switch(
                checked = config.enabled,
                onCheckedChange = { enabled ->
                    val updated = config.copy(
                        enabled = enabled,
                        trustedContactName = contactName.trim(),
                        trustedContactDestination = contactDest.trim(),
                        customNote = customNote.trim()
                    )
                    config = updated
                    EmergencyLegacyModeManager.saveConfig(context, userId, updated)
                    onShowToast(
                        if (enabled) "🛡️ Emergency Legacy Mode armed (14-day inactivity monitor)"
                        else "Emergency Legacy Mode paused"
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = midnightBlack,
                    checkedTrackColor = limeGreen
                ),
                modifier = Modifier.testTag("emergency_legacy_toggle")
            )
        }

        Surface(
            color = midnightBlack,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.7.dp, Color(0xFF263026)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (evaluation.isInactivityTriggerReached) {
                    "⚠️ 14-day inactivity threshold reached! Ready to dispatch AES-256 encrypted summary to ${config.trustedContactName}."
                } else {
                    "Heartbeat active · ${evaluation.daysRemaining} of ${config.inactivityDaysThreshold} inactivity days remaining before handover alert. Ghost Vault stays excluded."
                },
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = if (evaluation.isInactivityTriggerReached) limeGreen else Color(0xFFC8D2C8),
                lineHeight = 15.sp,
                modifier = Modifier.padding(10.dp)
            )
        }

        OutlinedTextField(
            value = contactName,
            onValueChange = { contactName = it },
            label = { Text("Trusted Contact Name", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = limeGreen,
                unfocusedBorderColor = Color(0xFF2B352B),
                focusedTextColor = pureWhite,
                unfocusedTextColor = pureWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("legacy_contact_name_input")
        )

        OutlinedTextField(
            value = contactDest,
            onValueChange = { contactDest = it },
            label = { Text("Trusted Email or Contact Handle", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = limeGreen,
                unfocusedBorderColor = Color(0xFF2B352B),
                focusedTextColor = pureWhite,
                unfocusedTextColor = pureWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("legacy_contact_destination_input")
        )

        OutlinedTextField(
            value = passphrase,
            onValueChange = { passphrase = it },
            label = { Text("AES-256 Encryption Passphrase (min 4 chars)", fontSize = 11.sp) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = limeGreen,
                unfocusedBorderColor = Color(0xFF2B352B),
                focusedTextColor = pureWhite,
                unfocusedTextColor = pureWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("legacy_passphrase_input")
        )

        OutlinedTextField(
            value = customNote,
            onValueChange = { customNote = it },
            label = { Text("Optional Emergency Instructions", fontSize = 11.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = limeGreen,
                unfocusedBorderColor = Color(0xFF2B352B),
                focusedTextColor = pureWhite,
                unfocusedTextColor = pureWhite
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val updated = config.copy(
                        trustedContactName = contactName.trim(),
                        trustedContactDestination = contactDest.trim(),
                        customNote = customNote.trim()
                    )
                    config = updated
                    EmergencyLegacyModeManager.saveConfig(context, userId, updated)
                    onShowToast("✅ Trusted contact saved for 14-day Emergency Legacy Mode")
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.9.dp, limeGreen.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = pureWhite),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("legacy_save_contact_button")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (contactName.isBlank() || contactDest.isBlank()) {
                        onShowToast("⚠️ Enter trusted contact name and email/handle first.")
                        return@Button
                    }
                    if (passphrase.trim().length < 4) {
                        onShowToast("⚠️ Enter an encryption passphrase (4+ chars) to encrypt summary.")
                        return@Button
                    }
                    val updated = config.copy(
                        enabled = true,
                        trustedContactName = contactName.trim(),
                        trustedContactDestination = contactDest.trim(),
                        customNote = customNote.trim()
                    )
                    config = updated
                    EmergencyLegacyModeManager.saveConfig(context, userId, updated)
                    val result = EmergencyLegacyModeManager.exportAndShareEncryptedSummary(
                        context = context,
                        userId = userId,
                        ownerDisplayName = ownerDisplayName,
                        config = updated,
                        passphrase = passphrase,
                        expenses = expenses,
                        khataFriends = khataFriends,
                        savingsGoals = savingsGoals
                    )
                    if (result.isSuccess) {
                        onShowToast("🔐 Encrypted Legacy Summary created & ready to send!")
                    } else {
                        onShowToast("❌ Failed to encrypt summary: ${result.exceptionOrNull()?.localizedMessage}")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = limeGreen,
                    contentColor = midnightBlack
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("legacy_send_encrypted_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Encrypt & Send", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
