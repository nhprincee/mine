package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receiver.AutoExpenseDetectionStore
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.util.BackupManager
import com.example.util.BackupVaultSnapshot
import com.example.util.DetectedBackupMetadata
import java.io.File
import kotlinx.coroutines.launch

data class KharcheeTutorialStepSpec(
    val stepNumber: Int,
    val title: String,
    val highlightBadge: String,
    val subtext: String,
    val icon: ImageVector,
    val accentColor: Color
)

private val KHARCHEE_TUTORIAL_STEPS = listOf(
    KharcheeTutorialStepSpec(
        stepNumber = 1,
        title = "Master Your Cashflow",
        highlightBadge = "CRED OBSIDIAN TITANIUM CARD & NET BALANCE",
        subtext = "Track your income, daily burn rate, and financial health score in one place.",
        icon = Icons.Default.AccountBalanceWallet,
        accentColor = Color(0xFFA855F7)
    ),
    KharcheeTutorialStepSpec(
        stepNumber = 2,
        title = "Advanced Khata Ledger",
        highlightBadge = "LENA & DENA PEER LEDGER SYSTEM",
        subtext = "Track micro-loans, peer dues, and settle balances with 1-tap reminders.",
        icon = Icons.Default.MenuBook,
        accentColor = Color(0xFF2EE59D)
    ),
    KharcheeTutorialStepSpec(
        stepNumber = 3,
        title = "Jarvis Financial Intelligence",
        highlightBadge = "ARC REACTOR FLOATING ORB TRIGGER",
        subtext = "Tap the Arc Core anytime for real-time spend audits, budget roasts, and smart advice.",
        icon = Icons.Default.AutoAwesome,
        accentColor = Color(0xFF38BDF8)
    ),
    KharcheeTutorialStepSpec(
        stepNumber = 4,
        title = "Next-Gen Insight Analytics",
        highlightBadge = "HOLOGRAPHIC SPLINES & 3D CATEGORY DONUTS",
        subtext = "Deep biometric spend radar and predictive month-end runway projections.",
        icon = Icons.Default.BarChart,
        accentColor = Color(0xFFD8B4FE)
    )
)

/**
 * Interactive 4-Step App Walkthrough + 2-Step Cold Start Setup Flow for First-Time Users.
 * Rendered in KHARCHEE's signature Deep Dark Navy (#05050A) & Electric Purple (#7C3AED) theme.
 */
@Composable
fun KharcheeFirstLaunchWalkthroughAndSetupOverlay(
    initialUserName: String,
    initialMonthlyBudget: Long,
    initialCurrency: String = "INR (₹)",
    showColdStartSetupAfterTutorial: Boolean = true,
    userId: String = "default",
    userEmail: String = "",
    onBackupRestored: (BackupVaultSnapshot) -> Unit = {},
    onDismissOrSkipToZeroStart: () -> Unit,
    onCompleteColdStartSetup: (
        userName: String,
        openingBalance: Long,
        monthlyBudgetTarget: Long,
        preferredCurrency: String,
        notifyPaymentReminders: Boolean,
        notifyNightGuardAlert: Boolean,
        notifyMorningReview: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val coroutineScope = rememberCoroutineScope()

    // phase 0 = 4-Step Interactive Walkthrough; phase 1 = 2-Step Cold Start Setup Sheet
    var activePhase by remember { mutableIntStateOf(0) }
    var tutorialStepIndex by remember { mutableIntStateOf(0) }
    var setupSubStep by remember { mutableIntStateOf(0) } // 0 = Step A (User & Opening Balance), 1 = Step B (Currency & Alerts)

    var detectedBackup by remember { mutableStateOf<DetectedBackupMetadata?>(null) }
    var isRestoringBackup by remember { mutableStateOf(false) }

    LaunchedEffect(userId, userEmail) {
        detectedBackup = BackupManager.scanGoogleDriveForPreviousBackup(
            context = context,
            userId = userId,
            userEmail = userEmail
        )
    }

    var nameInput by remember(initialUserName) {
        mutableStateOf(if (initialUserName.equals("Kharchee Member", ignoreCase = true)) "" else initialUserName)
    }
    var openingBalanceInput by remember { mutableStateOf("0") }
    var monthlyBudgetInput by remember(initialMonthlyBudget) {
        mutableStateOf(if (initialMonthlyBudget > 0L) initialMonthlyBudget.toString() else "")
    }
    var selectedCurrency by remember(initialCurrency) { mutableStateOf(initialCurrency) }
    var notifyPaymentReminders by remember { mutableStateOf(true) }
    var notifyNightGuard by remember { mutableStateOf(true) }
    var notifyMorningReview by remember { mutableStateOf(true) }

    var smsPermissionGranted by remember {
        mutableStateOf(AutoExpenseDetectionStore.hasSmsPermissions(context))
    }
    var smsPermissionFallbackNotice by remember { mutableStateOf<String?>(null) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        smsPermissionGranted = isGranted
        if (isGranted) {
            AutoExpenseDetectionStore.setSmsRationaleDismissed(context, true)
            smsPermissionFallbackNotice = "⚡ Real-Time SMS Auto-Track Enabled!"
        } else {
            AutoExpenseDetectionStore.setSmsRationaleDismissed(context, true)
            smsPermissionFallbackNotice = "📋 SMS permission skipped — Smart Clipboard Auto-Detection enabled as zero-permission fallback."
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFA855F7),
        unfocusedBorderColor = Color(0xFF33265A),
        focusedContainerColor = Color(0xFF0A0A14),
        unfocusedContainerColor = Color(0xFF0A0A14),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color(0xFFD8B4FE),
        unfocusedLabelColor = Color(0xFFB8B0D4)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05050A).copy(alpha = 0.86f))
            .padding(horizontal = 18.dp, vertical = 24.dp)
            .testTag("kharchee_first_launch_walkthrough_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(28.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFF7C3AED))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF140E2A),
                            Color(0xFF0B0818),
                            Color(0xFF05050A)
                        )
                    )
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFA855F7),
                            Color(0xFF7C3AED),
                            Color(0xFFD8B4FE)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Brand Bar + Instant Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A0A12))
                            .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        KharcheeArchitecturalAMark(size = 18.dp)
                    }
                    Column {
                        Text(
                            text = "KHARCHEE",
                            fontFamily = headingFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (activePhase == 0) {
                                "INTERACTIVE APP WALKTHROUGH (${tutorialStepIndex + 1}/4)"
                            } else {
                                "INITIAL COLD-START VAULT SETUP (${setupSubStep + 1}/2)"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }

                IconButton(
                    onClick = onDismissOrSkipToZeroStart,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1433))
                        .testTag("tutorial_close_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Walkthrough",
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // First-Time User Google Drive & Encrypted Vault Restore Detector Card
            detectedBackup?.let { backup ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF071B15))
                        .border(1.2.dp, Color(0xFF2EE59D).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("cold_start_restore_detected_card"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Restore Detected",
                            tint = Color(0xFF2EE59D),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "GOOGLE DRIVE CLOUD BACKUP DETECTED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFF2EE59D)
                        )
                    }
                    Text(
                        text = "Restore detected! We found a previous backup from ${backup.lastDateFormatted}. Restoring will retrieve your ₹${backup.netBalanceAmount} balance and transactions.",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Button(
                        onClick = {
                            if (!isRestoringBackup) {
                                isRestoringBackup = true
                                coroutineScope.launch {
                                    val result = BackupManager.restoreFromEncryptedFile(
                                        context = context,
                                        file = File(backup.encryptedFilePath),
                                        targetUserId = userId,
                                        password = "",
                                        mergeWithExisting = false
                                    )
                                    isRestoringBackup = false
                                    result.onSuccess { snapshot ->
                                        onBackupRestored(snapshot)
                                        onDismissOrSkipToZeroStart()
                                    }
                                }
                            }
                        },
                        enabled = !isRestoringBackup,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2EE59D),
                            contentColor = Color(0xFF040508)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("onboarding_restore_now_button")
                    ) {
                        Text(
                            text = if (isRestoringBackup) "Restoring Encrypted Vault..." else "Restore Now",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (activePhase == 0) {
                // ============================================================
                // PHASE 1: 4-STEP INTERACTIVE TUTORIAL WALKTHROUGH
                // ============================================================
                val step = KHARCHEE_TUTORIAL_STEPS[tutorialStepIndex.coerceIn(0, KHARCHEE_TUTORIAL_STEPS.lastIndex)]

                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tutorialStepContent"
                ) { currentStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF100B22))
                            .border(1.dp, currentStep.accentColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(currentStep.accentColor.copy(alpha = 0.18f))
                                    .border(1.dp, currentStep.accentColor, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentStep.icon,
                                    contentDescription = currentStep.title,
                                    tint = currentStep.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    color = currentStep.accentColor.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(0.8.dp, currentStep.accentColor.copy(alpha = 0.65f))
                                ) {
                                    Text(
                                        text = "STEP ${currentStep.stepNumber} OF 4 · ${currentStep.highlightBadge}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentStep.accentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = currentStep.title,
                                    fontFamily = headingFont,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = currentStep.subtext,
                            fontSize = 13.sp,
                            color = Color(0xFFEAE4F8),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Bottom Controls: "Skip Tutorial" (Left) + Progress Dots (Center) + "Next" / "Get Started" (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (showColdStartSetupAfterTutorial) {
                                activePhase = 1
                                setupSubStep = 0
                            } else {
                                onDismissOrSkipToZeroStart()
                            }
                        },
                        modifier = Modifier.testTag("tutorial_skip_button")
                    ) {
                        Text(
                            text = "Skip Tutorial",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB8B0D4),
                            maxLines = 1
                        )
                    }

                    // Horizontal Progress Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KHARCHEE_TUTORIAL_STEPS.forEachIndexed { idx, _ ->
                            val isSelected = idx == tutorialStepIndex
                            Box(
                                modifier = Modifier
                                    .height(7.dp)
                                    .width(if (isSelected) 22.dp else 7.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) Color(0xFFA855F7) else Color(0xFF33265A)
                                    )
                                    .clickable { tutorialStepIndex = idx }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (tutorialStepIndex < KHARCHEE_TUTORIAL_STEPS.lastIndex) {
                                tutorialStepIndex += 1
                            } else if (showColdStartSetupAfterTutorial) {
                                activePhase = 1
                                setupSubStep = 0
                            } else {
                                onDismissOrSkipToZeroStart()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("tutorial_next_button")
                    ) {
                        Text(
                            text = if (tutorialStepIndex < KHARCHEE_TUTORIAL_STEPS.lastIndex) "Next" else "Get Started",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            } else {
                // ============================================================
                // PHASE 2: 2-STEP FIRST-LAUNCH ONBOARDING SETUP SHEET (COLD START FORM)
                // ============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Step A · Balance & Budget", "Step B · Currency & Alerts").forEachIndexed { idx, label ->
                        val active = setupSubStep == idx
                        Surface(
                            color = if (active) Color(0xFF2B185E) else Color(0xFF100B22),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (active) Color(0xFFA855F7) else Color(0xFF281B4D)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { setupSubStep = idx }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (active) Color.White else Color(0xFFB8B0D4),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                if (setupSubStep == 0) {
                    // STEP A: User Details, Opening Balance & Monthly Budget Target
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Configure Your Opening Vault Balance",
                            fontFamily = headingFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Start with ₹0 or enter your current Bank/Cash balance to record your first baseline transaction.",
                            fontSize = 12.sp,
                            color = Color(0xFFB8B0D4),
                            lineHeight = 17.sp
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Your Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cold_start_name_input")
                        )

                        OutlinedTextField(
                            value = openingBalanceInput,
                            onValueChange = { openingBalanceInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Current Bank / Cash Opening Balance (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cold_start_opening_balance_input")
                        )

                        // Quick presets for Opening Balance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0L to "₹0", 5000L to "₹5,000", 10000L to "₹10,000", 25000L to "₹25,000").forEach { (amt, label) ->
                                val selected = (openingBalanceInput.toLongOrNull() ?: 0L) == amt
                                Surface(
                                    color = if (selected) Color(0xFF7C3AED).copy(alpha = 0.28f) else Color(0xFF100B22),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.8.dp, if (selected) Color(0xFFA855F7) else Color(0xFF281B4D)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { openingBalanceInput = amt.toString() }
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else Color(0xFFD8B4FE),
                                        maxLines = 1,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 6.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = monthlyBudgetInput,
                            onValueChange = { monthlyBudgetInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Monthly Budget Target (Optional ₹ Limit)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cold_start_monthly_budget_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onDismissOrSkipToZeroStart,
                                modifier = Modifier.testTag("cold_start_skip_zero_button")
                            ) {
                                Text(
                                    text = "Start with ₹0",
                                    fontSize = 12.sp,
                                    color = Color(0xFFB8B0D4)
                                )
                            }

                            Button(
                                onClick = { setupSubStep = 1 },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7C3AED),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("cold_start_next_step_button")
                            ) {
                                Text("Next: Currency & Alerts →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // STEP B: Preferred Currency, Auto-Track SMS Permission Rationale & Notification Alerts
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Preferred Currency & Smart Alerts",
                            fontFamily = headingFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("INR (₹)", "USD ($)", "EUR (€)", "GBP (£)").forEach { curr ->
                                val active = selectedCurrency == curr
                                Surface(
                                    color = if (active) Color(0xFF7C3AED).copy(alpha = 0.28f) else Color(0xFF100B22),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (active) Color(0xFFA855F7) else Color(0xFF281B4D)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedCurrency = curr }
                                ) {
                                    Text(
                                        text = curr,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else Color(0xFFD8B4FE),
                                        maxLines = 1,
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp)
                                    )
                                }
                            }
                        }

                        // Runtime SMS Auto-Track Permission Rationale Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF160E30))
                                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                                .testTag("onboarding_sms_permission_rationale_card"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF7C3AED).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = "Auto-Track SMS",
                                        tint = Color(0xFFD8B4FE),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚡ Auto-Track Spends (Zero Manual Typing)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Auto-Track Spends: Allow Kharchee to read transactional SMS so you never have to log expenses manually.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD8B4FE),
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            if (smsPermissionFallbackNotice != null) {
                                Text(
                                    text = smsPermissionFallbackNotice!!,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (smsPermissionGranted) Color(0xFF2EE59D) else Color(0xFFFBBF24)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (smsPermissionGranted) Color(0xFF059669) else Color(0xFF7C3AED),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("onboarding_allow_sms_permission_button")
                                ) {
                                    Text(
                                        text = if (smsPermissionGranted) "✓ SMS Auto-Track Active" else "Allow Auto-Track SMS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        AutoExpenseDetectionStore.setSmsRationaleDismissed(context, true)
                                        smsPermissionFallbackNotice = "📋 Using Smart Clipboard Auto-Detection fallback."
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.8.dp, Color(0xFF3B2C69)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("onboarding_use_clipboard_fallback_button")
                                ) {
                                    Text(
                                        text = "Use Clipboard",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB8B0D4),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF100B22))
                                .border(0.8.dp, Color(0xFF281B4D), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bill & Khata Due Reminders", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                                    Text("Notify before upcoming bills & peer settlements", fontSize = 10.sp, color = Color(0xFFB8B0D4))
                                }
                                Switch(
                                    checked = notifyPaymentReminders,
                                    onCheckedChange = { notifyPaymentReminders = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF7C3AED))
                                )
                            }

                            HorizontalDivider(color = Color(0xFF231742), thickness = 0.6.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Night Guard Late-Night Shield", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                                    Text("Prevent late-night impulse UPI spending", fontSize = 10.sp, color = Color(0xFFB8B0D4))
                                }
                                Switch(
                                    checked = notifyNightGuard,
                                    onCheckedChange = { notifyNightGuard = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF7C3AED))
                                )
                            }

                            HorizontalDivider(color = Color(0xFF231742), thickness = 0.6.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("JARVIS Daily Cashflow Briefing", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                                    Text("Daily safe burn rate & runway summary", fontSize = 10.sp, color = Color(0xFFB8B0D4))
                                }
                                Switch(
                                    checked = notifyMorningReview,
                                    onCheckedChange = { notifyMorningReview = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF7C3AED))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { setupSubStep = 0 },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFF33265A)),
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Text("Back", color = Color(0xFFD8B4FE), fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val cleanName = nameInput.trim().ifBlank { initialUserName.ifBlank { "Kharchee Member" } }
                                    val parsedOpeningBalance = openingBalanceInput.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                                    val parsedMonthlyBudget = monthlyBudgetInput.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                                    onCompleteColdStartSetup(
                                        cleanName,
                                        parsedOpeningBalance,
                                        parsedMonthlyBudget,
                                        selectedCurrency,
                                        notifyPaymentReminders,
                                        notifyNightGuard,
                                        notifyMorningReview
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7C3AED),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(0.6f)
                                    .testTag("cold_start_complete_setup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Complete Setup",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complete Setup", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
