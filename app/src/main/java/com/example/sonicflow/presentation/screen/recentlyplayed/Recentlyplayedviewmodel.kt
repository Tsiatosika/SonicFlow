package com.example.sonicflow.presentation.screen.recentlyplayed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.RecentlyPlayedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    private val recentlyPlayedRepository: RecentlyPlayedRepository,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    private val _recentlyPlayedTracks = MutableStateFlow<List<Track>>(emptyList())
    val recentlyPlayedTracks: StateFlow<List<Track>> = _recentlyPlayedTracks.asStateFlow()

    private val _currentPlayingTrack = MutableStateFlow<Track?>(null)
    val currentPlayingTrack: StateFlow<Track?> = _currentPlayingTrack.asStateFlow()

    // ✅ FIX : isLoading commence à true et passe à false après la première émission
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRecentlyPlayed()
        observeCurrentTrack()
    }

    private fun loadRecentlyPlayed() {
        viewModelScope.launch {
            try {
                // ✅ FIX : Le collect est non-bloquant dans une coroutine.
                // isLoading passe à false dès la première émission de données (même vide).
                recentlyPlayedRepository.getRecentlyPlayedTracks(50).collect { tracks ->
                    _recentlyPlayedTracks.value = tracks
                    _isLoading.value = false  // ✅ Toujours atteint après la 1ère émission
                    android.util.Log.d("RecentlyPlayedVM", "Loaded ${tracks.size} recently played tracks")
                }
            } catch (e: Exception) {
                android.util.Log.e("RecentlyPlayedVM", "Error loading recently played", e)
                _isLoading.value = false  // ✅ Ne pas bloquer l'UI en cas d'erreur
            }
        }
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            audioPlayerRepository.getCurrentPlayingTrack().collect { track ->
                _currentPlayingTrack.value = track
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                val currentList = _recentlyPlayedTracks.value
                val index = currentList.indexOf(track)
                // ✅ FIX : Si index == -1 (track pas dans la liste), jouer directement
                if (index != -1) {
                    audioPlayerRepository.playTrackList(currentList, index)
                } else {
                    audioPlayerRepository.playTrack(track)
                }
            } catch (e: Exception) {
                android.util.Log.e("RecentlyPlayedVM", "Error playing track", e)
            }
        }
    }

    fun removeFromRecent(trackId: Long) {
        viewModelScope.launch {
            try {
                recentlyPlayedRepository.removeFromRecentlyPlayed(trackId)
                android.util.Log.d("RecentlyPlayedVM", "Removed track $trackId from recent")
            } catch (e: Exception) {
                android.util.Log.e("RecentlyPlayedVM", "Error removing track", e)
            }
        }
    }

    fun toTrack(track: Track): Track {
        return track
    }
    fun clearAllRecent() {
        viewModelScope.launch {
            try {
                recentlyPlayedRepository.clearRecentlyPlayed()
                android.util.Log.d("RecentlyPlayedVM", "Cleared all recently played")
            } catch (e: Exception) {
                android.util.Log.e("RecentlyPlayedVM", "Error clearing recent", e)
            }
        }
    }
}