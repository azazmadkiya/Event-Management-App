package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.InviteChannel
import com.example.data.model.PersonType
import com.example.data.model.RsvpStatus
import com.example.util.ContactActionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InvitationTemplate(
    val title: String,
    val description: String,
    val buildMessage: (GuestEntity, EventEntity?) -> String
)

object InvitationTemplates {
    val templates = listOf(
        InvitationTemplate(
            title = "Formal Invite",
            description = "Elegant formal invitation",
            buildMessage = { guest, event ->
                val eventName = event?.title ?: "our special event"
                val dateStr = if (event != null) SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US).format(Date(event.startTimestamp)) else ""
                val venue = event?.venueName ?: ""
                "Dear ${guest.name},\n\nYou are cordially invited to $eventName" +
                        (if (dateStr.isNotEmpty()) " on $dateStr" else "") +
                        (if (venue.isNotEmpty()) " at $venue" else "") +
                        ". We would be honored by your presence.\n\nPlease confirm your RSVP status at your earliest convenience."
            }
        ),
        InvitationTemplate(
            title = "Casual Invite",
            description = "Friendly, informal message",
            buildMessage = { guest, event ->
                val eventName = event?.title ?: "the event"
                "Hey ${guest.name}! Join us for $eventName! It's going to be an amazing time. Let us know if you can make it! 🎉"
            }
        ),
        InvitationTemplate(
            title = "RSVP Reminder",
            description = "Polite follow-up reminder",
            buildMessage = { guest, event ->
                val eventName = event?.title ?: "the event"
                "Hi ${guest.name},\n\nFriendly reminder to update your RSVP for $eventName. We are finalizing arrangements and would love to know if you'll be joining us! 🙏"
            }
        ),
        InvitationTemplate(
            title = "Location & Schedule",
            description = "Venue address & schedule details",
            buildMessage = { guest, event ->
                val eventName = event?.title ?: "the event"
                val venue = event?.venueName ?: "Venue"
                val address = event?.venueAddress ?: ""
                val dateStr = if (event != null) SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US).format(Date(event.startTimestamp)) else ""
                "Hello ${guest.name}!\n\nHere are the event details for $eventName:\n📍 Venue: $venue\n📌 Address: $address\n📅 Date & Time: $dateStr\n\nLooking forward to seeing you!"
            }
        ),
        InvitationTemplate(
            title = "Custom",
            description = "Write your own message",
            buildMessage = { guest, event ->
                "Hello ${guest.name}! You are invited to ${event?.title ?: "our event"}. Please let us know if you can attend."
            }
        )
    )
}

@Composable
fun GuestDetailDialog(
    guest: GuestEntity,
    event: EventEntity?,
    onDismiss: () -> Unit,
    onEditGuest: (GuestEntity) -> Unit,
    onUpdateInviteChannel: (InviteChannel) -> Unit
) {
    val context = LocalContext.current
    var selectedTemplateIndex by remember { mutableStateOf(0) }
    var customMessage by remember {
        mutableStateOf(InvitationTemplates.templates[0].buildMessage(guest, event))
    }
    var savedAttachments by remember {
        mutableStateOf(com.example.util.SavedAttachmentHelper.getSavedAttachments(context, guest.eventId))
    }

    val rsvpStatus = RsvpStatus.fromString(guest.rsvpStatus)
    val inviteChannel = InviteChannel.fromString(guest.inviteChannel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guest.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = PersonType.fromString(guest.personType).label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // RSVP Badge
                    Surface(
                        color = rsvpStatus.containerColor,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(rsvpStatus.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = rsvpStatus.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = rsvpStatus.color
                            )
                        }
                    }

                    // Invite Channel Mark
                    Surface(
                        color = if (inviteChannel == InviteChannel.WHATSAPP_ONLY) Color(0xFF25D366).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable {
                            val next = if (inviteChannel == InviteChannel.WHATSAPP_ONLY) InviteChannel.STANDARD else InviteChannel.WHATSAPP_ONLY
                            onUpdateInviteChannel(next)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (inviteChannel == InviteChannel.WHATSAPP_ONLY) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp Only", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF25D366))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Invite", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (guest.plusOnes > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "+${guest.plusOnes} guests",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Contact Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val targetPhone = if (guest.whatsAppNumber.isNotBlank()) guest.whatsAppNumber else guest.phoneNumber
                        if (targetPhone.isNotBlank()) {
                            Text(
                                text = "📱 Phone/WhatsApp: $targetPhone",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (guest.email.isNotBlank()) {
                            Text(
                                text = "✉️ Email: ${guest.email}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (guest.tableNumber.isNotBlank()) {
                            Text(
                                text = "🪑 Table/Seat: ${guest.tableNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Section Title: Pre-Formatted Invitation Message
                Text(
                    text = "Pre-Formatted WhatsApp Invitation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Template Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(InvitationTemplates.templates.indices.toList()) { index ->
                        val template = InvitationTemplates.templates[index]
                        FilterChip(
                            selected = selectedTemplateIndex == index,
                            onClick = {
                                selectedTemplateIndex = index
                                customMessage = template.buildMessage(guest, event)
                            },
                            label = { Text(template.title, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Pre-formatted Message Text Box
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Invitation Message") },
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_detail_whatsapp_message_input")
                )

                // File Attachment Section
                com.example.ui.components.UploadAttachmentSection(
                    eventId = guest.eventId,
                    savedAttachments = savedAttachments,
                    onAttachmentsUpdated = { updated ->
                        savedAttachments = updated
                    },
                    title = "Attach Cards & Files (Auto-attached to WhatsApp)",
                    showExplanation = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetNumber = if (guest.whatsAppNumber.isNotBlank()) guest.whatsAppNumber else guest.phoneNumber
                    val uris = savedAttachments.map { Uri.parse(it.uriString) }
                    com.example.util.ContactActionHelper.sendWhatsAppWithMultipleFiles(
                        context = context,
                        phoneNumber = targetNumber,
                        message = customMessage,
                        fileUris = uris
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier.testTag("guest_detail_send_whatsapp_btn")
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send via WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
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
