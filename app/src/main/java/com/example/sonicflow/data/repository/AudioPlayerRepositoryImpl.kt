package com.example.sonicflow.data.repository

import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.domain.model.PlaybackState
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.service.AudioPlayerService
import com.example.sonicflow.service.AudioPlayerServiceConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@UnstableApi
class AudioPlayerRepositoryImpl @Inject constructor(
    private val serviceConnection: AudioPlayerServiceConnection
) : AudioPlayerRepository {

    override fun getPlaybackState(): Flow<PlaybackState> {
        return serviceConnection.getPlaybackState().map { serviceState ->
            // Convertir AudioPlayerService.PlaybackState vers domain.PlaybackState
            PlaybackState(
                isPlaying = serviceState.isPlaying,
                currentPosition = serviceState.currentPosition,
                duration = serviceState.duration,
                bufferedPosition = serviceState.bufferedPosition,
                playbackState = serviceState.playbackState
            )
        }
    }

    override fun getCurrentPlayingTrack(): Flow<Track?> {
        return serviceConnection.getCurrentPlayingTrack()
    }

    override suspend fun playTrack(track: Track) {
        android.util.Log.d("AudioPlayerRepository", "Playing single track: ${track.title}")
        serviceConnection.playTrack(track)
    }

    override suspend fun playTrackList(tracks: List<Track>, startIndex: Int) {
        android.util.Log.d("AudioPlayerRepository", "Playing track list: ${tracks.size} tracks, starting at index $startIndex")
        serviceConnection.playTrackList(tracks, startIndex)
    }

    override suspend fun playPlaylist(playlistId: Long, startIndex: Int) {
        // TODO: Implémenter la lecture de playlist par ID
    }

    override suspend fun pause() {
        serviceConnection.pause()
    }

    override suspend fun resume() {
        serviceConnection.resume()
    }

    override suspend fun seekTo(position: Long) {
        serviceConnection.seekTo(position)
    }

    override suspend fun skipToNext() {
        android.util.Log.d("AudioPlayerRepository", "Skip to next")
        serviceConnection.skipToNext()
    }

    override suspend fun skipToPrevious() {
        android.util.Log.d("AudioPlayerRepository", "Skip to previous")
        serviceConnection.skipToPrevious()
    }

    override suspend fun setShuffleModeEnabled(enabled: Boolean) {
        serviceConnection.setShuffleModeEnabled(enabled)
    }

    override suspend fun setRepeatMode(mode: Int) {
        serviceConnection.setRepeatMode(mode)
    }
}