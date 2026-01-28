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

    init {
        // Observer automatiquement les changements de la base de données
        observeTracks()
        // Scanner au démarrage si vide
        checkAndScanIfEmpty()
    }

    private fun observeTracks() {
        viewModelScope.launch {
            trackRepository.getAllTracks().collect { trackList ->
                android.util.Log.d("LibraryViewModel", "Tracks updated: ${trackList.size}")
                _tracks.value = trackList
            }
        }
    }

    private fun checkAndScanIfEmpty() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Vérifier le nombre de morceaux dans la DB
                val currentTracks = _tracks.value
                android.util.Log.d("LibraryViewModel", "Current tracks in DB: ${currentTracks.size}")

                if (currentTracks.isEmpty()) {
                    android.util.Log.d("LibraryViewModel", "Database empty, scanning files...")
                    trackRepository.refreshTracks()
                    android.util.Log.d("LibraryViewModel", "Scan completed")
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error during initial scan", e)
                _error.value = "Failed to load tracks: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTracks() {
        // Cette fonction relance juste le check
        checkAndScanIfEmpty()
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                android.util.Log.d("LibraryViewModel", "Manual refresh started...")
                trackRepository.refreshTracks()
                android.util.Log.d("LibraryViewModel", "Manual refresh completed")
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
                _error.value = "Search failed: ${e.message}"
            }
        }
    }
}