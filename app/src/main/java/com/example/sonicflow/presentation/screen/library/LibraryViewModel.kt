package com.example.sonicflow.presentation.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.FavoriteRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val audioPlayerRepository: AudioPlayerRepository,
    private val playlistRepository: PlaylistRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _favoriteTracks = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteTracks: StateFlow<Set<Long>> = _favoriteTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private var isInitialized = false
    private var isSearching = false

    init {
        observeTracks()
        observeFavorites()
    }

    private fun observeTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks().collect { trackList ->
                if (!isSearching) {
                    android.util.Log.d("LibraryViewModel", "Tracks updated: ${trackList.size}")
                    _tracks.value = trackList

                    if (!isInitialized && trackList.isEmpty()) {
                        isInitialized = true
                        android.util.Log.d("LibraryViewModel", "First load - DB empty, triggering scan")
                        refreshTracks()
                    } else if (!isInitialized) {
                        isInitialized = true
                    }
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavoriteTracks().collect { favoriteTracks ->
                _favoriteTracks.value = favoriteTracks.map { it.id }.toSet()
                android.util.Log.d("LibraryViewModel", "Favorites updated: ${favoriteTracks.size}")
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                playlistRepository.getAllPlaylists().collect { playlistList ->
                    _playlists.value = playlistList
                    android.util.Log.d("LibraryViewModel", "Playlists loaded: ${playlistList.size}")
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error loading playlists", e)
                _error.value = "Failed to load playlists: ${e.message}"
            }
        }
    }

    fun loadTracks() {
        refreshTracks()
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            isSearching = false

            try {
                android.util.Log.d("LibraryViewModel", "Starting track refresh...")
                trackRepository.refreshTracks()
                android.util.Log.d("LibraryViewModel", "Track refresh completed")
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error refreshing tracks", e)
                _error.value = "Failed to refresh: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchTracks(query: String) {
        viewModelScope.launch {
            try {
                isSearching = true
                android.util.Log.d("LibraryViewModel", "Searching for: $query")

                if (query.isNotEmpty()) {
                    trackRepository.searchTracks(query).collect { results ->
                        _tracks.value = results
                        android.util.Log.d("LibraryViewModel", "Search results: ${results.size}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Search error", e)
                _error.value = "Search failed: ${e.message}"
            }
        }
    }

    fun clearSearch() {
        viewModelScope.launch {
            android.util.Log.d("LibraryViewModel", "Clearing search")
            isSearching = false
            trackRepository.getAllTracks().collect { trackList ->
                _tracks.value = trackList
            }
        }
    }

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch {
            try {
                val isFavorite = _favoriteTracks.value.contains(trackId)

                if (isFavorite) {
                    android.util.Log.d("LibraryViewModel", "Removing track $trackId from favorites")
                    favoriteRepository.removeFromFavorites(trackId)
                    _successMessage.value = "Retiré des favoris"
                } else {
                    android.util.Log.d("LibraryViewModel", "Adding track $trackId to favorites")
                    favoriteRepository.addToFavorites(trackId)
                    _successMessage.value = "Ajouté aux favoris"
                }

                // Effacer le message après 2 secondes
                kotlinx.coroutines.delay(2000)
                _successMessage.value = null
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error toggling favorite", e)
                _error.value = "Erreur: ${e.message}"
            }
        }
    }

    fun isFavorite(trackId: Long): Boolean {
        return _favoriteTracks.value.contains(trackId)
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LibraryViewModel", "Adding track $trackId to playlist $playlistId")
                playlistRepository.addTrackToPlaylist(playlistId, trackId)

                loadPlaylists()

                _successMessage.value = "Morceau ajouté à la playlist"
                android.util.Log.d("LibraryViewModel", "Track added successfully")

                kotlinx.coroutines.delay(3000)
                _successMessage.value = null
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error adding track to playlist", e)
                _error.value = "Échec de l'ajout: ${e.message}"
            }
        }
    }

    fun createPlaylistAndAddTrack(playlistName: String, trackId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LibraryViewModel", "Creating playlist '$playlistName' and adding track $trackId")

                val playlistId = playlistRepository.createPlaylist(playlistName)
                android.util.Log.d("LibraryViewModel", "Playlist created with ID: $playlistId")

                playlistRepository.addTrackToPlaylist(playlistId, trackId)
                android.util.Log.d("LibraryViewModel", "Track added to new playlist")

                loadPlaylists()

                _successMessage.value = "Playlist '$playlistName' créée et morceau ajouté"

                kotlinx.coroutines.delay(3000)
                _successMessage.value = null
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error creating playlist", e)
                _error.value = "Échec de la création: ${e.message}"
            }
        }
    }

    fun playTrackFromList(selectedTrack: Track) {
        viewModelScope.launch {
            val allTracks = _tracks.value
            if (allTracks.isNotEmpty()) {
                val startIndex = allTracks.indexOfFirst { it.id == selectedTrack.id }
                if (startIndex >= 0) {
                    android.util.Log.d("LibraryViewModel", "Playing playlist from index $startIndex, total tracks: ${allTracks.size}")
                    playPlaylist(allTracks, startIndex)
                } else {
                    android.util.Log.e("LibraryViewModel", "Track not found in list")
                }
            }
        }
    }

    private suspend fun playPlaylist(tracks: List<Track>, startIndex: Int) {
        try {
            android.util.Log.d("LibraryViewModel", "Starting playlist with ${tracks.size} tracks at index $startIndex")
            audioPlayerRepository.playTrackList(tracks, startIndex)
        } catch (e: Exception) {
            android.util.Log.e("LibraryViewModel", "Error playing playlist", e)
            _error.value = "Failed to play: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}