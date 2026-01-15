package com.example.sonicflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sonicflow.data.local.dao.*
import com.example.sonicflow.data.local.database.entities.*

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRefEntity::class,
        WaveformDataEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackCrossRefDao(): PlaylistTrackCrossRefDao
    abstract fun waveformDataDao(): WaveformDataDao
}