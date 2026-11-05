package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.ExpenseRecord
import com.example.data.model.KharcheeEcosystemFeaturesStore
import com.example.data.model.SavingsGoalRecord
import com.example.ui.components.KharcheeArchitecturalLogo
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
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

@Composable
fun SavingsChallengesCardSection(
    userId: String,
    fieldColors: TextFieldColors,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var challenges by remember(userId) {
        mutableStateOf(KharcheeEcosystemFeaturesStore.getSavingsChallenges(context, userId))
    }
    var titleInput by remember { mutableStateOf("30-Day Smart Discipline Challenge") }
    var durationInput by remember { mutableStateOf(30) }
    var targetAmtInput by remember { mutableStateOf("5000") }
    var targetDateInput by remember { mutableStateOf("30 Nov 2026") }
    var contributeAmtInput by remember { mutableStateOf("500") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RichGraphite)
            .border(1.dp, HairlineGold, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("savings_challenges_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("SAVINGS CHALLENGES", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChampagneGold)
                Text("Healthy discretionary savings sprints — never skip essential expenses", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7 to "7-Day", 30 to "30-Day", 60 to "Custom 60d").forEach { (days, label) ->
                val sel = durationInput == days
                Surface(
                    color = if (sel) ChampagneGold.copy(alpha = 0.18f) else ObsidianBlack,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(0.8.dp, if (sel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(8.dp))
                        .clickable {
                            durationInput = days
                            titleInput = "$label Savings Challenge"
                        }
                ) {
                    Text(label, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = if (sel) ChampagneGold else MutedPlatinum, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Challenge Title") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1.2f)
            )
            OutlinedTextField(
                value = targetAmtInput,
                onValueChange = { targetAmtInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Target (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(0.8f)
            )
        }

        Button(
            onClick = {
                val target = targetAmtInput.toLongOrNull() ?: 0L
                if (target > 0L) {
                    challenges = KharcheeEcosystemFeaturesStore.createSavingsChallenge(
                        context = context,
                        userId = userId,
                        title = titleInput,
                        durationDays = durationInput,
                        targetAmount = target,
                        targetDate = targetDateInput,
                        reminderEnabled = true
                    )
                    onShowToast("🏆 Started $titleInput (₹$target)")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
            modifier = Modifier.fillMaxWidth().testTag("start_savings_challenge_btn")
        ) {
            Text("START SAVINGS CHALLENGE", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }

        challenges.forEach { ch ->
            val pct = if (ch.targetAmount > 0L) (ch.savedAmount.toFloat() / ch.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, if (ch.completed) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${ch.title} (${ch.durationDays}d)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = WarmIvory)
                    Text(
                        text = if (ch.completed) "✓ COMPLETED BADGE" else "₹${ch.savedAmount} / ₹${ch.targetAmount}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ch.completed) SophisticatedEmerald else ChampagneGold
                    )
                }
                LinearProgressIndicator(
                    progress = { pct },
                    color = if (ch.completed) SophisticatedEmerald else ChampagneGold,
                    trackColor = RichGraphite,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${ch.contributionsCount} contributions · Target: ${ch.targetDate}", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
                    OutlinedButton(
                        onClick = {
                            val addAmt = contributeAmtInput.toLongOrNull() ?: 500L
                            challenges = KharcheeEcosystemFeaturesStore.contributeToSavingsChallenge(context, userId, ch.id, addAmt)
                            onShowToast("✅ Added ₹$addAmt to ${ch.title}")
                        }
                    ) {
                        Text("+₹500", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = SophisticatedEmerald)
                    }
                }
            }
        }
    }
}

@Composable
fun KharcheeWrappedCardSection(
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    streakDays: Int
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var isYearly by remember { mutableStateOf(false) }
    var hideExactAmounts by remember { mutableStateOf(true) }

    val summary = remember(expenses, savingsGoals, streakDays, isYearly) {
        KharcheeEcosystemFeaturesStore.computeKharcheeWrapped(
            periodType = if (isYearly) "YEARLY" else "MONTHLY",
            expenses = expenses,
            savingsGoals = savingsGoals,
            streakDays = streakDays
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RichGraphite)
            .border(1.dp, HairlineGold, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("kharchee_wrapped_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            KharcheeArchitecturalLogo(compact = true)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(false to "Monthly", true to "Yearly").forEach { (yr, label) ->
                    val sel = isYearly == yr
                    Surface(
                        color = if (sel) ChampagneGold.copy(alpha = 0.2f) else ObsidianBlack,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(0.8.dp, if (sel) ChampagneGold else HairlinePlatinum, RoundedCornerShape(8.dp))
                            .clickable { isYearly = yr }
                    ) {
                        Text(label, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = if (sel) ChampagneGold else MutedPlatinum, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                }
            }
        }

        Text("KHARCHEE WRAPPED — ${summary.periodTitle.uppercase()}", fontFamily = headingFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = WarmIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(ObsidianBlack).padding(10.dp)) {
                Text("TOP CATEGORY", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                Text(summary.highestSpendingCategory, fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ChampagneGold)
                Text(if (hideExactAmounts) "₹ ••••" else "₹${summary.highestSpendingCategoryAmount}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = MutedIvory)
            }
            Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(ObsidianBlack).padding(10.dp)) {
                Text("MOST USED", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MutedPlatinum)
                Text(summary.mostUsedCategory, fontFamily = headingFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SophisticatedEmerald)
                Text("${summary.trackingStreakDays}d Streak", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = MutedIvory)
            }
        }

        Text("Achievements: ${summary.unlockedAchievements.joinToString(" · ")}", style = MaterialTheme.typography.bodySmall, color = WarmIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = hideExactAmounts,
                    onCheckedChange = { hideExactAmounts = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBlack, checkedTrackColor = SophisticatedEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mask Private Balances on Share", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
            OutlinedButton(
                onClick = {
                    val shareText = if (hideExactAmounts) {
                        "KHARCHEE WRAPPED (${summary.periodTitle})\nStreak: ${summary.trackingStreakDays} Days\nTop Category: ${summary.highestSpendingCategory}\nBadges: ${summary.unlockedAchievements.joinToString()}"
                    } else {
                        "KHARCHEE WRAPPED (${summary.periodTitle})\nIncome: ₹${summary.totalIncome} | Spent: ₹${summary.totalExpenses}\nSaved: ₹${summary.savingsProgress}\nStreak: ${summary.trackingStreakDays} Days"
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share KHARCHEE Wrapped"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SHARE WRAPPED", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ChampagneGold)
            }
        }
    }
}

@Composable
fun FinancialDataCenterCardSection(
    userId: String,
    expenses: List<ExpenseRecord>,
    savingsGoals: List<SavingsGoalRecord>,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var exportHistory by remember(userId) {
        mutableStateOf(KharcheeEcosystemFeaturesStore.getExportHistory(context, userId))
    }
    val snapReceipts = remember(expenses) {
        expenses.filter { it.note.contains("[SNAP_RECEIPT]") }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RichGraphite)
            .border(1.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("financial_data_center_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("FINANCIAL DATA CENTER", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChampagneGold)
                Text("CSV/Excel exports, JSON Backup, Receipt Archive (${snapReceipts.size} receipts) & Connected Services", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    KharcheeEcosystemFeaturesStore.recordDataExport(context, userId, "CSV / Excel Passbook", expenses.size)
                    exportHistory = KharcheeEcosystemFeaturesStore.getExportHistory(context, userId)
                    onShowToast("📄 Exported ${expenses.size} records to CSV")
                },
                modifier = Modifier.weight(1f)
            ) { Text("CSV / EXCEL", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = ChampagneGold) }

            OutlinedButton(
                onClick = {
                    val json = KharcheeEcosystemFeaturesStore.buildVaultBackupJson(context, userId, expenses, savingsGoals)
                    exportHistory = KharcheeEcosystemFeaturesStore.getExportHistory(context, userId)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(intent, "Backup KHARCHEE JSON"))
                },
                modifier = Modifier.weight(1f)
            ) { Text("JSON BACKUP", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = SophisticatedEmerald) }
        }

        Text("CONNECTED SERVICES: Firebase Auth (Active) · Cloud Firestore (Synced) · Gemini AI (Online)", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = SophisticatedEmerald)

        if (exportHistory.isNotEmpty()) {
            Text("RECENT DATA EXPORT HISTORY", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = MutedPlatinum)
            exportHistory.take(3).forEach { rec ->
                Text("• ${rec.format} (${rec.recordCount} records)", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }
    }
}

@Composable
fun FamilyWalletCardSection(
    userId: String,
    userDisplayName: String,
    fieldColors: TextFieldColors,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var walletState by remember(userId) {
        mutableStateOf(KharcheeEcosystemFeaturesStore.getFamilyWallet(context, userId, userDisplayName))
    }
    var memberNameInput by remember { mutableStateOf("") }
    var memberRoleInput by remember { mutableStateOf("Contributor") }
    var expTitleInput by remember { mutableStateOf("") }
    var expAmtInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RichGraphite)
            .border(1.dp, HairlineGold, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("family_wallet_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("FAMILY WALLET (${walletState.groupName.uppercase()})", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChampagneGold)
                Text("Shared household budget & expenses — private personal transactions remain isolated", style = MaterialTheme.typography.bodySmall, color = MutedIvory)
            }
        }

        val totalShared = walletState.sharedExpenses.sumOf { it.amount }
        Text("Shared Spent: ₹$totalShared / ₹${walletState.sharedMonthlyBudget} · Members: ${walletState.members.size}", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmIvory)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = memberNameInput,
                onValueChange = { memberNameInput = it },
                label = { Text("Invite Member Name") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (memberNameInput.isNotBlank()) {
                        walletState = KharcheeEcosystemFeaturesStore.addFamilyMember(context, userId, userDisplayName, memberNameInput, memberRoleInput)
                        onShowToast("👨‍👩‍👧 Added $memberNameInput ($memberRoleInput)")
                        memberNameInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack)
            ) {
                Text("INVITE", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = expTitleInput,
                onValueChange = { expTitleInput = it },
                label = { Text("Shared Expense") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = expAmtInput,
                onValueChange = { expAmtInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("₹ Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.weight(0.7f)
            )
            Button(
                onClick = {
                    val amt = expAmtInput.toLongOrNull() ?: 0L
                    if (expTitleInput.isNotBlank() && amt > 0L) {
                        walletState = KharcheeEcosystemFeaturesStore.addFamilySharedExpense(
                            context, userId, userDisplayName, expTitleInput, amt, "Household", userDisplayName, "EQUAL"
                        )
                        expTitleInput = ""
                        expAmtInput = ""
                        onShowToast("✅ Added shared expense ₹$amt")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = ObsidianBlack)
            ) {
                Text("SPLIT", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        walletState.members.forEach { m ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${m.name} (${m.role})", style = MaterialTheme.typography.bodySmall, color = WarmIvory)
                Text("Contributed: ₹${m.contributedAmount}", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = ChampagneGold)
            }
        }
    }
}
