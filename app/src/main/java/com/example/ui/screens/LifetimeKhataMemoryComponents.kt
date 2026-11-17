package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.KhataCalendarEvent
import com.example.data.model.KhataContactComputedState
import com.example.data.model.KhataDirection
import com.example.data.model.KhataLedgerEntry
import com.example.data.model.LifetimeContactProfileMetrics
import com.example.data.model.LifetimeKhataIntelligenceSummary
import com.example.data.model.PeriodKhataActivity
import com.example.data.model.SmartSettleRecommendation
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KhataBrandPurple
import com.example.ui.theme.KhataBurgundyBorder
import com.example.ui.theme.KhataBurgundySurface
import com.example.ui.theme.KhataCardSurface
import com.example.ui.theme.KhataDeepBurgundy
import com.example.ui.theme.KhataDenaDarkGlassSurface
import com.example.ui.theme.KhataDenaPay
import com.example.ui.theme.KhataElevatedCardSurface
import com.example.ui.theme.KhataLenaDarkGlassSurface
import com.example.ui.theme.KhataLenaReceive
import com.example.ui.theme.KhataMutedText
import com.example.ui.theme.KhataNetPositionHighlight
import com.example.ui.theme.KhataPlatinumHighlight
import com.example.ui.theme.KhataPrimaryBackground
import com.example.ui.theme.KhataPrimaryText
import com.example.ui.theme.KhataSecondaryBackground
import com.example.ui.theme.KhataSecondaryText
import com.example.ui.theme.LocalAppFontFamily
import kotlin.math.abs
import kotlin.math.max

/**
 * PHASE B — Personal Khata Universe: LIFETIME KHATA PROFILE Card
 * Displays complete lifetime relationship metrics, actual earliest start date,
 * Khata Passport statement modal, and Smart Nudge triggers.
 */
@Composable
fun LifetimeContactProfileCard(
    state: KhataContactComputedState,
    profile: LifetimeContactProfileMetrics,
    stealthMode: Boolean,
    userUpiId: String = "",
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var showPassportDialog by remember { mutableStateOf(false) }

    val firstDateLabel = profile.firstRecordedDateMillis?.let {
        AdvancedKhataStore.formatShortDate(it)
    } ?: AdvancedKhataStore.formatShortDate(state.contact.createdAtMillis)

    val latestDateLabel = profile.mostRecentDateMillis?.let {
        AdvancedKhataStore.formatShortDate(it)
    } ?: "No entries yet"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        KhataDeepBurgundy,
                        KhataCardSurface,
                        KhataSecondaryBackground
                    )
                )
            )
            .border(1.2.dp, KhataBurgundyBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("lifetime_khata_profile_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Lifetime Khata Profile",
                    tint = KhataPlatinumHighlight,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "LIFETIME KHATA PROFILE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = KhataPlatinumHighlight
                )
            }

            Surface(
                color = KhataBurgundySurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.9.dp, KhataBrandPurple),
                modifier = Modifier
                    .clickable { showPassportDialog = true }
                    .testTag("open_khata_passport_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Khata Passport",
                        tint = KhataPrimaryText,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Khata Passport",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KhataPrimaryText
                    )
                }
            }
        }

        // Relationship Origin Banner
        Surface(
            color = KhataSecondaryBackground.copy(alpha = 0.85f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.8.dp, KhataBurgundyBorder.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lifetime_relationship_start_banner")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = profile.relationshipStartBanner,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KhataPrimaryText
                )
                Text(
                    text = "First recorded: $firstDateLabel  •  Latest activity: $latestDateLabel",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = KhataSecondaryText
                )
            }
        }

        // Row 1: Total Money Given vs Total Money Received
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LifetimeMetricTile(
                label = "TOTAL MONEY GIVEN",
                value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(profile.totalMoneyGiven)}",
                subtext = "Lent ₹${formatIndianAmount(profile.totalMoneyLent)} + Paid",
                accentColor = KhataLenaReceive,
                modifier = Modifier.weight(1f)
            )
            LifetimeMetricTile(
                label = "TOTAL MONEY RECEIVED",
                value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(profile.totalMoneyReceived)}",
                subtext = "Borrowed ₹${formatIndianAmount(profile.totalMoneyBorrowed)} + Recd",
                accentColor = KhataDenaPay,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Total Transactions, Total Repayments, Total Settlements
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LifetimeMetricTile(
                label = "TRANSACTIONS",
                value = "${profile.totalTransactionsCount}",
                subtext = "${profile.editedEntriesCount} edited · ${profile.proofAttachmentsCount} proofs",
                accentColor = KhataPlatinumHighlight,
                modifier = Modifier.weight(1f)
            )
            LifetimeMetricTile(
                label = "REPAYMENTS",
                value = "${profile.totalRepaymentsCount}",
                subtext = if (stealthMode) "₹••••" else "₹${formatIndianAmount(profile.totalRepaymentsAmount)}",
                accentColor = KhataLenaReceive,
                modifier = Modifier.weight(1f)
            )
            LifetimeMetricTile(
                label = "SETTLEMENTS",
                value = "${profile.totalSettlementsCount}",
                subtext = if (state.isSettled) "Settled" else "Active",
                accentColor = KhataNetPositionHighlight,
                modifier = Modifier.weight(1f)
            )
        }

        // Smart Nudge Quick Row when contact owes money
        if (!state.isSettled && state.netDirection == KhataDirection.LENA_HAI && state.outstandingAmount > 0L) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Nudge:",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KhataSecondaryText
                )
                listOf("Polite" to "polite", "Friendly" to "friendly", "Firm" to "firm").forEach { (label, toneKey) ->
                    Surface(
                        color = KhataElevatedCardSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, KhataBurgundyBorder),
                        modifier = Modifier.clickable {
                            val msg = AdvancedKhataStore.buildSmartNudgeMessage(
                                contactName = state.contact.name,
                                outstandingAmount = state.outstandingAmount,
                                tone = toneKey,
                                myUpiId = userUpiId
                            )
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Send Smart Nudge"))
                            } catch (_: Exception) {
                                onShowToast("Copied nudge message")
                            }
                        }
                    ) {
                        Text(
                            text = "💬 $label",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KhataPrimaryText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showPassportDialog) {
        val passportText = remember(state, profile) {
            AdvancedKhataStore.generateKhataPassportText(state, profile)
        }
        Dialog(onDismissRequest = { showPassportDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(KhataCardSurface)
                    .border(1.4.dp, KhataBurgundyBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .testTag("khata_passport_dialog"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🏛️ Lifetime Khata Passport",
                    fontFamily = headingFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPrimaryText
                )
                Text(
                    text = profile.relationshipStartBanner,
                    fontSize = 12.sp,
                    color = KhataSecondaryText
                )
                Surface(
                    color = KhataPrimaryBackground,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, KhataBurgundyBorder.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = passportText,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        color = KhataPrimaryText,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cb?.setPrimaryClip(ClipData.newPlainText("Khata Passport", passportText))
                            onShowToast("📋 Khata Passport copied to clipboard")
                        },
                        border = BorderStroke(1.dp, KhataBurgundyBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = KhataPrimaryText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", color = KhataPrimaryText, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, passportText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Khata Passport"))
                            } catch (_: Exception) {
                            }
                            showPassportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KhataBurgundySurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = KhataPrimaryText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Passport", color = KhataPrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LifetimeMetricTile(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(KhataPrimaryBackground.copy(alpha = 0.8f))
            .border(0.8.dp, KhataBurgundyBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = KhataMutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtext,
            fontSize = 9.sp,
            color = KhataSecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Compact Khata Intelligence Card shown on the main Khata Overview screen, matching the
 * Deep Burgundy concept reference ("Khata Intelligence • View Insights ->").
 */
@Composable
fun KhataIntelligenceCompactPreviewCard(
    intelligence: LifetimeKhataIntelligenceSummary,
    allContactStates: List<KhataContactComputedState>,
    stealthMode: Boolean,
    onOpenFullIntelligence: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val topContact = remember(allContactStates) {
        allContactStates
            .maxByOrNull { it.totalOriginalLenaHai + it.totalOriginalDenaHai + it.outstandingAmount }
            ?.contact?.name ?: "Add Contact"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Khata Intelligence",
                    tint = KhataPlatinumHighlight,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Khata Intelligence",
                    fontFamily = headingFont,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPrimaryText
                )
            }
            Text(
                text = "View Insights →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KhataBrandPurple,
                modifier = Modifier
                    .clickable { onOpenFullIntelligence() }
                    .testTag("khata_open_intelligence_link")
            )
        }

        Surface(
            color = KhataCardSurface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.1.dp, KhataBurgundyBorder.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenFullIntelligence() }
                .testTag("khata_intelligence_compact_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                KhataDeepBurgundy.copy(alpha = 0.65f),
                                KhataCardSurface,
                                KhataElevatedCardSurface
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(KhataBurgundySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = KhataBrandPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Lifetime Lending Volume",
                            fontSize = 11.sp,
                            color = KhataSecondaryText
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(intelligence.totalMoneyLent)}",
                                fontFamily = headingFont,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KhataPrimaryText
                            )
                            Surface(
                                color = KhataLenaDarkGlassSurface,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, KhataLenaReceive.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = "${intelligence.totalTransactionsCount} Tx",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KhataLenaReceive,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(KhataBurgundyBorder.copy(alpha = 0.5f))
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(0.85f)
                        .padding(start = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Top Contact",
                            fontSize = 11.sp,
                            color = KhataSecondaryText
                        )
                        Text(
                            text = topContact,
                            fontFamily = headingFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KhataPrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * PHASE C — Full Lifetime Financial Intelligence Dashboard
 * Strictly separates Money Movement (Udhaar / Repayments) from Personal Spending/Income.
 */
@Composable
fun LifetimeKhataIntelligenceDashboard(
    intelligence: LifetimeKhataIntelligenceSummary,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lifetime_khata_intelligence_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Jarvis Khata Intelligence Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            KhataDeepBurgundy,
                            KhataCardSurface,
                            KhataElevatedCardSurface
                        )
                    )
                )
                .border(1.2.dp, KhataBurgundyBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Jarvis Khata Intelligence",
                    tint = KhataBrandPurple,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "JARVIS LIFETIME KHATA INTELLIGENCE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPlatinumHighlight
                )
            }
            Text(
                text = intelligence.jarvisIntelligenceSummary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = KhataPrimaryText
            )
            Text(
                text = "Note: Lending, borrowing, and repayments are tracked as relationship money movement — never mixed into your personal expense burn rate.",
                fontSize = 10.sp,
                color = KhataMutedText
            )
        }

        // 8 Core Lifetime Metrics Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KhataCardSurface)
                .border(1.dp, KhataBurgundyBorder.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "LIFETIME MONEY MOVEMENT LEDGER",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPlatinumHighlight
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                LifetimeMetricTile(
                    label = "LIFETIME GIVEN",
                    value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(intelligence.lifetimeTotalGiven)}",
                    subtext = "Lent + Repayments Paid",
                    accentColor = KhataLenaReceive,
                    modifier = Modifier.weight(1f)
                )
                LifetimeMetricTile(
                    label = "LIFETIME RECEIVED",
                    value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(intelligence.lifetimeTotalReceived)}",
                    subtext = "Borrowed + Repayments In",
                    accentColor = KhataDenaPay,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                LifetimeMetricTile(
                    label = "TOTAL MONEY LENT",
                    value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(intelligence.totalMoneyLent)}",
                    subtext = "Lena Hai Originated",
                    accentColor = KhataLenaReceive,
                    modifier = Modifier.weight(1f)
                )
                LifetimeMetricTile(
                    label = "TOTAL BORROWED",
                    value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(intelligence.totalMoneyBorrowed)}",
                    subtext = "Dena Hai Originated",
                    accentColor = KhataDenaPay,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                LifetimeMetricTile(
                    label = "TOTAL AMOUNT REPAID",
                    value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(intelligence.totalAmountRepaid)}",
                    subtext = "In: ₹${formatIndianAmount(intelligence.totalRepaymentsReceived)} · Out: ₹${formatIndianAmount(intelligence.totalRepaymentsMade)}",
                    accentColor = KhataNetPositionHighlight,
                    modifier = Modifier.weight(1f)
                )
                LifetimeMetricTile(
                    label = "OUTSTANDING NOW",
                    value = if (stealthMode) "₹••••" else "₹${formatIndianAmount(intelligence.currentOutstandingBalance)}",
                    subtext = "Net: ${if (intelligence.netPositionSigned >= 0) "+" else "-"}₹${formatIndianAmount(abs(intelligence.netPositionSigned))}",
                    accentColor = if (intelligence.netPositionSigned >= 0) KhataLenaReceive else KhataDenaPay,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                LifetimeMetricTile(
                    label = "TOTAL TRANSACTIONS",
                    value = "${intelligence.totalTransactionsCount}",
                    subtext = "Permanent Cloud Records",
                    accentColor = KhataPrimaryText,
                    modifier = Modifier.weight(1f)
                )
                LifetimeMetricTile(
                    label = "TOTAL SETTLEMENTS",
                    value = "${intelligence.totalSettlementsCount}",
                    subtext = "Completed Cycles",
                    accentColor = KhataLenaReceive,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Outstanding Balance Over Time Canvas Chart
        if (intelligence.balanceTrendPoints.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(KhataCardSurface)
                    .border(1.dp, KhataBurgundyBorder.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "OUTSTANDING BALANCE OVER TIME",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPlatinumHighlight
                )
                val points = intelligence.balanceTrendPoints.takeLast(20)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val maxAbs = points.maxOfOrNull { abs(it.runningNetSignedBalance) }?.coerceAtLeast(100L) ?: 100L
                    val midY = size.height / 2f
                    // Zero baseline
                    drawLine(
                        color = Color(0xFF78334F).copy(alpha = 0.45f),
                        start = Offset(0f, midY),
                        end = Offset(size.width, midY),
                        strokeWidth = 2f
                    )
                    if (points.size == 1) {
                        val y = midY - (points[0].runningNetSignedBalance.toFloat() / maxAbs.toFloat()) * (size.height * 0.4f)
                        drawCircle(
                            color = Color(0xFF55D6A5),
                            radius = 8f,
                            center = Offset(size.width / 2f, y)
                        )
                    } else {
                        val path = Path()
                        points.forEachIndexed { idx, pt ->
                            val x = (idx.toFloat() / (points.size - 1).toFloat()) * size.width
                            val normalized = (pt.runningNetSignedBalance.toFloat() / maxAbs.toFloat()).coerceIn(-1f, 1f)
                            val y = midY - normalized * (size.height * 0.42f)
                            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            drawCircle(
                                color = if (pt.runningNetSignedBalance >= 0L) Color(0xFF55D6A5) else Color(0xFFE78AA5),
                                radius = 5f,
                                center = Offset(x, y)
                            )
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFD78BAE),
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start: ${points.first().label}",
                        fontSize = 10.sp,
                        color = KhataMutedText
                    )
                    Text(
                        text = "Latest: ${points.last().label}",
                        fontSize = 10.sp,
                        color = KhataMutedText
                    )
                }
            }
        }

        // Year-wise Financial Activity
        if (intelligence.yearlyActivity.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(KhataCardSurface)
                    .border(1.dp, KhataBurgundyBorder.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "YEAR-WISE KHATA HISTORY (NEVER RESETS)",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPlatinumHighlight
                )
                intelligence.yearlyActivity.forEach { period ->
                    PeriodActivityRow(period = period, stealthMode = stealthMode)
                }
            }
        }

        // Month-wise Financial Activity
        if (intelligence.monthlyActivity.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(KhataCardSurface)
                    .border(1.dp, KhataBurgundyBorder.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MONTH-WISE KHATA ACTIVITY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPlatinumHighlight
                )
                intelligence.monthlyActivity.take(12).forEach { period ->
                    PeriodActivityRow(period = period, stealthMode = stealthMode)
                }
            }
        }
    }
}

@Composable
private fun PeriodActivityRow(
    period: PeriodKhataActivity,
    stealthMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KhataPrimaryBackground)
            .border(0.8.dp, KhataBurgundyBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = period.periodLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPrimaryText
            )
            Text(
                text = "${period.transactionCount} transactions • ${period.settlementCount} settlements",
                fontSize = 10.sp,
                color = KhataSecondaryText
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (stealthMode) "Lent: ₹•••" else "Lent: ₹${formatIndianAmount(period.totalLent)}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = KhataLenaReceive
            )
            Text(
                text = if (stealthMode) "Borrowed: ₹•••" else "Borrowed: ₹${formatIndianAmount(period.totalBorrowed)}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = KhataDenaPay
            )
        }
    }
}

/**
 * PHASE E — Smart Khata Tools Studio:
 * 1. Voice / Natural-Language Entry Studio (with confirmation before saving)
 * 2. Smart Settle Recommendations
 * 3. Khata Calendar (Transactions & Due Reminders)
 * 4. Lifetime Khata Backup / Export (JSON & Share)
 */
@Composable
fun SmartKhataToolsSection(
    userId: String,
    allContactStates: List<KhataContactComputedState>,
    stealthMode: Boolean,
    userUpiId: String = "",
    onRefreshTriggered: () -> Unit,
    onOpenContact: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current

    var voiceInputText by remember { mutableStateOf("") }
    val voiceDraft = remember(voiceInputText) {
        AdvancedKhataStore.parseVoiceEntryDraft(voiceInputText)
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()?.trim().orEmpty()
            if (spoken.isNotEmpty()) {
                voiceInputText = spoken
            }
        }
    }

    val smartSettleRecommendations = remember(allContactStates) {
        AdvancedKhataStore.computeSmartSettleRecommendations(allContactStates)
    }

    val calendarEvents = remember(allContactStates) {
        AdvancedKhataStore.computeKhataCalendarEvents(allContactStates)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_khata_tools_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. VOICE ENTRY STUDIO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KhataCardSurface)
                .border(1.2.dp, KhataBurgundyBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎙️ VOICE & NATURAL LANGUAGE ENTRY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KhataPlatinumHighlight
                    )
                    Text(
                        text = "Say or type: \"Gave 500 to Aman for dinner\"",
                        fontSize = 11.sp,
                        color = KhataSecondaryText
                    )
                }
                Surface(
                    color = KhataBurgundySurface,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, KhataBrandPurple),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Khata entry (e.g. Gave 500 to Aman for dinner)")
                                }
                                speechLauncher.launch(intent)
                            } catch (_: Exception) {
                                onShowToast("Voice input not available on this device; type your phrase below")
                            }
                        }
                        .testTag("khata_voice_mic_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = KhataPrimaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = voiceInputText,
                onValueChange = { voiceInputText = it },
                placeholder = {
                    Text("e.g., Gave 500 to Aman for dinner / Borrowed 300 from Sneha", color = KhataMutedText, fontSize = 12.sp)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KhataBrandPurple,
                    unfocusedBorderColor = KhataBurgundyBorder.copy(alpha = 0.6f),
                    focusedContainerColor = KhataPrimaryBackground,
                    unfocusedContainerColor = KhataPrimaryBackground,
                    focusedTextColor = KhataPrimaryText,
                    unfocusedTextColor = KhataPrimaryText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("khata_voice_draft_input")
            )

            if (voiceInputText.isNotBlank()) {
                Surface(
                    color = KhataPrimaryBackground,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (voiceDraft.isValid) KhataLenaReceive else KhataBurgundyBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DRAFT PREVIEW • ${voiceDraft.parseSummary}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (voiceDraft.isValid) KhataLenaReceive else KhataPlatinumHighlight
                        )
                        if (voiceDraft.isValid) {
                            Button(
                                onClick = {
                                    val res = AdvancedKhataStore.addUdhaarEntry(
                                        context = context,
                                        userId = userId,
                                        personName = voiceDraft.personName,
                                        amount = voiceDraft.amount,
                                        direction = voiceDraft.direction,
                                        description = voiceDraft.description,
                                        category = voiceDraft.category
                                    )
                                    if (res.isSuccess) {
                                        voiceInputText = ""
                                        onRefreshTriggered()
                                        onShowToast("✅ Confirmed & saved ₹${voiceDraft.amount} for ${voiceDraft.personName}")
                                    } else {
                                        onShowToast(res.exceptionOrNull()?.message ?: "Could not save draft")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KhataBurgundySurface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("khata_confirm_voice_draft_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KhataLenaReceive, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Confirm & Save (${voiceDraft.direction.label} ₹${voiceDraft.amount} · ${voiceDraft.personName})",
                                    color = KhataPrimaryText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. SMART SETTLE RECOMMENDATIONS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KhataCardSurface)
                .border(1.dp, KhataBurgundyBorder.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "⚡ SMART SETTLE ENGINE (${smartSettleRecommendations.size} ACTIVE)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPlatinumHighlight
            )
            if (smartSettleRecommendations.isEmpty()) {
                Text(
                    text = "All contacts are currently settled! No pending balances to simplify.",
                    fontSize = 12.sp,
                    color = KhataSecondaryText
                )
            } else {
                smartSettleRecommendations.forEach { rec ->
                    SmartSettleRecommendationRow(
                        rec = rec,
                        stealthMode = stealthMode,
                        userUpiId = userUpiId,
                        onSettleNow = {
                            val res = AdvancedKhataStore.settleContactInFull(
                                context = context,
                                userId = userId,
                                contactId = rec.contactId,
                                note = "Settled via Smart Settle Engine"
                            )
                            if (res.isSuccess) {
                                onRefreshTriggered()
                                onShowToast("✅ ${rec.contactName} settled in full!")
                            }
                        },
                        onOpenContact = { onOpenContact(rec.contactId) },
                        onShowToast = onShowToast
                    )
                }
            }
        }

        // 3. KHATA CALENDAR (TRANSACTIONS & DUE REMINDERS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KhataCardSurface)
                .border(1.dp, KhataBurgundyBorder.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Khata Calendar",
                    tint = KhataPlatinumHighlight,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "KHATA CALENDAR & PROMISE REMINDERS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPlatinumHighlight
                )
            }
            if (calendarEvents.isEmpty()) {
                Text(
                    text = "No scheduled due dates or calendar events yet.",
                    fontSize = 12.sp,
                    color = KhataSecondaryText
                )
            } else {
                calendarEvents.take(10).forEach { ev ->
                    KhataCalendarEventRow(
                        event = ev,
                        stealthMode = stealthMode,
                        onClick = { onOpenContact(ev.contactId) }
                    )
                }
            }
        }

        // 4. LIFETIME KHATA BACKUP & EXPORT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KhataCardSurface)
                .border(1.dp, KhataBurgundyBorder.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🛡️ LIFETIME KHATA EXPORT & PORTABLE BACKUP",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPlatinumHighlight
            )
            Text(
                text = "Export your complete Lifetime Khata Memory (contacts, transactions, repayments, and audit trail) as portable JSON or shareable ledger summary.",
                fontSize = 12.sp,
                color = KhataSecondaryText
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val json = AdvancedKhataStore.exportLifetimeKhataJsonBackup(context, userId)
                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cb?.setPrimaryClip(ClipData.newPlainText("Kharchee Lifetime Khata JSON", json))
                        onShowToast("📦 Lifetime Khata JSON Backup copied to clipboard")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KhataBurgundySurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("khata_export_json_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = KhataPrimaryText, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy JSON Backup", color = KhataPrimaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val json = AdvancedKhataStore.exportLifetimeKhataJsonBackup(context, userId)
                        try {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Kharchee Lifetime Khata Backup")
                                putExtra(Intent.EXTRA_TEXT, json)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Lifetime Khata Backup"))
                        } catch (_: Exception) {
                        }
                    },
                    border = BorderStroke(1.dp, KhataBurgundyBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = KhataPrimaryText, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Backup", color = KhataPrimaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SmartSettleRecommendationRow(
    rec: SmartSettleRecommendation,
    stealthMode: Boolean,
    userUpiId: String,
    onSettleNow: () -> Unit,
    onOpenContact: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val accent = if (rec.direction == KhataDirection.LENA_HAI) KhataLenaReceive else KhataDenaPay

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(KhataPrimaryBackground)
            .border(0.9.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .clickable { onOpenContact() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "${rec.contactName} • ${if (stealthMode) "₹••••" else "₹${formatIndianAmount(rec.outstandingAmount)}"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPrimaryText
            )
            Text(
                text = rec.priorityReason,
                fontSize = 11.sp,
                color = KhataSecondaryText
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (rec.direction == KhataDirection.LENA_HAI) {
                Surface(
                    color = KhataElevatedCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, KhataBurgundyBorder),
                    modifier = Modifier.clickable {
                        val msg = AdvancedKhataStore.buildSmartNudgeMessage(
                            contactName = rec.contactName,
                            outstandingAmount = rec.outstandingAmount,
                            tone = "friendly",
                            myUpiId = userUpiId
                        )
                        try {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Send Nudge"))
                        } catch (_: Exception) {
                            onShowToast("Nudge ready")
                        }
                    }
                ) {
                    Text(
                        text = "Nudge",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = KhataPlatinumHighlight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Surface(
                color = KhataBurgundySurface,
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.9.dp, accent),
                modifier = Modifier.clickable { onSettleNow() }
            ) {
                Text(
                    text = "1-Tap Settle",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPrimaryText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun KhataCalendarEventRow(
    event: KhataCalendarEvent,
    stealthMode: Boolean,
    onClick: () -> Unit
) {
    val accent = when {
        event.isOverdue -> KhataDenaPay
        event.isDueReminder -> KhataBrandPurple
        event.direction == KhataDirection.LENA_HAI -> KhataLenaReceive
        else -> KhataDenaPay
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KhataPrimaryBackground)
            .border(0.8.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${event.dateLabel} • ${event.title}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KhataPrimaryText
            )
            Text(
                text = event.subtitle,
                fontSize = 11.sp,
                color = KhataSecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(event.amount)}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
    }
}

/**
 * PHASE E — Proof Vault Dialog: Attaches a receipt photo (via zero-permission Android Photo Picker)
 * or receipt reference note to a Khata entry.
 */
@Composable
fun AttachKhataProofDialog(
    entry: KhataLedgerEntry,
    onDismiss: () -> Unit,
    onAttachProof: (uri: String, label: String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var selectedUri by remember(entry) { mutableStateOf(entry.proofAttachmentUri) }
    var proofLabel by remember(entry) {
        mutableStateOf(entry.proofAttachmentLabel.ifBlank { "UPI Screenshot / Bill Receipt" })
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(KhataCardSurface)
                .border(1.4.dp, KhataBurgundyBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("khata_proof_vault_dialog"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📎 Proof Vault · Attach Receipt",
                fontFamily = headingFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPrimaryText
            )
            Text(
                text = "Entry: ₹${formatIndianAmount(entry.amount)} (${entry.description}) with ${entry.contactName}",
                fontSize = 12.sp,
                color = KhataSecondaryText
            )

            OutlinedTextField(
                value = proofLabel,
                onValueChange = { proofLabel = it },
                label = { Text("Proof Label / UTR Reference", color = KhataSecondaryText) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KhataBrandPurple,
                    unfocusedBorderColor = KhataBurgundyBorder,
                    focusedTextColor = KhataPrimaryText,
                    unfocusedTextColor = KhataPrimaryText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                border = BorderStroke(1.dp, KhataBrandPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = KhataPlatinumHighlight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedUri.isNotBlank()) "Receipt Image Selected ✓ (Tap to Change)" else "Pick Screenshot / Receipt Image",
                    color = KhataPrimaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, KhataBurgundyBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = KhataSecondaryText)
                }
                Button(
                    onClick = {
                        val finalUri = selectedUri.ifBlank { "proof://utr/${System.currentTimeMillis()}" }
                        onAttachProof(finalUri, proofLabel.trim().ifBlank { "Receipt Proof" })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KhataBurgundySurface),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_attach_proof_button")
                ) {
                    Text("Save Proof", color = KhataPrimaryText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
