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
import com.example.data.model.KharcheeProfileAndToolsStore
import com.example.data.model.PAYMENT_MODES
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
import com.example.ui.theme.ThemePreferenceManager
import com.example.ui.theme.WarmIvory

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
    onDeleteAccount: () -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var displayName by remember(initialProfile) { mutableStateOf(initialProfile.customDisplayName) }
    var phone by remember(initialProfile) { mutableStateOf(initialProfile.phone) }
    var photoUri by remember(initialProfile) { mutableStateOf(initialProfile.profilePhotoUri) }
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

            // Three Independent Themes Selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Theme Preferences",
                    tint = ChampagneGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "THREE INDEPENDENT THEMES",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp,
                    color = ChampagneGold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ALL_HUD_THEMES.forEach { themeOption ->
                    val isSelected = selectedThemeId == themeOption.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) ElevatedGraphite else ObsidianBlack)
                            .border(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) themeOption.accent else HairlinePlatinum,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                selectedThemeId = themeOption.id
                                ThemePreferenceManager.saveThemePreference(context, themeOption.id)
                            }
                            .padding(12.dp)
                            .testTag("profile_theme_${themeOption.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(themeOption.background)
                                .border(1.dp, themeOption.accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = themeOption.symbol, color = themeOption.accent, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = themeOption.name,
                                fontFamily = headingFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = WarmIvory
                            )
                            Text(
                                text = themeOption.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MutedIvory
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected Theme",
                                tint = themeOption.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

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

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            // Sign Out & Account Deletion Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
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
