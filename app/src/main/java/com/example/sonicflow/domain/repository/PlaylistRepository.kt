package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks>
    suspend fun createPlaylist(name: String): Long
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun deletePlaylist(playlistId: Long)

    data class PlaylistWithTracks(
        val playlist: Playlist,
        val tracks: List<Track>
    )
}