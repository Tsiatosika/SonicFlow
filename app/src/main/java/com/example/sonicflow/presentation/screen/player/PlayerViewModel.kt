package com.example.sonicflow.presentation.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.example.sonicflow.domain.model.PlaybackState
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.FavoriteRepository
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository,
    private val trackRepository: TrackRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _favoriteTracks = MutableStateFlow<Set<Long>>(emptySet())

    init {
        observePlaybackState()
        observeCurrentTrack()
        observeFavorites()
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
                    updateFavoriteStatus(track.id)
                    android.util.Log.d("PlayerViewModel", "Current track: ${track.title}")
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavoriteTracks().collect { favoriteTracks ->
                _favoriteTracks.value = favoriteTracks.map { it.id }.toSet()
                android.util.Log.d("PlayerViewModel", "Favorites updated: ${favoriteTracks.size}")

                // Mettre à jour le statut favori du morceau actuel
                _currentTrack.value?.let { track ->
                    updateFavoriteStatus(track.id)
                }
            }
        }
    }

    private fun updateFavoriteStatus(trackId: Long) {
        _isFavorite.value = _favoriteTracks.value.contains(trackId)
        android.util.Log.d("PlayerViewModel", "Track $trackId is favorite: ${_isFavorite.value}")
    }

    fun loadAndPlayTrack(trackId: Long) {
        viewModelScope.launch {
            android.util.Log.d("PlayerViewModel", "Loading track with ID: $trackId")
            trackRepository.getTrackById(trackId).collect { track ->
                track?.let {
                    android.util.Log.d("PlayerViewModel", "Track found: ${it.title}, URI: ${it.uri}")
                    _currentTrack.value = it
                    updateFavoriteStatus(it.id)
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

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch {
            try {
                val isFavorite = _favoriteTracks.value.contains(trackId)

                if (isFavorite) {
                    android.util.Log.d("PlayerViewModel", "Removing track $trackId from favorites")
                    favoriteRepository.removeFromFavorites(trackId)
                } else {
                    android.util.Log.d("PlayerViewModel", "Adding track $trackId to favorites")
                    favoriteRepository.addToFavorites(trackId)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "Error toggling favorite", e)
            }
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