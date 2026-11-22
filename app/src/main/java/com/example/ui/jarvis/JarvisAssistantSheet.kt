package com.example.ui.jarvis

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseRecord
import com.example.data.model.JarvisChatMessage
import com.example.data.model.KhataFriendRecord
import com.example.data.model.ProactiveFinancialInsight
import com.example.ui.components.KharcheeSymbolLogo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.viewmodel.JarvisErrorState
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private data class JarvisQuickChip(
    val id: String,
    val label: String,
    val prompt: String
)

private val JARVIS_QUICK_CHIPS = listOf(
    JarvisQuickChip(
        id = "daily_spend",
        label = "Aaj ka kharcha",
        prompt = "How much did I spend today? Aaj ka kharcha"
    ),
    JarvisQuickChip(
        id = "budget_runway",
        label = "Kitna bacha",
        prompt = "How much budget is left? Kitna bacha"
    ),
    JarvisQuickChip(
        id = "hinglish_roast",
        label = "Roast Me 💀",
        prompt = "Roast Me / Audit my spend"
    ),
    JarvisQuickChip(
        id = "quick_add_chai",
        label = "Add 150 Chai",
        prompt = "Add 150 Chai"
    ),
    JarvisQuickChip(
        id = "khata_dues",
        label = "Khata Dues",
        prompt = "Mere Khata ledger ke lena dena dues batao"
    )
)

private fun formatInr(amount: Long): String {
    return try {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        "₹${formatter.format(amount)}"
    } catch (_: Exception) {
        "₹$amount"
    }
}

/**
 * Pure Lightweight J.A.R.V.I.S. AI Assistant ModalBottomSheet.
 * - Zero background Canvas infinite loops or heavy shaders.
 * - Zero blocking TextToSpeech or audio recorder bindings on launch.
 * - Instant dismissal via top-right "✕" button, outside scrim tap, or hardware Back button.
 * - Immediate deterministic Hinglish financial telemetry, intent routing & quick expense execution.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisAssistantSheet(
    userDisplayName: String,
    todaySpent: Long,
    monthSpent: Long,
    dailyLimit: Long,
    disciplineScore: Int,
    messages: List<JarvisChatMessage>,
    streamingReply: String,
    isGenerating: Boolean,
    activeMode: String,
    activeLanguage: String,
    errorState: JarvisErrorState?,
    latestSpokenText: String?,
    onConsumeLatestSpokenText: () -> Unit,
    onSelectMode: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    proactiveInsights: List<ProactiveFinancialInsight> = emptyList(),
    pendingProposedAction: JarvisProposedAction? = null,
    onConfirmProposedAction: ((JarvisProposedAction) -> Unit)? = null,
    onDismissProposedAction: (() -> Unit)? = null,
    initialPrompt: String? = null,
    totalBalance: Long = ((dailyLimit * 30L) - monthSpent).coerceAtLeast(0L),
    monthlyInflow: Long = 0L,
    monthlyOutflow: Long = monthSpent,
    topSpendingCategory: String = "None",
    remainingBudget: Long = ((dailyLimit * 30L) - monthSpent).coerceAtLeast(0L),
    allTransactions: List<ExpenseRecord> = emptyList(),
    khataEntities: List<KhataFriendRecord> = emptyList(),
    netBalance: Long = totalBalance,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismissError: () -> Unit,
    onStopGenerating: (() -> Unit)? = null,
    onRetryLastMessage: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color(0xFF05050A),
        scrimColor = Color(0xFF05050A).copy(alpha = 0.78f),
        dragHandle = null
    ) {
        JarvisAssistantOverlay(
            userDisplayName = userDisplayName,
            todaySpent = todaySpent,
            monthSpent = monthSpent,
            dailyLimit = dailyLimit,
            disciplineScore = disciplineScore,
            messages = messages,
            streamingReply = streamingReply,
            isGenerating = isGenerating,
            activeMode = activeMode,
            activeLanguage = activeLanguage,
            errorState = errorState,
            latestSpokenText = latestSpokenText,
            onConsumeLatestSpokenText = onConsumeLatestSpokenText,
            onSelectMode = onSelectMode,
            onSelectLanguage = onSelectLanguage,
            proactiveInsights = proactiveInsights,
            pendingProposedAction = pendingProposedAction,
            onConfirmProposedAction = onConfirmProposedAction,
            onDismissProposedAction = onDismissProposedAction,
            initialPrompt = initialPrompt,
            totalBalance = netBalance,
            monthlyInflow = monthlyInflow,
            monthlyOutflow = monthlyOutflow,
            topSpendingCategory = topSpendingCategory,
            remainingBudget = remainingBudget,
            allTransactions = allTransactions,
            khataEntities = khataEntities,
            netBalance = netBalance,
            onSendMessage = onSendMessage,
            onClearHistory = onClearHistory,
            onDismissError = onDismissError,
            onStopGenerating = onStopGenerating,
            onRetryLastMessage = onRetryLastMessage,
            onClose = onClose
        )
    }
}

@Composable
fun JarvisAssistantOverlay(
    userDisplayName: String,
    todaySpent: Long,
    monthSpent: Long,
    dailyLimit: Long,
    disciplineScore: Int,
    messages: List<JarvisChatMessage>,
    streamingReply: String,
    isGenerating: Boolean,
    activeMode: String,
    activeLanguage: String,
    errorState: JarvisErrorState?,
    latestSpokenText: String?,
    onConsumeLatestSpokenText: () -> Unit,
    onSelectMode: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    proactiveInsights: List<ProactiveFinancialInsight> = emptyList(),
    pendingProposedAction: JarvisProposedAction? = null,
    onConfirmProposedAction: ((JarvisProposedAction) -> Unit)? = null,
    onDismissProposedAction: (() -> Unit)? = null,
    initialPrompt: String? = null,
    totalBalance: Long = ((dailyLimit * 30L) - monthSpent).coerceAtLeast(0L),
    monthlyInflow: Long = 0L,
    monthlyOutflow: Long = monthSpent,
    topSpendingCategory: String = "None",
    remainingBudget: Long = ((dailyLimit * 30L) - monthSpent).coerceAtLeast(0L),
    allTransactions: List<ExpenseRecord> = emptyList(),
    khataEntities: List<KhataFriendRecord> = emptyList(),
    netBalance: Long = totalBalance,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismissError: () -> Unit,
    onStopGenerating: (() -> Unit)? = null,
    onRetryLastMessage: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    BackHandler {
        onClose()
    }

    LaunchedEffect(initialPrompt) {
        val cleanInitial = initialPrompt?.trim().orEmpty()
        if (cleanInitial.isNotEmpty()) {
            onSendMessage(cleanInitial)
        }
    }

    LaunchedEffect(latestSpokenText) {
        if (!latestSpokenText.isNullOrBlank()) {
            onConsumeLatestSpokenText()
        }
    }

    // Build a sharp, concise live-telemetry opener when chat history is empty.
    val displayMessages = remember(
        messages,
        userDisplayName,
        remainingBudget,
        todaySpent,
        dailyLimit,
        khataEntities,
        activeMode,
        activeLanguage
    ) {
        if (messages.isNotEmpty()) {
            messages
        } else {
            val dailyLeft = (dailyLimit - todaySpent).coerceAtLeast(0L)
            val dailyOver = (todaySpent - dailyLimit).coerceAtLeast(0L)
            val totalLeneHai = khataEntities.filter { it.balance > 0L }.sumOf { it.balance }
            val totalDeneHai = khataEntities.filter { it.balance < 0L }.sumOf { -it.balance }
            val spendHeadline = if (dailyOver > 0L) {
                "Today: ${formatInr(todaySpent)} spent (${formatInr(dailyOver)} over your ${formatInr(dailyLimit)} daily cap)."
            } else {
                "Today: ${formatInr(todaySpent)} / ${formatInr(dailyLimit)} daily cap (${formatInr(dailyLeft)} left)."
            }
            val khataNote = when {
                totalLeneHai > 0L && totalDeneHai > 0L -> "Khata: ${formatInr(totalLeneHai)} lena · ${formatInr(totalDeneHai)} dena."
                totalLeneHai > 0L -> "Khata: ${formatInr(totalLeneHai)} to receive."
                totalDeneHai > 0L -> "Khata: ${formatInr(totalDeneHai)} to pay."
                else -> null
            }
            val conciseIntro = buildString {
                appendLine(spendHeadline)
                append("Monthly budget left: ${formatInr(remainingBudget)}.")
                if (khataNote != null) {
                    append(" $khataNote")
                }
                appendLine()
                append("Ask anything or type 'Add 150 Chai' to log instantly.")
            }
            listOf(
                JarvisChatMessage(
                    id = "jarvis_welcome_roast",
                    role = "jarvis",
                    text = conciseIntro,
                    mode = activeMode,
                    language = activeLanguage
                )
            )
        }
    }

    val totalListItems = displayMessages.size +
        (if (isGenerating) 1 else 0) +
        (if (pendingProposedAction != null) 1 else 0)

    LaunchedEffect(totalListItems) {
        if (totalListItems > 0) {
            runCatching {
                listState.animateScrollToItem((totalListItems - 1).coerceAtLeast(0))
            }
        }
    }

    LaunchedEffect(isGenerating, streamingReply.length / 28) {
        if (isGenerating && totalListItems > 0) {
            runCatching {
                listState.scrollToItem((totalListItems - 1).coerceAtLeast(0))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.86f)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF05050A))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF7C3AED),
                        Color(0xFFA855F7),
                        Color(0xFF7C3AED)
                    )
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .navigationBarsPadding()
            .imePadding()
            .testTag("jarvis_assistant_overlay")
    ) {
        // Drag Handle Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA855F7).copy(alpha = 0.55f))
            )
        }

        // 1. TOP HEADER BAR WITH EXPLICIT "✕" CLOSE BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                JarvisCircularReferenceBadge(size = 36.dp)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KharcheeSymbolLogo(height = 13.dp)
                        Text(
                            text = "J.A.R.V.I.S. AI",
                            fontFamily = headingFont,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = if (isGenerating) {
                            "GEMINI AI · STREAMING LIVE"
                        } else {
                            "GEMINI AI · ${if (activeMode == "coach" || activeMode == "advice") "COACH" else "ROAST"} MODE"
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isGenerating) Color(0xFFB7F000) else Color(0xFFD8B4FE)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clear Chat History Button
                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF151329))
                            .border(0.8.dp, Color(0xFF2B224C), CircleShape)
                            .testTag("jarvis_clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat History",
                            tint = Color(0xFFB8B0D4),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Explicit Top-Right "✕" Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1638))
                        .border(1.dp, Color(0xFFA855F7), CircleShape)
                        .testTag("jarvis_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close J.A.R.V.I.S.",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. COMPACT LIVE TELEMETRY BAR (Total Balance · Inflow · Outflow · Health Score)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0D0E17))
                .border(0.8.dp, Color(0xFF2B224C), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TelemetryMiniStat(
                label = "BALANCE",
                value = formatInr(totalBalance),
                valueColor = Color.White
            )
            TelemetryMiniStat(
                label = "INFLOW",
                value = "+${formatInr(monthlyInflow)}",
                valueColor = Color(0xFF2EE59D)
            )
            TelemetryMiniStat(
                label = "OUTFLOW",
                value = "-${formatInr(monthlyOutflow)}",
                valueColor = Color(0xFFFF4D8D)
            )
            TelemetryMiniStat(
                label = "HEALTH",
                value = "$disciplineScore/900",
                valueColor = Color(0xFFD8B4FE)
            )
        }

        // 3. MODE & LANGUAGE TOGGLE STRIP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ModePillButton(
                    label = "🔥 Roast",
                    selected = activeMode == "roast",
                    onClick = { onSelectMode("roast") }
                )
                ModePillButton(
                    label = "🧠 Coach",
                    selected = activeMode == "coach" || activeMode == "advice",
                    onClick = { onSelectMode("coach") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("hinglish" to "Hinglish", "english" to "EN", "hindi" to "हिंदी").forEach { (code, label) ->
                    ModePillButton(
                        label = label,
                        selected = activeLanguage.equals(code, ignoreCase = true),
                        onClick = { onSelectLanguage(code) }
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF2B224C), thickness = 0.6.dp)

        // 4. CHAT MESSAGES LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = displayMessages,
                key = { it.id },
                contentType = { "jarvis_message" }
            ) { msg ->
                JarvisMessageBubble(
                    message = msg,
                    onCopy = { textToCopy ->
                        copyTextToClipboard(context, textToCopy)
                    }
                )
            }

            if (isGenerating && streamingReply.isNotBlank()) {
                item(
                    key = "jarvis_streaming_reply_item",
                    contentType = "jarvis_streaming"
                ) {
                    JarvisStreamingReplyBubble(streamingText = streamingReply)
                }
            } else if (isGenerating) {
                item(
                    key = "jarvis_typing_indicator_item",
                    contentType = "jarvis_typing"
                ) {
                    JarvisTypingIndicatorBubble()
                }
            }

            if (pendingProposedAction != null && onConfirmProposedAction != null && onDismissProposedAction != null) {
                item(
                    key = "proposed_action_${pendingProposedAction.id}",
                    contentType = "jarvis_proposed_action"
                ) {
                    JarvisProposedActionConfirmationCard(
                        proposedAction = pendingProposedAction,
                        onConfirm = onConfirmProposedAction,
                        onCancel = onDismissProposedAction
                    )
                }
            }
        }

        // 4B. REAL ERROR BANNER WITH RETRY & DISMISS (NEVER PRETENDS ERROR CAME FROM GEMINI)
        AnimatedVisibility(
            visible = errorState != null,
            enter = fadeIn(tween(180)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(180))
        ) {
            errorState?.let { activeError ->
                val errorTitle = when {
                    activeError.isOffline -> "⚠️ NO INTERNET CONNECTION"
                    activeError.isRateLimit -> "⚠️ GEMINI RATE LIMIT"
                    activeError.isTimeout -> "⚠️ GEMINI REQUEST TIMED OUT"
                    else -> "⚠️ GEMINI AI CONNECTION ERROR"
                }
                Surface(
                    color = Color(0xFF2A0D18),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.9.dp, Color(0xFFFF4D8D).copy(alpha = 0.75f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("jarvis_error_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = errorTitle,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF4D8D)
                        )
                        Text(
                            text = activeError.message,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFFFFE4EC)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (activeError.lastFailedPrompt.isNotBlank() && onRetryLastMessage != null) {
                                Surface(
                                    color = if (isGenerating) Color(0xFF533378) else Color(0xFFA855F7),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(50))
                                        .clickable(enabled = !isGenerating) { onRetryLastMessage() }
                                        .testTag("jarvis_retry_button")
                                ) {
                                    Text(
                                        text = "↻ Retry",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFF1A1020),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color(0xFFFF4D8D).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onDismissError() }
                                    .testTag("jarvis_dismiss_error_button")
                            ) {
                                Text(
                                    text = "Dismiss",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFB4CE),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4C. STOP GENERATION BAR WHILE STREAMING
        AnimatedVisibility(
            visible = isGenerating && onStopGenerating != null,
            enter = fadeIn(tween(150)) + expandVertically(tween(180)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(160))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color(0xFF211338),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.9.dp, Color(0xFFFF4D8D).copy(alpha = 0.8f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(50))
                        .clickable { onStopGenerating?.invoke() }
                        .testTag("jarvis_stop_generation_button")
                ) {
                    Text(
                        text = "■ Stop Generation",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB4CE),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 5. QUICK ACTION CHIPS ("Daily Spend", "Khata Dues", "Budget Runway", "Hinglish Roast")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                items = JARVIS_QUICK_CHIPS,
                key = { it.id },
                contentType = { "jarvis_quick_chip" }
            ) { chip ->
                Surface(
                    color = Color(0xFF151329),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, Color(0xFFA855F7).copy(alpha = if (isGenerating) 0.25f else 0.6f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !isGenerating) { onSendMessage(chip.prompt) }
                        .testTag("jarvis_chip_${chip.id}")
                ) {
                    Text(
                        text = chip.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isGenerating) Color(0xFF8E84AD) else Color(0xFFF3E8FF),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 6. BOTTOM MESSAGE INPUT & SEND / STOP BAR
        val submitCurrentInput = {
            val trimmed = inputText.trim()
            if (!isGenerating && trimmed.isNotEmpty()) {
                inputText = ""
                onSendMessage(trimmed)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = if (isGenerating) "J.A.R.V.I.S. is replying..." else "Ask anything or type 'Add 150 chai'...",
                        fontSize = 13.sp,
                        color = Color(0xFF8E84AD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submitCurrentInput() }),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0D0E17),
                    unfocusedContainerColor = Color(0xFF0D0E17),
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF2B224C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFA855F7)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("jarvis_input_field")
            )

            if (isGenerating && onStopGenerating != null) {
                IconButton(
                    onClick = { onStopGenerating() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF4D6D), Color(0xFFB91C4A))
                            )
                        )
                        .testTag("jarvis_stop_inline_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop generating response",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                IconButton(
                    enabled = !isGenerating && inputText.trim().isNotEmpty(),
                    onClick = submitCurrentInput,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isGenerating || inputText.trim().isEmpty()) {
                                    listOf(Color(0xFF3E355C), Color(0xFF2B224C))
                                } else {
                                    listOf(Color(0xFFA855F7), Color(0xFF7C3AED))
                                }
                            )
                        )
                        .testTag("jarvis_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send to Jarvis",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JarvisTypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_typing_dots")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(480), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(480, delayMillis = 160), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(480, delayMillis = 320), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("jarvis_typing_indicator"),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                .background(Color(0xFF141126))
                .border(
                    width = 0.8.dp,
                    color = Color(0xFFA855F7).copy(alpha = 0.55f),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "J.A.R.V.I.S. · GEMINI AI",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = Color(0xFFA855F7)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .alpha(alpha)
                            .clip(CircleShape)
                            .background(Color(0xFFB7F000))
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Thinking...",
                    fontSize = 12.sp,
                    color = Color(0xFFD8B4FE)
                )
            }
        }
    }
}

@Composable
private fun JarvisStreamingReplyBubble(streamingText: String) {
    val cursorTransition = rememberInfiniteTransition(label = "jarvis_stream_cursor")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "cursorAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("jarvis_streaming_bubble"),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF141126), Color(0xFF0D0E17))
                    )
                )
                .border(
                    width = 0.9.dp,
                    color = Color(0xFFA855F7).copy(alpha = 0.7f),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(cursorAlpha)
                        .clip(CircleShape)
                        .background(Color(0xFFB7F000))
                )
                Text(
                    text = "J.A.R.V.I.S. · LIVE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = Color(0xFFA855F7)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (cursorAlpha > 0.55f) "$streamingText▌" else "$streamingText ",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFFF7F4FF)
            )
        }
    }
}

@Composable
private fun TelemetryMiniStat(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8E84AD)
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun ModePillButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Color(0xFF2B185E) else Color(0xFF0D0E17),
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (selected) Color(0xFFA855F7) else Color(0xFF2B224C)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFFB8B0D4),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun JarvisMessageBubble(
    message: JarvisChatMessage,
    onCopy: (String) -> Unit
) {
    val isUser = message.role.equals("user", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2B185E), Color(0xFF1B103D))
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF141126), Color(0xFF0D0E17))
                        )
                    }
                )
                .border(
                    width = 0.8.dp,
                    color = if (isUser) Color(0xFFA855F7).copy(alpha = 0.65f) else Color(0xFF2B224C),
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUser) "YOU" else "J.A.R.V.I.S.",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = if (isUser) Color(0xFFD8B4FE) else Color(0xFFA855F7)
                )
                if (!isUser) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy response",
                        tint = Color(0xFF8E84AD),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onCopy(message.text) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFFF7F4FF)
            )
        }
    }
}

private fun copyTextToClipboard(context: Context, text: String) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("JARVIS Response", text))
        Toast.makeText(context, "Copied JARVIS response", Toast.LENGTH_SHORT).show()
    }
}
