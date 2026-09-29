package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.data.model.ItineraryItemEntity
import com.example.util.CalendarHelper
import java.util.UUID

@Composable
fun EventScheduleScreen(
    currentEvent: EventEntity?,
    itinerary: List<ItineraryItemEntity>,
    onAddItem: (ItineraryItemEntity) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    if (currentEvent == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Digital Calendar Actions Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("schedule_actions_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Digital Calendar Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Export the entire program to Google Calendar, Apple Calendar, or Outlook.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            CalendarHelper.addToSystemCalendar(context, currentEvent)
                        },
                        modifier = Modifier.weight(1f).testTag("add_to_calendar_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Event")
                    }

                    OutlinedButton(
                        onClick = {
                            CalendarHelper.exportIcsCalendar(context, currentEvent, itinerary)
                        },
                        modifier = Modifier.weight(1f).testTag("export_ics_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export (.ics)")
                    }
                }
            }
        }

        // Travel & Lodging Itinerary Section
        Card(
            modifier = Modifier.fillMaxWidth().testTag("travel_itinerary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AirportShuttle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Travel & Lodging Itinerary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Parking info
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Parking & Valet Service", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(currentEvent.travelInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shuttle info
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Guest Shuttle Schedule", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(currentEvent.shuttleSchedule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Timeline Header with Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order of Events (${itinerary.size} Milestones)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_itinerary_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Step", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Timeline Steps
        itinerary.forEachIndexed { index, item ->
            TimelineStepCard(
                item = item,
                isLast = index == itinerary.lastIndex,
                onAddToCalendar = {
                    CalendarHelper.addToSystemCalendar(context, currentEvent, item)
                }
            )
        }
    }

    if (showAddDialog) {
        AddItineraryDialog(
            eventId = currentEvent.id,
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                onAddItem(newItem)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TimelineStepCard(
    item: ItineraryItemEntity,
    isLast: Boolean,
    onAddToCalendar: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Vertical timeline bar with node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val icon = when (item.iconKey) {
                        "cocktail" -> Icons.Default.LocalBar
                        "dinner" -> Icons.Default.Restaurant
                        "music" -> Icons.Default.MusicNote
                        "ceremony" -> Icons.Default.Star
                        else -> Icons.Default.CalendarMonth
                    }
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Step Card Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("timeline_item_${item.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = item.timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onAddToCalendar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Add milestone to calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (item.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddItineraryDialog(
    eventId: String,
    onDismiss: () -> Unit,
    onSave: (ItineraryItemEntity) -> Unit
) {
    var timeLabel by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule Milestone") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = timeLabel,
                    onValueChange = { timeLabel = it },
                    label = { Text("Time (e.g. 8:00 PM - 9:00 PM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location inside Venue") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val item = ItineraryItemEntity(
                            id = "itin_${UUID.randomUUID()}",
                            eventId = eventId,
                            orderIndex = 99,
                            timeLabel = if (timeLabel.isBlank()) "TBD" else timeLabel,
                            title = title.trim(),
                            location = location.trim(),
                            description = description.trim(),
                            iconKey = "cocktail",
                            startEpochMillis = System.currentTimeMillis(),
                            endEpochMillis = System.currentTimeMillis() + 3600000L
                        )
                        onSave(item)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
