package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestDao {
    @Query("SELECT * FROM guests WHERE eventId = :eventId ORDER BY name ASC")
    fun getGuestsForEvent(eventId: String): Flow<List<GuestEntity>>

    @Query("SELECT * FROM guests WHERE eventId = :eventId")
    suspend fun getGuestsForEventSync(eventId: String): List<GuestEntity>

    @Query("SELECT * FROM guests WHERE id = :id LIMIT 1")
    fun getGuestById(id: String): Flow<GuestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: GuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(guests: List<GuestEntity>)

    @Update
    suspend fun updateGuest(guest: GuestEntity)

    @Delete
    suspend fun deleteGuest(guest: GuestEntity)

    @Query("UPDATE guests SET isCheckedIn = :isCheckedIn, checkInTimestamp = :timestamp WHERE id = :id")
    suspend fun updateCheckIn(id: String, isCheckedIn: Boolean, timestamp: Long?)

    @Query("UPDATE guests SET rsvpStatus = :status, updatedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateRsvpStatus(id: String, status: String, timestamp: Long)

    @Query("DELETE FROM guests WHERE id = :id")
    suspend fun deleteGuestById(id: String)
}
