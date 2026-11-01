package com.example.ui.jarvis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import kotlin.math.abs
import kotlin.math.sin

// Futuristic Black & Cyan-Blue Arc Reactor Palette for JARVIS
internal val JarvisAbyssBlack = Color(0xFF010409)
internal val JarvisReactorBlack = Color(0xFF000000)
internal val JarvisPanelBlack = Color(0xFF050B14)
internal val JarvisElevatedSurface = Color(0xFF0A1424)
internal val JarvisCyanPrimary = Color(0xFF00E5FF)
internal val JarvisCyanBright = Color(0xFF7AF6FF)
internal val JarvisCyanAccent = Color(0xFF00B4D8)
internal val JarvisCyanDeep = Color(0xFF0284C7)
internal val JarvisHairlineCyan = Color(0xFF15324E)
internal val JarvisTextWhite = Color(0xFFF0F9FF)
internal val JarvisTextSecondary = Color(0xFF94A3B8)
internal val JarvisAlertCrimson = Color(0xFFFF4D6D)

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
 * Primary J.A.R.V.I.S. AI Core Visual Component featuring the user's uploaded Stark Arc Reactor image
 * (`R.drawable.jarvis_reactor_core`) placed prominently in the centre of the screen with `ContentScale.Fit`
 * to preserve original proportions and details without distortion.
 *
 * State-driven cyan-blue animations:
 * - Idle (`ONLINE`): slow, subtle breathing cyan-blue ambient glow.
 * - Listening (`LISTENING`): animated outer cyan-blue glow and live listening indicator.
 * - Processing (`THINKING`): gentle rotating light-ring effect around the reactor core.
 * - Speaking (`SPEAKING`): subtle synchronized pulse animation.
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
    reactorSize: Dp = 196.dp,
    modifier: Modifier = Modifier
) {
    val coreStatus = resolveJarvisCoreStatus(
        isListening = isListening,
        isThinking = isThinking,
        isSpeaking = isSpeaking
    )

    val infiniteTransition = rememberInfiniteTransition(label = "jarvisReactorCoreTransition")

    // 1. Idle: slow, subtle breathing glow (3200ms)
    val idleBreathing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleBreathing"
    )

    // 2. Listening: animated outer glow & ripple expansion (950ms)
    val listeningGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listeningGlow"
    )

    // 3. Processing (Thinking): gentle rotating light-ring effect (4200ms continuous 360 deg)
    val processingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "processingRotation"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterRotation"
    )

    // 4. Speaking: subtle synchronized pulse animation (520ms)
    val speakingPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakingPulse"
    )

    val reactorScale = when (coreStatus) {
        JarvisCoreStatus.ONLINE -> 0.99f + (0.025f * idleBreathing)
        JarvisCoreStatus.LISTENING -> 1.0f + (0.045f * listeningGlow)
        JarvisCoreStatus.THINKING -> 1.0f + (0.02f * idleBreathing)
        JarvisCoreStatus.SPEAKING -> 0.99f + (0.055f * speakingPulse)
    }

    val statusSubtitle = when (coreStatus) {
        JarvisCoreStatus.LISTENING -> "Listening in ${activeLanguage.replaceFirstChar { it.uppercase() }} · Speak your command"
        JarvisCoreStatus.THINKING -> "Synthesizing live ledger, budgets & transaction telemetry..."
        JarvisCoreStatus.SPEAKING -> lastSpokenPreview.take(88).ifBlank { "Transmitting spoken financial briefing..." }
        JarvisCoreStatus.ONLINE -> "Tap the reactor core or microphone to converse with J.A.R.V.I.S."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        JarvisCyanDeep.copy(alpha = 0.16f),
                        JarvisPanelBlack,
                        JarvisReactorBlack
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        JarvisCyanPrimary.copy(alpha = 0.55f),
                        JarvisHairlineCyan,
                        JarvisCyanAccent.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("jarvis_holographic_orb_card"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Prominent Centered Reactor Visual Stage
        Box(
            modifier = Modifier
                .size(reactorSize)
                .clickable {
                    if (isSpeaking) onStopSpeaking() else onToggleMic()
                }
                .testTag("jarvis_central_orb_button"),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Cyan-Blue Lighting & State-Specific Ring Effects behind and around the Reactor
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val maxR = size.minDimension / 2f

                // Base ambient cyan-blue lighting modulated by current state
                val ambientAlpha = when (coreStatus) {
                    JarvisCoreStatus.ONLINE -> 0.22f + 0.14f * idleBreathing
                    JarvisCoreStatus.LISTENING -> 0.34f + 0.24f * listeningGlow
                    JarvisCoreStatus.THINKING -> 0.28f + 0.16f * idleBreathing
                    JarvisCoreStatus.SPEAKING -> 0.30f + 0.26f * speakingPulse
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            JarvisCyanPrimary.copy(alpha = ambientAlpha),
                            JarvisCyanDeep.copy(alpha = ambientAlpha * 0.55f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = maxR
                    ),
                    center = centerOffset,
                    radius = maxR
                )

                when (coreStatus) {
                    JarvisCoreStatus.ONLINE -> {
                        // Idle: Slow, subtle breathing outer halo ring
                        val idleRingR = maxR * (0.82f + 0.04f * idleBreathing)
                        drawCircle(
                            color = JarvisCyanPrimary.copy(alpha = 0.22f + 0.18f * idleBreathing),
                            radius = idleRingR,
                            center = centerOffset,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    JarvisCoreStatus.LISTENING -> {
                        // Listening: Animated outer glow & expanding acoustic rings
                        val outerWaveR = maxR * (0.80f + 0.16f * listeningGlow)
                        val innerWaveR = maxR * (0.74f + 0.10f * (1f - listeningGlow))
                        drawCircle(
                            color = JarvisCyanBright.copy(alpha = 0.55f * (1f - listeningGlow * 0.5f)),
                            radius = outerWaveR,
                            center = centerOffset,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        drawCircle(
                            color = JarvisCyanPrimary.copy(alpha = 0.45f),
                            radius = innerWaveR,
                            center = centerOffset,
                            style = Stroke(
                                width = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                            )
                        )
                    }

                    JarvisCoreStatus.THINKING -> {
                        // Processing: Gentle rotating light-ring effect
                        rotate(degrees = processingRotation, pivot = centerOffset) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        JarvisCyanDeep.copy(alpha = 0.35f),
                                        JarvisCyanPrimary,
                                        JarvisCyanBright,
                                        Color.Transparent
                                    ),
                                    center = centerOffset
                                ),
                                startAngle = 0f,
                                sweepAngle = 290f,
                                useCenter = false,
                                topLeft = Offset(centerOffset.x - maxR * 0.88f, centerOffset.y - maxR * 0.88f),
                                size = Size(maxR * 1.76f, maxR * 1.76f),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        rotate(degrees = counterRotation, pivot = centerOffset) {
                            drawCircle(
                                color = JarvisCyanAccent.copy(alpha = 0.45f),
                                radius = maxR * 0.80f,
                                center = centerOffset,
                                style = Stroke(
                                    width = 1.4.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f, 4f, 12f), 0f)
                                )
                            )
                        }
                    }

                    JarvisCoreStatus.SPEAKING -> {
                        // Speaking: Subtle synchronized pulse rings around the reactor
                        val pulseR1 = maxR * (0.78f + 0.12f * speakingPulse)
                        val pulseR2 = maxR * (0.86f + 0.10f * speakingPulse)
                        drawCircle(
                            color = JarvisCyanPrimary.copy(alpha = 0.52f),
                            radius = pulseR1,
                            center = centerOffset,
                            style = Stroke(width = (2.0f + 1.5f * speakingPulse).dp.toPx())
                        )
                        drawCircle(
                            color = JarvisCyanBright.copy(alpha = 0.28f * (1f - speakingPulse * 0.4f)),
                            radius = pulseR2,
                            center = centerOffset,
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                    }
                }
            }

            // Actual Uploaded Reactor Image Asset — Contain fitting (ContentScale.Fit) without distortion
            Box(
                modifier = Modifier
                    .fillMaxSize(0.84f)
                    .graphicsLayer {
                        scaleX = reactorScale
                        scaleY = reactorScale
                    }
                    .clip(CircleShape)
                    .background(JarvisReactorBlack)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                JarvisCyanPrimary.copy(alpha = 0.65f),
                                JarvisCyanDeep.copy(alpha = 0.25f),
                                JarvisCyanBright.copy(alpha = 0.75f),
                                JarvisCyanDeep.copy(alpha = 0.25f),
                                JarvisCyanPrimary.copy(alpha = 0.65f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jarvis_reactor_core),
                    contentDescription = "J.A.R.V.I.S. AI Core Reactor",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("jarvis_reactor_image")
                )

                // Subtle Cyan-Blue Rim Vignette & Synchronized State Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val r = size.minDimension / 2f
                    val innerPulseAlpha = when (coreStatus) {
                        JarvisCoreStatus.ONLINE -> 0.06f + 0.08f * idleBreathing
                        JarvisCoreStatus.LISTENING -> 0.12f + 0.18f * listeningGlow
                        JarvisCoreStatus.THINKING -> 0.10f + 0.12f * idleBreathing
                        JarvisCoreStatus.SPEAKING -> 0.10f + 0.22f * speakingPulse
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                JarvisCyanBright.copy(alpha = innerPulseAlpha),
                                JarvisCyanPrimary.copy(alpha = innerPulseAlpha * 0.5f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = r * 0.75f
                        ),
                        center = centerOffset,
                        radius = r * 0.75f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Clean Status Label: ONLINE / LISTENING / THINKING / SPEAKING
        Surface(
            color = JarvisCyanPrimary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                width = 1.dp,
                color = when (coreStatus) {
                    JarvisCoreStatus.LISTENING -> JarvisCyanBright
                    JarvisCoreStatus.THINKING -> JarvisCyanPrimary
                    JarvisCoreStatus.SPEAKING -> JarvisCyanBright
                    JarvisCoreStatus.ONLINE -> JarvisCyanAccent.copy(alpha = 0.65f)
                }
            ),
            modifier = Modifier.testTag("jarvis_status_label")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            if (coreStatus == JarvisCoreStatus.ONLINE) JarvisCyanPrimary
                            else JarvisCyanBright
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = coreStatus.label,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.2.sp,
                    color = JarvisTextWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Animated Listening / Speaking Indicator Bars
        AnimatedVisibility(visible = isListening || isSpeaking || isThinking) {
            val activePhase = when {
                isSpeaking -> speakingPulse
                isListening -> listeningGlow
                else -> idleBreathing
            }
            Canvas(
                modifier = Modifier
                    .width(160.dp)
                    .height(18.dp)
                    .padding(vertical = 2.dp)
            ) {
                val barCount = 16
                val barWidth = 3.5.dp.toPx()
                val totalBarsWidth = barCount * barWidth
                val gap = ((size.width - totalBarsWidth) / (barCount - 1).coerceAtLeast(1)).coerceAtLeast(2f)

                for (i in 0 until barCount) {
                    val phaseOffset = (activePhase * 6.28f) + (i * 0.48f)
                    val wave = abs(sin(phaseOffset)).toFloat()
                    val barHeight = (size.height * (0.22f + 0.78f * wave)).coerceAtLeast(3.dp.toPx())
                    val topY = (size.height - barHeight) / 2f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                JarvisCyanBright,
                                JarvisCyanPrimary,
                                JarvisCyanDeep
                            )
                        ),
                        topLeft = Offset(i * (barWidth + gap), topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }

        Text(
            text = statusSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/**
 * Confirmation-Gated Holographic Action Card inside J.A.R.V.I.S.
 * Styled with the black & cyan-blue futuristic JARVIS interface.
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
                        JarvisElevatedSurface,
                        JarvisPanelBlack
                    )
                )
            )
            .border(1.2.dp, JarvisCyanPrimary, RoundedCornerShape(18.dp))
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
                    tint = JarvisCyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "J.A.R.V.I.S. ACTION CONFIRMATION REQUIRED",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.9.sp,
                    color = JarvisCyanPrimary
                )
            }

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss Action Proposal",
                tint = JarvisTextSecondary,
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
            color = JarvisTextWhite,
            fontSize = 16.sp
        )

        Text(
            text = proposedAction.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisTextSecondary,
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
                    containerColor = JarvisCyanPrimary,
                    contentColor = JarvisAbyssBlack
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
                border = BorderStroke(0.8.dp, JarvisHairlineCyan),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = JarvisTextSecondary
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
