package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.GuestEntity
import com.example.data.model.RsvpStatus
import com.example.ui.navigation.AppBottomNav
import com.example.ui.navigation.AppNavRail
import com.example.ui.screens.SplashScreen
import com.example.ui.navigation.AppScreen
import com.example.ui.navigation.EventViteTopBar
import com.example.ui.screens.AddEditGuestDialog
import com.example.ui.screens.EventDetailsScreen
import com.example.ui.screens.GuestListScreen
import com.example.ui.screens.SecurityLockScreen
import com.example.ui.theme.EventViteTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.EventViewModel
import com.example.util.NotificationHelper

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannels(this)

        setContent {
            val viewModel: EventViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            // Request Notification Permission on Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* granted / denied */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            EventViteTheme(themeMode = themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: EventViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    var currentScreen by remember { mutableStateOf(AppScreen.EVENT) }

    val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()
    val currentEvent by viewModel.currentEvent.collectAsStateWithLifecycle()
    val rawGuests by viewModel.rawGuests.collectAsStateWithLifecycle()
    val filteredGuests by viewModel.filteredGuests.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedRsvpTab by viewModel.selectedRsvpTab.collectAsStateWithLifecycle()
    val selectedDietaryFilter by viewModel.selectedDietaryFilter.collectAsStateWithLifecycle()
    val selectedInviteChannelFilter by viewModel.selectedInviteChannelFilter.collectAsStateWithLifecycle()

    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val isSecurityLockEnabled by viewModel.isSecurityLockEnabled.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    var showAddGuestDialog by remember { mutableStateOf(false) }
    var guestToEdit by remember { mutableStateOf<GuestEntity?>(null) }
    var showSecurityPrompt by remember { mutableStateOf(false) }

    // Handle back button: return to EVENT screen if on GUESTS
    BackHandler(enabled = currentScreen != AppScreen.EVENT || showSecurityPrompt) {
        if (showSecurityPrompt) {
            showSecurityPrompt = false
        } else {
            currentScreen = AppScreen.EVENT
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                EventViteTopBar(
                    currentEvent = currentEvent,
                    allEvents = allEvents,
                    onSelectEvent = { viewModel.selectEvent(it) },
                    onAddEventClick = { currentScreen = AppScreen.EVENT },
                    isUnlocked = isUnlocked,
                    isSecurityEnabled = isSecurityLockEnabled,
                    onToggleLock = {
                        if (isUnlocked) {
                            viewModel.lockSession()
                        } else {
                            showSecurityPrompt = true
                        }
                    },
                    themeMode = themeMode,
                    onCycleTheme = {
                        val next = when (themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.SYSTEM
                        }
                        viewModel.themeMode.value = next
                    },
                    onClearAllDataClick = {
                        viewModel.deleteAllEvents()
                    }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    AppBottomNav(
                        currentScreen = currentScreen,
                        onScreenSelected = { currentScreen = it }
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    AppNavRail(
                        currentScreen = currentScreen,
                        onScreenSelected = { currentScreen = it }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showSecurityPrompt && !isUnlocked) {
                        SecurityLockScreen(
                            isSecurityEnabled = isSecurityLockEnabled,
                            onUnlockSuccess = {
                                viewModel.unlockDirectly()
                                showSecurityPrompt = false
                            },
                            onToggleSecurity = { enabled ->
                                viewModel.toggleSecurityLock(enabled, context)
                            }
                        )
                    } else {
                        when (currentScreen) {
                            AppScreen.EVENT -> {
                                EventDetailsScreen(
                                    currentEvent = currentEvent,
                                    onUpdateEvent = { updated ->
                                        viewModel.updateEvent(updated)
                                    },
                                    onDeleteEvent = { event ->
                                        viewModel.deleteEvent(event)
                                    },
                                    onAddEvent = { newEvent ->
                                        viewModel.insertEvent(newEvent)
                                    }
                                )
                            }
                            AppScreen.GUESTS -> {
                                GuestListScreen(
                                    currentEvent = currentEvent,
                                    guests = filteredGuests,
                                    allRawGuests = rawGuests,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                                    selectedRsvpTab = selectedRsvpTab,
                                    onRsvpTabChange = { viewModel.selectedRsvpTab.value = it },
                                    selectedDietaryFilter = selectedDietaryFilter,
                                    onDietaryFilterChange = { viewModel.selectedDietaryFilter.value = it },
                                    selectedInviteChannelFilter = selectedInviteChannelFilter,
                                    onInviteChannelFilterChange = { viewModel.selectedInviteChannelFilter.value = it },
                                    isSecurityLocked = isSecurityLockEnabled && !isUnlocked,
                                    onUnlockRequest = { showSecurityPrompt = true },
                                    onAddGuestClick = {
                                        guestToEdit = null
                                        showAddGuestDialog = true
                                    },
                                    onEditGuestClick = { guest ->
                                        guestToEdit = guest
                                        showAddGuestDialog = true
                                    },
                                    onDeleteGuestClick = { guest ->
                                        viewModel.deleteGuest(guest)
                                    },
                                    onToggleCheckIn = { guest ->
                                        viewModel.toggleCheckIn(guest)
                                    },
                                    onUpdateRsvpStatus = { guestId, status ->
                                        viewModel.updateRsvpStatus(guestId, status)
                                    },
                                    onUpdateInviteChannel = { guestId, channel ->
                                        viewModel.updateInviteChannel(guestId, channel)
                                    },
                                    onBackupGuests = { uri ->
                                        viewModel.backupGuests(context, uri)
                                    },
                                    onRestoreGuests = { uri ->
                                        viewModel.restoreGuests(context, uri)
                                    },
                                    onImportPhoneContacts = { newGuests ->
                                        newGuests.forEach { guest ->
                                            viewModel.insertGuest(guest)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Guest Dialog
    if (showAddGuestDialog && currentEvent != null) {
        AddEditGuestDialog(
            eventId = currentEvent!!.id,
            guestToEdit = guestToEdit,
            onDismiss = {
                showAddGuestDialog = false
                guestToEdit = null
            },
            onSaveGuest = { guest ->
                if (guestToEdit != null) {
                    viewModel.updateGuest(guest)
                } else {
                    viewModel.insertGuest(guest)
                }
                showAddGuestDialog = false
                guestToEdit = null
            }
        )
    }
}
