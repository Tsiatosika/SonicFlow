package com.example.sonicflow.domain.model

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long,
    val uri: String,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val displayName: String,
    val mimeType: String,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val albumArtUri: Uri? = null
)