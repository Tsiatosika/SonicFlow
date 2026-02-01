package com.example.sonicflow.presentation.screen.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Artist
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
class ArtistViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadArtists()
    }

    fun loadArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                trackRepository.getAllTracks().collect { tracks ->
                    // Grouper les morceaux par artiste
                    val artistsMap = tracks.groupBy { it.artist }

                    // Créer la liste des artistes avec leurs morceaux
                    val artistsList = artistsMap.map { (artistName, artistTracks) ->
                        Artist(
                            name = artistName,
                            trackCount = artistTracks.size,
                            tracks = artistTracks.sortedBy { it.title }
                        )
                    }.sortedBy { it.name }

                    _artists.value = artistsList
                    android.util.Log.d("ArtistViewModel", "Loaded ${artistsList.size} artists")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ArtistViewModel", "Error loading artists", e)
                _error.value = "Failed to load artists: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun playArtistTracks(artist: Artist, startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ArtistViewModel", "Playing ${artist.name} tracks from index $startIndex")
                audioPlayerRepository.playTrackList(artist.tracks, startIndex)
            } catch (e: Exception) {
                android.util.Log.e("ArtistViewModel", "Error playing artist tracks", e)
                _error.value = "Failed to play: ${e.message}"
            }
        }
    }

    fun playAllArtistTracks(artist: Artist) {
        playArtistTracks(artist, 0)
    }

    fun clearError() {
        _error.value = null
    }
}