package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseRecord
import com.example.data.model.KhataFriendRecord
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PAYMENT_MODES
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserProfileMetadata
import com.example.data.model.UserSettings
import com.example.ui.components.KharcheeArchitecturalLogo
import com.example.ui.theme.ALL_HUD_THEMES
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.ui.security.SetFourDigitPinDialog
import com.example.ui.theme.ThemePreferenceManager
import com.example.ui.theme.WarmIvory
import com.example.util.BackupManager
import com.example.util.BackupVaultSnapshot
import com.example.util.SecurityLockManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileBottomSheet(
    userId: String,
    initialProfile: UserProfileMetadata,
    settings: UserSettings,
    onDismiss: () -> Unit,
    onSaveProfileAndSettings: (
        displayName: String,
        phone: String,
        photoUri: String,
        dailyLimit: Long,
        monthlyBudget: Long,
        primaryWallet: String,
        upiId: String,
        themeId: String,
        notifyPaymentReminders: Boolean,
        notifyNightGuardAlert: Boolean,
        notifyMorningReview: Boolean
    ) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onOpenWalkthrough: (() -> Unit)? = null,
    liveExpenses: List<ExpenseRecord> = emptyList(),
    liveGoals: List<SavingsGoalRecord> = emptyList(),
    liveKhataFriends: List<KhataFriendRecord> = emptyList(),
    liveReminders: List<PaymentReminderRecord> = emptyList(),
    onBackupRestored: (BackupVaultSnapshot, Boolean) -> Unit = { _, _ -> },
    onShowToast: (String) -> Unit = {},
    onToggleHideBalance: (Boolean) -> Unit = {},
    onOpenSovereignTab: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val coroutineScope = rememberCoroutineScope()
    val driveSyncState by BackupManager.syncStateFlow.collectAsStateWithLifecycle()
    val isAppLockEnabled by SecurityLockManager.isAppLockEnabledFlow.collectAsStateWithLifecycle()
    val lockTimeoutMillis by SecurityLockManager.lockTimeoutFlow.collectAsStateWithLifecycle()
    val isFlagSecureEnabled by SecurityLockManager.isFlagSecureEnabledFlow.collectAsStateWithLifecycle()
    val hasPinConfigured by SecurityLockManager.hasPinConfiguredFlow.collectAsStateWithLifecycle()
    val isBatterySaverReducedMotion by com.example.util.PerformancePreferences.isBatterySaverReducedMotionFlow.collectAsStateWithLifecycle()
    var showSetPinDialog by remember { mutableStateOf(false) }
    var backupPassword by remember { mutableStateOf("") }
    var mergeOnRestore by remember { mutableStateOf(true) }
    var localBackupStatusBanner by remember { mutableStateOf<String?>(null) }

    var displayName by remember(initialProfile) { mutableStateOf(initialProfile.customDisplayName) }
    var phone by remember(initialProfile) { mutableStateOf(initialProfile.phone) }
    var photoUri by remember(initialProfile) { mutableStateOf(initialProfile.profilePhotoUri) }

    LaunchedEffect(userId) {
        BackupManager.ensureInitialized(context, userId)
        SecurityLockManager.ensureInitialized(context)
        com.example.util.PerformancePreferences.ensureInitialized(context)
    }

    val exportBackupDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { destinationUri: Uri? ->
        if (destinationUri != null) {
            coroutineScope.launch {
                val res = BackupManager.exportEncryptedBackupToUri(
                    context = context,
                    destinationUri = destinationUri,
                    userId = userId,
                    userEmail = initialProfile.email,
                    userDisplayName = displayName.ifBlank { "Kharchee Member" },
                    password = backupPassword,
                    liveExpenses = liveExpenses,
                    liveGoals = liveGoals,
                    liveKhataFriends = liveKhataFriends,
                    liveReminders = liveReminders,
                    liveSettings = settings
                )
                res.fold(
                    onSuccess = { snap ->
                        val msg = "✅ Encrypted backup saved (${BackupManager.BACKUP_FILE_NAME} · ${snap.expenses.size} txns)"
                        localBackupStatusBanner = msg
                        onShowToast(msg)
                    },
                    onFailure = { err ->
                        val msg = "⚠️ Export failed: ${err.localizedMessage}"
                        localBackupStatusBanner = msg
                        onShowToast(msg)
                    }
                )
            }
        }
    }

    val restoreBackupDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { sourceUri: Uri? ->
        if (sourceUri != null) {
            coroutineScope.launch {
                val res = BackupManager.restoreFromUri(
                    context = context,
                    sourceUri = sourceUri,
                    targetUserId = userId,
                    password = backupPassword,
                    mergeWithExisting = mergeOnRestore
                )
                res.fold(
                    onSuccess = { snap ->
                        onBackupRestored(snap, mergeOnRestore)
                        val msg = "✅ Restored ₹${snap.netBalanceAmount} balance & ${snap.expenses.size} transactions from ${BackupManager.BACKUP_FILE_NAME}!"
                        localBackupStatusBanner = msg
                        onShowToast(msg)
                    },
                    onFailure = { err ->
                        val msg = "⚠️ Restore failed: ${err.localizedMessage ?: "Invalid password or corrupted file"}"
                        localBackupStatusBanner = msg
                        onShowToast(msg)
                    }
                )
            }
        }
    }
    var dailyLimitStr by remember(settings.dailyLimit) { mutableStateOf(settings.dailyLimit.toString()) }
    var monthlyBudgetStr by remember(initialProfile) { mutableStateOf(initialProfile.monthlyBudget.toString()) }
    var primaryWallet by remember(initialProfile) { mutableStateOf(initialProfile.primaryWallet) }
    var upiId by remember(settings.myUpiId) { mutableStateOf(settings.myUpiId) }
    var selectedThemeId by remember(settings.themeId) { mutableStateOf(ThemePreferenceManager.normalizeThemeId(settings.themeId)) }
    var notifyPaymentReminders by remember(settings.notifyPaymentReminders) { mutableStateOf(settings.notifyPaymentReminders) }
    var notifyNightGuardAlert by remember(settings.notifyNightGuardAlert) { mutableStateOf(settings.notifyNightGuardAlert) }
    var notifyMorningReview by remember(settings.notifyMorningReview) { mutableStateOf(settings.notifyMorningReview) }
    var vaultPinEnabled by remember(initialProfile) { mutableStateOf(initialProfile.vaultPinProtectionEnabled) }
    var shareHideBalances by remember(initialProfile) { mutableStateOf(initialProfile.sharePrivacyHideBalances) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            photoUri = uri.toString()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ChampagneGold,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory,
        focusedLabelColor = ChampagneGold,
        unfocusedLabelColor = MutedIvory
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RichGraphite,
        contentColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag("my_profile_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with KHARCHEE Architectural Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KharcheeArchitecturalLogo(
                    symbolSize = 32.dp,
                    wordmarkFontSizeSp = 15,
                    subtitle = "MY PROFILE & SOVEREIGN ACCOUNT"
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Profile", tint = MutedPlatinum)
                }
            }

            // Ethical Privacy & Trust Pill (Top of Settings)
            Surface(
                color = Color(0xFF0A1F17),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = SophisticatedEmerald.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ethical_privacy_trust_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛡️ 100% Local-First Privacy • Zero Ads • Core Features Free Forever",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SophisticatedEmerald
                    )
                }
            }

            // Avatar Photo Upload + Account ID Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(ElevatedGraphite)
                        .border(1.2.dp, ChampagneGold, CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("profile_photo_picker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = remember(photoUri) {
                        if (photoUri.isBlank()) null
                        else try {
                            context.contentResolver.openInputStream(Uri.parse(photoUri))?.use { stream ->
                                BitmapFactory.decodeStream(stream)?.asImageBitmap()
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        val initials = displayName.trim().take(2).uppercase().ifEmpty { "KH" }
                        Text(
                            text = initials,
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = ChampagneGold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ChampagneGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified Account",
                            tint = SophisticatedEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (settings.isPro) "KHARCHEE PRO MEMBER" else "VERIFIED VAULT HOLDER",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SophisticatedEmerald
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displayName.ifBlank { "Kharchee Member" },
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = WarmIvory
                    )
                    if (initialProfile.email.isNotBlank()) {
                        Text(
                            text = initialProfile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedPlatinum,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElevatedGraphite)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Kharchee Account ID", userId))
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("copy_account_id_chip")
                    ) {
                        Text(
                            text = "ID: ${userId.take(14)}...",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = MetallicSilver
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Account ID",
                            tint = ChampagneGold,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Member Since: ${initialProfile.accountCreatedDate}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = MutedIvory,
                        modifier = Modifier.testTag("profile_created_date_text")
                    )
                }
            }

            // Editable Identity Fields
            Text(
                text = "IDENTITY & CONTACT DETAILS",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp,
                color = MetallicSilver
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_display_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_phone_input")
                )

                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Receiving UPI ID") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_upi_input")
                )
            }

            // Account & Budget Settings
            Text(
                text = "BUDGET & WALLET PREFERENCES",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp,
                color = MetallicSilver
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dailyLimitStr,
                    onValueChange = { dailyLimitStr = it },
                    label = { Text("Daily Limit (₹)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_daily_limit_input")
                )

                OutlinedTextField(
                    value = monthlyBudgetStr,
                    onValueChange = { monthlyBudgetStr = it },
                    label = { Text("Monthly Budget (₹)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_monthly_budget_input")
                )
            }

            // Primary Wallet Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (PAYMENT_MODES + listOf("Bank")).forEach { wallet ->
                    val active = primaryWallet == wallet
                    Surface(
                        color = if (active) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(12.dp))
                            .clickable { primaryWallet = wallet }
                    ) {
                        Text(
                            text = wallet,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) ChampagneGold else MutedPlatinum,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                        )
                    }
                }
            }

            // Single Universal Theme Locked: Deep Dark Navy + Electric Purple/Violet Luxury

            // Notification Preferences & Security Settings
            Text(
                text = "NOTIFICATION PREFERENCES & SECURITY SETTINGS",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp,
                color = MetallicSilver
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bill & Subscription Reminders", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WarmIvory)
                        Text("Alert before upcoming payment due dates", fontSize = 10.sp, color = MutedPlatinum)
                    }
                    Switch(
                        checked = notifyPaymentReminders,
                        onCheckedChange = { notifyPaymentReminders = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = SophisticatedEmerald)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Night Guard Late-Night Shield Alerts", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WarmIvory)
                        Text("Mindfulness prompt during late-night hours", fontSize = 10.sp, color = MutedPlatinum)
                    }
                    Switch(
                        checked = notifyNightGuardAlert,
                        onCheckedChange = { notifyNightGuardAlert = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = SophisticatedEmerald)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("JARVIS Daily Financial Briefing", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WarmIvory)
                        Text("Morning summary of runway & saved expenses", fontSize = 10.sp, color = MutedPlatinum)
                    }
                    Switch(
                        checked = notifyMorningReview,
                        onCheckedChange = { notifyMorningReview = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = SophisticatedEmerald)
                    )
                }

                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vault Session Lock & Share Privacy", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = WarmIvory)
                        Text("Never expose personal balances on shared streak cards", fontSize = 10.sp, color = MutedPlatinum)
                    }
                    Switch(
                        checked = shareHideBalances,
                        onCheckedChange = {
                            shareHideBalances = it
                            vaultPinEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = ChampagneGold)
                    )
                }
            }

            // ====================================================================
            // SECURITY & PRIVACY (BIOMETRIC FINGERPRINT + 4-DIGIT PIN + FLAG_SECURE)
            // ====================================================================
            Text(
                text = "SECURITY & PRIVACY",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp,
                color = MetallicSilver
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("security_and_privacy_section"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Biometric & App PIN Lock Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAppLockEnabled) Color(0xFFA855F7).copy(alpha = 0.2f)
                                    else Color(0xFF1F1938)
                                )
                                .border(
                                    width = 0.8.dp,
                                    color = if (isAppLockEnabled) Color(0xFFA855F7) else HairlinePlatinum,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric & App PIN Lock",
                                tint = if (isAppLockEnabled) Color(0xFFD8B4FE) else MutedPlatinum,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Biometric & App PIN Lock",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = WarmIvory
                            )
                            Text(
                                text = if (isAppLockEnabled) {
                                    "Active · Fingerprint / Face ID + 4-digit PBKDF2 PIN"
                                } else {
                                    "Require Fingerprint or 4-digit PIN when opening Kharchee"
                                },
                                fontSize = 10.sp,
                                color = if (isAppLockEnabled) SophisticatedEmerald else MutedPlatinum
                            )
                        }
                    }

                    Switch(
                        checked = isAppLockEnabled,
                        onCheckedChange = { wantEnabled ->
                            if (wantEnabled) {
                                if (!hasPinConfigured) {
                                    showSetPinDialog = true
                                } else {
                                    SecurityLockManager.setAppLockEnabled(context, true)
                                    onShowToast("🛡️ Biometric & App PIN Lock Enabled")
                                }
                            } else {
                                SecurityLockManager.setAppLockEnabled(context, false)
                                onShowToast("🔓 App Lock Disabled")
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = Color(0xFFA855F7)
                        ),
                        modifier = Modifier.testTag("biometric_app_pin_lock_switch")
                    )
                }

                // Quick actions when App Lock is enabled or PIN exists
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSetPinDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, Color(0xFFA855F7).copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD8B4FE)),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("change_or_set_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Configure 4-Digit PIN",
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasPinConfigured) "Change 4-Digit PIN" else "Set 4-Digit PIN",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }

                    if (isAppLockEnabled) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                SecurityLockManager.lockNow()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, ChampagneGold.copy(alpha = 0.75f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("lock_vault_now_button")
                        ) {
                            Text(
                                text = "🔒 Lock Vault Now",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

                // 2. Auto-Lock Timeout Option ("Auto-Lock: Immediately on app exit" vs "After 1 minute")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Auto-Lock: Immediately on app exit",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = WarmIvory
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isImmediate = lockTimeoutMillis == SecurityLockManager.TIMEOUT_IMMEDIATE
                        Surface(
                            color = if (isImmediate) Color(0xFFA855F7).copy(alpha = 0.22f) else RichGraphite,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 0.9.dp,
                                color = if (isImmediate) Color(0xFFA855F7) else HairlinePlatinum
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    SecurityLockManager.setLockTimeout(context, SecurityLockManager.TIMEOUT_IMMEDIATE)
                                    onShowToast("⚡ Auto-Lock set to Immediately on app exit")
                                }
                                .testTag("auto_lock_immediately_chip")
                        ) {
                            Text(
                                text = "Immediately on Exit",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isImmediate) Color(0xFFD8B4FE) else MutedPlatinum,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                            )
                        }

                        val isOneMin = lockTimeoutMillis == SecurityLockManager.TIMEOUT_ONE_MINUTE
                        Surface(
                            color = if (isOneMin) Color(0xFFA855F7).copy(alpha = 0.22f) else RichGraphite,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 0.9.dp,
                                color = if (isOneMin) Color(0xFFA855F7) else HairlinePlatinum
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    SecurityLockManager.setLockTimeout(context, SecurityLockManager.TIMEOUT_ONE_MINUTE)
                                    onShowToast("⏱️ Auto-Lock set to 1 minute grace")
                                }
                                .testTag("auto_lock_1min_chip")
                        ) {
                            Text(
                                text = "1 Minute Grace",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOneMin) Color(0xFFD8B4FE) else MutedPlatinum,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

                // 3. Hide Balance on Dashboard (Eye Toggle Masking '••••••')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hide Balance on Dashboard",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Mask Net Balance & burn numbers on Hero Card with '••••••'",
                            fontSize = 10.sp,
                            color = MutedPlatinum
                        )
                    }
                    Switch(
                        checked = settings.stealthMode,
                        onCheckedChange = { hide ->
                            onToggleHideBalance(hide)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = ChampagneGold
                        ),
                        modifier = Modifier.testTag("hide_balance_on_dashboard_switch")
                    )
                }

                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

                // 4. Incognito Screen Privacy (FLAG_SECURE for Recent Apps & Screenshot Blocking)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Incognito Screen Privacy (FLAG_SECURE)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Blank screen in Android Recent Apps switcher & block Khata screenshots",
                            fontSize = 10.sp,
                            color = MutedPlatinum
                        )
                    }
                    Switch(
                        checked = isFlagSecureEnabled || isAppLockEnabled,
                        onCheckedChange = { enabled ->
                            SecurityLockManager.setFlagSecureEnabled(context, enabled)
                            onShowToast(
                                if (enabled) "🛡️ Incognito Screen Privacy (FLAG_SECURE) Active"
                                else "Incognito Screen Privacy Updated"
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = SophisticatedEmerald
                        ),
                        modifier = Modifier.testTag("incognito_flag_secure_switch")
                    )
                }

                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

                // 5. Battery Saver / Reduced Motion (60fps Performance & Static Vector Mode)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Saver / Reduced Motion",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Pause infinite animations & switch gradient blurs to crisp static vector fills for 60fps efficiency",
                            fontSize = 10.sp,
                            color = MutedPlatinum
                        )
                    }
                    Switch(
                        checked = isBatterySaverReducedMotion,
                        onCheckedChange = { enabled ->
                            com.example.util.PerformancePreferences.setBatterySaverReducedMotion(context, enabled)
                            onShowToast(
                                if (enabled) "🔋 Battery Saver / Reduced Motion Active (60fps Static Vectors)"
                                else "✨ Standard Fluid Animations Restored"
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = ChampagneGold
                        ),
                        modifier = Modifier.testTag("battery_saver_reduced_motion_switch")
                    )
                }
            }

            if (showSetPinDialog) {
                SetFourDigitPinDialog(
                    onDismiss = { showSetPinDialog = false },
                    onPinConfigured = {
                        showSetPinDialog = false
                        vaultPinEnabled = true
                        onShowToast("🔐 4-Digit Vault PIN & Biometric App-Lock Activated!")
                    }
                )
            }

            // ====================================================================
            // CLOUD SYNC & BACKUPS (GOOGLE DRIVE APPDATA + AES-256 ENCRYPTED VAULT)
            // ====================================================================
            Text(
                text = "CLOUD SYNC & BACKUPS",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp,
                color = MetallicSilver
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("cloud_sync_and_backups_section"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Enable Google Drive Auto-Sync Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF7C3AED).copy(alpha = 0.22f))
                                .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (driveSyncState.autoSyncEnabled) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = "Google Drive Auto-Sync",
                                tint = Color(0xFFD8B4FE),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Enable Google Drive Auto-Sync",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = WarmIvory
                            )
                            Text(
                                text = "Private AppData folder (Drive.Scope.APPDATA) · Auto-syncs daily on Wi-Fi & on Logout",
                                fontSize = 10.sp,
                                color = MutedPlatinum,
                                lineHeight = 14.sp
                            )
                        }
                    }
                    Switch(
                        checked = driveSyncState.autoSyncEnabled,
                        onCheckedChange = { enabled ->
                            BackupManager.setAutoSyncEnabled(context, enabled, userId)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF7C3AED)
                        ),
                        modifier = Modifier.testTag("enable_google_drive_auto_sync_switch")
                    )
                }

                // 2. Last Sync Label + "Back up Now" Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElevatedGraphite)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Last Sync: ${driveSyncState.lastSyncFormatted}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SophisticatedEmerald,
                            modifier = Modifier.testTag("last_sync_datetime_label")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = driveSyncState.statusMessage,
                            fontSize = 10.sp,
                            color = MutedPlatinum
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val res = BackupManager.syncEncryptedBackupToGoogleDrive(
                                    context = context,
                                    userId = userId,
                                    userEmail = initialProfile.email,
                                    userDisplayName = displayName.ifBlank { "Kharchee Member" },
                                    password = backupPassword,
                                    liveExpenses = liveExpenses,
                                    liveGoals = liveGoals,
                                    liveKhataFriends = liveKhataFriends,
                                    liveReminders = liveReminders,
                                    liveSettings = settings
                                )
                                res.onSuccess { st ->
                                    localBackupStatusBanner = "✓ Synced ${BackupManager.BACKUP_FILE_NAME} (${st.latestBackupTxCount} txns)"
                                    onShowToast("✅ Encrypted backup synced to Google Drive AppData!")
                                }
                            }
                        },
                        enabled = !driveSyncState.isSyncing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("drive_backup_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Back up Now",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (driveSyncState.isSyncing) "Syncing..." else "Back up Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

                // 3. Encrypted Local File Export / Import (kharchee_secure_backup.enc)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AES-256-GCM ENCRYPTED FILE (${BackupManager.BACKUP_FILE_NAME})",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                }

                OutlinedTextField(
                    value = backupPassword,
                    onValueChange = { backupPassword = it },
                    label = { Text("Backup Password (Optional · AES-256 Key)", fontSize = 11.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backup_encryption_password_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val res = BackupManager.createEncryptedBackupFile(
                                    context = context,
                                    userId = userId,
                                    userEmail = initialProfile.email,
                                    userDisplayName = displayName.ifBlank { "Kharchee Member" },
                                    password = backupPassword,
                                    liveExpenses = liveExpenses,
                                    liveGoals = liveGoals,
                                    liveKhataFriends = liveKhataFriends,
                                    liveReminders = liveReminders,
                                    liveSettings = settings
                                )
                                res.onSuccess { (file, snap) ->
                                    localBackupStatusBanner = "✓ Saved encrypted ${file.name} (${snap.expenses.size} txns, ₹${snap.netBalanceAmount})"
                                    try {
                                        exportBackupDocumentLauncher.launch(BackupManager.BACKUP_FILE_NAME)
                                    } catch (_: Exception) {
                                        BackupManager.createShareEncryptedBackupIntent(context, file)?.let { shareIntent ->
                                            runCatching {
                                                context.startActivity(Intent.createChooser(shareIntent, "Export ${BackupManager.BACKUP_FILE_NAME}"))
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.9.dp, ChampagneGold.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmIvory),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("export_encrypted_backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Encrypted Backup",
                            tint = ChampagneGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup to .enc", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            val localFile = BackupManager.getPrimaryBackupFile(context)
                            if (localFile.exists() && localFile.length() > 0L) {
                                // Restore directly from local/Drive cached encrypted file, or let user pick a file
                                coroutineScope.launch {
                                    val res = BackupManager.restoreFromEncryptedFile(
                                        context = context,
                                        file = localFile,
                                        targetUserId = userId,
                                        password = backupPassword,
                                        mergeWithExisting = mergeOnRestore
                                    )
                                    res.fold(
                                        onSuccess = { snap ->
                                            onBackupRestored(snap, mergeOnRestore)
                                            val msg = "✅ Restored ₹${snap.netBalanceAmount} balance & ${snap.expenses.size} txns from ${BackupManager.BACKUP_FILE_NAME}!"
                                            localBackupStatusBanner = msg
                                            onShowToast(msg)
                                        },
                                        onFailure = {
                                            runCatching {
                                                restoreBackupDocumentLauncher.launch(arrayOf("*/*"))
                                            }
                                        }
                                    )
                                }
                            } else {
                                runCatching {
                                    restoreBackupDocumentLauncher.launch(arrayOf("*/*"))
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.9.dp, SophisticatedEmerald.copy(alpha = 0.75f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SophisticatedEmerald),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("restore_from_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Restore from File",
                            tint = SophisticatedEmerald,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore from File", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1)
                    }
                }

                if (localBackupStatusBanner != null) {
                    Text(
                        text = localBackupStatusBanner!!,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = SophisticatedEmerald,
                        modifier = Modifier.testTag("backup_restore_status_banner")
                    )
                }
            }

            // ====================================================================
            // NON-INTRUSIVE "KHARCHEE SOVEREIGN" OPTIONAL LIFETIME UPGRADE
            // ====================================================================
            Text(
                text = "KHARCHEE SOVEREIGN (OPTIONAL LUXURY PERKS)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp,
                color = ChampagneGold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, ChampagneGold.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("settings_kharchee_sovereign_card"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kharchee Sovereign Lifetime",
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = WarmIvory
                    )
                    Surface(
                        color = ChampagneGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, ChampagneGold.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "₹499 (One-Time)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ChampagneGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "All core tracking, Khata ledger, 1-tap UPI, SMS auto-detection, encrypted Drive backups, Biometric lock & Jarvis queries are 100% Free Forever. Optional Sovereign support unlocks:",
                    fontSize = 11.sp,
                    color = MutedPlatinum,
                    lineHeight = 15.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "✦ Custom Neon App Icons",
                        "✦ Automated Tax/GST PDF & Excel Reports",
                        "✦ Jarvis Voice Audio Mode",
                        "✦ Custom Obsidian/Gold Card Themes"
                    ).forEach { perk ->
                        Text(
                            text = perk,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }

                if (onOpenSovereignTab != null) {
                    OutlinedButton(
                        onClick = { onOpenSovereignTab() },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, ChampagneGold.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("open_sovereign_tab_from_settings_button")
                    ) {
                        Text(
                            text = "Explore Kharchee Sovereign Perks →",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Save Profile & Preferences Button
            Button(
                onClick = {
                    val parsedDaily = dailyLimitStr.toLongOrNull()?.coerceIn(50L, 500000L) ?: settings.dailyLimit
                    val parsedMonthly = monthlyBudgetStr.toLongOrNull()?.coerceAtLeast(1000L) ?: (parsedDaily * 30L)
                    KharcheeProfileAndToolsStore.saveProfileMetadata(
                        context = context,
                        userId = userId,
                        displayName = displayName.ifBlank { "Kharchee Member" },
                        phone = phone,
                        profilePhotoUri = photoUri,
                        monthlyBudget = parsedMonthly,
                        primaryWallet = primaryWallet,
                        vaultPinProtectionEnabled = vaultPinEnabled,
                        sharePrivacyHideBalances = shareHideBalances
                    )
                    ThemePreferenceManager.saveThemePreference(context, selectedThemeId)
                    onSaveProfileAndSettings(
                        displayName.ifBlank { "Kharchee Member" },
                        phone,
                        photoUri,
                        parsedDaily,
                        parsedMonthly,
                        primaryWallet,
                        upiId,
                        selectedThemeId,
                        notifyPaymentReminders,
                        notifyNightGuardAlert,
                        notifyMorningReview
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_save_button")
            ) {
                Text("Save Profile, Security & Theme Preferences", fontWeight = FontWeight.SemiBold)
            }

            if (onOpenWalkthrough != null) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenWalkthrough()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.9.dp, ChampagneGold.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmIvory),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_rewatch_tutorial_button")
                ) {
                    Text(
                        text = "App Walkthrough (Re-watch Interactive Tutorial)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            // Sign Out & Account Deletion Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        BackupManager.triggerAutoBackupOnLogout(
                            context = context,
                            userId = userId,
                            userEmail = initialProfile.email,
                            userDisplayName = displayName
                        )
                        onDismiss()
                        onSignOut()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, HairlinePlatinum),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmIvory),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("profile_sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, CrimsonRuby.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRuby),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("profile_delete_account_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Account",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Account", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = RichGraphite,
            title = {
                Text(
                    text = "Delete KHARCHEE Account?",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    color = CrimsonRuby
                )
            },
            text = {
                Text(
                    text = "This will permanently delete your saved transactions, Khata ledgers, savings goals, reminders, and profile settings from the vault and sign you out. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedPlatinum
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        KharcheeProfileAndToolsStore.clearAllForUser(context, userId)
                        onDismiss()
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRuby,
                        contentColor = WarmIvory
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("Permanently Delete & Sign Out", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = MutedPlatinum)
                }
            }
        )
    }
}
