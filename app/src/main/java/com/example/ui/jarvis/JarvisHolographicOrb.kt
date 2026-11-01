package com.example.ui.jarvis

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a structured, confirmation-gated action proposed by J.A.R.V.I.S.
 * Sensitive mutations (logging transactions, updating daily limits, toggling lockdown, or navigating)
 * require explicit user confirmation before execution.
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

    // 1. Detect Navigation requests ("open khata", "go to analytics", "show passbook", "open pro")
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

    // 2. Detect Daily Limit update ("set daily limit to 800", "change limit to ₹1200", "daily limit 600 kar do")
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

    // 3. Detect Lockdown Shield toggle ("activate lockdown", "turn on lockdown", "unlock vault")
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

    // 4. Detect Expense / Income logging ("log ₹120 for chai", "add expense 350 for lunch", "spent 250 on uber")
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
                summary = "Record UPI expense of ₹$parsedAmount under '$inferredCategory' in your live Firestore ledger.",
                amount = parsedAmount,
                category = inferredCategory
            )
        }
    }

    return null
}

/**
 * Tony Stark J.A.R.V.I.S.-inspired Central Holographic AI Orb & Reactive Voice Visualizer.
 * Responds dynamically to STANDBY, LISTENING, THINKING, and SPEAKING states with
 * counter-rotating concentric emerald telemetry rings, plasma core glow, and a 20-bar
 * acoustic frequency spectrum.
 */
@Composable
fun JarvisHolographicOrbVisualizer(
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    activeMode: String,
    activeLanguage: String,
    lastSpokenPreview: String,
    onToggleMic: () -> Unit,
    onStopSpeaking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    val infiniteTransition = rememberInfiniteTransition(label = "jarvisHoloOrb")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRingRotation"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRingRotation"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening || isSpeaking || isThinking) 700 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    val primaryOrbColor = when {
        isListening -> CrimsonRuby
        isSpeaking -> ChampagneGold
        isThinking -> EmeraldNeonGlow
        else -> SophisticatedEmerald
    }

    val stateTitle = when {
        isListening -> "🎙️ VOICE RECEPTOR ACTIVE · LISTENING"
        isSpeaking -> "🔊 NEURAL VOICE SYNTHESIS · SPEAKING"
        isThinking -> "⚡ QUANTUM LEDGER ANALYSIS IN PROGRESS"
        else -> "◆ J.A.R.V.I.S. HOLOGRAPHIC CORE ONLINE"
    }

    val stateSubtitle = when {
        isListening -> "Speak naturally in Hindi, Hinglish, or English..."
        isSpeaking -> lastSpokenPreview.take(90).ifBlank { "Delivering live financial briefing..." }
        isThinking -> "Scanning real-time Firestore transactions, daily reserve & Khata balances..."
        else -> "Tap the central orb or microphone to converse hands-free"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepForestAbyss,
                        RichGraphite,
                        ObsidianBlack
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryOrbColor.copy(alpha = 0.7f),
                        HairlinePlatinum,
                        primaryOrbColor.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("jarvis_holographic_orb_card"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Telemetry Status Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = primaryOrbColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.8.dp, primaryOrbColor.copy(alpha = 0.45f))
            ) {
                Text(
                    text = stateTitle,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp,
                    color = primaryOrbColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "${activeMode.uppercase()} · ${activeLanguage.uppercase()}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MutedPlatinum
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Central Animated Holographic Orb + Concentric Telemetry Rings
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clickable {
                        if (isSpeaking) onStopSpeaking() else onToggleMic()
                    }
                    .testTag("jarvis_central_orb_button"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension / 2f

                    // Outer Radial Holographic Aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryOrbColor.copy(alpha = 0.28f + 0.16f * corePulse),
                                SophisticatedEmerald.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = maxR
                        ),
                        center = centerOffset,
                        radius = maxR
                    )

                    // Outer Segmented Telemetry Ring (Clockwise)
                    rotate(degrees = outerRotation, pivot = centerOffset) {
                        drawCircle(
                            color = primaryOrbColor.copy(alpha = 0.55f),
                            radius = maxR * 0.88f,
                            center = centerOffset,
                            style = Stroke(
                                width = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f, 6f, 12f), 0f)
                            )
                        )
                    }

                    // Middle Orbital Arc Ring (Counter-Clockwise)
                    rotate(degrees = innerRotation, pivot = centerOffset) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    primaryOrbColor,
                                    EmeraldNeonGlow,
                                    Color.Transparent
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - maxR * 0.70f, centerOffset.y - maxR * 0.70f),
                            size = Size(maxR * 1.40f, maxR * 1.40f),
                            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Cardinal Holographic Crosshair Ticks
                    for (angleDeg in listOf(0f, 90f, 180f, 270f)) {
                        val rad = Math.toRadians((angleDeg + outerRotation * 0.5f).toDouble())
                        val rStart = maxR * 0.52f
                        val rEnd = maxR * 0.62f
                        drawLine(
                            color = WarmIvory.copy(alpha = 0.6f),
                            start = Offset(
                                (centerOffset.x + rStart * cos(rad)).toFloat(),
                                (centerOffset.y + rStart * sin(rad)).toFloat()
                            ),
                            end = Offset(
                                (centerOffset.x + rEnd * cos(rad)).toFloat(),
                                (centerOffset.y + rEnd * sin(rad)).toFloat()
                            ),
                            strokeWidth = 1.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Pulsing Inner Plasma Core
                    val coreRadius = maxR * (0.36f + 0.08f * corePulse)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                WarmIvory.copy(alpha = 0.9f),
                                primaryOrbColor,
                                DeepForestAbyss
                            ),
                            center = centerOffset,
                            radius = coreRadius
                        ),
                        center = centerOffset,
                        radius = coreRadius
                    )
                }

                // Center Interactive Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianBlack.copy(alpha = 0.72f))
                        .border(1.dp, primaryOrbColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isListening -> Icons.Default.MicOff
                            isSpeaking -> Icons.Default.StopCircle
                            isThinking -> Icons.Default.GraphicEq
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Interact with J.A.R.V.I.S. Voice Core",
                        tint = primaryOrbColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Right Column: Live State readout + 16-bar Reactive Acoustic Spectrum
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when {
                        isListening -> "Listening to Commander..."
                        isSpeaking -> "Speaking Response..."
                        isThinking -> "Synthesizing Telemetry..."
                        else -> "Stark Emerald Neural Core"
                    },
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory,
                    fontSize = 15.sp
                )

                Text(
                    text = stateSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedPlatinum,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )

                // 16-Bar Reactive Acoustic Waveform
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    val barCount = 18
                    val barWidth = 4.dp.toPx()
                    val totalBarsWidth = barCount * barWidth
                    val gap = ((size.width - totalBarsWidth) / (barCount - 1).coerceAtLeast(1)).coerceAtLeast(2f)
                    val activeAmplitude = if (isListening || isSpeaking || isThinking) 1f else 0.28f

                    for (i in 0 until barCount) {
                        val phaseOffset = (corePulse * 6.28f) + (i * 0.45f)
                        val normalizedWave = abs(sin(phaseOffset)).toFloat()
                        val barHeight = (size.height * (0.18f + 0.82f * normalizedWave * activeAmplitude))
                            .coerceAtLeast(3.dp.toPx())
                        val topY = (size.height - barHeight) / 2f
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryOrbColor,
                                    SophisticatedEmerald.copy(alpha = 0.55f)
                                )
                            ),
                            topLeft = Offset(i * (barWidth + gap), topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Confirmation-Gated Holographic Action Card inside J.A.R.V.I.S.
 * Ensures J.A.R.V.I.S. never mutates expenses, budgets, or settings without explicit user approval.
 */
@Composable
fun JarvisActionConfirmationCard(
    proposedAction: JarvisProposedAction,
    onConfirm: (JarvisProposedAction) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        ElevatedGraphite,
                        RichGraphite
                    )
                )
            )
            .border(1.2.dp, ChampagneGold, RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag("jarvis_action_confirmation_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    imageVector = Icons.Default.Security,
                    contentDescription = "Confirmation Required",
                    tint = ChampagneGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "J.A.R.V.I.S. ACTION CONFIRMATION REQUIRED",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.9.sp,
                    color = ChampagneGold
                )
            }

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss Action Proposal",
                tint = MutedIvory,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onCancel() }
            )
        }

        Text(
            text = proposedAction.title,
            fontFamily = headingFont,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmIvory,
            fontSize = 16.sp
        )

        Text(
            text = proposedAction.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedPlatinum,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onConfirm(proposedAction) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Confirm & Execute",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, HairlinePlatinum),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MutedPlatinum
                ),
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
