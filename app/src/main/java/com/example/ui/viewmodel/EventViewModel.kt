package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.SeedDataProvider
import com.example.data.model.InviteChannel
import com.example.data.model.AnnouncementEntity
import com.example.data.model.DietaryPreference
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.ItineraryItemEntity
import com.example.data.model.PhotoMemoryEntity
import com.example.data.model.RsvpStatus
import com.example.data.model.VenuePoi
import com.example.data.repository.EventRepository
import com.example.ui.theme.ThemeMode
import com.example.util.BiometricHelper
import com.example.util.GuestBackupHelper
import com.example.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository

    val allEvents: StateFlow<List<EventEntity>>
    private val _activeEventId = MutableStateFlow(SeedDataProvider.EVENT_ID_DEFAULT)
    val activeEventId: StateFlow<String> = _activeEventId.asStateFlow()

    val currentEvent: StateFlow<EventEntity?>
    val rawGuests: StateFlow<List<GuestEntity>>
    val announcements: StateFlow<List<AnnouncementEntity>>
    val itinerary: StateFlow<List<ItineraryItemEntity>>
    val photos: StateFlow<List<PhotoMemoryEntity>>

    // Filtering & Search
    val searchQuery = MutableStateFlow("")
    val selectedRsvpTab = MutableStateFlow("ALL") // "ALL", "ATTENDING", "PENDING", "DECLINED", "NOT_INVITED", "CHECKLIST"
    val selectedDietaryFilter = MutableStateFlow<String?>(null)
    val selectedInviteChannelFilter = MutableStateFlow<String?>(null) // null, "STANDARD", "WHATSAPP_ONLY"

    val filteredGuests: StateFlow<List<GuestEntity>>

    // Security & Biometric Vault
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isSecurityLockEnabled = MutableStateFlow(true)
    val isSecurityLockEnabled: StateFlow<Boolean> = _isSecurityLockEnabled.asStateFlow()

    // Cloud Synchronization
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncedTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncedTimestamp: StateFlow<Long> = _lastSyncedTimestamp.asStateFlow()

    // Theme Mode (System, Light, Dark)
    val themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    // Venue POIs
    val venuePois: List<VenuePoi> = SeedDataProvider.getVenuePoints()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = EventRepository(database)

        allEvents = repository.allEvents.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        currentEvent = _activeEventId.flatMapLatest { eventId ->
            repository.getEvent(eventId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        rawGuests = _activeEventId.flatMapLatest { eventId ->
            repository.getGuests(eventId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        announcements = _activeEventId.flatMapLatest { eventId ->
            repository.getAnnouncements(eventId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        itinerary = _activeEventId.flatMapLatest { eventId ->
            repository.getItinerary(eventId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        photos = _activeEventId.flatMapLatest { eventId ->
            repository.getPhotos(eventId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Combine for filtered guests list
        filteredGuests = combine(
            rawGuests,
            searchQuery,
            selectedRsvpTab,
            selectedInviteChannelFilter
        ) { guests, query, tab, inviteChannelFilter ->
            guests.filter { guest ->
                val matchesQuery = if (query.isBlank()) true else {
                    guest.name.contains(query, ignoreCase = true) ||
                            guest.phoneNumber.contains(query, ignoreCase = true) ||
                            guest.whatsAppNumber.contains(query, ignoreCase = true) ||
                            guest.email.contains(query, ignoreCase = true) ||
                            guest.tableNumber.contains(query, ignoreCase = true) ||
                            guest.personType.contains(query, ignoreCase = true)
                }

                val matchesTab = when (tab) {
                    "ALL" -> true
                    "ATTENDING" -> guest.rsvpStatus == RsvpStatus.ATTENDING.name
                    "INVITED" -> guest.rsvpStatus == RsvpStatus.INVITED.name
                    "PENDING" -> guest.rsvpStatus == RsvpStatus.PENDING.name
                    "DECLINED" -> guest.rsvpStatus == RsvpStatus.DECLINED.name
                    "NOT_INVITED" -> guest.rsvpStatus == RsvpStatus.NOT_INVITED.name
                    "CHECKLIST" -> true
                    else -> true
                }

                val matchesChannel = if (inviteChannelFilter == null) true else {
                    guest.inviteChannel.equals(inviteChannelFilter, ignoreCase = true)
                }

                matchesQuery && matchesTab && matchesChannel
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        _isSecurityLockEnabled.value = BiometricHelper.isSecurityLockEnabled(application)

        // Seed initial data if fresh launch
        viewModelScope.launch {
            repository.ensureInitialDataLoaded()
            allEvents.collect { events ->
                if (events.isNotEmpty() && events.none { it.id == _activeEventId.value }) {
                    _activeEventId.value = events.first().id
                }
            }
        }
    }

    fun selectEvent(eventId: String) {
        _activeEventId.value = eventId
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun insertEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
            _activeEventId.value = event.id
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun deleteAllEvents() {
        viewModelScope.launch {
            repository.deleteAllEvents()
        }
    }

    fun insertGuest(guest: GuestEntity) {
        viewModelScope.launch {
            repository.insertGuest(guest)
        }
    }

    fun updateGuest(guest: GuestEntity) {
        viewModelScope.launch {
            repository.updateGuest(guest)
        }
    }

    fun deleteGuest(guest: GuestEntity) {
        viewModelScope.launch {
            repository.deleteGuest(guest)
        }
    }

    fun toggleCheckIn(guest: GuestEntity) {
        viewModelScope.launch {
            repository.updateCheckIn(guest.id, !guest.isCheckedIn)
        }
    }

    fun updateRsvpStatus(guestId: String, status: RsvpStatus) {
        viewModelScope.launch {
            repository.updateRsvpStatus(guestId, status.name)
        }
    }

    fun createAnnouncement(
        title: String,
        message: String,
        isUrgent: Boolean,
        category: String,
        context: Context
    ) {
        val announcement = AnnouncementEntity(
            id = "ann_${UUID.randomUUID()}",
            eventId = _activeEventId.value,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            category = category,
            isUrgent = isUrgent,
            isRead = false
        )
        viewModelScope.launch {
            repository.insertAnnouncement(announcement)
            NotificationHelper.showNotification(
                context = context,
                notificationId = System.currentTimeMillis().toInt(),
                title = if (isUrgent) "🚨 URGENT: $title" else "📢 Event Update: $title",
                message = message,
                channelId = if (isUrgent) NotificationHelper.CHANNEL_ANNOUNCEMENTS else NotificationHelper.CHANNEL_UPDATES,
                isUrgent = isUrgent
            )
        }
    }

    fun markAnnouncementsRead() {
        viewModelScope.launch {
            repository.markAnnouncementsRead(_activeEventId.value)
        }
    }

    fun addItineraryItem(item: ItineraryItemEntity) {
        viewModelScope.launch {
            repository.insertItineraryItem(item)
        }
    }

    fun uploadPhoto(imageUri: String, caption: String, uploaderName: String) {
        val photo = PhotoMemoryEntity(
            id = "photo_${UUID.randomUUID()}",
            eventId = _activeEventId.value,
            imageUri = imageUri,
            caption = caption,
            uploaderName = if (uploaderName.isBlank()) "Guest" else uploaderName,
            timestamp = System.currentTimeMillis(),
            likesCount = 1,
            isLikedByUser = true
        )
        viewModelScope.launch {
            repository.insertPhoto(photo)
        }
    }

    fun togglePhotoLike(photo: PhotoMemoryEntity) {
        viewModelScope.launch {
            repository.togglePhotoLike(photo.id, photo.likesCount, photo.isLikedByUser)
        }
    }

    // Biometrics & PIN
    fun unlockWithPin(pin: String, context: Context): Boolean {
        val success = BiometricHelper.verifyPin(context, pin)
        if (success) {
            _isUnlocked.value = true
        }
        return success
    }

    fun unlockDirectly() {
        _isUnlocked.value = true
    }

    fun lockSession() {
        _isUnlocked.value = false
    }

    fun toggleSecurityLock(enabled: Boolean, context: Context) {
        _isSecurityLockEnabled.value = enabled
        BiometricHelper.setSecurityLockEnabled(context, enabled)
    }

    // Cloud Sync Simulation
    fun syncWithCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            delay(1200) // realistic cloud delta sync
            _lastSyncedTimestamp.value = System.currentTimeMillis()
            _isSyncing.value = false
        }
    }

    fun backupGuests(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = GuestBackupHelper.exportToJson(rawGuests.value)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                android.widget.Toast.makeText(context, "Guest backup exported successfully", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun restoreGuests(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (jsonString != null) {
                    val guests = GuestBackupHelper.importFromJson(jsonString)
                    if (guests != null) {
                        val currentEvId = _activeEventId.value
                        guests.forEach { guest ->
                            repository.insertGuest(guest.copy(eventId = currentEvId))
                        }
                        android.widget.Toast.makeText(context, "Restored ${guests.size} guests successfully", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Invalid backup file format", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Restore failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateInviteChannel(guestId: String, channel: InviteChannel) {
        viewModelScope.launch {
            val currentGuest = rawGuests.value.find { it.id == guestId }
            if (currentGuest != null) {
                val updated = currentGuest.copy(inviteChannel = channel.name, updatedTimestamp = System.currentTimeMillis())
                repository.updateGuest(updated)
            }
        }
    }
}
