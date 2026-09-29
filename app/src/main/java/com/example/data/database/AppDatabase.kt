package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AnnouncementDao
import com.example.data.dao.EventDao
import com.example.data.dao.GuestDao
import com.example.data.dao.ItineraryDao
import com.example.data.dao.PhotoMemoryDao
import com.example.data.model.AnnouncementEntity
import com.example.data.model.EventEntity
import com.example.data.model.GuestEntity
import com.example.data.model.ItineraryItemEntity
import com.example.data.model.PhotoMemoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EventEntity::class,
        GuestEntity::class,
        AnnouncementEntity::class,
        ItineraryItemEntity::class,
        PhotoMemoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun guestDao(): GuestDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun itineraryDao(): ItineraryDao
    abstract fun photoMemoryDao(): PhotoMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eventvite_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val events = SeedDataProvider.getInitialEvents()
            database.eventDao().insertAll(events)

            val eventId = events.firstOrNull()?.id ?: SeedDataProvider.EVENT_ID_DEFAULT

            val guests = SeedDataProvider.getInitialGuests(eventId)
            if (guests.isNotEmpty()) database.guestDao().insertAll(guests)

            val itinerary = SeedDataProvider.getInitialItinerary(eventId)
            if (itinerary.isNotEmpty()) database.itineraryDao().insertAll(itinerary)

            val announcements = SeedDataProvider.getInitialAnnouncements(eventId)
            if (announcements.isNotEmpty()) database.announcementDao().insertAll(announcements)

            val photos = SeedDataProvider.getInitialPhotos(eventId)
            if (photos.isNotEmpty()) database.photoMemoryDao().insertAll(photos)
        }
    }
}
