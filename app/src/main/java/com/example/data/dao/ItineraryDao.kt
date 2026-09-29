package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ItineraryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItineraryDao {
    @Query("SELECT * FROM itinerary_items WHERE eventId = :eventId ORDER BY orderIndex ASC, startEpochMillis ASC")
    fun getItineraryForEvent(eventId: String): Flow<List<ItineraryItemEntity>>

    @Query("SELECT * FROM itinerary_items WHERE eventId = :eventId ORDER BY orderIndex ASC, startEpochMillis ASC")
    suspend fun getItineraryForEventSync(eventId: String): List<ItineraryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItineraryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItineraryItemEntity>)

    @Delete
    suspend fun deleteItem(item: ItineraryItemEntity)
}
