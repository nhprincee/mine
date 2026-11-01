package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountabilityEvaluation
import com.example.data.model.ExpenseRecord
import com.example.data.model.JarvisChatMessage
import com.example.data.model.KhataFriendRecord
import com.example.data.model.NightGuardEventRecord
import com.example.data.model.NightGuardSchedule
import com.example.data.model.PaymentReminderRecord
import com.example.data.model.PendingSpendRequest
import com.example.data.model.ProactiveFinancialInsight
import com.example.data.model.SavingsGoalRecord
import com.example.data.model.UpiAccountabilityEngine
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.data.model.isSameDay
import com.example.data.model.isSameMonth
import com.example.data.repository.KharcheeRepository
import com.example.ui.jarvis.JarvisProposedAction
import com.example.ui.jarvis.parseNaturalLanguageActionProposal
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class JarvisErrorState(
    val message: String,
    val isRateLimit: Boolean = false,
    val lastFailedPrompt: String = ""
)

class KharcheeViewModel(
    private val repository: KharcheeRepository,
    private val currentUserId: String
) : ViewModel() {

    private val generativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    val settingsState: StateFlow<UiState<UserSettings?>> = repository.observeUserSettings(currentUserId)
        .map<UserSettings?, UiState<UserSettings?>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing user settings", error)
            emit(UiState.Error(error.message ?: "Failed to load settings"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val expensesState: StateFlow<UiState<List<ExpenseRecord>>> = repository.observeExpenses(currentUserId)
        .map<List<ExpenseRecord>, UiState<List<ExpenseRecord>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing expenses", error)
            emit(UiState.Error(error.message ?: "Failed to load expenses"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val khataFriendsState: StateFlow<UiState<List<KhataFriendRecord>>> = repository.observeKhataFriends(currentUserId)
        .map<List<KhataFriendRecord>, UiState<List<KhataFriendRecord>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing khata friends", error)
            emit(UiState.Error(error.message ?: "Failed to load Khata friends"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val savingsGoalsState: StateFlow<UiState<List<SavingsGoalRecord>>> = repository.observeSavingsGoals(currentUserId)
        .map<List<SavingsGoalRecord>, UiState<List<SavingsGoalRecord>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing savings goals", error)
            emit(UiState.Error(error.message ?: "Failed to load savings goals"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val jarvisMessagesState: StateFlow<UiState<List<JarvisChatMessage>>> = repository.observeJarvisMessages(currentUserId)
        .map<List<JarvisChatMessage>, UiState<List<JarvisChatMessage>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing JARVIS messages", error)
            emit(UiState.Error(error.message ?: "Failed to load JARVIS history"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val paymentRemindersState: StateFlow<UiState<List<PaymentReminderRecord>>> = repository.observePaymentReminders(currentUserId)
        .map<List<PaymentReminderRecord>, UiState<List<PaymentReminderRecord>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing payment reminders", error)
            emit(UiState.Error(error.message ?: "Failed to load payment reminders"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val nightGuardEventsState: StateFlow<UiState<List<NightGuardEventRecord>>> = repository.observeNightGuardEvents(currentUserId)
        .map<List<NightGuardEventRecord>, UiState<List<NightGuardEventRecord>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing Night Guard events", error)
            emit(UiState.Error(error.message ?: "Failed to load Night Guard events"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val proactiveInsights: StateFlow<List<ProactiveFinancialInsight>> = combine(
        expensesState,
        settingsState,
        khataFriendsState
    ) { expState, setState, khState ->
        val expenses = (expState as? UiState.Success)?.data ?: emptyList()
        val settings = (setState as? UiState.Success)?.data ?: UserSettings(userId = currentUserId)
        val khata = (khState as? UiState.Success)?.data ?: emptyList()
        generateProactiveFinancialInsights(expenses, settings, khata)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    private val _pendingJarvisPrompt = MutableStateFlow<String?>(null)
    val pendingJarvisPrompt: StateFlow<String?> = _pendingJarvisPrompt.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _latestAccountabilityAlert = MutableStateFlow<AccountabilityEvaluation?>(null)
    val latestAccountabilityAlert: StateFlow<AccountabilityEvaluation?> = _latestAccountabilityAlert.asStateFlow()

    // Night Guard & Pre-Spend Intercept State
    private val _pendingNightGuardSpend = MutableStateFlow<PendingSpendRequest?>(null)
    val pendingNightGuardSpend: StateFlow<PendingSpendRequest?> = _pendingNightGuardSpend.asStateFlow()

    private val _pendingPreSpendConfirm = MutableStateFlow<PendingSpendRequest?>(null)
    val pendingPreSpendConfirm: StateFlow<PendingSpendRequest?> = _pendingPreSpendConfirm.asStateFlow()

    // JARVIS Conversational State
    private val _jarvisMode = MutableStateFlow("roast") // "roast" or "advice"
    val jarvisMode: StateFlow<String> = _jarvisMode.asStateFlow()

    private val _jarvisLanguage = MutableStateFlow("hinglish") // "hinglish", "hindi", or "english"
    val jarvisLanguage: StateFlow<String> = _jarvisLanguage.asStateFlow()

    private val _jarvisStreamingReply = MutableStateFlow("")
    val jarvisStreamingReply: StateFlow<String> = _jarvisStreamingReply.asStateFlow()

    private val _jarvisIsGenerating = MutableStateFlow(false)
    val jarvisIsGenerating: StateFlow<Boolean> = _jarvisIsGenerating.asStateFlow()

    private val _jarvisError = MutableStateFlow<JarvisErrorState?>(null)
    val jarvisError: StateFlow<JarvisErrorState?> = _jarvisError.asStateFlow()

    private val _latestSpokenJarvisText = MutableStateFlow<String?>(null)
    val latestSpokenJarvisText: StateFlow<String?> = _latestSpokenJarvisText.asStateFlow()

    private val _pendingProposedAction = MutableStateFlow<JarvisProposedAction?>(null)
    val pendingProposedAction: StateFlow<JarvisProposedAction?> = _pendingProposedAction.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun dismissAccountabilityAlert() {
        _latestAccountabilityAlert.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun setJarvisMode(mode: String) {
        _jarvisMode.value = if (mode == "advice") "advice" else "roast"
    }

    fun setJarvisLanguage(language: String) {
        _jarvisLanguage.value = when (language.lowercase()) {
            "hindi" -> "hindi"
            "english" -> "english"
            else -> "hinglish"
        }
    }

    fun dismissJarvisError() {
        _jarvisError.value = null
    }

    fun consumeLatestSpokenJarvisText() {
        _latestSpokenJarvisText.value = null
    }

    fun dismissProposedAction() {
        _pendingProposedAction.value = null
    }

    fun executeConfirmedJarvisAction(
        action: JarvisProposedAction,
        onNavigateToTab: ((String) -> Unit)? = null
    ) {
        _pendingProposedAction.value = null
        when (action.actionType) {
            "LOG_EXPENSE" -> {
                addTransaction(
                    title = action.title.substringAfter("· ").trim().ifBlank { action.category },
                    category = action.category,
                    amount = action.amount,
                    paymentMode = "UPI",
                    type = "expense",
                    note = "Logged via J.A.R.V.I.S. Confirmed Action"
                )
            }
            "SET_DAILY_LIMIT" -> {
                updateDailyLimit(action.amount)
            }
            "TOGGLE_LOCKDOWN" -> {
                updateSettings(
                    lockdownActive = action.booleanFlag,
                    toastText = if (action.booleanFlag) "🔒 Lockdown Shield Activated via J.A.R.V.I.S." else "🔓 Lockdown Shield Lifted via J.A.R.V.I.S."
                )
            }
            "NAVIGATE_TAB" -> {
                onNavigateToTab?.invoke(action.targetTabId)
                _toastMessage.value = "⚡ J.A.R.V.I.S. opened ${action.targetTabId.uppercase()}"
            }
        }
    }

    fun queueJarvisPrompt(prompt: String) {
        _pendingJarvisPrompt.value = prompt
    }

    fun consumePendingJarvisPrompt(): String? {
        val p = _pendingJarvisPrompt.value
        _pendingJarvisPrompt.value = null
        return p
    }

    fun upgradeProPlan(tier: String, durationDays: Int) {
        viewModelScope.launch {
            val res = repository.updateProMembership(tier, durationDays)
            if (res.isSuccess) {
                _toastMessage.value = "👑 Upgraded to PRO ${tier.uppercase()}! Unlocked advanced analytics & priority JARVIS."
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to upgrade Pro plan"
            }
        }
    }

    fun updateDailyLimit(limit: Long) {
        updateSettings(
            dailyLimit = limit,
            toastText = "✅ Daily spending limit set to ₹$limit"
        )
    }

    fun generateProactiveFinancialInsights(
        expenses: List<ExpenseRecord>,
        settings: UserSettings,
        khata: List<KhataFriendRecord> = emptyList()
    ): List<ProactiveFinancialInsight> {
        return Companion.generateProactiveFinancialInsights(expenses, settings, khata)
    }

    private fun currentSettingsOrDefault(): UserSettings {
        return (settingsState.value as? UiState.Success)?.data ?: UserSettings(userId = currentUserId)
    }

    private fun currentExpensesOrEmpty(): List<ExpenseRecord> {
        return (expensesState.value as? UiState.Success)?.data ?: emptyList()
    }

    private fun currentKhataOrEmpty(): List<KhataFriendRecord> {
        return (khataFriendsState.value as? UiState.Success)?.data ?: emptyList()
    }

    private fun currentSavingsGoalsOrEmpty(): List<SavingsGoalRecord> {
        return (savingsGoalsState.value as? UiState.Success)?.data ?: emptyList()
    }

    private fun currentJarvisMessagesOrEmpty(): List<JarvisChatMessage> {
        return (jarvisMessagesState.value as? UiState.Success)?.data ?: emptyList()
    }

    private fun buildLiveFinancialContextSnapshot(userDisplayName: String): String {
        val settings = currentSettingsOrDefault()
        val expenses = currentExpensesOrEmpty()
        val khata = currentKhataOrEmpty()
        val goals = currentSavingsGoalsOrEmpty()
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)

        val todayExpenses = expenses.filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, now) }
        val monthExpenses = expenses.filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, now) }
        val monthIncomes = expenses.filter { isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, now) }

        val todaySpent = todayExpenses.sumOf { it.amount }
        val monthSpent = monthExpenses.sumOf { it.amount }
        val monthIncomeTotal = monthIncomes.sumOf { it.amount }
        val monthlyBudget = settings.dailyLimit * 30L
        val dailyRemaining = (settings.dailyLimit - todaySpent).coerceAtLeast(0L)

        var streak = 0
        for (i in 0 until 30) {
            val dayTarget = now - i * 86_400_000L
            val daySum = expenses
                .filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, dayTarget) }
                .sumOf { it.amount }
            if (daySum <= settings.dailyLimit) streak += 1 else break
        }

        var disciplineScore = 740 + min(streak * 8, 96)
        if (settings.dailyLimit > 0L) {
            val ratio = todaySpent.toDouble() / settings.dailyLimit.toDouble()
            disciplineScore += when {
                ratio <= 0.7 -> 35
                ratio <= 1.0 -> 15
                else -> -min(((ratio - 1.0) * 150.0).roundToInt(), 120)
            }
        }
        val clampedScore = disciplineScore.coerceIn(300, 900)

        val categoryBreakdown = monthExpenses
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .joinToString("; ") { "${it.key}: ₹${it.value}" }
            .ifEmpty { "No expenses logged this month" }

        val recentTxLines = expenses.take(12).joinToString("\n") { tx ->
            val dir = if (isIncomeTransaction(tx)) "INCOME (+)" else "EXPENSE (-)"
            val dateStr = dateFormat.format(Date(tx.txEpochMillis))
            "- [$dir] ₹${tx.amount} | ${tx.title} | Category: ${tx.category} | Mode: ${tx.paymentMode} | Note: ${tx.note.ifBlank { "None" }} | Date: $dateStr"
        }.ifEmpty { "- No transactions recorded yet." }

        val khataSummary = khata.joinToString("; ") { friend ->
            when {
                friend.balance > 0 -> "${friend.name} owes user ₹${friend.balance} (Lena Hai - ${friend.lastDescription})"
                friend.balance < 0 -> "User owes ${friend.name} ₹${-friend.balance} (Dena Hai - ${friend.lastDescription})"
                else -> "${friend.name}: Settled (₹0)"
            }
        }.ifEmpty { "No Khata split records." }

        val goalsSummary = goals.joinToString("; ") { goal ->
            val pct = if (goal.targetAmount > 0) ((goal.savedAmount * 100) / goal.targetAmount) else 0
            "${goal.title}: Saved ₹${goal.savedAmount} / Target ₹${goal.targetAmount} ($pct%)"
        }.ifEmpty { "No savings goals configured." }

        val proactiveInsightsText = generateProactiveFinancialInsights(expenses, settings, khata)
            .joinToString("\n") { "- [AUTONOMOUS TELEMETRY INSIGHT] ${it.title}: ${it.message}" }
            .ifEmpty { "- No anomalies detected." }

        val proStatusStr = if (settings.isPro) "PRO TITANIUM (${settings.proTier.uppercase()})" else "STANDARD MEMBER"

        return """
            === KHARCHEE LIVE USER FINANCIAL TELEMETRY (AUTHORIZED REAL DATA) ===
            User Name: $userDisplayName
            Membership Tier: $proStatusStr
            Daily Spending Limit: ₹${settings.dailyLimit}
            Monthly Budget Target (30x Daily): ₹$monthlyBudget
            Today's Total Expense: ₹$todaySpent (Remaining today: ₹$dailyRemaining)
            This Month's Total Expense: ₹$monthSpent
            This Month's Total Income: ₹$monthIncomeTotal
            Net Monthly Cashflow: ₹${monthIncomeTotal - monthSpent}
            Lockdown Shield Active: ${settings.lockdownActive}
            Discipline Score: $clampedScore / 900 (Streak: ${streak} days under daily limit)
            Monthly Category Spend Breakdown: $categoryBreakdown
            Khata Split Ledger Balances: $khataSummary
            Savings Goals: $goalsSummary
            Detected Autonomous Insights:
            $proactiveInsightsText
            Recent Transactions (Latest 12):
            $recentTxLines
            ======================================================================
        """.trimIndent()
    }

    fun sendJarvisMessage(
        rawPrompt: String,
        userDisplayName: String = "Commander"
    ) {
        val cleanPrompt = rawPrompt.trim()
        if (cleanPrompt.isEmpty() || _jarvisIsGenerating.value) return

        val activeMode = _jarvisMode.value
        val activeLang = _jarvisLanguage.value
        val allMessages = currentJarvisMessagesOrEmpty()
        val previousMessages = allMessages.takeLast(10)
        val earlierConversationDigest = buildConversationContextSummary(allMessages)

        // Check if the user prompt also requests a confirmation-gated action
        val detectedProposal = parseNaturalLanguageActionProposal(cleanPrompt)
        if (detectedProposal != null) {
            _pendingProposedAction.value = detectedProposal
        }

        _jarvisError.value = null
        _jarvisIsGenerating.value = true
        _jarvisStreamingReply.value = ""

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveJarvisMessage(
                role = "user",
                text = cleanPrompt,
                mode = activeMode,
                language = activeLang,
                txEpochMillis = now
            )

            val languageInstruction = when (activeLang) {
                "hindi" -> "Respond primarily in natural, conversational Hindi (Devanagari script: हिंदी) with clear ₹ numbers."
                "english" -> "Respond in crisp, witty, articulate English."
                else -> "Respond in natural Indian Hinglish (a lively mix of Hindi in Roman script and English, e.g., 'Bhai aaj ₹250 ka खर्चा ho gaya!'). If the user writes in Devanagari Hindi or pure English, adapt smoothly."
            }

            val personaInstruction = if (activeMode == "roast") {
                "PERSONA: You are J.A.R.V.I.S. (Roast Mode 🔥) inside Kharchee — Tony Stark's tactical AI fused with a sharp, witty Indian finance bro. Gently roast impulse spending (like repeat Chai, food delivery, or overshooting daily limits) while quoting the user's ACTUAL ₹ numbers, merchants, and categories. End with 1-2 concrete money-saving moves."
            } else {
                "PERSONA: You are J.A.R.V.I.S. (Wealth & Budget Coach Mode 🧠) inside Kharchee — an empathetic, analytical personal finance strategist. Provide structured expense analysis, spending summaries, 50/30/20 or daily budget optimizations, and Khata recovery reminders using the user's ACTUAL ₹ data."
            }

            val financialContext = buildLiveFinancialContextSnapshot(userDisplayName)
            val memorySection = if (earlierConversationDigest.isNotBlank()) {
                "\n=== EARLIER CONVERSATION MEMORY DIGEST ===\n$earlierConversationDigest\n==========================================\n"
            } else ""

            val systemAndUserPrompt = """
                You are J.A.R.V.I.S., the built-in AI financial assistant for the Kharchee Android app.
                $personaInstruction
                LANGUAGE RULE: $languageInstruction
                GROUNDING RULE: Always use the user's real Kharchee financial data provided below. Never invent fake transactions or mock balances. Keep responses concise (under 160 words), well-structured with bullet points where helpful, and easy to read or listen to via Text-to-Speech.

                $financialContext
                $memorySection
                User's Message:
                $cleanPrompt
            """.trimIndent()

            val chatHistory = previousMessages.map { msg ->
                content(role = if (msg.role == "user") "user" else "model") {
                    text(msg.text)
                }
            }

            try {
                var accumulatedText = ""
                withTimeout(60_000L) {
                    val chat = generativeModel.startChat(history = chatHistory)
                    chat.sendMessageStream(systemAndUserPrompt).collect { chunk ->
                        val part = chunk.text.orEmpty()
                        if (part.isNotEmpty()) {
                            accumulatedText += part
                            _jarvisStreamingReply.value = accumulatedText
                        }
                    }
                }

                val finalReply = accumulatedText.trim().ifEmpty {
                    "Jarvis telemetry online, Commander. I analyzed your ₹${currentExpensesOrEmpty().sumOf { it.amount }} ledger—ask me for a spending breakdown or budget plan!"
                }

                _jarvisStreamingReply.value = ""
                repository.saveJarvisMessage(
                    role = "jarvis",
                    text = finalReply,
                    mode = activeMode,
                    language = activeLang,
                    txEpochMillis = System.currentTimeMillis()
                )
                _latestSpokenJarvisText.value = finalReply
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "JARVIS request timed out", e)
                _jarvisStreamingReply.value = ""
                _jarvisError.value = JarvisErrorState(
                    message = "J.A.R.V.I.S. neural link timed out after 60s. Please check your connection and tap Retry.",
                    isRateLimit = false,
                    lastFailedPrompt = cleanPrompt
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "JARVIS inference error", e)
                _jarvisStreamingReply.value = ""
                val rawMsg = e.localizedMessage ?: e.message ?: "Unknown AI error"
                val isRateLimited = rawMsg.contains("429") ||
                    rawMsg.contains("quota", ignoreCase = true) ||
                    rawMsg.contains("rate limit", ignoreCase = true) ||
                    rawMsg.contains("RESOURCE_EXHAUSTED", ignoreCase = true)

                val friendlyMsg = if (isRateLimited) {
                    "⚡ API Rate Limit Reached (429 / Quota). Please wait 15–30 seconds before sending your next query to J.A.R.V.I.S."
                } else {
                    "Unable to reach J.A.R.V.I.S. AI core ($rawMsg). Please verify network connectivity and retry."
                }
                _jarvisError.value = JarvisErrorState(
                    message = friendlyMsg,
                    isRateLimit = isRateLimited,
                    lastFailedPrompt = cleanPrompt
                )
            } finally {
                _jarvisIsGenerating.value = false
            }
        }
    }

    fun clearJarvisConversation() {
        viewModelScope.launch {
            val res = repository.clearJarvisMessages()
            if (res.isSuccess) {
                _jarvisError.value = null
                _jarvisStreamingReply.value = ""
                _toastMessage.value = "🧹 Cleared J.A.R.V.I.S. conversation history"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to clear chat"
            }
        }
    }

    fun updateSettings(
        dailyLimit: Long? = null,
        lockdownActive: Boolean? = null,
        autoLockdownAt90: Boolean? = null,
        stealthMode: Boolean? = null,
        themeId: String? = null,
        myUpiId: String? = null,
        upiGuardTone: String? = null,
        nightGuardEnabled: Boolean? = null,
        nightGuardStartHour: Int? = null,
        nightGuardStartMinute: Int? = null,
        nightGuardEndHour: Int? = null,
        nightGuardEndMinute: Int? = null,
        enablePreSpendConfirm: Boolean? = null,
        notifyPaymentReminders: Boolean? = null,
        notifyNightGuardAlert: Boolean? = null,
        notifyMorningReview: Boolean? = null,
        toastText: String? = null
    ) {
        val current = currentSettingsOrDefault()
        viewModelScope.launch {
            val result = repository.upsertUserSettings(
                dailyLimit = dailyLimit ?: current.dailyLimit,
                lockdownActive = lockdownActive ?: current.lockdownActive,
                autoLockdownAt90 = autoLockdownAt90 ?: current.autoLockdownAt90,
                stealthMode = stealthMode ?: current.stealthMode,
                themeId = themeId ?: current.themeId,
                myUpiId = myUpiId ?: current.myUpiId,
                upiGuardTone = upiGuardTone ?: current.upiGuardTone,
                isPro = current.isPro,
                proTier = current.proTier,
                proExpiryMillis = current.proExpiryMillis,
                nightGuardEnabled = nightGuardEnabled ?: current.nightGuardEnabled,
                nightGuardStartHour = nightGuardStartHour ?: current.nightGuardStartHour,
                nightGuardStartMinute = nightGuardStartMinute ?: current.nightGuardStartMinute,
                nightGuardEndHour = nightGuardEndHour ?: current.nightGuardEndHour,
                nightGuardEndMinute = nightGuardEndMinute ?: current.nightGuardEndMinute,
                nightGuardCooldownEndMillis = current.nightGuardCooldownEndMillis,
                lastChallengeId = current.lastChallengeId,
                enablePreSpendConfirm = enablePreSpendConfirm ?: current.enablePreSpendConfirm,
                notifyPaymentReminders = notifyPaymentReminders ?: current.notifyPaymentReminders,
                notifyNightGuardAlert = notifyNightGuardAlert ?: current.notifyNightGuardAlert,
                notifyMorningReview = notifyMorningReview ?: current.notifyMorningReview
            )
            if (result.isSuccess && toastText != null) {
                _toastMessage.value = toastText
            } else if (result.isFailure) {
                _toastMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update settings"
            }
        }
    }

    fun requestAddTransaction(
        title: String,
        category: String,
        amount: Long,
        paymentMode: String = "UPI",
        type: String = "expense",
        note: String = "",
        triggerUpiGuard: Boolean = true
    ) {
        if (type == "income") {
            addTransaction(title, category, amount, paymentMode, type, note, triggerUpiGuard = false)
            return
        }

        val settings = currentSettingsOrDefault()
        val now = System.currentTimeMillis()
        val isNightGuardWindow = settings.nightGuardEnabled && NightGuardSchedule.isWithinNightGuardWindow(
            nowMillis = now,
            startHour = settings.nightGuardStartHour,
            startMinute = settings.nightGuardStartMinute,
            endHour = settings.nightGuardEndHour,
            endMinute = settings.nightGuardEndMinute
        )

        val pendingReq = PendingSpendRequest(
            title = title,
            category = category,
            amount = amount,
            paymentMode = paymentMode,
            note = note,
            triggerUpiGuard = triggerUpiGuard
        )

        if (isNightGuardWindow) {
            _pendingNightGuardSpend.value = pendingReq
        } else if (settings.enablePreSpendConfirm) {
            _pendingPreSpendConfirm.value = pendingReq
        } else {
            addTransaction(title, category, amount, paymentMode, type, note, triggerUpiGuard)
        }
    }

    fun triggerNightGuardSimulation() {
        _pendingNightGuardSpend.value = PendingSpendRequest(
            title = "Midnight Limited Sneaker Drop",
            category = "Shopping",
            amount = 2499L,
            paymentMode = "UPI",
            note = "Late-night impulse test",
            triggerUpiGuard = true
        )
    }

    fun triggerNightGuardCooldown(cooldownEndMillis: Long, lastChallengeId: String) {
        viewModelScope.launch {
            repository.updateNightGuardCooldown(cooldownEndMillis, lastChallengeId)
        }
    }

    fun onNightGuardCancelPurchase(challengeCategory: String) {
        val req = _pendingNightGuardSpend.value ?: return
        _pendingNightGuardSpend.value = null
        viewModelScope.launch {
            repository.recordNightGuardEvent(
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "cancelled",
                challengeCategory = challengeCategory
            )
            _toastMessage.value = "🛡️ Ruko Zara: Cancelled '${req.title}' · Saved ₹${req.amount}!"
        }
    }

    fun onNightGuardSaveForTomorrow(challengeCategory: String) {
        val req = _pendingNightGuardSpend.value ?: return
        _pendingNightGuardSpend.value = null
        viewModelScope.launch {
            repository.recordNightGuardEvent(
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "postponed",
                challengeCategory = challengeCategory
            )
            repository.createPaymentReminder(
                title = "${req.title} (Postponed from Night Guard)",
                amount = req.amount,
                category = req.category,
                dueEpochMillis = System.currentTimeMillis() + 86_400_000L,
                intervalType = "same_day",
                note = "Postponed late-night expense — review with fresh mind"
            )
            _toastMessage.value = "🌅 Saved '${req.title}' (₹${req.amount}) for tomorrow morning review"
        }
    }

    fun onNightGuardContinueProceed(challengeCategory: String) {
        val req = _pendingNightGuardSpend.value ?: return
        _pendingNightGuardSpend.value = null
        viewModelScope.launch {
            repository.recordNightGuardEvent(
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "proceeded",
                challengeCategory = challengeCategory
            )
        }
        addTransaction(
            title = req.title,
            category = req.category,
            amount = req.amount,
            paymentMode = req.paymentMode,
            type = "expense",
            note = req.note.ifBlank { "Cleared Night Guard ($challengeCategory)" },
            triggerUpiGuard = req.triggerUpiGuard
        )
    }

    fun onNightGuardEssentialBypass(bypassReason: String) {
        val req = _pendingNightGuardSpend.value ?: return
        _pendingNightGuardSpend.value = null
        viewModelScope.launch {
            repository.recordNightGuardEvent(
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "essential_bypass",
                bypassReason = bypassReason
            )
        }
        addTransaction(
            title = req.title,
            category = req.category,
            amount = req.amount,
            paymentMode = req.paymentMode,
            type = "expense",
            note = "Essential Bypass: $bypassReason",
            triggerUpiGuard = false
        )
    }

    fun onPreSpendCancel() {
        val req = _pendingPreSpendConfirm.value
        _pendingPreSpendConfirm.value = null
        if (req != null) {
            _toastMessage.value = "🧘 Mindful pause: Cancelled ₹${req.amount} (${req.title})"
        }
    }

    fun onPreSpendSaveForTomorrow() {
        val req = _pendingPreSpendConfirm.value ?: return
        _pendingPreSpendConfirm.value = null
        viewModelScope.launch {
            repository.createPaymentReminder(
                title = "${req.title} (Postponed)",
                amount = req.amount,
                category = req.category,
                dueEpochMillis = System.currentTimeMillis() + 86_400_000L,
                intervalType = "same_day",
                note = "Postponed via Pre-Spend Reflection"
            )
            _toastMessage.value = "🌅 Postponed '${req.title}' (₹${req.amount}) until tomorrow"
        }
    }

    fun onPreSpendConfirmProceed() {
        val req = _pendingPreSpendConfirm.value ?: return
        _pendingPreSpendConfirm.value = null
        addTransaction(
            title = req.title,
            category = req.category,
            amount = req.amount,
            paymentMode = req.paymentMode,
            type = "expense",
            note = req.note,
            triggerUpiGuard = req.triggerUpiGuard
        )
    }

    fun createPaymentReminder(
        title: String,
        amount: Long,
        category: String,
        dueEpochMillis: Long,
        intervalType: String,
        note: String
    ) {
        viewModelScope.launch {
            val res = repository.createPaymentReminder(
                title = title,
                amount = amount,
                category = category,
                dueEpochMillis = dueEpochMillis,
                intervalType = intervalType,
                note = note
            )
            if (res.isSuccess) {
                _toastMessage.value = "🔔 Scheduled reminder: $title (₹$amount)"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to create reminder"
            }
        }
    }

    fun markPaymentReminderPaid(reminder: PaymentReminderRecord, isPaid: Boolean) {
        viewModelScope.launch {
            val res = repository.markPaymentReminderPaid(reminder.id, isPaid)
            if (res.isSuccess) {
                _toastMessage.value = if (isPaid) "✅ Marked '${reminder.title}' as Paid" else "Reopened '${reminder.title}'"
            }
        }
    }

    fun deletePaymentReminder(reminder: PaymentReminderRecord) {
        viewModelScope.launch {
            repository.deletePaymentReminder(reminder.id)
            _toastMessage.value = "🗑️ Removed reminder '${reminder.title}'"
        }
    }

    fun addTransaction(
        title: String,
        category: String,
        amount: Long,
        paymentMode: String = "UPI",
        type: String = "expense",
        note: String = "",
        triggerUpiGuard: Boolean = true
    ) {
        val settings = currentSettingsOrDefault()
        val expenses = currentExpensesOrEmpty()
        val now = System.currentTimeMillis()

        if (type == "expense" && triggerUpiGuard) {
            val todaySpentBefore = expenses
                .filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, now) }
                .sumOf { it.amount }
            val monthSpentBefore = expenses
                .filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, now) }
                .sumOf { it.amount }
            val sameCatToday = expenses
                .count { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, now) && it.category == category } + 1

            val eval = UpiAccountabilityEngine.evaluate(
                amount = amount,
                merchant = title,
                category = category,
                todaySpentBefore = todaySpentBefore,
                monthSpentBefore = monthSpentBefore,
                dailyLimit = settings.dailyLimit,
                sameCategoryTodayCount = sameCatToday,
                tone = settings.upiGuardTone
            )
            _latestAccountabilityAlert.value = eval

            // Auto-lockdown at 90% if enabled
            val nextToday = todaySpentBefore + amount
            if (settings.autoLockdownAt90 && settings.dailyLimit > 0 && nextToday >= (settings.dailyLimit * 90) / 100 && !settings.lockdownActive) {
                updateSettings(lockdownActive = true)
            }
        }

        viewModelScope.launch {
            val res = repository.createExpense(
                title = title,
                category = category,
                amount = amount,
                paymentMode = paymentMode,
                type = type,
                note = note,
                txEpochMillis = now
            )
            if (res.isSuccess) {
                val prefix = if (type == "income") "💰 Logged Income" else "⚡ Logged"
                _toastMessage.value = "$prefix ₹$amount · $title"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to save transaction"
            }
        }
    }

    fun deleteExpense(expenseId: String, title: String) {
        viewModelScope.launch {
            val res = repository.deleteExpense(expenseId)
            if (res.isSuccess) {
                _toastMessage.value = "🗑️ Deleted $title"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to delete"
            }
        }
    }

    fun addKhataFriend(
        name: String,
        phone: String,
        upiId: String,
        amount: Long,
        description: String
    ) {
        viewModelScope.launch {
            val res = repository.createKhataFriend(
                name = name,
                phone = phone,
                upiId = upiId,
                initialBalance = amount,
                description = description
            )
            if (res.isSuccess) {
                _toastMessage.value = "🤝 Added $name to Khata Ledger"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to add friend"
            }
        }
    }

    fun recordKhataEntry(
        friend: KhataFriendRecord,
        deltaAmount: Long,
        description: String
    ) {
        viewModelScope.launch {
            val res = repository.addKhataEntry(friend, deltaAmount, description)
            if (res.isSuccess) {
                val label = if (deltaAmount >= 0) "Lena Hai +₹$deltaAmount" else "Dena Hai -₹${-deltaAmount}"
                _toastMessage.value = "📒 Updated ${friend.name}: $label"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to update Khata"
            }
        }
    }

    fun settleFriend(friend: KhataFriendRecord) {
        viewModelScope.launch {
            val res = repository.settleKhataFriend(friend.id)
            if (res.isSuccess) {
                _toastMessage.value = "✅ Settled up with ${friend.name}!"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to settle"
            }
        }
    }

    fun splitBillEqually(
        friends: List<KhataFriendRecord>,
        totalBill: Long,
        description: String,
        includeSelf: Boolean
    ) {
        viewModelScope.launch {
            val res = repository.splitBillAmongFriends(friends, totalBill, description, includeSelf)
            if (res.isSuccess) {
                _toastMessage.value = "🍕 Split ₹$totalBill across ${friends.size} friends!"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to split bill"
            }
        }
    }

    fun createSavingsGoal(title: String, targetAmount: Long, savedAmount: Long) {
        viewModelScope.launch {
            val res = repository.createSavingsGoal(title, targetAmount, savedAmount)
            if (res.isSuccess) {
                _toastMessage.value = "🎯 Created Savings Goal: $title"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to create goal"
            }
        }
    }

    fun addSavingsProgress(goal: SavingsGoalRecord, addAmount: Long) {
        viewModelScope.launch {
            val res = repository.addFundsToSavingsGoal(goal, addAmount)
            if (res.isSuccess) {
                _toastMessage.value = "💎 Added ₹$addAmount to ${goal.title}!"
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to update goal"
            }
        }
    }

    fun deleteSavingsGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goalId)
            _toastMessage.value = "Removed savings goal"
        }
    }

    companion object {
        const val TAG = "KharcheeVM"
        const val STOP_TIMEOUT_MILLIS = 5000L

        /**
         * Summarizes earlier multi-turn conversation messages when history exceeds 8 turns so
         * J.A.R.V.I.S. retains long-context memory without exceeding prompt token budgets.
         */
        fun buildConversationContextSummary(messages: List<JarvisChatMessage>): String {
            if (messages.size <= 8) return ""
            val olderMessages = messages.dropLast(8)
            val userTopics = olderMessages
                .filter { it.role == "user" }
                .takeLast(6)
                .mapIndexed { idx, msg -> "${idx + 1}. User asked: \"${msg.text.take(80)}\"" }
            val jarvisKeyPoints = olderMessages
                .filter { it.role == "jarvis" }
                .takeLast(4)
                .map { msg -> "- Jarvis advised: \"${msg.text.replace("\n", " ").take(100)}...\"" }

            return buildString {
                appendLine("Earlier in this conversation (${olderMessages.size} prior messages compressed):")
                if (userTopics.isNotEmpty()) {
                    appendLine(userTopics.joinToString("\n"))
                }
                if (jarvisKeyPoints.isNotEmpty()) {
                    appendLine(jarvisKeyPoints.joinToString("\n"))
                }
            }.trim()
        }

        fun generateProactiveFinancialInsights(
            expenses: List<ExpenseRecord>,
            settings: UserSettings,
            khata: List<KhataFriendRecord> = emptyList()
        ): List<ProactiveFinancialInsight> {
            val insights = mutableListOf<ProactiveFinancialInsight>()
            val now = System.currentTimeMillis()
            val sevenDaysAgo = now - 7 * 86_400_000L
            val fourteenDaysAgo = now - 14 * 86_400_000L

            val recentOutflows = expenses.filter { !isIncomeTransaction(it) }
            val todayExpenses = recentOutflows.filter { isSameDay(it.txEpochMillis, now) }
            val thisWeekExpenses = recentOutflows.filter { it.txEpochMillis in sevenDaysAgo..now }
            val lastWeekExpenses = recentOutflows.filter { it.txEpochMillis in fourteenDaysAgo until sevenDaysAgo }

            val todaySpent = todayExpenses.sumOf { it.amount }
            val thisWeekSpent = thisWeekExpenses.sumOf { it.amount }
            val safeDailyLimit = max(50L, settings.dailyLimit)

            // Insight 1: Daily spending limit critical or near breach
            if (todaySpent > safeDailyLimit) {
                val overage = todaySpent - safeDailyLimit
                insights.add(
                    ProactiveFinancialInsight(
                        id = "daily_limit_exceeded",
                        title = "🚨 Daily Limit Exceeded",
                        message = "You spent ₹$todaySpent today, which is ₹$overage above your ₹$safeDailyLimit daily ceiling.",
                        category = "Budget",
                        severity = "warning",
                        suggestedPrompt = "I overspent my daily limit by ₹$overage today. Give me an immediate tactical plan to stabilize my spending.",
                        iconEmoji = "🚨",
                        highlightStat = "Exceeded by ₹$overage"
                    )
                )
            } else if (todaySpent >= (safeDailyLimit * 0.75).toLong() && safeDailyLimit > 0) {
                val remaining = safeDailyLimit - todaySpent
                insights.add(
                    ProactiveFinancialInsight(
                        id = "daily_limit_near",
                        title = "⚠️ 75%+ Daily Allowance Used",
                        message = "Only ₹$remaining remaining today out of your ₹$safeDailyLimit daily target.",
                        category = "Budget",
                        severity = "warning",
                        suggestedPrompt = "I only have ₹$remaining remaining for today. How should I pace my evening expenses?",
                        iconEmoji = "⚠️",
                        highlightStat = "₹$remaining left today"
                    )
                )
            }

            // Insight 2: Category Spend Surge (e.g. Food, Chai & Snacks)
            val foodThisWeek = thisWeekExpenses.filter { it.category == "Food" || it.category == "Chai & Snacks" }
            val foodThisWeekSum = foodThisWeek.sumOf { it.amount }
            val foodLastWeekSum = lastWeekExpenses.filter { it.category == "Food" || it.category == "Chai & Snacks" }.sumOf { it.amount }

            if (foodThisWeekSum > 0) {
                val foodRatioOfTotal = if (thisWeekSpent > 0) (foodThisWeekSum.toDouble() / thisWeekSpent.toDouble()) else 0.0
                if (foodLastWeekSum > 0 && foodThisWeekSum > foodLastWeekSum) {
                    val surgePct = (((foodThisWeekSum - foodLastWeekSum).toDouble() / foodLastWeekSum.toDouble()) * 100.0).roundToInt()
                    if (surgePct >= 15) {
                        insights.add(
                            ProactiveFinancialInsight(
                                id = "food_surge",
                                title = "🍔 Food Spend Surged +$surgePct% This Week",
                                message = "Your food & dining expenses reached ₹$foodThisWeekSum this week (up from ₹$foodLastWeekSum last week).",
                                category = "Food",
                                severity = "surge",
                                suggestedPrompt = "Your food spending has increased this week. Want to see why? Break down my top food expenses.",
                                iconEmoji = "🍔",
                                highlightStat = "+$surgePct% week-over-week"
                            )
                        )
                    }
                } else if (foodRatioOfTotal >= 0.35 && foodThisWeekSum >= 300L) {
                    val pct = (foodRatioOfTotal * 100.0).roundToInt()
                    insights.add(
                        ProactiveFinancialInsight(
                            id = "food_high_share",
                            title = "🍔 Food & Dining is $pct% of Weekly Outflow",
                            message = "You spent ₹$foodThisWeekSum on food & chai across ${foodThisWeek.size} transactions this week.",
                            category = "Food",
                            severity = "surge",
                            suggestedPrompt = "Why has my food spending taken up $pct% of my expenses this week? Give me budget hacks.",
                            iconEmoji = "🍔",
                            highlightStat = "₹$foodThisWeekSum on Food"
                        )
                    )
                }
            }

            // Insight 3: Chai & Tapri repeat tracking
            val chaiToday = todayExpenses.filter { it.category == "Chai & Snacks" }
            if (chaiToday.size >= 2) {
                val chaiSum = chaiToday.sumOf { it.amount }
                insights.add(
                    ProactiveFinancialInsight(
                        id = "chai_frequency",
                        title = "☕ Tapri Streak: ${chaiToday.size} Chai Visits Today",
                        message = "Logged ${chaiToday.size} chai & snacks expenses totaling ₹$chaiSum today.",
                        category = "Chai & Snacks",
                        severity = "info",
                        suggestedPrompt = "Jarvis bhai, I had ${chaiToday.size} chai/snacks stops today totaling ₹$chaiSum. Roast me!",
                        iconEmoji = "☕",
                        highlightStat = "${chaiToday.size} visits today"
                    )
                )
            }

            // Insight 4: Khata Friends pending recovery
            val friendsWhoOwe = khata.filter { it.balance > 0 }
            val totalOwedToUser = friendsWhoOwe.sumOf { it.balance }
            if (totalOwedToUser > 0L) {
                insights.add(
                    ProactiveFinancialInsight(
                        id = "khata_pending",
                        title = "👥 ₹$totalOwedToUser Pending Recovery",
                        message = "${friendsWhoOwe.size} friends owe you money in your Khata split ledger.",
                        category = "Khata",
                        severity = "info",
                        suggestedPrompt = "Who owes me money in Khata and what is the best way to politely remind them on WhatsApp?",
                        iconEmoji = "👥",
                        highlightStat = "₹$totalOwedToUser to recover"
                    )
                )
            }

            // Insight 5: Positive savings discipline
            if (todaySpent <= (safeDailyLimit * 0.5).toLong() && safeDailyLimit > 0 && todayExpenses.isNotEmpty()) {
                insights.add(
                    ProactiveFinancialInsight(
                        id = "high_discipline",
                        title = "🛡️ Excellent Daily Discipline",
                        message = "You have spent only ₹$todaySpent today, leaving ₹${safeDailyLimit - todaySpent} safe reserve intact.",
                        category = "Savings",
                        severity = "positive",
                        suggestedPrompt = "I am well within my daily limit today. What should I do with my leftover daily surplus?",
                        iconEmoji = "💎",
                        highlightStat = "Safe reserve intact"
                    )
                )
            }

            // Empty state fallback if insufficient transactions
            if (recentOutflows.size < 3) {
                insights.add(
                    ProactiveFinancialInsight(
                        id = "empty_starter",
                        title = "⚡ Initialize Your AI Financial Core",
                        message = "Track 3+ UPI or cash expenses or scan an SMS to unlock JARVIS autonomous spending leak detection.",
                        category = "Guide",
                        severity = "info",
                        suggestedPrompt = "What is the best daily spending limit for a 50/30/20 budget with ₹30,000 monthly income?",
                        iconEmoji = "🚀",
                        highlightStat = "Ready to analyze"
                    )
                )
            }

            return insights
        }
    }
}
