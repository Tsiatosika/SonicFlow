package com.example.sonicflow.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sonicflow.data.local.database.converters.Converters
import com.example.sonicflow.data.local.dao.*
import com.example.sonicflow.data.local.database.entities.*

@Database(
    entities = [
        PlaylistEntity::class,
        TrackEntity::class,
        PlaylistTrackCrossRefEntity::class,
        WaveformDataEntity::class,
        FavoriteEntity::class,
        RecentlyPlayedEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackCrossRefDao(): PlaylistTrackCrossRefDao
    abstract fun waveformDataDao(): WaveformDataDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sonicflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}