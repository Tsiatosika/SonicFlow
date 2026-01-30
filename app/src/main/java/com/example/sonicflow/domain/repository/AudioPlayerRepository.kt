package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.PlaybackState
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface AudioPlayerRepository {
    fun getPlaybackState(): Flow<PlaybackState>
    fun getCurrentPlayingTrack(): Flow<Track?>
    suspend fun playTrack(track: Track)
    suspend fun playTrackList(tracks: List<Track>, startIndex: Int = 0)
    suspend fun playPlaylist(playlistId: Long, startIndex: Int = 0)
    suspend fun pause()
    suspend fun resume()
    suspend fun seekTo(position: Long)
    suspend fun skipToNext()
    suspend fun skipToPrevious()
    suspend fun setShuffleModeEnabled(enabled: Boolean)
    suspend fun setRepeatMode(mode: Int)
}