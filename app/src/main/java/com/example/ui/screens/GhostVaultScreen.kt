package com.example.ui.screens

import android.content.Context
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.util.PerformancePreferences
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
import org.json.JSONArray
import org.json.JSONObject

data class GhostVaultTransaction(
    val id: String,
    val title: String,
    val category: String,
    val amountInr: Long,
    val paymentMode: String,
    val secretNote: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

enum class GhostPinVerificationStatus {
    UNLOCKED_REAL_VAULT,
    UNLOCKED_DECOY_LEDGER,
    INVALID_PIN
}

/**
 * Secret Ghost Vault (Decoy Hidden Ledger) Engine:
 * - Protected by a separate 4-digit SHA-256 Ghost PIN (`hashGhostPinSha256`).
 * - Stores private expenses in an isolated AES-256-GCM encrypted mini-ledger (`kharchee_secret_ghost_vault_prefs`).
 * - Completely isolated from the main Room database, Home dashboard, recent activity stream, and Insights graphs.
 * - Supports a Decoy Mode (`0000` PIN when real PIN is different) that opens a harmless empty decoy ledger if someone demands access.
 */
object GhostVaultManager {

    private const val PREFS_NAME = "kharchee_secret_ghost_vault_prefs"
    private const val KEY_GHOST_PIN_SHA256 = "ghost_pin_sha256_hash"
    private const val KEY_GHOST_PIN_SALT = "ghost_pin_salt_hex"
    private const val KEY_ENCRYPTED_GHOST_LEDGER = "ghost_encrypted_ledger_blob"
    private const val GHOST_VAULT_MAGIC = "GHOST_AES256_V1"

    val GHOST_CATEGORIES = listOf(
        "Private Gift",
        "Secret Dining",
        "Personal Care",
        "Confidential Transfer",
        "Night Out",
        "Classified"
    )

    fun hashGhostPinSha256(pin: String, saltHex: String): String {
        val cleanPin = pin.trim()
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("KHARCHEE_GHOST_PIN_V1::$saltHex::$cleanPin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getOrCreateSaltHex(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_GHOST_PIN_SALT, null)
        if (!existing.isNullOrBlank()) return existing
        val randomBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val generated = randomBytes.joinToString("") { "%02x".format(it) }
        prefs.edit().putString(KEY_GHOST_PIN_SALT, generated).apply()
        return generated
    }

    fun isGhostPinConfigured(context: Context): Boolean {
        return !context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GHOST_PIN_SHA256, null)
            .isNullOrBlank()
    }

    fun setGhostPin(
        context: Context,
        newFourDigitPin: String,
        previousPinForMigration: String? = null
    ): Boolean {
        val clean = newFourDigitPin.trim()
        if (clean.length != 4 || !clean.all { it.isDigit() }) return false

        val existingItems = if (previousPinForMigration != null && isGhostPinConfigured(context)) {
            getGhostTransactions(context, previousPinForMigration)
        } else {
            emptyList()
        }

        val salt = getOrCreateSaltHex(context)
        val sha256Hash = hashGhostPinSha256(clean, salt)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GHOST_PIN_SHA256, sha256Hash)
            .apply()

        if (existingItems.isNotEmpty()) {
            saveEncryptedGhostTransactions(context, clean, existingItems)
        }
        return true
    }

    fun verifyGhostPin(context: Context, enteredPin: String): GhostPinVerificationStatus {
        val clean = enteredPin.trim()
        if (clean.length != 4 || !clean.all { it.isDigit() }) {
            return GhostPinVerificationStatus.INVALID_PIN
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedHash = prefs.getString(KEY_GHOST_PIN_SHA256, null) ?: return GhostPinVerificationStatus.INVALID_PIN
        val salt = getOrCreateSaltHex(context)
        val candidateHash = hashGhostPinSha256(clean, salt)
        if (candidateHash.equals(savedHash, ignoreCase = true)) {
            return GhostPinVerificationStatus.UNLOCKED_REAL_VAULT
        }
        // Decoy PIN "0000" opens a clean decoy empty ledger when the real PIN is not "0000"
        if (clean == "0000") {
            return GhostPinVerificationStatus.UNLOCKED_DECOY_LEDGER
        }
        return GhostPinVerificationStatus.INVALID_PIN
    }

    fun addGhostTransaction(
        context: Context,
        pin: String,
        title: String,
        category: String,
        amountInr: Long,
        paymentMode: String = "UPI",
        secretNote: String = "",
        nowMillis: Long = System.currentTimeMillis()
    ): List<GhostVaultTransaction> {
        if (verifyGhostPin(context, pin) != GhostPinVerificationStatus.UNLOCKED_REAL_VAULT) {
            return emptyList()
        }
        if (amountInr <= 0L) return getGhostTransactions(context, pin)
        val current = getGhostTransactions(context, pin)
        val newEntry = GhostVaultTransaction(
            id = "ghost_tx_$nowMillis",
            title = title.trim().ifBlank { category },
            category = category.ifBlank { "Private Gift" },
            amountInr = amountInr,
            paymentMode = paymentMode.ifBlank { "UPI" },
            secretNote = secretNote.trim(),
            timestampMillis = nowMillis
        )
        val updated = (listOf(newEntry) + current).take(100)
        saveEncryptedGhostTransactions(context, pin, updated)
        return updated
    }

    fun deleteGhostTransaction(
        context: Context,
        pin: String,
        transactionId: String
    ): List<GhostVaultTransaction> {
        if (verifyGhostPin(context, pin) != GhostPinVerificationStatus.UNLOCKED_REAL_VAULT) {
            return emptyList()
        }
        val updated = getGhostTransactions(context, pin).filterNot { it.id == transactionId }
        saveEncryptedGhostTransactions(context, pin, updated)
        return updated
    }

    fun getGhostTransactions(
        context: Context,
        pin: String
    ): List<GhostVaultTransaction> {
        val status = verifyGhostPin(context, pin)
        if (status != GhostPinVerificationStatus.UNLOCKED_REAL_VAULT) {
            return emptyList()
        }
        val rawEncrypted = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ENCRYPTED_GHOST_LEDGER, null) ?: return emptyList()
        val salt = getOrCreateSaltHex(context)
        val jsonStr = decryptGhostPayload(rawEncrypted, pin, salt) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<GhostVaultTransaction>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    GhostVaultTransaction(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        category = obj.optString("category", "Private Gift"),
                        amountInr = obj.optLong("amountInr", 0L),
                        paymentMode = obj.optString("paymentMode", "UPI"),
                        secretNote = obj.optString("secretNote", ""),
                        timestampMillis = obj.optLong("timestampMillis", 0L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveEncryptedGhostTransactions(
        context: Context,
        pin: String,
        list: List<GhostVaultTransaction>
    ) {
        val arr = JSONArray()
        list.forEach { tx ->
            arr.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("title", tx.title)
                    put("category", tx.category)
                    put("amountInr", tx.amountInr)
                    put("paymentMode", tx.paymentMode)
                    put("secretNote", tx.secretNote)
                    put("timestampMillis", tx.timestampMillis)
                }
            )
        }
        val salt = getOrCreateSaltHex(context)
        val encrypted = encryptGhostPayload(arr.toString(), pin, salt)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENCRYPTED_GHOST_LEDGER, encrypted)
            .apply()
    }

    private fun deriveGhostAesKey(pin: String, saltHex: String, ivNonce: ByteArray): SecretKeySpec {
        val pinHash = hashGhostPinSha256(pin, saltHex)
        val spec = PBEKeySpec(
            "GHOST_LEDGER_KEY::$pinHash".toCharArray(),
            ivNonce,
            10_000,
            256
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun encryptGhostPayload(plainJson: String, pin: String, saltHex: String): String {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val key = deriveGhostAesKey(pin, saltHex, iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val cipherBytes = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
        return "$GHOST_VAULT_MAGIC:$ivB64:$cipherB64"
    }

    private fun decryptGhostPayload(encryptedBlob: String, pin: String, saltHex: String): String? {
        return try {
            if (!encryptedBlob.startsWith("$GHOST_VAULT_MAGIC:")) return null
            val parts = encryptedBlob.split(":")
            if (parts.size < 3) return null
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipherBytes = Base64.decode(parts[2], Base64.NO_WRAP)
            val key = deriveGhostAesKey(pin, saltHex, iv)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun SecretGhostVaultStudioCard(
    isSovereignPro: Boolean,
    onRequestUnlockPro: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var refreshTick by remember { mutableIntStateOf(0) }
    val isPinConfigured = remember(context, refreshTick) {
        GhostVaultManager.isGhostPinConfigured(context)
    }

    var pinInput by remember { mutableStateOf("") }
    var unlockedPin by remember { mutableStateOf<String?>(null) }
    var isDecoyUnlocked by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }

    var showAddGhostTxModal by remember { mutableStateOf(false) }
    var newTxTitle by remember { mutableStateOf("") }
    var newTxAmount by remember { mutableStateOf("") }
    var newTxCategory by remember { mutableStateOf(GhostVaultManager.GHOST_CATEGORIES.first()) }
    var newTxNote by remember { mutableStateOf("") }

    val ghostTransactions = remember(context, unlockedPin, isDecoyUnlocked, refreshTick) {
        if (isDecoyUnlocked || unlockedPin == null) {
            emptyList()
        } else {
            GhostVaultManager.getGhostTransactions(context, unlockedPin!!)
        }
    }
    val totalGhostSpendInr = remember(ghostTransactions) {
        ghostTransactions.sumOf { it.amountInr }
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH) }

    val cyberPurple = Color(0xFFA855F7)
    val neonEmerald = Color(0xFF00E676)
    val gold = Color(0xFFFFD700)
    val crimson = Color(0xFFFF3366)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF070510),
                        Color(0xFF110C22),
                        Color(0xFF070510)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        cyberPurple,
                        gold.copy(alpha = 0.7f),
                        neonEmerald.copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
            .testTag("secret_ghost_vault_studio_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1236))
                        .border(1.dp, cyberPurple, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (unlockedPin != null || isDecoyUnlocked) Icons.Default.LockOpen else Icons.Default.VisibilityOff,
                        contentDescription = "Secret Ghost Vault",
                        tint = cyberPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "👻 SECRET GHOST VAULT · ISOLATED LEDGER",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = cyberPurple
                    )
                    Text(
                        text = "4-Digit SHA-256 Ghost PIN · Zero Trace on Home or Graphs",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFFF9FAFB)
                    )
                }
            }

            Surface(
                color = if (unlockedPin != null) Color(0xFF06291A) else Color(0xFF1F1638),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    0.8.dp,
                    if (unlockedPin != null) neonEmerald else cyberPurple
                )
            ) {
                Text(
                    text = when {
                        isDecoyUnlocked -> "🎭 DECOY MODE"
                        unlockedPin != null -> "🔓 UNLOCKED"
                        isPinConfigured -> "🔒 PIN ARMED"
                        else -> "⚙️ SET 4D PIN"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (unlockedPin != null) neonEmerald else Color(0xFFD8B4FE),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = "Expenses logged inside the Secret Ghost Vault are encrypted with AES-256-GCM using your separate 4-digit SHA-256 Ghost PIN and remain 100% invisible from your Home balance, recent activity stream, and Insights charts. Entering '0000' opens a harmless empty decoy ledger.",
            fontSize = 11.sp,
            color = Color(0xFFB8B0D4),
            lineHeight = 16.sp
        )

        if (unlockedPin == null && !isDecoyUnlocked) {
            // Locked / Setup State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { next ->
                        val digitsOnly = next.filter { it.isDigit() }.take(4)
                        pinInput = digitsOnly
                        statusBanner = null
                    },
                    label = {
                        Text(
                            if (isPinConfigured) "Enter 4-Digit Ghost PIN (or 0000 Decoy)"
                            else "Create Separate 4-Digit Ghost PIN",
                            fontSize = 11.sp
                        )
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cyberPurple,
                        unfocusedBorderColor = Color(0xFF2B224C),
                        focusedContainerColor = Color(0xFF05050A),
                        unfocusedContainerColor = Color(0xFF05050A),
                        focusedTextColor = Color(0xFFF9FAFB),
                        unfocusedTextColor = Color(0xFFF9FAFB)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ghost_vault_pin_input")
                )

                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            statusIsError = true
                            statusBanner = "⚠️ Enter a 4-digit numeric Ghost PIN."
                            return@Button
                        }
                        if (!isPinConfigured) {
                            val ok = GhostVaultManager.setGhostPin(context, pinInput)
                            if (ok) {
                                unlockedPin = pinInput
                                isDecoyUnlocked = false
                                pinInput = ""
                                refreshTick++
                                statusIsError = false
                                statusBanner = "✅ 4-Digit SHA-256 Ghost PIN armed & Ghost Vault unlocked!"
                                onShowToast("👻 Secret Ghost Vault Armed & Unlocked!")
                            }
                        } else {
                            when (GhostVaultManager.verifyGhostPin(context, pinInput)) {
                                GhostPinVerificationStatus.UNLOCKED_REAL_VAULT -> {
                                    unlockedPin = pinInput
                                    isDecoyUnlocked = false
                                    pinInput = ""
                                    statusIsError = false
                                    statusBanner = null
                                    onShowToast("👻 Ghost Vault Unlocked (Isolated Encrypted Ledger)")
                                }
                                GhostPinVerificationStatus.UNLOCKED_DECOY_LEDGER -> {
                                    unlockedPin = null
                                    isDecoyUnlocked = true
                                    pinInput = ""
                                    statusIsError = false
                                    statusBanner = null
                                    onShowToast("👻 Ghost Vault Opened")
                                }
                                GhostPinVerificationStatus.INVALID_PIN -> {
                                    statusIsError = true
                                    statusBanner = "❌ Invalid 4-Digit Ghost PIN. Access Denied."
                                    onShowToast("❌ Invalid Ghost PIN")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cyberPurple,
                        contentColor = Color(0xFFF9FAFB)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("ghost_vault_unlock_or_set_pin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPinConfigured) "Unlock" else "Arm PIN",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            // Unlocked Ghost Mini-Ledger (or Decoy Empty Ledger)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0816))
                    .border(0.8.dp, cyberPurple.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ISOLATED GHOST OUTFLOW (HIDDEN FROM HOME)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB8B0D4)
                    )
                    Text(
                        text = "₹${PerformancePreferences.preFormatInr(totalGhostSpendInr)}",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFFF9FAFB)
                    )
                    Text(
                        text = "${ghostTransactions.size} encrypted private ${if (ghostTransactions.size == 1) "entry" else "entries"} · AES-256-GCM",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = neonEmerald
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isDecoyUnlocked) {
                        Button(
                            onClick = { showAddGhostTxModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = neonEmerald,
                                contentColor = Color(0xFF030712)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("ghost_vault_add_entry_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ghost Spend",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            unlockedPin = null
                            isDecoyUnlocked = false
                            pinInput = ""
                            onShowToast("🔒 Ghost Vault Locked & Purged from RAM")
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, crimson),
                        modifier = Modifier.testTag("ghost_vault_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Ghost Vault",
                            tint = crimson,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lock",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = crimson
                        )
                    }
                }
            }

            if (ghostTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF05050A))
                        .border(0.6.dp, Color(0xFF2B224C), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDecoyUnlocked) {
                            "👻 No private transactions recorded in this ledger."
                        } else {
                            "👻 Your encrypted Ghost Ledger is empty. Tap '+ Ghost Spend' to record private expenses invisible to the main app."
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFF8E84AD)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ghostTransactions.take(6).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF05050A))
                                .border(0.7.dp, Color(0xFF2B224C), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("ghost_vault_tx_row_${tx.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B1236)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👻", fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tx.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFF9FAFB),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tx.category} · ${tx.paymentMode} · ${dateFormat.format(Date(tx.timestampMillis))}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = Color(0xFF8E84AD)
                                    )
                                    if (tx.secretNote.isNotBlank()) {
                                        Text(
                                            text = "🔒 ${tx.secretNote}",
                                            fontSize = 10.sp,
                                            color = Color(0xFFD8B4FE),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "-₹${PerformancePreferences.preFormatInr(tx.amountInr)}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = cyberPurple
                                )
                                IconButton(
                                    onClick = {
                                        val activePin = unlockedPin ?: return@IconButton
                                        GhostVaultManager.deleteGhostTransaction(context, activePin, tx.id)
                                        refreshTick++
                                        onShowToast("🗑️ Removed Ghost entry '${tx.title}'")
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Ghost Entry",
                                        tint = Color(0xFF8E84AD),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (statusBanner != null) {
            Text(
                text = statusBanner!!,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (statusIsError) crimson else neonEmerald
            )
        }
    }

    if (showAddGhostTxModal && unlockedPin != null) {
        AlertDialog(
            onDismissRequest = { showAddGhostTxModal = false },
            containerColor = Color(0xFF0D0E17),
            title = {
                Text(
                    text = "👻 Log Secret Ghost Spend",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFFF9FAFB)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "100% isolated from Home Balance, Passbook & Insights. Encrypted with your 4-digit SHA-256 Ghost PIN.",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = cyberPurple
                    )
                    OutlinedTextField(
                        value = newTxAmount,
                        onValueChange = { newTxAmount = it.filter { ch -> ch.isDigit() }.take(8) },
                        label = { Text("Secret Amount in ₹", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ghost_vault_modal_amount_input")
                    )
                    OutlinedTextField(
                        value = newTxTitle,
                        onValueChange = { newTxTitle = it.take(45) },
                        label = { Text("Code Name / Private Item", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ghost_vault_modal_title_input")
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GhostVaultManager.GHOST_CATEGORIES.forEach { cat ->
                            val selected = newTxCategory == cat
                            Surface(
                                color = if (selected) cyberPurple.copy(alpha = 0.25f) else Color(0xFF05050A),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    0.8.dp,
                                    if (selected) cyberPurple else Color(0xFF2B224C)
                                ),
                                modifier = Modifier.clickable { newTxCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (selected) Color(0xFFF9FAFB) else Color(0xFFB8B0D4),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newTxNote,
                        onValueChange = { newTxNote = it.take(80) },
                        label = { Text("Encrypted Private Note (Optional)", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = newTxAmount.toLongOrNull() ?: 0L
                        val activePin = unlockedPin ?: return@Button
                        if (amt > 0L) {
                            GhostVaultManager.addGhostTransaction(
                                context = context,
                                pin = activePin,
                                title = newTxTitle.ifBlank { newTxCategory },
                                category = newTxCategory,
                                amountInr = amt,
                                paymentMode = "Ghost UPI",
                                secretNote = newTxNote
                            )
                            newTxAmount = ""
                            newTxTitle = ""
                            newTxNote = ""
                            showAddGhostTxModal = false
                            refreshTick++
                            onShowToast("👻 Logged ₹$amt in Secret Ghost Vault (Hidden from main app)")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cyberPurple,
                        contentColor = Color(0xFFF9FAFB)
                    ),
                    modifier = Modifier.testTag("ghost_vault_modal_save_button")
                ) {
                    Text("Encrypt & Hide Spend", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddGhostTxModal = false }) {
                    Text("Cancel", color = Color(0xFFB8B0D4))
                }
            }
        )
    }
}
