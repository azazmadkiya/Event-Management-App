package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PhotoMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoMemoryDao {
    @Query("SELECT * FROM photo_memories WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getPhotosForEvent(eventId: String): Flow<List<PhotoMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PhotoMemoryEntity>)

    @Query("UPDATE photo_memories SET likesCount = :likesCount, isLikedByUser = :isLiked WHERE id = :id")
    suspend fun updateLikes(id: String, likesCount: Int, isLiked: Boolean)

    @Delete
    suspend fun deletePhoto(photo: PhotoMemoryEntity)
}
