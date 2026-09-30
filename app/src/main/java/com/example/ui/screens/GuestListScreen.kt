package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.model.InviteChannel
import com.example.data.model.DietaryPreference
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.PersonType
import com.example.data.model.RsvpStatus
import com.example.ui.components.ImportPhoneContactsDialog
import com.example.ui.components.UploadAttachmentSection
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
    var showUploadsDialog by remember { mutableStateOf(false) }
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
        "INVITED" to "Invited (${allRawGuests.count { it.rsvpStatus == RsvpStatus.INVITED.name }})",
        "PENDING" to "Pending (${allRawGuests.count { it.rsvpStatus == RsvpStatus.PENDING.name }})",
        "DECLINED" to "Declined (${allRawGuests.count { it.rsvpStatus == RsvpStatus.DECLINED.name }})",
        "NOT_INVITED" to "Draft (${allRawGuests.count { it.rsvpStatus == RsvpStatus.NOT_INVITED.name }})",
        "CHECKLIST" to "Checklist (${allRawGuests.count { it.isCheckedIn }}/${allRawGuests.count { it.rsvpStatus == RsvpStatus.ATTENDING.name }})"
    )

    val currentTabIndex = tabs.indexOfFirst { it.first == selectedRsvpTab }.coerceAtLeast(0)

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("guest_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Action Buttons Row (Uploads, Backup, Restore, Contacts, Send Msg)
            item(key = "top_action_buttons") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showUploadsDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("upload_files_top_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uploads", style = MaterialTheme.typography.labelMedium)
                    }

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
            }

            // Search Bar
            item(key = "guest_search_bar_item") {
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
            }

            // Status Tabs (Smooth Scrollable Tabs)
            item(key = "guest_status_tabs_item") {
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
            }

            // Invitation Option Filter Chips
            item(key = "guest_invite_channel_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedInviteChannelFilter == null,
                        onClick = { onInviteChannelFilterChange(null) },
                        label = { Text("All Invites") },
                        shape = RoundedCornerShape(10.dp)
                    )
                    InviteChannel.entries.forEach { channel ->
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
            }

            // Security Lock Banner if locked
            if (isSecurityLocked) {
                item(key = "security_locked_banner") {
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
            }

            // Guest Count & Select All Row
            item(key = "guest_selection_counter_row") {
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
            }

            // Guest List or Empty State
            if (guests.isEmpty()) {
                item(key = "empty_guests_item") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
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
                }
            } else {
                items(items = guests, key = { it.id }) { guest ->
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

    if (showUploadsDialog && currentEvent != null) {
        EventUploadsDialog(
            eventId = currentEvent.id,
            eventTitle = currentEvent.title,
            onDismiss = { showUploadsDialog = false }
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
    val guestInitial = guest.name.trim().take(1).uppercase()

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
            // Top Row: Selection Checkbox + Avatar + Guest Name & Category + RSVP Badge + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection Checkbox for bulk actions
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectedChange,
                    modifier = Modifier.testTag("select_guest_checkbox_${guest.id}")
                )

                // User Avatar Circle with Initial
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (guestInitial.isNotEmpty()) guestInitial else "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Sub-details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = guest.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (guest.plusOnes > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "+${guest.plusOnes}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
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
                                text = "Table: ${guest.tableNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // RSVP Badge Dropdown
                Box {
                    Surface(
                        color = status.containerColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { statusDropdownExpanded = true }
                            .testTag("rsvp_badge_${guest.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
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
                                text = status.displayName,
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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Send WhatsApp",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF075E54)
                                    )
                                }
                            },
                            onClick = {
                                statusDropdownExpanded = false
                                showWhatsAppDialog = true
                            }
                        )
                    }
                }

                // Card Overflow Menu (⋮)
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
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

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Second Row: Channel Badge & Check-in / Arrival Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Invitation Channel Badge (Clickable to switch)
                Box {
                    Surface(
                        color = if (inviteChannel == InviteChannel.WHATSAPP_ONLY) Color(0xFF25D366).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { channelDropdownExpanded = true }
                            .testTag("invite_channel_badge_${guest.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (inviteChannel == InviteChannel.WHATSAPP_ONLY) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF128C7E),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "WhatsApp Only",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF128C7E)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Standard Invite",
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

                // Check-in / Arrival Action Pill
                Surface(
                    onClick = onToggleCheckIn,
                    color = if (guest.isCheckedIn) StatusAttending.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (guest.isCheckedIn) StatusAttending else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("guest_checkin_box_${guest.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (guest.isCheckedIn) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = "Check-in Status",
                            tint = if (guest.isCheckedIn) StatusAttending else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (guest.isCheckedIn) "Arrived" else "Mark Arrived",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (guest.isCheckedIn) FontWeight.Bold else FontWeight.Medium,
                            color = if (guest.isCheckedIn) StatusAttending else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Plus One Names (if any)
            if (guest.plusOnes > 0 && guest.plusOneNames.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Party: ${guest.plusOneNames}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Contact & Navigation Action Bar
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Address/Masked Info
                if (isSecurityLocked) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = guest.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Quick Action Buttons: WhatsApp File + Message, WhatsApp Chat, Call, Email
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
                                        modifier = Modifier.size(15.dp)
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
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .testTag("call_button_${guest.id}")
                            ) {
                                Icon(
                                    Icons.Default.Call,
                                    contentDescription = "Call Invitee",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
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
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                    .testTag("email_button_${guest.id}")
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email Invitee",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
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

    var savedAttachments by remember {
        mutableStateOf(com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send WhatsApp Invite", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${guest.name} • ${if (guest.whatsAppNumber.isNotBlank()) guest.whatsAppNumber else guest.phoneNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

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
                            onClick = { message = "Hey ${guest.name}! Join us for $eventTitle. Can't wait to see you there! 🎉" },
                            label = { Text("Casual Invite") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { message = "Hi ${guest.name}, friendly reminder to kindly update your RSVP for $eventTitle. Thank you!" },
                            label = { Text("RSVP Reminder") }
                        )
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Personalized Message *") },
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("whatsapp_message_input")
                )

                // Professional Upload & Attachments Section
                UploadAttachmentSection(
                    eventId = eventId,
                    savedAttachments = savedAttachments,
                    onAttachmentsUpdated = { updated ->
                        savedAttachments = updated
                    },
                    title = "Attach Cards & Files",
                    showExplanation = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetNumber = if (guest.whatsAppNumber.isNotBlank()) guest.whatsAppNumber else guest.phoneNumber
                    val uris = savedAttachments.map { Uri.parse(it.uriString) }
                    ContactActionHelper.sendWhatsAppWithMultipleFiles(context, targetNumber, message, uris)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("send_whatsapp_with_file_btn")
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send via WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedGuestIds.isNotEmpty()) "Send WhatsApp (${selectedGuestIds.size} Selected)" else "Broadcast WhatsApp Message",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (selectedGuestIds.isNotEmpty())
                        "Sending personalized WhatsApp message and attached files to ${selectedGuestIds.size} selected guest(s)."
                    else
                        "Send personalized WhatsApp invitations with attached cards and files to your guests.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Template * ([Guest Name] will be auto-replaced)") },
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_message_input")
                )

                // Professional Upload & Attachments Section
                UploadAttachmentSection(
                    eventId = eventId,
                    savedAttachments = savedAttachments,
                    onAttachmentsUpdated = { updated ->
                        savedAttachments = updated
                    },
                    title = "Broadcast Media & Cards",
                    showExplanation = true
                )
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("send_broadcast_whatsapp_btn")
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedGuestIds.isNotEmpty()) "Send to Selected (${selectedGuestIds.size})" else "Start Bulk Send",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EventUploadsDialog(
    eventId: String,
    eventTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var savedAttachments by remember {
        mutableStateOf(com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uploads & Media Manager", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Upload and manage invitation cards (JPG/PNG) and documents (PDF) for $eventTitle. These files will be automatically attached when sending WhatsApp invitations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                UploadAttachmentSection(
                    eventId = eventId,
                    savedAttachments = savedAttachments,
                    onAttachmentsUpdated = { updated ->
                        savedAttachments = updated
                    },
                    title = "Event Media Files",
                    showExplanation = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done")
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
    var sentGuestIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val savedAttachments = remember { com.example.util.SavedAttachmentHelper.getSavedAttachments(context, eventId) }

    val currentGuest = recipients.getOrNull(currentIndex)

    if (currentGuest == null || recipients.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("WhatsApp Dispatch Complete 🎉") },
            text = { Text("All ${recipients.size} selected guest invitations have been processed!") },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("Done")
                }
            }
        )
        return
    }

    var customMessage by remember(currentIndex) {
        mutableStateOf(
            messageTemplate
                .replace("[Guest Name]", currentGuest.name)
                .replace("{GuestName}", currentGuest.name)
        )
    }

    val targetPhone = if (currentGuest.whatsAppNumber.isNotBlank()) currentGuest.whatsAppNumber else currentGuest.phoneNumber

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1-By-1 WhatsApp (${currentIndex + 1}/${recipients.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = Color(0xFF25D366).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${sentGuestIds.size}/${recipients.size} Sent",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF075E54),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / recipients.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color(0xFF25D366),
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive Guest Selector Chips Row
                Text("Tap Guest to Select:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(recipients) { idx, g ->
                        val isSent = sentGuestIds.contains(g.id)
                        val isSelected = idx == currentIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentIndex = idx },
                            label = {
                                Text(
                                    text = "${idx + 1}. ${g.name} ${if (isSent) "✓" else ""}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF25D366).copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFF075E54)
                            )
                        )
                    }
                }

                // Invitee Details Card
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest ${currentIndex + 1}: ${currentGuest.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📱 WhatsApp: $targetPhone",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (sentGuestIds.contains(currentGuest.id)) {
                            Surface(
                                color = Color(0xFF25D366),
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp).size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Editable Message
                Text(
                    text = "Message for ${currentGuest.name}:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("bulk_queue_msg_input")
                )

                // Saved Attachments Indicator
                if (savedAttachments.isNotEmpty()) {
                    Surface(
                        color = Color(0xFF25D366).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = Color(0xFF128C7E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${savedAttachments.size} File(s) Attached Automatically",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF128C7E)
                            )
                        }
                    }
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
                        message = customMessage,
                        fileUris = uris
                    )
                    sentGuestIds = sentGuestIds + currentGuest.id
                    if (currentIndex < recipients.size - 1) {
                        currentIndex++
                    } else {
                        android.widget.Toast.makeText(context, "All guest messages sent!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier.fillMaxWidth().testTag("send_individual_queue_btn")
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Send to ${currentGuest.name} on WhatsApp",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("◄ Back")
                    }
                    OutlinedButton(
                        onClick = { if (currentIndex < recipients.size - 1) currentIndex++ },
                        enabled = currentIndex < recipients.size - 1,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Next ►")
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Close")
                }
            }
        }
    )
}
