package com.example.sonicflow.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: String,
    val album: String?,
    val albumArtUri: String?
)