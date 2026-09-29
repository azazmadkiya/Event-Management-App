package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Send
import com.example.data.model.InviteChannel
import com.example.data.model.DietaryPreference
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.PersonType
import com.example.data.model.RsvpStatus
import com.example.ui.components.ImportPhoneContactsDialog
import com.example.ui.theme.Amber50
import com.example.ui.theme.Indigo50
import com.example.ui.theme.StatusAttending
import com.example.ui.theme.StatusDeclined
import com.example.ui.theme.StatusPending
import com.example.util.ContactActionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuestListScreen(
    currentEvent: EventEntity?,
    guests: List<GuestEntity>,
    allRawGuests: List<GuestEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedRsvpTab: String,
    onRsvpTabChange: (String) -> Unit,
    selectedDietaryFilter: String? = null,
    onDietaryFilterChange: (String?) -> Unit = {},
    selectedInviteChannelFilter: String? = null,
    onInviteChannelFilterChange: (String?) -> Unit = {},
    isSecurityLocked: Boolean,
    onUnlockRequest: () -> Unit,
    onAddGuestClick: () -> Unit,
    onEditGuestClick: (GuestEntity) -> Unit,
    onDeleteGuestClick: (GuestEntity) -> Unit,
    onToggleCheckIn: (GuestEntity) -> Unit,
    onUpdateRsvpStatus: (String, RsvpStatus) -> Unit,
    onUpdateInviteChannel: (String, InviteChannel) -> Unit = { _, _ -> },
    onBackupGuests: (Uri) -> Unit,
    onRestoreGuests: (Uri) -> Unit,
    onImportPhoneContacts: (List<GuestEntity>) -> Unit
) {
    val context = LocalContext.current
    var guestToDelete by remember { mutableStateOf<GuestEntity?>(null) }
    var guestForDetailView by remember { mutableStateOf<GuestEntity?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showPhoneContactsDialog by remember { mutableStateOf(false) }
    val selectedGuestIds = remember { mutableStateOf(setOf<String>()) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onBackupGuests(uri)
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onRestoreGuests(uri)
        }
    }

    val tabs = listOf(
        "ALL" to "All (${allRawGuests.size})",
        "ATTENDING" to "Attending OK (${allRawGuests.count { it.rsvpStatus == RsvpStatus.ATTENDING.name }})",
        "PENDING" to "Pending (${allRawGuests.count { it.rsvpStatus == RsvpStatus.PENDING.name }})",
        "DECLINED" to "Declined (${allRawGuests.count { it.rsvpStatus == RsvpStatus.DECLINED.name }})",
        "NOT_INVITED" to "Draft (${allRawGuests.count { it.rsvpStatus == RsvpStatus.NOT_INVITED.name }})",
        "CHECKLIST" to "Checklist (${allRawGuests.count { it.isCheckedIn }}/${allRawGuests.count { it.rsvpStatus == RsvpStatus.ATTENDING.name }})"
    )

    val currentTabIndex = tabs.indexOfFirst { it.first == selectedRsvpTab }.coerceAtLeast(0)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Backup & Restore & Contacts & Broadcast Action Buttons Row (Smooth Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { backupLauncher.launch("guest_backup_${System.currentTimeMillis()}.json") },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("backup_guests_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Backup", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { restoreLauncher.launch("application/json") },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("restore_guests_btn")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { showPhoneContactsDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("import_phone_contacts_btn")
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contacts", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = { showBroadcastDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("broadcast_msg_btn")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Msg", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by name, phone, email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_search_bar")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Tabs (Smooth Scrollable Tabs)
            ScrollableTabRow(
                selectedTabIndex = currentTabIndex,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_status_tabs")
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = currentTabIndex == index,
                        onClick = { onRsvpTabChange(tab.first) },
                        text = {
                            Text(
                                text = tab.second,
                                fontWeight = if (currentTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Invitation Option Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedInviteChannelFilter == null,
                        onClick = { onInviteChannelFilterChange(null) },
                        label = { Text("All Invites") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                items(InviteChannel.entries.toList()) { channel ->
                    FilterChip(
                        selected = selectedInviteChannelFilter == channel.name,
                        onClick = {
                            if (selectedInviteChannelFilter == channel.name) onInviteChannelFilterChange(null)
                            else onInviteChannelFilterChange(channel.name)
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (channel == InviteChannel.WHATSAPP_ONLY) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(channel.displayName)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Security Lock Banner if locked
            if (isSecurityLocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUnlockRequest() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest Contact Data is Protected",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Tap here to authenticate with Biometrics or PIN to view phone numbers & addresses.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guest Count & Select All Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedGuestIds.value.size == guests.size && guests.isNotEmpty(),
                        onCheckedChange = { checked ->
                            if (checked) {
                                selectedGuestIds.value = guests.map { it.id }.toSet()
                            } else {
                                selectedGuestIds.value = emptySet()
                            }
                        },
                        modifier = Modifier.testTag("select_all_guests_checkbox")
                    )
                    Text(
                        text = "Select All (${selectedGuestIds.value.size}/${guests.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedGuestIds.value.isNotEmpty()) {
                    Button(
                        onClick = { showBroadcastDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("whatsapp_selected_btn")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp (${selectedGuestIds.value.size})", style = MaterialTheme.typography.labelMedium)
                    }
                } else if (selectedRsvpTab == "CHECKLIST") {
                    Text(
                        text = "Day-of Arrival Checklist",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Guest List
            if (guests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No invitees found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap '+' below to add your first guest",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("guest_lazy_column"),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(guests, key = { it.id }) { guest ->
                        GuestItemCard(
                            guest = guest,
                            isSecurityLocked = isSecurityLocked,
                            isChecklistMode = selectedRsvpTab == "CHECKLIST",
                            onCardClick = { guestForDetailView = guest },
                            onToggleCheckIn = { onToggleCheckIn(guest) },
                            onEditClick = { onEditGuestClick(guest) },
                            onDeleteClick = { guestToDelete = guest },
                            onStatusChange = { newStatus -> onUpdateRsvpStatus(guest.id, newStatus) },
                            onUpdateInviteChannel = { channel -> onUpdateInviteChannel(guest.id, channel) },
                            eventTitle = currentEvent?.title ?: "Event",
                            isSelected = selectedGuestIds.value.contains(guest.id),
                            onSelectedChange = { selected ->
                                val current = selectedGuestIds.value.toMutableSet()
                                if (selected) current.add(guest.id) else current.remove(guest.id)
                                selectedGuestIds.value = current
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddGuestClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_guest_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Invitee")
        }
    }

    // Guest Detail View Dialog with WhatsApp intent
    guestForDetailView?.let { guest ->
        GuestDetailDialog(
            guest = guest,
            event = currentEvent,
            onDismiss = { guestForDetailView = null },
            onEditGuest = {
                guestForDetailView = null
                onEditGuestClick(it)
            },
            onUpdateInviteChannel = { newChannel ->
                onUpdateInviteChannel(guest.id, newChannel)
            }
        )
    }

    if (showBroadcastDialog) {
        BroadcastMessageDialog(
            guests = allRawGuests,
            selectedGuestIds = selectedGuestIds.value,
            eventId = currentEvent?.id ?: "",
            eventTitle = currentEvent?.title ?: "Event",
            onDismiss = { showBroadcastDialog = false }
        )
    }

    if (showPhoneContactsDialog && currentEvent != null) {
        ImportPhoneContactsDialog(
            eventId = currentEvent.id,
            onDismiss = { showPhoneContactsDialog = false },
            onImportGuests = { newGuests ->
                onImportPhoneContacts(newGuests)
            }
        )
    }

    // Delete Confirmation Dialog
    guestToDelete?.let { guest ->
        AlertDialog(
            onDismissRequest = { guestToDelete = null },
            title = { Text("Delete Invitee") },
            text = { Text("Are you sure you want to remove ${guest.name} from this event? All RSVP and contact info will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGuestClick(guest)
                        guestToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { guestToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GuestItemCard(
    guest: GuestEntity,
    isSecurityLocked: Boolean,
    isChecklistMode: Boolean,
    onCardClick: () -> Unit = {},
    onToggleCheckIn: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatusChange: (RsvpStatus) -> Unit,
    onUpdateInviteChannel: (InviteChannel) -> Unit = {},
    eventTitle: String,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var channelDropdownExpanded by remember { mutableStateOf(false) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    val status = RsvpStatus.fromString(guest.rsvpStatus)
    val inviteChannel = InviteChannel.fromString(guest.inviteChannel)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("guest_card_${guest.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (guest.isCheckedIn) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Selection Checkbox / Checklist Checkbox / Name / Category / RSVP Badge / Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection Checkbox for bulk WhatsApp
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectedChange,
                    modifier = Modifier.testTag("select_guest_checkbox_${guest.id}")
                )
                Spacer(modifier = Modifier.width(2.dp))

                // Checklist checkbox
                Checkbox(
                    checked = guest.isCheckedIn,
                    onCheckedChange = { onToggleCheckIn() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = StatusAttending,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.testTag("guest_checkin_box_${guest.id}")
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = guest.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (guest.plusOnes > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+${guest.plusOnes}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = PersonType.fromString(guest.personType).label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        if (guest.tableNumber.isNotBlank()) {
                            Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = guest.tableNumber,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                        // Invitation Option Mark/Badge (Clickable)
                        Box {
                            Surface(
                                color = if (inviteChannel == InviteChannel.WHATSAPP_ONLY) Color(0xFF25D366).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable { channelDropdownExpanded = true }
                                    .testTag("invite_channel_badge_${guest.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (inviteChannel == InviteChannel.WHATSAPP_ONLY) {
                                        Icon(
                                            Icons.Default.Chat,
                                            contentDescription = null,
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "WhatsApp Only",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF25D366)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Invite",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = channelDropdownExpanded,
                                onDismissRequest = { channelDropdownExpanded = false }
                            ) {
                                InviteChannel.entries.forEach { ch ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (ch == InviteChannel.WHATSAPP_ONLY) {
                                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                                                } else {
                                                    Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(ch.displayName)
                                            }
                                        },
                                        onClick = {
                                            onUpdateInviteChannel(ch)
                                            channelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // RSVP Badge with click to change
                Box {
                    Surface(
                        color = status.containerColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { statusDropdownExpanded = true }
                            .testTag("rsvp_badge_${guest.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(status.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = status.shortLabel,
                                color = status.color,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        RsvpStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(s.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(s.displayName)
                                    }
                                },
                                onClick = {
                                    onStatusChange(s)
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Card Overflow Menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View & Send WhatsApp Invite") },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366)) },
                            onClick = {
                                menuExpanded = false
                                onCardClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Invitee Details") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (inviteChannel == InviteChannel.WHATSAPP_ONLY) "Mark as Standard Invite" else "Mark as Invite Only WhatsApp") },
                            leadingIcon = {
                                if (inviteChannel == InviteChannel.WHATSAPP_ONLY) {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                } else {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366))
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                val nextChannel = if (inviteChannel == InviteChannel.WHATSAPP_ONLY) InviteChannel.STANDARD else InviteChannel.WHATSAPP_ONLY
                                onUpdateInviteChannel(nextChannel)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (guest.isCheckedIn) "Mark as Not Arrived" else "Mark as Checked In") },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onToggleCheckIn()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Invitee", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }



            // Plus One Names (if any)
            if (guest.plusOnes > 0 && guest.plusOneNames.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Guest party: ${guest.plusOneNames}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Contact & Navigation Action Bar
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Address/Masked Info
                if (isSecurityLocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Contact Info Locked",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Contact details protected by PIN/Biometric",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    if (guest.address.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    ContactActionHelper.openMapsNavigation(context, guest.address)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = guest.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Quick Action Buttons: WhatsApp File + Message, WhatsApp Chat, Call
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (guest.whatsAppNumber.isNotBlank() || guest.phoneNumber.isNotBlank()) {
                            // Dedicated Individual Send WhatsApp File + Message Button
                            Surface(
                                onClick = { showWhatsAppDialog = true },
                                color = Color(0xFF25D366).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("whatsapp_file_msg_button_${guest.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = "Send WhatsApp File & Message",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "File + Msg",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF075E54),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Quick WhatsApp Chat Button
                            IconButton(
                                onClick = {
                                    val targetNum = if (guest.whatsAppNumber.isNotBlank()) guest.whatsAppNumber else guest.phoneNumber
                                    ContactActionHelper.openWhatsApp(context, targetNum, "Hi ${guest.name}, you are cordially invited to $eventTitle!")
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape)
                                    .testTag("whatsapp_chat_button_${guest.id}")
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "Quick WhatsApp Chat",
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (showWhatsAppDialog) {
                            SendWhatsAppDialog(
                                guest = guest,
                                eventTitle = eventTitle,
                                eventId = guest.eventId,
                                onDismiss = { showWhatsAppDialog = false }
                            )
                        }

                        // Phone Call Button
                        if (guest.phoneNumber.isNotBlank()) {
                            IconButton(
                                onClick = { ContactActionHelper.makePhoneCall(context, guest.phoneNumber) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .testTag("call_button_${guest.id}")
                            ) {
                                Icon(
                                    Icons.Default.Call,
                                    contentDescription = "Call Invitee",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Email Button
                        if (guest.email.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    ContactActionHelper.sendEmail(
                                        context,
                                        guest.email,
                                        "Invitation: $eventTitle",
                                        "Dear ${guest.name},\n\nYou are invited to $eventTitle. Please confirm your RSVP."
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                    .testTag("email_button_${guest.id}")
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email Invitee",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Checked In Timestamp Banner
            if (guest.isCheckedIn && guest.checkInTimestamp != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = StatusAttending.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = StatusAttending,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Checked in at ${SimpleDateFormat("h:mm a", Locale.US).format(Date(guest.checkInTimestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusAttending,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SendWhatsAppDialog(
    guest: GuestEntity,
    eventTitle: String,
    eventId: String = guest.eventId,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var message by remember {
        mutableStateOf("Hi ${guest.name}, you are cordially invited to $eventTitle! Please confirm your attendance.")
    }

    var selectedFileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Auto-load saved event attachments
    LaunchedEffect(eventId) {
        val saved = com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId)
        val savedUris = saved.map { Uri.parse(it.uriString) }
        selectedFileUris = savedUris
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedAtt = com.example.util.SavedAttachmentHelper.copyAndSaveAttachment(context, eventId, uri)
            if (savedAtt != null) {
                selectedFileUris = selectedFileUris + Uri.parse(savedAtt.uriString)
            } else {
                selectedFileUris = selectedFileUris + uri
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedAtt = com.example.util.SavedAttachmentHelper.copyAndSaveAttachment(context, eventId, uri)
            if (savedAtt != null) {
                selectedFileUris = selectedFileUris + Uri.parse(savedAtt.uriString)
            } else {
                selectedFileUris = selectedFileUris + uri
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send WhatsApp File & Message") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "To: ${guest.name} (${if (guest.whatsAppNumber.isNotBlank()) guest.whatsAppNumber else guest.phoneNumber})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Quick Message Templates
                Text("Quick Templates:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { message = "Dear ${guest.name}, we request the pleasure of your company at $eventTitle. Please RSVP at your earliest convenience." },
                            label = { Text("Formal Invite") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { message = "Hey ${guest.name}! Join us for $eventTitle. Can't wait to see you there!" },
                            label = { Text("Casual Invite") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { message = "Hi ${guest.name}, friendly reminder to kindly update your RSVP for $eventTitle." },
                            label = { Text("RSVP Reminder") }
                        )
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Personalized Message *") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("whatsapp_message_input")
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text("Attachments (${selectedFileUris.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Image")
                    }

                    OutlinedButton(
                        onClick = {
                            pdfPickerLauncher.launch("application/pdf")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add PDF")
                    }
                }

                if (selectedFileUris.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        selectedFileUris.forEachIndexed { idx, uri ->
                            val isPdf = uri.toString().endsWith(".pdf", ignoreCase = true)
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = if (isPdf) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uri.lastPathSegment ?: "Attachment ${idx + 1}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedFileUris = selectedFileUris.filter { it != uri }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Remove file", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No files attached. Tap buttons above to attach flyers or PDFs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetNumber = if (guest.whatsAppNumber.isNotBlank()) guest.whatsAppNumber else guest.phoneNumber
                    ContactActionHelper.sendWhatsAppWithMultipleFiles(context, targetNumber, message, selectedFileUris)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier.testTag("send_whatsapp_with_file_btn")
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send via WhatsApp")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BroadcastMessageDialog(
    guests: List<GuestEntity>,
    selectedGuestIds: Set<String>,
    eventId: String,
    eventTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var message by remember {
        mutableStateOf("Hi [Guest Name], you are cordially invited to $eventTitle! Please confirm your attendance.")
    }
    var targetGroup by remember { mutableStateOf("ATTENDING") }
    var savedAttachments by remember {
        mutableStateOf(com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId))
    }
    var activeBatchRecipients by remember { mutableStateOf<List<GuestEntity>?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            com.example.util.SavedAttachmentHelper.copyAndSaveAttachment(context, eventId, uri)
            savedAttachments = com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId)
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            com.example.util.SavedAttachmentHelper.copyAndSaveAttachment(context, eventId, uri)
            savedAttachments = com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId)
        }
    }

    if (activeBatchRecipients != null) {
        BulkWhatsAppQueueDialog(
            recipients = activeBatchRecipients!!,
            messageTemplate = message,
            eventId = eventId,
            onDismiss = {
                activeBatchRecipients = null
                onDismiss()
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (selectedGuestIds.isNotEmpty()) "Send WhatsApp to ${selectedGuestIds.size} Selected" else "Upload & Send Message to Guests") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (selectedGuestIds.isNotEmpty())
                        "Sending message & saved attached files to ${selectedGuestIds.size} selected guest(s) with 'Hi [Guest Name]' greeting."
                    else
                        "Send custom message with 'Hi [Guest Name]' greeting and saved files via WhatsApp.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Box *") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_message_input")
                )

                if (selectedGuestIds.isEmpty()) {
                    Text("Target Group:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = targetGroup == "ATTENDING",
                            onClick = { targetGroup = "ATTENDING" },
                            label = { Text("Attending") }
                        )
                        FilterChip(
                            selected = targetGroup == "PENDING",
                            onClick = { targetGroup = "PENDING" },
                            label = { Text("Pending") }
                        )
                        FilterChip(
                            selected = targetGroup == "ALL",
                            onClick = { targetGroup = "ALL" },
                            label = { Text("All (${guests.size})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Saved Attachments (Auto-Saved for Event):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Image")
                    }

                    OutlinedButton(
                        onClick = {
                            pdfPickerLauncher.launch("application/pdf")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add PDF")
                    }
                }

                if (savedAttachments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        savedAttachments.forEach { att ->
                            Surface(
                                color = Color(0xFF25D366).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        if (att.mimeType.contains("pdf")) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color(0xFF128C7E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = att.fileName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF128C7E),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )
                                    IconButton(
                                        onClick = {
                                            com.example.util.SavedAttachmentHelper.deleteSavedAttachment(context, att.id)
                                            savedAttachments = com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Remove file", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val recipients = if (selectedGuestIds.isNotEmpty()) {
                        guests.filter { selectedGuestIds.contains(it.id) }
                    } else {
                        when (targetGroup) {
                            "ATTENDING" -> guests.filter { it.rsvpStatus == RsvpStatus.ATTENDING.name }
                            "PENDING" -> guests.filter { it.rsvpStatus == RsvpStatus.PENDING.name }
                            else -> guests
                        }
                    }
                    val validRecipients = recipients.filter { it.whatsAppNumber.isNotBlank() || it.phoneNumber.isNotBlank() }
                    if (validRecipients.isNotEmpty()) {
                        activeBatchRecipients = validRecipients
                    } else {
                        android.widget.Toast.makeText(context, "No valid phone/WhatsApp number found in selection", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier.testTag("send_broadcast_whatsapp_btn")
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedGuestIds.isNotEmpty()) "Send to Selected (${selectedGuestIds.size})" else "Start Bulk WhatsApp Send",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BulkWhatsAppQueueDialog(
    recipients: List<GuestEntity>,
    messageTemplate: String,
    eventId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(0) }
    val currentGuest = recipients.getOrNull(currentIndex)
    val savedAttachments = remember { com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId) }

    if (currentGuest == null || currentIndex >= recipients.size) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("WhatsApp Batch Complete 🎉") },
            text = { Text("All ${recipients.size} selected guest invitations have been processed!") },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        )
        return
    }

    val personalizedMessage = messageTemplate
        .replace("[Guest Name]", currentGuest.name)
        .replace("{GuestName}", currentGuest.name)
    val targetPhone = if (currentGuest.whatsAppNumber.isNotBlank()) currentGuest.whatsAppNumber else currentGuest.phoneNumber

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "WhatsApp Dispatch (${currentIndex + 1}/${recipients.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / recipients.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color(0xFF25D366),
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "To: ${currentGuest.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "📱 Phone: $targetPhone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "Personalized Message:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = personalizedMessage,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (savedAttachments.isNotEmpty()) {
                    Text(
                        text = "📎 ${savedAttachments.size} Saved Attachment(s) Included",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF128C7E)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uris = savedAttachments.map { Uri.parse(it.uriString) }
                    ContactActionHelper.sendWhatsAppWithMultipleFiles(
                        context = context,
                        phoneNumber = targetPhone,
                        message = personalizedMessage,
                        fileUris = uris
                    )
                    currentIndex++
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open WhatsApp for ${currentGuest.name}", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        currentIndex++
                    }
                ) {
                    Text("Skip / Next")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
