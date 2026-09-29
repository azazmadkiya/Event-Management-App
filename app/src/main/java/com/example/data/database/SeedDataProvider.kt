package com.example.data.database

import com.example.data.model.AnnouncementEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.ItineraryItemEntity
import com.example.data.model.PhotoMemoryEntity
import com.example.data.model.VenuePoi
import java.util.Calendar

object SeedDataProvider {
    const val EVENT_ID_DEFAULT = "event_default"

    fun getInitialEvents(): List<EventEntity> {
        return emptyList()
    }

    fun getInitialGuests(eventId: String): List<GuestEntity> {
        return emptyList()
    }

    fun getInitialItinerary(eventId: String): List<ItineraryItemEntity> {
        return emptyList()
    }

    fun getInitialAnnouncements(eventId: String): List<AnnouncementEntity> {
        return emptyList()
    }

    fun getInitialPhotos(eventId: String): List<PhotoMemoryEntity> {
        return emptyList()
    }

    fun getVenuePoints(): List<VenuePoi> {
        return emptyList()
    }
}
