package com.example.sonicflow.presentation.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
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
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var isInitialized = false
    private var isSearching = false

    init {
        observeTracks()
    }

    private fun observeTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks().collect { trackList ->
                // Ne mettre à jour que si on n'est pas en mode recherche
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
            // Recharger tous les morceaux
            trackRepository.getAllTracks().collect { trackList ->
                _tracks.value = trackList
            }
        }
    }

    // Fonction pour lancer une playlist à partir d'un morceau sélectionné
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
}