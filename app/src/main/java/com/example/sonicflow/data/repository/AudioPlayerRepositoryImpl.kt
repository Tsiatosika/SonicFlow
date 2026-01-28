package com.example.sonicflow.data.repository

import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.service.AudioPlayerService
import com.example.sonicflow.service.AudioPlayerServiceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@UnstableApi
class AudioPlayerRepositoryImpl @Inject constructor(
    private val serviceManager: AudioPlayerServiceManager
) : AudioPlayerRepository {

    private fun getService(): AudioPlayerService? = serviceManager.getService()

    override fun getPlaybackState(): Flow<AudioPlayerService.PlaybackState> {
        return getService()?.playbackState ?: flow {
            emit(AudioPlayerService.PlaybackState())
        }
    }

    override fun getCurrentPlayingTrack(): Flow<Track?> {
        return getService()?.currentPlayingTrack ?: flow {
            emit(null)
        }
    }

    override suspend fun playTrack(track: Track) {
        getService()?.playTrack(track)
    }

    override suspend fun playPlaylist(playlistId: Long, startIndex: Int) {
        // TODO: Implémenter la lecture de playlist
        // Il faudra récupérer les tracks de la playlist et les passer au service
    }

    override suspend fun pause() {
        getService()?.pause()
    }

    override suspend fun resume() {
        getService()?.resume()
    }

    override suspend fun seekTo(position: Long) {
        getService()?.seekTo(position)
    }

    override suspend fun skipToNext() {
        getService()?.skipToNext()
    }

    override suspend fun skipToPrevious() {
        getService()?.skipToPrevious()
    }

    override suspend fun setShuffleModeEnabled(enabled: Boolean) {
        getService()?.setShuffleModeEnabled(enabled)
    }

    override suspend fun setRepeatMode(mode: Int) {
        getService()?.setRepeatMode(mode)
    }
}