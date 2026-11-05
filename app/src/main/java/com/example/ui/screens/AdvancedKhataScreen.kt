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
import com.example.ui.components.KharcheeCompactHeaderBranding
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAppFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

/**
 * Production-quality KHATA (Personal Lending & Borrowing / Digital Bahi-Khata) Screen.
 * Built with KHARCHEE's Dark Navy + Purple/Violet visual identity.
 */
@Composable
fun AdvancedKhataScreen(
    userId: String,
    legacyFriends: List<KhataFriendRecord> = emptyList(),
    stealthMode: Boolean = false,
    onShowToast: (String) -> Unit = {},
    onOpenVaultTools: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val storeVersion by AdvancedKhataStore.versionFlow.collectAsState()
    var localRefreshTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(userId, legacyFriends) {
        AdvancedKhataStore.syncLegacyFirestoreFriendsIfNeeded(context, userId, legacyFriends)
        localRefreshTick++
    }

    val allContactStates = remember(userId, storeVersion, localRefreshTick) {
        AdvancedKhataStore.getAllContactStates(context, userId)
    }
    val summaryMetrics = remember(allContactStates) {
        AdvancedKhataStore.computeSummaryMetrics(allContactStates)
    }
    val auditHistory = remember(userId, storeVersion, localRefreshTick) {
        AdvancedKhataStore.loadAuditHistory(context, userId)
    }
    val contactStateMap = remember(allContactStates) {
        allContactStates.associateBy { it.contact.id }
    }

    // Sub-navigation: "overview" (Contacts & Summary), "history" (Full Khata History), or individual contactId
    var activeSubTab by remember { mutableStateOf("overview") } // "overview" or "history"
    var selectedContactId by remember { mutableStateOf<String?>(null) }

    // Search, Filter & Sort state for Contacts
    var contactSearchQuery by remember { mutableStateOf("") }
    var selectedContactFilter by remember { mutableStateOf(KhataContactFilter.ALL) }
    var selectedContactSort by remember { mutableStateOf(KhataContactSort.LATEST_ACTIVITY) }

    // Search & Filter state for Khata History
    var historySearchQuery by remember { mutableStateOf("") }
    var selectedHistoryFilter by remember { mutableStateOf(KhataHistoryFilter.ALL) }
    var customRangeDaysBack by remember { mutableStateOf("30") }

    // Modals state
    var activeAddEntryDirection by remember { mutableStateOf<KhataDirection?>(null) }
    var prefilledContactNameForAdd by remember { mutableStateOf("") }
    var prefilledContactPhoneForAdd by remember { mutableStateOf("") }

    var paymentModalContactState by remember { mutableStateOf<KhataContactComputedState?>(null) }
    var editingEntry by remember { mutableStateOf<KhataLedgerEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<KhataLedgerEntry?>(null) }

    val selectedContactState = remember(selectedContactId, allContactStates) {
        selectedContactId?.let { id -> allContactStates.find { it.contact.id == id } }
    }

    BackHandler(enabled = selectedContactId != null || activeSubTab == "history") {
        if (selectedContactId != null) {
            selectedContactId = null
        } else {
            activeSubTab = "overview"
        }
    }

    if (selectedContactState != null) {
        // --- SECTION 5. INDIVIDUAL PERSON LEDGER ---
        KhataIndividualPersonLedgerView(
            state = selectedContactState,
            stealthMode = stealthMode,
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
            onToggleReminder = { enabled ->
                AdvancedKhataStore.toggleContactReminder(
                    context = context,
                    userId = userId,
                    contactId = selectedContactState.contact.id,
                    enabled = enabled
                )
                localRefreshTick++
                onShowToast(if (enabled) "🔔 Due reminders enabled for ${selectedContactState.contact.name}" else "🔕 Reminders muted")
            }
        )
    } else {
        // --- SECTION 2, 3, 4 & 8: KHATA OVERVIEW & KHATA HISTORY ---
        val filteredContacts = remember(allContactStates, contactSearchQuery, selectedContactFilter, selectedContactSort) {
            AdvancedKhataStore.filterAndSortContacts(
                states = allContactStates,
                searchQuery = contactSearchQuery,
                filter = selectedContactFilter,
                sort = selectedContactSort
            )
        }

        val filteredHistory = remember(auditHistory, contactStateMap, historySearchQuery, selectedHistoryFilter, customRangeDaysBack) {
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
                endDateMillis = now
            )
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaiDeepCanvas)
                .testTag("khata_tab_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Branding & Mode Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        KharcheeCompactHeaderBranding(sectionLabel = "KHATA")
                        Text(
                            text = "Digital Bahi-Khata Ledger",
                            fontFamily = headingFont,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = if (activeSubTab == "overview") MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (activeSubTab == "overview") Color(0xFFD8B4FE) else MaiCardBorder),
                            modifier = Modifier
                                .clickable { activeSubTab = "overview" }
                                .testTag("khata_subtab_contacts")
                        ) {
                            Text(
                                text = "Contacts (${allContactStates.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            color = if (activeSubTab == "history") MaiPrimaryPurpleEnd else MaiCardSurfaceElevated,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (activeSubTab == "history") Color(0xFFD8B4FE) else MaiCardBorder),
                            modifier = Modifier
                                .clickable { activeSubTab = "history" }
                                .testTag("khata_subtab_history")
                        ) {
                            Text(
                                text = "History (${auditHistory.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        if (onOpenVaultTools != null) {
                            Surface(
                                color = MaiCardSurfaceElevated,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, MaiCardBorder),
                                modifier = Modifier
                                    .clickable { onOpenVaultTools() }
                                    .testTag("khata_open_vault_tools_chip")
                            ) {
                                Text(
                                    text = "Goals",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaiLavenderSubtext,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 2. KHATA SUMMARY (LENA HAI & DENA HAI CARDS + NET BALANCE + ACTIVE CONTACTS + OVERDUE) ---
            item {
                KhataSummaryHeroSection(
                    metrics = summaryMetrics,
                    stealthMode = stealthMode
                )
            }

            // --- SECTION 3. TWO PRIMARY ACTIONS (+ ADD LENA HAI & + ADD DENA HAI) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            prefilledContactNameForAdd = ""
                            prefilledContactPhoneForAdd = ""
                            activeAddEntryDirection = KhataDirection.LENA_HAI
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F1244),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.2.dp, MaiPositiveEmerald.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
                            .testTag("khata_add_lena_hai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallReceived,
                            contentDescription = "Add Lena Hai",
                            tint = MaiPositiveEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ Add Lena Hai",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            prefilledContactNameForAdd = ""
                            prefilledContactPhoneForAdd = ""
                            activeAddEntryDirection = KhataDirection.DENA_HAI
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF241136),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.2.dp, MaiExpenseRose.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
                            .testTag("khata_add_dena_hai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallMade,
                            contentDescription = "Add Dena Hai",
                            tint = MaiExpenseRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ Add Dena Hai",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (activeSubTab == "overview") {
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
                            title = if (allContactStates.isEmpty()) "Your Khata Ledger is Clean" else "No matching contacts found",
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
                    items(filteredContacts, key = { it.contact.id }) { contactState ->
                        KhataContactCardItem(
                            state = contactState,
                            stealthMode = stealthMode,
                            onClick = { selectedContactId = contactState.contact.id },
                            onQuickPayment = { paymentModalContactState = contactState }
                        )
                    }
                }
            } else {
                // --- SECTION 8. COMPLETE KHATA HISTORY ---
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
                            subtitle = "All original udhaar entries, partial payments, full settlements, and edits will appear here chronologically.",
                            onAddLenaHai = {
                                prefilledContactNameForAdd = ""
                                prefilledContactPhoneForAdd = ""
                                activeAddEntryDirection = KhataDirection.LENA_HAI
                            }
                        )
                    }
                } else {
                    items(filteredHistory, key = { it.id }) { auditItem ->
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
                    .background(MaiCardSurface)
                    .border(1.2.dp, MaiExpenseRose, RoundedCornerShape(22.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Delete Khata Entry?",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    text = "Are you sure you want to delete this ₹${formatIndianAmount(currentDeleting.amount)} entry (${currentDeleting.description}) for ${currentDeleting.contactName}? Balances will be recalculated and an audit record will be kept.",
                    fontSize = 13.sp,
                    color = MaiLavenderSubtext,
                    lineHeight = 19.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { deletingEntry = null },
                        border = BorderStroke(1.dp, MaiCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = MaiLavenderSubtext)
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaiExpenseRose),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_delete_khata_entry_button")
                    ) {
                        Text("Delete Entry", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KhataSummaryHeroSection(
    metrics: KhataSummaryMetrics,
    stealthMode: Boolean
) {
    val headingFont = LocalAppFontFamily.current
    val isNetReceivable = metrics.netOutstandingBalance >= 0L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF6B3DF0))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF261556),
                        Color(0xFF150C33),
                        Color(0xFF0C071E)
                    )
                )
            )
            .border(1.2.dp, Color(0xFF8B5CF6).copy(alpha = 0.65f), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("khata_summary_hero_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Two Prominent Summary Cards: A. LENA HAI & B. DENA HAI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card A: LENA HAI (Receivable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF120B29))
                    .border(1.dp, MaiPositiveEmerald.copy(alpha = 0.50f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .testTag("khata_summary_lena_hai_card"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallReceived,
                        contentDescription = "Lena Hai",
                        tint = MaiPositiveEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "LENA HAI",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = MaiPositiveEmerald
                    )
                }
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(metrics.totalLenaHai)}",
                    fontFamily = headingFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${metrics.lenaHaiPeopleCount} ${if (metrics.lenaHaiPeopleCount == 1) "person owes" else "people owe"} you",
                    fontSize = 11.sp,
                    color = MaiLavenderSubtext
                )
            }

            // Card B: DENA HAI (Payable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF120B29))
                    .border(1.dp, MaiExpenseRose.copy(alpha = 0.50f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .testTag("khata_summary_dena_hai_card"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallMade,
                        contentDescription = "Dena Hai",
                        tint = MaiExpenseRose,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "DENA HAI",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = MaiExpenseRose
                    )
                }
                Text(
                    text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(metrics.totalDenaHai)}",
                    fontFamily = headingFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "You owe ${metrics.denaHaiPeopleCount} ${if (metrics.denaHaiPeopleCount == 1) "person" else "people"}",
                    fontSize = 11.sp,
                    color = MaiLavenderSubtext
                )
            }
        }

        // Bottom Telemetry Row: Net Outstanding Balance | Active Contacts | Overdue Amount
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0E0822))
                .border(0.8.dp, MaiCardBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NET KHATA BALANCE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) {
                        "₹••••"
                    } else {
                        "${if (isNetReceivable) "+" else "-"}₹${formatIndianAmount(metrics.netOutstandingBalance)}"
                    },
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (isNetReceivable) MaiPositiveEmerald else MaiExpenseRose
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ACTIVE CONTACTS",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = "${metrics.totalActiveContacts} active · ${metrics.totalSettledContacts} settled",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFD8B4FE)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "OVERDUE",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    color = MaiLavenderSubtext
                )
                Text(
                    text = if (stealthMode) "₹•••" else "₹${formatIndianAmount(metrics.overdueAmount)}",
                    fontFamily = headingFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (metrics.overdueAmount > 0L) MaiWarningAmber else MaiLavenderSubtext
                )
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text("Search person by name or phone...", color = MaiLavenderSubtext, fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFD8B4FE))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaiLavenderSubtext)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaiCardBorderBright,
                unfocusedBorderColor = MaiCardBorder,
                focusedContainerColor = MaiCardSurface,
                unfocusedContainerColor = MaiCardSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KhataContactFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                Surface(
                    color = if (selected) MaiPrimaryPurpleEnd else MaiCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        0.9.dp,
                        if (selected) Color(0xFFD8B4FE) else MaiCardBorder
                    ),
                    modifier = Modifier
                        .clickable { onSelectFilter(filter) }
                        .testTag("khata_filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else MaiLavenderSubtext,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Sort Row: Latest Activity, Amount, Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort",
                    tint = MaiLavenderSubtext,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Sort by:",
                    fontSize = 11.sp,
                    color = MaiLavenderSubtext
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KhataContactSort.entries.forEach { sortMode ->
                    val selected = selectedSort == sortMode
                    Surface(
                        color = if (selected) Color(0xFF28194D) else Color.Transparent,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            0.8.dp,
                            if (selected) Color(0xFFA855F7) else MaiCardBorder
                        ),
                        modifier = Modifier.clickable { onSelectSort(sortMode) }
                    ) {
                        Text(
                            text = sortMode.label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color(0xFFD8B4FE) else MaiLavenderSubtext,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
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
    onQuickPayment: () -> Unit
) {
    val headingFont = LocalAppFontFamily.current
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }
    val lastDateStr = remember(state.lastTransactionMillis) {
        dateFmt.format(Date(state.lastTransactionMillis))
    }

    val badgeColor = when {
        state.isSettled -> Color(0xFFA78BFA)
        state.netDirection == KhataDirection.LENA_HAI -> MaiPositiveEmerald
        else -> MaiExpenseRose
    }
    val badgeText = when {
        state.isSettled -> "SETTLED"
        state.netDirection == KhataDirection.LENA_HAI -> "LENA HAI"
        else -> "DENA HAI"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaiCardSurface)
            .border(1.dp, if (state.isOverdue) MaiWarningAmber.copy(alpha = 0.7f) else MaiCardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(15.dp)
            .testTag("khata_contact_card_${state.contact.name}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Avatar Initials Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF4C1D95), Color(0xFF1E103A))
                            )
                        )
                        .border(1.2.dp, badgeColor.copy(alpha = 0.75f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.contact.avatarInitials,
                        fontFamily = headingFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = state.contact.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.isOverdue) {
                            Surface(
                                color = MaiWarningAmber.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "OVERDUE",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaiWarningAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (state.isUpcomingDue) {
                            Surface(
                                color = Color(0xFF38BDF8).copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "DUE SOON",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Last activity: $lastDateStr",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = badgeColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = badgeText,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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
                    color = badgeColor
                )
            }
        }

        if (!state.isSettled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recorded: ₹${formatIndianAmount(state.totalOriginalRecordedForActiveDirection)} · Paid: ₹${formatIndianAmount(state.totalPaidOrReceivedForActiveDirection)}",
                    fontSize = 11.sp,
                    color = MaiLavenderSubtext
                )
                Surface(
                    color = MaiCardSurfaceElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.8.dp, MaiCardBorderBright),
                    modifier = Modifier
                        .clickable { onQuickPayment() }
                        .testTag("khata_quick_payment_${state.contact.name}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Record Payment",
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Record Payment",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }
            }
        }
    }
}

/**
 * SECTION 5. INDIVIDUAL PERSON LEDGER
 */
@Composable
private fun KhataIndividualPersonLedgerView(
    state: KhataContactComputedState,
    stealthMode: Boolean,
    onBack: () -> Unit,
    onAddLenaHai: () -> Unit,
    onAddDenaHai: () -> Unit,
    onOpenRecordPayment: () -> Unit,
    onSettleInFull: () -> Unit,
    onEditEntry: (KhataLedgerEntry) -> Unit,
    onRequestDeleteEntry: (KhataLedgerEntry) -> Unit,
    onToggleReminder: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.ENGLISH) }
    val dueFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    val badgeColor = when {
        state.isSettled -> Color(0xFFA78BFA)
        state.netDirection == KhataDirection.LENA_HAI -> MaiPositiveEmerald
        else -> MaiExpenseRose
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaiDeepCanvas)
            .testTag("khata_individual_ledger_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header with Back button, Name, Phone & Reminder actions
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaiCardSurfaceElevated)
                            .border(1.dp, MaiCardBorder, CircleShape)
                            .testTag("khata_person_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = state.contact.name,
                            fontFamily = headingFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = state.contact.phone.ifBlank { "Personal Khata Ledger" },
                            fontSize = 12.sp,
                            color = MaiLavenderSubtext
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onToggleReminder(!state.contact.reminderEnabled) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaiCardSurfaceElevated)
                            .border(1.dp, MaiCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (state.contact.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Toggle Due Reminders",
                            tint = if (state.contact.reminderEnabled) Color(0xFFD8B4FE) else MaiMutedViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!state.isSettled) {
                        IconButton(
                            onClick = {
                                val msg = if (state.netDirection == KhataDirection.LENA_HAI) {
                                    "Hi ${state.contact.name}, gentle reminder from KHARCHEE Khata: ₹${formatIndianAmount(state.outstandingAmount)} is pending (Lena Hai)."
                                } else {
                                    "Hi ${state.contact.name}, KHARCHEE Khata summary: ₹${formatIndianAmount(state.outstandingAmount)} is scheduled for settlement."
                                }
                                try {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Send Khata Reminder"))
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaiCardSurfaceElevated)
                                .border(1.dp, MaiCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Khata Statement",
                                tint = Color(0xFFD8B4FE),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Person Summary Card: Outstanding Balance, Original Recorded, Received/Paid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF23134F), MaiCardSurface)
                        )
                    )
                    .border(1.2.dp, badgeColor.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                    .padding(18.dp),
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
                                state.isSettled -> "CURRENT STATUS · SETTLED"
                                state.netDirection == KhataDirection.LENA_HAI -> "OUTSTANDING · LENA HAI (RECEIVABLE)"
                                else -> "OUTSTANDING · DENA HAI (PAYABLE)"
                            },
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(state.outstandingAmount)}",
                            fontFamily = headingFont,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    if (state.isSettled) {
                        Surface(
                            color = MaiPositiveEmerald.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaiPositiveEmerald)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Settled",
                                    tint = MaiPositiveEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Settled",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaiPositiveEmerald
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF120B26))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ORIGINALLY RECORDED",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            color = MaiLavenderSubtext
                        )
                        Text(
                            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(state.totalOriginalLenaHai + state.totalOriginalDenaHai)}",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF120B26))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "RECEIVED / PAID",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 8.sp,
                            color = MaiLavenderSubtext
                        )
                        Text(
                            text = if (stealthMode) "₹••••" else "₹${formatIndianAmount(state.totalPaymentsReceived + state.totalPaymentsMade)}",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaiPositiveEmerald
                        )
                    }
                }

                // Action Buttons inside Person Ledger: Record Payment, Mark Settled, + Lena Hai, + Dena Hai
                if (!state.isSettled) {
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
                            border = BorderStroke(1.dp, MaiPositiveEmerald),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("person_ledger_settle_full_button")
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaiPositiveEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Settled", color = MaiPositiveEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                        border = BorderStroke(0.9.dp, MaiPositiveEmerald.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Lena Hai", color = MaiPositiveEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onAddDenaHai,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.9.dp, MaiExpenseRose.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Dena Hai", color = MaiExpenseRose, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section Header: Transaction Timeline
        item {
            Text(
                text = "Transaction Timeline (${state.activeEntries.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(state.activeEntries, key = { it.id }) { entry ->
            val isPayment = entry.recordType == KhataRecordType.PARTIAL_PAYMENT ||
                entry.recordType == KhataRecordType.FULL_SETTLEMENT
            val entryAccent = when {
                isPayment -> Color(0xFF38BDF8)
                entry.direction == KhataDirection.LENA_HAI -> MaiPositiveEmerald
                else -> MaiExpenseRose
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaiCardSurface)
                    .border(1.dp, MaiCardBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
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
                            color = entryAccent.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${entry.direction.label.uppercase()} · ${entry.recordType.label.uppercase()}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = entryAccent,
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
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = entryAccent
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${entry.category} · ${dateFmt.format(Date(entry.txDateMillis))}",
                            fontSize = 11.sp,
                            color = MaiMutedViolet
                        )
                        if (entry.dueDateMillis != null) {
                            Text(
                                text = "Due Date: ${dueFmt.format(Date(entry.dueDateMillis))}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD8B4FE)
                            )
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
                                tint = MaiExpenseRose,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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
                Text("Search Khata history by person or description...", color = MaiLavenderSubtext, fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search History", tint = Color(0xFFD8B4FE))
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaiCardBorderBright,
                unfocusedBorderColor = MaiCardBorder,
                focusedContainerColor = MaiCardSurface,
                unfocusedContainerColor = MaiCardSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
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
                    color = if (selected) MaiPrimaryPurpleEnd else MaiCardSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.9.dp, if (selected) Color(0xFFD8B4FE) else MaiCardBorder),
                    modifier = Modifier.clickable { onSelectFilter(filter) }
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else MaiLavenderSubtext,
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
                    focusedBorderColor = MaiCardBorderBright,
                    unfocusedBorderColor = MaiCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
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
        item.action == KhataAuditAction.DELETED_ENTRY -> MaiMutedViolet
        item.recordType == KhataRecordType.PARTIAL_PAYMENT || item.recordType == KhataRecordType.FULL_SETTLEMENT -> Color(0xFF38BDF8)
        item.direction == KhataDirection.LENA_HAI -> MaiPositiveEmerald
        else -> MaiExpenseRose
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(18.dp))
            .clickable { onClickContact() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                color = Color.White
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
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Surface(
                color = accent.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50)
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
            color = MaiLavenderSubtext
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFmt.format(Date(item.timestampMillis)),
                fontSize = 11.sp,
                color = MaiMutedViolet
            )
            Text(
                text = if (stealthMode) "Bal: ₹•••" else "Balance After: ₹${formatIndianAmount(item.remainingBalanceAfter)}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = Color(0xFFD8B4FE)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaiCardSurface)
            .border(1.dp, MaiCardBorder, RoundedCornerShape(22.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            tint = Color(0xFFD8B4FE),
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaiLavenderSubtext,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Button(
            onClick = onAddLenaHai,
            colors = ButtonDefaults.buttonColors(containerColor = MaiPrimaryPurpleEnd),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("+ Add First Khata Entry", color = Color.White, fontWeight = FontWeight.Bold)
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
                    label = { Text("Phone (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        color = Color.White
                    )
                    Text(
                        text = "${metrics.totalActiveContacts} active people in ledger",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
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
                        color = Color.White
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        color = Color.White
                    )
                    Text(
                        text = "Latest across Income, Expenses, Bills & Khata",
                        fontSize = 11.sp,
                        color = MaiLavenderSubtext
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
                    color = Color(0xFFD8B4FE)
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
