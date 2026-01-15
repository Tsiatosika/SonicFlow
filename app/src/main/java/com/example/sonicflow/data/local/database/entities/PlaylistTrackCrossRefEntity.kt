package com.example.sonicflow.data.local.database.entities

import androidx.room.Entity

@Entity(primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRefEntity(
    val playlistId: Long,
    val trackId: Long
)