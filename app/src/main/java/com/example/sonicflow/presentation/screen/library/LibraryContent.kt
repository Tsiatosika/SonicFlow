package com.example.sonicflow.presentation.screen.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sonicflow.domain.model.Album
import com.example.sonicflow.domain.model.Artist
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    viewModel: LibraryViewModel,
    onTrackClick: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,      // ✅ NOUVEAU
    onArtistClick: (Artist) -> Unit,    // ✅ NOUVEAU
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentPlayingTrack by viewModel.currentPlayingTrack.collectAsState()

    // Navigation states
    val navigateToAlbumName by viewModel.navigateToAlbum.collectAsState()
    val navigateToArtistName by viewModel.navigateToArtist.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTrackMenu by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Dialog de détails
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

    // ✅ VRAIE NAVIGATION VERS ALBUM
    LaunchedEffect(navigateToAlbumName) {
        navigateToAlbumName?.let { albumName ->
            // Créer l'objet Album à partir du nom
            val albumTracks = tracks.filter { it.album == albumName }
            if (albumTracks.isNotEmpty()) {
                val album = Album(
                    name = albumName,
                    artist = albumTracks.first().artist,
                    trackCount = albumTracks.size,
                    year = albumTracks.first().year,
                    tracks = albumTracks.sortedBy { it.trackNumber }
                )
                // Appeler le callback de navigation
                onAlbumClick(album)
                viewModel.clearAlbumNavigation()
            }
        }
    }

    // ✅ VRAIE NAVIGATION VERS ARTISTE
    LaunchedEffect(navigateToArtistName) {
        navigateToArtistName?.let { artistName ->
            // Créer l'objet Artist à partir du nom
            val artistTracks = tracks.filter { it.artist == artistName }
            if (artistTracks.isNotEmpty()) {
                val artist = Artist(
                    name = artistName,
                    trackCount = artistTracks.size,
                    tracks = artistTracks.sortedBy { it.title }
                )
                // Appeler le callback de navigation
                onArtistClick(artist)
                viewModel.clearArtistNavigation()
            }
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
                        TrackListSkeleton(itemCount = 10)
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
                            verticalArrangement = Arrangement.spacedBy(1.dp)
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

                            items(tracks, key = { it.id }) { track ->
                                AnimatedTrackItem(
                                    track = track,
                                    isCurrentlyPlaying = currentPlayingTrack?.id == track.id,
                                    onClick = {
                                        viewModel.playTrackFromList(track)
                                        onTrackClick(track)
                                    },
                                    onMoreClick = {
                                        selectedTrack = track
                                        showTrackMenu = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // MENU CONTEXTUEL COMPLET
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
                    viewModel.goToAlbum(selectedTrack!!.album)
                    showTrackMenu = false
                },
                onGoToArtist = {
                    viewModel.goToArtist(selectedTrack!!.artist)
                    showTrackMenu = false
                },
                onShare = {
                    viewModel.shareTrack(selectedTrack!!)
                    showTrackMenu = false
                },
                onDelete = {
                    showTrackMenu = false
                    showDeleteDialog = true
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

        // Dialog: Ajouter à une playlist
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

        // Dialog: Créer une playlist
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
                    selectedTrack = null
                }
            )
        }

        // Dialog: Confirmer suppression
        if (showDeleteDialog && selectedTrack != null) {
            DeleteTrackDialog(
                trackTitle = selectedTrack!!.title,
                onDismiss = {
                    showDeleteDialog = false
                    selectedTrack = null
                },
                onConfirm = {
                    viewModel.deleteTrack(selectedTrack!!.id)
                    showDeleteDialog = false
                    selectedTrack = null
                }
            )
        }

        // Snackbar de succès
        val successMessage by viewModel.successMessage.collectAsState()
        successMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(message)
            }
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            focusRequester.requestFocus()
        }
    }
}

// ============================================================================
// DIALOG DE DÉTAILS DU MORCEAU
// ============================================================================

@Composable
fun TrackDetailsDialog(
    detailsText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Détails du morceau",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = detailsText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

// ============================================================================
// MENU CONTEXTUEL COMPLET
// ============================================================================

@Composable
fun TrackContextMenu(
    track: Track,
    onDismiss: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAddToFavorites: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MenuOption(
                    icon = Icons.Default.Favorite,
                    text = "Ajouter aux favoris",
                    onClick = onAddToFavorites
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                MenuOption(
                    icon = Icons.Default.PlaylistAdd,
                    text = "Ajouter à une playlist",
                    onClick = onAddToPlaylist
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                MenuOption(
                    icon = Icons.Default.Album,
                    text = "Aller à l'album",
                    subtitle = track.album,
                    onClick = onGoToAlbum
                )

                MenuOption(
                    icon = Icons.Default.Person,
                    text = "Aller à l'artiste",
                    subtitle = track.artist,
                    onClick = onGoToArtist
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                MenuOption(
                    icon = Icons.Default.Share,
                    text = "Partager",
                    onClick = onShare
                )

                MenuOption(
                    icon = Icons.Default.Info,
                    text = "Détails du morceau",
                    onClick = onViewDetails
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                MenuOption(
                    icon = Icons.Default.Delete,
                    text = "Supprimer",
                    onClick = onDelete,
                    destructive = true
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
fun MenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    subtitle: String? = null,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (destructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (destructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
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

// ============================================================================
// DIALOG: Confirmer suppression
// ============================================================================

@Composable
fun DeleteTrackDialog(
    trackTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Supprimer le morceau ?") },
        text = {
            Column {
                Text("Voulez-vous vraiment supprimer :")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "\"$trackTitle\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cette action est irréversible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supprimer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

// ============================================================================
// SKELETON LOADERS
// ============================================================================

@Composable
fun TrackListSkeleton(
    itemCount: Int = 8,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(itemCount) {
            TrackItemSkeleton()
        }
    }
}

@Composable
fun TrackItemSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush(targetValue = 1000f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    showShimmer: Boolean = true
): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslation"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation, y = translateAnimation)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

// ============================================================================
// ANIMATED TRACK ITEM
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTrackItem(
    track: Track,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "trackScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrentlyPlaying)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "trackBackground"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
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
                    tint = if (isCurrentlyPlaying)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
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
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentlyPlaying)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// ============================================================================
// ÉTATS VIDES ET ERREURS
// ============================================================================

@Composable
fun ErrorState(
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
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptySearchState(searchQuery: String) {
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Essayez avec un autre terme",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun EmptyLibraryState(onScan: () -> Unit) {
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
        Text("No music found")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Make sure you have music files on your device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onScan) {
            Text("Scan for music")
        }
    }
}

// ============================================================================
// SEARCH BAR
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Close search")
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Rechercher...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
        }
    }
}

// ============================================================================
// DIALOGS (conservés)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
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
                    Text(
                        "Aucune playlist disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(playlists) { playlist ->
                            Card(
                                onClick = { onPlaylistSelected(playlist) },
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlaylistPlay,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${playlist.trackCount} morceaux",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
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
            Column {
                Text(
                    "Le morceau sera ajouté à cette playlist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = onNameChange,
                    label = { Text("Nom de la playlist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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