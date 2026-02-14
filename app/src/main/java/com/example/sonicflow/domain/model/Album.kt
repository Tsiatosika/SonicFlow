package com.example.sonicflow.domain.model

import android.net.Uri

data class Album(
    val name: String,
    val artist: String,
    val trackCount: Int,
    val year: Int = 0,
    val tracks: List<Track> = emptyList(),
    val albumArtUri: Uri? = tracks.firstOrNull()?.albumArtUri
)