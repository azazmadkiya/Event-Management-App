package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AnnouncementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getAnnouncementsForEvent(eventId: String): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(announcements: List<AnnouncementEntity>)

    @Query("UPDATE announcements SET isRead = 1 WHERE eventId = :eventId")
    suspend fun markAllAsRead(eventId: String)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)
}
