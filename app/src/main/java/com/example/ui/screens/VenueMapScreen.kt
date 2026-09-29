package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.data.model.VenuePoi
import com.example.util.ContactActionHelper

@Composable
fun VenueMapScreen(
    currentEvent: EventEntity?,
    pois: List<VenuePoi>
) {
    val context = LocalContext.current
    var selectedPoi by remember { mutableStateOf<VenuePoi?>(pois.firstOrNull()) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Entrance", "Ceremony", "Dining", "Activity", "Restroom")

    val filteredPois = if (selectedCategory == "All") pois else pois.filter { it.category == selectedCategory }

    if (currentEvent == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Venue Overview Banner
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("venue_info_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentEvent.venueName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentEvent.venueAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            ContactActionHelper.openMapsNavigation(
                                context,
                                currentEvent.venueAddress,
                                currentEvent.latitude,
                                currentEvent.longitude
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("google_maps_nav_btn")
                    ) {
                        Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Maps")
                    }
                }
            }
        }

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Interactive Floorplan Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("venue_floorplan_canvas_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive Venue Floorplan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap pin to inspect",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Floorplan Visual Container with Pins
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.25f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val heightPx = constraints.maxHeight.toFloat()

                    // Schematic architectural layout background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height

                        // Grid lines
                        val gridPaintColor = Color(0x18FFFFFF)
                        for (i in 1..8) {
                            val x = canvasW * (i / 9f)
                            drawLine(gridPaintColor, Offset(x, 0f), Offset(x, canvasH), strokeWidth = 1f)
                        }
                        for (i in 1..6) {
                            val y = canvasH * (i / 7f)
                            drawLine(gridPaintColor, Offset(0f, y), Offset(canvasW, y), strokeWidth = 1f)
                        }

                        // Pavilion Rotunda (Center)
                        drawCircle(
                            color = Color(0x336366F1),
                            radius = canvasW * 0.22f,
                            center = Offset(canvasW * 0.50f, canvasH * 0.45f)
                        )
                        drawCircle(
                            color = Color(0xFF6366F1),
                            radius = canvasW * 0.22f,
                            center = Offset(canvasW * 0.50f, canvasH * 0.45f),
                            style = Stroke(width = 2.5f)
                        )

                        // Grand Foyer (Left)
                        drawRoundRect(
                            color = Color(0x2238BDF8),
                            topLeft = Offset(canvasW * 0.12f, canvasH * 0.50f),
                            size = Size(canvasW * 0.26f, canvasH * 0.35f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(canvasW * 0.12f, canvasH * 0.50f),
                            size = Size(canvasW * 0.26f, canvasH * 0.35f),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 2f)
                        )

                        // Crystal Ballroom / Dining Wing (Top-Right)
                        drawRoundRect(
                            color = Color(0x22F59E0B),
                            topLeft = Offset(canvasW * 0.62f, canvasH * 0.18f),
                            size = Size(canvasW * 0.32f, canvasH * 0.35f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = Color(0xFFF59E0B),
                            topLeft = Offset(canvasW * 0.62f, canvasH * 0.18f),
                            size = Size(canvasW * 0.32f, canvasH * 0.35f),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 2f)
                        )

                        // Starlight Terrace (Bottom-Right)
                        drawRoundRect(
                            color = Color(0x2210B981),
                            topLeft = Offset(canvasW * 0.65f, canvasH * 0.60f),
                            size = Size(canvasW * 0.30f, canvasH * 0.32f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = Color(0xFF10B981),
                            topLeft = Offset(canvasW * 0.65f, canvasH * 0.60f),
                            size = Size(canvasW * 0.30f, canvasH * 0.32f),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 2f)
                        )

                        // Connecting Corridor lines
                        drawLine(
                            color = Color(0x66FFFFFF),
                            start = Offset(canvasW * 0.38f, canvasH * 0.58f),
                            end = Offset(canvasW * 0.50f, canvasH * 0.45f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0x66FFFFFF),
                            start = Offset(canvasW * 0.50f, canvasH * 0.45f),
                            end = Offset(canvasW * 0.62f, canvasH * 0.35f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0x66FFFFFF),
                            start = Offset(canvasW * 0.50f, canvasH * 0.45f),
                            end = Offset(canvasW * 0.65f, canvasH * 0.70f),
                            strokeWidth = 3f
                        )
                    }

                    // Render Clickable POI Pins
                    filteredPois.forEach { poi ->
                        val isSelected = selectedPoi?.id == poi.id
                        val pinColor = when (poi.category) {
                            "Ceremony" -> Color(0xFF818CF8)
                            "Dining" -> Color(0xFFFBBF24)
                            "Entrance" -> Color(0xFF38BDF8)
                            "Activity" -> Color(0xFF34D399)
                            "Restroom" -> Color(0xFFA78BFA)
                            else -> Color(0xFFF43F5E)
                        }

                        // Position pin based on percentages
                        val offsetX = (widthPx * poi.xPercent) - 16.dp.value
                        val offsetY = (heightPx * poi.yPercent) - 16.dp.value

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                                .size(36.dp)
                                .clickable { selectedPoi = poi }
                                .testTag("poi_pin_${poi.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                // Pulsing selection ring
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(pinColor.copy(alpha = 0.35f))
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = pinColor,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val icon = when (poi.iconName) {
                                        "car" -> Icons.Default.DirectionsCar
                                        "check" -> Icons.Default.Check
                                        "stage" -> Icons.Default.Star
                                        "dining" -> Icons.Default.Restaurant
                                        "cocktail" -> Icons.Default.LocalBar
                                        "camera" -> Icons.Default.CameraAlt
                                        "restroom" -> Icons.Default.Wc
                                        else -> Icons.Default.Place
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = poi.name,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected POI Detail Sheet
        selectedPoi?.let { poi ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("selected_poi_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                text = poi.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Venue POI", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = poi.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = poi.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Walking Directions Callout
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Walking Directions", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(poi.walkingDirections, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
