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
import kotlinx.coroutines.flow.take
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

    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    init {
        observePlaybackState()
        observeCurrentTrack()
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            audioPlayerRepository.getPlaybackState().collect { state ->
                _playbackState.value = state
                if (state.duration > 0) {
                    _isVisible.value = true
                }
            }
        }
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            audioPlayerRepository.getCurrentPlayingTrack().collect { track ->
                if (track != null) {
                    _currentTrack.value = track
                    _isVisible.value = true
                }
            }
        }
    }

    // Appelé depuis MiniPlayer quand hideOnPlayer passe à false
    fun refreshState() {
        viewModelScope.launch {
            audioPlayerRepository.getCurrentPlayingTrack().take(1).collect { track ->
                if (track != null) {
                    _currentTrack.value = track
                    _isVisible.value = true
                }
            }
        }
        viewModelScope.launch {
            audioPlayerRepository.getPlaybackState().take(1).collect { state ->
                _playbackState.value = state
                if (state.duration > 0) {
                    _isVisible.value = true
                }
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
            // Mise à jour locale immédiate du bouton
            _playbackState.value = _playbackState.value.copy(isPlaying = !wasPlaying)
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            audioPlayerRepository.skipToNext()
            // Petit délai pour que le repository ait le temps de mettre à jour le morceau
            kotlinx.coroutines.delay(300)
            // Relire depuis le repository
            audioPlayerRepository.getCurrentPlayingTrack().take(1).collect { track ->
                if (track != null) {
                    _currentTrack.value = track
                }
            }
            audioPlayerRepository.getPlaybackState().take(1).collect { state ->
                _playbackState.value = state
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            audioPlayerRepository.skipToPrevious()
            // Petit délai pour que le repository ait le temps de mettre à jour le morceau
            kotlinx.coroutines.delay(300)
            // Relire depuis le repository
            audioPlayerRepository.getCurrentPlayingTrack().take(1).collect { track ->
                if (track != null) {
                    _currentTrack.value = track
                }
            }
            audioPlayerRepository.getPlaybackState().take(1).collect { state ->
                _playbackState.value = state
            }
        }
    }

    fun hide() {
        _isVisible.value = false
    }

    fun show() {
        if (_currentTrack.value != null) {
            _isVisible.value = true
        }
    }
}