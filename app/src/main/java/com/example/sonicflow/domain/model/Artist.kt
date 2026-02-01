package com.example.sonicflow.domain.model

data class Artist(
    val name: String,
    val trackCount: Int,
    val tracks: List<Track> = emptyList()
)