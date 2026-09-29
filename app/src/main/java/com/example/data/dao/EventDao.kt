package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startTimestamp ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventById(id: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdSync(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventsCount(): Int

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
