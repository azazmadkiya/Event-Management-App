package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Chat
import com.example.data.model.InviteChannel
import com.example.data.model.DietaryPreference
import com.example.data.model.GuestEntity
import com.example.data.model.PersonType
import com.example.data.model.RsvpStatus
import java.util.UUID

@Composable
fun AddEditGuestDialog(
    eventId: String,
    guestToEdit: GuestEntity? = null,
    onDismiss: () -> Unit,
    onSaveGuest: (GuestEntity) -> Unit
) {
    var name by remember { mutableStateOf(guestToEdit?.name ?: "") }
    var personType by remember { mutableStateOf(guestToEdit?.personType ?: PersonType.FRIEND.name) }
    var phoneNumber by remember { mutableStateOf(guestToEdit?.phoneNumber ?: "") }
    var whatsAppNumber by remember { mutableStateOf(guestToEdit?.whatsAppNumber ?: "") }
    var email by remember { mutableStateOf(guestToEdit?.email ?: "") }
    var address by remember { mutableStateOf(guestToEdit?.address ?: "") }
    var rsvpStatus by remember { mutableStateOf(guestToEdit?.rsvpStatus ?: RsvpStatus.PENDING.name) }
    var inviteChannel by remember { mutableStateOf(guestToEdit?.inviteChannel ?: InviteChannel.STANDARD.name) }
    var dietaryPreference by remember { mutableStateOf(guestToEdit?.dietaryPreference ?: DietaryPreference.NONE.name) }
    var dietaryNotes by remember { mutableStateOf(guestToEdit?.dietaryNotes ?: "") }
    var plusOnes by remember { mutableIntStateOf(guestToEdit?.plusOnes ?: 0) }
    var plusOneNames by remember { mutableStateOf(guestToEdit?.plusOneNames ?: "") }
    var tableNumber by remember { mutableStateOf(guestToEdit?.tableNumber ?: "") }
    var notes by remember { mutableStateOf(guestToEdit?.notes ?: "") }

    var personTypeMenuExpanded by remember { mutableStateOf(false) }
    var rsvpMenuExpanded by remember { mutableStateOf(false) }
    var inviteChannelMenuExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (guestToEdit == null) "Add New Invitee" else "Edit Guest Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name is required") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_name_input")
                )

                // Person Type Dropdown
                Box {
                    OutlinedTextField(
                        value = PersonType.fromString(personType).label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Invitee Category") },
                        trailingIcon = {
                            IconButton(onClick = { personTypeMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { personTypeMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = personTypeMenuExpanded,
                        onDismissRequest = { personTypeMenuExpanded = false }
                    ) {
                        PersonType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    personType = type.name
                                    personTypeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // RSVP Status
                Box {
                    OutlinedTextField(
                        value = RsvpStatus.fromString(rsvpStatus).displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("RSVP Status") },
                        trailingIcon = {
                            IconButton(onClick = { rsvpMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select RSVP Status")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rsvpMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = rsvpMenuExpanded,
                        onDismissRequest = { rsvpMenuExpanded = false }
                    ) {
                        RsvpStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    rsvpStatus = status.name
                                    rsvpMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Invitation Option Dropdown (Invite vs Invite Only WhatsApp)
                Box {
                    OutlinedTextField(
                        value = InviteChannel.fromString(inviteChannel).displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Invitation Option") },
                        leadingIcon = {
                            if (InviteChannel.fromString(inviteChannel) == InviteChannel.WHATSAPP_ONLY) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF25D366))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { inviteChannelMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Invitation Option")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { inviteChannelMenuExpanded = true }
                            .testTag("invite_option_dropdown")
                    )
                    DropdownMenu(
                        expanded = inviteChannelMenuExpanded,
                        onDismissRequest = { inviteChannelMenuExpanded = false }
                    ) {
                        InviteChannel.entries.forEach { channel ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (channel == InviteChannel.WHATSAPP_ONLY) {
                                            Icon(Icons.Default.Chat, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF25D366), modifier = Modifier.size(18.dp))
                                        } else {
                                            Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(channel.displayName)
                                    }
                                },
                                onClick = {
                                    inviteChannel = channel.name
                                    inviteChannelMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Contact Phone & WhatsApp
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("guest_phone_input")
                )

                OutlinedTextField(
                    value = whatsAppNumber,
                    onValueChange = { whatsAppNumber = it },
                    label = { Text("WhatsApp Number") },
                    trailingIcon = {
                        if (phoneNumber.isNotBlank() && whatsAppNumber.isBlank()) {
                            IconButton(onClick = { whatsAppNumber = phoneNumber }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy from phone")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    supportingText = { Text("Used for direct 1-tap WhatsApp invitations") },
                    modifier = Modifier.fillMaxWidth().testTag("guest_whatsapp_input")
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("guest_email_input")
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Mailing Address / City") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("guest_address_input")
                )



                // Plus Ones Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Plus Ones (+Guests)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(if (plusOnes == 0) "Solo guest" else "+$plusOnes additional guest(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (plusOnes > 0) plusOnes-- },
                            enabled = plusOnes > 0
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease plus ones")
                        }
                        Text("$plusOnes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(
                            onClick = { if (plusOnes < 8) plusOnes++ },
                            enabled = plusOnes < 8
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase plus ones")
                        }
                    }
                }

                if (plusOnes > 0) {
                    OutlinedTextField(
                        value = plusOneNames,
                        onValueChange = { plusOneNames = it },
                        label = { Text("Plus One Names") },
                        placeholder = { Text("e.g. Spouse / partner name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Table / Seating
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = { tableNumber = it },
                    label = { Text("Table / Seat Assignment") },
                    placeholder = { Text("e.g. Table 4 - Rose Garden") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Additional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Host Notes") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val guest = GuestEntity(
                        id = guestToEdit?.id ?: "guest_${UUID.randomUUID()}",
                        eventId = eventId,
                        name = name.trim(),
                        personType = personType,
                        phoneNumber = phoneNumber.trim(),
                        whatsAppNumber = whatsAppNumber.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        rsvpStatus = rsvpStatus,
                        dietaryPreference = dietaryPreference,
                        dietaryNotes = dietaryNotes.trim(),
                        plusOnes = plusOnes,
                        plusOneNames = plusOneNames.trim(),
                        tableNumber = tableNumber.trim(),
                        isCheckedIn = guestToEdit?.isCheckedIn ?: false,
                        checkInTimestamp = guestToEdit?.checkInTimestamp,
                        notes = notes.trim(),
                        inviteChannel = inviteChannel,
                        invitedTimestamp = guestToEdit?.invitedTimestamp ?: System.currentTimeMillis(),
                        updatedTimestamp = System.currentTimeMillis()
                    )
                    onSaveGuest(guest)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_guest_button")
            ) {
                Text(if (guestToEdit == null) "Add Invitee" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
