package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_memories")
data class PhotoMemoryEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val imageUri: String, // content:// URI or preset tag
    val caption: String,
    val uploaderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByUser: Boolean = false,
    val presetDrawableKey: String = "" // used for built-in sample memories
)
