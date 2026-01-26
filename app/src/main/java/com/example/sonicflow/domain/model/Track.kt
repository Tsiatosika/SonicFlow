package com.example.sonicflow.domain.model

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long,
    val uri: String,
    val albumArtUri: String = "",
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val displayName: String,
    val mimeType: String
)