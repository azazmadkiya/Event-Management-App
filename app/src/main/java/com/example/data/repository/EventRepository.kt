package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.SeedDataProvider
import com.example.data.model.AnnouncementEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.ItineraryItemEntity
import com.example.data.model.PhotoMemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EventRepository(private val database: AppDatabase) {
    private val eventDao = database.eventDao()
    private val guestDao = database.guestDao()
    private val announcementDao = database.announcementDao()
    private val itineraryDao = database.itineraryDao()
    private val photoMemoryDao = database.photoMemoryDao()

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    fun getEvent(id: String): Flow<EventEntity?> = eventDao.getEventById(id)

    suspend fun getEventSync(id: String): EventEntity? = withContext(Dispatchers.IO) {
        eventDao.getEventByIdSync(id)
    }

    suspend fun insertEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteAllEvents() = withContext(Dispatchers.IO) {
        eventDao.deleteAllEvents()
    }

    fun getGuests(eventId: String): Flow<List<GuestEntity>> = guestDao.getGuestsForEvent(eventId)

    suspend fun getGuestsSync(eventId: String): List<GuestEntity> = withContext(Dispatchers.IO) {
        guestDao.getGuestsForEventSync(eventId)
    }

    suspend fun insertGuest(guest: GuestEntity) = withContext(Dispatchers.IO) {
        guestDao.insertGuest(guest)
    }

    suspend fun updateGuest(guest: GuestEntity) = withContext(Dispatchers.IO) {
        guestDao.updateGuest(guest.copy(updatedTimestamp = System.currentTimeMillis()))
    }

    suspend fun deleteGuest(guest: GuestEntity) = withContext(Dispatchers.IO) {
        guestDao.deleteGuest(guest)
    }

    suspend fun deleteGuestById(id: String) = withContext(Dispatchers.IO) {
        guestDao.deleteGuestById(id)
    }

    suspend fun updateCheckIn(id: String, isCheckedIn: Boolean) = withContext(Dispatchers.IO) {
        val timestamp = if (isCheckedIn) System.currentTimeMillis() else null
        guestDao.updateCheckIn(id, isCheckedIn, timestamp)
    }

    suspend fun updateRsvpStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        guestDao.updateRsvpStatus(id, status, System.currentTimeMillis())
    }

    fun getAnnouncements(eventId: String): Flow<List<AnnouncementEntity>> =
        announcementDao.getAnnouncementsForEvent(eventId)

    suspend fun insertAnnouncement(announcement: AnnouncementEntity) = withContext(Dispatchers.IO) {
        announcementDao.insertAnnouncement(announcement)
    }

    suspend fun markAnnouncementsRead(eventId: String) = withContext(Dispatchers.IO) {
        announcementDao.markAllAsRead(eventId)
    }

    fun getItinerary(eventId: String): Flow<List<ItineraryItemEntity>> =
        itineraryDao.getItineraryForEvent(eventId)

    suspend fun getItinerarySync(eventId: String): List<ItineraryItemEntity> = withContext(Dispatchers.IO) {
        itineraryDao.getItineraryForEventSync(eventId)
    }

    suspend fun insertItineraryItem(item: ItineraryItemEntity) = withContext(Dispatchers.IO) {
        itineraryDao.insertItem(item)
    }

    fun getPhotos(eventId: String): Flow<List<PhotoMemoryEntity>> =
        photoMemoryDao.getPhotosForEvent(eventId)

    suspend fun insertPhoto(photo: PhotoMemoryEntity) = withContext(Dispatchers.IO) {
        photoMemoryDao.insertPhoto(photo)
    }

    suspend fun togglePhotoLike(photoId: String, currentLikes: Int, isLiked: Boolean) = withContext(Dispatchers.IO) {
        val newLiked = !isLiked
        val newCount = if (newLiked) currentLikes + 1 else maxOf(0, currentLikes - 1)
        photoMemoryDao.updateLikes(photoId, newCount, newLiked)
    }

    suspend fun ensureInitialDataLoaded() = withContext(Dispatchers.IO) {
        if (eventDao.getEventsCount() == 0) {
            AppDatabase.populateInitialData(database)
        }
    }
}
