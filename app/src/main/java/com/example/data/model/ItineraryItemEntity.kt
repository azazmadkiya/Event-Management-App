package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itinerary_items")
data class ItineraryItemEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val orderIndex: Int,
    val timeLabel: String, // e.g. "4:30 PM"
    val title: String, // e.g. "Guest Arrival & Welcome Cocktail"
    val location: String, // e.g. "Glass Pavilion Promenade"
    val description: String = "",
    val iconKey: String = "cocktail", // cocktail, ceremony, dinner, music, toast, farewell
    val startEpochMillis: Long,
    val endEpochMillis: Long
)
