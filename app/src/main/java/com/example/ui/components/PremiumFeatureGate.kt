package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FirestorePremiumEntitlementState
import com.example.data.model.PremiumAccessDecision
import com.example.data.model.PremiumFeature
import com.example.data.model.PremiumLockReason
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily

private val ProGateSurface = Color(0xFF0A0D0A)
private val ProGateCard = Color(0xFF101510)
private val ProGateLime = Color(0xFFB7F000)
private val ProGateBorder = Color(0xFF233020)

/**
 * Conditional Compose gating wrapper backed by live Firestore entitlement state.
 * Renders [unlockedContent] when Firestore confirms an active Pro plan, or displays
 * a polished "Upgrade to Pro" lock card for non-premium / expired / pending users.
 */
@Composable
fun PremiumFeatureGate(
    feature: PremiumFeature,
    entitlementState: FirestorePremiumEntitlementState,
    onUpgradeToPro: () -> Unit,
    modifier: Modifier = Modifier,
    previewSummary: String? = null,
    unlockedContent: @Composable () -> Unit
) {
    val decision = entitlementState.evaluateAccess(feature)
    when (decision) {
        is PremiumAccessDecision.Granted -> unlockedContent()
        is PremiumAccessDecision.Locked -> {
            UpgradeToProGateCard(
                decision = decision,
                statusBadge = entitlementState.statusBadgeLabel(),
                previewSummary = previewSummary,
                onUpgradeToPro = onUpgradeToPro,
                modifier = modifier
            )
        }
    }
}

@Composable
fun UpgradeToProGateCard(
    decision: PremiumAccessDecision.Locked,
    statusBadge: String,
    previewSummary: String? = null,
    onUpgradeToPro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val feature = decision.feature
    val accentColor = when (decision.reason) {
        PremiumLockReason.SUBSCRIPTION_EXPIRED -> Color(0xFFFFB020)
        PremiumLockReason.PENDING_VERIFICATION -> Color(0xFF38BDF8)
        else -> ProGateLime
    }

    Surface(
        color = ProGateSurface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("premium_gate_card_${feature.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF131B11),
                            ProGateCard,
                            Color(0xFF080B08)
                        )
                    )
                )
                .padding(18.dp),
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.14f))
                            .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Premium feature locked",
                            tint = accentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = feature.title,
                            fontFamily = headingFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "KHARCHEE PRO · FIRESTORE VERIFIED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = ChampagneGold
                        )
                    }
                }

                Surface(
                    color = accentColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.55f)),
                    modifier = Modifier.testTag("premium_gate_badge_${feature.id}")
                ) {
                    Text(
                        text = statusBadge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = decision.ctaMessage,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFFD7E2D4)
            )

            if (!previewSummary.isNullOrBlank()) {
                Surface(
                    color = Color(0xFF0C120B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, ProGateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = previewSummary,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFFC5D2C1)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                feature.highlightBullets.forEach { bullet ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ProGateLime,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = bullet,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFFE6EFE3)
                        )
                    }
                }
            }

            Button(
                onClick = onUpgradeToPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProGateLime,
                    contentColor = Color(0xFF080B08)
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("upgrade_to_pro_cta_${feature.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = decision.ctaTitle,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Compact banner displayed on Analytics for non-premium users with 1-tap Upgrade to Pro CTA.
 */
@Composable
fun UpgradeToProCompactBanner(
    entitlementState: FirestorePremiumEntitlementState,
    onUpgradeToPro: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (entitlementState.isProActive) return
    val headingFont = LocalAppFontFamily.current
    val subtitle = when {
        entitlementState.hasPendingVerification ->
            "Verification pending in Firestore — Pro unlocks automatically once approved."
        entitlementState.isExpired ->
            "Your Pro access expired. Renew to unlock full analytics & statement exports."
        else ->
            "Unlock Cashflow Spline, 30-Day Heatmap, Speedometers & PDF/CSV exports."
    }

    Surface(
        color = Color(0xFF10160F),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ProGateLime.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("insights_upgrade_to_pro_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ProGateLime.copy(alpha = 0.14f))
                    .border(1.dp, ProGateLime.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "KHARCHEE Pro",
                    tint = ProGateLime,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (entitlementState.isExpired) "KHARCHEE Pro Expired" else "Upgrade to KHARCHEE Pro",
                    fontFamily = headingFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFFB8C9B4)
                )
            }
            Button(
                onClick = onUpgradeToPro,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProGateLime,
                    contentColor = Color(0xFF080B08)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("insights_banner_upgrade_button")
            ) {
                Text(
                    text = "Upgrade",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Modal Upgrade to Pro prompt displayed when a non-premium user taps a gated control
 * such as Custom Date Filters or Report Export.
 */
@Composable
fun UpgradeToProPromptDialog(
    feature: PremiumFeature,
    entitlementState: FirestorePremiumEntitlementState,
    onUpgradeToPro: () -> Unit,
    onDismiss: () -> Unit
) {
    val decision = entitlementState.evaluateAccess(feature)
    val lockedDecision = decision as? PremiumAccessDecision.Locked ?: return
    val headingFont = LocalAppFontFamily.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = ProGateSurface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.2.dp, ProGateLime.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upgrade_to_pro_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ProGateLime.copy(alpha = 0.16f))
                                .border(1.dp, ProGateLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Pro Feature",
                                tint = ProGateLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Upgrade to Pro",
                                fontFamily = headingFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = feature.title,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChampagneGold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("upgrade_to_pro_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Upgrade Prompt",
                            tint = Color(0xFFB8C9B4)
                        )
                    }
                }

                Text(
                    text = lockedDecision.ctaMessage,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFFDCE7D9)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    feature.highlightBullets.forEach { bullet ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ProGateLime,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = bullet,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onUpgradeToPro()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProGateLime,
                        contentColor = Color(0xFF080B08)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("upgrade_to_pro_dialog_cta")
                ) {
                    Text(
                        text = lockedDecision.ctaTitle,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, ProGateBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("upgrade_to_pro_dialog_dismiss")
                ) {
                    Text(
                        text = "Not Now",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB8C9B4)
                    )
                }
            }
        }
    }
}
