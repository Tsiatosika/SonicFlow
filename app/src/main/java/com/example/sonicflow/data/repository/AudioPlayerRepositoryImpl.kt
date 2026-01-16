package com.example.sonicflow.data.repository

import android.support.v4.media.session.PlaybackStateCompat
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.service.AudioPlayerService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AudioPlayerRepositoryImpl @Inject constructor(
    private val audioPlayerService: AudioPlayerService
) : AudioPlayerRepository {

    override fun getPlaybackState(): Flow<PlaybackStateCompat> {
        return audioPlayerService.playbackState
    }

    override fun getCurrentPlayingTrack(): Flow<Track?> {
        return audioPlayerService.currentPlayingTrack
    }

    override suspend fun playTrack(track: Track) {
        audioPlayerService.playTrack(track)
    }

    override suspend fun playPlaylist(playlistId: Long, startIndex: Int) {
        // Implémenter la lecture d'une playlist
    }

    override suspend fun pause() {
        audioPlayerService.pause()
    }

    override suspend fun resume() {
        audioPlayerService.resume()
    }

    override suspend fun seekTo(position: Long) {
        audioPlayerService.seekTo(position)
    }

    override suspend fun skipToNext() {
        audioPlayerService.skipToNext()
    }

    override suspend fun skipToPrevious() {
        audioPlayerService.skipToPrevious()
    }

    override suspend fun setShuffleModeEnabled(enabled: Boolean) {
        audioPlayerService.setShuffleModeEnabled(enabled)
    }

    override suspend fun setRepeatMode(mode: Int) {
        audioPlayerService.setRepeatMode(mode)
    }
}