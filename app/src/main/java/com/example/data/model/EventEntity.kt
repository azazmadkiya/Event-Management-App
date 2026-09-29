package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val eventType: String, // Wedding, Gala, Birthday, Conference, etc.
    val startTimestamp: Long,
    val endTimestamp: Long,
    val venueName: String,
    val venueAddress: String,
    val latitude: Double = 34.0736,
    val longitude: Double = -118.4004,
    val description: String,
    val dressCode: String = "Formal / Black Tie Optional",
    val hostName: String,
    val hostContact: String,
    val rsvpDeadlineTimestamp: Long,
    val maxCapacity: Int = 120,
    val coverGradientStart: Long = 0xFF4F46E5,
    val coverGradientEnd: Long = 0xFF7C3AED,
    val travelInfo: String = "Valet parking available at Main Gate. Group rate room block reserved at Grand Plaza Hotel under code EVENTVITE26.",
    val shuttleSchedule: String = "Complimentary hotel shuttle departs every 20 mins between 3:30 PM - 11:30 PM."
)
