package com.example.sonicflow.data.repository

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.service.AudioPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioPlayerRepositoryImpl @OptIn(UnstableApi::class)
@Inject constructor(
    private val audioPlayerService: AudioPlayerService
) : AudioPlayerRepository {

    @OptIn(UnstableApi::class)
    override fun getPlaybackState(): Flow<AudioPlayerService.PlaybackState> {
        return audioPlayerService.playbackState
    }

    override fun getCurrentPlayingTrack(): Flow<Track?> {
        return audioPlayerService.currentPlayingTrack
    }

    @OptIn(UnstableApi::class)
    override suspend fun playTrack(track: Track) {
        withContext(Dispatchers.Main) {
            audioPlayerService.playTrack(track)
        }
    }

    override suspend fun playPlaylist(playlistId: Long, startIndex: Int) {
        // TODO: Implémenter la lecture de playlist
    }

    @OptIn(UnstableApi::class)
    override suspend fun pause() {
        withContext(Dispatchers.Main) {
            audioPlayerService.pause()
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun resume() {
        withContext(Dispatchers.Main) {
            audioPlayerService.resume()
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun seekTo(position: Long) {
        withContext(Dispatchers.Main) {
            audioPlayerService.seekTo(position)
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun skipToNext() {
        withContext(Dispatchers.Main) {
            audioPlayerService.skipToNext()
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun skipToPrevious() {
        withContext(Dispatchers.Main) {
            audioPlayerService.skipToPrevious()
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun setShuffleModeEnabled(enabled: Boolean) {
        withContext(Dispatchers.Main) {
            audioPlayerService.setShuffleModeEnabled(enabled)
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun setRepeatMode(mode: Int) {
        withContext(Dispatchers.Main) {
            audioPlayerService.setRepeatMode(mode)
        }
    }
}