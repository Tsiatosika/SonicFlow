package com.example.sonicflow.presentation.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    private val _favoriteTracks = MutableStateFlow<List<Track>>(emptyList())
    val favoriteTracks: StateFlow<List<Track>> = _favoriteTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                favoriteRepository.getFavoriteTracks().collect { tracks ->
                    _favoriteTracks.value = tracks
                    android.util.Log.d("FavoritesViewModel", "Favorites loaded: ${tracks.size}")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error loading favorites", e)
                _isLoading.value = false
            }
        }
    }

    fun removeFromFavorites(trackId: Long) {
        viewModelScope.launch {
            try {
                android.util.Log.d("FavoritesViewModel", "Removing track $trackId from favorites")
                favoriteRepository.removeFromFavorites(trackId)
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error removing favorite", e)
            }
        }
    }

    fun playFavorites(startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                val tracks = _favoriteTracks.value
                if (tracks.isNotEmpty()) {
                    android.util.Log.d("FavoritesViewModel", "Playing favorites from index $startIndex")
                    audioPlayerRepository.playTrackList(tracks, startIndex)
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error playing favorites", e)
            }
        }
    }

    fun playAllFavorites() {
        playFavorites(0)
    }
}