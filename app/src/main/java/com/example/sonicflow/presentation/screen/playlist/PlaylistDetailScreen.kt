package com.example.sonicflow.presentation.screen.playlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonicflow.domain.model.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val playlistWithTracks by viewModel.currentPlaylistWithTracks.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddTrackDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistWithTracks(playlistId)
        viewModel.loadAllTracks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = playlistWithTracks?.playlist?.name ?: "Playlist",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTrackDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add tracks")
                    }
                }
            )
        },
        floatingActionButton = {
            if (playlistWithTracks?.tracks?.isNotEmpty() == true) {
                FloatingActionButton(
                    onClick = {
                        playlistWithTracks?.tracks?.let { tracks ->
                            viewModel.playPlaylist(tracks)
                            // Naviguer vers le player avec le premier morceau
                            onTrackClick(tracks.first())
                        }
                    }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play all")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                playlistWithTracks?.tracks?.isEmpty() == true -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Aucun morceau dans cette playlist")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Appuyez sur + pour ajouter des morceaux",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        // Header avec infos de la playlist
                        item {
                            PlaylistHeader(
                                playlistName = playlistWithTracks?.playlist?.name ?: "",
                                trackCount = playlistWithTracks?.tracks?.size ?: 0
                            )
                        }

                        // Liste des morceaux
                        playlistWithTracks?.tracks?.let { tracks ->
                            items(tracks, key = { it.id }) { track ->
                                PlaylistTrackItem(
                                    track = track,
                                    onClick = {
                                        // Jouer la playlist à partir de ce morceau
                                        viewModel.playPlaylist(tracks, tracks.indexOf(track))
                                        onTrackClick(track)
                                    },
                                    onRemoveClick = {
                                        trackToDelete = track
                                        showDeleteConfirmDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog pour ajouter des morceaux
    if (showAddTrackDialog) {
        AddTracksToPlaylistDialog(
            playlistId = playlistId,
            allTracks = allTracks,
            playlistTracks = playlistWithTracks?.tracks ?: emptyList(),
            onDismiss = { showAddTrackDialog = false },
            onTracksAdded = { selectedTracks ->
                selectedTracks.forEach { track ->
                    viewModel.addTrackToPlaylist(playlistId, track.id)
                }
                showAddTrackDialog = false
            }
        )
    }

    // Dialog de confirmation de suppression
    if (showDeleteConfirmDialog && trackToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Retirer le morceau") },
            text = {
                Text("Voulez-vous retirer \"${trackToDelete?.title}\" de cette playlist ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        trackToDelete?.let { track ->
                            viewModel.removeTrackFromPlaylist(playlistId, track.id)
                        }
                        showDeleteConfirmDialog = false
                        trackToDelete = null
                    }
                ) {
                    Text("Retirer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        trackToDelete = null
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun PlaylistHeader(
    playlistName: String,
    trackCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PlaylistPlay,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = playlistName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$trackCount morceau${if (trackCount > 1) "x" else ""}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistTrackItem(
    track: Track,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from playlist",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTracksToPlaylistDialog(
    playlistId: Long,
    allTracks: List<Track>,
    playlistTracks: List<Track>,
    onDismiss: () -> Unit,
    onTracksAdded: (List<Track>) -> Unit
) {
    val selectedTracks = remember { mutableStateListOf<Track>() }
    val playlistTrackIds = remember(playlistTracks) {
        playlistTracks.map { it.id }.toSet()
    }

    // Filtrer les morceaux qui ne sont pas déjà dans la playlist
    val availableTracks = remember(allTracks, playlistTrackIds) {
        allTracks.filter { it.id !in playlistTrackIds }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredTracks = remember(availableTracks, searchQuery) {
        if (searchQuery.isEmpty()) {
            availableTracks
        } else {
            availableTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter des morceaux") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Barre de recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Rechercher...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Compteur de sélection
                if (selectedTracks.isNotEmpty()) {
                    Text(
                        text = "${selectedTracks.size} morceau${if (selectedTracks.size > 1) "x" else ""} sélectionné${if (selectedTracks.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty())
                                "Tous les morceaux sont déjà dans la playlist"
                            else
                                "Aucun résultat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredTracks, key = { it.id }) { track ->
                            val isSelected = selectedTracks.contains(track)

                            Card(
                                onClick = {
                                    if (isSelected) {
                                        selectedTracks.remove(track)
                                    } else {
                                        selectedTracks.add(track)
                                    }
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null
                                    )

                                    Icon(
                                        Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTracksAdded(selectedTracks.toList())
                },
                enabled = selectedTracks.isNotEmpty()
            ) {
                Text("Ajouter${if (selectedTracks.isNotEmpty()) " (${selectedTracks.size})" else ""}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}