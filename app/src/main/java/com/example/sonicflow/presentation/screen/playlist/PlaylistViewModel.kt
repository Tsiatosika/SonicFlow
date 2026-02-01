package com.example.sonicflow.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: TrackRepository,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylistWithTracks = MutableStateFlow<PlaylistRepository.PlaylistWithTracks?>(null)
    val currentPlaylistWithTracks: StateFlow<PlaylistRepository.PlaylistWithTracks?> = _currentPlaylistWithTracks.asStateFlow()

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                playlistRepository.getAllPlaylists().collect { playlistList ->
                    _playlists.value = playlistList
                    android.util.Log.d("PlaylistViewModel", "Playlists loaded: ${playlistList.size}")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error loading playlists", e)
                _error.value = "Failed to load playlists: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun loadPlaylistWithTracks(playlistId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("PlaylistViewModel", "Loading playlist with ID: $playlistId")
                playlistRepository.getPlaylistWithTracks(playlistId).collect { playlistWithTracks ->
                    _currentPlaylistWithTracks.value = playlistWithTracks
                    android.util.Log.d("PlaylistViewModel", "Playlist loaded: ${playlistWithTracks.playlist.name}, tracks: ${playlistWithTracks.tracks.size}")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error loading playlist", e)
                _error.value = "Failed to load playlist: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun loadAllTracks() {
        viewModelScope.launch {
            try {
                trackRepository.getAllTracks().collect { tracks ->
                    _allTracks.value = tracks
                    android.util.Log.d("PlaylistViewModel", "All tracks loaded: ${tracks.size}")
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error loading all tracks", e)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                val playlistId = playlistRepository.createPlaylist(name)
                android.util.Log.d("PlaylistViewModel", "Playlist created with ID: $playlistId")
                loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error creating playlist", e)
                _error.value = "Failed to create playlist: ${e.message}"
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PlaylistViewModel", "Adding track $trackId to playlist $playlistId")
                playlistRepository.addTrackToPlaylist(playlistId, trackId)

                // Recharger la playlist actuelle si c'est celle qui est affichée
                if (_currentPlaylistWithTracks.value?.playlist?.id == playlistId) {
                    loadPlaylistWithTracks(playlistId)
                }

                // Recharger toutes les playlists pour mettre à jour les compteurs
                loadPlaylists()

                android.util.Log.d("PlaylistViewModel", "Track added successfully")
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error adding track to playlist", e)
                _error.value = "Failed to add track: ${e.message}"
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PlaylistViewModel", "Removing track $trackId from playlist $playlistId")
                playlistRepository.removeTrackFromPlaylist(playlistId, trackId)

                // Recharger la playlist
                loadPlaylistWithTracks(playlistId)
                loadPlaylists()

                android.util.Log.d("PlaylistViewModel", "Track removed successfully")
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error removing track", e)
                _error.value = "Failed to remove track: ${e.message}"
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PlaylistViewModel", "Deleting playlist $playlistId")
                playlistRepository.deletePlaylist(playlistId)
                loadPlaylists()
                android.util.Log.d("PlaylistViewModel", "Playlist deleted")
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error deleting playlist", e)
                _error.value = "Failed to delete playlist: ${e.message}"
            }
        }
    }

    fun playPlaylist(tracks: List<Track>, startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                if (tracks.isNotEmpty()) {
                    android.util.Log.d("PlaylistViewModel", "Playing playlist: ${tracks.size} tracks, starting at $startIndex")
                    audioPlayerRepository.playTrackList(tracks, startIndex)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error playing playlist", e)
                _error.value = "Failed to play: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}