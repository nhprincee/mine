package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.UserSettings
import com.example.data.model.isIncomeTransaction
import com.example.data.model.isSameDay
import com.example.data.model.isSameMonth
import com.example.data.repository.KharcheeRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.jarvis.JarvisAssistantOverlay
import com.example.ui.screens.AddSavingsGoalBottomSheet
import com.example.ui.screens.AddTransactionBottomSheet
import com.example.ui.screens.FakeMomCallOverlay
import com.example.ui.screens.HistoryTab
import com.example.ui.screens.HomeTab
import com.example.ui.screens.InsightsTab
import com.example.ui.screens.KhataTab
import com.example.ui.screens.NightGuardInterceptOverlay
import com.example.ui.screens.NightGuardSettingsBottomSheet
import com.example.ui.screens.PreSpendConfirmationSheet
import com.example.ui.screens.ProVaultTab
import com.example.ui.screens.SmartPaymentRemindersBottomSheet
import com.example.ui.screens.SmsUpiParserBottomSheet
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ElevatedGraphite
import com.example.ui.theme.FontPreferenceManager
import com.example.ui.theme.HairlineGold
import com.example.ui.theme.HairlinePlatinum
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KharcheeTheme
import com.example.ui.theme.LocalAppFontFamily
import com.example.ui.theme.LocalHudTheme
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MutedPlatinum
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RichGraphite
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.KharcheeViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.KharcheeNotificationManager
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        FontPreferenceManager.ensureInitialized(this)
        KharcheeNotificationManager.ensureChannels(this)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        KharcheeTheme(themeId = "stark") {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        KharcheeMainShell(
            currentUserId = user.uid,
            userDisplayName = user.displayName ?: user.email?.substringBefore("@") ?: user.phoneNumber ?: "Kharchee Sovereign"
        )
    }
}

enum class KharcheeTab(val id: String, val label: String, val icon: ImageVector) {
    HOME("home", "Vault", Icons.Default.Home),
    HISTORY("history", "Passbook", Icons.Default.History),
    INSIGHTS("insights", "Analytics", Icons.Default.Analytics),
    KHATA("khata", "Khata", Icons.Default.Groups),
    PRO("pro", "Private Club", Icons.Default.WorkspacePremium)
}

@Composable
fun KharcheeMainShell(
    currentUserId: String,
    userDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: KharcheeViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                KharcheeViewModel(KharcheeRepository(db), currentUserId)
            }
        }
    )

    val settingsUiState by viewModel.settingsState.collectAsStateWithLifecycle()
    val expensesUiState by viewModel.expensesState.collectAsStateWithLifecycle()
    val khataUiState by viewModel.khataFriendsState.collectAsStateWithLifecycle()
    val goalsUiState by viewModel.savingsGoalsState.collectAsStateWithLifecycle()
    val paymentRemindersUiState by viewModel.paymentRemindersState.collectAsStateWithLifecycle()
    val nightGuardEventsUiState by viewModel.nightGuardEventsState.collectAsStateWithLifecycle()
    val pendingNightGuardSpend by viewModel.pendingNightGuardSpend.collectAsStateWithLifecycle()
    val pendingPreSpendConfirm by viewModel.pendingPreSpendConfirm.collectAsStateWithLifecycle()
    val jarvisMessagesUiState by viewModel.jarvisMessagesState.collectAsStateWithLifecycle()
    val jarvisStreamingReply by viewModel.jarvisStreamingReply.collectAsStateWithLifecycle()
    val jarvisIsGenerating by viewModel.jarvisIsGenerating.collectAsStateWithLifecycle()
    val jarvisMode by viewModel.jarvisMode.collectAsStateWithLifecycle()
    val jarvisLanguage by viewModel.jarvisLanguage.collectAsStateWithLifecycle()
    val jarvisError by viewModel.jarvisError.collectAsStateWithLifecycle()
    val latestSpokenJarvisText by viewModel.latestSpokenJarvisText.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val latestAlert by viewModel.latestAccountabilityAlert.collectAsStateWithLifecycle()
    val proactiveInsights by viewModel.proactiveInsights.collectAsStateWithLifecycle()
    val pendingProposedAction by viewModel.pendingProposedAction.collectAsStateWithLifecycle()

    val settings = (settingsUiState as? UiState.Success)?.data ?: UserSettings(userId = currentUserId)
    val expenses = (expensesUiState as? UiState.Success)?.data ?: emptyList()
    val khataFriends = (khataUiState as? UiState.Success)?.data ?: emptyList()
    val savingsGoals = (goalsUiState as? UiState.Success)?.data ?: emptyList()
    val paymentReminders = (paymentRemindersUiState as? UiState.Success)?.data ?: emptyList()
    val nightGuardEvents = (nightGuardEventsUiState as? UiState.Success)?.data ?: emptyList()
    val jarvisMessages = (jarvisMessagesUiState as? UiState.Success)?.data ?: emptyList()

    var activeTab by remember { mutableStateOf(KharcheeTab.HOME) }
    var addModalType by remember { mutableStateOf<String?>(null) } // "expense" or "income"
    var showSmsParserModal by remember { mutableStateOf(false) }
    var showSavingsGoalModal by remember { mutableStateOf(false) }
    var showSmartRemindersModal by remember { mutableStateOf(false) }
    var showNightGuardSettingsModal by remember { mutableStateOf(false) }
    var showMomCallEscape by remember { mutableStateOf(false) }
    var showJarvisAssistant by remember { mutableStateOf(false) }
    var jarvisInitialPrompt by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg) {
        val msg = toastMsg
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    LaunchedEffect(paymentReminders, settings) {
        KharcheeNotificationManager.syncRemindersAndSchedules(
            context = context,
            reminders = paymentReminders,
            settings = settings
        )
    }

    // BackHandler for secondary tabs or overlays
    BackHandler(
        enabled = pendingNightGuardSpend != null ||
            showJarvisAssistant ||
            showMomCallEscape ||
            activeTab != KharcheeTab.HOME
    ) {
        when {
            pendingNightGuardSpend != null -> viewModel.onNightGuardCancelPurchase("back_navigation")
            showJarvisAssistant -> showJarvisAssistant = false
            showMomCallEscape -> showMomCallEscape = false
            else -> activeTab = KharcheeTab.HOME
        }
    }

    val now = System.currentTimeMillis()
    val todaySpent = remember(expenses) {
        expenses.filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, now) }
            .sumOf { it.amount }
    }
    val monthSpent = remember(expenses) {
        expenses.filter { !isIncomeTransaction(it) && isSameMonth(it.txEpochMillis, now) }
            .sumOf { it.amount }
    }

    val streakDays = remember(expenses, settings.dailyLimit) {
        var streak = 0
        for (i in 0 until 30) {
            val dayTarget = now - i * 86_400_000L
            val daySum = expenses
                .filter { !isIncomeTransaction(it) && isSameDay(it.txEpochMillis, dayTarget) }
                .sumOf { it.amount }
            if (daySum <= settings.dailyLimit) {
                streak += 1
            } else {
                break
            }
        }
        streak
    }

    val disciplineScore = remember(streakDays, settings.dailyLimit, todaySpent) {
        var score = 740 + min(streakDays * 8, 96)
        if (settings.dailyLimit > 0L) {
            val ratio = todaySpent.toDouble() / settings.dailyLimit.toDouble()
            score += when {
                ratio <= 0.7 -> 35
                ratio <= 1.0 -> 15
                else -> -min(((ratio - 1.0) * 150.0).roundToInt(), 120)
            }
        }
        score.coerceIn(300, 900)
    }

    KharcheeTheme(themeId = settings.themeId) {
        val hud = LocalHudTheme.current

        Box(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 600.dp

                Scaffold(
                    containerColor = ObsidianBlack,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { showJarvisAssistant = true },
                            containerColor = RichGraphite,
                            contentColor = ChampagneGold,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Open J.A.R.V.I.S. AI Concierge",
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = "J.A.R.V.I.S.",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.2.sp,
                                    color = WarmIvory
                                )
                            },
                            modifier = Modifier
                                .shadow(18.dp, RoundedCornerShape(18.dp))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(ChampagneGold, MutedPlatinum.copy(alpha = 0.4f))
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .testTag("open_jarvis_fab")
                        )
                    },
                    topBar = {
                        KharcheeTopHeader(
                            activeTab = activeTab,
                            stealthMode = settings.stealthMode,
                            streakDays = streakDays,
                            onOpenJarvis = { showJarvisAssistant = true },
                            onToggleStealth = {
                                viewModel.updateSettings(
                                    stealthMode = !settings.stealthMode,
                                    toastText = if (!settings.stealthMode) "🛡️ Stealth Zero-Balance Shield ON" else "👁️ Stealth Shield OFF"
                                )
                            },
                            onSignOut = {
                                signOut(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = {},
                                    scope = scope
                                )
                            }
                        )
                    },
                    bottomBar = {
                        if (!isWideScreen) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RichGraphite)
                            ) {
                                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)
                                NavigationBar(
                                    containerColor = RichGraphite,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                                ) {
                                    KharcheeTab.entries.forEach { tab ->
                                        val selected = activeTab == tab
                                        val activeColor by animateColorAsState(
                                            targetValue = if (selected) ChampagneGold else MutedIvory,
                                            animationSpec = tween(220),
                                            label = "navTabColor"
                                        )
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { activeTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.label,
                                                    tint = activeColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.label,
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontSize = 10.sp,
                                                    letterSpacing = 0.3.sp,
                                                    color = if (selected) WarmIvory else MutedIvory,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = ChampagneGold,
                                                selectedTextColor = WarmIvory,
                                                indicatorColor = ChampagneGold.copy(alpha = 0.14f),
                                                unselectedIconColor = MutedIvory,
                                                unselectedTextColor = MutedIvory
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${tab.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isWideScreen) {
                            NavigationRail(
                                containerColor = RichGraphite,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .border(0.6.dp, HairlinePlatinum)
                            ) {
                                KharcheeTab.entries.forEach { tab ->
                                    val selected = activeTab == tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { activeTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = ChampagneGold,
                                            selectedTextColor = WarmIvory,
                                            indicatorColor = ChampagneGold.copy(alpha = 0.15f),
                                            unselectedIconColor = MutedIvory,
                                            unselectedTextColor = MutedIvory
                                        ),
                                        modifier = Modifier.testTag("nav_rail_${tab.id}")
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            if (expensesUiState is UiState.Loading && settingsUiState is UiState.Loading) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = ChampagneGold)
                                }
                            } else {
                                when (activeTab) {
                                    KharcheeTab.HOME -> HomeTab(
                                        settings = settings,
                                        expenses = expenses,
                                        savingsGoals = savingsGoals,
                                        paymentReminders = paymentReminders,
                                        todaySpent = todaySpent,
                                        monthSpent = monthSpent,
                                        disciplineScore = disciplineScore,
                                        streakDays = streakDays,
                                        latestAlert = latestAlert,
                                        proactiveInsights = proactiveInsights,
                                        onOpenJarvisWithPrompt = { prompt ->
                                            jarvisInitialPrompt = prompt
                                            showJarvisAssistant = true
                                        },
                                        onDismissAlert = viewModel::dismissAccountabilityAlert,
                                        onQuickAdd = { title, cat, amt ->
                                            viewModel.requestAddTransaction(
                                                title = title,
                                                category = cat,
                                                amount = amt,
                                                paymentMode = "UPI",
                                                type = "expense",
                                                note = "1-Tap Quick Add"
                                            )
                                        },
                                        onOpenAddExpense = { addModalType = "expense" },
                                        onOpenAddIncome = { addModalType = "income" },
                                        onOpenSmsParser = { showSmsParserModal = true },
                                        onOpenUpiGuardSimulator = { showSmsParserModal = true },
                                        onOpenAddGoal = { showSavingsGoalModal = true },
                                        onAddGoalProgress = viewModel::addSavingsProgress,
                                        onDeleteExpense = { viewModel.deleteExpense(it.id, it.title) },
                                        onToggleLockdown = {
                                            val next = !settings.lockdownActive
                                            viewModel.updateSettings(
                                                lockdownActive = next,
                                                toastText = if (next) "🔒 Lockdown Shield Activated!" else "🔓 Lockdown Shield Lifted"
                                            )
                                        },
                                        onUpdateDailyLimit = { newLimit ->
                                            viewModel.updateSettings(
                                                dailyLimit = newLimit,
                                                toastText = "✅ Daily spending limit set to ₹$newLimit"
                                            )
                                        },
                                        onTriggerMomCall = { showMomCallEscape = true },
                                        onOpenNightGuardSettings = { showNightGuardSettingsModal = true },
                                        onTestNightGuard = viewModel::triggerNightGuardSimulation,
                                        onOpenSmartReminders = { showSmartRemindersModal = true }
                                    )

                                    KharcheeTab.HISTORY -> HistoryTab(
                                        expenses = expenses,
                                        stealthMode = settings.stealthMode,
                                        onDeleteExpense = { viewModel.deleteExpense(it.id, it.title) }
                                    )

                                    KharcheeTab.INSIGHTS -> InsightsTab(
                                        expenses = expenses,
                                        dailyLimit = settings.dailyLimit,
                                        streakDays = streakDays,
                                        disciplineScore = disciplineScore,
                                        stealthMode = settings.stealthMode,
                                        settings = settings,
                                        nightGuardEvents = nightGuardEvents
                                    )

                                    KharcheeTab.KHATA -> KhataTab(
                                        friends = khataFriends,
                                        myUpiId = settings.myUpiId,
                                        stealthMode = settings.stealthMode,
                                        onUpdateMyUpiId = { newUpi ->
                                            viewModel.updateSettings(
                                                myUpiId = newUpi,
                                                toastText = "✅ Saved receiving UPI ID: $newUpi"
                                            )
                                        },
                                        onAddFriend = viewModel::addKhataFriend,
                                        onAddEntry = viewModel::recordKhataEntry,
                                        onSettleFriend = viewModel::settleFriend,
                                        onSplitBill = viewModel::splitBillEqually,
                                        onShowToast = viewModel::showToast
                                    )

                                    KharcheeTab.PRO -> ProVaultTab(
                                        settings = settings,
                                        userDisplayName = userDisplayName,
                                        disciplineScore = disciplineScore,
                                        streakDays = streakDays,
                                        expenses = expenses,
                                        onSelectTheme = { newTheme ->
                                            viewModel.updateSettings(
                                                themeId = newTheme,
                                                toastText = "◆ Activated ${newTheme.uppercase()} Sovereign Theme"
                                            )
                                        },
                                        onToggleAutoLockdown = { enabled ->
                                            viewModel.updateSettings(
                                                autoLockdownAt90 = enabled,
                                                toastText = if (enabled) "🛡️ Auto-Lockdown at 90% Enabled" else "Auto-Lockdown Disabled"
                                            )
                                        },
                                        onSelectUpiTone = { tone ->
                                            viewModel.updateSettings(
                                                upiGuardTone = tone,
                                                toastText = "🎙️ UPI Guard Tone: ${tone.uppercase()}"
                                            )
                                        },
                                        onSelectFont = { fontName ->
                                            viewModel.showToast("✒️ Font set to $fontName")
                                        },
                                        onUpgradeProPlan = { tier, days ->
                                            viewModel.upgradeProPlan(tier, days)
                                        },
                                        onProActivated = { planSummary ->
                                            viewModel.showToast("👑 Activated $planSummary!")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (addModalType != null) {
                AddTransactionBottomSheet(
                    initialType = addModalType!!,
                    onDismiss = { addModalType = null },
                    onSubmit = { title, cat, amt, mode, type, note ->
                        viewModel.requestAddTransaction(title, cat, amt, mode, type, note)
                    }
                )
            }

            if (showSmsParserModal) {
                SmsUpiParserBottomSheet(
                    onDismiss = { showSmsParserModal = false },
                    onLogParsedTransaction = { title, cat, amt, mode, note ->
                        viewModel.requestAddTransaction(
                            title = title,
                            category = cat,
                            amount = amt,
                            paymentMode = mode,
                            type = "expense",
                            note = note,
                            triggerUpiGuard = true
                        )
                    }
                )
            }

            if (showSavingsGoalModal) {
                AddSavingsGoalBottomSheet(
                    onDismiss = { showSavingsGoalModal = false },
                    onCreateGoal = viewModel::createSavingsGoal
                )
            }

            if (showSmartRemindersModal) {
                SmartPaymentRemindersBottomSheet(
                    settings = settings,
                    reminders = paymentReminders,
                    onDismiss = { showSmartRemindersModal = false },
                    onCreateReminder = viewModel::createPaymentReminder,
                    onMarkReminderPaid = viewModel::markPaymentReminderPaid,
                    onDeleteReminder = viewModel::deletePaymentReminder,
                    onTogglePreSpendConfirm = { enabled ->
                        viewModel.updateSettings(
                            enablePreSpendConfirm = enabled,
                            toastText = if (enabled) "🧘 Pre-Spend Mindfulness Check Enabled" else "Pre-Spend Check Disabled"
                        )
                    },
                    onUpdateNotificationPrefs = { paymentRem, nightGuardAlert, morningRev ->
                        viewModel.updateSettings(
                            notifyPaymentReminders = paymentRem,
                            notifyNightGuardAlert = nightGuardAlert,
                            notifyMorningReview = morningRev,
                            toastText = "🔔 Notification preferences saved"
                        )
                    }
                )
            }

            if (showNightGuardSettingsModal) {
                NightGuardSettingsBottomSheet(
                    settings = settings,
                    onDismiss = { showNightGuardSettingsModal = false },
                    onSaveNightGuardConfig = { enabled, startH, startM, endH, endM ->
                        viewModel.updateSettings(
                            nightGuardEnabled = enabled,
                            nightGuardStartHour = startH,
                            nightGuardStartMinute = startM,
                            nightGuardEndHour = endH,
                            nightGuardEndMinute = endM,
                            toastText = if (enabled) "🌙 Night Guard Schedule Updated" else "Night Guard Paused"
                        )
                    },
                    onLaunchNightGuardPreview = {
                        showNightGuardSettingsModal = false
                        viewModel.triggerNightGuardSimulation()
                    }
                )
            }

            val currentPreSpend = pendingPreSpendConfirm
            if (currentPreSpend != null) {
                PreSpendConfirmationSheet(
                    pendingRequest = currentPreSpend,
                    settings = settings,
                    todaySpent = todaySpent,
                    monthSpent = monthSpent,
                    savingsGoals = savingsGoals,
                    onDismissCancel = viewModel::onPreSpendCancel,
                    onSaveForTomorrow = viewModel::onPreSpendSaveForTomorrow,
                    onConfirmRecord = viewModel::onPreSpendConfirmProceed
                )
            }

            val currentNightGuardSpend = pendingNightGuardSpend
            if (currentNightGuardSpend != null) {
                NightGuardInterceptOverlay(
                    userId = currentUserId,
                    pendingRequest = currentNightGuardSpend,
                    settings = settings,
                    todaySpent = todaySpent,
                    savingsGoals = savingsGoals,
                    onTriggerCooldown = viewModel::triggerNightGuardCooldown,
                    onCancelPurchase = viewModel::onNightGuardCancelPurchase,
                    onSaveForTomorrow = viewModel::onNightGuardSaveForTomorrow,
                    onContinueToRecordExpense = viewModel::onNightGuardContinueProceed,
                    onEssentialBypass = viewModel::onNightGuardEssentialBypass
                )
            }

            if (showMomCallEscape) {
                FakeMomCallOverlay(
                    onEndCall = {
                        showMomCallEscape = false
                        viewModel.showToast("📞 Saved by Mummy ❤️ · Escaped expensive group split!")
                    }
                )
            }

            if (showJarvisAssistant) {
                JarvisAssistantOverlay(
                    userDisplayName = userDisplayName,
                    todaySpent = todaySpent,
                    monthSpent = monthSpent,
                    dailyLimit = settings.dailyLimit,
                    disciplineScore = disciplineScore,
                    messages = jarvisMessages,
                    streamingReply = jarvisStreamingReply,
                    isGenerating = jarvisIsGenerating,
                    activeMode = jarvisMode,
                    activeLanguage = jarvisLanguage,
                    errorState = jarvisError,
                    latestSpokenText = latestSpokenJarvisText,
                    onConsumeLatestSpokenText = viewModel::consumeLatestSpokenJarvisText,
                    onSelectMode = viewModel::setJarvisMode,
                    onSelectLanguage = viewModel::setJarvisLanguage,
                    proactiveInsights = proactiveInsights,
                    pendingProposedAction = pendingProposedAction,
                    onConfirmProposedAction = { action ->
                        viewModel.executeConfirmedJarvisAction(
                            action = action,
                            onNavigateToTab = { targetTabId ->
                                val matched = KharcheeTab.entries.find { it.id == targetTabId }
                                if (matched != null) {
                                    activeTab = matched
                                    showJarvisAssistant = false
                                }
                            }
                        )
                    },
                    onDismissProposedAction = viewModel::dismissProposedAction,
                    initialPrompt = jarvisInitialPrompt,
                    onSendMessage = { prompt ->
                        jarvisInitialPrompt = null
                        viewModel.sendJarvisMessage(prompt, userDisplayName)
                    },
                    onClearHistory = viewModel::clearJarvisConversation,
                    onDismissError = viewModel::dismissJarvisError,
                    onClose = {
                        showJarvisAssistant = false
                        jarvisInitialPrompt = null
                    }
                )
            }
        }
    }
}

@Composable
private fun KharcheeTopHeader(
    activeTab: KharcheeTab,
    stealthMode: Boolean,
    streakDays: Int,
    onOpenJarvis: () -> Unit,
    onToggleStealth: () -> Unit,
    onSignOut: () -> Unit
) {
    val hud = LocalHudTheme.current
    val headingFont = LocalAppFontFamily.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(ElevatedGraphite, RichGraphite)
                            )
                        )
                        .border(0.8.dp, HairlineGold, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        fontFamily = headingFont,
                        color = ChampagneGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "KHARCHEE",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = WarmIvory,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = " · ${activeTab.label.uppercase()}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = hud.accent
                        )
                    }
                    Text(
                        text = if (stealthMode) "STEALTH VAULT · BALANCES MASKED" else "SOVEREIGN PRIVATE BANKING",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp,
                        color = MutedIvory
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = RichGraphite,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .border(0.8.dp, HairlineGold, RoundedCornerShape(50))
                        .clickable { onOpenJarvis() }
                        .testTag("open_jarvis_header_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Launch J.A.R.V.I.S.",
                            tint = ChampagneGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "JARVIS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = WarmIvory
                        )
                    }
                }

                Surface(
                    color = RichGraphite,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(0.8.dp, HairlinePlatinum, RoundedCornerShape(50))
                ) {
                    Text(
                        text = "◆ ${streakDays}D",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        color = ChampagneGold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = onToggleStealth,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_stealth_mode_button")
                ) {
                    Icon(
                        imageVector = if (stealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Stealth Mode",
                        tint = if (stealthMode) ChampagneGold else MutedPlatinum,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = MutedPlatinum,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)
    }
}
