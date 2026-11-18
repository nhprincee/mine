package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AdvancedKhataStore
import com.example.data.model.DostiCreditScoreInfo
import com.example.data.model.DostiReliabilityTier
import com.example.data.model.GroupSplitComputationResult
import com.example.data.model.KHATA_CATEGORIES
import com.example.data.model.KhataAuditAction
import com.example.data.model.KhataContactComputedState
import com.example.data.model.KhataContactFilter
import com.example.data.model.KhataContactSort
import com.example.data.model.KhataDirection
import com.example.data.model.KhataFriendRecord
import com.example.data.model.KhataHistoryAuditItem
import com.example.data.model.KhataHistoryFilter
import com.example.data.model.KhataLedgerEntry
import com.example.data.model.KhataRecordType
import com.example.data.model.KhataSummaryMetrics
import com.example.data.model.UnifiedActivityKind
import com.example.data.model.UnifiedRecentActivityItem
import com.example.data.repository.KharcheeRepository
import com.example.ui.components.KharcheeCompactHeaderBranding
import kotlinx.coroutines.launch
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KhataBrandPurple
import com.example.ui.theme.KhataBurgundyBorder
import com.example.ui.theme.KhataBurgundySurface
import com.example.ui.theme.KhataCardSurface
import com.example.ui.theme.KhataDeepBurgundy
import com.example.ui.theme.KhataDenaDarkGlassSurface
import com.example.ui.theme.KhataDenaGlassOverlay
import com.example.ui.theme.KhataDenaPay
import com.example.ui.theme.KhataElevatedCardSurface
import com.example.ui.theme.KhataLenaDarkGlassSurface
import com.example.ui.theme.KhataLenaGlassOverlay
import com.example.ui.theme.KhataLenaReceive
import com.example.ui.theme.KhataMutedText
import com.example.ui.theme.KhataNetPositionHighlight
import com.example.ui.theme.KhataPlatinumHighlight
import com.example.ui.theme.KhataPrimaryBackground
import com.example.ui.theme.KhataPrimaryText
import com.example.ui.theme.KhataSecondaryBackground
import com.example.ui.theme.KhataSecondaryText
import com.example.ui.theme.KhataSettledRoseSlate
import com.example.ui.theme.LocalAppFontFamily
import com.example.util.KhataSplitCardGenerator
import com.example.util.PaymentUtil
import com.example.util.VasooliReminderTone
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

// Deep Burgundy × Obsidian × Platinum × Muted Rose Palette (Phase 1)
private val KhataNeonEmerald = KhataLenaReceive // #55D6A5
private val KhataEmeraldGlassBg = KhataLenaGlassOverlay // #1455D6A5
private val KhataCoralCrimson = KhataDenaPay // #E78AA5
private val KhataCrimsonGlassBg = KhataDenaGlassOverlay // #14E78AA5
private val KhataSettledSlate = KhataSettledRoseSlate // #6B5B66

/**
 * Production-quality KHATA (Next-Gen Social Fintech Ledger & Digital Bahi-Khata) Screen.
 * Styled with the Deep Burgundy × Obsidian × Platinum × Muted Rose visual identity.
 */
@Composable
fun AdvancedKhataScreen(
    userId: String,
    legacyFriends: List<KhataFriendRecord> = emptyList(),
    stealthMode: Boolean = false,
    userDisplayName: String = "Kharchee Member",
    userUpiId: String = "",
    onShowToast: (String) -> Unit = {},
    onOpenVaultTools: (() -> Unit)? = null,
    onLogPersonalExpenseShare: ((title: String, category: String, amount: Long, note: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val headingFont = LocalAppFontFamily.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember(context) { KharcheeRepository(context.applicationContext) }
    val storeVersion by AdvancedKhataStore.versionFlow.collectAsState()
    val cloudSyncStatus by AdvancedKhataStore.cloudSyncStatusFlow.collectAsState()
    val pendingUpiSession by PaymentUtil.activePendingSessionFlow.collectAsState()
    var localRefreshTick by remember { mutableLongStateOf(0L) }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner, context, userId) {
        AdvancedKhataStore.onCloudWriteHook = { uid, updatedContact, updatedEntry ->
            coroutineScope.launch {
                if (updatedContact != null) {
                    repository.upsertKhataContactToCloud(updatedContact)
                }
                if (updatedEntry != null) {
                    repository.upsertKhataEntryToCloud(uid, updatedEntry)
                }
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val resumedSession = PaymentUtil.onAppResumedAfterUpiIntent(
                    context = context,
                    onShowToast = onShowToast
                )
                if (resumedSession != null) {
                    localRefreshTick++
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(userId, legacyFriends) {
        AdvancedKhataStore.migrateDefaultSlotToUserIfNeeded(context, userId)
        AdvancedKhataStore.syncLegacyFirestoreFriendsIfNeeded(context, userId, legacyFriends)
        localRefreshTick++
        repository.syncLifetimeKhataMemoryWithCloud(context.applicationContext)
        localRefreshTick++
    }

    val allContactStates = remember(userId, storeVersion, localRefreshTick) {
        AdvancedKhataStore.getAllContactStates(context, userId)
    }
    val summaryMetrics = remember(allContactStates) {
        AdvancedKhataStore.computeSummaryMetrics(allContactStates)
    }
    val lifetimeIntelligence = remember(allContactStates) {
        AdvancedKhataStore.computeLifetimeKhataIntelligence(allContactStates)
    }
    val auditHistory = remember(userId, storeVersion, localRefreshTick) {
        AdvancedKhataStore.loadAuditHistory(context, userId)
    }
    val contactStateMap = remember(allContactStates) {
        allContactStates.associateBy { it.contact.id }
    }

    // Sub-navigation: "overview" (Contacts & Summary), "history" (Lifetime Timeline), "intelligence" (Lifetime Insights), "smart_tools" (Voice/Settle/Calendar/Backup)
    var activeSubTab by remember { mutableStateOf("overview") }
    var selectedContactId by remember { mutableStateOf<String?>(null) }

    // Search, Filter & Sort state for Contacts
    var contactSearchQuery by remember { mutableStateOf("") }
    var selectedContactFilter by remember { mutableStateOf(KhataContactFilter.ALL) }
    var selectedContactSort by remember { mutableStateOf(KhataContactSort.LATEST_ACTIVITY) }

    // Search & Filter state for Khata History
    var historySearchQuery by remember { mutableStateOf("") }
    var selectedHistoryFilter by remember { mutableStateOf(KhataHistoryFilter.ALL) }
    var customRangeDaysBack by remember { mutableStateOf("30") }
    var selectedHistoryYear by remember { mutableStateOf<Int?>(null) }
    var selectedHistoryMonthKey by remember { mutableStateOf<String?>(null) }

    // Modals state
    var activeAddEntryDirection by remember { mutableStateOf<KhataDirection?>(null) }
    var prefilledContactNameForAdd by remember { mutableStateOf("") }
    var prefilledContactPhoneForAdd by remember { mutableStateOf("") }

    var paymentModalContactState by remember { mutableStateOf<KhataContactComputedState?>(null) }
    var pendingUpiVerifyContactState by remember { mutableStateOf<KhataContactComputedState?>(null) }
    var vasooliModalContactState by remember { mutableStateOf<KhataContactComputedState?>(null) }
    var vasooliSpecificEntry by remember { mutableStateOf<KhataLedgerEntry?>(null) }
    var showGroupBillSplitterModal by remember { mutableStateOf(false) }
    var promiseDateModalContactState by remember { mutableStateOf<KhataContactComputedState?>(null) }
    var editingEntry by remember { mutableStateOf<KhataLedgerEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<KhataLedgerEntry?>(null) }
    var attachingProofEntry by remember { mutableStateOf<KhataLedgerEntry?>(null) }

    val currentPendingUpiContact = pendingUpiVerifyContactState
    if (currentPendingUpiContact != null) {
        val initialContactVpa = remember(currentPendingUpiContact.contact.id, currentPendingUpiContact.contact.phone) {
            PaymentUtil.resolvePayeeVpa(currentPendingUpiContact.contact.phone, currentPendingUpiContact.contact.name)
                .ifBlank { PaymentUtil.getSavedVerifiedVpa(context, currentPendingUpiContact.contact.id) }
        }
        com.example.util.VerifyOrEnterRecipientUpiDialog(
            recipientName = currentPendingUpiContact.contact.name,
            amountInr = currentPendingUpiContact.outstandingAmount.toString(),
            initialVpa = initialContactVpa,
            onConfirmVerifiedUpiPay = { enteredVpa ->
                PaymentUtil.saveVerifiedVpa(context, currentPendingUpiContact.contact.id, enteredVpa)
                pendingUpiVerifyContactState = null
                PaymentUtil.launchUpiPaymentIntent(
                    context = context,
                    payeeVpa = enteredVpa,
                    payeeName = currentPendingUpiContact.contact.name,
                    amount = currentPendingUpiContact.outstandingAmount.toString(),
                    txnNote = "Settle Khata Due - Kharchee",
                    contactId = currentPendingUpiContact.contact.id,
                    onStatusMessage = onShowToast
                )
            },
            onOpenGenericUpiScanner = {
                pendingUpiVerifyContactState = null
                PaymentUtil.launchGenericUpiAppChooser(
                    context = context,
                    payeeName = currentPendingUpiContact.contact.name,
                    amount = currentPendingUpiContact.outstandingAmount.toString(),
                    txnNote = "Settle Khata Due - Kharchee",
                    contactId = currentPendingUpiContact.contact.id,
                    onStatusMessage = onShowToast
                )
            },
            onMarkPaidManually = {
                val target = currentPendingUpiContact
                pendingUpiVerifyContactState = null
                paymentModalContactState = target
            },
            onDismiss = { pendingUpiVerifyContactState = null }
        )
    }

    val selectedContactState = remember(selectedContactId, allContactStates) {
        selectedContactId?.let { id -> allContactStates.find { it.contact.id == id } }
    }

    BackHandler(enabled = selectedContactId != null || activeSubTab != "overview") {
        if (selectedContactId != null) {
            selectedContactId = null
        } else {
            activeSubTab = "overview"
        }
    }

    if (selectedContactState != null) {
        // --- SECTION 5. INDIVIDUAL PERSON LEDGER + LIFETIME KHATA PROFILE ---
        KhataIndividualPersonLedgerView(
            state = selectedContactState,
            stealthMode = stealthMode,
            userDisplayName = userDisplayName,
            userUpiId = userUpiId,
            onBack = { selectedContactId = null },
            onAddLenaHai = {
                prefilledContactNameForAdd = selectedContactState.contact.name
                prefilledContactPhoneForAdd = selectedContactState.contact.phone
                activeAddEntryDirection = KhataDirection.LENA_HAI
            },
            onAddDenaHai = {
                prefilledContactNameForAdd = selectedContactState.contact.name
                prefilledContactPhoneForAdd = selectedContactState.contact.phone
                activeAddEntryDirection = KhataDirection.DENA_HAI
            },
            onOpenRecordPayment = {
                paymentModalContactState = selectedContactState
            },
            onOpenVasooliSheet = { specificEntry ->
                vasooliSpecificEntry = specificEntry
                vasooliModalContactState = selectedContactState
            },
            onOpenSetPromiseDate = {
                promiseDateModalContactState = selectedContactState
            },
            onSettleInFull = {
                val res = AdvancedKhataStore.settleContactInFull(
                    context = context,
                    userId = userId,
                    contactId = selectedContactState.contact.id
                )
                if (res.isSuccess) {
                    localRefreshTick++
                    onShowToast("✅ ${selectedContactState.contact.name}'s Khata marked as Settled")
                } else {
                    onShowToast(res.exceptionOrNull()?.message ?: "Could not settle")
                }
            },
            onEditEntry = { entry -> editingEntry = entry },
            onRequestDeleteEntry = { entry -> deletingEntry = entry },
            onAttachProof = { entry -> attachingProofEntry = entry },
            onToggleReminder = { enabled ->
                AdvancedKhataStore.toggleContactReminder(
                    context = context,
                    userId = userId,
                    contactId = selectedContactState.contact.id,
                    enabled = enabled
                )
                localRefreshTick++
                onShowToast(if (enabled) "🔔 Due reminders enabled for ${selectedContactState.contact.name}" else "🔕 Reminders muted")
            },
            onShowToast = onShowToast
        )
    } else {
        // --- SECTION 2, 3, 4 & 8: KHATA OVERVIEW, TIMELINE, INTELLIGENCE & SMART TOOLS ---
        val filteredContacts = remember(allContactStates, contactSearchQuery, selectedContactFilter, selectedContactSort) {
            AdvancedKhataStore.filterAndSortContacts(
                states = allContactStates,
                searchQuery = contactSearchQuery,
                filter = selectedContactFilter,
                sort = selectedContactSort
            )
        }

        val filteredHistory = remember(
            auditHistory,
            contactStateMap,
            historySearchQuery,
            selectedHistoryFilter,
            customRangeDaysBack,
            selectedHistoryYear,
            selectedHistoryMonthKey
        ) {
            val now = System.currentTimeMillis()
            val days = (customRangeDaysBack.toLongOrNull() ?: 30L).coerceAtLeast(1L)
            val startMillis = if (selectedHistoryFilter == KhataHistoryFilter.CUSTOM_DATE_RANGE) {
                now - (days * 86_400_000L)
            } else null
            AdvancedKhataStore.filterHistoryItems(
                items = auditHistory,
                contactStatesById = contactStateMap,
                searchQuery = historySearchQuery,
                filter = selectedHistoryFilter,
                startDateMillis = startMillis,
                endDateMillis = now,
                selectedYear = selectedHistoryYear,
                selectedMonthOneBased = selectedHistoryMonthKey?.substringAfter("-")?.toIntOrNull()
            )
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            KhataPrimaryBackground,
                            KhataSecondaryBackground,
                            KhataPrimaryBackground
                        )
                    )
                )
                .testTag("khata_tab_list"),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 3: Khata Hero Header & Aligned Sub-Navigation
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Digital Bahi-Khata",
                                fontFamily = headingFont,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KhataPrimaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Your Lifetime Money Memory",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = KhataSecondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            color = KhataCardSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, KhataBurgundyBorder.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(KhataLenaReceive)
                                )
                                Text(
                                    text = cloudSyncStatus,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KhataPlatinumHighlight,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Aligned Sub-Navigation Strip (Contacts, History, Intelligence, Smart Tools)
                    Surface(
                        color = KhataSecondaryBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.8.dp, KhataBurgundyBorder.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val subTabs = listOf(
                                Triple("overview", "Contacts (${allContactStates.size})", "khata_subtab_contacts"),
                                Triple("history", "History", "khata_subtab_history"),
                                Triple("intelligence", "✨ Insights", "khata_subtab_intelligence"),
                                Triple("smart_tools", "🎙️ Smart Tools", "khata_subtab_smart_tools")
                            )
                            subTabs.forEach { (tabId, label, tag) ->
                                val isSelected = activeSubTab == tabId
                                Surface(
                                    color = if (isSelected) KhataBurgundySurface else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) {
                                        BorderStroke(1.dp, KhataBrandPurple.copy(alpha = 0.85f))
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { activeSubTab = tabId }
                                        .testTag(tag)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        if (tabId == "overview") {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = if (isSelected) KhataBrandPurple else KhataSecondaryText,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        } else if (tabId == "history") {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = if (isSelected) KhataBrandPurple else KhataSecondaryText,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = label,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSelected) KhataPrimaryText else KhataSecondaryText,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Post-Payment UPI Sync Confirmation Badge (shown on return from payment intent)
            if (pendingUpiSession?.hasReturnedToApp == true) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(KhataLenaDarkGlassSurface)
                            .border(1.dp, KhataLenaReceive.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("khata_post_payment_sync_badge"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ ${PaymentUtil.POST_PAYMENT_SYNC_MESSAGE}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KhataLenaReceive,
                            lineHeight = 15.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KhataSecondaryText,
                            modifier = Modifier
                                .clickable { PaymentUtil.clearPendingUpiSession(context) }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // --- SECTION 2. KHATA SUMMARY (LENA HAI & DENA HAI CARDS + NET BALANCE POSITION) ---
            item {
                KhataSummaryHeroSection(
                    metrics = summaryMetrics,
                    stealthMode = stealthMode
                )
            }

            // --- SECTION 3. PRIMARY ACTIONS (+ ADD LENA HAI, + ADD DENA HAI & COMPACT SPLIT GROUP BILL) ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // + Add Lena Hai (You Get)
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, KhataLenaReceive.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            KhataLenaDarkGlassSurface,
                                            KhataCardSurface
                                        )
                                    )
                                )
                                .clickable {
                                    prefilledContactNameForAdd = ""
                                    prefilledContactPhoneForAdd = ""
                                    activeAddEntryDirection = KhataDirection.LENA_HAI
                                }
                                .testTag("khata_add_lena_hai_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(KhataLenaReceive.copy(alpha = 0.16f))
                                        .border(0.8.dp, KhataLenaReceive.copy(alpha = 0.65f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallReceived,
                                        contentDescription = "Add Lena Hai",
                                        tint = KhataLenaReceive,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "+ Add Lena Hai",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = KhataPrimaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "You gave money",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KhataLenaReceive,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // + Add Dena Hai (You Give)
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, KhataDenaPay.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            KhataDeepBurgundy,
                                            KhataCardSurface
                                        )
                                    )
                                )
                                .clickable {
                                    prefilledContactNameForAdd = ""
                                    prefilledContactPhoneForAdd = ""
                                    activeAddEntryDirection = KhataDirection.DENA_HAI
                                }
                                .testTag("khata_add_dena_hai_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(KhataDenaPay.copy(alpha = 0.16f))
                                        .border(0.8.dp, KhataDenaPay.copy(alpha = 0.65f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallMade,
                                        contentDescription = "Add Dena Hai",
                                        tint = KhataDenaPay,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "+ Add Dena Hai",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = KhataPrimaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "You took money",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KhataDenaPay,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Compact Premium Split Group Bill Card — Pure White Surface (30% White Identity)
                    Surface(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E6DD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showGroupBillSplitterModal = true }
                            .testTag("khata_split_group_bill_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFFFF))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF080B08))
                                        .border(1.dp, Color(0xFFB7F000), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = "Split Group Bill",
                                        tint = Color(0xFFB7F000),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "Split Group Bill",
                                        fontFamily = headingFont,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF080B08),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Share. Settle. Enjoy Together. • Equal or custom",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475247),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                color = Color(0xFFB7F000),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color(0xFF080B08).copy(alpha = 0.15f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Split Bill",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF080B08),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFF080B08),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (activeSubTab == "overview") {
                val khataHallOfShame = HomeDailyCockpitEngine.computeHallOfShameDefaulters(allContactStates)
                if (khataHallOfShame.isNotEmpty()) {
                    item {
                        HallOfShameOverdueCarousel(
                            defaulters = khataHallOfShame,
                            stealthMode = stealthMode,
                            onTriggerWhatsAppVasooli = { defaulter ->
                                vasooliSpecificEntry = null
                                vasooliModalContactState = defaulter.contactState
                            },
                            onOpenKhataLedger = { selectedContactFilter = KhataContactFilter.OVERDUE }
                        )
                    }
                }

                // --- SECTION 4. CONTACT-WISE KHATA (SEARCH + FILTER + SORT + LIST) ---
                item {
                    KhataContactsSearchFilterAndSortBar(
                        searchQuery = contactSearchQuery,
                        onSearchQueryChange = { contactSearchQuery = it },
                        selectedFilter = selectedContactFilter,
                        onSelectFilter = { selectedContactFilter = it },
                        selectedSort = selectedContactSort,
                        onSelectSort = { selectedContactSort = it }
                    )
                }

                if (filteredContacts.isEmpty()) {
                    item {
                        KhataEmptyStateCard(
                            title = if (allContactStates.isEmpty()) "Khata is empty. Add a person to track Lena/Dena." else "No matching contacts found",
                            subtitle = if (allContactStates.isEmpty()) {
                                "Tap '+ Add Lena Hai' when someone owes you money, or '+ Add Dena Hai' when you owe someone."
                            } else {
                                "Try clearing your search query or switching the active filter."
                            },
                            onAddLenaHai = {
                                prefilledContactNameForAdd = ""
                                prefilledContactPhoneForAdd = ""
                                activeAddEntryDirection = KhataDirection.LENA_HAI
                            }
                        )
                    }
                } else {
                    items(
                        items = filteredContacts,
                        key = { it.contact.id },
                        contentType = { "khata_contact_row" }
                    ) { contactState ->
                        KhataContactCardItem(
                            state = contactState,
                            stealthMode = stealthMode,
                            onClick = { selectedContactId = contactState.contact.id },
                            onQuickPayment = { paymentModalContactState = contactState },
                            onInstantUpiPay = {
                                val resolvedVpa = PaymentUtil.resolvePayeeVpa(
                                    rawPhoneOrVpa = contactState.contact.phone,
                                    payeeName = contactState.contact.name
                                ).ifBlank { PaymentUtil.getSavedVerifiedVpa(context, contactState.contact.id) }
                                if (resolvedVpa.isNotBlank()) {
                                    PaymentUtil.launchUpiPaymentIntent(
                                        context = context,
                                        payeeVpa = resolvedVpa,
                                        payeeName = contactState.contact.name,
                                        amount = contactState.outstandingAmount.toString(),
                                        txnNote = "Settle Khata Due - Kharchee",
                                        contactId = contactState.contact.id,
                                        onStatusMessage = onShowToast
                                    )
                                } else {
                                    pendingUpiVerifyContactState = contactState
                                }
                            },
                            onShareWhatsAppSplitCard = {
                                vasooliSpecificEntry = null
                                vasooliModalContactState = contactState
                            }
                        )
                    }
                }

                // Khata Intelligence Compact Preview Card (matches Deep Burgundy reference design)
                item {
                    KhataIntelligenceCompactPreviewCard(
                        intelligence = lifetimeIntelligence,
                        allContactStates = allContactStates,
                        stealthMode = stealthMode,
                        onOpenFullIntelligence = { activeSubTab = "intelligence" }
                    )
                }
            } else if (activeSubTab == "intelligence") {
                item {
                    LifetimeKhataIntelligenceDashboard(
                        intelligence = lifetimeIntelligence,
                        stealthMode = stealthMode
                    )
                }
            } else if (activeSubTab == "smart_tools") {
                item {
                    SmartKhataToolsSection(
                        userId = userId,
                        allContactStates = allContactStates,
                        stealthMode = stealthMode,
                        userUpiId = userUpiId,
                        onRefreshTriggered = { localRefreshTick++ },
                        onOpenContact = { cid -> selectedContactId = cid },
                        onShowToast = onShowToast
                    )
                }
            } else {
                // --- SECTION 8. COMPLETE LIFETIME KHATA TIMELINE ---
                item {
                    KhataHistorySearchAndFilterBar(
                        searchQuery = historySearchQuery,
                        onSearchQueryChange = { historySearchQuery = it },
                        selectedFilter = selectedHistoryFilter,
                        onSelectFilter = { selectedHistoryFilter = it },
                        customRangeDays = customRangeDaysBack,
                        onCustomRangeDaysChange = { customRangeDaysBack = it.filter { ch -> ch.isDigit() } }
                    )
                }

                if (filteredHistory.isEmpty()) {
                    item {
                        KhataEmptyStateCard(
                            title = "No Khata History Records",
                            subtitle = "All original udhaar entries, partial payments, full settlements, and edits across your entire account lifetime will appear here chronologically.",
                            onAddLenaHai = {
                                prefilledContactNameForAdd = ""
                                prefilledContactPhoneForAdd = ""
                                activeAddEntryDirection = KhataDirection.LENA_HAI
                            }
                        )
                    }
                } else {
                    items(
                        items = filteredHistory,
                        key = { it.id },
                        contentType = { "khata_history_row" }
                    ) { auditItem ->
                        KhataHistoryAuditCard(
                            item = auditItem,
                            stealthMode = stealthMode,
                            onClickContact = {
                                if (contactStateMap.containsKey(auditItem.contactId)) {
                                    selectedContactId = auditItem.contactId
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- MODAL 1: ADD LENA HAI / ADD DENA HAI DIALOG ---
    val currentAddDirection = activeAddEntryDirection
    if (currentAddDirection != null) {
        AddKhataUdhaarEntryDialog(
            direction = currentAddDirection,
            initialPersonName = prefilledContactNameForAdd,
            initialPhone = prefilledContactPhoneForAdd,
            onDismiss = { activeAddEntryDirection = null },
            onSave = { name, phone, amount, dueDateMillis, description, category, notes ->
                val res = AdvancedKhataStore.addUdhaarEntry(
                    context = context,
                    userId = userId,
                    personName = name,
                    phone = phone,
                    amount = amount,
                    direction = currentAddDirection,
                    dueDateMillis = dueDateMillis,
                    description = description,
                    category = category,
                    notes = notes
                )
                if (res.isSuccess) {
                    localRefreshTick++
                    activeAddEntryDirection = null
                    onShowToast("✅ Saved ${currentAddDirection.label} entry of ₹${formatIndianAmount(amount)} for $name")
                } else {
                    onShowToast(res.exceptionOrNull()?.message ?: "Failed to save Khata entry")
                }
            }
        )
    }

    // --- MODAL 2: RECORD PARTIAL OR FULL PAYMENT DIALOG ---
    val currentPaymentContact = paymentModalContactState
    if (currentPaymentContact != null) {
        RecordKhataPaymentDialog(
            contactState = currentPaymentContact,
            onDismiss = { paymentModalContactState = null },
            onRecordPayment = { paymentAmt, desc, notes ->
                val res = AdvancedKhataStore.recordPayment(
                    context = context,
                    userId = userId,
                    contactId = currentPaymentContact.contact.id,
                    paymentAmount = paymentAmt,
                    description = desc,
                    notes = notes
                )
                if (res.isSuccess) {
                    localRefreshTick++
                    paymentModalContactState = null
                    val remaining = max(0L, currentPaymentContact.outstandingAmount - paymentAmt)
                    onShowToast(
                        if (remaining == 0L) {
                            "🎉 Settled ${currentPaymentContact.contact.name}'s Khata in full!"
                        } else {
                            "✅ Recorded ₹${formatIndianAmount(paymentAmt)} payment · Remaining: ₹${formatIndianAmount(remaining)}"
                        }
                    )
                } else {
                    onShowToast(res.exceptionOrNull()?.message ?: "Could not record payment")
                }
            }
        )
    }

    // --- MODAL 3: EDIT ENTRY DIALOG ---
    val currentEditing = editingEntry
    if (currentEditing != null) {
        EditKhataEntryDialog(
            entry = currentEditing,
            onDismiss = { editingEntry = null },
            onSaveEdited = { newAmt, newDesc, newCat, newDueMillis, newNotes ->
                val res = AdvancedKhataStore.editEntry(
                    context = context,
                    userId = userId,
                    entryId = currentEditing.id,
                    newAmount = newAmt,
                    newDescription = newDesc,
                    newCategory = newCat,
                    newDueDateMillis = newDueMillis,
                    newNotes = newNotes
                )
                if (res.isSuccess) {
                    localRefreshTick++
                    editingEntry = null
                    onShowToast("✅ Updated entry & recalculated Khata balances")
                } else {
                    onShowToast(res.exceptionOrNull()?.message ?: "Could not edit entry")
                }
            }
        )
    }

    // --- MODAL 4: DELETE ENTRY CONFIRMATION DIALOG ---
    val currentDeleting = deletingEntry
    if (currentDeleting != null) {
        Dialog(onDismissRequest = { deletingEntry = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(KhataCardSurface)
                    .border(1.2.dp, KhataDenaPay, RoundedCornerShape(22.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Delete Khata Entry?",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = KhataPrimaryText
                )
                Text(
                    text = "Are you sure you want to delete this ₹${formatIndianAmount(currentDeleting.amount)} entry (${currentDeleting.description}) for ${currentDeleting.contactName}? Balances will be recalculated and an audit record will be kept.",
                    fontSize = 13.sp,
                    color = KhataSecondaryText,
                    lineHeight = 19.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { deletingEntry = null },
                        border = BorderStroke(1.dp, KhataBurgundyBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = KhataSecondaryText)
                    }
                    Button(
                        onClick = {
                            val res = AdvancedKhataStore.deleteEntry(
                                context = context,
                                userId = userId,
                                entryId = currentDeleting.id
                            )
                            if (res.isSuccess) {
                                localRefreshTick++
                                deletingEntry = null
                                onShowToast("🗑️ Entry deleted & Khata balance updated")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KhataBurgundySurface),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_delete_khata_entry_button")
                    ) {
                        Text("Delete Entry", color = KhataPrimaryText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- MODAL 5: 3-TONE WHATSAPP VASOOLI ENGINE (REMINDER TONE SELECTOR + SPLIT RECEIPT CARD) ---
    val currentVasooliContact = vasooliModalContactState
    if (currentVasooliContact != null) {
        KhataVasooliToneSelectorDialog(
            contactState = currentVasooliContact,
            specificEntry = vasooliSpecificEntry,
            userDisplayName = userDisplayName,
            userUpiId = userUpiId,
            onDismiss = {
                vasooliModalContactState = null
                vasooliSpecificEntry = null
            },
            onSendVasooliCard = { customMessageText ->
                val targetEntry = vasooliSpecificEntry
                vasooliModalContactState = null
                vasooliSpecificEntry = null
                KhataSplitCardGenerator.shareSplitReceiptCardOnWhatsApp(
                    context = context,
                    contactState = currentVasooliContact,
                    userDisplayName = userDisplayName,
                    userUpiId = userUpiId,
                    specificEntryDescription = targetEntry?.description,
                    specificEntryAmount = targetEntry?.amount,
                    customCaptionText = customMessageText,
                    onStatusToast = onShowToast
                )
            }
        )
    }

    // --- MODAL 6: 1-TAP GROUP BILL & TRIP SPLITTER ---
    if (showGroupBillSplitterModal) {
        GroupBillAndTripSplitterDialog(
            existingContacts = allContactStates.map { it.contact },
            onDismiss = { showGroupBillSplitterModal = false },
            onConfirmGroupSplit = { billTitle, category, splitResult, dueDateMillis, notes ->
                val res = AdvancedKhataStore.recordGroupBillSplit(
                    context = context,
                    userId = userId,
                    billTitle = billTitle,
                    category = category,
                    splitResult = splitResult,
                    dueDateMillis = dueDateMillis,
                    notes = notes
                )
                if (res.isSuccess) {
                    if (splitResult.myPersonalShare > 0L) {
                        onLogPersonalExpenseShare?.invoke(
                            "$billTitle (My Share)",
                            category,
                            splitResult.myPersonalShare,
                            "Group Bill Split of ₹${formatIndianAmount(splitResult.totalBillAmount)}"
                        )
                    }
                    localRefreshTick++
                    showGroupBillSplitterModal = false
                    val friendCount = res.getOrDefault(0)
                    onShowToast(
                        "✅ Split ₹${formatIndianAmount(splitResult.totalBillAmount)}: Logged ₹${formatIndianAmount(splitResult.myPersonalShare)} in Expenses & added Lena Hai for $friendCount ${if (friendCount == 1) "friend" else "friends"}!"
                    )
                } else {
                    onShowToast(res.exceptionOrNull()?.message ?: "Could not split bill")
                }
            }
        )
    }

    // --- MODAL 7: PROMISE-TO-PAY DUE DATE COUNTDOWN SETTER ---
    val currentPromiseContact = promiseDateModalContactState
    if (currentPromiseContact != null) {
        SetPromiseToPayDateDialog(
            contactState = currentPromiseContact,
            onDismiss = { promiseDateModalContactState = null },
            onSavePromiseDate = { targetEntry, newPromiseMillis ->
                val res = AdvancedKhataStore.editEntry(
                    context = context,
                    userId = userId,
                    entryId = targetEntry.id,
                    newAmount = targetEntry.amount,
                    newDescription = targetEntry.description,
                    newCategory = targetEntry.category,
                    newDueDateMillis = newPromiseMillis,
                    newNotes = targetEntry.notes
                )
                if (res.isSuccess) {
                    localRefreshTick++
                    promiseDateModalContactState = null
                    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                    onShowToast("📅 Promise Date set to ${fmt.format(Date(newPromiseMillis))} for ${currentPromiseContact.contact.name}")
                } else {
                    onShowToast("Could not update promise date")
                }
            }
        )
    }

    // --- MODAL 8: PROOF VAULT ATTACHMENT DIALOG ---
    val currentProofEntry = attachingProofEntry
    if (currentProofEntry != null) {
        AttachKhataProofDialog(
            entry = currentProofEntry,
            onDismiss = { attachingProofEntry = null },
            onAttachProof = { uri, label ->
                val res = AdvancedKhataStore.attachProofToEntry(
                    context = context,
                    userId = userId,
                    entryId = currentProofEntry.id,
                    proofUri = uri,
                    proofLabel = label
                )
                if (res.isSuccess) {
                    localRefreshTick++
                    attachingProofEntry = null
                    onShowToast("📎 Proof attached to ${currentProofEntry.contactName}'s entry")
                } else {
                    onShowToast("Could not attach proof")
                }
            }
        )
    }
}

@Composable
private fun KhataSummaryHeroSection(
    metrics: KhataSummaryMetrics,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current
    val isNetZero = metrics.netOutstandingBalance == 0L
    val isNetReceivable = metrics.netOutstandingBalance >= 0L
    val totalVolume = (metrics.totalLenaHai + metrics.totalDenaHai).coerceAtLeast(1L)
    val greenRatio = when {
        metrics.totalLenaHai == 0L && metrics.totalDenaHai == 0L -> 0.5f
        else -> (metrics.totalLenaHai.toFloat() / totalVolume.toFloat()).coerceIn(0.08f, 0.92f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("khata_summary_hero_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Two Sophisticated Financial Summary Cards: LENA HAI (Emerald) & DENA HAI (Rose/Burgundy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Card: LENA HAI (Money to Receive)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = KhataLenaReceive.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0B2119),
                                KhataCardSurface,
                                KhataPrimaryBackground
                            )
                        )
                    )
                    .border(0.9.dp, KhataLenaReceive.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .testTag("khata_summary_lena_hai_card"),
                verticalArrangement = Arrangement.SpaceBetween
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
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(KhataLenaReceive.copy(alpha = 0.15f))
                                .border(0.7.dp, KhataLenaReceive.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallReceived,
                                contentDescription = "Lena Hai",
                                tint = KhataLenaReceive,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "LENA HAI",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = KhataLenaReceive,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "YOU GET",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = KhataLenaReceive.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(metrics.totalLenaHai)}",
                    fontFamily = headingFont,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataLenaReceive,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${metrics.lenaHaiPeopleCount} ${if (metrics.lenaHaiPeopleCount == 1) "person owes" else "people owe"} you",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = KhataSecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Card: DENA HAI (Money to Pay)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = KhataDenaPay.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                KhataDeepBurgundy,
                                KhataCardSurface,
                                KhataPrimaryBackground
                            )
                        )
                    )
                    .border(0.9.dp, KhataDenaPay.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .testTag("khata_summary_dena_hai_card"),
                verticalArrangement = Arrangement.SpaceBetween
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
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(KhataDenaPay.copy(alpha = 0.15f))
                                .border(0.7.dp, KhataDenaPay.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallMade,
                                contentDescription = "Dena Hai",
                                tint = KhataDenaPay,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "DENA HAI",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = KhataDenaPay,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "YOU GIVE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = KhataDenaPay.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(metrics.totalDenaHai)}",
                    fontFamily = headingFont,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataDenaPay,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "You owe ${metrics.denaHaiPeopleCount} ${if (metrics.denaHaiPeopleCount == 1) "person" else "people"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = KhataSecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Net Khata Position: Pure White High-Readability Summary Card (30% White Identity)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFFFFF))
                .border(1.dp, Color(0xFFE0E6DD), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("khata_net_position_tug_of_war_bar"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Metadata Row: Label + Active/Settled Counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NET KHATA POSITION",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF475247)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFF3F6F1),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, Color(0xFFE0E6DD))
                    ) {
                        Text(
                            text = "${metrics.totalActiveContacts} active · ${metrics.totalSettledContacts} settled",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(0xFF475247),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (metrics.overdueAmount > 0L) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (stealthMode) "Overdue ₹•••" else "Overdue ₹${formatIndianAmount(metrics.overdueAmount)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.5.sp,
                                color = Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Prominent Net Amount + Direction Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val netAccentColor = when {
                    isNetZero -> Color(0xFF080B08)
                    isNetReceivable -> Color(0xFF080B08)
                    else -> Color(0xFFB91C1C)
                }
                Text(
                    text = if (stealthMode) {
                        "₹••••"
                    } else {
                        val signPrefix = when {
                            isNetZero -> ""
                            isNetReceivable -> "+"
                            else -> "-"
                        }
                        "${signPrefix}₹${formatIndianAmount(abs(metrics.netOutstandingBalance))}"
                    },
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = netAccentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val statusBadgeText = when {
                    isNetZero -> "✓ All Settled"
                    isNetReceivable -> "↙ You Get"
                    else -> "↗ You Give"
                }
                val statusBadgeBg = when {
                    isNetZero -> Color(0xFFF3F6F1)
                    isNetReceivable -> Color(0xFFB7F000)
                    else -> Color(0xFFFEE2E2)
                }
                val statusBadgeTextCol = when {
                    isNetZero -> Color(0xFF475247)
                    isNetReceivable -> Color(0xFF080B08)
                    else -> Color(0xFFB91C1C)
                }
                Surface(
                    color = statusBadgeBg,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.9.dp, Color(0xFF080B08).copy(alpha = 0.12f))
                ) {
                    Text(
                        text = statusBadgeText,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusBadgeTextCol,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Proportional Balance Indicator + Legend
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE0E6DD)),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(greenRatio)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF080B08))
                    )
                    Box(
                        modifier = Modifier
                            .weight((1f - greenRatio).coerceAtLeast(0.08f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(KhataDenaPay)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB7F000))
                                .border(0.8.dp, Color(0xFF080B08), CircleShape)
                        )
                        Text(
                            text = "You Receive",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475247)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "You Pay",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475247)
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(KhataDenaPay)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KhataContactsSearchFilterAndSortBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: KhataContactFilter,
    onSelectFilter: (KhataContactFilter) -> Unit,
    selectedSort: KhataContactSort,
    onSelectSort: (KhataContactSort) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        // Section Header: "My People" + Compact Inline Sort Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My People",
                fontFamily = headingFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPrimaryText
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort",
                    tint = KhataMutedText,
                    modifier = Modifier.size(13.dp)
                )
                KhataContactSort.entries.forEach { sortMode ->
                    val selected = selectedSort == sortMode
                    Surface(
                        color = if (selected) KhataDeepBurgundy else KhataCardSurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            0.7.dp,
                            if (selected) KhataBurgundyBorder else KhataBurgundyBorder.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.clickable { onSelectSort(sortMode) }
                    ) {
                        Text(
                            text = sortMode.label,
                            fontSize = 9.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) KhataPlatinumHighlight else KhataSecondaryText,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text("Search person by name or phone...", color = KhataMutedText, fontSize = 12.5.sp)
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = KhataPlatinumHighlight,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = KhataSecondaryText)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KhataBurgundyBorder,
                unfocusedBorderColor = KhataBurgundyBorder.copy(alpha = 0.45f),
                focusedContainerColor = KhataCardSurface,
                unfocusedContainerColor = KhataCardSurface,
                focusedTextColor = KhataPrimaryText,
                unfocusedTextColor = KhataPrimaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("khata_search_contacts_input")
        )

        // Filter Chips: All, Lena Hai, Dena Hai, Overdue, Settled
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KhataContactFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                Surface(
                    color = if (selected) KhataBurgundySurface else KhataCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        0.8.dp,
                        if (selected) KhataBrandPurple else KhataBurgundyBorder.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .clickable { onSelectFilter(filter) }
                        .testTag("khata_filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) KhataPrimaryText else KhataSecondaryText,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KhataContactCardItem(
    state: KhataContactComputedState,
    stealthMode: Boolean,
    onClick: () -> Unit,
    onQuickPayment: () -> Unit,
    onInstantUpiPay: () -> Unit = {},
    onShareWhatsAppSplitCard: () -> Unit = {}
) {
    val headingFont = LocalAppFontFamily.current
    val dateFmt = remember { SimpleDateFormat("dd MMM", Locale.ENGLISH) }
    val lastDateStr = remember(state.lastTransactionMillis) {
        dateFmt.format(Date(state.lastTransactionMillis))
    }

    val accentColor = when {
        state.isSettled -> KhataSettledSlate
        state.netDirection == KhataDirection.LENA_HAI -> KhataNeonEmerald
        else -> KhataCoralCrimson
    }
    val statusRelationLabel = when {
        state.isSettled -> "Settled · $lastDateStr"
        state.netDirection == KhataDirection.LENA_HAI -> "Owes you · $lastDateStr"
        else -> "You owe · $lastDateStr"
    }
    val directionMicroTag = when {
        state.isSettled -> "✓ SETTLED"
        state.netDirection == KhataDirection.LENA_HAI -> "↙ LENA HAI"
        else -> "↗ DENA HAI"
    }

    // "Dosti Credit Score" & Payback Reliability Badge
    val dostiScore = remember(state) {
        AdvancedKhataStore.computeDostiCreditScore(state)
    }
    val dostiBadgeColor = when (dostiScore.tier) {
        DostiReliabilityTier.FAST_PAYER -> KhataNeonEmerald
        DostiReliabilityTier.MODERATE -> Color(0xFFFBBF24)
        DostiReliabilityTier.LATE_PAYER -> KhataCoralCrimson
    }

    // Promise-to-Pay countdown if dueDateMillis is set
    val promiseCountdown = remember(state.earliestUnsettledDueDateMillis, state.isSettled) {
        AdvancedKhataStore.formatPromiseToPayCountdown(
            dueDateMillis = state.earliestUnsettledDueDateMillis,
            isSettled = state.isSettled
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    state.isSettled -> KhataSecondaryBackground.copy(alpha = 0.85f)
                    state.netDirection == KhataDirection.LENA_HAI -> KhataCardSurface
                    else -> KhataElevatedCardSurface
                }
            )
            .border(
                0.9.dp,
                when {
                    state.isSettled -> KhataSettledSlate.copy(alpha = 0.4f)
                    state.isOverdue -> KhataDenaPay.copy(alpha = 0.75f)
                    else -> KhataBurgundyBorder.copy(alpha = 0.55f)
                },
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .testTag("khata_contact_card_${state.contact.name}")
    ) {
        // Subtle 3.5dp vertical status indicator bar
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Avatar Initials Circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = when {
                                        state.isSettled -> listOf(KhataElevatedCardSurface, KhataSecondaryBackground)
                                        state.netDirection == KhataDirection.LENA_HAI -> listOf(Color(0xFF102D22), KhataLenaDarkGlassSurface)
                                        else -> listOf(KhataBurgundySurface, KhataDeepBurgundy)
                                    }
                                )
                            )
                            .border(1.1.dp, accentColor.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.contact.avatarInitials,
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (state.isSettled) KhataSecondaryText else KhataPrimaryText
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = state.contact.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (state.isSettled) KhataSecondaryText else KhataPrimaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Dosti Credit Score & Payback Reliability Pill
                            Surface(
                                color = dostiBadgeColor.copy(alpha = 0.13f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.7.dp, dostiBadgeColor.copy(alpha = 0.55f)),
                                modifier = Modifier.testTag("khata_dosti_badge_${state.contact.name}")
                            ) {
                                Text(
                                    text = dostiScore.badgeLabel,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = dostiBadgeColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = statusRelationLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (state.isSettled) KhataMutedText else KhataSecondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (promiseCountdown != null) {
                                val cdColor = if (promiseCountdown.isOverdue) KhataDenaPay else KhataPlatinumHighlight
                                Surface(
                                    color = cdColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = promiseCountdown.label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cdColor,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Column: Prominent Balance + Direction Sub-label + Navigation Chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (stealthMode) {
                                "₹••••"
                            } else if (state.isSettled) {
                                "₹0"
                            } else {
                                "₹${formatIndianAmount(state.outstandingAmount)}"
                            },
                            fontFamily = headingFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (state.isSettled) KhataMutedText else accentColor,
                            maxLines = 1
                        )
                        Text(
                            text = directionMicroTag,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isSettled) KhataMutedText else accentColor.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Contact Details",
                        tint = KhataMutedText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (!state.isSettled) {
                HorizontalDivider(
                    color = KhataBurgundyBorder.copy(alpha = 0.28f),
                    thickness = 0.6.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total ₹${formatIndianAmount(state.totalOriginalRecordedForActiveDirection)} · Settled ₹${formatIndianAmount(state.totalPaidOrReceivedForActiveDirection)}",
                        fontSize = 10.sp,
                        color = KhataMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Every "Lena Hai" contact card has [📲 Vasooli / Split Card] button
                        if (state.netDirection == KhataDirection.LENA_HAI && state.outstandingAmount > 0L) {
                            Surface(
                                color = KhataLenaDarkGlassSurface,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.9.dp, KhataLenaReceive.copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .clickable { onShareWhatsAppSplitCard() }
                                    .testTag("khata_share_split_whatsapp_${state.contact.name}")
                            ) {
                                Text(
                                    text = "📲 Vasooli",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KhataLenaReceive,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Every "Dena Hai" contact card has [⚡ Pay ₹Amount] UPI button
                        if (state.netDirection == KhataDirection.DENA_HAI && state.outstandingAmount > 0L) {
                            Surface(
                                color = KhataBurgundySurface,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.9.dp, KhataDenaPay.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .clickable { onInstantUpiPay() }
                                    .testTag("khata_instant_upi_pay_${state.contact.name}")
                            ) {
                                Text(
                                    text = if (stealthMode) "⚡ Pay ₹•••" else "⚡ Pay ₹${formatIndianAmount(state.outstandingAmount)}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KhataPrimaryText,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            color = KhataElevatedCardSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, KhataBurgundyBorder.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .clickable { onQuickPayment() }
                                .testTag("khata_quick_payment_${state.contact.name}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = "Record Payment",
                                    tint = KhataPlatinumHighlight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Record",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KhataPlatinumHighlight,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * SECTION 5. INDIVIDUAL PERSON LEDGER (WITH GPAY-STYLE CHAT TIMELINE & PROMISE-TO-PAY COUNTDOWN)
 */
@Composable
private fun KhataIndividualPersonLedgerView(
    state: KhataContactComputedState,
    stealthMode: Boolean,
    userDisplayName: String = "Kharchee Member",
    userUpiId: String = "",
    onBack: () -> Unit,
    onAddLenaHai: () -> Unit,
    onAddDenaHai: () -> Unit,
    onOpenRecordPayment: () -> Unit,
    onOpenVasooliSheet: (KhataLedgerEntry?) -> Unit = {},
    onOpenSetPromiseDate: () -> Unit = {},
    onSettleInFull: () -> Unit,
    onEditEntry: (KhataLedgerEntry) -> Unit,
    onRequestDeleteEntry: (KhataLedgerEntry) -> Unit,
    @Suppress("UNUSED_PARAMETER") onAttachProof: (KhataLedgerEntry) -> Unit = {},
    onToggleReminder: (Boolean) -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.ENGLISH) }
    val dueFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    val badgeColor = when {
        state.isSettled -> KhataMutedText
        state.netDirection == KhataDirection.LENA_HAI -> KhataLenaReceive
        else -> KhataDenaPay
    }

    val dostiScore = remember(state) {
        AdvancedKhataStore.computeDostiCreditScore(state)
    }
    val dostiBadgeColor = when (dostiScore.tier) {
        DostiReliabilityTier.FAST_PAYER -> KhataLenaReceive
        DostiReliabilityTier.MODERATE -> Color(0xFFFBBF24)
        DostiReliabilityTier.LATE_PAYER -> KhataDenaPay
    }

    val promiseCountdown = remember(state.earliestUnsettledDueDateMillis, state.isSettled) {
        AdvancedKhataStore.formatPromiseToPayCountdown(
            dueDateMillis = state.earliestUnsettledDueDateMillis,
            isSettled = state.isSettled
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KhataPrimaryBackground)
            .testTag("khata_individual_ledger_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with Back button, Name, Dosti Credit Score Badge, Phone & Reminder actions
        item {
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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KhataElevatedCardSurface)
                            .border(0.9.dp, KhataBurgundyBorder.copy(alpha = 0.65f), CircleShape)
                            .testTag("khata_person_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KhataPrimaryText
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.contact.name,
                                fontFamily = headingFont,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KhataPrimaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                color = dostiBadgeColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, dostiBadgeColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = dostiScore.badgeLabel,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = dostiBadgeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = state.contact.phone.ifBlank { "Personal Khata Relationship" },
                            fontSize = 11.sp,
                            color = KhataSecondaryText
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onToggleReminder(!state.contact.reminderEnabled) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KhataElevatedCardSurface)
                            .border(0.9.dp, KhataBurgundyBorder.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (state.contact.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Toggle Due Reminders",
                            tint = if (state.contact.reminderEnabled) KhataBrandPurple else KhataMutedText,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    if (!state.isSettled) {
                        IconButton(
                            onClick = {
                                if (state.netDirection == KhataDirection.LENA_HAI) {
                                    onOpenVasooliSheet(null)
                                } else {
                                    val msg = "Hi ${state.contact.name}, KHARCHEE Khata summary: ₹${formatIndianAmount(state.outstandingAmount)} is scheduled for settlement."
                                    try {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, msg)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Send Khata Statement"))
                                    } catch (_: Exception) {
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(KhataElevatedCardSurface)
                                .border(0.9.dp, KhataBurgundyBorder.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Khata Statement",
                                tint = KhataPlatinumHighlight,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }

        // Person Summary Card: Outstanding Balance, Promise-to-Pay Countdown Chip, Original Recorded, Received/Paid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = when {
                                state.isSettled -> listOf(KhataElevatedCardSurface, KhataCardSurface)
                                state.netDirection == KhataDirection.LENA_HAI -> listOf(Color(0xFF0E221D), KhataCardSurface)
                                else -> listOf(KhataBurgundySurface, KhataCardSurface)
                            }
                        )
                    )
                    .border(1.dp, badgeColor.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when {
                                state.isSettled -> "CURRENT STATUS · ✓ SETTLED"
                                state.netDirection == KhataDirection.LENA_HAI -> "↙ YOU GET · LENA HAI (RECEIVABLE)"
                                else -> "↗ YOU GIVE · DENA HAI (PAYABLE)"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(state.outstandingAmount)}",
                            fontFamily = headingFont,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (state.isSettled) KhataSecondaryText else badgeColor
                        )
                    }

                    if (state.isSettled) {
                        Surface(
                            color = KhataElevatedCardSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.9.dp, KhataBurgundyBorder.copy(alpha = 0.55f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✓ Settled",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KhataSecondaryText
                                )
                            }
                        }
                    }
                }

                // FEATURE #3 — [📅 Set Due Date / Promise Date] chip + Live Promise Countdown Pill on active dues
                if (!state.isSettled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = KhataSecondaryBackground,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.9.dp, KhataBurgundyBorder.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .clickable { onOpenSetPromiseDate() }
                                .testTag("person_ledger_set_promise_date_chip")
                        ) {
                            Text(
                                text = if (state.earliestUnsettledDueDateMillis != null) {
                                    "📅 Promise: ${dueFmt.format(Date(state.earliestUnsettledDueDateMillis))} (Edit)"
                                } else {
                                    "📅 Set Due Date / Promise Date"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KhataPlatinumHighlight,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        if (promiseCountdown != null) {
                            val pillColor = if (promiseCountdown.isOverdue) KhataDenaPay else KhataLenaReceive
                            Surface(
                                color = pillColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.9.dp, pillColor.copy(alpha = 0.7f)),
                                modifier = Modifier.testTag("person_ledger_promise_countdown_pill")
                            ) {
                                Text(
                                    text = promiseCountdown.label,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = pillColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Original vs Received/Paid Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KhataSecondaryBackground)
                            .border(0.7.dp, KhataBurgundyBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ORIGINALLY RECORDED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            color = KhataMutedText
                        )
                        Text(
                            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(state.totalOriginalLenaHai + state.totalOriginalDenaHai)}",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = KhataPrimaryText
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KhataSecondaryBackground)
                            .border(0.7.dp, KhataBurgundyBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "RECEIVED / PAID",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            color = KhataMutedText
                        )
                        Text(
                            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(state.totalPaymentsReceived + state.totalPaymentsMade)}",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = KhataLenaReceive
                        )
                    }
                }

                // Action Buttons inside Person Ledger: 3-Tone Vasooli / Split Receipt (for Lena Hai), 1-Tap UPI Pay (for Dena Hai), Record Payment, Mark Settled
                if (!state.isSettled) {
                    if (state.netDirection == KhataDirection.LENA_HAI && state.outstandingAmount > 0L) {
                        Button(
                            onClick = { onOpenVasooliSheet(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KhataNeonEmerald,
                                contentColor = Color(0xFF040508)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = KhataNeonEmerald)
                                .testTag("person_ledger_share_split_whatsapp_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📲 Vasooli / Split Card (3-Tone WhatsApp)",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (state.netDirection == KhataDirection.DENA_HAI && state.outstandingAmount > 0L) {
                        var showPersonVerifyDialog by remember { mutableStateOf(false) }
                        val resolvedVpa = remember(state.contact.id, state.contact.phone, state.contact.name, showPersonVerifyDialog) {
                            PaymentUtil.resolvePayeeVpa(state.contact.phone, state.contact.name)
                                .ifBlank { PaymentUtil.getSavedVerifiedVpa(context, state.contact.id) }
                        }
                        if (showPersonVerifyDialog) {
                            com.example.util.VerifyOrEnterRecipientUpiDialog(
                                recipientName = state.contact.name,
                                amountInr = state.outstandingAmount.toString(),
                                initialVpa = resolvedVpa,
                                onConfirmVerifiedUpiPay = { enteredVpa ->
                                    PaymentUtil.saveVerifiedVpa(context, state.contact.id, enteredVpa)
                                    showPersonVerifyDialog = false
                                    PaymentUtil.launchUpiPaymentIntent(
                                        context = context,
                                        payeeVpa = enteredVpa,
                                        payeeName = state.contact.name,
                                        amount = state.outstandingAmount.toString(),
                                        txnNote = "Settle Khata Due - Kharchee",
                                        contactId = state.contact.id,
                                        onStatusMessage = onShowToast
                                    )
                                },
                                onOpenGenericUpiScanner = {
                                    showPersonVerifyDialog = false
                                    PaymentUtil.launchGenericUpiAppChooser(
                                        context = context,
                                        payeeName = state.contact.name,
                                        amount = state.outstandingAmount.toString(),
                                        txnNote = "Settle Khata Due - Kharchee",
                                        contactId = state.contact.id,
                                        onStatusMessage = onShowToast
                                    )
                                },
                                onMarkPaidManually = {
                                    showPersonVerifyDialog = false
                                    onOpenRecordPayment()
                                },
                                onDismiss = { showPersonVerifyDialog = false }
                            )
                        }
                        Button(
                            onClick = {
                                if (resolvedVpa.isNotBlank()) {
                                    PaymentUtil.launchUpiPaymentIntent(
                                        context = context,
                                        payeeVpa = resolvedVpa,
                                        payeeName = state.contact.name,
                                        amount = state.outstandingAmount.toString(),
                                        txnNote = "Settle Khata Due - Kharchee",
                                        contactId = state.contact.id,
                                        onStatusMessage = onShowToast
                                    )
                                } else {
                                    showPersonVerifyDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KhataCoralCrimson,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = KhataCoralCrimson)
                                .testTag("person_ledger_instant_upi_pay_button")
                        ) {
                            val vpaDisplay = if (resolvedVpa.isNotBlank()) "($resolvedVpa)" else "(Verify / Scan UPI)"
                            Text(
                                text = if (stealthMode) "⚡ Pay ₹••• via UPI $vpaDisplay" else "⚡ Pay ₹${formatIndianAmount(state.outstandingAmount)} $vpaDisplay",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenRecordPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = MaiPrimaryPurpleEnd),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("person_ledger_record_payment_button")
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Payment", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onSettleInFull,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, KhataNeonEmerald),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("person_ledger_settle_full_button")
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = KhataNeonEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Settled", color = KhataNeonEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddLenaHai,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.9.dp, KhataLenaReceive.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Lena Hai", color = KhataLenaReceive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onAddDenaHai,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.9.dp, KhataDenaPay.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Dena Hai", color = KhataDenaPay, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Lifetime Financial Relationship Profile Card
        item {
            val contactProfileMetrics = remember(state) {
                AdvancedKhataStore.computeLifetimeContactProfileMetrics(state)
            }
            LifetimeContactProfileCard(
                state = state,
                profile = contactProfileMetrics,
                stealthMode = stealthMode,
                userUpiId = userUpiId,
                onShowToast = onShowToast
            )
        }

        // Section Header: GPay-Style Chat Timeline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Relationship Ledger (${state.activeEntries.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KhataPrimaryText
                )
                Text(
                    text = "← Borrowed (Dena)  •  You Gave (Lena) →",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = KhataSecondaryText
                )
            }
        }

        items(
            items = state.activeEntries,
            key = { it.id },
            contentType = { "khata_entry_row" }
        ) { entry ->
            val isPayment = entry.recordType == KhataRecordType.PARTIAL_PAYMENT ||
                entry.recordType == KhataRecordType.FULL_SETTLEMENT
            val isRightAlignedLena = entry.direction == KhataDirection.LENA_HAI
            val bubbleAccent = when {
                isPayment -> Color(0xFF38BDF8)
                isRightAlignedLena -> KhataNeonEmerald
                else -> KhataCoralCrimson
            }
            val bubbleBg = when {
                isPayment -> Color(0xFF0A1C2E)
                isRightAlignedLena -> Color(0xFF061E14)
                else -> Color(0xFF210811)
            }
            val bubbleShape = if (isRightAlignedLena) {
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
            } else {
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
            }
            val entryCountdown = remember(entry.dueDateMillis, isPayment) {
                AdvancedKhataStore.formatPromiseToPayCountdown(
                    dueDateMillis = entry.dueDateMillis,
                    isSettled = isPayment
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (isRightAlignedLena) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .clip(bubbleShape)
                        .background(bubbleBg)
                        .border(1.2.dp, bubbleAccent.copy(alpha = 0.7f), bubbleShape)
                        .padding(14.dp)
                        .testTag("khata_chat_bubble_${entry.id}"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Surface(
                                color = bubbleAccent.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (isRightAlignedLena) {
                                        "↙ YOU GET · ${entry.recordType.label.uppercase()}"
                                    } else {
                                        "↗ YOU GIVE · ${entry.recordType.label.uppercase()}"
                                    },
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = bubbleAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (entry.isEdited) {
                                Text(
                                    text = "(Edited)",
                                    fontSize = 10.sp,
                                    color = Color(0xFFD8B4FE),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(entry.amount)}",
                            fontFamily = headingFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = bubbleAccent
                        )
                    }

                    Text(
                        text = entry.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    if (entry.notes.isNotBlank()) {
                        Text(
                            text = "Note: ${entry.notes}",
                            fontSize = 12.sp,
                            color = MaiLavenderSubtext
                        )
                    }

                    // 1-Tap 3-Tone WhatsApp Vasooli & Split Card button on every friend's due entry ("Lena Hai")
                    if (entry.direction == KhataDirection.LENA_HAI && !isPayment) {
                        Surface(
                            color = Color(0xFF0A2E1E),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, KhataNeonEmerald),
                            modifier = Modifier
                                .clickable { onOpenVasooliSheet(entry) }
                                .testTag("entry_share_split_whatsapp_${entry.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Split Card on WhatsApp",
                                    tint = KhataNeonEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "📲 Vasooli / Share Split Card on WhatsApp",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KhataNeonEmerald
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${entry.category} · ${dateFmt.format(Date(entry.txDateMillis))}",
                                fontSize = 11.sp,
                                color = MaiMutedViolet
                            )
                            if (entry.dueDateMillis != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Promise: ${dueFmt.format(Date(entry.dueDateMillis))}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD8B4FE)
                                    )
                                    if (entryCountdown != null) {
                                        Text(
                                            text = entryCountdown.label,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (entryCountdown.isOverdue) KhataCoralCrimson else KhataNeonEmerald
                                        )
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onEditEntry(entry) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("edit_khata_entry_${entry.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Entry",
                                    tint = MaiLavenderSubtext,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onRequestDeleteEntry(entry) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("delete_khata_entry_${entry.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Entry",
                                    tint = KhataCoralCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * FEATURE #2 — 3-Tone WhatsApp Vasooli Engine (Reminder Tone Selector + Attached Kharchee Split Receipt Card)
 */
@Composable
private fun KhataVasooliToneSelectorDialog(
    contactState: KhataContactComputedState,
    specificEntry: KhataLedgerEntry?,
    userDisplayName: String,
    userUpiId: String,
    onDismiss: () -> Unit,
    onSendVasooliCard: (String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val effectiveAmount = specificEntry?.amount ?: contactState.outstandingAmount.coerceAtLeast(0L)
    val effectiveItemSummary = specificEntry?.description
        ?: contactState.activeEntries.firstOrNull()?.description?.ifBlank { "Shared Split / Udhaar" }
        ?: "Shared Split / Udhaar"

    var selectedTone by remember { mutableStateOf(VasooliReminderTone.POLITE) }
    var editableMessage by remember(selectedTone, effectiveAmount, effectiveItemSummary) {
        mutableStateOf(
            KhataSplitCardGenerator.buildVasooliToneMessage(
                tone = selectedTone,
                friendName = contactState.contact.name,
                amountInr = effectiveAmount,
                itemSummary = effectiveItemSummary,
                userDisplayName = userDisplayName,
                userUpiId = userUpiId
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0A0F18))
                .border(1.5.dp, KhataNeonEmerald, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("khata_vasooli_tone_dialog"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3-TONE WHATSAPP VASOOLI ENGINE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KhataNeonEmerald
                    )
                    Text(
                        text = "Remind ${contactState.contact.name} • ₹${formatIndianAmount(effectiveAmount)}",
                        fontFamily = headingFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Text(
                    text = "✕",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiLavenderSubtext,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(6.dp)
                )
            }

            // 3 Selectable Tone Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VasooliReminderTone.entries.forEach { tone ->
                    val isSelected = selectedTone == tone
                    Surface(
                        color = if (isSelected) Color(0xFF062919) else Color(0xFF131926),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            if (isSelected) 1.4.dp else 1.dp,
                            if (isSelected) KhataNeonEmerald else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTone = tone }
                            .testTag("vasooli_tone_chip_${tone.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = tone.badgeTitle,
                                fontFamily = headingFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isSelected) KhataNeonEmerald else Color.White
                            )
                            Text(
                                text = tone.subtitle,
                                fontSize = 11.sp,
                                color = MaiLavenderSubtext
                            )
                        }
                    }
                }
            }

            // Live Message Preview / Editor
            OutlinedTextField(
                value = editableMessage,
                onValueChange = { editableMessage = it },
                label = { Text("WhatsApp Message Preview", color = KhataNeonEmerald, fontSize = 11.sp) },
                minLines = 4,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E8F0),
                    focusedBorderColor = KhataNeonEmerald,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF070B12),
                    unfocusedContainerColor = Color(0xFF070B12)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vasooli_message_preview_input")
            )

            // Attached Split Receipt Card Indicator
            Surface(
                color = Color(0xFF062014),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, KhataNeonEmerald.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = KhataNeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "🧾 Dark Kharchee Split Receipt Card (PNG + UPI QR) auto-attached",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }

            Button(
                onClick = { onSendVasooliCard(editableMessage) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KhataNeonEmerald,
                    contentColor = Color(0xFF040508)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("vasooli_send_whatsapp_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📲 Send Vasooli + Split Card on WhatsApp",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * FEATURE #4 — 1-Tap Group Bill & Trip Splitter Dialog
 */
@Composable
private fun GroupBillAndTripSplitterDialog(
    existingContacts: List<com.example.data.model.KhataContact>,
    onDismiss: () -> Unit,
    onConfirmGroupSplit: (
        billTitle: String,
        category: String,
        splitResult: GroupSplitComputationResult,
        dueDateMillis: Long?,
        notes: String
    ) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var billTitle by remember { mutableStateOf("") }
    var totalBillText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Outing") }
    var isEqualSplit by remember { mutableStateOf(true) }
    var includeSelfInSplit by remember { mutableStateOf(true) }
    var dueDaysText by remember { mutableStateOf("3") }

    // Available friends list (existing Khata contacts + any inline-added friends)
    var candidateFriends by remember(existingContacts) {
        mutableStateOf(
            if (existingContacts.isNotEmpty()) {
                existingContacts.map { it.name to it.phone }
            } else {
                listOf("Rahul" to "", "Aman" to "")
            }
        )
    }
    var selectedFriendNames by remember(candidateFriends) {
        mutableStateOf(candidateFriends.take(2).map { it.first }.toSet())
    }
    var newFriendNameInput by remember { mutableStateOf("") }
    var customAmountsMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val totalBillAmount = totalBillText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val selectedFriendPairs = remember(candidateFriends, selectedFriendNames) {
        candidateFriends.filter { selectedFriendNames.contains(it.first) }
    }
    val parsedCustomAmounts = remember(customAmountsMap) {
        customAmountsMap.mapValues { (_, v) -> v.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    }

    val splitComputation = remember(
        totalBillAmount,
        selectedFriendPairs,
        isEqualSplit,
        includeSelfInSplit,
        parsedCustomAmounts
    ) {
        AdvancedKhataStore.computeGroupBillSplit(
            totalBillAmount = totalBillAmount,
            selectedFriends = selectedFriendPairs,
            isEqualSplit = isEqualSplit,
            includeSelf = includeSelfInSplit,
            customFriendAmounts = parsedCustomAmounts
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0B0E18))
                .border(1.5.dp, Color(0xFFA855F7), RoundedCornerShape(24.dp))
                .padding(18.dp)
                .testTag("khata_group_bill_splitter_dialog"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👥 1-TAP GROUP BILL & TRIP SPLITTER",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD8B4FE)
                    )
                    Text(
                        text = "Auto-Log Expense + Friends' Khata",
                        fontFamily = headingFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Text(
                    text = "✕",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiLavenderSubtext,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(6.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = totalBillText,
                    onValueChange = { totalBillText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total Bill (₹)", fontSize = 11.sp) },
                    placeholder = { Text("1200") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KhataNeonEmerald,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("group_split_total_amount_input")
                )

                OutlinedTextField(
                    value = billTitle,
                    onValueChange = { billTitle = it },
                    label = { Text("For (Pizza / Trip)", fontSize = 11.sp) },
                    placeholder = { Text("Goa / Dinner") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("group_split_title_input")
                )
            }

            // Equal Split vs Custom Amounts Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (isEqualSplit) Color(0xFF062919) else Color(0xFF131926),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isEqualSplit) KhataNeonEmerald else Color(0xFF334155)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEqualSplit = true }
                        .testTag("group_split_mode_equal")
                ) {
                    Text(
                        text = "⚡ Equal Split",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isEqualSplit) KhataNeonEmerald else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    color = if (!isEqualSplit) Color(0xFF281548) else Color(0xFF131926),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (!isEqualSplit) Color(0xFFA855F7) else Color(0xFF334155)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEqualSplit = false }
                        .testTag("group_split_mode_custom")
                ) {
                    Text(
                        text = "✏️ Custom Amounts",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (!isEqualSplit) Color(0xFFD8B4FE) else Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Select Friends chips + Inline Add Friend
            Text(
                text = "Select Friends to Split With (${selectedFriendNames.size} selected):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaiLavenderSubtext
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                candidateFriends.forEach { (friendName, _) ->
                    val isSelected = selectedFriendNames.contains(friendName)
                    Surface(
                        color = if (isSelected) Color(0xFF062919) else Color(0xFF131926),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (isSelected) KhataNeonEmerald else Color(0xFF334155)),
                        modifier = Modifier.clickable {
                            selectedFriendNames = if (isSelected) {
                                selectedFriendNames - friendName
                            } else {
                                selectedFriendNames + friendName
                            }
                        }
                    ) {
                        Text(
                            text = if (isSelected) "✓ $friendName" else "+ $friendName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) KhataNeonEmerald else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Quick inline friend adder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newFriendNameInput,
                    onValueChange = { newFriendNameInput = it },
                    placeholder = { Text("Add friend name...", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val clean = newFriendNameInput.trim()
                        if (clean.isNotBlank()) {
                            if (candidateFriends.none { it.first.equals(clean, ignoreCase = true) }) {
                                candidateFriends = candidateFriends + (clean to "")
                            }
                            selectedFriendNames = selectedFriendNames + clean
                            newFriendNameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1E6D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Custom Amount inputs when Custom mode is active
            if (!isEqualSplit && selectedFriendPairs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedFriendPairs.take(4).forEach { (friendName, _) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$friendName's Share (₹):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customAmountsMap[friendName].orEmpty(),
                                onValueChange = { entered ->
                                    customAmountsMap = customAmountsMap + (friendName to entered.filter { c -> c.isDigit() })
                                },
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }

            // Live Split Summary Preview Box
            Surface(
                color = Color(0xFF07160F),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, KhataNeonEmerald.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⚡ LIVE SPLIT PREVIEW",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KhataNeonEmerald
                    )
                    Text(
                        text = "• Your Personal Expense Share: ₹${formatIndianAmount(splitComputation.myPersonalShare)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    splitComputation.friendShares.forEach { share ->
                        Text(
                            text = "• ${share.name} owes you (Lena Hai): ₹${formatIndianAmount(share.amount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KhataNeonEmerald
                        )
                    }
                    if (!splitComputation.isValid) {
                        Text(
                            text = splitComputation.validationMessage,
                            fontSize = 11.sp,
                            color = MaiWarningAmber
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val dueDays = dueDaysText.toLongOrNull() ?: 3L
                    val dueMillis = System.currentTimeMillis() + (dueDays.coerceAtLeast(1L) * 86_400_000L)
                    onConfirmGroupSplit(
                        billTitle.ifBlank { "Group Split Bill" },
                        selectedCategory,
                        splitComputation,
                        dueMillis,
                        "1-Tap Group Split"
                    )
                },
                enabled = splitComputation.isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KhataNeonEmerald,
                    contentColor = Color(0xFF040508)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("group_split_confirm_button")
            ) {
                Text(
                    text = "⚡ Split & Log Expense + ${splitComputation.friendShares.size} Khata Dues",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * FEATURE #3 — Promise-to-Pay Date Setter Dialog
 */
@Composable
private fun SetPromiseToPayDateDialog(
    contactState: KhataContactComputedState,
    onDismiss: () -> Unit,
    onSavePromiseDate: (targetEntry: KhataLedgerEntry, newDueDateMillis: Long) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val targetEntry = remember(contactState) {
        contactState.activeEntries.firstOrNull { it.recordType == KhataRecordType.ORIGINAL_ENTRY }
            ?: contactState.activeEntries.firstOrNull()
    }
    var customDaysText by remember { mutableStateOf("3") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0B0E18))
                .border(1.4.dp, Color(0xFF38BDF8), RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag("set_promise_date_dialog"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📅 Set Promise-to-Pay Date",
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = "Track a live countdown pill (⏰ Due in X days / 🚨 Overdue) for ${contactState.contact.name}'s ₹${formatIndianAmount(contactState.outstandingAmount)} balance.",
                fontSize = 12.sp,
                color = MaiLavenderSubtext
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2 to "In 2d", 3 to "In 3d", 7 to "In 7d", 15 to "In 15d").forEach { (days, label) ->
                    Surface(
                        color = Color(0xFF131D31),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (targetEntry != null) {
                                    val newMillis = System.currentTimeMillis() + (days * 86_400_000L)
                                    onSavePromiseDate(targetEntry, newMillis)
                                }
                            }
                            .testTag("promise_quick_days_$days")
                    ) {
                        Text(
                            text = label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = customDaysText,
                onValueChange = { customDaysText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Or enter custom days from today") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MaiCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = MaiLavenderSubtext)
                }
                Button(
                    onClick = {
                        if (targetEntry != null) {
                            val days = (customDaysText.toLongOrNull() ?: 3L).coerceAtLeast(1L)
                            val newMillis = System.currentTimeMillis() + (days * 86_400_000L)
                            onSavePromiseDate(targetEntry, newMillis)
                        } else {
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8),
                        contentColor = Color(0xFF040508)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_save_promise_date_button")
                ) {
                    Text("Save Promise Date", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun KhataHistorySearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: KhataHistoryFilter,
    onSelectFilter: (KhataHistoryFilter) -> Unit,
    customRangeDays: String,
    onCustomRangeDaysChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text("Search lifetime history by person or note...", color = KhataMutedText, fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search History", tint = KhataPlatinumHighlight)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KhataBurgundyBorder,
                unfocusedBorderColor = KhataBurgundyBorder.copy(alpha = 0.45f),
                focusedContainerColor = KhataCardSurface,
                unfocusedContainerColor = KhataCardSurface,
                focusedTextColor = KhataPrimaryText,
                unfocusedTextColor = KhataPrimaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("khata_history_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KhataHistoryFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                Surface(
                    color = if (selected) KhataBurgundySurface else KhataCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        0.9.dp,
                        if (selected) KhataBrandPurple.copy(alpha = 0.85f) else KhataBurgundyBorder.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.clickable { onSelectFilter(filter) }
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) KhataPrimaryText else KhataSecondaryText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (selectedFilter == KhataHistoryFilter.CUSTOM_DATE_RANGE) {
            OutlinedTextField(
                value = customRangeDays,
                onValueChange = onCustomRangeDaysChange,
                label = { Text("Show records from last N days") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KhataBurgundyBorder,
                    unfocusedBorderColor = KhataBurgundyBorder.copy(alpha = 0.45f),
                    focusedTextColor = KhataPrimaryText,
                    unfocusedTextColor = KhataPrimaryText
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun KhataHistoryAuditCard(
    item: KhataHistoryAuditItem,
    stealthMode: Boolean,
    onClickContact: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.ENGLISH) }
    val accent = when {
        item.action == KhataAuditAction.DELETED_ENTRY -> KhataMutedText
        item.recordType == KhataRecordType.PARTIAL_PAYMENT || item.recordType == KhataRecordType.FULL_SETTLEMENT -> KhataPlatinumHighlight
        item.direction == KhataDirection.LENA_HAI -> KhataLenaReceive
        else -> KhataDenaPay
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KhataCardSurface)
            .border(0.9.dp, KhataBurgundyBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClickContact() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.contactName,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPrimaryText
            )
            Text(
                text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(item.amount)}",
                fontFamily = headingFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.direction.label} · ${item.action.label}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Surface(
                color = accent.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.7.dp, accent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = item.paymentStatus,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = item.description,
            fontSize = 12.sp,
            color = KhataSecondaryText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFmt.format(Date(item.timestampMillis)),
                fontSize = 10.sp,
                color = KhataMutedText
            )
            Text(
                text = if (stealthMode) "Bal: ₹•••" else "Balance After: ₹${formatIndianAmount(item.remainingBalanceAfter)}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = KhataPlatinumHighlight
            )
        }
    }
}

@Composable
private fun KhataEmptyStateCard(
    title: String,
    subtitle: String,
    onAddLenaHai: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(KhataElevatedCardSurface, KhataCardSurface)
                )
            )
            .border(0.9.dp, KhataBurgundyBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KhataBurgundySurface)
                .border(0.8.dp, KhataBurgundyBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = KhataPlatinumHighlight,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPrimaryText
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = KhataSecondaryText,
                lineHeight = 15.sp
            )
        }
        Surface(
            color = KhataBurgundySurface,
            shape = RoundedCornerShape(50),
            border = BorderStroke(0.9.dp, KhataBrandPurple.copy(alpha = 0.75f)),
            modifier = Modifier.clickable { onAddLenaHai() }
        ) {
            Text(
                text = "+ Add First",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhataPrimaryText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun AddKhataUdhaarEntryDialog(
    direction: KhataDirection,
    initialPersonName: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onSave: (
        personName: String,
        phone: String,
        amount: Long,
        dueDateMillis: Long?,
        description: String,
        category: String,
        notes: String
    ) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var name by remember { mutableStateOf(initialPersonName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(KHATA_CATEGORIES.first()) }
    var notes by remember { mutableStateOf("") }
    var hasDueDate by remember { mutableStateOf(false) }
    var dueInDaysStr by remember { mutableStateOf("7") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val accentColor = if (direction == KhataDirection.LENA_HAI) MaiPositiveEmerald else MaiExpenseRose

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaiCardSurface)
                .border(1.4.dp, accentColor, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (direction == KhataDirection.LENA_HAI) {
                    "+ Add Lena Hai (You Will Receive)"
                } else {
                    "+ Add Dena Hai (You Have to Pay)"
                },
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = Color.White
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorText = null
                },
                label = { Text("Person's Name *") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("khata_entry_name_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it.filter { ch -> ch.isDigit() }
                        errorText = null
                    },
                    label = { Text("Amount (₹) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaiCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("khata_entry_amount_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("UPI ID / Phone") },
                    placeholder = { Text("friend@upi", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaiCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Reason (Optional)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("khata_entry_description_input")
            )

            // Category chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KHATA_CATEGORIES.forEach { cat ->
                    val selected = selectedCategory == cat
                    Surface(
                        color = if (selected) MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, if (selected) Color(0xFFD8B4FE) else MaiCardBorder),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Optional Due Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasDueDate,
                        onCheckedChange = { hasDueDate = it },
                        colors = CheckboxDefaults.colors(checkedColor = accentColor)
                    )
                    Text("Set Due Date", fontSize = 12.sp, color = Color.White)
                }
                if (hasDueDate) {
                    OutlinedTextField(
                        value = dueInDaysStr,
                        onValueChange = { dueInDaysStr = it.filter { ch -> ch.isDigit() || ch == '-' } },
                        label = { Text("Days from today") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(135.dp)
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    fontSize = 12.sp,
                    color = MaiExpenseRose,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MaiCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = MaiLavenderSubtext)
                }
                Button(
                    onClick = {
                        val cleanName = name.trim()
                        val amt = amountStr.toLongOrNull() ?: 0L
                        if (cleanName.isEmpty()) {
                            errorText = "Please enter the person's name"
                            return@Button
                        }
                        if (amt <= 0L) {
                            errorText = "Please enter a valid amount greater than ₹0"
                            return@Button
                        }
                        val dueMillis = if (hasDueDate) {
                            val days = dueInDaysStr.toLongOrNull() ?: 7L
                            System.currentTimeMillis() + (days * 86_400_000L)
                        } else null
                        onSave(cleanName, phone, amt, dueMillis, description, selectedCategory, notes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("khata_save_entry_button")
                ) {
                    Text(
                        text = "Save ${direction.label}",
                        color = Color(0xFF070510),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordKhataPaymentDialog(
    contactState: KhataContactComputedState,
    onDismiss: () -> Unit,
    onRecordPayment: (amount: Long, description: String, notes: String) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var paymentAmountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaiCardSurface)
                .border(1.4.dp, MaiCardBorderBright, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Record Payment · ${contactState.contact.name}",
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = "Current Outstanding (${contactState.netDirection?.label ?: "Settled"}): ₹${formatIndianAmount(contactState.outstandingAmount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD8B4FE)
            )

            OutlinedTextField(
                value = paymentAmountStr,
                onValueChange = {
                    paymentAmountStr = it.filter { ch -> ch.isDigit() }
                    errorText = null
                },
                label = { Text("Payment Amount (₹) *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaiCardBorderBright,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("khata_payment_amount_input")
            )

            // Quick Percentage Chips: 25%, 50%, 100% (Full Settlement)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(25 to "25%", 50 to "50%", 100 to "Full (₹${formatIndianAmount(contactState.outstandingAmount)})").forEach { (pct, label) ->
                    Surface(
                        color = MaiCardSurfaceElevated,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, MaiCardBorderBright),
                        modifier = Modifier.clickable {
                            val calc = ((contactState.outstandingAmount * pct) / 100L).coerceAtLeast(1L)
                            paymentAmountStr = calc.toString()
                        }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Payment Description (e.g. UPI Partial Return)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaiCardBorderBright,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    fontSize = 12.sp,
                    color = MaiExpenseRose,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MaiCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = MaiLavenderSubtext)
                }
                Button(
                    onClick = {
                        val amt = paymentAmountStr.toLongOrNull() ?: 0L
                        if (amt <= 0L) {
                            errorText = "Enter a payment amount greater than ₹0"
                            return@Button
                        }
                        if (amt > contactState.outstandingAmount) {
                            errorText = "Payment cannot exceed outstanding ₹${formatIndianAmount(contactState.outstandingAmount)}"
                            return@Button
                        }
                        onRecordPayment(amt, description, notes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaiPrimaryPurpleEnd),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("khata_confirm_payment_button")
                ) {
                    Text("Record Payment", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EditKhataEntryDialog(
    entry: KhataLedgerEntry,
    onDismiss: () -> Unit,
    onSaveEdited: (
        newAmount: Long,
        newDescription: String,
        newCategory: String,
        newDueDateMillis: Long?,
        newNotes: String
    ) -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    var amountStr by remember(entry) { mutableStateOf(entry.amount.toString()) }
    var description by remember(entry) { mutableStateOf(entry.description) }
    var category by remember(entry) { mutableStateOf(entry.category) }
    var notes by remember(entry) { mutableStateOf(entry.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaiCardSurface)
                .border(1.2.dp, MaiCardBorderBright, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Edit Khata Entry",
                fontFamily = headingFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Amount (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaiCardBorderBright,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaiCardBorderBright,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaiCardBorderBright,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MaiCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = MaiLavenderSubtext)
                }
                Button(
                    onClick = {
                        val amt = amountStr.toLongOrNull() ?: 0L
                        if (amt > 0L) {
                            onSaveEdited(amt, description, category, entry.dueDateMillis, notes)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaiPrimaryPurpleEnd),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// SECTION 10. HOME DASHBOARD KHATA SUMMARY INTEGRATION
// ============================================================================
@Composable
fun DashboardKhataSummarySection(
    userId: String,
    legacyFriends: List<KhataFriendRecord>,
    stealthMode: Boolean,
    onOpenKhataScreen: () -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val storeVersion by AdvancedKhataStore.versionFlow.collectAsState()

    LaunchedEffect(userId, legacyFriends) {
        AdvancedKhataStore.syncLegacyFirestoreFriendsIfNeeded(context, userId, legacyFriends)
    }

    val contactStates = remember(userId, storeVersion, legacyFriends) {
        AdvancedKhataStore.getAllContactStates(context, userId)
    }
    val metrics = remember(contactStates) {
        AdvancedKhataStore.computeSummaryMetrics(contactStates)
    }
    val recentAudit = remember(userId, storeVersion) {
        AdvancedKhataStore.loadAuditHistory(context, userId).firstOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("dashboard_khata_summary_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Khata Summary",
                    tint = Color(0xFFD8B4FE),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Khata Udhaar Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${metrics.totalActiveContacts} active people in ledger",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = MaiPrimaryPurpleEnd,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .clickable { onOpenKhataScreen() }
                    .testTag("dashboard_open_khata_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Open Khata",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Khata",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .border(0.8.dp, MaiPositiveEmerald.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "LENA HAI",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiPositiveEmerald
                )
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(metrics.totalLenaHai)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color.White
                )
                Text(
                    text = "${metrics.lenaHaiPeopleCount} people owe you",
                    fontSize = 10.sp,
                    color = MaiLavenderSubtext
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaiCardSurfaceElevated)
                    .border(0.8.dp, MaiExpenseRose.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "DENA HAI",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaiExpenseRose
                )
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(metrics.totalDenaHai)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color.White
                )
                Text(
                    text = "You owe ${metrics.denaHaiPeopleCount} people",
                    fontSize = 10.sp,
                    color = MaiLavenderSubtext
                )
            }
        }

        if (recentAudit != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D081D))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest: ${recentAudit.contactName} · ${recentAudit.action.label}",
                    fontSize = 11.sp,
                    color = MaiLavenderSubtext,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (stealthMode) "₹•••" else "₹${formatIndianAmount(recentAudit.amount)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE)
                )
            }
        }
    }
}

// ============================================================================
// SECTION 11. HOME DASHBOARD RECENT HISTORY INTEGRATION (5 LATEST ACTIVITIES + VIEW ALL)
// ============================================================================
@Composable
fun DashboardUnifiedRecentHistorySection(
    activities: List<UnifiedRecentActivityItem>,
    stealthMode: Boolean,
    onViewAllHistory: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val dateFmt = remember { SimpleDateFormat("dd MMM, h:mm a", Locale.ENGLISH) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("dashboard_recent_history_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Recent History",
                    tint = Color(0xFFD8B4FE),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "RECENT HISTORY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Latest across Income, Expenses, Bills & Khata",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onViewAllHistory() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("dashboard_recent_history_view_all_button")
            ) {
                Text(
                    text = "VIEW ALL",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8B4FE),
                    maxLines = 1,
                    softWrap = false
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View All History",
                    tint = Color(0xFFD8B4FE),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (activities.isEmpty()) {
            Text(
                text = "No recent activity yet. New income, expenses, bill payments, and Khata entries will appear here.",
                fontSize = 12.sp,
                color = MaiLavenderSubtext
            )
        } else {
            activities.take(5).forEach { act ->
                val accent = when (act.kind) {
                    UnifiedActivityKind.INCOME,
                    UnifiedActivityKind.KHATA_LENA_ENTRY,
                    UnifiedActivityKind.KHATA_PAYMENT_RECEIVED -> MaiPositiveEmerald
                    UnifiedActivityKind.KHATA_SETTLEMENT -> Color(0xFF38BDF8)
                    else -> MaiExpenseRose
                }
                val icon = when (act.kind) {
                    UnifiedActivityKind.INCOME -> Icons.Default.TrendingUp
                    UnifiedActivityKind.EXPENSE -> Icons.Default.TrendingDown
                    UnifiedActivityKind.BILL_PAYMENT -> Icons.Default.ReceiptLong
                    UnifiedActivityKind.KHATA_SETTLEMENT -> Icons.Default.CheckCircle
                    else -> Icons.Default.MenuBook
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaiCardSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                .background(accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = act.kind.badgeLabel,
                                tint = accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = act.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${act.kind.badgeLabel} · ${dateFmt.format(Date(act.timestampMillis))}",
                                fontSize = 11.sp,
                                color = MaiLavenderSubtext,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = if (stealthMode) {
                            "₹••••"
                        } else {
                            "${if (act.isPositiveInflow) "+" else "-"}₹${formatIndianAmount(act.amount)}"
                        },
                        fontFamily = headingFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent
                    )
                }
            }
        }
    }
}
