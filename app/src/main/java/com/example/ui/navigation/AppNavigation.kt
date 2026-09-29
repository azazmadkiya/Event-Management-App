package com.example.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.ui.theme.ThemeMode

enum class AppScreen(val label: String, val icon: ImageVector) {
    EVENT("Event", Icons.Default.Event),
    GUESTS("Guests", Icons.Default.People)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventViteTopBar(
    currentEvent: EventEntity?,
    allEvents: List<EventEntity>,
    onSelectEvent: (String) -> Unit,
    onAddEventClick: () -> Unit,
    isUnlocked: Boolean,
    isSecurityEnabled: Boolean,
    onToggleLock: () -> Unit,
    themeMode: ThemeMode,
    onCycleTheme: () -> Unit,
    onClearAllDataClick: () -> Unit = {}
) {
    var eventMenuExpanded by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                onClick = { eventMenuExpanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentEvent?.title ?: "Event Planner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Switch Event",
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = eventMenuExpanded,
                    onDismissRequest = { eventMenuExpanded = false }
                ) {
                    allEvents.forEach { event ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = event.title,
                                    fontWeight = if (event.id == currentEvent?.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSelectEvent(event.id)
                                eventMenuExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ Add New Event", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            eventMenuExpanded = false
                            onAddEventClick()
                        }
                    )
                    if (allEvents.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Delete All Default Data", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                eventMenuExpanded = false
                                showClearDataDialog = true
                            }
                        )
                    }
                }
            }

            if (showClearDataDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showClearDataDialog = false },
                    title = { Text("Delete All Event Data?") },
                    text = { Text("Are you sure you want to remove all default/saved events and start with clean data?") },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                showClearDataDialog = false
                                onClearAllDataClick()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete All")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.OutlinedButton(onClick = { showClearDataDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        },
        actions = {
            // Security Vault Status
            if (isSecurityEnabled) {
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier.testTag("topbar_security_btn")
                ) {
                    Icon(
                        if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = if (isUnlocked) "Vault Unlocked" else "Vault Locked",
                        tint = if (isUnlocked) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Dark Mode Switcher
            IconButton(
                onClick = onCycleTheme,
                modifier = Modifier.testTag("topbar_theme_btn")
            ) {
                val icon = when (themeMode) {
                    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                }
                Icon(icon, contentDescription = "Toggle Theme")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun AppBottomNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("app_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val primaryScreens = listOf(
            AppScreen.EVENT,
            AppScreen.GUESTS
        )

        primaryScreens.forEach { screen ->
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label, fontSize = 12.sp) },
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}

@Composable
fun AppNavRail(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight().testTag("app_nav_rail"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AppScreen.entries.forEach { screen ->
            NavigationRailItem(
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                modifier = Modifier.testTag("rail_item_${screen.name.lowercase()}")
            )
        }
    }
}
