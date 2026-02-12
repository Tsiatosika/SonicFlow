package com.example.sonicflow.presentation.screen.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.presentation.components.ModernTrackItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    viewModel: LibraryViewModel,
    onTrackClick: (Track) -> Unit,
    onAlbumClick: (albumName: String, artistName: String) -> Unit,  // Navigation par Strings
    onArtistClick: (artistName: String) -> Unit,                    // Navigation par String
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentPlayingTrack by viewModel.currentPlayingTrack.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showTrackMenu by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val showTrackDetailsDialog by viewModel.showTrackDetailsDialog.collectAsState()
    val trackDetailsText by viewModel.trackDetailsText.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPlaylists()
    }

    LaunchedEffect(searchQuery) {
        delay(300)
        if (searchQuery.isNotEmpty()) {
            viewModel.searchTracks(searchQuery)
        } else {
            viewModel.clearSearch()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // SearchBar
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        onSearchActiveChange(false)
                        searchQuery = ""
                        viewModel.clearSearch()
                    },
                    focusRequester = focusRequester
                )
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        ErrorState(
                            error = error!!,
                            onRetry = { viewModel.loadTracks() }
                        )
                    }
                    tracks.isEmpty() && searchQuery.isNotEmpty() -> {
                        EmptySearchState(searchQuery = searchQuery)
                    }
                    tracks.isEmpty() -> {
                        EmptyLibraryState(
                            onScan = { viewModel.refreshTracks() }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                item {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "${tracks.size} résultat${if (tracks.size > 1) "s" else ""}",
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // MODERN TRACK ITEMS
                            items(tracks, key = { it.id }) { track ->
                                ModernTrackItem(
                                    track = track,
                                    isCurrentlyPlaying = currentPlayingTrack?.id == track.id,
                                    onClick = {
                                        viewModel.playTrackFromList(track)
                                        onTrackClick(track)
                                    },
                                    onMoreClick = {
                                        selectedTrack = track
                                        showTrackMenu = true
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }

                            // Spacer pour éviter que le dernier item soit caché par le MiniPlayer
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }

        // MENU CONTEXTUEL
        if (showTrackMenu && selectedTrack != null) {
            TrackContextMenu(
                track = selectedTrack!!,
                onDismiss = { showTrackMenu = false },
                onAddToPlaylist = {
                    showTrackMenu = false
                    showPlaylistDialog = true
                },
                onAddToFavorites = {
                    viewModel.toggleFavorite(selectedTrack!!.id)
                    showTrackMenu = false
                },
                onGoToAlbum = {
                    // Navigation par nom d'album + artiste
                    onAlbumClick(selectedTrack!!.album, selectedTrack!!.artist)
                    showTrackMenu = false
                },
                onGoToArtist = {
                    // Navigation par nom d'artiste
                    onArtistClick(selectedTrack!!.artist)
                    showTrackMenu = false
                },
                onViewDetails = {
                    viewModel.showTrackDetails(selectedTrack!!)
                    showTrackMenu = false
                }
            )
        }

        // Dialog: Détails du morceau
        if (showTrackDetailsDialog) {
            TrackDetailsDialog(
                detailsText = trackDetailsText,
                onDismiss = { viewModel.dismissTrackDetails() }
            )
        }

        // Dialog: Ajouter à playlist
        if (showPlaylistDialog && selectedTrack != null) {
            AddToPlaylistDialog(
                track = selectedTrack!!,
                playlists = playlists,
                onDismiss = { showPlaylistDialog = false },
                onPlaylistSelected = { playlist ->
                    viewModel.addTrackToPlaylist(playlist.id, selectedTrack!!.id)
                    showPlaylistDialog = false
                },
                onCreateNew = {
                    showPlaylistDialog = false
                    showCreatePlaylistDialog = true
                }
            )
        }

        // Dialog: Créer nouvelle playlist
        if (showCreatePlaylistDialog && selectedTrack != null) {
            CreatePlaylistDialog(
                playlistName = newPlaylistName,
                onNameChange = { newPlaylistName = it },
                onDismiss = {
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                },
                onConfirm = {
                    viewModel.createPlaylistAndAddTrack(newPlaylistName, selectedTrack!!.id)
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }
            )
        }
    }
}

// ============================================================================
// COMPOSANTS UI
// ============================================================================

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}

@Composable
private fun EmptySearchState(searchQuery: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aucun résultat pour \"$searchQuery\"")
    }
}

@Composable
private fun EmptyLibraryState(onScan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Bibliothèque vide")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Scannez votre appareil pour trouver de la musique",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onScan) {
            Text("Scanner")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close")
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Rechercher...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
        }
    }
}

// ============================================================================
// DIALOGS
// ============================================================================

@Composable
fun TrackContextMenu(
    track: Track,
    onDismiss: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAddToFavorites: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onViewDetails: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Options")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column {
                MenuOption(
                    icon = Icons.Default.PlaylistAdd,
                    text = "Ajouter à une playlist",
                    onClick = onAddToPlaylist
                )
                MenuOption(
                    icon = Icons.Default.Favorite,
                    text = "Ajouter aux favoris",
                    onClick = onAddToFavorites
                )
                MenuOption(
                    icon = Icons.Default.Album,
                    text = "Aller à l'album",
                    onClick = onGoToAlbum
                )
                MenuOption(
                    icon = Icons.Default.Person,
                    text = "Aller à l'artiste",
                    onClick = onGoToArtist
                )
                MenuOption(
                    icon = Icons.Default.Info,
                    text = "Détails",
                    onClick = onViewDetails
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun TrackDetailsDialog(
    detailsText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Détails du morceau") },
        text = {
            Text(
                text = detailsText,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    track: Track,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreateNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Ajouter à une playlist")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                if (playlists.isEmpty()) {
                    Text("Aucune playlist disponible")
                } else {
                    playlists.forEach { playlist ->
                        MenuOption(
                            icon = Icons.Default.PlaylistPlay,
                            text = "${playlist.name} (${playlist.trackCount})",
                            onClick = { onPlaylistSelected(playlist) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCreateNew,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Créer une nouvelle playlist")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun CreatePlaylistDialog(
    playlistName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle playlist") },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = onNameChange,
                label = { Text("Nom de la playlist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = playlistName.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}