package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ALL_CATEGORIES
import com.example.data.model.ExpenseRecord
import com.example.data.model.PAYMENT_MODES
import com.example.data.model.isIncomeTransaction
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRuby
import com.example.ui.theme.ElevatedGraphite
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

@Composable
fun HistoryTab(
    expenses: List<ExpenseRecord>,
    stealthMode: Boolean,
    onDeleteExpense: (ExpenseRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val headingFont = LocalAppFontFamily.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") } // "All", "expense", "income"
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedMode by remember { mutableStateOf("All") }

    val filtered = remember(expenses, searchQuery, selectedType, selectedCategory, selectedMode) {
        expenses.filter { tx ->
            val matchesQuery = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true)
            val isInc = isIncomeTransaction(tx)
            val matchesType = when (selectedType) {
                "expense" -> !isInc
                "income" -> isInc
                else -> true
            }
            val matchesCat = selectedCategory == "All" || tx.category == selectedCategory
            val matchesMode = selectedMode == "All" || tx.paymentMode == selectedMode
            matchesQuery && matchesType && matchesCat && matchesMode
        }
    }

    val totalFilteredExpense = remember(filtered) {
        filtered.filter { !isIncomeTransaction(it) }.sumOf { it.amount }
    }
    val totalFilteredIncome = remember(filtered) {
        filtered.filter { isIncomeTransaction(it) }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("history_tab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(RichGraphite)
                    .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = "PRIVATE LEDGER PASSBOOK",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.4.sp,
                    color = ChampagneGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Audit & Filter Transactions",
                    fontFamily = headingFont,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmIvory
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search merchant, category, or note...",
                            color = MutedIvory,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = ChampagneGold)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MutedPlatinum)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChampagneGold,
                        unfocusedBorderColor = HairlinePlatinum,
                        focusedContainerColor = ElevatedGraphite,
                        unfocusedContainerColor = ElevatedGraphite,
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type & Mode Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipButton("All Types", selectedType == "All") { selectedType = "All" }
                    FilterChipButton("Expenses Only", selectedType == "expense") { selectedType = "expense" }
                    FilterChipButton("Income Only", selectedType == "income") { selectedType = "income" }
                    PAYMENT_MODES.forEach { mode ->
                        FilterChipButton(mode, selectedMode == mode) {
                            selectedMode = if (selectedMode == mode) "All" else mode
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipButton("All Categories", selectedCategory == "All") { selectedCategory = "All" }
                    ALL_CATEGORIES.forEach { cat ->
                        FilterChipButton(cat, selectedCategory == cat) {
                            selectedCategory = if (selectedCategory == cat) "All" else cat
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = HairlinePlatinum, thickness = 0.6.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Summary Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "FILTERED OUTFLOW",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = MutedIvory
                        )
                        Text(
                            text = if (stealthMode) "Outflow: ₹•••" else "Outflow: -₹$totalFilteredExpense",
                            fontFamily = headingFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CrimsonRuby
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "FILTERED INFLOW",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = MutedIvory
                        )
                        Text(
                            text = if (stealthMode) "Inflow: ₹•••" else "Inflow: +₹$totalFilteredIncome",
                            fontFamily = headingFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SophisticatedEmerald
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching ledger entries found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedIvory
                    )
                }
            }
        } else {
            items(filtered, key = { it.id.ifEmpty { it.title + it.txEpochMillis } }) { tx ->
                TransactionRowCard(
                    expense = tx,
                    stealthMode = stealthMode,
                    onDelete = { onDeleteExpense(tx) }
                )
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) ChampagneGold.copy(alpha = 0.16f) else ElevatedGraphite,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .border(
                width = 0.8.dp,
                color = if (selected) HairlineGold else HairlinePlatinum,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) ChampagneGold else MutedPlatinum,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
        )
    }
}
