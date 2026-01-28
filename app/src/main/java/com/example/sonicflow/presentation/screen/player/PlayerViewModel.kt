package com.example.sonicflow.presentation.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.TrackRepository
import com.example.sonicflow.service.AudioPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi @HiltViewModel
class PlayerViewModel @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository,
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(AudioPlayerService.PlaybackState())
    val playbackState: StateFlow<AudioPlayerService.PlaybackState> = _playbackState.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    init {
        observePlaybackState()
        observeCurrentTrack()
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            audioPlayerRepository.getPlaybackState().collect { state ->
                _playbackState.value = state
                android.util.Log.d("PlayerViewModel", "Playback state: $state")
            }
        }
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            audioPlayerRepository.getCurrentPlayingTrack().collect { track ->
                if (track != null) {
                    _currentTrack.value = track
                    android.util.Log.d("PlayerViewModel", "Current track: ${track.title}")
                }
            }
        }
    }

    fun loadAndPlayTrack(trackId: Long) {
        viewModelScope.launch {
            android.util.Log.d("PlayerViewModel", "Loading track with ID: $trackId")
            trackRepository.getTrackById(trackId).collect { track ->
                track?.let {
                    android.util.Log.d("PlayerViewModel", "Track found: ${it.title}, URI: ${it.uri}")
                    _currentTrack.value = it
                    // Lancer automatiquement la lecture
                    playTrack(it)
                }
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            android.util.Log.d("PlayerViewModel", "Playing track: ${track.title}")
            audioPlayerRepository.playTrack(track)
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            if (_playbackState.value.isPlaying) {
                android.util.Log.d("PlayerViewModel", "Pausing")
                audioPlayerRepository.pause()
            } else {
                android.util.Log.d("PlayerViewModel", "Resuming")
                audioPlayerRepository.resume()
            }
        }
    }

    fun seekTo(position: Long) {
        viewModelScope.launch {
            audioPlayerRepository.seekTo(position)
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            audioPlayerRepository.skipToNext()
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            audioPlayerRepository.skipToPrevious()
        }
    }

    fun toggleShuffle() {
        viewModelScope.launch {
            val newShuffleState = !_isShuffleEnabled.value
            _isShuffleEnabled.value = newShuffleState
            audioPlayerRepository.setShuffleModeEnabled(newShuffleState)
        }
    }

    fun toggleRepeatMode() {
        viewModelScope.launch {
            val newRepeatMode = when (_repeatMode.value) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            _repeatMode.value = newRepeatMode
            audioPlayerRepository.setRepeatMode(newRepeatMode)
        }
    }

    fun formatDuration(millis: Long): String {
        if (millis < 0) return "0:00"

        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = millis / (1000 * 60 * 60)

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}