package com.example.ui.jarvis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily

// Deep Navy + Black + Purple/Violet AI Command Center Palette
internal val JarvisAbyssBlack = Color(0xFF05050A)
internal val JarvisDeepNavyTop = Color(0xFF14092E)
internal val JarvisDeepNavyMid = Color(0xFF0D0E17)
internal val JarvisReactorBlack = Color(0xFF05050A)
internal val JarvisPanelBlack = Color(0xFF0D0E17)
internal val JarvisElevatedSurface = Color(0xFF151329)
internal val JarvisUserBubbleStart = Color(0xFF281554)
internal val JarvisUserBubbleEnd = Color(0xFF180C36)
internal val JarvisCyanPrimary = Color(0xFFA855F7)      // Electric Purple/Violet primary accent
internal val JarvisCyanBright = Color(0xFFD8B4FE)       // Luminous Lavender-Violet highlight
internal val JarvisCyanAccent = Color(0xFF7C3AED)       // Deep Royal Violet accent
internal val JarvisCyanDeep = Color(0xFF4C1D95)         // Cosmic Indigo-Violet depth
internal val JarvisHologramCyan = Color(0xFFA855F7)     // Unified Violet accent
internal val JarvisHairlineCyan = Color(0xFF2B224C)     // Subtle Violet-Navy hairline border
internal val JarvisTextWhite = Color(0xFFF7F4FF)
internal val JarvisTextSecondary = Color(0xFFB8B0D4)
internal val JarvisAlertCrimson = Color(0xFFFF4D8D)
internal val JarvisEmeraldAccent = Color(0xFF2EE59D)

/**
 * Clean canonical status state for the JARVIS AI Core.
 */
enum class JarvisCoreStatus(val label: String) {
    ONLINE("ONLINE"),
    LISTENING("LISTENING"),
    THINKING("THINKING"),
    SPEAKING("SPEAKING")
}

fun resolveJarvisCoreStatus(
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean
): JarvisCoreStatus = when {
    isListening -> JarvisCoreStatus.LISTENING
    isThinking -> JarvisCoreStatus.THINKING
    isSpeaking -> JarvisCoreStatus.SPEAKING
    else -> JarvisCoreStatus.ONLINE
}

/**
 * Represents a structured, confirmation-gated action proposed by J.A.R.V.I.S.
 */
data class JarvisProposedAction(
    val id: String,
    val actionType: String, // "LOG_EXPENSE", "LOG_INCOME", "SET_DAILY_LIMIT", "TOGGLE_LOCKDOWN", "NAVIGATE_TAB"
    val title: String,
    val summary: String,
    val amount: Long = 0L,
    val category: String = "Food",
    val targetTabId: String = "home",
    val booleanFlag: Boolean = true
)

/**
 * Parses natural language user prompts (English, Hindi, Hinglish) to detect when the user is
 * asking J.A.R.V.I.S. to perform an in-app action, returning a confirmation-gated proposal.
 */
fun parseNaturalLanguageActionProposal(rawPrompt: String): JarvisProposedAction? {
    val clean = rawPrompt.trim()
    val lower = clean.lowercase()

    if (lower.contains("open ") || lower.contains("go to ") || lower.contains("show ") || lower.contains("dikhao")) {
        when {
            lower.contains("khata") || lower.contains("split") || lower.contains("udhaar") -> {
                return JarvisProposedAction(
                    id = "nav_khata_${System.currentTimeMillis()}",
                    actionType = "NAVIGATE_TAB",
                    title = "Navigate to Khata Split Ledger",
                    summary = "Switch screen to Khata Ledger to inspect friend balances & WhatsApp reminders.",
                    targetTabId = "khata"
                )
            }
            lower.contains("insight") || lower.contains("analytic") || lower.contains("chart") || lower.contains("graph") -> {
                return JarvisProposedAction(
                    id = "nav_insights_${System.currentTimeMillis()}",
                    actionType = "NAVIGATE_TAB",
                    title = "Open Wealth Analytics & Charts",
                    summary = "Switch screen to 7-Day & 30-Day Capital Velocity Analytics.",
                    targetTabId = "insights"
                )
            }
            lower.contains("history") || lower.contains("passbook") -> {
                return JarvisProposedAction(
                    id = "nav_history_${System.currentTimeMillis()}",
                    actionType = "NAVIGATE_TAB",
                    title = "Open Passbook History",
                    summary = "Switch screen to full searchable transaction passbook.",
                    targetTabId = "history"
                )
            }
            lower.contains("pro ") || lower.contains("private club") || lower.contains("membership") -> {
                return JarvisProposedAction(
                    id = "nav_pro_${System.currentTimeMillis()}",
                    actionType = "NAVIGATE_TAB",
                    title = "Open Kharchee Private Club (PRO)",
                    summary = "Switch screen to Kharchee PRO membership & HUD theme vault.",
                    targetTabId = "pro"
                )
            }
        }
    }

    val numberRegex = Regex("""(?:₹|rs\.?|inr)?\s*(\d{2,6})""", RegexOption.IGNORE_CASE)
    if ((lower.contains("daily limit") || lower.contains("spending limit") || lower.contains("reserve limit")) &&
        (lower.contains("set") || lower.contains("change") || lower.contains("update") || lower.contains("kar do"))
    ) {
        val match = numberRegex.find(clean)
        val parsedAmount = match?.groupValues?.getOrNull(1)?.toLongOrNull()
        if (parsedAmount != null && parsedAmount in 50L..50_000L) {
            return JarvisProposedAction(
                id = "limit_${System.currentTimeMillis()}",
                actionType = "SET_DAILY_LIMIT",
                title = "Update Daily Reserve Limit to ₹$parsedAmount",
                summary = "J.A.R.V.I.S. will recalibrate your daily spending ceiling to ₹$parsedAmount/day (₹${parsedAmount * 30}/month).",
                amount = parsedAmount
            )
        }
    }

    if (lower.contains("lockdown")) {
        val enable = !(lower.contains("off") || lower.contains("disable") || lower.contains("unlock") || lower.contains("lift"))
        return JarvisProposedAction(
            id = "lockdown_${System.currentTimeMillis()}",
            actionType = "TOGGLE_LOCKDOWN",
            title = if (enable) "Engage Emergency Lockdown Shield" else "Lift Emergency Lockdown Shield",
            summary = if (enable) {
                "Lock new outflow logging to protect your remaining daily reserve."
            } else {
                "Restore normal transaction logging across your Kharchee vault."
            },
            booleanFlag = enable
        )
    }

    val isExpenseTrigger = lower.startsWith("log ") ||
        lower.startsWith("add expense") ||
        lower.startsWith("spent ") ||
        lower.contains("kharcha add") ||
        lower.contains("log expense")
    if (isExpenseTrigger) {
        val match = numberRegex.find(clean)
        val parsedAmount = match?.groupValues?.getOrNull(1)?.toLongOrNull()
        if (parsedAmount != null && parsedAmount > 0L) {
            val inferredCategory = when {
                lower.contains("chai") || lower.contains("coffee") || lower.contains("tea") || lower.contains("snack") || lower.contains("samosa") -> "Chai & Snacks"
                lower.contains("uber") || lower.contains("ola") || lower.contains("auto") || lower.contains("metro") || lower.contains("petrol") || lower.contains("fuel") -> "Transport"
                lower.contains("grocery") || lower.contains("milk") || lower.contains("blinkit") || lower.contains("zepto") || lower.contains("instamart") -> "Grocery"
                lower.contains("bill") || lower.contains("recharge") || lower.contains("wifi") || lower.contains("electricity") -> "Bills"
                lower.contains("movie") || lower.contains("netflix") || lower.contains("game") -> "Entertainment"
                else -> "Food"
            }
            val cleanTitle = clean
                .replace(Regex("""(?i)^(log|add expense|spent)\s*"""), "")
                .replace(Regex("""(?i)(₹|rs\.?|inr)?\s*\d+\s*(for|on|in)?\s*"""), "")
                .trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                .ifBlank { "$inferredCategory Spend" }

            return JarvisProposedAction(
                id = "tx_${System.currentTimeMillis()}",
                actionType = "LOG_EXPENSE",
                title = "Log ₹$parsedAmount · $cleanTitle",
                summary = "Record UPI expense of ₹$parsedAmount under '$inferredCategory' in your live ledger.",
                amount = parsedAmount,
                category = inferredCategory
            )
        }
    }

    return null
}

/**
 * Lightweight Circular J.A.R.V.I.S. Visual Emblem (Zero infinite animations).
 */
@Composable
fun JarvisCircularReferenceBadge(
    size: Dp,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(JarvisReactorBlack)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(JarvisCyanBright, JarvisCyanPrimary, JarvisCyanAccent)
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.jarvis_reactor_core),
            contentDescription = "J.A.R.V.I.S. Circular Core",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

/**
 * Confirmation card rendered inside the chat when J.A.R.V.I.S. proposes a sensitive action.
 */
@Composable
fun JarvisProposedActionConfirmationCard(
    proposedAction: JarvisProposedAction,
    onConfirm: (JarvisProposedAction) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(JarvisElevatedSurface)
            .border(1.dp, JarvisCyanPrimary.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("jarvis_action_confirmation_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Action Confirmation",
                tint = JarvisCyanBright,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "CONFIRMATION REQUIRED",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                color = JarvisCyanBright
            )
        }

        Text(
            text = proposedAction.title,
            fontFamily = headingFont,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = JarvisTextWhite
        )

        Text(
            text = proposedAction.summary,
            fontSize = 12.sp,
            color = JarvisTextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onConfirm(proposedAction) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyanAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("jarvis_confirm_action_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Confirm Action",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Confirm",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, JarvisHairlineCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisTextSecondary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("jarvis_cancel_action_button")
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Lightweight Floating Arc Reactor Trigger button displayed on the bottom-right across all tabs.
 * Zero infinite Canvas loops — instant response on click.
 */
@Composable
fun FloatingJarvisArcReactorTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGenerating: Boolean = false
) {
    Box(
        modifier = modifier
            .size(62.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = JarvisCyanPrimary,
                spotColor = JarvisCyanAccent
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        JarvisDeepNavyTop,
                        JarvisReactorBlack
                    )
                )
            )
            .border(
                width = 1.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        JarvisCyanBright,
                        JarvisCyanPrimary,
                        JarvisCyanAccent
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("floating_jarvis_arc_reactor_trigger"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.jarvis_reactor_core),
            contentDescription = "Open J.A.R.V.I.S. AI Assistant",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
        )

        Surface(
            color = JarvisReactorBlack.copy(alpha = 0.90f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(0.8.dp, if (isGenerating) JarvisEmeraldAccent else JarvisCyanBright),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isGenerating) "SYNC" else "JARVIS",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
                color = if (isGenerating) JarvisEmeraldAccent else JarvisCyanBright,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
    }
}
