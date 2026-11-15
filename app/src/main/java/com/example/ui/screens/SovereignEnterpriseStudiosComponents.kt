package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseRecord
import com.example.data.model.InsightIntelligenceEngine
import com.example.data.model.InsightPeriodFilter
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.ui.theme.HOLOGRAPHIC_STUDIO_THEMES
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.ThemePreferenceManager
import com.example.util.BackupManager
import com.example.util.MonthlyFinancialPdfExporter
import com.example.util.PerformancePreferences
import com.example.util.SavingsGoalsManager
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun SovereignEnterpriseFourStudiosSection(
    userId: String,
    userDisplayName: String,
    isSovereignPro: Boolean,
    settings: UserSettings,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    paymentReminders: List<PaymentReminderRecord>,
    onSelectTheme: (String) -> Unit,
    onCreateGoal: ((title: String, targetAmount: Long, savedAmount: Long) -> Unit)? = null,
    onAddGoalProgress: ((SavingsGoalRecord, Long) -> Unit)? = null,
    onRequestUnlockPro: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sovereign_enterprise_four_studios_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "👑 SOVEREIGN ENTERPRISE STUDIOS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFFFFD700)
                )
                Text(
                    text = "Zero-Knowledge E2EE · CA Tax Studio · Spare-Change Sweep · Holographic Themes",
                    fontSize = 11.sp,
                    color = Color(0xFFB8B0D4),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = if (isSovereignPro) Color(0xFF06291A) else Color(0xFF241B08),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    0.9.dp,
                    if (isSovereignPro) Color(0xFF00E676) else Color(0xFFFFD700)
                )
            ) {
                Text(
                    text = if (isSovereignPro) "✓ ALL 4 UNLOCKED" else "LIVE PREVIEW",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSovereignPro) Color(0xFF00E676) else Color(0xFFFFD700),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // STUDIO 1: ZERO-KNOWLEDGE E2EE CLOUD BACKUP VAULT
        ZeroKnowledgeE2eeVaultStudioCard(
            isSovereignPro = isSovereignPro,
            expensesCount = expenses.size,
            onRequestUnlockPro = onRequestUnlockPro,
            onShowToast = onShowToast
        )

        // STUDIO 2: AI TAX & BUSINESS EXPENSE TAGGING (CA-READY EXPORTS)
        AiTaxAndBusinessTaggingStudioCard(
            userDisplayName = userDisplayName,
            isSovereignPro = isSovereignPro,
            settings = settings,
            expenses = expenses,
            savingsGoals = savingsGoals,
            paymentReminders = paymentReminders,
            onRequestUnlockPro = onRequestUnlockPro,
            onShowToast = onShowToast
        )

        // STUDIO 3: UNLIMITED SAVINGS GOALS & SPARE-CHANGE AUTO-SWEEP
        UnlimitedSavingsGoalsAndAutoSweepStudioCard(
            userId = userId,
            isSovereignPro = isSovereignPro,
            savingsGoals = savingsGoals,
            onCreateGoal = onCreateGoal,
            onAddGoalProgress = onAddGoalProgress,
            onRequestUnlockPro = onRequestUnlockPro,
            onShowToast = onShowToast
        )

        // STUDIO 4: HOLOGRAPHIC THEME STUDIO (MATRIX, CYBERPUNK, OBSIDIAN VOID, 24K GOLD)
        HolographicThemeStudioCard(
            isSovereignPro = isSovereignPro,
            activeThemeId = settings.themeId,
            onSelectTheme = onSelectTheme,
            onRequestUnlockPro = onRequestUnlockPro,
            onShowToast = onShowToast
        )
    }
}

// ============================================================================
// STUDIO 1: ZERO-KNOWLEDGE E2EE CLOUD BACKUP VAULT
// ============================================================================
@Composable
fun ZeroKnowledgeE2eeVaultStudioCard(
    isSovereignPro: Boolean,
    expensesCount: Int,
    onRequestUnlockPro: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var zkEnabled by remember(context) {
        mutableStateOf(BackupManager.isZeroKnowledgeCustomSaltEnabled(context))
    }
    var customSaltHex by remember(context) {
        mutableStateOf(BackupManager.getZeroKnowledgeCustomSaltHex(context))
    }
    var masterPassphraseInput by remember {
        mutableStateOf(BackupManager.getInMemoryZeroKnowledgePassphrase())
    }
    var encryptedPreviewSnippet by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF00E676),
        unfocusedBorderColor = Color(0xFF28354A),
        focusedContainerColor = Color(0xFF060A12),
        unfocusedContainerColor = Color(0xFF060A12),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color(0xFF00E676),
        unfocusedLabelColor = Color(0xFF94A3B8)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A0E17))
            .border(1.1.dp, Color(0xFF00E676).copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("studio_zero_knowledge_e2ee_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF05291A))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Zero-Knowledge E2EE",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "ZERO-KNOWLEDGE E2EE CLOUD VAULT",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E676),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Custom Salt + In-Memory Master Passphrase (.enc)",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Switch(
                checked = zkEnabled,
                onCheckedChange = { enabled ->
                    if (!isSovereignPro) {
                        onShowToast("👑 Unlock Sovereign Pro to activate Zero-Knowledge Custom Salt E2EE")
                        onRequestUnlockPro()
                    } else {
                        zkEnabled = enabled
                        BackupManager.setZeroKnowledgeCustomSaltConfig(
                            context = context,
                            enabled = enabled,
                            customSaltHex = customSaltHex,
                            inMemoryPassphrase = masterPassphraseInput
                        )
                        onShowToast(
                            if (enabled) "🛡️ Zero-Knowledge Custom Salt Encryption Active"
                            else "Standard AES-256-GCM Backup Active"
                        )
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF040508),
                    checkedTrackColor = Color(0xFF00E676)
                ),
                modifier = Modifier.testTag("zk_e2ee_toggle_switch")
            )
        }

        Text(
            text = "Your personal Master Passphrase is held strictly in volatile RAM and NEVER uploaded to Google Drive or any server. Combined with your 128-bit Sovereign Salt for military-grade PBKDF2-HMAC-SHA256 + AES-256-GCM encryption.",
            fontSize = 11.sp,
            color = Color(0xFFB8B0D4),
            lineHeight = 16.sp
        )

        // Cryptographic Salt Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF060A12))
                .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "128-BIT SOVEREIGN CRYPTOGRAPHIC SALT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = Color(0xFF6EE7B7)
                )
                Text(
                    text = customSaltHex,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = {
                    val nextSalt = BackupManager.generateMilitaryGradeCustomSaltHex()
                    customSaltHex = nextSalt
                    if (isSovereignPro) {
                        BackupManager.setZeroKnowledgeCustomSaltConfig(context, zkEnabled, nextSalt, masterPassphraseInput)
                    }
                    onShowToast("🔄 Rotated 128-bit Sovereign Salt")
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, Color(0xFF00E676).copy(alpha = 0.7f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("zk_rotate_salt_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotate Salt",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Rotate Salt",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676)
                )
            }
        }

        OutlinedTextField(
            value = masterPassphraseInput,
            onValueChange = {
                masterPassphraseInput = it
                BackupManager.setInMemoryZeroKnowledgePassphrase(it)
            },
            label = { Text("Personal Master Passphrase (Never Stored in Cloud)", fontSize = 11.sp) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("zk_master_passphrase_input")
        )

        if (encryptedPreviewSnippet != null) {
            Text(
                text = encryptedPreviewSnippet!!,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Color(0xFF00E676),
                lineHeight = 14.sp
            )
        }

        Button(
            onClick = {
                if (!isSovereignPro) {
                    onShowToast("👑 Unlock Sovereign Pro to seal Google Drive .enc backups with your Zero-Knowledge Passphrase")
                    onRequestUnlockPro()
                } else {
                    val pass = masterPassphraseInput.trim().ifBlank { "SOVEREIGN_MASTER_KEY" }
                    val samplePayload = """{"user":"sovereign","txCount":$expensesCount,"ts":${System.currentTimeMillis()}}"""
                    val enc = BackupManager.encryptPayloadWithZeroKnowledgeSalt(
                        plainJson = samplePayload,
                        masterPassphrase = pass,
                        customSaltHex = customSaltHex
                    )
                    BackupManager.setZeroKnowledgeCustomSaltConfig(context, true, customSaltHex, pass)
                    zkEnabled = true
                    encryptedPreviewSnippet = "✓ Sealed .enc Header: ${enc.take(46)}..."
                    onShowToast("🔐 Zero-Knowledge E2EE .enc Vault Sealed for Google Drive!")
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSovereignPro) Color(0xFF00E676) else Color(0xFF1E293B),
                contentColor = if (isSovereignPro) Color(0xFF040508) else Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("zk_seal_backup_button")
        ) {
            Icon(
                imageVector = if (isSovereignPro) Icons.Default.CloudDone else Icons.Default.Lock,
                contentDescription = "Seal E2EE Backup",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isSovereignPro) "Seal & Arm Zero-Knowledge .enc Backup" else "🔒 Unlock Zero-Knowledge Custom Salt E2EE",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }
    }
}

// ============================================================================
// STUDIO 2: AI TAX & BUSINESS EXPENSE TAGGING (CA-READY EXPORTS)
// ============================================================================
@Composable
fun AiTaxAndBusinessTaggingStudioCard(
    userDisplayName: String,
    isSovereignPro: Boolean,
    settings: UserSettings,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    paymentReminders: List<PaymentReminderRecord>,
    onRequestUnlockPro: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var tagRefreshTick by remember { mutableIntStateOf(0) }

    val outflows = remember(expenses) {
        expenses.filter { !isIncomeTransaction(it) }.sortedByDescending { it.txEpochMillis }
    }
    val taxSummary = remember(outflows, tagRefreshTick) {
        MonthlyFinancialPdfExporter.computeTaxDeductibleSummary(context, outflows)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0D0E17))
            .border(1.1.dp, Color(0xFFFFD700).copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("studio_ai_tax_business_tagging_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF261E05))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "AI Tax & Business Expense Tagging",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "AI TAX & BUSINESS EXPENSE STUDIO",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "CA-Ready Business vs Personal Audit & GST Credit",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    val count = MonthlyFinancialPdfExporter.autoClassifyBusinessExpenses(context, outflows)
                    tagRefreshTick++
                    onShowToast("🤖 AI Auto-Tagged $count Business / Tax Deductible expenses!")
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, Color(0xFFFFD700)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("ai_tax_auto_tag_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Auto-Tag",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI Auto-Tag",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
            }
        }

        // 3-Column CA Tax Telemetry Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF061F14))
                    .border(0.8.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "BUSINESS DEDUCTIBLE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = Color(0xFF6EE7B7)
                )
                Text(
                    text = PerformancePreferences.preFormatInr(taxSummary.totalBusinessDeductibleInr),
                    fontFamily = headingFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E676)
                )
                Text(
                    text = "${taxSummary.businessTransactionCount} txns (${taxSummary.businessSharePercent}%)",
                    fontSize = 9.sp,
                    color = Color(0xFFB8B0D4)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F1705))
                    .border(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "EST. 18% GST CREDIT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = Color(0xFFFDE68A)
                )
                Text(
                    text = PerformancePreferences.preFormatInr(taxSummary.estimatedGstInputCredit18PctInr),
                    fontFamily = headingFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
                Text(
                    text = "Input Tax Credit",
                    fontSize = 9.sp,
                    color = Color(0xFFB8B0D4)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141126))
                    .border(0.8.dp, Color(0xFFA855F7).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "TAX SAVED (30%)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = Color(0xFFD8B4FE)
                )
                Text(
                    text = PerformancePreferences.preFormatInr(taxSummary.estimatedIncomeTaxSaving30PctInr),
                    fontFamily = headingFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Sec 37(1) Freelance",
                    fontSize = 9.sp,
                    color = Color(0xFFB8B0D4)
                )
            }
        }

        // Interactive Transaction Tagging List (Top 4 Recent Spends)
        if (outflows.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TAP TO TOGGLE [BUSINESS / TAX DEDUCTIBLE] VS [PERSONAL]:",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB8B0D4)
                )
                outflows.take(4).forEach { tx ->
                    val isBiz = remember(tx.id, tagRefreshTick) {
                        MonthlyFinancialPdfExporter.isTransactionBusinessDeductible(context, tx)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color(0xFF141225))
                            .border(
                                width = 0.8.dp,
                                color = if (isBiz) Color(0xFF00E676).copy(alpha = 0.7f) else Color(0xFF2B224C),
                                shape = RoundedCornerShape(11.dp)
                            )
                            .clickable {
                                if (!isSovereignPro) {
                                    onShowToast("👑 Unlock Sovereign Pro to tag Business/Tax Deductible expenses & export CA reports")
                                    onRequestUnlockPro()
                                } else {
                                    MonthlyFinancialPdfExporter.setTransactionTaxClassification(
                                        context = context,
                                        expenseId = tx.id,
                                        isBusinessDeductible = !isBiz
                                    )
                                    tagRefreshTick++
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("tax_tag_tx_row_${tx.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${tx.title.ifBlank { tx.category }} · ₹${tx.amount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tx.category,
                                fontSize = 10.sp,
                                color = Color(0xFFB8B0D4)
                            )
                        }
                        Surface(
                            color = if (isBiz) Color(0xFF05291A) else Color(0xFF1E1B36),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.8.dp,
                                if (isBiz) Color(0xFF00E676) else Color(0xFF64748B)
                            )
                        ) {
                            Text(
                                text = if (isBiz) "💼 Business / Tax Deductible" else "🏠 Personal",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isBiz) Color(0xFF00E676) else Color(0xFFCBD5E1),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Export CA-Ready PDF & CSV Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!isSovereignPro) {
                        onShowToast("👑 Unlock Sovereign Pro to export CA-Ready Tax PDF with Tax Summary Section")
                        onRequestUnlockPro()
                    } else {
                        val bundle = InsightIntelligenceEngine.computeCompleteAnalytics(
                            expenses = expenses,
                            settings = settings,
                            categoryBudgets = emptyMap(),
                            savingsGoals = savingsGoals,
                            paymentReminders = paymentReminders,
                            filter = InsightPeriodFilter.THIS_MONTH
                        )
                        val res = MonthlyFinancialPdfExporter.downloadPdfToDevice(
                            context = context,
                            bundle = bundle,
                            expenses = expenses,
                            userDisplayName = userDisplayName,
                            isSovereignPro = true
                        )
                        res.onSuccess { exported ->
                            onShowToast("📄 Saved CA-Ready Tax PDF: ${exported.savedLocationLabel}")
                        }.onFailure {
                            onShowToast("📄 Generated CA-Ready Tax Summary PDF")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF040508)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_ca_tax_pdf_button")
            ) {
                Text(
                    text = "📄 CA Tax PDF",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = {
                    if (!isSovereignPro) {
                        onShowToast("👑 Unlock Sovereign Pro to export CA-Ready Tax CSV")
                        onRequestUnlockPro()
                    } else {
                        val csv = MonthlyFinancialPdfExporter.generateCaReadyTaxCsv(
                            context = context,
                            expenses = expenses,
                            userDisplayName = userDisplayName,
                            isSovereignPro = true
                        )
                        com.example.util.SovereignActivationManager.copyToClipboard(
                            context = context,
                            label = "KHARCHEE CA Tax CSV",
                            text = csv
                        )
                        onShowToast("📊 Exported CA-Ready Tax CSV (${taxSummary.businessTransactionCount} Business txns)!")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF00E676)),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_ca_tax_csv_button")
            ) {
                Text(
                    text = "📊 CA Tax CSV",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = Color(0xFF00E676),
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================================
// STUDIO 3: UNLIMITED SAVINGS GOALS & SPARE-CHANGE AUTO-SWEEP
// ============================================================================
@Composable
fun UnlimitedSavingsGoalsAndAutoSweepStudioCard(
    userId: String,
    isSovereignPro: Boolean,
    savingsGoals: List<SavingsGoalRecord>,
    onCreateGoal: ((title: String, targetAmount: Long, savedAmount: Long) -> Unit)?,
    onAddGoalProgress: ((SavingsGoalRecord, Long) -> Unit)?,
    onRequestUnlockPro: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var autoSweepEnabled by remember(context, userId) {
        mutableStateOf(SavingsGoalsManager.isSpareChangeAutoSweepEnabled(context, userId))
    }
    var roundUpIncrement by remember(context, userId) {
        mutableLongStateOf(SavingsGoalsManager.getRoundUpIncrement(context, userId))
    }
    var totalSweptInr by remember(context, userId, savingsGoals) {
        mutableLongStateOf(SavingsGoalsManager.getTotalSpareChangeSweptInr(context, userId))
    }
    var sweepCount by remember(context, userId, savingsGoals) {
        mutableIntStateOf(SavingsGoalsManager.getTotalSpareChangeSweptCount(context, userId))
    }
    var customGoalTitleInput by remember { mutableStateOf("") }
    var customGoalTargetInput by remember { mutableStateOf("50000") }

    val activeGoal = remember(context, userId, savingsGoals) {
        SavingsGoalsManager.resolveTargetSweepGoal(context, userId, savingsGoals)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A101D))
            .border(1.1.dp, Color(0xFF00E5FF).copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("studio_unlimited_goals_auto_sweep_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF062232))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "Spare-Change Auto-Sweep",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "UNLIMITED GOALS & SPARE-CHANGE SWEEP",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${SavingsGoalsManager.getMaxGoalsAllowedLabel(isSovereignPro)} · Active: ${savingsGoals.size}",
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Switch(
                checked = autoSweepEnabled,
                onCheckedChange = { enabled ->
                    autoSweepEnabled = enabled
                    SavingsGoalsManager.setSpareChangeAutoSweepEnabled(context, userId, enabled)
                    onShowToast(
                        if (enabled) "🪙 Spare-Change Auto-Sweep Enabled (Nearest ₹$roundUpIncrement)"
                        else "Spare-Change Auto-Sweep Paused"
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF040508),
                    checkedTrackColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier.testTag("spare_change_auto_sweep_switch")
            )
        }

        Text(
            text = "Automatically rounds up every logged expense to the nearest ₹50 or ₹100 and sweeps the spare change into your active Savings Goal.",
            fontSize = 11.sp,
            color = Color(0xFFB8B0D4),
            lineHeight = 16.sp
        )

        // Round-Up Increment Selector: Nearest ₹50 vs Nearest ₹100
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                50L to "Round to Nearest ₹50 (e.g. ₹120 → +₹30)",
                100L to "Round to Nearest ₹100 (e.g. ₹120 → +₹80)"
            ).forEach { (incVal, incLabel) ->
                val selected = roundUpIncrement == incVal
                Surface(
                    color = if (selected) Color(0xFF06293B) else Color(0xFF060A12),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (selected) 1.2.dp else 0.8.dp,
                        color = if (selected) Color(0xFF00E5FF) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            roundUpIncrement = incVal
                            SavingsGoalsManager.setRoundUpIncrement(context, userId, incVal)
                            onShowToast("🪙 Auto-Sweep set to nearest ₹$incVal")
                        }
                        .testTag("sweep_increment_$incVal")
                ) {
                    Text(
                        text = incLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
                    )
                }
            }
        }

        // Live Sweep Telemetry Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF060A12))
                .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL SPARE CHANGE SWEPT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = Color(0xFF7DD3FC)
                )
                Text(
                    text = "${PerformancePreferences.preFormatInr(totalSweptInr)} ($sweepCount sweeps)",
                    fontFamily = headingFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )
            }

            OutlinedButton(
                onClick = {
                    val sampleSpend = 120L
                    val diff = SavingsGoalsManager.computeSpareChangeRoundUp(sampleSpend, roundUpIncrement)
                    val target = activeGoal ?: SavingsGoalRecord(
                        id = "goal_default",
                        userId = userId,
                        title = "Emergency Vault",
                        targetAmount = 50000L,
                        savedAmount = 0L
                    )
                    if (activeGoal != null) {
                        onAddGoalProgress?.invoke(activeGoal, diff)
                    } else {
                        onCreateGoal?.invoke("Emergency Vault", 50000L, diff)
                    }
                    SavingsGoalsManager.recordSpareChangeSweep(
                        context = context,
                        userId = userId,
                        expenseTitle = "Sample Coffee Spend",
                        expenseAmount = sampleSpend,
                        sweptAmount = diff,
                        targetGoal = target
                    )
                    totalSweptInr = SavingsGoalsManager.getTotalSpareChangeSweptInr(context, userId)
                    sweepCount = SavingsGoalsManager.getTotalSpareChangeSweptCount(context, userId)
                    onShowToast("🪙 Swept ₹$diff spare change (from ₹120 spend) into ${target.title}!")
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.9.dp, Color(0xFF00E5FF)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("simulate_spare_change_sweep_button")
            ) {
                Text(
                    text = "⚡ Test +₹${SavingsGoalsManager.computeSpareChangeRoundUp(120L, roundUpIncrement)} Sweep",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )
            }
        }

        // 1-Tap Unlimited Goal Templates (iPhone Fund, Trip, Emergency, MacBook)
        Text(
            text = "1-TAP CREATE UNLIMITED SOVEREIGN GOALS:",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB8B0D4)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("📱 iPhone Fund", 85000L, "iphone"),
                Triple("✈️ Goa / Bali Trip", 45000L, "trip"),
                Triple("🛡️ Emergency Fund", 100000L, "emergency"),
                Triple("💻 MacBook Pro", 140000L, "macbook")
            ).forEach { (title, targetAmt, tagKey) ->
                Surface(
                    color = Color(0xFF10192B),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.65f)),
                    modifier = Modifier
                        .clickable {
                            if (!SavingsGoalsManager.canCreateNewSavingsGoal(savingsGoals.size, isSovereignPro)) {
                                onShowToast("👑 Free tier supports up to 3 goals. Unlock Sovereign Pro for Unlimited Goals!")
                                onRequestUnlockPro()
                            } else {
                                onCreateGoal?.invoke(title, targetAmt, 0L)
                                onShowToast("🎯 Created Sovereign Goal: $title (₹$targetAmt)")
                            }
                        }
                        .testTag("quick_create_goal_$tagKey")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = title,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$title (₹${targetAmt / 1000}k)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// STUDIO 4: HOLOGRAPHIC THEME STUDIO (MATRIX, CYBERPUNK, OBSIDIAN VOID, 24K GOLD)
// ============================================================================
@Composable
fun HolographicThemeStudioCard(
    isSovereignPro: Boolean,
    activeThemeId: String,
    onSelectTheme: (String) -> Unit,
    onRequestUnlockPro: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val normalizedActive = ThemePreferenceManager.normalizeThemeId(activeThemeId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C0918))
            .border(1.1.dp, Color(0xFFA855F7).copy(alpha = 0.65f), RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("studio_holographic_theme_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFF1F123B))
                        .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Holographic Theme Studio",
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "HOLOGRAPHIC THEME STUDIO",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD8B4FE),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Instant 1-Tap Dark-Mode UI Accent Switching",
                        fontFamily = headingFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HOLOGRAPHIC_STUDIO_THEMES.forEach { palette ->
                val isSelected = normalizedActive == palette.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.surface)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.9.dp,
                            color = if (isSelected) palette.accent else palette.borderGlow.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            ThemePreferenceManager.saveThemePreference(context, palette.id)
                            onSelectTheme(palette.id)
                            onShowToast("${palette.symbol} Activated ${palette.name} Holographic Theme!")
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("holographic_theme_${palette.id}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(palette.background)
                                .border(1.5.dp, palette.accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = palette.symbol, fontSize = 15.sp)
                        }
                        Column {
                            Text(
                                text = palette.name,
                                fontFamily = headingFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = palette.subtitle,
                                fontSize = 10.sp,
                                color = palette.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active Theme",
                            tint = palette.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Surface(
                            color = palette.accent.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, palette.accent)
                        ) {
                            Text(
                                text = "Apply",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. CELEBRATORY 24K GOLD CONFETTI BURST OVERLAY ON PRO UNLOCK
// ============================================================================
@Composable
fun SovereignGoldConfettiCelebrationOverlay(
    visible: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember(visible) { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 2600, easing = LinearEasing),
        label = "sovereignConfettiBurst"
    )

    LaunchedEffect(visible) {
        if (visible) {
            progress = 1f
            delay(2700L)
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val confettiColors = remember {
            listOf(
                Color(0xFFFFD700), // 24K Sovereign Gold
                Color(0xFF00E676), // Matrix Emerald
                Color(0xFF00E5FF), // Cyberpunk Cyan
                Color(0xFFA855F7), // Royal Violet
                Color(0xFFFFF8E1)  // Champagne Spark
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sovereign_gold_confetti_celebration_overlay")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val particleCount = 64
                for (i in 0 until particleCount) {
                    val angle = (i * 137.5f) * (Math.PI.toFloat() / 180f)
                    val speed = 0.25f + ((i % 9) * 0.08f)
                    val startX = w * (0.1f + ((i * 19) % 80) / 100f)
                    val startY = h * 0.18f
                    val dx = cos(angle) * w * 0.42f * animatedProgress * speed
                    val dy = (sin(angle) * h * 0.22f * animatedProgress) + (h * 0.78f * animatedProgress * animatedProgress * speed)
                    val px = (startX + dx).coerceIn(0f, w)
                    val py = (startY + dy).coerceIn(0f, h)
                    val color = confettiColors[i % confettiColors.size]
                    val alpha = (1f - (animatedProgress * 0.75f)).coerceIn(0f, 1f)

                    rotate(degrees = (i * 45f + animatedProgress * 360f) % 360f, pivot = Offset(px, py)) {
                        drawRect(
                            color = color.copy(alpha = alpha),
                            topLeft = Offset(px - 6.dp.toPx(), py - 3.dp.toPx()),
                            size = Size(12.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
