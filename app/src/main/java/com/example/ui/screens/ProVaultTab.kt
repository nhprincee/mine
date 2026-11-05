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
        id = "gold_monthly",
        title = "PRO Silver Edition",
        priceDisplay = "₹30/month",
        amountInr = 30,
        cycleLabel = "Billed monthly · Cancel anytime",
        badgeText = "OBSIDIAN SILVER TIER",
        tagline = "Full J.A.R.V.I.S. Voice Copilot + Obsidian Themes",
        accentColor = ObsidianDarkSilver,
        rimColors = listOf(
            ObsidianDarkSilver,
            ObsidianDarkBorder,
            ObsidianDarkSilver
        ),
        tierKey = "gold",
        durationDays = 30
    ),
    ProTierPlan(
        id = "platinum_quarterly",
        title = "PRO Obsidian Elite",
        priceDisplay = "₹99/3 months",
        amountInr = 99,
        cycleLabel = "Quarterly Private Banking Pass · Priority AI Core",
        badgeText = "OBSIDIAN ELITE · PREFERRED",
        tagline = "Obsidian Titanium Reserve + Unlimited Split Khata",
        accentColor = ObsidianDarkEmeraldAccent,
        rimColors = listOf(
            ObsidianDarkEmeraldAccent,
            ObsidianDarkSilver,
            ObsidianDarkEmeraldAccent
        ),
        tierKey = "platinum",
        durationDays = 90
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
        title = "J.A.R.V.I.S. Uncapped Voice Analytics",
        subtitle = "Unlimited Hindi, Hinglish & English spoken coaching and real-time spend insights",
        icon = Icons.Default.AutoAwesome,
        tint = ObsidianDarkEmeraldAccent
    ),
    ProFeatureHighlight(
        title = "Obsidian Luxury HUD Themes",
        subtitle = "Switch seamlessly between Obsidian Dark, Obsidian Light, Metallic Silver & Carbon Slate",
        icon = Icons.Default.Palette,
        tint = ObsidianDarkSilver
    ),
    ProFeatureHighlight(
        title = "Unlimited Split Khata Ledgers",
        subtitle = "Track unlimited friend group splits, 1-tap UPI settlements & WhatsApp reminders",
        icon = Icons.Default.Groups,
        tint = ObsidianDarkEmeraldAccent
    ),
    ProFeatureHighlight(
        title = "Zero Ads",
        subtitle = "100% distraction-free matte-black and metallic-silver private banking experience",
        icon = Icons.Default.Verified,
        tint = ObsidianDarkPrimaryText
    )
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
    modifier: Modifier = Modifier
) {
    val hud = LocalHudTheme.current
    val headingFont = LocalAppFontFamily.current
    val context = LocalContext.current
    val selectedFontId by FontPreferenceManager.selectedFontId.collectAsStateWithLifecycle()

    val activeProTier = if (settings.isPro) settings.proTier else null
    var selectedPlanId by remember(activeProTier) {
        mutableStateOf(if (activeProTier == "gold") "gold_monthly" else "platinum_quarterly")
    }
    var showCheckoutModal by remember { mutableStateOf(false) }

    val selectedPlan = remember(selectedPlanId) {
        PRO_TIER_PLANS.find { it.id == selectedPlanId } ?: PRO_TIER_PLANS.last()
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("pro_vault_tab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                                "ACTIVE: ${if (settings.proTier == "platinum") "PRO OBSIDIAN ELITE" else "PRO SILVER"} · VALID UNTIL $expiryFormatted"
                            } else {
                                "OBSIDIAN PRIVATE CLUB PASS (STANDARD)"
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
                                text = "PRO SPENDING VELOCITY & TRENDS",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.2.sp,
                                color = ChampagneGold
                            )
                            Text(
                                text = if (settings.isPro) "Unrestricted Autonomous Analytics Active" else "Private Banking Telemetry Preview",
                                fontFamily = headingFont,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmIvory,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Surface(
                        color = if (settings.isPro) SophisticatedEmerald.copy(alpha = 0.14f) else ChampagneGold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, if (settings.isPro) SophisticatedEmerald else ChampagneGold)
                    ) {
                        Text(
                            text = if (settings.isPro) "👑 UNLOCKED" else "🔒 PRO ACCESS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (settings.isPro) SophisticatedEmerald else ChampagneGold,
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
                        text = if (settings.isPro) {
                            "Neural telemetry verified: Spending velocity is within optimal parameters. Top outflow category: ${thisWeekExpenses.groupBy { it.category }.maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key ?: "None"}."
                        } else {
                            "Preview Mode: Upgrade to Kharchee PRO for uncapped neural telemetry, personalized budget forecasting, and daily audio spending audits."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedIvory,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // DEDICATED PRO MEMBERSHIP & UPGRADE SECTION
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
                                ChampagneGold.copy(alpha = 0.65f),
                                MutedPlatinum.copy(alpha = 0.35f),
                                ChampagneGold.copy(alpha = 0.55f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
                    .testTag("pro_membership_upgrade_section"),
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(ElevatedGraphite)
                                .border(0.8.dp, HairlineGold, RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Kharchee PRO Membership",
                                tint = ChampagneGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "KHARCHEE PRO VAULT MEMBERSHIP",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.2.sp,
                                color = ChampagneGold
                            )
                            Text(
                                text = "Sovereign Titanium Privileges & Uncapped J.A.R.V.I.S. AI",
                                fontFamily = headingFont,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmIvory
                            )
                        }
                    }
                }

                // Tier Cards: PRO Gold Edition (₹30/month) & PRO Platinum Elite (₹99/3 months)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PRO_TIER_PLANS.forEach { plan ->
                        val isSelected = selectedPlanId == plan.id
                        val isCurrentlyActive = settings.isPro && settings.proTier == plan.tierKey
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) ElevatedGraphite else ObsidianBlack)
                                .border(
                                    width = if (isSelected) 1.4.dp else 0.8.dp,
                                    brush = if (isSelected) {
                                        Brush.linearGradient(plan.rimColors)
                                    } else {
                                        Brush.linearGradient(listOf(HairlinePlatinum, HairlinePlatinum))
                                    },
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedPlanId = plan.id }
                                .padding(16.dp)
                                .testTag("pro_tier_card_${plan.id}")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = plan.accentColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.border(
                                            0.8.dp,
                                            plan.accentColor.copy(alpha = 0.45f),
                                            RoundedCornerShape(50)
                                        )
                                    ) {
                                        Text(
                                            text = if (isCurrentlyActive) "✓ ACTIVE PLAN" else plan.badgeText,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.8.sp,
                                            color = plan.accentColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = plan.priceDisplay,
                                        fontFamily = headingFont,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WarmIvory,
                                        fontSize = 20.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = plan.title,
                                            fontFamily = headingFont,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = WarmIvory,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = plan.tagline,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedPlatinum,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = plan.cycleLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedIvory,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected Tier",
                                            tint = plan.accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Features Highlighted Checklist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "INCLUDED PRIVATE BANKING PRIVILEGES",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = MutedPlatinum
                    )
                    PRO_FEATURE_HIGHLIGHTS.forEach { feat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElevatedGraphite)
                                    .border(0.8.dp, feat.tint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = feat.icon,
                                    contentDescription = feat.title,
                                    tint = feat.tint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = feat.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarmIvory,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = feat.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedIvory,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Premium Razorpay / UPI Activation Button with Sleek Champagne Gold Metallic Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ChampagneGold,
                                    ChampagneGoldSoft,
                                    MutedPlatinum,
                                    ChampagneGold
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = WarmIvory.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { showCheckoutModal = true }
                        .testTag("activate_pro_razorpay_upi_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Activate via Razorpay or UPI",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = "ACTIVATE ${selectedPlan.title.uppercase()} (${selectedPlan.priceDisplay}) · RAZORPAY / UPI",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = ObsidianBlack
                        )
                    }
                }
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

        // HUD Theme Switcher
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
                        Icons.Default.Palette,
                        contentDescription = "Themes",
                        tint = ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SOVEREIGN VAULT FINISHES",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = WarmIvory
                    )
                }

                ALL_HUD_THEMES.forEach { itemTheme ->
                    val isSelected = settings.themeId == itemTheme.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ElevatedGraphite else ObsidianBlack)
                            .border(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) itemTheme.accent else HairlinePlatinum,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectTheme(itemTheme.id) }
                            .padding(14.dp)
                            .testTag("select_theme_${itemTheme.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(itemTheme.accent.copy(alpha = 0.16f))
                                .border(0.8.dp, itemTheme.accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = itemTheme.symbol, color = itemTheme.accent, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = itemTheme.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = WarmIvory,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = itemTheme.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedIvory,
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Theme",
                                tint = itemTheme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

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
    }

    if (showCheckoutModal) {
        AlertDialog(
            onDismissRequest = { showCheckoutModal = false },
            containerColor = RichGraphite,
            title = {
                Text(
                    text = "KHARCHEE PRO · ${selectedPlan.title}",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Plan: ${selectedPlan.title} (${selectedPlan.priceDisplay})",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ChampagneGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = ElevatedGraphite,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.8.dp, HairlineGold, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "RAZORPAY CHECKOUT · SETUP / SANDBOX STATE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChampagneGold
                            )
                            Text(
                                text = "Live production activation requires server-side Razorpay signature verification (RAZORPAY_KEY_ID & HMAC webhook secret). When live Razorpay server keys are not configured, you can launch UPI checkout or preview Pro in Sandbox/Test mode.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedPlatinum,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Text(
                        text = "Includes J.A.R.V.I.S. Uncapped Voice Analytics, Three Independent HUD Themes, Unlimited Split Khata Ledgers, and Zero Ads.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedIvory
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val upiUri = Uri.parse(
                                "upi://pay?pa=kharchee.pro@okaxis&pn=Kharchee%20PRO%20Vault&am=${selectedPlan.amountInr}&cu=INR&tn=${Uri.encode(selectedPlan.title)}"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        } catch (_: Exception) {
                        }
                        showCheckoutModal = false
                        onUpgradeProPlan?.invoke(selectedPlan.tierKey, selectedPlan.durationDays)
                        onProActivated?.invoke("${selectedPlan.title} (${selectedPlan.priceDisplay} · Sandbox Preview)")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    )
                ) {
                    Text(
                        text = "Enable Sandbox Preview (${selectedPlan.priceDisplay})",
                        fontWeight = FontWeight.SemiBold,
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
