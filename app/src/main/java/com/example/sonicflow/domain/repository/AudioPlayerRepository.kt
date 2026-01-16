package com.example.sonicflow.domain.repository

import android.support.v4.media.session.PlaybackStateCompat
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface AudioPlayerRepository {
    fun getPlaybackState(): Flow<PlaybackStateCompat>
    fun getCurrentPlayingTrack(): Flow<Track?>
    suspend fun playTrack(track: Track)
    suspend fun playPlaylist(playlistId: Long, startIndex: Int = 0)
    suspend fun pause()
    suspend fun resume()
    suspend fun seekTo(position: Long)
    suspend fun skipToNext()
    suspend fun skipToPrevious()
    suspend fun setShuffleModeEnabled(enabled: Boolean)
    suspend fun setRepeatMode(mode: Int)
}