package com.example.sonicflow.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylistWithTracks = MutableStateFlow<PlaylistRepository.PlaylistWithTracks?>(null)
    val currentPlaylistWithTracks: StateFlow<PlaylistRepository.PlaylistWithTracks?> = _currentPlaylistWithTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                playlistRepository.getAllPlaylists().collect { playlistList ->
                    _playlists.value = playlistList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load playlists: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun loadPlaylistWithTracks(playlistId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.getPlaylistWithTracks(playlistId).collect { playlistWithTracks ->
                    _currentPlaylistWithTracks.value = playlistWithTracks
                }
            } catch (e: Exception) {
                _error.value = "Failed to load playlist: ${e.message}"
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                val playlistId = playlistRepository.createPlaylist(name)
                android.util.Log.d("PlaylistViewModel", "Playlist created with ID: $playlistId")
            } catch (e: Exception) {
                _error.value = "Failed to create playlist: ${e.message}"
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.addTrackToPlaylist(playlistId, trackId)
                android.util.Log.d("PlaylistViewModel", "Track $trackId added to playlist $playlistId")
            } catch (e: Exception) {
                _error.value = "Failed to add track: ${e.message}"
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
                android.util.Log.d("PlaylistViewModel", "Track $trackId removed from playlist $playlistId")
            } catch (e: Exception) {
                _error.value = "Failed to remove track: ${e.message}"
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlistId)
                android.util.Log.d("PlaylistViewModel", "Playlist $playlistId deleted")
            } catch (e: Exception) {
                _error.value = "Failed to delete playlist: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}