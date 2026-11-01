package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KhataFriendRecord
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
import kotlin.math.abs

@Composable
fun KhataTab(
    friends: List<KhataFriendRecord>,
    myUpiId: String,
    stealthMode: Boolean,
    onUpdateMyUpiId: (String) -> Unit,
    onAddFriend: (name: String, phone: String, upiId: String, amount: Long, description: String) -> Unit,
    onAddEntry: (friend: KhataFriendRecord, deltaAmount: Long, description: String) -> Unit,
    onSettleFriend: (KhataFriendRecord) -> Unit,
    onSplitBill: (selectedFriends: List<KhataFriendRecord>, totalBill: Long, description: String, includeSelf: Boolean) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headingFont = LocalAppFontFamily.current
    var activePanel by remember { mutableStateOf("none") } // "none", "add_friend", "split_bill", "upi_edit"
    var upiDraft by remember(myUpiId) { mutableStateOf(myUpiId) }

    // Add friend form state
    var friendName by remember { mutableStateOf("") }
    var friendPhone by remember { mutableStateOf("") }
    var friendUpi by remember { mutableStateOf("") }
    var friendAmount by remember { mutableStateOf("") }
    var friendDesc by remember { mutableStateOf("") }
    var isYouOweFriend by remember { mutableStateOf(false) }

    // Split bill form state
    var splitTotalStr by remember { mutableStateOf("") }
    var splitDesc by remember { mutableStateOf("") }
    var includeSelfInSplit by remember { mutableStateOf(true) }
    val selectedFriendIds = remember { mutableStateListOf<String>() }

    // Quick entry modal per friend
    var quickEntryFriend by remember { mutableStateOf<KhataFriendRecord?>(null) }
    var quickDeltaStr by remember { mutableStateOf("") }
    var quickDeltaDesc by remember { mutableStateOf("") }

    val totalLenaHai = remember(friends) {
        friends.filter { it.balance > 0L }.sumOf { it.balance }
    }
    val totalDenaHai = remember(friends) {
        friends.filter { it.balance < 0L }.sumOf { abs(it.balance) }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ChampagneGold,
        unfocusedBorderColor = HairlinePlatinum,
        focusedContainerColor = ObsidianBlack,
        unfocusedContainerColor = ObsidianBlack,
        focusedTextColor = WarmIvory,
        unfocusedTextColor = WarmIvory,
        focusedLabelColor = ChampagneGold,
        unfocusedLabelColor = MutedIvory
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("khata_tab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Khata Summary Card (Lena Hai vs Dena Hai)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOVEREIGN KHATA LEDGER",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.4.sp,
                            color = ChampagneGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Peer Settlements & Splits",
                            fontFamily = headingFont,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmIvory
                        )
                    }
                    Surface(
                        color = ElevatedGraphite,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(0.8.dp, HairlineGold, RoundedCornerShape(50))
                            .clickable {
                                activePanel = if (activePanel == "upi_edit") "none" else "upi_edit"
                            }
                            .testTag("configure_my_upi_button")
                    ) {
                        Text(
                            text = if (myUpiId.isNotBlank()) "UPI: $myUpiId ✎" else "Set Receiving UPI ✎",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ChampagneGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElevatedGraphite)
                            .border(0.8.dp, SophisticatedEmerald.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "LENA HAI (RECEIVABLE)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = MutedPlatinum
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stealthMode) "₹0" else "+₹$totalLenaHai",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = SophisticatedEmerald
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElevatedGraphite)
                            .border(0.8.dp, CrimsonRuby.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "DENA HAI (PAYABLE)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = MutedPlatinum
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stealthMode) "₹0" else "-₹$totalDenaHai",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = CrimsonRuby
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { activePanel = if (activePanel == "add_friend") "none" else "add_friend" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("khata_new_friend_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Counterparty", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { activePanel = if (activePanel == "split_bill") "none" else "split_bill" },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.8.dp, HairlineGold),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ElevatedGraphite,
                            contentColor = ChampagneGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("khata_split_bill_button")
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = "Split Bill", tint = ChampagneGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Equal Split", color = ChampagneGold, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                // Receiving UPI ID Editor
                AnimatedVisibility(visible = activePanel == "upi_edit") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElevatedGraphite)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Your Receiving UPI ID (for Instant Settlement Links)",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 13.sp,
                            color = WarmIvory
                        )
                        OutlinedTextField(
                            value = upiDraft,
                            onValueChange = { upiDraft = it },
                            placeholder = { Text("yourname@okaxis", color = MutedIvory) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    onUpdateMyUpiId(upiDraft.trim())
                                    activePanel = "none"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack)
                            ) {
                                Text("Save UPI ID", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Add Friend Panel
                AnimatedVisibility(visible = activePanel == "add_friend") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElevatedGraphite)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add Friend to Khata Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmIvory
                        )
                        OutlinedTextField(
                            value = friendName,
                            onValueChange = { friendName = it },
                            label = { Text("Friend Name") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("khata_friend_name_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = friendPhone,
                                onValueChange = { friendPhone = it },
                                label = { Text("Phone (WhatsApp)") },
                                singleLine = true,
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = friendAmount,
                                onValueChange = { friendAmount = it },
                                label = { Text("Amount (₹)") },
                                singleLine = true,
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("khata_friend_amount_input")
                            )
                        }
                        OutlinedTextField(
                            value = friendDesc,
                            onValueChange = { friendDesc = it },
                            label = { Text("Reason (e.g. Dinner Split)") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = if (!isYouOweFriend) SophisticatedEmerald.copy(alpha = 0.16f) else ObsidianBlack,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.8.dp, if (!isYouOweFriend) SophisticatedEmerald else HairlinePlatinum, RoundedCornerShape(10.dp))
                                    .clickable { isYouOweFriend = false }
                            ) {
                                Text(
                                    text = "Lena Hai (Owes You)",
                                    fontSize = 12.sp,
                                    color = SophisticatedEmerald,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Surface(
                                color = if (isYouOweFriend) CrimsonRuby.copy(alpha = 0.16f) else ObsidianBlack,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.8.dp, if (isYouOweFriend) CrimsonRuby else HairlinePlatinum, RoundedCornerShape(10.dp))
                                    .clickable { isYouOweFriend = true }
                            ) {
                                Text(
                                    text = "Dena Hai (You Owe)",
                                    fontSize = 12.sp,
                                    color = CrimsonRuby,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val parsedAmt = friendAmount.toLongOrNull() ?: 0L
                                if (friendName.isNotBlank() && parsedAmt > 0L) {
                                    val signedAmt = if (isYouOweFriend) -parsedAmt else parsedAmt
                                    onAddFriend(friendName, friendPhone, friendUpi, signedAmt, friendDesc.ifBlank { "Group Split" })
                                    friendName = ""
                                    friendPhone = ""
                                    friendUpi = ""
                                    friendAmount = ""
                                    friendDesc = ""
                                    activePanel = "none"
                                } else {
                                    onShowToast("Enter friend name and valid amount")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                            modifier = Modifier.fillMaxWidth().testTag("khata_save_friend_button")
                        ) {
                            Text("Save to Khata Ledger", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Equal Bill Split Panel
                AnimatedVisibility(visible = activePanel == "split_bill") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElevatedGraphite)
                            .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Equal Group Bill Splitter",
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmIvory
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = splitTotalStr,
                                onValueChange = { splitTotalStr = it },
                                label = { Text("Total Bill (₹)") },
                                singleLine = true,
                                colors = fieldColors,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = splitDesc,
                                onValueChange = { splitDesc = it },
                                label = { Text("Description") },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "Select counterparties to split with:",
                            fontSize = 12.sp,
                            color = MutedPlatinum
                        )
                        friends.forEach { fr ->
                            val isSelected = fr.id in selectedFriendIds
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedFriendIds.remove(fr.id) else selectedFriendIds.add(fr.id)
                                    }
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedFriendIds.add(fr.id) else selectedFriendIds.remove(fr.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ChampagneGold,
                                        checkmarkColor = ObsidianBlack
                                    )
                                )
                                Text(text = fr.name, color = WarmIvory, fontSize = 13.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = includeSelfInSplit,
                                onCheckedChange = { includeSelfInSplit = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ChampagneGold,
                                    checkmarkColor = ObsidianBlack
                                )
                            )
                            Text("Include my own share in divisor", color = MutedPlatinum, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val total = splitTotalStr.toLongOrNull() ?: 0L
                                val chosen = friends.filter { it.id in selectedFriendIds }
                                if (total > 0L && chosen.isNotEmpty()) {
                                    onSplitBill(chosen, total, splitDesc.ifBlank { "Group Bill" }, includeSelfInSplit)
                                    splitTotalStr = ""
                                    splitDesc = ""
                                    selectedFriendIds.clear()
                                    activePanel = "none"
                                } else {
                                    onShowToast("Enter total bill and select at least 1 friend")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Split Equally Now", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Quick Add Entry Panel for a chosen friend
        if (quickEntryFriend != null) {
            item {
                val target = quickEntryFriend!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RichGraphite)
                        .border(1.dp, ChampagneGold, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Record Entry for ${target.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmIvory
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quickDeltaStr,
                            onValueChange = { quickDeltaStr = it },
                            label = { Text("Amount (₹)") },
                            singleLine = true,
                            colors = fieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = quickDeltaDesc,
                            onValueChange = { quickDeltaDesc = it },
                            label = { Text("Note") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val amt = quickDeltaStr.toLongOrNull() ?: 0L
                                if (amt > 0L) {
                                    onAddEntry(target, amt, quickDeltaDesc.ifBlank { "Split added" })
                                    quickEntryFriend = null
                                    quickDeltaStr = ""
                                    quickDeltaDesc = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SophisticatedEmerald, contentColor = ObsidianBlack),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Lena (Owes You)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val amt = quickDeltaStr.toLongOrNull() ?: 0L
                                if (amt > 0L) {
                                    onAddEntry(target, -amt, quickDeltaDesc.ifBlank { "Borrowed" })
                                    quickEntryFriend = null
                                    quickDeltaStr = ""
                                    quickDeltaDesc = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRuby, contentColor = WarmIvory),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("- Dena (You Owe)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Friends List
        if (friends.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                        .padding(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No counterparties in your Khata ledger yet. Tap 'Add Counterparty' above.",
                        color = MutedIvory,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(friends, key = { it.id.ifEmpty { it.name } }) { friend ->
                val isPositive = friend.balance > 0L
                val isNegative = friend.balance < 0L
                val balColor = when {
                    isPositive -> SophisticatedEmerald
                    isNegative -> CrimsonRuby
                    else -> MutedPlatinum
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(RichGraphite)
                        .border(0.8.dp, HairlinePlatinum, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ElevatedGraphite)
                                .border(0.8.dp, HairlineGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.avatarInitials,
                                fontFamily = headingFont,
                                fontWeight = FontWeight.ExtraBold,
                                color = ChampagneGold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = WarmIvory,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = friend.lastDescription.ifBlank { "No recent split note" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedIvory,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when {
                                    stealthMode -> "₹•••"
                                    isPositive -> "+₹${friend.balance}"
                                    isNegative -> "-₹${abs(friend.balance)}"
                                    else -> "Settled"
                                },
                                fontFamily = headingFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = balColor
                            )
                            Text(
                                text = when {
                                    isPositive -> "LENA HAI"
                                    isNegative -> "DENA HAI"
                                    else -> "ALL CLEAR"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.8.sp,
                                color = balColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons Row for Friend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                quickEntryFriend = if (quickEntryFriend?.id == friend.id) null else friend
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, HairlinePlatinum),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Split", tint = MutedPlatinum, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Split", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = WarmIvory)
                        }

                        if (friend.balance != 0L) {
                            OutlinedButton(
                                onClick = { onSettleFriend(friend) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, SophisticatedEmerald.copy(alpha = 0.45f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Settle Up",
                                    tint = SophisticatedEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Settle", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SophisticatedEmerald)
                            }
                        }

                        if (isPositive) {
                            OutlinedButton(
                                onClick = {
                                    val upiLink = if (myUpiId.isNotBlank()) "upi://pay?pa=$myUpiId&am=${friend.balance}&cu=INR" else ""
                                    val msg = "◆ Hey ${friend.name}! Friendly Kharchee reminder: ₹${friend.balance} is pending for '${friend.lastDescription}'. ${if (upiLink.isNotEmpty()) "Pay via UPI: $upiLink" else ""}"
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, msg)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Send Khata Reminder"))
                                    } catch (e: Exception) {
                                        onShowToast("Reminder copied: $msg")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, HairlineGold),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Remind Friend",
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remind", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ChampagneGold)
                            }
                        }
                    }
                }
            }
        }
    }
}
