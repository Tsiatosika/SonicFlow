package com.example.sonicflow.domain.model

data class Album(
    val name: String,
    val artist: String,
    val trackCount: Int,
    val year: Int = 0,
    val tracks: List<Track> = emptyList()
)