package com.example.sonicflow.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long,
    val uri: String,
    val albumArtUri: String? = null,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val displayName: String,
    val mimeType: String
)