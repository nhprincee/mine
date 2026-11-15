package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseRecord
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.STARK_ARC_BADGES
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.ui.components.KharcheeCompactHeaderBranding
import com.example.ui.theme.ALL_APP_FONTS
import com.example.ui.theme.ALL_HUD_THEMES
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldSoft
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.FontPreferenceManager
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.LocalHudTheme
import com.example.ui.theme.MetallicSilver
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianDarkBackground
import com.example.ui.theme.ObsidianDarkBorder
import com.example.ui.theme.ObsidianDarkEmeraldAccent
import com.example.ui.theme.ObsidianDarkPrimarySurface
import com.example.ui.theme.ObsidianDarkPrimaryText
import com.example.ui.theme.ObsidianDarkSecondarySurface
import com.example.ui.theme.ObsidianDarkSilver
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.SophisticatedEmerald
import com.example.ui.theme.WarmIvory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private data class ProTierPlan(
    val id: String,
    val title: String,
    val priceDisplay: String,
    val amountInr: Int,
    val cycleLabel: String,
    val badgeText: String,
    val tagline: String,
    val accentColor: Color,
    val rimColors: List<Color>,
    val tierKey: String = "gold",
    val durationDays: Int = 30
)

private val PRO_TIER_PLANS = listOf(
    ProTierPlan(
        id = "sovereign_lifetime",
        title = "₹299 Lifetime Sovereign",
        priceDisplay = "₹299",
        amountInr = 299,
        cycleLabel = "One-Time Lifetime Unlock · Zero Subscriptions · All Sovereign Power Perks Forever",
        badgeText = "BEST VALUE · LIFETIME SOVEREIGN",
        tagline = "Custom Neon App Icons + Automated Tax/GST PDF & Excel Reports + Jarvis Voice Audio Mode + Custom Obsidian/Gold Card Themes",
        accentColor = ChampagneGold,
        rimColors = listOf(
            ChampagneGold,
            Color(0xFFA855F7),
            ObsidianDarkEmeraldAccent
        ),
        tierKey = "platinum",
        durationDays = 36500
    ),
    ProTierPlan(
        id = "starter_monthly",
        title = "₹30 / Month (Starter)",
        priceDisplay = "₹30 / Mo",
        amountInr = 30,
        cycleLabel = "30-Day Starter Access · Manual Renewal · No Auto-Debit",
        badgeText = "STARTER · 30 DAYS",
        tagline = "Try all Sovereign Power Perks, Custom Card Themes & Jarvis Voice Briefings for 30 days",
        accentColor = Color(0xFFD8B4FE),
        rimColors = listOf(
            Color(0xFFA855F7),
            ObsidianDarkSilver,
            ChampagneGold
        ),
        tierKey = "gold",
        durationDays = 30
    )
)

private data class ProFeatureHighlight(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color
)

private val PRO_FEATURE_HIGHLIGHTS = listOf(
    ProFeatureHighlight(
        title = "Custom Neon App Icons",
        subtitle = "Personalize your home screen with Cyber-Violet, Brushed 24K Gold & Stealth Obsidian launcher emblems",
        icon = Icons.Default.Palette,
        tint = Color(0xFFD8B4FE)
    ),
    ProFeatureHighlight(
        title = "Automated Tax/GST PDF & Excel Reports",
        subtitle = "1-tap CA-ready FY audit sheets, GST input credit summaries & structured CSV/Excel ledger exports",
        icon = Icons.Default.Verified,
        tint = ObsidianDarkEmeraldAccent
    ),
    ProFeatureHighlight(
        title = "Jarvis Voice Audio Mode",
        subtitle = "Hands-free spoken Hindi, Hinglish & English audio financial briefings and real-time voice coaching",
        icon = Icons.Default.AutoAwesome,
        tint = ChampagneGold
    ),
    ProFeatureHighlight(
        title = "Custom Obsidian/Gold Card Themes",
        subtitle = "Unlock Brushed 24K Sovereign Gold, Matte Titanium & Cyber-Neon finishes for your Hero Balance Card",
        icon = Icons.Default.WorkspacePremium,
        tint = ObsidianDarkSilver
    )
)

private val LIFETIME_FREE_CORE_PROMISES = listOf(
    "Adding unlimited expenses & incomes" to "100% Free Forever",
    "Full Khata ledger & 1-tap UPI settlements" to "100% Free Forever",
    "Real-time SMS Auto-detection & clipboard parsing" to "100% Free Forever",
    "Encrypted Google Drive backup & restore" to "100% Free Forever",
    "Biometric/PIN App-Lock & Jarvis financial queries" to "100% Free Forever"
)

@Composable
fun ProVaultTab(
    settings: UserSettings,
    userDisplayName: String,
    disciplineScore: Int,
    streakDays: Int,
    expenses: List<ExpenseRecord> = emptyList(),
    userId: String = settings.userId.ifBlank { "default_user" },
    savingsGoals: List<SavingsGoalRecord> = emptyList(),
    paymentReminders: List<PaymentReminderRecord> = emptyList(),
    onDeleteAccountRequested: (() -> Unit)? = null,
    onShowToast: (String) -> Unit = {},
    onSelectTheme: (String) -> Unit,
    onToggleAutoLockdown: (Boolean) -> Unit,
    onSelectUpiTone: (String) -> Unit,
    onSelectFont: ((String) -> Unit)? = null,
    onUpgradeProPlan: ((String, Int) -> Unit)? = null,
    onProActivated: ((String) -> Unit)? = null,
    onCreateGoal: ((title: String, targetAmount: Long, savedAmount: Long) -> Unit)? = null,
    onAddGoalProgress: ((SavingsGoalRecord, Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hud = LocalHudTheme.current
    val headingFont = LocalAppFontFamily.current
    val context = LocalContext.current
    val selectedFontId by FontPreferenceManager.selectedFontId.collectAsStateWithLifecycle()

    val activeProTier = if (settings.isPro) settings.proTier else null
    var selectedPlanId by remember(activeProTier) {
        mutableStateOf(
            if (activeProTier == "gold") "starter_monthly" else "sovereign_lifetime"
        )
    }
    var showCheckoutModal by remember { mutableStateOf(false) }
    var showGoldConfettiBurst by remember { mutableStateOf(false) }
    var selectedNeonIconStyle by remember { mutableStateOf("Cyber-Violet A-Emblem") }
    var selectedCardFinish by remember { mutableStateOf("Obsidian Titanium Mesh") }
    var jarvisVoiceAudioEnabled by remember { mutableStateOf(true) }
    var selectedMetalFinish by remember(context) {
        mutableStateOf(SovereignBlackClubEngine.getSelectedMetalFinish(context))
    }

    val deviceHash = remember(context) {
        com.example.util.SovereignActivationManager.getOrCreateDeviceHash(context)
    }
    var utrInputText by remember { mutableStateOf("") }
    var activationCodeInput by remember {
        mutableStateOf(com.example.util.SovereignActivationManager.getSavedVerifiedActivationCode(context))
    }
    var activationStatusMessage by remember { mutableStateOf<String?>(null) }
    var activationIsError by remember { mutableStateOf(false) }
    var showFounderKeyGenDialog by remember { mutableStateOf(false) }
    var versionTapCounter by remember { mutableStateOf(0) }

    val selectedPlan = remember(selectedPlanId) {
        PRO_TIER_PLANS.find { it.id == selectedPlanId } ?: PRO_TIER_PLANS.first()
    }

    val now = System.currentTimeMillis()
    val sevenDaysAgo = now - 7 * 86_400_000L
    val fourteenDaysAgo = now - 14 * 86_400_000L

    val recentOutflows = expenses.filter { !isIncomeTransaction(it) }
    val thisWeekExpenses = recentOutflows.filter { it.txEpochMillis in sevenDaysAgo..now }
    val lastWeekExpenses = recentOutflows.filter { it.txEpochMillis in fourteenDaysAgo until sevenDaysAgo }

    val thisWeekSpent = thisWeekExpenses.sumOf { it.amount }
    val lastWeekSpent = lastWeekExpenses.sumOf { it.amount }
    val dailyAvgSpent = if (thisWeekSpent > 0) thisWeekSpent / 7L else 0L

    val velocityPct = if (lastWeekSpent > 0) {
        (((thisWeekSpent - lastWeekSpent).toDouble() / lastWeekSpent.toDouble()) * 100.0).roundToInt()
    } else 0

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val expiryFormatted = remember(settings.proExpiryMillis) {
        if (settings.proExpiryMillis > 0L) dateFormat.format(Date(settings.proExpiryMillis)) else "Active Obsidian Tier"
    }

    Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("pro_vault_tab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. INTERACTIVE 3D SHIMMERING SOVEREIGN METAL CARD (HERO STATUS FLEX)
        item {
            Interactive3dSovereignMetalCardSection(
                userDisplayName = userDisplayName,
                deviceHash = deviceHash,
                isSovereignPro = settings.isPro,
                disciplineScore = disciplineScore,
                streakDays = streakDays,
                selectedFinish = selectedMetalFinish,
                onSelectFinish = { finish ->
                    selectedMetalFinish = finish
                    SovereignBlackClubEngine.setSelectedMetalFinish(context, finish)
                    onShowToast("✨ Previewing ${finish.displayName} Metal Card")
                }
            )
        }

        // 2. 4 REAL WORKING POWER FEATURES EXCLUSIVE TO SOVEREIGN PRO (WITH LIVE TEASERS)
        item {
            SovereignFourPowerFeaturesShowcaseSection(
                userId = userId,
                userDisplayName = userDisplayName,
                isSovereignPro = settings.isPro,
                expenses = expenses,
                disciplineScore = disciplineScore,
                streakDays = streakDays,
                onRequestScrollToUnlock = {
                    showCheckoutModal = true
                },
                onShowToast = onShowToast
            )
        }

        // 2B. 4 ELITE ENTERPRISE POWER-FEATURES & CUSTOMIZATION STUDIOS
        item {
            SovereignEnterpriseFourStudiosSection(
                userId = userId,
                userDisplayName = userDisplayName,
                isSovereignPro = settings.isPro,
                settings = settings,
                expenses = expenses,
                savingsGoals = savingsGoals,
                paymentReminders = paymentReminders,
                onSelectTheme = onSelectTheme,
                onCreateGoal = onCreateGoal,
                onAddGoalProgress = onAddGoalProgress,
                onRequestUnlockPro = {
                    showCheckoutModal = true
                },
                onShowToast = onShowToast
            )
        }

        // 2C. SECRET GHOST VAULT (DECOY HIDDEN LEDGER PROTECTED BY SEPARATE 4-DIGIT SHA-256 GHOST PIN)
        item {
            SecretGhostVaultStudioCard(
                isSovereignPro = settings.isPro,
                onRequestUnlockPro = {
                    showCheckoutModal = true
                },
                onShowToast = onShowToast
            )
        }

        // Ethical Privacy & Trust Pill
        item {
            Surface(
                color = Color(0xFF0A1F17),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = SophisticatedEmerald.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sovereign_tab_ethical_privacy_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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
        }

        // 100% LIFETIME FREE CORE PROMISE CARD (ZERO PAYWALLS ON CORE UTILITY)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(RichGraphite)
                    .border(
                        width = 1.dp,
                        color = SophisticatedEmerald.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
                    .testTag("lifetime_free_core_promise_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "100% LIFETIME FREE CORE PROMISE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.1.sp,
                        color = SophisticatedEmerald
                    )
                    Surface(
                        color = SophisticatedEmerald.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, SophisticatedEmerald.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "ZERO PAYWALLS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedEmerald,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Every essential financial tool in Kharchee is permanently unlocked for all users — no trial limits, no lock icons, and no subscription traps.",
                    fontSize = 12.sp,
                    color = MutedIvory,
                    lineHeight = 17.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    LIFETIME_FREE_CORE_PROMISES.forEach { (featureName, badgeLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(11.dp))
                                .background(ObsidianBlack)
                                .border(0.6.dp, HairlinePlatinum, RoundedCornerShape(11.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Free Forever",
                                    tint = SophisticatedEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = featureName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarmIvory
                                )
                            }
                            Text(
                                text = badgeLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedEmerald
                            )
                        }
                    }
                }
            }
        }

        // Obsidian Luxury Matte-Black & Metallic-Silver Discipline Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(18.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ObsidianDarkBackground,
                                ObsidianDarkPrimarySurface,
                                ObsidianDarkSecondarySurface,
                                ObsidianDarkBackground
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ObsidianDarkSilver.copy(alpha = 0.6f),
                                ObsidianDarkBorder,
                                ObsidianDarkEmeraldAccent.copy(alpha = 0.45f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KharcheeCompactHeaderBranding(isPro = true)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = ObsidianDarkSecondarySurface,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(0.8.dp, ObsidianDarkBorder, RoundedCornerShape(50))
                    ) {
                        Text(
                            text = if (settings.isPro) {
                                "ACTIVE: KHARCHEE SOVEREIGN LIFETIME · UNLOCKED FOREVER"
                            } else {
                                "KHARCHEE SOVEREIGN · CORE FOREVER FREE"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = ObsidianDarkSilver,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "KHARCHEE DISCIPLINE INDEX",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = ObsidianDarkSilver
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$disciplineScore",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.displayLarge,
                            color = ObsidianDarkPrimaryText,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "/ 900 · ${streakDays}d Runway",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ObsidianDarkEmeraldAccent,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PRIVATE MEMBER",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                                color = ObsidianDarkSilver.copy(alpha = 0.75f)
                            )
                            Text(
                                text = userDisplayName.uppercase(),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = ObsidianDarkPrimaryText
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ACTIVE AESTHETIC",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                                color = ObsidianDarkSilver.copy(alpha = 0.75f)
                            )
                            Text(
                                text = hud.name.uppercase(),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = ObsidianDarkSilver
                            )
                        }
                    }
                }
            }
        }

        // KHARCHEE PRO 40-MODULE INTELLIGENCE SUITE HUB
        item {
            KharcheePro30ModuleHubSection(
                userId = userId,
                userDisplayName = userDisplayName,
                isProUnlocked = settings.isPro,
                disciplineScore = disciplineScore,
                streakDays = streakDays,
                dailyLimit = settings.dailyLimit,
                expenses = expenses,
                savingsGoals = savingsGoals,
                paymentReminders = paymentReminders,
                proExpiryMillis = settings.proExpiryMillis,
                onDeleteAccountRequested = onDeleteAccountRequested,
                onShowToast = onShowToast
            )
        }

        // ADVANCED PRO ANALYTICS & VELOCITY DASHBOARD (FEATURE 4)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(RichGraphite)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                if (settings.isPro) ChampagneGold.copy(alpha = 0.8f) else HairlinePlatinum,
                                MutedPlatinum.copy(alpha = 0.4f),
                                if (settings.isPro) ChampagneGold.copy(alpha = 0.6f) else HairlinePlatinum
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
                    .testTag("pro_analytics_dashboard_section"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElevatedGraphite)
                                .border(0.8.dp, ChampagneGold, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Advanced Analytics",
                                tint = ChampagneGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SPENDING VELOCITY & TRENDS",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.2.sp,
                                color = ChampagneGold
                            )
                            Text(
                                text = "Unrestricted Autonomous Analytics Active",
                                fontFamily = headingFont,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmIvory,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Surface(
                        color = SophisticatedEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, SophisticatedEmerald)
                    ) {
                        Text(
                            text = "✓ FREE CORE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SophisticatedEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Metric Grid: Daily Burn Rate, Week-over-Week Velocity, Projected Month-End
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElevatedGraphite)
                            .border(0.6.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "DAILY BURN RATE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = MutedPlatinum
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹$dailyAvgSpent/d",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "vs ₹${settings.dailyLimit} target",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (dailyAvgSpent > settings.dailyLimit) CrimsonRuby else SophisticatedEmerald
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElevatedGraphite)
                            .border(0.6.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "WEEKLY VELOCITY",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = MutedPlatinum
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (velocityPct >= 0) "+$velocityPct%" else "$velocityPct%",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (velocityPct > 10) CrimsonRuby else ChampagneGold
                        )
                        Text(
                            text = "₹$thisWeekSpent this week",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedIvory
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElevatedGraphite)
                            .border(0.6.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "PROJECTED OUTFLOW",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = MutedPlatinum
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${dailyAvgSpent * 30L}",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "30-day projection",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedIvory
                        )
                    }
                }

                // JARVIS Autonomous Health Audit Score
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🧠", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "J.A.R.V.I.S. FINANCIAL HEALTH AUDIT",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = ChampagneGold
                            )
                        }
                        Text(
                            text = "$disciplineScore / 900",
                            fontFamily = headingFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SophisticatedEmerald
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Neural telemetry verified: Spending velocity is within optimal parameters. Top outflow category: ${thisWeekExpenses.groupBy { it.category }.maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key ?: "None"}. All Jarvis financial queries are 100% free forever.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedIvory,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 3. PSYCHOLOGICAL VALUE COMPARISON & "₹1/DAY" CONVERSION ENGINE
        //    + 4. STREAMLINED 3-STEP INSTANT UPI + WHATSAPP KEY UNLOCK BAR
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pro_membership_upgrade_section"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SovereignValueComparisonAndConversionSection(
                    selectedPlanId = selectedPlanId,
                    isProActive = settings.isPro,
                    activeProTier = activeProTier,
                    onSelectPlan = { planId ->
                        selectedPlanId = planId
                    }
                )

                // Interactive Kharchee Sovereign Exclusive Power Perks Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, ChampagneGold.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("sovereign_power_perks_controls"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SOVEREIGN EXCLUSIVE POWER PERKS STUDIO",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.1.sp,
                        color = ChampagneGold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Cyber-Violet A-Emblem", "24K Sovereign Gold", "Stealth Titanium").forEach { iconStyle ->
                            val active = selectedNeonIconStyle == iconStyle
                            Surface(
                                color = if (active) Color(0xFFA855F7).copy(alpha = 0.22f) else RichGraphite,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (active) Color(0xFFA855F7) else HairlinePlatinum
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedNeonIconStyle = iconStyle
                                        onShowToast("✨ Neon App Icon style set to $iconStyle")
                                    }
                            ) {
                                Text(
                                    text = iconStyle,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color(0xFFD8B4FE) else MutedPlatinum,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                jarvisVoiceAudioEnabled = !jarvisVoiceAudioEnabled
                                onShowToast(
                                    if (jarvisVoiceAudioEnabled) "🎙️ Jarvis Voice Audio Mode Enabled"
                                    else "🔇 Jarvis Voice Audio Mode Muted"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, ChampagneGold.copy(alpha = 0.7f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (jarvisVoiceAudioEnabled) "🎙️ Voice Mode: ON" else "🔇 Voice Mode: OFF",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChampagneGold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onShowToast("📊 Generated FY Tax/GST PDF & Excel Summary (${expenses.size} txns)")
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SophisticatedEmerald.copy(alpha = 0.7f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "📄 Tax/GST Report",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedEmerald
                            )
                        }
                    }
                }

                StreamlinedThreeStepUpiWhatsappUnlockBar(
                    deviceHash = deviceHash,
                    selectedPlanTitle = selectedPlan.title,
                    selectedPlanAmountInr = selectedPlan.amountInr,
                    isProActive = settings.isPro,
                    utrInputText = utrInputText,
                    activationCodeInput = activationCodeInput,
                    onActivationCodeChange = {
                        activationCodeInput = it
                        activationStatusMessage = null
                    },
                    activationStatusMessage = activationStatusMessage,
                    activationIsError = activationIsError,
                    onStep1PayViaUpi = {
                        showCheckoutModal = true
                        com.example.util.SovereignActivationManager.launchFounderUpiPayment(
                            context = context,
                            planTitle = selectedPlan.title,
                            amountInr = selectedPlan.amountInr,
                            deviceHash = deviceHash,
                            onStatusMessage = { msg ->
                                activationIsError = false
                                activationStatusMessage = msg
                                onShowToast(msg)
                            }
                        )
                    },
                    onStep2GetKeyOnWhatsApp = {
                        com.example.util.SovereignActivationManager.openWhatsAppForActivationKey(
                            context = context,
                            deviceHash = deviceHash,
                            planTitle = selectedPlan.title,
                            amountInr = selectedPlan.amountInr,
                            utrNumber = utrInputText,
                            onStatusMessage = onShowToast
                        )
                    },
                    onStep3VerifyAndUnlock = {
                        val verification = com.example.util.SovereignActivationManager.verifyActivationCode(
                            deviceHash = deviceHash,
                            enteredCode = activationCodeInput
                        )
                        if (verification.isValid) {
                            com.example.util.SovereignActivationManager.recordVerifiedActivation(
                                context = context,
                                activationCode = activationCodeInput,
                                result = verification
                            )
                            activationIsError = false
                            activationStatusMessage = "✅ Verified HMAC-SHA256 Key! Unlocked ${verification.planDisplayName} on Device [#KHR-$deviceHash]."
                            showGoldConfettiBurst = true
                            onUpgradeProPlan?.invoke(verification.tierKey, verification.durationDays)
                            onProActivated?.invoke(verification.planDisplayName)
                            onShowToast("👑 ${verification.planDisplayName} Unlocked! All 4 Enterprise Studios Active!")
                        } else {
                            activationIsError = true
                            activationStatusMessage = verification.errorMessage
                            onShowToast(verification.errorMessage ?: "Invalid Activation Key")
                        }
                    },
                    onCopyDeviceHash = {
                        com.example.util.SovereignActivationManager.copyToClipboard(
                            context = context,
                            label = "Kharchee DeviceHash",
                            text = deviceHash
                        )
                        onShowToast("📋 Copied Member ID #KHR-$deviceHash")
                    }
                )
            }
        }

        // Typography & Font Customization Card (Manrope ExtraBold + Inter Body)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FontDownload,
                        contentDescription = "Typography & Font Customization",
                        tint = ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TYPOGRAPHY & FONT CUSTOMIZATION",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = WarmIvory
                    )
                }

                ALL_APP_FONTS.forEach { fontOption ->
                    val isSelected = selectedFontId == fontOption.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ElevatedGraphite else ObsidianBlack)
                            .border(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) ChampagneGold else HairlinePlatinum,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                FontPreferenceManager.setSelectedFont(context, fontOption.id)
                                onSelectFont?.invoke(fontOption.name)
                            }
                            .padding(14.dp)
                            .testTag("select_font_${fontOption.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ElevatedGraphite)
                                .border(0.8.dp, if (isSelected) ChampagneGold else HairlinePlatinum, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aa",
                                fontFamily = fontOption.fontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) ChampagneGold else MutedPlatinum,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fontOption.name,
                                fontFamily = fontOption.fontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmIvory,
                                fontSize = 15.sp
                            )
                            Text(
                                text = fontOption.subtitle,
                                fontFamily = fontOption.bodyFontFamily,
                                color = MutedIvory,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = fontOption.sampleText,
                                fontFamily = fontOption.fontFamily,
                                color = ChampagneGold,
                                fontSize = 12.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected Font",
                                tint = ChampagneGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Single Universal Purple Luxury Theme Locked: multi-theme switcher removed

        // Discipline Automation & UPI Guard Tone Settings
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Discipline Guard",
                        tint = ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UPI ACCOUNTABILITY & AUTO-LOCKDOWN",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = WarmIvory
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Lockdown at 90% Daily Reserve",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = WarmIvory
                        )
                        Text(
                            text = "Automatically engages spending shield when daily outflow crosses 90%",
                            fontSize = 11.sp,
                            color = MutedIvory
                        )
                    }
                    Switch(
                        checked = settings.autoLockdownAt90,
                        onCheckedChange = onToggleAutoLockdown,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = ChampagneGold
                        )
                    )
                }

                Text(
                    text = "UPI Accountability Alert Tone:",
                    fontSize = 12.sp,
                    color = MutedPlatinum
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "hinglish" to "◆ Hinglish",
                        "friendly" to "❖ Friendly",
                        "strict" to "◈ Strict"
                    ).forEach { (toneKey, label) ->
                        val active = settings.upiGuardTone == toneKey
                        Surface(
                            color = if (active) ChampagneGold.copy(alpha = 0.16f) else ObsidianBlack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    0.8.dp,
                                    if (active) ChampagneGold else HairlinePlatinum,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectUpiTone(toneKey) }
                                .testTag("upi_tone_$toneKey")
                        ) {
                            Text(
                                text = label,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (active) ChampagneGold else MutedPlatinum,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sovereign Reward Vault Badges
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MilitaryTech,
                    contentDescription = "Sovereign Reward Vault",
                    tint = ChampagneGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SOVEREIGN ACHIEVEMENT HONORS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = ChampagneGold
                )
            }
        }

        items(STARK_ARC_BADGES, key = { it.id }) { badge ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElevatedGraphite)
                        .border(0.8.dp, HairlineGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = badge.symbol, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmIvory,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = badge.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedIvory,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    color = SophisticatedEmerald.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.45f), RoundedCornerShape(50))
                ) {
                    Text(
                        text = badge.requirementText,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = SophisticatedEmerald,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        // Version Footer (7-Tap Trigger for Founder Admin Key Generator)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        versionTapCounter++
                        if (versionTapCounter >= 7) {
                            versionTapCounter = 0
                            showFounderKeyGenDialog = true
                        } else if (versionTapCounter >= 4) {
                            onShowToast("👑 ${7 - versionTapCounter} more taps for Founder Key Generator")
                        }
                    }
                    .padding(vertical = 10.dp)
                    .testTag("pro_tab_version_7tap_footer"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Kharchee ${com.example.util.SovereignActivationManager.APP_VERSION_LABEL} · DeviceHash: $deviceHash",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = MutedPlatinum
                )
            }
        }
    }

    SovereignGoldConfettiCelebrationOverlay(
        visible = showGoldConfettiBurst,
        onFinished = { showGoldConfettiBurst = false }
    )
    }

    if (showFounderKeyGenDialog) {
        com.example.util.FounderAdminKeyGeneratorDialog(
            currentPhoneDeviceHash = deviceHash,
            onDismiss = { showFounderKeyGenDialog = false },
            onShowToast = onShowToast
        )
    }

    if (showCheckoutModal) {
        AlertDialog(
            onDismissRequest = { showCheckoutModal = false },
            containerColor = RichGraphite,
            title = {
                Text(
                    text = "${selectedPlan.title} — ${selectedPlan.priceDisplay}",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Anti-Bypass Offline Verification · DeviceHash: $deviceHash",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = ChampagneGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. Complete your ₹${selectedPlan.amountInr} UPI payment.\n2. Tap 'Get Activation Key on WhatsApp' to send your DeviceHash ($deviceHash) & 12-digit UPI UTR.\n3. Enter the Founder HMAC-SHA256 Activation Key to unlock Pro on this phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = utrInputText,
                        onValueChange = { utrInputText = it.take(24) },
                        label = { Text("UPI UTR / Ref No (Optional for WhatsApp)", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChampagneGold,
                            unfocusedBorderColor = HairlinePlatinum,
                            focusedContainerColor = ObsidianBlack,
                            unfocusedContainerColor = ObsidianBlack,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_modal_utr_input")
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = activationCodeInput,
                        onValueChange = {
                            activationCodeInput = it.uppercase(Locale.US)
                            activationStatusMessage = null
                        },
                        label = { Text("Enter Activation Code (KHR-SOV-...)", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SophisticatedEmerald,
                            unfocusedBorderColor = HairlinePlatinum,
                            focusedContainerColor = ObsidianBlack,
                            unfocusedContainerColor = ObsidianBlack,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_modal_activation_code_input")
                    )
                    OutlinedButton(
                        onClick = {
                            com.example.util.SovereignActivationManager.openWhatsAppForActivationKey(
                                context = context,
                                deviceHash = deviceHash,
                                planTitle = selectedPlan.title,
                                amountInr = selectedPlan.amountInr,
                                utrNumber = utrInputText,
                                onStatusMessage = onShowToast
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedEmerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_modal_whatsapp_key_button")
                    ) {
                        Text(
                            text = "💬 Get Activation Key on WhatsApp (DeviceHash: $deviceHash)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SophisticatedEmerald
                        )
                    }
                    if (activationStatusMessage != null) {
                        Text(
                            text = activationStatusMessage!!,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (activationIsError) CrimsonRuby else SophisticatedEmerald
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (activationCodeInput.isBlank()) {
                            // Launch UPI payment if they haven't entered a code yet (NEVER auto-unlocks!)
                            com.example.util.SovereignActivationManager.launchFounderUpiPayment(
                                context = context,
                                planTitle = selectedPlan.title,
                                amountInr = selectedPlan.amountInr,
                                deviceHash = deviceHash,
                                onStatusMessage = { msg ->
                                    activationIsError = false
                                    activationStatusMessage = msg
                                    onShowToast(msg)
                                }
                            )
                        } else {
                            val verification = com.example.util.SovereignActivationManager.verifyActivationCode(
                                deviceHash = deviceHash,
                                enteredCode = activationCodeInput
                            )
                            if (verification.isValid) {
                                com.example.util.SovereignActivationManager.recordVerifiedActivation(
                                    context = context,
                                    activationCode = activationCodeInput,
                                    result = verification
                                )
                                showCheckoutModal = false
                                showGoldConfettiBurst = true
                                onUpgradeProPlan?.invoke(verification.tierKey, verification.durationDays)
                                onProActivated?.invoke(verification.planDisplayName)
                                onShowToast("👑 ${verification.planDisplayName} Unlocked! All 4 Enterprise Studios Active!")
                            } else {
                                activationIsError = true
                                activationStatusMessage = verification.errorMessage
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    ),
                    modifier = Modifier.testTag("checkout_modal_confirm_button")
                ) {
                    Text(
                        text = if (activationCodeInput.isBlank()) {
                            "Pay ₹${selectedPlan.amountInr} via UPI"
                        } else {
                            "Verify Key & Unlock Pro"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCheckoutModal = false }) {
                    Text("Close", color = MutedPlatinum, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}
