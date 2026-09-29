package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "SCHEDULE_UPDATE", // "SCHEDULE_UPDATE", "VENUE_ALERT", "MILESTONE", "REMINDER"
    val isUrgent: Boolean = false,
    val isRead: Boolean = false
)
