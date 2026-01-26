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
open class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTracks()
    }

    fun loadTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                trackRepository.getAllTracks().collect { trackList ->
                    _tracks.value = trackList
                }
            } catch (e: Exception) {
                _error.value = "Failed to load tracks: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                trackRepository.refreshTracks()
                loadTracks() // Recharger après rafraîchissement
            } catch (e: Exception) {
                _error.value = "Failed to refresh: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchTracks(query: String) {
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                trackRepository.searchTracks(query).collect { results ->
                    _tracks.value = results
                }
            } else {
                loadTracks()
            }
        }
    }
}