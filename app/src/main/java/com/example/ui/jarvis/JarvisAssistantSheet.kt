package com.example.ui.jarvis

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

    // Build an automatic deterministic welcome status message when chat history is empty, synced to live database state
    val displayMessages = remember(
        messages,
        userDisplayName,
        netBalance,
        monthlyInflow,
        monthlyOutflow,
        topSpendingCategory,
        remainingBudget,
        todaySpent,
        dailyLimit,
        disciplineScore,
        allTransactions.size,
        khataEntities
    ) {
        if (messages.isNotEmpty()) {
            messages
        } else {
            val cal = Calendar.getInstance()
            val daysLeftInMonth = (cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH) + 1).coerceAtLeast(1)
            val dailySafeLimit = (remainingBudget / daysLeftInMonth).coerceAtLeast(0L)
            val totalLeneHai = khataEntities.filter { it.balance > 0 }.sumOf { it.balance }
            val totalDeneHai = khataEntities.filter { it.balance < 0 }.sumOf { -it.balance }
            val khataLine = if (totalLeneHai > 0L || totalDeneHai > 0L) {
                "Market me ₹$totalLeneHai lene bache hain aur ₹$totalDeneHai chukane hain."
            } else {
                "Khata clean hai Sir, kisi ka udhar baki nahi hai."
            }
            val monthlyTarget = (dailyLimit * 30L).coerceAtLeast(1500L)
            val isOverspending = monthlyOutflow > (monthlyTarget * 70L) / 100L
            val roastStatusLine = if (isOverspending) {
                "Aapka budget critical stage par hai! Swiggy aur luxury offline sprees band kijiye, varna month-end par water-only diet pe jaana padega. 💀"
            } else {
                "Discipline bohot badhiya hai, Chief. You are in the green zone. Backed by solid budgeting!"
            }
            val initialRoast = buildString {
                appendLine("Aaj aapne total ₹$todaySpent kharch kiye hain, Chief. $topSpendingCategory par sabse zyada spent hua hai.")
                appendLine("Balance ${formatInr(netBalance)} bacha hai. Month-end tak smooth chalne ke liye, aapka daily spending limit ₹$dailySafeLimit se upar nahi jana chahiye.")
                appendLine()
                appendLine("🧠 AI CFO Audit: $roastStatusLine")
                appendLine()
                appendLine("⚡ Live Database Telemetry ($userDisplayName · ${allTransactions.size} txns):")
                appendLine("• Net Balance: ${formatInr(netBalance)} | Health Score: $disciplineScore/900")
                appendLine("• Monthly Inflow: +${formatInr(monthlyInflow)} | Outflow: -${formatInr(monthlyOutflow)}")
                appendLine("• Top Category: $topSpendingCategory | Remaining Budget: ${formatInr(remainingBudget)}")
                appendLine("• Khata Status: $khataLine")
                append("💡 1-Tap Command: Type 'Add 150 Chai' to log an expense instantly!")
            }
            listOf(
                JarvisChatMessage(
                    id = "jarvis_welcome_roast",
                    role = "jarvis",
                    text = initialRoast,
                    mode = activeMode,
                    language = activeLanguage
                )
            )
        }
    }

    LaunchedEffect(displayMessages.size) {
        if (displayMessages.isNotEmpty()) {
            runCatching {
                listState.scrollToItem((displayMessages.size - 1).coerceAtLeast(0))
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
                        text = "LIVE TELEMETRY · HINGLISH ROAST ENGINE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD8B4FE)
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
                            .size(36.dp)
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
                        .size(36.dp)
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
                    selected = activeMode == "coach",
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
                    border = BorderStroke(0.8.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSendMessage(chip.prompt) }
                        .testTag("jarvis_chip_${chip.id}")
                ) {
                    Text(
                        text = chip.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF3E8FF),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 6. BOTTOM MESSAGE INPUT & SEND BAR
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
                        text = "Try 'Add 150 chai', 'Aaj ka kharcha', 'Khata dues'...",
                        fontSize = 13.sp,
                        color = Color(0xFF8E84AD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
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

            IconButton(
                onClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty()) {
                        inputText = ""
                        onSendMessage(trimmed)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFA855F7), Color(0xFF7C3AED))
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
