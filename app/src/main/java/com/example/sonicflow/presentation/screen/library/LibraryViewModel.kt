package com.example.sonicflow.presentation.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var isInitialized = false

    init {
        observeTracks()
        // Le scan initial sera déclenché après la première collection
    }

    private fun observeTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks().collect { trackList ->
                android.util.Log.d("LibraryViewModel", "Tracks updated: ${trackList.size}")
                _tracks.value = trackList

                // Scanner automatiquement si la DB est vide au premier chargement
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

    fun loadTracks() {
        // Force un refresh
        refreshTracks()
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

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
                if (query.isNotEmpty()) {
                    trackRepository.searchTracks(query).collect { results ->
                        _tracks.value = results
                    }
                } else {
                    // Revenir à tous les morceaux
                    trackRepository.getAllTracks().collect { trackList ->
                        _tracks.value = trackList
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Search error", e)
                _error.value = "Search failed: ${e.message}"
            }
        }
    }
}