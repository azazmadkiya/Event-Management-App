package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.AnnouncementEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnnouncementsScreen(
    announcements: List<AnnouncementEntity>,
    onBroadcastAlert: (title: String, message: String, isUrgent: Boolean, category: String, context: Context) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val context = LocalContext.current
    var showBroadcastDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Real-Time Updates",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live announcements & schedule changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    if (announcements.any { !it.isRead }) {
                        IconButton(onClick = onMarkAllRead) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark All Read", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Button(
                        onClick = { showBroadcastDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("broadcast_new_alert_btn")
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (announcements.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No updates yet", fontWeight = FontWeight.Bold)
                        Text("Broadcast updates will appear here in real-time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(announcements, key = { it.id }) { ann ->
                        AnnouncementItemCard(announcement = ann)
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showBroadcastDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_broadcast_alert"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Campaign, contentDescription = "Broadcast Alert")
        }
    }

    if (showBroadcastDialog) {
        BroadcastDialog(
            onDismiss = { showBroadcastDialog = false },
            onBroadcast = { title, msg, isUrgent, cat ->
                onBroadcastAlert(title, msg, isUrgent, cat, context)
                showBroadcastDialog = false
            }
        )
    }
}

@Composable
fun AnnouncementItemCard(announcement: AnnouncementEntity) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("announcement_item_${announcement.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (announcement.isUrgent) Color(0x18EF4444) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when (announcement.category) {
                        "VENUE_ALERT" -> Color(0xFF38BDF8)
                        "MILESTONE" -> Color(0xFFF59E0B)
                        "SCHEDULE_UPDATE" -> Color(0xFF6366F1)
                        else -> Color(0xFF10B981)
                    }

                    Surface(
                        color = if (announcement.isUrgent) Color(0xFFEF4444) else badgeColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (announcement.isUrgent) "URGENT ALERT" else announcement.category.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = SimpleDateFormat("MMM dd, h:mm a", Locale.US).format(Date(announcement.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BroadcastDialog(
    onDismiss: () -> Unit,
    onBroadcast: (title: String, message: String, isUrgent: Boolean, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("SCHEDULE_UPDATE") }

    val categories = listOf("SCHEDULE_UPDATE", "VENUE_ALERT", "MILESTONE", "REMINDER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Broadcast Event Update")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "This notification will be delivered immediately to all guests and trigger an Android push notification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title *") },
                    placeholder = { Text("e.g. Ceremony Starting in 15 Minutes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_title_input")
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body *") },
                    placeholder = { Text("Please take your seats in the Grand Pavilion.") },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_message_input")
                )

                // Category chips
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (category == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { category = cat }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = cat.take(8),
                                color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Urgent Push Notification Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Urgent Priority Push", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Vibrate device with heads-up banner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it },
                        modifier = Modifier.testTag("broadcast_urgent_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onBroadcast(title.trim(), message.trim(), isUrgent, category)
                    }
                },
                modifier = Modifier.testTag("broadcast_confirm_btn")
            ) {
                Text("Send Alert Now")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
