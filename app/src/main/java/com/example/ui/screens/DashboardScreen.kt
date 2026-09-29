package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnnouncementEntity
import com.example.data.model.DietaryPreference
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.ItineraryItemEntity
import com.example.data.model.RsvpStatus
import com.example.ui.theme.Amber50
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo80
import com.example.ui.theme.StatusAttending
import com.example.ui.theme.StatusDeclined
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusPending
import com.example.util.CalendarHelper
import com.example.util.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    currentEvent: EventEntity?,
    guests: List<GuestEntity>,
    announcements: List<AnnouncementEntity>,
    itinerary: List<ItineraryItemEntity>,
    isSyncing: Boolean,
    lastSyncedTimestamp: Long,
    onSyncWithCloud: () -> Unit,
    onNavigateToGuests: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onAddGuestClick: () -> Unit,
    onBroadcastAlertClick: () -> Unit,
    onAddEventClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (currentEvent == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "No Events Created Yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Start planning your event by adding your event details, venue, date, and guest invites.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Button(
                    onClick = onAddEventClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("dashboard_create_first_event_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Your Event")
                }
            }
        }
        return
    }

    // Metric Calculations
    val totalGuests = guests.size
    val attendingGuests = guests.filter { it.rsvpStatus == RsvpStatus.ATTENDING.name }
    val attendingCount = attendingGuests.size
    val totalAttendingParty = attendingGuests.sumOf { it.totalPartySize }
    val pendingCount = guests.count { it.rsvpStatus == RsvpStatus.PENDING.name }
    val declinedCount = guests.count { it.rsvpStatus == RsvpStatus.DECLINED.name }
    val checkedInCount = guests.count { it.isCheckedIn }

    val acceptanceRate = if (totalGuests > 0) ((attendingCount.toFloat() / totalGuests) * 100).toInt() else 0
    val progressAnimated by animateFloatAsState(
        targetValue = if (totalGuests > 0) attendingCount.toFloat() / totalGuests else 0f,
        label = "acceptance_progress"
    )

    // Countdown Calculations
    val now = System.currentTimeMillis()
    val daysUntilEvent = maxOf(0L, TimeUnit.MILLISECONDS.toDays(currentEvent.startTimestamp - now))
    val daysUntilRsvp = maxOf(0L, TimeUnit.MILLISECONDS.toDays(currentEvent.rsvpDeadlineTimestamp - now))

    val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy 'at' h:mm a", Locale.US)
    val eventDateStr = dateFormat.format(Date(currentEvent.startTimestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(currentEvent.coverGradientStart),
                                Color(currentEvent.coverGradientEnd)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = currentEvent.eventType.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Countdown Pill
                        Surface(
                            color = if (daysUntilRsvp <= 3) Color(0x66EF4444) else Color(0x44F59E0B),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (daysUntilRsvp == 0L) "RSVP Closed Today" else "$daysUntilRsvp days for RSVP",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentEvent.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = eventDateStr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFF8FAFC)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentEvent.venueName} • ${currentEvent.venueAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days to event counter
                    Surface(
                        color = Color(0x22FFFFFF),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "🗓️ $daysUntilEvent days until celebration • Host: ${currentEvent.hostName}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Acceptance Rate & RSVP Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_rsvp_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RSVP Acceptance Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$attendingCount accepted of $totalGuests invitees",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "$acceptanceRate%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusAttending
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progressAnimated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = StatusAttending,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown Chips Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPill(
                        label = "OK / Attending",
                        value = "$attendingCount",
                        subValue = "+$totalAttendingParty party",
                        color = StatusAttending,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToGuests("ATTENDING") }
                    )
                    StatPill(
                        label = "Pending",
                        value = "$pendingCount",
                        subValue = "Awaiting reply",
                        color = StatusPending,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToGuests("PENDING") }
                    )
                    StatPill(
                        label = "Declined",
                        value = "$declinedCount",
                        subValue = "Regrets sent",
                        color = StatusDeclined,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToGuests("DECLINED") }
                    )
                    StatPill(
                        label = "Checked In",
                        value = "$checkedInCount",
                        subValue = "At venue",
                        color = Indigo50,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToGuests("CHECKLIST") }
                    )
                }
            }
        }

        // Quick Actions Row
        Text(
            text = "Quick Actions & Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onAddGuestClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("dashboard_add_guest_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Invitee")
            }

            OutlinedButton(
                onClick = onBroadcastAlertClick,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("dashboard_broadcast_alert_button")
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast Alert")
            }

            OutlinedButton(
                onClick = {
                    PdfReportGenerator.generateAndSharePdfReport(context, currentEvent, guests, itinerary)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("dashboard_export_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF Report")
            }

            OutlinedButton(
                onClick = {
                    CalendarHelper.addToSystemCalendar(context, currentEvent)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("dashboard_add_calendar_button")
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Calendar")
            }

            OutlinedButton(
                onClick = onNavigateToMap,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("dashboard_venue_map_button")
            ) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Venue Map")
            }
        }



        // Latest Announcement Preview
        if (announcements.isNotEmpty()) {
            val latest = announcements.first()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_latest_announcement_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (latest.isUrgent) Color(0x22EF4444) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = if (latest.isUrgent) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (latest.isUrgent) "URGENT VENUE ALERT" else "LATEST EVENT UPDATE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (latest.isUrgent) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = SimpleDateFormat("h:mm a", Locale.US).format(Date(latest.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = latest.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = latest.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onNavigateToAnnouncements,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View All Updates (${announcements.size})", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Offline & Cloud Sync Status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isSyncing) MaterialTheme.colorScheme.primary else StatusAttending,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isSyncing) "Syncing with cloud..." else "Offline-first database active",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Last synced: ${SimpleDateFormat("h:mm:ss a", Locale.US).format(Date(lastSyncedTimestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onSyncWithCloud,
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isSyncing) "Syncing..." else "Sync Now", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    subValue: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
