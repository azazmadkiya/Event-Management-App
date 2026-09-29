package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EventEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.delay

@Composable
fun EventDetailsScreen(
    currentEvent: EventEntity?,
    onUpdateEvent: (EventEntity) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onAddEvent: (EventEntity) -> Unit
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    if (currentEvent == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("No event selected")
                Button(onClick = { showAddEventDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create New Event")
                }
            }
        }
        if (showAddEventDialog) {
            AddEditEventDialog(
                eventToEdit = null,
                onDismiss = { showAddEventDialog = false },
                onSave = { newEvent ->
                    onAddEvent(newEvent)
                    showAddEventDialog = false
                }
            )
        }
        return
    }

    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US)
    val formattedDate = dateFormatter.format(Date(currentEvent.startTimestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card with Title, Edit, and Delete Buttons
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("event_details_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentEvent.eventType.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("edit_event_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("delete_event_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentEvent.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentEvent.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Add New Event Action Button
        Button(
            onClick = { showAddEventDialog = true },
            modifier = Modifier.fillMaxWidth().testTag("add_new_event_btn"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Another Event")
        }

        // Event Date & Time Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("event_date_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Event Date & Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Live Countdown Card
        EventCountdownCard(startTimestamp = currentEvent.startTimestamp)

        // Event Venue & Address Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("event_address_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Venue & Address",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentEvent.venueName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentEvent.venueAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Host & Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Event Details & Host",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Host", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentEvent.hostName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Dress Code", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentEvent.dressCode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Developed By Azazmadkiya",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showEditDialog) {
        AddEditEventDialog(
            eventToEdit = currentEvent,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onUpdateEvent(updated)
                showEditDialog = false
            }
        )
    }

    if (showAddEventDialog) {
        AddEditEventDialog(
            eventToEdit = null,
            onDismiss = { showAddEventDialog = false },
            onSave = { newEvent ->
                onAddEvent(newEvent)
                showAddEventDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete '${currentEvent.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEvent(currentEvent)
                        showDeleteDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddEditEventDialog(
    eventToEdit: EventEntity?,
    onDismiss: () -> Unit,
    onSave: (EventEntity) -> Unit
) {
    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var eventType by remember { mutableStateOf(eventToEdit?.eventType ?: "Gathering") }
    var venueName by remember { mutableStateOf(eventToEdit?.venueName ?: "") }
    var venueAddress by remember { mutableStateOf(eventToEdit?.venueAddress ?: "") }
    var dateString by remember {
        mutableStateOf(
            if (eventToEdit != null)
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(eventToEdit.startTimestamp))
            else
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(System.currentTimeMillis() + 86400000L))
        )
    }
    var description by remember { mutableStateOf(eventToEdit?.description ?: "") }
    var hostName by remember { mutableStateOf(eventToEdit?.hostName ?: "") }
    var dressCode by remember { mutableStateOf(eventToEdit?.dressCode ?: "Formal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (eventToEdit != null) "Edit Event Details" else "Create New Event") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_event_title_input")
                )
                OutlinedTextField(
                    value = eventType,
                    onValueChange = { eventType = it },
                    label = { Text("Event Type (e.g. Wedding, Gala)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date & Time (yyyy-MM-dd HH:mm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_event_date_input")
                )
                OutlinedTextField(
                    value = venueName,
                    onValueChange = { venueName = it },
                    label = { Text("Venue Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_event_venue_input")
                )
                OutlinedTextField(
                    value = venueAddress,
                    onValueChange = { venueAddress = it },
                    label = { Text("Event Address *") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("edit_event_address_input")
                )
                OutlinedTextField(
                    value = hostName,
                    onValueChange = { hostName = it },
                    label = { Text("Host Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dressCode,
                    onValueChange = { dressCode = it },
                    label = { Text("Dress Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && venueAddress.isNotBlank()) {
                        val parsedTimestamp = try {
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(dateString)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        val eventId = eventToEdit?.id ?: "event_${UUID.randomUUID()}"
                        val event = EventEntity(
                            id = eventId,
                            title = title.trim(),
                            eventType = eventType.trim(),
                            startTimestamp = parsedTimestamp,
                            endTimestamp = parsedTimestamp + 14400000L,
                            venueName = venueName.trim(),
                            venueAddress = venueAddress.trim(),
                            description = description.trim(),
                            dressCode = dressCode.trim(),
                            hostName = hostName.trim(),
                            hostContact = "",
                            rsvpDeadlineTimestamp = parsedTimestamp - 86400000L
                        )
                        onSave(event)
                    }
                },
                modifier = Modifier.testTag("save_event_btn")
            ) {
                Text(if (eventToEdit != null) "Save Changes" else "Create")
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
fun EventCountdownCard(startTimestamp: Long) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val diff = startTimestamp - currentTime
    val days = diff / (1000 * 60 * 60 * 24)
    val hours = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60
    val seconds = (diff / 1000) % 60

    val isPassed = diff < 0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag("event_countdown_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isPassed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isPassed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPassed) "Event Has Concluded" else "Countdown to Event",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isPassed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CountdownTimeUnit(value = days.toString(), label = "Days")
                    CountdownTimeUnit(value = hours.toString().padStart(2, '0'), label = "Hours")
                    CountdownTimeUnit(value = minutes.toString().padStart(2, '0'), label = "Mins")
                    CountdownTimeUnit(value = seconds.toString().padStart(2, '0'), label = "Secs")
                }
            } else {
                Text(
                    text = "This event took place in the past.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CountdownTimeUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(56.dp),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )
    }
}
