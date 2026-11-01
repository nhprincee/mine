package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ESSENTIAL_BYPASS_REASONS
import com.example.data.model.NightGuardChallenge
import com.example.data.model.NightGuardChallengeEngine
import com.example.data.model.NightGuardCooldownStore
import com.example.data.model.NightGuardSchedule
import com.example.data.model.PendingSpendRequest
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.DeepForestAbyss
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.EmeraldNeonGlow
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
import kotlin.math.max
import kotlinx.coroutines.delay

@Composable
fun NightGuardInterceptOverlay(
    userId: String,
    pendingRequest: PendingSpendRequest,
    settings: UserSettings,
    todaySpent: Long,
    savingsGoals: List<SavingsGoalRecord>,
    onTriggerCooldown: (cooldownEndMillis: Long, lastChallengeId: String) -> Unit,
    onCancelPurchase: (challengeCategory: String) -> Unit,
    onSaveForTomorrow: (challengeCategory: String) -> Unit,
    onContinueToRecordExpense: (challengeCategory: String) -> Unit,
    onEssentialBypass: (bypassReason: String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var recentIds by remember(userId) {
        mutableStateOf(
            NightGuardCooldownStore.getRecentChallengeIds(context, userId) +
                listOfNotNull(settings.lastChallengeId.takeIf { it.isNotBlank() })
        )
    }

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var currentChallenge by remember {
        mutableStateOf<NightGuardChallenge>(
            NightGuardChallengeEngine.pickNextChallenge(excludeIds = recentIds)
        )
    }
    var selectedOption by remember(currentChallenge.id) { mutableStateOf<String?>(null) }
    var challengeCompleted by remember { mutableStateOf(false) }
    var showEssentialBypassPanel by remember { mutableStateOf(false) }
    var selectedBypassReason by remember { mutableStateOf(ESSENTIAL_BYPASS_REASONS.first()) }
    var memoryCardVisible by remember(currentChallenge.id) { mutableStateOf(true) }

    // Effective wall-clock cooldown end timestamp (survives recomposition and app restarts)
    var effectiveCooldownEndMillis by remember(userId, settings.nightGuardCooldownEndMillis) {
        mutableLongStateOf(
            NightGuardCooldownStore.resolveEffectiveCooldownEndMillis(
                context = context,
                userId = userId,
                firestoreCooldownEndMillis = settings.nightGuardCooldownEndMillis
            )
        )
    }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val remainingCooldownMillis = NightGuardCooldownStore.getRemainingCooldownMillis(
        nowMillis = nowMillis,
        effectiveCooldownEndMillis = effectiveCooldownEndMillis
    )
    val isInCooldown = remainingCooldownMillis > 0L
    var wasInCooldown by remember { mutableStateOf(isInCooldown) }

    // Tick wall-clock timer every 500ms while in cooldown
    LaunchedEffect(effectiveCooldownEndMillis) {
        while (true) {
            val currentNow = System.currentTimeMillis()
            nowMillis = currentNow
            val rem = NightGuardCooldownStore.getRemainingCooldownMillis(currentNow, effectiveCooldownEndMillis)
            if (rem <= 0L) {
                if (wasInCooldown) {
                    wasInCooldown = false
                    // Cooldown expired: generate a fresh non-repeating challenge
                    val updatedRecent = NightGuardCooldownStore.recordRecentChallengeId(
                        context = context,
                        userId = userId,
                        challengeId = currentChallenge.id
                    )
                    recentIds = updatedRecent
                    currentChallenge = NightGuardChallengeEngine.pickNextChallenge(
                        excludeIds = updatedRecent,
                        preferredCategory = selectedCategoryFilter
                    )
                    selectedOption = null
                }
                break
            } else {
                wasInCooldown = true
            }
            delay(500L)
        }
    }

    val dailyLimit = max(50L, settings.dailyLimit)
    val remainingBeforeSpend = max(0L, dailyLimit - todaySpent)
    val remainingAfterSpend = dailyLimit - (todaySpent + pendingRequest.amount)
    val primaryGoal = savingsGoals.firstOrNull()

    val ObsidianBlack = com.example.ui.theme.ObsidianDarkBackground
    val RichGraphite = com.example.ui.theme.ObsidianDarkPrimarySurface
    val ElevatedGraphite = com.example.ui.theme.ObsidianDarkSecondarySurface
    val HairlinePlatinum = com.example.ui.theme.ObsidianDarkBorder
    val HairlineGold = com.example.ui.theme.ObsidianDarkSilver.copy(alpha = 0.42f)
    val WarmIvory = com.example.ui.theme.ObsidianDarkPrimaryText
    val MutedPlatinum = com.example.ui.theme.ObsidianDarkSecondaryText
    val MutedIvory = com.example.ui.theme.ObsidianDarkMutedText
    val ChampagneGold = com.example.ui.theme.ObsidianDarkSilver
    val SophisticatedEmerald = com.example.ui.theme.ObsidianDarkEmeraldAccent
    val CrimsonRuby = com.example.ui.theme.ObsidianDarkNegative

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBlack,
                        RichGraphite,
                        ObsidianBlack
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("night_guard_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SophisticatedEmerald.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f), RoundedCornerShape(50))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = "Night Guard Active",
                            tint = SophisticatedEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NIGHT GUARD ACTIVE · ${
                                NightGuardSchedule.formatTimeWindow(
                                    settings.nightGuardStartHour,
                                    settings.nightGuardStartMinute,
                                    settings.nightGuardEndHour,
                                    settings.nightGuardEndMinute
                                )
                            }",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SophisticatedEmerald
                        )
                    }
                }

                IconButton(
                    onClick = { onCancelPurchase(currentChallenge.category) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElevatedGraphite)
                        .testTag("night_guard_close_cancel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel & Close",
                        tint = MutedPlatinum,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Hero Header Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                RichGraphite,
                                ElevatedGraphite,
                                ObsidianBlack
                            )
                        )
                    )
                    .border(1.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
                    .padding(22.dp)
            ) {
                Text(
                    text = "RUKO ZARA. THINK BEFORE YOU SPEND.",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory,
                    lineHeight = 30.sp,
                    modifier = Modifier.testTag("night_guard_headline")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Will this purchase still matter tomorrow morning?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ChampagneGold
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "INTERCEPTED DISCRETIONARY OUTFLOW",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = MutedIvory
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${pendingRequest.title} · ${pendingRequest.category}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmIvory
                        )
                    }
                    Text(
                        text = "₹${pendingRequest.amount}",
                        fontFamily = headingFont,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChampagneGold
                    )
                }
            }

            // State 1: Mandatory 5-Minute Cooldown Screen
            if (isInCooldown && !challengeCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(RichGraphite)
                        .border(1.2.dp, CrimsonRuby.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                        .testTag("night_guard_cooldown_card"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(CrimsonRuby.copy(alpha = 0.14f))
                            .border(1.dp, CrimsonRuby.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "5-Minute Cooldown",
                            tint = CrimsonRuby,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "MANDATORY 5-MINUTE COOLDOWN",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.4.sp,
                        color = CrimsonRuby
                    )

                    Text(
                        text = NightGuardCooldownStore.formatRemainingCooldown(remainingCooldownMillis),
                        fontFamily = headingFont,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory,
                        modifier = Modifier.testTag("night_guard_cooldown_timer")
                    )

                    Text(
                        text = "Take a moment. You don't need to decide right now.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ChampagneGold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Incorrect answer submitted. Further challenge attempts are locked until the 5-minute reflection timer completes. Closing or restarting the app will not reset this timer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedPlatinum,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onCancelPurchase(currentChallenge.category) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedEmerald,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cooldown_cancel_purchase_button")
                        ) {
                            Text("Cancel & Save ₹${pendingRequest.amount}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onSaveForTomorrow(currentChallenge.category) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, ChampagneGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cooldown_save_tomorrow_button")
                        ) {
                            Text("Save for Tomorrow", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // State 2: Interactive Brain Diversion Challenge (when not in cooldown and not yet completed)
            if (!isInCooldown && !challengeCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                        .testTag("night_guard_challenge_card"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Brain Diversion Challenge",
                                tint = ChampagneGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BRAIN DIVERSION CHALLENGE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.1.sp,
                                color = ChampagneGold
                            )
                        }
                        Surface(
                            color = ElevatedGraphite,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(0.6.dp, HairlineGold, RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "~${currentChallenge.estimatedSeconds}s · ${currentChallenge.category}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = WarmIvory,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Category Switcher Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NightGuardChallengeEngine.CHALLENGE_CATEGORIES.forEach { cat ->
                            val isSelected = currentChallenge.category == cat
                            Surface(
                                color = if (isSelected) SophisticatedEmerald.copy(alpha = 0.18f) else ObsidianBlack,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .border(
                                        0.8.dp,
                                        if (isSelected) SophisticatedEmerald else HairlinePlatinum,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable {
                                        selectedCategoryFilter = cat
                                        val updatedRecent = NightGuardCooldownStore.recordRecentChallengeId(
                                            context = context,
                                            userId = userId,
                                            challengeId = currentChallenge.id
                                        )
                                        recentIds = updatedRecent
                                        currentChallenge = NightGuardChallengeEngine.pickNextChallenge(
                                            excludeIds = updatedRecent,
                                            preferredCategory = cat
                                        )
                                        selectedOption = null
                                    }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) SophisticatedEmerald else MutedPlatinum,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Memory Sequence Flash Card if applicable
                    if (currentChallenge.memoryFlashSequence != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(ObsidianBlack)
                                .border(0.8.dp, HairlineGold, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MEMORY SEQUENCE CUE",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    color = ChampagneGold
                                )
                                Text(
                                    text = if (memoryCardVisible) "Hide Sequence" else "Peek Sequence",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = SophisticatedEmerald,
                                    modifier = Modifier.clickable { memoryCardVisible = !memoryCardVisible }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (memoryCardVisible) currentChallenge.memoryFlashSequence!! else "•••  ·  •••  ·  •••  ·  •••",
                                fontFamily = headingFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmIvory,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = currentChallenge.prompt,
                        fontFamily = headingFont,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory,
                        lineHeight = 22.sp
                    )

                    Text(
                        text = currentChallenge.subtitleHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedPlatinum
                    )

                    // Answer Options
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        currentChallenge.options.forEachIndexed { idx, option ->
                            val isSelected = selectedOption == option
                            Surface(
                                color = if (isSelected) ChampagneGold.copy(alpha = 0.18f) else ObsidianBlack,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isSelected) ChampagneGold else HairlinePlatinum,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedOption = option }
                                    .testTag("challenge_option_$idx")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) ChampagneGold else WarmIvory
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) ChampagneGold else ElevatedGraphite)
                                            .border(0.8.dp, HairlinePlatinum, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "⚠️ Warning: Submitting an incorrect answer triggers a mandatory 5-minute reflection cooldown.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedIvory
                    )

                    Button(
                        onClick = {
                            val chosen = selectedOption ?: return@Button
                            val updatedRecent = NightGuardCooldownStore.recordRecentChallengeId(
                                context = context,
                                userId = userId,
                                challengeId = currentChallenge.id
                            )
                            recentIds = updatedRecent

                            if (NightGuardChallengeEngine.validateAnswer(currentChallenge, chosen)) {
                                challengeCompleted = true
                            } else {
                                // Wrong answer -> Mandatory 5-minute wall-clock cooldown
                                val cooldownEnd = System.currentTimeMillis() + NightGuardChallengeEngine.FIVE_MINUTES_MS
                                NightGuardCooldownStore.saveLocalCooldownEndMillis(context, userId, cooldownEnd)
                                effectiveCooldownEndMillis = cooldownEnd
                                nowMillis = System.currentTimeMillis()
                                onTriggerCooldown(cooldownEnd, currentChallenge.id)
                            }
                        },
                        enabled = selectedOption != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBlack,
                            disabledContainerColor = ElevatedGraphite,
                            disabledContentColor = MutedIvory
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_challenge_answer_button")
                    ) {
                        Text("Verify Answer & Evaluate Purchase", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // State 3: Correct Answer -> Decision Screen
            if (challengeCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(RichGraphite)
                        .border(1.2.dp, SophisticatedEmerald, RoundedCornerShape(24.dp))
                        .padding(22.dp)
                        .testTag("night_guard_decision_screen"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Challenge Complete",
                            tint = SophisticatedEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COGNITIVE CHECK PASSED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = SophisticatedEmerald
                        )
                    }

                    Text(
                        text = "Challenge complete. Now, do you really need this purchase?",
                        fontFamily = headingFont,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmIvory
                    )

                    // Financial Impact Breakdown Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ObsidianBlack)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DecisionImpactRow(
                            label = "Expense Amount",
                            value = "₹${pendingRequest.amount}",
                            valueColor = ChampagneGold
                        )
                        DecisionImpactRow(
                            label = "Remaining Daily Budget Before Spend",
                            value = "₹$remainingBeforeSpend / ₹$dailyLimit",
                            valueColor = WarmIvory
                        )
                        DecisionImpactRow(
                            label = "Remaining Daily Budget After Spend",
                            value = if (remainingAfterSpend >= 0) "₹$remainingAfterSpend left" else "Exceeds by ₹${-remainingAfterSpend}",
                            valueColor = if (remainingAfterSpend >= 0) SophisticatedEmerald else CrimsonRuby
                        )
                        if (primaryGoal != null && primaryGoal.targetAmount > 0L) {
                            val goalImpactPct = ((pendingRequest.amount.toDouble() / primaryGoal.targetAmount.toDouble()) * 100.0).toInt().coerceAtLeast(1)
                            DecisionImpactRow(
                                label = "Impact on '${primaryGoal.title}' Goal",
                                value = "$goalImpactPct% of ₹${primaryGoal.targetAmount} target",
                                valueColor = ChampagneGold
                            )
                        }
                    }

                    // Choice 1: Cancel Purchase (Recommended)
                    Button(
                        onClick = { onCancelPurchase(currentChallenge.category) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedEmerald,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("decision_cancel_purchase_button")
                    ) {
                        Text(
                            text = "Cancel purchase (Save ₹${pendingRequest.amount})",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Choice 2: Save for Tomorrow
                    OutlinedButton(
                        onClick = { onSaveForTomorrow(currentChallenge.category) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ChampagneGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("decision_save_tomorrow_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save for tomorrow (Remind me in morning)",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Choice 3: Continue to Record Expense
                    OutlinedButton(
                        onClick = { onContinueToRecordExpense(currentChallenge.category) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.8.dp, HairlinePlatinum),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedPlatinum),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("decision_continue_record_button")
                    ) {
                        Text(
                            text = "Continue to record expense",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Essential Payment Bypass Section (Always accessible for legitimate urgent needs)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEssentialBypassPanel = !showEssentialBypassPanel }
                        .testTag("toggle_essential_bypass_button"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Essential Payment Bypass",
                            tint = ChampagneGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Essential or Emergency Payment?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmIvory
                            )
                            Text(
                                text = "Medical, emergency travel, essential bills or urgent necessities",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedIvory,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = if (showEssentialBypassPanel) "Hide" else "Bypass",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChampagneGold
                    )
                }

                AnimatedVisibility(visible = showEssentialBypassPanel) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SELECT ESSENTIAL REASON FOR IMMEDIATE LOGGING:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = MutedPlatinum
                        )
                        ESSENTIAL_BYPASS_REASONS.forEach { reason ->
                            val active = selectedBypassReason == reason
                            Surface(
                                color = if (active) ChampagneGold.copy(alpha = 0.15f) else ObsidianBlack,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        0.8.dp,
                                        if (active) ChampagneGold else HairlinePlatinum,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedBypassReason = reason }
                            ) {
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (active) ChampagneGold else WarmIvory,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { onEssentialBypass(selectedBypassReason) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = ObsidianBlack
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_essential_bypass_button")
                        ) {
                            Text(
                                text = "Record Essential Expense Immediately ($selectedBypassReason)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DecisionImpactRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    val headingFont = LocalAppFontFamily.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedPlatinum,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontFamily = headingFont,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreSpendConfirmationSheet(
    pendingRequest: PendingSpendRequest,
    settings: UserSettings,
    todaySpent: Long,
    monthSpent: Long,
    savingsGoals: List<SavingsGoalRecord>,
    onDismissCancel: () -> Unit,
    onSaveForTomorrow: () -> Unit,
    onConfirmRecord: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val dailyLimit = max(50L, settings.dailyLimit)
    val remainingAfterSpend = dailyLimit - (todaySpent + pendingRequest.amount)
    val monthlyRemainingAfter = (dailyLimit * 30L) - (monthSpent + pendingRequest.amount)
    val primaryGoal = savingsGoals.firstOrNull()

    var checkNecessary by remember { mutableStateOf(false) }
    var checkWaitTomorrow by remember { mutableStateOf(false) }
    var checkGoalImpact by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissCancel,
        containerColor = RichGraphite,
        contentColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag("pre_spend_confirmation_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Pre-Spend Reflection",
                    tint = ChampagneGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRE-SPEND MINDFULNESS CHECK",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = ChampagneGold
                )
            }

            Text(
                text = "Pause Before Recording ₹${pendingRequest.amount}",
                fontFamily = headingFont,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmIvory
            )

            Text(
                text = "${pendingRequest.title} · ${pendingRequest.category} (${pendingRequest.paymentMode})",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedPlatinum
            )

            // Budget Impact Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianBlack)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DecisionImpactRow(
                    label = "Daily Reserve Impact",
                    value = if (remainingAfterSpend >= 0) "₹$remainingAfterSpend remaining today" else "Over daily limit by ₹${-remainingAfterSpend}",
                    valueColor = if (remainingAfterSpend >= 0) SophisticatedEmerald else CrimsonRuby
                )
                DecisionImpactRow(
                    label = "Monthly Budget Runway",
                    value = if (monthlyRemainingAfter >= 0) "₹$monthlyRemainingAfter left this month" else "Exceeds monthly cap by ₹${-monthlyRemainingAfter}",
                    valueColor = if (monthlyRemainingAfter >= 0) WarmIvory else CrimsonRuby
                )
                if (primaryGoal != null && primaryGoal.targetAmount > 0L) {
                    val goalRemaining = max(0L, primaryGoal.targetAmount - primaryGoal.savedAmount)
                    DecisionImpactRow(
                        label = "Savings Goal (${primaryGoal.title})",
                        value = "₹$goalRemaining still needed to reach target",
                        valueColor = ChampagneGold
                    )
                }
            }

            // 3 Reflection Prompts
            Text(
                text = "QUICK REFLECTION CHECKLIST",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = MutedPlatinum
            )

            ReflectionCheckRow(
                checked = checkNecessary,
                onCheckedChange = { checkNecessary = it },
                question = "Is this necessary?"
            )
            ReflectionCheckRow(
                checked = checkWaitTomorrow,
                onCheckedChange = { checkWaitTomorrow = it },
                question = "Can this wait until tomorrow?"
            )
            ReflectionCheckRow(
                checked = checkGoalImpact,
                onCheckedChange = { checkGoalImpact = it },
                question = "Will this affect my savings goal?"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissCancel,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.8.dp, SophisticatedEmerald),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SophisticatedEmerald),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pre_spend_cancel_button")
                ) {
                    Text("Cancel Spend", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onSaveForTomorrow,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.8.dp, ChampagneGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pre_spend_postpone_button")
                ) {
                    Text("Wait Till Tomorrow", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = onConfirmRecord,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pre_spend_confirm_button")
            ) {
                Text("Confirm & Record ₹${pendingRequest.amount}", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReflectionCheckRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    question: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ElevatedGraphite)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = SophisticatedEmerald,
                uncheckedColor = MutedPlatinum,
                checkmarkColor = ObsidianBlack
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = WarmIvory
        )
    }
}
