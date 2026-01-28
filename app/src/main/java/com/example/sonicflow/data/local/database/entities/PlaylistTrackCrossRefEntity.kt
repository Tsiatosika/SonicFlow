package com.example.sonicflow.data.local.database.entities

import androidx.room.Entity

@Entity(
    tableName = "playlist_track_cross_ref",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRefEntity(
    val playlistId: Long,
    val trackId: Long,
    val position: Int = 0,
    val dateAdded: Long = System.currentTimeMillis()
)