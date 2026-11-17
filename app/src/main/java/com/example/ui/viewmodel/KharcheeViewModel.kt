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
import com.example.receiver.AutoExpenseDetectionStore
import com.example.ui.jarvis.JarvisProposedAction
import com.example.ui.jarvis.parseNaturalLanguageActionProposal
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import com.example.util.PerformancePreferences
import com.example.util.PreFormattedTransactionUiModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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

    private fun isFirebaseAuthenticated(): Boolean {
        return runCatching {
            val uid = Firebase.auth.currentUser?.uid
            !uid.isNullOrBlank() && uid == currentUserId
        }.getOrDefault(false)
    }

    private val _localSettings = MutableStateFlow<UserSettings?>(UserSettings(id = currentUserId, userId = currentUserId))
    private val _localExpenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    private val _localDeletedExpenseIds = MutableStateFlow<Set<String>>(emptySet())
    private val _localKhataFriends = MutableStateFlow<List<KhataFriendRecord>>(emptyList())
    private val _localSavingsGoals = MutableStateFlow<List<SavingsGoalRecord>>(emptyList())
    private val _localPaymentReminders = MutableStateFlow<List<PaymentReminderRecord>>(emptyList())
    private val _localNightGuardEvents = MutableStateFlow<List<NightGuardEventRecord>>(emptyList())

    private val remoteSettingsFlow: Flow<UserSettings?> = if (isFirebaseAuthenticated()) {
        repository.observeUserSettings(currentUserId)
            .onStart { emit(null) }
            .catch { error ->
                val firestoreError = error as? FirebaseFirestoreException ?: error.cause as? FirebaseFirestoreException
                Log.w(TAG, "Error observing user settings: ${firestoreError?.code ?: error.message}")
                emit(null)
            }
    } else {
        flowOf(null)
    }

    val settingsState: StateFlow<UiState<UserSettings?>> = combine(
        remoteSettingsFlow,
        _localSettings
    ) { remote, local ->
        UiState.Success(remote ?: local ?: UserSettings(id = currentUserId, userId = currentUserId)) as UiState<UserSettings?>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(UserSettings(id = currentUserId, userId = currentUserId))
    )

    private val remoteExpensesFlow: Flow<List<ExpenseRecord>> = if (isFirebaseAuthenticated()) {
        repository.observeExpenses(currentUserId)
            .onStart { emit(emptyList()) }
            .catch { error ->
                val firestoreError = error as? FirebaseFirestoreException ?: error.cause as? FirebaseFirestoreException
                Log.w(TAG, "Error observing expenses: ${firestoreError?.code ?: error.message}")
                emit(emptyList())
            }
    } else {
        flowOf(emptyList())
    }

    val expensesState: StateFlow<UiState<List<ExpenseRecord>>> = combine(
        remoteExpensesFlow,
        _localExpenses,
        AutoExpenseDetectionStore.autoExpensesFlow,
        _localDeletedExpenseIds
    ) { remoteList, localList, autoSmsList, deletedIds ->
        val filteredAutoSms = autoSmsList.filter {
            it.userId == currentUserId || it.userId == "local_user" || it.userId.isBlank()
        }
        val merged = (localList + filteredAutoSms + remoteList)
            .filterNot { it.id in deletedIds }
            .distinctBy { rec ->
                if (rec.id.isNotBlank() && !rec.id.startsWith("local_tx_") && !rec.id.startsWith("auto_sms_")) {
                    rec.id
                } else {
                    "${rec.title.trim().lowercase()}:${rec.amount}:${rec.txEpochMillis / 2000L}"
                }
            }
            .sortedByDescending { it.txEpochMillis }
        UiState.Success(merged) as UiState<List<ExpenseRecord>>
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(emptyList())
    )

    private val _dashboardPageSize = MutableStateFlow(PerformancePreferences.DEFAULT_DASHBOARD_PAGE_SIZE)
    val dashboardPageSize: StateFlow<Int> = _dashboardPageSize.asStateFlow()

    /**
     * Paginated top-20 dashboard slice + pre-formatted currency/date strings computed on Dispatchers.IO
     * so @Composable draw phases never execute heavy formatting or iterate thousands of items.
     */
    val paginatedPreFormattedExpensesState: StateFlow<List<PreFormattedTransactionUiModel>> = combine(
        expensesState,
        _dashboardPageSize
    ) { uiState, pageLimit ->
        val raw = (uiState as? UiState.Success)?.data ?: emptyList()
        raw.take(pageLimit.coerceAtLeast(20)).map { tx ->
            val isInc = isIncomeTransaction(tx)
            val inrStr = PerformancePreferences.preFormatInr(tx.amount)
            PreFormattedTransactionUiModel(
                id = tx.id.ifBlank { "${tx.title}_${tx.txEpochMillis}" },
                title = tx.title.ifBlank { tx.category },
                category = tx.category,
                amount = tx.amount,
                formattedInrAmount = inrStr,
                signedFormattedAmount = "${if (isInc) "+" else "-"}$inrStr",
                formattedDateLabel = PerformancePreferences.preFormatDate(tx.txEpochMillis),
                isIncome = isInc,
                paymentMode = tx.paymentMode,
                txEpochMillis = tx.txEpochMillis
            )
        }
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    fun loadMoreDashboardTransactions() {
        _dashboardPageSize.value = (_dashboardPageSize.value + 20).coerceAtMost(1000)
    }

    private val remoteKhataFlow: Flow<List<KhataFriendRecord>> = if (isFirebaseAuthenticated()) {
        repository.observeKhataFriends(currentUserId)
            .onStart { emit(emptyList()) }
            .catch { error ->
                val firestoreError = error as? FirebaseFirestoreException ?: error.cause as? FirebaseFirestoreException
                Log.w(TAG, "Error observing khata friends: ${firestoreError?.code ?: error.message}")
                emit(emptyList())
            }
    } else {
        flowOf(emptyList())
    }

    val khataFriendsState: StateFlow<UiState<List<KhataFriendRecord>>> = combine(
        remoteKhataFlow,
        _localKhataFriends
    ) { remote, local ->
        val merged = (local + remote).distinctBy { it.id.ifBlank { it.name.lowercase() } }
        UiState.Success(merged) as UiState<List<KhataFriendRecord>>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(emptyList())
    )

    private val remoteGoalsFlow: Flow<List<SavingsGoalRecord>> = if (isFirebaseAuthenticated()) {
        repository.observeSavingsGoals(currentUserId)
            .onStart { emit(emptyList()) }
            .catch { error ->
                val firestoreError = error as? FirebaseFirestoreException ?: error.cause as? FirebaseFirestoreException
                Log.w(TAG, "Error observing savings goals: ${firestoreError?.code ?: error.message}")
                emit(emptyList())
            }
    } else {
        flowOf(emptyList())
    }

    val savingsGoalsState: StateFlow<UiState<List<SavingsGoalRecord>>> = combine(
        remoteGoalsFlow,
        _localSavingsGoals
    ) { remote, local ->
        val merged = (local + remote).distinctBy { it.id.ifBlank { it.title.lowercase() } }
        UiState.Success(merged) as UiState<List<SavingsGoalRecord>>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(emptyList())
    )

    private val _localJarvisMessages = MutableStateFlow<List<JarvisChatMessage>>(emptyList())

    private val remoteJarvisFlow: Flow<List<JarvisChatMessage>> = if (isFirebaseAuthenticated()) {
        repository.observeJarvisMessages(currentUserId)
            .onStart { emit(emptyList()) }
            .catch { emit(emptyList()) }
    } else {
        flowOf(emptyList())
    }

    val jarvisMessagesState: StateFlow<UiState<List<JarvisChatMessage>>> = combine(
        remoteJarvisFlow,
        _localJarvisMessages
    ) { remoteList, localList ->
        val merged = (remoteList + localList)
            .distinctBy { "${it.role}:${it.txEpochMillis}:${it.text.hashCode()}" }
            .sortedBy { it.txEpochMillis }
        UiState.Success(merged) as UiState<List<JarvisChatMessage>>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(emptyList())
    )

    private val remotePaymentRemindersFlow: Flow<List<PaymentReminderRecord>> = if (isFirebaseAuthenticated()) {
        repository.observePaymentReminders(currentUserId)
            .onStart { emit(emptyList()) }
            .catch { error ->
                val firestoreError = error as? FirebaseFirestoreException ?: error.cause as? FirebaseFirestoreException
                Log.w(TAG, "Error observing payment reminders: ${firestoreError?.code ?: error.message}")
                emit(emptyList())
            }
    } else {
        flowOf(emptyList())
    }

    val paymentRemindersState: StateFlow<UiState<List<PaymentReminderRecord>>> = combine(
        remotePaymentRemindersFlow,
        _localPaymentReminders
    ) { remote, local ->
        val merged = (local + remote).distinctBy { it.id.ifBlank { "${it.title}:${it.dueEpochMillis}" } }
        UiState.Success(merged) as UiState<List<PaymentReminderRecord>>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(emptyList())
    )

    private val remoteNightGuardFlow: Flow<List<NightGuardEventRecord>> = if (isFirebaseAuthenticated()) {
        repository.observeNightGuardEvents(currentUserId)
            .onStart { emit(emptyList()) }
            .catch { error ->
                val firestoreError = error as? FirebaseFirestoreException ?: error.cause as? FirebaseFirestoreException
                Log.w(TAG, "Error observing Night Guard events: ${firestoreError?.code ?: error.message}")
                emit(emptyList())
            }
    } else {
        flowOf(emptyList())
    }

    val nightGuardEventsState: StateFlow<UiState<List<NightGuardEventRecord>>> = combine(
        remoteNightGuardFlow,
        _localNightGuardEvents
    ) { remote, local ->
        val merged = (local + remote).distinctBy { it.id.ifBlank { "${it.title}:${it.eventEpochMillis}" } }
        UiState.Success(merged) as UiState<List<NightGuardEventRecord>>
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Success(emptyList())
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
        if (isFirebaseAuthenticated()) {
            viewModelScope.launch {
                repository.seedInitialDataIfNeeded()
            }
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

    fun upgradeProPlan(
        tier: String,
        durationDays: Int,
        orderId: String = "",
        paymentId: String = "",
        utrNumber: String = "",
        deviceHash: String = "KHARCHEE"
    ) {
        viewModelScope.launch {
            val res = repository.updateProMembership(
                tier = tier,
                durationDays = durationDays,
                orderId = orderId,
                paymentId = paymentId,
                utrNumber = utrNumber,
                deviceHash = deviceHash
            )
            if (res.isSuccess) {
                _toastMessage.value = "⏳ Payment verification request submitted for ${tier.uppercase()}. Pro access activates once verified by server."
            } else {
                _toastMessage.value = res.exceptionOrNull()?.localizedMessage
                    ?: "Backend payment verification service unavailable — Pro status remains unchanged."
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
        return buildLiveFinancialContextSnapshotFromData(
            userDisplayName = userDisplayName,
            expenses = expenses,
            settings = settings,
            khata = khata,
            goals = goals
        )
    }

    fun sendJarvisMessage(
        rawPrompt: String,
        userDisplayName: String = "Commander",
        extraKhataRecords: List<KhataFriendRecord> = emptyList()
    ) {
        val cleanPrompt = rawPrompt.trim()
        if (cleanPrompt.isEmpty()) return

        val activeMode = _jarvisMode.value
        val activeLang = _jarvisLanguage.value

        _jarvisError.value = null
        _jarvisStreamingReply.value = ""

        val now = System.currentTimeMillis()
        val userMsg = JarvisChatMessage(
            id = "local_user_$now",
            userId = currentUserId,
            role = "user",
            text = cleanPrompt,
            mode = activeMode,
            language = activeLang,
            txEpochMillis = now
        )

        // 1. Check if user typed a natural quick-add command (e.g., "Add 150 Chai", "spent 200 uber", "milla 500 bonus", "debt 300 Rahul")
        val quickAddCmd = parseQuickAddExpenseCommand(cleanPrompt)
        val deterministicReply: String
        if (quickAddCmd != null) {
            _pendingProposedAction.value = null
            when (quickAddCmd.actionKind) {
                "income" -> {
                    addTransaction(
                        title = quickAddCmd.title,
                        category = "Salary",
                        amount = quickAddCmd.amount,
                        paymentMode = "UPI",
                        type = "income",
                        note = "Logged via Jarvis CFO",
                        triggerUpiGuard = false
                    )
                    deterministicReply = "Transaction logged instantly! ₹${quickAddCmd.amount} ${quickAddCmd.title} added to income."
                }
                "debt" -> {
                    addKhataFriend(
                        name = quickAddCmd.title,
                        phone = "",
                        upiId = "",
                        amount = quickAddCmd.amount,
                        description = "Logged via Jarvis CFO"
                    )
                    deterministicReply = "Transaction logged instantly! ₹${quickAddCmd.amount} ${quickAddCmd.title} recorded in Khata ledger."
                }
                else -> {
                    addTransaction(
                        title = quickAddCmd.title,
                        category = quickAddCmd.category,
                        amount = quickAddCmd.amount,
                        paymentMode = "UPI",
                        type = "expense",
                        note = "Logged via Jarvis CFO"
                    )
                    deterministicReply = "Transaction logged instantly! ₹${quickAddCmd.amount} ${quickAddCmd.title} added to spends."
                }
            }
        } else {
            // Check if the user prompt requests a navigation/settings confirmation-gated action
            val detectedProposal = parseNaturalLanguageActionProposal(cleanPrompt)
            if (detectedProposal != null && detectedProposal.actionType != "LOG_EXPENSE") {
                _pendingProposedAction.value = detectedProposal
            }

            val mergedKhata = (currentKhataOrEmpty() + extraKhataRecords)
                .distinctBy { it.name.trim().lowercase() }

            deterministicReply = buildDeterministicJarvisFallbackReply(
                prompt = cleanPrompt,
                userDisplayName = userDisplayName,
                mode = activeMode,
                language = activeLang,
                expenses = currentExpensesOrEmpty(),
                settings = currentSettingsOrDefault(),
                khata = mergedKhata,
                goals = currentSavingsGoalsOrEmpty()
            )
        }

        val replyTimestamp = now + 1L
        val jarvisMsg = JarvisChatMessage(
            id = "local_jarvis_$replyTimestamp",
            userId = currentUserId,
            role = "jarvis",
            text = deterministicReply,
            mode = activeMode,
            language = activeLang,
            txEpochMillis = replyTimestamp
        )

        // Immediately publish both messages to the UI with zero blocking wait
        _localJarvisMessages.value = _localJarvisMessages.value + userMsg + jarvisMsg
        _latestSpokenJarvisText.value = deterministicReply
        _jarvisIsGenerating.value = false

        // Non-blocking background sync to Firestore (safe against offline/timeout)
        viewModelScope.launch {
            runCatching {
                withTimeout(2_500L) {
                    repository.saveJarvisMessage(
                        role = "user",
                        text = cleanPrompt,
                        mode = activeMode,
                        language = activeLang,
                        txEpochMillis = now
                    )
                    repository.saveJarvisMessage(
                        role = "jarvis",
                        text = deterministicReply,
                        mode = activeMode,
                        language = activeLang,
                        txEpochMillis = replyTimestamp
                    )
                }
            }
        }
    }

    fun clearJarvisConversation() {
        _localJarvisMessages.value = emptyList()
        _jarvisError.value = null
        _jarvisStreamingReply.value = ""
        _toastMessage.value = "🧹 Cleared J.A.R.V.I.S. conversation history"
        viewModelScope.launch {
            runCatching {
                withTimeout(2_500L) {
                    repository.clearJarvisMessages()
                }
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
        val updatedLocal = current.copy(
            dailyLimit = dailyLimit ?: current.dailyLimit,
            lockdownActive = lockdownActive ?: current.lockdownActive,
            autoLockdownAt90 = autoLockdownAt90 ?: current.autoLockdownAt90,
            stealthMode = stealthMode ?: current.stealthMode,
            themeId = themeId ?: current.themeId,
            myUpiId = myUpiId ?: current.myUpiId,
            upiGuardTone = upiGuardTone ?: current.upiGuardTone,
            nightGuardEnabled = nightGuardEnabled ?: current.nightGuardEnabled,
            nightGuardStartHour = nightGuardStartHour ?: current.nightGuardStartHour,
            nightGuardStartMinute = nightGuardStartMinute ?: current.nightGuardStartMinute,
            nightGuardEndHour = nightGuardEndHour ?: current.nightGuardEndHour,
            nightGuardEndMinute = nightGuardEndMinute ?: current.nightGuardEndMinute,
            enablePreSpendConfirm = enablePreSpendConfirm ?: current.enablePreSpendConfirm,
            notifyPaymentReminders = notifyPaymentReminders ?: current.notifyPaymentReminders,
            notifyNightGuardAlert = notifyNightGuardAlert ?: current.notifyNightGuardAlert,
            notifyMorningReview = notifyMorningReview ?: current.notifyMorningReview
        )
        _localSettings.value = updatedLocal
        if (toastText != null) {
            _toastMessage.value = toastText
        }
        if (!isFirebaseAuthenticated()) return

        viewModelScope.launch {
            repository.upsertUserSettings(
                dailyLimit = updatedLocal.dailyLimit,
                lockdownActive = updatedLocal.lockdownActive,
                autoLockdownAt90 = updatedLocal.autoLockdownAt90,
                stealthMode = updatedLocal.stealthMode,
                themeId = updatedLocal.themeId,
                myUpiId = updatedLocal.myUpiId,
                upiGuardTone = updatedLocal.upiGuardTone,
                nightGuardEnabled = updatedLocal.nightGuardEnabled,
                nightGuardStartHour = updatedLocal.nightGuardStartHour,
                nightGuardStartMinute = updatedLocal.nightGuardStartMinute,
                nightGuardEndHour = updatedLocal.nightGuardEndHour,
                nightGuardEndMinute = updatedLocal.nightGuardEndMinute,
                nightGuardCooldownEndMillis = updatedLocal.nightGuardCooldownEndMillis,
                lastChallengeId = updatedLocal.lastChallengeId,
                enablePreSpendConfirm = updatedLocal.enablePreSpendConfirm,
                notifyPaymentReminders = updatedLocal.notifyPaymentReminders,
                notifyNightGuardAlert = updatedLocal.notifyNightGuardAlert,
                notifyMorningReview = updatedLocal.notifyMorningReview
            )
        }
    }

    /**
     * Zero-friction 1-Tap Expense Logging (bypasses confirmation dialogs and Night Guard modals).
     * Used by the 1-Tap Quick Repeating Spend Strip and Smart Clipboard Auto-Detection "Log Now" button.
     */
    fun instantOneTapLogExpense(
        title: String,
        category: String,
        amount: Long,
        paymentMode: String = "UPI",
        note: String = "1-Tap Instant Log"
    ) {
        addTransaction(
            title = title,
            category = category,
            amount = amount,
            paymentMode = paymentMode,
            type = "expense",
            note = note,
            triggerUpiGuard = true
        )
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
        val current = currentSettingsOrDefault()
        _localSettings.value = current.copy(
            nightGuardCooldownEndMillis = cooldownEndMillis,
            lastChallengeId = lastChallengeId
        )
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.updateNightGuardCooldown(cooldownEndMillis, lastChallengeId)
        }
    }

    fun onNightGuardCancelPurchase(challengeCategory: String) {
        val req = _pendingNightGuardSpend.value ?: return
        _pendingNightGuardSpend.value = null
        val now = System.currentTimeMillis()
        _localNightGuardEvents.value = listOf(
            NightGuardEventRecord(
                id = "local_ng_$now",
                userId = currentUserId,
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "cancelled",
                challengeCategory = challengeCategory,
                eventEpochMillis = now
            )
        ) + _localNightGuardEvents.value
        _toastMessage.value = "🛡️ Ruko Zara: Cancelled '${req.title}' · Saved ₹${req.amount}!"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.recordNightGuardEvent(
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "cancelled",
                challengeCategory = challengeCategory
            )
        }
    }

    fun onNightGuardSaveForTomorrow(challengeCategory: String) {
        val req = _pendingNightGuardSpend.value ?: return
        _pendingNightGuardSpend.value = null
        val now = System.currentTimeMillis()
        _localNightGuardEvents.value = listOf(
            NightGuardEventRecord(
                id = "local_ng_$now",
                userId = currentUserId,
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "postponed",
                challengeCategory = challengeCategory,
                eventEpochMillis = now
            )
        ) + _localNightGuardEvents.value
        createPaymentReminder(
            title = "${req.title} (Postponed from Night Guard)",
            amount = req.amount,
            category = req.category,
            dueEpochMillis = now + 86_400_000L,
            intervalType = "same_day",
            note = "Postponed late-night expense — review with fresh mind"
        )
        _toastMessage.value = "🌅 Saved '${req.title}' (₹${req.amount}) for tomorrow morning review"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.recordNightGuardEvent(
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "postponed",
                challengeCategory = challengeCategory
            )
        }
    }

    fun onNightGuardContinueProceed(challengeCategory: String) {
        val req = _pendingNightGuardSpend.value ?: return
        _pendingNightGuardSpend.value = null
        val now = System.currentTimeMillis()
        _localNightGuardEvents.value = listOf(
            NightGuardEventRecord(
                id = "local_ng_$now",
                userId = currentUserId,
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "proceeded",
                challengeCategory = challengeCategory,
                eventEpochMillis = now
            )
        ) + _localNightGuardEvents.value
        if (isFirebaseAuthenticated()) {
            viewModelScope.launch {
                repository.recordNightGuardEvent(
                    title = req.title,
                    category = req.category,
                    amount = req.amount,
                    outcome = "proceeded",
                    challengeCategory = challengeCategory
                )
            }
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
        val now = System.currentTimeMillis()
        _localNightGuardEvents.value = listOf(
            NightGuardEventRecord(
                id = "local_ng_$now",
                userId = currentUserId,
                title = req.title,
                category = req.category,
                amount = req.amount,
                outcome = "essential_bypass",
                bypassReason = bypassReason,
                eventEpochMillis = now
            )
        ) + _localNightGuardEvents.value
        if (isFirebaseAuthenticated()) {
            viewModelScope.launch {
                repository.recordNightGuardEvent(
                    title = req.title,
                    category = req.category,
                    amount = req.amount,
                    outcome = "essential_bypass",
                    bypassReason = bypassReason
                )
            }
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
        createPaymentReminder(
            title = "${req.title} (Postponed)",
            amount = req.amount,
            category = req.category,
            dueEpochMillis = System.currentTimeMillis() + 86_400_000L,
            intervalType = "same_day",
            note = "Postponed via Pre-Spend Reflection"
        )
        _toastMessage.value = "🌅 Postponed '${req.title}' (₹${req.amount}) until tomorrow"
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
        val now = System.currentTimeMillis()
        val localReminder = PaymentReminderRecord(
            id = "local_rem_$now",
            userId = currentUserId,
            title = title,
            amount = amount,
            category = category,
            dueEpochMillis = dueEpochMillis,
            intervalType = intervalType,
            isPaid = false,
            note = note
        )
        _localPaymentReminders.value = listOf(localReminder) + _localPaymentReminders.value
        _toastMessage.value = "🔔 Scheduled reminder: $title (₹$amount)"
        if (!isFirebaseAuthenticated()) return

        viewModelScope.launch {
            repository.createPaymentReminder(
                title = title,
                amount = amount,
                category = category,
                dueEpochMillis = dueEpochMillis,
                intervalType = intervalType,
                note = note
            )
        }
    }

    fun markPaymentReminderPaid(reminder: PaymentReminderRecord, isPaid: Boolean) {
        _localPaymentReminders.value = _localPaymentReminders.value.map {
            if (it.id == reminder.id) it.copy(isPaid = isPaid) else it
        }
        _toastMessage.value = if (isPaid) "✅ Marked '${reminder.title}' as Paid" else "Reopened '${reminder.title}'"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.markPaymentReminderPaid(reminder.id, isPaid)
        }
    }

    fun deletePaymentReminder(reminder: PaymentReminderRecord) {
        _localPaymentReminders.value = _localPaymentReminders.value.filterNot { it.id == reminder.id }
        _toastMessage.value = "🗑️ Removed reminder '${reminder.title}'"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.deletePaymentReminder(reminder.id)
        }
    }

    fun addExpense(
        title: String,
        category: String = "Food",
        amount: Long
    ) {
        addTransaction(
            title = title,
            category = category,
            amount = amount,
            paymentMode = "UPI",
            type = "expense",
            note = "Logged via Jarvis Quick Command"
        )
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

        val localRecord = ExpenseRecord(
            id = "local_tx_$now",
            userId = currentUserId,
            title = title,
            category = category,
            amount = amount,
            paymentMode = paymentMode,
            type = type,
            note = note,
            txEpochMillis = now
        )
        _localExpenses.value = listOf(localRecord) + _localExpenses.value
        val prefix = if (type == "income") "💰 Logged Income" else "⚡ Logged"
        _toastMessage.value = "$prefix ₹$amount · $title"

        if (!isFirebaseAuthenticated()) return

        viewModelScope.launch {
            repository.createExpense(
                title = title,
                category = category,
                amount = amount,
                paymentMode = paymentMode,
                type = type,
                note = note,
                txEpochMillis = now
            )
        }
    }

    fun deleteExpense(expenseId: String, title: String) {
        _localDeletedExpenseIds.value = _localDeletedExpenseIds.value + expenseId
        _localExpenses.value = _localExpenses.value.filterNot { it.id == expenseId }
        _toastMessage.value = "🗑️ Deleted $title"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    fun updateExistingExpense(expense: ExpenseRecord) {
        val currentLocal = _localExpenses.value
        val idx = currentLocal.indexOfFirst { it.id == expense.id }
        if (idx >= 0) {
            val mutable = currentLocal.toMutableList()
            mutable[idx] = expense
            _localExpenses.value = mutable
        } else {
            _localExpenses.value = listOf(expense) + currentLocal.filterNot { it.id == expense.id }
        }
        _toastMessage.value = "✏️ Updated ${expense.title} (₹${expense.amount})"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteUserAccountData(onComplete: () -> Unit) {
        _localExpenses.value = emptyList()
        _localKhataFriends.value = emptyList()
        _localSavingsGoals.value = emptyList()
        _localPaymentReminders.value = emptyList()
        _localNightGuardEvents.value = emptyList()
        _localJarvisMessages.value = emptyList()
        if (!isFirebaseAuthenticated()) {
            _toastMessage.value = "Account data wiped from vault."
            onComplete()
            return
        }
        viewModelScope.launch {
            repository.deleteAllUserData()
            _toastMessage.value = "Account data wiped from vault."
            onComplete()
        }
    }

    /**
     * Populates the live ViewModel flows and syncs with Firestore when a user restores
     * from an encrypted `kharchee_secure_backup.enc` file or Google Drive AppData backup.
     */
    fun applyRestoredBackupSnapshot(
        snapshot: com.example.util.BackupVaultSnapshot,
        mergeWithExisting: Boolean = true
    ) {
        if (mergeWithExisting) {
            _localExpenses.value = (snapshot.expenses + _localExpenses.value)
                .distinctBy { "${it.title.trim().lowercase()}:${it.amount}:${it.txEpochMillis / 2000L}" }
                .sortedByDescending { it.txEpochMillis }
            _localSavingsGoals.value = (snapshot.savingsGoals + _localSavingsGoals.value)
                .distinctBy { it.title.trim().lowercase() }
            _localKhataFriends.value = (snapshot.khataFriends + _localKhataFriends.value)
                .distinctBy { it.name.trim().lowercase() }
            _localPaymentReminders.value = (snapshot.paymentReminders + _localPaymentReminders.value)
                .distinctBy { "${it.title.trim().lowercase()}:${it.dueEpochMillis}" }
        } else {
            _localDeletedExpenseIds.value = emptySet()
            _localExpenses.value = snapshot.expenses.sortedByDescending { it.txEpochMillis }
            _localSavingsGoals.value = snapshot.savingsGoals
            _localKhataFriends.value = snapshot.khataFriends
            _localPaymentReminders.value = snapshot.paymentReminders
        }
        _localSettings.value = snapshot.settings.copy(
            id = currentUserId,
            userId = currentUserId,
            dailyLimit = snapshot.dailyLimit.coerceAtLeast(50L)
        )
        _toastMessage.value = "✅ Restored ₹${snapshot.netBalanceAmount} balance & ${snapshot.expenses.size} transactions!"
    }

    fun addKhataFriend(
        name: String,
        phone: String,
        upiId: String,
        amount: Long,
        description: String
    ) {
        val now = System.currentTimeMillis()
        val initials = name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "FR" }
        val localFriend = KhataFriendRecord(
            id = "local_khata_$now",
            userId = currentUserId,
            name = name,
            phone = phone,
            avatarInitials = initials,
            upiId = upiId,
            balance = amount,
            lastDescription = description,
            settled = amount == 0L
        )
        _localKhataFriends.value = listOf(localFriend) + _localKhataFriends.value
        _toastMessage.value = "🤝 Added $name to Khata Ledger"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.createKhataFriend(
                name = name,
                phone = phone,
                upiId = upiId,
                initialBalance = amount,
                description = description
            )
        }
    }

    fun recordKhataEntry(
        friend: KhataFriendRecord,
        deltaAmount: Long,
        description: String
    ) {
        val nextBalance = friend.balance + deltaAmount
        _localKhataFriends.value = _localKhataFriends.value.map {
            if (it.id == friend.id) it.copy(balance = nextBalance, lastDescription = description, settled = nextBalance == 0L) else it
        }
        val label = if (deltaAmount >= 0) "Lena Hai +₹$deltaAmount" else "Dena Hai -₹${-deltaAmount}"
        _toastMessage.value = "📒 Updated ${friend.name}: $label"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.addKhataEntry(friend, deltaAmount, description)
        }
    }

    fun settleFriend(friend: KhataFriendRecord) {
        _localKhataFriends.value = _localKhataFriends.value.map {
            if (it.id == friend.id) it.copy(balance = 0L, settled = true, lastDescription = "Settled up") else it
        }
        _toastMessage.value = "✅ Settled up with ${friend.name}!"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.settleKhataFriend(friend.id)
        }
    }

    fun splitBillEqually(
        friends: List<KhataFriendRecord>,
        totalBill: Long,
        description: String,
        includeSelf: Boolean
    ) {
        if (friends.isEmpty() || totalBill <= 0L) return
        val divisor = if (includeSelf) friends.size + 1 else friends.size
        val share = totalBill / max(1, divisor)
        val targetIds = friends.map { it.id }.toSet()
        _localKhataFriends.value = _localKhataFriends.value.map {
            if (it.id in targetIds) {
                val nb = it.balance + share
                it.copy(balance = nb, settled = nb == 0L, lastDescription = description)
            } else {
                it
            }
        }
        _toastMessage.value = "🍕 Split ₹$totalBill across ${friends.size} friends!"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.splitBillAmongFriends(friends, totalBill, description, includeSelf)
        }
    }

    fun createSavingsGoal(title: String, targetAmount: Long, savedAmount: Long) {
        val now = System.currentTimeMillis()
        val localGoal = SavingsGoalRecord(
            id = "local_goal_$now",
            userId = currentUserId,
            title = title,
            targetAmount = targetAmount,
            savedAmount = savedAmount
        )
        _localSavingsGoals.value = listOf(localGoal) + _localSavingsGoals.value
        _toastMessage.value = "🎯 Created Savings Goal: $title"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.createSavingsGoal(title, targetAmount, savedAmount)
        }
    }

    fun addSavingsProgress(goal: SavingsGoalRecord, addAmount: Long) {
        _localSavingsGoals.value = _localSavingsGoals.value.map {
            if (it.id == goal.id) it.copy(savedAmount = it.savedAmount + addAmount) else it
        }
        _toastMessage.value = "💎 Added ₹$addAmount to ${goal.title}!"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.addFundsToSavingsGoal(goal, addAmount)
        }
    }

    fun deleteSavingsGoal(goalId: String) {
        _localSavingsGoals.value = _localSavingsGoals.value.filterNot { it.id == goalId }
        _toastMessage.value = "Removed savings goal"
        if (!isFirebaseAuthenticated()) return
        viewModelScope.launch {
            repository.deleteSavingsGoal(goalId)
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

        /**
         * Builds the full live financial context telemetry string from real user data,
         * including Current Total Balance, Monthly Inflow/Outflow, Top Spending Category, and Remaining Budget.
         */
        fun buildLiveFinancialContextSnapshotFromData(
            userDisplayName: String,
            expenses: List<ExpenseRecord>,
            settings: UserSettings,
            khata: List<KhataFriendRecord> = emptyList(),
            goals: List<SavingsGoalRecord> = emptyList()
        ): String {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)

            val allIncomes = expenses.filter { isIncomeTransaction(it) }
            val allExpenses = expenses.filter { !isIncomeTransaction(it) }
            val todayExpenses = allExpenses.filter { isSameDay(it.txEpochMillis, now) }
            val monthExpenses = allExpenses.filter { isSameMonth(it.txEpochMillis, now) }
            val monthIncomes = allIncomes.filter { isSameMonth(it.txEpochMillis, now) }

            val todaySpent = todayExpenses.sumOf { it.amount }
            val monthSpent = monthExpenses.sumOf { it.amount }
            val monthIncomeTotal = monthIncomes.sumOf { it.amount }
            val totalIncomeAll = allIncomes.sumOf { it.amount }
            val totalExpenseAll = allExpenses.sumOf { it.amount }
            val monthlyBudget = max(1500L, settings.dailyLimit * 30L)
            val remainingMonthlyBudget = (monthlyBudget - monthSpent).coerceAtLeast(0L)
            val dailyRemaining = (settings.dailyLimit - todaySpent).coerceAtLeast(0L)
            val totalBalance = totalIncomeAll - totalExpenseAll

            val sortedCategories = monthExpenses
                .groupBy { it.category.ifBlank { "General" } }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .entries
                .sortedByDescending { it.value }

            val topCategoryEntry = sortedCategories.firstOrNull()
            val topCategorySummary = if (topCategoryEntry != null) {
                val sharePct = if (monthSpent > 0L) ((topCategoryEntry.value * 100L) / monthSpent) else 0L
                "${topCategoryEntry.key} (₹${topCategoryEntry.value} · $sharePct% of monthly outflow)"
            } else {
                "None logged this month (₹0)"
            }

            var streak = 0
            for (i in 0 until 30) {
                val dayTarget = now - i * 86_400_000L
                val daySum = allExpenses
                    .filter { isSameDay(it.txEpochMillis, dayTarget) }
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

            val categoryBreakdown = sortedCategories
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
                Current Total Balance: ₹$totalBalance
                Monthly Inflow (Income): ₹$monthIncomeTotal
                Monthly Outflow (Expenses): ₹$monthSpent
                Net Monthly Cashflow: ₹${monthIncomeTotal - monthSpent}
                Top Spending Category: $topCategorySummary
                Monthly Budget Target (30x Daily): ₹$monthlyBudget
                Remaining Monthly Budget: ₹$remainingMonthlyBudget
                Daily Spending Limit: ₹${settings.dailyLimit}
                Today's Total Expense: ₹$todaySpent (Remaining today: ₹$dailyRemaining)
                Lockdown Shield Active: ${settings.lockdownActive}
                Discipline Score: $clampedScore / 900 (Streak: $streak days under daily limit)
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

        data class JarvisQuickExpenseCommand(
            val title: String,
            val category: String,
            val amount: Long,
            val actionKind: String = "expense" // "expense" | "income" | "debt"
        )

        /**
         * Parses natural language quick commands like:
         * - "Add 150 Chai" -> category = "Food", amount = 150, title = "Chai", actionKind = "expense"
         * - "spent ₹250 uber" -> category = "Travel", amount = 250, title = "Uber", actionKind = "expense"
         * - "milla 500 cashback" -> category = "Income", amount = 500, title = "Cashback", actionKind = "income"
         * - "debt 300 Rahul" -> category = "Khata", amount = 300, title = "Rahul", actionKind = "debt"
         */
        fun parseQuickAddExpenseCommand(rawPrompt: String): JarvisQuickExpenseCommand? {
            val clean = rawPrompt.trim()
            val lower = clean.lowercase()
            if (lower.contains("limit") || lower.contains("lockdown") || lower.contains("goal")) {
                return null
            }

            // Pattern 1: "Add 150 chai", "add ₹150 for chai", "spent 250 on uber", "milla 500 bonus", "debt 300 rahul"
            val amountFirstRegex = Regex(
                """^(?:(add|log|spent|spend|record|milla|mila|received|debt|udhar|udhaar))(?:\s+expense|\s+income)?\s+(?:₹|rs\.?|inr)?\s*(\d{1,7})\s+(?:for\s+|on\s+|in\s+|from\s+|to\s+|ka\s+|ke\s+liye\s+)?(.+)$""",
                RegexOption.IGNORE_CASE
            )
            // Pattern 2: "Add chai 150", "spent coffee ₹120", "milla salary 5000", "debt rahul 300"
            val titleFirstRegex = Regex(
                """^(?:(add|log|spent|spend|record|milla|mila|received|debt|udhar|udhaar))(?:\s+expense|\s+income)?\s+([a-zA-Z][a-zA-Z0-9\s]{1,30}?)\s+(?:₹|rs\.?|inr)?\s*(\d{1,7})$""",
                RegexOption.IGNORE_CASE
            )
            // Pattern 3: "150 chai add karo" or "500 cashback milla"
            val suffixAddRegex = Regex(
                """^(?:₹|rs\.?|inr)?\s*(\d{1,7})\s+([a-zA-Z][a-zA-Z0-9\s]{1,30}?)\s+(add|log|spent|milla|mila|debt|udhar)(?:\s+karo|\s+kar\s+do)?$""",
                RegexOption.IGNORE_CASE
            )

            var verbToken: String? = null
            var parsedAmount: Long? = null
            var rawTitlePart: String? = null

            amountFirstRegex.find(clean)?.let { match ->
                verbToken = match.groupValues[1].lowercase()
                parsedAmount = match.groupValues[2].toLongOrNull()
                rawTitlePart = match.groupValues[3]
            } ?: titleFirstRegex.find(clean)?.let { match ->
                verbToken = match.groupValues[1].lowercase()
                rawTitlePart = match.groupValues[2]
                parsedAmount = match.groupValues[3].toLongOrNull()
            } ?: suffixAddRegex.find(clean)?.let { match ->
                parsedAmount = match.groupValues[1].toLongOrNull()
                rawTitlePart = match.groupValues[2]
                verbToken = match.groupValues[3].lowercase()
            }

            val amt = parsedAmount ?: return null
            if (amt <= 0L) return null

            val actionKind = when (verbToken) {
                "milla", "mila", "received" -> "income"
                "debt", "udhar", "udhaar" -> "debt"
                else -> "expense"
            }

            val cleanItem = rawTitlePart
                ?.replace(Regex("""(?i)\b(ke naam par|add karo|kar do|please|jarvis|sir|chief)\b"""), "")
                ?.trim()
                ?.ifBlank { if (actionKind == "income") "Income" else if (actionKind == "debt") "Contact" else "Expense" }
                ?: "Expense"

            val formattedTitle = cleanItem.split(Regex("\\s+")).joinToString(" ") { word ->
                word.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString() }
            }

            val itemLower = formattedTitle.lowercase()
            val inferredCategory = when {
                actionKind == "income" -> "Income"
                actionKind == "debt" -> "Khata"
                itemLower.contains("chai") || itemLower.contains("tea") || itemLower.contains("coffee") ||
                    itemLower.contains("samosa") || itemLower.contains("snack") || itemLower.contains("food") ||
                    itemLower.contains("zomato") || itemLower.contains("swiggy") || itemLower.contains("dinner") ||
                    itemLower.contains("lunch") || itemLower.contains("breakfast") || itemLower.contains("pizza") ||
                    itemLower.contains("burger") || itemLower.contains("biryani") || itemLower.contains("momos") -> "Food"
                itemLower.contains("uber") || itemLower.contains("ola") || itemLower.contains("rapido") ||
                    itemLower.contains("auto") || itemLower.contains("cab") || itemLower.contains("metro") ||
                    itemLower.contains("bus") || itemLower.contains("train") || itemLower.contains("petrol") ||
                    itemLower.contains("fuel") || itemLower.contains("travel") -> "Travel"
                itemLower.contains("grocery") || itemLower.contains("milk") || itemLower.contains("blinkit") ||
                    itemLower.contains("zepto") || itemLower.contains("instamart") || itemLower.contains("mart") ||
                    itemLower.contains("vegetable") || itemLower.contains("fruit") -> "Grocery"
                itemLower.contains("bill") || itemLower.contains("recharge") || itemLower.contains("wifi") ||
                    itemLower.contains("electricity") || itemLower.contains("rent") || itemLower.contains("emi") ||
                    itemLower.contains("jio") || itemLower.contains("airtel") -> "Bills"
                itemLower.contains("shopping") || itemLower.contains("amazon") || itemLower.contains("flipkart") ||
                    itemLower.contains("myntra") || itemLower.contains("clothes") || itemLower.contains("shoes") -> "Shopping"
                else -> "Food"
            }

            return JarvisQuickExpenseCommand(
                title = formattedTitle,
                category = inferredCategory,
                amount = amt,
                actionKind = actionKind
            )
        }

        /**
         * Dynamic AI Financial CFO Intent & Keyword Router connected directly to live KharcheeViewModel state:
         * - Quick Action ("Add 150 Chai", "spent", "milla", "debt")
         * - SPEND / EXPENSE / AAJ KA KHARCHA ("How much did I spend today?" / "Aaj ka kharcha")
         * - BUDGET / RUNWAY / KITNA BACHA ("How much budget is left?" / "Kitna bacha")
         * - ROAST / AUDIT MY SPEND ("Roast Me / Audit my spend" with >70% vs <=70% dynamic CFO logic)
         * - KHATA / LENA DENA / DUES
         */
        fun buildDeterministicJarvisFallbackReply(
            prompt: String,
            userDisplayName: String,
            mode: String,
            language: String,
            expenses: List<ExpenseRecord>,
            settings: UserSettings,
            khata: List<KhataFriendRecord> = emptyList(),
            goals: List<SavingsGoalRecord> = emptyList()
        ): String {
            // 1. Check Quick Action first
            val quickCmd = parseQuickAddExpenseCommand(prompt)
            if (quickCmd != null) {
                return when (quickCmd.actionKind) {
                    "income" -> "Transaction logged instantly! ₹${quickCmd.amount} ${quickCmd.title} added to income."
                    "debt" -> "Transaction logged instantly! ₹${quickCmd.amount} ${quickCmd.title} recorded in Khata ledger."
                    else -> "Transaction logged instantly! ₹${quickCmd.amount} ${quickCmd.title} added to spends."
                }
            }

            val now = System.currentTimeMillis()
            val allIncomes = expenses.filter { isIncomeTransaction(it) }
            val allExpenses = expenses.filter { !isIncomeTransaction(it) }
            val todayExpenses = allExpenses.filter { isSameDay(it.txEpochMillis, now) }
            val monthExpenses = allExpenses.filter { isSameMonth(it.txEpochMillis, now) }
            val monthIncomes = allIncomes.filter { isSameMonth(it.txEpochMillis, now) }

            val todaySpent = todayExpenses.sumOf { it.amount }
            val monthSpent = monthExpenses.sumOf { it.amount }
            val monthInflow = monthIncomes.sumOf { it.amount }
            val totalIncomeAll = allIncomes.sumOf { it.amount }
            val totalExpenseAll = allExpenses.sumOf { it.amount }
            val dailyLimit = max(50L, settings.dailyLimit)
            val monthlyBudget = max(1500L, dailyLimit * 30L)
            val remainingMonthlyBudget = (monthlyBudget - monthSpent).coerceAtLeast(0L)
            val totalBalance = totalIncomeAll - totalExpenseAll

            val cal = java.util.Calendar.getInstance()
            val maxDayInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val currentDayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val daysLeftInMonth = (maxDayInMonth - currentDayOfMonth + 1).coerceAtLeast(1)
            val dailySafeLimit = (remainingMonthlyBudget / daysLeftInMonth).coerceAtLeast(0L)

            val topTodayCategoryEntry = todayExpenses
                .groupBy { it.category.ifBlank { "Food" } }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .entries
                .maxByOrNull { it.value }

            val topMonthCategoryEntry = monthExpenses
                .groupBy { it.category.ifBlank { "General" } }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .entries
                .maxByOrNull { it.value }

            val topCategoryName = topMonthCategoryEntry?.key ?: topTodayCategoryEntry?.key ?: "General"
            val topTodayOrMonthCategory = topTodayCategoryEntry?.key ?: topMonthCategoryEntry?.key ?: "None"

            val totalLeneHai = khata.filter { it.balance > 0 }.sumOf { it.balance }
            val totalDeneHai = khata.filter { it.balance < 0 }.sumOf { -it.balance }
            val pendingPersonName = khata.firstOrNull { it.balance != 0L }?.name ?: "Contact"

            val lowerPrompt = prompt.trim().lowercase()

            val isKhataIntent = Regex(
                """\b(khata|lena|dena|dues|udhar|udhaar|settlement|hisab|baki)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(lowerPrompt)

            val isRoastOrAdviceIntent = Regex(
                """\b(roast|advice|paisa kahan|sach batao|ambani|audit|discipline|health)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(lowerPrompt)

            val isBudgetRunwayIntent = Regex(
                """\b(budget|runway|kitna bacha|how much budget|left|balance|safe limit|days left|mahine ka)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(lowerPrompt)

            val isSpendIntent = Regex(
                """\b(spend|spent|expense|expenses|aaj|today|kharcha|kharch)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(lowerPrompt)

            val primaryReply = when {
                isKhataIntent -> {
                    if (totalLeneHai > 0L || totalDeneHai > 0L) {
                        "Market me ₹$totalLeneHai lene bache hain aur ₹$totalDeneHai chukane hain. $pendingPersonName se settlement pending hai."
                    } else {
                        "Khata clean hai Sir, kisi ka udhar baki nahi hai."
                    }
                }
                isRoastOrAdviceIntent -> {
                    val isOverspending = monthSpent > (monthlyBudget * 70L) / 100L
                    if (isOverspending) {
                        "Aapka budget critical stage par hai! Swiggy aur luxury offline sprees band kijiye, varna month-end par water-only diet pe jaana padega. 💀"
                    } else {
                        "Discipline bohot badhiya hai, Chief. You are in the green zone. Backed by solid budgeting!"
                    }
                }
                isBudgetRunwayIntent -> {
                    "Balance ₹$totalBalance bacha hai. Month-end tak smooth chalne ke liye, aapka daily spending limit ₹$dailySafeLimit se upar nahi jana chahiye."
                }
                isSpendIntent -> {
                    "Aaj aapne total ₹$todaySpent kharch kiye hain, Chief. $topTodayOrMonthCategory par sabse zyada spent hua hai."
                }
                else -> {
                    "Aaj aapne total ₹$todaySpent kharch kiye hain, Chief. $topTodayOrMonthCategory par sabse zyada spent hua hai."
                }
            }

            val liveTelemetryLine = "📊 Live Sync ($userDisplayName) — Current Total Balance: ₹$totalBalance | Inflow: +₹$monthInflow | Outflow: -₹$monthSpent | Top: $topCategoryName | Remaining Budget: ₹$remainingMonthlyBudget"
            return "$primaryReply\n\n$liveTelemetryLine"
        }
    }
}

