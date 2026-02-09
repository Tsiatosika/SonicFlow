package com.example.sonicflow.presentation.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.PlaybackState
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // ✅ SUPPRIMÉ isVisible - on se base uniquement sur currentTrack != null

    init {
        observeCurrentTrack()
        observePlaybackState()
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            audioPlayerRepository.getCurrentPlayingTrack().collect { track ->
                _currentTrack.value = track
                if (track != null) {
                    android.util.Log.d("MiniPlayerViewModel", "Track updated: ${track.title}")
                }
            }
        }
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            audioPlayerRepository.getPlaybackState().collect { state ->
                _playbackState.value = state
            }
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            val wasPlaying = _playbackState.value.isPlaying
            if (wasPlaying) {
                audioPlayerRepository.pause()
            } else {
                audioPlayerRepository.resume()
            }
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
}