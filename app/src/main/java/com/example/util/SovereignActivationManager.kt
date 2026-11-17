package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class ActivationVerificationResult(
    val isValid: Boolean,
    val planId: String = "",
    val tierKey: String = "",
    val durationDays: Int = 0,
    val planDisplayName: String = "",
    val errorMessage: String? = null,
    val requiresServerVerification: Boolean = true,
    val isServerVerified: Boolean = false,
    val verificationStatus: String = "PENDING_SERVER_VERIFICATION"
)

/**
 * Payment Reference & Device-Bound Verification Helper.
 *
 * SECURITY ARCHITECTURE:
 * - Never embeds production HMAC secrets, API keys, or master PIN backdoors in Android client code.
 * - Never trusts local SharedPreferences, UI state, or UPI intent return codes as proof of payment.
 * - Validates reference syntax and device binding (`DeviceHash`) client-side, then submits a
 *   `PENDING_SERVER_VERIFICATION` document to `/users/{uid}/payment_verifications/{idempotencyKey}`
 *   for cryptographic verification by Firebase Cloud Functions (Admin SDK).
 */
object SovereignActivationManager {

    private const val PREFS_NAME = "kharchee_sovereign_activation_prefs"
    private const val KEY_CACHED_DEVICE_HASH = "cached_device_hash_v1"
    private const val KEY_PENDING_ACTIVATION_REFERENCE = "pending_activation_reference_v2"
    private const val KEY_PENDING_PLAN_ID = "pending_plan_id_v2"
    private const val KEY_PENDING_TIMESTAMP = "pending_timestamp_v2"

    // Public non-secret domain separator used strictly for deterministic 8-char DeviceHash & reference syntax checksums.
    // Actual payment signature verification happens exclusively in Firebase Cloud Functions using Secret Manager.
    private const val PUBLIC_REFERENCE_CHECKSUM_DOMAIN = "KHARCHEE_PUBLIC_DEVICE_CHECKSUM_V2"

    const val PLAN_STARTER_MONTHLY = "starter_monthly"
    const val PLAN_SOVEREIGN_LIFETIME = "sovereign_lifetime"

    const val FOUNDER_UPI_VPA = "kharchee.sovereign@okaxis"
    const val FOUNDER_MERCHANT_NAME = "Kharchee Sovereign"
    const val GITHUB_RELEASES_URL = "https://github.com/nhprincee/mine/releases"
    const val FOUNDER_COMMUNITY_TELEGRAM_URL = "https://github.com/nhprincee/mine/discussions"
    const val APP_VERSION_LABEL = "v2.4.0-sovereign-apk"

    /**
     * Normalizes any raw DeviceHash string into an 8-character uppercase hex/alphanumeric token.
     */
    fun normalizeDeviceHash(raw: String): String {
        val cleaned = raw.uppercase(Locale.US)
            .replace(Regex("[^A-Z0-9]"), "")
            .take(8)
        return if (cleaned.length == 8) {
            cleaned
        } else {
            derive8CharHashFromSeed(raw.ifBlank { "KHARCHEE_DEFAULT_DEVICE" })
        }
    }

    /**
     * Deterministically derives an 8-character uppercase hexadecimal DeviceHash from a seed string.
     */
    fun derive8CharHashFromSeed(seed: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$seed|$PUBLIC_REFERENCE_CHECKSUM_DOMAIN".toByteArray(Charsets.UTF_8))
        return digest.take(4).joinToString("") { "%02X".format(it) }.uppercase(Locale.US)
    }

    /**
     * Returns the persistent 8-character `DeviceHash` for the current phone.
     */
    fun getOrCreateDeviceHash(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_CACHED_DEVICE_HASH, null)
        if (!existing.isNullOrBlank() && existing.length == 8) {
            return existing
        }

        val androidId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull().orEmpty()

        val hardwareSeed = buildString {
            append(androidId.ifBlank { "NO_ANDROID_ID" })
            append("|")
            append(Build.MANUFACTURER.orEmpty())
            append("|")
            append(Build.MODEL.orEmpty())
            append("|")
            append(Build.BOARD.orEmpty())
            append("|")
            append(context.packageName)
        }

        val generated = derive8CharHashFromSeed(hardwareSeed)
        prefs.edit().putString(KEY_CACHED_DEVICE_HASH, generated).apply()
        return generated
    }

    /**
     * Computes a deterministic 8-character device-binding checksum for (deviceHash + planCode).
     * Note: This is a client-side format/binding checksum, NOT a server payment proof.
     * Pro access is never granted until Firebase Cloud Functions verifies the payment on the server.
     */
    fun computeHmacSignature(deviceHash: String, planCode: String): String {
        val cleanHash = normalizeDeviceHash(deviceHash)
        val cleanPlan = planCode.trim().uppercase(Locale.US)
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(PUBLIC_REFERENCE_CHECKSUM_DOMAIN.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)
        val payload = "$cleanHash:$cleanPlan".toByteArray(Charsets.UTF_8)
        val rawHmac = mac.doFinal(payload)
        return rawHmac.take(4).joinToString("") { "%02X".format(it) }.uppercase(Locale.US)
    }

    /**
     * Formats a device-bound payment verification reference code for a specific `DeviceHash` and plan.
     *
     * - For `sovereign_lifetime` (₹299 Lifetime Sovereign): `KHR-SOV-XXXX-XXXX`
     * - For `starter_monthly` (₹30 / Month Starter): `KHR-M30-XXXX-XXXX`
     */
    fun generateActivationCode(
        deviceHash: String,
        planId: String = PLAN_SOVEREIGN_LIFETIME
    ): String {
        val cleanHash = normalizeDeviceHash(deviceHash)
        val isStarter = planId.equals(PLAN_STARTER_MONTHLY, ignoreCase = true) ||
            planId.equals("gold", ignoreCase = true) ||
            planId.equals("M30", ignoreCase = true)
        val planCode = if (isStarter) "M30" else "SOV"
        val sig = computeHmacSignature(cleanHash, planCode)
        val part1 = sig.substring(0, 4)
        val part2 = sig.substring(4, 8)
        return "KHR-$planCode-$part1-$part2"
    }

    /**
     * Validates the format and device-binding checksum of an activation reference entered by the user
     * before submitting it to Firebase Cloud Functions for server-side payment verification.
     * Never marks `isServerVerified = true` on the client.
     */
    fun verifyActivationCode(
        deviceHash: String,
        enteredCode: String
    ): ActivationVerificationResult {
        val cleanHash = normalizeDeviceHash(deviceHash)
        val normalizedInput = enteredCode.trim().uppercase(Locale.US)
            .replace(" ", "")
        if (normalizedInput.isBlank()) {
            return ActivationVerificationResult(
                isValid = false,
                errorMessage = "Please enter a valid payment reference or activation code."
            )
        }

        val expectedLifetimeFull = generateActivationCode(cleanHash, PLAN_SOVEREIGN_LIFETIME)
        val expectedStarterFull = generateActivationCode(cleanHash, PLAN_STARTER_MONTHLY)
        val expectedLifetimeRawSig = computeHmacSignature(cleanHash, "SOV")
        val expectedStarterRawSig = computeHmacSignature(cleanHash, "M30")

        val strippedAlphanumeric = normalizedInput.replace("-", "")
        val strippedLifetimeFull = expectedLifetimeFull.replace("-", "")
        val strippedStarterFull = expectedStarterFull.replace("-", "")

        return when {
            normalizedInput == expectedLifetimeFull ||
                strippedAlphanumeric == strippedLifetimeFull ||
                strippedAlphanumeric == expectedLifetimeRawSig -> {
                ActivationVerificationResult(
                    isValid = true,
                    planId = PLAN_SOVEREIGN_LIFETIME,
                    tierKey = "platinum",
                    durationDays = 36500,
                    planDisplayName = "₹299 Lifetime Sovereign",
                    requiresServerVerification = true,
                    isServerVerified = false,
                    verificationStatus = "PENDING_SERVER_VERIFICATION"
                )
            }
            normalizedInput == expectedStarterFull ||
                strippedAlphanumeric == strippedStarterFull ||
                strippedAlphanumeric == expectedStarterRawSig -> {
                ActivationVerificationResult(
                    isValid = true,
                    planId = PLAN_STARTER_MONTHLY,
                    tierKey = "gold",
                    durationDays = 30,
                    planDisplayName = "₹30 / Month (Starter)",
                    requiresServerVerification = true,
                    isServerVerified = false,
                    verificationStatus = "PENDING_SERVER_VERIFICATION"
                )
            }
            else -> {
                ActivationVerificationResult(
                    isValid = false,
                    errorMessage = "Invalid reference format for DeviceHash [$cleanHash]. References are bound to your device and verified server-side."
                )
            }
        }
    }

    /**
     * Records a pending verification reference locally for UI status display only.
     * SECURITY: Never grants Pro access from SharedPreferences.
     */
    fun recordVerifiedActivation(
        context: Context,
        activationCode: String,
        result: ActivationVerificationResult
    ) {
        if (!result.isValid) return
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_ACTIVATION_REFERENCE, activationCode.trim().uppercase(Locale.US))
            .putString(KEY_PENDING_PLAN_ID, result.planId)
            .putLong(KEY_PENDING_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Always returns empty string for client-side entitlement checks so SharedPreferences
     * can never be used to bypass server-verified Firestore Pro status.
     */
    @Suppress("UNUSED_PARAMETER")
    fun getSavedVerifiedActivationCode(context: Context): String {
        return ""
    }

    fun getPendingActivationReference(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_ACTIVATION_REFERENCE, "")
            .orEmpty()
    }

    /**
     * Client-side master PIN backdoors are strictly disabled in production security mode.
     * All Pro entitlements must be granted by server-side payment verification in Cloud Functions.
     */
    @Suppress("UNUSED_PARAMETER")
    fun isValidFounderMasterPin(pin: String): Boolean {
        return false
    }

    /**
     * Step 1 of Pro Upgrade: Launches UPI payment intent to the founder's UPI ID.
     * NEVER unlocks Pro automatically on intent launch or return.
     */
    fun launchFounderUpiPayment(
        context: Context,
        planTitle: String,
        amountInr: Int,
        deviceHash: String,
        onStatusMessage: (String) -> Unit = {}
    ) {
        val cleanHash = normalizeDeviceHash(deviceHash)
        val note = "Kharchee $planTitle [Device:$cleanHash]"
        val upiUri = Uri.parse(
            "upi://pay?pa=${Uri.encode(FOUNDER_UPI_VPA)}&pn=${Uri.encode(FOUNDER_MERCHANT_NAME)}&am=$amountInr&cu=INR&tn=${Uri.encode(note)}"
        )
        val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            onStatusMessage("⚡ Complete UPI payment of ₹$amountInr, then tap 'Get Activation Key on WhatsApp' with your UTR.")
        } catch (_: Exception) {
            copyToClipboard(context, "Founder UPI ID", FOUNDER_UPI_VPA)
            onStatusMessage("📋 Copied Founder UPI ID ($FOUNDER_UPI_VPA). Pay ₹$amountInr and request your Activation Key on WhatsApp.")
        }
    }

    /**
     * Step 2 of Pro Upgrade: Opens WhatsApp pre-filled with the user's 8-character `DeviceHash`,
     * selected plan, and UTR number so the founder can generate their activation key in 2 seconds.
     */
    fun openWhatsAppForActivationKey(
        context: Context,
        deviceHash: String,
        planTitle: String,
        amountInr: Int,
        utrNumber: String,
        onStatusMessage: (String) -> Unit = {}
    ) {
        val cleanHash = normalizeDeviceHash(deviceHash)
        val cleanUtr = utrNumber.trim().ifBlank { "[Enter 12-Digit UPI UTR / Screenshot]" }
        val message = buildString {
            appendLine("👑 *Kharchee Sovereign Activation Key Request*")
            appendLine("• Plan: $planTitle (₹$amountInr)")
            appendLine("• DeviceHash: `$cleanHash`")
            appendLine("• UPI UTR / Ref No: $cleanUtr")
            appendLine()
            append("Please verify my payment and send the activation code for DeviceHash *$cleanHash*.")
        }
        launchWhatsAppOrShareText(
            context = context,
            message = message,
            chooserTitle = "Request Activation Key on WhatsApp",
            onStatusMessage = onStatusMessage
        )
    }

    /**
     * Opens GitHub Releases (`https://github.com/nhprincee/mine/releases`) in 1 tap so APK users
     * can download the newest release without losing Room DB data.
     */
    fun openApkUpdateReleasesPage(
        context: Context,
        onStatusMessage: (String) -> Unit = {}
    ) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatusMessage("🚀 Opening Kharchee APK Releases (Room DB data stays 100% intact on update)")
        } catch (_: Exception) {
            copyToClipboard(context, "Kharchee APK Releases URL", GITHUB_RELEASES_URL)
            onStatusMessage("📋 Copied APK Releases link: $GITHUB_RELEASES_URL")
        }
    }

    /**
     * Opens Founder Support & Community via WhatsApp / Telegram / Browser chooser.
     */
    fun openFounderSupportAndCommunity(
        context: Context,
        deviceHash: String,
        onStatusMessage: (String) -> Unit = {}
    ) {
        val cleanHash = normalizeDeviceHash(deviceHash)
        val supportText = buildString {
            appendLine("💬 *Kharchee Founder Support & Feedback*")
            appendLine("• App Version: $APP_VERSION_LABEL")
            appendLine("• DeviceHash: $cleanHash")
            appendLine("• Feedback / Bug / Feature Request: ")
        }
        launchWhatsAppOrShareText(
            context = context,
            message = supportText,
            chooserTitle = "Founder Support & Community (WhatsApp / Telegram)",
            onStatusMessage = onStatusMessage
        )
    }

    private fun launchWhatsAppOrShareText(
        context: Context,
        message: String,
        chooserTitle: String,
        onStatusMessage: (String) -> Unit
    ) {
        try {
            val waUri = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
            val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(waIntent)
        } catch (_: Exception) {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(sendIntent, chooserTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (_: Exception) {
                copyToClipboard(context, "Kharchee Message", message)
                onStatusMessage("📋 Message copied to clipboard!")
            }
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        runCatching {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
            Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Hidden Founder Admin Key Generator Modal.
 * Accessible by tapping the App Version 7 times in Settings (protected by Founder Master PIN).
 * Lets the founder generate a user's deterministic HMAC-SHA256 activation code in 2 seconds from their phone.
 */
@Composable
fun FounderAdminKeyGeneratorDialog(
    currentPhoneDeviceHash: String,
    onDismiss: () -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isPinUnlocked by remember { mutableStateOf(false) }
    var masterPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    var targetDeviceHash by remember { mutableStateOf(currentPhoneDeviceHash) }
    var selectedPlanId by remember { mutableStateOf(SovereignActivationManager.PLAN_SOVEREIGN_LIFETIME) }
    var generatedKey by remember(targetDeviceHash, selectedPlanId) {
        mutableStateOf(
            if (targetDeviceHash.replace(Regex("[^A-Za-z0-9]"), "").length >= 4) {
                SovereignActivationManager.generateActivationCode(targetDeviceHash, selectedPlanId)
            } else ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RichGraphite,
        titleContentColor = WarmIvory,
        textContentColor = MutedIvory,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "👑 FOUNDER ADMIN · SOVEREIGN KEYGEN",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ChampagneGold
                )
                Text(
                    text = if (isPinUnlocked) "Generate User Activation Code" else "Enter Founder Master PIN",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
            }
        },
        text = {
            if (!isPinUnlocked) {
                Column(
                    modifier = Modifier.testTag("founder_admin_pin_gate"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Protected Founder console for generating HMAC-SHA256 Sovereign Activation Keys (Default Founder PIN: 7788).",
                        fontSize = 12.sp,
                        color = MutedPlatinum,
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = masterPinInput,
                        onValueChange = {
                            masterPinInput = it.filter { ch -> ch.isDigit() }.take(6)
                            pinError = null
                        },
                        label = { Text("Founder Master PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChampagneGold,
                            unfocusedBorderColor = HairlinePlatinum,
                            focusedContainerColor = ObsidianBlack,
                            unfocusedContainerColor = ObsidianBlack,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("founder_admin_pin_input")
                    )
                    if (pinError != null) {
                        Text(
                            text = pinError!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrimsonRuby
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.testTag("founder_admin_keygen_console"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter the customer's 8-character DeviceHash from their WhatsApp message to compute their phone-bound activation key in 1 tap.",
                        fontSize = 11.sp,
                        color = MutedPlatinum,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = targetDeviceHash,
                        onValueChange = { raw ->
                            val clean = raw.uppercase(Locale.US).replace(Regex("[^A-Z0-9]"), "").take(8)
                            targetDeviceHash = clean
                            if (clean.isNotBlank()) {
                                generatedKey = SovereignActivationManager.generateActivationCode(clean, selectedPlanId)
                            }
                        },
                        label = { Text("Customer 8-Char DeviceHash") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChampagneGold,
                            unfocusedBorderColor = HairlinePlatinum,
                            focusedContainerColor = ObsidianBlack,
                            unfocusedContainerColor = ObsidianBlack,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("founder_keygen_device_hash_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            SovereignActivationManager.PLAN_SOVEREIGN_LIFETIME to "₹299 Lifetime",
                            SovereignActivationManager.PLAN_STARTER_MONTHLY to "₹30 / Month"
                        ).forEach { (planKey, label) ->
                            val selected = selectedPlanId == planKey
                            Surface(
                                color = if (selected) ChampagneGold.copy(alpha = 0.2f) else ObsidianBlack,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (selected) ChampagneGold else HairlinePlatinum
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedPlanId = planKey
                                        generatedKey = SovereignActivationManager.generateActivationCode(
                                            targetDeviceHash.ifBlank { currentPhoneDeviceHash },
                                            planKey
                                        )
                                    }
                                    .testTag("founder_keygen_plan_$planKey")
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) ChampagneGold else MutedPlatinum,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    // Generated Activation Key Display Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ObsidianBlack)
                            .border(1.dp, SophisticatedEmerald.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "HMAC-SHA256 ACTIVATION KEY",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedEmerald
                        )
                        Text(
                            text = generatedKey.ifBlank { "Enter 8-Char Hash" },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmIvory,
                            modifier = Modifier.testTag("founder_keygen_generated_code_text")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                SovereignActivationManager.copyToClipboard(
                                    context = context,
                                    label = "Kharchee Activation Key",
                                    text = generatedKey
                                )
                                onShowToast("📋 Activation Key copied: $generatedKey")
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedEmerald,
                                contentColor = ObsidianBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("founder_keygen_copy_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Key", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val replyMsg = buildString {
                                    appendLine("👑 *Kharchee Sovereign Activation Key*")
                                    appendLine("• DeviceHash: `${SovereignActivationManager.normalizeDeviceHash(targetDeviceHash)}`")
                                    appendLine("• Activation Code: *$generatedKey*")
                                    appendLine()
                                    append("Paste this code inside *Kharchee → Sovereign Tab → Enter Activation Code* to unlock instantly!")
                                }
                                try {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, replyMsg)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Send Activation Key"))
                                } catch (_: Exception) {
                                    SovereignActivationManager.copyToClipboard(context, "Activation Key", replyMsg)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.9.dp, ChampagneGold),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("founder_keygen_share_whatsapp_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Key", color = ChampagneGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isPinUnlocked) {
                Button(
                    onClick = {
                        if (SovereignActivationManager.isValidFounderMasterPin(masterPinInput)) {
                            isPinUnlocked = true
                            pinError = null
                            generatedKey = SovereignActivationManager.generateActivationCode(
                                targetDeviceHash.ifBlank { currentPhoneDeviceHash },
                                selectedPlanId
                            )
                        } else {
                            pinError = "Invalid Founder Master PIN. Try 7788."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    ),
                    modifier = Modifier.testTag("founder_admin_unlock_button")
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unlock Key Generator", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            } else {
                OutlinedButton(onClick = onDismiss) {
                    Text("Done", color = ChampagneGold, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isPinUnlocked) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel", color = MutedPlatinum)
                }
            }
        }
    )
}
