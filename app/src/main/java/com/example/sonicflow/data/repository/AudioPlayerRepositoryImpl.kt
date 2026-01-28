package com.example.sonicflow.data.repository

import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.service.AudioPlayerService
import com.example.sonicflow.service.AudioPlayerServiceConnection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@UnstableApi
class AudioPlayerRepositoryImpl @Inject constructor(
    private val serviceConnection: AudioPlayerServiceConnection
) : AudioPlayerRepository {

    override fun getPlaybackState(): Flow<AudioPlayerService.PlaybackState> {
        return serviceConnection.getPlaybackState()
    }

    override fun getCurrentPlayingTrack(): Flow<Track?> {
        return serviceConnection.getCurrentPlayingTrack()
    }

    override suspend fun playTrack(track: Track) {
        android.util.Log.d("AudioPlayerRepository", "Playing track: ${track.title}")
        serviceConnection.playTrack(track)
    }

    override suspend fun playPlaylist(playlistId: Long, startIndex: Int) {
        // TODO: Implémenter la lecture de playlist
        // Il faudra récupérer les tracks de la playlist et les passer au service
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
        serviceConnection.skipToNext()
    }

    override suspend fun skipToPrevious() {
        serviceConnection.skipToPrevious()
    }

    override suspend fun setShuffleModeEnabled(enabled: Boolean) {
        serviceConnection.setShuffleModeEnabled(enabled)
    }

    override suspend fun setRepeatMode(mode: Int) {
        serviceConnection.setRepeatMode(mode)
    }
}