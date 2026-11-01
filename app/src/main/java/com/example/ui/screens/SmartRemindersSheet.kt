package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EXPENSE_CATEGORIES
import com.example.data.model.NightGuardSchedule
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.REMINDER_INTERVAL_OPTIONS
import com.example.data.model.UserSettings
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import com.example.util.KharcheeNotificationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPaymentRemindersBottomSheet(
    settings: UserSettings,
    reminders: List<PaymentReminderRecord>,
    onDismiss: () -> Unit,
    onCreateReminder: (title: String, amount: Long, category: String, dueEpochMillis: Long, intervalType: String, note: String) -> Unit,
    onMarkReminderPaid: (PaymentReminderRecord, Boolean) -> Unit,
    onDeleteReminder: (PaymentReminderRecord) -> Unit,
    onTogglePreSpendConfirm: (Boolean) -> Unit,
    onUpdateNotificationPrefs: (paymentReminders: Boolean, nightGuardAlert: Boolean, morningReview: Boolean) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var hasNotifPermission by remember {
        mutableStateOf(KharcheeNotificationManager.hasNotificationPermission(context))
    }
    var permissionDeniedNote by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        permissionDeniedNote = !granted
    }

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Bills") }
    var selectedDueDaysOffset by remember { mutableIntStateOf(1) }
    var selectedInterval by remember { mutableStateOf("1_day_before") }
    var note by remember { mutableStateOf("") }

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

    val dateFmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH) }

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
                .testTag("smart_reminders_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SMART PAYMENT REMINDERS & MINDFULNESS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = ChampagneGold
                    )
                    Text(
                        text = "Scheduled Bills & Pre-Spend Guard",
                        fontFamily = headingFont,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedPlatinum)
                }
            }

            // Notification Permission Status Card
            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ENABLE SYSTEM NOTIFICATION ALERTS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChampagneGold
                    )
                    Text(
                        text = if (permissionDeniedNote) {
                            "Notification permission is currently disabled. Reminders will still appear inside your Kharchee Dashboard."
                        } else {
                            "Allow system notifications to receive timely bill due alerts, Night Guard activation notices, and morning reviews."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory
                    )
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBlack
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("grant_notifications_permission_button")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Allow Notifications", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }

            // Pre-Spend Confirmation Toggle Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElevatedGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pre-Spend Confirmation Prompt",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmIvory
                    )
                    Text(
                        text = "Ask 'Is this necessary?' & show budget/goal impact before logging discretionary expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedIvory,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = settings.enablePreSpendConfirm,
                    onCheckedChange = onTogglePreSpendConfirm,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = SophisticatedEmerald
                    ),
                    modifier = Modifier.testTag("toggle_pre_spend_confirm_switch")
                )
            }

            // Add New Payment Reminder Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, HairlineGold, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SCHEDULE NEW PAYMENT REMINDER",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChampagneGold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Bill / Payment Title") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("reminder_title_input")
                    )
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reminder_amount_input")
                    )
                }

                Text(
                    text = "CATEGORY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MutedPlatinum
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EXPENSE_CATEGORIES.forEach { cat ->
                        val active = selectedCategory == cat
                        Surface(
                            color = if (active) ChampagneGold.copy(alpha = 0.16f) else ElevatedGraphite,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(50))
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (active) ChampagneGold else WarmIvory,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "DUE DATE TIMING",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MutedPlatinum
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        1 to "Tomorrow",
                        3 to "In 3 Days",
                        7 to "In 7 Days",
                        15 to "In 15 Days"
                    ).forEach { (days, label) ->
                        val active = selectedDueDaysOffset == days
                        Surface(
                            color = if (active) SophisticatedEmerald.copy(alpha = 0.16f) else ElevatedGraphite,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(0.8.dp, if (active) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                                .clickable { selectedDueDaysOffset = days }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (active) SophisticatedEmerald else WarmIvory,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = "REMINDER INTERVAL",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = MutedPlatinum
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    REMINDER_INTERVAL_OPTIONS.forEach { (key, label) ->
                        val active = selectedInterval == key
                        Surface(
                            color = if (active) ChampagneGold.copy(alpha = 0.16f) else ElevatedGraphite,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(50))
                                .clickable { selectedInterval = key }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (active) ChampagneGold else WarmIvory,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val amt = amountStr.toLongOrNull() ?: 0L
                        if (amt > 0L) {
                            val dueMillis = System.currentTimeMillis() + (selectedDueDaysOffset.toLong() * 86_400_000L)
                            onCreateReminder(
                                title.ifBlank { "$selectedCategory Bill" },
                                amt,
                                selectedCategory,
                                dueMillis,
                                selectedInterval,
                                note
                            )
                            title = ""
                            amountStr = ""
                            note = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_payment_reminder_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Scheduled Reminder", fontWeight = FontWeight.SemiBold)
                }
            }

            // Existing Payment Reminders List
            Text(
                text = "SCHEDULED REMINDERS (${reminders.count { !it.isPaid }} UPCOMING)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = SophisticatedEmerald
            )

            if (reminders.isEmpty()) {
                Text(
                    text = "No scheduled payment reminders yet. Add a recurring bill or postponed expense above.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedIvory
                )
            } else {
                reminders.forEach { reminder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElevatedGraphite)
                            .border(
                                0.8.dp,
                                if (reminder.isPaid) SophisticatedEmerald.copy(alpha = 0.4f) else HairlinePlatinum,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = reminder.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (reminder.isPaid) MutedPlatinum else WarmIvory
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (reminder.isPaid) SophisticatedEmerald.copy(alpha = 0.15f) else ChampagneGold.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = if (reminder.isPaid) "PAID" else reminder.intervalType.replace("_", " ").uppercase(),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (reminder.isPaid) SophisticatedEmerald else ChampagneGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${reminder.category} · Due ${dateFmt.format(Date(reminder.dueEpochMillis))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedIvory,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${reminder.amount}",
                                fontFamily = headingFont,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (reminder.isPaid) SophisticatedEmerald else ChampagneGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onMarkReminderPaid(reminder, !reminder.isPaid) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Mark Paid",
                                    tint = if (reminder.isPaid) SophisticatedEmerald else MutedPlatinum,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteReminder(reminder) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Reminder",
                                    tint = CrimsonRuby.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)

            // Notification Channels Toggles
            Text(
                text = "NOTIFICATION PREFERENCES",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MutedPlatinum
            )

            NotificationPrefSwitchRow(
                title = "Upcoming Bill & Payment Reminders",
                subtitle = "Notify on scheduled due dates",
                checked = settings.notifyPaymentReminders,
                onCheckedChange = {
                    onUpdateNotificationPrefs(it, settings.notifyNightGuardAlert, settings.notifyMorningReview)
                }
            )
            NotificationPrefSwitchRow(
                title = "Night Guard Activation Alert",
                subtitle = "Remind when Night Guard window begins",
                checked = settings.notifyNightGuardAlert,
                onCheckedChange = {
                    onUpdateNotificationPrefs(settings.notifyPaymentReminders, it, settings.notifyMorningReview)
                }
            )
            NotificationPrefSwitchRow(
                title = "Morning Financial Review",
                subtitle = "8:30 AM summary of postponed items & daily reserve",
                checked = settings.notifyMorningReview,
                onCheckedChange = {
                    onUpdateNotificationPrefs(settings.notifyPaymentReminders, settings.notifyNightGuardAlert, it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationPrefSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = WarmIvory)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MutedIvory, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = ChampagneGold
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NightGuardSettingsBottomSheet(
    settings: UserSettings,
    onDismiss: () -> Unit,
    onSaveNightGuardConfig: (enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
    onLaunchNightGuardPreview: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var enabled by remember(settings.nightGuardEnabled) { mutableStateOf(settings.nightGuardEnabled) }
    var startHour by remember(settings.nightGuardStartHour) { mutableIntStateOf(settings.nightGuardStartHour) }
    var endHour by remember(settings.nightGuardEndHour) { mutableIntStateOf(settings.nightGuardEndHour) }

    val startOptions = listOf(20, 21, 22, 23, 0)
    val endOptions = listOf(4, 5, 6, 7, 8)

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
                .testTag("night_guard_settings_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SophisticatedEmerald.copy(alpha = 0.15f))
                            .border(0.8.dp, SophisticatedEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = "Night Guard",
                            tint = SophisticatedEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NIGHT GUARD IMPULSE PROTECTION",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SophisticatedEmerald
                        )
                        Text(
                            text = "Ruko Zara · Late-Night Shield",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmIvory
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedPlatinum)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElevatedGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Night Guard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmIvory
                    )
                    Text(
                        text = "Intercept discretionary expenses during late-night hours with a brain diversion challenge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedIvory
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = SophisticatedEmerald
                    ),
                    modifier = Modifier.testTag("night_guard_enable_switch")
                )
            }

            Text(
                text = "START TIME (LOCAL TIMEZONE)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = ChampagneGold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                startOptions.forEach { hr ->
                    val active = startHour == hr
                    Surface(
                        color = if (active) ChampagneGold.copy(alpha = 0.18f) else ObsidianBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, if (active) ChampagneGold else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { startHour = hr }
                    ) {
                        Text(
                            text = NightGuardSchedule.formatHourMinute12h(hr, 0),
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (active) ChampagneGold else WarmIvory,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = "END TIME (MORNING UNLOCK)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = SophisticatedEmerald
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                endOptions.forEach { hr ->
                    val active = endHour == hr
                    Surface(
                        color = if (active) SophisticatedEmerald.copy(alpha = 0.18f) else ObsidianBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, if (active) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                            .clickable { endHour = hr }
                    ) {
                        Text(
                            text = NightGuardSchedule.formatHourMinute12h(hr, 0),
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (active) SophisticatedEmerald else WarmIvory,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    onDismiss()
                    onLaunchNightGuardPreview()
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, SophisticatedEmerald),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SophisticatedEmerald),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_night_guard_challenge_button")
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Late-Night Purchase & Test Challenge", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    onSaveNightGuardConfig(enabled, startHour, 0, endHour, 0)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_night_guard_settings_button")
            ) {
                Text("Save Night Guard Schedule", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
