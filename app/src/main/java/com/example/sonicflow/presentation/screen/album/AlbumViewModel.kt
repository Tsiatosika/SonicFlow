package com.example.sonicflow.presentation.screen.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Album
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.ALBUM_NAME)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    enum class SortOrder {
        ALBUM_NAME, ARTIST_NAME, YEAR_DESC, YEAR_ASC, TRACK_COUNT
    }

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                trackRepository.getAllTracks().collect { tracks ->
                    val albumsMap = tracks.groupBy { "${it.album}|${it.artist}" }
                    val albumsList = albumsMap.map { (key, albumTracks) ->
                        val albumName = key.split("|")[0]
                        val artistName = key.split("|")[1]
                        val year = albumTracks.firstOrNull()?.year ?: 0

                        Album(
                            name = albumName,
                            artist = artistName,
                            trackCount = albumTracks.size,
                            year = year,
                            tracks = albumTracks.sortedBy { it.trackNumber }
                        )
                    }
                    _albums.value = sortAlbums(albumsList, _sortOrder.value)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load albums: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getAlbumByName(albumName: String, artistName: String): Flow<Album?> = flow {
        trackRepository.getAllTracks().collect { tracks ->
            val albumTracks = tracks.filter {
                it.album == albumName && it.artist == artistName
            }

            if (albumTracks.isNotEmpty()) {
                val album = Album(
                    name = albumName,
                    artist = artistName,
                    trackCount = albumTracks.size,
                    year = albumTracks.firstOrNull()?.year ?: 0,
                    tracks = albumTracks.sortedBy { it.trackNumber }
                )
                emit(album)
            } else {
                emit(null)
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _albums.value = sortAlbums(_albums.value, order)
    }

    private fun sortAlbums(albums: List<Album>, order: SortOrder): List<Album> {
        return when (order) {
            SortOrder.ALBUM_NAME -> albums.sortedBy { it.name.lowercase() }
            SortOrder.ARTIST_NAME -> albums.sortedBy { it.artist.lowercase() }
            SortOrder.YEAR_DESC -> albums.sortedByDescending { it.year }
            SortOrder.YEAR_ASC -> albums.sortedBy { it.year }
            SortOrder.TRACK_COUNT -> albums.sortedByDescending { it.trackCount }
        }
    }

    fun playAlbumTracks(album: Album, startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                audioPlayerRepository.playTrackList(album.tracks, startIndex)
            } catch (e: Exception) {
                _error.value = "Failed to play: ${e.message}"
            }
        }
    }

    fun playAllAlbumTracks(album: Album) {
        playAlbumTracks(album, 0)
    }

    fun clearError() {
        _error.value = null
    }
}