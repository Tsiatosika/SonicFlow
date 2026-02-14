package com.example.sonicflow.presentation.screen.library

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.FavoriteRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackRepository: TrackRepository,
    private val playlistRepository: PlaylistRepository,
    private val favoriteRepository: FavoriteRepository,
    private val audioPlayerRepository: AudioPlayerRepository
) : ViewModel() {

    // Tracks
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())

    // Loading & Error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Playlists
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // Favorites
    private val _favoriteTracks = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteTracks: StateFlow<Set<Long>> = _favoriteTracks.asStateFlow()

    // Current playing track
    private val _currentPlayingTrack = MutableStateFlow<Track?>(null)
    val currentPlayingTrack: StateFlow<Track?> = _currentPlayingTrack.asStateFlow()

    // Success messages
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Track details dialog
    private val _showTrackDetailsDialog = MutableStateFlow(false)
    val showTrackDetailsDialog: StateFlow<Boolean> = _showTrackDetailsDialog.asStateFlow()

    private val _trackDetailsText = MutableStateFlow("")
    val trackDetailsText: StateFlow<String> = _trackDetailsText.asStateFlow()

    init {
        // 🔥 NE PAS CHARGER LES TRACKS ICI - Attendre l'appel explicite
        android.util.Log.d("LibraryViewModel", "ViewModel initialized - NOT loading tracks yet")
        loadPlaylists()
        observeFavorites()
        observeCurrentTrack()
    }

    // ========================================================================
    // TRACK LOADING
    // ========================================================================

    fun loadTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            android.util.Log.d("LibraryViewModel", "🔄 Starting to load tracks...")

            try {
                // 🔥 ÉTAPE 1 : Scanner MediaStore et remplir la base de données
                android.util.Log.d("LibraryViewModel", "📱 Scanning MediaStore...")
                trackRepository.refreshTracks()
                android.util.Log.d("LibraryViewModel", "✅ MediaStore scan complete")

                // 🔥 ÉTAPE 2 : Charger depuis Room Database
                trackRepository.getAllTracks().collect { trackList ->
                    _allTracks.value = trackList
                    _tracks.value = trackList
                    _isLoading.value = false
                    android.util.Log.d("LibraryViewModel", "✅ Loaded ${trackList.size} tracks")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load tracks: ${e.message}"
                _isLoading.value = false
                android.util.Log.e("LibraryViewModel", "❌ Error loading tracks", e)
            }
        }
    }

    fun refreshTracks() {
        android.util.Log.d("LibraryViewModel", "🔄 Refreshing tracks...")
        loadTracks()
    }

    // ========================================================================
    // SEARCH
    // ========================================================================

    fun searchTracks(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _tracks.value = _allTracks.value
                    return@launch
                }

                val filteredTracks = _allTracks.value.filter { track ->
                    track.title.contains(query, ignoreCase = true) ||
                            track.artist.contains(query, ignoreCase = true) ||
                            track.album.contains(query, ignoreCase = true)
                }

                _tracks.value = filteredTracks
                android.util.Log.d("LibraryViewModel", "Search '$query': ${filteredTracks.size} results")
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error searching tracks", e)
            }
        }
    }

    fun clearSearch() {
        _tracks.value = _allTracks.value
    }

    // ========================================================================
    // PLAYBACK
    // ========================================================================

    fun playTrackFromList(track: Track) {
        viewModelScope.launch {
            try {
                val currentTracks = _tracks.value
                val startIndex = currentTracks.indexOf(track)

                if (startIndex != -1) {
                    audioPlayerRepository.playTrackList(currentTracks, startIndex)
                    android.util.Log.d("LibraryViewModel", "Playing track: ${track.title}")
                } else {
                    audioPlayerRepository.playTrack(track)
                }
            } catch (e: Exception) {
                _error.value = "Failed to play track: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error playing track", e)
            }
        }
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            audioPlayerRepository.getCurrentPlayingTrack().collect { track ->
                _currentPlayingTrack.value = track
                if (track != null) {
                    android.util.Log.d("LibraryViewModel", "Current track: ${track.title}")
                }
            }
        }
    }

    // ========================================================================
    // PLAYLISTS
    // ========================================================================

    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                playlistRepository.getAllPlaylists().collect { playlistList ->
                    _playlists.value = playlistList
                    android.util.Log.d("LibraryViewModel", "Loaded ${playlistList.size} playlists")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load playlists: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error loading playlists", e)
            }
        }
    }

    fun createPlaylistAndAddTrack(playlistName: String, trackId: Long) {
        viewModelScope.launch {
            try {
                val playlistId = playlistRepository.createPlaylist(playlistName)
                playlistRepository.addTrackToPlaylist(playlistId, trackId)

                showSuccessMessage("Playlist '$playlistName' créée et morceau ajouté")
                android.util.Log.d("LibraryViewModel", "Created playlist: $playlistName")
            } catch (e: Exception) {
                _error.value = "Failed to create playlist: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error creating playlist", e)
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.addTrackToPlaylist(playlistId, trackId)

                val playlist = _playlists.value.find { it.id == playlistId }
                showSuccessMessage("Ajouté à ${playlist?.name ?: "la playlist"}")
                android.util.Log.d("LibraryViewModel", "Added track to playlist: $playlistId")
            } catch (e: Exception) {
                _error.value = "Failed to add track to playlist: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error adding track to playlist", e)
            }
        }
    }

    // ========================================================================
    // FAVORITES
    // ========================================================================

    private fun observeFavorites() {
        viewModelScope.launch {
            try {
                favoriteRepository.getFavoriteTracks().collect { favoriteTracks ->
                    _favoriteTracks.value = favoriteTracks.map { it.id }.toSet()
                    android.util.Log.d("LibraryViewModel", "Loaded ${favoriteTracks.size} favorites")
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error observing favorites", e)
            }
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            try {
                val isFavorite = _favoriteTracks.value.contains(track.id)

                if (isFavorite) {
                    favoriteRepository.removeFromFavorites(track.id)
                    showSuccessMessage("Retiré des favoris")
                    android.util.Log.d("LibraryViewModel", "Removed from favorites: ${track.id}")
                } else {
                    favoriteRepository.addToFavorites(track.id)
                    showSuccessMessage("Ajouté aux favoris ❤️")
                    android.util.Log.d("LibraryViewModel", "Added to favorites: ${track.id}")
                }
            } catch (e: Exception) {
                _error.value = "Erreur favoris: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error toggling favorite", e)
            }
        }
    }

    fun shareTrack(track: Track) {
        viewModelScope.launch {
            try {
                val shareText = buildString {
                    append("🎵 ${track.title}\n")
                    append("👤 ${track.artist}\n")
                    append("💿 ${track.album}\n")
                    append("\nÉcoute ce morceau avec SonicFlow!")
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "🎵 ${track.title}")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Partager via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(chooserIntent)
                android.util.Log.d("LibraryViewModel", "Shared track: ${track.title}")
            } catch (e: Exception) {
                _error.value = "Erreur de partage: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error sharing track", e)
            }
        }
    }

    fun showTrackDetails(track: Track) {
        viewModelScope.launch {
            try {
                val details = buildString {
                    appendLine("📀 INFORMATIONS DU MORCEAU\n")
                    appendLine("🎵 Titre")
                    appendLine("   ${track.title}\n")
                    appendLine("👤 Artiste")
                    appendLine("   ${track.artist}\n")
                    appendLine("💿 Album")
                    appendLine("   ${track.album}\n")
                    appendLine("⏱️ Durée")
                    appendLine("   ${formatDuration(track.duration)}\n")
                    appendLine("🆔 ID")
                    appendLine("   ${track.id}\n")
                    appendLine("📁 Emplacement")
                    appendLine("   ${track.uri}")
                }

                _trackDetailsText.value = details
                _showTrackDetailsDialog.value = true

                android.util.Log.d("LibraryViewModel", "Showing details for: ${track.title}")
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error showing details", e)
            }
        }
    }

    fun dismissTrackDetails() {
        _showTrackDetailsDialog.value = false
    }


    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            try {
                val track = _allTracks.value.find { it.id == trackId }
                if (track == null) {
                    _error.value = "Morceau introuvable"
                    return@launch
                }

                // Supprimer du MediaStore (Android 10+)
                val deleted = try {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        trackId
                    )

                    val rows = context.contentResolver.delete(uri, null, null)
                    rows > 0
                } catch (e: SecurityException) {
                    // Sur Android 10+, on pourrait avoir besoin d'une permission utilisateur
                    // Pour l'instant, on retire juste de la liste
                    android.util.Log.w("LibraryViewModel", "Security exception: ${e.message}")
                    false
                } catch (e: Exception) {
                    android.util.Log.e("LibraryViewModel", "Error deleting from MediaStore", e)
                    false
                }

                // Retirer de la liste locale
                val updatedAllTracks = _allTracks.value.filter { it.id != trackId }
                _allTracks.value = updatedAllTracks
                _tracks.value = _tracks.value.filter { it.id != trackId }

                if (deleted) {
                    showSuccessMessage("\"${track.title}\" supprimé définitivement")
                } else {
                    showSuccessMessage("\"${track.title}\" retiré de la bibliothèque")
                }

                android.util.Log.d("LibraryViewModel", "Deleted track: ${track.title}")
            } catch (e: Exception) {
                _error.value = "Erreur de suppression: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error deleting track", e)
            }
        }
    }


    private val _navigateToAlbum = MutableStateFlow<String?>(null)
    val navigateToAlbum: StateFlow<String?> = _navigateToAlbum.asStateFlow()

    fun goToAlbum(albumName: String) {
        viewModelScope.launch {
            try {
                _navigateToAlbum.value = albumName
                android.util.Log.d("LibraryViewModel", "Navigate to album: $albumName")
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error navigating to album", e)
            }
        }
    }

    fun clearAlbumNavigation() {
        _navigateToAlbum.value = null
    }


    private val _navigateToArtist = MutableStateFlow<String?>(null)
    val navigateToArtist: StateFlow<String?> = _navigateToArtist.asStateFlow()

    fun goToArtist(artistName: String) {
        viewModelScope.launch {
            try {
                _navigateToArtist.value = artistName
                android.util.Log.d("LibraryViewModel", "Navigate to artist: $artistName")
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
                android.util.Log.e("LibraryViewModel", "Error navigating to artist", e)
            }
        }
    }

    fun clearArtistNavigation() {
        _navigateToArtist.value = null
    }

    private fun formatDuration(millis: Long): String {
        if (millis < 0) return "0:00"

        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = millis / (1000 * 60 * 60)

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun showSuccessMessage(message: String) {
        viewModelScope.launch {
            _successMessage.value = message
            kotlinx.coroutines.delay(3000)
            _successMessage.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }
}