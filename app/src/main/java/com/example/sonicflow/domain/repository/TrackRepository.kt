package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAllTracks(): Flow<List<Track>>
    fun searchTracks(query: String): Flow<List<Track>>
    fun getTrackById(id: Long): Flow<Track?>
    suspend fun refreshTracks()
}